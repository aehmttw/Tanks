package tanks.minigames.controlpoint;

import tanks.Drawing;
import tanks.Game;
import tanks.Team;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.EventControlPointCaptureProgressUpdate;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class GamePoint
{
    private final String id;

    private final double width;
    private final double height;
    private final double centerX;
    private final double centerY;

    private double captureProgress = 0.0; // -1.0: Red, 1.0: Blue
    private long lastCheckTime = 0;

    private final List<Tank> tanksInPoint = new CopyOnWriteArrayList<>();

    private Team controllingTeam = null;
    private final Team teamRed;
    private final Team teamBlue;

    public GamePoint(String id, double width, double height, double centerX, double centerY, Team teamRed, Team teamBlue)
    {
        this.id = id;
        this.width = width;
        this.height = height;
        this.centerX = centerX;
        this.centerY = centerY;
        this.teamRed = teamRed;
        this.teamBlue = teamBlue;
    }

    public void update()
    {
        Team oldControlling = controllingTeam;

        if (ScreenPartyHost.isServer)
        {
            int redTankCount = 0, blueTankCount = 0;

            List<Tank> tanksCopy = new ArrayList<>(tanksInPoint);
            for (Tank tank : tanksCopy)
            {
                if (tank.destroy)
                {
                    tanksInPoint.remove(tank);
                } else
                {
                    if (tank.team.name.equals("red"))
                    {
                        redTankCount++;
                    } else if (tank.team.name.equals("blue"))
                    {
                        blueTankCount++;
                    }
                }
            }

            long currTime = System.currentTimeMillis();
            if (currTime - lastCheckTime > 200)
            {
                if (redTankCount > blueTankCount)
                {
                    if (captureProgress > -1.0)
                    {
                        captureProgress -= (0.008 * (redTankCount - blueTankCount));
                        if (captureProgress < -1.0)
                        {
                            captureProgress = -1.0;
                        }
                    }
                }
                else if (redTankCount < blueTankCount)
                {
                    if (captureProgress < 1.0)
                    {
                        captureProgress += (0.008 * (blueTankCount - redTankCount));
                        if (captureProgress > 1.0)
                        {
                            captureProgress = 1.0;
                        }
                    }
                }

                Team newControlling = null;
                if (captureProgress <= -1.0)
                {
                    newControlling = teamRed;
                }
                else if (captureProgress >= 1.0)
                {
                    newControlling = teamBlue;
                }

                controllingTeam = newControlling;
                Game.eventsOut.add(new EventControlPointCaptureProgressUpdate(id, captureProgress));
                lastCheckTime = currTime;
            }
        }

        Team newControlling;
        if (captureProgress <= -1.0)
        {
            newControlling = teamRed;
        }
        else if (captureProgress >= 1.0)
        {
            newControlling = teamBlue;
        }
        else
        {
            newControlling = null;
        }

        if (Game.player != null && Game.player.tank != null && !Game.player.tank.destroy)
        {
            boolean controlChanged =
                oldControlling == null && newControlling != null
                || oldControlling != null && newControlling == null
                || oldControlling != null && !oldControlling.equals(newControlling);

            if (controlChanged && newControlling != null &&
                newControlling.equals(Game.player.tank.team))
            {
                Drawing.drawing.playSound("bonus1.ogg");
            }
        }

        controllingTeam = newControlling;
    }

    /**
     * Check if a tank is within the range of this point.
     * just checks the range, does not return whether it is actually at the point.
     *
     * @param tank tank to checked.
     * @return is in point range?
     */
    public boolean checkTankInPointRange(Tank tank)
    {
        double tankX = tank.posX;
        double tankY = tank.posY;

        double halfWidth = width / 2.0;
        double halfHeight = height / 2.0;

        return tankX >= (centerX - halfWidth) &&
            tankX <= (centerX + halfWidth) &&
            tankY >= (centerY - halfHeight) &&
            tankY <= (centerY + halfHeight);
    }

    /**
     * Add a tank to this point.
     *
     * @param tank tank to added.
     */
    public void addTank(Tank tank)
    {
        if (tanksInPoint.contains(tank)) return;
        tanksInPoint.add(tank);
    }

    /**
     * Remove a tank from this point.
     *
     * @param tank tank to removed.
     */
    public void removeTank(Tank tank)
    {
        tanksInPoint.remove(tank);
    }

    /**
     * Get tanks in this point.
     * read only.
     *
     * @return tanks arrayList
     */
    public List<Tank> getTanksInPoint()
    {
        return tanksInPoint;
    }

    /**
     * Get this point's center x location
     *
     * @return x location
     */
    public double getCenterX()
    {
        return centerX;
    }

    /**
     * Get this point's center y location
     *
     * @return y location
     */
    public double getCenterY()
    {
        return centerY;
    }

    /**
     * Get this point's height
     *
     * @return height
     */
    public double getHeight()
    {
        return height;
    }

    /**
     * Get this point's width
     *
     * @return height
     */
    public double getWidth()
    {
        return width;
    }

    public double getCaptureProgress()
    {
        return captureProgress;
    }

    public String getId()
    {
        return id;
    }

    public void clear()
    {
        tanksInPoint.clear();
    }

    public Team getControllingTeam()
    {
        return controllingTeam;
    }

    public void setCaptureProgress(double captureProgress)
    {
        this.captureProgress = captureProgress;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        GamePoint point = (GamePoint) o;
        return Double.compare(width, point.width) == 0 && Double.compare(height, point.height) == 0 && Double.compare(centerX, point.centerX) == 0 && Double.compare(centerY, point.centerY) == 0 && Double.compare(captureProgress, point.captureProgress) == 0 && lastCheckTime == point.lastCheckTime && Objects.equals(id, point.id) && Objects.equals(tanksInPoint, point.tanksInPoint) && Objects.equals(controllingTeam, point.controllingTeam) && Objects.equals(teamRed, point.teamRed) && Objects.equals(teamBlue, point.teamBlue);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, width, height, centerX, centerY, captureProgress, lastCheckTime, tanksInPoint, controllingTeam, teamRed, teamBlue);
    }

    @Override
    public String toString()
    {
        return "GamePoint{" +
            "id='" + id + '\'' +
            ", width=" + width +
            ", height=" + height +
            ", centerX=" + centerX +
            ", centerY=" + centerY +
            ", captureProgress=" + captureProgress +
            ", lastCheckTime=" + lastCheckTime +
            ", tanksInPoint=" + tanksInPoint +
            ", controllingTeam=" + controllingTeam +
            ", teamRed=" + teamRed +
            ", teamBlue=" + teamBlue +
            '}';
    }
}

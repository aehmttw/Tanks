package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

import java.util.ArrayList;

public class BulletWhistlingBird extends Bullet
{
    public static String bullet_name = "whistling bird";
    public static int targetIndex = 0;
    public static ArrayList<Tank> closestTanks = new ArrayList<>();
    public static ArrayList<Double> distances = new ArrayList<>();

    private double refreshCounter = 50;
    private boolean fired = false;

    public BulletWhistlingBird(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
    }

    public BulletWhistlingBird(double x, double y, int bounces, Tank t)
    {
        this(x, y, bounces, t, true, null);
    }

    public BulletWhistlingBird(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) {
        this(x, y, bounces, t, true, ib);
    }

    @Override
    public void update()
    {
        super.update();

        if (this.age < 200)
            this.setPolarMotion(this.getAngleInDirection(this.tank.posX, this.tank.posY) + Math.PI / (2 + Panel.frameFrequency / 5), 12);
        else
            this.launchBullet();

        if (this.age > 600)
            this.destroy = true;

        refreshCounter -= Panel.frameFrequency;
        if (refreshCounter <= 0)
        {
            closestTanks.clear();
            distances.clear();
            refreshCounter = 50;
        }
    }

    public void launchBullet()
    {
        if (fired || ScreenGame.finishedQuick)
            return;

        if (closestTanks.size() == 0)
        {
            for (Tank t : Tank.idMap.values())
            {
                if (!Team.isAllied(this.tank, t))
                {
                    boolean added = false;
                    double distance = Movable.distanceBetween(this, t);
                    for (int i = 0; i < closestTanks.size(); i++)
                    {
                        if (distance < distances.get(i))
                        {
                            closestTanks.add(i, t);
                            distances.add(i, distance);
                            added = true;
                            break;
                        }
                    }

                    if (!added) {
                        closestTanks.add(t);
                        distances.add(distance);
                    }
                }
            }
        }

        if (targetIndex < closestTanks.size())
            this.setMotionInDirection(closestTanks.get(targetIndex).posX, closestTanks.get(targetIndex).posY, this.item.speed);
        else
            this.setMotionInDirectionWithOffset(closestTanks.get(0).posX, closestTanks.get(0).posY, this.item.speed, 0.2);

        fired = true;
        targetIndex++;
    }

    @Override
    public void collidedWithTank(Tank t)
    {
        if (t == this.tank)
            return;

        super.collidedWithTank(t);
        int index = closestTanks.indexOf(t);

        if (index > -1) {
            closestTanks.remove(index);
            distances.remove(index);
        }

        this.destroy = true;
    }

    @Override
    public void collidedWithObject(Movable o)
    {
        if (!(o instanceof BulletWhistlingBird && Team.isAllied(this, o)))
            super.collidedWithObject(o);
    }

    @Override
    public void onDestroy() {
        targetIndex = 0;
    }
}

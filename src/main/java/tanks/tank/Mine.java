package tanks.tank;

import tanks.*;
import tanks.network.event.EventMineChangeTimer;
import tanks.network.event.EventMineRemove;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.ItemMine;

import java.util.ArrayList;
import java.util.HashMap;

public class Mine extends Movable implements IAvoidObject
{
    public static double mine_size = 30;
    public static double mine_radius = Game.tile_size * 2.5;

    public double timer;
    public double size = mine_size;
    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;
    public double height = 0;

    public double triggeredTimer = 50;
    public double damage = 2;
    public boolean destroysObstacles = true;

    public double radius = mine_radius;
    public Tank tank;
    public ItemMine item;
    public double cooldown = 0;
    public int lastBeep = Integer.MAX_VALUE;

    public int networkID = -1;

    public static int currentID = 0;
    public static ArrayList<Integer> freeIDs = new ArrayList<>();
    public static HashMap<Integer, Mine> idMap = new HashMap<>();

    public Mine(double x, double y, double timer, Tank t, ItemMine item)
    {
        super(x, y);

        if (t != null)
            this.posZ = t.posZ;

        this.timer = timer;
        this.drawLevel = 2;
        tank = t;

        this.item = item;

        if (!ScreenPartyLobby.isClient)
        {
            this.item.liveMines++;
        }

        this.team = t.team;
        double[] outlineCol = Team.getObjectColor(t.colorR, t.colorG, t.colorB, t);
        this.outlineColorR = outlineCol[0];
        this.outlineColorG = outlineCol[1];
        this.outlineColorB = outlineCol[2];

        if (!ScreenPartyLobby.isClient)
        {
            if (freeIDs.size() > 0)
                this.networkID = freeIDs.remove(0);
            else
            {
                this.networkID = currentID;
                currentID++;
            }

            idMap.put(this.networkID, this);
        }

        for (IFixedMenu m : ModAPI.menuGroup)
        {
            if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.mines_placed))
            {
                if (((Scoreboard) m).players.isEmpty())
                    ((Scoreboard) m).addTeamScore(this.team, 1);

                else if (this.tank instanceof TankPlayer)
                    ((Scoreboard) m).addPlayerScore(((TankPlayer) this.tank).player, 1);

                else if (this.tank instanceof TankPlayerRemote)
                    ((Scoreboard) m).addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
            }
        }
    }

    public Mine(double x, double y, Tank t, ItemMine im)
    {
        this(x, y, 1000, t, im);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 0.5);

        if (Game.enable3d && Game.enable3dBg && Game.fancyTerrain)
        {
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY + this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY + this.size / 2));
        }

        if (Game.enable3d)
        {
            for (double i = height; i < height + 6; i++)
            {
                double frac = ((i - height + 1) / 6 + 1) / 2;
                Drawing.drawing.setColor(this.outlineColorR * frac, this.outlineColorG  * frac, this.outlineColorB * frac, 255, 0.5);
                Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + i + 1.5, this.size, this.size, true, false);
            }

            Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 1);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ + height + 1, this.size * 4, this.size * 4, true, false);
        }
        else
        {
            Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4, this.size * 4);
        }

        Drawing.drawing.setColor(255, Math.min(1000, this.timer) / 1000.0 * 255, 0, 255, 0.5);

        if (timer < 150 && ((int) timer % 20) / 10 == 1)
            Drawing.drawing.setColor(255, 255, 0, 255, 0.5);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + height + 7.5, this.size * 0.8, this.size * 0.8, true, false);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, this.size * 0.8, this.size * 0.8);
    }

    @Override
    public void update()
    {
        this.timer -= Panel.frameFrequency;

        if (this.timer < 0)
            this.timer = 0;

        if ((this.timer <= 0 || destroy) && !ScreenPartyLobby.isClient)
            this.explode();

        int beepTime = ((int)this.timer / 10);
        if (this.timer <= 150 && beepTime % 2 == 1 && this.lastBeep != beepTime && this.tank == Game.playerTank)
        {
            Drawing.drawing.playSound("beep.ogg", 1f);
            this.lastBeep = beepTime;
        }

        super.update();

        boolean enemyNear = false;
        boolean allyNear = false;
        for (Movable m: Game.movables)
        {
            if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(radius, 2))
            {
                if (m instanceof Tank && !m.destroy && ((Tank) m).targetable)
                {
                    if (Team.isAllied(m, this.tank))
                        allyNear = true;
                    else
                        enemyNear = true;
                }
            }
        }

        if (enemyNear && !allyNear && this.timer > this.triggeredTimer && !this.isRemote)
        {
            this.timer = this.triggeredTimer;
            Game.eventsOut.add(new EventMineChangeTimer(this));
        }
    }

    public void explode()
    {
        Game.eventsOut.add(new EventMineRemove(this));
        Game.removeMovables.add(this);

        if (!ScreenPartyLobby.isClient)
        {
            freeIDs.add(this.networkID);
            idMap.remove(this.networkID);

            Explosion e = new Explosion(this);
            e.explode();

            this.item.liveMines--;
        }
    }

    @Override
    public double getRadius()
    {
        return this.radius;
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return this.timer;
    }
}

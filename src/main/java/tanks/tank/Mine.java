package tanks.tank;

import tanks.*;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenEditorBullet;
import tanks.gui.screen.ScreenEditorMine;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.ItemMine;
import tanks.network.event.EventMineChangeTimer;
import tanks.network.event.EventMineRemove;
import tanks.tankson.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@TanksONable("mine")
public class Mine extends Movable implements IAvoidObject, IDrawableLightSource, ICopyable<Mine>, ITanksONEditable
{
    public static double mine_size = 30;
    public static double mine_radius = Game.tile_size * 2.25;

    @Property(id = "explosion", name = "Explosion")
    public Explosion explosion = new Explosion();

    @Property(id = "timer", name = "Fuse length", desc = "The mine will explode this much time after it is placed \n \n 1 time unit = 0.01 seconds")
    public double timer = 1000;

    @Property(id = "size", name = "Size")
    public double size = mine_size;

    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;
    public double height = 0;

    @Property(id = "triggered_timer", name = "Triggered fuse length", desc = "If an enemy tank is within this mine's radius, its fuse will be shortened to this length \n \n 1 time unit = 0.01 seconds")
    public double triggeredTimer = 50;

    public Tank tank;
    public ItemMine.ItemStackMine item;
    public int lastBeep = Integer.MAX_VALUE;

    @Property(id = "max_live_mines", name = "Max live mines", desc = "The maximum number of this mine placed by one tank that can be onscreen at a time")
    public int maxLiveMines = 2;

    public int networkID = -1;

    public static int currentID = 0;
    public static ArrayList<Integer> freeIDs = new ArrayList<>();
    public static HashMap<Integer, Mine> idMap = new HashMap<>();

    public double[] lightInfo = new double[]{0, 0, 0, 0, 0, 0, 0};

    /**
     * Do not use if you plan to place this mine in the game field. Only for templates.
     * Use another constructor if you want to add the mine to the game field.
     */
    public Mine()
    {
        super(0, 0);
    }

    public Mine(double x, double y, double timer, Tank t, ItemMine.ItemStackMine item)
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

    public Mine(double x, double y, Tank t, ItemMine.ItemStackMine im)
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
                Drawing.drawing.setColor(this.outlineColorR * frac, this.outlineColorG * frac, this.outlineColorB * frac, 255, 0.5);
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

//        if (this.tankKnockback > 0 || this.bulletKnockback > 0)
//        {
//            Drawing.drawing.setColor(255, 0, 255, 64);
//            drawRange2D(this.posX, this.posY, this.knockbackRadius);
//        }
//
//        if (this.damage > 0)
//        {
//            Drawing.drawing.setColor(255, 0, 0, 64);
//            drawRange2D(this.posX, this.posY, this.radius);
//        }
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
            if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(this.explosion.radius, 2))
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
        return this.explosion.radius;
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return this.timer;
    }

    public static void drawRange(double posX, double posY, double size)
    {
        int faces = (int) (size + 5);
        double r = Drawing.drawing.currentColorR;
        double g = Drawing.drawing.currentColorG;
        double b = Drawing.drawing.currentColorB;
        double a = Drawing.drawing.currentColorA;

        Game.game.window.shapeRenderer.setBatchMode(true, true, true, false, false);
        for (int f = 0; f < faces; f++)
        {
            for (int i = 0; i <= 10; i++)
            {
                double hfrac = i / 10.0;
                double hfrac2 = (i + 1) / 10.0;
                double height = Math.sin(hfrac);
                double pitchMul = Math.cos(hfrac);
                double height2 = Math.sin(hfrac2);
                double pitchMul2 = Math.cos(hfrac2);

                double angle = f * Math.PI * 2 / faces;
                double angle2 = (f + 1) * Math.PI * 2 / faces;
                Drawing.drawing.setColor(r, g, b, (1 - hfrac) * a, 1);
                Drawing.drawing.addVertex(posX + Math.cos(angle) * size * pitchMul, posY + Math.sin(angle) * size * pitchMul, height * size);
                Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * pitchMul, posY + Math.sin(angle2) * size * pitchMul, height * size);
                Drawing.drawing.setColor(r, g, b, (1 - hfrac2) * a, 1);
                Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * pitchMul2, posY + Math.sin(angle2) * size * pitchMul2, height2 * size);
                Drawing.drawing.addVertex(posX + Math.cos(angle) * size * pitchMul2, posY + Math.sin(angle) * size * pitchMul2, height2 * size);
            }
        }
        Game.game.window.shapeRenderer.setBatchMode(false, true, false);
    }

    public static void drawRange2D(double posX, double posY, double size)
    {
        drawRange2D(posX, posY, size, false);
    }

    public static void drawRange2D(double posX, double posY, double size, boolean inverted)
    {
        int faces = (int) (size + 5);
        double r = Drawing.drawing.currentColorR;
        double g = Drawing.drawing.currentColorG;
        double b = Drawing.drawing.currentColorB;
        double a = Drawing.drawing.currentColorA;

        Game.game.window.shapeRenderer.setBatchMode(true, true, false);
        for (int f = 0; f < faces; f++)
        {
            double angle = f * Math.PI * 2 / faces;
            double angle2 = (f + 1) * Math.PI * 2 / faces;
            double inner = Math.max(0.5, (size - Game.tile_size * 2) / size);

            if (inverted)
                inner = 2 - inner;

            Drawing.drawing.setColor(r, g, b, a, 1);
            Drawing.drawing.addVertex(posX + Math.cos(angle) * size, posY + Math.sin(angle) * size, 0);
            Drawing.drawing.addVertex(posX + Math.cos(angle2) * size, posY + Math.sin(angle2) * size, 0);
            Drawing.drawing.setColor(r, g, b, 0, 1);
            Drawing.drawing.addVertex(posX + Math.cos(angle2) * size * inner, posY + Math.sin(angle2) * size * inner, 0);
            Drawing.drawing.addVertex(posX + Math.cos(angle) * size * inner, posY + Math.sin(angle) * size * inner, 0);
        }
        Game.game.window.shapeRenderer.setBatchMode(false, true, false);

        Drawing.drawing.setColor(r, g, b, a);
    }

    @Override
    public boolean lit()
    {
        return Game.fancyLights;
    }

    @Override
    public double[] getLightInfo()
    {
        this.lightInfo[3] = 2;

        this.lightInfo[4] = this.outlineColorR;
        this.lightInfo[5] = this.outlineColorG;
        this.lightInfo[6] = this.outlineColorB;
        return this.lightInfo;
    }

    @Override
    public String toString()
    {
        return Serializer.toTanksON(this);
    }

    @Override
    public String getName()
    {
        return "Mine";
    }
}

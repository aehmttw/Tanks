package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.modapi.ModAPI;
import tanks.modapi.ModLevel;
import tanks.tank.Tank;

public class ObstacleWater extends Obstacle
{
    public static final int drownTime = 4000;

    public ObstacleWater(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;

        this.isSurfaceTile = false;
        this.batchDraw = false;

        if (Game.enable3d)
            this.drawLevel = 6;
        else
            this.drawLevel = 1;

        if (Game.fancyTerrain)
        {
            this.colorR = 50;
            this.colorG = 100;
            this.colorB = 255 - Math.random() * 45;
            this.colorA = 175;
        }
        else
        {
            this.colorR = 50;
            this.colorG = 100;
            this.colorB = 255;
        }

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = this.colorR;
            this.stackColorB[i] = this.colorB;
            this.stackColorG[i] = this.colorG;
        }

        this.description = "A pool of water that can slow and drown tanks";
    }


    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank)
        {
            Tank t = (Tank) m;

            AttributeModifier a = new AttributeModifier("water", "velocity", AttributeModifier.Operation.multiply, -0.5);
            a.duration = 30;
            a.deteriorationAge = 20;
            t.addUnduplicateAttribute(a);

            if (!Game.enable3dBg)
                return;

            if (t.posZ < -Game.tile_size)
                t.waterEnterTime = Math.min(t.waterEnterTime + Panel.frameFrequency, drownTime);
            else
                t.waterEnterTime = Math.max(t.waterEnterTime - Panel.frameFrequency, 0);

            if (this.stackHeight > 1 && t.waterEnterTime >= drownTime && t.health > 0)
            {
                boolean kill = t.damage(Panel.frameFrequency / 2500, this);

                if (kill && ScreenPartyHost.isServer)
                {
                    String message = null;

                    if (Game.currentGame != null && Game.currentGame.enableKillMessages)
                        message = Game.currentGame.generateDrownMessage(t);

                    else if (Game.currentLevel instanceof ModLevel && ((ModLevel) Game.currentLevel).enableKillMessages)
                        message = ((ModLevel) Game.currentLevel).generateDrownMessage(t);

                    else if (((ModLevel) Game.currentLevel).enableKillMessages)
                        message = Level.genDrownMessage(t);

                    if (message != null)
                        ModAPI.sendChatMessage(message);
                }
            }

            boolean floatUp = false;
            boolean[] found = new boolean[4];
            for (Obstacle o : Game.obstacles)
            {
                if (o instanceof ObstacleWater)
                {
                    if (Game.lessThan(o.posX - 25, t.posX + Game.tile_size * t.vX, o.posX + 25 + 50 * t.vX))
                        found[0] = true;
                    if (Game.lessThan(o.posY - 25, t.posY + Game.tile_size * t.vY, o.posY + 25 + 50 * t.vY))
                        found[1] = true;
                    if (Game.lessThan(o.posX - 25 - 50 * t.vX, t.posX - Game.tile_size * t.vX, o.posX + 25))
                        found[2] = true;
                    if (Game.lessThan(o.posY - 25 - 50 * t.vY, t.posY - Game.tile_size * t.vY, o.posY + 25))
                        found[3] = true;
                }
            }

            if ((!found[0] && t.vX > 0) ||
                    (!found[1] && t.vY > 0) ||
                    (!found[2] && t.vX < 0) ||
                    (!found[3] && t.vY < 0) ||
                    (t.posZ < -this.stackHeight * Game.tile_size))
                floatUp = true;

            if (!floatUp && t.posZ > -this.stackHeight * Game.tile_size)
            {
                if (((t.vX > 0 || t.vY < 0) && (t.posX > this.posX && t.posX < this.posX + Game.tile_size) && (t.posY < this.posY && t.posY > this.posY - Game.tile_size)) ||
                        (t.vX < 0 || t.vY > 0) && (t.posX < this.posX && t.posX > this.posX - Game.tile_size) && (t.posY > this.posY && t.posY < t.posY + Game.tile_size))
                    t.posZ -= Panel.frameFrequency;
            }
            else if (floatUp && t.posZ < 0)
                t.posZ += Panel.frameFrequency;
        }

        this.onObjectEntryLocal(m);
    }


    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && m instanceof Tank && !ScreenGame.finished && Math.random() * Panel.frameFrequency <= 0.1 * Game.effectMultiplier)
        {
            Tank t = (Tank) m;

            double a = m.getPolarDirection();
            Effect e1 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            Effect e2 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            e1.drawLayer = 1;
            e2.drawLayer = 1;
            e1.setPolarMotion(a - Math.PI / 2, t.size * 0.25);
            e2.setPolarMotion(a + Math.PI / 2, t.size * 0.25);
            e1.size = t.size / 5;
            e2.size = t.size / 5;
            e1.posX += e1.vX;
            e1.posY += e1.vY;
            e2.posX += e2.vX;
            e2.posY += e2.vY;
            e1.angle = a;
            e2.angle = a;
            e1.setPolarMotion(0, 0);
            e2.setPolarMotion(0, 0);

            double var = 20;
            e1.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e1.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e1.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            e2.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e2.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e2.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            double angle = t.getPolarDirection() + Math.PI / 2;

            e1.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e1.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e1.vZ = Math.sqrt(t.vX * t.vX + t.vY * t.vY) / 2;
            e1.addPolarMotion(angle, (Math.random() - 0.5) * e1.vZ);

            e2.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e2.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e2.vZ = e1.vZ;
            e2.addPolarMotion(angle, (Math.random() - 0.5) * e2.vZ);

            e1.vZ *= (Math.random() * 0.6 + 0.4);
            e2.vZ *= (Math.random() * 0.6 + 0.4);

            e1.maxAge = 50 + Math.random() * 20;
            e2.maxAge = 50 + Math.random() * 20;

            e1.size /= 2;
            e2.size /= 2;

            Game.effects.add(e1);
            Game.effects.add(e2);
        }
    }


    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        if (Game.enable3d)
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
        else
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
    }

    @Override
    public void drawTile(double r, double g, double b, double depth, double extra)
    {
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(this, this.posX, this.posY, -Game.tile_size * this.stackHeight, Game.tile_size, Game.tile_size, -extra);
    }

    public double getTileHeight()
    {
        return -this.stackHeight * Game.tile_size;
    }

    public double getGroundHeight()
    {
        return -this.stackHeight * Game.tile_size;
    }
}

package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.rendering.ShaderMud;
import tanks.tank.Tank;

public class ObstacleMud extends Obstacle
{
    public ObstacleMud(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        this.isSurfaceTile = true;

        this.colorR = 70;
        this.colorG = 30;
        this.colorB = 0;

        this.replaceTiles = true;

        this.description = "A thick puddle of mud that slows tanks down";

        this.tileRenderer = ShaderMud.class;
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank)
            m.addStatusEffect(StatusEffect.mud, 0, 20, 30);

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

            e1.enableGlow = false;
            e2.enableGlow = false;

            Game.effects.add(e1);
            Game.effects.add(e2);
        }

        if (m instanceof Tank & Game.playerTank != null)
        {
            double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));
            double distsq = Math.pow(m.posX - Game.playerTank.posX, 2) + Math.pow(m.posY - Game.playerTank.posY, 2);

            double radius = 250000;
            if (distsq <= radius && Math.random() < Panel.frameFrequency * 0.05 && speed > 0 && Game.playerTank != null && !Game.playerTank.destroy)
            {
                int sound = (int) (Math.random() * 8 + 1);
                Drawing.drawing.playSound("mud" + sound + ".ogg", (float) ((speed / 3.0f) + 0.5f) * 1.25f, (float) (speed * 0.025 * (radius - distsq) / radius));
            }
        }
    }

    @Override
    public void draw()
    {
        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
    }

//    @Override
//    public void drawTile(IBatchRenderableObject o, double r, double g, double b, double d, double extra)
//    {
//        double frac = Obstacle.draw_size / Game.tile_size;
//
//        if (frac < 1 || extra != 0)
//        {
//            Drawing.drawing.setColor(this.colorR * frac + r * (1 - frac), this.colorG * frac + g * (1 - frac), this.colorB * frac + b * (1 - frac));
//            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra);
//        }
//        else
//        {
//            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
//            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra, (byte) 61);
//        }
//    }

    public double getTileHeight()
    {
        return 0;
    }

    public double getGroundHeight()
    {
        return 0;
    }
}

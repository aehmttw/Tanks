package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.tank.IAvoidObject;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ObstacleLava extends Obstacle implements IAvoidObject
{
    public double particles = 0;

    public ObstacleLava(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        this.isSurfaceTile = true;

        this.colorR = 200;
        this.colorG = 20;
        this.colorB = 0;

        this.replaceTiles = true;
        this.update = true;

        this.description = "A pool of hot lava that severely damages tanks";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank && ((Tank) m).flashAnimation < 1 && !ScreenGame.finished)
        {
            ((Tank) m).flashAnimation = 1;
            ((Tank) m).damage(0.005 * Panel.frameFrequency, m);
        }

        this.onObjectEntryLocal(m);
    }

    public void addEffect()
    {
        if (Game.effectsEnabled && !ScreenGame.finished)
        {
            if (Math.random() < Game.effectMultiplier * 0.01)
            {
                if (Game.enable3d)
                {
                    Effect e = Effect.createNewEffect(this.posX + (Math.random() - 0.5) * Game.tile_size, this.posY + (Math.random() - 0.5) * Game.tile_size, 0, Effect.EffectType.piece);
                    e.colR = 255;
                    e.colG = Math.random() * 255;
                    e.colB = 0;
                    e.vZ = Math.random() + 1;
                    Game.addEffects.add(e);
                }
                else
                {
                    Effect e = Effect.createNewEffect(this.posX + (Math.random() - 0.5) * Game.tile_size, this.posY + (Math.random() - 0.5) * Game.tile_size, Effect.EffectType.piece);
                    e.colR = 255;
                    e.colG = Math.random() * 255;
                    e.colB = 0;
                    Game.addEffects.add(e);
                }
            }
        }
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (m instanceof Tank && !ScreenGame.finishedQuick)
        {
            this.particles += Panel.frameFrequency * 10;

            if (Game.playerTank != null)
            {
                double distsq = Math.pow(m.posX - Game.playerTank.posX, 2) + Math.pow(m.posY - Game.playerTank.posY, 2);

                double radius = 250000;
                if (distsq <= radius && !Game.playerTank.destroy)
                {
                    Drawing.drawing.playSound("hiss.ogg", (float) (Math.random() * 0.2 + 1), (float) (0.05 * (radius - distsq) / radius));
                }
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

    @Override
    public void update()
    {
        this.particles += Math.random() * Panel.frameFrequency;

        while (this.particles > 0)
        {
            this.particles--;
            this.addEffect();
        }
    }

    @Override
    public void drawTile(IBatchRenderableObject o, double r, double g, double b, double d, double extra)
    {
        double frac = Obstacle.draw_size / Game.tile_size;

        if (frac < 1 || extra != 0)
        {
            Drawing.drawing.setColor(this.colorR * frac + r * (1 - frac), this.colorG * frac + g * (1 - frac), this.colorB * frac + b * (1 - frac));
            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra);
        }
        else
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra, (byte) 61);
        }
    }

    public double getTileHeight()
    {
        return 0;
    }

    public double getGroundHeight()
    {
        return 0;
    }

    @Override
    public double getRadius()
    {
        return Game.tile_size;
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return Math.sqrt(Math.pow(posX - this.posX, 2) + Math.pow(posY - this.posY, 2));
    }
}

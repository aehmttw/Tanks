package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;

public class ObstacleColor extends Obstacle
{
    public double duration;

    public final long defineTime = System.currentTimeMillis();

    public boolean flashing = false;

    public double flashSpeedMultiplier = 1;

    public double alphaCounter = 0;

    public ObstacleColor(String name, double posX, double posY, double r, double g, double b, double a)
    {
        super(name, posX, posY);

        this.drawLevel = 0;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        this.isSurfaceTile = true;
        this.update = true;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;

        this.duration = 0;
    }

    @Override
    public void draw()
    {
        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
            Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
    }

    @Override
    public void update()
    {
        if (this.duration > 0)
        {
            if (System.currentTimeMillis() - this.defineTime > duration * 10)
                Game.removeObstacles.add(this);
        }

        if (flashing)
        {
            alphaCounter = (alphaCounter + (Panel.frameFrequency / 50) * flashSpeedMultiplier) % 90;
            this.colorA = Math.sin(alphaCounter) * 100;
        }
    }

    @Override
    public void drawTile(IBatchRenderableObject o, double r, double g, double b, double d, double extra)
    {
        if (this.colorA < 5)
            return;

        double frac = Obstacle.draw_size / Game.tile_size;

        if (frac < 1 && extra == 0)
        {
            Drawing.drawing.setColor(this.colorR * frac + r * (1 - frac), this.colorG * frac + g * (1 - frac), this.colorB * frac + b * (1 - frac), this.colorA);
            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra);
        }
        else
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
            Drawing.drawing.fillBox(o, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, d * (1 - frac) + extra, (byte) 61);
        }
    }

    public double getTileHeight()
    {
        return 0;
    }
}

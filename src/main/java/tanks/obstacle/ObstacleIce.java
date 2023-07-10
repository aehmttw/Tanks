package tanks.obstacle;

import basewindow.IBatchRenderableObject;
import tanks.*;
import tanks.tank.Tank;

public class ObstacleIce extends Obstacle
{
    public ObstacleIce(String name, double posX, double posY)
    {
        super(name, posX, posY);

        if (Game.enable3d)
            this.drawLevel = 6;
        else
            this.drawLevel = 1;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        this.isSurfaceTile = true;

        this.colorR = 200;
        this.colorG = 225;
        this.colorB = 255;
        this.colorA = 180;

        this.replaceTiles = true;

        this.description = "A slippery layer of ice";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank)
        {
            m.addStatusEffect(StatusEffect.ice, 0, 5, 10);
        }
    }

    @Override
    public void draw()
    {
        System.out.println("drawing time");
        double h = this.baseGroundHeight;

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA * (h - Obstacle.draw_size / Game.tile_size * 15) / (h - 15));

        if (!Game.enable3d)
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        else
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
    }

    @Override
    public void drawTile(IBatchRenderableObject o, double r, double g, double b, double d, double extra)
    {
        double frac = Obstacle.draw_size / Game.tile_size;

        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(o, this.posX, this.posY, -frac * 15 - extra, Game.tile_size, Game.tile_size, d + extra);
    }

    public double getTileHeight()
    {
        double frac = Obstacle.draw_size / Game.tile_size;
        return -frac * 15;
    }

    public double getGroundHeight()
    {
        return 0;
    }
}

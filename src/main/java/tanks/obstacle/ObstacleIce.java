package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.StatusEffect;
import tanks.rendering.ShaderGroundIce;
import tanks.rendering.ShaderIce;
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

        this.renderer = ShaderIce.class;
        this.tileRenderer = ShaderGroundIce.class;

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
        double h = this.baseGroundHeight;

        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA * (h - Obstacle.draw_size / Game.tile_size * 15) / (h - 15));
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
        else
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
        }
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

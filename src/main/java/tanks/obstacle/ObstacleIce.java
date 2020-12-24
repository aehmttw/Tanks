package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class ObstacleIce extends Obstacle
{
    public ObstacleIce(String name, double posX, double posY)
    {
        super(name, posX, posY);

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

        this.description = "A slippery layer of ice";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Tank)
        {
            AttributeModifier a = new AttributeModifier("ice_accel", "acceleration", AttributeModifier.Operation.multiply, -0.75);
            a.duration = 10;
            a.deteriorationAge = 5;
            m.addUnduplicateAttribute(a);

            AttributeModifier b = new AttributeModifier("ice_slip", "friction", AttributeModifier.Operation.multiply, -0.875);
            b.duration = 10;
            b.deteriorationAge = 5;
            m.addUnduplicateAttribute(b);

            AttributeModifier c = new AttributeModifier("ice_max_speed", "max_speed", AttributeModifier.Operation.multiply, 3);
            c.duration = 10;
            c.deteriorationAge = 5;
            m.addUnduplicateAttribute(c);
        }
    }

    @Override
    public void draw()
    {
        double h = (Game.sampleGroundHeight(this.posX, this.posY));

        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA * (h - Obstacle.draw_size / Game.tile_size * 15) / (h - 15));

        if (!Game.enable3d)
            Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        else
            Drawing.drawing.fillBox(this.posX, this.posY, 0, Game.tile_size, Game.tile_size, 0, (byte) 61);
    }

    @Override
    public void drawTile(double r, double g, double b, double d)
    {
        double frac = Obstacle.draw_size / Game.tile_size;

        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(this.posX, this.posY, -frac * 15, Game.tile_size, Game.tile_size, d);
    }

    public double getTileHeight()
    {
        return 0;
    }
}

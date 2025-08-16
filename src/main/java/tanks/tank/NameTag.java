package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;

public class NameTag implements IDrawable
{
    public Tank tank;
    public double ox;
    public double oy;
    public double oz;
    public double size = 20;
    public String name;
    public int drawLevel = 9;

    public NameTag(Tank m, double ox, double oy, double oz, String name)
    {
        this.tank = m;
        this.ox = ox;
        this.oy = oy;
        this.oz = oz;
        this.name = name;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setFontSize(size * ((Game.tile_size - this.tank.destroyTimer) / Game.tile_size) * Math.min(this.tank.drawAge / Game.tile_size, 1));

        Drawing.drawing.setColor(this.tank.secondaryColor.red, this.tank.secondaryColor.green, this.tank.secondaryColor.blue, 255, 0);

        if (Game.enable3d)
            Drawing.drawing.drawText(tank.posX + ox + 2, tank.posY + oy + 2, tank.posZ + oz + 2, name);
        else
            Drawing.drawing.drawText(tank.posX + ox + 2, tank.posY + oy + 2, name);

        Drawing.drawing.setColor(this.tank.color.red, this.tank.color.green, this.tank.color.blue, 255, 0.5);

        if (Game.enable3d)
            Drawing.drawing.drawText(tank.posX + ox, tank.posY + oy, tank.posZ + oz, name);
        else
            Drawing.drawing.drawText(tank.posX + ox, tank.posY + oy, name);
    }
}

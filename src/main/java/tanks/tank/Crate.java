package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.obstacle.Obstacle;

public class Crate extends Movable
{
    public Tank tank;
    public double size;
    public double age = 0;
    public double iPosZ;

    public Crate(Tank tank, double height)
    {
        super(tank.posX, tank.posY);
        this.iPosZ = height;
        this.posZ = this.iPosZ;
        this.tank = tank;
        this.size = tank.size * 1.5;
        this.drawLevel = 9;
    }

    @Override
    public void draw()
    {
        if (this.age <= 0)
            Drawing.drawing.playGlobalSound("accel.ogg", (float) (0.75f / this.iPosZ * 1000), 0.25f);

        if (Game.game.window.drawingShadow || !Game.shadowsEnabled)
            this.age += Panel.frameFrequency;

        double size = this.size * Obstacle.draw_size / Game.tile_size * Math.min(1, this.age / (Game.tile_size * 1.5));

        if (Game.enable3d)
        {
            Drawing.drawing.setColor(this.tank.secondaryColor);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ, size, size, size, this.tank.baseSkin.base);

            Drawing.drawing.setColor(this.tank.color);
            Drawing.drawing.setColor(this.tank.color.red * 0.6, this.tank.color.green * 0.6, this.tank.color.blue * 0.6);
            Drawing.drawing.setColor(this.tank.color.red * 0.8, this.tank.color.green * 0.8, this.tank.color.blue * 0.8);

            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ, size * 0.8, size * 0.8, size + 2, this.tank.colorSkin.base);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ + size * 0.1, size * 0.8, size + 2, size * 0.8, this.tank.colorSkin.base);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ + size * 0.1, size + 2, size * 0.8, size * 0.8, this.tank.colorSkin.base);

            if (this.tank.emblem != null)
            {
                Drawing.drawing.setColor(this.tank.emblemColor);
                Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY, this.posZ + size + 2, size * 0.75, size * 0.75);
            }
        }
        else
        {
            Drawing.drawing.setColor(this.tank.secondaryColor);
            Drawing.drawing.fillRect(this.posX, this.posY - this.posZ, size, size);
            Drawing.drawing.setColor(this.tank.color);
            Drawing.drawing.fillRect(this.posX, this.posY - this.posZ, size * 0.8, size * 0.8);

            if (this.tank.emblem != null)
            {
                Drawing.drawing.setColor(this.tank.emblemColor);
                Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY - this.posZ, size * 0.75, size * 0.75);
            }
        }

        double frac = Math.max(0, (this.iPosZ - this.posZ) / this.iPosZ);

        Drawing.drawing.setColor(this.tank.color, frac * 255, 1);
        fillOutlineRect(this.posX, this.posY, this.size * (2 - frac));
        Drawing.drawing.setColor(this.tank.color, 255, 1);
        fillOutlineRect(this.posX, this.posY, this.size * (frac));

        Drawing.drawing.setColor(this.tank.emblemColor);
        if (this.tank.emblem != null)
            Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY, frac * this.size * 0.75, frac * this.size * 0.75);

        if (Game.glowEnabled)
        {
            Drawing.drawing.setColor(this.tank.secondaryColor.red, this.tank.secondaryColor.green, this.tank.secondaryColor.blue, frac * 255, 1);
            Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4, this.size * 4);
        }
    }

    public static void fillOutlineRect(double x, double y, double size)
    {
        double border = size / 10;

        double x1 = x + size / 2 - border / 2;
        double x2 = x - size / 2 + border / 2;
        double y1 = y + size / 2 - border / 2;
        double y2 = y - size / 2 + border / 2;
        Drawing.drawing.fillRect(x1, y, border, size - border * 2);
        Drawing.drawing.fillRect(x2, y, border, size - border * 2);
        Drawing.drawing.fillRect(x, y1, size, border);
        Drawing.drawing.fillRect(x, y2, size, border);
    }

    public void update()
    {
        if (!this.destroy)
            this.posZ -= 10 * Panel.frameFrequency;

        if (this.posZ <= 0 && !this.destroy)
        {
            this.posZ = 0;
            this.destroy = true;
            Drawing.drawing.playSound("open2.ogg");

            tank.droppedFromCrate = true;
            tank.drawAge = 50;
            if (!tank.isRemote)
            {
                if (tank instanceof TankPlayer)
                    Game.addPlayerTank(((TankPlayer) tank).player, tank.posX, tank.posY, tank.angle, tank.team);
                else
                    Game.addTank(tank);
            }
        }

        if (this.destroy)
        {
            this.size -= Panel.frameFrequency;

            if (this.size <= 0)
            {
                Game.removeMovables.add(this);
                this.size = 0;
            }
        }
    }
}

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

    public Crate(Tank tank)
    {
        super(tank.posX, tank.posY);
        this.posZ = 1000;
        this.tank = tank;
        this.size = tank.size * 1.5;
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.tank.colorR, this.tank.colorG, this.tank.colorB);

        double size = this.size * Obstacle.draw_size / Game.tile_size;

        if (Game.enable3d)
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ, size, size, size);
        else
            Drawing.drawing.fillRect(this.posX, this.posY - this.posZ, size, size);
    }

    public void update()
    {
        if (!this.destroy)
            this.posZ -= 10 * Panel.frameFrequency;

        if (this.posZ <= 0 && !this.destroy)
        {
            this.destroy = true;
            Game.movables.add(tank);
            tank.drawAge = 50;
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

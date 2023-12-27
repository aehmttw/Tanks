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

    public Crate(Tank tank)
    {
        super(tank.posX, tank.posY);
        this.posZ = 1000;
        this.tank = tank;
        this.size = tank.size * 1.5;
        this.drawLevel = 9;
    }

    @Override
    public void draw()
    {
        this.age += Panel.frameFrequency;
        double size = this.size * Obstacle.draw_size / Game.tile_size * Math.min(1, this.age / (Game.tile_size * 1.5));

        if (Game.enable3d)
        {
            Drawing.drawing.setColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ, size, size, size);

            TankModels.FullTankModel model = TankModels.fullTankModels.get(this.tank.baseModel.file);

            if (model == TankModels.tank)
                model = TankModels.fullTankModels.get(this.tank.colorModel.file);

            Drawing.drawing.setColor(this.tank.colorR, this.tank.colorG, this.tank.colorB);
            //Drawing.drawing.drawModel(m, this.posX, this.posY,  this.posZ - 1, size * 0.8, size * 0.8, (size + 2) * 2, 0, 0, 0);
            Drawing.drawing.setColor(this.tank.colorR * 0.6, this.tank.colorG * 0.6, this.tank.colorB * 0.6);
            //Drawing.drawing.drawModel(m, this.posX, this.posY + size * 0.5 + 1,  this.posZ + size * 0.4 + 1, size * 0.8, size * 0.8, (size + 2) * 2, 0, 0, Math.PI / 2);
            Drawing.drawing.setColor(this.tank.colorR * 0.8, this.tank.colorG * 0.8, this.tank.colorB * 0.8);
            //Drawing.drawing.drawModel(m, this.posX + size * 0.5 + 1, this.posY,  this.posZ + size * 0.4 + 1, size * 0.8, size * 0.8, (size + 2) * 2, 0, Math.PI / 2, 0);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ, size * 0.8, size * 0.8, size + 2, model.texture);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ + size * 0.1, size * 0.8, size + 2, size * 0.8, model.texture);
            Drawing.drawing.fillBox(this.posX, this.posY, this.posZ + size * 0.1, size + 2, size * 0.8, size * 0.8, model.texture);

            if (this.tank.emblem != null)
            {
                Drawing.drawing.setColor(this.tank.emblemR, this.tank.emblemG, this.tank.emblemB);
                Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY, this.posZ + size + 2, size * 0.75, size * 0.75);
            }
        }
        else
        {
            Drawing.drawing.setColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB);
            Drawing.drawing.fillRect(this.posX, this.posY - this.posZ, size, size);
            Drawing.drawing.setColor(this.tank.colorR, this.tank.colorG, this.tank.colorB);
            Drawing.drawing.fillRect(this.posX, this.posY - this.posZ, size * 0.8, size * 0.8);

            if (this.tank.emblem != null)
            {
                Drawing.drawing.setColor(this.tank.emblemR, this.tank.emblemG, this.tank.emblemB);
                Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY - this.posZ, size * 0.75, size * 0.75);
            }
        }

        double frac = Math.max(0, (1000 - this.posZ) / 1000);

        Drawing.drawing.setColor(this.tank.colorR, this.tank.colorG, this.tank.colorB, frac * 255, 1);
        fillOutlineRect(this.posX, this.posY, this.size * (2 - frac));
        Drawing.drawing.setColor(this.tank.colorR, this.tank.colorG, this.tank.colorB, 255, 1);
        fillOutlineRect(this.posX, this.posY, this.size * (frac));

        Drawing.drawing.setColor(this.tank.emblemR, this.tank.emblemG, this.tank.emblemB);
        if (this.tank.emblem != null)
            Drawing.drawing.drawImage(this.tank.emblem, this.posX, this.posY, frac * this.size * 0.75, frac * this.size * 0.75);

        if (Game.glowEnabled)
        {
            Drawing.drawing.setColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB, frac * 255, 1);
            Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4, this.size * 4);
        }
    }

    public void fillOutlineRect(double x, double y, double size)
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
            Drawing.drawing.playSound("open.ogg");

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

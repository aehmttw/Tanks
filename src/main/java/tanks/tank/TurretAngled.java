package tanks.tank;

import tanks.Drawing;
import tanks.Game;

public class TurretAngled extends Turret
{
    public TurretAngled(Tank t)
    {
        super(t);
    }

    public void draw(double yaw, double pitch, boolean forInterface, boolean in3d, boolean transparent)
    {
        this.posX = tank.posX;
        this.posY = tank.posY;

        double s = (this.tank.size * (Game.tile_size - this.tank.destroyTimer) / Game.tile_size) * Math.min(this.tank.drawAge / Game.tile_size, 1);

        double l = length * (Game.tile_size - this.tank.destroyTimer) / Game.tile_size - Math.max(Game.tile_size - tank.drawAge, 0) / Game.tile_size * length;

        if (forInterface)
            l = Math.min(length, Game.tile_size * 1.5);

        if (transparent)
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127, 0.5);
        else
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 0.5);

        if (forInterface)
            Drawing.drawing.drawInterfaceModel(turret_model, this.posX, this.posY, l, l * size / 8, yaw);
        else if (!in3d)
            Drawing.drawing.drawModel(turret_model, this.posX, this.posY, l, l * size / 8, yaw);
        else
            Drawing.drawing.drawModel(turret_model, this.posX, this.posY, (s * 1.3) / 2, l, l * size / 8, l * size / 8, yaw, pitch, 0);

        if (transparent)
            Drawing.drawing.setColor((this.colorR + this.tank.colorR) / 2, (this.colorG + this.tank.colorG) / 2, (this.colorB + this.tank.colorB) / 2, 127, 0.5);
        else
            Drawing.drawing.setColor((this.colorR + this.tank.colorR) / 2, (this.colorG + this.tank.colorG) / 2, (this.colorB + this.tank.colorB) / 2, 255, 0.5);

        if (forInterface)
            Drawing.drawing.drawInterfaceModel(base_model, this.posX, this.posY, l, l, yaw);
        else if (!in3d)
            Drawing.drawing.drawModel(base_model, this.posX, this.posY, l, l, yaw);
        else
            Drawing.drawing.drawModel(base_model, this.posX, this.posY, s / 2, l, l, l, yaw);
    }
}

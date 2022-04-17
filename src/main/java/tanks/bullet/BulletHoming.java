package tanks.bullet;

import tanks.*;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Ray;
import tanks.tank.Tank;

public class BulletHoming extends Bullet
{
    public static String bullet_name = "homing";
    public Tank target = null;

    public BulletHoming(double x, double y, int bounces, Tank t, ItemBullet item)
    {
        super(x, y, bounces, t, item);
    }

    public void update()
    {
        Tank nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Movable m: Game.movables)
        {
            if (m instanceof Tank && !Team.isAllied(this, m) && !m.destroy)
            {
                Tank t = (Tank) m;
                double d = Movable.distanceBetween(this, m);

                if (d < nearestDist)
                {
                    nearestDist = d;
                    nearest = t;
                }
            }
        }

        if (nearest != null && !this.destroy)
        {
            double a = this.getAngleInDirection(nearest.posX, nearest.posY);
            Ray r = new Ray(this.posX, this.posY, a, 0, this.tank);
            if (r.getTarget() == nearest)
            {
                double s = this.getSpeed();
                this.addPolarMotion(a, Panel.frameFrequency / 5.5);
                double s2 = this.getSpeed();
                this.vX *= s / s2;
                this.vY *= s / s2;

                this.target = nearest;

                if (Game.bulletTrails && Math.random() < Panel.frameFrequency)
                {
                    Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
                    double var = 50;
                    e.maxAge /= 2;

                    double r1 = 255;
                    double g1 = 120;
                    double b1 = 0;

                    e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
                    e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
                    e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

                    if (Game.enable3d)
                        e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
                    else
                        e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

                    Game.effects.add(e);
                }
            }
        }

        super.update();
    }
}

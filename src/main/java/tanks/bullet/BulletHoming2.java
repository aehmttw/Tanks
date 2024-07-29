package tanks.bullet;

import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletHoming2 extends Bullet
{
    public static String bullet_name = "homing";

    public BulletHoming2(double x, double y, int bounces, Tank t, ItemBullet item)
    {
        this(x, y, bounces, t, false, item);
    }

    public BulletHoming2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.homingSharpness = 1.0 / 5.5;
    }
}

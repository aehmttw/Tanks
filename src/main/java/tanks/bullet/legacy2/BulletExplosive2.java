package tanks.bullet.legacy2;

import tanks.bullet.Bullet;
import tanks.item.legacy.ItemBullet;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class BulletExplosive2 extends Bullet
{
    public static String bullet_name = "explosive";

    public BulletExplosive2(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, true, ib);
    }

    public BulletExplosive2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.name = bullet_name;

        this.hitExplosion = new Explosion(this.posX, this.posY, Mine.mine_radius, this.damage, true, this.tank, this.item);
    }
}

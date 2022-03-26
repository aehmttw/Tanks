package tanks.bullet;

import tanks.Game;
import tanks.event.EventLayMine;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class BulletExplosive extends Bullet
{
    public static String bullet_name = "explosive";

    public BulletExplosive(double x, double y, int bounces, Tank t)
    {
        this(x, y, bounces, t, true, null);
    }

    public BulletExplosive(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.outlineColorR = 255;
        this.outlineColorG = 255;
        this.outlineColorB = 0;
        this.name = bullet_name;

        this.playPopSound = false;
    }

    /** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
    @Deprecated
    public BulletExplosive(Double x, Double y, Integer bounces, Tank t, ItemBullet ib)
    {
        this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, true, ib);
    }

    @Override
    public void onDestroy()
    {
        Explosion e = new Explosion(this.posX, this.posY, Mine.mine_radius, this.damage, true, this.tank, this.item);
        e.explode();
    }

    @Override
    public void update()
    {
        if (((int) this.age % 80) / 40 == 1)
            this.outlineColorG = 255;
        else
            this.outlineColorG = 0;

        super.update();
    }

}

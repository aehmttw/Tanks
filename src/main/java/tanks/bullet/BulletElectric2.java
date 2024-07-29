package tanks.bullet;

import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletElectric2 extends BulletInstant
{
    public static String bullet_name = "electric";

    public BulletElectric2(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, false, ib);
    }

    public BulletElectric2(double x, double y, int bounces, Tank t, boolean affectsLiveBullets, ItemBullet ib)
    {
        super(x, y, 0, t, affectsLiveBullets, ib);
        this.rebounds = bounces;
        this.name = "electric";

        this.damage = 0.125;
        this.effect = BulletEffect.none;
        this.itemSound = "laser.ogg";

        this.overrideBaseColor = true;
        this.baseColorR = 0;
        this.baseColorG = 255;
        this.baseColorB = 255;

        this.overrideOutlineColor = true;
        this.outlineColorR = 200;
        this.outlineColorG = 255;
        this.outlineColorB = 255;

        this.hitStun = 100;
    }

}

package tanks.bullet;

import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletFlame2 extends BulletGas
{
    public static String bullet_name = "flamethrower";

    public BulletFlame2(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, false, ib);
    }

    public BulletFlame2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.itemSound = "flame.ogg";
        this.pitchVariation = 0.0;

        this.overrideBaseColor = true;
        this.overrideOutlineColor = true;
        this.baseColorR = 255;
        this.baseColorG = 255;
        this.baseColorB = 0;
        this.outlineColorR = 255;
        this.outlineColorG = 0;
        this.outlineColorB = 0;
        this.glowIntensity = 1;
        this.glowSize = 3;
        this.maxLiveBullets = 0;
        this.cooldown = 0;
        this.effect = Bullet.BulletEffect.none;
        this.bounces = 0;
        this.damage = 0.1;
        this.lowersBushes = false;
        this.burnsBushes = true;
        this.bulletCollision = false;

        this.life = 100;
        this.endSize = Bullet.bullet_size * 10;
    }
}

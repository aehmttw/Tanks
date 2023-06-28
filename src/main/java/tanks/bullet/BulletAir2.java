package tanks.bullet;

import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletAir2 extends BulletGas
{
    public static String bullet_name = "air";

    public BulletAir2(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, false, ib);
    }

    public BulletAir2(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.itemSound = "wind.ogg";
        this.pitchVariation = 1.0;

        this.overrideBaseColor = true;
        this.overrideOutlineColor = true;

        this.baseColorR = 160;
        this.baseColorG = 179;
        this.baseColorB = 191;
        this.noiseR = 95;
        this.noiseG = 76;
        this.noiseB = 64;
        this.outlineColorR = 100;
        this.outlineColorG = 131;
        this.outlineColorB = 151;
        this.glowIntensity = 0;
        this.glowSize = 0;
        this.opacity = 1.0 / 6;
        this.heavy = true;

        this.bulletHitKnockback = 0.04;
        this.tankHitKnockback = 0.1;

        this.lowersBushes = false;

        this.life = 200;
        this.endSize = Bullet.bullet_size * 40;
    }
}

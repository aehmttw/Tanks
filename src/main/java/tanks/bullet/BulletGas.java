package tanks.bullet;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableWithGlow;
import tanks.Movable;
import tanks.item.ItemBullet;
import tanks.network.event.EventTankControllerAddVelocity;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;
import tanks.tankson.Property;

public class BulletGas extends Bullet implements IDrawableWithGlow
{
    public static String bullet_class_name = "gas";

    public double startSize;

    @Property(id = "end_size", name = "End size", category = BulletPropertyCategory.appearance)
    public double endSize;

    public double startR;
    public double startG;
    public double startB;
    public double endR;
    public double endG;
    public double endB;

    @Property(id = "color_noise_r", name = "Noise red")
    public double noiseR;
    @Property(id = "color_noise_g", name = "Noise green")
    public double noiseG;
    @Property(id = "color_noise_b", name = "Noise blue")
    public double noiseB;

    public double baseDamage;
    public double baseBulletKB;
    public double baseTankKB;

    @Property(id = "opacity", name = "Opacity", category = BulletPropertyCategory.appearance)
    public double opacity = 1;

    public BulletGas()
    {
        this.init();
    }

    public BulletGas(double x, double y, Tank t, boolean affectsLiveBulletCount, ItemBullet.ItemStackBullet ib)
    {
        super(x, y, t, affectsLiveBulletCount, ib);
        this.init();
    }

    protected void init()
    {
        this.typeName = bullet_class_name;
        this.useCustomWallCollision = true;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.externalBulletCollision = false;
        this.destroyBullets = false;
        this.canMultiDamage = true;
        this.canBeCanceled = false;
        this.effect = BulletEffect.none;
        this.homingSilent = true;
    }

    @Override
    public void update()
    {
        if (this.age <= 0)
        {
            this.startSize = this.size;
            double colorRandom = Math.random();
            this.startR = this.baseColorR + colorRandom * this.noiseR;
            this.startG = this.baseColorG + colorRandom * this.noiseG;
            this.startB = this.baseColorB + colorRandom * this.noiseB;
            this.endR = this.outlineColorR + colorRandom * this.noiseR;
            this.endG = this.outlineColorG + colorRandom * this.noiseG;
            this.endB = this.outlineColorB + colorRandom * this.noiseB;
            this.baseDamage = this.damage;
            this.baseBulletKB = this.bulletHitKnockback;
            this.baseTankKB = this.tankHitKnockback;

            if (!this.lowersBushes)
                this.drawLevel = 0;
        }

        this.size = this.startSize * (1 - this.age / this.lifespan) + this.endSize * (this.age / this.lifespan);

        double frac = 0;
        if (this.lifespan > 0)
            frac = Math.pow(this.startSize, 2) / Math.pow(this.size, 2);

        this.damage = this.baseDamage * Math.max(0, 1.0 - this.age / this.lifespan);
        this.tankHitKnockback = this.baseTankKB * frac;
        this.bulletHitKnockback = this.baseBulletKB * frac;

        if (this.age > lifespan)
            Game.removeMovables.add(this);

        super.update();
    }

    @Override
    public void draw()
    {
        double rawOpacity = (1.0 - (this.age) / lifespan);
        rawOpacity *= rawOpacity * this.frameDamageMultipler;
        double opacity = Math.min(rawOpacity * 255 * this.opacity, 254) * (1 - this.destroyTimer / this.maxDestroyTimer);

        double frac = 0;
        if (this.lifespan > 0)
            frac = Math.max(0, 1 - this.age / this.lifespan);

        Drawing.drawing.setColor(this.startR * frac + this.endR * (1 - frac), this.startG * frac + this.endG * (1 - frac), this.startB * frac + this.endB * (1 - frac), opacity, this.luminance);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, size, size);
    }

    @Override
    public void drawGlow()
    {
        double rawOpacity = (1.0 - (this.age) / lifespan);
        rawOpacity *= rawOpacity * this.frameDamageMultipler * this.glowIntensity;
        double opacity = Math.min(rawOpacity * 255 * this.opacity, 255) * (1 - this.destroyTimer / this.maxDestroyTimer);

        double frac = 0;
        if (this.lifespan > 0)
            frac = Math.max(0, 1 - this.age / this.lifespan);

        Drawing.drawing.setColor(this.startR * frac + this.endR * (1 - frac), this.startG * frac + this.endG * (1 - frac), this.startB * frac + this.endB * (1 - frac), opacity, opacity / 255 * this.glowIntensity);

        if (Game.enable3d)
            Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, size * this.glowSize, size * this.glowSize, true, false);
        else
            Drawing.drawing.fillGlow(this.posX, this.posY, size * this.glowSize, size * this.glowSize);
    }

    @Override
    public void collidedWithObject(Movable m)
    {
        this.pushObject(m);

        double bulletKb = this.bulletHitKnockback;
        this.bulletHitKnockback = 0;
        super.collidedWithObject(m);
        this.bulletHitKnockback = bulletKb;
    }

    @Override
    public void collidedWithTank(Tank t)
    {
        this.pushObject(t);

        double tankKb = this.tankHitKnockback;
        this.tankHitKnockback = 0;
        super.collidedWithTank(t);
        this.tankHitKnockback = tankKb;
    }

    public void pushObject(Movable o)
    {
        if (!(o instanceof Tank || o instanceof Bullet))
            return;

        double mul;
        if (o instanceof Bullet)
            mul = this.bulletHitKnockback * Math.pow(Bullet.bullet_size, 2) / Math.max(1, Math.pow(((Bullet) o).size, 2));
        else
            mul = this.tankHitKnockback * Math.pow(Game.tile_size, 2)  / Math.max(1, Math.pow(((Tank) o).size, 2));

        double f = Math.pow(this.frameDamageMultipler, 2);
        double x = this.vX * f * mul;
        double y = this.vY * f * mul;

        o.vX += x;
        o.vY += y;

        if (o instanceof TankPlayerRemote)
            Game.eventsOut.add(new EventTankControllerAddVelocity((Tank) o, x, y, false));
    }

    @Override
    public boolean isGlowEnabled()
    {
        return this.glowIntensity > 0 && this.glowSize > 0;
    }

    @Override
    public void addDestroyEffect()
    {

    }
}

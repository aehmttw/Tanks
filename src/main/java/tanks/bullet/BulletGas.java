package tanks.bullet;

import basewindow.Color;
import tanks.*;
import tanks.item.ItemBullet;
import tanks.network.event.EventTankControllerAddVelocity;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;
import tanks.tankson.Property;

import java.util.ArrayList;
import java.util.Random;

public class BulletGas extends Bullet implements IDrawableWithGlow
{
    public static String bullet_class_name = "gas";

    public double startSize;

    @Property(id = "end_size", name = "End size", category = BulletPropertyCategory.appearance)
    public double endSize;

    public Color startColor = new Color();
    public Color endColor = new Color();

    @Property(id = "color_noise", name = "Noise", category = BulletPropertyCategory.appearanceNoiseColor, miscType = Property.MiscType.colorRGB)
    public Color noise = new Color();

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
        this.effect.trailEffects.clear();
        this.homingSilent = true;
    }

    @Override
    public void update()
    {
        if (this.age <= 0)
        {
            this.startSize = this.size;
            double colorRandom = Math.random();
            this.startColor.red = this.baseColor.red + colorRandom * this.noise.red;
            this.startColor.green = this.baseColor.green + colorRandom * this.noise.green;
            this.startColor.blue = this.baseColor.blue + colorRandom * this.noise.blue;
            this.endColor.red = this.outlineColor.red + colorRandom * this.noise.red;
            this.endColor.green = this.outlineColor.green + colorRandom * this.noise.green;
            this.endColor.blue = this.outlineColor.blue + colorRandom * this.noise.blue;
            this.baseDamage = this.damage;
            this.baseBulletKB = this.bulletHitKnockback;
            this.baseTankKB = this.tankHitKnockback;

            if (!this.lowersBushes)
                this.drawLevel = 0;
        }

        this.size = this.lifespan <= 0 ? this.startSize : this.startSize * (1 - this.age / this.lifespan) + this.endSize * (this.age / this.lifespan);

        double frac = 1;
        if (this.lifespan > 0)
            frac = Math.pow(this.startSize, 2) / Math.pow(this.size, 2);

        this.damage = this.baseDamage * (this.lifespan <= 0 ? 1 : Math.max(0, 1.0 - this.age / this.lifespan));
        this.tankHitKnockback = this.baseTankKB * frac;
        this.bulletHitKnockback = this.baseBulletKB * frac;

        if (this.age > lifespan && lifespan > 0)
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

        Drawing.drawing.setColor(this.startColor.red * frac + this.endColor.red * (1 - frac), this.startColor.green * frac + this.endColor.green * (1 - frac), this.startColor.blue * frac + this.endColor.blue * (1 - frac), opacity, this.effect.luminance);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, size, size);
    }

    @Override
    public void drawGlow()
    {
        double rawOpacity = (1.0 - (this.age) / lifespan);
        rawOpacity *= this.frameDamageMultipler * this.effect.glowIntensity;
        double opacity = Math.min(rawOpacity * 255 * this.opacity, 255) * (1 - this.destroyTimer / this.maxDestroyTimer);

        double frac = 0;
        if (this.lifespan > 0)
            frac = Math.max(0, 1 - this.age / this.lifespan);

        if (!this.effect.overrideGlowColor)
            Drawing.drawing.setColor(this.startColor.red * frac + this.endColor.red * (1 - frac), this.startColor.green * frac + this.endColor.green * (1 - frac), this.startColor.blue * frac + this.endColor.blue * (1 - frac), opacity, opacity / 255 * this.effect.glowIntensity);
        else
            Drawing.drawing.setColor(this.effect.glowColor.red, this.effect.glowColor.green, this.effect.glowColor.blue, opacity, opacity / 255 * this.effect.glowIntensity);

        if (Game.enable3d)
            Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, size * this.effect.glowSize, size * this.effect.glowSize, true, false);
        else
            Drawing.drawing.fillGlow(this.posX, this.posY, size * this.effect.glowSize, size * this.effect.glowSize);
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
        return this.effect.glowIntensity > 0 && this.effect.glowSize > 0;
    }

    @Override
    public void addDestroyEffect()
    {

    }

    @Override
    public void drawForInterface(double x, double width, double y, double size, ArrayList<Effect> effects, Random r, Color base, Color turret)
    {
        double speed = this.speed;
        double life = Math.min(this.lifespan, 200);
        if (this.lifespan > life)
            speed *= this.lifespan / life;

        double l = Math.min(Drawing.drawing.interfaceSizeX * 0.6, this.lifespan * this.speed);
        double start = x - l / 2;

        r.setSeed(0);

        double[] randoms = new double[(int) life * 2];
        for (int i = 0; i < randoms.length; i++)
        {
            randoms[i] = r.nextDouble();
        }

        double spread = Math.min(this.accuracySpread, 30);

        Color c1 = this.overrideBaseColor ? this.baseColor : base;
        Color c2 = this.overrideOutlineColor ? this.outlineColor : turret;

        double startSize = this.size;
        double endSize = this.endSize;
        double max = Math.max(startSize, endSize);
        double limit = Math.min(max, 500);
        startSize *= limit / max;
        endSize *= limit / max;

        for (int i = 0; i < life; i++)
        {
            double rawOpacity = (1.0 - i / life);
            double opacity = Math.min(rawOpacity * rawOpacity * 255 * this.opacity, 254);

            double frac = 0;
            if (life > 0)
                frac = Math.max(0, 1 - i / life);

            Drawing.drawing.setColor(c1.red * frac + c2.red * (1 - frac) + randoms[i] * this.noise.red,
                    c1.green * frac + c2.green * (1 - frac) + randoms[i] * this.noise.green,
                    c1.blue * frac + c2.blue * (1 - frac) + randoms[i] * this.noise.blue,
                    opacity, this.effect.luminance);
            Drawing.drawing.fillInterfaceOval(start + l * (1 - frac), y + (randoms[(int) (life + i)] - 0.5) * spread * i / 100.0 * speed, (frac * startSize + (1 - frac) * endSize), (frac * startSize + (1 - frac) * endSize));
        }

        for (int i = 0; i < life; i++)
        {
            double rawOpacity = (1.0 - i / life);
            double opacity = Math.min(rawOpacity * 255 * this.opacity, 254);

            double frac = 0;
            if (life > 0)
                frac = Math.max(0, 1 - i / life);

            if (!this.effect.overrideGlowColor)
                Drawing.drawing.setColor(c1.red * frac + c2.red * (1 - frac) + randoms[i] * this.noise.red,
                        c1.green * frac + c2.green * (1 - frac) + randoms[i] * this.noise.green,
                        c1.blue * frac + c2.blue * (1 - frac) + randoms[i] * this.noise.blue,
                        opacity, opacity / 255 * this.effect.glowIntensity);
            else
                Drawing.drawing.setColor(this.effect.glowColor.red, this.effect.glowColor.green, this.effect.glowColor.blue, opacity, opacity / 255 * this.effect.glowIntensity);

            Drawing.drawing.fillInterfaceGlow(start + l * (1 - frac), y + (randoms[(int) (life + i)] - 0.5) * spread * i / 100.0 * speed, this.effect.glowSize * (frac * startSize + (1 - frac) * endSize), this.effect.glowSize * (frac * startSize + (1 - frac) * endSize));

        }
    }
}

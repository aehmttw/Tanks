package tanks.bullet;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.network.event.EventBulletUpdate;
import tanks.network.event.EventTankControllerAddVelocity;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

public abstract class BulletGas extends Bullet
{
    public double life = 200;
    public double age = 0;
    public double startSize;
    public double endSize;

    public double startR;
    public double startG;
    public double startB;
    public double endR;
    public double endG;
    public double endB;

    public BulletGas(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, false, ib);
    }

    public BulletGas(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.useCustomWallCollision = true;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.effect = BulletEffect.none;
        this.externalBulletCollision = false;
    }

    @Override
    public void update()
    {
        if (this.age <= 0)
        {
            this.startSize = this.size;
            this.startR = this.baseColorR;
            this.startG = this.baseColorG;
            this.startB = this.baseColorB;
        }

        this.age += Panel.frameFrequency;
        this.size = this.startSize * (1 - this.age / this.life) + this.endSize * (this.age / this.life);

        super.update();

        if (this.age > life)
            Game.removeMovables.add(this);
    }

    @Override
    public void draw()
    {
        double rawOpacity = (1.0 - (this.age)/life);
        rawOpacity *= rawOpacity * this.frameDamageMultipler;
        double opacity = Math.min(rawOpacity * 255 / 6, 255);
        double frac = this.age / this.life;

        Drawing.drawing.setColor(this.startR * (1 - frac) + this.endR * frac, this.startG * (1 - frac) + this.endG * frac, this.startB * (1 - frac) + this.endB * frac, opacity, 0.5 /* TODO: glow*/);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, size, size);
    }

    @Override
    public void addDestroyEffect()
    {

    }
}

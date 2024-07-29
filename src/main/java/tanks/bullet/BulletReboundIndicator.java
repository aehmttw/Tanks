package tanks.bullet;

import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.network.event.EventBulletReboundIndicator;

public class BulletReboundIndicator extends Movable
{
    public double size;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorR2;
    public double colorG2;
    public double colorB2;
    public double maxAge;
    public double age;

    public BulletReboundIndicator(Bullet b)
    {
        super(b.posX, b.posY);
        this.posZ = b.posZ;

        this.size = b.size;
        this.colorR = b.baseColorR;
        this.colorG = b.baseColorG;
        this.colorB = b.baseColorB;
        this.colorR2 = b.outlineColorR;
        this.colorG2 = b.outlineColorG;
        this.colorB2 = b.outlineColorB;

        this.maxAge = b.delay;

        if (!b.tank.isRemote)
            Game.eventsOut.add(new EventBulletReboundIndicator(this));
    }

    public BulletReboundIndicator(double posX, double posY, double posZ, double size, double maxAge, double colorR, double colorG, double colorB, double colorR2, double colorG2, double colorB2)
    {
        super(posX, posY);
        this.posZ = posZ;
        this.size = size;
        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;
        this.colorR2 = colorR2;
        this.colorG2 = colorG2;
        this.colorB2 = colorB2;
        this.maxAge = maxAge;
    }

    @Override
    public void update()
    {
        this.age += Panel.frameFrequency;

        if (this.age > this.maxAge)
            Game.removeMovables.add(this);

        if (Game.effectsEnabled && Panel.frameFrequency * Game.effectMultiplier >= Math.random())
        {
            Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
            double var = 50;
            e.maxAge /= 2;
            e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
            e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
            e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));
            e.glowR = e.colR - this.colorR2;
            e.glowG = e.colG - this.colorG2;
            e.glowB = e.colB - this.colorB2;

            if (Game.enable3d)
                e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0 * 4);
            else
                e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

            Game.effects.add(e);
        }
    }

    @Override
    public void draw()
    {

    }
}

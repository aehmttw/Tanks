package tanks.bullet;

import basewindow.Color;
import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.network.event.EventBulletReboundIndicator;

public class BulletReboundIndicator extends Movable
{
    public double size;
    public Color color = new Color();
    public Color color2 = new Color();
    public double maxAge;
    public double age;

    public BulletReboundIndicator(Bullet b)
    {
        super(b.posX, b.posY);
        this.posZ = b.posZ;

        this.size = b.size;
        this.color.set(b.baseColor);
        this.color2.set(b.outlineColor);

        this.maxAge = b.delay;

        if (!b.tank.isRemote)
            Game.eventsOut.add(new EventBulletReboundIndicator(this));
    }

    public BulletReboundIndicator(double posX, double posY, double posZ, double size, double maxAge, double r1, double g1, double b1, double r2, double g2, double b2)
    {
        super(posX, posY);
        this.posZ = posZ;
        this.size = size;
        this.color.set(r1, g1, b1);
        this.color2.set(r2, g2, b2);
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
            e.maxAge /= 2;
            e.setColorWithNoise(this.color, 50);
            e.setGlowColor(this.color2);

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

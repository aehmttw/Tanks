package tanks.bullet.legacy;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.network.event.EventBulletUpdate;
import tanks.network.event.EventTankControllerAddVelocity;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;
import tanks.tank.TankPlayerRemote;

public class BulletAir extends Bullet
{
    public static String bullet_name = "air";

    double life = 200;
    double age = 0;
    public double sizeMul = 2;
    public double col = Math.random() * 95 + 160;

    public BulletAir(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, bounces, t, false, ib);
    }

    public BulletAir(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsLiveBulletCount, ib);
        this.useCustomWallCollision = true;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.effect = BulletEffect.none;
        this.drawLevel = 0;
        this.externalBulletCollision = false;
        this.name = bullet_name;
        this.itemSound = "wind.ogg";
        this.damage = 0;
        this.pitchVariation = 1;
        this.canMultiDamage = true;
    }

    @Override
    public void update()
    {
        if (this.age <= 0)
        {
            this.sizeMul = this.size / Bullet.bullet_size;
            this.life *= this.sizeMul;
        }

        this.age += Panel.frameFrequency;
        this.size = (int) (2 * this.age / sizeMul + 10);

        super.update();

        if (this.age > life)
            Game.removeMovables.add(this);
    }

    @Override
    public void collidedWithObject(Movable o)
    {
        if (!(o instanceof Tank || o instanceof Bullet))
            return;

        double mul = 10.0;

        if (o instanceof Bullet)
            mul = 400 / Math.pow(((Bullet) o).size, 2);
        else
            mul *= Game.tile_size * Game.tile_size / Math.pow(((Tank) o).size, 2);

        double f = Math.pow(this.frameDamageMultipler, 2);

        System.out.println(f + " " + mul / (size * size));

        double x = this.vX * f * mul / (size * size);
        double y = this.vY * f * mul / (size * size);

        o.vX += x;
        o.vY += y;

        if (o instanceof TankPlayerRemote)
            Game.eventsOut.add(new EventTankControllerAddVelocity((Tank) o, x, y, false));
    }

    public void collidedWithTank(Tank t)
    {
        this.collidedWithObject(t);
    }

    @Override
    public void draw()
    {
        double rawOpacity = (1.0 - (this.age)/life);
        rawOpacity *= rawOpacity * this.frameDamageMultipler;
        double opacity = Math.min(rawOpacity * 255 / 6, 255);

        double col = (this.col - 60 * (this.age / life));

        Drawing.drawing.setColor(col, (col * 4 + 255) / 5, (col * 2 + 255) / 3, opacity, 0.5);

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

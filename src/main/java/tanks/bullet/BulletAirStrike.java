package tanks.bullet;

import tanks.*;
import tanks.item.ItemBullet;
import tanks.network.event.EventBulletBounce;
import tanks.tank.Tank;

import java.util.ArrayList;

public class BulletAirStrike extends Bullet
{
    public static String bullet_class_name = "air_strike";

    public ArrayList<Double> pastPosX = new ArrayList<>();
    public ArrayList<Double> pastPosY = new ArrayList<>();
    public ArrayList<Double> pastPosZ = new ArrayList<>();
    public ArrayList<Double> pastTime = new ArrayList<>();

    public double initialX;
    public double initialY;
    public double finalX;
    public double finalY;

    public BulletAirStrike()
    {
        this.init();
    }

    public BulletAirStrike(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet ib)
    {
        super(x, y, t, affectsMaxLiveBullets, ib);
        this.init();
    }

    protected void init()
    {
        this.typeName = bullet_class_name;

        this.trail3d = true;

        this.enableExternalCollisions = false;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.enableCollision = false;
        this.posZ = Game.tile_size / 2;
        this.maxDestroyTimer = 100;
        this.canBeCanceled = false;
        this.moveOut = false;
        this.vZ = 0.1;
        this.edgeCollision = false;
        this.autoZ = false;
        this.showDefaultTrace = false;
        this.revertSpeed = false;
    }

    @Override
    public void update()
    {
        if (this.age <= 0)
        {
            this.vX = 0;
            this.vY = 0;
            this.initialX = this.posX;
            this.initialY = this.posY;
        }

        super.update();

        if (this.posZ <= Game.tile_size / 2 && !this.destroy)
        {
            this.vX = 0;
            this.vY = 0;

            if (!this.tank.isRemote)
                this.checkCollision();

            this.checkCollisionLocal();

            this.bounces--;

            if (this.bounces < 0 || this.posX >= Game.currentSizeX * Game.tile_size || this.posX < 0 || this.posY >= Game.currentSizeY * Game.tile_size || this.posY < 0 )
            {
                this.destroy = true;
                Drawing.drawing.playSound("bullet_explode.ogg", (float) (Bullet.bullet_size / this.size));
                this.vZ = 0;
            }
            else
            {
                this.stopTrails();
                this.posZ = Game.tile_size / 2 + 1;
                this.vZ = 0.1;

                this.finalX += this.finalX - this.initialX;
                this.finalY += this.finalY - this.initialY;
                this.initialX = this.posX;
                this.initialY = this.posY;

                if (!this.isRemote)
                {
                    this.collisionX = this.initialX;
                    this.collisionY = this.initialY;
                }

                this.addTrail(true);
                Drawing.drawing.playSound("bounce.ogg", (float) (Bullet.bullet_size / this.size));
                Game.eventsOut.add(new EventBulletBounce(this));
            }
        }

        if (this.posZ > 1100)
        {
            this.vZ = -0.1;
            this.posZ -= (this.posZ - 1100) * 2;
            this.stopTrails();

            if (!this.isRemote)
            {
                this.posX = this.finalX;
                this.posY = this.finalY;
                this.collisionX = this.finalX;
                this.collisionY = this.finalY;
                Game.eventsOut.add(new EventBulletBounce(this));
            }

            this.addTrail(true);
            Drawing.drawing.playSound("beep.ogg", 1.25f);
            Drawing.drawing.playSound("accel.ogg", (float) (Math.sqrt(this.speed) * 0.4));
        }

        double mul = 1;
        if (this.vZ < 0)
            mul = -2;

        this.vZ += mul * this.speed / 31.25 * Panel.frameFrequency;

        if (!this.destroy)
        {
            this.pastPosX.add(this.posX);

            if (Game.enable3d)
                this.pastPosY.add(this.posY);
            else
                this.pastPosY.add(this.posY - this.posZ + 25);

            this.pastPosZ.add(this.posZ);
            this.pastTime.add(this.age);
        }
    }

    public void draw()
    {
        if (this.vZ < 0)
        {
            Drawing.drawing.setColor(0, 0, 0, (60 - this.posZ / 32) * (1 - Math.min(this.destroyTimer / 60, 1)));
            Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 2, this.size * 2, true);
            Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);
        }

        if (!Game.enable3d)
            this.posY -= this.posZ - Game.tile_size / 2;

        super.draw();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 2;
    }

    @Override
    public void setTargetLocation(double x, double y)
    {
        this.vX = 0;
        this.vY = 0;
        this.finalX = x;
        this.finalY = y;
    }

    public void drawTrace(double ix, double iy, double fx, double fy)
    {
        double posZ = 25;

        double dx = fx - ix;
        double dy = fy - iy;

        while (posZ < 1100)
        {
            for (int i = 0; i <= this.bounces; i++)
            {
                Effect e1 = Effect.createNewEffect(ix + dx * i, iy + dy * i, posZ, Effect.EffectType.ray);
                e1.size = this.size;
                Game.effects.add(e1);
            }

            posZ += this.size * 1.5;
        }
    }

    @Override
    public Bullet rebound(Movable m)
    {
        Bullet b = super.rebound(m);
        b.posZ = Game.tile_size / 2 + 1;
        return b;
    }
}

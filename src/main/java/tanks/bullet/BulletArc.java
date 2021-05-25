package tanks.bullet;

import tanks.AttributeModifier;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

import java.util.ArrayList;

public class BulletArc extends Bullet
{
    public static String bullet_name = "arc";

    public double maxAge;

    public double angle;

    public static final double gravity = 0.1;

    public ArrayList<Double> pastPosX = new ArrayList<>();

    public ArrayList<Double> pastPosY = new ArrayList<>();

    public ArrayList<Double> pastPosZ = new ArrayList<>();

    public ArrayList<Double> pastTime = new ArrayList<>();

    public BulletArc(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet ib)
    {
        super(x, y, 0, t, affectsMaxLiveBullets, ib);
        this.playPopSound = false;
        this.name = bullet_name;
        //this.effect = BulletEffect.trail;
        this.itemSound = "arc.ogg";

        this.enableExternalCollisions = false;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.enableCollision = false;
        this.posZ = Game.tile_size / 2;
        this.maxDestroyTimer = 100;
        this.obstacleCollision = false;
        this.canBeCanceled = false;
        this.moveOut = false;

        this.autoZ = false;
    }

    public BulletArc(double x, double y, int bounces, Tank t)
    {
        this(x, y, t, true, null);
    }

    /**
     * Do not use, instead use the constructor with primitive data types.
     */
    @Deprecated
    public BulletArc(Double x, Double y, Integer bounces, Tank t, ItemBullet ib)
    {
        this(x, y, t, true, ib);
    }

    @Override
    public void update()
    {
        super.update();

        double gravMod = 1;
        for (int i = 0; i < this.attributes.size(); i++)
        {
            AttributeModifier a = this.attributes.get(i);

            if (a.type.equals("velocity"))
            {
                gravMod = a.getValue(gravMod);
            }
        }

        this.vZ -= gravity * Panel.frameFrequency * gravMod;

        if (this.posZ <= Game.tile_size / 2 && !this.destroy)
        {
            this.vX = 0;
            this.vY = 0;
            this.vZ = 0;

            if (!this.tank.isRemote)
                this.checkCollision();

            this.checkCollisionLocal();
            this.destroy = true;
            Drawing.drawing.playSound("bullet_explode.ogg", (float) (Bullet.bullet_size / this.size));
        }

        if (!this.destroy)
            this.angle = this.getPolarDirection();

        this.pastPosX.add(this.posX);

        if (Game.enable3d)
            this.pastPosY.add(this.posY);
        else
            this.pastPosY.add(this.posY - this.posZ + 25);

        this.pastPosZ.add(this.posZ);
        this.pastTime.add(this.age);
    }

    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, (60 - this.posZ / 32) * (1 - Math.min(this.destroyTimer / 60, 1)));
        Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 2, this.size * 2, true);
        Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

        if (!Game.enable3d)
            this.posY -= this.posZ - Game.tile_size / 2;

        if (Game.bulletTrails)
        {
            boolean stop = false;
            double length = 100;
            Drawing.drawing.setColor(80, 80, 80, 64 * (1 - this.destroyTimer / this.maxDestroyTimer));

            if (Game.enable3d)
                Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, this.size, this.size, true, false);
            else
                Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

            Drawing.drawing.setColor(0, 0, 0, 0, 0.5);
            Game.game.window.setBatchMode(true, true, true, false);
            for (int i = this.pastTime.size() - 1; i >= 1; i--)
            {
                if (stop)
                    break;

                double t = this.age - this.pastTime.get(i);

                if (t > length)
                    stop = true;

                double x = this.pastPosX.get(i);
                double y = this.pastPosY.get(i);
                double z = this.pastPosZ.get(i);

                double x1 = this.pastPosX.get(i - 1);
                double y1 = this.pastPosY.get(i - 1);
                double z1 = this.pastPosZ.get(i - 1);

                double a = Math.PI / 2 + this.angle;

                for (int j = 0; j < 16; j++)
                {
                    double ax = Math.cos(a);
                    double ay = Math.sin(a);

                    a += Math.PI / 8;
                    double ax2 = Math.cos(a);
                    double ay2 = Math.sin(a);

                    Drawing.drawing.setColor(80, 80, 80, (length - t) / length * 64, 0.5);
                    Drawing.drawing.addVertex(x + this.size / 2 * ax, y + this.size / 2 * ay, z);
                    Drawing.drawing.addVertex(x + this.size / 2 * ax2, y + this.size / 2 * ay2, z);

                    double t1 = Math.max(this.age - this.pastTime.get(i - 1), 0);
                    Drawing.drawing.setColor(80, 80, 80, (length - t1) / length * 64, 0.5);
                    Drawing.drawing.addVertex(x1 + this.size / 2 * ax2, y1 + this.size / 2 * ay2, z1);
                    Drawing.drawing.addVertex(x1 + this.size / 2 * ax, y1 + this.size / 2 * ay, z1);
                }
            }
            Game.game.window.setBatchMode(false, true, true, false);
        }

        if (this.destroyTimer <= 60)
            super.draw();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 2;
    }

    @Override
    public void setTargetLocation(double x, double y)
    {
        double dist = Math.min(1000 * this.getSpeed() / 3.125, Math.sqrt(Math.pow(x - this.posX, 2) + Math.pow(y - this.posY, 2)));
        this.vZ = dist / this.getSpeed() * 0.5 * BulletArc.gravity;
    }

    @Override
    public void addDestroyEffect()
    {
        if (!Game.enable3d)
            this.posY -= this.posZ - Game.tile_size / 2;

        super.addDestroyEffect();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 2;
    }
}

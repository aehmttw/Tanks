package tanks.bullet;

import tanks.AttributeModifier;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
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
    public double lastAddedAge = -1000;

    public BulletArc(double x, double y, Tank t, int bounces, boolean affectsMaxLiveBullets, ItemBullet ib)
    {
        super(x, y, bounces, t, affectsMaxLiveBullets, ib);
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

    public BulletArc(double x, double y, int bounces, Tank t, ItemBullet ib)
    {
        this(x, y, t, bounces, true, ib);
    }

    @Override
    public void update()
    {
        super.update();

        double gravMod = this.getAttributeValue(AttributeModifier.velocity, 1);

        this.vZ -= gravity * Panel.frameFrequency * gravMod;

        if (this.posZ < Game.tile_size / 2 && !this.destroy)
        {
            if (this.bounces > 0)
            {
                this.bounces--;
                this.posZ += 2 * ((Game.tile_size / 2) - this.posZ);
                this.vZ = Math.abs(this.vZ) * 0.75;

                if (!this.tank.isRemote)
                    this.checkCollision();

                this.checkCollisionLocal();
            }
            else
            {
                double dif = (this.posZ - Game.tile_size / 2) / this.vZ;
                this.posX -= dif * this.vX;
                this.posY -= dif * this.vY;

                this.vX = 0;
                this.vY = 0;
                this.vZ = 0;

                if (!this.tank.isRemote)
                    this.checkCollision();

                this.checkCollisionLocal();

                this.destroy = true;
            }

            Drawing.drawing.playSound("bullet_explode.ogg", (float) (Bullet.bullet_size / this.size));
        }

        if (!this.destroy)
            this.angle = this.getPolarDirection();

        if (this.age - this.lastAddedAge > 10)
        {
            int count = 1;
            if (this.pastPosX.isEmpty())
                count = 2;

            for (int i = 0; i < count; i++)
            {
                this.pastPosX.add(this.posX);

                if (Game.enable3d)
                    this.pastPosY.add(this.posY);
                else
                    this.pastPosY.add(this.posY - this.posZ + 25);

                this.pastPosZ.add(this.posZ);
                this.pastTime.add(this.age);

                this.lastAddedAge = this.age;
            }
        }
        else
        {
            this.pastPosX.set(this.pastPosX.size() - 1, this.posX);
            this.pastPosY.set(this.pastPosY.size() - 1, this.posY);
            this.pastPosZ.set(this.pastPosZ.size() - 1, this.posZ);
        }
    }

    public void draw()
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 2 * (60 - this.posZ / 32) * (1 - Math.min(this.destroyTimer / 60, 1)), 0.5);
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
            Game.game.window.shapeRenderer.setBatchMode(true, true, true, false);
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
            Game.game.window.shapeRenderer.setBatchMode(false, true, true, false);
        }

        if (this.destroyTimer <= 60)
            super.draw();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 2;

        double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;

        if (destroy)
            time = 0;

        double limit = 50;
        if (time <= limit && !ScreenGame.finishedQuick)
        {
            double frac;

            frac = 1 - time / limit;

            double s = (1.5 - frac) * this.size * 4;
            double d = Math.max(1 - (this.destroyTimer / this.maxDestroyTimer) * 2, 0);

            Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, frac * 255 * d, 1);
            Drawing.drawing.drawImage(frac * Math.PI / 2, "cursor.png", this.posX + this.vX * time, this.posY + this.vY * time, s, s);

            if (Game.glowEnabled)
            {
                Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, frac * 255 * d, 1);
                Drawing.drawing.fillGlow(this.posX + this.vX * time, this.posY + this.vY * time, this.size * 4, this.size * 4);
            }
        }
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

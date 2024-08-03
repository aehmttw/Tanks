package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemBullet2;
import tanks.tank.Tank;

public class BulletArc extends Bullet
{
    public static String bullet_name = "artillery";

    public double maxAge;
    public double angle;
    public static final double gravity = 0.1;

    public BulletArc()
    {
        this.init();
    }

    public BulletArc(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet2.ItemStackBullet ib)
    {
        super(x, y, bounces, t, affectsMaxLiveBullets, ib);
        this.init();
    }

    public BulletArc(double x, double y, int bounces, Tank t, ItemBullet2.ItemStackBullet ib)
    {
        this(x, y, bounces, t, true, ib);
    }

    protected void init()
    {
        this.enableExternalCollisions = false;
        this.playPopSound = false;
        this.playBounceSound = false;
        this.enableCollision = false;
        this.posZ = Game.tile_size / 2;
        this.maxDestroyTimer = 100;
        this.obstacleCollision = false;
        this.canBeCanceled = false;
        this.moveOut = false;
        this.trail3d = true;

        this.autoZ = false;
    }

    @Override
    public void update()
    {
        super.update();

        if (this.delay > 0)
            return;

        double gravMod = this.getAttributeValue(AttributeModifier.velocity, 1);

        this.vZ -= gravity * Panel.frameFrequency * gravMod;

        if (this.posZ < Game.tile_size / 2 && !this.destroy)
        {
            if (this.bounces > 0 && (this.posX >= 0 && this.posX <= Game.currentSizeX * Game.tile_size && this.posY >= 0 && this.posY <= Game.currentSizeY * Game.tile_size))
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
    }

    public void draw()
    {
        if (this.delay > 0)
            return;

        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 2 * (60 - this.posZ / 32) * (1 - Math.min(this.destroyTimer / 60, 1)), 0.5);
        Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 2, this.size * 2, true);
        Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

        if (!Game.enable3d)
            this.posY -= this.posZ - Game.tile_size / 4;

        super.draw();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 4;

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
            this.posY -= this.posZ - Game.tile_size / 4;

        super.addDestroyEffect();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 4;
    }
}

package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemBullet;
import tanks.tank.Tank;
import tanks.tankson.Property;

public class BulletArc extends Bullet
{
    public static String bullet_class_name = "artillery";

    public double angle;
    public static final double gravity = 0.1;

    @Property(id = "min_air_time", minValue = 0.0, name = "Minimum air time", category = BulletPropertyCategory.firing, desc = "The minimum time the bullet must spend in air before it lands \n \n 1 time unit = 0.01 seconds")
    public double minAirTime = 0;

    @Property(id = "min_range", minValue = 0.0, name = "Minimum range", category = BulletPropertyCategory.firing, desc = "The minimum distance this bullet may land from the tank that fired it \n \n 1 tile = 50 units")
    public double minRange = 0;

    @Property(id = "max_range", minValue = 0.0, name = "Maximum range", category = BulletPropertyCategory.firing,  desc = "The maximum distance this bullet may land from the tank that fired it. Set to 0 for unlimited. \n \n 1 tile = 50 units")
    public double maxRange = 0;

    @Property(id = "accuracy_spread_circle", minValue = 0.0, name = "Landing accuracy spread", category = BulletPropertyCategory.firing, desc = "The maximum distance between the target aim location and where the bullet actually lands, relative to the distance traveled by the bullet. Larger values are less accurate. \n \n A value of 1 corresponds to the bullet landing off by up to one tile per tile traveled.")
    public double accuracySpreadCircle = 0;

    protected double warningIndicatorTime = 100;

    public BulletArc()
    {
        this.init();
    }

    public BulletArc(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet ib)
    {
        super(x, y, t, affectsMaxLiveBullets, ib);
        this.init();
    }

    protected void init()
    {
        this.typeName = bullet_class_name;
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
        this.edgeCollision = false;
        this.showTrace = false;
        this.revertSpeed = false;

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
        this.posZ -= gravity * gravMod * Panel.frameFrequency * Panel.frameFrequency * 0.5;

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

            this.playArcPop();
        }

        if (!this.destroy)
            this.angle = this.getPolarDirection();
    }

    public void playArcPop()
    {
        Drawing.drawing.playSound("bullet_explode.ogg", (float) (Bullet.bullet_size / this.size));
    }

    public void draw()
    {
        if (this.delay > 0)
            return;

        this.drawShadow();

        if (!Game.enable3d)
            this.posY -= this.posZ - Game.tile_size / 4;

        super.draw();

        if (!Game.enable3d)
            this.posY += this.posZ - Game.tile_size / 4;

        double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;

        if (destroy)
            time = 0;

        if (time <= warningIndicatorTime && !ScreenGame.finishedQuick)
        {
            double frac;

            frac = 1 - time / warningIndicatorTime;
            this.drawCursor(frac, this.posX + this.vX * time, this.posY + this.vY * time);
        }
    }

    public void drawShadow()
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 2 * (60 - this.posZ / 32) * (1 - Math.min(this.destroyTimer / 60, 1)), 0.5);
        Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 2, this.size * 2, true);
        Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);
    }

    public void drawCursor(double frac, double x, double y)
    {
        double s = (1.5 - frac) * this.size * 4;
        double d = Math.max(1 - (this.destroyTimer / this.maxDestroyTimer) * 2, 0);
        Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, frac * 255 * d, 1);
        Drawing.drawing.drawImage(frac * Math.PI / 2, "cursor.png", x, y, s, s);

        if (Game.glowEnabled)
        {
            Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, frac * 255 * d, 1);
            Drawing.drawing.fillGlow(x, y, this.size * 4, this.size * 4);
        }
    }

    @Override
    public void setTargetLocation(double x, double y)
    {
        double angle = Math.random() * Math.PI * 2;
        double dx = x - this.posX;
        double dy = y - this.posY;
        double d = Math.sqrt(dx * dx + dy * dy);
        double s = Math.abs(this.speed);

        if (d > this.maxRange && this.maxRange > 0)
        {
            dx *= this.maxRange / d;
            dy *= this.maxRange / d;
            d = this.maxRange;
        }

        if (d < this.minRange)
        {
            dx *= this.minRange / d;
            dy *= this.minRange / d;
            d = this.minRange;
        }

        double offset = Math.random() * this.accuracySpreadCircle * d;

        double f = 1;
        if (d / s < this.minAirTime)
            f = d / (s * this.minAirTime);

        dx += Math.sin(angle) * offset;
        dy += Math.cos(angle) * offset;
        d = Math.sqrt(dx * dx + dy * dy);

        this.setMotionInDirection(this.posX + dx, this.posY + dy, s * f);
        this.speed *= f;
        this.vZ = d / s * 0.5 * BulletArc.gravity / f;
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

    @Override
    public double getRangeMin()
    {
        return this.minRange;
    }

    @Override
    public double getRangeMax()
    {
        return this.maxRange;
    }
}

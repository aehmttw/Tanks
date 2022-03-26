package tanks.tank;

import tanks.*;
import tanks.bullet.BulletArc;
import tanks.event.EventShootBullet;

/**
 * A stationary tank which lobs bullets over walls
 * @see TankBrown
 */
public class TankMustard extends TankAIControlled
{
    public double radius = 1000;
    protected double distance;

    public TankMustard(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 180, 160, 0, angle, ShootAI.straight);

        this.enableMovement = false;
        this.enableMineLaying = false;
        this.enablePredictiveFiring = true;
        this.liveBulletMax = 5;
        this.aimTurretSpeed = 0.02;
        this.enableLookingAtTargetEnemy = false;
        this.cooldownBase = 200;
        this.cooldownRandom = 100;
        this.bulletBounces = 0;

        this.coinValue = 4;
        this.turret.size *= 1.75;

        this.description = "A stationary tank which lobs---bullets over walls";
    }

    @Override
    public void update()
    {
        super.update();
        double pitch = Math.atan(this.distance / this.bulletSpeed * 0.5 * BulletArc.gravity / this.bulletSpeed);
        this.pitch -= Movable.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
    }

    @Override
    public void checkAndShoot()
    {
        if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) <= this.aimThreshold)
            this.shoot();
    }

    @Override
    public void setAimAngleStraight()
    {
        if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
        {
            Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
            r.size = Game.tile_size * this.hitboxSize - 1;
            r.enableBounciness = false;
            this.disableOffset = false;

            double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
            double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);

            double distBtwn = Movable.distanceBetween(this, this.targetEnemy);
            double time = distBtwn / Math.sqrt(this.bulletSpeed * this.bulletSpeed - speed * speed);

            double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

            double d = r.getDist();

            if (d * d > distSq && speed < this.bulletSpeed)
            {
                this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / this.bulletSpeed);

                double c = Math.cos(Movable.absoluteAngleBetween(targetEnemy.getLastPolarDirection(), this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY)));

                double a1 = Math.pow(this.bulletSpeed, 2) - Math.pow(targetEnemy.getLastSpeed(), 2);
                double b1 = -2 * targetEnemy.getLastSpeed() * Movable.distanceBetween(this, this.targetEnemy) * c;
                double c1 = -Math.pow(Movable.distanceBetween(this, targetEnemy), 2);
                double t = (-b1 + Math.sqrt(b1 * b1 - 4 * a1 * c1)) / (2 * a1);

                this.distance = Math.sqrt(Math.pow(targetEnemy.posX + t * targetEnemy.lastFinalVX - this.posX, 2) + Math.pow(targetEnemy.posY + t * targetEnemy.lastFinalVY - this.posY, 2));
            }
            else
            {
                this.aimAngle = this.getAngleInDirection(r.posX, r.posY);
                this.distance = Math.sqrt(Math.pow(r.posX - this.posX, 2) + Math.pow(r.posY - this.posY, 2));
            }
        }
        else
        {
            this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
            this.distance = Math.sqrt(Math.pow(targetEnemy.posX - this.posX, 2) + Math.pow(targetEnemy.posY - this.posY, 2));

            this.disableOffset = false;
        }
    }

    @Override
    public void shoot()
    {
        if (this.cooldown > 0 || this.targetEnemy == null || Movable.distanceBetween(this, this.targetEnemy) > this.radius ||
                Movable.angleBetween(this.aimAngle, this.angle) > this.aimThreshold && !this.disabled && !this.destroy)
            return;

        Drawing.drawing.playGlobalSound("arc.ogg", 1 / 2.5f);

        BulletArc b = new BulletArc(this.posX, this.posY, 0, this);
        b.team = this.team;
        b.addPolarMotion(this.aimAngle, this.bulletSpeed);
        b.vZ = this.distance / this.bulletSpeed * 0.5 * BulletArc.gravity;
        b.size = 25;
        b.bounces = this.bulletBounces;
        Game.eventsOut.add(new EventShootBullet(b));

        Game.movables.add(b);
        this.cooldown = this.cooldownBase;
    }
}

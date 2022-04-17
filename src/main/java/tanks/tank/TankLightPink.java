package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventTankLightPinkAngry;
import tanks.event.EventTankUpdateColor;

/**
 * A tank which gets angry on line of sight
 */
public class TankLightPink extends TankAIControlled
{
    public double angerTimer = 0;

    public double shootCycleTime = 60;
    public double shootTimer = 0;
    public int shotCount = 5;
    public int shots = 0;
    public boolean shooting = false;
    public double spread = Math.PI / 20;
    public int fanDirection;
    public double startAngle;

    public TankLightPink(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 211, 255, angle, ShootAI.reflect);

        this.aimTurretSpeed = 0.06;
        this.seekChance = 1;
        this.motionChangeChance = 0.001;
        this.coinValue = 10;
        this.enablePredictiveFiring = true;

        this.description = "A tank which gets angry---on line of sight";
    }

    @Override
    public void postUpdate()
    {
        double prevTimer = this.angerTimer;

        if (this.seesTargetEnemy)
            this.angerTimer = 500;
        else
            this.angerTimer -= Panel.frameFrequency;

        if (this.angerTimer <= 0)
        {
            this.maxSpeed = 1.0;
            this.colorG = 211;
            this.enablePathfinding = false;
            this.enableDefensiveFiring = true;

            this.cooldownBase = 120;
            this.cooldownRandom = 60;
            this.bullet.speed = 25.0 / 8;
            this.bullet.bounces = 1;
            this.bullet.effect = Bullet.BulletEffect.trail;
            this.shootAIType = ShootAI.reflect;

            if (prevTimer > 0)
            {
                Drawing.drawing.playGlobalSound("slowdown.ogg", 0.75f);
                Game.eventsOut.add(new EventTankUpdateColor(this));
            }
        }
        else
        {
            this.maxSpeed = 2.0;
            this.colorG = 150;
            this.enablePathfinding = true;
            this.enableDefensiveFiring = false;

            this.cooldownBase = 150;
            this.cooldownRandom = 0;
            this.bullet.speed = 25.0 / 4;
            this.bullet.bounces = 0;
            this.bullet.effect = Bullet.BulletEffect.fire;
            this.shootAIType = ShootAI.straight;

            if (prevTimer <= 0)
            {
                Game.eventsOut.add(new EventTankLightPinkAngry(this.networkID));

                Effect e1 = Effect.createNewEffect(this.posX, this.posY, this.posZ + this.size * 0.75, Effect.EffectType.exclamation);
                e1.size = this.size;
                Game.effects.add(e1);

                Drawing.drawing.playGlobalSound("timer.ogg", 1.25f);
                Game.eventsOut.add(new EventTankUpdateColor(this));
            }
        }
    }

    @Override
    public void shoot()
    {
        if (this.angerTimer <= 0)
        {
            super.shoot();
            return;
        }

        if (this.shooting)
            return;

        this.aimTimer = 10;
        this.aim = false;

        if (this.cooldown <= 0 && this.bullet.liveBullets < this.bullet.maxLiveBullets && !this.disabled && !this.destroy)
        {
            boolean cancel = false;
            for (double offset = -spread * 2; offset <= spread * 2; offset += spread)
            {
                Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bullet.bounces, this, 2.5);
                a.size = this.bullet.size;
                a.moveOut(this.size / 2.5);

                Movable m = a.getTarget();

                if (Team.isAllied(this, m))
                {
                    cancel = true;
                    break;
                }
            }

            if (!cancel)
            {
                this.shooting = true;
                this.shootTimer = -this.shootCycleTime / 2;
                this.shots = 0;
                this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
                this.startAngle = this.angle;
            }
        }
    }

    @Override
    public void updateTurretAI()
    {
        if (!this.shooting)
            super.updateTurretAI();
        else if (this.shootTimer <= -this.shootCycleTime / 2 && this.targetEnemy != null)
        {
            this.aimAngle = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

            double speed = this.aimTurretSpeed;

            if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
                speed /= 2;

            if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
                speed /= 2;

            if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
                speed /= 2;

            if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.aimThreshold)
            {
                if (Movable.angleBetween(this.angle, this.aimAngle) < 0)
                    this.angle += speed * Panel.frameFrequency;
                else
                    this.angle -= speed * Panel.frameFrequency;

                this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
            }
            else
            {
                this.angle = this.aimAngle;
                this.shootTimer += Panel.frameFrequency;
            }
        }
        else
        {
            this.angle = this.aimAngle + this.fanDirection * this.spread * (this.shotCount - 1) * (Math.abs(this.shootTimer / this.shootCycleTime) - 0.5);

            int s = (int) Math.round(this.shootTimer * this.shotCount / this.shootCycleTime);
            if (this.shots < s)
            {
                this.bullet.attemptUse(this);
                this.shots = s;
            }

            if (this.shootTimer > this.shootCycleTime)
            {
                this.shooting = false;
            }

            this.shootTimer += Panel.frameFrequency;
        }
    }
}

package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Team;
import tanks.bullet.Bullet;
import tanks.bullet.BulletExplosive;
import tanks.event.EventShootBullet;

/**
 * A tank which shoots explosive bullets
 */
public class TankOrangeRed extends TankAIControlled
{
    public TankOrangeRed(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 70, 0, angle, ShootAI.straight);

        this.enableMovement = true;
        this.maxSpeed = 0.75;
        this.enableMineLaying = false;
        this.enablePredictiveFiring = false;
        this.liveBulletMax = 2;
        this.cooldownRandom = 120;
        this.cooldownBase = 120;
        this.aimTurretSpeed = 0.02;
        this.bullet.bulletClass = BulletExplosive.class;
        this.bullet.bounces = 0;
        this.bullet.effect = Bullet.BulletEffect.trail;
        this.bullet.size = 20;
        this.bullet.heavy = true;
        this.bullet.name = "Explosive bullet";
        this.enableLookingAtTargetEnemy = true;
        this.motionChangeChance = 0.001;
        this.enablePathfinding = true;
        this.ignoreDestructible = true;
        this.enableDefensiveFiring = true;
        this.resistExplosions = true;
        this.enableMineAvoidance = false;

        this.coinValue = 4;

        this.description = "A tank which shoots explosive bullets";
    }

    public void shoot()
    {
        this.aimTimer = 10;
        this.aim = false;

        if (this.cooldown <= 0 && this.liveBullets < this.liveBulletMax && !this.disabled && !this.destroy)
        {
            double an = this.angle;

            if (this.targetEnemy != null && this.enablePredictiveFiring && this.shootAIType == ShootAI.straight)
                an = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

            Ray a2 = new Ray(this.posX, this.posY, an, this.bullet.bounces, this);
            a2.size = this.bullet.size;
            a2.getTarget();
            a2.ignoreDestructible = this.ignoreDestructible;

            double dist = a2.age;
            // Cancels if the bullet will hit another enemy
            double offset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((dist / 100.0), 2);

            if (this.disableOffset)
            {
                offset = 0;
                this.disableOffset = false;
            }

            Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bullet.bounces, this, 2.5);
            a.size = this.bullet.size;
            a.moveOut(this.size / 2.5);

            Movable m = a.getTarget();

            // Checks if the target is an enemy.
            if (!Team.isAllied(this, m))
            {
                for (Movable m2: Game.movables)
                {
                    if (Team.isAllied(m2, this) && m2 instanceof Tank && !((Tank) m2).resistExplosions && this.team.friendlyFire && Math.pow(m2.posX - a.posX, 2) + Math.pow(m2.posY - a.posY, 2) <= Math.pow(Mine.mine_size, 2))
                        return;
                }

                this.bullet.use(this);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        Explosion e = new Explosion(this.posX, this.posY, Mine.mine_radius, 2, true, this);
        e.explode();
    }
}

package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.Team;
import tanks.bullet.Bullet;
import tanks.bullet.BulletExplosive;
import tanks.event.EventLayMine;

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
        this.bulletBounces = 0;
        this.bulletClass = BulletExplosive.class;
        this.bulletEffect = Bullet.BulletEffect.trail;
        this.bulletSize = 20;
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

            Ray a2 = new Ray(this.posX, this.posY, an, this.bulletBounces, this);
            a2.size = this.bulletSize;
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

            Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bulletBounces, this, 2.5);
            a.size = this.bulletSize;
            a.moveOut(this.size / 2.5);

            Movable m = a.getTarget();

            if (!Team.isAllied(this, m))
            {
                for (Movable mm: Game.movables)
                {
                    if (Team.isAllied(mm, this) && mm instanceof Tank && !((Tank) mm).resistExplosions && this.team.friendlyFire && Math.pow(mm.posX - a.posX, 2) + Math.pow(mm.posY - a.posY, 2) <= Math.pow(Game.tile_size * 2.5, 2))
                        return;
                }

                this.launchBullet(offset);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        Mine m = new Mine(this.posX, this.posY, 0, this);
        Game.eventsOut.add(new EventLayMine(m));
        Game.movables.add(m);
    }
}

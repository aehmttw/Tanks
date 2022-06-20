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
        this.bullet.maxLiveBullets = 2;
        this.cooldownRandom = 120;
        this.cooldownBase = 120;
        this.aimTurretSpeed = 0.02;
        this.bullet.bulletClass = BulletExplosive.class;
        this.bullet.bounces = 0;
        this.bullet.effect = Bullet.BulletEffect.trail;
        this.bullet.size = 20;
        this.bullet.name = "Explosive bullet";
        this.enableLookingAtTargetEnemy = true;
        this.motionChangeChance = 0.001;
        this.enablePathfinding = true;
        this.ignoreDestructible = true;
        this.enableDefensiveFiring = true;
        this.resistExplosions = true;
        this.enableMineAvoidance = false;
        this.explodeOnDestroy = true;

        this.coinValue = 4;

        this.description = "A tank which shoots explosive bullets";
    }
}

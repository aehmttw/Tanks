package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletExplosive;

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
        this.cooldownRandom = 40;
        this.cooldownBase = 160;
        this.turretAimSpeed = 0.02;
        this.bullet.bulletClass = BulletExplosive.class;
        this.bullet.bounces = 0;
        this.bullet.effect = Bullet.BulletEffect.trail;
        this.bullet.size = 20;
        this.bullet.name = "Explosive bullet";
        this.enableLookingAtTargetEnemy = true;
        this.turnChance = 0.001;
        this.enablePathfinding = true;
        this.aimIgnoreDestructible = true;
        this.enableDefensiveFiring = true;
        this.resistExplosions = true;
        this.enableMineAvoidance = false;
        this.explodeOnDestroy = true;
        this.baseModel = TankModels.diagonalstripes_base_model;
        this.emblem = "emblems/bang.png";
        this.emblemR = 159;
        this.emblemG = 67;
        this.emblemB = 32;
        this.coinValue = 4;

        this.description = "A tank which shoots explosive bullets";
    }
}

package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;

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
        this.bullet.bulletClass = BulletArc.class;
        this.bullet.maxLiveBullets = 5;
        this.bullet.effect = Bullet.BulletEffect.none;
        this.bullet.size = 25;
        this.turretAimSpeed = 0.02;
        this.enableLookingAtTargetEnemy = false;
        this.cooldownBase = 200;
        this.cooldownRandom = 100;
        this.bullet.bounces = 0;
        this.aimAccuracyOffset = 0;
        this.bullet.name = "Artillery shell";
        this.colorModel = TankModels.fixed_color_model;

        this.coinValue = 4;
        this.turretSize *= 1.75;

        this.description = "A stationary tank which lobs bullets over walls";
    }
}

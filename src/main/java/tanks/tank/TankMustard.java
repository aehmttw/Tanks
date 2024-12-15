package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultBullets;

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

        this.setBullet(DefaultBullets.artillery_shell);

        this.turretAimSpeed = 0.02;
        this.enableLookingAtTargetEnemy = false;
        this.cooldownBase = 200;
        this.cooldownRandom = 100;
        this.aimAccuracyOffset = 0;

        if (Game.tankTextures)
        {
            this.colorModel = TankModels.fixed.color;
        }

        this.coinValue = 4;
        this.turretSize *= 1.75;

        this.description = "A stationary tank which lobs bullets over walls";
    }
}

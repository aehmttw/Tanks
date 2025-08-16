package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

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
        this.cooldownRandom = 40;
        this.cooldownBase = 160;
        this.turretAimSpeed = 0.02;
        this.enableLookingAtTargetEnemy = true;
        this.turnChance = 0.001;
        this.enablePathfinding = true;
        this.aimIgnoreDestructible = true;
        this.enableDefensiveFiring = true;
        this.resistExplosions = true;
        this.enableMineAvoidance = false;
        this.explodeOnDestroy = this.getMine().explosion;

        this.setBullet(DefaultItems.explosive_bullet);

        if (Game.tankTextures)
        {
            this.baseSkin = TankModels.diagonalStripes;
            this.emblem = "emblems/bang.png";
            this.emblemColor.red = 159;
            this.emblemColor.green = 67;
            this.emblemColor.blue = 32;
        }

        this.coinValue = 4;

        this.description = "A tank which shoots explosive bullets";
    }
}

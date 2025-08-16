package tanks.tank;

import tanks.Game;

/**
 * A tank which mimics the closest tank it sees
 */
public class TankMimic extends TankAIControlled
{
    public TankMimic(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.reflect);

        this.enableMovement = true;
        this.enablePathfinding = true;
        this.seekChance = 1;
        this.cooldownBase = 200;
        this.cooldownRandom = 400;
        this.enableMineLaying = true;

        this.baseSkin = TankModels.checkerboard;
        this.colorSkin = TankModels.checkerboard;
        this.turretSkin = TankModels.checkerboard;
        this.turretBaseSkin = TankModels.checkerboard;

        this.avoidanceSeekOpenSpaces = false;
        this.bulletAvoidBehavior = BulletAvoidBehavior.intersect;

        this.transformMimic = true;

        this.coinValue = 10;

        this.description = "A tank which mimics the closest tank it sees";
    }
}

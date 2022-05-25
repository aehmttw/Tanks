package tanks.tank;

import basewindow.Model;
import tanks.*;

/**
 * A tank which mimics the closest tank it sees
 */
public class TankMimic extends TankAIControlled
{
    public static Model base_model;
    public static Model color_model;
    public static Model turret_model;
    public static Model turret_base_model;

    public TankMimic(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.reflect);

        this.enableMovement = true;
        this.enablePathfinding = true;
        this.seekChance = 1;
        this.cooldownBase = 200;
        this.cooldownRandom = 400;
        this.enableMineLaying = true;

        this.baseModel = base_model;
        this.colorModel = color_model;
        this.turretModel = turret_model;
        this.turretBaseModel = turret_base_model;

        this.mimic = true;

        this.coinValue = 10;

        this.description = "A tank which mimics the---closest tank it sees";
    }
}

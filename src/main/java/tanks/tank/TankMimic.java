package tanks.tank;

import basewindow.Model;
import tanks.*;

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

        this.baseModel = TankModels.mimic_base_model;
        this.colorModel = TankModels.mimic_color_model;
        this.turretModel = TankModels.mimic_turret_model;
        this.turretBaseModel = TankModels.mimic_turret_base_model;

        this.transformMimic = true;

        this.coinValue = 10;

        this.description = "A tank which mimics the closest tank it sees";
    }
}

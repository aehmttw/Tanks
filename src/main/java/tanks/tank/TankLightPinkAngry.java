package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;

public class TankLightPinkAngry extends TankAIControlled
{
    public TankLightPinkAngry(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 150, 255, angle, ShootAI.straight);

        this.secondaryColorR = Turret.calculateSecondaryColor(255);
        this.secondaryColorG = Turret.calculateSecondaryColor(211);
        this.secondaryColorB = Turret.calculateSecondaryColor(255);

        this.turretAimSpeed = 0.06;
        this.seekChance = 1;
        this.turnChance = 0.001;
        this.coinValue = 10;
        this.enablePredictiveFiring = true;
        this.bullet.cooldownBase = 1;
        this.bullet.maxLiveBullets = 0;

        this.maxSpeed = 2.0;
        this.enablePathfinding = true;
        this.enableDefensiveFiring = false;

        this.cooldownBase = 150;
        this.cooldownRandom = 0;
        this.bullet.speed = 25.0 / 4;
        this.bullet.bounces = 0;
        this.bullet.effect = Bullet.BulletEffect.fire;

        if (Game.tankTextures)
        {
            this.emblem = "emblems/angry.png";
            this.emblemR = 200;
        }

        // TODO fix music in mimic
        this.shotRoundCount = 5;

        this.description = "A tank which gets angry on line of sight, shooting fans of bullets";
    }
}

package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;

/**
 * A tank which gets angry on line of sight
 */
public class TankLightPink extends TankAIControlled
{
    public TankLightPink(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 211, 255, angle, ShootAI.reflect);

        this.turretAimSpeed = 0.06;
        this.seekChance = 1;
        this.turnChance = 0.001;
        this.coinValue = 10;
        this.enablePredictiveFiring = true;
        this.bullet.cooldownBase = 1;
        this.bullet.maxLiveBullets = 0;
        this.maxSpeed = 1.0;
        this.enablePathfinding = false;
        this.enableDefensiveFiring = true;
        this.cooldownBase = 120;
        this.cooldownRandom = 60;
        this.bullet.bounces = 1;
        this.bullet.effect = Bullet.BulletEffect.trail;
        this.shootAIType = ShootAI.reflect;
        this.avoidanceSeekOpenSpaces = true;
        this.bulletAvoidBehvavior = BulletAvoidBehavior.dodge;

        if (Game.tankTextures)
        {
            this.emblem = "emblems/angry.png";
            this.emblemR = this.secondaryColorR;
            this.emblemG = this.secondaryColorG;
            this.emblemB = this.secondaryColorB;
        }

        this.sightTransformTank = new TankLightPinkAngry(this.name, x, y, angle);

        this.description = "A tank which gets angry on line of sight";
    }
}

package tanks.tank;

import tanks.Game;
import tanks.bullet.DefaultItems;

import java.util.HashSet;

public class TankLightPinkAngry extends TankAIControlled
{
    public TankLightPinkAngry(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 150, 255, angle, ShootAI.straight);

        this.secondaryColor.red = Turret.calculateSecondaryColor(255);
        this.secondaryColor.green = Turret.calculateSecondaryColor(211);
        this.secondaryColor.blue = Turret.calculateSecondaryColor(255);

        this.turretAimSpeed = 0.06;
        this.seekChance = 1;
        this.turnChance = 0.001;
        this.coinValue = 10;
        this.enablePredictiveFiring = true;

        this.maxSpeed = 2.0;
        this.enablePathfinding = true;
        this.enableDefensiveFiring = false;

        this.cooldownBase = 150;
        this.cooldownRandom = 0;

        this.setBullet(DefaultItems.rocket);
        this.getBullet().maxLiveBullets = 0;

        this.bulletAvoidBehavior = BulletAvoidBehavior.aggressive_dodge;
        this.targetEnemySightBehavior = TargetEnemySightBehavior.sidewind;

        if (Game.tankTextures)
        {
            this.emblem = "emblems/angry.png";
            this.emblemColor.red = 200;
            this.baseSkin = TankModels.diagonalStripes;
        }

        HashSet<String> musics = Game.registryTank.tankMusics.get("lightpink_angry");

        if (musics != null)
            this.musicTracks.addAll(musics);

        this.shotRoundCount = 5;

        this.description = "A tank which gets angry on line of sight, shooting fans of bullets";
    }
}

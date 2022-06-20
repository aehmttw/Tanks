package tanks.tank;

import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.event.EventCreateCustomTank;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryTank;

/**
 * A tank which spawns mini tanks and shoots 2-bounce rockets
 */
public class TankPink extends TankAIControlled
{
	public TankPink(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 127, 127, angle, ShootAI.reflect);
		this.enableMovement = false;
		this.enableMineLaying = false;
		this.bullet.maxLiveBullets = 2;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.aimTurretSpeed = 0.02;
		this.bullet.bounces = 2;
		this.bullet.speed = 25.0 / 4;
		this.bullet.effect = Bullet.BulletEffect.fireTrail;
		this.bullet.name = "Bouncy fire bullet";
		this.turretIdleTimerBase = 25;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtTargetEnemy = false;

		this.spawnsTanks = true;
		this.spawnedTankEntry = Game.registryTank.getEntry("mini");

		this.coinValue = 12;

		this.description = "A tank which spawns---mini tanks and shoots---2-bounce rockets";
	}
}

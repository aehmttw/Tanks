package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.Team;
import tanks.bullet.Bullet;
import tanks.bullet.BulletBoost;
import tanks.bullet.BulletHealing;
import tanks.event.EventLayMine;
import tanks.event.EventTankUpdateColor;

/**
 * A tank which adds extra health to its allies and becomes explosive as a last stand
 */
public class TankMedic extends TankAIControlled
{
	public TankMedic(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.straight);

		this.texture = "emblems/medic.png";
		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.bullet.maxLiveBullets = 1;
		this.cooldownRandom = 0;
		this.cooldownBase = 0;
		this.aimTurretSpeed = 0.02;
		this.bullet.bounces = 0;
		this.bullet.effect = Bullet.BulletEffect.none;
		this.bullet.damage = 0.01;
		this.bullet.bulletClass = BulletHealing.class;
		this.bullet.cooldownBase = 0;
		this.bullet.name = "Healing ray";
		this.motionChangeChance = 0.001;
		this.enablePathfinding = true;
		this.seekChance = 0.01;
		this.dealsDamage = false;
		this.commitsSuicide = true;

		this.coinValue = 4;

		this.description = "A tank which adds extra health---to its allies and becomes---explosive as a last stand";
	}
}
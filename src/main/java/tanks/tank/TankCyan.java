package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletFreeze;

import java.util.Arrays;

public class TankCyan extends TankAIControlled
{
	public TankCyan(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 128, 255, 255, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 0.75;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 240;
		this.aimTurretSpeed = 0.01;
		this.bulletBounces = 0;
		this.bulletEffect = Bullet.BulletEffect.ice;
		this.bulletSpeed = 25.0 / 8;
		this.bulletDamage = 0.25;
		this.bulletClass = BulletFreeze.class;
		this.enableLookingAtTargetEnemy = false;
		this.motionChangeChance = 0.001;

		this.attributeImmunities.addAll(Arrays.asList("ice_slip", "ice_accel", "ice_max_speed", "freeze"));
		
		this.coinValue = 4;

		this.description = "A support tank which shoots freezing---bullets that deal low damage";
	}
}

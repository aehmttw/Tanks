package tanks.tank;

import tanks.*;
import tanks.bullet.BulletLaser;
import tanks.event.EventTankRedUpdateCharge;

public class TankRed extends TankAIControlled
{
	public boolean lineOfSight = false;
	public double idleTime = 0;

	public TankRed(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 0, 0, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.bulletBounces = 0;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.setFrameDamageMultiplier = false;
		this.cooldown = 250;
		this.cooldownBase = 100;
		this.cooldownRandom = 0;
		this.bulletClass = BulletLaser.class;
		this.bulletSound = "laser.ogg";
		this.bulletSoundVolume = 0.3f;

		this.coinValue = 6;

		this.description = "A stationary tank which---shoots deadly lasers";
	}

	@Override
	public void update()
	{
		this.idleTime += Panel.frameFrequency;

		if (this.idleTime >= 300)
			this.cooldownBase = 100;

		this.lineOfSight = false;

		if (this.cooldown < this.cooldownBase)
		{
			if (!this.destroy)
				Game.eventsOut.add(new EventTankRedUpdateCharge(this.networkID, (cooldownBase - this.cooldown) / cooldownBase));

			this.colorR = Math.min((200 + (cooldownBase - this.cooldown) / cooldownBase * 55), 255);
			this.colorG = (cooldownBase - this.cooldown) / cooldownBase * 100;
			this.colorB = (cooldownBase - this.cooldown) / cooldownBase * 100;
		}

		super.update();

		if (!lineOfSight)
		{
			this.cooldown = Math.max(this.cooldown, this.cooldownBase);
			this.colorR = 200;
			this.colorG = 0;
			this.colorB = 0;

			if (!this.destroy)
				Game.eventsOut.add(new EventTankRedUpdateCharge(this.networkID, 0));
		}
	}

	@Override
	public void shoot()
	{
		if (this.disabled || this.destroy)
			return;

		this.lineOfSight = true;

		if (this.cooldown > 0)
		{
			this.idleTime = 0;
			if (Math.random() * cooldownBase * Game.effectMultiplier > cooldown && Game.effectsEnabled)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.charge);

				e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * 50 - 25));
				e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * 50 - 25));
				e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * 50 - 25));

				Game.effects.add(e);
			}
			return;

		}

		Ray r = new Ray(this.posX, this.posY, this.angle, 0, this);
		r.moveOut(4);
		Movable m = r.getTarget();

		if (!Team.isAllied(m, this))
		{
			this.cooldownBase = this.cooldownBase * 0.75 + 1;
			super.launchBullet(0);

			if (this.targetEnemy == null || this.targetEnemy.destroy)
				this.cooldownBase = 100;

			this.cooldown = Math.max(this.cooldown, this.cooldownBase);
		}
		else
			this.cooldown = (Math.max(this.cooldown, 0));
	}
}
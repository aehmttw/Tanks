package tanks.tank;

import tanks.*;
import tanks.bullet.BulletLaser;
import tanks.event.EventShootBullet;
import tanks.event.EventTankRedUpdateCharge;

public class TankRed extends TankAIControlled
{
	public boolean lineOfSight = false;
	public double maxCooldown = 100;
	public double idleTime = 0;

	public TankRed(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 200, 0, 0, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.cooldown = 250;

		this.coinValue = 3;

		this.description = "A stationary tank which---shoots deadly lasers";
	}

	@Override
	public void update()
	{
		this.idleTime += Panel.frameFrequency;

		if (this.idleTime >= 300)
			this.maxCooldown = 100;

		this.lineOfSight = false;

		if (this.cooldown < this.maxCooldown)
		{
			if (!this.destroy)
				Game.eventsOut.add(new EventTankRedUpdateCharge(this.networkID, (maxCooldown - this.cooldown) / maxCooldown));

			this.colorR = Math.min((200 + (maxCooldown - this.cooldown) / maxCooldown * 55), 255);
			this.colorG = (maxCooldown - this.cooldown) / maxCooldown * 100;
			this.colorB = (maxCooldown - this.cooldown) / maxCooldown * 100;
		}

		super.update();

		if (!lineOfSight)
		{
			this.cooldown = Math.max(this.cooldown, this.maxCooldown);
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
		this.lineOfSight = true;

		if (this.cooldown > 0)
		{
			this.idleTime = 0;
			if (Math.random() * maxCooldown > cooldown && Game.fancyGraphics)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.charge);

				double var = 50;
				e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

				Game.effects.add(e);
			}
			return;

		}

		Ray r = new Ray(this.posX, this.posY, this.angle, 0, this);
		r.moveOut(4);
		Movable m = r.getTarget();

		if (!Team.isAllied(m, this))
		{
			this.maxCooldown = this.maxCooldown * 0.75 + 1;
			BulletLaser b = new BulletLaser(this.posX, this.posY, 0, this);
			b.team = this.team;
			b.setPolarMotion(this.angle, 25.0/8);
			b.moveOut(16);
			Game.movables.add(b);
			Drawing.drawing.playGlobalSound("laser.ogg");

			if (this.targetEnemy.destroy)
				this.maxCooldown = 100;

			this.cooldown = Math.max(this.cooldown, this.maxCooldown);
		}
		else
		{
			this.cooldown = (Math.max(this.cooldown, 0));
		}
	}
}
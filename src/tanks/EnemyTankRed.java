package tanks;

import java.awt.Color;

public class EnemyTankRed extends EnemyTank
{
	boolean lineOfSight = false;
	double maxCooldown = 150;
	
	public EnemyTankRed(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(200, 0, 0), angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtPlayer = false;
		this.cooldown = 250;
		
		this.coinValue = 3;
	}
	
	@Override
	public void update()
	{
		this.lineOfSight = false;
		
		if (this.cooldown < this.maxCooldown)
			this.color = new Color(Math.min((int) (200 + (maxCooldown - this.cooldown) / maxCooldown * 55), 255), (int)((maxCooldown - this.cooldown) / maxCooldown * 100), (int) ((maxCooldown - this.cooldown) / maxCooldown * 100));	
	
		super.update();
		
		if (!lineOfSight)
			this.cooldown = Math.max(this.cooldown, this.maxCooldown);
	}
	
	@Override
	public void shoot() 
	{
		this.lineOfSight = true;
		if (this.cooldown > 0)
		{
			if (Math.random() * maxCooldown > cooldown && Game.graphicalEffects)
			{
				Effect e = new Effect(this.posX, this.posY, Effect.EffectType.charge);
				double var = 50;
				e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
				Game.effects.add(e);
			}
			return;

		}

		LaserBullet b = new LaserBullet(this.posX, this.posY, Color.red, 0, this);
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		b.shoot();
		this.cooldown = Math.max(this.cooldown, this.maxCooldown);
	}
}

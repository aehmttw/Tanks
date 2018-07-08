package tanks.legacy;

import java.awt.Color;
import tanks.*;

public class EnemyTankRed extends Tank
{
	int moveTime = 0;
	double aimAngle = 0;

	int age = 0;

	double maxCooldown = 150;
	
	public EnemyTankRed(double x, double y, int size) 
	{
		super(x, y, size, new Color(200, 0, 0));
		this.liveBulletMax = 1;
		this.cooldown = maxCooldown;
	}

	public EnemyTankRed(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
		this.coinValue = 3;
	}

	@Override
	public void shoot() 
	{
		if (this.cooldown > 0)
		{
			this.cooldown -= Panel.frameFrequency;

			if (Math.random() * maxCooldown > cooldown && Game.graphicalEffects)
			{
				Effect e = new Effect(this.posX, this.posY, Effect.EffectType.charge);
				double var = 50;
				e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
				Game.effects.add(e);
			}
			return;

		}

		LaserBullet b = new LaserBullet(this.posX, this.posY, Color.blue, 0, this);
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		b.shoot();
		this.cooldown = maxCooldown;
	}

	@Override
	public void update()
	{
		this.age++;

		this.color = new Color(Math.min((int) (200 + (maxCooldown - this.cooldown) / maxCooldown * 55), 255), (int)((maxCooldown - this.cooldown) / maxCooldown * 100), (int) ((maxCooldown - this.cooldown) / maxCooldown * 100));

		if (!this.destroy)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);
			Movable m = a.getTarget();
			//if (m != null)
			//	System.out.println(((Tank)m).color);

			if (!(m == null))
			{
				if(m.equals(Game.player))
				{
					this.shoot();
				}
				else
				{
					this.cooldown = maxCooldown;
				}
			}
			else
			{
				this.cooldown = maxCooldown;
			}
		}	

		//this.moveTime--;

		this.aimAngle = this.getAngleInDirection(Game.player.posX, Game.player.posY);


		if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
			//if ((this.aimAngle - this.angle) % (Math.PI * 2) < (this.angle - this.aimAngle) % (Math.PI * 2))
			this.angle+=0.005;
		else
			this.angle-=0.005;

		//if (Math.abs(this.aimAngle - this.angle) < 0.02)
		//	this.angle = this.aimAngle;

		this.angle = this.angle % (Math.PI * 2);

		super.update();
	}
}

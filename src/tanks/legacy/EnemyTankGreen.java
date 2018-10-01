package tanks.legacy;

import java.awt.Color;
import tanks.*;
import tanks.tank.Tank;

@Deprecated
public class EnemyTankGreen extends Tank
{
	double lockedAngle = 0;
	double searchAngle = 0;
	double aimAngle = 0;

	int idleTimer = (int) (Math.random() * 500) + 25;
			
	int cooldown = 0;

	int aimTimer = 0;

	boolean aim = false;

	enum Phase {clockwise, counterClockwise, aiming}

	Phase searchPhase = Phase.clockwise;
	Phase idlePhase = Phase.clockwise;

	public EnemyTankGreen(double x, double y, int size) 
	{
		super("legacy-green", x, y, size, new Color(100, 200, 0));
		this.liveBulletMax = 1;
		if (Math.random() < 0.5)
			this.idlePhase = Phase.counterClockwise;
		
		this.coinValue = 10;
	}
	public EnemyTankGreen(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		
		this.aimTimer = (int)(Math.random() * 25)+10;
		this.aim = false;
		
		if (this.cooldown <= 0)
		{
			double offset = Math.random() * 0.1 - 0.05;
			
			Ray a = new Ray(this.posX, this.posY, this.angle + offset, 2, this);
			Movable m = a.getTarget();
			if (!(m instanceof Tank && !m.equals(Game.player)))
			{
				//if (m != null)
				//	System.out.println(((Tank)m).color);
				Bullet b = new Bullet(this.posX, this.posY, 2, this);
				b.setPolarMotion(angle + offset, 25.0/2);
				b.moveOut(4);
				//b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/2);
				b.effect = Bullet.BulletEffect.fireTrail;
				Game.movables.add(b);
				this.cooldown = 150;
			}
		}
		
	}

	@Override
	public void update()
	{
		//System.out.println(this.idlePhase + " " + this.searchPhase + " " + aim);
		if (!this.destroy)
		{
			if (this.searchPhase == Phase.clockwise)
			{
				searchAngle += Math.random() * 0.1;
				//if (Math.random() < 0.01)
				//	this.searchPhase = Phase.counterClockwise;
			}
			else if (this.searchPhase == Phase.counterClockwise)
			{
				searchAngle -= Math.random() * 0.1;
				//if (Math.random() < 0.01)
				//	this.searchPhase = Phase.clockwise;
			}
			else
			{
				searchAngle = this.lockedAngle + Math.random() * 0.2 - 0.1;
				this.aimTimer--;
				if (this.aimTimer <= 0)
				{
					this.aimTimer = 0;
					if (Math.random() < 0.5)
						this.searchPhase = Phase.clockwise;
					else
						this.searchPhase = Phase.counterClockwise;
				}
			}

			Ray ray = new Ray(this.posX, this.posY, this.searchAngle, 2, this);
			Movable target = ray.getTarget();
			if (target != null)
				if (target.equals(Game.player))
				{
					this.lockedAngle = this.angle;
					this.searchPhase = Phase.aiming;
					this.aimAngle = this.searchAngle % (Math.PI * 2);
					this.aim = true;

				}
			
			if (aim)
			{
				if (Math.abs(this.aimAngle - this.angle) < 0.06)
				{
					this.angle = this.aimAngle;
					this.shoot();
				}
				else
				{
					if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
					//if ((this.aimAngle - this.angle) % (Math.PI * 2) < (this.angle - this.aimAngle) % (Math.PI * 2))
						this.angle+=0.02;
					else
						this.angle-=0.02;
					
					this.angle = this.angle % (Math.PI * 2);
				}
			}
			else
			{
				if (this.idlePhase == Phase.clockwise)
					this.angle += 0.005;
				else
					this.angle -= 0.005;
				
				this.idleTimer--;
				
				if (this.idleTimer <= 0)
				{
					if (this.idlePhase == Phase.clockwise)
						this.idlePhase = Phase.counterClockwise;
					else
						this.idlePhase = Phase.clockwise;
						
					this.idleTimer = (int) (Math.random() * 500) + 25;
				}
			}

		}
		this.cooldown--;
		
		super.update();

		//if (Math.random() * 300 < 1 && this.liveBullets < this.liveBulletMax)
		//	this.shoot();
	}
}

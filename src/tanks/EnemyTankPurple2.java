package tanks;

import java.awt.Color;
import java.util.ArrayList;

public class EnemyTankPurple2 extends Tank
{
	double lockedAngle = 0;
	double searchAngle = 0;
	double aimAngle = 0;
	double direction = ((int)(Math.random() * 8))/2.0;

	int[] distances = new int[8];
	
	int idleTimer = (int) (Math.random() * 500) + 25;
	int cooldown = 0;
	int aimTimer = 0;
	boolean aim = false;
	
	int age = 0;

	enum Phase {clockwise, counterClockwise, aiming}

	Phase searchPhase = Phase.clockwise;
	Phase idlePhase = Phase.clockwise;

	public EnemyTankPurple2(double x, double y, int size) 
	{
		super(x, y, size, new Color(150, 0, 200));
		this.liveBulletMax = 1;
		if (Math.random() < 0.5)
			this.idlePhase = Phase.counterClockwise;
	}
	public EnemyTankPurple2(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		this.lockedAngle = this.angle;
		//this.searchPhase = Phase.aiming;
		this.aimTimer = (int)(Math.random() * 25)+10;
		this.aim = false;

		if (this.cooldown <= 0 && this.liveBullets < 5)
		{
			double offset = Math.random() * 0.1 - 0.05;

			Ray a = new Ray(this.posX, this.posY, this.angle + offset, 1, this);
			Movable m = a.getTarget();
			if (!(m instanceof Tank && !m.equals(Game.player)))
			{
				//if (m != null)
				//	System.out.println(((Tank)m).color);
				Bullet b = new Bullet(this.posX, this.posY, Color.blue, 1, this);
				b.setPolarMotion(angle + offset, 25.0/4);
				b.moveOut(8);
				//b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/2);
				b.effect = Bullet.BulletEffect.trail;
				Game.movables.add(b);
				this.cooldown = 40;
			}
		}

	}

	@Override
	public void update()
	{
		this.age++;
		
		if (!this.destroy)
		{
			if (Math.random() < 0.01 || this.hasCollided)
			{
				ArrayList<Double> directions = new ArrayList<Double>();

				for (double dir = 0; dir < 4; dir += 0.5)
				{
					Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tank_size);
					r.size = Game.tank_size;
					
					int dist = r.getDist();
					
					distances[(int) (dir * 2)] = dist;
					
					if (!(dir == (this.direction + 2) % 4 || dir == (this.direction + 1.5) % 4 || dir == (this.direction + 2.5) % 4))
					{
						if (dist >= 4)
							directions.add(dir);
					}
				}	
				
				int chosenDir = (int)(Math.random() * directions.size());
				
				if (directions.size() == 0)
					this.direction = (this.direction + 2) % 4;
				else
					this.direction = directions.get(chosenDir);
			}
			
			this.setPolarMotion(this.direction / 2 * Math.PI, 2.5);
			
			double offsetMotion = Math.sin(this.age * 0.02);
			if (offsetMotion < 0)
			{
				int dist = this.distances[(int) (this.direction * 2 + 6) % 8];
				offsetMotion *= Math.max(1, (dist - 1) / 5.0);
			}
			else
			{
				int dist = this.distances[(int) (this.direction * 2 + 2) % 8];
				offsetMotion *= Math.max(1, (dist - 1) / 5.0);
			}
			
			this.addPolarMotion((this.direction + 1) / 2 * Math.PI, offsetMotion);


			if (this.searchPhase == Phase.clockwise)
			{
				searchAngle += Math.random() * 0.2;
			}
			else if (this.searchPhase == Phase.counterClockwise)
			{
				searchAngle -= Math.random() * 0.2;
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

			Ray ray = new Ray(this.posX, this.posY, this.searchAngle, 1, this);
			Movable target = ray.getTarget();
			if (target != null)
				if (target.equals(Game.player))
				{
					this.aim = true;
					this.aimAngle = this.searchAngle % (Math.PI * 2);
				}

			if (aim)
			{
				if (Math.abs(this.aimAngle - this.angle) < 0.06)
					this.shoot();
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

package tanks.legacy;

import java.awt.Color;
import java.util.ArrayList;

import tanks.*;

public class EnemyTankDarkRed extends Tank
{
	//double lockedAngle = 0;
	//double searchAngle = 0;
	
	boolean locked = false;
	double aimAngle = 0;
	double direction = ((int)(Math.random() * 8))/2.0;
	double avoidDirection = 0;


	int[] distances = new int[8];

	int idleTimer = (int) (Math.random() * 500) + 25;
	int cooldown = 0;
	int mineTimer = (int) (Math.random() * 2000 + 2000);
	int aimTimer = 0;
	int avoidTimer = 0;
	//boolean aim = false;

	//boolean straightShoot = false;
	boolean moveAwayFromPlayer = false;

	double fleeDirection = Math.PI / 4;

	int age = 0;

	enum RotationPhase {clockwise, counterClockwise, aiming}

	//RotationPhase searchPhase = RotationPhase.clockwise;
	//RotationPhase idlePhase = RotationPhase.clockwise;

	public EnemyTankDarkRed(double x, double y, int size) 
	{
		super(x, y, size, new Color(100, 0, 0));

		this.coinValue = 10;
	}
	public EnemyTankDarkRed(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		if (!locked)
			return;
		
		this.aimTimer = 10;

		if (this.cooldown <= 0 && this.liveBullets < 10)
		{
			double offset = Math.random() * 0.1 - 0.05;

			Ray a = new Ray(this.posX, this.posY, this.angle + offset, 1, this);
			Movable m = a.getTarget();
			if (!(m instanceof Tank && !m.equals(Game.player)))
			{
				//if (m != null)
				//	System.out.println(((Tank)m).color);
				Bullet b = new Bullet(this.posX, this.posY, Color.black, 0, this);
				b.setPolarMotion(angle + offset, 25.0/2);
				b.moveOut(4);
				//b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/2);
				Game.movables.add(b);
				this.cooldown = 5;
				b.size /= 2;
				b.damage = 0.25;
			}
		}

	}

	@Override
	public void update()
	{
		this.angle = this.angle % (Math.PI * 2);
		//System.out.println(this.aimAngle + " " + this.angle);

		this.age++;

		if (!this.destroy)
		{
			boolean avoid = false;
			ArrayList<Bullet> toAvoid = new ArrayList<Bullet>();

			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (Game.movables.get(i) instanceof Bullet && !Game.movables.get(i).destroy)
				{
					Bullet b = (Bullet) Game.movables.get(i);
					if (Math.abs(b.posX - this.posX) < Game.tank_size * 10 && Math.abs(b.posY - this.posY) < Game.tank_size * 10)
					{
						Ray r = b.getRay();

						Movable m = r.getTarget(1.5, 1.5);
						if (m != null)
						{
							if (m.equals(this))
							{
								avoid = true;
								toAvoid.add(b);
							}
						}
					}
				}
			}

			if (avoid)
			{
				Bullet nearest = null;
				double nearestDist = 1000;
				for (int i = 0; i < toAvoid.size(); i++)
				{
					double dist = Movable.distanceBetween(this, toAvoid.get(i));
					if (dist < nearestDist)
					{
						nearest = toAvoid.get(i);
						nearestDist = dist;
					}
				}

				this.avoidTimer = 20;
				this.avoidDirection = nearest.getPolarDirection() + fleeDirection;
			}
			
			if (this.avoidTimer > 0)
			{
				this.avoidTimer--;
				this.setPolarMotion(avoidDirection, 2.5);
				this.addPolarMotion(this.direction / 2 * Math.PI, 2.5);

			}
			else
			{
				fleeDirection = -fleeDirection;

				if (this.moveAwayFromPlayer)
				{
					this.setMotionAwayFromDirection(Game.player.posX, Game.player.posY, 2.5);
				}
				else
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
				}
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
			}

			this.aimAngle = this.getAngleInDirection(Game.player.posX + Game.player.vX * Movable.distanceBetween(this, Game.player) / 12.5, Game.player.posY + Game.player.vY * Movable.distanceBetween(this, Game.player) / 12.5);

			double a = this.getAngleInDirection(Game.player.posX, Game.player.posY);
			Ray rayToPlayer = new Ray(this.posX, this.posY, a, 0, this);
			Movable playerTarget = rayToPlayer.getTarget();
			if (playerTarget != null)
			{
				if (playerTarget.equals(Game.player))
				{
					this.moveAwayFromPlayer = true;
				}
				else
					this.moveAwayFromPlayer = false;
			}
			else
				this.moveAwayFromPlayer = false;


			//System.out.println(straightShoot);

			{
				Ray r = new Ray(this.posX, this.posY, /*this.angle*/a, 0, this);
				Movable m = r.getTarget();
				//System.out.println(m);

				if (m != null)
					if (m.equals(Game.player))
						this.shoot();
			}
			
			if (Math.abs(this.aimAngle - this.angle) > 0.04 && !locked)
			{
				if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
					//if ((this.aimAngle - this.angle) % (Math.PI * 2) < (this.angle - this.aimAngle) % (Math.PI * 2))
					this.angle+=0.03;
				else
					this.angle-=0.03;
				
				if (Math.abs(this.angle - this.aimAngle) < 0.08)
					this.locked = true;
				
				this.angle = this.angle % (Math.PI * 2);
			}
			
			if (locked)
				this.angle = this.aimAngle;

			boolean laidMine = false;

			double nearestX = 1000;
			double nearestY = 1000;
			double nearestTimer = 1000;


			Movable nearest = null;
			if (!laidMine)
				for (int i = 0; i < Game.movables.size(); i++)
				{
					Movable m = Game.movables.get(i);
					if (m instanceof Mine && Math.abs(this.posX - m.posX) < Game.tank_size * 3 && Math.abs(this.posY - m.posY) < Game.tank_size * 3)
					{
						if (nearestX + nearestY > this.posX - m.posX + this.posY - m.posY)
						{
							nearestX = this.posX - m.posX;
							nearestY = this.posY - m.posY;
							//nearest = m;
						}
						if (nearestTimer > ((Mine)m).timer)
						{
							nearestTimer = ((Mine)m).timer;
							nearest = m;
						}
					}
				}

			if (nearest != null)
			{
				this.setMotionAwayFromDirection(nearest.posX, nearest.posY, 2.5);
			}
			else
			{
				if (this.mineTimer <= 0)
				{
					//System.out.println(this.mineTimer);

					boolean layMine = true;
					int i = 0;
					while (i < Game.movables.size())
					{
						Movable m = Game.movables.get(i);
						if (m instanceof Tank && !m.equals(Game.player) && !m.equals(this))
						{
							Tank t = (Tank) m;
							if (Math.abs(t.posX - this.posX) <= 200 && Math.abs(t.posY - this.posY) <= 200)
							{
								//System.out.println(Math.abs(t.posX - this.posX) + " " + Math.abs(t.posY - this.posY));
								layMine = false;
								break;
							}

						}
						i++;
					}

					if (layMine)
					{
						Game.movables.add(new Mine(this.posX, this.posY, this));
						this.mineTimer = (int) (Math.random() * 2000 + 2000);
						double angleV = Math.random() * Math.PI * 2;
						this.setPolarMotion(angleV, 2.5);
						laidMine = true;
					}

				}
			}

			if (Math.abs(nearestX) + Math.abs(nearestY) <= 1)
			{
				this.setPolarMotion(Math.random() * 2 * Math.PI, 2.5);
			}

			this.mineTimer--;

		}
		this.cooldown--;

		super.update();

		//if (Math.random() * 300 < 1 && this.liveBullets < this.liveBulletMax)
		//	this.shoot();
	}
}

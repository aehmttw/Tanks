package tanks.legacy;

import java.awt.Color;

import tanks.Bullet;
import tanks.Game;
import tanks.Mine;
import tanks.Movable;
import tanks.Ray;
import tanks.tank.Tank;

@Deprecated
public class EnemyTankYellow extends Tank
{
	int mineTimer = (int) (Math.random() * 600 + 200);

	public EnemyTankYellow(double x, double y, int size) 
	{
		super("legacy-yellow", x, y, size, new Color(235, 200, 0));
		this.liveBulletMax = 1;
		this.hasCollided = true;
		this.coinValue = 3;
	}

	public EnemyTankYellow(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		Bullet b = new Bullet(this.posX, this.posY, 1, this);
		b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/4);
		b.moveOut(8);
		b.effect = Bullet.BulletEffect.trail;
		Game.movables.add(b);
	}

	@Override
	public void update()
	{
		if (Math.random() * 300 < 1 && this.liveBullets < this.liveBulletMax && !this.destroy)
		{
			if (Math.abs(this.posX - Game.player.posX) < 400 && Math.abs(this.posY - Game.player.posY) < 400)
			{
				Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);
				Movable m = a.getTarget();
				//if (m != null)
				//	System.out.println(((Tank)m).color);

				if (!(m instanceof Tank && !m.equals(Game.player)))
					this.shoot();
			}
		}

		boolean laidMine = false;


		if (hasCollided)
		{
			double angleV = Math.random() * Math.PI * 2;
			this.setPolarMotion(angleV, 3.5);
		}

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
					this.mineTimer = (int) (Math.random() * 600 + 200);
					double angleV = this.getPolarDirection() + Math.PI + (Math.random() - 0.5) * Math.PI / 2;
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

		this.angle = this.getAngleInDirection(Game.player.posX, Game.player.posY);

		super.update();
	}
}

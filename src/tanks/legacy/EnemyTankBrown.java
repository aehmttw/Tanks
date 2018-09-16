package tanks.legacy;

import java.awt.Color;

import tanks.Bullet;
import tanks.Game;
import tanks.Movable;
import tanks.Ray;
import tanks.Tank;

@Deprecated
public class EnemyTankBrown extends Tank
{
	enum Phase {clockwise, counterClockwise}

	Phase phase;
	int idleTimer = (int) (Math.random() * 500) + 500;

	public EnemyTankBrown(double x, double y, int size) 
	{
		super("legacy-brown", x, y, size, new Color(150, 80, 0));
		this.liveBulletMax = 1;

		if (Math.random() < 0.5)
			this.phase = Phase.clockwise;
		else
			this.phase = Phase.counterClockwise;
		
		this.coinValue = 1;
	}

	public EnemyTankBrown(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		if (this.liveBullets < this.liveBulletMax)
		{
			Bullet b = new Bullet(this.posX, this.posY, 1, this);
			b.setPolarMotion(this.angle, 25.0/4);
			b.moveOut(8);
			b.effect = Bullet.BulletEffect.trail;
			Game.movables.add(b);
		}
	}

	@Override
	public void update()
	{

		Ray a = new Ray(this.posX, this.posY, this.angle, 1, this);
		Movable m = a.getTarget();

		//if (m != null)
		//	System.out.println(((Tank)m).color);

		if (!(m == null))
			if (m.equals(Game.player))
				this.shoot();

		if (this.phase == Phase.clockwise)
			this.angle += 0.01;
		else
			this.angle -= 0.01;

		this.idleTimer--;

		if (idleTimer <= 0)
		{
			this.idleTimer = (int) (Math.random() * 500) + 500;
			if (this.phase == Phase.clockwise)
				this.phase = Phase.counterClockwise;
			else
				this.phase = Phase.clockwise;
		}

		super.update();
	}
}

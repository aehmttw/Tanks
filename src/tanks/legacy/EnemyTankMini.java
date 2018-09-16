package tanks.legacy;

import java.awt.Color;
import tanks.*;

@Deprecated
public class EnemyTankMini extends Tank
{
	int moveTime = 0;
	double aimAngle = 0;
	EnemyTankPink tank;

	boolean previousDestroy = false;


	public EnemyTankMini(double x, double y, int size) 
	{
		super("legacy-mini", x, y, size, new Color(255, 127, 127));
		this.liveBulletMax = 1;
		this.turret.size /= 2;
		this.turret.length /= 2;
		this.lives = 0.5;
	}

	public EnemyTankMini(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
		this.coinValue = 1;
	}

	public EnemyTankMini(double x, double y, int size, double a, EnemyTankPink t) 
	{
		this(x, y, size, a);
		t.spawnedMinis++;
		this.tank = t;
	}

	@Override
	public void shoot() 
	{
		Bullet b = new Bullet(this.posX, this.posY, 0, this);
		b.size /= 2;
		b.damage = 0.25;
		b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/4);
		b.moveOut(4);
		Game.movables.add(b);
	}

	@Override
	public void update()
	{
		if (Math.random() * 100 < 1 && this.liveBullets < this.liveBulletMax && !this.destroy)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);
			Movable m = a.getTarget();
			//if (m != null)
			//	System.out.println(((Tank)m).color);

			if (!(m == null))
				if(m.equals(Game.player))
					this.shoot();
		}

		if (this.moveTime <= 0 || hasCollided)
		{
			double angleV = Math.random() * Math.PI * 2;
			this.setPolarMotion(angleV, 2.5);
			this.moveTime = (int) (Math.random() * 100 + 25);
		}


		//this.moveTime--;

		this.aimAngle = this.getAngleInDirection(Game.player.posX, Game.player.posY);


		if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
			//if ((this.aimAngle - this.angle) % (Math.PI * 2) < (this.angle - this.aimAngle) % (Math.PI * 2))
			this.angle+=0.02;
		else
			this.angle-=0.02;

		if (Math.abs(this.aimAngle - this.angle) < 0.02)
			this.angle = this.aimAngle;

		this.angle = this.angle % (Math.PI * 2);

		super.update();

		if (this.destroy)
		{
			if (!this.previousDestroy && this.tank != null)
			{
				this.tank.spawnedMinis--;
			}

			this.previousDestroy = true;
		}

	}

}

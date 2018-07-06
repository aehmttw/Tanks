package tanks;

import java.awt.Color;

public class EnemyTankOrange extends Tank
{
	int moveTime = 0;
	double aimAngle = 0;

	public EnemyTankOrange(double x, double y, int size) 
	{
		super(x, y, size, new Color(230, 150, 0));
		this.liveBulletMax = 1;
	}

	public EnemyTankOrange(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
		this.coinValue = 5;
	}

	@Override
	public void shoot() 
	{
		Flame b = new Flame(this.posX, this.posY, Color.blue, 0, this);
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		Game.movables.add(b);
		this.cooldown = 0;
	}

	@Override
	public void update()
	{

		if (Movable.distanceBetween(this, Game.player) < 400)
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
			this.setPolarMotion(angleV, 1.5);
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
	}
}

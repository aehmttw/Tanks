package tanks;

import java.awt.Color;

public class EnemyTankYellow extends Tank
{
	int mineTimer = (int) (Math.random() * 800 + 200);
	
	public EnemyTankYellow(double x, double y, int size) 
	{
		super(x, y, size, new Color(235, 200, 0));
		this.liveBulletMax = 1;
		this.hasCollided = true;
	}
	
	public EnemyTankYellow(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
	}

	@Override
	public void shoot() 
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.blue, 1, this);
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
				AimRay a = new AimRay(this.posX, this.posY, this.angle, 0, this);
				Movable m = a.getTarget();
				//if (m != null)
				//	System.out.println(((Tank)m).color);

				if (!(m instanceof Tank && !m.equals(Game.player)))
					this.shoot();
			}
		}

		boolean laidMine = false;

		if (this.mineTimer <= 0)
		{
			Game.movables.add(new Mine(this.posX, this.posY, this));
			this.mineTimer = (int) (Math.random() * 800 + 200);
			double angleV = Math.random() * Math.PI * 2;
			this.setPolarMotion(angleV, 2.5);
			laidMine = true;
		}

		if (hasCollided)
		{
			double angleV = Math.random() * Math.PI * 2;
			this.setPolarMotion(angleV, 2.5);
		}

		if (!laidMine)
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m instanceof Mine && Math.abs(this.posX - m.posX) < Game.tank_size * 2 && Math.abs(this.posY - m.posY) < Game.tank_size * 2)
				{
					this.setMotionAwayFromDirection(m.posX, m.posY, 2.5);
				}
			}

		this.mineTimer--;

		this.angle = this.getAngleInDirection(Game.player.posX, Game.player.posY);

		super.update();
	}
}

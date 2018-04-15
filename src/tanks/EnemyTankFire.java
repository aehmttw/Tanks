package tanks;

import java.awt.Color;

public class EnemyTankFire extends Tank
{
	int moveTime = 0;
	
	public EnemyTankFire(double x, double y, int size) 
	{
		super(x, y, size, new Color(0, 150, 100));
		this.liveBulletMax = 1;
	}

	@Override
	public void shoot() 
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.red, 0, this);
		b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/2);
		b.moveOut(4);
		b.effect = Bullet.BulletEffect.fire;
		Game.movables.add(b);
	}
	
	@Override
	public void update()
	{
		if (Math.random() * 300 < 1 && this.liveBullets < this.liveBulletMax && !this.destroy)
		{
			AimRay a = new AimRay(this.posX, this.posY, this.angle, 0, this);
			Movable m = a.getTarget();
			//if (m != null)
			//	System.out.println(((Tank)m).color);
			
			if (!(m instanceof Tank && !m.equals(Game.player)))
				this.shoot();
		}
		
		if (this.moveTime <= 0 || hasCollided)
		{
			double angleV = Math.random() * Math.PI * 2;
			this.setPolarMotion(angleV, 1.5);
			this.moveTime = (int) (Math.random() * 100 + 25);
		}
			
			
		//this.moveTime--;
		
		this.angle = this.getAngleInDirection(Game.player.posX, Game.player.posY);
		
		super.update();
	}
}

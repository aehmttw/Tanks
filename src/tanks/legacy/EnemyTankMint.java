package tanks.legacy;

import java.awt.Color;
import tanks.*;

@Deprecated
public class EnemyTankMint extends Tank
{
	int moveTime = 0;
	double aimAngle = 0;

	public EnemyTankMint(double x, double y, int size) 
	{
		super("legacy-mint", x, y, size, new Color(0, 100, 100));
		this.liveBulletMax = 1;
	}
	
	public EnemyTankMint(double x, double y, int size, double a) 
	{
		this(x, y, size);
		this.angle = a;
		this.coinValue = 4;
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

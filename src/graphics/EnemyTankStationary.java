package graphics;

import java.awt.Color;

public class EnemyTankStationary extends Tank
{
	public EnemyTankStationary(double x, double y, int size) 
	{
		super(x, y, size, new Color(150, 80, 0));
		this.liveBulletMax = 1;
	}

	@Override
	public void shoot() 
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.blue, 1, this);
		b.setMotionInDirection(Game.player.posX, Game.player.posY, 25.0/8);
		b.moveOut(17);
		b.effect = Bullet.BulletEffect.trail;
		Game.movables.add(b);
	}
	
	@Override
	public void update()
	{
		if (Math.random() * 300 < 1 && this.liveBullets < this.liveBulletMax)
			this.shoot();
		
		this.angle = this.getAngleInDirection(Game.player.posX, Game.player.posY);
		
		super.update();
	}
}

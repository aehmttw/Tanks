package tanks;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class PlayerTank extends Tank
{
	int cooldown = 0;
	
	public PlayerTank(double x, double y, int size, Color color)
	{
		super(x, y, size, color);
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
	}

	@Override
	public void update()
	{
		if (KeyInputListener.keys.contains(KeyEvent.VK_LEFT) && !KeyInputListener.keys.contains(KeyEvent.VK_RIGHT))
			this.vX = Math.max(this.vX - accel, -maxV);
		else if (KeyInputListener.keys.contains(KeyEvent.VK_RIGHT) && !KeyInputListener.keys.contains(KeyEvent.VK_LEFT))
			this.vX = Math.min(this.vX + accel, maxV);
		else
		{
			if (this.vX > 0)
				this.vX = Math.max(this.vX - accel, 0);
			else if (this.vX < 0)
				this.vX = Math.min(this.vX + accel, 0);
		}
		
		if (KeyInputListener.keys.contains(KeyEvent.VK_UP) && !KeyInputListener.keys.contains(KeyEvent.VK_DOWN))
			this.vY = Math.max(this.vY - accel, -maxV);
		else if (KeyInputListener.keys.contains(KeyEvent.VK_DOWN) && !KeyInputListener.keys.contains(KeyEvent.VK_UP))
			this.vY = Math.min(this.vY + accel, maxV);
		else
		{
			if (this.vY > 0)
				this.vY = Math.max(this.vY - accel, 0);
			else if (this.vY < 0)
				this.vY = Math.min(this.vY + accel, 0);
		}
		if (this.cooldown > 0)
			this.cooldown--;
		
		if (KeyInputListener.keys.contains(KeyEvent.VK_SPACE) && this.cooldown <= 0 && this.liveBullets < this.liveBulletMax)
			this.shoot();
		
		if (KeyInputListener.keys.contains(KeyEvent.VK_ENTER) && this.cooldown <= 0 && this.liveMines < this.liveMinesMax)
			this.layMine();
		
		this.angle = this.getAngleInDirection(Screen.screen.getMouseX(), Screen.screen.getMouseY());

		
		super.update();
	}
	
	@Override
	public void shoot()
	{	
		this.cooldown = 20;
		Bullet b = new Bullet(posX, posY, Color.BLACK, 1, this);
		b.setMotionInDirection(Screen.screen.getMouseX(), Screen.screen.getMouseY(), 25.0 / 4.0);
		b.moveOut(8);
		
		b.effect = Bullet.BulletEffect.trail;
		
		//b.vX += this.vX;
		//b.vY += this.vY;
		Game.movables.add(b);
	}
	
	public void layMine()
	{	
		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);
	
		Game.movables.add(m);
	}
}

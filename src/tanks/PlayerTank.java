package tanks;

import java.awt.Color;
import java.awt.event.KeyEvent;

public class PlayerTank extends Tank
{
	double cooldown = 0;

	public PlayerTank(double x, double y, double angle)
	{
		super("player", x, y, Game.tank_size, new Color(0, 150, 255));
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
		this.coinValue = -5;
		this.angle = angle;
		
		if (Game.insanity)
			this.lives = 10;
	}

	@Override
	public void update()
	{	
		if (Game.insanity)
			this.liveBulletMax = 10;
		else
			this.liveBulletMax = 5;

		boolean up = KeyInputListener.keys.contains(KeyEvent.VK_UP) || KeyInputListener.keys.contains(KeyEvent.VK_W);
		boolean down = KeyInputListener.keys.contains(KeyEvent.VK_DOWN) || KeyInputListener.keys.contains(KeyEvent.VK_S);
		boolean left = KeyInputListener.keys.contains(KeyEvent.VK_LEFT) || KeyInputListener.keys.contains(KeyEvent.VK_A);
		boolean right = KeyInputListener.keys.contains(KeyEvent.VK_RIGHT) || KeyInputListener.keys.contains(KeyEvent.VK_D);

		double acceleration = accel;
		if (up && left || up && right || down && left || down && right)
		{
			acceleration /= Math.sqrt(2);
		}
		
		if (left && !right)
			this.vX = Math.max(this.vX - acceleration * Panel.frameFrequency, -maxV);
		else if (right && !left)
			this.vX = Math.min(this.vX + acceleration * Panel.frameFrequency, maxV);
		else
		{
			if (this.vX > 0)
				this.vX = Math.max(this.vX - acceleration * Panel.frameFrequency, 0);
			else if (this.vX < 0)
				this.vX = Math.min(this.vX + acceleration * Panel.frameFrequency, 0);
		}

		if (up && !down)
			this.vY = Math.max(this.vY - acceleration * Panel.frameFrequency, -maxV);
		else if (down && !up)
			this.vY = Math.min(this.vY + acceleration * Panel.frameFrequency, maxV);
		else
		{
			if (this.vY > 0)
				this.vY = Math.max(this.vY - acceleration * Panel.frameFrequency, 0);
			else if (this.vY < 0)
				this.vY = Math.min(this.vY + acceleration * Panel.frameFrequency, 0);
		}
		
		if (this.cooldown > 0)
			this.cooldown -= Panel.frameFrequency;

		boolean shoot = false;
		if (KeyInputListener.keys.contains(KeyEvent.VK_SPACE) || MouseInputListener.lClick)
			shoot = true;

		boolean mine = false;
		if (KeyInputListener.keys.contains(KeyEvent.VK_ENTER) || MouseInputListener.rClick)
			mine = true;

		if (shoot && this.cooldown <= 0 && this.liveBullets < this.liveBulletMax)
			this.shoot();

		if (mine && this.cooldown <= 0 && this.liveMines < this.liveMinesMax)
			this.layMine();

		this.angle = this.getAngleInDirection(Window.window.getMouseX(), Window.window.getMouseY());


		super.update();
	}

	@Override
	public void shoot()
	{	
		if (Game.bulletLocked)
			return;
			
		this.cooldown = 20;

		if (!Game.insanity)
		{
			fireBullet(25 / 4, 1, Color.black, Bullet.BulletEffect.trail);
		}
		else
		{
			/*LaserBullet b = new LaserBullet(this.posX, this.posY, Color.blue, 0, this);
			b.setPolarMotion(this.angle, 25.0/4);
			b.moveOut(8);
			b.shoot();
			this.cooldown = 0;*/
			fireBullet(25 / 2, 2, Color.red, Bullet.BulletEffect.fireTrail);
		}
	}
	
	public void fireBullet(double speed, int bounces, Color color, Bullet.BulletEffect effect)
	{
		Bullet b = new Bullet(posX, posY, color, bounces, this);
		b.setMotionInDirection(Window.window.getMouseX(), Window.window.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2));
		b.effect = effect;
		Game.movables.add(b);
	}

	public void layMine()
	{	
		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.movables.add(m);
	}
}

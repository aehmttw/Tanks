package tanks.tank;

import org.lwjgl.glfw.GLFW;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;

public class TankPlayer extends Tank
{
	public Player player = Game.player;
	public boolean enableDestroyCheat = false;

	public TankPlayer(double x, double y, double angle)
	{		
		super("player", x, y, Game.tank_size, 0, 150, 255);
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
		this.angle = angle;
		this.player.tank = this;
	}

	@Override
	public void update()
	{
		boolean up = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_UP) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_W);
		boolean down = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_DOWN) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_S);
		boolean left = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_LEFT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_A);
		boolean right = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_RIGHT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_D);
		boolean trace = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_PERIOD) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_4);

		boolean destroy = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_BACKSPACE);

		if (destroy && this.enableDestroyCheat)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (!Team.isAllied(this, Game.movables.get(i)))
					Game.movables.get(i).destroy = true;
			}
		}

		double acceleration = accel;
		double maxVelocity = maxV;
	
		double x = 0;
		double y = 0;
		
		double a = -1;

		if (left)
			x -= 1;
		
		if (right)
			x += 1;
		
		if (up)
			y -= 1;
		
		if (down)
			y += 1;
		
		if (x == 1 && y == 0)
			a = 0;
		else if (x == 1 && y == 1)
			a = Math.PI / 4;
		else if (x == 0 && y == 1)
			a = Math.PI / 2;
		else if (x == -1 && y == 1)
			a = 3 * Math.PI / 4;
		else if (x == -1 && y == 0)
			a = Math.PI;
		else if (x == -1 && y == -1)
			a = 5 * Math.PI / 4;
		else if (x == 0 && y == -1)
			a = 3 * Math.PI / 2;
		else if (x == 1 && y == -1)
			a = 7 * Math.PI / 4;
		
		if (a >= 0)
			this.addPolarMotion(a, acceleration * Panel.frameFrequency);
		else
		{
			this.vX *= Math.pow(0.95, Panel.frameFrequency);
			this.vY *= Math.pow(0.95, Panel.frameFrequency);
			
			if (Math.abs(this.vX) < 0.001)
				this.vX = 0;
			
			if (Math.abs(this.vY) < 0.001)
				this.vY = 0;
		}
		
		double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);
	
		if (speed > maxVelocity)
			this.setPolarMotion(this.getPolarDirection(), maxVelocity);
		
		if (this.cooldown > 0)
			this.cooldown -= Panel.frameFrequency;

		boolean shoot = false;
		if (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_SPACE) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1))
			shoot = true;

		boolean mine = false;
		if (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_ENTER) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_2))
			mine = true;

		if (shoot && this.cooldown <= 0 && !this.disabled)
			this.shoot();

		if (mine && this.cooldown <= 0 && !this.disabled)
			this.layMine();

		this.angle = this.getAngleInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY());
		
		if (trace && !Game.bulletLocked && !this.disabled)
		{
			Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);
			r.vX /= 2;
			r.vY /= 2;
			r.trace = true;
			r.dotted = true;
			r.moveOut(10 * this.size / Game.tank_size);
			r.getTarget();
		}
		
		super.update();
	}
	
	public void shoot()
	{	
		if (Game.bulletLocked || this.destroy)
			return;

		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(false))
				return;
		}

		if (this.liveBullets >= this.liveBulletMax)
			return;

		this.cooldown = 20;

		fireBullet(25 / 4.0, 1, Bullet.BulletEffect.trail);
	}

	public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg");

		Bullet b = new Bullet(posX, posY, bounces, this);
		b.setMotionInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2 * this.size / Game.tank_size));
		b.effect = effect;
		
		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}

	public void fireBullet(Bullet b, double speed)
	{
		if (b.itemSound != null)
			Drawing.drawing.playGlobalSound(b.itemSound, (float) (Bullet.bullet_size / b.size));

	    b.setMotionInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0 * b.recoil);

		b.moveOut((int) (25.0 / speed * 2 * this.size / Game.tank_size));

		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
	}
	
	public void layMine()
	{	
		if (Game.bulletLocked || this.destroy)
			return;
		
		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(true))
				return;
		}

		if (this.liveMines >= this.liveMinesMax )
			return;

		Drawing.drawing.playGlobalSound("lay_mine.ogg");

		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.movables.add(m);
	}

	@Override
	public void onDestroy()
	{
		if (Crusade.crusadeMode)
			this.player.remainingLives--;
	}
}

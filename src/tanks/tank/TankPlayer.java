package tanks.tank;

import org.lwjgl.glfw.GLFW;

import tanks.Bullet;
import tanks.Game;
import tanks.Mine;
import tanks.Panel;
import tanks.Team;
import tanks.Drawing;

public class TankPlayer extends Tank
{
	public TankPlayer(double x, double y, double angle)
	{		
		super("player", x, y, Game.tank_size, 0, 150, 255);
		this.liveBulletMax = 5;
		this.liveMinesMax = 2;
		this.coinValue = -5;
		this.angle = angle;
		//this.lives = 10;
	}

	@Override
	public void update()
	{		
		this.liveBulletMax = 5;

		boolean up = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_UP) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_W);
		boolean down = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_DOWN) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_S);
		boolean left = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_LEFT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_A);
		boolean right = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_RIGHT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_D);
		boolean destroy = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_BACKSPACE);

		double acceleration = accel;
		double maxVelocity = maxV;
		
		if (destroy)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				if (!Team.isAllied(this, Game.movables.get(i)))
					Game.movables.get(i).destroy = true;
			}
		}

		if (up && left || up && right || down && left || down && right)
		{
			acceleration /= Math.sqrt(2);
			maxVelocity /= Math.sqrt(2);
		}

		if (left && !right)
			this.vX = Math.max(this.vX - acceleration * Panel.frameFrequency, -maxVelocity);
		else if (right && !left)
			this.vX = Math.min(this.vX + acceleration * Panel.frameFrequency, maxVelocity);
		else
		{
			if (this.vX > 0)
				this.vX = Math.max(this.vX - acceleration * Panel.frameFrequency, 0);
			else if (this.vX < 0)
				this.vX = Math.min(this.vX + acceleration * Panel.frameFrequency, 0);
		}

		if (up && !down)
			this.vY = Math.max(this.vY - acceleration * Panel.frameFrequency, -maxVelocity);
		else if (down && !up)
			this.vY = Math.min(this.vY + acceleration * Panel.frameFrequency, maxVelocity);
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
		if (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_SPACE) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1))
			shoot = true;

		boolean mine = false;
		if (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_ENTER) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_2))
			mine = true;

		if (shoot && this.cooldown <= 0 && this.liveBullets < this.liveBulletMax && !this.disabled)
			this.shoot();

		if (mine && this.cooldown <= 0 && this.liveMines < this.liveMinesMax && !this.disabled)
			this.layMine();

		this.angle = this.getAngleInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY());


		super.update();
	}
	
	@Override
	public void shoot()
	{	
		if (Game.bulletLocked)
			return;

		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(false))
				return;
		}
		
		this.cooldown = 20;

		/*BulletLaser b = new BulletLaser(this.posX, this.posY, 0, this);
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		b.shoot();
		this.cooldown = 0;*/
			
		fireBullet(25 / 4.0, 1, Bullet.BulletEffect.trail);
		
	}

	public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
	{		
		Drawing.drawing.playSound("resources/shoot.wav");

		Bullet b = new Bullet(posX, posY, bounces, this);
		b.setMotionInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2));
		b.effect = effect;
				
		Game.movables.add(b);
	}

	public void fireBullet(Bullet b, double speed)
	{
		Drawing.drawing.playSound("resources/shoot.wav");

	    b.setMotionInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0 * b.recoil);

		b.moveOut((int) (25.0 / speed * 2));
		Game.movables.add(b);
	}

	
	public void layMine()
	{	
		if (Game.bulletLocked)
			return;
		
		if (Panel.panel.hotbar.enabledItemBar)
		{
			if (Panel.panel.hotbar.currentItemBar.useItem(true))
				return;
		}

		Drawing.drawing.playSound("resources/lay-mine.wav");
		
		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.movables.add(m);
	}
}

package tanks.tank;

import java.awt.Color;
import java.awt.event.KeyEvent;

import tanks.Bullet;
import tanks.Game;
import tanks.InputKeyboard;
import tanks.InputMouse;
import tanks.Mine;
import tanks.Panel;
import tanks.Drawing;

public class TankPlayer extends Tank
{
	public double cooldown = 0;
	
	//double maxCooldown = 100;

	public TankPlayer(double x, double y, double angle)
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

		boolean up = InputKeyboard.keys.contains(KeyEvent.VK_UP) || InputKeyboard.keys.contains(KeyEvent.VK_W);
		boolean down = InputKeyboard.keys.contains(KeyEvent.VK_DOWN) || InputKeyboard.keys.contains(KeyEvent.VK_S);
		boolean left = InputKeyboard.keys.contains(KeyEvent.VK_LEFT) || InputKeyboard.keys.contains(KeyEvent.VK_A);
		boolean right = InputKeyboard.keys.contains(KeyEvent.VK_RIGHT) || InputKeyboard.keys.contains(KeyEvent.VK_D);

		double acceleration = accel;
		double maxVelocity = maxV;

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
		if (InputKeyboard.keys.contains(KeyEvent.VK_SPACE) || InputMouse.lClick)
			shoot = true;

		boolean mine = false;
		if (InputKeyboard.keys.contains(KeyEvent.VK_ENTER) || InputMouse.rClick)
			mine = true;

		if (shoot && this.cooldown <= 0 && this.liveBullets < this.liveBulletMax)
			this.shoot();

		if (mine && this.cooldown <= 0 && this.liveMines < this.liveMinesMax)
			this.layMine();

		this.angle = this.getAngleInDirection(Drawing.window.getMouseX(), Drawing.window.getMouseY());


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
			/*this.cooldown -= Panel.frameFrequency;
			
			if (this.cooldown > 0)
			{
				if (Math.random() * maxCooldown > cooldown && Game.graphicalEffects)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.charge);
					double var = 50;
					e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
					Game.effects.add(e);
				}
				return;

			}
			else
			{
				//BulletLaser b = new BulletLaser(this.posX, this.posY, 2, this);
				//b.setPolarMotion(this.angle, 25.0/4);
				//b.moveOut(8);
				//b.shoot();
				//this.maxCooldown = this.maxCooldown * 0.75 + 1;
				//this.cooldown = Math.max(this.cooldown, maxCooldown);
			}*/
			
			/*BulletLaser b = new BulletLaser(this.posX, this.posY, 0, this);
			b.setPolarMotion(this.angle, 25.0/4);
			b.moveOut(8);
			b.shoot();
			this.cooldown = 0;*/
			
			fireBullet(25 / 4, 1, Color.black, Bullet.BulletEffect.trail);
		}
		else
		{
			fireBullet(25 / 2, 2, Color.red, Bullet.BulletEffect.fireTrail);
		}
	}

	public void fireBullet(double speed, int bounces, Color color, Bullet.BulletEffect effect)
	{
		Drawing.playSound("resources/shoot.wav");

		Bullet b = new Bullet(posX, posY, bounces, this);
		b.setMotionInDirection(Drawing.window.getMouseX(), Drawing.window.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2));
		b.effect = effect;
		Game.movables.add(b);
	}

	public void fireBullet(Bullet b, double speed)
	{
		Drawing.playSound("resources/shoot.wav");

	    b.setMotionInDirection(Drawing.window.getMouseX(), Drawing.window.getMouseY(), speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

		b.moveOut((int) (25.0 / speed * 2));
		Game.movables.add(b);
	}

	
	public void layMine()
	{	
		if (Game.bulletLocked)
			return;

		Drawing.playSound("resources/lay-mine.wav");
		
		this.cooldown = 50;
		Mine m = new Mine(posX, posY, this);

		Game.movables.add(m);
	}
}

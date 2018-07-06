package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Effect extends Movable
{
	static enum EffectType {fire, smokeTrail, trail, ray, mineExplosion, laser, piece, obstaclePiece, charge, tread}
	public EffectType type;
	double age = 0;
	public Color col;
	public double maxAge = Math.random() * 100 + 50;
	public double targetX;
	public double targetY;
	public double size;
	
	public Effect(double x, double y, EffectType type)
	{
		super(x, y);
		this.type = type;
		if (type.equals(EffectType.charge))
		{
			this.addPolarMotion(Math.random() * Math.PI * 2, Math.random() * 3 + 3);
			this.posX -= this.vX * 25;
			this.posY -= this.vY * 25;
			this.maxAge = 25;
		}
	}

	@Override
	public void checkCollision() {}
	
	public void drawWithoutUpdate(Graphics p)
	{
		//p.setColor(Color.red);
		//Screen.fillRect(p, this.posX, this.posY, 4, 4);
		double opacityMultiplier = Obstacle.draw_size * 1.0 / Obstacle.obstacle_size;
		
		if (this.type == EffectType.fire)
		{
			if (this.age >= 20)
			{
				Game.removeEffects.add(this);
				return;
			}
			int size = (int) (this.age * 3 + 10);
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 255);
			
			int green = Math.min(255, (int)(127 + 128.0*(this.age / 20.0)));
			Color col = new Color(255, green, 0, (int) (opacity * opacityMultiplier));
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
			
		}
		else if (this.type == EffectType.smokeTrail)
		{
			
			double opacityModifier = Math.max(0, Math.min(1, this.age / 40.0 - 0.25));
			int size = 20;
			double rawOpacity = (1.0 - (this.age)/200.0);
			rawOpacity *= rawOpacity * rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 100);
						
			Color col = new Color(127, 127, 127, (int) (opacity * opacityMultiplier * opacityModifier));
			
			if (opacity <= 0)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.trail)
		{
			if (this.age > 50)
			{
				Game.removeEffects.add(this);
				return;
			}
			int size = (int)Math.min(20, this.age / 20.0 + 10);
			double rawOpacity = (1.0 - (this.age)/50.0);
			rawOpacity *= rawOpacity * rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 25);
			
			Color col = new Color(127, 127, 127, (int) (opacity * opacityMultiplier));
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.ray)
		{
			int size = 6;
			p.setColor(new Color(0, 0, 0, 50));
			Screen.fillOval(p, this.posX, this.posY, size, size);
			
			Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.mineExplosion)
		{
			if (this.age > 20)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			int size = Game.tank_size * 4;
			int opacity = (int) (100 - this.age * 5);
			p.setColor(new Color(255, 0, 0, opacity));
			Screen.fillOval(p, this.posX, this.posY, size, size);	
		}
		else if (this.type == EffectType.laser)
		{
			if (this.age > 21)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			//int size = Bullet.bullet_size / 2;
			//double size = (int) (255 - this.age * 12);
			double size = Bullet.bullet_size - this.age / 2;
			p.setColor(new Color(255, 0, 0));
			Screen.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.piece)
		{
			if (this.age > this.maxAge)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			int size = 1 + (int) (Bullet.bullet_size * (1 - this.age / this.maxAge));
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.obstaclePiece)
		{
			if (this.age > this.maxAge)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			int size = 1 + (int) (Bullet.bullet_size * (1 - this.age / this.maxAge));
			p.setColor(col);
			Screen.fillRect(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.charge)
		{	
			if (this.age > this.maxAge)
			{
				Game.removeEffects.add(this);
				return;
			}
			
			int size = 1 + (int) (Bullet.bullet_size * (this.age / this.maxAge));
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.tread)
		{	
			if (this.age > 510)
			{
				Game.removeBelowEffects.add(this);
				return;
			}
			
			int opacity = (int) (255 - this.age / 2) / 4;
			p.setColor(new Color(0, 0, 0, opacity));
			Screen.fillRect(p, this.posX, this.posY, size * Obstacle.draw_size / Obstacle.obstacle_size, size * Obstacle.draw_size / Obstacle.obstacle_size);
		}
	}
	
	@Override
	public void draw(Graphics p) 
	{
		this.drawWithoutUpdate(p);	
		this.posX += this.vX * Panel.frameFrequency;
		this.posY += this.vY * Panel.frameFrequency;
		this.age += Panel.frameFrequency;
	}
}

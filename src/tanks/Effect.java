package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Effect extends Movable
{
	static enum EffectType {fire, smokeTrail, trail, ray, mineExplosion}
	public EffectType type;
	int age = 0;
	
	public Effect(double x, double y, EffectType type)
	{
		super(x, y);
		this.type = type;
	}

	@Override
	public void checkCollision() {}

	@Override
	public void draw(Graphics p) 
	{
		double opacityMultiplier = Obstacle.draw_size * 1.0 / Obstacle.obstacle_size;
		
		if (this.type == EffectType.fire)
		{
			int size = this.age * 3 + 10;
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 255);
			
			int green = (int)(127 + 128.0*(this.age / 20.0));
			Color col = new Color(255, green, 0, (int) (opacity * opacityMultiplier));
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
			if (this.age >= 20)
				Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.smokeTrail)
		{
			double opacityModifier = Math.max(0, Math.min(1, this.age / 40.0 - 0.25));
			int size = 20;
			double rawOpacity = (1.0 - (this.age)/200.0);
			rawOpacity *= rawOpacity * rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 100);
			
			Color col = new Color(127, 127, 127, (int) (opacity * opacityMultiplier * opacityModifier));
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
			if (this.age >= 200)
				Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.trail)
		{
			int size = (int)Math.min(20, this.age / 20.0 + 10);
			double rawOpacity = (1.0 - (this.age)/50.0);
			rawOpacity *= rawOpacity * rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 25);
			
			Color col = new Color(127, 127, 127, (int) (opacity * opacityMultiplier));
			
			p.setColor(col);
			Screen.fillOval(p, this.posX, this.posY, size, size);
			if (this.age >= 50)
				Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.ray)
		{
			int size = 6;
			p.setColor(Color.black);
			Screen.fillOval(p, this.posX, this.posY, size, size);
			
			Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.mineExplosion)
		{
			int size = Game.tank_size * 4;
			int opacity = 100 - this.age * 5;
			p.setColor(new Color(255, 0, 0, opacity));
			Screen.fillRect(p, this.posX, this.posY, size, size);
			
			if (this.age >= 20)
				Game.removeEffects.add(this);
		}
		
		this.age++;
	}
}

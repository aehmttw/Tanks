package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Effect extends Movable
{
	public enum EffectType {fire, smokeTrail, trail, ray, mineExplosion, laser, piece, obstaclePiece, charge, tread, darkFire}
	public EffectType type;
	double age = 0;
	public Color col;
	public double maxAge = 100;
	public double size;
	public double ffOpacityMultiplier = Math.min(1, Panel.frameFrequency);
	public boolean removed = false;

	public static Effect createNewEffect(double x, double y, EffectType type)
	{
		if (Game.recycleEffects.size() > 0)
		{
			Effect e = Game.recycleEffects.remove(0);
			e.refurbish();
			e.initialize(x, y, type);
			
			return e;
		}
		else
		{
			Effect e = new Effect();
			e.initialize(x, y, type);
			return e;
		}
	}
	
	public static Effect createNewEffect(double x, double y, EffectType type, double opacityMultiplier, double age)
	{
		Effect e = Effect.createNewEffect(x, y, type);
		e.ffOpacityMultiplier = Math.min(1, Panel.frameFrequency * opacityMultiplier);
		e.age = age * Panel.frameFrequency;
		return e;
	}
	
	/**
	 * Use Effect.createNewEffect(double x, double y, Effect.EffectType type) instead of this because it can refurbish and reuse old effects 
	 * @param x
	 * @param y
	 * @param type
	 */
	protected Effect()
	{
		super(0, 0);	
	}
	
	protected void initialize(double x, double y, EffectType type)
	{
		this.posX = x;
		this.posY = y;
		this.type = type;
		
		if (type == EffectType.fire)
			this.maxAge = 20;
		else if (type == EffectType.smokeTrail)
			this.maxAge = 200;
		else if (type == EffectType.trail)
			this.maxAge = 50;
		else if (type == EffectType.ray)
			this.maxAge = 1;
		else if (type == EffectType.mineExplosion)
			this.maxAge = 20;
		else if (type == EffectType.laser)
			this.maxAge = 21;
		else if (type == EffectType.piece)
			this.maxAge = Math.random() * 100 + 50;
		else if (type == EffectType.obstaclePiece)
			this.maxAge = Math.random() * 100 + 50;
		else if (type.equals(EffectType.charge))
		{
			this.addPolarMotion(Math.random() * Math.PI * 2, Math.random() * 3 + 3);
			this.posX -= this.vX * 25;
			this.posY -= this.vY * 25;
			this.maxAge = 25;
		}
		else if (type == EffectType.tread)
		{
			this.maxAge = 510;
			if (Game.graphicalEffects)
				this.maxAge *= 2;
		}	
		else if (type == EffectType.darkFire)
			this.maxAge = 20;
	}
	
	protected void refurbish()
	{		
		this.posX = 0;
		this.posY = 0;
		this.vX = 0;
		this.vY = 0;
		this.type = null;
		this.age = 0;
		this.col = null;
		this.maxAge = Math.random() * 100 + 50;
		this.size = 0;
		this.ffOpacityMultiplier =  Math.min(1, Panel.frameFrequency);
		this.removed = false;
	}

	@Override
	public void checkCollision() {}
	
	@Override
	public void draw(Graphics p)
	{
		if (this.maxAge < this.age)
			return;
		
		if (this.age < 0)
			Game.exitToCrash(new RuntimeException("Effect with negative age"));
		
		double opacityMultiplier = ScreenGame.finishTimer / ScreenGame.finishTimerMax;
		
		if (this.type == EffectType.fire)
		{	
			int size = (int) (this.age * 3 + 10);
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 255);
			
			int green = Math.min(255, (int)(127 + 128.0*(this.age / 20.0)));
			Color col = new Color(255, green, 0,  Math.min(255, Math.max(0, (int) (opacity * opacityMultiplier * ffOpacityMultiplier))));
			
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
			
		}
		else if (this.type == EffectType.smokeTrail)
		{			
			double opacityModifier = Math.max(0, Math.min(1, this.age / 40.0 - 0.25));
			int size = 20;
			double rawOpacity = (1.0 - (this.age)/200.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 100);
						
			Color col = new Color(0, 0, 0, Math.min(255, Math.max(0, (int) (opacity * opacityMultiplier * opacityModifier * ffOpacityMultiplier))));
		
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.trail)
		{
			int size = (int)Math.min(20, this.age / 20.0 + 10);
			double rawOpacity = (1.0 - (this.age)/50.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 25);
			
			Color col = new Color(127, 127, 127, Math.min(255, Math.max(0, (int) (opacity * opacityMultiplier * ffOpacityMultiplier))));
			
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.ray)
		{
			int size = 6;
			p.setColor(new Color(0, 0, 0, 50));
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.mineExplosion)
		{
			int size = Game.tank_size * 4;
			int opacity = (int) (100 - this.age * 5);
			p.setColor(new Color(255, 0, 0, opacity));
			Drawing.fillOval(p, this.posX, this.posY, size, size);	
		}
		else if (this.type == EffectType.laser)
		{
			double size = Bullet.bullet_size - this.age / 2;
			p.setColor(new Color(255, 0, 0));
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.piece)
		{
			int size = 1 + (int) (Bullet.bullet_size * (1 - this.age / this.maxAge));
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.obstaclePiece)
		{
			int size = 1 + (int) (Bullet.bullet_size * (1 - this.age / this.maxAge));
			p.setColor(col);
			Drawing.fillRect(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.charge)
		{	
			int size = 1 + (int) (Bullet.bullet_size * (this.age / this.maxAge));
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.tread)
		{	
			double opacityFactor = 2;
			
			if (Game.graphicalEffects)
			{
				opacityFactor = 4;
			}
			
			int opacity = (int) (255 - this.age / opacityFactor) / 4;
			p.setColor(new Color(0, 0, 0, opacity));
			Drawing.fillRect(p, this.posX, this.posY, size * Obstacle.draw_size / Obstacle.obstacle_size, size * Obstacle.draw_size / Obstacle.obstacle_size);
		}
		else if (this.type == EffectType.darkFire)
		{	
			int size = (int) (this.age * 3 + 10);
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			int opacity = (int)(rawOpacity * 255);
			
			int red = Math.min(255, (int)(128 - 128.0 * (this.age / 20.0)));
			Color col = new Color(red / 2, 0, red,  Math.min(255, Math.max(0, (int) (opacity * opacityMultiplier * ffOpacityMultiplier))));
			
			p.setColor(col);
			Drawing.fillOval(p, this.posX, this.posY, size, size);
			
		}
		else
		{
			Game.exitToCrash(new RuntimeException("Invalid effect type!"));
		}
	}
	
	@Override
	public void update() 
	{	
		this.posX += this.vX * Panel.frameFrequency;
		this.posY += this.vY * Panel.frameFrequency;
		this.age += Panel.frameFrequency;
		//this.age++;
		
		if (this.age > this.maxAge && !removed)
		{
			removed = true;
			
			if (Game.effects.contains(this))
				Game.removeEffects.add(this);
			
			else if (Game.belowEffects.contains(this))
				Game.removeBelowEffects.add(this);
		}
	}
}

package tanks;

import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

public class Effect extends Movable
{
	public enum EffectType {fire, smokeTrail, trail, ray, mineExplosion, laser, piece, obstaclePiece, obstaclePiece3d, charge, tread, darkFire, electric, healing, stun, bushBurn}
	public EffectType type;
	double age = 0;
	public double colR;
	public double colG;
	public double colB;

	public double maxAge = 100;
	public double size;
	public boolean removed = false;
	public double radius;
	public double angle;
	public double distance;
	
	public static Effect createNewEffect(double x, double y, double z, EffectType type)
	{
		//while (Game.recycleEffects.size() > 0 && Game.recycleEffects.get(0) == null)
		//	Game.recycleEffects.remove(0);
		
		if (Game.recycleEffects.size() > 0)
		{
			Effect e = Game.recycleEffects.remove(0);
			
			e.refurbish();
			e.initialize(x, y, z, type);
			
			return e;
		}
		else
		{
			Effect e = new Effect();
			e.initialize(x, y, z, type);
			return e;
		}
	}
	
	public static Effect createNewEffect(double x, double y, EffectType type, double age)
	{
		return Effect.createNewEffect(x, y, 0, type, age);
	}
	
	public static Effect createNewEffect(double x, double y, double z, EffectType type, double age)
	{
		Effect e = Effect.createNewEffect(x, y, z, type);
		e.age = age;
		return e;
	}
	
	public static Effect createNewEffect(double x, double y, EffectType type)
	{
		return Effect.createNewEffect(x, y, 0, type);
	}
	
	/**
	 * Use Effect.createNewEffect(double x, double y, Effect.EffectType type) instead of this because it can refurbish and reuse old effects
	 */
	protected Effect()
	{
		super(0, 0);	
	}
	
	protected void initialize(double x, double y, double z, EffectType type)
	{
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.type = type;
		
		if (type == EffectType.fire)
			this.maxAge = 20;
		else if (type == EffectType.smokeTrail)
			this.maxAge = 200;
		else if (type == EffectType.trail)
			this.maxAge = 50;
		else if (type == EffectType.ray)
			this.maxAge = 20;
		else if (type == EffectType.mineExplosion)
			this.maxAge = 20;
		else if (type == EffectType.laser)
			this.maxAge = 21;
		else if (type == EffectType.piece)
			this.maxAge = Math.random() * 100 + 50;
		else if (type == EffectType.obstaclePiece)
			this.maxAge = Math.random() * 100 + 50;
		else if (type == EffectType.obstaclePiece3d)
			this.maxAge = Math.random() * 100 + 50;
		else if (type.equals(EffectType.charge))
		{
			if (Game.enable3d)
				this.add3dPolarMotion(Math.random() * Math.PI * 2,-Math.random() * Math.PI / 2, Math.random() * 3 + 3);
			else
				this.addPolarMotion(Math.random() * Math.PI * 2, Math.random() * 3 + 3);

			this.posX -= this.vX * 25;
			this.posY -= this.vY * 25;
			this.posZ -= this.vZ * 25;
			this.maxAge = 25;
		}
		else if (type == EffectType.tread)
		{
			this.maxAge = 510;
			if (Game.fancyGraphics)
				this.maxAge *= 2;
		}	
		else if (type == EffectType.darkFire)
			this.maxAge = 20;
		else if (type == EffectType.stun)
		{
			this.angle += Math.PI * 2 * Math.random();
			this.maxAge = 80 + Math.random() * 40;
			this.size = Math.random() * 5 + 5;
			this.distance = Math.random() * 50 + 25;
		}
		else if (type == EffectType.healing)
			this.maxAge = 21;
		else if (type == EffectType.bushBurn)
			this.maxAge = this.posZ * 2;
	}
	
	protected void refurbish()
	{		
		this.posX = 0;
		this.posY = 0;
		this.posZ = 0;
		this.vX = 0;
		this.vY = 0;
		this.vZ = 0;
		this.type = null;
		this.age = 0;
		this.colR = 0;
		this.colG = 0;
		this.colB = 0;
		this.maxAge = Math.random() * 100 + 50;
		this.size = 0;
		this.removed = false;
		this.angle = 0;
		this.distance = 0;
		this.radius = 0;
	}

	@Override
	public void draw()
	{
		if (this.maxAge > 0 && this.maxAge < this.age)
			return;
		
		if (this.age < 0)
			Game.exitToCrash(new RuntimeException("Effect with negative age"));
		
		double opacityMultiplier = ScreenGame.finishTimer / ScreenGame.finishTimerMax;
		Drawing drawing = Drawing.drawing;
		
		if (this.type == EffectType.fire)
		{	
			double size = (this.age * 3 + 10);
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			double opacity = (rawOpacity * 255) / 4;
			
			double green = Math.min(255, (255 - 255.0*(this.age / 20.0)));
			drawing.setColor(255, green, 0,  Math.min(255, Math.max(0, (opacity * opacityMultiplier))));
			
			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.smokeTrail)
		{			
			double opacityModifier = Math.max(0, Math.min(1, this.age / 40.0 - 0.25));
			int size = 20;
			double rawOpacity = (1.0 - (this.age)/200.0);
			rawOpacity *= rawOpacity * rawOpacity;
			double opacity = (rawOpacity * 100) / 4;
			
			drawing.setColor(0, 0, 0, Math.min(255, Math.max(0, (opacity * opacityMultiplier * opacityModifier))));

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.trail)
		{
			double size = Math.min(20, this.age / 20.0 + 10);
			double rawOpacity = (1.0 - (this.age) / 50.0);
			rawOpacity *= rawOpacity * rawOpacity;
			double opacity = (rawOpacity * 50);
			drawing.setColor(127, 127, 127, Math.min(255, Math.max(0, (opacity * opacityMultiplier))));
			
			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.ray)
		{
			int size = 6;
			drawing.setColor(0, 0, 0, 50);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);

			Game.removeEffects.add(this);
		}
		else if (this.type == EffectType.mineExplosion)
		{
			double size = (radius * 2);
			double opacity = (100 - this.age * 5);
			drawing.setColor(255, 0, 0, opacity);
			drawing.fillForcedOval(this.posX, this.posY, size, size);	
		}
		else if (this.type == EffectType.laser)
		{
			double size = Bullet.bullet_size - this.age / 2;
			drawing.setColor(255, 0, 0);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.piece)
		{
			double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
			drawing.setColor(this.colR, this.colG, this.colB);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.obstaclePiece)
		{
			double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
			drawing.setColor(this.colR, this.colG, this.colB);
			
			drawing.fillRect(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.obstaclePiece3d)
		{
			double size = 1 + (Bullet.bullet_size * (1 - this.age / this.maxAge));
			drawing.setColor(this.colR, this.colG, this.colB);
			
			drawing.fillBox(this.posX, this.posY, this.posZ, size, size, size);
		}
		else if (this.type == EffectType.charge)
		{
			double size = 1 + (Bullet.bullet_size * (this.age / this.maxAge));
			drawing.setColor(this.colR, this.colG, this.colB);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.tread)
		{	
			double opacityFactor = 2;
			
			if (Game.fancyGraphics)
			{
				opacityFactor = 4;
			}
			
			double opacity = (255 - this.age / opacityFactor) / 4;
			drawing.setColor(0, 0, 0, opacity);
			drawing.fillRect(this.posX, this.posY, size * Obstacle.draw_size / Obstacle.obstacle_size, size * Obstacle.draw_size / Obstacle.obstacle_size);
		}
		else if (this.type == EffectType.darkFire)
		{	
			double size = (this.age * 3 + 10);
			double rawOpacity = (1.0 - (this.age)/20.0);
			rawOpacity *= rawOpacity * rawOpacity;
			double opacity = (rawOpacity * 255) / 4;
			
			double red = Math.min(255, (128 - 128.0 * (this.age / 20.0)));
			drawing.setColor(red / 2, 0, red,  Math.min(255, Math.max(0, (opacity * opacityMultiplier))));
			
			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);			
		}
		else if (this.type == EffectType.stun)
		{	
			double size = 1 + (this.size * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));
			double angle = this.angle + this.age / 20;
			double distance = 1 + (this.distance * Math.min(Math.min(1, (this.maxAge - this.age) * 3 / this.maxAge), Math.min(1, this.age * 3 / this.maxAge)));

			drawing.setColor(this.colR, this.colG, this.colB);
			double[] o = Movable.getLocationInDirection(angle, distance);
			
			if (Game.enable3d)
				drawing.fillOval(this.posX + o[0], this.posY + o[1], this.posZ, size, size);
			else
				drawing.fillOval(this.posX + o[0], this.posY + o[1], size, size);
		}
		else if (this.type == EffectType.electric)
		{
			double size = Math.max(0, Bullet.bullet_size - this.age / 2);
			drawing.setColor(0, 255, 255);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.healing)
		{
			double size = Bullet.bullet_size - this.age / 2;
			drawing.setColor(0, 255, 0);

			if (Game.enable3d)
				drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
			else
				drawing.fillOval(this.posX, this.posY, size, size);
		}
		else if (this.type == EffectType.bushBurn)
		{
			if (Game.enable3d)
			{
				Drawing.drawing.setColor(this.colR, this.colG, this.colB);
				Drawing.drawing.fillBox(this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, this.posZ);
			}
			else
			{
				Drawing.drawing.setColor(this.colR, this.colG, this.colB, this.posZ);
				Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
			}

			this.posZ -= Panel.frameFrequency / 2;
			this.colR = Math.max(this.colR - Panel.frameFrequency, 0);
			this.colG = Math.max(this.colG - Panel.frameFrequency, 0);
			this.colB = Math.max(this.colB - Panel.frameFrequency, 0);
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
		this.posZ += this.vZ * Panel.frameFrequency;
		
		if (this.maxAge >= 0)
			this.age += Panel.frameFrequency;
		//this.age++;
		
		if (this.maxAge > 0 && this.age > this.maxAge && !removed)
		{
			removed = true;
			
			if (Game.effects.contains(this))
				Game.removeEffects.add(this);
			
			else if (Game.belowEffects.contains(this))
				Game.removeBelowEffects.add(this);
		}
	}
}

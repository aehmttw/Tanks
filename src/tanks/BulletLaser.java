package tanks;

import tanks.tank.Tank;

public class BulletLaser extends Bullet
{
	public BulletLaser(double x, double y, int bounces, Tank t) 
	{
		super(x, y, bounces, t);
		t.liveBullets--;
		this.playPopSound = false;
		this.baseColorR = 255;
		this.baseColorG = 0;
		this.baseColorB = 0;

	}
	
	/** Do not use, instead use the constructor with primitive data types. */
	@Deprecated
	public BulletLaser(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x, y, bounces, t);
		this.item = ib;
		this.item.liveBullets--;
	}
	
	@Override
	public void update()
	{
		this.shoot();
		Drawing.drawing.playSound("resources/laser.wav");
		Game.movables.remove(this);
	}
	
	public void shoot()
	{
		while(!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;
			
			super.update();
			Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.laser));
		}

		if (Game.fancyGraphics)
		{
			for (int i = 0; i < this.size * 4; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
				int var = 50;
				e.maxAge /= 2;
				e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
				Game.effects.add(e);
			}
		}
	}
}

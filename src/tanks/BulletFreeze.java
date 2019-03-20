package tanks;

import tanks.tank.Tank;

public class BulletFreeze extends Bullet
{
	public BulletFreeze(double x, double y, int bounces, Tank t) 
	{
		super(x, y, bounces, t);
		this.outlineColorR = 255;
		this.outlineColorG = 255;
		this.outlineColorB = 255;

	}
	
	public BulletFreeze(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount) 
	{
		this(x, y, bounces, t);
		this.affectsMaxLiveBullets = affectsLiveBulletCount;
		if (!this.affectsMaxLiveBullets)
			this.tank.liveBullets--;
	}

	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public BulletFreeze(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, false);
		this.item = ib;
	}
	
	@Override
	public void update()
	{
		if (this.destroy && this.destroyTimer == 0)
		{
			Game.movables.add(new AreaEffectFreeze(this.posX, this.posY));
			Drawing.drawing.playSound("resources/freeze.wav");
		}

		super.update();
	}	

	@Override
	public void draw()
	{
		if (Game.fancyGraphics)
		{
			for (int i = 0; i < 30 - 10 * Math.sin(this.age / 12.0); i++)
			{
				Drawing.drawing.setColor(255, 255, 255, 20);
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
			}
		}

		super.draw();
	}

}

package tanks.bullet;

import tanks.AreaEffectFreeze;
import tanks.Drawing;
import tanks.Game;
import tanks.hotbar.ItemBullet;
import tanks.tank.Tank;

public class BulletFreeze extends Bullet
{
	public BulletFreeze(double x, double y, int bounces, Tank t) 
	{
		this(x, y, bounces, t, true, null);
	}
	
	public BulletFreeze(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.outlineColorR = 255;
		this.outlineColorG = 255;
		this.outlineColorB = 255;
		this.name = "freeze";

		this.playPopSound = false;
	}

	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public BulletFreeze(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, true, ib);
	}
	
	@Override
	public void onDestroy()
	{
		Game.movables.add(new AreaEffectFreeze(this.posX, this.posY));
		Drawing.drawing.playGlobalSound("freeze.ogg");
	}

	@Override
	public void draw()
	{
		if (Game.fancyGraphics)
		{
			for (int i = 0; i < 30 - 10 * Math.sin(this.age / 12.0); i++)
			{
				Drawing.drawing.setColor(255, 255, 255, 20);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, i * 4, i * 4);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, i * 4, i * 4);
			}
		}

		super.draw();
	}

}

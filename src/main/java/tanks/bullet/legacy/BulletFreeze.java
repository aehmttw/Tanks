package tanks.bullet.legacy;

import tanks.AreaEffectFreeze;
import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.legacy.ItemBullet;
import tanks.tank.Tank;

public class BulletFreeze extends Bullet
{
	public static String bullet_name = "freezing";

	public BulletFreeze(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, true, ib);
	}
	
	public BulletFreeze(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.overrideOutlineColor = true;
		this.outlineColorR = 255;
		this.outlineColorG = 255;
		this.outlineColorB = 255;
		this.name = bullet_name;

		this.playPopSound = false;
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
		if (Game.bulletTrails)
		{
			for (int i = 0; i < 30 - 10 * Math.sin(this.age / 12.0); i++)
			{
				Drawing.drawing.setColor(255, 255, 255, 20, 1);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, i * 4, i * 4);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, i * 4, i * 4);
			}
		}

		super.draw();
	}

}

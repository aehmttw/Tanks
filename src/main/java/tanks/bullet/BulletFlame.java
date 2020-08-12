package tanks.bullet;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletFlame extends Bullet
{
	double life = 100;
	double age = 0;
	double frequency = Panel.frameFrequency;
	
	public BulletFlame(double x, double y, int bounces, Tank t) 
	{
		this(x, y, bounces, t, false, null);
	}
	
	/** Do not use, instead use the constructor with primitive data types. Intended for Item use only!*/
	@Deprecated
	public BulletFlame(Double x, Double y, Integer bounces, Tank t, ItemBullet ib) 
	{
		this(x.doubleValue(), y.doubleValue(), bounces.intValue(), t, false, ib);
	}
	
	public BulletFlame(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.useCustomWallCollision = true;
		this.playPopSound = false;
		this.name = "flame";
		this.itemSound = "flame.ogg";
	}
	
	@Override
	public void update()
	{
		this.age += Panel.frameFrequency;
		this.size = (int) (this.age + 10);
		
		this.damage = frequency * Math.max(0, 0.2 - this.age / 500.0) / 2;
		
		super.update();
		
		if (this.age > life)
			Game.removeMovables.add(this);
	}
	
	@Override
	public void draw()
	{
		double rawOpacity = (1.0 - (this.age)/life);
		rawOpacity *= rawOpacity;
		double opacity = rawOpacity * 255;
		
		double green = (255 - 255.0 * (this.age / life));
		
		Drawing.drawing.setColor(255, green, 0, opacity);
		
		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size, size);

		Drawing.drawing.setColor(255, green, 0, opacity);

		if (Game.superGraphics)
		{
			if (Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, size * 2, size * 2, true, false);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, size * 2, size * 2);
		}
	}

}

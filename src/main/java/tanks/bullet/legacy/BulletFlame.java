package tanks.bullet.legacy;

import tanks.Drawing;
import tanks.Game;
import tanks.IDrawableWithGlow;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.hotbar.item.ItemBullet;
import tanks.tank.Tank;

public class BulletFlame extends Bullet implements IDrawableWithGlow
{
	public static String bullet_name = "flamethrower";

	double life = 100;
	double age = 0;
	public double sizeMul = 1;

	public BulletFlame(double x, double y, int bounces, Tank t, ItemBullet ib)
	{
		this(x, y, bounces, t, false, ib);
	}

	public BulletFlame(double x, double y, int bounces, Tank t, boolean affectsLiveBulletCount, ItemBullet ib)
	{
		super(x, y, bounces, t, affectsLiveBulletCount, ib);
		this.useCustomWallCollision = true;
		this.playPopSound = false;
		this.playBounceSound = false;
		this.name = bullet_name;
		this.bulletCollision = false;
		this.itemSound = "flame.ogg";
		this.pitchVariation = 0.0;
	}
	
	@Override
	public void update()
	{
		if (this.age <= 0)
		{
			this.sizeMul = this.size / Bullet.bullet_size;
			this.life *= this.sizeMul;
			System.out.println(this.frameDamageMultipler);
		}

		this.age += Panel.frameFrequency;
		this.size = (int) (this.age / sizeMul + 10);
		
		this.damage = Math.max(0, 0.2 - this.age / sizeMul / 500.0) / 2;
		
		super.update();
		
		if (this.age > life)
			Game.removeMovables.add(this);
	}
	
	@Override
	public void draw()
	{
		double rawOpacity = (1.0 - (this.age) / life);
		rawOpacity *= rawOpacity;
		double opacity = rawOpacity * 255;
		
		double green = (255 - 255.0 * (this.age / life));
		
		Drawing.drawing.setColor(255, green, 0, opacity, 1);
		
		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, size, size);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size, size);
	}

	@Override
	public void drawGlow()
	{
		double rawOpacity = (1.0 - (this.age)/life);

		double green = (255 - 255.0 * (this.age / life));
		double mul = Math.sqrt(rawOpacity) / 2;
		Drawing.drawing.setColor(255 * mul, green * mul, green * mul / 2, 255, 1);

		if (Game.enable3d)
			Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, size * 3, size * 3, true, false);
		else
			Drawing.drawing.fillGlow(this.posX, this.posY, size * 3, size * 3);
	}

	@Override
	public boolean isGlowEnabled()
	{
		return true;
	}

	@Override
	public void addDestroyEffect()
	{

	}
}

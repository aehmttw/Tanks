package tanks.bullets;

import tanks.tank.Tank;

public abstract class BulletInstant extends Bullet
{
	public boolean shotQueued = false;
	
	public BulletInstant(double x, double y, int bounces, Tank t)
	{
		super(x, y, bounces, t);
	}
	
	public BulletInstant(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, true);
	}
	
	public BulletInstant(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, boolean fireEvent)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets, fireEvent);
	}
	
	public abstract void shoot();

}

package tanks.bullet;

import tanks.tank.Tank;

public abstract class BulletInstant extends Bullet
{
	public boolean shotQueued = false;
	
	public BulletInstant(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets)
	{
		super(x, y, bounces, t, affectsMaxLiveBullets);
	}

}

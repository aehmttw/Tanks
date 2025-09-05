package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.bullet.Bullet;

public class EventBulletBounce extends PersonalEvent
{
	public int bullet;
	public double posX;
	public double posY;
	public double vX;
	public double vY;

	public EventBulletBounce()
	{

	}

	public EventBulletBounce(Bullet b)
	{
		this.bullet = b.networkID;
		this.posX = b.collisionX;
		this.posY = b.collisionY;
		this.vX = b.vX;
		this.vY = b.vY;
	}

	@Override
	public void execute()
	{
		Bullet b = Bullet.idMap.get(this.bullet);
		
		if (b != null && this.clientID == null)
		{
			b.posX = this.posX;
			b.posY = this.posY;
			b.vX = this.vX;
			b.vY = this.vY;
			b.collisionX = this.posX;
			b.collisionY = this.posY;
			b.addTrail();
		}
	}

}

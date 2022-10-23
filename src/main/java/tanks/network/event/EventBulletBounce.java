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
	public void write(ByteBuf b)
	{
		b.writeInt(this.bullet);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.vX);
		b.writeDouble(this.vY);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.bullet = b.readInt();
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.vX = b.readDouble();
		this.vY = b.readDouble();
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

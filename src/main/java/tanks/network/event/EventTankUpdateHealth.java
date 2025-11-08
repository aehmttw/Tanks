package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.tank.Tank;

public class EventTankUpdateHealth extends PersonalEvent
{
	public int tank;
	public double health;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t)
	{
		tank = t.networkID;
		health = t.health;
	}
	
	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(tank);

		if (t == null || this.clientID != null)
			return;

		if (t.health > health && health > 0)
			t.damageFlashAnimation = 1;
		else if (health > t.health)
			t.healFlashAnimation = 1;

		double before = t.health;
		t.health = health;

		if ((int) (before) != (int) (t.health) && t.health > 0)
		{
			Effect e = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.shield);
			e.size = t.size;
			e.radius = t.health - 1;
			Game.effects.add(e);
		}

		if (t.health <= 0)
		{
			t.vX = 0;
			t.vY = 0;
		}
	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeDouble(this.health);
	}

	@Override
	public void read(ByteBuf b)
	{
		this.tank = b.readInt();
		this.health = b.readDouble();
	}
}

package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.tank.Tank;

public class EventTankCharge extends PersonalEvent implements IStackableEvent
{
	public int tank;
	public double charge;

	public EventTankCharge()
	{

	}

	public EventTankCharge(int tank, double charge)
	{
		this.tank = tank;
		this.charge = charge;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		Tank t = Tank.idMap.get(this.tank);

		if (t == null)
			return;

        if (Math.random() * Game.effectMultiplier < charge && Game.effectsEnabled)
        {
            Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 4, Effect.EffectType.charge);

            e.setColor(t.color);

            Game.effects.add(e);
        }

	}

	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.tank);
		b.writeDouble(this.charge);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.tank = b.readInt();
		this.charge = b.readDouble();
	}

	@Override
	public int getIdentifier()
	{
		return this.tank;
	}
}

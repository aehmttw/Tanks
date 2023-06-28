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

            double var = 50;
            e.colR = Math.min(255, Math.max(0, t.colorR + Math.random() * var - var / 2));
            e.colG = Math.min(255, Math.max(0, t.colorG + Math.random() * var - var / 2));
            e.colB = Math.min(255, Math.max(0, t.colorB + Math.random() * var - var / 2));

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

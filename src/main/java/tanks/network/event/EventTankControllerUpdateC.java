package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Panel;
import tanks.tank.*;

public class EventTankControllerUpdateC extends PersonalEvent implements IStackableEvent
{
    public int tank;
    public double posX;
    public double posY;
    public double vX;
    public double vY;
    public double angle;
    public double mX;
    public double mY;
    public boolean action1;
    public boolean action2;
    public double time;
    public long sysTime = System.currentTimeMillis();

    @NetworkIgnored
    public boolean[] quickActions = new boolean[TankPlayer.max_abilities];

    public EventTankControllerUpdateC()
    {

    }

    public EventTankControllerUpdateC(TankPlayerController t)
    {
        this.tank = t.networkID;
        this.posX = t.posX;
        this.posY = t.posY;
        this.vX = t.vX;
        this.vY = t.vY;
        this.mX = t.mouseX;
        this.mY = t.mouseY;
        this.angle = t.angle;
        this.action1 = t.action1;
        this.action2 = t.action2;
        System.arraycopy(t.quickActions, 0, this.quickActions, 0, this.quickActions.length);
        this.time = Panel.frameFrequency;
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeShort(quickActions.length);
        for (boolean b1: quickActions)
            b.writeBoolean(b1);
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        int len = b.readShort();
        for (int i = 0; i < len; i++)
            this.quickActions[i] = b.readBoolean();
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t instanceof TankPlayerRemote && ((TankPlayerRemote) t).player.clientID.equals(this.clientID))
            ((TankPlayerRemote) t).controllerUpdate(this.posX, this.posY, this.vX, this.vY, this.angle, this.mX, this.mY, this.action1, this.action2, this.quickActions, this.time, this.sysTime);
    }

    @Override
    public boolean isStackable()
    {
        return !(action1 || action2);
    }

    @Override
    public int getIdentifier()
    {
        return tank;
    }
}

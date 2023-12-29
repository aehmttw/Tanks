package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Panel;
import tanks.tank.Tank;
import tanks.tank.TankPlayerController;
import tanks.tank.TankPlayerRemote;

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
        this.time = Panel.frameFrequency;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.tank);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.vX);
        b.writeDouble(this.vY);
        b.writeDouble(this.angle);
        b.writeDouble(this.mX);
        b.writeDouble(this.mY);
        b.writeBoolean(this.action1);
        b.writeBoolean(this.action2);
        b.writeDouble(this.time);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.tank = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.vX = b.readDouble();
        this.vY = b.readDouble();
        this.angle = b.readDouble();
        this.mX = b.readDouble();
        this.mY = b.readDouble();
        this.action1 = b.readBoolean();
        this.action2 = b.readBoolean();
        this.time = b.readDouble();
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t instanceof TankPlayerRemote && ((TankPlayerRemote) t).player.clientID.equals(this.clientID))
        {
            ((TankPlayerRemote) t).controllerUpdate(this.posX, this.posY, this.vX, this.vY, this.angle, this.mX, this.mY, this.action1, this.action2, this.time, this.sysTime);
        }
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
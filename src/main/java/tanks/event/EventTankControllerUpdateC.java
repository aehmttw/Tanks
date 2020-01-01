package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.tank.Tank;
import tanks.tank.TankPlayerController;
import tanks.tank.TankPlayerRemote;

public class EventTankControllerUpdateC extends PersonalEvent
{
    public int tank;
    public double posX;
    public double posY;
    public double vX;
    public double vY;
    public double angle;
    public boolean action1;
    public boolean action2;
    public long time;

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
        this.angle = t.angle;
        this.action1 = t.action1;
        this.action2 = t.action2;
        this.time = System.currentTimeMillis();
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
        b.writeBoolean(this.action1);
        b.writeBoolean(this.action2);
        b.writeLong(this.time);
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
        this.action1 = b.readBoolean();
        this.action2 = b.readBoolean();
        this.time = b.readLong();
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t instanceof TankPlayerRemote && ((TankPlayerRemote) t).player.clientID.equals(this.clientID))
        {
            ((TankPlayerRemote) t).controllerUpdate(this.posX, this.posY, this.vX, this.vY, this.angle, this.action1, this.action2, this.frame, this.time);
        }
    }

}
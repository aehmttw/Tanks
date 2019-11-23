package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.tank.Tank;
import tanks.tank.TankPlayerController;
import tanks.tank.TankRemote;

public class EventTankControllerUpdateS extends EventTankUpdate
{
    public boolean forced;

    public EventTankControllerUpdateS()
    {

    }

    public EventTankControllerUpdateS(Tank t, boolean forced)
    {
        super(t);
        this.forced = forced;
    }

    @Override
    public void read(ByteBuf b)
    {
        super.read(b);
        this.forced = b.readBoolean();
    }

    @Override
    public void write(ByteBuf b)
    {
        super.write(b);
        b.writeBoolean(this.forced);
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (this.clientID == null && (t instanceof TankRemote || (t instanceof TankPlayerController && (this.forced || !Game.clientID.equals(((TankPlayerController) t).clientID)))))
        {
            t.posX = this.posX;
            t.posY = this.posY;
            t.vX = this.vX;
            t.vY = this.vY;
            t.angle = this.angle;
        }
    }
}

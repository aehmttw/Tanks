package tanks.network.event;

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
    public int quickActions;
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

        int i = 0;
        for (boolean b : t.quickActions)
            this.quickActions |= (b ? 1 : 0) << i++;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(this.tank);

        if (t instanceof TankPlayerRemote && ((TankPlayerRemote) t).player.clientID.equals(this.clientID))
        {
            ((TankPlayerRemote) t).controllerUpdate(this.posX, this.posY, this.vX, this.vY, this.angle, this.mX, this.mY, this.action1, this.action2, this.quickActions, this.time,
                this.sysTime);
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

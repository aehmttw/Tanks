package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventTankControllerUpdateAmmunition extends PersonalEvent
{
    public UUID clientIdTarget;
    public int action1Live;
    public int action1Max;
    public int action2Live;
    public int action2Max;


    public EventTankControllerUpdateAmmunition()
    {

    }

    public EventTankControllerUpdateAmmunition(UUID clientID, int a1, int a1max, int a2, int a2max)
    {
        this.clientIdTarget = clientID;
        this.action1Live = a1;
        this.action1Max = a1max;
        this.action2Live = a2;
        this.action2Max = a2max;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.clientIdTarget.toString());
        b.writeInt(this.action1Live);
        b.writeInt(this.action1Max);
        b.writeInt(this.action2Live);
        b.writeInt(this.action2Max);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.clientIdTarget = UUID.fromString(NetworkUtils.readString(b));
        this.action1Live = b.readInt();
        this.action1Max = b.readInt();
        this.action2Live = b.readInt();
        this.action2Max = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && clientIdTarget.equals(Game.clientID))
        {
            Game.playerTank.liveBullets = action1Live;
            Game.playerTank.liveBulletMax = action1Max;
            Game.playerTank.liveMines = action2Live;
            Game.playerTank.liveMinesMax = action2Max;
        }
    }
}

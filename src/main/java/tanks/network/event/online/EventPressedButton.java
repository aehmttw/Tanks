package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.network.event.PersonalEvent;
import tanksonline.TanksOnlineServerHandler;

public class EventPressedButton extends PersonalEvent implements IOnlineServerEvent
{
    public int id;

    public EventPressedButton()
    {

    }

    public EventPressedButton(int buttonID)
    {
        this.id = buttonID;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
    }

    @Override
    public void execute()
    {

    }

    @Override
    public void execute(TanksOnlineServerHandler s)
    {
        if (this.clientID != null)
            s.screen.buttonPressed(this.id);
    }
}

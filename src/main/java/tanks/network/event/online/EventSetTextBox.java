package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;
import tanksonline.TanksOnlineServerHandler;

public class EventSetTextBox extends PersonalEvent implements IOnlineServerEvent
{
    public int id;
    public String input;

    public EventSetTextBox()
    {

    }

    public EventSetTextBox(int textBoxID, String input)
    {
        this.id = textBoxID;
        this.input = input;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        NetworkUtils.writeString(b, this.input);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.input = NetworkUtils.readString(b);
    }

    @Override
    public void execute()
    {

    }

    @Override
    public void execute(TanksOnlineServerHandler s)
    {
        if (this.clientID != null)
            s.screen.textboxEdited(this.id, this.input);
    }
}

package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.event.PersonalEvent;
import tanks.gui.screen.ScreenOnline;
import tanks.network.NetworkUtils;

public class EventUpdateTextBox extends PersonalEvent
{
    public int id;
    public String input;

    public EventUpdateTextBox()
    {

    }

    public EventUpdateTextBox(int textBoxID, String input)
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
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ((ScreenOnline) Game.screen).textboxes.get(id).inputText = input;
        }
    }
}

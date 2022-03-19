package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.NetworkUtils;

public class EventServerChat extends PersonalEvent
{
    public String message;
    public boolean iconEnabled;
    public int r1;
    public int g1;
    public int b1;
    public int r2;
    public int g2;
    public int b2;

    public EventServerChat() {}

    public EventServerChat(String message)
    {
        this.message = message;
        this.iconEnabled = false;
    }

    public EventServerChat(String message, int r1, int g1, int b1, int r2, int g2, int b2)
    {
        this.message = message;
        this.iconEnabled = true;
        this.r1 = r1;
        this.g1 = g1;
        this.b1 = b1;
        this.r2 = r2;
        this.g2 = g2;
        this.b2 = b2;
    }


    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.message);
        b.writeBoolean(this.iconEnabled);

        if (this.iconEnabled)
        {
            b.writeInt(this.r1);
            b.writeInt(this.g1);
            b.writeInt(this.b1);
            b.writeInt(this.r2);
            b.writeInt(this.g2);
            b.writeInt(this.b2);
        }

    }

    @Override
    public void read(ByteBuf b)
    {
        this.message = NetworkUtils.readString(b);
        this.iconEnabled = b.readBoolean();

        if (this.iconEnabled)
        {
            this.r1 = b.readInt();
            this.g1 = b.readInt();
            this.b1 = b.readInt();
            this.r2 = b.readInt();
            this.g2 = b.readInt();
            this.b2 = b.readInt();
        }
    }

    @Override
    public void execute()
    {
        ChatMessage c = new ChatMessage(this.message);
        c.enableTankIcon = this.iconEnabled;

        if (this.iconEnabled)
        {
            c.r1 = this.r1;
            c.g1 = this.g1;
            c.b1 = this.b1;

            c.r2 = this.r2;
            c.g2 = this.g2;
            c.b2 = this.b2;
        }

        ScreenPartyHost.chat.add(0, c);
    }
}

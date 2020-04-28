package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.UUIDTextBox;
import tanks.gui.screen.ScreenOnline;
import tanks.gui.screen.ScreenOnlineWaiting;
import tanks.network.NetworkUtils;

public class EventAddUUIDTextBox extends EventAddTextBox
{
    public EventAddUUIDTextBox()
    {

    }

    public EventAddUUIDTextBox(int id, UUIDTextBox t)
    {
        this.id = id;
        this.label = t.labelText;
        this.defaultInput = t.inputText;
        this.posX = t.posX;
        this.posY = t.posY;
        this.sizeX = t.sizeX;
        this.sizeY = t.sizeY;
        this.hover = t.hoverTextRaw;
        this.wait = t.wait;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            UUIDTextBox t;

            final int textBoxID = this.id;
            t = new UUIDTextBox(this.posX, this.posY, this.sizeX, this.sizeY, this.label, null, this.defaultInput, this.hover);

            t.function = new Runnable()
            {
                @Override
                public void run()
                {
                    Game.eventsOut.add(new EventSetTextBox(textBoxID, t.inputText));

                    if (wait)
                        Game.screen = new ScreenOnlineWaiting();
                }
            };

            if (hover.equals(""))
                t.enableHover = false;

            s.addTextbox(this.id, t);
        }
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(id);

        NetworkUtils.writeString(b, label);
        NetworkUtils.writeString(b, defaultInput);
        b.writeDouble(posX);
        b.writeDouble(posY);
        b.writeDouble(sizeX);
        b.writeDouble(sizeY);
        NetworkUtils.writeString(b, hover);

        b.writeBoolean(wait);
    }

    @Override
    public void read(ByteBuf b)
    {
        id = b.readInt();

        label = NetworkUtils.readString(b);
        defaultInput = NetworkUtils.readString(b);
        posX = b.readDouble();
        posY = b.readDouble();
        sizeX = b.readDouble();
        sizeY = b.readDouble();
        hover = NetworkUtils.readString(b);

        wait = b.readBoolean();
    }
}

package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanks.gui.screen.ScreenOnlineWaiting;
import tanks.network.NetworkUtils;

public class EventAddButton extends PersonalEvent
{
    public int id;

    public String text;
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public boolean enabled;
    public String hover;

    public int xAlignment;
    public int yAlignment;

    public boolean wait;

    public EventAddButton()
    {

    }

    public EventAddButton(int id, Button b)
    {
        this.id = id;
        this.text = b.text;
        this.posX = b.posX;
        this.posY = b.posY;
        this.sizeX = b.sizeX;
        this.sizeY = b.sizeY;
        this.enabled = b.enabled;
        this.hover = b.hoverTextRaw;
        this.wait = b.wait;
        this.xAlignment = b.xAlignment;
        this.yAlignment = b.yAlignment;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        NetworkUtils.writeString(b, this.text);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.sizeX);
        b.writeDouble(this.sizeY);
        b.writeBoolean(this.enabled);
        NetworkUtils.writeString(b, this.hover);
        b.writeInt(this.xAlignment);
        b.writeInt(this.yAlignment);
        b.writeBoolean(this.wait);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.text = NetworkUtils.readString(b);
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.sizeX = b.readDouble();
        this.sizeY = b.readDouble();
        this.enabled = b.readBoolean();
        this.hover = NetworkUtils.readString(b);
        this.xAlignment = b.readInt();
        this.yAlignment = b.readInt();
        this.wait = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            Button b;

            final int buttonID = this.id;
            b = new Button(this.posX, this.posY, this.sizeX, this.sizeY, this.text, () ->
            {
                Game.eventsOut.add(new EventPressedButton(buttonID));

                if (wait)
                    Game.screen = new ScreenOnlineWaiting();
            }, hover);

            b.enabled = this.enabled;
            b.enableHover = !this.hover.equals("");

            b.posX -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeX * (xAlignment + 1) / 2.0;
            b.posY -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeY * (yAlignment + 1) / 2.0;

            s.addButton(this.id, b);
        }
    }
}

package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenOnline;
import tanks.network.NetworkUtils;

public class EventAddText extends PersonalEvent
{
    public int id;

    public String text;
    public double posX;
    public double posY;
    public double size;
    public int alignment;

    public int xAlignment;
    public int yAlignment;

    public EventAddText()
    {

    }


    public EventAddText(int id, ScreenOnline.Text t)
    {
        this.id = id;
        this.text = t.text;
        this.posX = t.posX;
        this.posY = t.posY;
        this.size = t.size;
        this.alignment = t.alignment;
        this.xAlignment = t.xAlignment;
        this.yAlignment = t.yAlignment;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        NetworkUtils.writeString(b, this.text);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.size);
        b.writeInt(this.alignment);
        b.writeInt(this.xAlignment);
        b.writeInt(this.yAlignment);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.text = NetworkUtils.readString(b);
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.size = b.readDouble();
        this.alignment = b.readInt();
        this.xAlignment = b.readInt();
        this.yAlignment = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            ScreenOnline.Text t = new ScreenOnline.Text(this.text, this.posX, this.posY, this.size, this.alignment);
            t.posX -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeX * (xAlignment + 1) / 2.0;
            t.posY -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeY * (yAlignment + 1) / 2.0;
            s.addText(this.id, t);
        }
    }
}

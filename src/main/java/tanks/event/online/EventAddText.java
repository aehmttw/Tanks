package tanks.event.online;

import io.netty.buffer.ByteBuf;
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
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            s.addText(this.id, new ScreenOnline.Text(this.text, this.posX, this.posY, this.size, this.alignment));
        }
    }
}

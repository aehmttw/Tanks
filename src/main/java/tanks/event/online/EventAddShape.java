package tanks.event.online;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.event.PersonalEvent;
import tanks.gui.screen.ScreenOnline;

public class EventAddShape extends PersonalEvent
{
    public int id;

    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public int type;

    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;

    public EventAddShape()
    {

    }


    public EventAddShape(int id, ScreenOnline.Shape s)
    {
        this.id = id;
        this.posX = s.posX;
        this.posY = s.posY;
        this.sizeX = s.sizeX;
        this.sizeY = s.sizeY;
        this.type = s.type;
        this.colorR = s.colorR;
        this.colorG = s.colorG;
        this.colorB = s.colorB;
        this.colorA = s.colorA;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.id);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
        b.writeDouble(this.sizeX);
        b.writeDouble(this.sizeY);
        b.writeInt(this.type);
        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.colorA);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.id = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
        this.sizeX = b.readDouble();
        this.sizeY = b.readDouble();
        this.type = b.readInt();
        this.colorR = b.readDouble();
        this.colorG = b.readDouble();
        this.colorB = b.readDouble();
        this.colorA = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            s.addShape(this.id, new ScreenOnline.Shape(this.posX, this.posY, this.sizeX, this.sizeY, this.type, this.colorR, this.colorG, this.colorB, this.colorA));
        }
    }
}

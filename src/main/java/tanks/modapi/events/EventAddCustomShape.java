package tanks.modapi.events;

import io.netty.buffer.ByteBuf;
import tanks.event.PersonalEvent;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.CustomShape;
import tanks.network.NetworkUtils;

public class EventAddCustomShape extends PersonalEvent
{
    public CustomShape.types type;
    public int posX;
    public int posY;
    public int sizeX;
    public int sizeY;
    public int duration;
    public int colorR;
    public int colorB;
    public int colorG;
    public int colorA;

    public EventAddCustomShape()
    {

    }

    public EventAddCustomShape(CustomShape.types type, int x, int y, int sizeX, int sizeY, int duration, int r, int g, int b, int a)
    {
        this.type = type;
        this.posX = x;
        this.posY = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        this.duration = duration;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.type.toString());
        b.writeInt(this.posX);
        b.writeInt(this.posY);
        b.writeInt(this.sizeX);
        b.writeInt(this.sizeY);
        b.writeInt(this.duration);
        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);
        b.writeInt(this.colorA);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.type = CustomShape.types.valueOf(NetworkUtils.readString(b));
        this.posX = b.readInt();
        this.posY = b.readInt();
        this.sizeX = b.readInt();
        this.sizeY = b.readInt();
        this.duration = b.readInt();
        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();
        this.colorA = b.readInt();
    }

    @Override
    public void execute()
    {
        ModAPI.menuGroup.add(new CustomShape(this.type, this.posX, this.posY, this.sizeX, this.sizeY, this.duration, this.colorR, this.colorG, this.colorB, this.colorA));
    }
}

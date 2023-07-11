package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.network.NetworkUtils;

public class EventAddFixedShape extends PersonalEvent
{
    public String type;
    public int posX;
    public int posY;
    public int sizeX;
    public int sizeY;
    public int colorR;
    public int colorB;
    public int colorG;
    public boolean centered;

    public EventAddFixedShape()
    {

    }

    public EventAddFixedShape(String type, int x, int y)
    {
        this(type, x, y, (int) Drawing.drawing.currentColorR, (int) Drawing.drawing.currentColorG, (int) Drawing.drawing.currentColorB, true);
    }

    public EventAddFixedShape(String type, int x, int y, int r, int g, int b)
    {
        this(type, x, y, r, g, b, true);
    }

    public EventAddFixedShape(String type, int x, int y, int r, int g, int b, boolean centered)
    {
        this.type = type;
        this.posX = x;
        this.posY = y;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        this.centered = centered;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.type);
        b.writeInt(this.posX);
        b.writeInt(this.posY);

        b.writeInt(this.colorR);
        b.writeInt(this.colorG);
        b.writeInt(this.colorB);

        b.writeBoolean(this.centered);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.type = NetworkUtils.readString(b);
        this.posX = b.readInt();
        this.posY = b.readInt();

        this.colorR = b.readInt();
        this.colorG = b.readInt();
        this.colorB = b.readInt();

        this.centered = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.centered)
        {
            this.posX -= this.sizeX / 2;
            this.posY -= this.sizeY / 2;
        }

        /*Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        if (this.type.equals("rect fill"))
            ModAPI.fixedShapes.fillRect(this.posX, this.posY, this.sizeX, this.sizeY);

        else if (this.type.equals("rect nofill"))
            ModAPI.fixedShapes.drawRect(this.posX, this.posY, this.sizeX, this.sizeY);

        else if (this.type.equals("oval fill"))
            ModAPI.fixedShapes.fillOval(this.posX, this.posY, this.sizeX, this.sizeY);

        else if (this.type.equals("oval nofill"))
            ModAPI.fixedShapes.drawOval(this.posX, this.posY, this.sizeX, this.sizeY);

        else if (this.type.startsWith("image"))
            ModAPI.fixedShapes.drawImage(this.posX, this.posY, this.sizeX, this.sizeY, this.type.substring(5), false);

        else
            System.err.println("Invalid type: " + this.type);*/
    }
}

package tanks.network.event.online;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenOnline;
import tanks.network.event.PersonalEvent;

import io.netty.buffer.ByteBuf;

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

    public int xAlignment;
    public int yAlignment;

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
        this.xAlignment = s.xAlignment;
        this.yAlignment = s.yAlignment;
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            ScreenOnline.Shape shape = new ScreenOnline.Shape(this.posX, this.posY, this.sizeX, this.sizeY, this.type, this.colorR, this.colorG, this.colorB, this.colorA);
            shape.posX -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeX * (xAlignment + 1) / 2.0;
            shape.posY -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeY * (yAlignment + 1) / 2.0;
            s.addShape(this.id, shape);
        }
    }
}

package tanks.gui;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.network.NetworkUtils;

public class TextWithStyling
{
    public boolean changedByAnimation = false;

    public String text;

    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;
    public double fontSize;

    public TextWithStyling(String text, double r, double g, double b, double fontSize)
    {
        this(text, r, g, b, 255, fontSize);
    }

    public TextWithStyling(String text, double r, double g, double b)
    {
        this(text, r, g, b, 255, 24);
    }

    public TextWithStyling(String text, double r, double g, double b, double a, double fontSize)
    {
        this.text = text;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.colorA = a;
        this.fontSize = fontSize;
    }

    public TextWithStyling()
    {

    }

    public void setColor()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
    }

    public void drawText(double posX, double posY, double posZ)
    {
        setColor();
        Drawing.drawing.setFontSize(this.fontSize);
        Drawing.drawing.drawText(posX, posY, posZ, this.text);
    }

    public void drawText(double posX, double posY)
    {
        setColor();
        Drawing.drawing.setFontSize(this.fontSize);
        Drawing.drawing.drawText(posX, posY, this.text);
    }

    public void drawInterfaceText(double posX, double posY)
    {
        setColor();
        Drawing.drawing.setInterfaceFontSize(this.fontSize);
        Drawing.drawing.drawInterfaceText(posX, posY, this.text);
    }

    public TextWithStyling shadowColor()
    {
        double[] output = new double[] {this.colorR - 50, this.colorG - 50, this.colorB - 50};

        for (int i = 0; i < 3; i++)
        {
            if (output[i] < 0)
                output[i] += 100;
        }

        return new TextWithStyling(text, output[0], output[1], output[2], 255, this.fontSize);
    }

    public void writeTo(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.text);
        b.writeDouble(this.fontSize);

        b.writeDouble(this.colorR);
        b.writeDouble(this.colorG);
        b.writeDouble(this.colorB);
        b.writeDouble(this.colorA);
    }

    public static TextWithStyling readFrom(ByteBuf b)
    {
        TextWithStyling t = new TextWithStyling();
        t.text = NetworkUtils.readString(b);
        t.fontSize = b.readDouble();

        t.colorR = b.readDouble();
        t.colorG = b.readDouble();
        t.colorB = b.readDouble();
        t.colorA = b.readDouble();

        return t;
    }
}

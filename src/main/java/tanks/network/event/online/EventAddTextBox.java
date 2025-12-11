package tanks.network.event.online;

import io.netty.buffer.ByteBuf;
import tanks.*;
import tanks.gui.TextBox;
import tanks.gui.screen.*;
import tanks.network.NetworkUtils;
import tanks.network.event.PersonalEvent;

public class EventAddTextBox extends PersonalEvent
{
    public int id;

    public String label;
    public String defaultInput;
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public String hover;

    public boolean enableSpaces;
    public boolean allowSpaces;
    public boolean allowLetters;
    public boolean allowNumbers;
    public boolean allowColons;
    public boolean allowAll;
    public boolean allowDots;
    public boolean enablePunctuation;
    public boolean checkMaxValue;
    public boolean checkMinValue;
    public boolean allowNegatives;
    public boolean allowDoubles;

    public boolean lowerCase;
    public boolean enableCaps;

    public int maxChars;
    public double maxValue;
    public double minValue;

    public int xAlignment;
    public int yAlignment;

    public boolean wait;

    public EventAddTextBox()
    {

    }

    public EventAddTextBox(int id, TextBox t)
    {
        this.id = id;
        this.label = t.labelText;
        this.defaultInput = t.inputText;
        this.posX = t.posX;
        this.posY = t.posY;
        this.sizeX = t.sizeX;
        this.sizeY = t.sizeY;
        this.hover = t.hoverTextRaw;
        this.enableSpaces = t.enableSpaces;
        this.allowSpaces = t.allowSpaces;
        this.allowLetters = t.allowLetters;
        this.allowNumbers = t.allowNumbers;
        this.allowColons = t.allowColons;
        this.allowAll = t.allowAll;
        this.allowDots = t.allowDots;
        this.enablePunctuation = t.enablePunctuation;
        this.checkMaxValue = t.checkMaxValue;
        this.checkMinValue = t.checkMinValue;
        this.allowNegatives = t.allowNegatives;
        this.allowDoubles = t.allowDoubles;
        this.lowerCase = t.lowerCase;
        this.enableCaps = t.enableCaps;
        this.maxChars = t.maxChars;
        this.maxValue = t.maxValue;
        this.minValue = t.minValue;
        this.xAlignment = t.xAlignment;
        this.yAlignment = t.yAlignment;
        this.wait = t.wait;
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

        b.writeBoolean(enableSpaces);
        b.writeBoolean(allowSpaces);
        b.writeBoolean(allowLetters);
        b.writeBoolean(allowNumbers);
        b.writeBoolean(allowColons);
        b.writeBoolean(allowAll);
        b.writeBoolean(allowDots);
        b.writeBoolean(enablePunctuation);
        b.writeBoolean(checkMaxValue);
        b.writeBoolean(checkMinValue);
        b.writeBoolean(allowNegatives);
        b.writeBoolean(allowDoubles);

        b.writeBoolean(lowerCase);
        b.writeBoolean(enableCaps);

        b.writeInt(maxChars);
        b.writeDouble(maxValue);
        b.writeDouble(minValue);

        b.writeInt(xAlignment);
        b.writeInt(yAlignment);

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

        enableSpaces = b.readBoolean();
        allowSpaces = b.readBoolean();
        allowLetters = b.readBoolean();
        allowNumbers = b.readBoolean();
        allowColons = b.readBoolean();
        allowAll = b.readBoolean();
        allowDots = b.readBoolean();
        enablePunctuation = b.readBoolean();
        checkMaxValue = b.readBoolean();
        checkMinValue = b.readBoolean();
        allowNegatives = b.readBoolean();
        allowDoubles = b.readBoolean();

        lowerCase = b.readBoolean();
        enableCaps = b.readBoolean();

        maxChars = b.readInt();
        maxValue = b.readDouble();
        minValue = b.readDouble();

        xAlignment = b.readInt();
        yAlignment = b.readInt();

        wait = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.screen instanceof ScreenOnline)
        {
            ScreenOnline s = (ScreenOnline) Game.screen;
            TextBox t;

            final int textBoxID = this.id;
            t = new TextBox(this.posX, this.posY, this.sizeX, this.sizeY, this.label, null, this.defaultInput, this.hover);

            t.function = () ->
            {
                Game.eventsOut.add(new EventSetTextBox(textBoxID, t.inputText));

                if (wait)
                    Game.screen = new ScreenOnlineWaiting();
            };

            if (hover.equals(""))
                t.enableHover = false;

            t.enableSpaces = this.enableSpaces;
            t.allowSpaces = this.allowSpaces;
            t.allowLetters = this.allowLetters;
            t.allowNumbers = this.allowNumbers;
            t.allowColons = this.allowColons;
            t.allowAll = this.allowAll;
            t.allowDots = this.allowDots;
            t.enablePunctuation = this.enablePunctuation;
            t.checkMaxValue = this.checkMaxValue;
            t.checkMinValue = this.checkMinValue;
            t.allowNegatives = this.allowNegatives;
            t.allowDoubles = this.allowDoubles;
            t.lowerCase = this.lowerCase;
            t.enableCaps = this.enableCaps;
            t.maxChars = this.maxChars;
            t.maxValue = this.maxValue;
            t.minValue = this.minValue;
            t.posX -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeX * (xAlignment + 1) / 2.0;
            t.posY -= (Drawing.drawing.interfaceScaleZoom - 1) * Drawing.drawing.interfaceSizeY * (yAlignment + 1) / 2.0;
            s.addTextbox(this.id, t);
        }
    }
}

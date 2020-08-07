package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.gui.screen.ScreenInfo;
import tanks.gui.screen.ScreenSelector;

public class Selector implements IDrawable, ITrigger
{
    public Runnable function;
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;
    public String text;

    public boolean enableHover = false;
    public String[] hoverText;
    public String hoverTextRaw = "";

    public String[] options;
    public int selectedOption;

    public boolean selected = false;
    public boolean infoSelected = false;

    public boolean justPressed = false;

    public boolean enabled = true;

    public double colorR = 255;
    public double colorG = 255;
    public double colorB = 255;
    public double bgColorR = 200;
    public double bgColorG = 200;
    public double bgColorB = 200;
    public double hoverColorR = 240;
    public double hoverColorG = 240;
    public double hoverColorB = 255;

    public boolean quick = false;

    public boolean silent = false;

    public boolean format = true;

    //public String sound = "click.ogg";

    /** If set to true and is part of an online service, pressing the button sends the player to a loading screen*/
    public boolean wait = false;

    /** For online service use with changing interface scales
     * -1 = left
     * 0 = middle
     * 1 = right*/
    public int xAlignment = 0;

    /** For online service use with changing interface scales
     * -1 = top
     * 0 = middle
     * 1 = bottom*/
    public int yAlignment = 0;

    public Selector(double x, double y, double sX, double sY, String text, String[] o, Runnable f)
    {
        this.function = f;

        this.posX = x;
        this.posY = y;
        this.sizeX = sX;
        this.sizeY = sY;
        this.text = text;
        this.options = o;

        //if (text.toLowerCase().contains("back") || text.toLowerCase().contains("quit"))
        //	this.sound = "cancel.ogg";
    }

    public Selector(double x, double y, double sX, double sY, String text, String[] o, Runnable f, String hoverText)
    {
        this(x, y, sX, sY, text, o, f);
        this.enableHover = true;
        this.hoverText = hoverText.split("---");
        this.hoverTextRaw = hoverText;
    }

    public Selector(double x, double y, double sX, double sY, String text, String[] o)
    {
        this.posX = x;
        this.posY = y;
        this.sizeX = sX;
        this.sizeY = sY;
        this.text = text;
        this.options = o;

        this.enabled = false;
    }

    public Selector(double x, double y, double sX, double sY, String text, String[] o, String hoverText)
    {
        this(x, y, sX, sY, text, o);

        this.enableHover = true;
        this.hoverText = hoverText.split("---");
        this.hoverTextRaw = hoverText;
    }

    public void draw()
    {
        Drawing drawing = Drawing.drawing;

        drawing.setInterfaceFontSize(24);

        drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
        drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - 30, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - 30, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - 30, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - 15, sizeX, 30);

        if (selected && !Game.game.window.touchscreen)
            drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
        else
            drawing.setColor(this.colorR, this.colorG, this.colorB);

        double m = 0.8;
        drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY * m);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

        drawing.setColor(0, 0, 0);

        drawing.drawInterfaceText(posX, posY - 30, text);

        if (format)
            Drawing.drawing.drawInterfaceText(posX, posY, Game.formatString(options[selectedOption]));
        else
            Drawing.drawing.drawInterfaceText(posX, posY, options[selectedOption]);

        if (enableHover)
        {
            if (infoSelected && !Game.game.window.touchscreen)
            {
                drawing.setColor(0, 0, 255);
                drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
                drawing.setColor(255, 255, 255);
                drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
                drawing.drawTooltip(this.hoverText);
            }
            else
            {
                drawing.setColor(0, 150, 255);
                drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
                drawing.setColor(255, 255, 255);
                drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
            }
        }
    }

    public void update()
    {
        this.justPressed = false;

        if (!Game.game.window.touchscreen)
        {
            double mx = Drawing.drawing.getInterfaceMouseX();
            double my = Drawing.drawing.getInterfaceMouseY();

            boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

            if (handled)
                Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
        }
        else
        {
            for (int i: Game.game.window.touchPoints.keySet())
            {
                InputPoint p = Game.game.window.touchPoints.get(i);

                if (p.tag.equals(""))
                {
                    double mx = Drawing.drawing.getInterfacePointerX(p.x);
                    double my = Drawing.drawing.getInterfacePointerY(p.y);

                    boolean handled = checkMouse(mx, my, p.valid);

                    if (handled)
                        p.tag = "button";
                }
            }
        }
    }

    public boolean checkMouse(double mx, double my, boolean valid)
    {
        boolean handled = false;

        if (Game.game.window.touchscreen)
        {
            sizeX += 20;
            sizeY += 20;
        }

        selected = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - 30 && my < posY + sizeY / 2;
        infoSelected = (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);

        if (selected && valid)
        {
            if (infoSelected && this.enableHover && Game.game.window.touchscreen)
            {
                handled = true;
                Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
                //Drawing.drawing.playSound(this.sound, 1f, 1f);
                Drawing.drawing.playVibration("click");
                Game.screen = new ScreenInfo(Game.screen, this.text, this.hoverText);
            }
            else if (enabled)
            {
                handled = true;
                this.setScreen();
                this.justPressed = true;

                if (!this.silent)
                {
                    Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
                    //Drawing.drawing.playSound(this.sound, 1f, 1f);
                    Drawing.drawing.playVibration("click");
                }
            }
        }

        if (Game.game.window.touchscreen)
        {
            sizeX -= 20;
            sizeY -= 20;
        }

        return handled;
    }

    public void setScreen()
    {
        Game.screen = new ScreenSelector(this, Game.screen);
    }

    @Override
    public void setPosition(double x, double y)
    {
        this.posX = x;
        this.posY = y;
    }
}

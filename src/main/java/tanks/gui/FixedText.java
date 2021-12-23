package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;

public class FixedText implements IFixedMenu
{
    public String location;
    public double posX;
    public double posY;
    public String text;
    public int duration;
    public boolean afterGameStarted;
    public boolean hasItems = false;

    public double fontSize;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA = 255;

    private long defineTime = 0;

    public FixedText(double x, double y, String text) {
        this(x, y, text, false, 0, 24, 0, 0, 0);
    }

    public FixedText(String location, String text) {
        this(location, text, false, 0, 0, 0, 0);
    }

    public FixedText(String location, String text, boolean afterGameStarted, int duration, double r, double g, double b)
    {
        this.location = location;
        this.text = text;
        this.afterGameStarted = afterGameStarted;
        this.duration = duration;

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        if (!afterGameStarted)
            defineTime = System.currentTimeMillis();
    }

    public FixedText(double x, double y, String text, boolean afterGameStarted, int duration, double fontSize, double r, double g, double b)
    {
        this.posX = x;
        this.posY = y;
        this.text = text;
        this.duration = duration;
        this.afterGameStarted = afterGameStarted;

        this.fontSize = fontSize;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        if (!afterGameStarted)
            defineTime = System.currentTimeMillis();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);
        Drawing.drawing.setFontSize(this.fontSize);
        Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.text);

        if (defineTime > 0 && System.currentTimeMillis() - defineTime > duration)
        {
            if (this.colorA <= 0)
                ModAPI.removeMenus.add(this);

            this.colorA -= Panel.frameFrequency * 1.25;
        }
    }

    @Override
    public void update()
    {
        if (afterGameStarted)
        {
            if (Game.screen instanceof ScreenGame && !((ScreenGame) Game.screen).playing)
                return;
            else if (defineTime == 0)
                defineTime = System.currentTimeMillis();
        }

        if (this.location != null)
        {
            switch (this.location)
            {
                case "title":
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 - 50;
                    this.fontSize = 60;
                    break;
                case "subtitle":
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 + 10;
                    this.fontSize = 40;
                    break;
                case "actionbar":
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight - (this.hasItems ? 200 - Game.player.hotbar.percentHidden : 100);
                    this.fontSize = 20;
                    break;
            }
        }
    }
}

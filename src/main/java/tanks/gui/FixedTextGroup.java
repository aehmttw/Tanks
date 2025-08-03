package tanks.gui;

import tanks.*;
import tanks.gui.screen.ScreenGame;

import java.util.ArrayList;

public class FixedTextGroup implements IFixedMenu
{
    public double posX;
    public double posY;
    public String location;
    public String[] texts;
    public Integer[] durations;
    public boolean afterGameStarted;
    public double fontSize;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA = 255;
    protected long defineTime = 0;
    protected int textNo = 0;
    protected int actionBarLocation = 100;

    public FixedTextGroup(double x, double y, String[] texts, boolean afterGameStarted, Integer[] durationsInMs, double fontSize, double r, double g, double b)
    {
        this.posX = x;
        this.posY = y;
        this.texts = texts;
        this.afterGameStarted = afterGameStarted;
        this.durations = durationsInMs;

        this.fontSize = fontSize;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;

        if (!afterGameStarted)
            defineTime = System.currentTimeMillis();

        if (Game.currentLevel.startingItems.size() > 0 || Game.currentLevel.shop.size() > 0)
            actionBarLocation = 200;
    }

    public FixedTextGroup(String location, String[] texts, boolean afterGameStarted, Integer[] durationsInMs, double fontSize, double r, double g, double b)
    {
        this.location = location;
        this.texts = texts;
        this.afterGameStarted = afterGameStarted;
        this.durations = durationsInMs;

        this.fontSize = fontSize;

        if (r < 0 || g < 0 || b < 0)
        {
            int brightness = Level.isDark() ? 255 : 0;
            this.colorR = brightness;
            this.colorG = brightness;
            this.colorB = brightness;
        }

        if (!afterGameStarted)
            defineTime = System.currentTimeMillis();

        if (Game.currentLevel.startingItems.size() > 0 || Game.currentLevel.shop.size() > 0)
            actionBarLocation = 200;
    }

    public FixedTextGroup(double x, double y, ArrayList<String> texts, boolean afterGameStarted, ArrayList<Integer> durationsInMs, double fontSize, double r, double g, double b)
    {
        this(x, y, texts.toArray(new String[0]), afterGameStarted, durationsInMs.toArray(new Integer[0]), fontSize, r, g, b);
    }

    public FixedTextGroup(String location, ArrayList<String> texts, boolean afterGameStarted, ArrayList<Integer> durationsInMs, double fontSize, double r, double g, double b)
    {
        this(location, texts.toArray(new String[0]), afterGameStarted, durationsInMs.toArray(new Integer[0]), fontSize, r, g, b);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        Drawing.drawing.setFontSize(this.fontSize);
        Drawing.drawing.drawInterfaceText(this.posX, this.posY, this.texts[textNo]);
    }

    @Override
    public void update()
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

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
                    this.posY = Panel.windowHeight - actionBarLocation + Game.player.hotbar.percentHidden;
                    this.fontSize = 20;
                    break;
            }
        }

        if (System.currentTimeMillis() - defineTime > this.durations[textNo])
        {
            if (textNo + 1 >= this.texts.length)
            {
                this.colorA -= Panel.frameFrequency * 1.25;

                if (this.colorA <= 0)
                    ModAPI.removeMenus.add(this);
            }
            else
            {
                textNo++;
                defineTime = System.currentTimeMillis();
            }
        }

    }
}

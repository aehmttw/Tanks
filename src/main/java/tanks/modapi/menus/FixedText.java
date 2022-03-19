package tanks.modapi.menus;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.ModAPI;

public class FixedText extends FixedMenu
{
    public enum types {title, subtitle, actionbar}

    public types location;
    public double posX;
    public double posY;
    public String text;
    public boolean afterGameStarted;
    public boolean hasItems = false;

    public double fontSize;
    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA = 255;

    private long defineTime = 0;

    public FixedText(double x, double y, String text) {
        this(x, y, text, false, 0, 24, 255, 255, 255);
    }

    public FixedText(types location, String text) {
        this(location, text, false, 0, 255, 255, 255);
    }

    public FixedText(types location, String text, boolean afterGameStarted, int duration, double r, double g, double b)
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
        ModAPI.fixedText.drawString(this.posX - ModAPI.fixedText.getStringSizeX(this.fontSize / 40, this.text) / 2,
                this.posY - ModAPI.fixedText.getStringSizeY(this.fontSize / 40, this.text) / 2,
                this.fontSize / 40, this.fontSize / 40,
                this.text);

        if (duration > 0 && System.currentTimeMillis() - defineTime > duration)
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
                case title:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 - 50;
                    this.fontSize = 60;
                    break;
                case subtitle:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight / 2 + 10;
                    this.fontSize = 40;
                    break;
                case actionbar:
                    this.posX = Panel.windowWidth / 2;
                    this.posY = Panel.windowHeight - (this.hasItems ? 190 - Game.player.hotbar.percentHidden * 0.9 : 100);
                    this.fontSize = 20;
                    break;
            }
        }
    }
}

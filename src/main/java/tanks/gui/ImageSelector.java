package tanks.gui;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenSelector;

public class ImageSelector extends Selector
{
    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, Runnable f)
    {
        super(x, y, sX, sY, text, o, f);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, Runnable f, String hoverText)
    {
        super(x, y, sX, sY, text, o, f, hoverText);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o)
    {
        super(x, y, sX, sY, text, o);
        this.format = false;
    }

    public ImageSelector(double x, double y, double sX, double sY, String text, String[] o, String hoverText)
    {
        super(x, y, sX, sY, text, o, hoverText);
        this.format = false;
    }

    @Override
    public void draw()
    {
        //super.draw();

        Drawing drawing = Drawing.drawing;

        drawing.setInterfaceFontSize(this.sizeY * 0.6);

        if (Game.superGraphics)
            TextBox.drawTallGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);

        drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
        drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - sizeY * 3 / 4, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - 15, sizeX, sizeY * 3 / 4);

        double m = 0.8;

        if (Game.superGraphics)
        {
            if (selected && !Game.game.window.touchscreen)
                TextBox.drawTallGlow(this.posX - sizeX / 2 + sizeY * 7 / 8, this.posY + 5, this.sizeY * (3.0 / 4 + m), this.sizeY * m, sizeY * 3 / 4, 0.65, 0, 0, 0, 80, false);
                //Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
            else
                TextBox.drawTallGlow(this.posX - sizeX / 2 + sizeY * 7 / 8, this.posY + 5, this.sizeY * (3.0 / 4 + m), this.sizeY * m, sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
                //Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

            if (this.lastFrame == Panel.panel.ageFrames - 1)
            {
                for (Effect e : this.glowEffects)
                {
                    e.drawGlow();
                    e.draw();
                }
            }
        }

        if (selected && !Game.game.window.touchscreen)
            drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
        else
            drawing.setColor(this.colorR, this.colorG, this.colorB);

        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY * 5 / 4, posY, sizeY * m, sizeY * m);

        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - sizeY * 3 / 4, sizeY * m, sizeY * m);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY * 5 / 4, posY - sizeY * 3 / 4, sizeY * m, sizeY * m);

        drawing.fillInterfaceRect(posX - sizeX / 2 + sizeY * 7 / 8, posY - sizeY * 3 / 8, sizeY * (3.0 / 4 + m), sizeY * (3.0 / 4));
        drawing.fillInterfaceRect(posX - sizeX / 2 + sizeY * 7 / 8, posY - sizeY * 3 / 8, sizeY * (3.0 / 4), sizeY * (3.0 / 4 + m));

        drawing.setColor(0, 0, 0);

        drawing.drawInterfaceText(posX, posY - sizeY * 3 / 8, text);

        if (enableHover)
        {
            if (Game.superGraphics)
            {
                if (infoSelected && !Game.game.window.touchscreen)
                {
                    Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
                    Drawing.drawing.setColor(0, 0, 255);
                    Drawing.drawing.fillInterfaceGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
                }
                else
                    Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
            }

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

        if (images != null)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceImage(images[selectedOption], this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, sizeY * (3.0 / 4 + m), sizeY * (3.0 / 4 + m));
        }

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceImage(options[selectedOption], posX - sizeX / 2 + sizeY * 7 / 8, posY - sizeY * 3 / 8, sizeY * (3.0 / 4 + m), sizeY * (3.0 / 4 + m));
    }

    @Override
    public void addEffect()
    {
        Button.addEffect(posX - sizeX / 2 + sizeY * 7 / 8, posY - sizeY * 3 / 8, this.sizeY * 1.5, this.sizeY * 1.5, this.glowEffects);
    }

    @Override
    public void setScreen()
    {
        ScreenSelector s = new ScreenSelector(this, Game.screen);
        s.drawImages = true;

        s.buttonList.objHeight *= 2;
        s.buttonList.objWidth = s.buttonList.objHeight;
        s.buttonList.objXSpace = s.buttonList.objWidth + 30;
        s.buttonList.objYSpace = s.buttonList.objHeight + 30;
        s.buttonList.hideText = true;
        s.buttonList.rows = 3;
        s.buttonList.columns = 10;
        s.buttonList.sortButtons();
        s.drawBehindScreen = this.drawBehindScreen;
        Game.screen = s;
    }
}

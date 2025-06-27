package tanks.gui;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.bullet.BulletEffect;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInfo;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.tank.Tank;

import java.util.ArrayList;

public class SelectorDrawable extends Button
{
    public double colorR = 255;
    public double colorG = 255;
    public double colorB = 255;
    public double bgColorR = 200;
    public double bgColorG = 200;
    public double bgColorB = 200;
    public double hoverColorR = 240;
    public double hoverColorG = 240;
    public double hoverColorB = 255;
    public String optionText = "";
    public Tank tank;
    public BulletEffect bulletEffect;
    public Object value;
    public ArrayList<Tank> multiTanks = new ArrayList<>();
    public ArrayList<Effect> effects = new ArrayList<>();
    public ArrayList<Effect> removeEffects = new ArrayList<>();

    public SelectorDrawable(double x, double y, double sX, double sY, String text, Runnable f)
    {
        super(x, y, sX, sY, text, f);
    }

    public SelectorDrawable(double x, double y, double sX, double sY, String text, Runnable f, String hoverText, Object... hoverTextOptions)
    {
        super(x, y, sX, sY, text, f, hoverText, hoverTextOptions);
    }

    public SelectorDrawable(double x, double y, double sX, double sY, String text)
    {
        super(x, y, sX, sY, text);
    }

    public SelectorDrawable(double x, double y, double sX, double sY, String text, String hoverText, Object... hoverTextOptions)
    {
        super(x, y, sX, sY, text, hoverText, hoverTextOptions);
    }

    public void draw()
    {
        Drawing drawing = Drawing.drawing;

        drawing.setInterfaceFontSize(this.sizeY * 0.6);

        if (Game.glowEnabled)
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

        if (Game.glowEnabled)
        {
            if (selected && !Game.game.window.touchscreen)
                Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
            else
                Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

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

        drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY * m);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

        drawing.setColor(0, 0, 0);

        drawing.drawInterfaceText(posX, posY - sizeY * 13 / 16, translatedText);

        double size = this.sizeY * 0.6;
        if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, optionText) / Drawing.drawing.interfaceScale > this.sizeX - 80)
            Drawing.drawing.setInterfaceFontSize(size * (this.sizeX - 80) / (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, optionText) / Drawing.drawing.interfaceScale));

        Drawing.drawing.drawInterfaceText(posX, posY, this.optionText);


        if (enableHover)
        {
            if (Game.glowEnabled)
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
                drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
                drawing.drawTooltip(this.hoverText);
            }
            else
            {
                drawing.setColor(0, 150, 255);
                drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
                drawing.setColor(255, 255, 255);
                drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
            }
        }

        if (this.image != null)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceImage(this.image, this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, this.sizeY, this.sizeY);
        }

        if (this.multiTanks.size() > 1)
        {
            double start = -this.sizeX / 2 + this.sizeY / 2 + 30;
            double end = this.sizeX / 2 - this.sizeY / 2 - 30;

            for (int i = 0; i < this.multiTanks.size(); i++)
            {
                double base = Math.min(1, 1.0 / 6 * this.multiTanks.size());
                double frac = base * i / (this.multiTanks.size() - 1);
                this.multiTanks.get(i).drawForInterface(this.posX + start * (base - frac) + end * frac, this.posY, 0.5);
            }
        }
        else if (this.tank != null)
        {
            this.tank.drawForInterface(this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, 0.5);
        }
        else if (this.bulletEffect != null)
        {
            if (!Game.game.window.drawingShadow)
            {
                for (Effect e : this.effects)
                {
                    e.update();

                    if (e.age > e.maxAge)
                        removeEffects.add(e);
                }

                for (Effect f : this.effects)
                {
                    f.draw();
                }

                for (Effect f : this.effects)
                {
                    f.drawGlow();
                }
            }

            this.bulletEffect.drawForInterface(this.posX, this.sizeX * 0.8, this.posY, Bullet.bullet_size, this.effects);
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

        selected = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;
        infoSelected = (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);

        if (selected && valid)
        {
            if (infoSelected && this.enableHover && Game.game.window.touchscreen && !fullInfo)
            {
                handled = true;
                Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
                Drawing.drawing.playVibration("click");

                if (Game.screen instanceof ScreenGame && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
                    ((ScreenGame) Game.screen).overlay = new ScreenInfo(null, this.translatedText, this.hoverText);
                else
                    Game.screen = new ScreenInfo(Game.screen, this.translatedText, this.hoverText);
            }
            else if (enabled)
            {
                handled = true;

                function.run();

                if (!this.silent)
                {
                    Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
                    Drawing.drawing.playVibration("click");
                }

                this.justPressed = true;
            }
        }

        if (Game.game.window.touchscreen)
        {
            sizeX -= 20;
            sizeY -= 20;
        }

        return handled;
    }
}

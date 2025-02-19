package tanks.gui;

import basewindow.IModel;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.gui.screen.ScreenInfo;
import tanks.gui.screen.ScreenSelector;
import tanks.translation.Translation;

import java.util.ArrayList;

public class Selector implements IDrawable, ITrigger
{
    public Runnable function;
    public double posX;
    public double posY;
    public double sizeX;
    public double sizeY;

    public String rawText;
    public String text;
    public String translatedText;

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
    public boolean manualDarkMode = false;

    public long lastFrame;
    public double effectTimer;
    public ArrayList<Effect> glowEffects = new ArrayList<>();

    public String[] sounds;
    public String[] images;
    public IModel[] models;

    public boolean quick = false;

    public boolean silent = false;

    public boolean format = true;
    public boolean translate = true;
    public boolean music = false;

    public boolean drawBehindScreen = false;

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
        this.setText(text);
        this.options = o;
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
        this.setText(text);
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

        this.drawSelection();

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

        if (images != null)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceImage(images[selectedOption], this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, this.sizeY, this.sizeY);
        }

        if (models != null)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceModel2D(models[selectedOption], this.posX - this.sizeX / 2 + this.sizeY / 2 + 10, this.posY, 0, this.sizeY, this.sizeY, this.sizeY);
        }
    }

    public void drawSelection()
    {
        String s = options[selectedOption];

        if (music)
            s = s.substring(s.indexOf("tank/") + "tank/".length(), s.indexOf(".ogg"));

        if (format)
            s = Game.formatString(s);


        if (translate)
            Drawing.drawing.drawInterfaceText(posX, posY, Translation.translate(s));
        else
            Drawing.drawing.drawInterfaceText(posX, posY, s);
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

        if (Game.glowEnabled)
        {
            if (this.lastFrame < Panel.panel.ageFrames - 1)
                this.glowEffects.clear();

            this.lastFrame = Panel.panel.ageFrames;

            for (int i = 0; i < this.glowEffects.size(); i++)
            {
                Effect e = this.glowEffects.get(i);
                e.update();

                if (e.age > e.maxAge)
                {
                    this.glowEffects.remove(i);
                    i--;
                }
            }

            if (this.selected && this.enabled && !Game.game.window.touchscreen)
            {
                this.effectTimer += 0.25 * (this.sizeX + this.sizeY) / 400 * Math.random() * Game.effectMultiplier;

                while (this.effectTimer >= 0.4 / Panel.frameFrequency)
                {
                    this.effectTimer -= 0.4 / Panel.frameFrequency;
                    this.addEffect();
                }
            }
        }
    }

    public void addEffect()
    {
        Button.addEffect(this.posX, this.posY, this.sizeX - this.sizeY * (1 - 0.8), this.sizeY * 0.8, this.glowEffects);
    }

    public void submitEffect()
    {
        for (int i = 0; i < 0.2 * (this.sizeX + this.sizeY) * Game.effectMultiplier; i++)
            Button.addEffect(this.posX, this.posY, this.sizeX - this.sizeY * (1 - 0.8), this.sizeY * 0.8, this.glowEffects, Math.random() * 4, 0.8, 0.25);
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
            if (infoSelected && this.enableHover && Game.game.window.touchscreen)
            {
                handled = true;
                Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
                Drawing.drawing.playVibration("click");
                Game.screen = new ScreenInfo(Game.screen, this.translatedText, this.hoverText);
            }
            else if (enabled)
            {
                handled = true;
                this.setScreen();
                this.justPressed = true;

                if (!this.silent)
                {
                    Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
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
        this.resetLayout();
        ScreenSelector s = new ScreenSelector(this, Game.screen);
        s.images = this.images;
        s.models = this.models;
        s.buttonList.manualDarkMode = this.manualDarkMode;

        if (this.images != null)
            s.drawImages = true;

        if (this.models != null)
            s.drawModels = true;

        s.drawBehindScreen = this.drawBehindScreen;
        Game.screen = s;
    }

    public void resetLayout()
    {
        Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;
        Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
        Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;
    }

    @Override
    public void setPosition(double x, double y)
    {
        this.posX = x;
        this.posY = y;
    }

    public void setText(String text)
    {
        this.rawText = text;
        this.text = text;
        this.translatedText = Translation.translate(text);
    }

    public void setText(String text, String text2)
    {
        this.rawText = text + text2;
        this.text = text + text2;
        this.translatedText = Translation.translate(text) + Translation.translate(text2);
    }

    public void setText(String text, Object... objects)
    {
        this.rawText = text;
        this.text = String.format(text, objects);
        this.translatedText = Translation.translate(text, objects);
    }

    public void setTextArgs(Object... objects)
    {
        this.text = String.format(this.rawText, objects);
        this.translatedText = Translation.translate(this.rawText, objects);
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenOptionsFramerate extends Screen
{
    public ScreenOptionsFramerate()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        maxFPS.allowLetters = false;
        maxFPS.allowSpaces = false;
        maxFPS.maxChars = 3;
        maxFPS.maxValue = 400;
        maxFPS.checkMaxValue = true;
        maxFPS.minValue = 10;
        maxFPS.checkMinValue = true;
        maxFPS.integer = true;

        maxFPS.r1 = 210;
        maxFPS.g1 = 210;
        maxFPS.b1 = 210;
    }

    Button vsync = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "V-Sync", new Runnable()
    {
        @Override
        public void run()
        {
            Game.vsync = true;
            Game.maxFPS = 0;
            Game.game.window.setVsync(Game.vsync);
        }
    },
            "Limits framerate to your screen's refresh rate------May fix issues with screen tearing");


    TextBoxSlider maxFPS = new TextBoxSlider(this.centerX, this.centerY - this.objYSpace * 0, this.objWidth, this.objHeight, "Framerate limit", new Runnable()
    {
        @Override
        public void run()
        {
            Game.vsync = false;
            Game.game.window.setVsync(Game.vsync);

            if (maxFPS.inputText.length() <= 0)
                maxFPS.inputText = maxFPS.previousInputText;

            Game.maxFPS = Integer.parseInt(maxFPS.inputText);
        }
    }
            , Game.maxFPS, 10, 240, 10);


    Button unlimited = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Unlimited", new Runnable()
    {
        @Override
        public void run()
        {
            Game.vsync = false;
            Game.maxFPS = 0;
            Game.game.window.setVsync(Game.vsync);
        }
    },
            "Disables the framerate limit------May cause issues with inconsistent game speed");

    Button manual = new Button(this.centerX, this.centerY - this.objYSpace * 0.25, this.objWidth, this.objHeight, "Manual limit", new Runnable()
    {
        @Override
        public void run()
        {
            Game.vsync = false;
            Game.maxFPS = 60;
            maxFPS.inputText = Game.maxFPS + "";
            maxFPS.value = Game.maxFPS;
            Game.game.window.setVsync(Game.vsync);
        }
    },
            "Set a manual framerate limit");


    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsGraphics());

    @Override
    public void update()
    {
        vsync.update();

        if (Game.framework != Game.Framework.libgdx)
            unlimited.update();

        if (Game.vsync)
        {
            vsync.enabled = false;
            unlimited.enabled = true;
            manual.update();
        }
        else if (Game.maxFPS == 0)
        {
            unlimited.enabled = false;
            vsync.enabled = true;
            manual.update();
        }
        else
        {
            unlimited.enabled = true;
            vsync.enabled = true;
            maxFPS.update();
        }

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (Game.framework != Game.Framework.libgdx)
            unlimited.draw();
        else
        {
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(unlimited.posX, unlimited.posY - this.objYSpace / 4, "Framerate is capped to your");
            Drawing.drawing.drawInterfaceText(unlimited.posX, unlimited.posY + this.objYSpace / 4, "display's refresh rate on mobile");

        }

        if (Game.maxFPS > 0 && !Game.vsync)
            maxFPS.draw();
        else
            manual.draw();

        vsync.draw();

        back.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);

        if (Game.vsync)
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Maximum framerate: \u00A7200100000255V-Sync");
        else if (Game.maxFPS > 0)
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Maximum framerate: %s", (Object)("\u00A7000200000255" + Game.maxFPS));
        else
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Maximum framerate: \u00A7000100200255unlimited");

    }
}

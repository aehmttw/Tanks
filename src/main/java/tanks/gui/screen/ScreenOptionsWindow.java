package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsWindow extends Screen
{
    public static final String infoBarText = "Info bar: ";
    public static final String warnText = "Warn before exit: ";
    public static final String constrainMouseText = "Constrain mouse: ";

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 210, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions());

    Button fullscreen = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.75, this.objWidth, this.objHeight, "", () -> Game.game.window.setFullscreen(!Game.game.window.fullscreen), "Can also be toggled at any time---by pressing " + Game.game.input.fullscreen.getInputs());

    Button showStats = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Drawing.drawing.showStats(!Drawing.drawing.enableStats);

            if (Drawing.drawing.enableStats)
                showStats.setText(infoBarText, ScreenOptions.onText);
            else
                showStats.setText(infoBarText, ScreenOptions.offText);
        }
    },
            "Shows the following information---" +
                    "at the bottom of the screen:---" +
                    "---" +
                    "Game version---" +
                    "Framerate---" +
                    "Network latency (if in a party)---" +
                    "Memory usage");

    Button confirmClose = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.warnBeforeClosing = !Game.warnBeforeClosing;

            if (Game.warnBeforeClosing)
                confirmClose.setText(warnText, ScreenOptions.onText);
            else
                confirmClose.setText(warnText, ScreenOptions.offText);
        }
    },
            "Warn before closing the game---while in an editor");

    Button constrainMouse = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
       {
           @Override
           public void run()
           {
               Game.constrainMouse = !Game.constrainMouse;

               if (Game.constrainMouse)
                   constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
               else
                   constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
           }
       },
               "Disallows your mouse pointer from---leaving the window while playing");

    public static final String fullscreenText = "Fullscreen: ";

    public TextBox width;
    public TextBox height;

    public ScreenOptionsWindow()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        width = new TextBox(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 4, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 0.25, this.objWidth * 0.45, this.objHeight, "Width", () ->
        {
            if (width.inputText.length() <= 2)
                width.inputText = (int) Game.game.window.absoluteWidth + "";
            else
                Game.game.window.setResolution(Integer.parseInt(width.inputText), (int) Game.game.window.absoluteHeight);
        }
                , "");

        width.allowLetters = false;
        width.allowSpaces = false;
        width.minValue = 200;
        width.checkMinValue = true;
        width.maxChars = 4;

        height = new TextBox(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace * 3 / 4, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 0.25, this.objWidth * 0.45, this.objHeight, "Height", () ->
        {
            if (height.inputText.length() <= 2)
                height.inputText = (int) Game.game.window.absoluteWidth + "";
            else
                Game.game.window.setResolution((int) Game.game.window.absoluteWidth, Integer.parseInt(height.inputText));
        }
                , "");

        height.allowLetters = false;
        height.allowSpaces = false;
        height.minValue = 200;
        height.checkMinValue = true;
        height.maxChars = 4;

        if (Drawing.drawing.enableStats)
            showStats.setText(infoBarText, ScreenOptions.onText);
        else
            showStats.setText(infoBarText, ScreenOptions.offText);

        if (Game.warnBeforeClosing)
            confirmClose.setText(warnText, ScreenOptions.onText);
        else
            confirmClose.setText(warnText, ScreenOptions.offText);

        if (Game.constrainMouse)
            constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
        else
            constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
    }


    @Override
    public void update()
    {
        back.update();
        fullscreen.update();
        width.update();
        height.update();
        showStats.update();
        confirmClose.update();
        constrainMouse.update();

        if (!width.selected)
            width.inputText = (int) Game.game.window.absoluteWidth + "";

        if (!height.selected)
            height.inputText = (int) Game.game.window.absoluteHeight + "";
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 1.35, "Window resolution");
        width.draw();
        height.draw();
        if (Game.framework == Game.Framework.libgdx)
            fullscreen.setText(fullscreenText, ScreenOptions.onText);

        fullscreen.draw();

        showStats.draw();
        confirmClose.draw();
        constrainMouse.draw();

        fullscreen.setText(fullscreenText, (Game.game.window.fullscreen ? ScreenOptions.onText : ScreenOptions.offText));

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Window options");
    }

}

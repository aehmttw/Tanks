package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenOptionsWindow extends Screen
{
    public static ScreenOverlayControls overlay = new ScreenOverlayControls();

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptionsGraphics());

    Button fullscreen = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "", () -> Game.game.window.setFullscreen(!Game.game.window.fullscreen), "Can also be toggled at any time---by pressing " + Game.game.input.fullscreen.getInputs());

    Button maxFPS = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsFramerate();
        }
    },
            "Limiting your framerate may---decrease battery consumption");

    public static final String fullscreenText = "Fullscreen: ";

    public TextBox width;
    public TextBox height;

    public ScreenOptionsWindow()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        width = new TextBox(Drawing.drawing.interfaceSizeX / 2 - this.objXSpace / 4, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 0.5, this.objWidth / 2, this.objHeight, "Width", () ->
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

        height = new TextBox(Drawing.drawing.interfaceSizeX / 2 + this.objXSpace / 4, Drawing.drawing.interfaceSizeY / 2 + this.objYSpace * 0.5, this.objWidth / 2, this.objHeight, "Height", () ->
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

        if (Game.vsync)
            maxFPS.setText("Max FPS: \u00A7200100000255V-Sync");
        else if (Game.maxFPS > 0)
            maxFPS.setText("Max FPS: %s", (Object)("\u00A7000200000255" + Game.maxFPS));
        else
            maxFPS.setText("Max FPS: \u00A7000100200255unlimited");
    }


    @Override
    public void update()
    {
        back.update();
        fullscreen.update();
        width.update();
        height.update();
        maxFPS.update();

        if (!width.selected)
            width.inputText = (int) Game.game.window.absoluteWidth + "";

        if (!height.selected)
            height.inputText = (int) Game.game.window.absoluteHeight + "";
    }

    @Override
    public void draw()
    {
        Drawing.drawing.forceRedrawTerrain();
        this.drawDefaultBackground();
        back.draw();
        maxFPS.draw();

        fullscreen.setText(fullscreenText, (Game.game.window.fullscreen ? ScreenOptions.onText : ScreenOptions.offText));

        if (Game.framework == Game.Framework.libgdx)
            fullscreen.setText(fullscreenText, ScreenOptions.onText);

        fullscreen.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - this.objYSpace * 0.6, "Window resolution");
        width.draw();
        height.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Window options");
    }

}

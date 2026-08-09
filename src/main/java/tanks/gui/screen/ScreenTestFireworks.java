package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenTestFireworks extends Screen implements IDarkScreen
{
    public String debugFireworks = "Manual fireworks: ";

    public DisplayFireworks fireworksDisplay = new DisplayFireworks();

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back",
        () -> Game.screen = new ScreenTestDebug());

    Button fireworksMode = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            DisplayFireworks.debug = !DisplayFireworks.debug;

            if (DisplayFireworks.debug)
                fireworksMode.setText(debugFireworks, ScreenOptions.onText);
            else
                fireworksMode.setText(debugFireworks, ScreenOptions.offText);
        }
    });

    TextBox fireworksCount = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Firework count",
        () ->
        {

            try
            {
                DisplayFireworks.firework_frequency = Double.parseDouble(this.fireworksCount.inputText);
            }
            catch (Exception e)
            {
                this.fireworksCount.inputText = this.fireworksCount.previousInputText;
            }
        }, DisplayFireworks.firework_frequency + "");

    public ScreenTestFireworks()
    {
        if (DisplayFireworks.debug)
            fireworksMode.setText(debugFireworks, ScreenOptions.onText);
        else
            fireworksMode.setText(debugFireworks, ScreenOptions.offText);

        this.music = "win_music.ogg";

        fireworksCount.allowLetters = false;
        fireworksCount.allowSpaces = false;
        fireworksCount.allowDoubles = true;
    }

    @Override
    public void update()
    {
        fireworksMode.update();
        fireworksCount.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
    }

    @Override
    public void drawUI()
    {
        Drawing.drawing.setInterfaceFontSize(this.titleSize * 2);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "Fireworks!!!");

        if (!Game.game.window.mainRenderPasses.drawingShadow)
            fireworksDisplay.draw();

        fireworksMode.draw();
        fireworksCount.draw();
        back.draw();
    }
}

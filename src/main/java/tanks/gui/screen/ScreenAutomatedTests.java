package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenAutomatedTests extends Screen
{
    public Screen previous = Game.screen;

    public Button runTests = new Button(Drawing.drawing.interfaceSizeX * 0.85, Drawing.drawing.interfaceSizeY - this.objHeight * 2.5, this.objWidth, this.objHeight, "Run tests", this::startTests);

    public Button back = new Button(Drawing.drawing.interfaceSizeX * 0.85, Drawing.drawing.interfaceSizeY - this.objHeight * 1.5, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = previous;
        Game.cleanUp();
    });

    public ScreenAutomatedTests()
    {
        this.music = previous.music;
        this.musicID = previous.musicID;
    }

    @Override
    public void update()
    {
        runTests.update();
        back.update();
    }

    @Override
    public void draw()
    {
        drawDefaultBackground();
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX * 0.85, this.centerY, Drawing.drawing.interfaceSizeX * 0.3, Drawing.drawing.interfaceSizeY);
        runTests.draw();
        back.draw();
    }

    public void startTests()
    {

    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.UUIDTextBox;

public class ScreenTestTextbox extends Screen
{
    public ScreenTestTextbox()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        box.allowAll = true;
        box.enableCaps = true;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTestDebug()
    );

    TextBox box = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 700, 40, "Text box", () ->
    {

    }, "");

    UUIDTextBox uuidBox = new UUIDTextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 700, 40, "UUID box", () ->
    {

    }, "");


    @Override
    public void update()
    {
        back.update();
        box.update();
        uuidBox.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Text box test");

        box.draw();
        uuidBox.draw();

        back.draw();
    }
}

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
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";

        box.allowAll = true;
        box.enableCaps = true;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenDebug();
        }
    }
    );

    TextBox box = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 700, 40, "Text box", new Runnable()
    {
        @Override
        public void run()
        {

        }
    }, "");

    UUIDTextBox uuidBox = new UUIDTextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 700, 40, "UUID box", new Runnable()
    {
        @Override
        public void run()
        {

        }
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
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Text box test");

        box.draw();
        uuidBox.draw();

        back.draw();
    }
}

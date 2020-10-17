package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenDebug extends Screen
{
    public ScreenDebug()
    {
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTitle();
        }
    }
    );

    Button keyboardTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Test keyboard", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTestKeyboard();
        }
    }
    );

    Button textboxTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Test text boxes", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTestTextbox();
        }
    }
    );

    @Override
    public void update()
    {
        keyboardTest.update();
        textboxTest.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Debug menu");

        keyboardTest.draw();
        textboxTest.draw();
        back.draw();
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenDebug extends Screen
{
    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTitle();
        }
    }
    );

    Button keyboardTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Test keyboard", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenTestKeyboard();
        }
    }
    );

    Button textboxTest = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Test text boxes", new Runnable()
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

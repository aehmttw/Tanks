package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTestKeyboard extends Screen
{
    public ScreenTestKeyboard()
    {
        this.music = "tomato_feast_1.ogg";
        this.musicID = "menu";
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenDebug();
        }
    }
    );

    @Override
    public void update()
    {
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Key test");
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 100, "Press a key to show its code");

        if (!Game.game.window.validPressedKeys.isEmpty())
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "" + Game.game.window.validPressedKeys.get(0));

        back.draw();
    }
}

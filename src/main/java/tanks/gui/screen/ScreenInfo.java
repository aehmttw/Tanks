package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenInfo extends Screen
{
    public Screen previous;
    public String title;
    public String[] text;

    public ScreenInfo(Screen screen, String title, String[] text)
    {
        this.previous = screen;
        this.music = previous.music;
        this.musicID = previous.musicID;
        this.title = title;
        this.text = text;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    @Override
    public void update()
    {
        Game.game.window.showKeyboard = false;
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 800, 400);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, this.title);

        Drawing.drawing.setColor(255, 255, 255);
        for (int i = 0; i < text.length; i++)
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + (i - (text.length - 1) / 2.0) * 30, this.text[i]);
        }

        back.draw();
    }
}

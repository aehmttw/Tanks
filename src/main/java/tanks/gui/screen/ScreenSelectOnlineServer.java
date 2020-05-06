package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenSelectOnlineServer extends Screen
{
    public ScreenSelectOnlineServer()
    {
        ip.allowDots = true;
        ip.maxChars = 43;
        ip.allowColons = true;
        ip.lowerCase = true;
    }

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlayMultiplayer();
        }
    }
    );

    TextBox ip = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 800, 40, "Online server URL or IP Address", new Runnable()
    {

        @Override
        public void run()
        {
            Game.lastOnlineServer = ip.inputText;
            ScreenOptions.saveOptions(Game.homedir);
        }
    },
            Game.lastOnlineServer, "Leave blank to use the---default online server");

    @Override
    public void update()
    {
        ip.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        ip.draw();
        back.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Select online server");
    }
}

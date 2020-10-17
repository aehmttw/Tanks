package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.network.Client;

import java.util.HashMap;

public class ScreenOverlayOnline extends Screen
{
    public static final int max_button_count = 5;
    public HashMap<Integer, Button> buttons = new HashMap<Integer, Button>();
    public Button disconnect = new Button(0, 0, this.objWidth, this.objHeight, "Disconnect", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenPartyLobby.isClient = false;
            Client.handler.ctx.close();

            Game.cleanUp();

            Panel.onlinePaused = false;
            Game.screen = new ScreenPlayMultiplayer();
        }
    }, "This button will disconnect---you from the online service");

    public String title = "Online server menu";

    public ScreenOverlayOnline()
    {
        this.disconnect.unselectedColB = 225;
        this.disconnect.unselectedColG = 225;

        this.disconnect.selectedColR = 255;
        this.disconnect.selectedColB = 200;
        this.disconnect.selectedColG = 200;
    }

    @Override
    public void update()
    {
        double pos = Drawing.drawing.interfaceSizeY / 2 - 30 * (1 + buttons.keySet().size());

        for (int i = 0; i < max_button_count; i++)
        {
            Button b = buttons.get(i);
            if (b != null)
            {
                b.posX = Drawing.drawing.interfaceSizeX / 2;
                b.posY = pos;
                pos += 60;
                b.update();
            }
        }

        disconnect.posX = Drawing.drawing.interfaceSizeX / 2;
        disconnect.posY = pos;
        disconnect.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(127, 178, 228, 64);
        Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

        double lowest = -1;
        for (int i = 0; i < max_button_count; i++)
        {
            Button b = buttons.get(i);
            if (b != null)
            {
                b.draw();

                if (lowest == -1)
                    lowest = b.posY;
            }
        }

        if (lowest == -1)
            lowest = disconnect.posY;

        disconnect.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, lowest - 60, title);
    }
}

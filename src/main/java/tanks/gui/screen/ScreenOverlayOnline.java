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
    public HashMap<Integer, Button> buttons = new HashMap<>();
    public Button disconnect = new Button(0, 0, this.objWidth, this.objHeight, "Disconnect", () ->
    {
        ScreenPartyLobby.isClient = false;
        Client.handler.ctx.close();

        Game.cleanUp();

        Panel.onlinePaused = false;
        Game.screen = new ScreenPlayMultiplayer();
    }, "This button will disconnect---you from the online service");

    public String title = "Online server menu";

    public ScreenOverlayOnline()
    {
        this.disconnect.bgColB = 225;
        this.disconnect.bgColG = 225;

        this.disconnect.selectedColR = 255;
        this.disconnect.selectedColB = 200;
        this.disconnect.selectedColG = 200;
    }

    @Override
    public void update()
    {
        double pos = this.centerY - this.objYSpace / 2 * (1 + buttons.keySet().size());

        for (int i = 0; i < max_button_count; i++)
        {
            Button b = buttons.get(i);
            if (b != null)
            {
                b.posX = this.centerX;
                b.posY = pos;
                pos += this.objYSpace;
                b.update();
            }
        }

        disconnect.posX = this.centerX;
        disconnect.posY = pos;
        disconnect.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(127, 178, 228, 64);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

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
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, lowest - this.objYSpace, title);
    }
}

package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenConfirmEndParty extends Screen implements IPartyMenuScreen
{
    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    public Button confirm = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "End party", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenPartyHost.isServer = false;
            ScreenPartyHost.server.close();
            ScreenPartyHost.activeScreen = null;
            Game.screen = new ScreenParty();
            ScreenPartyHost.includedPlayers.clear();
            ScreenPartyHost.readyPlayers.clear();
            ScreenPartyHost.activeScreen = null;
            Crusade.currentCrusade = null;

            Game.players.clear();
            Game.players.add(Game.player);

            ScreenPartyHost.disconnectedPlayers.clear();
        }
    }
    );

    public ScreenConfirmEndParty()
    {
        this.music = "tomato_feast_3.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        back.update();
        confirm.update();

        ScreenPartyHost.chatbox.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        confirm.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 160, "Are you sure you want to end the party?");

        if (Crusade.currentCrusade != null)
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 100, "All players will be disconnected,");
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, "and progress in the crusade will be lost!");
        }
        else
        {
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 80, "All players will be disconnected!");
        }

        long time = System.currentTimeMillis();
        for (int i = 0; i < ScreenPartyHost.chat.size(); i++)
        {
            ChatMessage c = ScreenPartyHost.chat.get(i);
            if (time - c.time <= 30000 || ScreenPartyHost.chatbox.selected)
            {
                Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
            }
        }

        ScreenPartyHost.chatbox.draw();
    }
}

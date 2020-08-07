package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;
import tanks.network.Client;

public class ScreenConfirmLeaveParty extends Screen implements IPartyMenuScreen
{
    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPartyLobby();
        }
    }
    );

    public Button confirm = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Leave party", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenPartyLobby.isClient = false;
            Client.handler.ctx.close();
            Game.screen = new ScreenJoinParty();
            ScreenPartyLobby.connections.clear();
        }
    }
    );

    public ScreenConfirmLeaveParty()
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        back.update();
        confirm.update();

        ScreenPartyLobby.chatbox.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        confirm.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 100, "Are you sure you want to leave the party?");

        long time = System.currentTimeMillis();
        for (int i = 0; i < ScreenPartyLobby.chat.size(); i++)
        {
            ChatMessage c = ScreenPartyLobby.chat.get(i);
            if (time - c.time <= 30000 || ScreenPartyLobby.chatbox.selected)
            {
                Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
            }
        }

        ScreenPartyLobby.chatbox.draw();
    }
}

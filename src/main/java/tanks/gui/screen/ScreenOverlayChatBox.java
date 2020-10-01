package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;

import java.util.ArrayList;

public class ScreenOverlayChatBox
{
    public static void update(boolean persistent)
    {
        if (ScreenPartyHost.isServer)
            ScreenPartyHost.chatbox.update(persistent);
        else if (ScreenPartyLobby.isClient && !Game.connectedToOnline && ScreenPartyLobby.chatbox != null)
            ScreenPartyLobby.chatbox.update(persistent);
    }

    public static void draw(boolean persistent)
    {
        if (ScreenPartyLobby.isClient && (Game.connectedToOnline || ScreenPartyLobby.chatbox == null))
            return;

        ChatBox chatbox = null;
        ArrayList<ChatMessage> chat = null;

        if (ScreenPartyLobby.isClient)
        {
            chatbox = ScreenPartyLobby.chatbox;
            chat = ScreenPartyLobby.chat;
        }
        else if (ScreenPartyHost.isServer)
        {
            chatbox = ScreenPartyHost.chatbox;
            chat = ScreenPartyHost.chat;
        }

        if (chatbox != null)
        {
            if (Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen)
                chatbox.defaultTextColor = "\u00A7255255255255";

            chatbox.draw(persistent);
            long time = System.currentTimeMillis();
            for (int i = 0; i < chat.size(); i++)
            {
                ChatMessage c = chat.get(i);
                if (time - c.time <= 30000 || chatbox.selected)
                {
                    if (Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen)
                        Drawing.drawing.setColor(255, 255, 255);
                    else
                        Drawing.drawing.setColor(0, 0, 0);

                    Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
                }
            }

            if (Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen)
                chatbox.defaultTextColor = "\u00A7127127127255";
        }
    }
}

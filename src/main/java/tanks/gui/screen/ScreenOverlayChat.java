package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;

import java.util.ArrayList;

public class ScreenOverlayChat
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
            if ((Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen) || (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3))
                chatbox.defaultTextColor = "\u00A7255255255255";

            chatbox.draw(persistent);
            long time = System.currentTimeMillis();
            for (int i = 0; i < chat.size(); i++)
            {
                ChatMessage c = chat.get(i);
                if (time - c.time <= 30000 || chatbox.selected)
                {
                    double xStart = 20;
                    double xPad = -10;
                    double yPad = 5;

                    double width = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, c.message) / Drawing.drawing.interfaceScale;
                    double height = Game.game.window.fontRenderer.getStringSizeY(Drawing.drawing.fontSize, c.message) / Drawing.drawing.interfaceScale;

                    if ((Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen) || (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3))
                        Drawing.drawing.setColor(0, 0, 0, 127);
                    else
                        Drawing.drawing.setColor(255, 255, 255, 127);

                    if (Game.framework != Game.Framework.swing)
                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.interfaceSizeY - i * 30 - 70, width + xPad, height + yPad);

                    Game.game.window.setBatchMode(true, false, false);

                    for (int j = 0; j < 30; j++)
                    {
                        Drawing.drawing.addInterfaceVertex(xStart - xPad / 2, Drawing.drawing.interfaceSizeY - i * 30 - 70, 0);
                        Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * (height + yPad) / 2,
                                Drawing.drawing.interfaceSizeY - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * (height + yPad) / 2, 0);
                        Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * (height + yPad) / 2,
                                Drawing.drawing.interfaceSizeY - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * (height + yPad) / 2, 0);
                    }

                    for (int j = 0; j < 30; j++)
                    {
                        Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2, Drawing.drawing.interfaceSizeY - i * 30 - 70, 0);
                        Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * (height + yPad) / 2,
                                Drawing.drawing.interfaceSizeY - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * (height + yPad) / 2, 0);
                        Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * (height + yPad) / 2,
                                Drawing.drawing.interfaceSizeY - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * (height + yPad) / 2, 0);
                    }

                    Game.game.window.setBatchMode(false, false, false);

                    if ((Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen) || (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3))
                        Drawing.drawing.setColor(255, 255, 255);
                    else
                        Drawing.drawing.setColor(0, 0, 0);


                    Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
                }
            }

            if ((Panel.win && Game.fancyGraphics && Game.screen instanceof IDarkScreen) || (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3))
                chatbox.defaultTextColor = "\u00A7127127127255";
        }
    }
}

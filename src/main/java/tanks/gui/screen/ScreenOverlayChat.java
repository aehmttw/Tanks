package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.tank.TankModels;

import java.util.ArrayList;

public class ScreenOverlayChat
{
    public static ArrayList<ChatMessage> systemMessages = new ArrayList<>();

    public static void update(boolean persistent)
    {
        if (ScreenPartyHost.isServer)
            ScreenPartyHost.chatbox.update(persistent);
        else if (ScreenPartyLobby.isClient && !Game.connectedToOnline && ScreenPartyLobby.chatbox != null)
            ScreenPartyLobby.chatbox.update(persistent);
    }

    public static ChatBox getChatBox()
    {
        if (ScreenPartyLobby.isClient)
            return ScreenPartyLobby.chatbox;
        else if (ScreenPartyHost.isServer)
            return ScreenPartyHost.chatbox;

        return null;
    }

    public static ArrayList<ChatMessage> getChat()
    {
        if (ScreenPartyLobby.isClient)
            return ScreenPartyLobby.chat;
        else if (ScreenPartyHost.isServer)
            return ScreenPartyHost.chat;
        else
            return systemMessages;
    }

    public static void draw(boolean persistent)
    {
        Game.game.window.shapeRenderer.setBatchMode(false, false, false);

        ChatBox chatbox = getChatBox();
        ArrayList<ChatMessage> chat = getChat();

        if (chatbox != null)
        {
            if (isDark())
                chatbox.defaultTextColor = "\u00A7255255255255";

            chatbox.draw(persistent);

            if (isDark())
                chatbox.defaultTextColor = "\u00A7127127127255";
        }

        if (chat != null)
        {
            int i = 0;
            int startY = chatbox == null ? 40 : 70;
            long time = System.currentTimeMillis();
            synchronized (chat)
            {
                for (int in = 0; in < chat.size(); in++)
                {
                    if (in >= 30)
                        continue;

                    int timeout = chatbox == null ? 10000 : 30000;

                    ChatMessage c = chat.get(in);

                    if (time - c.time <= timeout || (chatbox != null && chatbox.selected))
                    {
                        Drawing.drawing.setInterfaceFontSize(24);

                        double xStart = 20;
                        double xPad = -10;
                        double yPad = 5;

                        double width = 0;

                        for (String s : c.lines)
                            width = Math.max(width, Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale);

                        double height = 22 * c.lines.size() + 8 * (c.lines.size() - 1);
                        double radius = 13.5;

                        if (isDark())
                            Drawing.drawing.setColor(0, 0, 0, 127);
                        else
                            Drawing.drawing.setColor(255, 255, 255, 127);

                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + radius / 2, width + xPad, radius);
                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1)) * 30 - startY - radius / 2, width + xPad, radius);
                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1) / 2.0) * 30 - startY, width + xPad + radius * 2, height + yPad - radius * 2);

                        Game.game.window.shapeRenderer.setBatchMode(true, false, false);

                        for (int j = 0; j < 15; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY, 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 15) / 30.0 * Math.PI) * radius, 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 16) / 30.0 * Math.PI) * radius, 0);
                        }

                        for (int j = 15; j < 30; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 15) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 16) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                        }

                        for (int j = 0; j < 15; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY, 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 15) / 30.0 * Math.PI) * radius, 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 16) / 30.0 * Math.PI) * radius, 0);
                        }

                        for (int j = 15; j < 30; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 15) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY + Math.sin((j + 16) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                        }

                        Game.game.window.shapeRenderer.setBatchMode(false, false, false);

                        double x = 34;
                        double y = Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1)) * 30 - startY;
                        double size = Game.tile_size * 0.4;

                        if (c.enableTankIcon)
                        {
                            Drawing.drawing.setColor(c.r2, c.g2, c.b2);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, size, size, 0);

                            Drawing.drawing.setColor(c.r1, c.g1, c.b1);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, size, size, 0);

                            Drawing.drawing.setColor(c.r2, c.g2, c.b2);

                            Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, size, size, 0);

                            if (c.r3 >= 0)
                                Drawing.drawing.setColor(c.r3, c.g3, c.b3);
                            else
                                Drawing.drawing.setColor((c.r1 + c.r2) / 2, (c.g1 + c.g2) / 2, (c.b1 + c.b2) / 2);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, size, size, 0);
                        }

                        for (int j = c.lines.size() - 1; j >= 0; j--)
                        {
                            double mx = 20;
                            double my = Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - startY;

                            if (isDark())
                                Drawing.drawing.setColor(255, 255, 255);
                            else
                                Drawing.drawing.setColor(0, 0, 0);

                            Drawing.drawing.drawInterfaceText(mx, my, c.lines.get(j), false);
                            i++;
                        }
                    }
                }
            }
        }
    }

    public static boolean isDark()
    {
        return (((Panel.win && Game.effectsEnabled) || Panel.darkness > 0) && Game.screen instanceof IDarkScreen) || (Level.isDark());
    }

    public static void addChat(String chat)
    {
        getChat().add(new ChatMessage(chat));
    }
}

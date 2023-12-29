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

        Game.game.window.shapeRenderer.setBatchMode(false, false, false);

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
            if (isDark())
                chatbox.defaultTextColor = "\u00A7255255255255";

            chatbox.draw(persistent);
            long time = System.currentTimeMillis();

            int i = 0;

            synchronized (chat)
            {
                for (int in = 0; in < chat.size(); in++)
                {
                    if (in >= 30)
                        continue;

                    ChatMessage c = chat.get(in);

                    if (time - c.time <= 30000 || chatbox.selected)
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

                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + radius / 2, width + xPad, radius);
                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1)) * 30 - 70 - radius / 2, width + xPad, radius);
                        Drawing.drawing.fillInterfaceRect(width / 2 + xStart, Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1) / 2.0) * 30 - 70, width + xPad + radius * 2, height + yPad - radius * 2);

                        Game.game.window.shapeRenderer.setBatchMode(true, false, false);

                        for (int j = 0; j < 15; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70, 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * radius, 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * radius, 0);
                        }

                        for (int j = 15; j < 30; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(xStart - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                        }

                        for (int j = 0; j < 15; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70, 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * radius, 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * radius, 0);
                        }

                        for (int j = 15; j < 30; j++)
                        {
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2, Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 15) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                            Drawing.drawing.addInterfaceVertex(width + xStart + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * radius,
                                    Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70 + Math.sin((j + 16) / 30.0 * Math.PI) * radius - 30 * (c.lines.size() - 1), 0);
                        }

                        Game.game.window.shapeRenderer.setBatchMode(false, false, false);

                        double x = 34;
                        double y = Drawing.drawing.getInterfaceEdgeY(true) - (i + (c.lines.size() - 1)) * 30 - 70;
                        double size = Game.tile_size * 0.4;

                        if (c.enableTankIcon)
                        {
                            Drawing.drawing.setColor(c.r2, c.g2, c.b2);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, size, size, 0);

                            Drawing.drawing.setColor(c.r1, c.g1, c.b1);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, size, size, 0);

                            Drawing.drawing.setColor(c.r2, c.g2, c.b2);

                            Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, size, size, 0);

                            Drawing.drawing.setColor((c.r1 + c.r2) / 2, (c.g1 + c.g2) / 2, (c.b1 + c.b2) / 2);
                            Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, size, size, 0);
                        }

                        for (int j = c.lines.size() - 1; j >= 0; j--)
                        {
                            double mx = 20;
                            double my = Drawing.drawing.getInterfaceEdgeY(true) - i * 30 - 70;

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

            if (isDark())
                chatbox.defaultTextColor = "\u00A7127127127255";
        }
    }

    public static boolean isDark()
    {
        return (((Panel.win && Game.effectsEnabled) || Panel.darkness > 0) && Game.screen instanceof IDarkScreen) || (Level.isDark());
    }
}

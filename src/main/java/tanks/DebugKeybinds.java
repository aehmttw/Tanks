package tanks;

import basewindow.BaseWindow;
import basewindow.InputCodes;
import basewindow.ShaderGroup;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.gui.ChatMessage;
import tanks.gui.ScreenElement;
import tanks.gui.screen.ScreenCrusadeDetails;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;
import tanks.rendering.TerrainRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tanks.Panel.notifs;

public class DebugKeybinds
{
    public static void drawAndUpdate()
    {
        if (Game.game.window.drawingShadow || !Game.game.window.pressedKeys.contains(InputCodes.KEY_F3))
            return;

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_B))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_B);
            Game.drawFaces = !Game.drawFaces;
            notifs.add(new ScreenElement.Notification("Collision boxes: \u00a7255200000255"
                    + (Game.drawFaces ? "shown" : "hidden"), 200));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_V))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_V);
            if (Game.currentLevel != null)
                Game.currentLevel.reloadTiles();
            notifs.add(new ScreenElement.Notification(Game.currentLevel != null ? "Reloaded tiles" : "Reload tiles failed: " +
                    "Game.\u00a7200125255255currentLevel\u00a7255255255255 = \u00a7255128128255null",
                    200));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_K))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_K);
            Function<List<Integer>, List<String>> func = l -> l.stream().map(Game.game.window::getKeyText).collect(Collectors.toList());
            System.out.println("Game.screen = " + Game.screen.getClass().getSimpleName());
            System.out.println("pressedKeys: " + func.apply(Game.game.window.pressedKeys));
            System.out.println("validPressedKeys: " + func.apply(Game.game.window.validPressedKeys));
            System.out.println("textPressedKeys: " + func.apply(Game.game.window.textPressedKeys));
            notifs.add(new ScreenElement.Notification("Pressed keys have been logged to the console", 300));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_D))
        {
            ArrayList<ChatMessage> chat = null;
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_D);

            if (ScreenPartyLobby.isClient)
                chat = ScreenPartyLobby.chat;
            else if (ScreenPartyHost.isServer)
                chat = ScreenPartyHost.chat;

            if (chat != null)
            {
                synchronized (chat)
                {
                    chat.clear();
                }
                notifs.add(new ScreenElement.Notification("Chat cleared", 200));
            }
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_A))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_A);

            if (!(Game.screen instanceof ScreenCrusadeDetails))
            {
                Drawing.drawing.terrainRenderer.reset();
                notifs.add(new ScreenElement.Notification("Terrain reloaded!"));
            }
            else
                notifs.add(new ScreenElement.Notification("F3+A doesn't work here!"));

        }

        // How to use: Run in debug mode -> Edit shader -> Rebuild (Ctrl/Cmd + F9) -> F3+T
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_T))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_T);

            HashMap<Class<? extends ShaderGroup>, ShaderGroup> newShaders = new HashMap<>();
            for (Map.Entry<Class<? extends ShaderGroup>, ShaderGroup> entry : Game.game.shaderInstances.entrySet())
            {
                try
                {
                    ShaderGroup s;
                    try
                    {
                        s = entry.getKey().getConstructor(BaseWindow.class)
                                .newInstance(Game.game.window);
                    }
                    catch (NoSuchMethodException e)
                    {
                        s = entry.getKey().getConstructor(BaseWindow.class, String.class)
                                .newInstance(Game.game.window, entry.getValue().name);
                    }

                    s.initialize();
                    newShaders.put(entry.getKey(), s);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }

            Game.game.shaderInstances = newShaders;
            Drawing.drawing.terrainRenderer.reset();
            notifs.add(new ScreenElement.Notification("Shaders reloaded! (Remember to rebuild)"));
        }

        int brightness;
        if (Game.currentLevel != null && Level.isDark())
            brightness = 255;
        else
            brightness = 0;

        Drawing.drawing.setColor(brightness, brightness, brightness);
        Drawing.drawing.setInterfaceFontSize(16);

        double mx = Game.game.window.absoluteMouseX, my = Game.game.window.absoluteMouseY;

        String text;
        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_S))
        {
            if (Game.game.window.shift)
                text = "(" + (int) (mx - Game.screen.getOffsetX()) + ", " + (int) (my - Game.screen.getOffsetY()) + ")  " + Drawing.drawing.interfaceScale + ", " + Drawing.drawing.interfaceScaleZoom;
            else
                text = "(" + Math.round(Drawing.drawing.getMouseX()) + ", " + Math.round(Drawing.drawing.getMouseY()) + ")";
        }
        else
        {
            int posX = (int) (((Math.round(Drawing.drawing.getMouseX() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);
            int posY = (int) (((Math.round(Drawing.drawing.getMouseY() / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2) - 25) / 50);

            if (Game.screen instanceof ScreenLevelEditor) {
                posX = (int) (((ScreenLevelEditor) Game.screen).mousePlaceable.posX / Game.tile_size - 0.5);
                posY = (int) (((ScreenLevelEditor) Game.screen).mousePlaceable.posY / Game.tile_size - 0.5);
            }

            text = "P: (" + posX + ", " + posY + ")";

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_1))
            {
                Chunk.Tile t1 = Chunk.getTile(posX, posY);

                if (t1 != null)
                {
                    if (mx > Drawing.drawing.getInterfaceEdgeX(true) - 200)
                        mx -= 200;
                    if (my > Drawing.drawing.getInterfaceEdgeY(true) - 100)
                        my -= 100;

                    if (Level.isDark() && t1.fullObstacle != null)
                    {
                        Obstacle o = t1.fullObstacle;
                        if ((o.colorR + o.colorG + o.colorB + o.colorA / 2) / 4 > 200)
                            Drawing.drawing.setColor(0, 0, 0, 128);
                    }

                    Game.game.window.fontRenderer.drawString(mx + 10, my + 30, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "O: " + (t1.fullObstacle != null ? t1.fullObstacle.name : "none") + " SO: " + (t1.surfaceObstacle != null ? t1.surfaceObstacle.name : "none")
                                    + " E: " + (t1.extraObstacle != null ? t1.extraObstacle.name : "none"));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 50, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "H: " + (int) t1.height() + " GH+D: " + (int) (t1.groundHeight() + t1.depth) + " E: " + TerrainRenderer.getExtra(posX, posY));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 70, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "S: " + t1.solid() + " U: " + t1.unbreakable());
                }
            }
            else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_2))
            {
                ObjectArrayList<Movable> v = Game.getMovablesInRadius(mx, my, 50);
                if (!v.isEmpty())
                    text = v.get(0).getMetadata();
            }
            else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_3))
            {
                double finalMx = mx, finalMy = my;
                Chunk.runIfTilePresent(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY(), t ->
                {
                    if (t.fullObstacle == null)
                        return;

                    Drawing.drawing.setColor(brightness, brightness, brightness);
                    Drawing.drawing.setInterfaceFontSize(16);
                    Game.game.window.fontRenderer.drawString(finalMx + 10, finalMy + 30, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            "M: " + t.fullObstacle.getMetadata());
                });
            }
        }

        Game.game.window.fontRenderer.drawString(mx + 10, my + 10, Drawing.drawing.fontSize, Drawing.drawing.fontSize, text);
    }
}

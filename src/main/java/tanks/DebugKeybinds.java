package tanks;

import basewindow.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.gui.*;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.*;
import tanks.rendering.TerrainRenderer;
import tanks.tank.*;

import java.util.*;
import java.util.stream.Collectors;

import static tanks.Panel.notifs;

public class DebugKeybinds
{
    public static void handleDebugKeybinds()
    {
        if (!Game.game.window.pressedKeys.contains(InputCodes.KEY_F3))
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
            else
                Chunk.populateChunks(Chunk.defaultLevel);
            notifs.add(new ScreenElement.Notification(Game.currentLevel != null ? "Reloaded tiles with current level" :
                    "Reloaded tiles with default level", 200));
        }

        if (Game.game.window.pressedKeys.contains(InputCodes.KEY_G))
        {
            Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_G);
            Chunk.debug = !Chunk.debug;
            notifs.add(new ScreenElement.Notification("Chunk borders: \u00a7255200000255"
                    + (Chunk.debug ? "shown" : "hidden"), 200));
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
                Chunk c = Chunk.getChunk(posX, posY);
                Chunk.Tile t1 = Chunk.getTile(posX, posY);

                if (c != null)
                    text += " C: (" + c.chunkX + ", " + c.chunkY + ")";

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
                            String.format("O: %s SO: %s E: %s", t1.fullObstacle != null ? t1.fullObstacle.name : "none", t1.surfaceObstacle != null ? t1.surfaceObstacle.name : "none", t1.extraObstacle != null ? t1.extraObstacle.name : "none"));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 50, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            String.format("H: %.0f GH: %.0f E: %.0f, D: %.1f", t1.height(), t1.groundHeight(), TerrainRenderer.getExtra(posX, posY), t1.depth));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 70, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            String.format("C: (%.0f, %.0f, %.0f)", t1.colR, t1.colG, t1.colB));
                    Game.game.window.fontRenderer.drawString(mx + 10, my + 90, Drawing.drawing.fontSize, Drawing.drawing.fontSize,
                            String.format("TS: %b BS: %b U: %b", t1.tankSolid(), t1.bulletSolid(), Game.obstaclesToUpdate.contains(t1.obstacle())));
                    if (c != null && t1.obstacle() != null && !c.obstacles.contains(t1.obstacle()))
                    {
                        Drawing.drawing.setColor(255, 0, 0);
                        Game.game.window.fontRenderer.drawString(mx + 10, my + 90, Drawing.drawing.fontSize, Drawing.drawing.fontSize, "IN: false");
                    }
                }
            }
            else if (Game.game.window.pressedKeys.contains(InputCodes.KEY_2))
            {
                ObjectArrayList<Movable> v = Movable.getMovablesInRadius(mx, my, 50);
                if (!v.isEmpty())
                    text += " M: " + v.get(0).getMetadata();
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

    public static void renderDebugging()
    {
        if (Game.game.window.drawingShadow)
            return;

        handleDebugKeybinds();
        Face.drawDebug();
        Chunk.drawDebugStuff();
        Ray.drawDebug();

        if (Game.drawAvoidObjects)
        {
            for (IAvoidObject o : Game.avoidObjects)
            {
                if (!(o instanceof GameObject)) continue;
                Drawing.drawing.setColor(255, 0, 0, 50);
                Mine.drawRange2D(((GameObject) o).posX, ((GameObject) o).posY, o.getRadius());
            }
        }

        if (Game.showUpdatingObstacles)
        {
            for (Obstacle o : Game.obstaclesToUpdate)
                o.draw3dOutline(255, 255, 0);
        }
    }
}

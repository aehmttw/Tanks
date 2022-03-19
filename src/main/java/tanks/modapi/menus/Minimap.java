package tanks.modapi.menus;

import basewindow.InputCodes;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.modapi.ModAPI;
import tanks.modapi.ModLevel;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleText;
import tanks.tank.Mine;
import tanks.tank.Tank;

import java.util.ArrayList;

public class Minimap extends FixedMenu
{
    public static float defaultZoom = 1.5f;
    public boolean draggable = false;
    public boolean resizable = false;
    public Level level = Game.currentLevel;
    public int posX = (int) (Panel.windowWidth - sizeX);
    public int posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight);
    public static boolean enabled;
    public boolean centered = true;
    public boolean forceDisabled;

    public static float scale = 1.5f;
    public static int posOffsetX = 0;
    public static int posOffsetY = 0;
    public static double panOffsetX = 0;
    public static double panOffsetY = 0;
    public static int sizeX = 200;
    public static int sizeY = 230;
    public static boolean darkMode = true;
    public static boolean colorfulObstacles = true;

    String closeControl = Game.game.input.minimapToggle.input1.getInputName() + " to close";

    public Minimap()
    {
        forceDisabled = (Game.currentGame != null && Game.currentGame.forceDisableMinimap) ||
                (Game.currentLevel instanceof ModLevel && ((ModLevel) Game.currentLevel).forceDisableMinimap);
    }

    @Override
    public void draw()
    {
        if (!enabled)
            return;

        int brightness = darkMode ? 0 : 255;
        Drawing.drawing.setColor(brightness, brightness, brightness, 150);
        ModAPI.fixedShapes.fillRect(posX, posY, sizeX, sizeY);

        brightness = darkMode ? 255 : 0;
        Drawing.drawing.setColor(brightness, brightness, brightness);
        ModAPI.fixedText.drawString(posX + 40, posY + 10, 0.5, 0.5, "Minimap (x" + scale + ")");
        ModAPI.fixedText.drawString(posX + 30, posY + 210, 0.5, 0.5, "Mode: " + (colorfulObstacles ? "Obstacles" : "Tanks"));

        if (forceDisabled)
        {
            Drawing.drawing.setColor(255, 0, 0);
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 90, posY + sizeY / 2.0 - 50, 0.5, 0.5, "Disabled by level");
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 40, posY + sizeY / 2.0 - 30, 0.5, 0.5, "settings");

            Drawing.drawing.setColor(brightness, brightness, brightness);
            ModAPI.fixedText.drawString(posX + sizeX / 2.0 - 50, posY + sizeY / 2.0 + 10, 0.5, 0.5, closeControl);

            return;
        }

        ArrayList<Obstacle>[] layers = new ArrayList[10];
        for (int i = 0; i < 10; i++)
            layers[i] = new ArrayList<>();

        for (Obstacle o : Game.obstacles)
        {
            if (o.startHeight > 0)
                continue;

            double x;
            double y;

            if (centered) {
                x = (posX + 95) + o.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                y = (posY + 110) + o.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
            }
            else {
                x = (posX + 95 - panOffsetX) + o.posX / 13 * scale;
                y = (posY + 110 - panOffsetY) + o.posY / 13 * scale;
            }

            if ((posX < x && x < posX + sizeX) && (posY + 30 < y && y < posY + (sizeY - 30)))
            {
                if (colorfulObstacles)
                {
                    layers[o.drawLevel].add(o);
                }
                else if (o.tankCollision)
                {
                    if (o.destructible)
                        Drawing.drawing.setColor(101, 60, 22);
                    else
                        Drawing.drawing.setColor(100, 100, 100);

                    ModAPI.fixedShapes.fillRect(x, y, 4 * scale, 4 * scale);
                }
            }
        }

        if (colorfulObstacles)
        {
            for (ArrayList<Obstacle> a : layers)
            {
                for (Obstacle o : a)
                {
                    double x;
                    double y;

                    if (centered) {
                        x = (posX + 95) + o.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                        y = (posY + 110) + o.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
                    }
                    else {
                        x = (posX + 95 - panOffsetX) + o.posX / 13 * scale;
                        y = (posY + 110 - panOffsetY) + o.posY / 13 * scale;
                    }

                    double fontSize = 24 / 40.0 / 8 * scale;
                    Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB);
                    if (o instanceof ObstacleText)
                        ModAPI.fixedText.drawString(x - ModAPI.fixedText.getStringSizeX(fontSize, ((ObstacleText) o).text) / 2, y - ModAPI.fixedText.getStringSizeY(fontSize, ((ObstacleText) o).text), fontSize, fontSize, ((ObstacleText) o).text);
                    else
                        ModAPI.fixedShapes.fillRect(x, y, 4 * scale, 4 * scale);
                }
            }
        }

        for (Movable m : Game.movables)
        {
            double x;
            double y;

            if (centered) {
                x = (posX + 95) + m.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                y = (posY + 110) + m.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
            }
            else {
                x = (posX + 95 - panOffsetX) + m.posX / 13 * scale;
                y = (posY + 110 - panOffsetY) + m.posY / 13 * scale;
            }

            if ((posX < x && x < posX + sizeX) && (posY + 30 < y && y < posY + (sizeY - 30)))
                if (m instanceof Tank && !m.destroy && ((Tank) m).needsToKill)
                {
                    if (m.team != null && m.team.enableColor)
                        Drawing.drawing.setColor(m.team.teamColorR, m.team.teamColorG, m.team.teamColorB);
                    else
                        Drawing.drawing.setColor(((Tank) m).colorR, ((Tank) m).colorG, ((Tank) m).colorB);

                    if (m.equals(Game.playerTank))
                    {
                        if (!centered)
                        {
                            if (!Game.playerTank.destroy || ScreenGame.finished || ScreenGame.finishedQuick)
                                Drawing.drawing.setColor(0, 255, 0);
                            else
                                Drawing.drawing.setColor(255, 0, 0);

                            ModAPI.fixedShapes.drawImage(x, y, 12, 10, "/images/vertical_arrow_white.png", Game.playerTank.angle - ModAPI.left, false);
                        }

                    } else
                        ModAPI.fixedShapes.fillOval(x, y, 8 * (((Tank) m).size / 50), 8 * (((Tank) m).size / 50));
                }
                else if (!colorfulObstacles)
                {
                    if (m instanceof Mine) {
                        Drawing.drawing.setColor(((Mine) m).outlineColorR, ((Mine) m).outlineColorG, ((Mine) m).outlineColorB);
                        ModAPI.fixedShapes.fillOval(x, y, 5, 5);
                    }

                    else if (m instanceof Bullet && !m.destroy)
                    {
                        Drawing.drawing.setColor(((Bullet) m).baseColorR, ((Bullet) m).baseColorG, ((Bullet) m).baseColorB);
                        ModAPI.fixedShapes.fillOval(x, y, 5, 5);
                        Drawing.drawing.setColor(((Bullet) m).outlineColorR, ((Bullet) m).outlineColorG, ((Bullet) m).outlineColorB);
                        ModAPI.fixedShapes.drawOval(x, y, 5, 5);
                    }
                }
        }

        Drawing.drawing.setColor(0, 255, 0);
        if (centered && !Game.playerTank.destroy)
            ModAPI.fixedShapes.drawImage(posX + sizeX / 2.0, posY + sizeY / 2.0, 12, 10, "/images/vertical_arrow_white.png", Game.playerTank.angle - ModAPI.left, false);
    }

    @Override
    public void update()
    {
        if (Game.game.input.minimapToggle.isValid()) {
            Game.game.input.minimapToggle.invalidate();
            enabled = !enabled;
        }

        if (!enabled)
            return;

        posX = (int) (Panel.windowWidth - sizeX - posOffsetX);
        posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight - posOffsetY);

        if (posX < 0)
            posOffsetX = (int) (Panel.windowWidth - sizeX);
        else if (posY < 0)
            posOffsetY = (int) (Panel.windowHeight - sizeY);

        if (draggable && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1) && !forceDisabled)
        {
            if ((posX < Game.game.window.absoluteMouseX && Game.game.window.absoluteMouseX < posX + 200) && (posY < Game.game.window.absoluteMouseY && Game.game.window.absoluteMouseY < posY + 200))
            {
                posOffsetX = (int) (Panel.windowWidth - Game.game.window.absoluteMouseX - sizeX / 2);
                posOffsetY = (int) (Panel.windowHeight - Game.game.window.absoluteMouseY - sizeY / 2);

                if (posOffsetX < 0)
                    posOffsetX = 0;
                else if (posOffsetX > Panel.windowWidth - sizeX)
                    posOffsetX = (int) (Panel.windowWidth - sizeX);

                if (posOffsetY < 0)
                    posOffsetY = 0;
                else if (posOffsetY > Panel.windowHeight - sizeY - Drawing.drawing.statsHeight)
                    posOffsetY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight);
            }
        }

        if (Game.game.input.minimapChangeTheme.isValid()) {
            Game.game.input.minimapChangeTheme.invalidate();
            darkMode = !darkMode;
        }

        if (Game.game.input.minimapChangeType.isValid()) {
            Game.game.input.minimapChangeType.invalidate();
            colorfulObstacles = !colorfulObstacles;
        }

        if (Game.game.input.minimapIncreaseScale.isValid() && scale < 3) {
            Game.game.input.minimapIncreaseScale.invalidate();
            scale += 0.25;
        }

        if (Game.game.input.minimapDecreaseScale.isValid() && scale > 0.25) {
            Game.game.input.minimapDecreaseScale.invalidate();
            scale -= 0.25;
        }

        if (Game.game.input.minimapPanUp.isValid()) {
            Game.game.input.minimapPanUp.invalidate();

            if (centered) {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY -= 40 / scale;
        }

        if (Game.game.input.minimapPanDown.isValid()) {
            Game.game.input.minimapPanDown.invalidate();

            if (centered) {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY += 40 / scale;
        }

        if (Game.game.input.minimapPanRight.isValid()) {
            Game.game.input.minimapPanRight.invalidate();

            if (centered) {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX += 40 / scale;
        }

        if (Game.game.input.minimapPanLeft.isValid()) {
            Game.game.input.minimapPanLeft.invalidate();

            if (centered) {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX -= 40 / scale;
        }

        if (Game.game.input.minimapRecenter.isValid()) {
            Game.game.input.minimapRecenter.invalidate();

            panOffsetX = 0;
            panOffsetY = 0;
            centered = true;
        }
    }
}

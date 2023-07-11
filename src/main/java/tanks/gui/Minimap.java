package tanks.gui;

import basewindow.InputCodes;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class Minimap implements IFixedMenu
{
    public boolean draggable = false;
    public boolean resizable = false;
    public Level level = Game.currentLevel;
    public int posX = (int) (Panel.windowWidth - sizeX);
    public int posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight);
    public boolean enabled;
    public boolean centered = true;
    public static boolean darkMode = true;
    public static float scale = 1.5f;
    public static int posOffsetX = 0;
    public static int posOffsetY = 0;
    public static double panOffsetX = 0;
    public static double panOffsetY = 0;
    public static int sizeX = 200;
    public static int sizeY = 200;
    public static boolean colorfulObstacles = false;

    public Minimap()
    {
        enabled = false;//!(this.level instanceof ModLevel && ((ModLevel) this.level).forceDisableMinimap) /*&& Game.autoMinimapEnabled*/;
    }

    @Override
    public void draw()
    {
        if (!enabled)
            return;

        //int brightness = darkMode ? 0 : 255;
        //Drawing.drawing.setColor(brightness, brightness, brightness, 200);
        Drawing.drawing.setColor(Level.currentColorR, Level.currentColorG, Level.currentColorB, 180);
        Drawing.drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

        //brightness = darkMode ? 255 : 0;
        //Drawing.drawing.setColor(brightness, brightness, brightness);

        Drawing.drawing.setColor(174, 92, 16);
        double thickness = 4 * scale;

        Drawing.drawing.fillInterfaceRect(posX - sizeX / 2.0, posY, thickness, sizeY + thickness);
        Drawing.drawing.fillInterfaceRect(posX + sizeX / 2.0, posY, thickness, sizeY + thickness);
        Drawing.drawing.fillInterfaceRect(posX, posY - sizeY / 2.0, sizeX + thickness, thickness);
        Drawing.drawing.fillInterfaceRect(posX, posY + sizeY / 2.0, sizeX + thickness, thickness);

        //ModAPI.fixedText.drawString(posX + 40, posY + 10, 0.5, 0.5, "Minimap (x" + scale + ")");
        //ModAPI.fixedText.drawString(posX + 30, posY + 210, 0.5, 0.5, "Mode: " + (colorfulObstacles ? "Obstacles" : "Tanks"));

        for (Obstacle o : Game.obstacles)
        {
            double x;
            double y;

            if (centered)
            {
                x = (posX) + o.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                y = (posY) + o.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
            }
            else
            {
                x = (posX - panOffsetX) + o.posX / 13 * scale;
                y = (posY - panOffsetY) + o.posY / 13 * scale;
            }

            if ((posX < x - sizeX / 2.0 && x < posX + sizeX / 2.0) && (posY < y - sizeY / 2.0 && y < posY + sizeY / 2.0))
            {
                if (colorfulObstacles)
                {
                    Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB);
                    Drawing.drawing.fillInterfaceRect(x, y, 4 * scale, 4 * scale);
                }
                else if (o.tankCollision)
                {
                    if (o.destructible)
                        Drawing.drawing.setColor(101, 60, 22);
                    else
                        Drawing.drawing.setColor(100, 100, 100);

                    Drawing.drawing.fillInterfaceRect(x, y, 4 * scale, 4 * scale);
                }
            }
        }

        for (Movable m : Game.movables)
        {
            double x;
            double y;

            if (centered)
            {
                x = (posX + 95) + m.posX / 13 * scale - Game.playerTank.posX / 13 * scale;
                y = (posY + 110) + m.posY / 13 * scale - Game.playerTank.posY / 13 * scale;
            }
            else
            {
                x = (posX + 95 - panOffsetX) + m.posX / 13 * scale;
                y = (posY + 110 - panOffsetY) + m.posY / 13 * scale;
            }

            if (m instanceof Tank && !m.destroy && ((Tank) m).mandatoryKill)
            {
                if ((posX < x && x < posX + sizeX) && (posY + 30 < y && y < posY + (sizeY - 30)))
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

                            Drawing.drawing.drawInterfaceImage(Game.playerTank.angle - Math.PI * 3 / 2, "/images/icons/vertical_arrow_white.png", x, y, 12, 10);
                        }

                    }
                    else
                        Drawing.drawing.fillInterfaceOval(x, y, 8 * (((Tank) m).size / 50), 8 * (((Tank) m).size / 50));
                }
            }
            else if (!colorfulObstacles)
            {
                if (m instanceof Mine)
                {
                    Drawing.drawing.setColor(((Mine) m).outlineColorR, ((Mine) m).outlineColorG, ((Mine) m).outlineColorB);
                    Drawing.drawing.fillInterfaceOval(x, y, 5, 5);
                }

                else if (m instanceof Bullet && !m.destroy)
                {
                    Drawing.drawing.setColor(((Bullet) m).baseColorR, ((Bullet) m).baseColorG, ((Bullet) m).baseColorB);
                    Drawing.drawing.fillInterfaceOval(x, y, 5, 5);
                    Drawing.drawing.setColor(((Bullet) m).outlineColorR, ((Bullet) m).outlineColorG, ((Bullet) m).outlineColorB);
                    Drawing.drawing.fillInterfaceOval(x, y, 5, 5);
                }
            }
        }

        Drawing.drawing.setColor(Game.playerTank.colorR, Game.playerTank.colorG, Game.playerTank.colorB);
        if (centered && !Game.playerTank.destroy)
            Drawing.drawing.drawInterfaceImage(Game.playerTank.angle - Math.PI, "/images/icons/vertical_arrow_white.png", posX, posY, 12, 10);
    }

    @Override
    public void update()
    {
        /*if (Game.game.input.minimapToggle.isValid() && !(Game.currentLevel instanceof ModLevel && ((ModLevel) Game.currentLevel).forceDisableMinimap))
        {
            Game.game.input.minimapToggle.invalidate();
            enabled = !enabled;
        }*/

        if (!enabled)
            return;

        posX = (int) (Panel.windowWidth - sizeX - posOffsetX);
        posY = (int) (Panel.windowHeight - sizeY - Drawing.drawing.statsHeight - posOffsetY);

        if (posX < 0)
            posOffsetX = (int) (Panel.windowWidth - sizeX);
        else if (posY < 0)
            posOffsetY = (int) (Panel.windowHeight - sizeY);

        if (draggable && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
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

        /* more stuff for next ver ig
        if (resizable && Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
        {
            draggable = false;
            if (posX - 50 < Game.game.window.absoluteMouseX && Game.game.window.absoluteMouseX < posX + 20) {
                sizeX += Game.game.window.absoluteMouseX - posX;
            }
            else if (posX + sizeX - 20 < Game.game.window.absoluteMouseX && Game.game.window.absoluteMouseX < posX + 50) {
                sizeX += Game.game.window.absoluteMouseX - (posX + sizeX);
            }
            else
                draggable = true;
        }*/


        /*if (Game.game.input.minimapChangeTheme.isValid())
        {
            Game.game.input.minimapChangeTheme.invalidate();
            darkMode = !darkMode;
        }

        if (Game.game.input.minimapChangeType.isValid())
        {
            Game.game.input.minimapChangeType.invalidate();
            colorfulObstacles = !colorfulObstacles;
        }

        if (Game.game.input.minimapIncreaseScale.isValid() && scale < 3)
        {
            Game.game.input.minimapIncreaseScale.invalidate();
            scale += 0.25;
        }

        if (Game.game.input.minimapDecreaseScale.isValid() && scale > 0.25)
        {
            Game.game.input.minimapDecreaseScale.invalidate();
            scale -= 0.25;
        }

        if (Game.game.input.minimapPanUp.isValid())
        {
            Game.game.input.minimapPanUp.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY -= 40 / scale;
        }

        if (Game.game.input.minimapPanDown.isValid())
        {
            Game.game.input.minimapPanDown.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetY += 40 / scale;
        }

        if (Game.game.input.minimapPanRight.isValid())
        {
            Game.game.input.minimapPanRight.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX += 40 / scale;
        }

        if (Game.game.input.minimapPanLeft.isValid())
        {
            Game.game.input.minimapPanLeft.invalidate();

            if (centered)
            {
                centered = false;
                panOffsetX = Game.playerTank.posX / 13 * scale;
                panOffsetY = Game.playerTank.posY / 13 * scale;
            }
            panOffsetX -= 40 / scale;
        }

        if (Game.game.input.minimapRecenter.isValid())
        {
            Game.game.input.minimapRecenter.invalidate();

            panOffsetX = 0;
            panOffsetY = 0;
            centered = true;
        }*/
    }
}

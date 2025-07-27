package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.obstacle.Obstacle;

public class ScreenExit extends Screen
{
    public long startTime = System.currentTimeMillis();

    double outroTime = 1000;
    double outroAnimationTime = 500;

    protected long lastTime = System.currentTimeMillis();

    public ScreenExit()
    {
        if (Game.fancyTerrain && Game.enable3d)
            outroAnimationTime = 1000;
    }

    @Override
    public void update()
    {
        if (System.currentTimeMillis() - startTime >= outroTime + outroAnimationTime)
        {
            Game.game.window.windowHandler.onWindowClose();

            if (Game.game.window.soundsEnabled)
                Game.game.window.soundPlayer.exit();

            if (Game.game.window.platformHandler == null)
                System.exit(0);
            else
                Game.game.window.platformHandler.quit();
        }
    }

    @Override
    public void draw()
    {
        Game.game.window.clipMultiplier = 2;
        Game.game.window.clipDistMultiplier = 1;

        if (!Game.game.window.drawingShadow || !Game.shadowsEnabled)
            lastTime = System.currentTimeMillis();

        if (lastTime - startTime < outroTime + outroAnimationTime)
        {
            double frac = ((lastTime - startTime) / outroAnimationTime);
            double frac2 = Math.min(1, frac);

            Drawing.drawing.setColor(Level.currentColor.red * (1 - frac2), Level.currentColor.green * (1 - frac2), Level.currentColor.blue * (1 - frac2));
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Game.game.window.absoluteWidth * 1.2 / Drawing.drawing.interfaceScale, Game.game.window.absoluteHeight * 1.2 / Drawing.drawing.interfaceScale);
            Obstacle.draw_size = Game.tile_size * (1 - frac2);

            Drawing.drawing.setInterfaceFontSize(48);
            Drawing.drawing.setColor(255, 255, 255, 255 * Math.max(0, Math.min(1 - ((lastTime - startTime - outroAnimationTime) / outroTime), 1)));
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "\"Tanks\" for playing!");

            if (lastTime - startTime <= outroAnimationTime)
            {
                Game.screen.drawDefaultBackground(1 - frac);
                Panel.panel.drawBar(frac * 40);
            }

            Panel.panel.drawMouseTarget();
        }
    }
}

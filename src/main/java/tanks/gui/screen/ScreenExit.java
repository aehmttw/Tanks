package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class ScreenExit extends Screen
{
    public long startTime = System.currentTimeMillis();

    double outroTime = 1000;
    double outroAnimationTime = 500;


    public ScreenExit()
    {
        if (Game.fancyGraphics && Game.enable3d)
            outroAnimationTime = 1000;
    }

    @Override
    public void update()
    {
        if (System.currentTimeMillis() - startTime >= outroTime + outroAnimationTime)
        {
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
        if (System.currentTimeMillis() - startTime < outroTime + outroAnimationTime)
        {
            double frac = ((System.currentTimeMillis() - startTime) / outroAnimationTime);
            double frac2 = Math.min(1, frac);

            Drawing.drawing.setColor(174 * (1 - frac2), 92 * (1 - frac2), 16 * (1 - frac2));
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX * 1.2, Drawing.drawing.sizeY * 1.2);

            Drawing.drawing.setInterfaceFontSize(48);
            Drawing.drawing.setColor(255, 255, 255, 255 * Math.max(0, Math.min(1 - ((System.currentTimeMillis() - startTime - outroAnimationTime) / outroTime), 1)));
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, "\"Tanks\" for playing!");

            if (System.currentTimeMillis() - startTime <= outroAnimationTime)
            {
                Game.screen.drawDefaultBackground(1 - frac);
                Panel.panel.drawBar(frac * 40);
            }

            Panel.panel.drawMouseTarget();
        }
    }
}

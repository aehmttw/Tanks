package tanks.gui.screen;

import basewindow.transformation.Translation;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Panel;
import tanks.tank.Tank;
import tanks.tank.TankDummyLoadingScreen;

public class ScreenSplash extends Screen
{
    public long startTime = System.currentTimeMillis();

    public static double splashTime = 5000;
    public static double introTime = 1000;
    public boolean sound;
    public double alpha;
    public double frameStartTime;
    public double introAnimationTime = 500;
    public Translation zoomTranslation = new Translation(Game.game.window, 0, 0, 0);
    public Tank dummySpin;

    public ScreenSplash()
    {
        sound = false;
        alpha = 0;
        dummySpin = new TankDummyLoadingScreen(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2);
    }

    @Override
    public void update()
    {
        if (!sound)
        {
            Drawing.drawing.playSound("splash_jingle.ogg");
            sound = true;
        }
        if (System.currentTimeMillis() - startTime <= splashTime)
        {
            alpha+=0.5;
            if(alpha > 255)
                alpha = 255;
        }
    }

    @Override
    public void draw()
    {
        if (System.currentTimeMillis() - startTime <= splashTime) {
            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.fillInterfaceRect(700, 450, 1400, 900);
            Drawing.drawing.setColor(255, 255, 255, alpha);
            Drawing.drawing.fillInterfaceGlow(700, 300, 400, 400);
            Drawing.drawing.drawInterfaceImage("opal.PNG", 700, 300, 200, 200);
            Drawing.drawing.setInterfaceFontSize(100);
            Drawing.drawing.drawInterfaceText(700,600,"Opal Games");
        }
        else if (System.currentTimeMillis() - startTime <= splashTime + introTime + introAnimationTime)
        {
            //Add in Inital Animation Here.
        }
        else
            Game.exitToTitle();
    }
}

package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlaySingleplayer extends Screen
{
    Button randomLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Random level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.reset();
            Game.screen = new ScreenGame();
        }
    }
            , "Generate a random level to play");

    Button crusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Crusades", new Runnable()
    {
        @Override
        public void run()
        {
            if (Crusade.currentCrusade == null)
                Game.screen = new ScreenCrusades();
            else
                Game.screen = new ScreenResumeCrusade();
        }
    }
            , "Fight battles in an order,---and see how long you can survive!");

    Button create = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "My levels", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenSavedLevels();
        }
    }
    , "Create and play your own levels!");

    Button tutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Tutorial", new Runnable()
    {
        @Override
        public void run()
        {
            Tutorial.loadTutorial(false, Game.game.window.touchscreen);
        }
    }, "Learn how to play Tanks!"
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlay();
        }
    }
    );

    @Override
    public void update()
    {
        randomLevel.update();
        crusade.update();
        create.update();
        tutorial.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, "Select a single-player game mode");
        back.draw();
        tutorial.draw();
        create.draw();
        crusade.draw();
        randomLevel.draw();
    }

}

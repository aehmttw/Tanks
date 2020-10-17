package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlaySingleplayer extends Screen
{
    public ScreenPlaySingleplayer()
    {
        this.music = "tomato_feast_3.ogg";
        this.musicID = "menu";
    }

    Button randomLevel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Random level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.reset();
            Game.screen = new ScreenGame();
        }
    }
            , "Generate a random level to play");

    Button crusade = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Crusades", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenCrusades();
        }
    }
            , "Fight battles in an order,---and see how long you can survive!");

    Button create = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "My levels", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenSavedLevels();
        }
    }
    , "Create and play your own levels!");

    Button tutorial = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Tutorial", new Runnable()
    {
        @Override
        public void run()
        {
            new Tutorial().loadTutorial(false, Game.game.window.touchscreen);
        }
    }, "Learn how to play Tanks!"
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Back", new Runnable()
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
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, "Select a singleplayer game mode");
        back.draw();
        tutorial.draw();
        create.draw();
        crusade.draw();
        randomLevel.draw();
    }

}

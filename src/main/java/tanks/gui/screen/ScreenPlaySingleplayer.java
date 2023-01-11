package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlaySingleplayer extends Screen
{
    public ScreenPlaySingleplayer()
    {
        this.music = "menu_3.ogg";
        this.musicID = "menu";
    }

    Button randomLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1.75, this.objWidth, this.objHeight, "Random level", () ->
    {
        Game.cleanUp();
        Game.loadRandomLevel();
        Game.screen = new ScreenGame();
    }
            , "Generate a random level to play");

    Button crusade = new Button(this.centerX, this.centerY - this.objYSpace * 0.75, this.objWidth, this.objHeight, "Crusades", () -> Game.screen = new ScreenCrusades()
            , "Fight battles in an order,---and see how long you can survive!");

    Button minigames = new Button(this.centerX, this.centerY + this.objYSpace * 0.25, this.objWidth, this.objHeight, "Minigames", () -> Game.screen = new ScreenMinigames()
            , "Play Tanks in new ways!");

    Button create = new Button(this.centerX, this.centerY + this.objYSpace * 1.25, this.objWidth, this.objHeight, "My levels", () -> Game.screen = new ScreenSavedLevels()
            , "Create and play your own levels!");

    Button tutorial = new Button(this.centerX, this.centerY + this.objYSpace * 2.25, this.objWidth, this.objHeight, "Tutorial", () -> new Tutorial().loadTutorial(false, Game.game.window.touchscreen), "Learn how to play Tanks!"
    );

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlay()
    );

    @Override
    public void update()
    {
        randomLevel.update();
        crusade.update();
        minigames.update();
        create.update();
        tutorial.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select a singleplayer game mode");
        back.draw();
        tutorial.draw();
        create.draw();
        minigames.draw();
        crusade.draw();
        randomLevel.draw();
    }

}

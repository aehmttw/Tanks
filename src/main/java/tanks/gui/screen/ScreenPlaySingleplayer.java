package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.modapi.ModAPI;

public class ScreenPlaySingleplayer extends Screen
{
    public ScreenPlaySingleplayer()
    {
        this.music = "menu_3.ogg";
        this.musicID = "menu";

        if (ModAPI.registeredCustomGames.size() > 0) {
            tutorial.posY += this.objYSpace;
            back.posY += this.objYSpace;
        }
    }

    Button randomLevel = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Random level", () -> {
        Game.cleanUp();
        Game.loadRandomLevel();
        Game.screen = new ScreenGame();
    },
            "Generate a random level to play");

    Button crusade = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Crusades", () -> Game.screen = new ScreenCrusades()
            , "Fight battles in an order,---and see how long you can survive!");

    Button levels = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "My levels", () -> Game.screen = new ScreenSavedLevels()
            , "Create and play your own levels!");

    Button modLevels = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Modded levels", () -> Game.screen = new ScreenModdedLevels(),
            "Play your modded levels!");

    Button tutorial = new Button(this.centerX, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Tutorial", () -> new Tutorial().loadTutorial(false, Game.game.window.touchscreen), "Learn how to play Tanks!");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlay());

    @Override
    public void update()
    {
        randomLevel.update();
        crusade.update();
        levels.update();
        tutorial.update();
        back.update();

        if (ModAPI.registeredCustomGames.size() > 0)
            modLevels.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select a singleplayer game mode");

        if (ModAPI.registeredCustomGames.size() > 0)
            modLevels.draw();

        back.draw();
        tutorial.draw();
        levels.draw();
        crusade.draw();
        randomLevel.draw();
    }

}

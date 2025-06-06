package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;

public class ScreenCrusadeOptions extends Screen
{
    public Crusade crusade;
    public ScreenCrusadeEditor previous;

    public TextBox startingLives;
    public TextBox bonusLifeFrequency;

    public String toggleNamesText = "Level names: ";
    public Button toggleNames = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            crusade.showNames = !crusade.showNames;

            if (crusade.showNames)
                toggleNames.setText(toggleNamesText, ScreenOptions.onText);
            else
                toggleNames.setText(toggleNamesText, ScreenOptions.offText);
        }
    }, "Show level names before---the battle begins");

    public String toggleRespawnsText = "Bots respawn: ";
    public Button toggleRespawns = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            crusade.respawnTanks = !crusade.respawnTanks;

            if (crusade.respawnTanks)
                toggleRespawns.setText(toggleRespawnsText, ScreenOptions.onText);
            else
                toggleRespawns.setText(toggleRespawnsText, ScreenOptions.offText);
        }
    }, "Toggles whether tanks you---destroyed should come back when---retrying the level.------When off, you will not be able to---replay battles you've cleared.");

    public int titleOffset = -270;

    public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public ScreenCrusadeOptions(ScreenCrusadeEditor p)
    {
        super(350, 40, 380, 60);

        this.previous = p;
        this.crusade = p.crusade;

        this.music = "menu_editor.ogg";
        this.musicID = "menu";

        this.allowClose = false;

        if (crusade.showNames)
            toggleNames.setText(toggleNamesText, ScreenOptions.onText);
        else
            toggleNames.setText(toggleNamesText, ScreenOptions.offText);

        if (crusade.respawnTanks)
            toggleRespawns.setText(toggleRespawnsText, ScreenOptions.onText);
        else
            toggleRespawns.setText(toggleRespawnsText, ScreenOptions.offText);

        startingLives = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Starting lives", () ->
        {
            if (startingLives.inputText.length() == 0)
                startingLives.inputText = crusade.startingLives + "";
            else
                crusade.startingLives = Integer.parseInt(startingLives.inputText);
        }
                , crusade.startingLives + "");

        startingLives.allowLetters = false;
        startingLives.allowSpaces = false;
        startingLives.minValue = 1;
        startingLives.checkMinValue = true;
        startingLives.maxChars = 9;

        bonusLifeFrequency = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "Bonus life frequency", () ->
        {
            if (bonusLifeFrequency.inputText.length() == 0)
                bonusLifeFrequency.inputText = crusade.bonusLifeFrequency + "";
            else
                crusade.bonusLifeFrequency = Integer.parseInt(bonusLifeFrequency.inputText);
        }
                , crusade.bonusLifeFrequency + "");

        bonusLifeFrequency.allowLetters = false;
        bonusLifeFrequency.allowSpaces = false;
        bonusLifeFrequency.minValue = 1;
        bonusLifeFrequency.checkMinValue = true;
        bonusLifeFrequency.maxChars = 9;
    }

    @Override
    public void update()
    {
        startingLives.update();
        bonusLifeFrequency.update();
        toggleNames.update();
        toggleRespawns.update();
        quit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, 60, width, 120);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, 60, this.crusade.name.replace("_", " "));

        bonusLifeFrequency.draw();
        startingLives.draw();
        toggleRespawns.draw();
        toggleNames.draw();

        quit.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + titleOffset, "Crusade options");
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new ScreenConfirmSaveCrusade(Game.screen, this.previous);
    }
}

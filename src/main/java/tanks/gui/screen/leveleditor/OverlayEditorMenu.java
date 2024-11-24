package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.SpeedrunTimer;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

public class OverlayEditorMenu extends ScreenLevelEditorOverlay
{
    public boolean showTime = false;

    public Button resume = new Button(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Edit", this::escape);

    public Button play = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2.5), this.objWidth, this.objHeight, "Play", () -> screenLevelEditor.play());

    public Button playUnavailable = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2.5), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

    public Button options = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 0.5), this.objWidth, this.objHeight, "Level options", () -> Game.screen = new OverlayLevelOptions(Game.screen, screenLevelEditor));

    public Button quit = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2.5), this.objWidth, this.objHeight, "Exit", () ->
    {
        screenLevelEditor.save();

        Game.cleanUp();
        Game.screen = new ScreenSavedLevels();
    }
    );

    public Button clone = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 0.5), this.objWidth, this.objHeight, "Make a copy", () -> Game.screen = new OverlayCloneLevel(Game.screen, screenLevelEditor));

    public Button delete = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 1.5), this.objWidth, this.objHeight, "Delete level", () -> Game.screen = new OverlayConfirmDelete(Game.screen, screenLevelEditor));

    public OverlayEditorMenu(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.allowClose = false;

        if (!screenLevelEditor.level.editable)
        {
            play.posY += 60;
            delete.posY -= 60;
            quit.posY -= 60;
        }

        //this.music = "editor_paused.ogg";
        //this.musicID = "editor";
    }

    public void update()
    {
        if (screenLevelEditor.level.editable)
        {
            resume.update();
            options.update();
            super.update();
        }

        delete.update();
        quit.update();
        clone.update();

        if (screenLevelEditor.spawns.size() > 0)
            play.update();
        else
            playUnavailable.update();

        if (Game.game.input.editorPlay.isValid() && screenLevelEditor.spawns.size() > 0)
        {
            screenLevelEditor.play();
            Game.game.input.play.invalidate();
        }
    }

    public void draw()
    {
        if (Level.isDark())
            this.screenLevelEditor.fontBrightness = 255;
        else
            this.screenLevelEditor.fontBrightness = 0;

        super.draw();

        if (screenLevelEditor.level.editable)
        {
            resume.draw();
            options.draw();
        }

        delete.draw();
        quit.draw();
        clone.draw();

        if (screenLevelEditor.spawns.size() > 0)
            play.draw();
        else
            playUnavailable.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level menu");

        if (Game.showSpeedrunTimer && showTime)
            SpeedrunTimer.draw();
    }
}

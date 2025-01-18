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

    public Button play = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2.5), this.objWidth, this.objHeight, "Play", () -> editor.play());

    public Button playUnavailable = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2.5), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

    public Button options = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 0.5), this.objWidth, this.objHeight, "Level options", () -> Game.screen = new OverlayLevelOptions(Game.screen, editor));

    public Button quit = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2.5), this.objWidth, this.objHeight, "Exit", () ->
    {
        editor.save();

        Game.cleanUp();
        System.gc();
        Game.screen = new ScreenSavedLevels();
    }
    );

    public Button clone = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 0.5), this.objWidth, this.objHeight, "Make a copy", () -> Game.screen = new OverlayCloneLevel(Game.screen, editor));

    public Button delete = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 1.5), this.objWidth, this.objHeight, "Delete level", () -> Game.screen = new OverlayConfirmDelete(Game.screen, editor));

    public OverlayEditorMenu(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        if (!screenLevelEditor.level.editable)
        {
            play.posY += 60;
            delete.posY -= 60;
            quit.posY -= 60;
        }
    }

    public void update()
    {
        if (!editor.initialized)
            editor.initialize();

        if (editor.level.editable)
        {
            resume.update();
            options.update();
            super.update();
        }

        delete.update();
        quit.update();
        clone.update();

        if (!editor.spawns.isEmpty())
            play.update();
        else
            playUnavailable.update();

        if (Game.game.input.editorPlay.isValid() && !editor.spawns.isEmpty())
        {
            editor.play();
            Game.game.input.play.invalidate();
        }
    }

    public void draw()
    {
        if (Level.isDark())
            this.editor.fontBrightness = 255;
        else
            this.editor.fontBrightness = 0;

        super.draw();

        if (editor.level.editable)
        {
            resume.draw();
            options.draw();
        }

        delete.draw();
        quit.draw();
        clone.draw();

        if (!editor.spawns.isEmpty())
            play.draw();
        else
            playUnavailable.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level menu");

        if (Game.showSpeedrunTimer && showTime)
            SpeedrunTimer.draw();
    }
}

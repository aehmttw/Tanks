package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

public class OverlayConfirmDelete extends ScreenLevelEditorOverlay
{
    public Button cancelDelete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", this::escape
    );

    public Button confirmDelete = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", () ->
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + editor.name);

        Game.cleanUp();

        while (file.exists())
        {
            file.delete();
        }

        Game.screen = new ScreenSavedLevels();
    }
    );

    public OverlayConfirmDelete(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);
    }

    public void update()
    {
        this.cancelDelete.update();
        this.confirmDelete.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to delete the level?");

        this.cancelDelete.draw();
        this.confirmDelete.draw();
    }
}

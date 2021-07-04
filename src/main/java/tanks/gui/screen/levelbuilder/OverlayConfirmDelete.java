package tanks.gui.screen.levelbuilder;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

public class OverlayConfirmDelete extends ScreenLevelBuilderOverlay
{
    public Button cancelDelete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button confirmDelete = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + screenLevelEditor.name);

            Game.cleanUp();

            while (file.exists())
            {
                file.delete();
            }

            Game.screen = new ScreenSavedLevels();
        }
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

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to delete the level?");

        this.cancelDelete.draw();
        this.confirmDelete.draw();
    }
}

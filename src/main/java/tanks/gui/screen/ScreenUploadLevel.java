package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.SavedFilesList;

public class ScreenUploadLevel extends ScreenOnline
{
    public SavedFilesList levels;

    public ScreenUploadLevel()
    {
        this.levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -30, (name, file) ->
        {
            ScreenPreviewUploadLevel sc = new ScreenPreviewUploadLevel(name, (ScreenUploadLevel) Game.screen);
            if (Game.loadLevel(file, sc))
            {
                sc.level = Game.currentLevel;
                Game.screen = sc;
            }
        }, (file) -> null);
    }

    @Override
    public void update()
    {
        super.update();
        this.levels.update();
        ScreenSavedLevels.page = this.levels.page;
    }

    @Override
    public void draw()
    {
        super.draw();
        this.levels.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Select a level to upload...");
    }

}

package tanks.gui.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenCrusadeAddLevel extends ScreenPlaySavedLevels
{
    public ScreenCrusadeEditor previous;

    public ScreenCrusadeAddLevel(ScreenCrusadeEditor s)
    {
        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.previous = s;

        this.allowClose = false;

        this.title = "Select a level to add";

        this.quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = previous
        );
    }

    @Override
    public void initializeLevels()
    {
        this.allLevels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -30,
                (name, file) ->
                {
                    ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(name, null, this, previous);
                    if (Game.loadLevel(file, s))
                    {
                        s.level = Game.currentLevel.levelString;
                        Game.screen = s;
                    }
                }, (file) -> "Last modified---" + Game.timeInterval(file.lastModified(), System.currentTimeMillis()) + " ago");

        this.levels = allLevels.clone();

        allLevels.sortedByTime = ScreenSavedLevels.sortByTime;
        allLevels.sort(ScreenSavedLevels.sortByTime);

        if (allLevels.sortedByTime)
            sort.setHoverText("Sorting by last modified");
        else
            sort.setHoverText("Sorting by name");

        levels = allLevels.clone();
        newLevelsList();
    }

    @Override
    public void update()
    {
        super.update();

        if (Game.game.input.editorPause.isValid())
        {
            quit.function.run();
            Game.game.input.editorPause.invalidate();
        }
    }

    @Override
    public void onAttemptClose()
    {
        Game.screen = new ScreenConfirmSaveCrusade(Game.screen, previous);
    }
}

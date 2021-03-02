package tanks.gui.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenCrusadeAddLevel extends ScreenPlaySavedLevels
{
    public ScreenCrusadeBuilder previous;
    public ScreenCrusadeAddLevel instance;

    public ScreenCrusadeAddLevel(ScreenCrusadeBuilder s)
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.instance = this;
        this.previous = s;

        this.title = "Select a level to add";

        this.quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
        {
            @Override
            public void run()
            {
                Game.screen = previous;
            }
        }
        );
    }

    @Override
    public void initializeLevels()
    {
        this.levels = new SavedFilesList(Game.homedir + Game.levelDir, ScreenSavedLevels.page, 0, -30,
                (name, file) ->
                {
                    ScreenCrusadeEditLevel s = new ScreenCrusadeEditLevel(name, null, instance, previous);
                    if (Game.loadLevel(file, s))
                    {
                        s.level = Game.currentLevel.levelString;
                        Game.screen = s;
                    }
                }, (file) -> null);
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
}

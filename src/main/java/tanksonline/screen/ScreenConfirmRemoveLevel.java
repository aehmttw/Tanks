package tanksonline.screen;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServerHandler;
import tanksonline.UploadedLevel;

public class ScreenConfirmRemoveLevel extends ScreenLayout
{
    public UploadedLevel level;
    public ScreenLayout screen;

    public Button cancelDelete = new Button(sizeX / 2, sizeY / 2 + 60, 350, 40, "No", new Runnable()
    {
        @Override
        public void run()
        {
            screen.setScreen();
        }
    }
    );

    public Button confirmDelete = new Button(sizeX / 2, sizeY / 2, 350, 40, "Yes", new Runnable()
    {
        @Override
        public void run()
        {
            synchronized (PlayerMap.instance)
            {
                PlayerMap.instance.deleteLevel(level);
            }

            ScreenHome s = new ScreenHome(player);
            s.setScreen();
        }
    }
    );

    public ScreenConfirmRemoveLevel(TanksOnlineServerHandler player, UploadedLevel l, ScreenLayout s)
    {
        super(player);
        this.level = l;
        this.screen = s;

        confirmDelete.wait = true;
        cancelDelete.wait = true;

        this.buttons.add(confirmDelete);
        this.buttons.add(cancelDelete);

        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.texts.add(new ScreenOnline.Text("Would you like to remove the level \"" + level.name + "\" from Tanks Online?",
                sizeX / 2, sizeY / 2 - 90, 24, 0));
    }
}

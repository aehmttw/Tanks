package tanksonline.screen;

import tanks.gui.Button;
import tanksonline.TanksOnlineServerHandler;

public class ScreenUploadLevel extends ScreenSpecial
{
    Button quit = new Button(sizeX / 2, sizeY / 2 + 300, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenHome s = new ScreenHome(player);
            s.setScreen();
        }
    }
    );

    public ScreenUploadLevel(TanksOnlineServerHandler player)
    {
        super(player, "upload_level");

        quit.wait = true;
        this.buttons.add(quit);
    }


}

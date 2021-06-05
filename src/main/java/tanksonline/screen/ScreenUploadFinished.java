package tanksonline.screen;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.TanksOnlineServerHandler;

public class ScreenUploadFinished extends ScreenLayout
{
    Button back = new Button(sizeX / 2, sizeY / 2 + 30, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenLayout s = new ScreenHome(player);
            s.setScreen();
        }
    }
    );

    public ScreenUploadFinished(TanksOnlineServerHandler player, String message)
    {
        super(player);
        this.texts.add(new ScreenOnline.Text(message, sizeX / 2, sizeY / 2 - 90, 24, 0));

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        back.wait = true;
        this.buttons.add(back);
    }
}

package tanksonline.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.AccessCode;
import tanksonline.TanksOnlineServerHandler;

public class ScreenWelcomeToTanksOnline extends ScreenLayout
{
    Button play = new Button(sizeX / 2, sizeY / 2 + 30, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenHome s = new ScreenHome(player);
            s.setScreen();
        }
    }
    );

    public ScreenWelcomeToTanksOnline(TanksOnlineServerHandler player, AccessCode accessCode)
    {
        super(player);
        this.texts.add(new ScreenOnline.Text("Welcome to Tanks Online!", sizeX / 2, sizeY / 2 - 60, 24, 0));

        this.music = "tomato_feast_3.ogg";
        this.musicID = "menu";

        if (accessCode.expiration < 0)
            this.texts.add(new ScreenOnline.Text("You now have indefinite access to Tanks Online", sizeX / 2, sizeY / 2 - 30, 18, 0));
        else
            this.texts.add(new ScreenOnline.Text("You now have access to Tanks Online for " + Game.timeInterval(System.currentTimeMillis(), accessCode.expiration), sizeX / 2, sizeY / 2 - 30, 18, 0));

        play.wait = true;
        this.buttons.add(play);
    }
}

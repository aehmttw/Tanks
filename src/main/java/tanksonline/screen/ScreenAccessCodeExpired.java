package tanksonline.screen;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlinePlayer;
import tanksonline.TanksOnlineServerHandler;

import java.io.File;

public class ScreenAccessCodeExpired extends ScreenLayout
{
    Button back = new Button(sizeX / 2, sizeY / 2 + 60, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenInsertAccessCode s = new ScreenInsertAccessCode(player);
            s.setScreen();
        }
    }
    );

    public ScreenAccessCodeExpired(TanksOnlineServerHandler player)
    {
        super(player);
        this.texts.add(new ScreenOnline.Text("Oh noes!", sizeX / 2, sizeY / 2 - 120, 24, 0));

        this.texts.add(new ScreenOnline.Text("Your Tanks Online access code has expired!", sizeX / 2, sizeY / 2 - 60, 24, 0));
        this.texts.add(new ScreenOnline.Text("Don't worry though - your data has not been lost", sizeX / 2, sizeY / 2 - 30, 24, 0));
        this.texts.add(new ScreenOnline.Text("To continue using Tanks Online, obtain another acccess code", sizeX / 2, sizeY / 2 - 0, 24, 0));

        back.wait = true;
        this.buttons.add(back);

        this.music = "tomato_feast_2.ogg";
        this.musicID = "menu";

        synchronized (PlayerMap.instance)
        {
            TanksOnlinePlayer p = PlayerMap.instance.getPlayer(player.computerID);
            p.accessCode.players.remove(p);
            p.accessCode.save(new File(PlayerMap.access_codes_dir + "/" + p.accessCode.id + ".tanks"));
            p.accessCode = null;
        }
    }
}

package tanksonline.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.AccessCode;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlinePlayer;
import tanksonline.TanksOnlineServerHandler;

import java.io.File;

public class ScreenUnlinkAccessCode extends ScreenLayout
{
    public AccessCode accessCode;

    Button unlink = new Button(sizeX / 2, sizeY / 2 + 150, 350, 40, "Unlink access code", () ->
    {
        synchronized (PlayerMap.instance)
        {
            TanksOnlinePlayer p = PlayerMap.instance.getPlayer(player.computerID);
            p.accessCode.players.remove(p);
            p.accessCode.save(new File(PlayerMap.access_codes_dir + "/" + p.accessCode.id + ".tanks"));
            p.accessCode = null;
        }

        ScreenInsertAccessCode s = new ScreenInsertAccessCode(player);
        s.setScreen();
    }
    );

    Button back = new Button(sizeX / 2, sizeY / 2 + 210, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenAccessInfo s = new ScreenAccessInfo(player, accessCode);
            s.setScreen();
        }
    }
    );

    public ScreenUnlinkAccessCode(TanksOnlineServerHandler player, AccessCode ac)
    {
        super(player);
        this.accessCode = ac;

        this.music = "menu_3.ogg";
        this.musicID = "menu";

        this.texts.add(new ScreenOnline.Text("Unlink access code", sizeX / 2, sizeY / 2 - 150, 24, 0));
        this.texts.add(new ScreenOnline.Text("Are you sure you would like to unlink this access code?", sizeX / 2, sizeY / 2 - 90, 24, 0));
        this.texts.add(new ScreenOnline.Text("Doing so will revoke your access to Tanks Online", sizeX / 2, sizeY / 2 - 60, 24, 0));
        this.texts.add(new ScreenOnline.Text("until you link an access code again.", sizeX / 2, sizeY / 2 - 30, 24, 0));
        this.texts.add(new ScreenOnline.Text("Anyone else will be able to use this access code.", sizeX / 2, sizeY / 2 + 30, 24, 0));

        if (ac.expiration > 0)
        {
            this.texts.add(new ScreenOnline.Text("Additionally, the access code will continue to expire in " + Game.timeInterval(System.currentTimeMillis(), ac.expiration), sizeX / 2, sizeY / 2 + 90, 24, 0));
        }

        unlink.wait = true;
        back.wait = true;
        this.buttons.add(back);
        this.buttons.add(unlink);
    }
}

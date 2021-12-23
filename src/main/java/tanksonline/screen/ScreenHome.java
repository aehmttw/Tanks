package tanksonline.screen;

import tanks.event.online.EventSilentDisconnect;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServerHandler;

public class ScreenHome extends ScreenLayout
{
    Button upload = new Button(sizeX / 2, sizeY / 2 - 120, 350, 40, "Upload level", () ->
    {
        ScreenUploadLevel s = new ScreenUploadLevel(player);
        s.setScreen();
    }
    );

    Button browse = new Button(sizeX / 2, sizeY / 2 - 60, 350, 40, "Browse levels", () ->
    {
        synchronized (PlayerMap.instance)
        {
            ScreenBrowseLevels s = new ScreenBrowseLevels(player, "Browse levels", PlayerMap.instance.getLevels());
            s.setScreen();
        }
    }
    );

    Button myUploadedLevels = new Button(sizeX / 2, sizeY / 2 + 0, 350, 40, "My uploaded levels", () ->
    {
        synchronized (PlayerMap.instance)
        {
            ScreenBrowseLevels s = new ScreenBrowseLevels(player, "My uploaded levels", PlayerMap.instance.getLevels(player.computerID));
            s.setScreen();
        }
    }
    );

    Button account = new Button(sizeX / 2, sizeY / 2 + 60, 350, 40, "My account", () ->
    {
        synchronized (PlayerMap.instance)
        {
            ScreenAccessInfo s = new ScreenAccessInfo(player, PlayerMap.instance.getPlayer(player.computerID).accessCode);
            s.setScreen();
        }
    }
    );

    Button back = new Button(sizeX / 2, sizeY / 2 + 120, 350, 40, "Back", () -> player.sendEvent(new EventSilentDisconnect())
    );

    public ScreenHome(TanksOnlineServerHandler player)
    {
        super(player);
        this.texts.add(new ScreenOnline.Text("Tanks Online menu", sizeX / 2, sizeY / 2 - 240, 24, 0));

        this.music = "menu_3.ogg";
        this.musicID = "menu";

        upload.wait = true;
        browse.wait = true;
        myUploadedLevels.wait = true;
        account.wait = true;
        back.wait = true;

        this.buttons.add(upload);
        this.buttons.add(browse);
        this.buttons.add(myUploadedLevels);
        this.buttons.add(account);
        this.buttons.add(back);
    }
}

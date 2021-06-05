package tanksonline.screen;

import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.AccessCode;
import tanksonline.TanksOnlineServerHandler;

public class ScreenAccessInfo extends ScreenLayout
{
    public AccessCode accessCode;

    Button back = new Button(sizeX / 2, sizeY / 2 + 180, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenHome s = new ScreenHome(player);
            s.setScreen();
        }
    }
    );

    Button unlink = new Button(sizeX / 2, sizeY / 2 + 120, 350, 40, "Unlink access code", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenUnlinkAccessCode s = new ScreenUnlinkAccessCode(player, accessCode);
            s.setScreen();
        }
    }
    );

    public ScreenAccessInfo(TanksOnlineServerHandler player, AccessCode accessCode)
    {
        super(player);
        this.accessCode = accessCode;

        this.music = "menu_3.ogg";
        this.musicID = "menu";

        this.texts.add(new ScreenOnline.Text("My account", sizeX / 2, sizeY / 2 - 120, 24, 0));

        this.texts.add(new ScreenOnline.Text("Your access code:", sizeX / 2, sizeY / 2 - 60, 24, 0));
        this.texts.add(new ScreenOnline.Text(accessCode.id.toString(), sizeX / 2, sizeY / 2 - 30, 24, 0));

        if (accessCode.expiration < 0)
            this.texts.add(new ScreenOnline.Text("Your access will last indefinitely", sizeX / 2, sizeY / 2 + 0, 24, 0));
        else
            this.texts.add(new ScreenOnline.Text("Your access will expire in " + Game.timeInterval(System.currentTimeMillis(), accessCode.expiration), sizeX / 2, sizeY / 2 + 0, 24, 0));

        if (accessCode.maxUses < 0)
            this.texts.add(new ScreenOnline.Text("Accounts using this access code: " + accessCode.players.size(), sizeX / 2, sizeY / 2 + 30, 24, 0));
        else
            this.texts.add(new ScreenOnline.Text("Accounts using this access code: " + accessCode.players.size() + "/" + accessCode.maxUses, sizeX / 2, sizeY / 2 + 30, 24, 0));

        this.texts.add(new ScreenOnline.Text(accessCode.comment, sizeX / 2, sizeY / 2 + 60, 24, 0));

        back.wait = true;
        unlink.wait = true;
        this.buttons.add(unlink);
        this.buttons.add(back);
    }
}

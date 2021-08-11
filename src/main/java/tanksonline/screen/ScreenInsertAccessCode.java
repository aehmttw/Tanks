package tanksonline.screen;

import tanks.event.online.EventSilentDisconnect;
import tanks.gui.Button;
import tanks.gui.UUIDTextBox;
import tanks.gui.screen.ScreenOnline;
import tanksonline.AccessCode;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlinePlayer;
import tanksonline.TanksOnlineServerHandler;

import java.io.File;
import java.util.UUID;

public class ScreenInsertAccessCode extends ScreenLayout
{
    public UUIDTextBox idBox = new UUIDTextBox(sizeX / 2, sizeY / 2, 700, 40, "Access code", new Runnable()
    {
        @Override
        public void run()
        {

        }
    }, "", "You can obtain an access code---from the developers of Tanks");

    public Button confirm = new Button(sizeX / 2, sizeY / 2 + 90, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            try
            {
                String i = idBox.inputText;
                UUID id = UUID.fromString(i.substring(0, 8) + "-" + i.substring(8, 12) + "-" + i.substring(12, 16) + "-" + i.substring(16, 20) + "-" + i.substring(20));
                synchronized (AccessCode.accessCodes)
                {
                    AccessCode c = AccessCode.accessCodes.get(id);
                    if (c != null && c.valid() && (c.players.size() < c.maxUses || c.maxUses < 0))
                    {
                        synchronized (PlayerMap.instance)
                        {
                            TanksOnlinePlayer p = PlayerMap.instance.getPlayer(player.computerID);
                            p.registered = true;
                            p.accessCode = c;
                            c.players.add(p);

                            c.save(new File(PlayerMap.access_codes_dir + "/" + c.id + ".tanks"));
                            PlayerMap.instance.save();

                            ScreenWelcomeToTanksOnline h = new ScreenWelcomeToTanksOnline(player, c);
                            h.setScreen();

                            return;
                        }
                    }

                }
            }
            catch (Exception e)
            {

            }

            ScreenInvalidAccessCode s = new ScreenInvalidAccessCode(player);
            s.setScreen();
        }
    });

    Button back = new Button(sizeX / 2, sizeY / 2 + 150, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            player.sendEvent(new EventSilentDisconnect());
        }
    }
    );

    public ScreenInsertAccessCode(TanksOnlineServerHandler player)
    {
        super(player);

        this.music = "menu_2.ogg";
        this.musicID = "menu";

        this.textBoxes.add(idBox);
        this.texts.add(new ScreenOnline.Text("Please insert an access code to continue", sizeX / 2, sizeY / 2 - 120, 24, 0));
        confirm.wait = true;
        back.wait = true;
        this.buttons.add(confirm);
        this.buttons.add(back);
    }
}

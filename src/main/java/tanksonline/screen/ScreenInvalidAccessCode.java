package tanksonline.screen;

import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.TanksOnlineServerHandler;

public class ScreenInvalidAccessCode extends ScreenLayout
{
    Button back = new Button(sizeX / 2, sizeY / 2 + 60, 350, 40, "Ok", () ->
    {
        ScreenInsertAccessCode s = new ScreenInsertAccessCode(player);
        s.setScreen();
    }
    );

    public ScreenInvalidAccessCode(TanksOnlineServerHandler player)
    {
        super(player);
        this.texts.add(new ScreenOnline.Text("Invalid access code!", sizeX / 2, sizeY / 2 - 60, 24, 0));

        this.music = "menu_2.ogg";
        this.musicID = "menu";

        back.wait = true;
        this.buttons.add(back);
    }
}

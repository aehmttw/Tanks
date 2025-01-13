package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.network.FriendsMixin;

import java.util.ArrayList;

public class ScreenJoinSteamLobby extends Screen
{
    public ScreenJoinParty screen;
    public ButtonList parties;

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        public void run()
        {
            Game.screen = screen;
        }
    }
    );


    public ScreenJoinSteamLobby(ScreenJoinParty s, ArrayList<Button> parties)
    {
        super(350, 40, 380, 60);

        this.screen = s;
        this.music = "menu_2.ogg";
        this.musicID = "menu";

        this.parties = new ButtonList(parties, 0, 0, -30);
    }

    @Override
    public void update()
    {
        parties.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        parties.draw();
        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);

        if (parties.buttons.isEmpty())
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No public parties found. Why not host one?");
        else
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.75, "Parties located geographically nearby will likely have less network lag");

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Public Steam parties");
    }
}

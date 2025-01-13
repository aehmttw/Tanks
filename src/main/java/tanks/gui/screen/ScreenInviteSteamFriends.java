package tanks.gui.screen;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamID;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.network.FriendsMixin;

import java.util.ArrayList;

public class ScreenInviteSteamFriends extends Screen
{
    public static final long game_id = 1660910;

    public Screen screen;
    public ButtonList friends;

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        public void run()
        {
            Game.screen = screen;
        }
    }
    );


    public ScreenInviteSteamFriends(Screen s)
    {
        super(350, 40, 380, 60);

        this.screen = s;
        this.music = "menu_4.ogg";
        this.musicID = "menu";

        Game.steamNetworkHandler.friends.updateFriends();

        ArrayList<Button> f = new ArrayList<>();

        FriendsMixin d = Game.steamNetworkHandler.friends;
        for (int i: d.friendUserIDs.keySet())
        {
            String name = d.friendNames.get(i);

            StringBuilder newName = new StringBuilder();

            for (char c: name.toCharArray())
            {
                if ("`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+{}|:\"<>?".contains((c + "").toLowerCase()))
                    newName.append(c);
            }

            Button b = new Button(0, 0, this.objWidth, this.objHeight, "", () -> {});
            b.function = () ->
            {
                Game.steamNetworkHandler.matchmaking.inviteUserToLobby(Game.steamNetworkHandler.currentLobby, d.friendUserIDs.get(i));
                b.setSubtext("Invited!");
                b.enabled = false;
            };
            b.text = newName.toString();

            SteamFriends.PersonaState status = d.friendStatuses.get(i);
            if (d.friendGameIDs.get(i) == game_id)
                b.setSubtext("Playing Tanks!");
            else
                b.setSubtext(status.toString());

            if (!status.equals(SteamFriends.PersonaState.Offline))
                f.add(b);
        }

        friends = new ButtonList(f, 0, 0, -30);
    }

    @Override
    public void update()
    {
        friends.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        friends.draw();
        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Invite Steam friends");
    }
}

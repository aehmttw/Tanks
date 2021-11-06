package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.network.FriendsMixin;

import java.util.ArrayList;

public class ScreenJoinSteamFriends extends Screen
{
    public static final long game_id = 1660910;

    public ScreenJoinParty screen;
    public ButtonList friends;

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        public void run()
        {
            Game.screen = screen;
        }
    }
    );


    public ScreenJoinSteamFriends(ScreenJoinParty s)
    {
        super(350, 40, 380, 60);

        this.screen = s;
        this.music = "menu_2.ogg";
        this.musicID = "menu";

        ArrayList<Button> f = new ArrayList<>();

        FriendsMixin d = Game.steamNetworkHandler.friends;
        for (int i: d.friendUserIDs.keySet())
        {
            if (d.friendGameIDs.get(i) == game_id)
            {
                String name = d.friendNames.get(i);

                StringBuilder newName = new StringBuilder();

                for (char c: name.toCharArray())
                {
                    if ("`1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./~!@#$%^&*()_+{}|:\"<>?".contains((c + "").toLowerCase()))
                        newName.append(c);
                }

                f.add(new Button(0, 0, this.objWidth, this.objHeight, newName.toString(), new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = screen.ip.inputText;
                        screen.ip.inputText = "steam:" + i;
                        screen.join.function.run();
                        screen.ip.inputText = s;
                    }
                }));
            }
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
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Steam friends playing Tanks");
    }
}

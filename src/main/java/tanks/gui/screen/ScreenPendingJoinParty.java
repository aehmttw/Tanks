package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.network.Client;

public class ScreenPendingJoinParty extends Screen
{
    public Button confirm = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Leave party", () ->
    {
        Drawing.drawing.playSound("leave.ogg");
        ScreenPartyLobby.isClient = false;

        Client.handler.close();

        Game.screen = new ScreenJoinParty();
        ScreenPartyLobby.connections.clear();
    }
    );

    public ScreenPendingJoinParty()
    {
        this.music = "waiting_music_2.ogg";
    }

    @Override
    public void update()
    {

        confirm.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        confirm.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5 / 3, "Connected to party!");
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2 / 3, "You will be let in when the game finishes");

    }
}

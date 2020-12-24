package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.network.Client;

public class ScreenConfirmLeaveParty extends Screen
{
    public Button back = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPartyLobby();
        }
    }
    );

    public Button confirm = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "Leave party", new Runnable()
    {
        @Override
        public void run()
        {
            Drawing.drawing.playSound("leave.ogg");
            ScreenPartyLobby.isClient = false;
            Client.handler.ctx.close();
            Game.screen = new ScreenJoinParty();
            ScreenPartyLobby.connections.clear();
        }
    }
    );

    public ScreenConfirmLeaveParty()
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        back.update();
        confirm.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        back.draw();
        confirm.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 5 / 3, "Are you sure you want to leave the party?");
    }
}

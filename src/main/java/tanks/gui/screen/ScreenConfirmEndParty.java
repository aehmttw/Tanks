package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ChatMessage;

public class ScreenConfirmEndParty extends Screen
{
    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    public Button confirm = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "End party", new Runnable()
    {
        @Override
        public void run()
        {
            Drawing.drawing.playSound("leave.ogg");

            ScreenPartyHost.isServer = false;
            ScreenPartyHost.server.close();
            ScreenPartyHost.activeScreen = null;
            Game.screen = new ScreenParty();
            ScreenPartyHost.includedPlayers.clear();
            ScreenPartyHost.readyPlayers.clear();
            ScreenPartyHost.activeScreen = null;
            Crusade.currentCrusade = null;

            Game.players.clear();
            Game.players.add(Game.player);

            ScreenPartyHost.disconnectedPlayers.clear();
        }
    }
    );

    public ScreenConfirmEndParty()
    {
        this.music = "menu_3.ogg";
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
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 8 / 3, "Are you sure you want to end the party?");

        if (Crusade.currentCrusade != null)
        {
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 5 / 3, "All players will be disconnected,");
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace, "and progress in the crusade will be lost!");
        }
        else
        {
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4 / 3, "All players will be disconnected!");
        }
    }
}

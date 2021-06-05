package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlayMultiplayer extends Screen
{
    public ScreenPlayMultiplayer()
    {
        this.music = "menu_2.ogg";
        this.musicID = "menu";
    }

    Button party = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenParty();
        }
    },
            "Play with other people who are---connected to your local network---(or who are port forwarding)");

    Button online = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Online", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenJoinOnlineServer();
        }
    },
            "Access the online Tanks community!");


    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenPlay();
        }
    }
    );

    @Override
    public void update()
    {
        party.update();
        online.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Select a multiplayer game mode");
        back.draw();
        online.draw();
        party.draw();
    }

}

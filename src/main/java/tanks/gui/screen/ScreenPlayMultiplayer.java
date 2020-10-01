package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenPlayMultiplayer extends Screen
{
    public ScreenPlayMultiplayer()
    {
        this.music = "tomato_feast_2.ogg";
        this.musicID = "menu";
    }

    Button party = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Party", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenParty();
        }
    },
            "Play with other people who are---connected to your local network---(or who are port forwarding)");

    Button online = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Online", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenJoinOnlineServer();
        }
    },
            "Access the online Tanks community!");


    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Back", new Runnable()
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
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, "Select a multiplayer game mode");
        back.draw();
        online.draw();
        party.draw();
    }

}

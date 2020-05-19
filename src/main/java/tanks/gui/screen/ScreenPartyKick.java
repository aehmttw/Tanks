package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.event.EventKick;
import tanks.gui.Button;
import tanks.network.ServerHandler;

public class ScreenPartyKick extends Screen implements IPartyMenuScreen
{
    public ServerHandler handler;

    public Button kick;

    public Button cancel = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 120, 360, 40, "Cancel", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenPartyHost.activeScreen;
        }
    }
    );

    public ScreenPartyKick(ServerHandler h)
    {
        this.music = "tomato_feast_2.ogg";
        this.musicID = "menu";

        handler = h;
        kick = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 120, 360, 40, "Kick " + handler.username, new Runnable()
        {
             @Override
            public void run()
            {
                handler.sendEventAndClose(new EventKick("You were kicked from the party"));
                Game.screen = ScreenPartyHost.activeScreen;
            }
        }
        );
    }

    @Override
    public void update()
    {
        cancel.update();
        kick.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Would you like to kick " + handler.username + " from the party?");

        cancel.draw();
        kick.draw();
    }
}

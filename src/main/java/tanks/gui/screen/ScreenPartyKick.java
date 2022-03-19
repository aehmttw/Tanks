package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.event.EventKick;
import tanks.gui.Button;
import tanks.network.ServerHandler;

public class ScreenPartyKick extends Screen
{
    public ServerHandler handler;

    public Button kick;

    public Button cancel = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Cancel", () -> Game.screen = ScreenPartyHost.activeScreen
    );

    public ScreenPartyKick(ServerHandler h)
    {
        this.music = "menu_3.ogg";
        this.musicID = "menu";

        handler = h;
        kick = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Kick " + handler.username, () ->
        {
            handler.sendEventAndClose(new EventKick("You were kicked from the party"));
            Game.screen = ScreenPartyHost.activeScreen;
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

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Would you like to kick %s from the party?", handler.username);

        cancel.draw();
        kick.draw();
    }
}

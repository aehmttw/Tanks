package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenShareSelect extends Screen
{
    Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer)
            Game.screen = ScreenPartyHost.activeScreen;
        else
            Game.screen = new ScreenPartyLobby();
    }
    );

    Button level = new Button(this.centerX, this.centerY - this.objYSpace / 2, this.objWidth, this.objHeight, "Share a level", () -> Game.screen = new ScreenShareLevel());

    Button crusade = new Button(this.centerX, this.centerY + this.objYSpace / 2, this.objWidth, this.objHeight, "Share a crusade", () -> Game.screen = new ScreenShareCrusade());

    public ScreenShareSelect()
    {
        this.music = "menu_4.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        level.update();
        crusade.update();
        quit.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "What would you like to share?");

        level.draw();
        crusade.draw();
        quit.draw();
    }
}

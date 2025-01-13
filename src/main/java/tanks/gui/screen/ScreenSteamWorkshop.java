package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenSteamWorkshop extends Screen
{
    public ScreenSteamWorkshop()
    {
        this.music = "menu_3.ogg";
        this.musicID = "menu";
    }

    Button upload = new Button(this.centerX, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Upload", () ->
    {
        if (Game.agreedToWorkshopAgreement)
            Game.screen = new ScreenShareLevel();
        else
            Game.screen = new ScreenSteamWorkshopAgreement();
    },
            "Upload a level!");

    Button download = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Download", () ->
    {
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search("Level", 0, 18);
    },
            "Download a level!");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlay());

    @Override
    public void update()
    {
        upload.update();
        download.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Steam workshop");

        back.draw();
        download.draw();
        upload.draw();
    }
}

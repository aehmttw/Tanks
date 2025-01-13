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

    Button uploadLevel = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Upload level", () ->
    {
        if (Game.agreedToWorkshopAgreement)
            Game.screen = new ScreenShareLevel();
        else
            Game.screen = new ScreenSteamWorkshopAgreement();
    },
            "Share a level you made with the Tanks community!");

    Button downloadLevel = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Browse levels", () ->
    {
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search("Level", 0, 18, null, null, Game.steamNetworkHandler.workshop.searchByScore);
    },
            "Browse levels the Tanks community has created!");

    Button uploadCrusade = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Upload crusade", () ->
    {
        if (Game.agreedToWorkshopAgreement)
            Game.screen = new ScreenShareCrusade();
        else
            Game.screen = new ScreenSteamWorkshopAgreement();
    },
            "Share a crusade you made with the Tanks community!");

    Button downloadCrusade = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Browse crusades", () ->
    {
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search("Crusade", 0, 18, null, null, Game.steamNetworkHandler.workshop.searchByScore);
    },
            "Browse crusades the Tanks community has created!");


    Button myLevels = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "My uploaded creations", () ->
    {
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search(null, 0, 18, Game.steamNetworkHandler.playerID, null, Game.steamNetworkHandler.workshop.searchByScore);
    },
            "View everything you have uploaded");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenPlay());

    @Override
    public void update()
    {
        uploadLevel.update();
        downloadLevel.update();
        uploadCrusade.update();
        downloadCrusade.update();
        myLevels.update();
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
        downloadCrusade.draw();
        uploadCrusade.draw();
        myLevels.draw();
        downloadLevel.draw();
        uploadLevel.draw();
    }
}

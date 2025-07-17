package tanks.gui.screen;

import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamResult;
import com.codedisaster.steamworks.SteamUGCDetails;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenWorkshopLevelDownloadFailed extends Screen
{
    public Screen screen;

    public SteamUGCDetails workshopDetails;
    public SteamResult result;

    public int votesUp = 0;
    public int votesDown = 0;
    public boolean confirmingDelete = false;
    public boolean showDelete = false;

    public static final double votePosY = Drawing.drawing.interfaceSizeY - 125;

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580 + this.objXSpace, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = screen;
        }
    });

    public Button more = new Button(200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "More by this user", () ->
    {
        Game.cleanUp();
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search(null, 0, 18, workshopDetails.getOwnerID(), null, Game.steamNetworkHandler.workshop.searchByScore);
    });

    public Button cancelDelete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", () -> { confirmingDelete = false; });

    public Button confirmDelete = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", () ->
    {
        Game.cleanUp();
        Game.steamNetworkHandler.workshop.delete(workshopDetails, "Level");
        Game.screen = new ScreenWaiting("Removing level from server...");
    }
    );

    public Button delete = new Button(200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Remove from server", () ->
    {
        confirmingDelete = true;
    });

    public Button voteUp = new Button(Drawing.drawing.interfaceSizeX - 630 + this.objXSpace, votePosY, this.objHeight, this.objHeight, "\u00A7000200000255+", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == -1)
            votesDown--;

        votesUp++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = 1;

        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), true);
    }, "Like the level");

    public Button voteDown = new Button(Drawing.drawing.interfaceSizeX - 515 + this.objXSpace, votePosY, this.objHeight, this.objHeight, "\u00A7200000000255-", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == 1)
            votesUp--;

        votesDown++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = -1;


        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), false);
    }, "Dislike the level");

    public Button showPage = new Button(Drawing.drawing.interfaceSizeX - 435 + this.objXSpace, Drawing.drawing.interfaceSizeY - 200, this.objHeight, this.objHeight, "", () ->
    {
        Game.steamNetworkHandler.friends.friends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + Long.parseLong(workshopDetails.getPublishedFileID().toString(), 16), SteamFriends.OverlayToWebPageMode.Default);
    }, "View level page on Steam");

    public ScreenWorkshopLevelDownloadFailed(SteamResult r, SteamUGCDetails d, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.screen = s;

        voteUp.fullInfo = true;
        voteDown.fullInfo = true;

        showPage.fullInfo = true;
        showPage.imageSizeX = 30;
        showPage.imageSizeY = 30;
        showPage.image = "icons/link.png";

        if (d.getOwnerID().equals(Game.steamNetworkHandler.playerID))
            this.showDelete = true;

        this.workshopDetails = d;
        this.result = r;
    }

    @Override
    public void update()
    {
        if (confirmingDelete)
        {
            confirmDelete.update();
            cancelDelete.update();
        }
        else
        {
            this.back.update();

            if (showDelete)
                this.delete.update();

            if (workshopDetails != null)
            {
                int v = Game.steamNetworkHandler.workshop.currentDownloadVote;
                voteUp.enabled = v == -1 || v == 0;
                voteDown.enabled = v == 1 || v == 0;

                voteUp.update();
                voteDown.update();
                showPage.update();

                more.update();
            }
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (confirmingDelete)
        {
            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.drawPopup(this.centerX, this.centerY, 700, 350);

            confirmDelete.draw();
            cancelDelete.draw();

            Drawing.drawing.setColor(255, 255, 255);

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to remove the level?");
        }
        else
        {
            this.back.draw();

            Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace, "Failed to download the level!");

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, SteamResults.getMessage(result));

            if (result.equals(SteamResult.FileNotFound))
                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "The level may not have been properly uploaded.");
            else
                Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "Please try again.");

            if (showDelete)
                this.delete.draw();

            if (workshopDetails != null)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.drawConcentricPopup(Drawing.drawing.interfaceSizeX - 580 + this.objXSpace, votePosY, this.objWidth, this.objHeight * 1.75, 5, 20);

                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 700 + this.objXSpace, votePosY, "Vote:");

                Drawing.drawing.setColor(0, 200, 0);
                Drawing.drawing.drawInterfaceText(voteUp.posX + 30, votePosY, votesUp + "", false);

                Drawing.drawing.setColor(200, 0, 0);
                Drawing.drawing.drawInterfaceText(voteDown.posX + 30, votePosY, votesDown + "", false);

                voteDown.draw();
                voteUp.draw();

                showPage.draw();

                String name = Game.steamNetworkHandler.friends.knownUsernamesByID.get(this.workshopDetails.getOwnerID().getAccountID());
                if (name != null)
                    this.more.setText("More by %s", (Object) name);

                more.draw();
            }
        }
    }
}

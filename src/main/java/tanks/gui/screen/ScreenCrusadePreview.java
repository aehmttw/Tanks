package tanks.gui.screen;

import basewindow.BaseFile;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamUGCDetails;
import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.event.EventShareCrusade;

import java.io.IOException;

public class ScreenCrusadePreview extends Screen implements ICrusadePreviewScreen
{
    public Crusade crusade;

    public Screen previous;

    public double titleOffset = -this.objYSpace * 4.5;

    public boolean uploadMode;

    public TextBox crusadeName;
    public TextBox description;
    public String[] descriptionText;

    public boolean downloaded = false;

    public boolean showWaiting = false;

    public boolean showDelete = false;
    public boolean confirmingDelete = false;
    public SteamUGCDetails workshopDetails;

    public int votesUp = 0;
    public int votesDown = 0;

    protected int textOffset = 0;
    protected int levelsTextOffset = 0;

    protected double offset = 0;

    public DisplayCrusadeLevels background;

    public Button upload = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Share", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
            {
                Game.screen = ScreenPartyHost.activeScreen;
                EventShareCrusade e = new EventShareCrusade(crusade, crusade.name);
                e.clientID = Game.clientID;
                Game.eventsIn.add(e);
            }
            else if (ScreenPartyLobby.isClient)
            {
                Game.screen = new ScreenPartyLobby();
                Game.eventsOut.add(new EventShareCrusade(crusade, crusade.name));
            }
            else
            {
                showWaiting = true;
                Game.steamNetworkHandler.workshop.upload("Crusade", crusadeName.inputText, crusade.contents, description.inputText);
            }

            Game.cleanUp();
        }
    });

    public Button download = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Download", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");

            boolean success = false;
            if (!file.exists())
            {
                try
                {
                    if (file.create())
                    {
                        file.startWriting();
                        file.println(crusade.contents);
                        file.stopWriting();
                        success = true;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace(Game.logger);
                    e.printStackTrace();
                }
            }

            if (success)
            {
                download.enabled = false;
                downloaded = true;
                download.setText("Crusade downloaded!");
            }
        }
    });

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = previous;
        }
    }
    );

    public Button lookInside = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Look inside", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenCrusadeEditor(crusade, true, Game.screen);
        }
    }
    );

    public Button delete = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Remove from server", () ->
    {
        confirmingDelete = true;
    });

    public Button more = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "More by this user", () ->
    {
        Game.cleanUp();
        Game.screen = new ScreenWorkshopSearchWaiting();
        Game.steamNetworkHandler.workshop.search(null, 0, 18, workshopDetails.getOwnerID(), null, Game.steamNetworkHandler.workshop.searchByScore);
    });

    public Button cancelDelete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "No", () -> { confirmingDelete = false; });

    public Button confirmDelete = new Button(this.centerX, (int) (this.centerY), this.objWidth, this.objHeight, "Yes", () ->
    {
        Game.cleanUp();
        Game.steamNetworkHandler.workshop.delete(workshopDetails, "Crusade");
        Game.screen = new ScreenWaiting("Removing crusade from server...");
    }
    );

    public double votePosY = this.centerY + this.objYSpace * 2.75;

    public Button voteUp = new Button(this.centerX + this.objXSpace - 50, votePosY, this.objHeight, this.objHeight, "\u00A7000200000255+", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == -1)
            votesDown--;

        votesUp++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = 1;

        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), true);
    }, "Like the crusade");

    public Button voteDown = new Button(this.centerX + this.objXSpace + 65, votePosY, this.objHeight, this.objHeight, "\u00A7200000000255-", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == 1)
            votesUp--;

        votesDown++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = -1;


        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), false);
    }, "Dislike the crusade");

    public Button showPage = new Button(this.centerX + this.objXSpace - this.objWidth / 2 + this.objHeight / 2, this.centerY + this.objYSpace * 1.5, this.objHeight, this.objHeight, "", () ->
    {
        Game.steamNetworkHandler.friends.friends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + Long.parseLong(workshopDetails.getPublishedFileID().toString(), 16), SteamFriends.OverlayToWebPageMode.Default);
    }, "View crusade page on Steam");

    public ScreenCrusadePreview(Crusade c, Screen previous, boolean upload)
    {
        super(350, 40, 380, 60);

        this.previous = previous;
        this.uploadMode = upload;

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.crusade = c;

        crusadeName = new TextBox(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight,
                !uploadMode ? "Crusade save name" : "Crusade upload name", () ->
        {
            if (crusadeName.inputText.equals(""))
                crusadeName.inputText = crusadeName.previousInputText;
            updateDownloadButton();
        }
                , crusade.name.replace("_", " "));

        crusadeName.enableCaps = true;

        this.updateDownloadButton();

        voteUp.fullInfo = true;
        voteDown.fullInfo = true;

        showPage.fullInfo = true;
        showPage.imageSizeX = 30;
        showPage.imageSizeY = 30;
        showPage.image = "icons/link.png";

        description = new TextBox(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth * 2.5, this.objHeight, "Description", () -> {

        }, "");
        description.enableCaps = true;
        description.allowSpaces = true;
        description.enableSpaces = true;
        description.enablePunctuation = true;
        description.maxChars = 150;
        description.hintText = "Click to add a description...";

        if (upload)
            this.lookInside.posY = this.description.posY - this.objYSpace * 1.5;
        else
            this.lookInside.posY = this.crusadeName.posY - this.objYSpace * 1.5;

        if (Game.previewCrusades)
            this.background = new DisplayCrusadeLevels(this.crusade);
    }

    public void setOffset(double offset)
    {
        this.offset = offset;
        this.upload.posY -= offset;
        this.download.posY -= offset;
        this.quit.posY -= offset;
        this.delete.posY -= offset;
        this.more.posY -= offset;
        this.showPage.posY -= offset;
        this.voteDown.posY -= offset;
        this.voteUp.posY -= offset;
        this.votePosY -= offset;
        this.crusadeName.posY -= offset;
        this.lookInside.posY = this.centerY - this.objYSpace;
    }

    @Override
    public void update()
    {
        if (showWaiting)
        {
            Game.screen = new ScreenWaiting("Uploading crusade...");
        }

        if (confirmingDelete)
        {
            confirmDelete.update();
            cancelDelete.update();
        }
        else
        {
            quit.update();
            lookInside.update();

            if (uploadMode)
            {
                upload.update();

                if (!ScreenPartyLobby.isClient && !ScreenPartyHost.isServer)
                {
                    crusadeName.update();
                    description.update();
                }
            }
            else
            {
                crusadeName.update();
                download.update();
            }

            if (workshopDetails != null)
            {
                int v = Game.steamNetworkHandler.workshop.currentDownloadVote;
                voteUp.enabled = v == -1 || v == 0;
                voteDown.enabled = v == 1 || v == 0;

                voteUp.update();
                voteDown.update();

                showPage.update();

                if (showDelete)
                    delete.update();

                more.update();
            }
        }
    }

    @Override
    public void draw()
    {
        if (Game.previewCrusades)
        {
            this.background.draw();

            if (!Game.game.window.drawingShadow)
                Game.game.window.clearDepth();

            double width = 0.7;
            if (workshopDetails != null)
                width = 0.85;

            if (!confirmingDelete)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.drawPopup(this.centerX, this.centerY, Drawing.drawing.baseInterfaceSizeX * width, this.objYSpace * 11 - offset * 2);
                Drawing.drawing.setColor(255, 255, 255);
            }
        }
        else
            this.drawDefaultBackground();

        if (confirmingDelete)
        {
            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.drawPopup(this.centerX, this.centerY, 700, 350);

            confirmDelete.draw();
            cancelDelete.draw();

            Drawing.drawing.setColor(255, 255, 255);

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, "Are you sure you want to remove the crusade?");
        }
        else
        {
            if (Game.previewCrusades)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(this.textSize * 2);

            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY + this.textOffset - this.objYSpace * 3.5 + offset, crusade.name.replace("_", " "));

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            if (this.descriptionText != null)
            {
                double dy = this.centerY + this.textOffset - this.objYSpace * 1 + offset - (this.descriptionText.length - 1) * 0.5 * 30;
                for (int i = 0; i < this.descriptionText.length; i++)
                {
                    Drawing.drawing.drawInterfaceText(this.centerX, dy + i * 30, this.descriptionText[i]);
                }
            }

            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.textOffset + this.levelsTextOffset - this.objYSpace * 2.5 + offset, "Levels: %d", crusade.levels.size());


//            Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
//            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 1.5, crusade.name.replace("_", " "));
//            Drawing.drawing.setInterfaceFontSize(this.textSize);
//            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 0.5, "Levels: %d", crusade.levels.size());
//            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 0.5, "Starting lives: %d", crusade.startingLives);
//            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + this.objYSpace * 1, "Bonus life frequency: %d", crusade.bonusLifeFrequency);

            lookInside.draw();
            quit.draw();

//            Drawing.drawing.setInterfaceFontSize(this.titleSize);
//            Drawing.drawing.setColor(0, 0, 0);
//            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY + titleOffset, "Crusade details");

            if (uploadMode)
            {
                upload.draw();

                if (!ScreenPartyLobby.isClient && !ScreenPartyHost.isServer)
                {
                    crusadeName.draw();
                    description.draw();
                }
            }
            else
            {
                crusadeName.draw();
                download.draw();
            }

            if (workshopDetails != null)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.drawConcentricPopup(this.centerX + this.objXSpace, votePosY, this.objWidth, this.objHeight * 1.75, 5, 20);

                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(this.centerX + this.objXSpace - 120, votePosY, "Vote:");

                Drawing.drawing.setColor(0, 200, 0);
                Drawing.drawing.drawInterfaceText(voteUp.posX + 30, votePosY, votesUp + "", false);

                Drawing.drawing.setColor(200, 0, 0);
                Drawing.drawing.drawInterfaceText(voteDown.posX + 30, votePosY, votesDown + "", false);

                voteDown.draw();
                voteUp.draw();

                showPage.draw();

                if (showDelete)
                    delete.draw();

                String name = Game.steamNetworkHandler.friends.knownUsernamesByID.get(this.workshopDetails.getOwnerID().getAccountID());
                if (name != null)
                    this.more.setText("More by %s", (Object) name);

                more.draw();
            }
        }
    }

    public void updateDownloadButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.crusadeDir + "/" + crusadeName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            download.setText("Pick a different name...");
            download.enabled = false;
        }
        else
        {
            download.setText("Download");
            download.enabled = true;
        }
    }

    @Override
    public void setupLayoutParameters()
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.objYSpace = 55;
            this.centerY -= 5;
        }
    }
}

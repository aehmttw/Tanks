package tanks.gui.screen;

import basewindow.BaseFile;
import com.codedisaster.steamworks.SteamFriends;
import com.codedisaster.steamworks.SteamUGCDetails;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenSaveLevel extends Screen implements ILevelPreviewScreen
{
    public Level queuedLevel = null;

    public String level;
    public TextBox levelName;
    public String[] description = null;
    public boolean downloaded = false;

    public boolean fromInterlevel = false;

    public Screen screen;

    public DisplayLevel levelDisplay;

    public boolean showDelete = false;
    public boolean confirmingDelete = false;
    public SteamUGCDetails workshopDetails;

    public int votesUp = 0;
    public int votesDown = 0;

    public static final double votePosY = Drawing.drawing.interfaceSizeY - 125;

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            if (!fromInterlevel)
                Game.cleanUp();
            else
                Game.silentCleanUp();

            Game.screen = screen;
        }
    });

    public Button reveal = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Show level", () ->
    {
        this.queuedLevel.loadLevel(this);
        this.queuedLevel = null;
    });

    public Button download = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Download", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

            boolean success = false;
            if (!file.exists())
            {
                try
                {
                    if (file.create())
                    {
                        file.startWriting();
                        file.println(level);
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

                if (fromInterlevel)
                    download.setText("Level saved!");
                else
                    download.setText("Level downloaded!");
            }
        }
    });

    public Button quickPlay = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Quick play", () ->
    {
        ArrayList<Obstacle> obstacles = new ArrayList<>(Game.obstacles);
        Game.cleanUp();

        Level l = new Level(this.level);
        l.loadLevel();
        Obstacle.draw_size = Game.tile_size;

        Game.obstacles.clear();
        Game.obstacles.addAll(obstacles);

        for (Movable m: Game.movables)
        {
            if (m instanceof Tank)
                ((Tank) m).drawAge = 50;
        }

        ScreenInterlevel.fromQuickPlay = this;
        Game.screen = new ScreenGame();
    });

    public Button delete = new Button(200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Remove from server", () ->
    {
        confirmingDelete = true;
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

    public Button voteUp = new Button(Drawing.drawing.interfaceSizeX - 630, votePosY, this.objHeight, this.objHeight, "\u00A7000200000255+", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == -1)
            votesDown--;

        votesUp++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = 1;

        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), true);
    }, "Like the level");

    public Button voteDown = new Button(Drawing.drawing.interfaceSizeX - 515, votePosY, this.objHeight, this.objHeight, "\u00A7200000000255-", () ->
    {
        if (Game.steamNetworkHandler.workshop.currentDownloadVote == 1)
            votesUp--;

        votesDown++;
        Game.steamNetworkHandler.workshop.currentDownloadVote = -1;


        Game.steamNetworkHandler.workshop.workshop.setUserItemVote(workshopDetails.getPublishedFileID(), false);
    }, "Dislike the level");

    public Button showPage = new Button(Drawing.drawing.interfaceSizeX - 435, Drawing.drawing.interfaceSizeY - 200, this.objHeight, this.objHeight, "", () ->
    {
        Game.steamNetworkHandler.friends.friends.activateGameOverlayToWebPage("steam://url/CommunityFilePage/" + Long.parseLong(workshopDetails.getPublishedFileID().toString(), 16), SteamFriends.OverlayToWebPageMode.Default);
    }, "View level page on Steam");

    public ScreenSaveLevel(String name, String level, Screen s)
    {
        this(name, level, s, false);
    }

    public ScreenSaveLevel(String name, String level, Screen s, boolean fromInterlevel)
    {
        super(350, 40, 380, 60);

        this.fromInterlevel = fromInterlevel;
        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.screen = s;
        this.level = level;

        this.levelDisplay = new DisplayLevel();

        Obstacle.draw_size = Game.tile_size;

        levelName = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 150, this.objWidth, this.objHeight, "Level save name", () ->
        {
            if (levelName.inputText.equals(""))
                levelName.inputText = levelName.previousInputText;
            updateDownloadButton();
        }
                , name.replace("_", " "));

        levelName.enableCaps = true;

        this.updateDownloadButton();

        if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
        {
            levelName.posY += 40;
            download.posY += 40;
            back.posY += 40;
        }
        else if (ScreenPartyHost.isServer && !fromInterlevel)
        {
            quickPlay.posY -= 40;
            levelName.posY -= 60;
            download.posY -= 60;
        }

        voteUp.fullInfo = true;
        voteDown.fullInfo = true;

        showPage.fullInfo = true;
        showPage.imageSizeX = 30;
        showPage.imageSizeY = 30;
        showPage.image = "icons/link.png";
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
            this.download.update();
            this.back.update();

            if (this.queuedLevel != null)
                this.reveal.update();

            if (showDelete)
                this.delete.update();

            if (!this.downloaded)
                this.levelName.update();

            if (workshopDetails != null)
            {
                int v = Game.steamNetworkHandler.workshop.currentDownloadVote;
                voteUp.enabled = v == -1 || v == 0;
                voteDown.enabled = v == 1 || v == 0;

                voteUp.update();
                voteDown.update();

                if (Game.game.input.play.isValid())
                {
                    Game.game.input.play.invalidate();
                    quickPlay.function.run();
                }
                else
                    quickPlay.update();

                showPage.update();

                more.update();
            }

            if (ScreenPartyHost.isServer && !fromInterlevel)
                quickPlay.update();
        }
    }

    @Override
    public void draw()
    {
        this.levelDisplay.draw();

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
            this.download.draw();
            this.back.draw();

            if (this.description != null && this.description.length > 0)
                Drawing.drawing.drawTooltip(this.description, delete.posX - this.objWidth / 2, delete.posY - this.objYSpace * 1.5);

            if (this.queuedLevel != null)
            {
                Drawing.drawing.setColor(0, 0, 0);
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace, "This level is not automatically drawn because");
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace / 2, "it is large (size: %dx%d, tanks: %d) and may lag the game.", this.queuedLevel.sizeX, this.queuedLevel.sizeY, this.queuedLevel.tanks.size());
                this.reveal.draw();
            }

            if (!this.downloaded)
                this.levelName.draw();

            if (showDelete)
                this.delete.draw();

            if (workshopDetails != null)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.drawConcentricPopup(Drawing.drawing.interfaceSizeX - 580, votePosY, this.objWidth, this.objHeight * 1.75, 5, 20);

                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX - 700, votePosY, "Vote:");

                Drawing.drawing.setColor(0, 200, 0);
                Drawing.drawing.drawInterfaceText(voteUp.posX + 30, votePosY, votesUp + "", false);

                Drawing.drawing.setColor(200, 0, 0);
                Drawing.drawing.drawInterfaceText(voteDown.posX + 30, votePosY, votesDown + "", false);

                voteDown.draw();
                voteUp.draw();
                quickPlay.draw();

                showPage.draw();

                String name = Game.steamNetworkHandler.friends.knownUsernamesByID.get(this.workshopDetails.getOwnerID().getAccountID());
                if (name != null)
                    this.more.setText("More by %s", (Object) name);

                more.draw();
            }

            if (ScreenPartyHost.isServer && !fromInterlevel)
                quickPlay.draw();
        }
    }

    public void updateDownloadButton()
    {
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName.inputText.replace(" ", "_") + ".tanks");

        if (file.exists())
        {
            download.setText("Pick a different name...");
            download.enabled = false;
        }
        else
        {
            if (fromInterlevel)
                download.setText("Save");
            else
                download.setText("Download");

            download.enabled = true;
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.levelDisplay.spawns;
    }
}

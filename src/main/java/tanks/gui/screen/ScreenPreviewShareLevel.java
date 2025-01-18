package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.network.event.EventShareLevel;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenPreviewShareLevel extends Screen implements ILevelPreviewScreen
{
    public String name;
    public Level level;
    public Screen screen;
    public DisplayLevel levelDisplay;
    public boolean hideUI = false;
    public boolean showUI = false;

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            Game.screen = screen;
        }
    });


    public Button upload = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Share", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
            {
                Game.screen = ScreenPartyHost.activeScreen;
                EventShareLevel e = new EventShareLevel(level, name);
                e.clientID = Game.clientID;
                Game.eventsIn.add(e);
                Game.cleanUp();
            }
            else if (ScreenPartyLobby.isClient)
            {
                Game.screen = new ScreenPartyLobby();
                Game.eventsOut.add(new EventShareLevel(level, name));
                Game.cleanUp();
            }
            else
            {
                Game.steamNetworkHandler.workshop.upload("Level", name, level.levelString);
                hideUI = true;
                infoBar = Drawing.drawing.enableStats;
                mouseTarget = Panel.showMouseTarget;
                mouseTargetHeight = Panel.showMouseTargetHeight;
                Panel.showMouseTarget = false;
                Panel.showMouseTargetHeight = false;
                Drawing.drawing.showStats(false);
            }
        }
    });

    public TextBox levelName;

    public ScreenPreviewShareLevel(String name, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.name = name;

        this.levelDisplay = new DisplayLevel();

        Obstacle.draw_size = Game.tile_size;
        this.screen = s;

        if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
        {
            upload.posY += 40;
            back.posY += 40;
        }

        levelName = new TextBox(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 110, this.objWidth, this.objHeight, "Level upload name", () ->
        {
            if (levelName.inputText.equals(""))
                levelName.inputText = levelName.previousInputText;
            this.name = levelName.inputText;
        }
                , name.replace("_", " "));
        levelName.enableCaps = true;
    }

    @Override
    public void update()
    {
        this.back.update();
        this.upload.update();

        if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
            levelName.update();
    }

    boolean mouseTarget;
    boolean mouseTargetHeight;
    boolean infoBar;


    @Override
    public void draw()
    {
        this.levelDisplay.draw();

        if (showUI && !Game.game.window.drawingShadow)
        {
            Panel.showMouseTarget = mouseTarget;
            Panel.showMouseTargetHeight = mouseTargetHeight;
            Drawing.drawing.showStats(infoBar);
            showUI = false;
            hideUI = false;
            Game.cleanUp();
            Game.screen = new ScreenWaiting("Uploading level...");
        }

        if (!hideUI && !Game.game.window.drawingShadow)
        {
            this.back.draw();
            this.upload.draw();

            if (!ScreenPartyHost.isServer && !ScreenPartyLobby.isClient)
                levelName.draw();
        }

        if (hideUI && !Game.game.window.drawingShadow)
        {
            showUI = true;
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.levelDisplay.spawns;
    }
}

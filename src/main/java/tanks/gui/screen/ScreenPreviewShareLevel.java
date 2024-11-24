package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
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
            }
            else
            {
                Game.screen = new ScreenPartyLobby();
                Game.eventsOut.add(new EventShareLevel(level, name));
            }

            Game.cleanUp();
        }
    });

    public ScreenPreviewShareLevel(String name, Screen s)
    {
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.name = name;

        this.levelDisplay = new DisplayLevel();

        Obstacle.draw_size = Game.tile_size;
        this.screen = s;
    }

    @Override
    public void update()
    {
        this.back.update();
        this.upload.update();

        if (Game.enable3d)
            Game.recomputeHeightGrid();
    }

    @Override
    public void draw()
    {
        this.levelDisplay.draw();
        this.back.draw();
        this.upload.draw();
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.levelDisplay.spawns;
    }
}

package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.network.event.online.EventUploadLevel;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenPreviewUploadLevel extends Screen implements ILevelPreviewScreen, IOnlineScreen
{
    public String name;
    public Level level;
    public ScreenUploadLevel screen;
    public DisplayLevel levelDisplay;

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            Game.screen = screen;
        }
    });

    public Button upload = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 50, this.objWidth, this.objHeight, "Upload", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOnlineWaiting();
            Game.eventsOut.add(new EventUploadLevel(name, Game.currentLevelString));
        }
    });

    public ScreenPreviewUploadLevel(String name, ScreenUploadLevel s)
    {
        super(350, 40, 380, 60);

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

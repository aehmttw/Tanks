package tanks.tank;

import tanks.Game;
import tanks.gui.screen.ILevelPreviewScreen;

public class TankSpawnMarker extends Tank
{
    public String defaultTexture = "emblems/player_spawn.png";
    public TankSpawnMarker(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 0, 150, 255);
        this.angle = angle;
        this.orientation = angle;
        this.emblem = this.defaultTexture;
        this.emblemG = 200;
        this.emblemB = 255;
    }

    @Override
    public void draw()
    {
        if (Game.screen instanceof ILevelPreviewScreen && ((ILevelPreviewScreen) Game.screen).getSpawns().size() > 1)
            this.emblem = this.defaultTexture;
        else
            this.emblem = null;

        super.draw();
    }
}

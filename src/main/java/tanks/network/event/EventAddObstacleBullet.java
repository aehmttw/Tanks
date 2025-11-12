package tanks.network.event;

import tanks.Game;
import tanks.obstacle.*;

public class EventAddObstacleBullet extends PersonalEvent
{
    public double posX;
    public double posY;
    public double colorR;
    public double colorG;
    public double colorB;
    public boolean success;

    public EventAddObstacleBullet()
    {

    }

    public EventAddObstacleBullet(Obstacle o, boolean success)
    {
        this.posX = o.posX;
        this.posY = o.posY;
        this.colorR = o.colorR;
        this.colorG = o.colorG;
        this.colorB = o.colorB;
        this.success = success;
    }

    @Override
    public void execute()
    {
        ObstacleStackable o = new ObstacleStackable("normal", this.posX / 50 - 0.5, this.posY / 50 - 0.5);
        o.colorR = this.colorR;
        o.colorG = this.colorG;
        o.colorB = this.colorB;

        for (int i = 0; i < o.stackColorR.length; i++)
        {
            o.stackColorR[i] = this.colorR;
            o.stackColorG[i] = this.colorG;
            o.stackColorB[i] = this.colorB;
        }

        o.setUpdate(true);
        o.shouldClip = true;
        o.clipFrames = 2;

        if (success)
            Game.addObstacle(o);
        else
            o.playDestroyAnimation(this.posX, this.posY, Game.tile_size);
    }
}

package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.network.NetworkUtils;
import tanks.obstacle.ObstacleText;

public class EventAddObstacleText extends PersonalEvent
{
    public int id;

    public String text;
    public double posX;
    public double posY;
    public double colorR;
    public double colorB;
    public double colorG;
    public long duration;

    public EventAddObstacleText()
    {

    }

    /**This event adds an ObstacleText at the desired location.*/
    public EventAddObstacleText(int id, String text, double posX, double posY, double colorR, double colorG, double colorB, long duration)
    {
        this.id = id;

        this.text = text;
        this.posX = posX;
        this.posY = posY;

        this.colorR = colorR;
        this.colorG = colorG;
        this.colorB = colorB;

        this.duration = duration;
    }

    @Override
    public void execute()
    {
        String[] lines = text.split("---");
        for (int i = 0; i < lines.length; i++)
        {
            ObstacleText o = new ObstacleText("text", posX, posY + i);
            o.text = lines[i];
            o.duration = duration;
            o.colorR = this.colorR;
            o.colorG = this.colorG;
            o.colorB = this.colorB;
            Game.obstacles.add(o);
        }
    }
}

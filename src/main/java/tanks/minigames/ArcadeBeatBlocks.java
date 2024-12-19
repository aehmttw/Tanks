package tanks.minigames;

import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleBeatBlock;

import java.util.ArrayList;

public class ArcadeBeatBlocks extends Arcade
{
    protected ArrayList<Obstacle> addObstacles = new ArrayList<>();

    public int lastRampageLevel = 0;

    public ArcadeBeatBlocks()
    {
        super("{28,18,235,207,166,20,20,20,0,100,50|" +
                "0-10-beat-1#2.5,1-10-beat-1#2.0,2-10-beat-1#2.5,3-10-beat-1#2.0,4-4-beat-0#2.5,4-5-beat-0#2.0,4-6-beat-0#2.5,4-7-beat-0#2.0,4-8-beat-0#2.5,4-9-beat-0#2.0,4-10-beat-1#2.5," +
                "5-4-beat-0#2.0,5-10-beat-1#2.0,6-4-beat-0#2.5,6-10-beat-1#2.5,7-4-beat-0#2.0,7-10-beat-1#2.0,7-11-beat-1#2.5,7-12-beat-1#2.0,7-13-beat-0#2.5,8-4-beat-0#2.5,8-13-beat-0#2.0," +
                "9-4-beat-1#2.0,9-13-beat-0#2.5,10-4-beat-1#2.5,10-13-beat-0#2.0,11-4-beat-1#2.0,11-13-beat-0#2.5,12-4-beat-1#2.5,12-13-beat-0#2.0,12-14-beat-0#2.5,12-15-beat-0#2.0,12-16-beat-0#2.5," +
                "12-17-beat-0#2.0,13-4-beat-1#2.0,13-13-beat-1#2.5,14-4-beat-1#2.5,14-13-beat-1#2.0,15-0-beat-0#2.0,15-1-beat-0#2.5,15-2-beat-0#2.0,15-3-beat-0#2.5,15-4-beat-0#2.0," +
                "15-13-beat-1#2.5,16-4-beat-0#2.5,16-13-beat-1#2.0,17-4-beat-0#2.0,17-13-beat-1#2.5,18-4-beat-0#2.5,18-13-beat-1#2.0,19-4-beat-0#2.0,19-13-beat-0#2.5,20-4-beat-0#2.5," +
                "20-5-beat-1#2.0,20-6-beat-1#2.5,20-7-beat-1#2.0,20-13-beat-0#2.0,21-7-beat-1#2.5,21-13-beat-0#2.5,22-7-beat-1#2.0,22-13-beat-0#2.0,23-7-beat-1#2.5,23-8-beat-0#2.0," +
                "23-9-beat-0#2.5,23-10-beat-0#2.0,23-11-beat-0#2.5,23-12-beat-0#2.0,23-13-beat-0#2.5,24-7-beat-1#2.0,25-7-beat-1#2.5,26-7-beat-1#2.0,27-7-beat-1#2.5|10-10-player-0-ally," +
                "12-10-player-0-ally,14-10-player-0-ally,16-10-player-0-ally,18-10-player-0-ally,17-7-player-2-ally,15-7-player-2-ally,13-7-player-2-ally,11-7-player-2-ally,9-7-player-2-ally" +
                "|ally-true,enemy-true}\n");

        this.waveSize *= 2;
        this.waveTriggerCount *= 2;
    }

    public void setRampage(int value)
    {
        int prevLast = lastRampageLevel;
        lastRampageLevel = value;

        super.setRampage(value);

        if (prevLast == 0 && value == 0)
            return;

        if (prevLast / 2 == value / 2)
            return;

        value = Math.min(6, value);
        this.beatBlocks = (int) Math.pow(2, value / 2);

        for (Obstacle o : Game.obstacles)
        {
            if (o instanceof ObstacleBeatBlock)
            {
                Obstacle o1 = new ObstacleBeatBlock(o.name, (int) (o.posX / Game.tile_size), (int) (o.posY / Game.tile_size));
                o1.setMetadata(((ObstacleBeatBlock)o).beatPattern % 2 + 2 * (value / 2) + "#" + ((ObstacleBeatBlock)o).stackHeight);
                Game.removeObstacles.add(o);
                addObstacles.add(o1);
            }
        }

        for (Obstacle o : addObstacles)
        {
            Game.addObstacle(o);
        }
        addObstacles.clear();
    }

}

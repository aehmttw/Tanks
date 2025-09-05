package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Effect;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleShrubbery;

public class EventObstacleShrubberyBurn extends PersonalEvent
{
    public double posX;
    public double posY;

    public EventObstacleShrubberyBurn()
    {

    }

    public EventObstacleShrubberyBurn(double x, double y)
    {
        this.posX = x;
        this.posY = y;
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
            return;

        for (int i = 0; i < Game.obstacles.size(); i++)
        {
            Obstacle o = Game.obstacles.get(i);
            if (o instanceof ObstacleShrubbery && o.posX == this.posX && o.posY == this.posY)
            {
                Game.removeObstacles.add(o);

                Effect e;
                if (Game.enable3d)
                    e = (Effect.createNewEffect(this.posX, this.posY,
                            Obstacle.draw_size * (0.25 + 0.75 * ((ObstacleShrubbery) o).heightMultiplier * (1 - (255 - ((ObstacleShrubbery) o).height) / 128)),
                            Effect.EffectType.bushBurn));
                else
                    e = (Effect.createNewEffect(this.posX, this.posY, ((ObstacleShrubbery) o).height, Effect.EffectType.bushBurn));

                e.setColor(o.colorR, o.colorG, o.colorB);

                Game.effects.add(e);
            }
        }
    }
}

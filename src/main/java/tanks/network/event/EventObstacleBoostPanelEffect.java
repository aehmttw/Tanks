package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Movable;
import tanks.bullet.Bullet;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleBoostPanel;
import tanks.tank.Tank;

public class EventObstacleBoostPanelEffect extends PersonalEvent
{
    public boolean isTank;
    public int networkID;
    public double posX;
    public double posY;

    public EventObstacleBoostPanelEffect(Movable m, Obstacle o)
    {
        this.posX = o.posX;
        this.posY = o.posY;

        if (m instanceof Tank)
        {
            this.networkID = ((Tank) m).networkID;
            this.isTank = true;
        }
        else
        {
            this.networkID = ((Bullet) m).networkID;
            this.isTank = false;
        }
    }

    public EventObstacleBoostPanelEffect()
    {

    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeBoolean(this.isTank);
        b.writeInt(this.networkID);
        b.writeDouble(this.posX);
        b.writeDouble(this.posY);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.isTank = b.readBoolean();
        this.networkID = b.readInt();
        this.posX = b.readDouble();
        this.posY = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (clientID != null)
            return;

        Movable m;

        if (isTank)
            m = Tank.idMap.get(this.networkID);
        else
            m = Bullet.idMap.get(this.networkID);

        if (m == null)
            return;

        Obstacle o = Game.getObstacle(this.posX, this.posY);
        if (o instanceof ObstacleBoostPanel)
            ((ObstacleBoostPanel) o).addEntryEffect(m);
    }
}

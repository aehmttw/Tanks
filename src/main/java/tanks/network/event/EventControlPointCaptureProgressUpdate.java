package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.minigames.controlpoint.ControlPoint;
import tanks.minigames.controlpoint.GamePoint;
import tanks.network.NetworkUtils;

public class EventControlPointCaptureProgressUpdate extends PersonalEvent
{
    public String pointId;
    public double captureProgress;

    public EventControlPointCaptureProgressUpdate() {}

    public EventControlPointCaptureProgressUpdate(String pointId, double captureProgress)
    {
        this.pointId = pointId;
        this.captureProgress = captureProgress;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, pointId);
        b.writeDouble(captureProgress);
    }

    @Override
    public void read(ByteBuf b)
    {
        pointId = NetworkUtils.readString(b);
        captureProgress = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (clientID == null && Game.currentLevel instanceof ControlPoint)
        {
            ControlPoint cp = (ControlPoint) Game.currentLevel;
            for (GamePoint point : cp.getPoints())
            {
                if (point.getId().equals(pointId))
                {
                    point.setCaptureProgress(captureProgress);
                }
            }
        }
    }
}

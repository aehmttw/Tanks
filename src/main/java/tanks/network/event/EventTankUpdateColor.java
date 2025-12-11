package tanks.network.event;

import basewindow.Color;
import tanks.tank.Tank;

public class EventTankUpdateColor extends PersonalEvent implements IStackableEvent
{
    public int tank;

    public Color color1 = new Color();
    public Color color2 = new Color();
    public Color color3 = new Color();
    public boolean tertiaryColor;

    public EventTankUpdateColor()
    {

    }

    public EventTankUpdateColor(Tank t)
    {
        tank = t.networkID;

        color1.set(t.color);
        color2.set(t.secondaryColor);
        color3.set(t.tertiaryColor);

        tertiaryColor = t.enableTertiaryColor;
    }

    @Override
    public void execute()
    {
        Tank t = Tank.idMap.get(tank);

        if (t == null || this.clientID != null)
            return;

        t.color.set(this.color1);
        t.secondaryColor.set(this.color2);
        t.tertiaryColor.set(this.color3);

        t.enableTertiaryColor = tertiaryColor;
    }

    @Override
    public int getIdentifier()
    {
        return this.tank;
    }
}

package tanks.item.legacy;

import tanks.item.legacy.Item;
import tanks.tank.Tank;

public class ItemRemote extends Item
{
    public int bounces;
    public double range;

    @Override
    public boolean usable(Tank t)
    {
        return false;
    }

    @Override
    public void use(Tank t)
    {

    }

    @Override
    public void fromString(String s)
    {

    }

    @Override
    public String getTypeName()
    {
        return "Item";
    }
}

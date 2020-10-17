package tanks.hotbar.item;

public class ItemRemote extends Item
{
    public int bounces;

    @Override
    public boolean usable()
    {
        return false;
    }

    @Override
    public void use()
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

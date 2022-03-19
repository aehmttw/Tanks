package tanks.modapi.menus;

import tanks.IDrawable;
import tanks.modapi.ModAPI;

public abstract class FixedMenu implements IDrawable
{
    public double id = Math.random() * Double.MAX_VALUE;

    public double posX = 0;
    public double posY = 0;
    public double posZ = 0;
    public double sizeX = 300;
    public double sizeY = 300;
    public double sizeZ = 1;
    public int duration = 0;
    public boolean afterGameStarted = false;
    public int drawLevel = 3;

    public FixedMenu()
    {
        ModAPI.ids.put(this.id, this);
    }

    public abstract void draw();
    public abstract void update();
}

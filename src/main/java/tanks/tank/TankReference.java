package tanks.tank;

import tanks.Game;
import tanks.tankson.Property;
import tanks.tankson.TanksONable;

@TanksONable("tank_ref")
public class TankReference implements ITankField
{
    @Property(id = "tank")
    public String tankName;

    public TankReference(String name)
    {
        this.tankName = name;
    }

    @Override
    public Tank resolve()
    {
        return Game.currentLevel.lookupTank(this.tankName);
    }

    @Override
    public String getName()
    {
        return this.tankName;
    }
}

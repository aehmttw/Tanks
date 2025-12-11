package tanks.network.event;

import tanks.*;
import tanks.tank.Tank;

public class EventTankUpdateHealth extends PersonalEvent implements IStackableEvent
{
	public int tank;
	public double health;
	
	public EventTankUpdateHealth()
	{
		
	}
	
	public EventTankUpdateHealth(Tank t)
	{
		tank = t.networkID;
		health = t.health;
	}
	
	@Override
	public void execute() 
	{
		Tank t = Tank.idMap.get(tank);

		if (t == null || this.clientID != null)
			return;

		if (t.health > health && health > 0)
			t.damageFlashAnimation = 1;
		else if (health > t.health)
			t.healFlashAnimation = 1;

		double before = t.health;
		t.health = health;
		t.addDamageEffect(before);

		if (t.health <= 0)
		{
			t.vX = 0;
			t.vY = 0;
		}
	}

    @Override
    public int getIdentifier()
    {
        return tank;
    }
}

package tanks.event;

import tanks.Game;
import tanks.Mine;
import tanks.tank.Tank;

public class EventLayMine implements INetworkEvent
{
	public Mine mine;
	
	public EventLayMine(Mine m)
	{
		mine = m;
	}
	
	public EventLayMine(String s)
	{
		String[] parts = s.split(",");
		this.mine = new Mine(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Tank.idMap.get(Integer.parseInt(parts[0])));
		this.mine.timer = Double.parseDouble(parts[3]);
		this.mine.radius = Double.parseDouble(parts[4]);
	}
	
	@Override
	public String getNetworkString()
	{
		return mine.tank.networkID + "," + mine.posX + "," + mine.posY + "," + mine.timer + "," + mine.radius;
	}

	@Override
	public void execute() 
	{
		Game.movables.add(this.mine);
	}
}

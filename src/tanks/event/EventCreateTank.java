package tanks.event;

import tanks.Game;
import tanks.Team;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventCreateTank implements INetworkEvent
{
	public Tank tank;
	
	public EventCreateTank(Tank tank)
	{
		this.tank = tank;
	}
	
	public EventCreateTank(String s)
	{
		String[] parts2 = s.split(",");
		
		String type = parts2[0];

		double posX = Double.parseDouble(parts2[1]);
		double posY = Double.parseDouble(parts2[2]);
		double angle = Double.parseDouble(parts2[3]);
		
		Team team;
		if (parts2.length >= 5)
			team = Game.currentLevel.teamsMap.get(parts2[4]);
		else
			team = Game.enemyTeam;
		
		this.tank = Game.registryTank.getEntry(type).getTank(posX, posY, angle);
		this.tank.team = team;

	}

	@Override
	public String getNetworkString() 
	{
		return tank.name + "," + tank.posX + "," + tank.posY + "," + tank.angle + "," + tank.team.name;
	}
	
	@Override
	public void execute()
	{
		Game.movables.add(new TankRemote(this.tank));
	}
}

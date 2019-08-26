package tanks.event;

import tanks.Game;
import tanks.Team;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventCreateCustomTank implements INetworkEvent
{
	public Tank tank;
	
	public EventCreateCustomTank(Tank tank)
	{
		this.tank = tank;
	}
	
	public EventCreateCustomTank(String s)
	{
		String[] parts2 = s.split(",");
		
		String type = parts2[0];

		double posX = Double.parseDouble(parts2[1]);
		double posY = Double.parseDouble(parts2[2]);
		double angle = Double.parseDouble(parts2[3]);
		Team team = Game.currentLevel.teamsMap.get(parts2[4]);
		double size = Double.parseDouble(parts2[5]);
		double ts = Double.parseDouble(parts2[6]);
		double tl = Double.parseDouble(parts2[7]);
		double r = Double.parseDouble(parts2[8]);
		double g = Double.parseDouble(parts2[9]);
		double b = Double.parseDouble(parts2[10]);
		double lives = Double.parseDouble(parts2[11]);
		double baseLives = Double.parseDouble(parts2[12]);
		
		this.tank = new TankRemote(type, posX, posY, angle, team, size, ts, tl, r, g, b, lives, baseLives);

	}

	@Override
	public String getNetworkString() 
	{
		return tank.name + "," + tank.posX + "," + tank.posY + "," + tank.angle + "," + tank.team.name + ","
				+ tank.size + "," + tank.turret.size + "," + tank.turret.length + "," 
				+ tank.colorR + "," + tank.colorG + "," + tank.colorB + "," + tank.lives + "," + tank.baseLives;
	}
	
	@Override
	public void execute()
	{
		Game.movables.add(this.tank);
	}
}

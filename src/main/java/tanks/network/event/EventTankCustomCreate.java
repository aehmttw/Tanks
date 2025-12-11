package tanks.network.event;

import basewindow.Color;
import tanks.*;
import tanks.tank.*;

public class EventTankCustomCreate extends PersonalEvent
{
	public String name;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	public double size;
	public double turretSize;
	public double turretLength;
	public Color color = new Color();
	public double lives;
	public double baseLives;

	public int id;

	public EventTankCustomCreate()
	{
	
	}
	
	public EventTankCustomCreate(Tank t)
	{
		this.name = t.name;
		this.posX = t.posX;
		this.posY = t.posY;
		this.angle = t.angle;
		
		if (t.team == null)
			this.team = "*";
		else if (t.team == Game.enemyTeam)
			this.team = "**";
		else
			this.team = t.team.name;
		
		this.size = t.size;
		this.turretSize = t.turretSize;
		this.turretLength = t.turretLength;
		this.color.set(t.color);
		this.lives = t.health;
		this.baseLives = t.baseHealth;

		this.id = t.networkID;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Team t = Game.currentLevel.teamsMap.get(team);
		
		if (this.team.equals("**"))
			t = Game.enemyTeam;

		TankRemote tank = new TankRemote(name, posX, posY, angle, t, size, turretSize, turretLength, color.red, color.green, color.blue, lives, baseLives);
		tank.setNetworkID(this.id);

		Game.movables.add(tank);
	}
}

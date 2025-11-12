package tanks.network.event;

import tanks.*;
import tanks.tank.*;

public class EventTankCreate extends PersonalEvent
{
	public String type;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	public int id;
	public double drawAge;
	
	public EventTankCreate()
	{
		
	}
	
	public EventTankCreate(Tank t)
	{
		this.type = t.name;
		this.posX = t.posX;
		this.posY = t.posY;
		this.angle = t.angle;
		this.id = t.networkID;
		this.drawAge = t.drawAge;

		if (t.team == null)
			this.team = "*";
		else if (t.team == Game.enemyTeam)
			this.team = "**";
		else
			this.team = t.team.name;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Tank t = Game.registryTank.getEntry(type).getTank(posX, posY, angle);

		Team tm = Game.currentLevel.teamsMap.get(team);
		
		if (this.team.equals("**"))
			tm = Game.enemyTeam;
		
		t.team = tm;
		t.setNetworkID(id);
		t.drawAge = drawAge;
				
		Game.movables.add(new TankRemote(t));
	}
}

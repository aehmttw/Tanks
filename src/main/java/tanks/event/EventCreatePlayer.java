package tanks.event;

import java.util.UUID;

import tanks.Game;
import tanks.Team;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankRemote;

public class EventCreatePlayer implements INetworkEvent
{
	public UUID clientID;
	public String username;
	public double posX;
	public double posY;
	public double angle;
	public Team team;
	
	public EventCreatePlayer(UUID id, String username, double x, double y, double angle, Team t)
	{
		this.clientID = id;
		this.posX = x;
		this.posY = y;
		this.angle = angle;
		this.team = t;
		this.username = username;
	}
	
	public EventCreatePlayer(String s)
	{
		String[] parts2 = s.split(",");
		posX = Double.parseDouble(parts2[2]);
		posY = Double.parseDouble(parts2[3]);
		angle = Double.parseDouble(parts2[4]);
		
		clientID = UUID.fromString(parts2[0]);
		username = parts2[1];

		if (parts2.length >= 5)
			team = Game.currentLevel.teamsMap.get(parts2[5]);
		else
			team = Game.playerTeam;
	}

	@Override
	public String getNetworkString() 
	{
		String t = "*";
		if (team != null)
			t = team.name;
		
		return clientID.toString() + "," + username + "," + posX + "," + posY + "," + angle + "," + t;
	}
	
	@Override
	public void execute()
	{
		Tank t;
		
		if (clientID.equals(Game.clientID))
		{
			t = new TankPlayer(posX, posY, angle, clientID);
			Game.player = (TankPlayer) t;
		}
		else
		{
			t = new TankRemote(new TankPlayer(posX, posY, angle, clientID));
			t.showName = true;
		}
		
		t.name = this.username;
		
		if (Game.enableChatFilter)
			t.name = Game.chatFilter.filterChat(t.name);
		
		t.team = team;

		Game.movables.add(t);
	}
}

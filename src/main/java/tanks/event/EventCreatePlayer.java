package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankRemote;

import java.util.UUID;

public class EventCreatePlayer implements INetworkEvent
{
	public UUID clientId;
	public String username;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	
	public EventCreatePlayer()
	{
	
	}
	
	public EventCreatePlayer(UUID id, String username, double x, double y, double angle, Team t)
	{
		this.clientId = id;
		this.posX = x;
		this.posY = y;
		this.angle = angle;
		
		if (t == null)
			this.team = "*";
		else if (t == Game.playerTeam)
			this.team = "**";
		else
			this.team = t.name;
		
		this.username = username;
	}
	
	
	@Override
	public void execute()
	{
		Tank t;
		
		if (clientId.equals(Game.clientID))
		{
			t = new TankPlayer(posX, posY, angle, clientId);
			Game.player = (TankPlayer) t;
		}
		else
		{
			t = new TankRemote(new TankPlayer(posX, posY, angle, clientId));
			t.showName = true;
		}
		
		t.name = this.username;
		t.nameTag.name = this.username;
		
		if (Game.enableChatFilter)
			t.name = Game.chatFilter.filterChat(t.name);
		
		if (team.equals("**"))
			t.team = Game.playerTeam;
		else if (team.equals("*"))
			t.team = null;
		else
			t.team = Game.currentLevel.teamsMap.get(team);

		Game.movables.add(t);
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.clientId.toString());
		NetworkUtils.writeString(b, this.username);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.angle);
		NetworkUtils.writeString(b, this.team);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.clientId = UUID.fromString(NetworkUtils.readString(b));
		this.username = NetworkUtils.readString(b);
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.angle = b.readDouble();
		this.team = NetworkUtils.readString(b);
	}
}

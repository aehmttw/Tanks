package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.Team;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.NetworkUtils;
import tanks.tank.*;

import java.util.UUID;

public class EventCreatePlayer extends PersonalEvent
{
	public Player player;

	public UUID clientIdTarget;
	public String username;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	
	public EventCreatePlayer()
	{
	
	}
	
	public EventCreatePlayer(Player p, double x, double y, double angle, Team t)
	{
		this.player = p;
		this.clientIdTarget = p.clientID;
		this.posX = x;
		this.posY = y;
		this.angle = angle;
		
		if (t == null)
			this.team = "*";
		else if (t == Game.playerTeam)
			this.team = "**";
		else
			this.team = t.name;
		
		this.username = p.username;
	}
	
	@Override
	public void execute()
	{
		Tank t;

		if (this.clientID != null)
			return;

		if (ScreenPartyHost.isServer)
			ScreenPartyHost.includedPlayers.add(this.clientIdTarget);
		else
			ScreenPartyLobby.includedPlayers.add(this.clientIdTarget);

		if (clientIdTarget.equals(Game.clientID))
		{
			if (ScreenPartyHost.isServer)
				t = new TankPlayer(posX, posY, angle);
			else
				t = new TankPlayerController(posX, posY, angle, clientIdTarget);

			Game.playerTank = t;
		}
		else
		{
			if (!ScreenPartyHost.isServer)
			{
				TankPlayer t2 = new TankPlayer(posX, posY, angle);
				t2.player = new Player(clientIdTarget, "");
				t = new TankRemote(t2);
			}
			else
				t = new TankPlayerRemote(posX, posY, angle, this.player);

			t.showName = true;
			t.nameTag.name = this.username;

			if (Game.enableChatFilter)
				t.nameTag.name = Game.chatFilter.filterChat(t.nameTag.name);
		}
		
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
		NetworkUtils.writeString(b, this.clientIdTarget.toString());
		NetworkUtils.writeString(b, this.username);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.angle);
		NetworkUtils.writeString(b, this.team);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.clientIdTarget = UUID.fromString(NetworkUtils.readString(b));
		this.username = NetworkUtils.readString(b);
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.angle = b.readDouble();
		this.team = NetworkUtils.readString(b);
	}
}

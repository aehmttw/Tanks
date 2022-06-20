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

	public int colorR;
	public int colorG;
	public int colorB;

	public int colorR2;
	public int colorG2;
	public int colorB2;

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

		this.colorR = p.colorR;
		this.colorG = p.colorG;
		this.colorB = p.colorB;

		this.colorR2 = p.turretColorR;
		this.colorG2 = p.turretColorG;
		this.colorB2 = p.turretColorB;
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
			{
				t = new TankPlayerController(posX, posY, angle, clientIdTarget);
				t.registerNetworkID();
			}

			Game.playerTank = t;
		}
		else
		{
			if (!ScreenPartyHost.isServer)
			{
				TankPlayer t2 = new TankPlayer(posX, posY, angle);
				t2.player = new Player(clientIdTarget, "");
				t2.registerNetworkID();
				t = new TankRemote(t2);
			}
			else
			{
				t = new TankPlayerRemote(posX, posY, angle, this.player);
				((TankPlayerRemote) t).refreshAmmo();
			}

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

		t.colorR = this.colorR;
		t.colorG = this.colorG;
		t.colorB = this.colorB;

		t.secondaryColorR = this.colorR2;
		t.nameTag.colorR = this.colorR2;

		t.secondaryColorG = this.colorG2;
		t.nameTag.colorG = this.colorG2;

		t.secondaryColorB = this.colorB2;
		t.nameTag.colorB = this.colorB2;

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

		b.writeInt(this.colorR);
		b.writeInt(this.colorG);
		b.writeInt(this.colorB);
		b.writeInt(this.colorR2);
		b.writeInt(this.colorG2);
		b.writeInt(this.colorB2);
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

		this.colorR = b.readInt();
		this.colorG = b.readInt();
		this.colorB = b.readInt();
		this.colorR2 = b.readInt();
		this.colorG2 = b.readInt();
		this.colorB2 = b.readInt();
	}
}

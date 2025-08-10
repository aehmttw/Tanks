package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.Player;
import tanks.Team;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.minigames.Arcade;
import tanks.minigames.RampageTrial;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;
import tanks.tank.*;

import java.util.UUID;

public class EventTankPlayerCreate extends PersonalEvent
{
	public Player player;

	public UUID clientIdTarget;
	public String username;
	public double posX;
	public double posY;
	public double angle;
	public String team;

	public Color color = new Color();
	public Color color2 = new Color();
	public Color color3 = new Color();

	public int networkID;

	public double drawAge;

	public EventTankPlayerCreate()
	{
	
	}
	
	public EventTankPlayerCreate(Player p, double x, double y, double angle, Team t, int networkID, double drawAge)
	{
		this.player = p;
		this.clientIdTarget = p.clientID;
		this.posX = x;
		this.posY = y;
		this.angle = angle;
		this.networkID = networkID;
		this.drawAge = drawAge;

		if (t == null)
			this.team = "*";
		else if (t == Game.playerTeam)
			this.team = "**";
		else if (t == Game.playerTeamNoFF)
			this.team = "***";
		else
			this.team = t.name;

		this.username = p.username;

		this.color.set(p.color);
		this.color2.set(p.color2);
		this.color3.set(p.color3);
	}
	
	@Override
	public void execute()
	{
		Tank t;

		if (this.clientID != null)
			return;

		if (ScreenPartyHost.isServer)
			ScreenPartyHost.includedPlayers.add(this.clientIdTarget);
		else if (ScreenPartyLobby.isClient)
			ScreenPartyLobby.includedPlayers.add(this.clientIdTarget);

		if (player != null && player.isBot)
		{
			t = new TankPlayerBot(posX, posY, angle, player);
			Game.currentLevel.playerBuilds.get(0).clonePropertiesTo((TankPlayerBot) t);
		}
		else if (clientIdTarget.equals(Game.clientID))
		{
			if (!ScreenPartyLobby.isClient)
				t = new TankPlayer(posX, posY, angle);
			else
				t = new TankPlayerController(posX, posY, angle, clientIdTarget);

			Game.playerTank = (TankPlayer) t;
			Game.player.tank = t;

			if (!Crusade.crusadeMode)
				Game.player.buildName = Game.currentLevel.playerBuilds.get(0).name;
		}
		else
		{
			if (ScreenPartyLobby.isClient)
			{
				TankPlayer t2 = new TankPlayer(posX, posY, angle);
				t2.player = new Player(clientIdTarget, "");
				setColor(t2);
				t2.saveColors();
				Game.currentLevel.playerBuilds.get(0).clonePropertiesTo(t2);
				t = new TankRemote(t2);
			}
			else
			{
				t = new TankPlayerRemote(posX, posY, angle, this.player);
				((TankPlayerRemote) t).refreshAmmo();
			}

            t.hasName = true;
			t.showName = true;
			t.nameTag.name = this.username;

			if (Game.enableChatFilter)
				t.nameTag.name = Game.chatFilter.filterChat(t.nameTag.name);
		}

		setColor(t);

		if (t instanceof TankPlayable)
			Game.currentLevel.playerBuilds.get(0).clonePropertiesTo((TankPlayable) t);

		if (team.equals("**"))
			t.team = Game.playerTeam;
		else if (team.equals("***"))
			t.team = Game.playerTeamNoFF;
		else if (team.equals("*"))
			t.team = null;
		else
			t.team = Game.currentLevel.teamsMap.get(team);

		if (player != null)
			player.tank = t;

		if (ScreenPartyLobby.isClient)
		{
			for (ConnectedPlayer c: ScreenPartyLobby.connections)
			{
				if (c.clientId.equals(clientIdTarget))
				{
					if (t.team != null && t.team.enableColor)
						c.teamColor.set(t.team.teamColor);
					else
						c.teamColor.set(255, 255, 255);
				}
			}
		}

		t.drawAge = this.drawAge;

		t.setNetworkID(this.networkID);

		if ((Game.currentLevel instanceof Arcade || Game.currentLevel instanceof RampageTrial) && Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).playing)
			t.invulnerabilityTimer = 250;

		Game.addMovable(t);
	}

	public void setColor(Tank t)
	{
		t.color.set(this.color);
		t.secondaryColor.set(this.color2);
		t.tertiaryColor.set(this.color3);
		t.emblemColor.set(this.color2);

		if (t instanceof TankPlayable)
			((TankPlayable) t).saveColors();
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
		b.writeInt(this.networkID);

		NetworkUtils.writeColor(b, this.color);
		NetworkUtils.writeColor(b, this.color2);
		NetworkUtils.writeColor(b, this.color3);

		b.writeDouble(this.drawAge);
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
		this.networkID = b.readInt();

		NetworkUtils.readColor(b, this.color);
		NetworkUtils.readColor(b, this.color2);
		NetworkUtils.readColor(b, this.color3);

		this.drawAge = b.readDouble();
	}
}

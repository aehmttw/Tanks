package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.Player;
import tanks.gui.ChatMessage;
import tanks.gui.screen.IPartyGameScreen;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;
import tanks.network.ServerHandler;

import java.util.UUID;

public class EventSendClientDetails extends PersonalEvent implements IServerThreadEvent
{
	public int version;
	public UUID clientID;
	public String username;
	
	public EventSendClientDetails()
	{
		
	}
	
	public EventSendClientDetails(int version, UUID clientID, String username)
	{
		this.version = version;
		this.clientID = clientID;
		this.username = username;
	}
	
	@Override
	public void write(ByteBuf b) 
	{
		b.writeInt(this.version);
		NetworkUtils.writeString(b, clientID.toString());
		NetworkUtils.writeString(b, username);
	}
	
	@Override
	public void read(ByteBuf b) 
	{
		this.version = b.readInt();
		this.clientID = UUID.fromString(NetworkUtils.readString(b));
		this.username = NetworkUtils.readString(b);
	}
	
	@Override
	public void execute()
	{
		
	}
	
	@Override
	public void execute(ServerHandler s)
	{
		if (this.clientID == null || Game.isOnlineServer || !ScreenPartyHost.isServer)
			return;

		if (Game.screen instanceof IPartyGameScreen)
		{
			s.sendEventAndClose(new EventKick("Please wait for the current game to finish!"));
			return;
		}


		if (this.version != Game.network_protocol)
		{
			s.sendEventAndClose(new EventKick("You must be using " + Game.version + " to join this party!"));
			return;
		}

		if (Game.usernameInvalid(this.username) || this.username.equals(""))
		{
			s.sendEventAndClose(new EventKick("Invalid username!"));
			return;
		}
		
		s.clientID = this.clientID;
	
		if (Game.enableChatFilter)
			s.username = Game.chatFilter.filterChat(this.username);
		else
			s.username = this.username;
		
		s.rawUsername = this.username;

		synchronized (s.server.connections)
		{
			for (int i = 0; i < s.server.connections.size(); i++)
			{
				if (this.clientID.equals(s.server.connections.get(i).clientID))
				{
					s.sendEventAndClose(new EventKick("You are already in this party!"));
					return;
				}
			}
			
			s.server.connections.add(s);
		}

		synchronized (ScreenPartyHost.disconnectedPlayers)
		{
			for (int i = 0; i < ScreenPartyHost.disconnectedPlayers.size(); i++)
			{
				if (ScreenPartyHost.disconnectedPlayers.get(i).equals(this.clientID))
				{
					ScreenPartyHost.disconnectedPlayers.remove(i);
					i--;
				}
			}
		}

		Player p = new Player(this.clientID, this.username);
		Game.players.add(p);
		s.player = p;

		s.sendEvent(new EventConnectionSuccess());
		s.sendEvent(new EventAnnounceConnection(new ConnectedPlayer(Game.clientID, Game.player.username), true));

		if (Crusade.currentCrusade != null)
			s.sendEvent(new EventBeginCrusade());

		Game.eventsIn.add(new EventPlaySound("join.ogg", 1.0f, 1.0f));
		
		ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + s.username + " has joined the party\u00A7000000000255"));

		for (ScreenPartyHost.SharedLevel l: ScreenPartyHost.activeScreen.sharedLevels)
		{
			EventShareLevel e = new EventShareLevel();
			e.username = l.creator;
			e.name = l.name;
			e.level = l.level;
			s.sendEvent(e);
		}

		Game.eventsOut.add(new EventChat("\u00A7000127255255" + this.username + " has joined the party\u00A7000000000255"));
		
		for (int i = 0; i < s.server.connections.size(); i++)
		{
			ServerHandler h = s.server.connections.get(i);

			if (h != s)
			{
				s.sendEvent(new EventAnnounceConnection(new ConnectedPlayer(h.clientID, h.rawUsername), true));
			}
		}

		Game.eventsOut.add(new EventAnnounceConnection(new ConnectedPlayer(s.clientID, s.rawUsername), true));
		Game.eventsOut.add(new EventPlaySound("join.ogg", 1.0f, 1.0f));
	}
}

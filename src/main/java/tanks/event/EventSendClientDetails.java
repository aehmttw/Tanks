package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.ChatMessage;
import tanks.gui.screen.IPartyMenuScreen;
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
		if (this.clientID == null)
			return;

		if (!(Game.screen instanceof IPartyMenuScreen))
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
		
		s.sendEvent(new EventConnectionSuccess());
		
		s.sendEvent(new EventAnnounceConnection(new ConnectedPlayer(Game.clientID, Game.username), true));
		
		ScreenPartyHost.chat.add(0, new ChatMessage("\u00A7000127255255" + s.username + " has joined the party\u00A7000000000255"));

		Game.eventsOut.add(new EventChat("\u00A7000127255255" + this.username + " has joined the party\u00A7000000000255"));
		
		for (int i = 0; i < s.server.connections.size(); i++)
		{
			ServerHandler h = s.server.connections.get(i);
			s.sendEvent(new EventAnnounceConnection(new ConnectedPlayer(h.clientID, h.rawUsername), true));
		}
	}
}

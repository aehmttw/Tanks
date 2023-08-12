package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.ConnectedPlayer;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventAnnounceConnection extends PersonalEvent
{
	public String name;
	public UUID clientIdTarget;
	public boolean joined;

	public EventAnnounceConnection()
	{
		
	}
	
	public EventAnnounceConnection(ConnectedPlayer p, boolean joined)
	{
		this.name = p.rawUsername;
		this.clientIdTarget = p.clientId;
		this.joined = joined;
	}

	@Override
	public void execute() 
	{
		if (this.clientID != null)
			return;

		if (this.joined)
		{
			ScreenPartyLobby.connections.add(new ConnectedPlayer(this.clientIdTarget, this.name));
		}
		else
		{
			ScreenPartyLobby.includedPlayers.remove(this.clientIdTarget);

			for (int i = 0; i < ScreenPartyLobby.connections.size(); i++)
			{
				if (ScreenPartyLobby.connections.get(i).clientId.equals(this.clientIdTarget))
				{
					ScreenPartyLobby.connections.remove(i);
					i--;
				}
			}
		}
	}

	@Override
	public void read(ByteBuf b)
	{
		this.joined = b.readBoolean();
		this.clientIdTarget = UUID.fromString(NetworkUtils.readString(b));
		this.name = NetworkUtils.readString(b);
	}
	
	@Override
	public void write(ByteBuf b)
	{
		b.writeBoolean(this.joined);
		NetworkUtils.writeString(b, this.clientIdTarget.toString());
		NetworkUtils.writeString(b, this.name);
	}

}

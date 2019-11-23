package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.gui.screen.ScreenKicked;
import tanks.network.NetworkUtils;

public class EventKick extends PersonalEvent
{	
	public String reason;
	
	public EventKick()
	{
		
	}
	
	public EventKick(String reason)
	{
		this.reason = reason;
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.cleanUp();
			Game.screen = new ScreenKicked(reason);
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		NetworkUtils.writeString(b, this.reason);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.reason = NetworkUtils.readString(b);
	}
}

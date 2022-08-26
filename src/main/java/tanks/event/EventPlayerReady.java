package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;

public class EventPlayerReady extends PersonalEvent
{
	public EventPlayerReady()
	{

	}

	@Override
	public void execute() 
	{
		if (!ScreenPartyHost.includedPlayers.contains(this.clientID))
			return;

		Player pl = null;

		for (Player p: Game.players)
		{
			if (p.clientID.equals(this.clientID))
			{
				pl = p;
			}
		}

		if (pl != null)
		{
			if (!ScreenPartyHost.readyPlayers.contains(pl))
				ScreenPartyHost.readyPlayers.add(pl);

			Game.eventsOut.add(new EventUpdateReadyPlayers(ScreenPartyHost.readyPlayers));

			if (ScreenPartyHost.readyPlayers.size() >= ScreenPartyHost.includedPlayers.size() && Game.screen instanceof ScreenGame)
			{
				Game.eventsOut.add(new EventBeginLevelCountdown());
				((ScreenGame) Game.screen).cancelCountdown = false;
			}
		}
	}

	@Override
	public void write(ByteBuf b)
	{
		
	}

	@Override
	public void read(ByteBuf b)
	{
		
	}

}

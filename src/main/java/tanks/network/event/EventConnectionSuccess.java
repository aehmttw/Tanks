package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.ChatBox;
import tanks.gui.screen.ScreenPartyLobby;

public class EventConnectionSuccess extends PersonalEvent
{	
	public EventConnectionSuccess()
	{
		
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.screen = new ScreenPartyLobby();
            ScreenPartyLobby.chatbox = new ChatBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.getInterfaceEdgeY(true) - 30, Drawing.drawing.interfaceSizeX - 20, 40, Game.game.input.chat, () -> Game.eventsOut.add(new EventChat(ScreenPartyLobby.chatbox.inputText)));
			Game.eventsOut.add(new EventSendTankColors(Game.player));
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

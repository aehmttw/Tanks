package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Crusade;
import tanks.Game;
import tanks.Panel;
import tanks.gui.screen.ScreenPartyCrusadeInterlevel;
import tanks.network.NetworkUtils;

public class EventReturnToCrusade extends PersonalEvent
{
	public String msg1;
	public String msg2;
	public boolean win;
	public boolean lose;

	public EventReturnToCrusade()
	{

	}

	public EventReturnToCrusade(Crusade c)
	{
		if (c.win)
		{
			msg1 = "You finished the crusade!";
			win = true;
		}
		else if (c.lose)
		{
			msg1 = "Game over!";
			lose = true;
		}
		else
		{
			if (Panel.levelPassed)
				msg1 = "Battle cleared!";
			else
				msg1 = "Battle failed!";
		}

		if (c.lifeGained)
			msg2 = "You gained a life for clearing Battle " + (c.currentLevel + 1) + "!";
		else
			msg2 = "";
	}

	@Override
	public void execute() 
	{
		if (this.clientID == null)
		{
			Game.silentCleanUp();
			ScreenPartyCrusadeInterlevel s = new ScreenPartyCrusadeInterlevel(this.win, this.lose);
			s.msg1 = this.msg1;
			s.msg2 = this.msg2;
			Game.screen = s;

			System.gc();
		}
	}
}

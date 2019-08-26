package tanks.event;

import tanks.Game;
import tanks.Movable;
import tanks.tank.Tank;

public class EventTick implements INetworkEvent
{
	public String text = "";
	public boolean isEmpty = true;

	@Override
	public String getNetworkString()
	{
		return text;
	}

	public EventTick()
	{
		String s = "";
		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);
			if (m instanceof Tank && !m.isRemote)
			{
				Tank t = (Tank) m;
				s += t.networkID + "," + t.posX + "," + t.posY + "," + t.vX + "," + t.vY + "," + t.angle + "|";
				this.isEmpty = false;
			}
		}	

		if (s.length() > 0)
			text = s.substring(0, s.length() - 1);
	}

	public EventTick(String s)
	{
		if (s != "")
			this.isEmpty = false;
		this.text = s;
	}

	@Override
	public void execute()
	{		
		String[] s = text.split("\\|");
		for (int i = 0; i < s.length; i++)
		{
			String[] t = s[i].split(",");		
			//System.out.println(Arrays.toString(t));
			Tank tank = Tank.idMap.get(Integer.parseInt(t[0]));

			if (tank != null)
			{
				tank.posX = Double.parseDouble(t[1]);
				tank.posY = Double.parseDouble(t[2]);
				tank.vX = Double.parseDouble(t[3]);
				tank.vY = Double.parseDouble(t[4]);
				tank.angle = Double.parseDouble(t[5]);
			}
		}
	}
}

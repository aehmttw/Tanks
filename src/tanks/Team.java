package tanks;

import java.awt.Color;

public class Team 
{
	public boolean enableColor;
	public Color teamColor = Color.black;
	public boolean friendlyFire = true;
	public String name = "";
	
	public Team(String name)
	{
		this.name = name;
		this.enableColor = false;
	}
	
	public Team(String name, boolean ff)
	{
		this.name = name;
		this.friendlyFire = ff;
	}
	
	public Team(String name, boolean ff, Color col)
	{
		this.name = name;
		this.enableColor = true;
		this.teamColor = col;
		this.friendlyFire = ff;
	}

	public static boolean isAllied(Movable a, Movable b)
	{
		if (a == null || b == null)
			return false;
		if (a == b)
			return true;
		if (a.team == null || b.team == null)
			return false;
		else return a.team == b.team;
	}
	
	public static Color getObjectColor(Color c, Movable m)
	{
		if (m.team == null)
			return c;
		else if (!m.team.enableColor)
			return c;
		else
			return m.team.teamColor;
	}
}

package tanks;

import basewindow.Color;

public class Team
{
	public boolean enableColor;
	public Color teamColor = new Color();

	public boolean friendlyFire = true;
	public String name;

	public static double[] returnColor = new double[3];

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
	
	public Team(String name, boolean ff, double r, double g, double b)
	{
		this.name = name;
		this.enableColor = true;
		this.teamColor.red = r;
		this.teamColor.green = g;
		this.teamColor.blue = b;

		this.friendlyFire = ff;
	}

	public static boolean isAllied(Movable a, Movable b)
	{
//		if (a == b && Game.currentLevel != null && Game.currentLevel.disableFriendlyFire)
//			return true;
		if (a == null || b == null)
			return false;
		if (a == b)
			return true;
		if (a.team == null || b.team == null)
			return false;
		else return a.team == b.team;
	}
	
	public static double[] getObjectColor(double r, double g, double b, Movable m)
	{
		if (m.team == null)
			return setTeamColor(r, g, b);
		else if (!m.team.enableColor)
            return setTeamColor(r, g, b);
		else
			return setTeamColor(m.team.teamColor.red, m.team.teamColor.green, m.team.teamColor.blue);
	}

	protected static double[] setTeamColor(double r, double g, double b)
    {
        returnColor[0] = r;
		returnColor[1] = g;
		returnColor[2] = b;
        return returnColor;
    }

	public static double[] getObjectColor(double[] col, double r, double g, double b, Movable m)
	{
		if (m.team == null)
			return setTeamColor(col, r, g, b);
		else if (!m.team.enableColor)
			return setTeamColor(col, r, g, b);
		else
			return setTeamColor(col, m.team.teamColor.red, m.team.teamColor.green, m.team.teamColor.blue);
	}

	protected static double[] setTeamColor(double[] col, double r, double g, double b)
	{
		col[0] = r;
		col[1] = g;
		col[2] = b;
		return col;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Team)
			return this.name.equals(((Team) obj).name);
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode();
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}

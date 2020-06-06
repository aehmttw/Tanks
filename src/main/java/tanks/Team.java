package tanks;

public class Team 
{
	public boolean enableColor;
	public double teamColorR = 0;
	public double teamColorG = 0;
	public double teamColorB = 0;

	public boolean friendlyFire = true;
	public String name;

	protected static double[] teamColor = new double[3];
	
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
		this.teamColorR = r;
		this.teamColorG = g;
		this.teamColorB = b;

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
	
	public static double[] getObjectColor(double r, double g, double b, Movable m)
	{
		if (m.team == null)
			return setTeamColor(r, g, b);
		else if (!m.team.enableColor)
            return setTeamColor(r, g, b);
		else
			return setTeamColor(m.team.teamColorR, m.team.teamColorG, m.team.teamColorB);
	}

	protected static double[] setTeamColor(double r, double g, double b)
    {
        teamColor[0] = r;
        teamColor[1] = g;
        teamColor[2] = b;
        return teamColor;
    }
}

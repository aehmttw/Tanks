package tanks.tank;

import tanks.Bullet;
import tanks.Game;
import tanks.Mine;
import tanks.Team;

public class TankRemote extends Tank
{
	public final boolean isCopy;
	public final Tank tank;

	public TankRemote(String name, double x, double y, double angle, Team team, double size, double ts, double tl, double r, double g, double b, double lives, double baselives)
	{
		super(name, x, y, size, r, g, b);
		this.team = team;
		this.lives = lives;
		this.baseLives = baselives;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = null;
		this.turret.size = ts;
		this.turret.length = tl;
		this.invulnerable = true;
	}
	
	public TankRemote(Tank t)
	{
		super(t.name, t.posX, t.posY, t.size, t.colorR, t.colorG, t.colorB, false);
		this.team = t.team;
		this.lives = t.lives;
		this.baseLives = t.baseLives;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = t;
		this.turret.length = t.turret.length;
		this.turret.size = t.turret.size;
		this.invulnerable = true;
		this.networkID = t.networkID;
		this.texture = t.texture;
		Tank.idMap.put(this.networkID, this);
	}

	@Override
	public void shoot() 
	{
		
	}
	
	public void shoot(Bullet b)
	{
		Game.movables.add(b);
	}
	
	public void layMine(Mine m)
	{
		Game.movables.add(m);
	}
	
	public void updateState(double x, double y, double vx, double vy, double angle, double health)
	{
		this.posX = x;
		this.posY = y;
		this.vX = vx;
		this.vY = vy;
		this.lives = health;
		this.angle = angle;
	}
}

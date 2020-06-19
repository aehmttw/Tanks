package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventCreateCustomTank extends PersonalEvent
{
	public String name;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	public double size;
	public double turretSize;
	public double turretLength;
	public double red;
	public double green;
	public double blue;
	public double lives;
	public double baseLives;

	public EventCreateCustomTank()
	{
	
	}
	
	public EventCreateCustomTank(Tank t)
	{
		this.name = t.name;
		this.posX = t.posX;
		this.posY = t.posY;
		this.angle = t.angle;
		
		if (t.team == null)
			this.team = "*";
		else if (t.team == Game.enemyTeam)
			this.team = "**";
		else
			this.team = t.team.name;
		
		this.size = t.size;
		this.turretSize = t.turret.size;
		this.turretLength = t.turret.length;
		this.red = t.colorR;
		this.green = t.colorG;
		this.blue = t.colorB;
		this.lives = t.health;
		this.baseLives = t.baseHealth;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Team t = Game.currentLevel.teamsMap.get(team);
		
		if (this.team.equals("**"))
			t = Game.enemyTeam;
		
		Game.movables.add(new TankRemote(name, posX, posY, angle, t, size, turretSize, turretLength, red, green, blue, lives, baseLives));
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.name);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.angle);
		NetworkUtils.writeString(b, this.team);
		b.writeDouble(this.size);
		b.writeDouble(this.turretSize);
		b.writeDouble(this.turretLength);
		b.writeDouble(this.red);
		b.writeDouble(this.green);
		b.writeDouble(this.blue);
		b.writeDouble(this.lives);
		b.writeDouble(this.baseLives);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.name = NetworkUtils.readString(b);
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.angle = b.readDouble();
		this.team = NetworkUtils.readString(b);
		this.size = b.readDouble();
		this.turretSize = b.readDouble();
		this.turretLength = b.readDouble();
		this.red = b.readDouble();
		this.green = b.readDouble();
		this.blue = b.readDouble();
		this.lives = b.readDouble();
		this.baseLives = b.readDouble();
	}
}

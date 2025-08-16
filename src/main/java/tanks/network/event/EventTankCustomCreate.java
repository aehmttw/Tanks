package tanks.network.event;

import basewindow.Color;
import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventTankCustomCreate extends PersonalEvent
{
	public String name;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	public double size;
	public double turretSize;
	public double turretLength;
	public Color color = new Color();
	public double lives;
	public double baseLives;

	public int id;

	public EventTankCustomCreate()
	{
	
	}
	
	public EventTankCustomCreate(Tank t)
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
		this.turretSize = t.turretSize;
		this.turretLength = t.turretLength;
		this.color.set(t.color);
		this.lives = t.health;
		this.baseLives = t.baseHealth;

		this.id = t.networkID;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Team t = Game.currentLevel.teamsMap.get(team);
		
		if (this.team.equals("**"))
			t = Game.enemyTeam;

		TankRemote tank = new TankRemote(name, posX, posY, angle, t, size, turretSize, turretLength, color.red, color.green, color.blue, lives, baseLives);
		tank.setNetworkID(this.id);

		Game.movables.add(tank);
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
		NetworkUtils.writeColor(b, this.color);
		b.writeDouble(this.lives);
		b.writeDouble(this.baseLives);

		b.writeInt(this.id);
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
		NetworkUtils.readColor(b, this.color);
		this.lives = b.readDouble();
		this.baseLives = b.readDouble();

		this.id = b.readInt();
	}
}

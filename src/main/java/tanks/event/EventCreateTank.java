package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Team;
import tanks.network.NetworkUtils;
import tanks.tank.Tank;
import tanks.tank.TankRemote;

public class EventCreateTank extends PersonalEvent
{
	public String type;
	public double posX;
	public double posY;
	public double angle;
	public String team;
	public int id;
	
	public EventCreateTank()
	{
		
	}
	
	public EventCreateTank(Tank t)
	{
		this.type = t.name;
		this.posX = t.posX;
		this.posY = t.posY;
		this.angle = t.angle;
		this.id = t.networkID;

		if (t.team == null)
			this.team = "*";
		else if (t.team == Game.enemyTeam)
			this.team = "**";
		else
			this.team = t.team.name;
	}
	
	@Override
	public void execute()
	{
		if (this.clientID != null)
			return;

		Tank t = Game.registryTank.getEntry(type).getTank(posX, posY, angle);

		Team tm = Game.currentLevel.teamsMap.get(team);
		
		if (this.team.equals("**"))
			tm = Game.enemyTeam;
		
		t.team = tm;
		t.networkID = this.id;
		Tank.idMap.put(t.networkID, t);
				
		Game.movables.add(new TankRemote(t));
	}

	@Override
	public void write(ByteBuf b) 
	{
		NetworkUtils.writeString(b, this.type);
		b.writeDouble(this.posX);
		b.writeDouble(this.posY);
		b.writeDouble(this.angle);
		NetworkUtils.writeString(b, this.team);
		b.writeInt(this.id);
	}

	@Override
	public void read(ByteBuf b) 
	{
		this.type = NetworkUtils.readString(b);
		this.posX = b.readDouble();
		this.posY = b.readDouble();
		this.angle = b.readDouble();
		this.team = NetworkUtils.readString(b);
		this.id = b.readInt();
	}
}

package tanks;

import java.util.ArrayList;

import tanks.tank.Tank;

public class ObstacleTeleporter extends Obstacle
{	
	double cooldown;
	double green = 255;

	public ObstacleTeleporter(String name, double posX, double posY) 
	{
		super(name, posX, posY);

		this.destructible = false;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.drawBelow = true;
		this.update = true;
		this.colorR = 0;
		this.colorG = 255;
		this.colorB = 255;
		this.draggable = false;
	}

	@Override
	public void draw()
	{	
		Drawing.drawing.setColor(127, 127, 127);
		Drawing.drawing.fillOval(this.posX, this.posY, draw_size, draw_size);

		if (this.cooldown > 0)
			this.green = Math.min(255, this.green - 2.55 * Panel.frameFrequency);
		else
			this.green = Math.max(0, this.green + 2.55 * Panel.frameFrequency);

		Drawing.drawing.setColor(0, this.green, 255);
		
		Drawing.drawing.fillOval(this.posX, this.posY, draw_size / 2, draw_size / 2);
	}
	
	@Override
	public void drawForInterface(double x, double y)
	{	
		Drawing.drawing.setColor(127, 127, 127);
		Drawing.drawing.fillInterfaceOval(x, y, draw_size, draw_size);

		Drawing.drawing.setColor(0, 255, 255);
		
		Drawing.drawing.fillInterfaceOval(x, y, draw_size / 2, draw_size / 2);
	}

	@Override
	public void update()
	{
		ArrayList<ObstacleTeleporter> teleporters = new ArrayList<ObstacleTeleporter>(); 
		Tank t = null;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank && !((Tank) m).invulnerable && Movable.distanceBetween(this, m) < ((Tank)m).size)
			{
				t = (Tank) m;

				if (this.cooldown > 0)
				{
					this.cooldown = Math.max(100, this.cooldown);
					continue;
				}

				for (int j = 0; j < Game.obstacles.size(); j++)
				{
					Obstacle o = Game.obstacles.get(j);
					if (o instanceof ObstacleTeleporter && o != this)
					{
						teleporters.add((ObstacleTeleporter) o);
					}
				}			

			}
		}

		this.cooldown = Math.max(0, this.cooldown - Panel.frameFrequency);

		if (t != null && teleporters.size() > 0 && this.cooldown <= 0)
		{
			int i = (int) (Math.random() * teleporters.size());

			ObstacleTeleporter o = teleporters.get(i);
			o.cooldown = 500;
			this.cooldown = 500;
			Game.movables.add(new TeleporterOrb(t.posX, t.posY, this.posX, this.posY, o.posX, o.posY, t));
		}
	}
}

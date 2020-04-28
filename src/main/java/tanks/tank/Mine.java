package tanks.tank;

import tanks.*;
import tanks.event.EventLayMine;
import tanks.event.EventMineExplode;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

public class Mine extends Movable
{
	public static double mine_size = 30;
	public double timer;
	public double size = mine_size;
	public double outlineColorR;
	public double outlineColorG;
	public double outlineColorB;

	public double radius = Game.tank_size * 2.5;
	public Tank tank;

	public int networkID;

	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
	public static HashMap<Integer, Mine> idMap = new HashMap<Integer, Mine>();

	public Mine(double x, double y, double timer, Tank t)
	{
		super(x, y);

		this.timer = timer;
		this.drawLevel = 2;
		tank = t;
		t.liveMines++;
		this.team = t.team;
		double[] outlineCol = Team.getObjectColor(t.colorR, t.colorG, t.colorB, t);
		this.outlineColorR = outlineCol[0];
		this.outlineColorG = outlineCol[1];
		this.outlineColorB = outlineCol[2];
		
		if (!t.isRemote)
			Game.eventsOut.add(new EventLayMine(this));

		if (freeIDs.size() > 0)
			this.networkID = freeIDs.remove(0);
		else
		{
			this.networkID = currentID;
			currentID++;
		}

		idMap.put(this.networkID, this);
	}
	
	public Mine(double x, double y, Tank t) 
	{
		this(x, y, 1000, t);
	}

	@Override
	public void draw() 
	{	
		Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB);
		Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

		Drawing.drawing.setColor(255, (this.timer) / 1000.0 * 255, 0);

		if (timer < 150 && ((int) timer % 16) / 8 == 1)
			Drawing.drawing.setColor(255, 255, 0);

		Drawing.drawing.fillOval(this.posX, this.posY, this.size * 0.8, this.size * 0.8);
	}

	@Override
	public void update()
	{
		this.timer -= Panel.frameFrequency;

		if (this.timer < 0)
			this.timer = 0;

		if (destroy && !this.tank.isRemote)
			this.explode();

		if (this.timer <= 0 && !this.tank.isRemote)
			this.explode();

		super.update();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius - Game.tank_size, 2))
			{
				if (o instanceof Tank && !o.destroy && !o.equals(this.tank))
				{
					this.timer = Math.min(150, this.timer);
				}		
			}
		}
	}

	public void explode()
	{		
		Drawing.drawing.playSound("explosion.ogg");

		if (Game.fancyGraphics)
		{
			for (int j = 0; j < 400; j++)
			{
				double random = Math.random();
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
				e.maxAge /= 2;
				e.colR = 255;
				e.colG = (1 - random) * 155 + Math.random() * 100;
				e.colB = 0;

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI / 2, random * (this.radius - Game.tank_size / 2) / Game.tank_size * 2);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, random * (this.radius - Game.tank_size / 2) / Game.tank_size * 2);
				Game.effects.add(e);
			}
		}

		this.destroy = true;

		if (!this.tank.isRemote)
		{
			Game.eventsOut.add(new EventMineExplode(this));

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable o = Game.movables.get(i);
				if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius, 2))
				{
					if (o instanceof Tank && !o.destroy && !((Tank) o).invulnerable)
					{
						if (!(Team.isAllied(this, o) && !this.team.friendlyFire))
						{
							((Tank) o).lives -= 2;
							((Tank) o).flashAnimation = 1;

							if (((Tank) o).lives <= 0)
							{
								((Tank) o).flashAnimation = 0;
								o.destroy = true;

								if (this.tank.equals(Game.playerTank))
									Panel.panel.hotbar.currentCoins.coins += ((Tank) o).coinValue;
							}
						}
					}
					else if (o instanceof Mine && !o.destroy)
					{
						o.destroy = true;
					}
				}
			}
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius, 2) && o.destructible)
			{
				Game.removeObstacles.add(o);

				if (Game.fancyGraphics)
				{	
					if (Game.enable3d)
					{
						for (int j = 0; j < Obstacle.obstacle_size; j += 10)
						{
							for (int k = 0; k < Obstacle.obstacle_size; k += 10)
							{
								for (int l = 0; l < Obstacle.obstacle_size; l += 10)
								{
									Effect e = Effect.createNewEffect(o.posX + j + 5 - Obstacle.obstacle_size / 2, o.posY + k + 5 - Obstacle.obstacle_size / 2, l, Effect.EffectType.obstaclePiece3d);

									e.colR = o.colorR;
									e.colG = o.colorG;
									e.colB = o.colorB;

									double dist = Movable.distanceBetween(this, e);
									double angle = this.getAngleInDirection(e.posX, e.posY);
									double rad = radius - Game.tank_size / 2;
									e.addPolarMotion(angle, (rad * Math.sqrt(2) - dist) / (rad * 2) + Math.random() * 2);
									e.vZ = (rad * Math.sqrt(2) - dist) / (rad * 2) + Math.random() * 2;

									Game.effects.add(e);

								}
							}
						}
					}
					else
					{
						for (int j = 0; j < Obstacle.obstacle_size - 6; j += 4)
						{
							for (int k = 0; k < Obstacle.obstacle_size - 6; k += 4)
							{
								Effect e = Effect.createNewEffect(o.posX + j + 5 - Obstacle.obstacle_size / 2, o.posY + k + 5 - Obstacle.obstacle_size / 2, Effect.EffectType.obstaclePiece);

								e.colR = o.colorR;
								e.colG = o.colorG;
								e.colB = o.colorB;

								double dist = Movable.distanceBetween(this, e);
								double angle = this.getAngleInDirection(e.posX, e.posY);
								double rad = radius - Game.tank_size / 2;
								e.addPolarMotion(angle, (rad * Math.sqrt(2) - dist) / (rad * 2) + Math.random() * 2);

								Game.effects.add(e);
							}
						}
					}
				}
			}
		}

		tank.liveMines--;

		Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.mineExplosion);
		e.radius = this.radius - Game.tank_size * 0.5;
		Game.effects.add(e);

		Game.removeMovables.add(this);

		freeIDs.add(this.networkID);
		idMap.remove(this.networkID);
	}
}

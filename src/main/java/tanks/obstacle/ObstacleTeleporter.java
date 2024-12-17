package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.leveleditor.selector.LevelEditorSelector;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;
import tanks.tank.TeleporterOrb;

import java.util.ArrayList;

/**
 * A teleporter which randomly transports the player to another teleporter in the level
 */
public class ObstacleTeleporter extends Obstacle
{
	public double cooldown;
	public double brightness = 1;

	public Effect glow;

	public ObstacleTeleporter(String name, double posX, double posY)
	{
		super(name, posX, posY);

		this.replaceTiles = false;
		this.enableGroupID = true;
		this.destructible = false;
		this.tankCollision = false;
		this.bulletCollision = false;
		this.checkForObjects = true;
		this.drawLevel = 0;
		this.update = true;
		this.colorR = 0;
		this.colorG = 255;
		this.colorB = 255;
		this.draggable = false;
		this.enableStacking = false;
		this.type = ObstacleType.extra;

		this.batchDraw = false;

		glow = Effect.createNewEffect(this.posX, this.posY, 0, Effect.EffectType.teleporterLight);

		this.description = "A teleporter which randomly transports you to another teleporter in the level";
	}

	@Override
	public void draw()
	{
		double height = this.baseGroundHeight;

		if (Game.enable3d)
		{
			for (double i = height; i < height + 5; i++)
			{
				double frac = ((i - height) / 5 + 1) / 2;
				Drawing.drawing.setColor(127 * frac, 127 * frac, 127 * frac, 255, 0.25);
				Drawing.drawing.fillOval(this.posX, this.posY, i, draw_size, draw_size, true, false);
			}
		}
		else
		{
			Drawing.drawing.setColor(127, 127, 127, 255, 0.25);
			Drawing.drawing.fillOval(this.posX, this.posY, draw_size, draw_size);
		}

		if (this.cooldown > 0)
			this.brightness = Math.max(0, this.brightness - 0.01 * Panel.frameFrequency);
		else
			this.brightness = Math.min(1, this.brightness + 0.01 * Panel.frameFrequency);

		if (Game.enable3d)
		{
			Drawing.drawing.setColor(this.colorR * (2 - this.brightness) / 2, this.colorG * (2 - this.brightness) / 2, this.colorB * (2 - this.brightness) / 2, 255, 1);

			if (Game.glowEnabled)
				Drawing.drawing.fillGlow(this.posX, this.posY, height + 7, draw_size * 20 / 8, draw_size * 20 / 8, true, false);

			Drawing.drawing.setColor(this.colorR * (2 - this.brightness) / 2, this.colorG * (2 - this.brightness) / 2, this.colorB * (2 - this.brightness) / 2, 255, (2 - this.brightness) / 2);
			Drawing.drawing.fillOval(this.posX, this.posY, height + 6, draw_size * 5 / 8, draw_size * 5 / 8, true, false);
			Drawing.drawing.setColor(this.brightness * this.colorR + 255 * (1 - this.brightness), this.brightness * this.colorG + 255 * (1 - this.brightness), this.brightness * this.colorB  + 255 * (1 - this.brightness), 255, (2 - this.brightness) / 2);
			Drawing.drawing.fillOval(this.posX, this.posY, height + 7, draw_size / 2, draw_size / 2, true, false);

			if (Game.fancyTerrain)
			{
				glow.posX = this.posX;
				glow.posY = this.posY;
				glow.posZ = height;
				glow.size = this.brightness;

				if (Game.screen instanceof ScreenGame)
					((ScreenGame) Game.screen).drawables[9].add(glow);
			}
		}
		else
		{
			Drawing.drawing.setColor(this.colorR * (2 - this.brightness) / 2, this.colorG * (2 - this.brightness) / 2, this.colorB * (2 - this.brightness) / 2, 255, 1);

			if (Game.glowEnabled)
				Drawing.drawing.fillGlow(this.posX, this.posY, draw_size * 20 / 8, draw_size * 20 / 8);

			Drawing.drawing.setColor(this.colorR * (2 - this.brightness) / 2, this.colorG * (2 - this.brightness) / 2, this.colorB * (2 - this.brightness) / 2, 255, (2 - this.brightness) / 2);

			Drawing.drawing.fillOval(this.posX, this.posY, draw_size * 5 / 8, draw_size * 5 / 8);
			Drawing.drawing.setColor(this.brightness * this.colorR + 255 * (1 - this.brightness), this.brightness * this.colorG + 255 * (1 - this.brightness), this.brightness * this.colorB  + 255 * (1 - this.brightness), 255, (2 - this.brightness) / 2);
			Drawing.drawing.fillOval(this.posX, this.posY, draw_size / 2, draw_size / 2);
		}
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		Drawing.drawing.setColor(127, 127, 127);
		Drawing.drawing.fillInterfaceOval(x, y, draw_size, draw_size);
		Drawing.drawing.setColor(0, 127, 127);
		Drawing.drawing.fillInterfaceOval(x, y, draw_size * 5 / 8, draw_size * 5 / 8);
		Drawing.drawing.setColor(0, 255, 255);
		Drawing.drawing.fillInterfaceOval(x, y, draw_size / 2, draw_size / 2);
	}

	@Override
	public void update()
	{
		ArrayList<ObstacleTeleporter> teleporters = new ArrayList<>();
		Tank t = null;

		if (!ScreenGame.finished)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (m instanceof Tank && ((Tank) m).targetable && Movable.distanceBetween(this, m) < ((Tank) m).size)
				{
					t = (Tank) m;

					if (this.cooldown > 0)
					{
						this.cooldown = Math.max(100, this.cooldown);
						continue;
					}

					if (!m.isRemote)
					{
						for (int j = 0; j < Game.obstacles.size(); j++)
						{
							Obstacle o = Game.obstacles.get(j);
							if (o instanceof ObstacleTeleporter && o != this && o.groupID == this.groupID && ((ObstacleTeleporter) o).cooldown <= 0)
							{
								teleporters.add((ObstacleTeleporter) o);
							}
						}
					}
				}
			}

			this.cooldown = Math.max(0, this.cooldown - Panel.frameFrequency);

			if (t != null && !teleporters.isEmpty() && this.cooldown <= 0)
			{
				int i = (int) (Math.random() * teleporters.size());

				ObstacleTeleporter o = teleporters.get(i);
				o.cooldown = 500;
				this.cooldown = 500;
				Game.movables.add(new TeleporterOrb(t.posX, t.posY, this.posX, this.posY, o.posX, o.posY, t));
			}
		}
	}

	@Override
	public void onPropertySet(LevelEditorSelector<?> s)
	{
		double[] col = getColorFromID(this.groupID);
		this.colorR = col[0];
		this.colorG = col[1];
		this.colorB = col[2];
	}

	public static double[] getColorFromID(int id)
	{
		int i = id;
		int c = 0;

		while (i > 1)
		{
			i = i / 2;
			c++;
		}

		double sections = Math.pow(2, c);

		double col = (id - sections + 0.5) / sections * 255 * 6;

		if (id == 0)
			col = 0;

		col = (col + 255 * 3) % (255 * 6);

		double r = 0;
		double g = 0;
		double b = 0;

		if (col <= 255)
		{
			r = 255;
			g = col;
			b = 0;
		}
		else if (col <= 255 * 2)
		{
			r = 255 * 2 - col;
			g = 255;
			b = 0;
		}
		else if (col <= 255 * 3)
		{
			g = 255;
			b = col - 255 * 2;
		}
		else if (col <= 255 * 4)
		{
			g = 255 * 4 - col;
			b = 255;
		}
		else if (col <= 255 * 5)
		{
			r = col - 255 * 4;
			g = 0;
			b = 255;
		}
		else if (col <= 255 * 6)
		{
			r = 255;
			g = 0;
			b = 255 * 6 - col;
		}

		return new double[]{r, g, b};
	}

	public double getTileHeight()
	{
		return 0;
	}
}

package tanks.tank;

import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.Team;
import tanks.Turret;
import tanks.event.EventTankDestroyed;
import tanks.gui.Panel;
import tanks.obstacles.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

import tanks.AttributeModifier;
import tanks.Drawing;

public abstract class Tank extends Movable
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
	public static HashMap<Integer, Tank> idMap = new HashMap<Integer, Tank>();

	public double angle = 0;

	public boolean showName = false;

	public boolean invulnerable = false;
	public boolean targetable = true;

	public boolean disabled = false;

	public boolean functional = true;

	public int coinValue = 0;
	public int networkID;

	public String name = "";

	public double accel = 0.1;
	public double maxV = 2.5;
	public int liveBullets = 0;
	public int liveMines = 0;
	public double size;
	public double colorR;
	public double colorG;
	public double colorB;
	public int liveBulletMax;
	public int liveMinesMax;
	public double drawAge = 0;
	public double destroyTimer = 0;
	public boolean hasCollided = false;
	public double flashAnimation = 0;
	public double treadAnimation = 0;
	public boolean drawTread = false;
	public String texture = null;

	public double baseLives = 1;
	public double lives = 1;

	public Turret turret;

	public Tank(String name, double x, double y, double size, double r, double g, double b, boolean countID) 
	{
		super(x, y);
		this.size = size;
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		turret = new Turret(this);
		this.name = name;
		this.drawLevel = 4;

		if (countID)
		{
			if (freeIDs.size() > 0)
				this.networkID = freeIDs.remove(0);
			else
			{
				this.networkID = currentID;
				currentID++;
			}
			idMap.put(this.networkID, this);
		}
		else
			this.networkID = -1;
	}

	public Tank(String name, double x, double y, double size, double r, double g, double b) 
	{
		this(name, x, y, size, r, g, b, true);
	}

	@Override
	public void checkCollision() 
	{
		if (this.size <= 0)
			return;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (o instanceof Tank && ((Tank)o).size > 0)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double dx = this.posX - o.posX;
				double dy = this.posY - o.posY;

				double bound = this.size / 2 + ((Tank)o).size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
					{
						hasCollided = true;
						double v = (this.vX + o.vX) / 2;
						this.vX = v;
						o.vX = v;
						this.posX += horizontalDist - bound;
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						double v = (this.vY + o.vY) / 2;
						this.vY = v;
						o.vY = v;
						this.posY += verticalDist - bound;
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						hasCollided = true;
						double v = (this.vX + o.vX) / 2;
						this.vX = v;
						o.vX = v;
						this.posX -= horizontalDist - bound;
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						double v = (this.vY + o.vY) / 2;
						this.vY = v;
						o.vY = v;
						this.posY -= verticalDist - bound;
					}
				}
			}
		}

		hasCollided = false;

		if (this.posX + this.size / 2 > Drawing.drawing.sizeX)
		{
			this.posX = Drawing.drawing.sizeX - this.size / 2;
			this.vX = 0;
			hasCollided = true;
		}
		if (this.posY + this.size / 2 > Drawing.drawing.sizeY)
		{
			this.posY = Drawing.drawing.sizeY - this.size / 2;
			this.vY = 0;
			hasCollided = true;
		}
		if (this.posX - this.size / 2 < 0)
		{
			this.posX = this.size / 2;
			this.vX = 0;
			hasCollided = true;
		}
		if (this.posY - this.size / 2 < 0)
		{
			this.posY = this.size / 2;
			this.vY = 0;
			hasCollided = true;
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			boolean bouncy = o.bouncy;

			if (!o.tankCollision && !o.checkForObjects)
				continue;

			double horizontalDist = Math.abs(this.posX - o.posX);
			double verticalDist = Math.abs(this.posY - o.posY);

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double bound = this.size / 2 + Obstacle.obstacle_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
			{
				if (o.checkForObjects)
					o.onObjectEntry(this);

				if (!o.tankCollision)
					continue;

				if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX += horizontalDist - bound;
				}
				else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vY = -this.vY;
					else
						this.vY = 0;
					this.posY += verticalDist - bound;
				}
				else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX -= horizontalDist - bound;
				}
				else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vY = -this.vY;
					else
						this.vY = 0;
					this.posY -= verticalDist - bound;
				}
			}
		}
	}

	@Override
	public void update()
	{	
		this.checkCollision();

		this.treadAnimation += Math.sqrt(this.vX * this.vX + this.vY * this.vY) * Panel.frameFrequency;

		if (this.treadAnimation > this.size * 4 / 5)
		{
			this.drawTread = true;
			this.treadAnimation -= this.size * 4 / 5;
		}

		this.flashAnimation = Math.max(0, this.flashAnimation - 0.05 * Panel.frameFrequency);

		if (destroy)
		{
			if (this.destroyTimer <= 0 && this.lives <= 0)
			{
				Drawing.drawing.playSound("resources/destroy.wav");

				if (!freeIDs.contains(this.networkID))
				{
					if (!this.isRemote)
						Game.events.add(new EventTankDestroyed(this));
					freeIDs.add(this.networkID);
					idMap.remove(this.networkID);
				}

				if (Game.fancyGraphics)
				{
					for (int i = 0; i < this.size * 4; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
						int var = 50;

						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));
						e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);
						Game.effects.add(e);
					}
				}
			}

			this.destroyTimer += Panel.frameFrequency;
		}

		if (this.destroyTimer > Game.tank_size)
			Game.removeMovables.add(this);

		if (this.drawTread)
		{
			this.drawTread = false;
			double a = this.getPolarDirection();
			Effect e1 = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.tread);
			Effect e2 = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.tread);
			e1.setPolarMotion(a - Math.PI / 2, this.size * 0.25);
			e2.setPolarMotion(a + Math.PI / 2, this.size * 0.25);
			e1.size = this.size / 5;
			e2.size = this.size / 5;
			e1.posX += e1.vX;
			e1.posY += e1.vY;
			e2.posX += e2.vX;
			e2.posY += e2.vY;
			e1.setPolarMotion(0, 0);
			e2.setPolarMotion(0, 0);
			Game.belowEffects.add(e1);
			Game.belowEffects.add(e2);
		}

		for (int i = 0; i < this.attributes.size(); i++)
		{
			AttributeModifier a = this.attributes.get(i);
			if (a.name.equals("healray"))
			{
				if (this.lives < this.baseLives)
				{
					this.attributes.remove(a);
					i--;
				}
			}
		}

		super.update();
	}

	public abstract void shoot();

	@Override
	public void drawForInterface(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawTank(true);
		this.posX = x1;
		this.posY = y1;	
	}


	public void drawTank(boolean forInterface)
	{
		double s = (this.size * (Game.tank_size - destroyTimer) / Game.tank_size) * Math.min(this.drawAge / Game.tank_size, 1);
		double sizeMod = 1;

		if (forInterface)
			s = Math.min(this.size, Game.tank_size * 1.5);

		Drawing drawing = Drawing.drawing;
		double[] teamColor = Team.getObjectColor(this.colorR, this.colorG, this.colorB, this);

		if (!(teamColor[0] == this.colorR && teamColor[1] == this.colorG && teamColor[2] == this.colorB))
		{
			Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2]);

			if (forInterface)
				drawing.fillInterfaceRect(this.posX, this.posY, s, s);
			else
			{
				if (Game.enable3d)
					drawing.fillBox(this.posX, this.posY, 0, s, s, s / 2 - 0.1);
				else
					drawing.fillRect(this.posX, this.posY, s, s);
			}

			sizeMod = 0.8;
		}
		
		if (this instanceof TankPlayer && ((TankPlayer) this).ui)
			sizeMod *= Panel.LOAD_SIZE_MOD;

		double flash = Math.min(1, this.flashAnimation);

		if (forInterface || !Game.enable3d)
		{
			Drawing.drawing.setColor(0, 255, 0);
			for (int i = 0; i < this.attributes.size(); i++)
			{
				AttributeModifier a = this.attributes.get(i);
				if (a.name.equals("healray"))
				{
					double mod = 1 + 0.4 * (this.lives - this.baseLives);

					if (this.lives > this.baseLives)
					{
						if (forInterface)
							drawing.fillInterfaceRect(this.posX, this.posY, s * mod, s * mod);
						else
						{
							if (!Game.enable3d)
								drawing.fillRect(this.posX, this.posY, s * mod, s * mod);
						}
					}
				}
			}
		}

		Drawing.drawing.setColor(this.colorR * (1 - flash) + 255 * flash,  this.colorG * (1 - flash), this.colorB * (1 - flash));

		if (forInterface)
			drawing.fillInterfaceRect(this.posX, this.posY, s * sizeMod, s * sizeMod);
		else
		{
			if (Game.enable3d)
				drawing.fillBox(this.posX, this.posY, 0, s * sizeMod, s * sizeMod, s / 2);
			else
				drawing.fillRect(this.posX, this.posY, s * sizeMod, s * sizeMod);

		}

		if (this.lives > 1)
		{
			for (int i = 1; i < lives; i++)
			{
				if (forInterface)
					drawing.drawInterfaceRect(this.posX, 
							this.posY, 8 * i + s, 
							8 * i + s);
				else
					drawing.drawRect(this.posX, 
							this.posY, 8 * i + this.size * (Game.tank_size - destroyTimer) / Game.tank_size - Math.max(Game.tank_size - drawAge, 0) / Game.tank_size * this.size, 
							8 * i + this.size * (Game.tank_size - destroyTimer) / Game.tank_size - Math.max(Game.tank_size - drawAge, 0) / Game.tank_size * this.size);
			}
		}

		Drawing.drawing.setColor(255, 255, 255);

		if (this.texture != null)
		{
			if (forInterface)
				drawing.drawInterfaceImage(this.texture, this.posX, this.posY, s * sizeMod, s * sizeMod);
			else
			{
				if (Game.enable3d)
					drawing.drawImage(this.texture, this.posX, this.posY, this.size / 2 + 0.1, s * sizeMod, s * sizeMod);
				else
					drawing.drawImage(this.texture, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
		}

		if (!forInterface && Game.enable3d)
		{
			Drawing.drawing.setColor(0, 255, 0, 127);
			for (int i = 0; i < this.attributes.size(); i++)
			{
				AttributeModifier a = this.attributes.get(i);
				if (a.name.equals("healray"))
				{
					double mod = 1 + 0.4 * (this.lives - this.baseLives);

					if (this.lives > this.baseLives)
					{
						if (!forInterface && Game.enable3d)
						{
							drawing.fillBox(this.posX, this.posY, 0, s * mod, s * mod, s / 2 - 0.2);
						}
					}
				}
			}
		}

		this.turret.draw(angle, forInterface, true);

		if (this.showName)
			this.drawName();
	}

	@Override
	public void draw() 
	{
		drawAge += Panel.frameFrequency;
		this.drawTank(false);
	}

	public void drawName()
	{
		Drawing.drawing.setFontSize(20);
		Drawing.drawing.drawText(this.posX, this.posY + 35, this.name);
	}

	public void drawOutline() 
	{
		drawAge = Game.tank_size;
		Drawing drawing = Drawing.drawing;

		//g.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), 128));
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		drawing.fillRect(this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX, this.posY - this.size * 0.4, this.size, this.size * 0.2);
		drawing.fillRect(this.posX, this.posY + this.size * 0.4, this.size, this.size * 0.2);

		if (this.lives > 1)
		{
			for (int i = 1; i < lives; i++)
			{
				drawing.drawRect(this.posX, this.posY, 8 * i + this.size * (Game.tank_size - destroyTimer) / Game.tank_size - Math.max(Game.tank_size - drawAge, 0), 8 * i + this.size * (Game.tank_size - destroyTimer) / Game.tank_size - Math.max(Game.tank_size - drawAge, 0));
			}
		}

		Drawing.drawing.setColor(255, 255, 255, 127);

		if (this.texture != null)
		{
			drawing.drawImage(this.texture, this.posX, this.posY, this.size, this.size);
		}


		this.turret.draw(angle, false, false);
	}

	public void drawAt(double x, double y)
	{	
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawTank(false);
		this.posX = x1;
		this.posY = y1;	
	}
}

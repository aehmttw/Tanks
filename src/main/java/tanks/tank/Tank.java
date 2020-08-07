package tanks.tank;

import tanks.*;
import tanks.event.EventTankAddAttributeModifier;
import tanks.event.EventTankUpdate;
import tanks.event.EventTankUpdateHealth;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Tank extends Movable implements ISolidObject
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<Integer>();
	public static HashMap<Integer, Tank> idMap = new HashMap<Integer, Tank>();

	public double angle = 0;

	public boolean depthTest = true;

	public boolean invulnerable = false;
	public boolean targetable = true;

	public boolean disabled = false;
	public boolean inControlOfMotion = true;

	public boolean tookRecoil = false;
	public double recoilSpeed = 0;

	public int coinValue = 0;

	public int networkID;

	public String name;

	public String description = "";

	public double acceleration = 0.1;
	public double accelerationModifier = 1;
	public double frictionModifier = 1;
	public double maxSpeedModifier = 1;
	public double maxSpeed = 3.0;
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
	public double orientation = 0;

	public double baseHealth = 1;
	public double health = 1;

	public boolean[][] hiddenPoints = new boolean[3][3];
	public boolean hidden = false;

	public boolean[][] canHidePoints = new boolean[3][3];
	public boolean canHide = false;

	public Turret turret;

	public boolean standardUpdateEvent = true;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	public static Model base_model = new Model();
	public static Model color_model = new Model();

	public static Model health_model = new Model();

	public Tank(String name, double x, double y, double size, double r, double g, double b, boolean countID) 
	{
		super(x, y);
		this.size = size;
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		turret = new Turret(this);
		this.name = name;
		this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, this.name, r, g, b);

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

	public void checkCollision()
	{
		if (this.size <= 0)
			return;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (this != o && o instanceof Tank && ((Tank)o).size > 0)
			{
				Tank t = (Tank) o;
				double distSq = Math.pow(this.posX - o.posX, 2) + Math.pow(this.posY - o.posY, 2);

				if (distSq <= Math.pow((this.size + t.size) / 2, 2))
				{
					this.hasCollided = true;
					t.hasCollided = true;

					double ourMass = this.size * this.size;
					double theirMass = t.size * t.size;

					double angle = this.getAngleInDirection(t.posX, t.posY);

					double ourV = Math.sqrt(this.vX * this.vX + this.vY * this.vY);
					double ourAngle = this.getPolarDirection();
					double ourParallelV = ourV * Math.cos(ourAngle - angle);
					double ourPerpV = ourV * Math.sin(ourAngle - angle);

					double theirV = Math.sqrt(t.vX * t.vX + t.vY * t.vY);
					double theirAngle = t.getPolarDirection();
					double theirParallelV = theirV * Math.cos(theirAngle - angle);
					double theirPerpV = theirV * Math.sin(theirAngle - angle);

					double newV = (ourParallelV * ourMass + theirParallelV * theirMass) / (ourMass + theirMass);

					double dist = Math.sqrt(distSq);
					this.moveInDirection(Math.cos(angle), Math.sin(angle), (dist - (this.size + t.size) / 2) * theirMass / (ourMass + theirMass));
					t.moveInDirection(Math.cos(Math.PI + angle), Math.sin(Math.PI + angle), (dist - (this.size + t.size) / 2) * ourMass / (ourMass + theirMass));

					this.setMotionInDirection(t.posX, t.posY, newV);
					this.addPolarMotion(angle + Math.PI / 2, ourPerpV);

					t.setMotionInDirection(this.posX, this.posY, -newV);
					t.addPolarMotion(angle + Math.PI / 2, theirPerpV);
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

			double bound = this.size / 2 + Game.tile_size / 2;

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
		this.treadAnimation += Math.sqrt(this.vX * this.vX + this.vY * this.vY) * Panel.frameFrequency;

		if (this.treadAnimation > this.size * 4 / 5 && !this.destroy && !ScreenGame.finished)
		{
			this.drawTread = true;
			this.treadAnimation -= this.size * 4 / 5;
		}

		this.flashAnimation = Math.max(0, this.flashAnimation - 0.05 * Panel.frameFrequency);

		if (destroy)
		{
			if (this.destroyTimer <= 0 && this.health <= 0)
			{
				Drawing.drawing.playSound("destroy.ogg", (float) (Game.tile_size / this.size));

				if (!freeIDs.contains(this.networkID))
				{
					if (!this.isRemote)
						Game.eventsOut.add(new EventTankUpdateHealth(this));

					freeIDs.add(this.networkID);
					idMap.remove(this.networkID);
				}

				this.onDestroy();

				if (Game.fancyGraphics)
				{
					for (int i = 0; i < this.size * 4; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
						double var = 50;

						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);

						Game.effects.add(e);
					}
				}
			}

			this.destroyTimer += Panel.frameFrequency;
		}

		if (this.destroyTimer > Game.tile_size)
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
			e1.angle = a;
			e2.angle = a;
			e1.setPolarMotion(0, 0);
			e2.setPolarMotion(0, 0);
			//setEffectHeight(e1);
			//setEffectHeight(e2);
			Game.tracks.add(e1);
			Game.tracks.add(e2);
		}

		this.accelerationModifier = 1;
		this.frictionModifier = 1;
		this.maxSpeedModifier = 1;

		for (int i = 0; i < this.attributes.size(); i++)
		{
			AttributeModifier a = this.attributes.get(i);

			if (a.name.equals("healray"))
			{
				if (this.health < this.baseHealth)
				{
					this.attributes.remove(a);
					i--;
				}
			}
			else if (a.type.equals("acceleration"))
				this.accelerationModifier = a.getValue(this.accelerationModifier);
			else if (a.type.equals("friction"))
				this.frictionModifier = a.getValue(this.frictionModifier);
			else if (a.type.equals("max_speed"))
				this.maxSpeedModifier = a.getValue(this.maxSpeedModifier);
		}

		super.update();

		if (this.health <= 0)
			this.destroy = true;

		this.checkCollision();

		if (!(Math.abs(this.posX - this.lastPosX) < 0.01 && Math.abs(this.posY - this.lastPosY) < 0.01) && !this.destroy && !ScreenGame.finished)
		{
			double dir = Math.PI + this.getAngleInDirection(this.lastPosX, this.lastPosY);
			if (Movable.absoluteAngleBetween(this.orientation, dir) <= Movable.absoluteAngleBetween(this.orientation + Math.PI, dir))
				this.orientation -= Movable.angleBetween(this.orientation, dir) / 10 * Panel.frameFrequency;
			else
				this.orientation -= Movable.angleBetween(this.orientation + Math.PI, dir) / 10 * Panel.frameFrequency;
		}

		if (!this.isRemote && this.standardUpdateEvent && ScreenPartyHost.isServer)
			Game.eventsOut.add(new EventTankUpdate(this));

		this.canHide = true;
		for (int i = 0; i < this.canHidePoints.length; i++)
		{
			for (int j = 0; j < this.canHidePoints[i].length; j++)
			{
				canHide = canHide && canHidePoints[i][j];
				canHidePoints[i][j] = false;
			}
		}

		this.hidden = true;
		for (int i = 0; i < this.hiddenPoints.length; i++)
		{
			for (int j = 0; j < this.hiddenPoints[i].length; j++)
			{
				hidden = hidden && hiddenPoints[i][j];
				hiddenPoints[i][j] = false;
			}
		}

		if (this.hasCollided)
		{
			this.tookRecoil = false;
			this.inControlOfMotion = true;
		}
	}

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
		double s = (this.size * (Game.tile_size - destroyTimer) / Game.tile_size) * Math.min(this.drawAge / Game.tile_size, 1);
		double sizeMod = 1;

		if (forInterface)
			s = Math.min(this.size, Game.tile_size * 1.5);

		Drawing drawing = Drawing.drawing;
		double[] teamColor = Team.getObjectColor(this.turret.colorR, this.turret.colorG, this.turret.colorB, this);

		if (Game.framework == Game.Framework.swing)
			teamColor = Team.getObjectColor(this.colorR, this.colorG, this.colorB, this);

		//double[] teamColor = Team.getObjectColor(172, 129, 74, this);

		Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2]);

		if (Game.superGraphics)
		{
			if (forInterface)
				Drawing.drawing.fillInterfaceGlow(this.posX, this.posY, s * 4, s * 4);
			else if (!Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, s * 4, s * 4);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, this.size / 4, s * 4, s * 4, true, false);
		}

		if (!forInterface)
		{
			for (int i = 0; i < this.attributes.size(); i++)
			{
				AttributeModifier a = this.attributes.get(i);
				if (a.name.equals("healray"))
				{
					double mod = 1 + 0.4 * Math.min(1, this.health - this.baseHealth);

					if (this.health > this.baseHealth)
					{
						if (!Game.enable3d)
						{
							Drawing.drawing.setColor(0, 255, 0);
							drawing.drawModel(base_model, this.posX, this.posY, s * mod, s * mod, this.orientation);
						}
						else
						{
							Drawing.drawing.setColor(0, 255, 0, 127);
							drawing.drawModel(base_model, this.posX, this.posY, 0, s * mod, s * mod, s - 2, this.orientation);
						}
					}
				}
			}
		}

		Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2]);

		if (forInterface)
			drawing.drawInterfaceModel(base_model, this.posX, this.posY, s, s, this.orientation);
		else
		{
			if (Game.enable3d)
				drawing.drawModel(base_model, this.posX, this.posY, 0, s, s, s, this.orientation);
			else
				drawing.drawModel(base_model, this.posX, this.posY, s, s, this.orientation);
		}


		double flash = Math.min(1, this.flashAnimation);

		Drawing.drawing.setColor(this.colorR * (1 - flash) + 255 * flash, this.colorG * (1 - flash), this.colorB * (1 - flash));

		if (forInterface)
			drawing.drawInterfaceModel(color_model, this.posX, this.posY, s * sizeMod, s * sizeMod, this.orientation);
		else
		{
			if (Game.enable3d)
				drawing.drawModel(color_model, this.posX, this.posY, 0, s, s, s, this.orientation);
			else
				drawing.drawModel(color_model, this.posX, this.posY, s, s, this.orientation);
		}

		if (this.health > 1 && this.size > 0 && !forInterface)
		{
			double size = s;
			for (int i = 1; i < health; i++)
			{
				if (Game.enable3d)
					drawing.drawModel(health_model,
							this.posX, this.posY, s / 4,
							size, size, s,
							this.orientation);
				else
					drawing.drawModel(health_model,
							this.posX, this.posY,
							size, size,
							this.orientation);

				size *= 1.1;
			}
		}

		Drawing.drawing.setColor(255, 255, 255);

		this.turret.draw(angle, forInterface, Game.enable3d, false);

		sizeMod = 0.5;

		Drawing.drawing.setColor(255, 255, 255);
		if (this.texture != null)
		{
			if (forInterface)
				drawing.drawInterfaceImage(this.texture, this.posX, this.posY, s * sizeMod, s * sizeMod);
			else
			{
				if (Game.enable3d)
					drawing.drawImage(this.angle, this.texture, this.posX, this.posY, 0.82 * s, s * sizeMod, s * sizeMod);
				else
					drawing.drawImage(this.angle, this.texture, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
		}

		Drawing.drawing.setColor(this.turret.colorR, this.turret.colorG, this.turret.colorB);
	}

	@Override
	public void draw() 
	{
		drawAge += Panel.frameFrequency;
		this.drawTank(false);
	}

	public void drawOutline() 
	{
		drawAge = Game.tile_size;
		Drawing drawing = Drawing.drawing;

		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 127);
		drawing.fillRect(this.posX - this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX + this.size * 0.4, this.posY, this.size * 0.2, this.size);
		drawing.fillRect(this.posX, this.posY - this.size * 0.4, this.size * 0.6, this.size * 0.2);
		drawing.fillRect(this.posX, this.posY + this.size * 0.4, this.size * 0.6, this.size * 0.2);

		this.turret.draw(angle, false, false, true);

		if (this.texture != null)
		{
			Drawing.drawing.setColor(255, 255, 255, 127);
			drawing.drawImage(this.texture, this.posX, this.posY, this.size / 2, this.size / 2);
		}

		Drawing.drawing.setColor(this.turret.colorR, this.turret.colorG, this.turret.colorB);
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

	@Override
	public void addAttribute(AttributeModifier m)
	{
		super.addAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventTankAddAttributeModifier(this, m, false));
	}

	@Override
	public void addUnduplicateAttribute(AttributeModifier m)
	{
		super.addUnduplicateAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventTankAddAttributeModifier(this, m, true));
	}

	public void onDestroy()
	{

	}

	@Override
	public Face[] getHorizontalFaces()
	{
		double s = this.size / 2;

		if (this.horizontalFaces == null)
		{
			this.horizontalFaces = new Face[2];
			this.horizontalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX + s, this.posY - s, true, true, true, true);
			this.horizontalFaces[1] = new Face(this, this.posX - s, this.posY + s, this.posX + s, this.posY + s, true, false,true, true);
		}
		else
		{
			this.horizontalFaces[0].update(this.posX - s, this.posY - s, this.posX + s, this.posY - s);
			this.horizontalFaces[1].update(this.posX - s, this.posY + s, this.posX + s, this.posY + s);
		}

		return this.horizontalFaces;
	}

	@Override
	public Face[] getVerticalFaces()
	{
		double s = this.size / 2;

		if (this.verticalFaces == null)
		{
			this.verticalFaces = new Face[2];
			this.verticalFaces[0] = new Face(this, this.posX - s, this.posY - s, this.posX - s, this.posY + s, false, true, true, true);
			this.verticalFaces[1] = new Face(this, this.posX + s, this.posY - s, this.posX + s, this.posY + s, false, false, true, true);
		}
		else
		{
			this.verticalFaces[0].update(this.posX - s, this.posY - s, this.posX - s, this.posY + s);
			this.verticalFaces[1].update(this.posX + s, this.posY - s, this.posX + s, this.posY + s);
		}

		return this.verticalFaces;
	}

	public void setEffectHeight(Effect e)
	{
		if (Game.enable3d && Game.enable3dBg && Game.fancyGraphics)
		{
			e.posZ = Math.max(e.posZ, Game.sampleHeight(e.posX - e.size / 2, e.posY - e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleHeight(e.posX + e.size / 2, e.posY - e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleHeight(e.posX - e.size / 2, e.posY + e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleHeight(e.posX + e.size / 2, e.posY + e.size / 2));
		}
		else
			e.posZ = 1;
	}

	public void processRecoil(double recoil)
	{
		if (this.vX * this.vX + this.vY * this.vY > this.maxSpeed * this.maxSpeed)
		{
			this.tookRecoil = true;
			this.inControlOfMotion = false;
			this.recoilSpeed = 25.0 / 16.0 * recoil;
		}
	}
}

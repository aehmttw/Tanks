package tanks.tank;

import basewindow.Model;
import basewindow.ModelPart;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventTankAddAttributeModifier;
import tanks.event.EventTankUpdate;
import tanks.event.EventTankUpdateHealth;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Tank extends Movable implements ISolidObject
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<>();
	public static HashMap<Integer, Tank> idMap = new HashMap<>();

	public static Model base_model;
	public static Model color_model;

	public static ModelPart health_model;

	public Model baseModel = base_model;
	public Model colorModel = color_model;
	public Model turretBaseModel = Turret.base_model;
	public Model turretModel = Turret.turret_model;

	public double angle = 0;
	public double pitch = 0;

	public boolean depthTest = true;

	public boolean invulnerable = false;
	public boolean targetable = true;

	public boolean resistExplosions = false;
	public boolean resistBullets = false;

	public boolean disabled = false;
	public boolean inControlOfMotion = true;
	public boolean positionLock = false;

	public boolean tookRecoil = false;
	public double recoilSpeed = 0;

	public int coinValue = 0;

	public int networkID;
	public int crusadeID = -1;

	public String name;

	public String description = "";

	public double cooldown = 0;
	public double acceleration = 0.05;
	public double accelerationModifier = 1;
	public double frictionModifier = 1;
	public double maxSpeedModifier = 1;
	public double maxSpeed = 1.5;
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

	public double hitboxSize = 0.95;

	public double baseHealth = 1;
	public double health = 1;

	/** Whether this tank needs to be destroyed before the level ends. */
	public boolean needsToKill = true;

	public boolean[][] hiddenPoints = new boolean[3][3];
	public boolean hidden = false;

	public boolean[][] canHidePoints = new boolean[3][3];
	public boolean canHide = false;

	public Turret turret;

	public boolean standardUpdateEvent = true;

	public Face[] horizontalFaces;
	public Face[] verticalFaces;

	public HashMap<String, Object> extraProperties = new HashMap<>();

	public boolean isBoss = false;
	public Tank possessor;
	public boolean overridePossessedKills = true;

	public long lastFarthestInSightUpdate = 0;
	public Tank lastFarthestInSight = null;

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

		if (countID && ScreenPartyHost.isServer)
			this.registerNetworkID();
		else
			this.networkID = -1;
	}

	public void registerNetworkID()
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
			Movable m = Game.movables.get(i);

			if (m.skipNextUpdate)
				continue;

			if (this != m && m instanceof Tank && ((Tank)m).size > 0)
			{
				Tank t = (Tank) m;
				double distSq = Math.pow(this.posX - m.posX, 2) + Math.pow(this.posY - m.posY, 2);

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

					if (distSq > Math.pow((this.posX + this.vX) - (t.posX + t.vX), 2) + Math.pow((this.posY + this.vY) - (t.posY + t.vY), 2))
					{
						this.setMotionInDirection(t.posX, t.posY, newV);
						this.addPolarMotion(angle + Math.PI / 2, ourPerpV);

						t.setMotionInDirection(this.posX, this.posY, -newV);
						t.addPolarMotion(angle + Math.PI / 2, theirPerpV);
					}
				}
			}
		}

		hasCollided = false;

		this.size *= this.hitboxSize;

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

			if (!o.tankCollision && !o.checkForObjects || o.startHeight > 1)
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

				if (!o.hasLeftNeighbor() && dx <= 0 && dx >= -bound && horizontalDist >= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX += horizontalDist - bound;
				}
				else if (!o.hasUpperNeighbor() && dy <= 0 && dy >= -bound && horizontalDist <= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vY = -this.vY;
					else
						this.vY = 0;
					this.posY += verticalDist - bound;
				}
				else if (!o.hasRightNeighbor() && dx >= 0 && dx <= bound && horizontalDist >= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX -= horizontalDist - bound;
				}
				else if (!o.hasLowerNeighbor() && dy >= 0 && dy <= bound && horizontalDist <= verticalDist)
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

		this.size /= this.hitboxSize;
	}

	@Override
	public void update()
	{
		this.treadAnimation += Math.sqrt(this.lastFinalVX * this.lastFinalVX + this.lastFinalVY * this.lastFinalVY) * Panel.frameFrequency;

		if (this.treadAnimation > this.size * 2 / 5 && !this.destroy && !ScreenGame.finished)
		{
			this.drawTread = true;

			if (this.size > 0)
				this.treadAnimation %= this.size * 2 / 5;
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

				if (Game.effectsEnabled)
				{
					for (int i = 0; i < this.size * 2 * Game.effectMultiplier; i++)
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
			this.drawTread();
		}

		this.accelerationModifier = 1;
		this.frictionModifier = 1;
		this.maxSpeedModifier = 1;

		double boost = 0;
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
			else if (a.name.equals("boost_effect"))
				boost = a.getValue(boost);
		}

		if (Math.random() * Panel.frameFrequency < boost * Game.effectMultiplier && Game.effectsEnabled)
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, Game.tile_size / 2, Effect.EffectType.piece);
			double var = 50;

			e.colR = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
			e.colG = Math.min(255, Math.max(0, 180 + Math.random() * var - var / 2));
			e.colB = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));

			if (Game.enable3d)
				e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random());
			else
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random());

			Game.effects.add(e);
		}

		super.update();

		if (this.health <= 0)
			this.destroy = true;

		this.checkCollision();

		this.orientation = (this.orientation + Math.PI * 2) % (Math.PI * 2);

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

		if (this.possessor != null)
			this.possessor.updatePossessing();
	}

	public void drawTread()
	{
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
		this.setEffectHeight(e1);
		this.setEffectHeight(e2);
		Game.tracks.add(e1);
		Game.tracks.add(e2);
	}

	public void drawForInterface(double x, double y, double sizeMul)
	{
		double s = this.size;

		if (this.size > Game.tile_size * 1.5)
			this.size = Game.tile_size * 1.5;

		this.size *= sizeMul;
		this.turret.length *= this.size / s;
		this.drawForInterface(x, y);
		this.turret.length *= s / this.size;

		this.size = s;
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
		double glow = 0.5;

		double s = (this.size * (Game.tile_size - destroyTimer) / Game.tile_size) * Math.min(this.drawAge / Game.tile_size, 1);
		double sizeMod = 1;

		if (forInterface)
			s = Math.min(this.size, Game.tile_size * 1.5);

		Drawing drawing = Drawing.drawing;
		double[] teamColor = Team.getObjectColor(this.turret.colorR, this.turret.colorG, this.turret.colorB, this);

		for (int i = 0; i < this.attributes.size(); i++)
		{
			AttributeModifier a = this.attributes.get(i);
			if (a.type.equals("glow"))
			{
				glow = a.getValue(glow);
			}
		}

		Drawing.drawing.setColor(teamColor[0] * glow * 2, teamColor[1] * glow * 2, teamColor[2] * glow * 2, 255, 1);

		if (Game.glowEnabled)
		{
			double size = 4 * s;
			if (forInterface)
				Drawing.drawing.fillInterfaceGlow(this.posX, this.posY, size, size);
			else if (!Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, size, size);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, Math.max(this.size / 4, 11), size, size,true, false);
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
							Drawing.drawing.setColor(0, 255, 0, 255, 1);
							drawing.drawModel(this.baseModel, this.posX, this.posY, s * mod, s * mod, this.orientation);
						}
						else
						{
							Drawing.drawing.setColor(0, 255, 0, 127, 1);
							drawing.drawModel(this.baseModel, this.posX, this.posY, this.posZ, s * mod, s * mod, s - 2, this.orientation);
						}
					}
				}
			}
		}

		Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2], 255, glow);

		if (forInterface)
			drawing.drawInterfaceModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		else
		{
			if (Game.enable3d)
				drawing.drawModel(this.baseModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation);
			else
				drawing.drawModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		}


		double flash = Math.min(1, this.flashAnimation);

		Drawing.drawing.setColor(this.colorR * (1 - flash) + 255 * flash, this.colorG * (1 - flash), this.colorB * (1 - flash), 255, glow);

		if (forInterface)
			drawing.drawInterfaceModel(this.colorModel, this.posX, this.posY, s * sizeMod, s * sizeMod, this.orientation);
		else
		{
			if (Game.enable3d)
				drawing.drawModel(this.colorModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation);
			else
				drawing.drawModel(this.colorModel, this.posX, this.posY, s, s, this.orientation);
		}

		if (this.health > 1 && this.size > 0 && !forInterface)
		{
			double size = s;
			for (int i = 1; i < Math.min(health, 6); i++)
			{
				if (Game.enable3d)
					drawing.drawModel(health_model,
							this.posX, this.posY, this.posZ + s / 4,
							size, size, s,
							this.orientation, 0, 0);
				else
					drawing.drawModel(health_model,
							this.posX, this.posY,
							size, size,
							this.orientation);

				size *= 1.1;
			}
		}

		this.drawTurret(forInterface, Game.enable3d, false);

		sizeMod = 0.5;

		Drawing.drawing.setColor(255, 255, 255, 255, glow);
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

		/*Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.setFontSize(24);
		Drawing.drawing.drawText(posX, posY, 50, networkID + "");
		*/
		Drawing.drawing.setColor(this.turret.colorR, this.turret.colorG, this.turret.colorB);
	}

	public void drawTurret(boolean forInterface, boolean in3d, boolean transparent)
	{
		this.turret.draw(angle, pitch, forInterface, in3d, transparent);
	}

	@Override
	public void draw()
	{
		if (!Game.game.window.drawingShadow)
			drawAge += Panel.frameFrequency;

		this.drawTank(false);

		if (this.possessor != null)
		{
			this.possessor.drawPossessing();
			this.possessor.drawGlowPossessing();
		}
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

		this.drawTurret(false, false, true);

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

	public void drawOutlineAt(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawOutline();
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
		double s = this.size * this.hitboxSize / 2;

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
		double s = this.size * this.hitboxSize / 2;

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

	public boolean damage(double amount, IGameObject source)
	{
		this.health -= amount * this.getDamageMultiplier(source);

		if (this.health <= 1)
		{
			for (int i = 0; i < this.attributes.size(); i++)
			{
				if (this.attributes.get(i).type.equals("healray"))
				{
					this.attributes.remove(i);
					i--;
				}
			}
		}

		Game.eventsOut.add(new EventTankUpdateHealth(this));

		Tank owner = null;

		if (source instanceof Bullet)
			owner = ((Bullet) source).tank;
		else if (source instanceof Mine)
			owner = ((Mine) source).tank;
		else if (source instanceof Tank)
			owner = (Tank) source;

		if (this.health > 0)
			this.flashAnimation = 1;

		this.checkHit(owner, source);

		if (this.health > 6 && (int) (this.health + amount) != (int) (this.health))
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ + this.size * 0.75, Effect.EffectType.shield);
			e.size = this.size;
			e.radius = this.health - 1;
			Game.effects.add(e);
		}

		return this.health <= 0;
	}

	public void checkHit(Tank owner, IGameObject source)
	{
		if (Crusade.crusadeMode && Crusade.currentCrusade != null && !ScreenPartyLobby.isClient)
		{
			if (owner instanceof IServerPlayerTank)
			{
				CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(((IServerPlayerTank) owner).getPlayer());

				if (cp != null && this.health <= 0)
				{
					if (this.possessor != null && this.possessor.overridePossessedKills)
						cp.addKill(this.possessor);
					else
						cp.addKill(this);
				}

				if (cp != null && (source instanceof Bullet || source instanceof Mine))
					cp.addItemHit(source);
			}

			if (owner != null && this instanceof IServerPlayerTank && this.health <= 0)
			{
				CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(((IServerPlayerTank) this).getPlayer());

				if (cp != null)
				{
					if (owner.possessor != null && owner.possessor.overridePossessedKills)
						cp.addDeath(owner.possessor);
					else
						cp.addDeath(owner);
				}
			}
		}
	}

	public double getDamageMultiplier(IGameObject source)
	{
		if (this.invulnerable || (source instanceof Bullet && this.resistBullets) || (source instanceof Mine && this.resistExplosions))
			return 0;

		return 1;
	}

	public void setEffectHeight(Effect e)
	{
		if (Game.enable3d && Game.enable3dBg && Game.glowEnabled)
		{
			e.posZ = Math.max(e.posZ, Game.sampleTerrainGroundHeight(e.posX - e.size / 2, e.posY - e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleTerrainGroundHeight(e.posX + e.size / 2, e.posY - e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleTerrainGroundHeight(e.posX - e.size / 2, e.posY + e.size / 2));
			e.posZ = Math.max(e.posZ, Game.sampleTerrainGroundHeight(e.posX + e.size / 2, e.posY + e.size / 2));
			e.posZ++;
		}
		else
			e.posZ = 1;
	}

	public void processRecoil()
	{
		if (this.vX * this.vX + this.vY * this.vY > Math.pow(this.maxSpeed * this.maxSpeedModifier, 2) * 1.0001 && !this.positionLock)
		{
			this.tookRecoil = true;
			this.inControlOfMotion = false;
			this.recoilSpeed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);
		}
	}

	public void updatePossessing()
	{

	}

	public void drawPossessing()
	{

	}

	public void drawGlowPossessing()
	{

	}

	public double getAutoZoomRaw()
	{
		double nearest = Double.MAX_VALUE;

		double farthestInSight = -1;

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank && !Team.isAllied(m, this) && m != this && !((Tank) m).hidden && !m.destroy)
			{
				double boundedX = Math.min(Math.max(this.posX, Drawing.drawing.interfaceSizeX * 0.4),
						Game.currentSizeX * Game.tile_size - Drawing.drawing.interfaceSizeX * 0.4);
				double boundedY = Math.min(Math.max(this.posY, Drawing.drawing.interfaceSizeY * 0.4),
						Game.currentSizeY * Game.tile_size - Drawing.drawing.interfaceSizeY * 0.4);

				double xDist = Math.abs(m.posX - boundedX);
				double yDist = Math.abs(m.posY - boundedY);
				double dist = Math.max(xDist / (Drawing.drawing.interfaceSizeX),
						yDist / (Drawing.drawing.interfaceSizeY)) * 3;

				if (dist < nearest)
				{
					nearest = dist;
				}

				if (dist <= 3 && dist > farthestInSight)
				{
					Ray r = new Ray(this.posX, this.posY, 0, 0, this);
					r.vX = m.posX - this.posX;
					r.vY = m.posY - this.posY;

					if ((m == this.lastFarthestInSight && System.currentTimeMillis() - this.lastFarthestInSightUpdate <= 1000)
							|| r.getTarget() == m)
					{
						farthestInSight = dist;
						this.lastFarthestInSight = (Tank) m;
						this.lastFarthestInSightUpdate = System.currentTimeMillis();
					}
				}
			}
		}

		return Math.max(nearest, farthestInSight);
	}

	public double getAutoZoom()
	{
		double dist = Math.min(2.5, Math.max(1, getAutoZoomRaw()));
		return 1 / dist;
	}
}

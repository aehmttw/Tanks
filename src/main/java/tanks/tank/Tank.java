package tanks.tank;

import basewindow.Model;
import basewindow.ModelPart;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.network.event.EventTankAddAttributeModifier;
import tanks.network.event.EventTankUpdate;
import tanks.network.event.EventTankUpdateHealth;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;
import tanks.tankson.Property;

import static tanks.tank.TankPropertyCategory.*;

import java.util.*;

public abstract class Tank extends Movable implements ISolidObject
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<>();
	public static HashMap<Integer, Tank> idMap = new HashMap<>();

	public static ModelPart health_model;

	public boolean fromRegistry = false;

	@Property(category = appearanceBody, id = "color_model", name = "Tank body model", miscType = Property.MiscType.colorModel)
	public Model colorModel = TankModels.tank.color;
	@Property(category = appearanceTreads, id = "base_model", name = "Tank treads model", miscType = Property.MiscType.baseModel)
	public Model baseModel = TankModels.tank.base;
	@Property(category = appearanceTurretBase, id = "turret_base_model", name = "Turret base model", miscType = Property.MiscType.turretBaseModel)
	public Model turretBaseModel = TankModels.tank.turretBase;
	@Property(category = appearanceTurretBarrel, id = "turret_model", name = "Turret barrel model", miscType = Property.MiscType.turretModel)
	public Model turretModel = TankModels.tank.turret;

	public double angle = 0;
	public double pitch = 0;

	public boolean depthTest = true;

	public boolean invulnerable = false;
	public boolean targetable = true;
	public double invulnerabilityTimer = 0;

	public boolean disabled = false;
	public boolean inControlOfMotion = true;
	public boolean positionLock = false;

	public boolean fullBrightness = false;

	public boolean tookRecoil = false;
	public double recoilSpeed = 0;

	public HashSet<ClippedTile> clippedTiles = new HashSet<>();
	public HashSet<ClippedTile> stillClippedTiles = new HashSet<>();

	/** If spawned by another tank, set to the tank that spawned this tank*/
	protected Tank parent = null;

	@Property(category = general, id = "name", name = "Tank name", miscType = Property.MiscType.name)
	public String name;

	@Property(category = general, id = "coin_value", name = "Coin value")
	public int coinValue = 0;

	@Property(category = general, minValue = 0.0, id = "base_health", name = "Hitpoints", desc = "The default bullet does one hitpoint of damage")
	public double baseHealth = 1;
	public double health = 1;

	@Property(category = general, id = "resist_bullets", name = "Bullet immunity")
	public boolean resistBullets = false;
	@Property(category = general, id = "resist_explosions", name = "Explosion immunity")
	public boolean resistExplosions = false;
	@Property(category = general, id = "resist_freezing", name = "Freezing immunity")
	public boolean resistFreeze = false;

	public int networkID = -1;
	public int crusadeID = -1;

	@Property(category = general, id = "description", name = "Tank description", miscType = Property.MiscType.description)
	public String description = "";

	@Property(category = movementGeneral, id = "max_speed", name = "Top speed", minValue = 0.0)
	public double maxSpeed = 1.5;

	@Property(category = movementGeneral, id = "acceleration", name = "Acceleration", minValue = 0.0)
	public double acceleration = 0.05;

	@Property(category = movementGeneral, id = "friction", name = "Friction", minValue = 0.0, maxValue = 1.0)
	public double friction = 0.05;

	public double accelerationModifier = 1;
	public double frictionModifier = 1;
	public double maxSpeedModifier = 1;

	@Property(category = appearanceGeneral, id = "size", name = "Tank size", minValue = 0.0, desc = "1 tile = 50 units")
	public double size;

	@Property(category = appearanceBody, id = "color_r", name = "Red", miscType = Property.MiscType.color)
	public double colorR;
	@Property(category = appearanceBody, id = "color_g", name = "Green", miscType = Property.MiscType.color)
	public double colorG;
	@Property(category = appearanceBody, id = "color_b", name = "Blue", miscType = Property.MiscType.color)
	public double colorB;

	@Property(category = appearanceGlow, id = "glow_intensity", name = "Aura intensity", minValue = 0.0)
	public double glowIntensity = 1;
	@Property(category = appearanceGlow, id = "glow_size", name = "Aura size", minValue = 0.0)
	public double glowSize = 4;
	@Property(category = appearanceGlow, id = "light_intensity", name = "Light intensity", minValue = 0.0)
	public double lightIntensity = 1;
	@Property(category = appearanceGlow, id = "light_size", name = "Light size", minValue = 0.0)
	public double lightSize = 0;
	@Property(category = appearanceGlow, id = "luminance", name = "Tank luminance", minValue = 0.0, maxValue = 1.0, desc = "How bright the tank will be in dark lighting. At 0, the tank will be shaded like terrain by lighting. At 1, the tank will always be fully bright.")
	public double luminance = 0.5;

	/** Important: this option only is useful for the tank editor. Secondary color will be treated independently even if disabled. */
	@Property(category = appearanceTurretBarrel, id = "enable_color2", name = "Custom color", miscType = Property.MiscType.color)
	public boolean enableSecondaryColor = false;
	@Property(category = appearanceTurretBarrel, id = "color_r2", name = "Red", miscType = Property.MiscType.color)
	public double secondaryColorR;
	@Property(category = appearanceTurretBarrel, id = "color_g2", name = "Green", miscType = Property.MiscType.color)
	public double secondaryColorG;
	@Property(category = appearanceTurretBarrel, id = "color_b2", name = "Blue", miscType = Property.MiscType.color)
	public double secondaryColorB;
	@Property(category = appearanceTurretBarrel, id = "turret_size", name = "Turret thickness", minValue = 0.0)
	public double turretSize = 8;
	@Property(category = appearanceTurretBarrel, id = "turret_length", name = "Turret length", minValue = 0.0)
	public double turretLength = Game.tile_size;
	@Property(category = appearanceTurretBarrel, id = "multiple_turrets", name = "Multiple turrets", desc = "If enabled, the turret will reflect the bullet multishot count")
	public boolean multipleTurrets = true;

	/** Important: tertiary color values will not be used unless this option is set to true! */
	@Property(category = appearanceTurretBase, id = "enable_color3", name = "Custom color", miscType = Property.MiscType.color)
	public boolean enableTertiaryColor = false;
	@Property(category = appearanceTurretBase, id = "color_r3", name = "Red", miscType = Property.MiscType.color)
	public double tertiaryColorR;
	@Property(category = appearanceTurretBase, id = "color_g3", name = "Green", miscType = Property.MiscType.color)
	public double tertiaryColorG;
	@Property(category = appearanceTurretBase, id = "color_b3", name = "Blue", miscType = Property.MiscType.color)
	public double tertiaryColorB;

	@Property(category = appearanceTracks, id = "enable_tracks", name = "Lays tracks")
	public boolean enableTracks = true;
	@Property(category = appearanceTracks, id = "track_spacing", name = "Track spacing", minValue = 0.0)
	public double trackSpacing = 0.4;

	/** The bullet a tank uses. If you want to change this, make sure to use setBullet() because it also updates the bulletItem. */
	@Property(category = firingGeneral, id = "bullet", name = "Bullet")
	public Bullet bullet = TankPlayer.default_bullet.clonePropertiesTo(new Bullet());
	public ItemBullet.ItemStackBullet bulletItem = new ItemBullet.ItemStackBullet(null, new ItemBullet(this.bullet), -1);

	/** The mine a tank uses. If you want to change this, make sure to use setMine() because it also updates the mineItem. */
	@Property(category = mines, id = "mine", name = "Mine")
	public Mine mine = TankPlayer.default_mine.clonePropertiesTo(new Mine());
	public ItemMine.ItemStackMine mineItem = new ItemMine.ItemStackMine(null, new ItemMine(this.mine), -1);

	/** Age in frames*/
	protected double age = 0;
	/** A tank will spawn other tanks on the second frame it updates if it was spawned by another tank, to prevent infinite loop for recursively spawning tanks*/
	protected boolean readyForInitialSpawn = true;

	public double drawAge = 0;
	public double destroyTimer = 0;
	public boolean hasCollided = false;
	public double damageFlashAnimation = 0;
	public double healFlashAnimation = 0;
	public double treadAnimation = 0;
	public boolean drawTread = false;

	@Property(category = appearanceEmblem, id = "emblem", name = "Tank emblem", miscType = Property.MiscType.emblem)
	public String emblem = null;
	@Property(category = appearanceEmblem, id = "emblem_r", name = "Red", miscType = Property.MiscType.color)
	public double emblemR;
	@Property(category = appearanceEmblem, id = "emblem_g", name = "Green", miscType = Property.MiscType.color)
	public double emblemG;
	@Property(category = appearanceEmblem, id = "emblem_b", name = "Blue", miscType = Property.MiscType.color)
	public double emblemB;

	public double orientation = 0;

	public double hitboxSize = 0.95;

	@Property(category = general, id = "explode_on_destroy", name = "Explosive", desc="If set, the tank will explode when destroyed. The explosion will use the properties of the tank's mine.")
	public boolean explodeOnDestroy = false;

	public boolean droppedFromCrate = false;

	/** Whether this tank needs to be destroyed before the level ends. */
	@Property(category = general, id = "mandatory_kill", name = "Must be destroyed", desc="Whether the tank needs to be destroyed to clear the level")
	public boolean mandatoryKill = true;

	/** Used for custom tanks, see /music/tank for built-in tanks */
	@Property(category = general, id = "music", name = "Music tracks", miscType = Property.MiscType.music)
	public HashSet<String> musicTracks = new HashSet<>();

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
	public Tank possessingTank = null;
	public boolean overridePossessedKills = true;

	public long lastFarthestInSightUpdate = 0;
	public Tank lastFarthestInSight = null;

	public Tank(String name, double x, double y, double size, double r, double g, double b)
	{
		super(x, y);
		this.size = size;
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
		turret = new Turret(this);
		this.name = name;
		this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, this.name);

		this.drawLevel = 4;
	}

	public void unregisterNetworkID()
	{
		if (idMap.get(this.networkID) == this)
			idMap.remove(this.networkID);

		if (!freeIDs.contains(this.networkID))
		{
			freeIDs.add(this.networkID);
		}
	}

	public static int nextFreeNetworkID()
	{
		if (freeIDs.size() > 0)
			return freeIDs.remove(0);
		else
		{
			currentID++;
			return currentID - 1;
		}
	}

	public void registerNetworkID()
	{
		if (ScreenPartyLobby.isClient)
			Game.exitToCrash(new RuntimeException("Do not automatically assign network IDs on client!"));

		this.networkID = nextFreeNetworkID();
		idMap.put(this.networkID, this);
	}

	public void setNetworkID(int id)
	{
		this.networkID = id;
		idMap.put(id, this);
	}

	/**
	 * Always use this to change a tank's bullet
	 * @param b the bullet to set the tank to use
	 */
	public void setBullet(Bullet b)
	{
		this.bullet = b.getCopy();
		this.bulletItem.item.bullet = this.bullet;
	}

	/**
	 * Always use this to change a tank's mine
	 * @param m the mine to set the tank to use
	 */
	public void setMine(Mine m)
	{
		this.mine = m.getCopy();
		this.mineItem.item.mine = this.mine;
	}

	public void fireBullet(Bullet b, double speed, double offset)
	{

	}

	public void layMine(Mine m)
	{

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

			if (this != m && m instanceof Tank && ((Tank)m).size > 0 && !m.destroy)
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

		this.clippedTiles.clear();
		this.clippedTiles.addAll(this.stillClippedTiles);
		this.stillClippedTiles.clear();

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

				if (this.stillClips(o.posX, o.posY))
					continue;
				if (o.shouldClip)
				{
					ClippedTile c = new ClippedTile((int) (o.posX / Game.tile_size), (int) (o.posY / Game.tile_size));
					this.stillClippedTiles.add(c);
					this.clippedTiles.add(c);
					continue;
				}

				if ((!o.hasLeftNeighbor() || this.clips(o.posX - Game.tile_size, o.posY)) && dx <= 0 && dx >= -bound && horizontalDist >= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX += horizontalDist - bound;
				}
				else if ((!o.hasUpperNeighbor() || this.clips(o.posX, o.posY - Game.tile_size)) && dy <= 0 && dy >= -bound && horizontalDist <= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vY = -this.vY;
					else
						this.vY = 0;
					this.posY += verticalDist - bound;
				}
				else if ((!o.hasRightNeighbor() || this.clips(o.posX + Game.tile_size, o.posY))&& dx >= 0 && dx <= bound && horizontalDist >= verticalDist)
				{
					hasCollided = true;
					if (bouncy)
						this.vX = -this.vX;
					else
						this.vX = 0;
					this.posX -= horizontalDist - bound;
				}
				else if ((!o.hasLowerNeighbor() || this.clips(o.posX, o.posY + Game.tile_size)) && dy >= 0 && dy <= bound && horizontalDist <= verticalDist)
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
		if (this.networkID < 0)
		{
			// If you get this crash, please make sure you call Game.addTank() to add them to movables, or use registerNetworkID()!
			Game.exitToCrash(new RuntimeException("Network ID not assigned to tank!"));
		}

		this.bulletItem.item.bullet = this.bullet;
		this.mineItem.item.mine = this.mine;

		this.age += Panel.frameFrequency;
		this.invulnerabilityTimer = Math.max(0, this.invulnerabilityTimer - Panel.frameFrequency);

		this.treadAnimation += Math.sqrt(this.lastFinalVX * this.lastFinalVX + this.lastFinalVY * this.lastFinalVY) * Panel.frameFrequency;

		if (this.enableTracks && this.treadAnimation > this.size * this.trackSpacing && !this.destroy)
		{
			this.drawTread = true;

			if (this.size > 0)
				this.treadAnimation %= this.size * this.trackSpacing;
		}

		if (this.resistFreeze)
		{
			this.attributeImmunities.addAll(Arrays.asList("ice_slip", "ice_accel", "ice_max_speed", "freeze"));
		}

		this.damageFlashAnimation = Math.max(0, this.damageFlashAnimation - 0.05 * Panel.frameFrequency);
		this.healFlashAnimation = Math.max(0, this.healFlashAnimation - 0.05 * Panel.frameFrequency);

		if (destroy)
		{
			if (this.destroyTimer <= 0)
			{
				Game.eventsOut.add(new EventTankUpdateHealth(this));
				this.unregisterNetworkID();
			}

			if (this.destroyTimer <= 0 && this.health <= 0)
			{
				Drawing.drawing.playSound("destroy.ogg", (float) (Game.tile_size / this.size));

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
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.atan(Math.random()), Math.random() * this.size / 50.0);
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
		}


		this.accelerationModifier = this.getAttributeValue(AttributeModifier.acceleration, this.accelerationModifier);

		if (!(this instanceof TankAIControlled))
			this.frictionModifier = this.getAttributeValue(AttributeModifier.friction, this.frictionModifier);

		this.maxSpeedModifier = this.getAttributeValue(AttributeModifier.max_speed, this.maxSpeedModifier);

		boost = this.getAttributeValue(AttributeModifier.ember_effect, boost);

		if (Math.random() * Panel.frameFrequency < boost * Game.effectMultiplier && Game.effectsEnabled && !ScreenGame.finishedQuick)
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

		if (this.health <= 0.00000001)
		{
			this.destroy = true;
			this.health = 0;
		}

		if (this.managedMotion)
		{
			this.checkCollision();

			this.orientation = (this.orientation + Math.PI * 2) % (Math.PI * 2);

			if (!(Math.abs(this.posX - this.lastPosX) < 0.01 && Math.abs(this.posY - this.lastPosY) < 0.01) && !this.destroy && !ScreenGame.finished)
			{
				double dist = Math.sqrt(Math.pow(this.posX - this.lastPosX, 2) + Math.pow(this.posY - this.lastPosY, 2));

				double dir = Math.PI + this.getAngleInDirection(this.lastPosX, this.lastPosY);
				if (Movable.absoluteAngleBetween(this.orientation, dir) <= Movable.absoluteAngleBetween(this.orientation + Math.PI, dir))
					this.orientation -= Movable.angleBetween(this.orientation, dir) / 20 * dist;
				else
					this.orientation -= Movable.angleBetween(this.orientation + Math.PI, dir) / 20 * dist;
			}
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
			this.recoilSpeed *= 0.5;
		}

		if (this.possessor != null)
			this.possessor.updatePossessing();
	}

	public void drawTread()
	{
		double a = this.orientation;
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
		e1.firstDraw();
		e2.firstDraw();
		Game.tracks.add(e1);
		Game.tracks.add(e2);
	}

	public void drawForInterface(double x, double y, double sizeMul)
	{
		double s = this.size;

		if (this.size > Game.tile_size * 1.5)
			this.size = Game.tile_size * 1.5;

		this.size *= sizeMul;
		this.drawForInterface(x, y);
		this.size = s;
	}

	@Override
	public void drawForInterface(double x, double y)
	{
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawTank(true, false);
		this.posX = x1;
		this.posY = y1;	
	}

	public void drawTank(boolean forInterface, boolean interface3d)
	{
		double luminance = this.getAttributeValue(AttributeModifier.glow, this.luminance);
		double glow = this.getAttributeValue(AttributeModifier.glow, 1);

		double s = (this.size * (Game.tile_size - destroyTimer) / Game.tile_size) * Math.min(this.drawAge / Game.tile_size, 1);
		double sizeMod = 1;

		if (forInterface && !interface3d)
			s = Math.min(this.size, Game.tile_size * 1.5);

		Drawing drawing = Drawing.drawing;
		double[] teamColor = Team.getObjectColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB, this);

		Drawing.drawing.setColor(teamColor[0] * glow * this.glowIntensity, teamColor[1] * glow * this.glowIntensity, teamColor[2] * glow * this.glowIntensity, 255, 1);

		if (Game.glowEnabled)
		{
			double size = this.glowSize * s;
			if (forInterface)
				Drawing.drawing.fillInterfaceGlow(this.posX, this.posY, size, size);
			else if (!Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, size, size);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, Math.max(this.size / 4, 11), size, size,true, false);
		}

		if (this.lightIntensity > 0 && this.lightSize > 0)
		{
			double i = this.lightIntensity;

			while (i > 0)
			{
				double size = this.lightSize * s * i / this.lightIntensity;
				Drawing.drawing.setColor(255, 255, 255, i * 255);

				if (!(forInterface && !interface3d))
					Drawing.drawing.fillForcedGlow(this.posX, this.posY, 0, size, size, false, false, false, true);

				i--;
			}
		}

		if (this.fullBrightness)
			luminance = 1;

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


		Drawing.drawing.setColor(teamColor[0], teamColor[1], teamColor[2], 255, luminance);

		if (forInterface)
		{
			if (interface3d)
				drawing.drawInterfaceModel(this.baseModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation, 0, 0);
			else
				drawing.drawInterfaceModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		}
		else
		{
			if (Game.enable3d)
				drawing.drawModel(this.baseModel, this.posX, this.posY, this.posZ, s, s, s, this.orientation);
			else
				drawing.drawModel(this.baseModel, this.posX, this.posY, s, s, this.orientation);
		}


		double dmgFlash = Math.min(1, this.damageFlashAnimation);
		double healFlash = Math.min(1, this.healFlashAnimation);

		Drawing.drawing.setColor(this.colorR * (1 - Math.max(dmgFlash, healFlash)) + 255 * dmgFlash, this.colorG * (1 - Math.max(dmgFlash, healFlash)) + 255 * healFlash, this.colorB * (1 - Math.max(dmgFlash, healFlash)), 255, luminance);

		if (forInterface)
		{
			if (interface3d)
				drawing.drawInterfaceModel(this.colorModel, this.posX, this.posY, this.posZ, s * sizeMod, s * sizeMod, s * sizeMod, this.orientation, 0, 0);
			else
				drawing.drawInterfaceModel(this.colorModel, this.posX, this.posY, s * sizeMod, s * sizeMod, this.orientation);
		}
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

		this.drawTurret(forInterface, interface3d || (!forInterface && Game.enable3d), false);

		sizeMod = 0.5;

		Drawing.drawing.setColor(this.emblemR, this.emblemG, this.emblemB, 255, luminance);
		if (this.emblem != null)
		{
			if (forInterface)
			{
				if (interface3d)
					drawing.drawInterfaceImage(0, this.emblem, this.posX, this.posY, 0.82 * s, s * sizeMod, s * sizeMod);
				else
					drawing.drawInterfaceImage(this.emblem, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
			else
			{
				if (Game.enable3d)
					drawing.drawImage(this.angle, this.emblem, this.posX, this.posY, 0.82 * s, s * sizeMod, s * sizeMod);
				else
					drawing.drawImage(this.angle, this.emblem, this.posX, this.posY, s * sizeMod, s * sizeMod);
			}
		}

		if (Game.showTankIDs)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setFontSize(30);
			Drawing.drawing.drawText(this.posX, this.posY, 50, this.networkID + "");
		}


		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
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

		this.drawTank(false, false);

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

		if (this.emblem != null)
		{
			Drawing.drawing.setColor(this.emblemR, this.emblemG, this.emblemB, 127);
			drawing.drawImage(this.angle, this.emblem, this.posX, this.posY, this.size / 2, this.size / 2);
		}

		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
	}

	public void drawAt(double x, double y)
	{	
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.drawTank(false, false);
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
		if (this.explodeOnDestroy && !(this.droppedFromCrate && this.age < 250))
		{
			Explosion e = new Explosion(this.posX, this.posY, this, this.mineItem);
			this.mine.explosion.clonePropertiesTo(e);
			e.explode();
		}
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
		double finalAmount = amount * this.getDamageMultiplier(source);
		this.health -= finalAmount;

		if (this.health <= 1)
		{
			for (int i = 0; i < this.attributes.size(); i++)
			{
				if (this.attributes.get(i).type.name.equals("healray"))
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
		else if (source instanceof Explosion)
			owner = ((Explosion) source).tank;
		else if (source instanceof Tank)
			owner = (Tank) source;

		if (this.health > 0)
		{
			if (finalAmount > 0)
				this.damageFlashAnimation = 1;
			else if (finalAmount < 0)
				this.healFlashAnimation = 1;
		}
		else
			this.destroy = true;

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
						cp.addKill(this.getTopLevelPossessor());
					else
						cp.addKill(this);
				}

				if (cp != null && (source instanceof Bullet || source instanceof Explosion))
				{
					if (source instanceof Bullet)
						cp.addItemHit(((Bullet) source).item);
					else
						cp.addItemHit(((Explosion) source).item);
				}
			}

			if (owner != null && this instanceof IServerPlayerTank && this.health <= 0)
			{
				CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(((IServerPlayerTank) this).getPlayer());

				if (cp != null)
				{
					if (owner.possessor != null && owner.possessor.overridePossessedKills)
						cp.addDeath(owner.getTopLevelPossessor());
					else
						cp.addDeath(owner);
				}
			}
		}
	}

	public double getDamageMultiplier(IGameObject source)
	{
		if ((this.invulnerable || this.invulnerabilityTimer > 0) || (source instanceof Bullet && this.resistBullets) || (source instanceof Explosion && this.resistExplosions))
			return 0;

		return 1;
	}

	public void setEffectHeight(Effect e)
	{
		if (Game.enable3d && Game.enable3dBg)
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
		boolean farthestIsInSight = false;

		Movable nearestM = null;
		Movable farthestM = null;

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
				double dist = Math.max(xDist / (Drawing.drawing.interfaceSizeX), yDist / (Drawing.drawing.interfaceSizeY)) * 2.2;

				if (dist < nearest)
				{
					nearest = dist;
					nearestM = m;
				}

				if (dist > farthestInSight)
				{
					Ray r = new Ray(this.posX, this.posY, 0, 0, this);
					r.vX = m.posX - this.posX;
					r.vY = m.posY - this.posY;

					boolean isInSight = r.getTarget() == m;
					if ((m == this.lastFarthestInSight && System.currentTimeMillis() - this.lastFarthestInSightUpdate <= 1000) || isInSight)
					{
						farthestM = m;
						farthestInSight = dist;
						farthestIsInSight = false;

						if (isInSight)
						{
							farthestIsInSight = true;
							this.lastFarthestInSight = (Tank) m;
							this.lastFarthestInSightUpdate = System.currentTimeMillis();
						}
					}
				}
			}
		}

		if (Game.drawAutoZoom)
		{
			if (farthestM != null)
			{
				Effect e = Effect.createNewEffect(farthestM.posX, farthestM.posY, 50, Effect.EffectType.explosion);
				e.colR = 0;

				if (farthestIsInSight)
					e.colG = 255;
				else
					e.colB = 255;

				e.radius = 100;
				Game.effects.add(e);
			}

			if (nearestM != null)
			{
				Effect e = Effect.createNewEffect(nearestM.posX, nearestM.posY, 50, Effect.EffectType.explosion);
				e.radius = 100;
				Game.effects.add(e);
			}
		}

		return Math.max(nearest, farthestInSight);
	}

	public double getAutoZoom()
	{
		double dist = Math.min(4, Math.max(1, getAutoZoomRaw()));
		double targetScale = Drawing.drawing.interfaceScale / dist;
		return Math.max(Math.min((targetScale - Drawing.drawing.unzoomedScale) / Math.max(0.001, Drawing.drawing.interfaceScale - Drawing.drawing.unzoomedScale), 1), 0);
	}

	public void setBufferCooldown(double value)
	{
		this.bulletItem.cooldown = Math.max(this.bulletItem.cooldown, value);
		this.mineItem.cooldown = Math.max(this.mineItem.cooldown, value);
	}

	public Tank getTopLevelPossessor()
	{
		if (this.possessor == null)
			return null;
		else
		{
			Tank p = this.possessor;
			while (p.possessor != null)
			{
				p = p.possessor;
			}

			return p;
		}
	}

	public Tank getBottomLevelPossessing()
	{
		Tank p = this;
		while (p.possessingTank != null)
		{
			p = p.possessingTank;
		}

		return p;
	}

	public void drawSpinny(double s)
	{
		double fade = Math.max(0, Math.sin(Math.min(s, 50) / 100 * Math.PI));

		double frac = (System.currentTimeMillis() % 2000) / 2000.0;
		double size = Math.max(800 * (0.5 - frac), 0) * fade;
		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 64 * Math.sin(Math.min(frac * Math.PI, Math.PI / 2)) * fade);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.size / 2, size, size, false, false);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size, size);

		double frac2 = ((250 + System.currentTimeMillis()) % 2000) / 2000.0;
		double size2 = Math.max(800 * (0.5 - frac2), 0) * fade;

		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB, 64 * Math.sin(Math.min(frac2 * Math.PI, Math.PI / 2)) * fade);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.size / 2, size2, size2, false, false);
		else
			Drawing.drawing.fillOval(this.posX, this.posY, size2, size2);

		Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
		this.drawSpinny(this.posX, this.posY, this.size / 2, 200, 4, 0.3, 75 * fade, 0.5 * fade, false);
		Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);
		this.drawSpinny(this.posX, this.posY, this.size / 2, 198, 3, 0.5, 60 * fade, 0.375 * fade, false);
	}

	public void drawSpinny(double x, double y, double z, int max, int parts, double speed, double size, double dotSize, boolean invert)
	{
		for (int i = 0; i < max; i++)
		{
			double frac = (System.currentTimeMillis() / 1000.0 * speed + i * 1.0 / max) % 1;
			double s = Math.max(Math.abs((i % (max * 1.0 / parts)) / 10.0 * parts), 0);

			if (invert)
			{
				frac = -frac;
			}

			double v = size * Math.cos(frac * Math.PI * 2);
			double v1 = size * Math.sin(frac * Math.PI * 2);

			if (Game.enable3d)
				Drawing.drawing.fillOval(x + v, y + v1, z, s * dotSize, s * dotSize, false, false);
			else
				Drawing.drawing.fillOval(x + v, y + v1, s * dotSize, s * dotSize);
		}
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2)
	{
		drawTank(x, y, r1, g1, b1, r2, g2, b2, (r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2, Game.tile_size / 2);
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2, double size)
	{
		drawTank(x, y, r1, g1, b1, r2, g2, b2, (r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2, size);
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2, double r3, double g3, double b3)
	{
		drawTank(x, y, r1, g1, b1, r2, g2, b2, r3, g3, b3, Game.tile_size / 2);
	}

	public static void drawTank(double x, double y, double r1, double g1, double b1, double r2, double g2, double b2, double r3, double g3, double b3, double size)
	{
		Drawing.drawing.setColor(r2, g2, b2);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.base, x, y, size, size, 0);

		Drawing.drawing.setColor(r1, g1, b1);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.color, x, y, size, size, 0);

		Drawing.drawing.setColor(r2, g2, b2);

		Drawing.drawing.drawInterfaceModel(TankModels.tank.turret, x, y, size, size, 0);

		Drawing.drawing.setColor(r3, g3, b3);
		Drawing.drawing.drawInterfaceModel(TankModels.tank.turretBase, x, y, size, size, 0);
	}

	protected static class ClippedTile
	{
		public final int x;
		public final int y;

		public ClippedTile(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object c)
		{
			return c instanceof ClippedTile && ((ClippedTile) c).x == this.x && ((ClippedTile) c).y == this.y;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(x, y);
		}
	}

	public boolean clips(double x, double y)
	{
		if (this.clippedTiles.isEmpty())
			return false;
		else
			return this.clippedTiles.contains(new ClippedTile((int) (x / Game.tile_size), (int) (y / Game.tile_size)));
	}

	public boolean stillClips(double x, double y)
	{
		if (this.clippedTiles.isEmpty())
			return false;
		else
		{
			ClippedTile c = new ClippedTile((int) (x / Game.tile_size), (int) (y / Game.tile_size));
			if (this.clippedTiles.contains(c))
			{
				this.stillClippedTiles.add(c);
				return true;
			}
			return false;
		}
	}
}

package tanks.bullet;

import tanks.*;
import tanks.effect.AttributeModifier;
import tanks.effect.EffectManager;
import tanks.effect.StatusEffect;
import tanks.gui.ChatMessage;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.ItemBullet;
import tanks.minigames.Minigame;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleStackable;
import tanks.tank.*;
import tanks.tankson.*;

import java.util.ArrayList;
import java.util.HashMap;

@TanksONable("bullet")
public class Bullet extends Movable implements ICopyable<Bullet>, ITanksONEditable
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<>();
	public static HashMap<Integer, Bullet> idMap = new HashMap<>();

	public static String bullet_class_name = "bullet";

	public int networkID;

	public enum BulletEffect {none, trail, long_trail, fire, dark_fire, fire_trail, ice, ember}

	public static double bullet_size = 10;

	public String typeName;

	public boolean playPopSound = true;
	public boolean playBounceSound = true;
	public double age = 0;

	@Property(id = "size", minValue = 0.0, name = "Size", category = BulletPropertyCategory.appearance, desc = "1 tile = 50 units")
	public double size = bullet_size;

	public boolean canBeCanceled = true;
	public boolean moveOut = true;

	public boolean respectXRay = true;

	@Property(id = "bounces", minValue = 0.0, name = "Bounces", category = BulletPropertyCategory.travel, desc = "The bullet will bounce off blocks this many times before being destroyed on impact")
	public int bounces = 1;
	public int bouncyBounces = 100;

	public double ageFrac = 0;
	public double quarterAgeFrac = 0;
	public double halfAgeFrac = 0;

	@Property(id = "override_color", name = "Custom primary color", desc = "If disabled, the bullet will use the color of the tank which fired it")
	public boolean overrideBaseColor;
	@Property(id = "color_r", name = "Primary red")
	public double baseColorR;
	@Property(id = "color_g", name = "Primary green")
	public double baseColorG;
	@Property(id = "color_b", name = "Primary blue")
	public double baseColorB;

	@Property(id = "override_color2", name = "Custom secondary color", desc = "If disabled, the bullet will use the color of the tank which fired it")
	public boolean overrideOutlineColor;
	@Property(id = "color_r2", name = "Secondary red")
	public double outlineColorR;
	@Property(id = "color_g2", name = "Secondary green")
	public double outlineColorG;
	@Property(id = "color_b2", name = "Secondary blue")
	public double outlineColorB;

	public double originalOutlineColorR;
	public double originalOutlineColorG;
	public double originalOutlineColorB;

	public double opacityMultiplier = 1;

	@Property(id = "luminance", minValue = 0.0, maxValue = 1.0, name = "Luminance", category = BulletPropertyCategory.appearanceGlow, desc = "How bright the bullet will be in dark lighting. At 0, the bullet will be shaded like terrain by lighting. At 1, the bullet will always be fully bright.")
	public double luminance = 0.5;

	//@Property(id = "trails", name = "Trail", category = BulletPropertyCategory.appearance)
	public ArrayList<Trail> trailEffects = new ArrayList<>();

	@Property(id = "glow_intensity", minValue = 0.0, name = "Glow intensity", category = BulletPropertyCategory.appearanceGlow)
	public double glowIntensity = 1;
	@Property(id = "glow_size", minValue = 0.0, name = "Glow size", category = BulletPropertyCategory.appearanceGlow)
	public double glowSize = 4;

	public double iPosZ;
	public boolean autoZ = true;

	public double destroyTimer = 0;
	public double maxDestroyTimer = 60;

	public Tank tank;

	@Property(id = "damage", name = "Damage", category = BulletPropertyCategory.impact, desc = "A damage of 1.0 will destroy the default player tank. Set to negative to heal tanks.")
	public double damage = 1;

	@Property(id = "max_extra_health", minValue = 0.0, name = "Max extra hitpoints", category = BulletPropertyCategory.impact, desc = "Applicable if damage is negative: this bullet will not heal a tank to more than its default hitpoints plus 'max extra hitpoints'")
	public double maxExtraHealth = 1;

	@Property(id = "knockback_tank", name = "Tank knockback", category = BulletPropertyCategory.impact, desc = "The amount this bullet will knock back tanks it hits. Knockback is automatically scaled by this bullet's velocity and the tank's size. \n \n " +
			"A knockback value of 1 means the bullet will add its velocity to a tank of standard size. Knockback scales inversely to tank size squared.")
	public double tankHitKnockback = 0;

	@Property(id = "knockback_bullet", name = "Bullet knockback", category = BulletPropertyCategory.impact, desc = "The amount this bullet will knock back other bullets it collides with, instead of destroying them. Knockback is automatically scaled by this bullet's velocity both bullets' sizes. \n \n " +
			"A bullet with a knockback value of 1 will stop another bullet in a head-on collision if both bullets have the same size and speed. Knockback scales by ratio of bullet size squared.")
	public double bulletHitKnockback = 0;

	@Property(id = "explosion", name = "Explosion", category = BulletPropertyCategory.impact, nullable = true, desc = "The explosion produced when this bullet is destroyed")
	public Explosion hitExplosion = null;

	@Property(id = "stun", minValue = 0.0, name = "Stun duration", category = BulletPropertyCategory.impact, desc = "Will prevent a tank from moving for this much time on impact \n \n 1 time unit = 0.01 seconds")
	public double hitStun = 0;

	@Property(id = "freezing", name = "Freezing", category = BulletPropertyCategory.impact, desc = "If set, will create a circle of freezing when this bullet is destroyed")
	public boolean freezing = false;

	@Property(id = "boosting", name = "Boosting", category = BulletPropertyCategory.impact, desc = "If set, will boost the speed of tanks hit. Boost duration scales with bullet size.")
	public boolean boosting = false;

	//@Property(id = "area_effect", name = "Area effect", category = BulletPropertyCategory.impact)
	public AreaEffect hitAreaEffect = null;

	protected double delay = 0;
	protected ArrayList<Movable> previousRebounds = new ArrayList<>();
	protected boolean beganRebound = true;
	protected boolean failedRebound = false;
	protected Bullet reboundSuccessor = null;

	public boolean enableExternalCollisions = true;

	@Property(id = "speed", name = "Speed", category = BulletPropertyCategory.travel)
	public double speed = 25.0 / 8;

	/** If bullet should return to original speed if blown to a slower speed by something like wind */
	public boolean revertSpeed = true;

	@Property(id = "lifespan", minValue = 0.0, name = "Lifespan", category = BulletPropertyCategory.travel, desc = "After this long, the bullet will destroy itself automatically. Set to 0 for unlimited lifespan. \n \n 1 time unit = 0.01 seconds")
	public double lifespan = 0;

	/** If true, this selected bullet will show a ray when the aim keybind is pressed */
	public boolean showDefaultTrace = true;

	@Property(id = "range", minValue = 0.0, name = "Range", category = BulletPropertyCategory.travel, desc = "If the bullet goes farther than this distance from where it was initially fired, it will destroy itself automatically. Set to 0 for unlimited range. \n \n 1 tile = 50 units")
	public double range = 0;

	@Property(id = "heavy", name = "Heavy", category = BulletPropertyCategory.travel, desc = "Heavy bullets will pass through tanks and non-heavy bullets without being destroyed")
	public boolean heavy = false;

	@Property(id = "collide_obstacles", name = "Block collision", category = BulletPropertyCategory.travel, desc = "If disabled, the bullet will pass through blocks but still collide with the edges of the level")
	public boolean obstacleCollision = true;
	public boolean edgeCollision = true;

	@Property(id = "collide_bullets", name = "Bullet collision", category = BulletPropertyCategory.travel)
	public boolean bulletCollision = true;

	@Property(id = "collide_mines", name = "Mine collision", category = BulletPropertyCategory.travel)
	public boolean mineCollision = true;

	public boolean destroyBullets = true;

	@Property(id = "effect", name = "Effect", category = BulletPropertyCategory.appearance, desc = "Defines the bullet's trail, glow, and particle effects")
	public BulletEffect effect = BulletEffect.trail;

	public boolean useCustomWallCollision = false;
	public double wallCollisionSize = 10;

	@Property(id = "bush_burn", name = "Burns shrubbery", category = BulletPropertyCategory.travel, desc = "If enabled, the bullet will remove shrubbery it passes through")
	public boolean burnsBushes = false;

	@Property(id = "bush_lower", name = "Lowers shrubbery", category = BulletPropertyCategory.travel, desc = "If enabled, the bullet will lower shrubbery it passes through, revealing their contents \n If disabled, the bullet will make leaf particle effects as it exits shrubbery")
	public boolean lowersBushes = true;

	@Property(id = "homing_sharpness", name = "Homing strength", category = BulletPropertyCategory.travel, desc = "If nonzero, the bullet will change direction when a nearby enemy tank is in line of sight. Greater values will result in sharper turns. Negative values result in the bullet moving away from the tank.")
	public double homingSharpness = 0;

	@Property(id = "chain_count", minValue = 0.0, name = "Max chain", category = BulletPropertyCategory.travel, desc = "Once this bullet hits a tank or bullet, it will fire again from the hit target towards another nearby enemy up to this many times")
	public int rebounds = 0;

	@Property(id = "chain_delay", minValue = 0.0, name = "Chain delay", category = BulletPropertyCategory.travel, desc = "The time between hitting a tank or bullet and firing itself again \n \n 1 time unit = 0.01 seconds")
	public double reboundDelay = 10;

	public Tank homingTarget = null;
	public Tank homingPrevTarget = null;
	public double homingTargetTime = 0;

	/** If true, homing won't make sounds or particles */
	public boolean homingSilent = false;

	public ItemBullet.ItemStackBullet item;

	@Property(id = "max_live_bullets", minValue = 0.0, name = "Max live bullets", category = BulletPropertyCategory.firing, desc = "The maximum number of this bullet fired by one tank that can be onscreen at a time")
	public int maxLiveBullets = 5;

	@Property(id = "recoil", name = "Recoil", category = BulletPropertyCategory.firing)
	public double recoil = 1.0;

	@Property(id = "shot_count", minValue = 1.0, name = "Shot count", category = BulletPropertyCategory.firing, desc = "The number of bullets fired at once")
	public int shotCount = 1;

	@Property(id = "multishot_spread_angle", minValue = 0.0, maxValue = 360, name = "Multishot spread angle", category = BulletPropertyCategory.firing, desc = "The angle spread in degrees of multiple bullets fired at once")
	public double multishotSpread = 0;

	@Property(id = "accuracy_spread_angle", minValue = 0.0, name = "Accuracy spread angle", category = BulletPropertyCategory.firing, desc = "The size of the random inaccuracy angle variation of a bullet when fired, in degrees. Larger values are less accurate.")
	public double accuracySpread = 0;

	public boolean canMultiDamage = false;

	public double frameDamageMultipler = 1;

	public double collisionX;
	public double collisionY;

	public boolean enableCollision = true;

	public boolean externalBulletCollision = true;

	public boolean affectsMaxLiveBullets;

	/**
	 * Movables collided with this bullet to prevent double collisions
	 */
	public ArrayList<Movable> inside = new ArrayList<>();
	public ArrayList<Movable> insideOld = new ArrayList<>();

	@Property(id = "sound", name = "Shot sound", category = BulletPropertyCategory.firing, miscType = Property.MiscType.bulletSound)
	public String shotSound = "shoot.ogg";
	@Property(id = "sound_pitch", minValue = 0.0, maxValue = 10.0, name = "Sound pitch", category = BulletPropertyCategory.firing)
	public double pitch = 1;
	@Property(id = "sound_pitch_variation", minValue = 0.0, maxValue = 10.0, name = "Sound pitch variation", category = BulletPropertyCategory.firing)
	public double pitchVariation = 0;
	@Property(id = "sound_volume", minValue = 0.0, maxValue = 1.0, name = "Sound volume", category = BulletPropertyCategory.firing)
	public double soundVolume = 1;

	protected ArrayList<Trail>[] trails;
	protected boolean addedTrail = false;
	protected double lastTrailAngle = -1;
	protected double lastTrailPitch = -1;
	protected boolean trail3d = false;

	public double originX;
	public double originY;

	public boolean justBounced = false;

	public double[] lightInfo = new double[]{0, 0, 0, 0, 0, 0, 0};

	public final boolean isTemplate;

	/**
	 * Do not use if you plan to place this bullet in the game field. Only for templates.
	 * Use another constructor if you want to add the bullet to the game field.
	 */
	public Bullet()
	{
		super(0, 0);
		this.isTemplate = true;
		this.typeName = bullet_class_name;
	}

	public Bullet(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet item)
	{
		super(x, y);
		this.typeName = bullet_class_name;

		this.isTemplate = false;

		this.item = item;
		this.vX = 0;
		this.vY = 0;

		this.tank = t;
		this.team = t.team;

		this.originX = this.tank.posX;
		this.originY = this.tank.posY;

		this.iPosZ = this.tank.size / 2 + this.tank.turretSize / 2;

		this.isRemote = t.isRemote;

		this.affectsMaxLiveBullets = affectsMaxLiveBullets;

		if (!this.tank.isRemote && this.affectsMaxLiveBullets)
			this.item.liveBullets++;

		AttributeModifier a = this.tank.em().getAttribute(AttributeModifier.bullet_boost);
		if (a != null)
			em().addStatusEffect(StatusEffect.boost_bullet, a.age, 0, a.deteriorationAge, a.duration);
		AttributeModifier.recycle(a);

		if (!this.tank.isRemote)
		{
			if (!freeIDs.isEmpty())
				this.networkID = freeIDs.remove(0);
			else
			{
				this.networkID = currentID;
				currentID++;
			}

			idMap.put(this.networkID, this);
		}

		this.previousRebounds.add(this.tank);

		this.drawLevel = 8;
    }

	public void setColorFromTank()
	{
		if (!this.overrideBaseColor)
		{
			this.baseColorR = this.tank.colorR;
			this.baseColorG = this.tank.colorG;
			this.baseColorB = this.tank.colorB;
		}

		if (!this.overrideOutlineColor)
		{
			double[] oc = Team.getObjectColor(this.tank.secondaryColorR, this.tank.secondaryColorG, this.tank.secondaryColorB, this.tank);
			this.outlineColorR = oc[0];
			this.outlineColorG = oc[1];
			this.outlineColorB = oc[2];
		}
	}

	public void moveOut(double amount)
	{
		double a = this.getPolarDirection();
		this.moveInDirection(Math.cos(a), Math.sin(a), amount);
	}

	public void setTargetLocation(double x, double y)
	{

	}

	public void collided()
	{

	}

	public void rebound(Movable m)
	{
		try
		{
			Bullet b = this.getClass().getConstructor(double.class, double.class, Tank.class, boolean.class, ItemBullet.ItemStackBullet.class)
					.newInstance(m.posX, m.posY, this.tank, false, this.item);
			this.clonePropertiesTo(b);
			b.iPosZ = this.posZ;
			b.posZ = this.posZ;
			if (b instanceof BulletArc || b instanceof BulletAirStrike)
				b.posZ += 15;
			b.previousRebounds = this.previousRebounds;
			b.affectsMaxLiveBullets = this.affectsMaxLiveBullets;
			b.previousRebounds.add(m);
			b.delay = this.reboundDelay;
			b.rebounds = this.rebounds - 1;
			b.beganRebound = false;
			Game.movables.add(b);
			Game.movables.add(new BulletReboundIndicator(b));

			this.reboundSuccessor = b;
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	/**
	 * Triggered when colliding with an obstacle or map edge and having no bounces left
	 */
	public void collidedWithNothing()
	{

	}

	public void collidedWithTank(Tank t)
	{
		double vX = this.vX;
		double vY = this.vY;

		if (!heavy)
		{
			boolean pop = this.playPopSound;
			this.playPopSound = false;
			this.pop();
			this.playPopSound = pop;
		}

		if (!ScreenGame.finishedQuick && t.getDamageMultiplier(this) > 0)
		{
			if (!(Team.isAllied(this, t) && !this.team.friendlyFire) && this.tankHitKnockback != 0)
			{
				double mul = Game.tile_size * Game.tile_size / Math.max(1, Math.pow(t.size, 2)) * this.tankHitKnockback * this.frameDamageMultipler;
				t.vX += vX * mul;
				t.vY += vY * mul;

				t.recoilSpeed = t.getSpeed();
				if (t.recoilSpeed > t.maxSpeed)
				{
					t.inControlOfMotion = false;
					t.tookRecoil = true;
				}

				if (this.damage <= 0 && this.playBounceSound)
					Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / size));

				if (t instanceof TankPlayerRemote)
					Game.eventsOut.add(new EventTankControllerAddVelocity(t, vX * mul, vY * mul, t.tookRecoil));
			}

			double dmg = this.damage;

			if (Team.isAllied(this, t) && !this.team.friendlyFire)
				dmg = Math.min(0, dmg);

			double healthBefore = t.health;
			double healFlashBefore = t.healFlashAnimation;

			boolean kill = t.damage(dmg * this.frameDamageMultipler, this);
			if (!t.destroy && this.hitStun > 0 && !(Team.isAllied(this, t) && this.team != null && !this.team.friendlyFire))
				this.applyStun(t);

			if (this.damage < 0)
			{
				if (healthBefore == t.health)
					t.healFlashAnimation = healFlashBefore;

				float pitch = (float) ((Math.min(t.health, t.baseHealth + this.maxExtraHealth) / (t.baseHealth + this.maxExtraHealth) / 2) + 1f) / 2;
				if (this.item.item.cooldownBase > 0)
					Drawing.drawing.playGlobalSound("heal_impact_2.ogg", pitch);
				else
				{
					float freq = (float) (this.frameDamageMultipler / 10);
					if (Game.game.window.touchscreen)
						freq = 1;
					Drawing.drawing.playGlobalSound("heal2.ogg", pitch, freq);
				}

				t.em().addAttribute(AttributeModifier.obtain("healray", AttributeModifier.healray, AttributeModifier.Operation.add, 1.0));
			}

			if (kill)
			{
				t.vX = 0;
				t.vY = 0;

				if (Game.currentLevel instanceof Minigame)
				{
					((Minigame) Game.currentLevel).onKill(this.tank, t);

					if (((Minigame) Game.currentLevel).enableKillMessages && ScreenPartyHost.isServer)
					{
						String message = ((Minigame) Game.currentLevel).generateKillMessage(t, this.tank, true);
						ScreenPartyHost.chat.add(0, new ChatMessage(message));
						Game.eventsOut.add(new EventChat(message));
					}

					for (IFixedMenu m : ModAPI.menuGroup)
					{
						if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.kills))
						{
							if (!((Scoreboard) m).teams.isEmpty())
								((Scoreboard) m).addTeamScore(this.tank.team, 1);

							else if (this.tank instanceof TankPlayer && !((Scoreboard) m).players.isEmpty())
								((Scoreboard) m).addPlayerScore(((TankPlayer) this.tank).player, 1);

							else if (this.tank instanceof TankPlayerRemote && !((Scoreboard) m).players.isEmpty())
								((Scoreboard) m).addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
						}
					}
				}

				if (this.tank.equals(Game.playerTank))
				{
					if (Game.currentLevel instanceof Minigame && (t instanceof TankPlayer || t instanceof TankPlayerRemote))
						Game.player.hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
					else
						Game.player.hotbar.coins += t.coinValue;
				}
				else if (this.tank instanceof IServerPlayerTank && Crusade.crusadeMode)
				{
					if (Game.currentLevel instanceof Minigame && (t instanceof TankPlayer || t instanceof TankPlayerRemote))
						((IServerPlayerTank) this.tank).getPlayer().hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
					else
						((IServerPlayerTank) this.tank).getPlayer().hotbar.coins += t.coinValue;

					if (this.tank instanceof TankPlayerRemote)
					Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
				}
				else if ((!Game.currentLevel.shop.isEmpty() || !Game.currentLevel.startingItems.isEmpty()) && !(t instanceof TankPlayer || t instanceof TankPlayerRemote))
				{
					if (this.tank instanceof TankPlayerRemote)
					{
						((TankPlayerRemote) this.tank).player.hotbar.coins += t.coinValue;
						Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
					}
				}
				else if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).playerKillCoins > 0)
				{
					if (this.tank instanceof TankPlayer)
					{
						((TankPlayer) this.tank).player.hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
						Game.eventsOut.add(new EventUpdateCoins(((TankPlayer) this.tank).player));
					}

					else if (this.tank instanceof TankPlayerRemote)
					{
						((TankPlayerRemote) this.tank).player.hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
						Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
					}
				}
			}
			else
			{
				if (this.playPopSound && dmg > 0)
					Drawing.drawing.playGlobalSound("damage.ogg", (float) (bullet_size / size));

				if (this.boosting)
				{
					EffectManager tem = t.getEffectManager();
					AttributeModifier c = AttributeModifier.obtain("boost_speed", AttributeModifier.velocity, AttributeModifier.Operation.multiply, 3);
					c.duration = 10 * this.size;
					c.deteriorationAge = 5 * this.size;
					tem.addUnduplicateAttribute(c);

					AttributeModifier e = AttributeModifier.obtain("bullet_boost", AttributeModifier.bullet_boost, AttributeModifier.Operation.multiply, 1);
					e.duration = 10 * this.size;
					e.deteriorationAge = 5 * this.size;
					tem.addUnduplicateAttribute(e);

					AttributeModifier a = AttributeModifier.obtain("boost_glow", AttributeModifier.glow, AttributeModifier.Operation.multiply, 1);
					a.duration = 10 * this.size;
					a.deteriorationAge = 5 * this.size;
					tem.addUnduplicateAttribute(a);

					AttributeModifier b = AttributeModifier.obtain("boost_slip", AttributeModifier.friction, AttributeModifier.Operation.multiply, -0.75);
					b.duration = 10 * this.size;
					b.deteriorationAge = 5 * this.size;
					tem.addUnduplicateAttribute(b);

					AttributeModifier d = AttributeModifier.obtain("boost_effect", AttributeModifier.ember_effect, AttributeModifier.Operation.add, 1);
					d.duration = 10 * this.size;
					d.deteriorationAge = 5 * this.size;
					tem.addUnduplicateAttribute(d);
				}
			}
		}
		else if (this.playPopSound && !this.heavy)
			Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));
	}

	public void collidedWithObject(Movable o)
	{
		if (o instanceof Mine && this.mineCollision)
		{
			this.collidedWithMisc(o);
			o.destroy = true;

			if (this.destroy && this.rebounds > 0)
				this.rebound(o);
		}
		else if (o instanceof Bullet && ((Bullet) o).enableCollision && ((Bullet) o).enableExternalCollisions)
		{
			this.collidedWithBullet((Bullet) o);

			if (this.destroy && this.rebounds > 0)
				this.rebound(o);

			if (o.destroy && ((Bullet) o).rebounds > 0)
				((Bullet) o).rebound(this);

			if (((Bullet) o).reboundSuccessor != null && this.reboundSuccessor != null)
			{
				this.reboundSuccessor.inside.add(((Bullet) o).reboundSuccessor);
				this.reboundSuccessor.insideOld.add(((Bullet) o).reboundSuccessor);
				((Bullet) o).reboundSuccessor.inside.add(this.reboundSuccessor);
				((Bullet) o).reboundSuccessor.insideOld.add(this.reboundSuccessor);
			}
		}
	}

	public void push(Bullet b)
	{
		b.vX += this.vX * Math.pow(this.size, 2) / Math.max(1, Math.pow(b.size, 2)) * this.bulletHitKnockback * this.frameDamageMultipler;
		b.vY += this.vY * Math.pow(this.size, 2) / Math.max(1, Math.pow(b.size, 2)) * this.bulletHitKnockback * this.frameDamageMultipler;
		b.collisionX = b.posX;
		b.collisionY = b.posY;
		b.addTrail();
	}

	protected void pop()
	{
		if (this.playPopSound)
			Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));

		this.destroy = true;
		this.vX = 0;
		this.vY = 0;
	}

	/**
	 * When 2 bullets that knock bullets collide, an elastic collision takes place, with mass scaling by bullet knockback.
	 */
	public void collideBounce(Bullet b)
	{
		double toAngle = this.getAngleInDirection(b.posX, b.posY);

		double ourSpeed = this.getSpeed();
		double theirSpeed = b.getSpeed();

		double ourDir = this.getPolarDirection();
		double theirDir = b.getPolarDirection();

		double ourMass = this.bulletHitKnockback * this.frameDamageMultipler * this.size * this.size;
		double theirMass = b.bulletHitKnockback * b.frameDamageMultipler * b.size * b.size;

		this.collisionX = this.posX;
		this.collisionY = this.posY;
		b.collisionX = b.posX;
		b.collisionY = b.posY;

		double co1 = (ourSpeed * Math.cos(ourDir - toAngle) * (ourMass - theirMass) + 2 * theirMass * theirSpeed * Math.cos(theirDir - toAngle)) / Math.max(1, ourMass + theirMass);
		double vX1 = co1 * Math.cos(toAngle) + ourSpeed * Math.sin(ourDir - toAngle) * Math.cos(toAngle + Math.PI / 2);
		double vY1 = co1 * Math.sin(toAngle) + ourSpeed * Math.sin(ourDir - toAngle) * Math.sin(toAngle + Math.PI / 2);

		double co2 = (theirSpeed * Math.cos(theirDir - toAngle) * (theirMass - ourMass) + 2 * ourMass * ourSpeed * Math.cos(ourDir - toAngle)) / Math.max(1, theirMass + ourMass);
		double vX2 = co2 * Math.cos(toAngle) + theirSpeed * Math.sin(theirDir - toAngle) * Math.cos(toAngle + Math.PI / 2);
		double vY2 = co2 * Math.sin(toAngle) + theirSpeed * Math.sin(theirDir - toAngle) * Math.sin(toAngle + Math.PI / 2);

		this.vX = vX1;
		this.vY = vY1;
		b.vX = vX2;
		b.vY = vY2;

		double dist = Movable.distanceBetween(this, b);
		double sizes = (this.size + b.size) / 2;
		this.moveInAngle(toAngle, dist - sizes);
		b.moveInAngle(toAngle + Math.PI, dist - sizes);

		Game.eventsOut.add(new EventBulletBounce(this));
		Game.eventsOut.add(new EventBulletBounce(b));

		if (this.playBounceSound && b.playBounceSound)
		{
			Drawing.drawing.playGlobalSound("bump.ogg", (float) (bullet_size / size), 0.5f);
			Drawing.drawing.playGlobalSound("bump.ogg", (float) (bullet_size / b.size), 0.5f);
		}

		this.addTrail();
		b.addTrail();
	}

	public void collidedWithBullet(Bullet b)
	{
		if (this.heavy == b.heavy && b.enableExternalCollisions)
		{
			if (this.bulletHitKnockback == 0 && b.bulletHitKnockback == 0)
			{
				this.pop();
				if (this.destroyBullets)
					b.pop();
			}
			else if (this.bulletHitKnockback != 0 && b.bulletHitKnockback == 0)
			{
				this.push(b);
				if (b.destroyBullets)
					this.pop();
			}
			else if (this.bulletHitKnockback == 0)
			{
				b.push(this);
				if (this.destroyBullets)
					b.pop();
			}
			else
			{
				this.collideBounce(b);
			}
		}
		else
		{
			Bullet h;
			Bullet l;

			if (this.heavy)
			{
				h = this;
				l = b;
			}
			else
			{
				h = b;
				l = this;
			}

			if (h.bulletHitKnockback != 0)
			{
				h.push(l);
				if (this.playBounceSound)
					Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / h.size), 1f);
			}
			else if (this.destroyBullets)
				l.pop();
		}
	}

	public void collidedWithMisc(Movable m)
	{
		if (!heavy)
		{
			this.pop();
			m.destroy = true;

			if (this.rebounds > 0)
				this.rebound(m);
		}
	}

	public void checkCollisionLocal()
	{
		if (this.destroy)
			return;

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			if (!o.checkForObjects)
				continue;

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double horizontalDist = Math.abs(dx);
			double verticalDist = Math.abs(dy);

			double s = this.size;
			if (useCustomWallCollision)
				s = this.wallCollisionSize;

			double bound = s / 2 + Game.tile_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
				o.onObjectEntryLocal(this);
		}
	}

	public void checkCollision()
	{
		if (this.destroy)
			return;

		boolean bouncy = false;
		boolean allowBounce = true;

		boolean collided = false;

		double prevX = this.posX;
		double prevY = this.posY;

		if (this.obstacleCollision)
		{
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if ((!o.bulletCollision && !o.checkForObjects) || (o instanceof ObstacleStackable && ((ObstacleStackable) o).startHeight > 1))
					continue;

				double dx = this.posX - o.posX;
				double dy = this.posY - o.posY;

				double horizontalDist = Math.abs(dx);
				double verticalDist = Math.abs(dy);

				double s = this.size;

				if (useCustomWallCollision)
					s = this.wallCollisionSize;

				double bound = s / 2 + Game.tile_size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					if (o.checkForObjects)
						o.onObjectEntry(this);

					if (!o.bulletCollision)
						continue;

					boolean left = o.hasLeftNeighbor();
					boolean right = o.hasRightNeighbor();
					boolean up = o.hasUpperNeighbor();
					boolean down = o.hasLowerNeighbor();

					if (left && dx <= 0)
						horizontalDist = 0;

					if (right && dx >= 0)
						horizontalDist = 0;

					if (up && dy <= 0)
						verticalDist = 0;

					if (down && dy >= 0)
						verticalDist = 0;

					bouncy = o.bouncy;
					allowBounce = o.allowBounce;
					collided = true;

					if (!left && dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
					{
						if (allowBounce)
						{
							this.posX += 2 * (horizontalDist - bound);
							this.vX = -Math.abs(this.vX);
						}

						this.collisionX = this.posX - (horizontalDist - bound);
						this.collisionY = this.posY - (horizontalDist - bound) / vX * vY;
						if (Math.abs(this.vX) < 0.0001)
							this.collisionY = this.posY;
					}
					else if (!up && dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						if (allowBounce)
						{
							this.posY += 2 * (verticalDist - bound);
							this.vY = -Math.abs(this.vY);
						}

						this.collisionX = this.posX - (verticalDist - bound) / vY * vX;
						this.collisionY = this.posY - (verticalDist - bound);
						if (Math.abs(this.vY) < 0.0001)
							this.collisionX = this.posX;
					}
					else if (!right && dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						if (allowBounce)
						{
							this.posX -= 2 * (horizontalDist - bound);
							this.vX = Math.abs(this.vX);
						}

						this.collisionX = this.posX + (horizontalDist - bound);
						this.collisionY = this.posY + (horizontalDist - bound) / vX * vY;
						if (Math.abs(this.vX) < 0.0001)
							this.collisionY = this.posY;
					}
					else if (!down && dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						if (allowBounce)
						{
							this.posY -= 2 * (verticalDist - bound);
							this.vY = Math.abs(this.vY);
						}

						this.collisionX = this.posX + (verticalDist - bound) / vY * vX;
						this.collisionY = this.posY + (verticalDist - bound);
						if (Math.abs(this.vY) < 0.0001)
							this.collisionX = this.posX;
					}
				}
			}
		}

		if (this.edgeCollision)
		{
			if (this.posX + this.size / 2 > Drawing.drawing.sizeX)
			{
				collided = true;
				this.posX = Drawing.drawing.sizeX - this.size / 2 - (this.posX + this.size / 2 - Drawing.drawing.sizeX);
				this.vX = -Math.abs(this.vX);

				this.collisionX = this.posX - (this.posX + this.size / 2 - Drawing.drawing.sizeX);
				this.collisionY = this.posY - (this.posX + this.size / 2 - Drawing.drawing.sizeX) / vX * vY;
			}
			if (this.posX - this.size / 2 < 0)
			{
				collided = true;
				this.posX = this.size / 2 - (this.posX - this.size / 2);
				this.vX = Math.abs(this.vX);

				this.collisionX = this.posX - (this.posX - this.size / 2);
				this.collisionY = this.posY - (this.posX - this.size / 2) / vX * vY;
			}
			if (this.posY + this.size / 2 > Drawing.drawing.sizeY)
			{
				collided = true;
				this.posY = Drawing.drawing.sizeY - this.size / 2 - (this.posY + this.size / 2 - Drawing.drawing.sizeY);
				this.vY = -Math.abs(this.vY);

				this.collisionX = this.posX - (this.posY + this.size / 2 - Drawing.drawing.sizeY) / vY * vX;
				this.collisionY = this.posY - (this.posY + this.size / 2 - Drawing.drawing.sizeY);
			}
			if (this.posY - this.size / 2 < 0)
			{
				collided = true;
				this.posY = this.size / 2 - (this.posY - this.size / 2);
				this.vY = Math.abs(this.vY);

				this.collisionX = this.posX - (this.posY - this.size / 2) / vY * vX;
				this.collisionY = this.posY - (this.posY - this.size / 2);
			}

			if (collided && this.age == 0)
			{
				this.destroy = true;
				this.posX = prevX;
				this.posY = prevY;
				this.collided();
				return;
			}
		}

		this.insideOld.clear();

		if (!canMultiDamage)
			this.insideOld.addAll(this.inside);

		this.inside.clear();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);

			if (o instanceof Tank && !o.destroy)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				Tank t = ((Tank) o);

				double bound = this.size / 2 + t.size * t.hitboxSize / 2;

				if (horizontalDist < bound && verticalDist < bound && t.size > 0)
				{
					this.collisionX = this.posX;
					this.collisionY = this.posY;

					if (!this.insideOld.contains(t))
					{
						this.collided();
						this.collidedWithTank(t);

						if (this.rebounds > 0)
							this.rebound(t);
					}

					this.inside.add(t);
				}
			}
			else if (((o instanceof Bullet && ((Bullet) o).enableCollision && ((Bullet) o).delay <= 0 && (((Bullet) o).bulletCollision && ((Bullet) o).externalBulletCollision && this.bulletCollision))
					|| o instanceof Mine) && o != this && !o.destroy)
			{
				double distSq = Math.pow(this.posX - o.posX, 2) + Math.pow(this.posY - o.posY, 2);

				double s;

				if (o instanceof Mine)
					s = ((Mine) o).size;
				else
					s = ((Bullet) o).size;

				double bound = this.size / 2 + s / 2;

				if (distSq <= bound * bound)
				{
					this.collisionX = this.posX;
					this.collisionY = this.posY;

					if (!this.insideOld.contains(o))
					{
						this.collided();
						this.collidedWithObject(o);
					}

					this.inside.add(o);
				}
			}
		}

		if (collided)
		{
			this.collided();

			if (!bouncy)
				this.bounces--;
			else
				this.bouncyBounces--;

			this.justBounced = true;

			if (this.bounces < 0 || this.bouncyBounces < 0 || !allowBounce)
			{
				this.collidedWithNothing();
				this.pop();
			}
			else if (this.playBounceSound)
				Drawing.drawing.playGlobalSound("bounce.ogg", (float) (bullet_size / size));

			if (!destroy)
			{
				Game.eventsOut.add(new EventBulletBounce(this));
				this.addTrail();
			}
		}
	}

	public Ray getRay()
	{
		Ray r = new Ray(posX, posY, this.getAngleInDirection(this.posX + this.vX, this.posY + this.vY), this.bounces, tank);
		r.size = this.size;
		return r;
	}

	public void updateHoming()
	{
		double frameFrequency = affectedByFrameFrequency ? Panel.frameFrequency : 1;

		Tank nearest = null;
		double nearestDist = Double.MAX_VALUE;

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank && (Team.isAllied(this, m) != this.isHarmful()) && !m.destroy)
			{
				Tank t = (Tank) m;
				double d = Movable.distanceBetween(this, m);

				if (d < nearestDist)
				{
					nearestDist = d;
					nearest = t;
				}
			}
		}

		double s = this.getSpeed();
		if (!this.isRemote)
		{
			this.homingTarget = null;
			if (nearest != null && !this.destroy)
			{
				double a = this.getAngleInDirection(nearest.posX, nearest.posY);
				Ray r = new Ray(this.posX, this.posY, a, 0, this.tank);

				if (this instanceof BulletArc || this instanceof BulletAirStrike)
				{
					s = Math.sqrt(this.vX * this.vX + this.vY * this.vY + this.vZ * this.vZ);
					if (this.vZ < 0)
					{
						double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * BulletArc.gravity * (this.posZ - Game.tile_size / 2))) / BulletArc.gravity;

						double dx = ((nearest.posX - this.posX) - this.vX * time) / (0.5 * time * time);
						double dy = ((nearest.posY - this.posY) - this.vY * time) / (0.5 * time * time);
						double change = Math.sqrt(dx * dx + dy * dy);
						double cappedChange = Math.min(change, this.homingSharpness / 2.5);

						if (change > 0.00001)
						{
							this.vX += dx / change * cappedChange * frameFrequency;
							this.vY += dy / change * cappedChange * frameFrequency;
						}

						if (this instanceof BulletArc)
							this.vZ -= BulletArc.gravity * this.homingSharpness * frameFrequency;

						this.homingTarget = nearest;
					}
				}
				else if (r.getTarget() == nearest)
				{
					this.addPolarMotion(a, frameFrequency * this.homingSharpness);
					double s2 = this.getSpeed();
					this.vX *= s / s2;
					this.vY *= s / s2;

					this.homingTarget = nearest;
				}
			}

			if (this.homingTarget != this.homingPrevTarget)
			{
				Game.eventsOut.add(new EventBulletUpdateTarget(this));
			}
		}

		if (this.homingTarget != null)
		{
			double nX = this.vX / s;
			double nY = this.vY / s;

			if (Game.playerTank != null && !Game.playerTank.destroy && !ScreenGame.finishedQuick && !this.homingSilent)
			{
				double d = Movable.distanceBetween(this, Game.playerTank);

				if (d <= 500)
				{
					double dX = (this.posX - Game.playerTank.posX) / d;
					double dY = (this.posY - Game.playerTank.posY) / d;

					double v = 1 + ((nX * dX + nY * dY) * this.speed) / 15.0;

					Drawing.drawing.playSound("wind.ogg", (float) (0.8 / (float) v + Math.random() * 0.1f - 0.05f), (float) Math.min(1, 100 / d));
				}
			}

			if (Game.bulletTrails && Math.random() < frameFrequency * Game.effectMultiplier && Game.effectsEnabled && !this.homingSilent)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
				double var = 50;
				e.maxAge /= 2;

				double r1 = 255;
				double g1 = 120;
				double b1 = 0;

				e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

				double v = this.homingSharpness > 0 ? 1 : -1;

				if (Game.enable3d)
					e.set3dPolarMotion(Math.PI + this.getAngleInDirection(this.homingTarget.posX, this.homingTarget.posY) + (Math.random() - 0.5) * 0.01, Math.PI * 0.1 * (Math.random() - 0.5), this.size / 50.0 * (12 + Math.random() * 4) * v);
				else
					e.setPolarMotion(Math.PI + this.getAngleInDirection(this.homingTarget.posX, this.homingTarget.posY) + (Math.random() - 0.5) * 0.01, this.size / 50.0 * (12 + Math.random() * 4) * v);

				Game.effects.add(e);
			}

			this.homingTargetTime += frameFrequency;
		}

		if (homingPrevTarget != this.homingTarget)
			this.homingTargetTime = 0;

		this.homingPrevTarget = this.homingTarget;

	}

	public void applyStun(Movable movable)
	{
		AttributeModifier a = AttributeModifier.obtain(AttributeModifier.velocity, AttributeModifier.Operation.multiply, -1);
		a.duration = this.hitStun;
		movable.em().addAttribute(a);
		if (!this.tank.isRemote)
		{
			Game.eventsOut.add(new EventBulletStunEffect(movable.posX, movable.posY, this.posZ, this.hitStun / 100.0));

			if (Game.effectsEnabled)
			{
				for (int i = 0; i < 25 * Game.effectMultiplier; i++)
				{
					Effect e = Effect.createNewEffect(movable.posX, movable.posY, this.posZ, Effect.EffectType.stun);
					e.maxAge *= this.hitStun / 100.0;
					double var = 50;
					e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
					e.glowR = 0;
					e.glowG = 128;
					e.glowB = 128;
					Game.effects.add(e);
				}
			}
		}
	}

	public void initTrails()
	{
		double mul = this.effect == BulletEffect.long_trail ? 1.5 : 1;

		if (this.effect == BulletEffect.trail || this.effect == BulletEffect.long_trail || this.effect == BulletEffect.fire || this.effect == BulletEffect.dark_fire)
			this.trailEffects.add(new Trail(this, this.speed, 0, 0, 0,1, 1, 15 * mul, 0, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5, true, true));

		if (this.effect == BulletEffect.fire_trail)
		{
			this.trailEffects.add(new Trail(this, this.speed, 0, 0, 7,2, 2, 50, 0, 80, 80, 80, 100, 80, 80, 80, 0, false, 0.5, false, true));
			this.trailEffects.add(new Trail(this, this.speed, 0, 0, 3,2, 2, 4, 0, 80, 80, 80, 0, 80, 80, 80, 100, false, 0.5, true, false));
		}

		if (this.effect == BulletEffect.fire || this.effect == BulletEffect.fire_trail)
			this.trailEffects.add(new Trail(this, this.speed, 0, 0, 0, 5, 1, 5, 0, 255, 255, 0, 255, 255, 0, 0, 0, false, 1, true, true));

		if (this.effect == BulletEffect.dark_fire)
			this.trailEffects.add(new Trail(this, this.speed, 0, 0, 0, 5, 1, 5, 0,  64, 0, 128, 255, 0, 0, 0, 0, false, 1, true, true));

		this.trails = (ArrayList<Trail>[])(new ArrayList[this.trailEffects.size()]);

		for (int i = 0; i < this.trails.length; i++)
			this.trails[i] = new ArrayList<>();
	}

	public boolean isHarmful()
	{
		return !(this.damage <= 0 && !this.freezing && this.bulletHitKnockback == 0 && this.tankHitKnockback == 0 && this.hitStun <= 0);
	}

	@Override
	public void update()
	{
		if (this.isTemplate)
			Game.exitToCrash(new Exception("Do not add template bullets to the game field: found " + this));

		double frameFrequency = affectedByFrameFrequency ? Panel.frameFrequency : 1;

		if (this.delay > 0)
		{
			this.delay -= frameFrequency;
			return;
		}

		if (this.age <= 0)
		{
			this.originalOutlineColorR = this.outlineColorR;
			this.originalOutlineColorG = this.outlineColorG;
			this.originalOutlineColorB = this.outlineColorB;
		}

		if (!this.previousRebounds.isEmpty() && !this.beganRebound)
		{
			this.beganRebound = true;

			Movable m = this.previousRebounds.get(this.previousRebounds.size() - 1);
			this.posX = m.posX;
			this.posY = m.posY;
			this.inside.add(m);
			this.insideOld.add(m);

			Movable nearest = null;
			double nearestDist = Double.MAX_VALUE;
			for (Movable m1: Game.movables)
			{
				boolean eligible = false;
				if (m1 instanceof Tank && this.isHarmful() != Team.isAllied(this, m1))
					eligible = true;
				else if ((m1 instanceof Bullet && ((Bullet) m1).enableCollision && ((Bullet) m1).bulletCollision && ((Bullet) m1).delay <= 0) && this.bulletCollision && !Team.isAllied(this, m1))
					eligible = true;
				else if (m1 instanceof Mine && this.mineCollision && !Team.isAllied(this, m1))
					eligible = true;

				if (this == m1 || m1.destroy || this.previousRebounds.contains(m1))
					eligible = false;

				if (eligible)
				{
					double d = Movable.distanceBetween(this, m1);
					if (d < nearestDist)
					{
						nearest = m1;
						nearestDist = d;
					}
				}
			}

			if (nearest == null)
			{
				if (this.affectsMaxLiveBullets)
					this.item.liveBullets--;

				this.failedRebound = true;
				Game.removeMovables.add(this);
				return;
			}
			else
			{
				this.setMotionInDirection(nearest.posX, nearest.posY, this.speed);
				this.setTargetLocation(nearest.posX, nearest.posY);
				Game.eventsOut.add(new EventShootBullet(this));
			}
		}

		if (this.trails == null)
			this.initTrails();

		if (this.homingSharpness != 0)
			this.updateHoming();

		if (this.hitExplosion != null)
		{
			this.playPopSound = false;
			this.outlineColorR = 255;
			this.outlineColorG = (((int) this.age % 80) / 40 == 1) ? 255 : 0;
			this.outlineColorB = 0;
		}

		if (this.freezing || this.boosting)
			this.playPopSound = false;

		if (!this.isRemote && ScreenPartyHost.isServer && (this.vX != this.lastOriginalVX || this.vY != this.lastOriginalVY) && !justBounced)
			Game.eventsOut.add(new EventBulletUpdate(this));

		this.justBounced = false;

		super.update();

		if (this.age == 0)
		{
			this.collisionX = this.posX;
			this.collisionY = this.posY;
			this.dealsDamage = this.damage > 0;

			this.addTrail();
		}

		if (!this.destroy && this.revertSpeed)
		{
			double frac = Math.pow(0.999, frameFrequency);
			this.setPolarMotion(this.getPolarDirection(), this.getSpeed() * frac + this.speed * (1 -frac));
		}

		boolean noTrails = true;

		for (ArrayList<Trail> trails : this.trails)
		{
			double trailLength = 0;
			for (int i = 0; i < trails.size(); i++)
			{
				Trail t = trails.get(i);

				if (this.destroy)
					t.spawning = false;

				if (t.expired)
				{
					trails.remove(i);
					i--;
				}
				else
				{
					trailLength += t.update(trailLength, this.destroy);
					noTrails = false;
				}
			}
		}

		if (destroy)
		{
			if (this.destroyTimer <= 0 && !freeIDs.contains(this.networkID))
			{
				if (!this.tank.isRemote)
					Game.eventsOut.add(new EventBulletDestroyed(this));

				freeIDs.add(this.networkID);
				idMap.remove(this.networkID);

				if (this.affectsMaxLiveBullets && this.reboundSuccessor == null)
					this.item.liveBullets--;

				if (!this.isRemote)
					this.onDestroy();
			}

			if (this.destroyTimer <= 0 && Game.effectsEnabled)
			{
				this.addDestroyEffect();
			}

			this.destroyTimer += frameFrequency;
			this.vX = 0;
			this.vY = 0;
		}
		else
		{
			double frac = 1 / (1 + this.age / 100);

			if (this.autoZ)
				this.posZ = this.iPosZ * frac + (Game.tile_size / 4) * (1 - frac);

			this.ageFrac += frameFrequency * Game.effectMultiplier;
			this.halfAgeFrac += frameFrequency * Game.effectMultiplier;
			this.quarterAgeFrac += frameFrequency * Game.effectMultiplier;

			if (Game.bulletTrails)
			{
				while (this.ageFrac >= 1 && Game.effectsEnabled)
				{
					this.ageFrac -= 1;

					if (this.effect.equals(BulletEffect.ice) || this.effect.equals(BulletEffect.ember))
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
						double var = 50;
						e.maxAge /= 2;

						double r1 = 128;
						double g1 = 255;
						double b1 = 255;

						if (this.effect.equals(BulletEffect.ember))
						{
							r1 = 255;
							g1 = 180;
							b1 = 0;
						}

						e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

						if (this.effect.equals(BulletEffect.ice))
						{
							e.glowR = 90;
							e.glowG = 180;
							e.glowB = 180;
						}

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);


						Game.effects.add(e);
					}
					else if (Game.fancyBulletTrails && this.effect.equals(BulletEffect.dark_fire))
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
						double var = 50;
						e.maxAge /= 4;
						e.colR = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, 0 + Math.random() * var - var / 2));
						e.enableGlow = false;

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 12);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);


						Game.effects.add(e);
					}
					else if (Game.fancyBulletTrails && (this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.fire_trail)))
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
						double var = 50;
						e.maxAge /= 4;
						e.colR = 255;
						e.colG = Math.min(255, Math.max(0, 180 + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, 64 + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 12);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);


						Game.effects.add(e);
					}
				}

				while (this.quarterAgeFrac >= 0.25)
				{
					this.quarterAgeFrac -= 0.25;
				}

				while (this.halfAgeFrac >= 0.5)
				{
					this.halfAgeFrac -= 0.5;
				}
			}
		}

		if (this.destroyTimer >= maxDestroyTimer && noTrails)
		{
			Game.removeMovables.add(this);
		}

		this.updateTrails();

		this.addedTrail = false;

		if (this.enableCollision)
		{
			if (!this.tank.isRemote)
				this.checkCollision();
			else
				this.checkCollisionLocal();
		}

		this.age += frameFrequency;

		if (((this.age > lifespan && this.lifespan > 0) || (this.range > 0 && Math.pow(this.originX - this.posX, 2) + Math.pow(this.originY - posY, 2) > this.range * this.range)) && !this.destroy)
		{
			this.pop();
			this.collisionX = this.posX;
			this.collisionY = this.posY;
			this.collided();
		}
	}

	public void updateTrails()
	{
		if (this.effect != BulletEffect.none && !this.addedTrail && !this.destroy &&
				(Movable.absoluteAngleBetween(this.getPolarDirection(), this.lastTrailAngle) >= 0.001 || (this.trail3d && Movable.absoluteAngleBetween(this.getPolarPitch(), this.lastTrailPitch) >= 0.1)))
		{
			this.addTrail(true);
		}
	}

	public void addTrail()
	{
		this.addTrail(false);
	}

	public void addTrail(boolean redirect)
	{
		if (this.trails == null)
			this.initTrails();

		this.addedTrail = true;

		if (!Game.bulletTrails || this.effect == BulletEffect.none)
			return;

		double speed = this.speed;

		double x = this.collisionX;
		double y = this.collisionY;
		double z = this.posZ;

		if (redirect)
		{
			x = this.posX;
			y = this.posY;
		}

		this.lastTrailAngle = this.getPolarDirection();

		if (this.trail3d)
			this.lastTrailPitch = this.getPolarPitch();

		for (int i = 0; i < this.trailEffects.size(); i++)
		{
			Trail t = this.trailEffects.get(i);

			if (!this.trail3d || !Game.enable3d)
				this.addTrailObj(i, new Trail(this, this.speed, x, y, this.size * speed / 3.125 * t.delay, this.size / 2 * t.backWidth, this.size / 2 * t.frontWidth, this.size * speed / 3.125 * t.maxLength, this.lastTrailAngle,
					t.frontR, t.frontG, t.frontB, t.frontA, t.backR, t.backG, t.backB, t.backA, t.glow, t.luminosity, t.frontCircle, t.backCircle), redirect);
			else
				this.addTrailObj(i, new Trail3D(this, this.speed, x, y, z, this.size * speed / 3.125 * t.delay, this.size / 2 * t.backWidth, this.size / 2 * t.frontWidth, this.size * speed / 3.125 * t.maxLength, this.lastTrailAngle, this.lastTrailPitch,
						t.frontR, t.frontG, t.frontB, t.frontA, t.backR, t.backG, t.backB, t.backA, t.glow, t.luminosity, t.frontCircle, t.backCircle), redirect);
		}
	}

	public void stopTrails()
	{
		for (ArrayList<Trail> trail : this.trails)
		{
			if (trail.size() > 0)
			{
				Trail t = trail.get(0);
				if (t.spawning)
				{
					t.spawning = false;
					t.frontX = this.posX;
					t.frontY = this.posY;
				}
			}
		}
	}

	protected void addTrailObj(int group, Trail t, boolean redirect)
	{
		Trail old = null;

		if (this.trails[group].size() > 0)
			old = this.trails[group].get(0);

		this.trails[group].add(0, t);

		if (old != null && old.spawning)
		{
			old.spawning = false;
			old.frontX = t.backX;
			old.frontY = t.backY;

			if (redirect)
			{
				double angle = this.getPolarDirection();
				double offset = Movable.angleBetween(angle, old.angle) / 2;

				if (t instanceof Trail3D && old instanceof Trail3D)
				{
					Trail3D t1 = (Trail3D) t;
					Trail3D old1 = (Trail3D) old;
					double offset2 = Movable.angleBetween(t1.pitch, old1.pitch) / 2;
					old1.setFrontAngleOffset(offset, offset2);
					t1.setBackAngleOffset(-offset, -offset2);
				}
				else
				{
					old.setFrontAngleOffset(offset);
					t.setBackAngleOffset(-offset);
				}
			}
		}
	}

	@Override
	public void draw()
	{
		if (this.delay > 0)
			return;

		double glow = this.em().getAttributeValue(AttributeModifier.glow, 0.5);

		if (this.freezing && Game.bulletTrails)
		{
			for (int i = 0; i < 30 - 10 * Math.sin(this.age / 12.0); i++)
			{
				Drawing.drawing.setColor(255, 255, 255, 20, 1);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, i * 4, i * 4);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, i * 4, i * 4);
			}
		}

		if (this.boosting && Game.glowEnabled)
		{
			double frac = Math.min(1, this.destroyTimer / 60);
			Drawing.drawing.setColor(255, 180, 0, 180 * frac, 1);

			if (Game.enable3d)
				Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, this.size * 8, this.size * 8);
			else
				Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 8, this.size * 8);
		}

		if (Game.glowEnabled)
		{
			Drawing.drawing.setColor(this.outlineColorR * glow * 2, this.outlineColorG * glow * 2, this.outlineColorB * glow * 2, 255, 1);

			double sizeMul = 1;
			boolean shade = false;

			if (this.effect == BulletEffect.fire || this.effect == BulletEffect.fire_trail)
			{
				Drawing.drawing.setColor(255, 180, 0, 200, 1);
				sizeMul = 4;
			}
			else if (this.effect == BulletEffect.dark_fire)
			{
				Drawing.drawing.setColor(0, 0, 0, 127);
				sizeMul = 1.5;
				shade = true;
			}

			if (this.destroyTimer < 60.0)
			{
				sizeMul *= 1 - destroyTimer / 60.0;

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ, this.size * 4 * sizeMul, this.size * 4 * sizeMul, true, true, shade);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4 * sizeMul, this.size * 4 * sizeMul, shade);
			}
		}

		if (this.trails == null)
			this.initTrails();

		for (ArrayList<Trail> trail : this.trails)
		{
			for (Trail t : trail)
			{
				t.draw();
			}
		}

		if (this.destroyTimer < 60.0)
		{
			double opacity = ((60 - destroyTimer) / 60.0);
			double sizeModifier = destroyTimer * (size / Bullet.bullet_size);

			if (Game.playerTank != null && Game.playerTank.team != null && !Game.playerTank.team.friendlyFire && Team.isAllied(Game.playerTank, this))
			{
				double opacityMod = 0.25 + 0.25 * Math.sin(this.age / 100.0 * Math.PI * 4) * opacityMultiplier;
				double s = 2.5;

				Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, opacity * opacity * opacity * 255.0 * opacityMod, glow);

				if (Game.enable3d)
					Drawing.drawing.fillOval(posX, posY, posZ, s * size + sizeModifier, s * size + sizeModifier);
				else
					Drawing.drawing.fillOval(posX, posY, s * size + sizeModifier, s * size + sizeModifier);
			}


			if (this.bulletHitKnockback != 0 || this.tankHitKnockback != 0)
			{
				Drawing.drawing.setColor(255, 0, 255, opacity * opacity * opacity * 255.0 * opacityMultiplier, glow);
				double bumper = (1 + Math.sin(this.age / 100.0 * Math.PI * 4)) * 0.25 + 1.2;
				if (Game.enable3d)
					Drawing.drawing.fillOval(posX, posY, posZ, bumper * size + sizeModifier, bumper * size + sizeModifier);
				else
					Drawing.drawing.fillOval(posX, posY, bumper * size + sizeModifier, bumper * size + sizeModifier);
			}

			Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, opacity * opacity * opacity * 255.0 * opacityMultiplier, glow);

			if (Game.enable3d)
			{
				if (Game.xrayBullets && this.respectXRay)
					Drawing.drawing.fillOval(posX, posY, posZ - 0.5, size + sizeModifier, size + sizeModifier, false, true);
				Drawing.drawing.fillOval(posX, posY, posZ, size + sizeModifier, size + sizeModifier);
			}
			else
				Drawing.drawing.fillOval(posX, posY, size + sizeModifier, size + sizeModifier);

			Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, opacity * opacity * opacity * 255.0 * opacityMultiplier, glow);

			if (Game.enable3d)
				Drawing.drawing.fillOval(posX, posY, posZ, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6, 1);
			else
				Drawing.drawing.fillOval(posX, posY, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6);

		}

		if (this.homingSharpness != 0 && !this.homingSilent)
			this.drawHoming();
	}

	public void drawHoming()
	{
		double limit = 50;
		if (!ScreenGame.finishedQuick && this.homingTarget != null)
		{
			double frac;

			frac = Math.min(homingTargetTime / limit, 1);

			double s = (2 - frac) * 80;
			double d = Math.min((1 - this.destroyTimer / this.maxDestroyTimer) * 2, 1);

			Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, frac * 255 * d, 1);
			Drawing.drawing.drawImage(frac * Math.PI / 2 + this.getAngleInDirection(this.homingTarget.posX, this.homingTarget.posY), "cursor.png", this.homingTarget.posX, this.homingTarget.posY, s, s);

			if (Game.glowEnabled)
			{
				Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, frac * 255 * d, 1);
				Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 16, this.size * 16);
			}
		}
		Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, 255, 1);
	}

	@Override
	public void initEffectManager(EffectManager em)
	{
		em.addAttributeCallback = this::sendEvent;
	}

	public void sendEvent(AttributeModifier m, boolean unduplicate)
	{
		if (!this.isRemote)
			Game.eventsOut.add(new EventBulletAddAttributeModifier(this, m, unduplicate));
	}

	public void onDestroy()
	{
		if (!ScreenPartyLobby.isClient)
		{
			if (this.hitExplosion != null)
			{
				this.hitExplosion.posX = this.posX;
				this.hitExplosion.posY = this.posY;
				this.hitExplosion.tank = this.tank;
				this.hitExplosion.item = this.item;
				this.hitExplosion.team = this.team;
				this.hitExplosion.explode();
			}

			if (this.freezing)
			{
				Game.movables.add(new AreaEffectFreeze(this.posX, this.posY));
				Drawing.drawing.playGlobalSound("freeze.ogg");
			}
		}

		if (this.boosting)
		{
			if (Game.playerTank != null && !Game.playerTank.destroy)
			{
				double distsq = Math.pow(this.posX - Game.playerTank.posX, 2) + Math.pow(this.posY - Game.playerTank.posY, 2);

				double radius = 250000;
				if (distsq <= radius)
				{
					Drawing.drawing.playSound("boost.ogg", (float) (10.0 / this.size), (float) ((radius - distsq) / radius));
				}
			}

			if (Game.effectsEnabled)
			{
				for (int i = 0; i < 25 * Game.effectMultiplier; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, Game.tile_size / 2, Effect.EffectType.piece);
					double var = 50;

					e.colR = Math.min(255, Math.max(0, this.outlineColorR + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, this.outlineColorG + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, this.outlineColorB + Math.random() * var - var / 2));

					if (Game.enable3d)
						e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() + 0.5);
					else
						e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() + 0.5);

					Game.effects.add(e);
				}
			}
		}

		em().recycle();
	}

	public void addDestroyEffect()
	{
		for (int i = 0; i < this.size * 4 * Game.effectMultiplier; i++)
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
			double var = 50;
			e.maxAge /= 2;
			e.colR = Math.min(255, Math.max(0, this.baseColorR + Math.random() * var - var / 2));
			e.colG = Math.min(255, Math.max(0, this.baseColorG + Math.random() * var - var / 2));
			e.colB = Math.min(255, Math.max(0, this.baseColorB + Math.random() * var - var / 2));
			e.glowR = e.colR - this.outlineColorR;
			e.glowG = e.colG - this.outlineColorG;
			e.glowB = e.colB - this.outlineColorB;

			if (Game.enable3d)
				e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0 * 4);
			else
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

			Game.effects.add(e);
		}
	}

	public double getSize()
	{
		return size;
	}

	public double getRangeMin()
	{
		return -1;
	}

	public double getRangeMax()
	{
		return this.range;
	}

	@Override
	public String getName()
	{
		return Game.formatString(this.typeName);
	}
}

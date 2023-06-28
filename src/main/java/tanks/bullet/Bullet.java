package tanks.bullet;

import tanks.*;
import tanks.bullet.legacy.BulletFlame;
import tanks.gui.ChatMessage;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.hotbar.item.ItemBullet;
import tanks.minigames.Minigame;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.tank.*;

import java.util.ArrayList;
import java.util.HashMap;

import static tanks.tank.TankProperty.Category.appearanceGlow;

public class Bullet extends Movable implements IDrawableLightSource
{
	public static int currentID = 0;
	public static ArrayList<Integer> freeIDs = new ArrayList<>();
	public static HashMap<Integer, Bullet> idMap = new HashMap<>();

	public static String bullet_name = "normal";

	public int networkID;

	public enum BulletEffect {none, fire, darkFire, fireTrail, ice, trail, ember}

	public static double bullet_size = 10;

	@BulletProperty(id = "name", name = "Bullet name", category = BulletProperty.Category.appearance)
	public String name;

	public boolean playPopSound = true;
	public boolean playBounceSound = true;
	public double age = 0;

	@BulletProperty(id = "size", name = "Size", category = BulletProperty.Category.appearance)
	public double size;

	public boolean canBeCanceled = true;
	public boolean moveOut = true;

	@BulletProperty(id = "bounces", name = "Bounces", category = BulletProperty.Category.travel)
	public int bounces;
	public int bouncyBounces = 100;

	public double ageFrac = 0;
	public double quarterAgeFrac = 0;
	public double halfAgeFrac = 0;

	@BulletProperty(id = "override_color", name = "Custom primary color", category = BulletProperty.Category.appearance)
	public boolean overrideBaseColor;
	@BulletProperty(id = "color_r", name = "Primary red", category = BulletProperty.Category.appearance)
	public double baseColorR;
	@BulletProperty(id = "color_g", name = "Primary green", category = BulletProperty.Category.appearance)
	public double baseColorG;
	@BulletProperty(id = "color_b", name = "Primary blue", category = BulletProperty.Category.appearance)
	public double baseColorB;

	@BulletProperty(id = "override_color2", name = "Custom secondary color", category = BulletProperty.Category.appearance)
	public boolean overrideOutlineColor;
	@BulletProperty(id = "color_r2", name = "Secondary red", category = BulletProperty.Category.appearance)
	public double outlineColorR;
	@BulletProperty(id = "color_g2", name = "Secondary green", category = BulletProperty.Category.appearance)
	public double outlineColorG;
	@BulletProperty(id = "color_b2", name = "Secondary blue", category = BulletProperty.Category.appearance)
	public double outlineColorB;

	@BulletProperty(id = "luminance", name = "Luminance", category = BulletProperty.Category.appearance)
	public double luminance = 0.5;

	@TankProperty(category = appearanceGlow, id = "glow_intensity", name = "Aura intensity")
	public double glowIntensity = 1;
	@TankProperty(category = appearanceGlow, id = "glow_size", name = "Aura size")
	public double glowSize = 4;

	public double iPosZ;
	public boolean autoZ = true;

	public double destroyTimer = 0;
	public double maxDestroyTimer = 60;

	public Tank tank;

	@BulletProperty(id = "damage", name = "Damage", category = BulletProperty.Category.impact)
	public double damage = 1;

	@BulletProperty(id = "knockback_tank", name = "Tank knockback", category = BulletProperty.Category.impact)
	public double tankHitKnockback = 0;

	@BulletProperty(id = "knockback_bullet", name = "Bullet knockback", category = BulletProperty.Category.impact)
	public double bulletHitKnockback = 0;

	@BulletProperty(id = "explosion", name = "Explosion", category = BulletProperty.Category.impact)
	public Explosion hitExplosion = null;

	@BulletProperty(id = "stun", name = "Stun duration", category = BulletProperty.Category.impact)
	public double hitStun = 0;

	@BulletProperty(id = "area_effect", name = "Area effect", category = BulletProperty.Category.impact)
	public AreaEffect hitAreaEffect = null;


	public boolean enableExternalCollisions = true;

	@BulletProperty(id = "collide_obstacles", name = "Obstacle collision", category = BulletProperty.Category.travel)
	public boolean obstacleCollision = true;

	@BulletProperty(id = "collide_bullets", name = "Bullet collision", category = BulletProperty.Category.travel)
	public boolean bulletCollision = true;

	public boolean destroyBullets = true;

	@BulletProperty(id = "bush_burn", name = "Burns shrubbery", category = BulletProperty.Category.travel)
	public boolean burnsBushes = false;

	@BulletProperty(id = "bush_lower", name = "Lowers shrubbery", category = BulletProperty.Category.travel)
	public boolean lowersBushes = true;

	@BulletProperty(id = "speed", name = "Speed", category = BulletProperty.Category.travel)
	public double speed = 0;

	@BulletProperty(id = "lifespan", name = "Lifespan", category = BulletProperty.Category.travel)
	public double life = 0;

	@BulletProperty(id = "effect", name = "Effect", category = BulletProperty.Category.appearance)
	public BulletEffect effect = BulletEffect.none;

	public boolean useCustomWallCollision = false;
	public double wallCollisionSize = 10;

	@BulletProperty(id = "heavy", name = "Heavy", category = BulletProperty.Category.travel)
	public boolean heavy = false;

	public ItemBullet item;

	@BulletProperty(id = "max_live_bullets", name = "Max live bullets", category = BulletProperty.Category.firing)
	public int maxLiveBullets = 5;

	@BulletProperty(id = "cooldown", name = "Cooldown", category = BulletProperty.Category.firing)
	public double cooldown = 20;

	@BulletProperty(id = "recoil", name = "Recoil", category = BulletProperty.Category.firing)
	public double recoil = 1.0;

	@BulletProperty(id = "accuracy_spread_angle", name = "Accuracy spread angle", category = BulletProperty.Category.firing)
	public double accuracySpread = 0;

	@BulletProperty(id = "shot_count", name = "Shot count", category = BulletProperty.Category.firing)
	public int shotCount = 1;

	@BulletProperty(id = "multishot_spread_angle", name = "Multishot spread angle", category = BulletProperty.Category.firing)
	public double multishotSpread = 0;

	public boolean shouldDodge = true;

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

	@BulletProperty(id = "sound", name = "Shot sound", category = BulletProperty.Category.firing)
	public String itemSound = "shoot.ogg";
	@BulletProperty(id = "sound_pitch_variation", name = "Sound pitch variation", category = BulletProperty.Category.firing)
	public double pitchVariation = 0;

	protected ArrayList<Trail>[] trails;
	protected boolean addedTrail = false;
	protected double lastTrailAngle = -1;

	public boolean justBounced = false;

	public double[] lightInfo = new double[]{0, 0, 0, 0, 0, 0, 0};

	public Bullet(double x, double y, int bounces, Tank t, ItemBullet item)
	{
		this(x, y, bounces, t, true, item);
	}

	public Bullet(double x, double y, int bounces, Tank t, boolean affectsMaxLiveBullets, ItemBullet item)
	{
		super(x, y);

		this.item = item;
		this.vX = 0;
		this.vY = 0;
		this.size = bullet_size;
		this.baseColorR = t.colorR;
		this.baseColorG = t.colorG;
		this.baseColorB = t.colorB;

		double[] oc = Team.getObjectColor(t.secondaryColorR, t.secondaryColorG, t.secondaryColorB, t);
		this.outlineColorR = oc[0];
		this.outlineColorG = oc[1];
		this.outlineColorB = oc[2];

		this.bounces = bounces;
		this.tank = t;
		this.team = t.team;
		this.name = bullet_name;

		this.iPosZ = this.tank.size / 2 + this.tank.turretSize / 2;

		this.isRemote = t.isRemote;

		//if (!t.isRemote && fireEvent)
		//	Game.eventsOut.add(new EventShootBullet(this));

		this.affectsMaxLiveBullets = affectsMaxLiveBullets;

		if (!this.tank.isRemote && this.affectsMaxLiveBullets)
			this.item.liveBullets++;

		AttributeModifier a = this.tank.getAttribute(AttributeModifier.bullet_boost);

		if (a != null)
			this.addStatusEffect(StatusEffect.boost_bullet, a.age, 0, a.deteriorationAge, a.duration);

		this.trails = (ArrayList<Trail>[])(new ArrayList[10]);

		for (int i = 0; i < this.trails.length; i++)
			this.trails[i] = new ArrayList<>();

		if (!this.tank.isRemote)
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

		this.drawLevel = 8;

		for (IFixedMenu m : ModAPI.menuGroup)
		{
			if (m instanceof Scoreboard)
			{
				if (((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.shots_fired) ||
						(((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.shots_fired_no_multiple_fire)
								&& !(this instanceof BulletHealing || this instanceof BulletFlame)))
				{
					if (((Scoreboard) m).players.isEmpty())
						((Scoreboard) m).addTeamScore(this.team, 1);
					else if (this.tank instanceof TankPlayer)
						((Scoreboard) m).addPlayerScore(((TankPlayer) this.tank).player, 1);
					else if (this.tank instanceof TankPlayerRemote)
						((Scoreboard) m).addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
				}
			}
		}
	}

	public void moveOut(double amount)
	{
		this.moveInDirection(vX, vY, amount);
	}

	public void setTargetLocation(double x, double y)
	{

	}

	public void collided()
	{

	}

	public void collidedWithTank(Tank t)
	{
		if (!heavy)
		{
			this.playPopSound = false;
			this.pop();
		}

		if (!(Team.isAllied(this, t) && !this.team.friendlyFire) && !ScreenGame.finishedQuick && t.getDamageMultiplier(this) > 0)
		{
			if (this.tankHitKnockback > 0)
			{
				double mul = Game.tile_size * Game.tile_size / Math.pow(t.size, 2) * this.tankHitKnockback;
				t.vX += this.vX * mul;
				t.vY += this.vY * mul;

				t.recoilSpeed = t.getSpeed();
				if (t.recoilSpeed > t.maxSpeed)
				{
					t.inControlOfMotion = false;
					t.tookRecoil = true;
				}

				if (this.damage <= 0 && this.playBounceSound)
					Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / size));
			}

			boolean kill = t.damage(this.damage * this.frameDamageMultipler, this);

			if (kill)
			{
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
				else if (this.tank instanceof TankPlayerRemote && Crusade.crusadeMode)
				{
					((TankPlayerRemote) this.tank).player.hotbar.coins += t.coinValue;
					Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
				}
				else if ((Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0) && !(t instanceof TankPlayer || t instanceof TankPlayerRemote))
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
			else if (this.playPopSound && this.damage > 0)
				Drawing.drawing.playGlobalSound("damage.ogg", (float) (bullet_size / size));
		}
		else if (this.playPopSound && !this.heavy)
			Drawing.drawing.playGlobalSound("bullet_explode.ogg", (float) (bullet_size / size));
	}

	public void collidedWithObject(Movable o)
	{
		if (o instanceof Mine)
			this.collidedWithMisc(o);
		else if (o instanceof Bullet && ((Bullet) o).enableCollision && ((Bullet) o).enableExternalCollisions)
			this.collidedWithBullet((Bullet) o);
	}

	public void push(Bullet b)
	{
		b.vX += this.vX * Math.pow(this.size, 2) / Math.pow(b.size, 2) * this.bulletHitKnockback * Panel.frameFrequency;
		b.vY += this.vY * Math.pow(this.size, 2) / Math.pow(b.size, 2) * this.bulletHitKnockback * Panel.frameFrequency;
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

		double ourMass = this.bulletHitKnockback * this.size * this.size;
		double theirMass = b.bulletHitKnockback * b.size * b.size;

		this.collisionX = this.posX;
		this.collisionY = this.posY;
		b.collisionX = b.posX;
		b.collisionY = b.posY;

		double co1 = (ourSpeed * Math.cos(ourDir - toAngle) * (ourMass - theirMass) + 2 * theirMass * theirSpeed * Math.cos(theirDir - toAngle)) / (ourMass + theirMass);
		double vX1 = co1 * Math.cos(toAngle) + ourSpeed * Math.sin(ourDir - toAngle) * Math.cos(toAngle + Math.PI / 2);
		double vY1 = co1 * Math.sin(toAngle) + ourSpeed * Math.sin(ourDir - toAngle) * Math.sin(toAngle + Math.PI / 2);

		double co2 = (theirSpeed * Math.cos(theirDir - toAngle) * (theirMass - ourMass) + 2 * ourMass * ourSpeed * Math.cos(ourDir - toAngle)) / (theirMass + ourMass);
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

		if (this.playBounceSound && b.playBounceSound)
		{
			Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / size), 0.5f);
			Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / b.size), 0.5f);
		}

		this.addTrail();
		b.addTrail();
	}

	public void collidedWithBullet(Bullet b)
	{
		if (this.heavy == b.heavy && this.enableExternalCollisions)
		{
			if (this.bulletHitKnockback <= 0 && b.bulletHitKnockback <= 0)
			{
				this.pop();
				if (this.destroyBullets)
					b.pop();
			}
			else if (this.bulletHitKnockback > 0 && b.bulletHitKnockback <= 0)
			{
				this.push(b);
				if (this.destroyBullets)
					this.pop();
			}
			else if (this.bulletHitKnockback <= 0)
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

			if (this.playBounceSound)
				Drawing.drawing.playSound("bump.ogg", (float) (bullet_size / h.size), 1f);

			if (h.bulletHitKnockback > 0)
				h.push(l);
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

				if ((!o.bulletCollision && !o.checkForObjects) || o.startHeight > 1)
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
						this.posX += 2 * (horizontalDist - bound);
						this.vX = -Math.abs(this.vX);

						this.collisionX = this.posX - (horizontalDist - bound);
						this.collisionY = this.posY - (horizontalDist - bound) / vX * vY;
					}
					else if (!up && dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						this.posY += 2 * (verticalDist - bound);
						this.vY = -Math.abs(this.vY);

						this.collisionX = this.posX - (verticalDist - bound) / vY * vX;
						this.collisionY = this.posY - (verticalDist - bound);
					}
					else if (!right && dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						this.posX -= 2 * (horizontalDist - bound);
						this.vX = Math.abs(this.vX);

						this.collisionX = this.posX + (horizontalDist - bound);
						this.collisionY = this.posY + (horizontalDist - bound) / vX * vY;
					}
					else if (!down && dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						this.posY -= 2 * (verticalDist - bound);
						this.vY = Math.abs(this.vY);

						this.collisionX = this.posX + (verticalDist - bound) / vY * vX;
						this.collisionY = this.posY + (verticalDist - bound);
					}
				}
			}

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
					}

					this.inside.add(t);
				}
			}
			else if (((o instanceof Bullet && ((Bullet) o).enableCollision && (((Bullet) o).bulletCollision && ((Bullet) o).externalBulletCollision && this.bulletCollision)) || o instanceof Mine) && o != this && !o.destroy)
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

	@Override
	public void update()
	{
		if (!this.isRemote && ScreenPartyHost.isServer && (this.vX != this.lastOriginalVX || this.vY != this.lastOriginalVY) && !justBounced)
			Game.eventsOut.add(new EventBulletUpdate(this));

		this.justBounced = false;

		super.update();

		if (this.age == 0)
		{
			this.collisionX = this.posX;
			this.collisionY = this.posY;

			this.addTrail();
		}

		if (!this.destroy)
		{
			double frac = Math.pow(0.999, Panel.frameFrequency);
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
					trailLength += t.update(trailLength);
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

				if (this.affectsMaxLiveBullets)
				{
					this.item.liveBullets--;
				}

				if (!this.isRemote)
					this.onDestroy();
			}

			if (this.destroyTimer <= 0 && Game.effectsEnabled)
			{
				this.addDestroyEffect();
			}

			this.destroyTimer += Panel.frameFrequency;
			this.vX = 0;
			this.vY = 0;
		}
		else
		{
			double frac = 1 / (1 + this.age / 100);

			if (this.autoZ)
				this.posZ = this.iPosZ * frac + (Game.tile_size / 4) * (1 - frac);

			this.ageFrac += Panel.frameFrequency * Game.effectMultiplier;
			this.halfAgeFrac += Panel.frameFrequency * Game.effectMultiplier;
			this.quarterAgeFrac += Panel.frameFrequency * Game.effectMultiplier;

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
					else if (Game.fancyBulletTrails && this.effect.equals(BulletEffect.darkFire))
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
					else if (Game.fancyBulletTrails && (this.effect.equals(BulletEffect.fire) || this.effect.equals(BulletEffect.fireTrail)))
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

		if (this.effect != BulletEffect.none && !this.addedTrail && !this.destroy && Movable.absoluteAngleBetween(this.getPolarDirection(), this.lastTrailAngle) >= 0.001)
		{
			this.addTrail(true);
		}

		this.addedTrail = false;

		if (this.enableCollision)
		{
			if (!this.tank.isRemote)
				this.checkCollision();
			else
				this.checkCollisionLocal();
		}

		this.age += Panel.frameFrequency;

		if (this.age > life && this.life > 0)
			this.destroy = true;
	}

	public void addTrail()
	{
		this.addTrail(false);
	}

	public void addTrail(boolean redirect)
	{
		this.addedTrail = true;

		if (!Game.bulletTrails || this.effect == BulletEffect.none)
			return;

		double speed = this.speed;

		double x = this.collisionX;
		double y = this.collisionY;

		if (redirect)
		{
			x = this.posX;
			y = this.posY;
		}

		for (ArrayList<Trail> trail : this.trails)
		{
			if (trail.size() > 0)
			{
				Trail t = trail.get(0);
				t.spawning = false;
				t.frontX = x;
				t.frontY = y;
			}
		}

		this.lastTrailAngle = this.getPolarDirection();

		if (this.effect == BulletEffect.trail || this.effect == BulletEffect.fire || this.effect == BulletEffect.darkFire)
			this.addTrailObj(0, new Trail(this, this.speed, x, y, this.size / 2, this.size / 2, 15 * this.size * speed / 3.125, this.lastTrailAngle, 127, 127, 127, 100, 127, 127, 127, 0, false, 0.5), redirect);

		if (this.effect == BulletEffect.fire || this.effect == BulletEffect.fireTrail)
			this.addTrailObj(2, new Trail(this, this.speed, x, y, this.size / 2 * 5, this.size / 2, 10 * this.size * speed / 6.25, this.lastTrailAngle, 255, 255, 0, 255, 255, 0, 0, 0, false, 1), redirect);

		if (this.effect == BulletEffect.darkFire)
			this.addTrailObj(2, new Trail(this, this.speed, x, y, this.size / 2 * 5, this.size / 2, 10 * this.size * speed / 6.25, this.lastTrailAngle, 64, 0, 128, 255, 0, 0, 0, 0, false, 0.5), redirect);

		if (this.effect == BulletEffect.fireTrail)
		{
			Trail t = new Trail(this, this.speed, x, y, this.size, this.size, 100 * this.size * speed / 6.25, this.lastTrailAngle, 80, 80, 80, 100, 80, 80, 80, 0, false, 0.5);
			t.delay = 14 * this.size * speed / 6.25;
			t.frontCircle = false;
			this.addTrailObj(0, t, redirect);

			Trail t2 = new Trail(this, this.speed, x, y, this.size, this.size, 8 * this.size * speed / 6.25, this.lastTrailAngle, 80, 80, 80, 0, 80, 80, 80, 100, false, 0.5);
			t2.delay = 6 * this.size * speed / 6.25;
			t2.backCircle = false;
			this.addTrailObj(1, t2, redirect);
		}
	}

	protected void addTrailObj(int group, Trail t, boolean redirect)
	{
		Trail old = null;

		if (this.trails[group].size() > 0)
			old = this.trails[group].get(0);

		this.trails[group].add(0, t);

		if (redirect && old != null)
		{
			double angle = this.getPolarDirection();
			double offset = Movable.angleBetween(angle, old.angle) / 2;
			old.setFrontAngleOffset(offset);
			t.setBackAngleOffset(-offset);
		}
	}

	@Override
	public void draw()
	{
		double glow = this.getAttributeValue(AttributeModifier.glow, 0.5);

		if (Game.glowEnabled)
		{
			Drawing.drawing.setColor(this.outlineColorR * glow * 2, this.outlineColorG * glow * 2, this.outlineColorB * glow * 2, 255, 1);

			double sizeMul = 1;
			boolean shade = false;

			if (this.effect == BulletEffect.fire || this.effect == BulletEffect.fireTrail)
			{
				Drawing.drawing.setColor(255, 180, 0, 200, 1);
				sizeMul = 4;
			}
			else if (this.effect == BulletEffect.darkFire)
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
				double opacityMod = 0.25 + 0.25 * Math.sin(this.age / 100.0 * Math.PI * 4);
				double s = 2.5;

				Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, opacity * opacity * opacity * 255.0 * opacityMod, glow);

				if (Game.enable3d)
					Drawing.drawing.fillOval(posX, posY, posZ, s * size + sizeModifier, s * size + sizeModifier);
				else
					Drawing.drawing.fillOval(posX, posY, s * size + sizeModifier, s * size + sizeModifier);
			}


			if (this.bulletHitKnockback > 0 || this.tankHitKnockback > 0)
			{
				Drawing.drawing.setColor(255, 0, 255, opacity * opacity * opacity * 255.0, glow);
				double bumper = (1 + Math.sin(this.age / 100.0 * Math.PI * 4)) * 0.25 + 1.2;
				if (Game.enable3d)
					Drawing.drawing.fillOval(posX, posY, posZ, bumper * size + sizeModifier, bumper * size + sizeModifier);
				else
					Drawing.drawing.fillOval(posX, posY, bumper * size + sizeModifier, bumper * size + sizeModifier);
			}

			Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, opacity * opacity * opacity * 255.0, glow);

			if (Game.enable3d)
			{
				if (Game.xrayBullets)
					Drawing.drawing.fillOval(posX, posY, posZ - 0.5, size + sizeModifier, size + sizeModifier, false, true);
				Drawing.drawing.fillOval(posX, posY, posZ, size + sizeModifier, size + sizeModifier);
			}
			else
				Drawing.drawing.fillOval(posX, posY, size + sizeModifier, size + sizeModifier);

			Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, opacity * opacity * opacity * 255.0, glow);

			if (Game.enable3d)
				Drawing.drawing.fillOval(posX, posY, posZ, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6, 1);
			else
				Drawing.drawing.fillOval(posX, posY, (size + sizeModifier) * 0.6, (size + sizeModifier) * 0.6);

		}
	}

	@Override
	public void addAttribute(AttributeModifier m)
	{
		super.addAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventBulletAddAttributeModifier(this, m, false));
	}

	@Override
	public void addUnduplicateAttribute(AttributeModifier m)
	{
		super.addUnduplicateAttribute(m);

		if (!this.isRemote)
			Game.eventsOut.add(new EventBulletAddAttributeModifier(this, m, true));
	}

	public void onDestroy()
	{

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

	@Override
	public boolean lit()
	{
		return false;
	}

	@Override
	public double[] getLightInfo()
	{
		this.lightInfo[3] = Math.max(0, 1 - this.destroyTimer / this.maxDestroyTimer);
		this.lightInfo[4] = this.outlineColorR;
		this.lightInfo[5] = this.outlineColorG;
		this.lightInfo[6] = this.outlineColorB;
		return this.lightInfo;
	}

}

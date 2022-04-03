package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventLayMine;
import tanks.event.EventShootBullet;
import tanks.event.EventTankUpdateVisibility;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemMine;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTeleporter;
import static tanks.tank.TankPropertyAnnotation.Category.*;

import java.util.ArrayList;
import java.util.Random;

/** This class is the 'skeleton' tank class.
 *  It can be extended and values can be changed to easily produce an AI for another tank.
 *  Also, the behavior is split into many methods which are intended to be overridden easily.*/
public class TankAIControlled extends Tank
{
	/** The type which shows what direction the tank is moving. Clockwise and Counter Clockwise are for idle, while Aiming is for when the tank aims.*/
	protected enum RotationPhase {clockwise, counterClockwise, aiming}

	// The following are properties which are used externally to determine the behavior settings of the tank.
	// Simple modifications of tanks can just change these values to produce a desired behavior.
	// More complex behaviors may require overriding of methods.
	// These values do not change normally along the course of the game.

	/** When set to true, the tank will vanish when the level begins*/
	@TankPropertyAnnotation(category = appearanceGeneral, name = "Invisible")
	public boolean invisible = false;

	@TankPropertyAnnotation(category = movementGeneral, name = "Can move")
	public boolean enableMovement = true;

	/** Chance per frame to change direction*/
	@TankPropertyAnnotation(category = movementIdle, name = "Turn chance", desc = "Chance of the tank to change the direction in which it is moving")
	public double motionChangeChance = 0.01;
	/** Time waited when changing direction of motion*/
	@TankPropertyAnnotation(category = movementIdle, name = "Turn pause time", desc = "Time the tank pauses when changing directions")
	public double directionChangeCooldown = 15;
	/** Multiplier of time the tank will hide in a shrub*/
	@TankPropertyAnnotation(category = movementIdle, name = "Bush hide time", desc = "Time the tank will stop moving to hide in bushes")
	public double hideAmount = 350;

	@TankPropertyAnnotation(category = movementAvoid, name = "Avoid bullets")
	public boolean enableBulletAvoidance = true;
	@TankPropertyAnnotation(category = movementAvoid, name = "Avoid mines")
	public boolean enableMineAvoidance = true;
	/** How close the tank needs to get to a mine to avoid it*/
	@TankPropertyAnnotation(category = movementAvoid, name = "Mine sight radius", desc = "If the tank is within this fraction of a mine's radius, it will move away from the mine")
	public double avoidSensitivity = 1.5;
	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	@TankPropertyAnnotation(category = movementAvoid, name = "Bullet flee time", desc = "Time the tank will continue fleeing from a bullet until after it is no longer deemed a threat")
	public double avoidTimerBase = 30;


	/** If enabled, the tank may actively seek out enemies*/
	@TankPropertyAnnotation(category = movementPathfinding, name = "Seek targets", desc = "If enabled, the tank may decide to navigate through the level towards its target. If this tank can lay mines, it may also use them to get to the target.")
	public boolean enablePathfinding = false;
	/** Chance per frame to seek the target enemy*/
	@TankPropertyAnnotation(category = movementPathfinding, name = "Seek chance", desc = "Chance for this tank to decide to start navigating to its target")
	public double seekChance = 0.001;
	/** If set to true, when enters line of sight of target enemy, will stop pathfinding to it*/
	@TankPropertyAnnotation(category = movementPathfinding, name = "Stop on sight", desc = "If enabled, navigation to target will end when the this tank enters the target's line of sight")
	public boolean stopSeekingOnSight = false;
	/** Increasing this value increases how stubborn the tank is in following a path*/
	@TankPropertyAnnotation(category = movementPathfinding, name = "Seek patience", desc = "If this tank is blocked from navigating its path for this amount of time, it will abandon the navigation")
	public double seekTimerBase = 200;

	/** Type of behavior tank should have if its target enemy is in line of sight
	 * 	Approach = go towards the target enemy
	 * 	Flee = go away from the target enemy
	 * 	Strafe = move perpendicular to target enemy*/
	public enum TargetEnemySightBehavior {approach, flee, strafe}

	/** When set to true, will shoot a ray at the target enemy and enable reactions when the target enemy is in sight*/
	@TankPropertyAnnotation(category = movementOnSight, name = "Test sight", desc = "When enabled, the tank will test if its target is in its line of sight, and react accordingly")
	public boolean enableLookingAtTargetEnemy = true;
	/** When set to true, will call reactToTargetEnemySight() when an unobstructed line of sight to the target enemy can be made */
	public boolean enableTargetEnemyReaction = true;
	/** Type of behavior tank should have if its target enemy is in line of sight*/
	@TankPropertyAnnotation(category = movementOnSight, name = "Reaction", desc = "How the tank should react upon line of sight - either flee from the target, approach it, or strafe around it")
	public TargetEnemySightBehavior targetEnemySightBehavior = TargetEnemySightBehavior.approach;
	/** If set to strafe upon seeing the target enemy, chance to change orbit direction*/
	@TankPropertyAnnotation(category = movementOnSight, name = "Strafe frequency", desc = "If set to strafe on line of sight, chance the tank should change the direction it is strafing around the target")
	public double strafeDirectionChangeChance = 0.01;

	@TankPropertyAnnotation(category = mines, name = "Can lay mines")
	public boolean enableMineLaying = true;
	@TankPropertyAnnotation(category = mines, name = "Mine")
	public ItemMine mine = (ItemMine) TankPlayer.default_mine.clone();
	//public double mineFuseLength = 1000;
	/** Minimum time to lay a mine, added to mineTimerRandom * this.random.nextDouble()*/
	@TankPropertyAnnotation(category = mines, name = "Base cooldown", desc = "Minimum time between laying mines")
	public double mineTimerBase = 2000;
	/** Random factor in calculating time to lay a mine, multiplied by this.random.nextDouble() and added to mineTimerBase*/
	@TankPropertyAnnotation(category = mines, name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between laying mines")
	public double mineTimerRandom = 4000;

	@TankPropertyAnnotation(category = firingGeneral, name = "Bullet")
	public ItemBullet bullet = (ItemBullet) TankPlayer.default_bullet.clone();
	/** Minimum time in between shooting bullets, added to cooldownRandom * this.random.nextDouble()*/
	@TankPropertyAnnotation(category = firingGeneral, name = "Base cooldown", desc = "Minimum time between firing bullets")
	public double cooldownBase = 60;
	/** Random factor in calculating time between shooting bullets, multiplied by this.random.nextDouble() and added to cooldownBase*/
	@TankPropertyAnnotation(category = firingGeneral, name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between firing bullets")
	public double cooldownRandom = 20;

	/*public int bulletBounces = 1;
	public double bulletSize = Bullet.bullet_size;
	public double bulletDamage = 1;
	public double bulletSpeed = 25.0 / 8;
	public boolean bulletHeavy = false;
	public Bullet.BulletEffect bulletEffect = Bullet.BulletEffect.trail;*/

	/** Determines which type of AI the tank will use when shooting.
	 *  Straight means that the tank will shoot directly at the target enemy if the target enemy is in line of sight.
	 *  Reflect means that the tank will use a Ray with reflections to find possible ways to hit the target enemy.
	 *  Alternate means that the tank will switch between shooting straight at the target enemy and using the reflect AI with every shot.
	 *  Wander means that the tank will randomly rotate and shoot only if it detects the target enemy*/
	public enum ShootAI {wander, straight, alternate, reflect}

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Inaccuracy", desc = "Random angle added to bullet trajectory upon shooting to make things more unpredictable")
	public double aimAccuracyOffset = 0.2;
	/** Threshold angle difference needed between angle and aimAngle to count as touching the target enemy*/
	public double aimThreshold = 0.05;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * this.random.nextDouble()*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Turret base timer", desc = "Minimum time the turret will idly rotate in one direction before changing direction")
	public double turretIdleTimerBase = 25;
	/** Random factor in calculating time to randomly change idle direction, multiplied by this.random.nextDouble() and added to turretIdleTimerBase*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Turret random timer", desc = "A random percentage between 0% and 100% of this time value is added to the turret base rotation timer to get the time between changing idle rotation direction")
	public double turretIdleTimerRandom = 500;

	/** Speed at which the turret moves while idle*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Idle turret speed", desc = "Speed the turret turns at when not actively aiming at a target")
	public double idleTurretSpeed = 0.005;
	/** Speed at which the turret moves while aiming at a target enemy*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Aim turret speed", desc = "Speed the turret turns at when actively aiming toward a target")
	public double aimTurretSpeed = 0.03;

	/** Type of shooting AI to use*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Aiming behavior", desc = "Behavior for aiming and firing at targets------Wander: randomly rotate and shoot if target enemy falls in the trajectory---" +
			"Straight: shoot directly at the target, if in line of sight---Reflect: use obstacles to calculate bounces---Alternate: switch between straight and reflect with every shot")
	public ShootAI shootAIType;

	/** When set to true, will calculate target enemy velocity when shooting. Only effective when shootAIType is straight!*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Predictive", desc = "When enabled, will use the current velocity of the target to predict and fire towards its future position------Only works with straight aiming behavior!")
	public boolean enablePredictiveFiring = true;
	/** When set to true, will shoot at bullets aiming towards the tank*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Deflect bullets", desc = "When enabled, will shoot at incoming bullet threats to deflect them------Does not work with wander aiming behavior!")
	public boolean enableDefensiveFiring = false;
	/** Will look through destructible walls when set to true for bullet shooting, recommended for explosive bullets*/
	@TankPropertyAnnotation(category = firingBehavior, name = "Through walls", desc = "When enabled, will shoot at destructible blocks if the target is hiding behind them. This is useful for tanks with explosive bullets.")
	public boolean ignoreDestructible = false;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;

	public String shotSound = null;


	// The following are values which are internally used for carrying out behavior.
	// These values change constantly during the course of the game.

	/** Used for tanks which do not use the straight AI, when detecting the target enemy with a ray. Tells the tank to aim towards the found target angle.*/
	protected boolean aim = false;

	/** True for when a tank just laid a mine*/
	protected boolean laidMine = false;

	/** Alternates for tanks with the alternate AI. Tells tanks to shoot with reflection and then to shoot straight.*/
	protected boolean straightShoot = false;

	/** If a direct line of sight to the target enemy exists, set to true*/
	protected boolean seesTargetEnemy = false;

	/** Age in frames*/
	protected double age = 0;

	/** Stores distances to obstacles or tanks in 8 directions*/
	protected double[] distances = new double[8];

	/** Stores distances to obstacles or tanks in 32 directions*/
	protected double[] mineFleeDistances = new double[32];

	/** Time in which the tank will follow its initial flee path from a mine*/
	protected double mineFleeTimer = 0;

	/** Used only in non-straight AI tanks. When detecting the target enemy, set to the angle necessary to hit them. This angle is added to random offsets to search for the target enemy moving.*/
	protected double lockedAngle = 0;

	/** Used only in non-straight AI tanks. Angle at which the tank is searching with its aim ray for the target enemy*/
	protected double searchAngle = 0;

	/** Angle at which the tank aims after having found its target (if non-straight AI, found with a ray, otherwise just the angle to the tank)*/
	protected double aimAngle = 0;

	/** Direction in which the tank moves when idle*/
	protected double direction;

	/** When enabled, the current motion direction will be kept until the tank decides to change direction*/
	protected boolean overrideDirection = false;

	/** Direction in which the tank moves to avoid a bullet that will hit it*/
	protected double avoidDirection = 0;

	/** Time until the tank will change its idle turret's direction*/
	protected double idleTimer;

	/** Time between shooting bullets*/
	protected double cooldown = 200;

	/** Time until the next mine will be laid*/
	protected double mineTimer = -1;

	/** Time which the tank will aim at its lockedAngle until giving up and continuing to search*/
	protected double aimTimer = 0;

	/** Time the tank will continue to avoid a bullet*/
	protected double avoidTimer = 0;

	/** Nearest bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBullet;

	/** Disable offset to shoot a bullet*/
	protected boolean disableOffset = false;

	/** Direction added to the bullet's direction to flee a bullet, possibly mirrored*/
	protected double fleeDirection = Math.PI / 4;

	/** Phase the tank is searching in, not used for straight AI*/
	protected RotationPhase searchPhase = RotationPhase.clockwise;

	/** Phase the tank turret is idling in, not used for straight AI*/
	protected RotationPhase idlePhase = RotationPhase.clockwise;

	/** Time until the tank will continue motion*/
	protected double motionPauseTimer = 0;

	/** Changes when the tank's visibility state changes, indicating whether the tank is visible on screen*/
	public boolean currentlyVisible = true;

	/** Time this tank has been invisible for*/
	public double timeInvisible = 0;

	/** Normally the nearest tank not on this tank's team. This is the tank that this tank will fight.*/
	protected Movable targetEnemy;

	/** True if can find an enemy*/
	protected boolean hasTarget = true;

	/** Direction to strafe around target enemy, if set to strafe mode on sight*/
	protected double strafeDirection = Math.PI / 2;

	/** True while the tank is actively seeking out an enemy*/
	protected boolean currentlySeeking = false;

	/** Set to a value to temporarily pause the tank from seeking*/
	protected double seekPause = 0;

	/** Upon reaching zero, the current target path is abandoned*/
	protected double seekTimer = 0;

	/** Describes the path the tank is currently following*/
	protected ArrayList<Tile> path;

	/* Accelerations */
	protected double aX;
	protected double aY;

	/** The random number generator the tank uses to make decisions*/
	protected Random random;

	public TankAIControlled(String name, double x, double y, double size, double r, double g, double b, double angle, ShootAI ai)
	{
		super(name, x, y, size, r, g, b);

		this.random = new Random(Level.random.nextLong());
		this.direction = ((int)(this.random.nextDouble() * 8)) / 2.0;
		this.idleTimer = (this.random.nextDouble() * turretIdleTimerRandom) + turretIdleTimerBase;

		if (this.random.nextDouble() < 0.5)
			this.idlePhase = RotationPhase.counterClockwise;

		this.angle = angle;
		this.orientation = angle;

		this.liveBulletMax = 5;
		this.bullet.unlimitedStack = true;
		this.mine.unlimitedStack = true;

		this.shootAIType = ai;
	}

	public void updateVisibility()
	{
		if (this.invisible)
		{
			if (this.currentlyVisible)
			{
				this.currentlyVisible = false;
				Drawing.drawing.playGlobalSound("transform.ogg", 1.2f);
				Game.eventsOut.add(new EventTankUpdateVisibility(this.networkID, false));

				if (Game.effectsEnabled)
				{
					for (int i = 0; i < 50 * Game.effectMultiplier; i++)
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

			this.timeInvisible += Panel.frameFrequency;
		}
		else
			this.timeInvisible = 0;
	}

	@Override
	public void update()
	{
		this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);

		this.age += Panel.frameFrequency;

		this.vX *= Math.pow(1 - (0.05 * this.frictionModifier), Panel.frameFrequency);
		this.vY *= Math.pow(1 - (0.05 * this.frictionModifier), Panel.frameFrequency);

		if (!this.destroy)
		{
			if (this.shootAIType != ShootAI.wander)
				this.updateTarget();

			if (this.enableMovement)
				this.updateMotionAI();
			else
			{
				this.vX *= Math.pow(1 - (0.15 * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - (0.15 * this.frictionModifier), Panel.frameFrequency);

				if (this.enableDefensiveFiring)
					this.checkForBulletThreats();
			}

			if (!ScreenGame.finished)
			{
				this.updateTurretAI();

				this.updateMineAI();
			}

			this.postUpdate();
		}

		this.vX += this.aX * maxSpeed * Panel.frameFrequency * this.accelerationModifier;
		this.vY += this.aY * maxSpeed * Panel.frameFrequency * this.accelerationModifier;

		double currentSpeed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

		if (currentSpeed > maxSpeed * maxSpeedModifier)
			this.setPolarMotion(this.getPolarDirection(), maxSpeed * maxSpeedModifier);

		this.updateVisibility();
		super.update();
	}

	/** Prepare to fire a bullet*/
	public void shoot()
	{
		this.aimTimer = 10;
		this.aim = false;

		if (this.cooldown <= 0 && this.liveBullets < this.liveBulletMax && !this.disabled && !this.destroy)
		{
			double an = this.angle;

			if (this.targetEnemy != null && this.enablePredictiveFiring && this.shootAIType == ShootAI.straight)
				an = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

			Ray a2 = new Ray(this.posX, this.posY, an, this.bullet.bounces, this);
			a2.size = this.bullet.size;
			a2.getTarget();
			a2.ignoreDestructible = this.ignoreDestructible;

			double dist = a2.age;
			// Cancels if the bullet will hit another enemy
			double offset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((dist / 100.0), 2);

			if (this.disableOffset)
			{
				offset = 0;
				this.disableOffset = false;
			}

			Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bullet.bounces, this, 2.5);
			a.size = this.bullet.size;
			a.moveOut(this.size / 2.5);

			Movable m = a.getTarget();

			if (!Team.isAllied(this, m))
			{
				this.bullet.use(this);
			}
		}
	}

	/** Actually fire a bullet*/
	public void fireBullet(Bullet b, double speed, double offset)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg", (float) (Bullet.bullet_size / this.bullet.size));

		if (this.shotSound != null)
			Drawing.drawing.playGlobalSound(this.shotSound, (float) (Bullet.bullet_size / this.bullet.size));

		b.setPolarMotion(angle + offset, this.bullet.speed);
		b.moveOut(50 / this.bullet.speed * this.size / Game.tile_size);

		Game.movables.add(b);
		Game.eventsOut.add(new EventShootBullet(b));

		this.cooldown = this.random.nextDouble() * this.cooldownRandom + this.cooldownBase;

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}

	public void updateTarget()
	{
		double nearestDist = Double.MAX_VALUE;
		Movable nearest = null;
		this.hasTarget = false;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank && !Team.isAllied(this, m) && !((Tank) m).hidden && ((Tank) m).targetable)
			{
				double dist = Movable.distanceBetween(this, m);
				if (dist < nearestDist)
				{
					this.hasTarget = true;
					nearestDist = dist;
					nearest = m;
				}
			}
		}

		this.targetEnemy = nearest;
	}

	public void updateMotionAI()
	{
		if (this.enableBulletAvoidance)
			this.checkForBulletThreats();

		if (this.avoidTimer > 0)
		{
			this.avoidTimer -= Panel.frameFrequency;
			this.setPolarAcceleration(avoidDirection, acceleration * 2);
			this.overrideDirection = true;
		}
		else
		{
			fleeDirection = -fleeDirection;

			if (this.targetEnemy != null && this.seesTargetEnemy && this.enableTargetEnemyReaction)
			{
				if (this.currentlySeeking)
				{
					this.seekTimer -= Panel.frameFrequency;
					this.followPath();

					if (this.seekTimer <= 0)
						this.currentlySeeking = false;
				}
				else
					this.reactToTargetEnemySight();
			}
			else if (currentlySeeking && seekPause <= 0)
				this.followPath();
			else
				this.updateIdleMotion();
		}

	}

	public void reactToTargetEnemySight()
	{
		if (this.targetEnemy == null)
			return;

		this.overrideDirection = true;

		if (this.stopSeekingOnSight)
			this.currentlySeeking = false;

		if (this.targetEnemySightBehavior == TargetEnemySightBehavior.approach)
			this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.flee)
			this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.strafe)
		{
			if (this.random.nextDouble() < this.strafeDirectionChangeChance * Panel.frameFrequency)
				strafeDirection = -strafeDirection;

			this.setAccelerationInDirectionWithOffset(this.targetEnemy.posX, this.targetEnemy.posY, this.acceleration * 2, strafeDirection);
		}
	}

	public void updateIdleMotion()
	{
		if (this.random.nextDouble() < this.motionChangeChance * Panel.frameFrequency || this.hasCollided)
		{
			this.overrideDirection = false;

			double prevDirection = this.direction;

			ArrayList<Double> directions = new ArrayList<>();

			for (double dir = 0; dir < 4; dir += 0.5)
			{
				Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
				r.size = Game.tile_size * this.hitboxSize - 1;
				double dist = r.getDist() / Game.tile_size;

				distances[(int) (dir * 2)] = dist;

				if (!(dir == (this.direction + 2) % 4 || dir == (this.direction + 1.5) % 4 || dir == (this.direction + 2.5) % 4))
				{
					if (dist >= 4)
						directions.add(dir);
				}
			}

			int chosenDir = (int)(this.random.nextDouble() * directions.size());

			if (directions.size() == 0)
				this.direction = (this.direction + 2) % 4;
			else
				this.direction = directions.get(chosenDir);


			if (this.direction != prevDirection)
				this.motionPauseTimer = this.directionChangeCooldown;

			if (this.canHide)
				this.motionPauseTimer += this.hideAmount * (this.random.nextDouble() + 1);
		}

		if (this.motionPauseTimer > 0)
		{
			this.aX = 0;
			this.aY = 0;
			this.motionPauseTimer = (Math.max(0, this.motionPauseTimer - Panel.frameFrequency));
		}
		else
		{
			if (!this.overrideDirection)
			{
				this.setPolarAcceleration(this.direction / 2 * Math.PI, acceleration);
				this.addIdleMotionOffset();
			}
		}

		if (!this.currentlySeeking && this.enablePathfinding && this.random.nextDouble() < this.seekChance * Panel.frameFrequency && this.posX > 0 && this.posX < Game.currentSizeX * Game.tile_size && this.posY > 0 && this.posY < Game.currentSizeY * Game.tile_size)
		{
			Tile[][] tiles = new Tile[Game.currentSizeX][Game.currentSizeY];

			for (int i = 0; i < tiles.length; i++)
			{
				for (int j = 0; j < tiles[i].length; j++)
				{
					tiles[i][j] = new Tile(this.random, i, j);
				}
			}

			for (Obstacle o: Game.obstacles)
			{
				if (o.posX >= 0 && o.posY >= 0 && o.posX <= Game.currentSizeX * Game.tile_size && o.posY <= Game.currentSizeY * Game.tile_size)
				{
					Tile.Type t = Tile.Type.solid;

					if (!o.tankCollision && !(o instanceof ObstacleTeleporter))
						t = Tile.Type.empty;
					else if (o.destructible && this.enableMineLaying)
						t = Tile.Type.destructible;

					int x = (int) (o.posX / Game.tile_size);
					int y = (int) (o.posY / Game.tile_size);
					Tile tile = tiles[x][y];
					tile.type = t;
					tile.unfavorability = Math.min(tile.unfavorability, 10);

					for (int i = -1; i <= 1; i++)
					{
						for (int j = -1; j <= 1; j++)
						{
							if (x + i > 0 && x + i < tiles.length && y + j > 0 && y + j < tiles[0].length)
								tiles[x + i][y + j].unfavorability = Math.max(tile.unfavorability, 1);
						}
					}
				}
			}

			for (Movable m: Game.movables)
			{
				if (this.isInterestingPathTarget(m))
					tiles[Math.min(Game.currentSizeX - 1, Math.max(0, (int) (m.posX / Game.tile_size)))][Math.min(Game.currentSizeY - 1, Math.max(0, (int) (m.posY / Game.tile_size)))].interesting = true;
			}

			ArrayList<Tile> queue = new ArrayList<>();

			Tile t = tiles[(int)(this.posX / Game.tile_size)][(int)(this.posY / Game.tile_size)];
			t.explored = true;
			queue.add(t);

			Tile current = null;
			boolean found = false;

			while (!queue.isEmpty())
			{
				current = queue.remove(0);

				if (current.search(queue, tiles))
				{
					found = true;
					break;
				}
			}

			if (found)
			{
				this.seekTimer = this.seekTimerBase;
				this.currentlySeeking = true;
				this.path = new ArrayList<>();

				while (current.parent != null)
				{
					this.path.add(0, current);
					current = current.parent;
				}
			}
		}

		this.seekPause = Math.max(0, this.seekPause - Panel.frameFrequency);
	}

	public void followPath()
	{
		this.seekTimer -= Panel.frameFrequency;

		/*for (Tile t: this.path)
		{
			Game.effects.add(Effect.createNewEffect(t.posX, t.posY, 25, Effect.EffectType.laser));
		}*/

		if (this.path.isEmpty())
		{
			currentlySeeking = false;
			return;
		}

		Tile t = this.path.get(0);

		//double frac = Math.max(Math.min(1, (seekTimerBase - seekTimer) / seekTurnBase), 0);

		//double pvX = this.vX;
		//double pvY = this.vY;

		this.setAccelerationInDirection(t.shiftedX, t.shiftedY, this.acceleration);
		//this.vX = this.vX * frac + pvX * (1 - frac);
		//this.vY = this.vY * frac + pvY * (1 - frac);

		double mul = 1;

		if (this.path.size() > 0 && this.path.get(0).type == Tile.Type.destructible)
			mul = 3;
		else if (this.path.size() > 1 && this.path.get(1).type == Tile.Type.destructible)
			mul = 2;

		if (Math.pow(t.shiftedX - this.posX, 2) + Math.pow(t.shiftedY - this.posY, 2) <= Math.pow(Game.tile_size / 2 * mul, 2))
		{
			this.seekTimer = this.seekTimerBase;

			if (this.path.get(0).type == Tile.Type.destructible)
			{
				this.mine.use(this);
				this.seekTimer = this.seekTimerBase * 2;
				this.seekPause = this.mine.timer;
			}

			this.path.remove(0);
		}

		if (this.seekTimer < 0)
			this.currentlySeeking = false;
	}

	public void addIdleMotionOffset()
	{
		double offsetMotion = Math.sin(this.age * 0.02);
		if (offsetMotion < 0)
		{
			double dist = this.distances[(int) (this.direction * 2 + 6) % 8];
			offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;
		}
		else
		{
			double dist = this.distances[(int) (this.direction * 2 + 2) % 8];
			offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;
		}

		this.addPolarAcceleration((this.direction + 1) / 2 * Math.PI, offsetMotion);
	}

	public void checkForBulletThreats()
	{
		boolean avoid = false;

		ArrayList<Bullet> toAvoid = new ArrayList<>();
		ArrayList<Ray> toAvoidTargets = new ArrayList<>();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Bullet && !Game.movables.get(i).destroy)
			{
				Bullet b = (Bullet) Game.movables.get(i);
				if (!(b.tank == this && b.age < 20) && !(this.team != null && Team.isAllied(b, this) && !this.team.friendlyFire) && b.shouldDodge && Math.abs(b.posX - this.posX) < Game.tile_size * 10 && Math.abs(b.posY - this.posY) < Game.tile_size * 10 && b.getMotionInDirection(b.getAngleInDirection(this.posX, this.posY)) > 0)
				{
					Ray r = b.getRay();
					r.tankHitSizeMul = 4;

					Movable m = r.getTarget(3, this);
					if (m != null)
					{
						if (m.equals(this))
						{
							avoid = true;
							toAvoid.add(b);
							toAvoidTargets.add(r);
						}
					}
				}
			}
		}

		if (avoid)
		{
			Bullet nearest = null;
			double nearestDist = Double.MAX_VALUE;
			int nearestIndex = -1;
			for (int i = 0; i < toAvoid.size(); i++)
			{
				double dist = Movable.distanceBetween(this, toAvoid.get(i));
				if (dist < nearestDist)
				{
					nearest = toAvoid.get(i);
					nearestDist = dist;
					nearestIndex = i;
				}
			}

			double targetX = toAvoidTargets.get(nearestIndex).targetX;
			double targetY = toAvoidTargets.get(nearestIndex).targetY;

			this.avoidTimer = this.avoidTimerBase;
			this.avoidDirection = this.getAngleInDirection(targetX, targetY) + Math.PI;//nearest.getPolarDirection() + fleeDirection;
			double direction = nearest.getPolarDirection();
			double diff = Movable.angleBetween(this.avoidDirection, direction);

			if (Math.abs(diff) < Math.PI / 4)
				this.avoidDirection = direction + Math.signum(diff) * Math.PI / 4;

			Ray r = new Ray(this.posX, this.posY, this.avoidDirection, 0, this, Game.tile_size);
			r.size = Game.tile_size * this.hitboxSize - 1;
			double d = r.getDist();

			if (d < Game.tile_size * 2)
				this.avoidDirection = direction - diff;

			this.nearestBullet = nearest;
		}
	}

	public void updateTurretAI()
	{
		if (this.enableLookingAtTargetEnemy)
			this.lookAtTargetEnemy();

		if (this.shootAIType.equals(ShootAI.wander))
			this.updateTurretWander();
		else if (this.shootAIType.equals(ShootAI.straight))
			this.updateTurretStraight();
		else
			this.updateTurretReflect();

		this.cooldown -= Panel.frameFrequency;
	}

	public void updateTurretWander()
	{
		Ray a = new Ray(this.posX, this.posY, this.angle, this.bullet.bounces, this);
		a.moveOut(this.size / 10);
		a.size = this.bullet.size;
		a.ignoreDestructible = this.ignoreDestructible;

		Movable m = a.getTarget();

		if (!(m == null))
			if (!Team.isAllied(m, this) && m instanceof Tank && !((Tank) m).hidden)
				this.shoot();

		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.idleTurretSpeed * Panel.frameFrequency;
		else
			this.angle -= this.idleTurretSpeed * Panel.frameFrequency;

		this.idleTimer -= Panel.frameFrequency;

		if (idleTimer <= 0)
		{
			this.idleTimer = this.random.nextDouble() * turretIdleTimerRandom + turretIdleTimerBase;
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counterClockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public void updateTurretStraight()
	{
		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy && !this.nearestBullet.heavy && this.nearestBullet.canBeCanceled)
		{
			double a = this.nearestBullet.getAngleInDirection(this.posX + 50 / this.bullet.speed * this.nearestBullet.vX, this.posY + 50 / this.bullet.speed * this.nearestBullet.vY);
			double speed = this.nearestBullet.getLastMotionInDirection(a + Math.PI / 2);

			if (speed < this.bullet.speed)
			{
				double d = this.getAngleInDirection(nearestBullet.posX, nearestBullet.posY) - Math.asin(speed / this.bullet.speed);

				if (!Double.isNaN(d))
					this.aimAngle = d;
			}

			this.disableOffset = true;
		}
		else
		{
			if (this.hasTarget && this.targetEnemy != null)
			{
				this.setAimAngleStraight();
			}
		}

		this.checkAndShoot();

		double speed = this.aimTurretSpeed;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.aimThreshold)
		{
			if (Movable.angleBetween(this.angle, this.aimAngle) < 0)
				this.angle += speed * Panel.frameFrequency;
			else
				this.angle -= speed * Panel.frameFrequency;

			this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		}
		else
			this.angle = this.aimAngle;

		if (this.seesTargetEnemy && this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) < Game.tile_size * 6)
			this.cooldown -= Panel.frameFrequency;
	}

	public void setAimAngleStraight()
	{
		if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
			r.ignoreDestructible = this.ignoreDestructible;
			r.size = Game.tile_size * this.hitboxSize - 1;
			r.enableBounciness = false;
			this.disableOffset = false;

			double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
			double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);

			double distBtwn = Movable.distanceBetween(this, this.targetEnemy);
			double time = distBtwn / Math.sqrt(this.bullet.speed * this.bullet.speed - speed * speed);

			double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

			double d = r.getDist();

			if (d * d > distSq && speed < this.bullet.speed)
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / this.bullet.speed);
			else
				this.aimAngle = this.getAngleInDirection(r.posX, r.posY);
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.disableOffset = false;
		}
	}

	public void checkAndShoot()
	{
		Movable m = null;

		if (this.targetEnemy != null)
		{
			Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);
			r.moveOut(this.size / 10);
			r.size = this.bullet.size;
			r.ignoreDestructible = this.ignoreDestructible;
			m = r.getTarget();
		}

		if ((m != null && m.equals(this.targetEnemy) || (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy && !this.nearestBullet.heavy && this.nearestBullet.canBeCanceled) && Movable.absoluteAngleBetween(this.angle, this.aimAngle) <= this.aimThreshold))
			this.shoot();
	}

	public void updateTurretReflect()
	{
		if (this.seesTargetEnemy && this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) <= Game.tile_size * 6)
		{
			aim = true;
			this.aimAngle = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);
			this.cooldown -= Panel.frameFrequency;
		}

		this.search();

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy && !this.nearestBullet.heavy && this.nearestBullet.canBeCanceled)
		{
			double a = this.nearestBullet.getAngleInDirection(this.posX + 50 / this.bullet.speed * this.nearestBullet.vX, this.posY + 50 / this.bullet.speed * this.nearestBullet.vY);
			double speed = this.nearestBullet.getLastMotionInDirection(a + Math.PI / 2);

			if (speed < this.bullet.speed)
			{
				double d = this.getAngleInDirection(nearestBullet.posX, nearestBullet.posY) - Math.asin(speed / this.bullet.speed);

				if (!Double.isNaN(d))
				{
					this.aimAngle = d;
					this.aim = true;
				}
			}

			this.disableOffset = true;
		}

		if (aim && (this.hasTarget || (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy && !this.nearestBullet.heavy && this.nearestBullet.canBeCanceled)))
			this.updateAimingTurret();
		else if (currentlySeeking && this.seekPause <= 0)
			this.updateSeekingTurret();
		else
			this.updateIdleTurret();
	}

	public void search()
	{
		if (this.straightShoot)
		{
			this.searchAngle = this.aimAngle;
		}
		else if (this.searchPhase == RotationPhase.clockwise)
		{
			searchAngle += this.random.nextDouble() * 0.1 * Panel.frameFrequency;
		}
		else if (this.searchPhase == RotationPhase.counterClockwise)
		{
			searchAngle -= this.random.nextDouble() * 0.1 * Panel.frameFrequency;
		}
		else
		{
			searchAngle = this.lockedAngle + this.random.nextDouble() * this.searchRange - this.searchRange / 2;
			this.aimTimer -= Panel.frameFrequency;
			if (this.aimTimer <= 0)
			{
				this.aimTimer = 0;
				if (this.random.nextDouble() < 0.5)
					this.searchPhase = RotationPhase.clockwise;
				else
					this.searchPhase = RotationPhase.counterClockwise;
			}
		}

		Ray ray = new Ray(this.posX, this.posY, this.searchAngle, this.bullet.bounces, this);
		ray.moveOut(this.size / 10);
		ray.size = this.bullet.size;
		ray.ignoreDestructible = this.ignoreDestructible;

		Movable target = ray.getTarget();

		if (target != null && !(target instanceof TankNPC))
		{
			if (target.equals(this.targetEnemy))
			{
				this.lockedAngle = this.angle;
				this.searchPhase = RotationPhase.aiming;
				this.aim = true;
				this.aimAngle = this.searchAngle % (Math.PI * 2);
			}
			else if (target instanceof Tank && !((Tank) target).hidden && !Team.isAllied(target, this))
			{
				this.targetEnemy = target;
				this.lockedAngle = this.angle;
				this.searchPhase = RotationPhase.aiming;
				this.aim = true;
				this.aimAngle = this.searchAngle % (Math.PI * 2);
			}
		}
	}

	public void lookAtTargetEnemy()
	{
		if (!this.hasTarget || this.targetEnemy == null)
			return;

		double a;

		a = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

		Ray rayToTarget = new Ray(this.posX, this.posY, a, 0, this);
		rayToTarget.size = this.bullet.size;
		rayToTarget.moveOut(this.size / 10);
		rayToTarget.ignoreDestructible = this.ignoreDestructible;
		Movable target = rayToTarget.getTarget();

		if (target != null)
		{
			this.seesTargetEnemy = target.equals(this.targetEnemy);
		}
		else
			this.seesTargetEnemy = false;

		if (this.straightShoot)
		{
			if (target != null)
			{
				if (target.equals(this.targetEnemy))
				{
					this.aimAngle = a;
				}
				else
				{
					this.straightShoot = false;
				}
			}
			else
			{
				this.straightShoot = false;
			}
		}
	}

	public void updateAimingTurret()
	{
		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold)
		{
			this.angle = this.aimAngle;
			this.shoot();
		}
		else
		{
			double speed = this.aimTurretSpeed;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			//System.out.println(Movable.absoluteAngleBetween(this.aimAngle, this.angle) + " " + this.angle + " " + this.aimAngle);
			if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.aimThreshold)
			{
				if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
					this.angle += speed * Panel.frameFrequency;
				else
					this.angle -= speed * Panel.frameFrequency;

				this.angle = this.angle % (Math.PI * 2);
			}
			else
				this.angle = this.aimAngle;

			this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		}
	}

	public void updateIdleTurret()
	{
		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.idleTurretSpeed * Panel.frameFrequency;
		else
			this.angle -= this.idleTurretSpeed * Panel.frameFrequency;

		this.idleTimer -= Panel.frameFrequency;

		if (this.idleTimer <= 0)
		{
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counterClockwise;
			else
				this.idlePhase = RotationPhase.clockwise;

			this.idleTimer = (this.random.nextDouble() * this.turretIdleTimerRandom) + this.turretIdleTimerBase;
		}
	}

	public void updateSeekingTurret()
	{
		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.idleTurretSpeed * Panel.frameFrequency;
		else
			this.angle -= this.idleTurretSpeed * Panel.frameFrequency;

		double dir = this.getPolarDirection();
		if (Movable.absoluteAngleBetween(dir, this.angle) > Math.PI / 8)
		{
			if (Movable.angleBetween(dir, this.angle) < 0)
				this.idlePhase = RotationPhase.counterClockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public boolean isInterestingPathTarget(Movable m)
	{
		return m instanceof Tank && !Team.isAllied(m, this)
				&& m.posX >= 0 && m.posX / Game.tile_size < Game.currentSizeX
				&& m.posY >= 0 && m.posY / Game.tile_size < Game.currentSizeY;
	}

	public void updateMineAI()
	{
		double nearestX = Double.MAX_VALUE;
		double nearestY = Double.MAX_VALUE;
		double nearestTimer = Double.MAX_VALUE;

		if (this.mineTimer == -1)
			this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		Movable nearest = null;

		if (!laidMine && mineFleeTimer <= 0)
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m instanceof Mine && !(this.team != null && Team.isAllied(this, m) && !this.team.friendlyFire))
				{
					if (Math.pow(m.posX - this.posX, 2) + Math.pow(m.posY - this.posY, 2) <= Math.pow(((Mine)m).radius * this.avoidSensitivity, 2))
					{
						if (nearestX + nearestY > this.posX - m.posX + this.posY - m.posY)
						{
							nearestX = this.posX - m.posX;
							nearestY = this.posY - m.posY;
						}

						if (nearestTimer > ((Mine)m).timer)
						{
							nearestTimer = ((Mine)m).timer;
							nearest = m;
						}
					}
				}
			}

		if (this.mineFleeTimer > 0)
			this.mineFleeTimer = Math.max(0, this.mineFleeTimer - Panel.frameFrequency);

		laidMine = false;

		if (nearest != null)
		{
			if (this.enableMineAvoidance && this.enableMovement)
			{
				this.setAccelerationAwayFromDirection(nearest.posX, nearest.posY, acceleration);
				this.overrideDirection = true;
			}
		}
		else
		{

			if (this.mineTimer <= 0 && this.enableMineLaying && !this.disabled)
			{
				boolean layMine = true;
				int i = 0;
				while (i < Game.movables.size())
				{
					Movable m = Game.movables.get(i);
					if (m instanceof Tank && Team.isAllied(this, m) && m != this)
					{
						Tank t = (Tank) m;
						if (Math.pow(t.posX - this.posX, 2) + Math.pow(t.posY - this.posY, 2) <= Math.pow(200, 2))
						{
							layMine = false;
							break;
						}

					}
					i++;
				}

				if (layMine)
				{
					this.mine.use(this);
				}
			}

			if (!this.currentlySeeking)
				this.mineTimer = Math.max(0, this.mineTimer - Panel.frameFrequency);
		}

		if (Math.abs(nearestX) + Math.abs(nearestY) <= 1 && this.mineFleeTimer <= 0)
		{
			this.overrideDirection = true;
			this.setPolarAcceleration(this.random.nextDouble() * 2 * Math.PI, acceleration);
		}
	}

	public void layMine(Mine m)
	{
		Drawing.drawing.playGlobalSound("lay_mine.ogg");

		Game.eventsOut.add(new EventLayMine(m));
		Game.movables.add(m);
		this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		int count = mineFleeDistances.length;
		double[] d = mineFleeDistances;
		this.mineFleeTimer = 100;

		int k = 0;
		for (double dir = 0; dir < 4; dir += 4.0 / count)
		{
			Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
			r.size = Game.tile_size * this.hitboxSize - 1;

			double dist = r.getDist();

			d[k] = dist;
			k++;
		}

		int greatest = -1;
		double gValue = -1;
		for (int i = 0; i < d.length; i++)
		{
			if (d[i] > gValue)
			{
				gValue = d[i];
				greatest = i;
			}
		}

		//double angleV = this.getPolarDirection() + Math.PI + (this.random.nextDouble() - 0.5) * Math.PI / 2;
		this.overrideDirection = true;
		this.setPolarAcceleration(greatest * 2.0 / count * Math.PI, acceleration);
		laidMine = true;
	}

	/** Called after updating but before applying motion. Intended to be overridden.*/
	public void postUpdate()
	{

	}

	public static class Tile
	{
		public enum Type {empty, destructible, solid}
		public Tile parent;

		public double posX;
		public double posY;

		public double shiftedX;
		public double shiftedY;

		public int tileX;
		public int tileY;

		public Type type = Type.empty;
		public boolean explored = false;

		public boolean interesting = false;
		public int unfavorability = 0;

		public Tile(Random r, int x, int y)
		{
			this.shiftedX = (r.nextDouble() - 0.5) * Game.tile_size / 2;
			this.shiftedY = (r.nextDouble() - 0.5) * Game.tile_size / 2;

			this.posX = (x + 0.5) * Game.tile_size;
			this.posY = (y + 0.5) * Game.tile_size;

			this.shiftedX += this.posX;
			this.shiftedY += this.posY;

			this.tileX = x;
			this.tileY = y;
		}

		public boolean search(ArrayList<Tile> queue, Tile[][] map)
		{
			boolean freeLeft = this.tileX > 0;
			boolean freeTop = this.tileY > 0;
			boolean freeRight = this.tileX < map.length - 1;
			boolean freeBottom = this.tileY < map[0].length - 1;

			if (this.interesting)
				return true;

			if (this.unfavorability > 0)
			{
				queue.add(this);
				this.unfavorability--;
				return false;
			}

			boolean left = freeLeft && map[this.tileX - 1][this.tileY].type != Type.solid;
			boolean right = freeRight && map[this.tileX + 1][this.tileY].type != Type.solid;
			boolean top = freeTop && map[this.tileX][this.tileY - 1].type != Type.solid;
			boolean bottom = freeBottom && map[this.tileX][this.tileY + 1].type != Type.solid;

			if (freeLeft)
			{
				map[this.tileX - 1][this.tileY].explore(this, queue);

				if (freeTop && left && top)
					map[this.tileX - 1][this.tileY - 1].explore(this, queue);

				if (freeBottom && left && bottom)
					map[this.tileX - 1][this.tileY + 1].explore(this, queue);
			}

			if (freeTop)
				map[this.tileX][this.tileY - 1].explore(this, queue);

			if (freeBottom)
				map[this.tileX][this.tileY + 1].explore(this, queue);

			if (freeRight)
			{
				map[this.tileX + 1][this.tileY].explore(this, queue);

				if (freeTop && right && top)
					map[this.tileX + 1][this.tileY - 1].explore(this, queue);

				if (freeBottom && right && bottom)
					map[this.tileX + 1][this.tileY + 1].explore(this, queue);
			}

			return false;
		}

		public void explore(Tile parent, ArrayList<Tile> queue)
		{
			if (this.type != Type.solid && !this.explored)
			{
				this.parent = parent;
				queue.add(this);
			}

			this.explored = true;
		}
	}

	public void setPolarAcceleration(double angle, double acceleration)
	{
		double accX = acceleration * Math.cos(angle);
		double accY = acceleration * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	public void addPolarAcceleration(double angle, double acceleration)
	{
		double accX = acceleration * Math.cos(angle);
		double accY = acceleration * Math.sin(angle);
		this.aX += accX;
		this.aY += accY;
	}

	public void setAccelerationInDirection(double x, double y, double accel)
	{
		x -= this.posX;
		y -= this.posY;

		double angle = 0;
		if (x > 0)
			angle = Math.atan(y/x);
		else if (x < 0)
			angle = Math.atan(y/x) + Math.PI;
		else
		{
			if (y > 0)
				angle = Math.PI / 2;
			else if (y < 0)
				angle = Math.PI * 3 / 2;
		}
		double accX = accel * Math.cos(angle);
		double accY = accel * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	public void setAccelerationAwayFromDirection(double x, double y, double accel)
	{
		this.setAccelerationInDirectionWithOffset(x, y, accel, Math.PI);
	}

	public void setAccelerationInDirectionWithOffset(double x, double y, double accel, double a)
	{
		x -= this.posX;
		y -= this.posY;

		double angle = 0;
		if (x > 0)
			angle = Math.atan(y/x);
		else if (x < 0)
			angle = Math.atan(y/x) + Math.PI;
		else
		{
			if (y > 0)
				angle = Math.PI / 2;
			else if (y < 0)
				angle = Math.PI * 3 / 2;
		}
		angle += a;
		double accX = accel * Math.cos(angle);
		double accY = accel * Math.sin(angle);
		this.aX = accX;
		this.aY = accY;
	}

	@Override
	public void draw()
	{
		if (this.currentlyVisible || this.destroy)
			super.draw();
		else
		{
			if (this.size * 4 > this.timeInvisible * 2)
			{
				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size / 4, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2, true, false);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2);
			}
		}
	}

}

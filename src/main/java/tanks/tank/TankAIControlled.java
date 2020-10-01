package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTeleporter;

import java.util.ArrayList;

/** This class is the 'skeleton' tank class.
 *  It can be extended and values can be changed to easily produce an AI for another tank.
 *  Also, the behavior is split into many methods which are intended to be overridden easily.*/
public class TankAIControlled extends Tank
{
	/** Determines which type of AI the tank will use when shooting.
	 *  Straight means that the tank will shoot directly at the target enemy if the target enemy is in line of sight.
	 *  Reflect means that the tank will use a Ray with reflections to find possible ways to hit the target enemy.
	 *  Alternate means that the tank will switch between shooting straight at the target enemy and using the reflect AI with every shot.
	 *  Wander means that the tank will randomly rotate and shoot only if it detects the target enemy*/
	public enum ShootAI {wander, straight, alternate, reflect}

	/** The type which shows what direction the tank is moving. Clockwise and Counter Clockwise are for idle, while Aiming is for when the tank aims.*/
	protected enum RotationPhase {clockwise, counterClockwise, aiming}

	// The following are properties which are used externally to determine the behavior settings of the tank.
	// Simple modifications of tanks can just change these values to produce a desired behavior.
	// More complex behaviors may require overriding of methods.
	// These values do not change normally along the course of the game.

	public boolean enableMovement = true;
	/** When set to true, will call reactToTargetEnemySight() when an unobstructed line of sight to the target enemy can be made */
	public boolean enableTargetEnemyReaction = true;
	public boolean enableMineLaying = true;
	public boolean enableMineAvoidance = true;
	/** How close the tank needs to get to a mine to avoid it*/
	public double mineSensitivity = 1.5;
	public boolean enableBulletAvoidance = true;
	/** When set to true, will calculate target enemy velocity when shooting. Only effective when shootAIType is straight!*/
	public boolean enablePredictiveFiring = true;
	/** When set to true, will shoot at bullets aiming towards the tank*/
	public boolean enableDefensiveFiring = false;
	/** When set to true, will shoot a ray at the target enemy and enable reactions when the target enemy is in sight*/
	public boolean enableLookingAtTargetEnemy = true;

	public int bulletBounces = 1;
	public double bulletSize = Bullet.bullet_size;
	public double bulletDamage = 1;
	public double bulletSpeed = 25.0 / 8;
	public boolean bulletHeavy = false;
	public Bullet.BulletEffect bulletEffect = Bullet.BulletEffect.trail;

	public double mineFuseLength = 1000;

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	public double aimAccuracyOffset = 0.2;
	/** Threshold angle difference needed between angle and aimAngle to count as touching the target enemy*/
	public double aimThreshold = 0.05;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * Math.random()*/
	public double turretIdleTimerBase = 25;
	/** Random factor in calculating time to randomly change idle direction, multiplied by Math.random() and added to turretIdleTimerBase*/
	public double turretIdleTimerRandom = 500;

	/** Minimum time to lay a mine, added to mineTimerRandom * Math.random()*/
	public double mineTimerBase = 2000;
	/** Random factor in calculating time to lay a mine, multiplied by Math.random() and added to mineTimerBase*/
	public double mineTimerRandom = 4000;

	/** Minimum time in between shooting bullets, added to cooldownRandom * Math.random()*/
	public double cooldownBase = 60;
	/** Random factor in calculating time between shooting bullets, multiplied by Math.random() and added to cooldownBase*/
	public double cooldownRandom = 20;

	/** Time waited when changing direction of motion*/
	public double directionChangeCooldown = 15;

	/** Speed at which the turret moves while aiming at a target enemy*/
	public double aimTurretSpeed = 0.03;
	/** Speed at which the turret moves while idle*/
	public double idleTurretSpeed = 0.005;

	/** Chance per frame to change direction*/
	public double motionChangeChance = 0.01;

	/** Chance per frame to seek the target enemy*/
	public double seekChance = 0.001;

	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	public double avoidTimerBase = 30;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;

	/** Multiplier of time the tank will hide in a shrub*/
	public double hideAmount = 350;

	/** If enabled, the tank may actively seek out enemies*/
	public boolean enablePathfinding = false;

	/** Increasing this value increases how stubborn the tank is in following a path*/
	public double seekTimerBase = 200;

	/** Type of shooting AI to use*/
	public ShootAI shootAIType;


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
	protected int[] distances = new int[8];

	/** Stores distances to obstacles or tanks in 32 directions*/
	protected int[] mineFleeDistances = new int[32];

	/** Time in which the tank will follow its initial flee path from a mine*/
	protected double mineFleeTimer = 0;

	/** Used only in non-straight AI tanks. When detecting the target enemy, set to the angle necessary to hit them. This angle is added to random offsets to search for the target enemy moving.*/
	protected double lockedAngle = 0;

	/** Used only in non-straight AI tanks. Angle at which the tank is searching with its aim ray for the target enemy*/
	protected double searchAngle = 0;

	/** Angle at which the tank aims after having found its target (if non-straight AI, found with a ray, otherwise just the angle to the tank)*/
	protected double aimAngle = 0;

	/** Direction in which the tank moves when idle*/
	protected double direction = ((int)(Math.random() * 8))/2.0;

	/** When enabled, the current motion direction will be kept until the tank decides to change direction*/
	protected boolean overrideDirection = false;

	/** Direction in which the tank moves to avoid a bullet that will hit it*/
	protected double avoidDirection = 0;

	/** Time until the tank will change its idle turret's direction*/
	protected double idleTimer = (Math.random() * turretIdleTimerRandom) + turretIdleTimerBase;

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
	public boolean disableOffset = false;

	/** Direction added to the bullet's direction to flee a bullet, possibly mirrored*/
	protected double fleeDirection = Math.PI / 4;

	/** Phase the tank is searching in, not used for straight AI*/
	protected RotationPhase searchPhase = RotationPhase.clockwise;

	/** Phase the tank turret is idling in, not used for straight AI*/
	protected RotationPhase idlePhase = RotationPhase.clockwise;

	/** Time until the tank will continue motion*/
	protected double motionPauseTimer = 0;

	/** Normally the nearest tank not on this tank's team. This is the tank that this tank will fight.*/
	protected Movable targetEnemy;

	/** True if can find an enemy*/
	protected boolean hasTarget = true;

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

	public TankAIControlled(String name, double x, double y, double size, double r, double g, double b, double angle, ShootAI ai)
	{
		super(name, x, y, size, r, g, b);

		if (Math.random() < 0.5)
			this.idlePhase = RotationPhase.counterClockwise;

		this.angle = angle;
		this.orientation = angle;

		this.liveBulletMax = 5;

		this.shootAIType = ai;
	}

	@Override
	public void update()
	{
		this.angle = this.angle % (Math.PI * 2);

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

			if (this.enablePredictiveFiring && this.shootAIType == ShootAI.straight)
				an = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

			Ray a2 = new Ray(this.posX, this.posY, an, this.bulletBounces, this);
			a2.size = this.bulletSize;
			a2.getTarget();

			double dist = a2.age;
			// Cancels if the bullet will hit another enemy
			double offset = (Math.random() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((dist / 100.0), 2);

			if (this.disableOffset)
			{
				offset = 0;
				this.disableOffset = false;
			}

			Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bulletBounces, this, 2.5);
			a.size = this.bulletSize;
			a.moveOut(this.size / 2.5);

			Movable m = a.getTarget();

			if (!Team.isAllied(this, m))
			{
				this.launchBullet(offset);
			}
		}
	}

	/** Actually fire a bullet*/
	public void launchBullet(double offset)
	{
		Drawing.drawing.playGlobalSound("shoot.ogg", (float) (Bullet.bullet_size / this.bulletSize));

		Bullet b = new Bullet(this.posX, this.posY, this.bulletBounces, this);
		b.setPolarMotion(angle + offset, this.bulletSpeed);
		b.moveOut(50 / this.bulletSpeed * this.size / Game.tile_size);
		b.effect = this.bulletEffect;
		b.size = this.bulletSize;
		b.damage = this.bulletDamage;
		b.heavy = this.bulletHeavy;

		Game.movables.add(b);
		Game.eventsOut.add(new EventShootBullet(b));

		this.cooldown = Math.random() * this.cooldownRandom + this.cooldownBase;

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
		this.overrideDirection = true;
		this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
	}

	public void updateIdleMotion()
	{
		if (Math.random() < this.motionChangeChance * Panel.frameFrequency || this.hasCollided)
		{
			this.overrideDirection = false;

			double prevDirection = this.direction;

			ArrayList<Double> directions = new ArrayList<Double>();

			for (double dir = 0; dir < 4; dir += 0.5)
			{
				Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
				r.size = Game.tile_size * this.hitboxSize;
				int dist = r.getDist();

				distances[(int) (dir * 2)] = dist;

				if (!(dir == (this.direction + 2) % 4 || dir == (this.direction + 1.5) % 4 || dir == (this.direction + 2.5) % 4))
				{
					if (dist >= 4)
						directions.add(dir);
				}
			}

			int chosenDir = (int)(Math.random() * directions.size());

			if (directions.size() == 0)
				this.direction = (this.direction + 2) % 4;
			else
				this.direction = directions.get(chosenDir);


			if (this.direction != prevDirection)
				this.motionPauseTimer = this.directionChangeCooldown;

			if (this.canHide)
				this.motionPauseTimer += this.hideAmount * (Math.random() + 1);
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

		if (!this.currentlySeeking && this.enablePathfinding && Math.random() < this.seekChance * Panel.frameFrequency && this.posX > 0 && this.posX < Game.currentSizeX * Game.tile_size && this.posY > 0 && this.posY < Game.currentSizeY * Game.tile_size)
		{
			Tile[][] tiles = new Tile[Game.currentSizeX][Game.currentSizeY];

			for (int i = 0; i < tiles.length; i++)
			{
				for (int j = 0; j < tiles[i].length; j++)
				{
					tiles[i][j] = new Tile(i, j);
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
				tiles[Math.min(Game.currentSizeX - 1, Math.max(0, (int) (m.posX / Game.tile_size)))][Math.min(Game.currentSizeY - 1, Math.max(0, (int) (m.posY / Game.tile_size)))].interesting = true;
			}

			ArrayList<Tile> queue = new ArrayList<Tile>();

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
				this.path = new ArrayList<Tile>();

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
				this.layMine();
				this.seekTimer = this.seekTimerBase * 2;
				this.seekPause = this.mineFuseLength;
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
			int dist = this.distances[(int) (this.direction * 2 + 6) % 8];
			offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;
		}
		else
		{
			int dist = this.distances[(int) (this.direction * 2 + 2) % 8];
			offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;
		}

		this.addPolarAcceleration((this.direction + 1) / 2 * Math.PI, offsetMotion);
	}

	public void checkForBulletThreats()
	{
		boolean avoid = false;

		ArrayList<Bullet> toAvoid = new ArrayList<Bullet>();
		ArrayList<Ray> toAvoidTargets = new ArrayList<Ray>();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Bullet && !Game.movables.get(i).destroy)
			{
				Bullet b = (Bullet) Game.movables.get(i);
				if (!(b.tank == this && b.age < 20) && Math.abs(b.posX - this.posX) < Game.tile_size * 10 && Math.abs(b.posY - this.posY) < Game.tile_size * 10)
				{
					Ray r = b.getRay();

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
			r.size = Game.tile_size;
			int d = r.getDist();

			if (d < 2)
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
		Ray a = new Ray(this.posX, this.posY, this.angle, this.bulletBounces, this);
		a.moveOut(this.size / 10);
		a.size = this.bulletSize;

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
			this.idleTimer = Math.random() * turretIdleTimerRandom + turretIdleTimerBase;
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counterClockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public void updateTurretStraight()
	{
		if (!this.hasTarget)
			return;

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy)
		{
			double instant = (int) (25 / this.bulletSpeed * 2 * this.size / Game.tile_size);
			double s = this.bulletSpeed / Math.sqrt(this.nearestBullet.vX * this.nearestBullet.vX + this.nearestBullet.vY * this.nearestBullet.vY) / 2;
			this.aimAngle = this.getAngleInDirection(
					this.nearestBullet.posX + this.nearestBullet.vX * (Movable.distanceBetween(this, nearestBullet) - instant) / this.bulletSpeed * s,
					this.nearestBullet.posY + this.nearestBullet.vY * (Movable.distanceBetween(this, nearestBullet) - instant) / this.bulletSpeed * s);

			this.disableOffset = true;
		}
		else if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getPolarDirection(), 0, (Tank) targetEnemy);
			r.size = Game.tile_size;
			r.enableBounciness = false;

			this.disableOffset = false;
			double dist = Math.sqrt(Math.pow(targetEnemy.posX + targetEnemy.vX * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed - this.targetEnemy.posX, 2) +
					Math.pow(targetEnemy.posY + targetEnemy.vY * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed - this.targetEnemy.posY, 2));

			double d = r.getDist();

			if (d * 10 > dist)
			{
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX + targetEnemy.vX * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed, targetEnemy.posY + targetEnemy.vY * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed);
			}
			else
			{
				this.aimAngle = this.getAngleInDirection(r.posX, r.posY);
			}
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.disableOffset = false;
		}

		double a = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);

		Ray r = new Ray(this.posX, this.posY, a, 0, this);
		r.moveOut(this.size / 10);
		r.size = this.bulletSize;

		Movable m = r.getTarget();

		if (m != null)
			if (m.equals(this.targetEnemy))
				this.shoot();

		double speed = this.aimTurretSpeed;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
			speed /= 2;

		if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
			speed /= 2;


		if (Movable.absoluteAngleBetween(this.aimAngle, this.angle) > this.aimThreshold / 2)
		{
			if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
				this.angle += speed * Panel.frameFrequency;
			else
				this.angle -= speed * Panel.frameFrequency;

			this.angle = this.angle % (Math.PI * 2);
		}

		if (Math.abs(this.angle - this.aimAngle) < this.aimThreshold && !this.disableOffset)
			this.angle = this.aimAngle;
	}

	public void updateTurretReflect()
	{
		if (this.seesTargetEnemy && this.targetEnemy != null && Movable.distanceBetween(this, this.targetEnemy) < Game.tile_size * 2)
		{
			aim = true;
			this.aimAngle = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);
		}

		if (!this.straightShoot)
			this.search();

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy)
		{
			double instant = 50 / this.bulletSpeed * this.size / Game.tile_size;
			this.aim = true;
			double s = this.bulletSpeed / Math.sqrt(this.nearestBullet.vX * this.nearestBullet.vX + this.nearestBullet.vY * this.nearestBullet.vY) / 2;
			this.aimAngle = this.getAngleInDirection(
					this.nearestBullet.posX + this.nearestBullet.vX * (Movable.distanceBetween(this, nearestBullet) - instant) / this.bulletSpeed * s,
					this.nearestBullet.posY + this.nearestBullet.vY * (Movable.distanceBetween(this, nearestBullet) - instant) / this.bulletSpeed * s);
			this.disableOffset = true;
		}

		if (aim && this.hasTarget)
			this.updateAimingTurret();
		else if (currentlySeeking && this.seekPause <= 0)
			this.updateSeekingTurret();
		else
			this.updateIdleTurret();
	}

	public void search()
	{
		if (this.searchPhase == RotationPhase.clockwise)
		{
			searchAngle += Math.random() * 0.2 * Panel.frameFrequency;
		}
		else if (this.searchPhase == RotationPhase.counterClockwise)
		{
			searchAngle -= Math.random() * 0.2 * Panel.frameFrequency;
		}
		else
		{
			searchAngle = this.lockedAngle + Math.random() * this.searchRange - this.searchRange / 2;
			this.aimTimer -= Panel.frameFrequency;
			if (this.aimTimer <= 0)
			{
				this.aimTimer = 0;
				if (Math.random() < 0.5)
					this.searchPhase = RotationPhase.clockwise;
				else
					this.searchPhase = RotationPhase.counterClockwise;
			}
		}

		Ray ray = new Ray(this.posX, this.posY, this.searchAngle, this.bulletBounces, this);
		ray.moveOut(this.size / 10);
		ray.size = this.bulletSize;

		Movable target = ray.getTarget();

		if (target != null)
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
		if (!this.hasTarget)
			return;

		double a;

		if (this.enablePredictiveFiring)
			a = this.getAngleInDirection(this.targetEnemy.posX + this.targetEnemy.vX * Movable.distanceBetween(this, this.targetEnemy) / this.bulletSpeed, this.targetEnemy.posY + this.targetEnemy.vY * Movable.distanceBetween(this, this.targetEnemy) / this.bulletSpeed);
		else
			a = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

		Ray rayToTarget = new Ray(this.posX, this.posY, a, 0, this);
		rayToTarget.size = this.bulletSize;
		rayToTarget.moveOut(this.size / 10);
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

			if (Movable.angleBetween(this.angle, this.aimAngle) < 0)
				this.angle += speed * Panel.frameFrequency;
			else
				this.angle -= speed * Panel.frameFrequency;

			this.angle = this.angle % (Math.PI * 2);
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

			this.idleTimer = (Math.random() * this.turretIdleTimerRandom) + this.turretIdleTimerBase;
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
			this.mineTimer = (Math.random() * mineTimerRandom + mineTimerBase);

		Movable nearest = null;

		if (!laidMine && mineFleeTimer <= 0)
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m instanceof Mine)
				{
					if (Math.pow(m.posX - this.posX, 2) + Math.pow(m.posY - this.posY, 2) <= Math.pow(((Mine)m).radius * this.mineSensitivity, 2))
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
					this.layMine();
				}
			}

			if (!this.currentlySeeking)
				this.mineTimer = Math.max(0, this.mineTimer - Panel.frameFrequency);
		}

		if (Math.abs(nearestX) + Math.abs(nearestY) <= 1 && this.mineFleeTimer <= 0)
		{
			this.overrideDirection = true;
			this.setPolarAcceleration(Math.random() * 2 * Math.PI, acceleration);
		}
	}

	public void layMine()
	{
		Drawing.drawing.playGlobalSound("lay_mine.ogg");

		Game.movables.add(new Mine(this.posX, this.posY, this.mineFuseLength, this));
		this.mineTimer = (Math.random() * mineTimerRandom + mineTimerBase);

		int count = mineFleeDistances.length;
		int[] d = mineFleeDistances;
		this.mineFleeTimer = 100;

		int k = 0;
		for (double dir = 0; dir < 4; dir += 4.0 / count)
		{
			Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
			r.size = Game.tile_size;

			int dist = r.getDist();

			d[k] = dist;
			k++;
		}

		int greatest = -1;
		int gValue = -1;
		for (int i = 0; i < d.length; i++)
		{
			if (d[i] > gValue)
			{
				gValue = d[i];
				greatest = i;
			}
		}

		//double angleV = this.getPolarDirection() + Math.PI + (Math.random() - 0.5) * Math.PI / 2;
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

		public double shiftedX = (Math.random() - 0.5) * Game.tile_size / 2;
		public double shiftedY = (Math.random() - 0.5) * Game.tile_size / 2;

		public int tileX;
		public int tileY;

		public Type type = Type.empty;
		public boolean explored = false;

		public boolean interesting = false;
		public int unfavorability = 0;

		public Tile(int x, int y)
		{
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

}

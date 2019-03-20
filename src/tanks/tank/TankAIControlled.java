package tanks.tank;

import java.util.ArrayList;

import tanks.Bullet;
import tanks.Game;
import tanks.Mine;
import tanks.Movable;
import tanks.Panel;
import tanks.Ray;
import tanks.ScreenGame;
import tanks.Team;
import tanks.Drawing;

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
	public double bulletSpeed = 25.0 / 4;
	public boolean bulletHeavy = false;
	public Bullet.BulletEffect bulletEffect = Bullet.BulletEffect.trail;

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	public double aimAccuracyOffset = 0.2;
	/** Threshold angle difference needed between angle and aimAngle to count as touching the target enemy*/
	public double aimThreshold = 0.1;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * Math.random()*/
	public double turretIdleTimerBase = 25;
	/** Random factor in calculating time to randomly change idle direction, multiplied by Math.random() and added to turretIdleTimerBase*/
	public double turretIdleTimerRandom = 500;

	/** Minimum time to lay a mine, added to mineTimerRandom * Math.random()*/
	public double mineTimerBase = 1000;
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

	/** Speed at which the tank moves*/
	public double speed = 2.5;

	/** Chance per frame to change direction*/
	public double motionChangeChance = 0.01;

	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	public double avoidTimerBase = 30;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;

	/** Multiplier of time the tank will hide in a shrub*/
	public double hideAmount = 350;

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
	protected double fleeDirection = Math.PI / 2;

	/** Phase the tank is searching in, not used for straight AI*/
	protected RotationPhase searchPhase = RotationPhase.clockwise;

	/** Phase the tank turret is idling in, not used for straight AI*/
	protected RotationPhase idlePhase = RotationPhase.clockwise;

	/** Time until the tank will continue motion*/
	protected double motionPauseTimer = 0;

	/** Normally the nearest tank not on this tank's team. This is the tank that this tank will fight*/
	protected Movable targetEnemy;
	
	/** True if can find an enemy*/
	protected boolean hasTarget = true;
	
	public TankAIControlled(String name, double x, double y, int size, double r, double g, double b, double angle, ShootAI ai) 
	{
		super(name, x, y, size, r, g, b);

		if (Math.random() < 0.5)
			this.idlePhase = RotationPhase.counterClockwise;

		this.angle = angle;

		this.liveBulletMax = 5;

		this.shootAIType = ai;
	}

	@Override
	public void update()
	{
		this.angle = this.angle % (Math.PI * 2);

		this.age += Panel.frameFrequency;

		if (!this.destroy)
		{
			if (this.shootAIType != ShootAI.wander)
				this.updateTarget();

			if (this.enableMovement)
				this.updateMotionAI();
			else
			{
				this.vX *= 0.85;
				this.vY *= 0.85;
			}

			if (!ScreenGame.finished)
			{
				this.updateTurretAI();
				this.updateMineAI();
			}

			this.postUpdate();
		}

		super.update();
	}

	/** Prepare to fire a bullet*/
	@Override
	public void shoot() 
	{
		this.aimTimer = 10;
		this.aim = false;

		if (this.cooldown <= 0 && this.liveBullets < this.liveBulletMax && !this.disabled)
		{
			// Cancels if the bullet will hit another enemy
			double offset = Math.random() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2);

			if (this.disableOffset)
			{
				offset = 0;
				this.disableOffset = false;
			}

			Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.bulletBounces, this);
			a.moveOut(5);

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
		Drawing.drawing.playSound("resources/shoot.wav");

		Bullet b = new Bullet(this.posX, this.posY, this.bulletBounces, this);
		b.setPolarMotion(angle + offset, this.bulletSpeed);
		b.moveOut((int) (25 / this.bulletSpeed * 2 * this.size / Game.tank_size));
		b.effect = this.bulletEffect;
		b.size = this.bulletSize;
		b.damage = this.bulletDamage;
		b.heavy = this.bulletHeavy;
		Game.movables.add(b);
		this.cooldown = (int) (Math.random() * this.cooldownRandom + this.cooldownBase);

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}

	public void updateTarget()
	{
		double nearestDist = Double.MAX_VALUE;
		Movable nearest = this;
		this.hasTarget = false;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank && !Team.isAllied(this, m) && m.hiddenTimer <= 0 && !((Tank) m).invulnerable)
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
			this.setPolarMotion(avoidDirection, speed);
			this.overrideDirection = true;
		}
		else
		{
			fleeDirection = -fleeDirection;

			if (this.targetEnemy != null && this.seesTargetEnemy && this.enableTargetEnemyReaction)
			{
				this.reactToTargetEnemySight();
			}
			else
			{
				this.updateIdleMotion();
			}
		}

	}

	public void reactToTargetEnemySight()
	{
		this.overrideDirection = true;
		this.setMotionInDirection(targetEnemy.posX, targetEnemy.posY, speed);
	}

	public void updateIdleMotion()
	{
		if (Math.random() < this.motionChangeChance || this.hasCollided)
		{	
			this.overrideDirection = false;

			double prevDirection = this.direction;

			ArrayList<Double> directions = new ArrayList<Double>();

			for (double dir = 0; dir < 4; dir += 0.5)
			{
				Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tank_size);
				r.size = Game.tank_size;

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
			this.vX = 0;
			this.vY = 0;
			this.motionPauseTimer = (Math.max(0, this.motionPauseTimer - Panel.frameFrequency));	
		}
		else
		{
			if (!this.overrideDirection)
			{
				this.setPolarMotion(this.direction / 2 * Math.PI, speed);
				this.addIdleMotionOffset();
			}
		}
	}

	public void addIdleMotionOffset()
	{
		double offsetMotion = Math.sin(this.age * 0.02);
		if (offsetMotion < 0)
		{
			int dist = this.distances[(int) (this.direction * 2 + 6) % 8];
			offsetMotion *= Math.max(1, (dist - 1) / 5.0) * this.speed / 2.5;
		}
		else
		{
			int dist = this.distances[(int) (this.direction * 2 + 2) % 8];
			offsetMotion *= Math.max(1, (dist - 1) / 5.0) * this.speed / 2.5;
		}

		this.addPolarMotion((this.direction + 1) / 2 * Math.PI, offsetMotion);
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
				if (Math.abs(b.posX - this.posX) < Game.tank_size * 10 && Math.abs(b.posY - this.posY) < Game.tank_size * 10)
				{
					Ray r = b.getRay();

					Movable m = r.getTarget(2, 2, this);
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
		a.moveOut(5);

		Movable m = a.getTarget();

		if (!(m == null))
			if (!Team.isAllied(m, this) && m instanceof Tank && m.hiddenTimer <= 0)
				this.shoot();

		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.idleTurretSpeed * Panel.frameFrequency;
		else
			this.angle -= this.idleTurretSpeed * Panel.frameFrequency;

		this.idleTimer -= Panel.frameFrequency;

		if (idleTimer <= 0)
		{
			this.idleTimer = (int) (Math.random() * turretIdleTimerRandom) + turretIdleTimerBase;
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
			this.aimAngle = this.getAngleInDirection(this.nearestBullet.posX + this.nearestBullet.vX * Movable.distanceBetween(this, this.nearestBullet) / this.bulletSpeed, this.nearestBullet.posY + this.nearestBullet.vY * Movable.distanceBetween(this, nearestBullet) / this.bulletSpeed);
			this.disableOffset = true;
		}
		else if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank)
		{
			Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getPolarDirection(), 0, (Tank) targetEnemy, Game.tank_size);
			r.size = Game.tank_size;

			this.disableOffset = false;

			if (r.getDist() > 2)
			{
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX + targetEnemy.vX * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed, targetEnemy.posY + targetEnemy.vY * Movable.distanceBetween(this, targetEnemy) / this.bulletSpeed);
			}
			else
			{
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			}
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.disableOffset = false;
		}

		double a = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);

		Ray r = new Ray(this.posX, this.posY, a, 0, this);
		r.moveOut(5);

		Movable m = r.getTarget();

		if (m != null)
			if (m.equals(this.targetEnemy))
				this.shoot();


		if (Math.abs(this.aimAngle - this.angle) > this.aimThreshold / 2)
		{
			if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
				this.angle += this.aimTurretSpeed * Panel.frameFrequency;
			else
				this.angle -= this.aimTurretSpeed * Panel.frameFrequency;		

			this.angle = this.angle % (Math.PI * 2);
		}

		if (Math.abs(this.angle - this.aimAngle) < this.aimThreshold && !this.disableOffset)
			this.angle = this.aimAngle;
	}

	public void updateTurretReflect()
	{
		if (!this.straightShoot)
			this.search();

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy)
		{
			this.aimAngle = this.getAngleInDirection(this.nearestBullet.posX + this.nearestBullet.vX * Movable.distanceBetween(this, this.nearestBullet) / this.bulletSpeed, this.nearestBullet.posY + this.nearestBullet.vY * Movable.distanceBetween(this, nearestBullet) / this.bulletSpeed);
			this.disableOffset = true;
		}
		else if (aim && this.hasTarget)
		{
			this.updateAimingTurret();
		}
		else
		{
			this.updateIdleTurret();
		}
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
		ray.moveOut(5);

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
			else if (target instanceof Tank && target.hiddenTimer <= 0 && !Team.isAllied(target, this))
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
		rayToTarget.moveOut(5);
		Movable target = rayToTarget.getTarget();

		if (target != null)
		{
			if (target.equals(this.targetEnemy))
			{
				this.seesTargetEnemy = true;
			}
			else
				this.seesTargetEnemy = false;
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
			this.shoot();
		else
		{
			double speed = this.aimTurretSpeed;
			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			if (Movable.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 1.5)
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

			this.idleTimer = (int) (Math.random() * this.turretIdleTimerRandom) + this.turretIdleTimerBase;
		}
	}

	public void updateMineAI()
	{
		double nearestX = 1000;
		double nearestY = 1000;
		double nearestTimer = 1000;


		if (this.mineTimer == -1)
			this.mineTimer = (Math.random() * mineTimerRandom + mineTimerBase);

		Movable nearest = null;

		if (!laidMine)
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m instanceof Mine)
				{
					if (Math.pow(m.posX - this.posX, 2) + Math.pow(m.posY - this.posY, 2) <= Math.pow(((Mine)m).radius, 2))
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

		laidMine = false;

		if (nearest != null)
		{
			if (this.enableMineAvoidance && this.enableMovement)
			{
				this.setMotionAwayFromDirection(nearest.posX, nearest.posY, speed);
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
					Drawing.drawing.playSound("resources/lay-mine.wav");

					Game.movables.add(new Mine(this.posX, this.posY, this));
					this.mineTimer = (Math.random() * mineTimerRandom + mineTimerBase);
					double angleV = this.getPolarDirection() + Math.PI + (Math.random() - 0.5) * Math.PI / 2;
					this.overrideDirection = true;
					this.setPolarMotion(angleV, speed);
					laidMine = true;
				}
				else
					laidMine = false;

			}
		}

		if (Math.abs(nearestX) + Math.abs(nearestY) <= 1)
		{
			this.overrideDirection = true;
			this.setPolarMotion(Math.random() * 2 * Math.PI, speed);
		}

		this.mineTimer = Math.max(0, this.mineTimer - Panel.frameFrequency);
	}

	/** Called after updating but before applying motion. Intended to be overridden.*/
	public void postUpdate()
	{

	}
}

package tanks;

import java.awt.Color;
import java.util.ArrayList;

/** This class is the a skeleton tank class.
 *  It can be extended and values can be changed to easily produce an AI for another tank.
 *  Also, the behavior is split into many methods which are intended to be overridden easily.*/
public class EnemyTankDynamic extends Tank
{

	/** Determines which type of AI the tank will use when shooting.
	 *  Straight means that the tank will shoot directly at the player if the player is in line of sight.
	 *  Reflect means that the tank will use a Ray with reflections to find possible ways to hit the player.
	 *  Alternate means that the tank will switch between shooting straight at the player and using the reflect AI with every shot.*/
	public enum ShootAI {straight, alternate, reflect}

	/** The type which shows what direction the tank is moving. Clockwise and Counter Clockwise are for idle, while Aiming is for when the tank aims.*/
	protected enum RotationPhase {clockwise, counterClockwise, aiming}

	// The following are properties which are used externally to determine the behavior settings of the tank.
	// Simple modifications of tanks can just change these values to produce a desired behavior.
	// More complex behaviors may require overriding of methods.
	// These values do not change normally along the course of the game.

	/** When set to true, will call reactToPlayerSight() when an unobstructed line of sight to the player can be made */
	public boolean enablePlayerReaction = true;

	/** When set to true, the tank will be able to lay mines*/
	public boolean enableMineLaying = true;

	/** When set to true, the tank will avoid nearby mines*/
	public boolean enableMineAvoidance = true;

	/** When set to true, the tank will avoid nearby bullets which will hit it*/
	public boolean enableBulletAvoidance = true;

	/** When set to true, will calculate player velocity when shooting. Only effective when shootAIType is straight!*/
	public boolean enablePredictiveFiring = true;

	/** When set to true, will calculate player velocity when shooting. Only effective when shootAIType is straight!*/
	public boolean enableDefensiveFiring = false;
	
	/** Bounces calculated while aiming for the player, should usually be set to how many bounces the bullet it shoots has*/
	public int aimRayBounces = 1;

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	public double aimAccuracyOffset = 0.2;

	/** Threshold angle difference needed between angle and aimAngle to count as touching the player*/
	public double aimThreshold = 0.08;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * Math.random()*/
	public double turretIdleTimerBase = 25;

	/** Random factor in calculating time to randomly change idle direction, multiplied by Math.random() and added to turretIdleTimerBase*/
	public double turretIdleTimerRandom = 500;

	/** Minimum time to lay a mine, added to mineTimerRandom * Math.random()*/
	public double mineTimerBase = 2000;

	/** Random factor in calculating time to lay a mine, multiplied by Math.random() and added to mineTimerBase*/
	public double mineTimerRandom = 2000;

	/** Minimum time in between shooting bullets, added to cooldownRandom * Math.random()*/
	public double cooldownBase = 60;

	/** Random factor in calculating time between shooting bullets, multiplied by Math.random() and added to cooldownBase*/
	public double cooldownRandom = 20;

	/** Time waited when changing direction of motion*/
	public double directionChangeCooldown = 15;

	/** Speed at which the turret moves while aiming at a player*/
	public double aimTurretSpeed = 0.03;

	/** Speed at which the turret moves while idle*/
	public double idleTurretSpeed = 0.005;

	/** Speed at which the tank moves*/
	public double speed = 2.5;

	/** Chance per frame to change direction*/
	public double motionChangeChance = 0.001;

	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	public double avoidTimerBase = 30;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;
	
	/** Disable offset to shoot a bullet*/
	public boolean disableOffset = false;

	/** Type of shooting AI to use*/
	public ShootAI shootAIType;

	/** Bullet speed of it's bullets, used internally in calculations*/
	public double bulletSpeed = 25.0 / 4;

	// The following are values which are internally used for carrying out behavior.
	// These values change constantly during the course of the game.

	/** Used for tanks which do not use the straight AI, when detecting the player with a ray. Tells the tank to aim towards the found target angle.*/
	protected boolean aim = false;

	/** True for when a tank just laid a mine*/
	protected boolean laidMine = false;

	/** Alternates for tanks with the alternate AI. Tells tanks to shoot with reflection and then to shoot straight.*/
	protected boolean straightShoot = false;

	/** If a direct line of sight to the player exists, set to true*/
	protected boolean seesPlayer = false;

	/** For tanks with the straight AI, set to true when pointing at the player*/
	protected boolean locked = false;

	/** Age in frames*/
	protected int age = 0;

	/** Stores distances to obstacles or tanks in 8 directions*/
	protected int[] distances = new int[8];

	/** Used only in non-straight AI tanks. When detecting the player, set to the angle necessary to hit them. This angle is added to random offsets to search for the player moving.*/
	protected double lockedAngle = 0;

	/** Used only in non-straight AI tanks. Angle at which the tank is searching with its aim ray for the player*/
	protected double searchAngle = 0;

	/** Angle at which the tank aims after having found its target (if non-straight AI, found with a ray, otherwise just the angle to the tank)*/
	protected double aimAngle = 0;

	/** Direction in which the tank moves*/
	protected double direction = ((int)(Math.random() * 8))/2.0;

	/** Direction in which the tank moves to avoid a bullet that will hit it*/
	protected double avoidDirection = 0;

	/** Time until the tank will change its idle turret's direction*/
	protected double idleTimer = (Math.random() * turretIdleTimerRandom) + turretIdleTimerBase;

	/** Time between shooting bullets*/
	protected double cooldown = 100;

	/** Time until the next mine will be laid*/
	protected double mineTimer = (Math.random() * mineTimerBase + mineTimerRandom);

	/** Time which the tank will aim at its lockedAngle until giving up and continuing to search*/
	protected double aimTimer = 0;

	/** Time the tank will continue to avoid a bullet*/
	protected double avoidTimer = 0;

	/** Nearest bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBullet;
	
	/** Direction added to the bullet's direction to flee a bullet, possibly mirrored*/
	protected double fleeDirection = Math.PI / 2;

	/** Phase the tank is searching in, not used for straight AI*/
	protected RotationPhase searchPhase = RotationPhase.clockwise;

	/** Phase the tank turret is idling in, not used for straight AI*/
	protected RotationPhase idlePhase = RotationPhase.clockwise;

	/** Time until the tank will continue motion*/
	protected double motionTimer = 0;

	public EnemyTankDynamic(double x, double y, int size, Color color, double angle, ShootAI ai) 
	{
		super(x, y, size, color);

		if (Math.random() < 0.5)
			this.idlePhase = RotationPhase.counterClockwise;

		this.coinValue = 10;
		this.angle = angle;

		this.liveBulletMax = 5;

		this.shootAIType = ai;
	}

	/** Prepare to fire a bullet*/
	@Override
	public void shoot() 
	{
		this.aimTimer = 10;
		this.aim = false;

		if (this.cooldown <= 0 && this.liveBullets < this.liveBulletMax)
		{
			// Cancels if the bullet will hit another enemy
			double offset = Math.random() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2);

			if (this.disableOffset)
			{
				offset = 0;
				this.disableOffset = false;
			}
			
			Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.aimRayBounces, this);
			Movable m = a.getTarget();
			if (!(m instanceof Tank && !m.equals(Game.player)))
			{
				this.launchBullet(offset);
			}
		}
	}

	/** Actually fire a bullet*/
	public void launchBullet(double offset)
	{
		Bullet b = new Bullet(this.posX, this.posY, Color.blue, 1, this);
		b.setPolarMotion(angle + offset, 25.0/4);
		b.moveOut(8);

		b.effect = Bullet.BulletEffect.trail;
		Game.movables.add(b);
		this.cooldown = (int) (Math.random() * this.cooldownRandom + this.cooldownBase);

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}

	@Override
	public void update()
	{
		this.angle = this.angle % (Math.PI * 2);

		this.age++;

		if (!this.destroy)
		{
			this.updateMotionAI();
			this.updateTurretAI();
			this.updateMineAI();
		}

		super.update();
	}

	public void updateMotionAI()
	{
		if (this.enableBulletAvoidance)
			this.checkForBulletThreats();

		if (this.avoidTimer > 0)
		{
			this.avoidTimer -= Panel.frameFrequency;
			this.setPolarMotion(avoidDirection, speed);
		}
		else
		{
			fleeDirection = -fleeDirection;

			if (this.seesPlayer && this.enablePlayerReaction)
			{
				this.reactToPlayerSight();
			}
			else
			{
				this.updateIdleMotion();
			}
		}

	}

	public void reactToPlayerSight()
	{
		this.setMotionInDirection(Game.player.posX, Game.player.posY, speed);
	}

	public void updateIdleMotion()
	{
		if (Math.random() < this.motionChangeChance || this.hasCollided)
		{
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

			this.motionTimer = this.directionChangeCooldown;
		}

		if (this.motionTimer > 0)
		{
			this.vX = 0;
			this.vY = 0;
			this.motionTimer = (Math.max(0, this.motionTimer - Panel.frameFrequency));	
		}
		else
		{		
			this.setPolarMotion(this.direction / 2 * Math.PI, speed);
			this.addIdleMotionOffset();
		}
	}

	public void addIdleMotionOffset()
	{
		double offsetMotion = Math.sin(this.age * 0.02);
		if (offsetMotion < 0)
		{
			int dist = this.distances[(int) (this.direction * 2 + 6) % 8];
			offsetMotion *= Math.max(1, (dist - 1) / 5.0);
		}
		else
		{
			int dist = this.distances[(int) (this.direction * 2 + 2) % 8];
			offsetMotion *= Math.max(1, (dist - 1) / 5.0);
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

					Movable m = r.getTarget(2, 2);
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
		this.lookAtPlayer();

		if (this.shootAIType.equals(ShootAI.straight))
			this.updateTurretStraight();
		else
			this.updateTurretReflect();

		this.cooldown -= Panel.frameFrequency;
	}

	public void updateTurretStraight()
	{
		if (this.avoidTimer > 0 && this.enableDefensiveFiring && !this.nearestBullet.destroy)
		{
			this.aimAngle = this.getAngleInDirection(this.nearestBullet.posX + this.nearestBullet.vX * Movable.distanceBetween(this, this.nearestBullet) / this.bulletSpeed, this.nearestBullet.posY + this.nearestBullet.vY * Movable.distanceBetween(this, nearestBullet) / this.bulletSpeed);
			this.disableOffset = true;
			this.locked = false;
		}
		else if (this.enablePredictiveFiring)
		{
			this.aimAngle = this.getAngleInDirection(Game.player.posX + Game.player.vX * Movable.distanceBetween(this, Game.player) / this.bulletSpeed, Game.player.posY + Game.player.vY * Movable.distanceBetween(this, Game.player) / this.bulletSpeed);
			this.disableOffset = false;
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(Game.player.posX, Game.player.posY);
			this.disableOffset = false;
		}
		
		double a = this.getAngleInDirection(Game.player.posX, Game.player.posY);

		Ray r = new Ray(this.posX, this.posY, a, 0, this);
		Movable m = r.getTarget();

		if (m != null)
			if (m.equals(Game.player))
				this.shoot();

		if (Math.abs(this.aimAngle - this.angle) > this.aimThreshold / 2 && !locked)
		{
			if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
				this.angle += this.aimTurretSpeed;
			else
				this.angle -= this.aimTurretSpeed;

			if (Math.abs(this.angle - this.aimAngle) < this.aimThreshold && !this.disableOffset)
				this.locked = true;

			this.angle = this.angle % (Math.PI * 2);
		}

		if (locked)
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
			this.locked = false;
		}
		else if (aim)
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

		Ray ray = new Ray(this.posX, this.posY, this.searchAngle, 1, this);
		Movable target = ray.getTarget();
		if (target != null)
			if (target.equals(Game.player))
			{
				this.lockedAngle = this.angle;
				this.searchPhase = RotationPhase.aiming;
				this.aim = true;
				this.aimAngle = this.searchAngle % (Math.PI * 2);
			}
	}

	public void lookAtPlayer()
	{
		double a;

		if (this.enablePredictiveFiring)
			a = this.getAngleInDirection(Game.player.posX + Game.player.vX * Movable.distanceBetween(this, Game.player) / this.bulletSpeed, Game.player.posY + Game.player.vY * Movable.distanceBetween(this, Game.player) / this.bulletSpeed);
		else
			a = this.getAngleInDirection(Game.player.posX, Game.player.posY);

		Ray rayToPlayer = new Ray(this.posX, this.posY, a, 0, this);
		Movable playerTarget = rayToPlayer.getTarget();

		if (playerTarget != null)
		{
			if (playerTarget.equals(Game.player))
			{
				this.seesPlayer = true;
			}
			else
				this.seesPlayer = false;
		}
		else
			this.seesPlayer = false;

		if (this.straightShoot)
		{

			if (playerTarget != null)
			{
				if (playerTarget.equals(Game.player))
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
		if (Math.abs(this.aimAngle - this.angle) < this.aimThreshold)
			this.shoot();
		else
		{
			if ((this.angle - this.aimAngle + Math.PI * 3) % (Math.PI*2) - Math.PI < 0)
				this.angle += this.aimTurretSpeed * Panel.frameFrequency;
			else
				this.angle -= this.aimTurretSpeed * Panel.frameFrequency;

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

		Movable nearest = null;
		if (!laidMine)
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m instanceof Mine && Math.abs(this.posX - m.posX) < Game.tank_size * 3 && Math.abs(this.posY - m.posY) < Game.tank_size * 3)
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

		if (nearest != null)
		{
			if (this.enableMineAvoidance)
				this.setMotionAwayFromDirection(nearest.posX, nearest.posY, speed);
		}
		else
		{
			if (this.mineTimer <= 0 && this.enableMineLaying)
			{
				boolean layMine = true;
				int i = 0;
				while (i < Game.movables.size())
				{
					Movable m = Game.movables.get(i);
					if (m instanceof Tank && !m.equals(Game.player) && !m.equals(this))
					{
						Tank t = (Tank) m;
						if (Math.abs(t.posX - this.posX) <= 200 && Math.abs(t.posY - this.posY) <= 200)
						{
							layMine = false;
							break;
						}

					}
					i++;
				}

				if (layMine)
				{
					Game.movables.add(new Mine(this.posX, this.posY, this));
					this.mineTimer = (int) (Math.random() * mineTimerRandom + mineTimerBase);
					double angleV = this.getPolarDirection() + Math.PI + (Math.random() - 0.5) * Math.PI / 2;
					this.setPolarMotion(angleV, speed);
					laidMine = true;
				}
				else
					laidMine = false;

			}
		}

		if (Math.abs(nearestX) + Math.abs(nearestY) <= 1)
		{
			this.setPolarMotion(Math.random() * 2 * Math.PI, speed);
		}

		this.mineTimer -= Panel.frameFrequency;
	}
}

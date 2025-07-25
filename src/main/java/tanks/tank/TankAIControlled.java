package tanks.tank;

import basewindow.Color;
import basewindow.IModel;
import tanks.*;
import tanks.bullet.*;
import tanks.effect.AttributeModifier;
import tanks.gui.screen.ScreenGame;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleTeleporter;
import tanks.registry.RegistryTank;
import tanks.tankson.*;
import tanks.translation.Translation;

import java.lang.reflect.Field;
import java.util.*;

import static tanks.tank.TankPropertyCategory.*;

/** This class is the 'skeleton' tank class.
 *  It can be extended and values can be changed to easily produce an AI for another tank.
 *  Also, the behavior is split into many methods which are intended to be overridden easily.*/
@TanksONable("tank")
public class TankAIControlled extends Tank implements ITankField
{
	/** The type which shows what direction the tank is moving. Clockwise and Counter Clockwise are for idle, while Aiming is for when the tank aims.*/
	protected enum RotationPhase {clockwise, counter_clockwise, aiming, recalculating}

	// The following are properties which are used externally to determine the behavior settings of the tank.
	// Simple modifications of tanks can just change these values to produce a desired behavior.
	// More complex behaviors may require overriding of methods.
	// These values do not change normally along the course of the game.

	@Property(category = firingGeneral, id = "bullet", name = "Bullet")
	public ItemBullet.ItemStackBullet bulletItem = new ItemBullet.ItemStackBullet(null, DefaultItems.basic_bullet.getCopy(), 0);

	/** The mine a tank uses. If you want to change this, make sure to use setMine() because it also updates the mineItem. */
	@Property(category = mines, id = "mine", name = "Mine")
	public ItemMine.ItemStackMine mineItem = new ItemMine.ItemStackMine(null, DefaultItems.basic_mine.getCopy(), -1);

	@Property(category = movementGeneral, id = "enable_movement", name = "Can move")
	public boolean enableMovement = true;

	/** Chance per frame to change direction*/
	@Property(category = movementIdle, id = "motion_change_chance", name = "Turn chance", minValue = 0.0, desc = "Chance of the tank to change the direction in which it is moving")
	public double turnChance = 0.01;
	/** Time waited when changing direction of motion*/
	@Property(category = movementIdle, id = "turn_pause_time", name = "Turn pause time", minValue = 0.0, desc = "Time the tank pauses when changing directions \n \n 1 time unit = 0.01 seconds")
	public double turnPauseTime = 15;
	/** Multiplier of time the tank will hide in a shrub*/
	@Property(category = movementIdle, id = "bush_hide_time", name = "Bush hide time", minValue = 0.0, desc = "Time the tank will stop moving to hide in bushes \n \n 1 time unit = 0.01 seconds")
	public double bushHideTime = 350;

	@Property(category = movementIdle, id = "stay_near_parent", name = "Stay near parent", desc = "If spawned by another tank, whether this tank should try to stay near the tank that spawned it")
	public boolean stayNearParent = false;
	@Property(category = movementIdle, id = "max_distance_from_parent", name = "Parent boundary", minValue = 0.0, desc = "If stay near parent is set and this tank strays farther than this distance from the tank that spawned it, it will return to that tank \n \n 1 tile = 50 units")
	public double maxDistanceFromParent = 300;

	@Property(category = movementAvoid, id = "enable_bullet_avoidance", name = "Avoid bullets")
	public boolean enableBulletAvoidance = true;
	@Property(category = movementAvoid, id = "enable_mine_avoidance", name = "Avoid mines")
	public boolean enableMineAvoidance = true;
	@Property(category = movementAvoid, id = "avoid_seek_open_spaces", name = "Seek open spaces", desc = "If enabled, when this tank avoids farther bullets, it will seek out open spaces around it to make it harder to corner")
	public boolean avoidanceSeekOpenSpaces = false;

	/** The method used to avoid bullets
	 *  Back off = move away from the bullet directly
	 *  Dodge = move at an angle from the bullet
	 *  Aggressive Dodge = move at an angle toward the bullet
	 *  Intersect = move away
	 *  from where bullet path will intersect tank; less accurate */
	public enum BulletAvoidBehavior
	{intersect, back_off, dodge, aggressive_dodge, back_off_dodge}

	@Property(category = movementAvoid, id = "bullet_avoid_behavior", name = "Bullet avoid behavior", desc = "Method the tank will use to avoid bullets \n " +
			"\n Intersect: move away from where the bullet will hit the tank (less accurate) " +
			"\n Back off: move away from the bullet (may back into corners) " +
			"\n Dodge: move at an angle away from the bullet (more accurate) " +
			"\n Aggressive dodge: move at an angle toward the bullet " +
			"\n Back off dodge: back off if there is space behind, dodge otherwise")
	public BulletAvoidBehavior bulletAvoidBehvavior = BulletAvoidBehavior.intersect;
	/** How close the tank needs to get to a mine to avoid it*/
	@Property(category = movementAvoid, id = "mine_avoid_sensitivity", name = "Mine sight radius", desc = "If the tank is within this fraction of a mine's radius, it will move away from the mine")
	public double mineAvoidSensitivity = 1.5;
	/** Time which the tank will avoid a bullet after the bullet is no longer aiming at the tank*/
	@Property(category = movementAvoid, id = "bullet_avoid_timer_base", name = "Bullet flee time", desc = "Time the tank will continue fleeing from a bullet until after it is no longer deemed a threat \n \n 1 time unit = 0.01 seconds")
	public double bulletAvoidTimerBase = 30;

	/** If enabled, the tank may actively seek out enemies*/
	@Property(category = movementPathfinding, id = "enable_pathfinding", name = "Seek targets", desc = "If enabled, the tank may decide to navigate through the level towards its target. If this tank can lay mines, it may also use them to get to the target.")
	public boolean enablePathfinding = false;
	/** Chance per frame to seek the target enemy*/
	@Property(category = movementPathfinding, id = "seek_chance", minValue = 0.0, maxValue = 1.0, name = "Seek chance", desc = "Chance for this tank to decide to start navigating to its target")
	public double seekChance = 0.001;
	/** If set to true, when enters line of sight of target enemy, will stop pathfinding to it*/
	@Property(category = movementPathfinding, id = "stop_seeking_on_sight", name = "Stop on sight", desc = "If enabled, navigation to target will end when the this tank enters the target's line of sight")
	public boolean stopSeekingOnSight = false;
	/** Increasing this value increases how stubborn the tank is in following a path*/
	@Property(category = movementPathfinding, id = "seek_timer_base", minValue = 0.0, name = "Seek patience", desc = "If this tank is blocked from navigating its path for this amount of time, it will abandon the navigation \n \n 1 time unit = 0.01 seconds")
	public double seekTimerBase = 200;

	/** Type of behavior tank should have if its target enemy is in line of sight
	 * 	Approach = go towards the target enemy
	 * 	Flee = go away from the target enemy
	 * 	Strafe = move perpendicular to target enemy
	 * 	Sidewind = move at a 45 degree angle toward target enemy
	 * 	Backwind = move at a 45 degree angle away target enemy
	 * 	Keep Distance = stay a particular distance away from the target enemy*/
	public enum TargetEnemySightBehavior {approach, flee, strafe, sidewind, backwind, keep_distance}

	/** When set to true, will shoot a ray at the target enemy and enable reactions when the target enemy is in sight*/
	@Property(category = movementOnSight, id = "enable_looking_at_target_enemy", name = "Test sight", desc = "When enabled, the tank will test if its target is in its line of sight, and react accordingly")
	public boolean enableLookingAtTargetEnemy = true;
	/** When set to true, will call reactToTargetEnemySight() when an unobstructed line of sight to the target enemy can be made */
	public boolean enableTargetEnemyReaction = true;
	/** Type of behavior tank should have if its target enemy is in line of sight*/
	@Property(category = movementOnSight, id = "target_enemy_sight_behavior", name = "Reaction", desc = "How the tank should react upon line of sight \n " +
			"Approach - move directly toward the target \n " +
			"Flee - move directly away from the target \n " +
			"Strafe around it - move perpendicular to the target \n " +
			"Sidewind - zig-zag toward the target \n " +
			"Backwind - zig-zag away from the target \n " +
			"Keep distance - move to or away from the target until at a specific distance to it")
	public TargetEnemySightBehavior targetEnemySightBehavior = TargetEnemySightBehavior.approach;
	/** If set to strafe upon seeing the target enemy, chance to change orbit direction*/
	@Property(category = movementOnSight, id = "strafe_direction_change_chance", minValue = 0.0, maxValue = 1.0, name = "Strafe frequency", desc = "If set to strafe on line of sight, chance the tank should change the direction it is strafing around the target")
	public double strafeDirectionChangeChance = 0.01;
	/** If set to keep a distance, the tank will maintain that distance from its target upon sight*/
	@Property(category = movementOnSight, id = "target_sight_distance", minValue = 0.0, name = "Target distance", desc = "If set to keep distance on line of sight, how far away the tank will try to sit from its target \n \n 1 tile = 50 units")
	public double targetSightDistance = Game.tile_size * 6;

	/** Tank to transform into*/
	@Property(category = transformationOnSight, id = "sight_transform_tank", name = "Transformation tank", desc = "When set, the tank will transform into this tank upon entering line of sight with its target", nullable = true)
	public ITankField sightTransformTankField = null;
	/** Don't set this. It's automatically set by resolving "sightTransformTankField" which may be a reference! */
	private TankAIControlled sightTransformTank = null;
	/** Time for tank to revert after losing line of sight */
	@Property(category = transformationOnSight, id = "sight_transformation_revert_time", minValue = 0.0, name = "Sight revert time", desc = "After this much time has passed without the target in line of sight, the tank will revert back to its original form \n \n 1 time unit = 0.01 seconds")
	public double sightTransformRevertTime = 500;

	/** Tank to transform into*/
	@Property(category = transformationOnHealth, id = "health_transform_tank", name = "Transformation tank", desc = "When set, the tank will transform into this tank when its health is at or below the health threshold", nullable = true)
	public ITankField healthTransformTankField = null;
	/** Don't set this. It's automatically set by resolving "healthTransformTankField" which may be a reference! */
	private TankAIControlled healthTransformTank = null;
	/** Health threshold to transform */
	@Property(category = transformationOnHealth, id = "transform_health_threshold", minValue = 0.0, name = "Hitpoint threshold", desc = "Amount of health this tank must have equal to or less than to transform")
	public double transformHealthThreshold = 0;
	/** If set, the tank will seek and transform into other tanks in line of sight */
	@Property(category = transformationMimic, id = "transform_mimic", name = "Mimic", desc = "When enabled, the tank will mimic other nearby tanks it sees")
	public boolean transformMimic = false;

	/** Time for tank to revert after losing line of sight */
	@Property(category = transformationMimic, id = "mimic_revert_time", minValue = 0.0, name = "Mimic revert time", desc = "After this much time has passed without the target in line of sight, the tank will revert back to its original form \n \n 1 time unit = 0.01 seconds")
	public double mimicRevertTime = 200;
	/** Range tanks must be in to be mimicked */
	@Property(category = transformationMimic, id = "mimic_range", minValue = 0.0, name = "Mimic range", desc = "Maximum distance between this tank and a tank it mimics")
	public double mimicRange = Game.tile_size * 12;

	@Property(category = mines, id = "enable_mine_laying", name = "Can lay mines")
	public boolean enableMineLaying = true;

	//public double mineFuseLength = 1000;
	/** Minimum time to lay a mine, added to mineTimerRandom * this.random.nextDouble()*/
	@Property(category = mines, id = "mine_timer_base", minValue = 0.0, name = "Base cooldown", desc = "Minimum time between laying mines \n \n 1 time unit = 0.01 seconds")
	public double mineTimerBase = 2000;
	/** Random factor in calculating time to lay a mine, multiplied by this.random.nextDouble() and added to mineTimerBase*/
	@Property(category = mines, id = "mine_timer_random", minValue = 0.0, name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between laying mines \n \n 1 time unit = 0.01 seconds")
	public double mineTimerRandom = 4000;

	/** Minimum time in between shooting bullets, added to cooldownRandom * this.random.nextDouble()*/
	@Property(category = firingGeneral, id = "cooldown_base", minValue = 0.0, name = "Base cooldown", desc = "Minimum time between firing bullets \n \n Note: if the bullet's base cooldown is longer than the tank's base cooldown, the bullet's cooldown will be used \n \n 1 time unit = 0.01 seconds")
	public double cooldownBase = 60;
	/** Random factor in calculating time between shooting bullets, multiplied by this.random.nextDouble() and added to cooldownBase*/
	@Property(category = firingGeneral, id = "cooldown_random", minValue = 0.0, name = "Random cooldown", desc = "A random percentage between 0% and 100% of this time value is added to the base cooldown to get the time between firing bullets")
	public double cooldownRandom = 20;
	/** After every successive shot, cooldown will go down by this fraction */
	@Property(category = firingGeneral, id = "cooldown_speedup", minValue = 0.0, name = "Cooldown speedup", desc = "After every shot fired towards the same target, the cooldown will be decreased by this fraction of its current value")
	public double cooldownSpeedup = 0;
	/** Cooldown resets after no shots for this much time */
	@Property(category = firingGeneral, id = "cooldown_revert_time", minValue = 0.0, name = "Revert time", desc = "If the tank is unable to fire for this much time, the effects of cooldown speedup will reset \n \n 1 time unit = 0.01 seconds")
	public double cooldownRevertTime = 300;
	/** If set, the tank will charge a shot and wait its cooldown on the spot as it prepares to shoot */
	@Property(category = firingGeneral, id = "charge_up", name = "Charge up", desc = "If enabled, the tank will only wait its cooldown while aiming at an enemy tank, playing a charge up animation")
	public boolean chargeUp = false;

	/** Determines which type of AI the tank will use when shooting.
	 *  None means that the tank will not shoot
	 *  Sprinkler means the tank will just randomly shoot when it is able to
	 *  Straight means that the tank will shoot directly at the target enemy if the target enemy is in line of sight.
	 *  Reflect means that the tank will use a Ray with reflections to find possible ways to hit the target enemy.
	 *  Homing is similar to reflect but for tanks with homing bullets - fires if the bullet endpoint is in line of sight of the target.
	 *  Alternate means that the tank will switch between shooting straight at the target enemy and using the reflect AI with every shot.
	 *  Wander means that the tank will randomly rotate and shoot only if it detects the target enemy*/
	public enum ShootAI {none, sprinkler, wander, straight, homing, alternate, reflect}

	/** Type of shooting AI to use*/
	@Property(category = firingBehavior, id = "shoot_ai_type", name = "Aiming behavior", desc = "Behavior for aiming and firing at targets \n \n " +
			"None: do not shoot at all \n " +
			"Sprinkler: rotate randomly and continuously shoot \n " +
			"Wander: randomly rotate and shoot if target enemy falls in the trajectory \n " +
			"Straight: shoot directly at the target, if in line of sight \n " +
			"Reflect: use obstacles to calculate bounces \n " +
			"Alternate: switch between straight and reflect with every shot \n " +
			"Homing: like reflect, but recommended for homing bullets")
	public ShootAI shootAIType;

	/** Larger values decrease accuracy but make the tank behavior more unpredictable*/
	@Property(category = firingBehavior, id = "aim_accuracy_offset", minValue = 0.0, name = "Inaccuracy", desc = "Random angle added to bullet trajectory upon shooting to make things more unpredictable")
	public double aimAccuracyOffset = 0.2;
	/** Threshold angle difference needed between angle and aimAngle to count as touching the target enemy*/
	public double aimThreshold = 0.05;

	/** Minimum time to randomly change idle direction, added to turretIdleTimerRandom * this.random.nextDouble()*/
	@Property(category = firingBehavior, id = "turret_idle_timer_base", minValue = 0.0, name = "Turret base timer", desc = "Minimum time the turret will idly rotate in one direction before changing direction \n \n 1 time unit = 0.01 seconds")
	public double turretIdleTimerBase = 25;
	/** Random factor in calculating time to randomly change idle direction, multiplied by this.random.nextDouble() and added to turretIdleTimerBase*/
	@Property(category = firingBehavior, id = "turret_idle_timer_random", minValue = 0.0, name = "Turret random timer", desc = "A random percentage between 0% and 100% of this time value is added to the turret base rotation timer to get the time between changing idle rotation direction \n \n 1 time unit = 0.01 seconds")
	public double turretIdleTimerRandom = 500;

	/** Speed at which the turret moves while idle*/
	@Property(category = firingBehavior, id = "turret_idle_speed", minValue = 0.0, name = "Idle turret speed", desc = "Speed the turret turns at when not actively aiming at a target")
	public double turretIdleSpeed = 0.005;
	/** Speed at which the turret moves while aiming at a target enemy*/
	@Property(category = firingBehavior, id = "turret_aim_speed", minValue = 0.0, name = "Aim turret speed", desc = "Speed the turret turns at when actively aiming toward a target")
	public double turretAimSpeed = 0.03;

	/** When set to true, will calculate target enemy velocity when shooting. Only effective when shootAIType is straight!*/
	@Property(category = firingBehavior, id = "enable_predictive_firing", name = "Predictive", desc = "When enabled, will use the current velocity of the target to predict and fire towards its future position \n \n Only works with straight or alternate aiming behavior!")
	public boolean enablePredictiveFiring = true;
	/** When set to true, will shoot at bullets aiming towards the tank*/
	@Property(category = firingBehavior, id = "enable_defensive_firing", name = "Deflect bullets", desc = "When enabled, will shoot at incoming bullet threats to deflect them \n \n Does not work with wander or sprinkler aiming behavior!")
	public boolean enableDefensiveFiring = false;
	@Property(category = firingBehavior, id = "defensive_fire_urgency", name = "Deflection urgency", desc = "The random cooldown between firing bullets will be reduced by this fraction when the tank is deflecting incoming bullets. \n \n " +
			"For example, if a tank has a bullet base cooldown of 100, random cooldown of 50, and defensive firing urgency of 0.8, it will normally shoot every 100-150 time units, but when it is trying to deflect an incoming bullet, " +
			"it will shoot every 100-110 time units, since the random cooldown is reduced by the urgency factor of 0.8, from 50 to 10. \n \n 1 time unit = 0.01 seconds")
	public double defensiveFiringUrgency = 1;
	/** Will look through destructible walls when set to true for bullet shooting, recommended for explosive bullets*/
	@Property(category = firingBehavior, id = "aim_ignore_destructible", name = "Through walls", desc = "When enabled, will shoot at destructible blocks if the target is hiding behind them. This is useful for tanks with explosive bullets.")
	public boolean aimIgnoreDestructible = false;

	/** Number of bullets in bullet fan*/
	@Property(category = firingPattern, id = "shot_round_count", minValue = 1.0, name = "Shots per round", desc = "Number of bullets to fire per round")
	public int shotRoundCount = 1;
	/** Time to fire a full fan*/
	@Property(category = firingPattern, id = "shot_round_time", minValue = 0.0, name = "Round time", desc = "Amount of time it takes to fire a full round of bullets \n \n 1 time unit = 0.01 seconds")
	public double shootRoundTime = 60;
	/** Spread of a round*/
	@Property(category = firingPattern, id = "shot_round_spread", minValue = 0.0, name = "Round spread", desc = "Total angle of spread of a round")
	public double shotRoundSpread = 36;

	@TanksONable("spawned_tank")
	public static class SpawnedTankEntry
	{
		@Property(id = "tank")
		public ITankField tank;
		public Tank resolvedTank;

		@Property(id = "weight")
		public double weight;

		public SpawnedTankEntry(ITankField t, double weight)
		{
			this.tank = t;
			this.weight = weight;
		}

		public String toString()
		{
			return Serializer.toTanksON(this);
		}
	}

	@Property(category = spawning, id = "spawned_tanks", name = "Spawned tanks", desc = "Tanks which will be spawned by this tank as support", miscType = Property.MiscType.spawnedTanks)
	public ArrayList<SpawnedTankEntry> spawnedTankEntries = new ArrayList<>();
	/** Tanks spawned on initial load*/
	@Property(category = spawning, id = "spawned_initial_count", minValue = 0.0, name = "Initial count", desc = "Number of tanks spawned immediately when this tank is created")
	public int spawnedInitialCount = 4;
	/** Max number of spawned tanks*/
	@Property(category = spawning, id = "spawned_max_count", minValue = 0.0, name = "Max count", desc = "Maximum number of spawned tanks from this tank that can be on the field at once")
	public int spawnedMaxCount = 6;
	/** Chance for this tank to spawn another tank*/
	@Property(category = spawning, id = "spawn_chance", minValue = 0.0, maxValue = 1.0, name = "Spawn chance", desc = "Chance for this tank to spawn another tank")
	public double spawnChance = 0.003;

	/** Whether the tank should commit suicide when there are no allied tanks on the field */
	@Property(category = lastStand, id = "enable_suicide", name = "Last stand", desc = "When enabled and there are no allied tanks on the field, this tank will charge at the nearest enemy and destroy itself.")
	public boolean enableSuicide = false;
	@Property(category = lastStand, id = "suicide_explosion", name = "Destroy explosion", desc="When destroying itself from its last stand ability, the tank will explode with this explosion.", nullable = true)
	public Explosion suicideExplosion = new Explosion();
	/** Base factor in calculating suicide timer: base + random * Math.random()*/
	@Property(category = lastStand, id = "suicide_timer_base", minValue = 0.0, name = "Base timer", desc = "Minimum time this tank will charge at its enemy before blowing up \n \n 1 time unit = 0.01 seconds")
	public double suicideTimerBase = 500;
	/** Random factor in calculating suicide timer: base + random * Math.random() */
	@Property(category = lastStand, id = "suicide_timer_random", minValue = 0.0, name = "Random timer", desc = "A random fraction of this value is added to the base timer to get the time this tank will charge before exploding \n \n 1 time unit = 0.01 seconds")
	public double suicideTimerRandom = 250;
	/** Suicidal mode maximum speed increase*/
	@Property(category = lastStand, id = "suicide_speed_boost", name = "Speed boost", desc = "Maximum increase in speed while charging as a last stand")
	public double suicideSpeedBoost = 3;

	/** Range which rays will be used to detect a tank after being locked on to it. Larger values detect motion better but are less accurate.*/
	public double searchRange = 0.3;

	public String shotSound = null;

	// The following are values which are internally used for carrying out behavior.
	// These values change constantly during the course of the game.

	/** Used for tanks which do not use the straight AI, when detecting the target enemy with a ray. Tells the tank to aim towards the found target angle.*/
	protected boolean aim = false;

	/** If just stopped aiming */
	protected boolean stoppedAiming = false;

	/** True for when a tank just laid a mine*/
	protected boolean laidMine = false;

	/** Alternates for tanks with the alternate AI. Tells tanks to shoot with reflection and then to shoot straight.*/
	protected boolean straightShoot = false;

	/** If a direct line of sight to the target enemy exists, set to true*/
	protected boolean seesTargetEnemy = false;

	/** Stores distances to obstacles or tanks in 8 directions*/
	protected double[] distances = new double[8];

	/** Stores distances to obstacles or tanks in 32 directions*/
	protected double[] fleeDistances = new double[32];

	/** Stores directions a tank may flee from a bullet, relative to that bullet's direction */
	protected double[] fleeDirections = new double[fleeDistances.length];

	/** Cooldown before the tank will turn again if it's running into a wall */
	protected double gentleTurnCooldown = 0;

	/** Time in which the tank will follow its initial flee path from a mine*/
	protected double mineFleeTimer = 0;

	/** Used only in non-straight AI tanks. When detecting the target enemy, set to the angle necessary to hit them. This angle is added to random offsets to search for the target enemy moving.*/
	protected double lockedAngle = 0;

	/** Used only in non-straight AI tanks. Angle at which the tank is searching with its aim ray for the target enemy*/
	protected double searchAngle = 0;

	/** Angle at which the tank aims after having found its target (if non-straight AI, found with a ray, otherwise just the angle to the tank)*/
	protected double aimAngle = 0;

	/** Distance to target; only used for arc bullet shooting tanks*/
	protected double distance = 0;

	/** Direction in which the tank moves when idle*/
	protected double direction;

	/** When enabled, the current motion direction will be kept until the tank decides to change direction*/
	protected boolean overrideDirection = false;

	/** Direction in which the tank moves to avoid a bullet that will hit it*/
	protected double avoidDirection = 0;

	/** Time until the tank will change its idle turret's direction*/
	protected double idleTimer;

	/** Time between shooting bullets*/
	protected double cooldown = 250;

	/** Inaccuracy of next shot*/
	protected double shotOffset = 0;

	/** Time until the next mine will be laid*/
	protected double mineTimer = -1;

	/** Time which the tank will aim at its lockedAngle until giving up and continuing to search*/
	protected double aimTimer = 0;

	/** Time the tank will continue to avoid a bullet*/
	protected double avoidTimer = 0;

	/** Time until reverting transformation */
	protected double transformRevertTimer = 0;

	/** Set if the tank will eventually turn back into the original tank it was */
	protected boolean willRevertTransformation = true;

	/** Nearest bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBullet;

	/** Time until the nearest threat bullet will strike */
	protected double nearestBulletDist;

	/** Nearest deflectable bullet aiming at this tank, if avoid timer is > than 0*/
	protected Bullet nearestBulletDeflect;

	/** Time until the nearest deflectable threat bullet will strike */
	protected double nearestBulletDeflectDist;

	/** Number of bullet threats that will hit this tank */
	protected int bulletThreatCount;

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

	/** Normally the nearest tank not on this tank's team. This is the tank that this tank will fight.*/
	protected Movable targetEnemy;

	/** Tank that this tank is trying to mimic */
	protected Movable mimicTarget = null;

	/** True if can find an enemy*/
	protected boolean hasTarget = true;

	/** If true, charges towards nearest enemy and explodes */
	public boolean suicidal = false;

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

	/** Tanks that this tank has spawned */
	protected ArrayList<Tank> spawnedTanks = new ArrayList<>();

	/** Time until the tank will commit suicide */
	public double timeUntilDeath;

	/** The random number generator the tank uses to make decisions*/
	protected Random random;

	/** Progress of a shooting fan for tanks firing multiple bullets per round*/
	protected double shootTimer = 0;

	/** Number of shots fired in the current round*/
	protected int shots = 0;

	/** Whether shooting in a fan */
	protected boolean shootingInFan = false;

	/** 1 or -1, indicating direction of fan being fired*/
	protected int fanDirection;

	/** True if the tank charged up this frame*/
	protected boolean justCharged = false;

	/** Used to calculate cooldown when it goes down for each shot (when cooldownSpeedup is not zero)*/
	protected int cooldownStacks = 0;

	/** Time passed since we last had a target ready to shoot at, used to reset cooldown stacks*/
	protected double cooldownIdleTime = 0;

	/** Fan round inaccuracy*/
	protected double fanOffset;

	/** Time until mimicking ends */
	protected double mimicRevertCounter = this.mimicRevertTime;

	/** Mimic laser effect*/
	protected Laser laser;

	/** Tank this tank is transformed into*/
	protected TankAIControlled transformTank = null;

	/** True if able to mimic other tanks*/
	protected boolean canCurrentlyMimic = true;

	protected Color baseColor = new Color();
	protected double baseMaxSpeed;

	/** Set if tank transformed in the last frame */
	public boolean justTransformed = false;

	protected double lastCooldown = this.cooldown;

	/** Whether to use rays for AI calculation this frame. Don't need to every frame to save on performance. */
	protected boolean useRaysThisFrame = false;

	/** How often to shoot rays for AI calculations. */
	protected double rayFrequency = 5;

	/** The time since rays were used. Initially randomized to spread things out. */
	protected double timeSinceRaysUsed = Math.random() * rayFrequency;

	/** Last stored distance from the tank to a wall in its direction of motion */
	protected double lastDistanceToWall = 1000;

	// Predicted position target enemy will collide into a wall
	protected double targetEnemyPredictedCollisionX;
	protected double targetEnemyPredictedCollisionY;
	protected double targetEnemyPredictedCollisionDist;

	public TankAIControlled()
	{
		this("" + System.currentTimeMillis(), 0, 0, Game.tile_size, 0, 0, 0, 0, ShootAI.none);
	}

	public TankAIControlled(String name, double x, double y, double size, double r, double g, double b, double angle, ShootAI ai)
	{
		super(name, x, y, size, r, g, b);

		this.random = new Random(Level.random.nextLong());
		this.direction = ((int)(this.random.nextDouble() * 8)) / 2.0;

		if (this.random.nextDouble() < 0.5)
			this.idlePhase = RotationPhase.counter_clockwise;

		this.angle = angle;
		this.orientation = angle;

		this.shootAIType = ai;

		for (int i = 0; i < fleeDirections.length; i++)
		{
			fleeDirections[i] = Math.PI / 4 + (i * 2 / fleeDirections.length) * Math.PI / 2 + i * Math.PI / fleeDirections.length;
		}

		this.fromRegistry = !this.getClass().equals(TankAIControlled.class);
	}

	public void initialize()
	{
		this.baseMaxSpeed = this.maxSpeed;
		this.dealsDamage = !this.isSupportTank();
		this.baseColor.set(this.color);
		this.idleTimer = (this.random.nextDouble() * turretIdleTimerRandom) + turretIdleTimerBase;

		if (this.targetEnemySightBehavior == TargetEnemySightBehavior.sidewind)
			this.strafeDirection /= 2;
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.backwind)
			this.strafeDirection *= 1.5;

		if (this.random.nextDouble() < 0.5)
			this.strafeDirection = -this.strafeDirection;

		if (this.sightTransformTankField != null)
			this.sightTransformTank = ((TankAIControlled) this.sightTransformTankField.resolve()).instantiate(this.name, 0, 0, 0);

		if (this.healthTransformTankField != null)
			this.healthTransformTank = ((TankAIControlled) this.healthTransformTankField.resolve()).instantiate(this.name, 0, 0, 0);
	}

	public void updateStart()
	{

	}

	public void updateRayStatus()
	{
		if (useRaysThisFrame)
			timeSinceRaysUsed = 0;

		useRaysThisFrame = false;
		timeSinceRaysUsed += Panel.frameFrequency;
		if (timeSinceRaysUsed > rayFrequency)
			useRaysThisFrame = true;
	}

	@Override
	public void update()
	{
		if (this.age <= 0)
			this.initialize();

		this.updateStart();
		this.updateRayStatus();

		this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);

		this.justTransformed = false;

		if (this.spawnedTankEntries.size() > 0 && !ScreenGame.finishedQuick && !this.destroy)
			this.updateSpawningAI();

		if (!this.destroy)
		{
			this.updateTarget();

			if (this.tookRecoil)
			{
				if (this.recoilSpeed <= this.maxSpeed * this.maxSpeedModifier * 1.0001)
				{
					this.tookRecoil = false;
					this.inControlOfMotion = true;
				}
				else
				{
					this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
					this.recoilSpeed *= Math.pow(1 - this.friction * this.frictionModifier, Panel.frameFrequency);
				}
			}
			else if (this.inControlOfMotion)
			{
				this.vX *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);
				this.vY *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);

				if (this.enableMovement)
					this.updateMotionAI();
				else
				{
					this.vX *= Math.pow(1 - (0.15 * this.frictionModifier), Panel.frameFrequency);
					this.vY *= Math.pow(1 - (0.15 * this.frictionModifier), Panel.frameFrequency);

					if (this.enableDefensiveFiring && useRaysThisFrame)
						this.checkForBulletThreats();
				}
			}

			if (!ScreenGame.finished)
			{
				this.updateTurretAI();
				this.updateMineAI();
			}

			if (this.enableSuicide)
				this.updateSuicideAI();

			if (this.chargeUp)
				this.checkCharge();

			if (this.transformMimic)
				this.updateMimic();

			if (this.healthTransformTank != null && this.health <= this.transformHealthThreshold && !ScreenGame.finishedQuick)
				this.handleHealthTransformation();

			this.postUpdate();
		}

		if (!this.tookRecoil)
		{
			this.vX += this.aX * maxSpeed * Panel.frameFrequency * this.accelerationModifier;
			this.vY += this.aY * maxSpeed * Panel.frameFrequency * this.accelerationModifier;

			double currentSpeed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

			if (currentSpeed > maxSpeed * maxSpeedModifier)
				this.setPolarMotion(this.getPolarDirection(), maxSpeed * maxSpeedModifier);
		}

		double reload = em().getAttributeValue(AttributeModifier.reload, 1);
		this.bulletItem.updateCooldown(reload);
		this.mineItem.updateCooldown(reload);
		super.update();
	}

	/** Prepare to fire a bullet*/
	public void shoot()
	{
		if (this.suicidal)
			return;

		this.cooldownIdleTime = 0;
		this.aimTimer = 10;

		if (!chargeUp)
		{
			this.aim = false;
			this.stoppedAiming = true;
		}

		Bullet b = this.getBullet();

		boolean arc = this.getBullet() instanceof BulletArc;

		if ((this.bulletItem.liveBullets < b.maxLiveBullets || b.maxLiveBullets <= 0) && !this.disabled && !this.destroy)
		{
			if (this.cooldown <= 0)
			{
				this.aim = false;
				this.stoppedAiming = true;

				double lifeRange = b.lifespan * b.speed * em().getAttributeValue(AttributeModifier.bullet_speed, 1);
				double limitRange = b.getRangeMax();
				double range = Math.min(limitRange, lifeRange);

				if (limitRange <= 0)
					range = lifeRange;
				else if (lifeRange <= 0)
					range = limitRange;

				if (arc && (this.distance <= range || range <= 0))
				{
					if (this.shotRoundCount <= 1)
						this.bulletItem.attemptUse(this);
					else
					{
						this.shootingInFan = true;
						this.shootTimer = -this.shootRoundTime / 2;
						this.shots = 0;
						this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
						this.fanOffset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((GameObject.distanceBetween(this, this.targetEnemy) / 1000.0), 2);
					}
				}
				else if (!arc)
				{
					double extra = b.size / 2;
					if (b instanceof BulletGas)
						extra = ((BulletGas) b).endSize / 2;

					if (this.targetEnemy instanceof Tank)
						extra += ((Tank) this.targetEnemy).size / 2;

					boolean inRange = (range <= 0) || (GameObject.distanceBetween(this, this.targetEnemy) <= range + extra);
					if (!inRange)
						return;

					double an = this.angle;

					if (this.targetEnemy != null && !(b instanceof BulletInstant) && this.enablePredictiveFiring && (this.shootAIType == ShootAI.straight || straightShoot))
						an = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

					Ray a2 = new Ray(this.posX, this.posY, an, b.bounces, this);
					a2.size = b.size;
					a2.getTarget();
					a2.ignoreDestructible = this.aimIgnoreDestructible;
					a2.ignoreShootThrough = true;

					double dist = a2.age;
					// Cancels if the bullet will hit another enemy
					double offset = (this.random.nextDouble() * this.aimAccuracyOffset - (this.aimAccuracyOffset / 2)) / Math.max((dist / 100.0), 2);

					if (this.disableOffset)
					{
						offset = 0;
						this.disableOffset = false;
					}

					if (this.shotRoundCount <= 1)
						this.finalCheckAndShoot(offset);
					else
						this.finalCheckAndShootFan(offset);
				}
			}
			else if (this.chargeUp)
			{
				this.charge();
			}
		}
	}

	public void charge()
	{
		double reload = em().getAttributeValue(AttributeModifier.reload, 1);

		this.cooldown -= Panel.frameFrequency * reload;
		this.justCharged = true;

		double frac = this.cooldown / this.lastCooldown;
		this.color.red = ((this.baseColor.red + 255) / 2) * (1 - frac) + frac * this.baseColor.red;
		this.color.green = ((this.baseColor.green + 255) / 2) * (1 - frac) + frac * this.baseColor.green;
		this.color.blue = ((this.baseColor.blue + 255) / 2) * (1 - frac) + frac * this.baseColor.blue;
		Game.eventsOut.add(new EventTankUpdateColor(this));
		Game.eventsOut.add(new EventTankCharge(this.networkID, frac));

		if (Math.random() * this.lastCooldown * Game.effectMultiplier > cooldown && Game.effectsEnabled)
		{
			Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.charge);

			double var = 50;
			e.colR = Math.min(255, Math.max(0, this.color.red + Math.random() * var - var / 2));
			e.colG = Math.min(255, Math.max(0, this.color.green + Math.random() * var - var / 2));
			e.colB = Math.min(255, Math.max(0, this.color.blue + Math.random() * var - var / 2));

			Game.effects.add(e);
		}
	}

	public void checkCharge()
	{
		if (!this.justCharged)
		{
			this.cooldown = Math.pow(1 - this.cooldownSpeedup, this.cooldownStacks) * (this.random.nextDouble() * this.cooldownRandom + this.cooldownBase);
			this.lastCooldown = this.cooldown;

			this.color.set(this.baseColor);
			Game.eventsOut.add(new EventTankUpdateColor(this));
		}

		this.justCharged = false;
	}

	public void finalCheckAndShoot(double offset)
	{
		Ray a = new Ray(this.posX, this.posY, this.angle + offset, this.getBullet().bounces, this, 2.5);
		a.size = this.getBullet().size;
		a.moveOut(this.size / 2.5);

		Movable m = a.getTarget();

		if (this.isSupportTank() || (!Team.isAllied(this, m) && this.isTargetSafe(a.posX, a.posY)))
		{
			this.shotOffset = offset;
			this.bulletItem.attemptUse(this);
		}
	}

	public void finalCheckAndShootFan(double offset)
	{
		boolean cancel = false;
		for (int i = 0; i < this.shotRoundCount; i++)
		{
			double offset2 = (i - ((this.shotRoundCount - 1) / 2.0)) / this.shotRoundCount * (this.shotRoundSpread * Math.PI / 180);

			Ray a = new Ray(this.posX, this.posY, this.angle + offset + offset2, this.getBullet().bounces, this, 2.5);
			a.size = this.getBullet().size;
			a.moveOut(this.size / 2.5);

			Movable m = a.getTarget();

			if (Team.isAllied(this, m))
			{
				cancel = true;
				break;
			}
		}

		if (!cancel)
		{
			this.shootingInFan = true;
			this.shootTimer = -this.shootRoundTime / 2;
			this.shots = 0;
			this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
			this.fanOffset = offset;
		}
	}

	public boolean isTargetSafe(double posX, double posY)
	{
		if (this.getBullet().hitExplosion != null)
		{
			for (Movable m2 : Game.movables)
			{
				if (Team.isAllied(m2, this) && m2 instanceof Tank && !((Tank) m2).resistExplosions && (this.team == null || this.team.friendlyFire) && Math.pow(m2.posX - posX, 2) + Math.pow(m2.posY - posY, 2) <= Math.pow(Mine.mine_size, 2))
					return false;
			}
		}

		return true;
	}

	/** Actually fire a bullet*/
	public void fireBullet(Bullet b, double speed, double offset)
	{
		if (this.shotSound != null)
			Drawing.drawing.playGlobalSound(this.shotSound, (float) (Bullet.bullet_size / b.size));

		if (speed == 0)
			speed = Double.MIN_VALUE;

		b.setPolarMotion(angle + offset + this.shotOffset, speed);
		this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * em().getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);
		b.speed = Math.abs(speed);

		if (b instanceof BulletArc || b instanceof BulletAirStrike)
			b.setTargetLocation(this.posX + this.distance * Math.cos(this.angle + offset + this.shotOffset), this.posY + this.distance * Math.sin(this.angle + offset + this.shotOffset));
			//b.vZ = this.distance / b.speed * 0.5 * BulletArc.gravity;
		else
			b.moveOut(Math.signum(speed) * 50 * this.size / Game.tile_size * this.turretLength / Game.tile_size);

		Game.movables.add(b);
		Game.eventsOut.add(new EventShootBullet(b));

		double r = (this.enableDefensiveFiring && this.avoidTimer > 0 && this.disableOffset && this.bulletThreatCount > 1) ? (1 - this.defensiveFiringUrgency) : 1;

		this.cooldown = Math.pow(1 - this.cooldownSpeedup, this.cooldownStacks) * (r * this.random.nextDouble() * this.cooldownRandom + this.cooldownBase);
		this.lastCooldown = this.cooldown;
		this.cooldownStacks++;

		if (this.shootAIType.equals(ShootAI.alternate))
			this.straightShoot = !this.straightShoot;
	}

	public void updateTarget()
	{
		if (this.transformMimic)
			if (this.updateTargetMimic())
				return;

		double nearestDist = Double.MAX_VALUE;
		Movable nearest = null;
		this.hasTarget = false;

		Bullet b = this.getBullet();

		boolean lowPriority = true;
		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			boolean correctTeam = (this.isSupportTank() && Team.isAllied(this, m)) || (!this.isSupportTank() && !Team.isAllied(this, m));
			if (m instanceof Tank && correctTeam && !((Tank) m).hidden && ((Tank) m).currentlyTargetable && m != this)
			{
				if (b.damage < 0 && ((Tank) m).health - ((Tank) m).baseHealth >= b.maxExtraHealth && b.maxExtraHealth > 0)
					continue;

				boolean lowP = ((b.damage == 0 && b.boosting) && (m instanceof TankAIControlled && !((TankAIControlled) m).enableMovement));
				if (!lowPriority && lowP)
					continue;

				double dist = GameObject.distanceBetween(this, m);
				if (dist < nearestDist || (lowPriority && !lowP))
				{
					this.hasTarget = true;
					nearestDist = dist;
					nearest = m;
					lowPriority = lowP;
				}
			}
		}

		if (this.targetEnemy != nearest)
			this.cooldownStacks = 0;

		this.targetEnemy = nearest;
	}

	public boolean updateTargetMimic()
	{
		double nearestDist = this.mimicRange;

		if (useRaysThisFrame)
		{
			Movable nearest = null;
			this.hasTarget = false;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (m instanceof Tank && !(m instanceof TankAIControlled && ((TankAIControlled) m).transformMimic) && (((Tank) m).getTopLevelPossessor() == null || !(((Tank) m).getTopLevelPossessor().getClass().equals(this.getClass())))
						&& ((Tank) m).currentlyTargetable && GameObject.distanceBetween(m, this) < nearestDist && ((Tank) m).size == this.size && !m.destroy)
				{
					Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(m.posX, m.posY), 0, this);
					r.moveOut(5);
					if (r.getTarget() != m)
						continue;

					double distance = GameObject.distanceBetween(this, m);

					if (distance < nearestDist)
					{
						this.hasTarget = true;
						nearestDist = distance;
						nearest = m;
					}
				}
			}

			this.mimicTarget = nearest;
			this.targetEnemy = nearest;
		}

		this.canCurrentlyMimic = this.mimicTarget != null;

		return this.mimicTarget != null;
	}

	public void updateMotionAI()
	{
		if ((this.enableBulletAvoidance || this.enableDefensiveFiring) && useRaysThisFrame)
			this.checkForBulletThreats();

		if (this.avoidTimer > 0 && this.enableBulletAvoidance)
		{
			this.avoidTimer -= Panel.frameFrequency;
			this.setPolarAcceleration(avoidDirection, acceleration * 2);
			this.overrideDirection = true;
		}
		else
		{
			fleeDirection = -fleeDirection;

			if (this.targetEnemy != null && this.seesTargetEnemy && this.enableTargetEnemyReaction && this.enableLookingAtTargetEnemy)
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

		if (this.suicidal || this.targetEnemySightBehavior == TargetEnemySightBehavior.approach)
			this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.flee)
			this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.strafe || this.targetEnemySightBehavior == TargetEnemySightBehavior.sidewind || this.targetEnemySightBehavior == TargetEnemySightBehavior.backwind)
		{
			if (this.random.nextDouble() < this.strafeDirectionChangeChance * Panel.frameFrequency)
				strafeDirection = -strafeDirection;

			this.setAccelerationInDirectionWithOffset(this.targetEnemy.posX, this.targetEnemy.posY, this.acceleration * 2, strafeDirection);
		}
		else if (this.targetEnemySightBehavior == TargetEnemySightBehavior.keep_distance)
		{
			if (GameObject.distanceBetween(this, this.targetEnemy) < this.targetSightDistance)
				this.setAccelerationAwayFromDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
			else
				this.setAccelerationInDirection(targetEnemy.posX, targetEnemy.posY, this.acceleration);
		}
	}

	public void handleSightTransformation()
	{
		if (this.justTransformed)
			return;

		this.transformRevertTimer = this.sightTransformRevertTime;
		this.willRevertTransformation = true;
		this.transform(this.sightTransformTank);
		Drawing.drawing.playGlobalSound("timer.ogg", 1.25f);
		Effect e1 = Effect.createNewEffect(this.posX, this.posY, this.posZ + this.sightTransformTank.size * 0.75, Effect.EffectType.exclamation);
		e1.size = this.sightTransformTank.size;
		e1.colR = this.color.red;
		e1.colG = this.color.green;
		e1.colB = this.color.blue;
		e1.glowR = this.sightTransformTank.color.red;
		e1.glowG = this.sightTransformTank.color.green;
		e1.glowB = this.sightTransformTank.color.blue;
		Game.effects.add(e1);
		Game.eventsOut.add(new EventTankTransformPreset(this, true, false));
	}

	public void handleHealthTransformation()
	{
		if (this.justTransformed)
			return;

		this.willRevertTransformation = false;
		Game.eventsOut.add(new EventTankTransformPreset(this, false, false));
		this.transform(this.healthTransformTank);
	}

	public void transform(TankAIControlled t)
	{
		this.justTransformed = true;
		this.transformTank = t;
		this.possessingTank = t;
		t.posX = this.posX;
		t.posY = this.posY;
		t.vX = this.vX;
		t.vY = this.vY;
		t.angle = this.angle;
		t.pitch = this.pitch;
		t.team = this.team;
		t.health = this.health;
		t.orientation = this.orientation;
		t.drawAge = this.drawAge;
		t.possessor = this;
		t.skipNextUpdate = true;
		t.setEffectManager(this.em());
		t.coinValue = this.coinValue;
		t.currentlyVisible = true;
		t.cooldown = Math.min(t.cooldownBase, this.cooldown);
		t.age = 0;

		Tank p = this;
		if (this.getTopLevelPossessor() != null)
		{
			p = this.getTopLevelPossessor();
		}

		if (p instanceof TankAIControlled && ((TankAIControlled) p).transformMimic)
		{
			t.baseModel = this.baseModel;
			t.turretModel = this.turretModel;
			t.turretBaseModel = this.turretBaseModel;
		}

		t.crusadeID = this.crusadeID;

		t.setNetworkID(this.networkID);

		Game.movables.add(t);
		Game.removeMovables.add(this);
	}

	public void updateIdleMotion()
	{
		if (useRaysThisFrame)
		{
			lastDistanceToWall = 1000;

			if (!this.overrideDirection && this.gentleTurnCooldown <= 0)
			{
				Ray d = new Ray(this.posX, this.posY, this.getPolarDirection(), 0, this, Game.tile_size);
				d.size = Game.tile_size * this.hitboxSize - 1;

				lastDistanceToWall = d.getDist();
			}
		}

		if (this.gentleTurnCooldown > 0)
			this.gentleTurnCooldown -= Panel.frameFrequency;

		boolean turn = this.random.nextDouble() < this.turnChance * Panel.frameFrequency || this.hasCollided;

		if (turn || lastDistanceToWall <= 50)
		{
			this.overrideDirection = false;

			double prevDirection = this.direction;

			ArrayList<Double> directions = new ArrayList<>();

			boolean[] validDirs = new boolean[8];
			validDirs[(int) (2 * ((this.direction + 0) % 4))] = true;
			validDirs[(int) (2 * ((this.direction + 0.5) % 4))] = true;
			validDirs[(int) (2 * ((this.direction + 3.5) % 4))] = true;
			validDirs[(int) (2 * ((this.direction + 1) % 4))] = true;
			validDirs[(int) (2 * ((this.direction + 3) % 4))] = true;

			if (!turn)
				this.gentleTurnCooldown = 50;

			for (double dir = 0; dir < 4; dir += 0.5)
			{
				Ray r = new Ray(this.posX, this.posY, dir * Math.PI / 2, 0, this, Game.tile_size);
				r.size = Game.tile_size * this.hitboxSize - 1;
				double dist = r.getDist() / Game.tile_size;

				distances[(int) (dir * 2)] = dist;

				if (validDirs[(int) (dir * 2)])
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


			if (this.direction != prevDirection && turn)
				this.motionPauseTimer = this.turnPauseTime;

			if (this.canHide && turn)
				this.motionPauseTimer += this.bushHideTime * (this.random.nextDouble() + 1);
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
			this.pathfind();
		}

		this.seekPause = Math.max(0, this.seekPause - Panel.frameFrequency);

		if (this.parent != null && !seesTargetEnemy && this.stayNearParent)
		{
			if (!this.parent.destroy && Math.sqrt(Math.pow(this.posX - this.parent.posX, 2) + Math.pow(this.posY - this.parent.posY, 2)) > this.maxDistanceFromParent)
			{
				this.overrideDirection = true;
				this.setAccelerationInDirection(this.parent.posX, this.parent.posY, this.acceleration);
			}
		}
	}

	public void pathfind()
	{
		long time = System.currentTimeMillis();

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
            if (o.posX < 0 || o.posX >= Game.currentSizeX * Game.tile_size || o.posY < 0 || o.posY >= Game.currentSizeY * Game.tile_size)
                continue;

            Tile.Type t = Tile.Type.solid;

            if (!o.tankCollision && !(o instanceof ObstacleTeleporter))
                t = Tile.Type.empty;
            else if (o.destructible && this.enableMineLaying)
                t = Tile.Type.destructible;

            int x = (int) (o.posX / Game.tile_size);
            int y = (int) (o.posY / Game.tile_size);
            Tile tile = tiles[x][y];
            tile.type = t;

            if (o instanceof ObstacleTeleporter)
                tile.teleporterID = ((ObstacleTeleporter) o).groupID;

            if (o.unfavorability >= 0)
                tile.unfavorability = o.unfavorability;
            else if (o.tankCollision)
            {
                tile.unfavorability = 10;

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

		Queue<Tile> queue = new LinkedList<>();

		Tile t = tiles[(int)(this.posX / Game.tile_size)][(int)(this.posY / Game.tile_size)];
		t.explored = true;
		queue.add(t);

		Tile current = null;
		boolean found = false;

		while (!queue.isEmpty())
		{
			current = queue.remove();

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
				this.mineItem.attemptUse(this);
				this.seekTimer = this.seekTimerBase * 2;
				this.seekPause = this.getMine().timer;
			}

			this.path.remove(0);
		}

		if (this.seekTimer < 0)
			this.currentlySeeking = false;
	}

	public void addIdleMotionOffset()
	{
		double offsetMotion = Math.sin(this.age * 0.02);
        double dist;
        if (offsetMotion < 0)
            dist = this.distances[(int) (this.direction * 2 + 6) % 8];
		else
            dist = this.distances[(int) (this.direction * 2 + 2) % 8];
        offsetMotion *= Math.min(1, (dist - 1) / 5.0) * this.acceleration;

        this.addPolarAcceleration((this.direction + 1) / 2 * Math.PI, offsetMotion);
	}

	public void checkForBulletThreats()
	{
		boolean avoid = false;

		ArrayList<Bullet> toAvoid = new ArrayList<>();
		ArrayList<Double> toAvoidDist = new ArrayList<>();
		ArrayList<Bullet> toAvoidDeflect = new ArrayList<>();
		ArrayList<Double> toAvoidDeflectDist = new ArrayList<>();
		ArrayList<Ray> toAvoidTargets = new ArrayList<>();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Bullet && !Game.movables.get(i).destroy)
			{
				Bullet b = (Bullet) Game.movables.get(i);
				double dist = GameObject.distanceBetween(this, b);

				double distBox = this.enableMovement ? 10 : 20;
				if (!(b.tank == this && b.age < 20) && !(this.team != null && Team.isAllied(b, this) && !this.team.friendlyFire)
						&& (b.damage > 0 || b.hitStun > 0 || b.freezing || b.hitExplosion != null) && Math.abs(b.posX - this.posX) < Game.tile_size * distBox && Math.abs(b.posY - this.posY) < Game.tile_size * distBox
						&& (b.getMotionInDirection(b.getAngleInDirection(this.posX, this.posY)) > 0 || dist < this.size * 3))
				{
					int c = enableMovement ? 1 : 0;
					for (int o = 0; o <= c; o++)
					{
						int mul = o == 1 ? 3 : 1;

						if (dist < this.size * mul)
						{
							avoid = true;

							if (o == 1)
							{
								toAvoid.add(b);
								toAvoidDist.add(dist);
								toAvoidTargets.add(b.getRay());
							}
							else
							{
								toAvoidDeflect.add(b);
								toAvoidDeflectDist.add(dist);
							}
						}
						else
						{
							Ray r = b.getRay();
							r.tankHitSizeMul = 3;
							double d = r.getTargetDist(mul, this);
							if (d >= 0)
							{
								avoid = true;

								if (o == 1)
								{
									toAvoid.add(b);
									toAvoidDist.add(d);
									toAvoidTargets.add(r);
								}
								else
								{
									toAvoidDeflect.add(b);
									toAvoidDeflectDist.add(dist);
								}
							}
						}
					}
				}
			}
		}

		this.bulletThreatCount = toAvoidDeflect.size();

		if (avoid)
		{
			Bullet nearest = null;
			Ray nearestTarget = null;
			double nearestDist = Double.MAX_VALUE;

			for (int i = 0; i < toAvoid.size(); i++)
			{
				Bullet b = toAvoid.get(i);
				double dist = toAvoidDist.get(i) / b.getSpeed();
				if (dist < nearestDist)
				{
					nearest = b;
					nearestTarget = toAvoidTargets.get(i);
					nearestDist = dist;
				}
			}

			Bullet nearestDeflectable = null;
			double nearestDeflectableDist = Double.MAX_VALUE;
			for (int i = 0; i < toAvoidDeflect.size(); i++)
			{
				Bullet b = toAvoidDeflect.get(i);
				double dist = toAvoidDeflectDist.get(i) / b.getSpeed();
				if (!b.heavy && b.canBeCanceled && (!Team.isAllied(this, b) || !this.enableMovement) && dist < nearestDeflectableDist)
				{
					nearestDeflectable = b;
					nearestDeflectableDist = dist;
				}
			}

			this.nearestBulletDeflect = nearestDeflectable;
			this.nearestBulletDeflectDist = nearestDeflectableDist;

			if (nearestDeflectable != null || nearest != null)
				this.avoidTimer = this.bulletAvoidTimerBase;

			if (nearest == null)
				return;

			double direction = nearest.getPolarDirection();
			double distance = GameObject.distanceBetween(this, nearest);
			double diff = GameObject.angleBetween(direction, this.getAngleInDirection(nearest.posX, nearest.posY));

			if (this.enableMovement)
			{
				double m = distance / nearest.getSpeed() * this.maxSpeed;
				if (m > Game.tile_size * 4 && avoidanceSeekOpenSpaces)
				{
					int count = fleeDistances.length;
					double[] d = fleeDistances;

					for (int dir = 0; dir < count; dir++)
					{
						Ray r = new Ray(this.posX, this.posY, direction + fleeDirections[dir], 0, this, Game.tile_size);
						r.size = Game.tile_size * this.hitboxSize - 1;

						boolean b = this.targetEnemy != null && this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge && GameObject.absoluteAngleBetween(fleeDirections[dir] + direction, this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY)) > Math.PI * 0.5;

						double dist = r.getDist();
						d[dir] = dist;

						if (b)
							d[dir] = Math.min(d[dir] - Game.tile_size, Game.tile_size * 3);
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

					if (gValue < Game.tile_size * 4)
						this.avoidDirection = direction + fleeDirections[greatest];
					else if (this.avoidTimer <= 0)
					{
						// randomly pick one >= 3 tiles
						while (true)
						{
							int c = (int) (this.random.nextDouble() * count);
							if (d[c] >= Game.tile_size * 4)
							{
								this.avoidDirection = direction + fleeDirections[greatest];
								break;
							}
						}
					}
				}
				else
				{
					double frac = Math.max(0, 2 - Math.max(m / (Game.tile_size * 2), 1));

					if (this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge || this.bulletAvoidBehvavior == BulletAvoidBehavior.dodge)
					{
						double invert = 1;

						if (this.bulletAvoidBehvavior == BulletAvoidBehavior.aggressive_dodge)
							invert = -1;

						this.avoidDirection = direction + Math.PI * 0.5 * (1 - (1 - frac) * invert / 2) * Math.signum(diff);
					}
					else if (this.bulletAvoidBehvavior == BulletAvoidBehavior.back_off)
						this.avoidDirection = nearest.getAngleInDirection(this.posX, this.posY);
					else if (this.bulletAvoidBehvavior == BulletAvoidBehavior.back_off_dodge)
					{
						double a = nearest.getAngleInDirection(this.posX, this.posY);
						Ray r = new Ray(this.posX, this.posY, a, 0, this, Game.tile_size);
						r.size = Game.tile_size * this.hitboxSize - 1;
						double d = r.getDist();

						if (d < Game.tile_size * 2)
							this.avoidDirection = direction + Math.PI * 0.5 * (1 - (1 - frac) * -1 / 2) * Math.signum(diff);
						else
							this.avoidDirection = a;
					}
					else if (this.bulletAvoidBehvavior == BulletAvoidBehavior.intersect)
					{
						double targetX = nearestTarget.targetX;
						double targetY = nearestTarget.targetY;

						this.avoidDirection = this.getAngleInDirection(targetX, targetY) + Math.PI;
						diff = GameObject.angleBetween(this.avoidDirection, direction);

						if (Math.abs(diff) < Math.PI / 4)
							this.avoidDirection = direction + Math.signum(diff) * Math.PI / 4;

						Ray r = new Ray(this.posX, this.posY, this.avoidDirection, 0, this, Game.tile_size);
						r.size = Game.tile_size * this.hitboxSize - 1;
						double d = r.getDist();

						if (d < Game.tile_size * 2)
							this.avoidDirection = direction - diff;
					}
				}
			}

			this.nearestBullet = nearest;
			this.nearestBulletDist = nearestDist;
		}
	}

	public void updateTurretAI()
	{
		if (this.shootingInFan)
		{
			this.updateTurretFan();
			return;
		}

		Bullet b = this.getBullet();

		if ((this.enableLookingAtTargetEnemy || this.straightShoot || this.sightTransformTank != null) && this.useRaysThisFrame)
			this.lookAtTargetEnemy();

		if (b instanceof BulletArc)
		{
			this.setAimAngleArc();
			this.updateTurretStraight();
		}
		else if (b instanceof BulletAirStrike)
		{
			this.setAimAngleAirStrike();
			this.updateTurretStraight();
		}
		else
		{
			if (this.shootAIType.equals(ShootAI.homing))
				this.straightShoot = this.seesTargetEnemy;

			if (this.shootAIType.equals(ShootAI.none))
				this.angle = this.orientation;
			else if (this.shootAIType.equals(ShootAI.wander) || this.shootAIType.equals(ShootAI.sprinkler))
				this.updateTurretWander();
			else if (this.shootAIType.equals(ShootAI.straight) || this.straightShoot)
				this.updateTurretStraight();
			else
				this.updateTurretReflect();
		}

		if (!(b instanceof BulletArc || b instanceof BulletAirStrike))
			this.pitch -= GameObject.angleBetween(this.pitch, 0) / 10 * Panel.frameFrequency;

		if (!this.chargeUp)
		{
			double reload = em().getAttributeValue(AttributeModifier.reload, 1);
			this.cooldown -= Panel.frameFrequency * reload;
		}

		this.cooldownIdleTime += Panel.frameFrequency;

		if (this.cooldownIdleTime >= this.cooldownRevertTime)
			this.cooldownStacks = 0;
	}

	public void updateTurretFan()
	{
		if (this.shootTimer <= -this.shootRoundTime / 2 && this.targetEnemy != null)
		{
			double a = this.aimAngle;

			if (this.shootAIType == ShootAI.sprinkler)
				this.aimAngle = this.angle;

			double originalAimAngle = this.aimAngle;
			this.aimAngle = this.fanOffset + a;

			double speed = this.turretAimSpeed;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * Panel.frameFrequency)
			{
				if (GameObject.angleBetween(this.angle, this.aimAngle) < 0)
					this.angle += speed * Panel.frameFrequency;
				else
					this.angle -= speed * Panel.frameFrequency;

				this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
			}
			else
			{
				this.angle = this.aimAngle;
				this.shootTimer += Panel.frameFrequency;
			}

			this.aimAngle = originalAimAngle;
		}
		else
		{
			this.angle = this.aimAngle + this.fanDirection * (this.shotRoundSpread * Math.PI / 180) * (Math.abs(this.shootTimer / this.shootRoundTime) - 0.5);

			int s = (int) Math.round(this.shootTimer * this.shotRoundCount / this.shootRoundTime);
			if (this.shots < s)
			{
				this.bulletItem.attemptUse(this);
				this.shots = s;
			}

			if (this.shootTimer > this.shootRoundTime)
			{
				this.shootingInFan = false;
			}

			this.shootTimer += Panel.frameFrequency;
		}
	}

	public void updateTurretWander()
	{
		Bullet b = this.getBullet();
		if (useRaysThisFrame)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, b.bounces, this);
			a.moveOut(this.size / 10);
			a.size = b.size;
			a.ignoreDestructible = this.aimIgnoreDestructible;
			a.ignoreShootThrough = true;

			Movable m = a.getTarget();

			if (this.shootAIType == ShootAI.sprinkler)
			{
				if (this.cooldown <= 0)
				{
					if (this.shotRoundCount <= 1)
						this.bulletItem.attemptUse(this);
					else
					{
						this.shootingInFan = true;
						this.shootTimer = -this.shootRoundTime / 2;
						this.shots = 0;
						this.fanDirection = this.random.nextDouble() < 0.5 ? 1 : -1;
					}
				}
			}
			else
			{
				if (!(m == null))
					if (!Team.isAllied(m, this) && m instanceof Tank && !((Tank) m).hidden)
						this.shoot();
			}
		}

		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * Panel.frameFrequency;
		else
			this.angle -= this.turretIdleSpeed * Panel.frameFrequency;

		this.idleTimer -= Panel.frameFrequency;

		if (idleTimer <= 0)
		{
			this.idleTimer = this.random.nextDouble() * turretIdleTimerRandom + turretIdleTimerBase;
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public void updateTurretStraight()
	{
		Bullet b = this.getBullet();
		if (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy && (this.enableMovement || this.nearestBulletDeflectDist <= this.bulletThreatCount * Math.max(Math.max(this.cooldownBase, this.bulletItem.item.cooldownBase), 50) * 1.5))
		{
			if (b instanceof BulletInstant)
				this.aimAngle = this.getAngleInDirection(nearestBulletDeflect.posX, nearestBulletDeflect.posY);
			else
			{
				double a = this.nearestBulletDeflect.getAngleInDirection(this.posX + Game.tile_size / b.speed * this.nearestBulletDeflect.vX, this.posY + Game.tile_size / b.speed * this.nearestBulletDeflect.vY);
				double speed = this.nearestBulletDeflect.getLastMotionInDirection(a + Math.PI / 2);

				if (speed < b.speed)
				{
					double d = this.getAngleInDirection(nearestBulletDeflect.posX, nearestBulletDeflect.posY) - Math.asin(speed / b.speed);

					if (!Double.isNaN(d))
						this.aimAngle = d;
				}
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

		if (!this.hasTarget || this.targetEnemy == null)
			return;

		if (b instanceof BulletArc)
		{
			double pitch = Math.atan(this.distance / b.speed * 0.5 * BulletArc.gravity / b.speed);
			this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
		}
		else if (b instanceof BulletAirStrike)
		{
			double pitch = Math.PI / 2;
			this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
		}

		this.checkAndShoot();

		double speed = this.turretAimSpeed;

		if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
			speed /= 2;

		if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
			speed /= 2;

		if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
			speed /= 2;

		if (GameObject.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * Panel.frameFrequency)
		{
			if (GameObject.angleBetween(this.angle, this.aimAngle) < 0)
				this.angle += speed * Panel.frameFrequency;
			else
				this.angle -= speed * Panel.frameFrequency;

			this.angle = (this.angle + Math.PI * 2) % (Math.PI * 2);
		}
		else
			this.angle = this.aimAngle;
	}

	public void setAimAngleStraight()
	{
		Bullet b = this.getBullet();
		if (this.enablePredictiveFiring && !(b instanceof BulletInstant) && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			if (useRaysThisFrame)
			{
				Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
				r.ignoreDestructible = this.aimIgnoreDestructible;
				r.ignoreShootThrough = true;
				r.size = Game.tile_size * this.hitboxSize - 1;
				r.enableBounciness = false;
				this.disableOffset = false;

				targetEnemyPredictedCollisionDist = r.getDist();

				targetEnemyPredictedCollisionX = r.posX;
				targetEnemyPredictedCollisionY = r.posY;
			}

			double distBtwn = GameObject.distanceBetween(this, this.targetEnemy);
			double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
			double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);
			double time = distBtwn / Math.sqrt(b.speed * b.speed - speed * speed);
			double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

			double d = targetEnemyPredictedCollisionDist;
			if (d * d > distSq && speed < b.speed)
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / b.speed);
			else
				this.aimAngle = this.getAngleInDirection(targetEnemyPredictedCollisionX, targetEnemyPredictedCollisionY);
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.disableOffset = false;
		}
	}

	public void setAimAngleArc()
	{
		if (this.targetEnemy == null)
			return;

		Bullet b = this.getBullet();

		if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			if (useRaysThisFrame)
			{
				Ray r = new Ray(targetEnemy.posX, targetEnemy.posY, targetEnemy.getLastPolarDirection(), 0, (Tank) targetEnemy);
				r.size = Game.tile_size * this.hitboxSize - 1;
				r.enableBounciness = false;
				this.disableOffset = false;
				targetEnemyPredictedCollisionDist = r.getDist();
				targetEnemyPredictedCollisionX = r.posX;
				targetEnemyPredictedCollisionY = r.posY;
			}

			double a = this.targetEnemy.getAngleInDirection(this.posX, this.posY);
			double speed = this.targetEnemy.getLastMotionInDirection(a + Math.PI / 2);

			double distBtwn = GameObject.distanceBetween(this, this.targetEnemy);
			double time = distBtwn / Math.sqrt(b.speed * b.speed - speed * speed);

			double distSq = Math.pow(targetEnemy.lastFinalVX * time, 2) + Math.pow(targetEnemy.lastFinalVY * time, 2);

			double d = this.targetEnemyPredictedCollisionDist;
			if (d * d > distSq && speed < b.speed)
			{
				this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY) - Math.asin(speed / b.speed);

				double c = Math.cos(GameObject.absoluteAngleBetween(targetEnemy.getLastPolarDirection(), this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY)));

				double a1 = Math.pow(b.speed, 2) - Math.pow(targetEnemy.getLastSpeed(), 2);
				double b1 = -2 * targetEnemy.getLastSpeed() * GameObject.distanceBetween(this, this.targetEnemy) * c;
				double c1 = -Math.pow(GameObject.distanceBetween(this, targetEnemy), 2);
				double t = (-b1 + Math.sqrt(b1 * b1 - 4 * a1 * c1)) / (2 * a1);

				this.distance = Math.sqrt(Math.pow(targetEnemy.posX + t * targetEnemy.lastFinalVX - this.posX, 2) + Math.pow(targetEnemy.posY + t * targetEnemy.lastFinalVY - this.posY, 2));
			}
			else
			{
				this.aimAngle = this.getAngleInDirection(targetEnemyPredictedCollisionX, targetEnemyPredictedCollisionY);
				this.distance = Math.sqrt(Math.pow(targetEnemyPredictedCollisionX - this.posX, 2) + Math.pow(targetEnemyPredictedCollisionY - this.posY, 2));
			}
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.distance = Math.sqrt(Math.pow(targetEnemy.posX - this.posX, 2) + Math.pow(targetEnemy.posY - this.posY, 2));

			this.disableOffset = false;
		}
	}

	public void setAimAngleAirStrike()
	{
		if (this.targetEnemy == null)
			return;

		Bullet b = this.getBullet();

		if (this.enablePredictiveFiring && this.targetEnemy instanceof Tank && (this.targetEnemy.vX != 0 || this.targetEnemy.vY != 0))
		{
			double t1 = (-0.1 + Math.sqrt(0.01 + 2 * b.speed / 31.25 * 1100)) / (b.speed / 31.25);
			double t2 = (-0.1 + Math.sqrt(0.01 + 4 * b.speed / 31.25 * 1100)) / (2 * b.speed / 31.25);
			double x = this.targetEnemy.posX + (t1 + t2) * this.targetEnemy.vX;
			double y = this.targetEnemy.posY + (t1 + t2) * this.targetEnemy.vY;
			this.aimAngle = this.getAngleInDirection(x, y);
			this.distance = Math.sqrt(Math.pow(x - this.posX, 2) + Math.pow(y - this.posY, 2));
		}
		else
		{
			this.aimAngle = this.getAngleInDirection(targetEnemy.posX, targetEnemy.posY);
			this.distance = Math.sqrt(Math.pow(targetEnemy.posX - this.posX, 2) + Math.pow(targetEnemy.posY - this.posY, 2));
		}

		this.disableOffset = false;
	}


	public void checkAndShoot()
	{
		Movable m = null;

		Bullet b = this.getBullet();
		boolean arc = b instanceof BulletArc || b instanceof BulletAirStrike;

		if (this.targetEnemy != null && !arc)
		{
			Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);
			r.moveOut(this.size / 10);
			r.size = b.size;
			r.ignoreDestructible = this.aimIgnoreDestructible;
			r.ignoreShootThrough = true;
			m = r.getTarget();
		}

		if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) <= this.aimThreshold)
			if ((arc && this.targetEnemy != null) || (m != null && m.equals(this.targetEnemy) || (this.avoidTimer > 0 && this.disableOffset && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy)))
				this.shoot();
	}

	public void updateTurretReflect()
	{
		Bullet b = this.getBullet();
		if (this.seesTargetEnemy && this.targetEnemy != null && GameObject.distanceBetween(this, this.targetEnemy) <= Game.tile_size * 6 && !chargeUp)
		{
			aim = true;
			this.aimAngle = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);
		}

		this.search();

		if (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy && (this.enableMovement || this.nearestBulletDeflectDist <= this.bulletThreatCount * Math.max(Math.max(this.cooldownBase, this.bulletItem.item.cooldownBase), 50) * 1.5))
		{
			if (b instanceof BulletInstant)
			{
				this.aimAngle = this.getAngleInDirection(this.nearestBullet.posX, this.nearestBullet.posY);
				this.aim = true;
			}
			else
			{
				double a = this.nearestBulletDeflect.getAngleInDirection(this.posX + Game.tile_size / b.speed * this.nearestBulletDeflect.vX, this.posY + Game.tile_size / b.speed * this.nearestBulletDeflect.vY);
				double speed = this.nearestBulletDeflect.getLastMotionInDirection(a + Math.PI / 2);

				if (speed < b.speed)
				{
					double d = this.getAngleInDirection(nearestBulletDeflect.posX, nearestBulletDeflect.posY) - Math.asin(speed / b.speed);

					if (!Double.isNaN(d))
					{
						this.aimAngle = d;
						this.aim = true;
					}
				}
			}

			this.disableOffset = true;
		}

		if (aim && (this.hasTarget || (this.avoidTimer > 0 && this.enableDefensiveFiring && this.nearestBulletDeflect != null && !this.nearestBulletDeflect.destroy)))
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
		else if (this.searchPhase == RotationPhase.counter_clockwise)
		{
			searchAngle -= this.random.nextDouble() * 0.1 * Panel.frameFrequency;
		}
		else if (this.searchPhase == RotationPhase.aiming)
		{
			searchAngle = this.lockedAngle;
			if (this.aimTimer <= 0)
			{
				this.aimTimer = 0;
				if (this.random.nextDouble() < 0.5)
					this.searchPhase = RotationPhase.clockwise;
				else
					this.searchPhase = RotationPhase.counter_clockwise;
			}
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
					this.searchPhase = RotationPhase.counter_clockwise;
			}
		}

		if (this.useRaysThisFrame)
		{
			this.testSearch(this.searchAngle);

			if (this.searchPhase != RotationPhase.aiming)
				this.testSearch(this.angle);
		}
	}

	public void testSearch(double searchAngle)
	{
		Bullet b = this.getBullet();
		Ray ray = new Ray(this.posX, this.posY, searchAngle, b.bounces, this);
		ray.moveOut(this.size / 10);
		ray.size = b.size;
		ray.ignoreDestructible = this.aimIgnoreDestructible;
		ray.ignoreShootThrough = true;

		Movable target = ray.getTarget();
		double dist = ray.getDist();
		double bulletRange = b.lifespan * b.speed;
		boolean inRange = bulletRange == 0 || dist <= bulletRange;

		if (target == null && this.shootAIType == ShootAI.homing && this.targetEnemy != null && inRange)
		{
			Ray ray2 = new Ray(ray.posX, ray.posY, ray.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);
			ray2.moveOut(this.size / 50);
			ray2.size = b.size;
			ray2.ignoreDestructible = this.aimIgnoreDestructible;
			ray2.ignoreShootThrough = true;

			target = ray2.getTarget();
		}

		if (inRange && (target != null && !(target instanceof TankNPC) && target.equals(this.targetEnemy) || (target instanceof Tank && !((Tank) target).hidden && !Team.isAllied(target, this))))
		{
			this.targetEnemy = target;
			this.lockedAngle = searchAngle;
			this.searchPhase = RotationPhase.aiming;
			this.aim = true;
			this.aimAngle = searchAngle % (Math.PI * 2);
		}
		else if (this.searchPhase == RotationPhase.aiming)
			this.searchPhase = RotationPhase.recalculating;
	}

	public void lookAtTargetEnemy()
	{
		if (!this.hasTarget || this.targetEnemy == null)
			return;

		double a;

		a = this.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY);

		Ray rayToTarget = new Ray(this.posX, this.posY, a, 0, this);
		rayToTarget.size = this.getBullet().size;
		rayToTarget.moveOut(this.size / 10);
		rayToTarget.ignoreDestructible = this.aimIgnoreDestructible;
		rayToTarget.ignoreShootThrough = true;
		Movable target = rayToTarget.getTarget();

		if (target != null)
			this.seesTargetEnemy = target.equals(this.targetEnemy);
		else
			this.seesTargetEnemy = false;

		if (this.straightShoot)
		{
			if (target != null)
			{
				if (target.equals(this.targetEnemy))
					this.aimAngle = a;
				else
					this.straightShoot = false;
			}
			else
				this.straightShoot = false;
		}

		if (this.sightTransformTank != null && seesTargetEnemy && this.inControlOfMotion && !ScreenGame.finishedQuick)
			this.handleSightTransformation();
	}

	public void updateAimingTurret()
	{
		if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.turretAimSpeed * Panel.frameFrequency)
		{
			this.angle = this.aimAngle;
			this.shoot();
		}
		else
		{
			if (this.chargeUp)
				this.charge();

			double speed = this.turretAimSpeed;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 4)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 3)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.angle, this.aimAngle) < this.aimThreshold * 2)
				speed /= 2;

			if (GameObject.absoluteAngleBetween(this.aimAngle, this.angle) > this.turretAimSpeed * Panel.frameFrequency)
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
		if (this.stoppedAiming && !useRaysThisFrame)
			return; // prevents jittering

		this.stoppedAiming = false;

		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * Panel.frameFrequency;
		else
			this.angle -= this.turretIdleSpeed * Panel.frameFrequency;

		this.idleTimer -= Panel.frameFrequency;

		if (this.idleTimer <= 0)
		{
			if (this.idlePhase == RotationPhase.clockwise)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;

			this.idleTimer = (this.random.nextDouble() * this.turretIdleTimerRandom) + this.turretIdleTimerBase;
		}
	}

	public void updateSeekingTurret()
	{
		if (this.idlePhase == RotationPhase.clockwise)
			this.angle += this.turretIdleSpeed * Panel.frameFrequency;
		else
			this.angle -= this.turretIdleSpeed * Panel.frameFrequency;

		double dir = this.getPolarDirection();
		if (GameObject.absoluteAngleBetween(dir, this.angle) > Math.PI / 8)
		{
			if (GameObject.angleBetween(dir, this.angle) < 0)
				this.idlePhase = RotationPhase.counter_clockwise;
			else
				this.idlePhase = RotationPhase.clockwise;
		}
	}

	public boolean isInterestingPathTarget(Movable m)
	{
		Bullet b = this.getBullet();

		if (this.transformMimic)
			return m instanceof Tank && !(m.getClass().equals(this.getClass())) && ((Tank) m).size == this.size;
		else if (this.isSupportTank())
			return m instanceof Tank && Team.isAllied(m, this) && m != this
					&& (((Tank) m).health - ((Tank) m).baseHealth < b.maxExtraHealth || b.damage >= 0 || b.maxExtraHealth <= 0)
					&& !(m.getClass().equals(this.getClass()));
		else
			return m instanceof Tank && !Team.isAllied(m, this)
					&& m.posX >= 0 && m.posX / Game.tile_size < Game.currentSizeX
					&& m.posY >= 0 && m.posY / Game.tile_size < Game.currentSizeY;
	}

	public void updateMineAI()
	{
		double worstSeverity = Double.MAX_VALUE;

		if (this.mineTimer == -1)
			this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		Object nearest = null;

		if (!laidMine && mineFleeTimer <= 0)
		{
			for (IAvoidObject o: Game.avoidObjects)
			{
				if (o instanceof Movable && (Team.isAllied(this, (Movable) o) && (this.team != null && !this.team.friendlyFire)))
					continue;

				double distSq;

				if (o instanceof Movable)
					distSq = Math.pow(((Movable) o).posX - this.posX, 2) + Math.pow(((Movable) o).posY - this.posY, 2);
				else
					distSq = Math.pow(((Obstacle) o).posX - this.posX, 2) + Math.pow(((Obstacle) o).posY - this.posY, 2);

				if (distSq <= Math.pow(o.getRadius() * this.mineAvoidSensitivity, 2))
				{
					double d = o.getSeverity(this.posX, this.posY);

					if (d < worstSeverity)
					{
						worstSeverity = d;
						nearest = o;
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
				if (nearest instanceof Movable)
					this.setAccelerationAwayFromDirection(((Movable) nearest).posX, ((Movable) nearest).posY, acceleration);
				else
					this.setAccelerationAwayFromDirection(((Obstacle) nearest).posX, ((Obstacle) nearest).posY, acceleration);

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
					this.mineItem.attemptUse(this);
				}
			}

			if (!this.currentlySeeking)
				this.mineTimer = Math.max(0, this.mineTimer - Panel.frameFrequency);
		}

		if (worstSeverity <= 1 && this.mineFleeTimer <= 0 && this.enableMovement)
		{
			this.overrideDirection = true;
			this.setPolarAcceleration(this.random.nextDouble() * 2 * Math.PI, acceleration);
		}
	}

	public void layMine(Mine m)
	{
		Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

		Game.eventsOut.add(new EventLayMine(m));
		Game.movables.add(m);
		this.mineTimer = (this.random.nextDouble() * mineTimerRandom + mineTimerBase);

		int count = fleeDistances.length;
		double[] d = fleeDistances;
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

		if (this.enableMovement) // Otherwise stationary tanks will take off when they lay mines :P
		{
			this.setPolarAcceleration(greatest * 2.0 / count * Math.PI, acceleration);
			this.overrideDirection = true;
		}

		laidMine = true;
	}

	public void updateSpawningAI()
	{
		if (this.age <= 0 && !this.readyForInitialSpawn)
			this.readyForInitialSpawn = true;
		else if (this.readyForInitialSpawn && !this.destroy && !ScreenGame.finishedQuick)
		{
			this.readyForInitialSpawn = false;
			for (int i = 0; i < this.spawnedInitialCount; i++)
                spawnTank();
		}

		if (this.random.nextDouble() < this.spawnChance * Panel.frameFrequency && this.spawnedTanks.size() < this.spawnedMaxCount && !this.destroy && !ScreenGame.finishedQuick)
			spawnTank();

		for (int i = 0; i < this.spawnedTanks.size(); i++)
		{
			if (this.spawnedTanks.get(i).destroy)
			{
				this.spawnedTanks.remove(i);
				i--;
			}
		}
	}

	public void spawnTank()
	{
		try
		{
			double x;
			double y;

			int attempts = 0;
			while (true)
			{
				attempts++;

				double pos = (this.random.nextDouble() - 0.5) * (this.size + Game.tile_size);
				int side = (int) (this.random.nextDouble() * 4);

				x = pos;
				y = pos;

				if (side == 0)
					x = -(this.size / 2 + Game.tile_size / 2);
				else if (side == 1)
					x = (this.size / 2 + Game.tile_size / 2);
				else if (side == 2)
					y = -(this.size / 2 + Game.tile_size / 2);
				else if (side == 3)
					y = (this.size / 2 + Game.tile_size / 2);

				boolean retry = false;
				if (this.posX + x > Game.tile_size / 2 && this.posX + x < (Game.currentSizeX - 0.5) * Game.tile_size &&
						this.posY + y > Game.tile_size / 2 && this.posY + y < (Game.currentSizeY - 0.5) * Game.tile_size)
				{
					for (Obstacle o : Game.obstacles)
					{
						if (o.tankCollision && Math.abs(o.posX - (this.posX + x)) < Game.tile_size && Math.abs(o.posY - (this.posY + y)) < Game.tile_size)
						{
							retry = true;
							break;
						}
					}
				}
				else
					retry = true;

				if (!retry || attempts >= 10)
					break;
			}

			Tank t;

			Tank t2 = null;

			double totalWeight = 0;
			for (SpawnedTankEntry s: this.spawnedTankEntries)
			{
				totalWeight += s.weight;
			}
			double selected = this.random.nextDouble() * totalWeight;

			for (SpawnedTankEntry s: this.spawnedTankEntries)
			{
				selected -= s.weight;

				if (selected <= 0)
				{
					if (s.resolvedTank == null)
						s.resolvedTank = s.tank.resolve();

					if (s.resolvedTank.getClass().equals(TankAIControlled.class))
					{
						t2 = new TankAIControlled("", this.posX + x, this.posY + y, 0, 0, 0, 0, this.angle, ShootAI.none);
						((TankAIControlled)(s.resolvedTank)).cloneProperties((TankAIControlled) t2);
					}
					else
					{
						t2 = Game.registryTank.getEntry(s.resolvedTank.name).getTank(this.posX + x, this.posY + y, 0);
					}

					break;
				}
			}

			t = t2;

			t.team = this.team;
			t.crusadeID = this.crusadeID;
			t.parent = this;
			t.readyForInitialSpawn = false;

			this.spawnedTanks.add(t);

			Game.spawnTank(t, this);
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public void updateSuicideAI()
	{
		if (!this.suicidal)
		{
			boolean die = true;
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m != this && m.team == this.team && m.dealsDamage && !m.destroy)
				{
					die = false;
					break;
				}
			}

			if (die)
			{
				this.suicidal = true;
				this.timeUntilDeath = this.random.nextDouble() * this.suicideTimerRandom + this.suicideTimerBase;
			}

			return;
		}

		double frac = Math.min(this.timeUntilDeath / this.suicideTimerBase, 1);

		if (!this.disabled)
		{
			this.timeUntilDeath -= Panel.frameFrequency;
			this.maxSpeed = this.baseMaxSpeed + this.suicideSpeedBoost * (1 - frac);
			this.enableBulletAvoidance = false;
			this.enableMineAvoidance = false;
		}

		if (this.timeUntilDeath < this.suicideTimerBase)
		{
			this.color.set(frac * this.baseColor.red + (1 - frac) * 255, frac * this.baseColor.green, frac * this.baseColor.blue);

			if (this.timeUntilDeath < 150 && ((int) this.timeUntilDeath % 16) / 8 == 1)
			{
				this.color.set(255, 255, 0);
			}

			Game.eventsOut.add(new EventTankUpdateColor(this));
		}

		if (this.timeUntilDeath <= 0)
		{
			if (this.suicideExplosion != null)
			{
				Explosion e = new Explosion(this.posX, this.posY, this, this.mineItem);
				this.suicideExplosion.clonePropertiesTo(e);
				e.explode();
			}

			this.destroy = true;
			this.health = 0;
		}
	}

	@Override
	public void updatePossessing()
	{
		this.justTransformed = false;

		this.bulletItem.updateCooldown(1);
		this.mineItem.updateCooldown(1);
		this.cooldownIdleTime += Panel.frameFrequency;
		this.updateRayStatus();

		if (this.transformMimic)
			this.updatePossessingMimic();
		else
			this.updatePossessingTransform();
	}

	public void updatePossessingTransform()
	{
		if (this.transformTank.destroy)
			this.destroy = true;

		if (this.transformTank.destroy || this.destroy || ScreenGame.finishedQuick || this.positionLock || !this.willRevertTransformation || this.justTransformed)
			return;

		Movable m = null;

		this.posX = this.transformTank.posX;
		this.posY = this.transformTank.posY;
		this.vX = this.transformTank.vX;
		this.vY = this.transformTank.vY;
		this.angle = this.transformTank.angle;

		this.updateTarget();

		if (useRaysThisFrame)
		{
			if (this.transformTank.targetEnemy != null)
			{
				this.targetEnemy = this.transformTank.targetEnemy;
				Ray r = new Ray(this.transformTank.posX, this.transformTank.posY, this.transformTank.getAngleInDirection(this.targetEnemy.posX, this.targetEnemy.posY), 0, this);

				r.moveOut(5);

				m = r.getTarget();
			}

			if (this.targetEnemy == null || m != this.targetEnemy || this.targetEnemy.destroy)
				this.transformRevertTimer -= timeSinceRaysUsed;
			else
				this.transformRevertTimer = this.sightTransformRevertTime;
		}

		if (this.transformRevertTimer <= 0 && this.currentlyTargetable)
		{
			Game.removeMovables.add(this.sightTransformTank);
			Tank.idMap.put(this.networkID, this);
			this.health = this.sightTransformTank.health;
			this.orientation = this.sightTransformTank.orientation;
			this.pitch = this.sightTransformTank.pitch;
			this.drawAge = this.sightTransformTank.drawAge;
			this.setEffectManager(this.sightTransformTank.em());
			this.possessingTank = null;
			this.currentlyVisible = true;
			this.targetEnemy = null;
			this.cooldown = Math.min(this.cooldownBase, this.sightTransformTank.cooldown);
			Drawing.drawing.playGlobalSound("slowdown.ogg", 0.75f);
			Game.eventsOut.add(new EventTankTransformPreset(this, false, true));
			Game.movables.add(this);
			Game.removeMovables.add(this.sightTransformTank);
			this.skipNextUpdate = true;
			this.justTransformed = true;
			this.seesTargetEnemy = false;
		}

		if (this.possessor != null)
			this.possessor.updatePossessing();
	}

	public void updatePossessingMimic()
	{
		if (this.possessingTank.destroy || this.destroy || ScreenGame.finishedQuick || this.positionLock)
			return;

		this.updateTarget();

		Class<? extends Movable> c = null;

		Movable m = null;

		this.posX = this.possessingTank.posX;
		this.posY = this.possessingTank.posY;
		this.vX = this.possessingTank.vX;
		this.vY = this.possessingTank.vY;
		this.angle = this.possessingTank.angle;

		if (useRaysThisFrame)
		{
			if (this.targetEnemy != null)
			{
				Ray r = new Ray(this.possessingTank.posX, this.possessingTank.posY, 0, 0, this);
				r.vX = this.targetEnemy.posX - this.possessingTank.posX;
				r.vY = this.targetEnemy.posY - this.possessingTank.posY;

				double ma = Math.sqrt(r.vX * r.vX + r.vY * r.vY) / r.speed;
				r.vX /= ma;
				r.vY /= ma;

				r.moveOut(5);

				m = r.getTarget(2, (Tank) this.targetEnemy);

				if (((Tank) this.targetEnemy).possessor != null)
					c = ((Tank) this.targetEnemy).getTopLevelPossessor().getClass();
				else
					c = this.targetEnemy.getClass();

				if (c == TankPlayer.class || c == TankPlayerRemote.class)
					c = TankPlayerMimic.class;
			}

			if (this.targetEnemy == null || m != this.targetEnemy || this.targetEnemy.destroy || c != this.possessingTank.getClass() || GameObject.distanceBetween(this, this.targetEnemy) > this.mimicRange)
				this.mimicRevertCounter -= timeSinceRaysUsed;
			else
				this.mimicRevertCounter = this.mimicRevertTime;
		}

		Tank t = this.possessingTank.getBottomLevelPossessing();
		if (this.mimicRevertCounter <= 0 && this.currentlyTargetable)
		{
			Tank.idMap.put(this.networkID, this);
			this.health = t.health;
			this.orientation = t.orientation;
			this.drawAge = t.drawAge;
			this.setEffectManager(t.em());
			this.targetEnemy = null;

			if (t instanceof TankAIControlled)
				this.cooldown = Math.min(this.cooldownBase, ((TankAIControlled) t).cooldown);

			Drawing.drawing.playGlobalSound("slowdown.ogg", 1);

			Game.movables.add(this);
			Game.removeMovables.add(t);

			this.skipNextUpdate = true;
			Game.eventsOut.add(new EventTankMimicTransform(this, this));

			this.tryPossess();
		}

		if (this.targetEnemy != null && !this.targetEnemy.destroy && !t.destroy && this.canCurrentlyMimic && !this.positionLock)
		{
			this.laser = new Laser(t.posX, t.posY, t.size / 2, this.targetEnemy.posX, this.targetEnemy.posY, ((Tank)this.targetEnemy).size / 2,
					(this.mimicRange - GameObject.distanceBetween(t, this.targetEnemy)) / this.mimicRange * 10, this.targetEnemy.getAngleInDirection(t.posX, t.posY),
					((Tank) this.targetEnemy).color);
			Game.movables.add(this.laser);
			Game.eventsOut.add(new EventTankMimicLaser(t, (Tank) this.targetEnemy, this.mimicRange));
		}
		else
			Game.eventsOut.add(new EventTankMimicLaser(t, null, this.mimicRange));
	}

	public void tryPossess()
	{
		if (!this.seesTargetEnemy || !this.hasTarget || !(this.targetEnemy instanceof Tank) || this.destroy || !this.canCurrentlyMimic)
			return;

		try
		{
			this.mimicRevertCounter = this.mimicRevertTime;

			Class<? extends Movable> c = this.targetEnemy.getClass();
			Tank ct;

			ct = (Tank) this.targetEnemy;

			if (((Tank) this.targetEnemy).possessor != null)
			{
				ct = ((Tank) this.targetEnemy).getTopLevelPossessor();
				c = ct.getClass();
			}

			boolean player = false;

			if (c.equals(TankRemote.class))
				c = ((TankRemote) this.targetEnemy).tank.getClass();

			if (IServerPlayerTank.class.isAssignableFrom(c))
			{
				c = TankPlayerMimic.class;
				player = true;
			}

			Tank t;
			if (c.equals(TankAIControlled.class))
			{
				t = new TankAIControlled(this.name, this.posX, this.posY, this.size, this.color.red, this.color.green, this.color.blue, this.angle, ((TankAIControlled) ct).shootAIType);
				((TankAIControlled) ct).cloneProperties((TankAIControlled) t);
			}
			else
			{
				t = (Tank) c.getConstructor(String.class, double.class, double.class, double.class).newInstance(this.name, this.posX, this.posY, this.angle);
				t.fromRegistry = true;
				t.musicTracks = Game.registryTank.tankMusics.get(ct.name);

				if (t.musicTracks == null)
					t.musicTracks = new HashSet<>();
			}

			t.vX = this.vX;
			t.vY = this.vY;
			t.team = this.team;
			t.health = this.health;
			t.orientation = this.orientation;
			t.drawAge = this.drawAge;
			this.possessingTank = t;
			t.possessor = this;
			t.skipNextUpdate = true;
			t.setEffectManager(this.em());
			t.coinValue = this.coinValue;

			t.baseSkin = this.baseSkin;
			t.turretSkin = this.turretSkin;
			t.turretBaseSkin = this.turretBaseSkin;

			if (t instanceof TankAIControlled)
				((TankAIControlled) t).cooldown = Math.min(((TankAIControlled) t).cooldownBase, this.cooldown);

			t.age = 0;

			t.crusadeID = this.crusadeID;

			t.setNetworkID(this.networkID);

			this.justTransformed = true;

			Game.movables.add(t);
			Game.removeMovables.add(this);

			Drawing.drawing.playGlobalSound("transform.ogg");

			if (player)
			{
				this.possessingTank.color.set(((Tank) this.targetEnemy).color);
				this.possessingTank.secondaryColor.set(((Tank) this.targetEnemy).secondaryColor);
				this.possessingTank.tertiaryColor.set(((Tank) this.targetEnemy).tertiaryColor);
				this.possessingTank.enableTertiaryColor = true;
			}

			for (RegistryTank.TankEntry e: Game.registryTank.tankEntries)
			{
				if (e.tank.equals(c))
				{
					t.name = e.name;
				}
			}

			Game.eventsOut.add(new EventTankMimicTransform(this, (Tank) this.targetEnemy));

			if (Game.effectsEnabled)
			{
				for (int i = 0; i < 50 * Game.effectMultiplier; i++)
				{
					Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
					double var = 50;
					e.colR = Math.min(255, Math.max(0, this.possessingTank.color.red + Math.random() * var - var / 2));
					e.colG = Math.min(255, Math.max(0, this.possessingTank.color.green + Math.random() * var - var / 2));
					e.colB = Math.min(255, Math.max(0, this.possessingTank.color.blue + Math.random() * var - var / 2));

					if (Game.enable3d)
						e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * this.size / 50.0);
					else
						e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * this.size / 50.0);

					Game.effects.add(e);
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	public void updateMimic()
	{
		if (this.justTransformed)
			return;

		this.updateTarget();
		this.tryPossess();
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

		public int teleporterID = -1;

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

		public boolean search(Queue<Tile> queue, Tile[][] map)
		{
			if (this.teleporterID >= 0)
			{
				for (Obstacle o: Game.obstacles)
				{
					if (o instanceof ObstacleTeleporter && ((ObstacleTeleporter) o).groupID == this.teleporterID)
					{
						int x = (int) (o.posX / Game.tile_size);
						int y = (int) (o.posY / Game.tile_size);

						if (x >= 0 && x < Game.currentSizeX && y > 0 && y < Game.currentSizeY)
						{
							map[x][y].explore(this, queue);
						}
					}
				}
			}

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

		public void explore(Tile parent, Queue<Tile> queue)
		{
			if (this.type != Type.solid && !this.explored)
			{
				this.parent = parent;
				queue.add(this);
			}

			this.explored = true;
		}
	}

	public boolean isSupportTank()
	{
		return !this.suicidal && !this.getBullet().isHarmful();
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
	public String toString()
	{
		return Serializer.toTanksON(this);
	}

	public static TankAIControlled fromString(String s)
	{
		if (s.contains("{") && (!s.contains("[") || s.indexOf("{") < s.indexOf("[")))
			return (TankAIControlled) Serializer.fromTanksON(s);
		else
			return fromStringLegacy(s, null);
	}

	@Deprecated
	public static TankAIControlled fromStringLegacy(String s, String[] remainder)
	{
		String original = s;
		TankAIControlled t = new TankAIControlled(null, 0, 0, 0, 0, 0, 0, 0, ShootAI.none);

		boolean explodeOnDeath = false;
		try
		{
			s = s.substring(s.indexOf("[") + 1);
			if (s.isEmpty())
				return null;

			while (s.charAt(0) != ']')
			{
				int equals = s.indexOf("=");
				String value = s.substring(equals + 1, s.indexOf(";"));
				String propname = s.substring(0, equals);

				for (Field f : TankAIControlled.class.getFields())
				{
					boolean found = true;

					Property a = f.getAnnotation(Property.class);
					if (a != null && (a.id().equals(propname) || (a.id().equals("spawned_tanks") && propname.equals("spawned_tank") || (a.id().replace("_skin", "_model").equals(propname)))))
					{
						if (f.getType().equals(int.class))
							f.set(t, Integer.parseInt(value));
						else if (f.getType().equals(double.class))
							f.set(t, Double.parseDouble(value));
						else if (f.getType().equals(boolean.class))
							f.set(t, Boolean.parseBoolean(value));
						else if (f.getType().equals(String.class))
						{
							if (value.equals("*"))
								f.set(t, null);
							else if (value.startsWith("\u00A7"))
							{
								s = s.substring(equals + 2);
								int end = s.indexOf("\u00A7");
								value = s.substring(0, end);
								s = s.substring(end + 1);
								f.set(t, value);
							}
							else if (value.startsWith("<"))
							{
								s = s.substring(equals + 2);
								int end = s.indexOf(">");
								int length = Integer.parseInt(s.substring(0, end));
								value = s.substring(end + 1, end + 1 + length);
								s = s.substring(end + 1 + length);
								f.set(t, value);
							}
							else
								f.set(t, value);
						}
						else if (a.miscType() == Property.MiscType.music)
						{
							int end = s.indexOf("]");
							String[] csv = s.substring(s.indexOf("[") + 1, end).split(", ");
							HashSet<String> hashSet;
							if (csv[0].equals(""))
								hashSet = new HashSet<>();
							else
								hashSet = new HashSet<>(Arrays.asList(csv));

							f.set(t, hashSet);
						}
						else if (a.miscType() == Property.MiscType.spawnedTanks && !propname.equals("spawned_tank"))
						{
							s = s.substring(s.indexOf("[") + 1);
							ArrayList<SpawnedTankEntry> entries = (ArrayList<SpawnedTankEntry>) f.get(t);

							TankAIControlled target;
							while (!s.startsWith("]"))
							{
								int x = s.indexOf("x");
								String s1 = s.substring(0, x);
								s = s.substring(x + 1);
								if (s.equals("*"))
									target = null;
								else if (s.startsWith("<"))
								{
									String tank = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
									s = s.substring(s.indexOf(">") + 1);
									target = (TankAIControlled) Game.registryTank.getEntry(tank).getTank(0, 0, 0);
								}
								else
								{
									String[] r = new String[1];
									TankAIControlled t2 = TankAIControlled.fromStringLegacy(s, r);

									s = r[0];
									target = t2;
									s = s.substring(s.indexOf("]") + 1);
								}

								if (s.startsWith(", "))
									s = s.substring(2);
								entries.add(new SpawnedTankEntry(target, Double.parseDouble(s1)));
							}

							s = s.substring(1);
						}
						else if (a.miscType().equals(Property.MiscType.baseModel) || a.miscType().equals(Property.MiscType.colorModel) || a.miscType().equals(Property.MiscType.turretBaseModel) || a.miscType().equals(Property.MiscType.turretModel))
						{
							TankModels.TankSkin sk = Compatibility.convertModelToSkin(value);
							f.set(t, sk);
						}
						else if (f.getType().isEnum())
							f.set(t, Enum.valueOf((Class<? extends Enum>) f.getType(), value));
						else if (ItemBullet.ItemStackBullet.class.isAssignableFrom(f.getType()))
						{
							Item.ItemStack<?> i = Item.ItemStack.fromString(null, s);
							i.stackSize = 0;
							f.set(t, i);
							s = s.substring(s.indexOf("]") + 1);
						}
						else if (ItemMine.ItemStackMine.class.isAssignableFrom(f.getType()))
						{
							Item.ItemStack<?> i = Item.ItemStack.fromString(null, s);
							i.stackSize = 0;
							f.set(t, i);
							s = s.substring(s.indexOf("]") + 1);
						}
						else if (Explosion.class.isAssignableFrom(f.getType()))
						{
							if (value.equals("true"))
								explodeOnDeath = true;
						}
						else if (ITankField.class.isAssignableFrom(f.getType()) || propname.equals("spawned_tank"))
						{
							TankAIControlled target;

							if (value.equals("*"))
								target = null;
							else if (value.startsWith("<"))
							{
								String tank = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
								s = s.substring(s.indexOf(">") + 1);
								target = (TankAIControlled) Game.registryTank.getEntry(tank).getTank(0, 0, 0);
								target.fromRegistry = true;
							}
							else
							{
								String[] r = new String[1];
								TankAIControlled t2 = TankAIControlled.fromStringLegacy(s, r);

								s = r[0];
								target = t2;
								s = s.substring(s.indexOf("]") + 1);
							}

							if (propname.equals("spawned_tank"))
							{
								if (target != null)
									t.spawnedTankEntries.add(new SpawnedTankEntry(target, 1));
							}
							else
								f.set(t, target);
						}
					}
					else
						found = false;

					if (found)
						break;
				}

				s = s.substring(s.indexOf(";") + 1);
			}
		}
		catch (Exception e)
		{
			Game.logger.println("Failed to load tank: " + original);
			System.err.println("Failed to load tank: " + original);
			Game.exitToCrash(e);
		}

		if (remainder != null)
			remainder[0] = s;

		t.suicideExplosion = t.mineItem.item.mine.explosion;

		if (explodeOnDeath)
			t.explodeOnDestroy = t.mineItem.item.mine.explosion;

		return t;
	}

	public TankAIControlled instantiate(String name, double x, double y, double angle)
	{
		TankAIControlled t = new TankAIControlled(name, x, y, this.size, this.color.red, this.color.green, this.color.blue, angle, this.shootAIType);
		this.cloneProperties(t);
		return t;
	}

	public TankPlayer convertToPlayer(double x, double y, double angle)
	{
		TankPlayer t = new TankPlayer(x, y, angle);
		this.cloneProperties(t);
		return t;
	}

	public static ITankField cloneTankField(ITankField t1)
	{
		if (t1 instanceof TankAIControlled)
		{
			TankAIControlled t2 = new TankAIControlled("", 0, 0, 0, 0, 0, 0, 0, ShootAI.none);

			((TankAIControlled) t1).cloneProperties(t2);
			return t2;
		}
		else
			return t1;
	}

	public void cloneProperties(TankAIControlled t)
	{
		try
		{
			for (Field f : TankAIControlled.class.getFields())
			{
				Property a = f.getAnnotation(Property.class);
				if (a != null)
				{
					if (ICopyable.class.isAssignableFrom(f.getType()))
					{
						ICopyable<?> i1 = (ICopyable<?>) f.get(this);
						Object i2 = i1 == null ? null : i1.getCopy();
						f.set(t, i2);
					}
					else if (ITankField.class.isAssignableFrom(f.getType()))
					{
						ITankField t1 = (ITankField) f.get(this);
						f.set(t, cloneTankField(t1));
					}
					else if (a.miscType() == Property.MiscType.spawnedTanks)
					{
						ArrayList<SpawnedTankEntry> a1 = (ArrayList<SpawnedTankEntry>) f.get(this);

						ArrayList<SpawnedTankEntry> al = new ArrayList<SpawnedTankEntry>();
						for (SpawnedTankEntry o: a1)
						{
							al.add(new SpawnedTankEntry(cloneTankField(o.tank), o.weight));
						}

						f.set(t, al);
					}
					else if (a.miscType() == Property.MiscType.music)
					{
						if (f.get(this) != null)
							f.set(t, new HashSet<>((HashSet<String>) f.get(this)));
						else
							f.set(t, new HashSet<>());
					}
					else
						f.set(t, f.get(this));
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		t.health = t.baseHealth;
	}

	public void cloneProperties(TankPlayable t)
	{
		try
		{
			for (Field f : Tank.class.getFields())
			{
				t.abilities.clear();

				Property a = f.getAnnotation(Property.class);
				TankBuildProperty bp = f.getAnnotation(TankBuildProperty.class);
				if (a != null && bp != null)
				{
					if (ICopyable.class.isAssignableFrom(f.getType()))
					{
						ICopyable<?> i1 = (ICopyable<?>) f.get(this);
						Object i2 = i1 == null ? null : i1.getCopy();
						f.set(t, i2);
					}
					else if (ITankField.class.isAssignableFrom(f.getType()))
					{
						ITankField t1 = (ITankField) f.get(this);
						f.set(t, cloneTankField(t1));
					}
					else if (a.miscType() == Property.MiscType.spawnedTanks)
					{
						ArrayList<SpawnedTankEntry> a1 = (ArrayList<SpawnedTankEntry>) f.get(this);

						ArrayList<SpawnedTankEntry> al = new ArrayList<SpawnedTankEntry>();
						for (SpawnedTankEntry o: a1)
						{
							al.add(new SpawnedTankEntry(cloneTankField(o.tank), o.weight));
						}

						f.set(t, al);
					}
					else if (a.miscType() == Property.MiscType.music)
					{
						if (f.get(this) != null)
							f.set(t, new HashSet<>((HashSet<String>) f.get(this)));
						else
							f.set(t, new HashSet<>());
					}
					else
						f.set(t, f.get(this));
				}
			}

			if (!this.enableMovement)
				t.maxSpeed = 0;

			if (this.shootAIType != ShootAI.none)
			{
				ItemBullet.ItemStackBullet b = (ItemBullet.ItemStackBullet) this.bulletItem.getCopy();
				b.item.name = Translation.translate("Bullet");
				b.item.cooldownBase = this.cooldownBase;
				t.abilities.add(b);
			}

			if (this.enableMineLaying)
			{
				ItemMine.ItemStackMine m = this.mineItem;
				m.item.name = Translation.translate("Mine");
				m.item.cooldownBase = this.mineTimerBase;
				t.abilities.add(m);
			}

			t.overrideEmblemColor = true;
			t.overridePrimaryColor = true;
			t.overrideSecondaryColor = true;
			t.overrideTertiaryColor = true;
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		t.health = t.baseHealth;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public Tank resolve()
	{
		return this;
	}

	/**
	 * Populates a given (empty) hashset with the names of all tanks included as links by this tank or other tanks it uses (spawns, transforms into)
	 */
	//TODO: There's probably a much better way to do this involving tankson traversal, but that is kind of its own can of worms...
	public void getAllLinkedTankNames(HashSet<String> explored)
	{
		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (ITankField.class.isAssignableFrom(f.getType()) && f.getAnnotation(Property.class) != null)
				{
					ITankField t = (ITankField) f.get(this);
					if (t instanceof TankReference && !explored.contains(t.getName()) && TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(t.getName()).tank))
					{
						explored.add(t.getName());
						Tank ta = t.resolve();
						if (ta instanceof TankAIControlled)
							((TankAIControlled) ta).getAllLinkedTankNames(explored);
					}
					else if (t instanceof TankAIControlled)
						((TankAIControlled) t).getAllLinkedTankNames(explored);
				}
				else if (f.getAnnotation(Property.class) != null && f.getAnnotation(Property.class).miscType() == Property.MiscType.spawnedTanks)
				{
					ArrayList<SpawnedTankEntry> entries = (ArrayList<SpawnedTankEntry>) f.get(this);
					for (SpawnedTankEntry e: entries)
					{
						ITankField t = e.tank;
						if (t instanceof TankReference && !explored.contains(t.getName()) && TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(t.getName()).tank))
						{
							explored.add(t.getName());
							Tank ta = t.resolve();
							if (ta instanceof TankAIControlled)
								((TankAIControlled) ta).getAllLinkedTankNames(explored);
						}
						else if (t instanceof TankAIControlled)
							((TankAIControlled) t).getAllLinkedTankNames(explored);
					}
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	/**
	 * Removes linked tanks that do not link to any existing tank
	 */
	//TODO: There's probably a much better way to do this involving tankson traversal, but that is kind of its own can of worms...
	public void removeBrokenLinks()
	{
		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (ITankField.class.isAssignableFrom(f.getType()) && f.getAnnotation(Property.class) != null)
				{
					ITankField t = (ITankField) f.get(this);
					if (t instanceof TankReference && TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(t.getName()).tank))
					{
						Tank ta = t.resolve();
						if (ta == null)
							f.set(this, null);
					}
				}
				else if (f.getAnnotation(Property.class) != null && f.getAnnotation(Property.class).miscType() == Property.MiscType.spawnedTanks)
				{
					ArrayList<SpawnedTankEntry> entries = (ArrayList<SpawnedTankEntry>) f.get(this);
					for (int i = 0; i < entries.size(); i++)
					{
						SpawnedTankEntry e = entries.get(i);
						ITankField t = e.tank;
						if (t instanceof TankReference && TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(t.getName()).tank))
						{
							Tank ta = t.resolve();
							if (ta == null)
							{
								entries.remove(i);
								i--;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}

	/**
	 * Replaces all tank references in this tank which have the old name with the new name, recursively across all included (not-linked) tanks
	 * @param originalName Old name of the linked tank
	 * @param newName New name of the linked tank, or null to delete the old links entirely
	 * @return if anything changed
	 */
	//TODO: There's probably a much better way to do this involving tankson traversal, but that is kind of its own can of worms...
	public boolean renameLinkedTank(String originalName, String newName)
	{
		boolean edited = false;

		try
		{
			for (Field f : this.getClass().getFields())
			{
				if (ITankField.class.isAssignableFrom(f.getType()) && f.getAnnotation(Property.class) != null)
				{
					ITankField t = (ITankField) f.get(this);
					if (t instanceof TankAIControlled)
						edited |= ((TankAIControlled) t).renameLinkedTank(originalName, newName);
					else if (t instanceof TankReference)
					{
						if (t.getName().equals(originalName))
						{
							if (newName == null)
								f.set(this, null);
							else
								((TankReference) t).tankName = newName;

							edited = true;
						}
					}
				}
				else if (f.getAnnotation(Property.class) != null && f.getAnnotation(Property.class).miscType() == Property.MiscType.spawnedTanks)
				{
					ArrayList<SpawnedTankEntry> entries = (ArrayList<SpawnedTankEntry>) f.get(this);
					for (int i = 0; i < entries.size(); i++)
					{
						SpawnedTankEntry e = entries.get(i);
						ITankField t = e.tank;
						if (t instanceof TankAIControlled)
							edited |= ((TankAIControlled) t).renameLinkedTank(originalName, newName);
						else if (t instanceof TankReference)
						{
							if (t.getName().equals(originalName))
							{
								if (newName == null)
								{
									entries.remove(i);
									i--;
								}
								else
									((TankReference) t).tankName = newName;

								edited = true;
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}

		return edited;
	}

	public Bullet getBullet()
	{
		return this.bulletItem.item.bullet;
	}

	public Mine getMine()
	{
		return this.mineItem.item.mine;
	}

	public void setBullet(Bullet b)
	{
		this.bulletItem.item.bullet = b.getCopy();
		this.bulletItem.item.cooldownBase = Math.min(1, this.cooldownBase);
		if (this.cooldownRandom > 0 && this.bulletItem.item.cooldownBase <= 0)
			this.bulletItem.item.cooldownBase = Double.MIN_VALUE;
	}

	public void setMine(Mine m)
	{
		this.mineItem.item.mine = m.getCopy();
		this.mineItem.item.cooldownBase = Math.min(1, this.mineTimerBase);
		if (this.mineTimerBase > 0 && this.mineItem.item.cooldownBase <= 0)
			this.mineItem.item.cooldownBase = Double.MIN_VALUE;
	}

	public void setBullet(ItemBullet b)
	{
		this.bulletItem.item = b.getCopy();
		this.bulletItem.item.cooldownBase = Math.min(1, this.cooldownBase);
		if (this.cooldownRandom > 0 && this.bulletItem.item.cooldownBase <= 0)
			this.bulletItem.item.cooldownBase = Double.MIN_VALUE;
	}

	public void setMine(ItemMine m)
	{
		this.mineItem.item = m.getCopy();
		this.mineItem.item.cooldownBase = Math.min(1, this.mineTimerBase);
		if (this.mineTimerBase > 0 && this.mineItem.item.cooldownBase <= 0)
			this.mineItem.item.cooldownBase = Double.MIN_VALUE;
	}

	@Override
	public void setBufferCooldown(Item.ItemStack<?> stack, double value)
	{
		this.bulletItem.cooldown = Math.max(this.bulletItem.cooldown, value);
		this.mineItem.cooldown = Math.max(this.mineItem.cooldown, value);
	}
}

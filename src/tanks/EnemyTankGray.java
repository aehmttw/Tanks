package tanks;

import java.awt.Color;

public class EnemyTankGray extends EnemyTank
{
	public EnemyTankGray(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(100, 100, 100), angle, ShootAI.wander);

		this.enableMovement = true;
		this.speed = 1;
		this.enableMineLaying = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.idleTurretSpeed = 0.01;
		this.bulletBounces = 1;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtPlayer = false;
		this.motionChangeChance = 0.0005;
		
		this.coinValue = 1;
	}
}

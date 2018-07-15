package tanks;

import java.awt.Color;

public class EnemyTankBrown extends EnemyTank
{
	public EnemyTankBrown(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(150, 80, 0), angle, ShootAI.wander);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.liveBulletMax = 1;
		this.cooldownRandom = 60;
		this.cooldownBase = 120;
		this.idleTurretSpeed = 0.01;
		this.bulletBounces = 1;
		this.turretIdleTimerBase = 500;
		this.turretIdleTimerRandom = 500;
		this.enableLookingAtPlayer = false;
		this.aimAccuracyOffset = 0;
		
		this.coinValue = 1;
	}
}

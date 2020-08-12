package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.bullet.BulletElectric;

public class TankBlue extends TankAIControlled
{
	public TankBlue(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 0, 0, 200, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.cooldown = 100;
		this.cooldownBase = 200;

		this.coinValue = 4;

		this.description = "A stationary tank which shoots---stunning electricity that arcs---between targets";
	}

	@Override
	public void update()
	{
		super.update();
	}

	@Override
	public void shoot()
	{
		if (this.cooldown > 0)
			return;

		Drawing.drawing.playGlobalSound("laser.ogg");

		BulletElectric b = new BulletElectric(this.posX, this.posY, 5, this);
		b.team = this.team;
		b.setPolarMotion(this.angle, 25.0/8);
		b.moveOut(16);
		Game.movables.add(b);
		this.cooldown = this.cooldownBase;

	}
}

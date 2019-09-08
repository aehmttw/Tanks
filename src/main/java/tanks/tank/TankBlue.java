package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.bullet.BulletElectric;
import tanks.event.EventShootBullet;

import java.util.ArrayList;

public class TankBlue extends TankAIControlled
{
	public TankBlue(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 0, 0, 200, angle, ShootAI.straight);

		this.enableMovement = false;
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.liveBulletMax = 1;
		this.aimTurretSpeed = 0.02;
		this.enableLookingAtTargetEnemy = false;
		this.cooldown = 100;
		this.cooldownBase = 200;

		this.coinValue = 4;
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

		BulletElectric b = new BulletElectric(this.posX, this.posY, 5, this, new ArrayList<Movable>());
		b.team = this.team;
		b.setPolarMotion(this.angle, 25.0/4);
		b.moveOut(8);
		Game.eventsOut.add(new EventShootBullet(b));
		Game.movables.add(b);
		Drawing.drawing.pendingSounds.add("resources/laser.wav");
		this.cooldown = this.cooldownBase;

	}
}

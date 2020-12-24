package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.BulletFlame;
import tanks.event.EventShootBullet;

public class TankOrange extends TankAIControlled
{
	public TankOrange(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 230, 120, 0, angle, ShootAI.straight);

		this.enableMovement = true;
		this.maxSpeed = 1.0;

		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.aimTurretSpeed = 0.01;
		this.enablePathfinding = true;

		this.motionChangeChance = 0.001;
		this.mineSensitivity = 1;

		this.coinValue = 4;

		this.description = "A short-range tank which shoots fire";
	}

	@Override
	public void shoot()
	{
		if (Movable.distanceBetween(this, this.targetEnemy) < 400 && this.cooldown <= 0)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);
			Movable m = a.getTarget();

			if (!(m == null))
			{
				if (m.equals(this.targetEnemy))
				{
					Drawing.drawing.playGlobalSound("flame.ogg");

					BulletFlame b = new BulletFlame(this.posX, this.posY, 0, this);
					b.frameDamageMultipler = Panel.frameFrequency;
					b.setPolarMotion(this.angle, 25.0/8);
					b.moveOut(16);
					Game.movables.add(b);
					Game.eventsOut.add(new EventShootBullet(b));
					this.cooldown = 0;
				}
			}
		}
	}
}
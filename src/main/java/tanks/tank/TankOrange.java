package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Ray;
import tanks.bullets.BulletFlame;

public class TankOrange extends TankAIControlled
{
	public TankOrange(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, 230, 120, 0, angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 1.5;
		
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.aimTurretSpeed = 0.01;
				
		this.motionChangeChance = 0.001;
		
		this.coinValue = 6;
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
				if(m.equals(this.targetEnemy))
				{
					Drawing.drawing.playSound("resources/flame.wav");

					BulletFlame b = new BulletFlame(this.posX, this.posY, 0, this);
					b.setPolarMotion(this.angle, 25.0/4);
					b.moveOut(8);
					Game.movables.add(b);
					this.cooldown = 0;
				}
			}
		}
	}
}

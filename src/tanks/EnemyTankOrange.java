package tanks;

import java.awt.Color;

public class EnemyTankOrange extends EnemyTank
{
	public EnemyTankOrange(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(230, 120, 0), angle, ShootAI.straight);

		this.enableMovement = true;
		this.speed = 1.5;
		
		this.enableMineLaying = false;
		this.enablePredictiveFiring = false;
		this.idleTurretSpeed = 0.01;
		this.aimAccuracyOffset = 0;
				
		this.motionChangeChance = 0.0005;
		
		this.coinValue = 6;
	}
	
	@Override
	public void shoot() 
	{
		if (Movable.distanceBetween(this, Game.player) < 400 && this.cooldown <= 0)
		{
			Ray a = new Ray(this.posX, this.posY, this.angle, 0, this);
			Movable m = a.getTarget();
			//if (m != null)
			//	System.out.println(((Tank)m).color);

			if (!(m == null))
			{
				if(m.equals(Game.player))
				{
					Flame b = new Flame(this.posX, this.posY, Color.blue, 0, this);
					b.setPolarMotion(this.angle, 25.0/4);
					b.moveOut(8);
					Game.movables.add(b);
					this.cooldown = 0;
				}
			}
		}
	}
}

package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class EnemyTankWhite extends EnemyTankDynamic
{
	boolean vanish = false;
	
	public EnemyTankWhite(double x, double y, double angle)
	{
		super(x, y, Game.tank_size, new Color(255, 255, 255), angle, ShootAI.alternate);
		this.speed = 1.5;
		this.enableDefensiveFiring = true;
		
		this.coinValue = 4;
	}

	@Override
	public void draw(Graphics g)
	{
		if (this.age <= 0 || this.destroy)
			super.draw(g);
		else
		{
			if (!this.vanish)
			{
				this.vanish = true;
	
				for (int i = 0; i < 50; i++)
				{
					Effect e = new Effect(this.posX, this.posY, Effect.EffectType.piece);
					int var = 50;
					e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);
					Game.effects.add(e);
				}
			}
			
			for (int i = 0; i < Game.tank_size * 2 - this.age; i++)
			{
				g.setColor(new Color(255, 255, 255, (int)((Game.tank_size * 2 - i - this.age) * 2.55)));
				Screen.fillOval(g, this.posX, this.posY, i, i);
			}

			if (this.drawTread)
			{
				this.drawTread = false;
				double a = this.getPolarDirection();
				Effect e1 = new Effect(this.posX, this.posY, Effect.EffectType.tread);
				Effect e2 = new Effect(this.posX, this.posY, Effect.EffectType.tread);
				e1.setPolarMotion(a - Math.PI / 2, this.size * 0.25);
				e2.setPolarMotion(a + Math.PI / 2, this.size * 0.25);
				e1.size = this.size / 5;
				e2.size = this.size / 5;
				e1.posX += e1.vX;
				e1.posY += e1.vY;
				e2.posX += e2.vX;
				e2.posY += e2.vY;
				e1.setPolarMotion(0, 0);
				e2.setPolarMotion(0, 0);
				Game.belowEffects.add(e1);
				Game.belowEffects.add(e2);
			}
		}
	}
}

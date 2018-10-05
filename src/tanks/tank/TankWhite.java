package tanks.tank;

import java.awt.Color;
import java.awt.Graphics;

import tanks.Effect;
import tanks.Game;
import tanks.Drawing;

public class TankWhite extends TankAIControlled
{
	boolean vanish = false;
	
	public TankWhite(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tank_size, new Color(255, 255, 255), angle, ShootAI.alternate);
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
					Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
					int var = 50;
					e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);
					Game.effects.add(e);
				}
			}
			
			for (int i = 0; i < Game.tank_size * 2 - this.age; i++)
			{
				g.setColor(new Color(255, 255, 255, (int)((Game.tank_size * 2 - i - this.age) * 2.55)));
				Drawing.fillOval(g, this.posX, this.posY, i, i);
			}
		}
	}
}

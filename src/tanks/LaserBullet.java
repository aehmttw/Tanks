package tanks;

import java.awt.Color;

public class LaserBullet extends Bullet
{
	public LaserBullet(double x, double y, Color color, int bounces, Tank t) 
	{
		super(x, y, color, bounces, t);
		t.liveBullets--;
		this.color = Color.red;
	}
	
	public void shoot()
	{
		while(!this.destroy)
		{
			if (!Game.movables.contains(Game.player) || Game.player.destroy)
				this.destroy = true;
			
			this.update();
			Game.effects.add(new Effect(this.posX, this.posY, Effect.EffectType.laser));
		}

		if (Game.graphicalEffects)
		{
			for (int i = 0; i < this.size * 4; i++)
			{
				Effect e = new Effect(this.posX, this.posY, Effect.EffectType.piece);
				int var = 50;
				e.maxAge /= 2;
				e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
				Game.effects.add(e);
			}
		}
	}

}

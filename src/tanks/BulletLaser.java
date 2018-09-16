package tanks;

import java.awt.Color;

public class BulletLaser extends Bullet
{
	public BulletLaser(double x, double y, int bounces, Tank t) 
	{
		super(x, y, bounces, t);
		t.liveBullets--;
		this.baseColor = Color.red;
	}
	
	public void shoot()
	{
		while(!this.destroy)
		{
			if (ScreenGame.finished)
				this.destroy = true;
			
			this.update();
			Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.laser));
		}

		if (Game.graphicalEffects)
		{
			for (int i = 0; i < this.size * 4; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
				int var = 50;
				e.maxAge /= 2;
				e.col = new Color((int) Math.min(255, Math.max(0, this.baseColor.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.baseColor.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.baseColor.getBlue() + Math.random() * var - var / 2)));
				e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
				Game.effects.add(e);
			}
		}
	}

}

package tanks.tank;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.event.EventTankUpdateVisibility;

public class TankWhite extends TankAIControlled
{
	boolean vanish = false;

	boolean updateVanished = false;

	public TankWhite(String name, double x, double y, double angle)
	{
		super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.alternate);
		this.maxSpeed = 1.0;
		this.enableDefensiveFiring = true;
		this.enablePathfinding = true;

		this.coinValue = 4;

		this.description = "An invisible smart tank";
	}

	@Override
	public void update()
	{
		if (!this.updateVanished)
		{
			Game.eventsOut.add(new EventTankUpdateVisibility(this.networkID, false));
			this.updateVanished = true;
		}

		super.update();
	}

	@Override
	public void draw()
	{
		if (this.age <= 0 || this.destroy)
			super.draw();
		else
		{
			if (!this.vanish)
			{
				this.vanish = true;

				if (Game.fancyGraphics)
				{
					for (int i = 0; i < 50; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
						double var = 50;
						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);

						Game.effects.add(e);
					}
				}
			}

			if (this.size * 4 > this.age * 2)
			{
				Drawing.drawing.setColor(255, 255, 255);

				if (Game.enable3d)
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size / 4, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2, true, false);
				else
					Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4 - this.age * 2, this.size * 4 - this.age * 2);
			}
		}
	}
}

package tanks;

import tanks.AttributeModifier.Operation;
import tanks.event.EventCreateFreezeEffect;
import tanks.gui.screen.ScreenPartyLobby;

public class AreaEffectFreeze extends AreaEffect
{
	double size = 300;
	public AreaEffectFreeze(double x, double y)
	{
		super(x, y);
		this.isRemote = ScreenPartyLobby.isClient;
		this.constantlyImbue = false;
		this.imbueEffects();
		this.maxAge = 600;
		this.drawLevel = 9;

		if (!this.isRemote)
			Game.eventsOut.add(new EventCreateFreezeEffect(this));
	}

	@Override
	public void imbueEffects()
	{
		if (Game.fancyGraphics)
		{
			for (int i = 0; i < 200; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
				double var = 50;
				e.colR = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 200.0);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 200.0);

				e.maxAge *= 4;
				Game.effects.add(e);
			}
		}

		if (!this.isRemote)
		{
			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);

				if (Movable.distanceBetween(this, m) <= this.size / 2 && !m.destroy)
				{
					AttributeModifier a = new AttributeModifier("freeze", "velocity", Operation.multiply, -1);
					a.duration = 600;
					a.warmupAge = 50;
					a.deteriorationAge = 500;
					m.addAttribute(a);
				}
			}
		}
	}

	@Override
	public void draw()
	{
		double size = Math.min(this.size + Game.tank_size / 2, this.age * 8);
		for (int i = (int) Math.max(0, size - ((int) (50 * Math.min(100, 600 - this.age) / 100.0))); i < size; i += 2)
		{
			Drawing.drawing.setColor(200, 255, 255, 10);

			if (Game.enable3d)
				Drawing.drawing.fillOval(this.posX, this.posY, (size - i) + Game.tank_size / 4, i, i, true, false);
			else
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
		}
	}

}

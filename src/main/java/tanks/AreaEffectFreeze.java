package tanks;

import tanks.AttributeModifier.Operation;
import tanks.event.EventCreateFreezeEffect;
import tanks.gui.screen.ScreenGame;
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
		this.maxAge = 500;
		this.drawLevel = 9;

		if (!this.isRemote)
			Game.eventsOut.add(new EventCreateFreezeEffect(this));
	}

	@Override
	public void imbueEffects()
	{
		if (Game.effectsEnabled)
		{
			for (int i = 0; i < 100 * Game.effectMultiplier; i++)
			{
				Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
				double var = 50;
				e.fastRemoveOnExit = true;
				e.colR = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
				e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));

				if (Game.enable3d)
					e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 200.0);
				else
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 200.0);

				e.maxAge *= 3;
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
					a.duration = 500;
					a.warmupAge = 50;
					a.deteriorationAge = 400;
					m.addAttribute(a);
				}
			}
		}
	}

	@Override
	public void draw()
	{
		double size = Math.min(this.size + Game.tile_size / 2, this.age * 8);
		for (int i = (int) Math.max(0, size - ((int) (50 * Math.min(100, this.maxAge - this.age) / 100.0))); i < size; i += 2)
		{
			Drawing.drawing.setColor(200, 255, 255, 10, 0.5);

			if (Game.enable3d)
				Drawing.drawing.fillOval(this.posX, this.posY, (size - i) + Game.tile_size / 4, i, i, true, false);
			else
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
		}
	}

	@Override
	public void update()
	{
		if (ScreenGame.finishedQuick && this.age < 400)
			this.age = 400;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (Movable.distanceBetween(this, m) <= this.size / 2 && !m.destroy)
			{
				AttributeModifier a = new AttributeModifier("ice_accel", "acceleration", Operation.multiply, -0.75);
				a.duration = 10;
				a.deteriorationAge = 5;
				m.addUnduplicateAttribute(a);

				AttributeModifier b = new AttributeModifier("ice_slip", "friction", Operation.multiply, -0.875);
				b.duration = 10;
				b.deteriorationAge = 5;
				m.addUnduplicateAttribute(b);

				AttributeModifier c = new AttributeModifier("ice_max_speed", "max_speed", Operation.multiply, 3);
				c.duration = 10;
				c.deteriorationAge = 5;
				m.addUnduplicateAttribute(c);
			}
		}

		super.update();
	}

}

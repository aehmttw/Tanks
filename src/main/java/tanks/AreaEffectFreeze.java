package tanks;

import tanks.effect.AttributeModifier;
import tanks.effect.AttributeModifier.Operation;
import tanks.effect.StatusEffect;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.event.EventCreateFreezeEffect;

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
			for (Movable m : Movable.getMovablesInRadius(this.posX, this.posY, this.size / 2))
			{
                if (m.destroy)
                    continue;

                AttributeModifier a = AttributeModifier.newInstance("freeze", AttributeModifier.velocity, Operation.multiply, -1);
                a.duration = 500;
                a.warmupAge = 50;
                a.deteriorationAge = 400;
                m.em().addAttribute(a);
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

		for (Movable m : Movable.getMovablesInRadius(this.posX, this.posY, this.size / 2))
		{
			if (!m.destroy)
                m.em().addStatusEffect(StatusEffect.ice, 0, 5, 10);
		}

		super.update();
	}

}

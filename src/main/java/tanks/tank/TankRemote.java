package tanks.tank;

import tanks.*;

public class TankRemote extends Tank
{
	public final boolean isCopy;
	public final Tank tank;

	public boolean invisible = false;
	public boolean vanished = false;

	public double localAge = 0;

	public TankRemote(String name, double x, double y, double angle, Team team, double size, double ts, double tl, double r, double g, double b, double lives, double baselives)
	{
		super(name, x, y, size, r, g, b);
		this.angle = angle;
		this.team = team;
		this.lives = lives;
		this.baseLives = baselives;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = null;
		this.turret.size = ts;
		this.turret.length = tl;
		this.invulnerable = true;
		this.description = "A tank controlled by the server";
	}
	
	public TankRemote(Tank t)
	{
		super(t.name, t.posX, t.posY, t.size, t.colorR, t.colorG, t.colorB, false);
		this.angle = t.angle;
		this.team = t.team;
		this.lives = t.lives;
		this.baseLives = t.baseLives;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = t;
		this.turret.length = t.turret.length;
		this.turret.size = t.turret.size;
		this.invulnerable = true;
		this.networkID = t.networkID;
		this.texture = t.texture;
		this.description = t.description;
		Tank.idMap.put(this.networkID, this);
	}

	@Override
	public void update()
	{
		this.localAge += Panel.frameFrequency;
		super.update();
	}

	@Override
	public void draw()
	{
		if (!this.invisible || this.localAge <= 0 || this.destroy)
			super.draw();
		else
		{
			if (!this.vanished)
			{
				this.vanished = true;

				if (Game.fancyGraphics)
				{
					for (int i = 0; i < 50; i++)
					{
						Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
						double var = 50;
						e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
						e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
						e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

						if (Game.enable3d)
							e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * this.size / 50.0);
						else
							e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);

						Game.effects.add(e);						Game.effects.add(e);
					}
				}
			}

			for (int i = 0; i < Game.tile_size * 2 - this.localAge; i++)
			{
				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, (this.size * 2 - i - this.localAge) * 2.55);
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
			}
		}
	}
}

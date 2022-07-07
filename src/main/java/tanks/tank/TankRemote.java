package tanks.tank;

import tanks.*;

public class TankRemote extends Tank
{
	public final boolean isCopy;
	public final Tank tank;

	public boolean invisible = false;
	public boolean vanished = false;

	public double localAge = 0;

	public long lastUpdate = -1;

	public double interpolationTime = 1;

	public double interpolatedOffX = 0;
	public double interpolatedOffY = 0;
	public double interpolatedProgress = interpolationTime;

	public double interpolatedPosX = this.posX;
	public double interpolatedPosY = this.posY;

	public TankRemote(String name, double x, double y, double angle, Team team, double size, double ts, double tl, double r, double g, double b, double lives, double baselives)
	{
		super(name, x, y, size, r, g, b);
		this.angle = angle;
		this.orientation = angle;
		this.team = team;
		this.health = lives;
		this.baseHealth = baselives;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = null;
		this.turretSize = ts;
		this.turretLength = tl;
		this.invulnerable = true;
		this.description = "A tank controlled by the server";
	}
	
	public TankRemote(Tank t)
	{
		super(t.name, t.posX, t.posY, t.size, t.colorR, t.colorG, t.colorB, false);
		this.angle = t.angle;
		this.orientation = t.orientation;
		this.team = t.team;
		this.health = t.health;
		this.baseHealth = t.baseHealth;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = t;
		this.mandatoryKill = t.mandatoryKill;

		this.copyTank(t);

		this.invulnerable = true;
		this.networkID = t.networkID;
		Tank.idMap.put(this.networkID, this);
	}

	public void copyTank(Tank t)
	{
		this.turretLength = t.turretLength;
		this.turretSize = t.turretSize;
		this.colorR = t.colorR;
		this.colorG = t.colorG;
		this.colorB = t.colorB;
		this.secondaryColorR = t.secondaryColorR;
		this.secondaryColorG = t.secondaryColorG;
		this.secondaryColorB = t.secondaryColorB;
		this.enableTertiaryColor = t.enableTertiaryColor;
		this.tertiaryColorR = t.tertiaryColorR;
		this.tertiaryColorG = t.tertiaryColorG;
		this.tertiaryColorB = t.tertiaryColorB;
		this.emblem = t.emblem;
		this.emblemR = t.emblemR;
		this.emblemG = t.emblemG;
		this.emblemB = t.emblemB;
		this.description = t.description;
		this.baseModel = t.baseModel;
		this.colorModel = t.colorModel;
		this.turretBaseModel = t.turretBaseModel;
		this.turretModel = t.turretModel;
		this.mandatoryKill = t.mandatoryKill;
		this.luminance = t.luminance;
		this.glowIntensity = t.glowIntensity;
		this.glowSize = t.glowSize;
		this.lightSize = t.lightSize;
		this.lightIntensity = t.lightIntensity;
		this.bullet = t.bullet;
		this.mine = t.mine;
	}

	@Override
	public void update()
	{
		this.interpolatedProgress = Math.min(this.interpolatedProgress + Panel.frameFrequency, this.interpolationTime);

		this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
		this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

		this.localAge += Panel.frameFrequency;
		super.update();

		this.interpolatedPosX = this.posX;
		this.interpolatedPosY = this.posY;

		this.posX = this.posX + this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
		this.posY = this.posY + this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;
	}

	@Override
	public void draw()
	{
		double realX = this.posX;
		double realY = this.posY;

		this.posX = this.interpolatedPosX;
		this.posY = this.interpolatedPosY;

		if (!this.invisible || this.localAge <= 0 || this.destroy)
			super.draw();
		else
		{
			if (!this.vanished)
			{
				this.vanished = true;
				Drawing.drawing.playGlobalSound("transform.ogg", 1.2f);

				if (Game.effectsEnabled)
				{
					for (int i = 0; i < 50 * Game.effectMultiplier; i++)
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

						Game.effects.add(e);
					}
				}
			}

			for (int i = 0; i < Game.tile_size * 2 - this.localAge; i++)
			{
				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, (this.size * 2 - i - this.localAge) * 2.55);
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
			}
		}

		this.posX = realX;
		this.posY = realY;
	}
}

package tanks.tank;

import tanks.*;
import tanks.gui.screen.ScreenGame;

public class TankRemote extends Tank
{
	public final boolean isCopy;
	public final Tank tank;

	public boolean invisible = false;
	public boolean vanished = false;

	public double localAge = 0;

	public long lastUpdate = -1;

	public double interpolationTime = 1;

	public double prevKnownPosX;
	public double prevKnownPosY;
	public double prevKnownVX;
	public double prevKnownVY;
	public double prevKnownVXFinal;
	public double prevKnownVYFinal;

	public double currentKnownPosX;
	public double currentKnownPosY;
	public double currentKnownVX;
	public double currentKnownVY;

	public double lastAngle;
	public double lastPitch;
	public double currentAngle;
	public double currentPitch;

	public double timeSinceRefresh = 0;

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
		this.managedMotion = false;

		this.description = "A tank controlled by the server";
	}
	
	public TankRemote(Tank t)
	{
		super(t.name, t.posX, t.posY, t.size, t.colorR, t.colorG, t.colorB);
		this.angle = t.angle;
		this.orientation = t.orientation;
		this.team = t.team;
		this.health = t.health;
		this.baseHealth = t.baseHealth;
		this.isRemote = true;
		this.isCopy = false;
		this.tank = t;
		this.mandatoryKill = t.mandatoryKill;
		this.drawAge = t.drawAge;
		this.managedMotion = false;

		this.copyTank(t);

		this.invulnerable = true;

		this.setNetworkID(t.networkID);
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
		this.musicTracks = t.musicTracks;
		this.fromRegistry = t.fromRegistry;
		this.trackSpacing = t.trackSpacing;
		this.enableTracks = t.enableTracks;
	}

	@Override
	public void update()
	{
		if (this.localAge <= 0)
		{
			this.currentKnownPosX = this.posX;
			this.currentKnownPosY = this.posY;
			this.prevKnownPosX = this.posX;
			this.prevKnownPosY = this.posY;
		}

		this.timeSinceRefresh += Panel.frameFrequency;
		this.localAge += Panel.frameFrequency;

		super.update();

		double pvx = this.prevKnownVXFinal;
		double pvy = this.prevKnownVYFinal;
		double cvx = this.getAttributeValue(AttributeModifier.velocity, this.currentKnownVX) * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
		double cvy = this.getAttributeValue(AttributeModifier.velocity, this.currentKnownVY) * ScreenGame.finishTimer / ScreenGame.finishTimerMax;

		this.posX = cubicInterpolationVelocity(this.prevKnownPosX, pvx, this.currentKnownPosX, cvx, this.timeSinceRefresh, this.interpolationTime);
		this.posY = cubicInterpolationVelocity(this.prevKnownPosY, pvy, this.currentKnownPosY, cvy, this.timeSinceRefresh, this.interpolationTime);
		double frac = Math.min(1, this.timeSinceRefresh / this.interpolationTime);
		this.vX = (1 - frac) * this.prevKnownVX + frac * this.currentKnownVX;
		this.vY = (1 - frac) * this.prevKnownVY + frac * this.currentKnownVY;

		this.lastFinalVX = (this.posX - this.lastPosX) / Panel.frameFrequency;
		this.lastFinalVY = (this.posY - this.lastPosY) / Panel.frameFrequency;

		double angDiff = Movable.angleBetween(this.lastAngle, this.currentAngle);
		this.angle = this.lastAngle - frac * angDiff;
		this.pitch = (1 - frac) * this.lastPitch + frac * this.currentPitch;

		this.checkCollision();

		if (this.hasCollided)
		{
			this.prevKnownPosX = this.posX;
			this.prevKnownPosY = this.posY;
			this.prevKnownVX = this.vX;
			this.prevKnownVY = this.vY;
			this.prevKnownVXFinal = this.lastFinalVX;
			this.prevKnownVYFinal = this.lastFinalVY;
			this.lastAngle = this.angle;
			this.interpolationTime -= this.timeSinceRefresh;
			this.timeSinceRefresh = 0;
		}

		this.orientation = (this.orientation + Math.PI * 2) % (Math.PI * 2);

		if (!(Math.abs(this.posX - this.lastPosX) < 0.01 && Math.abs(this.posY - this.lastPosY) < 0.01) && !this.destroy && !ScreenGame.finished)
		{
			double dist = Math.sqrt(Math.pow(this.posX - this.lastPosX, 2) + Math.pow(this.posY - this.lastPosY, 2));

			double dir = Math.PI + this.getAngleInDirection(this.lastPosX, this.lastPosY);
			if (Movable.absoluteAngleBetween(this.orientation, dir) <= Movable.absoluteAngleBetween(this.orientation + Math.PI, dir))
				this.orientation -= Movable.angleBetween(this.orientation, dir) / 20 * dist;
			else
				this.orientation -= Movable.angleBetween(this.orientation + Math.PI, dir) / 20 * dist;
		}
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
	}

	public static double cubicInterpolationVelocity(double startPos, double startVel, double endPos, double endVel, double curTime, double totalTime)
	{
		double targetPos = endPos + endVel * totalTime;
		double frac = curTime / totalTime;

		if (frac > 1)
			return endPos + endVel * curTime;

		//return cubicInterpolation(startPos - startVel * totalTime, startPos, targetPos, targetPos + endVel * totalTime, frac);
		return cubicInterpolation2(startPos, startVel * totalTime, targetPos, endVel * totalTime, frac);
	}

	public static double cubicInterpolation(double v1, double v2, double v3, double v4, double frac)
	{
		double r = 0;
		r += v1 * (-0.5 * Math.pow(frac, 3) + Math.pow(frac, 2) - 0.5 * frac);
		r += v2 * (1.5 * Math.pow(frac, 3) - 2.5 * Math.pow(frac, 2) + 1);
		r += v3 * (-1.5 * Math.pow(frac, 3) + 2 * Math.pow(frac, 2) + 0.5 * frac);
		r += v4 * (0.5 * Math.pow(frac, 3) - 0.5 * Math.pow(frac, 2));
		return r;
	}

	public static double cubicInterpolation2(double p1, double v1, double p2, double v2, double frac)
	{
		double r = 0;
		r += (2 * Math.pow(frac, 3) - 3 * Math.pow(frac, 2) + 1) * p1;
		r += (Math.pow(frac, 3) - 2 * Math.pow(frac, 2) + frac) * v1;
		r += (-2 * Math.pow(frac, 3) + 3 * Math.pow(frac, 2)) * p2;
		r += (Math.pow(frac, 3) - Math.pow(frac, 2)) * v2;
		return r;
	}
}

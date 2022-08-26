package tanks.tank;

import tanks.*;
import tanks.event.EventTankTeleport;

/**
 * The orb that transfers the player's tank which is fired from the teleporter.
 * @see TankPlayer
 * @see tanks.obstacle.ObstacleTeleporter
 */
public class TeleporterOrb extends Movable
{
	public Tank tank;
	public double fX;
	public double fY;
	public double iX;
	public double iY;
	public double dX;
	public double dY;
	
	public double age = -100;
	public double maxAge = 200;
	public double endAge = 300;

	public double size;

	public TeleporterOrb(double x, double y, double iX, double iY, double destX, double destY, Tank t)
	{
		super(x, y);
		this.tank = t;
		this.size = t.size;
		this.drawLevel = 9;
		t.invulnerable = true;
		t.targetable = false;
		this.fX = x;
		this.fY = y;
		this.iX = iX;
		this.iY = iY;
		this.dX = destX;
		this.dY = destY;

		this.tank.tookRecoil = false;
		this.tank.inControlOfMotion = false;
		this.tank.positionLock = true;
		
		if (!t.isRemote)
			Game.eventsOut.add(new EventTankTeleport(this));
	}

    @Override
	public void draw() 
	{
		double frac = 1 - Math.min(this.posZ / 200, 1);

		//Drawing.drawing.setColor(255, 255, 255);
		Drawing.drawing.setColor(this.tank.colorR * (1 - frac) + 255 * frac, this.tank.colorG * (1 - frac) + 255 * frac, this.tank.colorB * (1 - frac) + 255 * frac);

		if (Game.enable3d)
			Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, (this.size - this.tank.size) / 2, (this.size - this.tank.size) / 2, true, true);

		for (int i = 0; i < this.size - this.tank.size; i++)
		{
			Drawing.drawing.setColor(this.tank.colorR * (1 - frac) + 255 * frac, this.tank.colorG * (1 - frac) + 255 * frac, this.tank.colorB * (1 - frac) + 255 * frac, 20);
			//Drawing.drawing.setColor(255, 255, 255, 20);

			if (Game.enable3d)
				Drawing.drawing.fillOval(this.posX, this.posY, this.posZ, i, i, false, true);
			else
				Drawing.drawing.fillOval(this.posX, this.posY, i, i);
		}

		//if (this.tank.size == 0)
		//	Game.effects.add(Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.glow));
	}
	
	@Override
	public void update()
	{
		float freq = (float) (Panel.frameFrequency / 10);

		if (Game.game.window.touchscreen)
			freq = 1;

		this.age += Panel.frameFrequency;

		this.tank.vX = 0;
		this.tank.vY = 0;

		if (this.age > this.endAge)
		{
			Game.removeMovables.add(this);
			this.tank.invulnerable = false;
			this.tank.targetable = true;
			this.tank.inControlOfMotion = true;
			this.tank.positionLock = false;

			for (int i = 0; i < 100 * Game.effectMultiplier; i++)
			{
				this.createEffect();
			}

			this.tank.size = this.size;
			return;
		}

		double frac = (Math.sin((this.maxAge - this.age) / this.maxAge * Math.PI - Math.PI / 2) + 1) / 2;
		double frac2 = (Math.sin((-this.age + 50) / 50 * Math.PI - Math.PI / 2) + 1) / 2;

		this.posZ = Math.sin((this.maxAge - Math.max(0, Math.min(this.maxAge, this.age))) / this.maxAge * Math.PI) *
				Math.sqrt(Math.pow(this.dX - this.iX, 2) + Math.pow(this.dY - this.iY, 2)) / 2;

		if (this.age <= 0)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1, 0.25f * freq);

			frac = 1;
		}
		else if (this.age >= this.maxAge)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1, 0.25f * freq);

			frac = 0;
		}

		if (this.tank == Game.playerTank)
			Drawing.drawing.playSound("teleport2.ogg", (float) (Math.sin((Math.min(Math.max(this.age, 0), this.maxAge) / this.maxAge) * Math.PI) / 4 + 0.5), freq * (1 - (float) (tank.size / size)) / 4f);


		if (this.age <= -50)
		{
			this.posX = this.fX * frac2 + this.iX * (1 - frac2);
			this.posY = this.fY * frac2 + this.iY * (1 - frac2);
		}
		else
		{
			this.posX = this.iX * frac + this.dX * (1 - frac);
			this.posY = this.iY * frac + this.dY * (1 - frac);
		}
		
		this.tank.posX = this.posX;
		this.tank.posY = this.posY;
		this.tank.posZ = this.posZ;
		
		double size = Math.max(-this.age / 100, Math.max((this.age - this.maxAge) / (this.endAge - this.maxAge), 0));
		
		this.tank.size = size * this.size;

		this.tank.disabled = this.tank.size <= 0;

		if (Math.random() < Panel.frameFrequency * Game.effectMultiplier)
			this.createEffect();

		super.update();
	}
	
	public void createEffect()
	{
		if (!Game.effectsEnabled)
			return;

		Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.teleporterPiece);
		double var = 50;
		
		e.colR = Math.min(this.tank.colorR, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colG = Math.min(this.tank.colorG, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colB = Math.min(this.tank.colorB, Math.max(0, 255 + Math.random() * var - var / 2));
		e.drawLayer = 9;

		if (Game.enable3d)
			e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI * 2, Math.random() * 4);
		else
			e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 4);

		Game.effects.add(e);		
	}
}

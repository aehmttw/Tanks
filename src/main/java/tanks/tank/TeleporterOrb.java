package tanks.tank;

import tanks.*;
import tanks.event.EventTankTeleport;

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
	public double tSize;

	public TeleporterOrb(double x, double y, double iX, double iY, double destX, double destY, Tank t)
	{
		super(x, y);
		this.tank = t;
		this.size = t.size;
		this.tSize = t.turret.size;
		this.drawLevel = 9;
		t.invulnerable = true;
		t.targetable = false;
		this.fX = x;
		this.fY = y;
		this.iX = iX;
		this.iY = iY;
		this.dX = destX;
		this.dY = destY;
		
		if (!t.isRemote)
			Game.eventsOut.add(new EventTankTeleport(this));
	}

    @Override
	public void draw() 
	{
		for (int i = 0; i < this.size - this.tank.size; i++)
		{
			Drawing.drawing.setColor(255, 255, 255, 20);

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
		this.age += Panel.frameFrequency;

		if (this.age > this.endAge)
		{
			Game.removeMovables.add(this);
			this.tank.invulnerable = false;
			this.tank.targetable = true;

			for (int i = 0; i < 100; i++)
			{
				this.createEffect();
			}

			this.tank.size = this.size;
			this.tank.turret.size = this.tSize;
			return;

		}

		double frac = (Math.sin((this.maxAge - this.age) / this.maxAge * Math.PI - Math.PI / 2) + 1) / 2;
		double frac2 = (Math.sin((-this.age + 50) / 50 * Math.PI - Math.PI / 2) + 1) / 2;

		this.posZ = Math.sin((this.maxAge - Math.max(0, Math.min(this.maxAge, this.age))) / this.maxAge * Math.PI) *
				Math.sqrt(Math.pow(this.dX - this.iX, 2) + Math.pow(this.dY - this.iY, 2)) / 2;


		if (this.age <= 0)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1, 0.25f);

			frac = 1;
		}
		else if (this.age >= this.maxAge)
		{
			if (this.tank == Game.playerTank)
				Drawing.drawing.playSound("teleport1.ogg", 1, 0.25f);

			frac = 0;
		}

		if (this.tank == Game.playerTank)
			Drawing.drawing.playSound("teleport2.ogg", (float) (Math.sin((Math.min(Math.max(this.age, 0), this.maxAge) / this.maxAge) * Math.PI) / 4 + 0.5), (1 - (float) (tank.size / size)) / 4f);


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
		this.tank.turret.size = size * this.tSize;
		this.tank.turret.length = (int) this.tank.size;
		
		this.tank.disabled = this.tank.size <= 0;

		this.createEffect();

		super.update();
	}
	
	public void createEffect()
	{
		if (!Game.fancyGraphics)
			return;
		
		Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.teleporterPiece);
		double var = 50;
		
		e.colR = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));

		if (Game.enable3d)
			e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI * 2, Math.random() * 4);
		else
			e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 4);

		Game.effects.add(e);		
	}
}

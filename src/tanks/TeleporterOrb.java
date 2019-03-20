package tanks;

import tanks.tank.Tank;

public class TeleporterOrb extends Movable
{
	public Tank tank;
	double fX;
	double fY;
	double iX;
	double iY;
	double dX;
	double dY;
	
	double age = -100;
	double maxAge = 200;
	double endAge = 300;

	double size;
	double tSize;

	public TeleporterOrb(double x, double y, double iX, double iY, double destX, double destY, Tank t)
	{
		super(x, y);
		this.tank = t;
		this.size = t.size;
		this.tSize = t.turret.size;
		this.drawAbove = true;
		t.invulnerable = true;
		this.fX = x;
		this.fY = y;
		this.iX = iX;
		this.iY = iY;
		this.dX = destX;
		this.dY = destY;
	}

	@Override
	public void checkCollision()
	{
		
	}

	@Override
	public void draw() 
	{
		for (int i = 0; i < this.size - this.tank.size; i++)
		{
			Drawing.drawing.setColor(255, 255, 255, 20);
			Drawing.drawing.fillOval(this.posX, this.posY, i, i);
		}
	}
	
	@Override
	public void update()
	{
		this.age += Panel.frameFrequency;
	
		if (this.age > this.endAge)
		{
			Game.removeMovables.add(this);
			this.tank.invulnerable = false;

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
		
		if (this.age <= 0)
			frac = 1;
				
		if (this.age >= this.maxAge)
			frac = 0;
		
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
		Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
		int var = 50;
		
		e.colR = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colG = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
		e.colB = Math.min(255, Math.max(0, 255 + Math.random() * var - var / 2));
		e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 4);
		Game.effects.add(e);		
	}
}

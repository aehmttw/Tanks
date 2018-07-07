package tanks;

import java.awt.Color;
import java.awt.Graphics;

public abstract class Tank extends Movable
{
	public double angle = 0;

	public int coinValue = 0;
	
	public double accel = 0.1;
	public double maxV = 2.5;
	public int liveBullets = 0;
	public int liveMines = 0;
	public int size;
	public Color color;
	public int liveBulletMax;
	public int liveMinesMax;
	public double drawAge = 0;
	public double destroyTimer = 0;
	public boolean hasCollided = false;
	public double flashAnimation = 0;
	public double treadAnimation = 0;
	public boolean drawTread = false;

	public double lives = 1;
	
	public Turret turret;
	
	public Tank(double x, double y, int size, Color color) 
	{
		super(x, y);
		this.size = size;
		this.color = color;
		turret = new Turret(this);
	}

	@Override
	public void checkCollision() 
	{
		hasCollided = false;
		
		if (this.posX + this.size / 2 > Screen.sizeX)
		{
			this.posX = Screen.sizeX - this.size / 2;
			hasCollided = true;
		}
		if (this.posY + this.size / 2 > Screen.sizeY)
		{
			this.posY = Screen.sizeY - this.size / 2;
			hasCollided = true;
		}
		if (this.posX - this.size / 2 < 0)
		{
			this.posX = this.size / 2;
			hasCollided = true;
		}
		if (this.posY - this.size / 2 < 0)
		{
			this.posY = this.size / 2;
			hasCollided = true;
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);

			double horizontalDist = Math.abs(this.posX - o.posX);
			double verticalDist = Math.abs(this.posY - o.posY);

			double dx = this.posX - o.posX;
			double dy = this.posY - o.posY;

			double bound = this.size / 2 + Obstacle.obstacle_size / 2;

			if (horizontalDist < bound && verticalDist < bound)
			{
				if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
				{
					hasCollided = true;
					this.posX += horizontalDist - bound;
				}
				else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
					this.posY += verticalDist - bound;
				}
				else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
				{
					hasCollided = true;
					this.posX -= horizontalDist - bound;
				}
				else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
				{
					hasCollided = true;
					this.posY -= verticalDist - bound;
				}
			}
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (o instanceof Tank)
			{
				double horizontalDist = Math.abs(this.posX - o.posX);
				double verticalDist = Math.abs(this.posY - o.posY);

				double dx = this.posX - o.posX;
				double dy = this.posY - o.posY;

				double bound = this.size / 2 + ((Tank)o).size / 2;

				if (horizontalDist < bound && verticalDist < bound)
				{
					if (dx <= 0 && dx > 0 - bound && horizontalDist > verticalDist)
					{
						hasCollided = true;
						this.posX += horizontalDist - bound;
					}
					else if (dy <= 0 && dy > 0 - bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						this.posY += verticalDist - bound;
					}
					else if (dx >= 0 && dx < bound && horizontalDist > verticalDist)
					{
						hasCollided = true;
						this.posX -= horizontalDist - bound;
					}
					else if (dy >= 0 && dy < bound && horizontalDist < verticalDist)
					{
						hasCollided = true;
						this.posY -= verticalDist - bound;
					}
				}
			}
		}
	}

	@Override
	public void update()
	{	
		this.treadAnimation += Math.sqrt(this.vX * this.vX + this.vY * this.vY) * Panel.frameFrequency;
		
		if (this.treadAnimation > this.size * 4 / 5)
		{
			this.drawTread = true;
			this.treadAnimation -= this.size * 4 / 5;
		}
		
		this.flashAnimation = Math.max(0, this.flashAnimation - 0.05 * Panel.frameFrequency);
		if (destroy)
		{
			if (this.destroyTimer <= 0 && this.lives <= 0 && Game.graphicalEffects)
			{
				for (int i = 0; i < this.size * 4; i++)
				{
					Effect e = new Effect(this.posX, this.posY, Effect.EffectType.piece);
					int var = 50;
					e.col = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
					e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0);
					Game.effects.add(e);
				}
			}
			
			this.destroyTimer += Panel.frameFrequency;
		}
		
		if (this.destroyTimer > Game.tank_size)
			Game.removeMovables.add(this);
		
		super.update();
	}

	public abstract void shoot();

	@Override
	public void draw(Graphics g) 
	{
		drawAge += Panel.frameFrequency;
		g.setColor(new Color((int) ((this.color.getRed() * (1 - this.flashAnimation) + 255 * this.flashAnimation)), (int) (this.color.getGreen() * (1 - this.flashAnimation)), (int) (this.color.getBlue() * (1 - this.flashAnimation))));
		Screen.fillRect(g, this.posX, this.posY, (this.size - destroyTimer) * Math.min(this.drawAge / Game.tank_size, 1), (this.size - destroyTimer) * Math.min(this.drawAge / Game.tank_size, 1));
		if (this.lives > 1)
		{
			for (int i = 1; i < lives; i++)
			{
				Screen.drawRect(g, this.posX, this.posY, 8 * i + this.size - destroyTimer - Math.max(Game.tank_size - drawAge, 0), 8 * i + this.size - destroyTimer - Math.max(Game.tank_size - drawAge, 0));
			}
		}
		this.turret.draw(g, angle);
		
		if (this.drawTread)
		{
			this.drawTread = false;
			double a = this.getPolarDirection();
			Effect e1 = new Effect(this.posX, this.posY, Effect.EffectType.tread);
			Effect e2 = new Effect(this.posX, this.posY, Effect.EffectType.tread);
			e1.setPolarMotion(a - Math.PI / 2, this.size * 0.25);
			e2.setPolarMotion(a + Math.PI / 2, this.size * 0.25);
			e1.size = this.size / 5;
			e2.size = this.size / 5;
			e1.posX += e1.vX;
			e1.posY += e1.vY;
			e2.posX += e2.vX;
			e2.posY += e2.vY;
			e1.setPolarMotion(0, 0);
			e2.setPolarMotion(0, 0);
			Game.belowEffects.add(e1);
			Game.belowEffects.add(e2);
		}
		
	}

}

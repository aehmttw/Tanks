package tanks;

import java.awt.Color;
import java.awt.Graphics;

import tanks.tank.Tank;

public class Mine extends Movable
{
	public static int mine_size = 30;
	public double timer = 1000;
	public int size = mine_size;
	public Color outlineColor;
	
	public Tank tank;

	public Mine(double x, double y, Tank t) 
	{
		super(x, y);
		tank = t;
		t.liveMines++;
		this.team = t.team;
		this.outlineColor = Team.getObjectColor(t.color, t);
	}

	@Override
	public void checkCollision() {	}

	@Override
	public void draw(Graphics p) 
	{	
		p.setColor(this.outlineColor);
		Drawing.fillOval(p, this.posX, this.posY, this.size, this.size);
		
		p.setColor(new Color(255, (int) ((this.timer) / 1000.0 * 255), 0));

		if (timer < 150 && ((int) timer % 16) / 8 == 1)
			p.setColor(Color.yellow);

		Drawing.fillOval(p, this.posX, this.posY, this.size * 0.8, this.size * 0.8);
	}

	@Override
	public void update()
	{
		this.timer -= Panel.frameFrequency;

		if (destroy)
			this.explode();

		if (this.timer <= 0)
			this.explode();
		super.update();

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable o = Game.movables.get(i);
			if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(Game.tank_size * 1.5, 2))
			{
				if (o instanceof Tank && !o.destroy && !o.equals(this.tank))
				{
					this.timer = Math.min(150, this.timer);
				}		
			}
		}
	}

	public void explode()
	{
		Drawing.playSound("resources/explosion.wav");

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.graphicalEffects)
			{
				for (int j = 0; j < 50; j++)
				{
					double random = Math.random();
					Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
					e.maxAge /= 2;
					e.col = new Color(255, (int) ((1 - random) * 155 + Math.random() * 100), 0);
					e.setPolarMotion(Math.random() * 2 * Math.PI, random * 4);
					Game.effects.add(e);
				}
			}


			Movable o = Game.movables.get(i);
			if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(Game.tank_size * 2.5, 2))
			{
				if (o instanceof Tank && !o.destroy)
				{
					this.destroy = true;

					if (!(Team.isAllied(this, o) && !this.team.friendlyFire))
					{
						((Tank) o).lives -= 2;
						((Tank)o).flashAnimation = 1;

						if (((Tank)o).lives <= 0)
						{
							((Tank)o).flashAnimation = 0;
							o.destroy = true;
							if (o.equals(Game.player))
								Game.coins -= 5;		
							if (this.tank.equals(Game.player))
								Game.coins += ((Tank)o).coinValue;
						}	
					}
				}		
				else if (o instanceof Mine && !o.destroy)
				{
					o.destroy = true;
				}		
			}
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Obstacle o = Game.obstacles.get(i);
			if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(Game.tank_size * 2.5, 2) && o.destructible)
			{
				Game.removeObstacles.add(o);

				if (Game.graphicalEffects)
				{
					for (int j = 0; j < Obstacle.obstacle_size - 4; j += 4)
					{
						for (int k = 0; k < Obstacle.obstacle_size - 4; k += 4)
						{
							int oX = 0;
							int oY = 0;

							/*if (j == 0)
							oX += 2;
						if (k == 0)
							oY += 2;

						if (j == Obstacle.obstacle_size - 8)
							oX -= 2;
						if (k == Obstacle.obstacle_size - 8)
							oY -= 2;*/

							Effect e = Effect.createNewEffect(o.posX + j + oX + 2 - Obstacle.obstacle_size / 2, o.posY + k + oY + 2 - Obstacle.obstacle_size / 2, Effect.EffectType.obstaclePiece);
							e.col = o.color;

							double dist = Movable.distanceBetween(this, e);
							double angle = this.getAngleInDirection(e.posX, e.posY);
							e.addPolarMotion(angle, (200 * Math.sqrt(2) - dist) / 400 + Math.random() * 2);

							Game.effects.add(e);

						}
					}
				}
			}
		}

		tank.liveMines--;
		Game.effects.add(Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.mineExplosion));

		Game.removeMovables.add(this);
	}

}

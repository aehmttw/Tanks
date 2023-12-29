package tanks.tank;

import tanks.Effect;
import tanks.Game;
import tanks.Movable;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Face;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;

public class Ray
{
	public double size = 10;
	public double tankHitSizeMul = 1;

	public int bounces;
	public int bouncyBounces = 100;
	public double posX;
	public double posY;
	public double vX;
	public double vY;

	public boolean enableBounciness = true;
	public boolean ignoreDestructible = false;
	public boolean ignoreShootThrough = false;

	public boolean trace = Game.traceAllRays;
	public boolean dotted = false;

	public double speed = 10;

	public double age = 0;
	public int traceAge;

	public Tank tank;
	public Tank targetTank;

	public ArrayList<Double> bounceX = new ArrayList<>();
	public ArrayList<Double> bounceY = new ArrayList<>();

	public double targetX;
	public double targetY;

	public Ray(double x, double y, double angle, int bounces, Tank tank)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Ray(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);

		this.posX = x;
		this.posY = y;
		this.bounces = bounces;

		this.tank = tank;
	}

	public Movable getTarget(double mul, Tank targetTank)
	{
		this.targetTank = targetTank;
		this.targetTank.size *= mul;

		for (Face f: this.targetTank.getHorizontalFaces())
			Game.horizontalFaces.remove(f);

		for (Face f: this.targetTank.getVerticalFaces())
			Game.verticalFaces.remove(f);

		for (Face f: this.targetTank.getHorizontalFaces())
			addFace(f);

		for (Face f: this.targetTank.getVerticalFaces())
			addFace(f);

		Movable m = this.getTarget();

		for (Face f: this.targetTank.getHorizontalFaces())
			Game.horizontalFaces.remove(f);

		for (Face f: this.targetTank.getVerticalFaces())
			Game.verticalFaces.remove(f);

		this.targetTank.size /= mul;

		for (Face f: this.targetTank.getHorizontalFaces())
			addFace(f);

		for (Face f: this.targetTank.getVerticalFaces())
			addFace(f);

		return m;
	}

	public void addFace(Face f)
	{
		if (f.horizontal)
		{
			int a = 0;
			int b = Game.horizontalFaces.size() - 1;

			boolean added = false;
			int m = 0;
			while (a <= b)
			{
				m = (a + b) / 2;

				if (Game.horizontalFaces.get(m).startY < f.startY)
					a = m + 1;
				else if (Game.horizontalFaces.get(m).startY > f.startY)
					b = m - 1;
				else
				{
					added = true;
					Game.horizontalFaces.add(m, f);
					break;
				}
			}

			if (!added)
				Game.horizontalFaces.add(m, f);
		}
		else
		{
			int a = 0;
			int b = Game.verticalFaces.size() - 1;

			boolean added = false;
			int m = 0;
			while (a <= b)
			{
				m = (a + b) / 2;

				if (Game.verticalFaces.get(m).startX < f.startX)
					a = m + 1;
				else if (Game.verticalFaces.get(m).startX > f.startX)
					b = m - 1;
				else
				{
					added = true;
					Game.verticalFaces.add(m, f);
					break;
				}
			}

			if (!added)
				Game.verticalFaces.add(m, f);
		}
	}

	public Movable getTarget()
	{
		double remainder = 0;

		if (isInsideObstacle(this.posX - size / 2, this.posY - size / 2) ||
				isInsideObstacle(this.posX + size / 2, this.posY - size / 2) ||
				isInsideObstacle(this.posX + size / 2, this.posY + size / 2) ||
				isInsideObstacle(this.posX - size / 2, this.posY + size / 2))
			return null;

		for (Movable m: Game.movables)
		{
			if (m instanceof Tank && m != this.tank)
			{
				Tank t = (Tank) m;
				if (this.posX + this.size / 2 >= t.posX - t.size / 2 &&
						this.posX - this.size / 2 <= t.posX + t.size / 2 &&
						this.posY + this.size / 2 >= t.posY - t.size / 2 &&
						this.posY - this.size / 2 <= t.posY + t.size / 2)
					return t;
			}
		}

		boolean firstBounce = this.targetTank == null;

		while (this.bounces >= 0 && this.bouncyBounces >= 0)
		{
			double t = Double.MAX_VALUE;
			double collisionX = -1;
			double collisionY = -1;
			Face collisionFace = null;

			if (vX > 0)
			{
				for (int i = 0; i < Game.verticalFaces.size(); i++)
				{
					double size = this.size;

					Face f = Game.verticalFaces.get(i);
					if (f.owner instanceof Movable)
						size *= tankHitSizeMul;

					boolean passThrough = false;
					if (f.owner instanceof Obstacle)
					{
						Obstacle o = (Obstacle) f.owner;

						if (!o.bouncy)
							passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
					}

					if (f.startX < this.posX + size / 2 || !f.solidBullet || !f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
						continue;

					double y = (f.startX - size / 2 - this.posX) * vY / vX + this.posY;
					if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
					{
						t = (f.startX - size / 2 - this.posX) / vX;
						collisionX = f.startX - size / 2;
						collisionY = y;
						collisionFace = f;
						break;
					}
				}
			}
			else if (vX < 0)
			{
				for (int i = Game.verticalFaces.size() - 1; i >= 0; i--)
				{
					Face f = Game.verticalFaces.get(i);

					double size = this.size;

					if (f.owner instanceof Movable)
						size *= tankHitSizeMul;


					boolean passThrough = false;
					if (f.owner instanceof Obstacle)
					{
						Obstacle o = (Obstacle) f.owner;

						if (!o.bouncy)
							passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
					}

					if (f.startX > this.posX - size / 2 || !f.solidBullet || f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
						continue;

					double y = (f.startX + size / 2 - this.posX) * vY / vX + this.posY;
					if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
					{
						t = (f.startX + size / 2 - this.posX) / vX;
						collisionX = f.startX + size / 2;
						collisionY = y;
						collisionFace = f;
						break;
					}
				}
			}

			boolean corner = false;
			if (vY > 0)
			{
				for (int i = 0; i < Game.horizontalFaces.size(); i++)
				{
					Face f = Game.horizontalFaces.get(i);

					double size = this.size;

					if (f.owner instanceof Movable)
						size *= tankHitSizeMul;

					boolean passThrough = false;
					if (f.owner instanceof Obstacle)
					{
						Obstacle o = (Obstacle) f.owner;

						if (!o.bouncy)
							passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
					}

					if (f.startY < this.posY + size / 2 || !f.solidBullet || !f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
						continue;

					double x = (f.startY - size / 2 - this.posY) * vX / vY + this.posX;
					if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
					{
						double t1 = (f.startY - size / 2 - this.posY) / vY;

						if (t1 == t)
							corner = true;
						else if (t1 < t)
						{
							collisionX = x;
							collisionY = f.startY - size / 2;
							collisionFace = f;
							t = t1;
						}

						break;
					}
				}
			}
			else if (vY < 0)
			{
				for (int i = Game.horizontalFaces.size() - 1; i >= 0; i--)
				{
					Face f = Game.horizontalFaces.get(i);

					double size = this.size;

					if (f.owner instanceof Movable)
						size *= tankHitSizeMul;

					boolean passThrough = false;
					if (f.owner instanceof Obstacle)
					{
						Obstacle o = (Obstacle) f.owner;

						if (!o.bouncy)
							passThrough = (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
					}

					if (f.startY > this.posY - size / 2 || !f.solidBullet || f.positiveCollision || (f.owner == this.tank && firstBounce) || passThrough)
						continue;

					double x = (f.startY + size / 2 - this.posY) * vX / vY + this.posX;
					if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
					{
						double t1 = (f.startY + size / 2 - this.posY) / vY;

						if (t1 == t)
							corner = true;
						else if (t1 < t)
						{
							collisionX = x;
							collisionY = f.startY + size / 2;
							collisionFace = f;
							t = t1;
						}
						break;
					}
				}
			}

			this.age += t;

			firstBounce = false;

			if (collisionFace != null)
			{
				if (trace)
				{
					double dx = collisionX - posX;
					double dy = collisionY - posY;

					double steps = (Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) + 1);

					if (dotted)
						steps /= 2;

					double s;
					for (s = remainder; s <= steps; s++)
					{
						double x = posX + dx * s / steps;
						double y = posY + dy * s / steps;

						this.traceAge++;

						double frac = 1 / (1 + this.traceAge / 100.0);
						double z = this.tank.size / 2 + this.tank.turretSize / 2 * frac + (Game.tile_size / 4) * (1 - frac);
						if (Game.screen instanceof ScreenGame && !ScreenGame.finished)
							Game.effects.add(Effect.createNewEffect(x, y, z, Effect.EffectType.ray));
					}

					remainder = s - steps;
				}

				this.posX = collisionX;
				this.posY = collisionY;

				if (collisionFace.owner instanceof Movable)
				{
					this.targetX = collisionX;
					this.targetY = collisionY;
					bounceX.add(collisionX);
					bounceY.add(collisionY);

					return (Movable) collisionFace.owner;
				}
				else if (collisionFace.owner instanceof Obstacle && ((Obstacle) collisionFace.owner).bouncy)
					this.bouncyBounces--;
				else if (collisionFace.owner instanceof Obstacle && !((Obstacle) collisionFace.owner).allowBounce)
					this.bounces = -1;
				else
					this.bounces--;

				bounceX.add(collisionX);
				bounceY.add(collisionY);

				if (this.bounces >= 0)
				{
					if (corner)
					{
						this.vX = -this.vX;
						this.vY = -this.vY;
					}
					else if (collisionFace.horizontal)
						this.vY = -this.vY;
					else
						this.vX = -this.vX;
				}
			}
			else
				return null;
		}

		return null;
	}

	public double getDist()
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		this.getTarget();

		double dist = 0;
		for (int i = 0; i < this.bounceX.size() - 1; i++)
		{
			dist += Math.sqrt(Math.pow(this.bounceX.get(i + 1) - this.bounceX.get(i), 2) + Math.pow(this.bounceY.get(i + 1) - this.bounceY.get(i), 2));
		}

		return dist;
	}

	public double getTargetDist(double mul, Tank m)
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		if (this.getTarget(mul, m) != m)
			return -1;

		double dist = 0;
		for (int i = 0; i < this.bounceX.size() - 1; i++)
		{
			dist += Math.sqrt(Math.pow(this.bounceX.get(i + 1) - this.bounceX.get(i), 2) + Math.pow(this.bounceY.get(i + 1) - this.bounceY.get(i), 2));
		}

		return dist;
	}

	public double getAngleInDirection(double x, double y)
	{
		x -= this.posX;
		y -= this.posY;

		double angle = 0;
		if (x > 0)
			angle = Math.atan(y/x);
		else if (x < 0)
			angle = Math.atan(y/x) + Math.PI;
		else
		{
			if (y > 0)
				angle = Math.PI / 2;
			else if (y < 0)
				angle = Math.PI * 3 / 2;
		}

		return angle;
	}

	public static boolean isInsideObstacle(double x, double y)
	{
		int ox = (int) (x / Game.tile_size);
		int oy = (int) (y / Game.tile_size);

		return !(ox >= 0 && ox < Game.currentSizeX && oy >= 0 && oy < Game.currentSizeY) || Game.game.solidGrid[ox][oy];
	}

	public void moveOut(double amount)
	{
		this.posX += this.vX * amount;
		this.posY += this.vY * amount;
	}
}

package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;

import java.util.ArrayList;

public class Firework extends Movable
{
	public static double trailLength = 50;

	public enum FireworkType {rocket, particle, trail, flash}
	public FireworkType type;

	public double age = 0;
	public double maxAge = (int) (Math.random() * 80 + 80);

	public boolean exploded = false;
	public double rotationMultiplier = Math.random() * 4 - 2;

	public double size = 8;

	public double colorR;
	public double colorG;
	public double colorB;

	public double aY = 0.0625 / 2;

	public FireworkPosition position;
	public FireworkPosition lastPosition;

	public Firework(FireworkType t, double x, double y, ArrayList<Firework> list)
	{
		super(x, y);

		this.type = t;
		this.posX = x;
		this.posY = y;
		list.add(this);
	}

	public void setRandomColor()
	{
		double col = (Math.random() * 255 * 6);
		double r = 0;
		double g = 0;
		double b = 0;

		if (col <= 255)
		{
			r = 255;
			g = col;
			b = 0;
		}
		else if (col <= 255 * 2)
		{
			r = 255 * 2 - col;
			g = 255;
			b = 0;
		}
		else if (col <= 255 * 3)
		{
			g = 255;
			b = col - 255 * 2;
		}
		else if (col <= 255 * 4)
		{
			g = 255 * 4 - col;
			b = 255;
		}
		else if (col <= 255 * 5)
		{
			r = col - 255 * 4;
			g = 0;
			b = 255;
		}
		else if (col <= 255 * 6)
		{
			r = 255;
			g = 0;
			b = 255 * 6 - col;
		}

		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
	}

	public void setVelocity()
	{
		vY = -Math.random() * 1.5 - 6;
		vX = Math.random() * 5 - 2.5;
		vZ = Math.random() * 5 - 2.5;
	}

	public void drawUpdate(ArrayList<Firework> current, ArrayList<Firework> next)
	{
		if (!this.exploded)
		{
			this.posX += this.vX * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
			this.posY += this.vY * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
			this.posZ += this.vZ * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		}

		if (type == FireworkType.rocket)
		{
			if (this.position == null)
			{
				this.position = new FireworkPosition(this);
				this.lastPosition = this.position;
				this.position.isFirst = true;
			}

			if (!this.exploded)
			{
				FireworkPosition pos = new FireworkPosition(this);
				this.lastPosition.next = pos;
				this.lastPosition = pos;
			}

			Game.game.window.shapeRenderer.setBatchMode(true, false, false, false, false);
			this.position.draw();
			Game.game.window.shapeRenderer.setBatchMode(false, false, false, false, false);

			while (this.position.next != null && this.position.expired)
			{
				this.position = this.position.next;
			}

			if (!this.exploded)
			{
				this.vY += aY * Panel.frameFrequency;

				Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

				if (!Game.enable3d)
					Drawing.drawing.fillInterfaceOval(posX, posY, this.size, this.size);
				else
					Drawing.drawing.fillInterfaceOval(posX, posY, posZ, this.size, this.size);
			}

			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, next);
			f.posZ = this.posZ;
			f.maxAge = 30;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;

			f.size = this.size;*/

			if (this.age >= this.maxAge && !this.exploded)
			{
				this.exploded = true;

				Drawing.drawing.playSound("destroy.ogg", 0.75f, 0.75f);

				double powerBase = Math.random() * 2 + 1;
				double powerRandom = Math.random() * 2;

				double limit = Math.random() * 50 + 50;

				for (int i = 0; i < limit; i++)
				{
					Firework e = new Firework(FireworkType.particle, this.posX, this.posY, next);
					e.size = 4 + Math.random() * 2;
					double var = 50;
					e.colorR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
					e.colorG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
					e.colorB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));
					e.posZ = this.posZ;
					e.vX = this.vX;
					e.vY = this.vY;
					e.vZ = this.vZ;

					double power = Math.random() * powerRandom + powerBase;

					if (!Game.enable3d)
						e.addPolarMotion(Math.random() * 2 * Math.PI, Math.random() * power);
					else
						e.add3dPolarMotion(Math.random() * Math.PI * 2, Math.asin(Math.random() * 2 - 1), power);

					e.maxAge = e.size * 40;
				}

				Firework e = new Firework(FireworkType.flash, this.posX, this.posY, next);
				e.colorR = this.colorR;
				e.colorG = this.colorG;
				e.colorB = this.colorB;
				e.posZ = this.posZ;
				e.vX = this.vX;
				e.vY = this.vY;
				e.vZ = this.vZ;
				e.maxAge = 25;
			}

			if (this.age < this.maxAge + trailLength)
				next.add(this);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));

			double s = Math.max(0, this.size - (this.age * this.size / this.maxAge));

			if (!Game.enable3d)
				Drawing.drawing.fillInterfaceOval(posX, posY, s, s);
			else
				Drawing.drawing.fillInterfaceOval(posX, posY, posZ, s, s);

			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}
		else if (type == FireworkType.particle)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));
			this.vY += 0.0625 * Panel.frameFrequency / 2;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity);
			double s = this.size - (this.age * this.size / this.maxAge);

			if (!Game.enable3d)
				Drawing.drawing.fillInterfaceOval(posX, posY, s, s);
			else
				Drawing.drawing.fillInterfaceOval(posX, posY, posZ, s, s);

			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = opacity / 50;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;
			f.size = this.size;
			this.list.add(f);*/

			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}
		else if (type == FireworkType.flash)
		{
			if (this.age < this.maxAge)
			{
				next.add(this);
			}
		}

		this.age += Panel.frameFrequency;
	}

	public void drawGlow()
	{
		if (type == FireworkType.rocket)
		{
			Game.game.window.shapeRenderer.setBatchMode(true, false, false, true, false);
			this.position.drawGlow();
			Game.game.window.shapeRenderer.setBatchMode(false, false, false, true, false);

			if (this.exploded)
				return;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

			if (Game.enable3d)
			{
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 4, this.size * 4);
				Drawing.drawing.setColor(this.colorR / 2, this.colorG / 2, this.colorB / 2);
				Drawing.drawing.fillInterfaceGlowSparkle(posX, posY, posZ, this.size * 4, this.age / 100.0 * rotationMultiplier);
			}
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 4, this.size * 4);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 2, this.size * 2);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 2, this.size * 2);
		}
		else if (type == FireworkType.particle)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
			{
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 8, this.size * 8);
				Drawing.drawing.setColor(this.colorR / 2, this.colorG / 2, this.colorB / 2);
				double s = Math.max(0, this.size - (this.age * this.size / this.maxAge));
				Drawing.drawing.fillInterfaceGlowSparkle(posX, posY, posZ, s * 4, this.age / 100.0 * rotationMultiplier);
			}
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 8, this.size * 8);
		}
		else if (type == FireworkType.flash)
		{
			double opacity = Math.min(255, Math.max(0, (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 40, this.size * 40);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 40, this.size * 40);
		}

	}

	@Override
	public void draw() { }

	public static class FireworkPosition
	{
		public double posX;
		public double posY;
		public double posZ;
		public double age;
		public boolean isFirst = false;

		public double oX;
		public double oY;

		public double maxAge = trailLength;
		public boolean expired = false;

		public Firework firework;

		public FireworkPosition next;

		public FireworkPosition(Firework f)
		{
			this.posX = f.posX;
			this.posY = f.posY;
			this.posZ = f.posZ;
			this.age = 0;

			this.firework = f;

			double speed = Math.sqrt(f.vX * f.vX + f.vY * f.vY);
			this.oX = -f.vY / speed / 2;
			this.oY = f.vX / speed / 2;

			if (speed == 0)
			{
				this.oX = 0.5;
				this.oY = 0;
			}
		}

		public void draw()
		{
			double s = Math.max(0, this.firework.size - (this.age * this.firework.size / this.maxAge)) * 0.75;
			double a = 0.5;

			if (s < 0)
				s = 0;

			if (next != null)
			{
				double s2 = Math.max(0, next.firework.size - (next.age * this.firework.size / next.maxAge)) * 0.75;

				if (s2 < 0)
				{
					this.expired = true;
					return;
				}

				Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

				Drawing.drawing.addInterfaceVertex(this.posX + this.oX * s, this.posY + this.oY * s, this.posZ);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				Drawing.drawing.setColor(next.firework.colorR, next.firework.colorG, next.firework.colorB, a * Math.max(0, Math.min(255, 255 - (next.age * 255.0 / next.maxAge))));

				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);

				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);
				Drawing.drawing.addInterfaceVertex(next.posX - next.oX * s2, next.posY - next.oY * s2, next.posZ);

				Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));

				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				this.next.draw();
			}

			if (next == null || isFirst)
			{
				int sign = 1;

				if (next == null)
					sign = -1;

				double angle = Movable.getPolarDirection(this.oX, this.oY);

				int sides = 12;
				for (int i = 0; i < sides; i++)
				{
					double a1 = angle + Math.PI * i / sides * sign;
					double a2 = angle + Math.PI * (i + 1) / sides * sign;

					Drawing.drawing.setColor(firework.colorR, firework.colorG, firework.colorB, a * Math.max(0, Math.min(255, 255 - (this.age * 255.0 / this.maxAge))));
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a1) * s / 2, this.posY + Math.sin(a1) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a2) * s / 2, this.posY + Math.sin(a2) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				}
			}

			this.age += Panel.frameFrequency;
		}

		public void drawGlow()
		{
			double s = Math.max(0, this.firework.size - (this.age * this.firework.size / this.maxAge)) * 3;
			double a = Math.min(255, 255 - (this.age * 255.0 / this.maxAge)) / 255.0 * 0.75;

			if (next != null)
			{
				double s2 = Math.max(0, next.firework.size - (next.age * this.firework.size / next.maxAge)) * 3;

				double a2 = Math.min(255, 255 - (next.age * 255.0 / next.maxAge)) / 255.0 * 0.75;

				if (s < 0)
					s = 0;

				if (s2 < 0)
				{
					this.expired = true;
					return;
				}

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX + this.oX * s, this.posY + this.oY * s, this.posZ);

				Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);
				Drawing.drawing.addInterfaceVertex(next.posX + next.oX * s2, next.posY + next.oY * s2, next.posZ);

				Drawing.drawing.setColor(firework.colorR * a2, firework.colorG * a2, firework.colorB * a2);
				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);

				Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);

				Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				Drawing.drawing.setColor(firework.colorR * a2, firework.colorG * a2, firework.colorB * a2);
				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);

				Drawing.drawing.addInterfaceVertex(next.posX, next.posY, next.posZ);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(next.posX - next.oX * s2, next.posY - next.oY * s2, next.posZ);

				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.addInterfaceVertex(this.posX - this.oX * s, this.posY - this.oY * s, this.posZ);

				this.next.drawGlow();
			}

			if (next == null || isFirst)
			{
				int sign = 1;

				if (next == null)
					sign = -1;

				double angle = Movable.getPolarDirection(this.oX, this.oY);

				int sides = 12;
				for (int i = 0; i < sides; i++)
				{
					double a1 = angle + Math.PI * i / sides * sign;
					double a2 = angle + Math.PI * (i + 1) / sides * sign;

					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a1) * s / 2, this.posY + Math.sin(a1) * s / 2, this.posZ);
					Drawing.drawing.addInterfaceVertex(this.posX + Math.cos(a2) * s / 2, this.posY + Math.sin(a2) * s / 2, this.posZ);
					Drawing.drawing.setColor(firework.colorR * a, firework.colorG * a, firework.colorB * a);
					Drawing.drawing.addInterfaceVertex(this.posX, this.posY, this.posZ);
				}
			}
		}
	}
}

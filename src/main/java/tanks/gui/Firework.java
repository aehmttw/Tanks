package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;

import java.util.ArrayList;

public class Firework extends Movable
{
	public enum FireworkType {rocket, particle, trail, flash}
	public FireworkType type;

	double age = 0;
	int maxAge = (int) (Math.random() * 80 + 80);

	int size = 8;

	public double colorR;
	public double colorG;
	public double colorB;

	public double aY = 0.0625 / 2;

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
		this.posX += this.vX * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		this.posY += this.vY * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		this.posZ += this.vZ * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;

		if (type == FireworkType.rocket)
		{
			this.vY += aY * Panel.frameFrequency;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

			if (!Game.enable3d)
				Drawing.drawing.fillInterfaceOval(posX, posY, this.size, this.size);
			else
				Drawing.drawing.fillInterfaceOval(posX, posY, posZ, this.size, this.size);

			Firework f = new Firework(FireworkType.trail, this.posX, this.posY, next);
			f.posZ = this.posZ;
			f.maxAge = 30;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;

			f.size = this.size;

			if (this.age >= this.maxAge)
			{
				Drawing.drawing.playSound("destroy.ogg", 0.75f, 0.75f);

				for (int i = 0; i < 50; i++)
				{
					Firework e = new Firework(FireworkType.particle, this.posX, this.posY, next);
					e.size = 4;
					double var = 50;
					e.colorR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
					e.colorG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
					e.colorB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));
					e.posZ = this.posZ;
					e.vX = this.vX;
					e.vY = this.vY;
					e.vZ = this.vZ;

					double power = Math.random() * 1 + 2;

					if (!Game.enable3d)
						e.addPolarMotion(Math.random() * 2 * Math.PI, Math.random() * power);
					else
						e.add3dPolarMotion(Math.random() * Math.PI * 2, Math.asin(Math.random() * 2 - 1), power);

					e.maxAge = 200;
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
			else
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
			int opacity = Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));
			this.vY += 0.0625 * Panel.frameFrequency / 2;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity);
			double s = this.size - (int) (this.age * this.size / this.maxAge);

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
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 4, this.size * 4);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 4, this.size * 4);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 2, this.size * 2);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 2, this.size * 2);
		}
		else if (type == FireworkType.particle)
		{
			int opacity = Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 8, this.size * 8);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 8, this.size * 8);
		}
		else if (type == FireworkType.flash)
		{
			int opacity = Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);

			if (Game.enable3d)
				Drawing.drawing.fillInterfaceGlow(posX, posY, posZ, this.size * 40, this.size * 40);
			else
				Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 40, this.size * 40);
		}

	}

	@Override
	public void draw() { }
}

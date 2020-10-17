package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;

import java.util.ArrayList;

public class Firework extends Movable
{
	public enum FireworkType {rocket, particle, trail}
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
	}

	public void drawUpdate(ArrayList<Firework> current, ArrayList<Firework> next)
	{
		if (type == FireworkType.rocket)
		{
			this.vY += aY * Panel.frameFrequency;

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);

			Drawing.drawing.fillInterfaceOval(posX, posY, this.size, this.size);

			Firework f = new Firework(FireworkType.trail, this.posX, this.posY, next);
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
					double power = Math.random() * 1 + 2;
					e.vX = this.vX;
					e.vY = this.vY;
					e.addPolarMotion(Math.random() * 2 * Math.PI, Math.random() * power);
					e.maxAge = 2 * 100;
				}
			}
			else
				next.add(this);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));

			double s = Math.max(0, this.size - (this.age * this.size / this.maxAge));
			Drawing.drawing.fillInterfaceOval(posX, posY, s, s);

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
			Drawing.drawing.fillInterfaceOval(posX, posY, s, s);

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

		this.posX += this.vX * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		this.posY += this.vY * Panel.frameFrequency / Drawing.drawing.interfaceScaleZoom;
		this.age += Panel.frameFrequency;
	}

	public void drawGlow()
	{
		if (type == FireworkType.rocket)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
			Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 4, this.size * 4);
		}
		else if (type == FireworkType.trail)
		{
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));
			Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 2, this.size * 2);
		}
		else if (type == FireworkType.particle)
		{
			int opacity = Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));

			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity / 2.0);
			Drawing.drawing.fillInterfaceGlow(posX, posY, this.size * 8, this.size * 8);
		}
	}

	@Override
	public void draw() { }
}

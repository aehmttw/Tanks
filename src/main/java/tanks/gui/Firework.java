package tanks.gui;

import tanks.Drawing;
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
	
	public ArrayList<Firework> list;
	public ArrayList<Firework> removeList;

	public double colorR;
	public double colorG;
	public double colorB;

	public Firework(FireworkType t, double x, double y, ArrayList<Firework> list, ArrayList<Firework> removeList)
	{
		super(x, y);

		this.type = t;
		this.posX = x;
		this.posY = y;
		list.add(this);
		this.list = list;
		this.removeList = removeList;
	}
	
	public void setRandomColor()
	{
		int col = (int) (Math.random() * 256 * 6);
		int r = 0;
		int g = 0;
		int b = 0;
		
		if (col < 256)
		{
			r = 255;
			g = col;
			b = 0;
		}
		else if (col < 256 * 2)
		{
			r = 256 * 2 - col - 1;
			g = 255;
			b = 0;
		}
		else if (col < 256 * 3)
		{
			g = 255;
			b = col - 256 * 2;
		}
		else if (col < 256 * 4)
		{
			g = 256 * 4 - col - 1;
			b = 255;
		}
		else if (col < 256 * 5)
		{
			r = col - 256 * 4;
			g = 0;
			b = 255;
		}
		else if (col < 256 * 6)
		{
			r = 255;
			g = 0;
			b = 256 * 6 - col - 1;
		}
		
		this.colorR = r;
		this.colorG = g;
		this.colorB = b;
	}
	
	public void drawUpdate()
	{
		if (type == FireworkType.rocket)
		{
			this.vY += 0.0625 * Panel.frameFrequency / 2;
			
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
			Drawing.drawing.fillOval(posX, posY, this.size, this.size);
			
			Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = 30;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;

			f.size = this.size;
			this.list.add(f);
			
			if (this.age >= this.maxAge)
			{
				Drawing.drawing.playSound("/destroy.wav");

				removeList.add(this);
				
				for (int i = 0; i < 50; i++)
				{
					Firework e = new Firework(FireworkType.particle, this.posX, this.posY, this.list, this.removeList);
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
					this.list.add(e);
				}
			}
		}
		else if (type == FireworkType.trail)
		{	
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge))));
			double s = Math.max(0, this.size - (int) (this.age * this.size / this.maxAge));
			Drawing.drawing.fillOval(posX, posY, s, s);
			
			if (this.age >= this.maxAge)
			{
				removeList.add(this);
			}
		}
		else if (type == FireworkType.particle)
		{	
			this.vY += 0.0625 * Panel.frameFrequency / 2;
			
			int opacity =  Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));
			Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, opacity);
			double s = this.size - (int) (this.age * this.size / this.maxAge);
			Drawing.drawing.fillOval(posX, posY, s, s);
			
			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = opacity / 50;
			f.colorR = this.colorR;
			f.colorG = this.colorG;
			f.colorB = this.colorB;
			f.size = this.size;
			this.list.add(f);*/
			
			if (this.age >= this.maxAge)
			{
				removeList.add(this);
			}
		}
		
		this.posX += this.vX * Panel.frameFrequency;
		this.posY += this.vY * Panel.frameFrequency;
		this.age += Panel.frameFrequency;
	}

    @Override
	public void draw() { }
}

package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class Firework extends Movable
{
	enum FireworkType {rocket, particle, trail}
	public FireworkType type;
	
	double age = 0;
	int maxAge = (int) (Math.random() * 80 + 80);
	
	int size = 8;
	
	public ArrayList<Firework> list;
	public ArrayList<Firework> removeList;

	public Color color;
	
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
			r = 0;
			g = 255;
			b = col - 256 * 2;
		}
		else if (col < 256 * 4)
		{
			r = 0;
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
		
		this.color = new Color(r, g, b);
	}
	
	public void drawUpdate(Graphics g)
	{
		if (type == FireworkType.rocket)
		{
			this.vY += 0.0625;
			
			g.setColor(this.color);
			Screen.fillOval(g, posX, posY, this.size, this.size);
			
			Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = 30;
			f.color = this.color;
			f.size = this.size;
			this.list.add(f);
			
			if (this.age >= this.maxAge)
			{
				removeList.add(this);
				
				for (int i = 0; i < 100; i++)
				{
					Firework e = new Firework(FireworkType.particle, this.posX, this.posY, this.list, this.removeList);
					e.size = 4;
					int var = 50;
					e.color = new Color((int) Math.min(255, Math.max(0, this.color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, this.color.getBlue() + Math.random() * var - var / 2)));
					double power = Math.random() * 6 + 2;
					e.vX = this.vX;
					e.vY = this.vY;
					e.addPolarMotion(Math.random() * 2 * Math.PI, Math.random() * power);
					e.maxAge = 100;
					this.list.add(e);
				}
			}
		}
		else if (type == FireworkType.trail)
		{	
			g.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), Math.max(0, Math.min(255, 255 - (int) (this.age * 255.0 / this.maxAge)))));
			Screen.fillOval(g, posX, posY, this.size, this.size);
			
			if (this.age >= this.maxAge)
			{
				removeList.add(this);
			}
		}
		else if (type == FireworkType.particle)
		{	
			this.vY += 0.0625;
			
			int opacity =  Math.min(255, Math.max(0, (int) (255 - this.age * 255.0 / this.maxAge)));
			g.setColor(new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), opacity));
			Screen.fillOval(g, posX, posY, this.size, this.size);
			
			/*Firework f = new Firework(FireworkType.trail, this.posX, this.posY, this.list, this.removeList);
			f.maxAge = opacity / 50;
			f.color = this.color;
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
	public void checkCollision() { }

	@Override
	public void draw(Graphics p) { }
}

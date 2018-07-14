package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Flame extends Bullet
{
	double life = 100;
	double age = 0;
	double frequency = Panel.frameFrequency;
	
	public Flame(double x, double y, Color color, int bounces, Tank t) 
	{
		super(x, y, color, bounces, t);
		t.liveBullets--;
		this.useCustomWallCollision = true;
	}
	
	@Override
	public void update()
	{
		this.age += Panel.frameFrequency;
		this.size = (int) (this.age + 10);
		
		this.damage = frequency * Math.max(0, 0.2 - this.age / 500.0) / 2;
		
		super.update();
		
		if (this.age > life)
			Game.removeMovables.add(this);
	}
	
	@Override
	public void draw(Graphics g)
	{
		double rawOpacity = (1.0 - (this.age)/life);
		rawOpacity *= rawOpacity;
		int opacity = (int)(rawOpacity * 255);
		
		int green = (int)(255 - 255.0*(this.age / life));
		Color col = new Color(255, green, 0, opacity);
		
		g.setColor(col);
		Window.fillOval(g, this.posX, this.posY, size, size);
	}

}

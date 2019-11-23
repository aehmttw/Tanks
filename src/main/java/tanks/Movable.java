package tanks;

import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.tank.NameTag;

import java.util.ArrayList;

public abstract class Movable implements IDrawableForInterface
{
	public double posX;
	public double posY;
	public double posZ = 0;
	public double vX;
	public double vY;
	public double vZ = 0;

	public double lastFinalVX;
	public double lastFinalVY;
	public double lastFinalVZ;


	public double cooldown = 0;
	public boolean destroy = false;

	public NameTag nameTag;
	public boolean showName = false;

	public int drawLevel = 3;
	//public boolean drawBelow = false;
	//public boolean drawAbove = false;
	public boolean canHide = false;
	public boolean isRemote = false;
	
	public double hiddenTimer = 0; 

	public ArrayList<AttributeModifier> attributes = new ArrayList<AttributeModifier>();
	
	public Team team;

	public Movable(double x, double y)
	{
		this.posX = x;
		this.posY = y;
	}

	public void update()
	{
		if (!destroy)
		{
			this.hiddenTimer = Math.max(0, this.hiddenTimer - Panel.frameFrequency);
			
			double vX2 = this.vX;
			double vY2 = this.vY;
			double vZ2 = this.vZ;

			ArrayList<AttributeModifier> removeAttributes = new ArrayList<AttributeModifier>(); 
			for (int i = 0; i < this.attributes.size(); i++)
			{
				AttributeModifier a = this.attributes.get(i);
				
				if (a.expired)
					removeAttributes.add(a);
				
				a.update();
				
				if (a.type.equals("velocity"))
				{
					vX2 = a.getValue(vX2);
					vY2 = a.getValue(vY2);
					vZ2 = a.getValue(vZ2);
				}
			}

			for (int i = 0; i < removeAttributes.size(); i++)
			{
				this.attributes.remove(removeAttributes.get(i));
			}

			removeAttributes.clear();

			this.lastFinalVX = vX2 / 2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
			this.lastFinalVY = vY2 / 2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
			this.lastFinalVZ = vZ2 / 2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;

			this.posX += this.lastFinalVX * Panel.frameFrequency;
			this.posY += this.lastFinalVY * Panel.frameFrequency;
			this.posZ += this.lastFinalVZ * Panel.frameFrequency;

			this.canHide = false;
		}
	}

	public void setMotionInDirection(double x, double y, double velocity)
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
		double velX = velocity * Math.cos(angle);
		double velY = velocity * Math.sin(angle);
		this.vX = velX;
		this.vY = velY;

	}

	public void setMotionAwayFromDirection(double x, double y, double velocity)
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
		angle += Math.PI;
		double velX = velocity * Math.cos(angle);
		double velY = velocity * Math.sin(angle);
		this.vX = velX;
		this.vY = velY;

	}

	public void setMotionInDirectionWithOffset(double x, double y, double velocity, double a)
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
		angle += a;
		double velX = velocity * Math.cos(angle);
		double velY = velocity * Math.sin(angle);
		this.vX = velX;
		this.vY = velY;

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

	public double getPolarDirection()
	{
		double x = this.vX;
		double y = this.vY;

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

	public void setPolarMotion(double angle, double velocity)
	{
		double velX = velocity * Math.cos(angle);
		double velY = velocity * Math.sin(angle);
		this.vX = velX;
		this.vY = velY;			
	}

	public void set3dPolarMotion(double angle1, double angle2, double velocity)
	{
		double velX = velocity * Math.cos(angle1) * Math.cos(angle2);
		double velY = velocity * Math.sin(angle1) * Math.cos(angle2);
		double velZ = velocity * Math.sin(angle2);

		this.vX = velX;
		this.vY = velY;
		this.vZ = velZ;
	}

	public void addPolarMotion(double angle, double velocity)
	{
		double velX = velocity * Math.cos(angle);
		double velY = velocity * Math.sin(angle);
		this.vX += velX;
		this.vY += velY;			
	}

	public void add3dPolarMotion(double angle1, double angle2, double velocity)
	{
		double velX = velocity * Math.cos(angle1) * Math.cos(angle2);
		double velY = velocity * Math.sin(angle1) * Math.cos(angle2);
		double velZ = velocity * Math.sin(angle2);

		this.vX += velX;
		this.vY += velY;
		this.vZ += velZ;
	}

	public void moveInDirection(double x, double y, double amount)
	{
		this.posX += amount * x;
		this.posY += amount * y;	
	}

	public void drawTeam()
	{
		Drawing.drawing.setFontSize(20);
		if (this.team != null)
			Drawing.drawing.drawText(this.posX, this.posY + 35, this.team.name);
	}


	public void addAttribute(AttributeModifier m)
	{
		this.attributes.add(m);
	}

	public void addUnduplicateAttribute(AttributeModifier m)
	{
		for (int i = 0; i < this.attributes.size(); i++)
		{
			if (this.attributes.get(i).name.equals(m.name))
			{
				this.attributes.remove(i);
				i--;
			}			
		}
		
		this.attributes.add(m);
	}

	public static double[] getLocationInDirection(double angle, double distance)
	{
		return new double[]{distance * Math.cos(angle), distance * Math.sin(angle)};	
	}

	public abstract void draw();
	
	public void drawAt(double x, double y)
	{	
		double x1 = this.posX;
		double y1 = this.posY;
		this.posX = x;
		this.posY = y;
		this.draw();
		this.posX = x1;
		this.posY = y1;
	}

	public void drawForInterface(double x, double y)
	{	
		this.drawAt(x, y);
	}

	public static double distanceBetween(final Movable a, final Movable b)
	{
		return Math.sqrt((a.posX-b.posX)*(a.posX-b.posX) + (a.posY-b.posY)*(a.posY-b.posY));
	}
	
	public static double distanceBetween(final Obstacle a, final Movable b)
	{
		return Math.sqrt((a.posX-b.posX)*(a.posX-b.posX) + (a.posY-b.posY)*(a.posY-b.posY));
	}

	public static double angleBetween(double a, double b)
	{
		return (a - b + Math.PI * 3) % (Math.PI*2) - Math.PI;
	}

	public static double absoluteAngleBetween(double a, double b)
	{
		return Math.abs((a - b + Math.PI * 3) % (Math.PI*2) - Math.PI);
	}
}

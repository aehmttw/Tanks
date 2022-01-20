package tanks;

import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;
import tanks.tank.NameTag;

import java.util.ArrayList;

public abstract class Movable implements IDrawableForInterface, IGameObject
{
	public double posX;
	public double posY;
	public double posZ = 0;

	public double lastPosX;
	public double lastPosY;
	public double lastPosZ = 0;

	public double vX;
	public double vY;
	public double vZ = 0;

	public double lastFinalVX;
	public double lastFinalVY;
	public double lastFinalVZ;

	public double lastVX;
	public double lastVY;
	public double lastVZ;

	public boolean destroy = false;
	public boolean dealsDamage = true;

	public NameTag nameTag;
	public boolean showName = false;

	public boolean skipNextUpdate = false;

	public int drawLevel = 3;
	public boolean isRemote = false;

	public ArrayList<AttributeModifier> attributes = new ArrayList<>();
	public ArrayList<String> attributeImmunities = new ArrayList<>();

	public Team team;

	public Movable(double x, double y)
	{
		this.posX = x;
		this.posY = y;

		this.lastPosX = x;
		this.lastPosY = y;
	}

	public void preUpdate()
	{
		this.lastVX = (this.posX - this.lastPosX) / Panel.frameFrequency;
		this.lastVY = (this.posY - this.lastPosY) / Panel.frameFrequency;
		this.lastVZ = (this.posZ - this.lastPosZ) / Panel.frameFrequency;

		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.lastPosZ = this.posZ;
	}

	public void update()
	{
		if (!destroy)
		{
			double vX2 = this.vX;
			double vY2 = this.vY;
			double vZ2 = this.vZ;

			ArrayList<Integer> removeIndexes = new ArrayList<>();
			for (int i = 0; i < this.attributes.size(); i++)
			{
				AttributeModifier a = this.attributes.get(i);

				if (a.expired)
				{
					// Adds index to list to later get removed.
					removeIndexes.add(i);
				}

				a.update();

				if (!a.expired && a.type.equals("velocity"))
				{
					vX2 = a.getValue(vX2);
					vY2 = a.getValue(vY2);
					vZ2 = a.getValue(vZ2);
				}
			}

			// Remove all attributes with indexes.
			int offSet = 0;
			for (Integer removeIndex : removeIndexes)
			{
				this.attributes.remove(removeIndex + offSet);
				offSet--;
			}

			this.lastFinalVX = vX2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
			this.lastFinalVY = vY2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
			this.lastFinalVZ = vZ2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;

			this.posX += this.lastFinalVX * Panel.frameFrequency;
			this.posY += this.lastFinalVY * Panel.frameFrequency;
			this.posZ += this.lastFinalVZ * Panel.frameFrequency;
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
		return Movable.getPolarDirection(this.vX, this.vY);
	}

	public double getLastPolarDirection()
	{
		return Movable.getPolarDirection(this.lastVX, this.lastVY);
	}

	public static double getPolarDirection(double x, double y)
	{
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

	public double getSpeed()
	{
		return Math.sqrt(Math.pow(this.vX, 2) + Math.pow(this.vY, 2));
	}

	public double getLastSpeed()
	{
		return Math.sqrt(Math.pow(this.lastVX, 2) + Math.pow(this.lastVY, 2));
	}

	public double getMotionInDirection(double angle)
	{
		return this.getSpeed() * Math.cos(this.getPolarDirection() - angle);
	}

	public double getLastMotionInDirection(double angle)
	{
		return this.getLastSpeed() * Math.cos(this.getLastPolarDirection() - angle);
	}

	public void drawTeam()
	{
		Drawing.drawing.setFontSize(20);
		if (this.team != null)
			Drawing.drawing.drawText(this.posX, this.posY + 35, this.team.name);
	}


	public void addAttribute(AttributeModifier m)
	{
		if (!this.attributeImmunities.contains(m.name))
			this.attributes.add(m);
	}

	public void addUnduplicateAttribute(AttributeModifier m)
	{
		if (this.attributeImmunities.contains(m.name))
			return;

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
		return Math.abs((a - b + Math.PI * 3) % (Math.PI * 2) - Math.PI);
	}
}

package tanks;

import tanks.bullet.Bullet;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.network.event.EventStatusEffectBegin;
import tanks.network.event.EventStatusEffectDeteriorate;
import tanks.network.event.EventStatusEffectEnd;
import tanks.obstacle.Obstacle;
import tanks.tank.Mine;
import tanks.tank.NameTag;
import tanks.tank.Tank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

	public double lastOriginalVX;
	public double lastOriginalVY;
	public double lastOriginalVZ;

	public boolean destroy = false;
	public boolean dealsDamage = true;

	public NameTag nameTag;
	public boolean showName = false;

	public boolean skipNextUpdate = false;

	public int drawLevel = 3;
	public boolean isRemote = false;

	public ArrayList<AttributeModifier> attributes = new ArrayList<>();
	public HashMap<StatusEffect, StatusEffect.Instance> statusEffects = new HashMap<>();
	public HashSet<String> attributeImmunities = new HashSet<>();

	public Team team;

	protected ArrayList<StatusEffect> removeStatusEffects = new ArrayList<>();

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

		this.lastOriginalVX = this.vX;
		this.lastOriginalVY = this.vY;
		this.lastOriginalVZ = this.vZ;

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

			ArrayList<AttributeModifier> toRemove = new ArrayList<>();
			for (AttributeModifier a : attributes)
			{
				if (a.expired)
				{
					// Adds attribute to list to later get removed.
					toRemove.add(a);
				}

				a.update();
			}

			for (AttributeModifier a : toRemove)
			{
				attributes.remove(a);
			}

			this.updateStatusEffects();

			vX2 = this.getAttributeValue(AttributeModifier.velocity, vX2);
			vY2 = this.getAttributeValue(AttributeModifier.velocity, vY2);
			vZ2 = this.getAttributeValue(AttributeModifier.velocity, vZ2);

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

	public void addStatusEffect(StatusEffect s, double warmup, double deterioration, double duration)
	{
		this.addStatusEffect(s, 0, warmup, deterioration, duration);
	}

	public void addStatusEffect(StatusEffect s, double age, double warmup, double deterioration, double duration)
	{
		if (deterioration > duration)
			throw new RuntimeException("Deterioration age > duration");

		StatusEffect prevEffect = null;
		for (StatusEffect e: this.statusEffects.keySet())
		{
			if (e.family != null && e.family.equals(s.family))
				prevEffect = e;
		}

		if (prevEffect != null)
			this.statusEffects.remove(prevEffect);

		boolean dontAdd = false;
		if (warmup <= age && this.statusEffects.get(s) != null)
		{
			StatusEffect.Instance i = this.statusEffects.get(s);
			if (i.age > i.warmupAge && i.age < i.deteriorationAge)
				dontAdd = true;
		}

		if (!dontAdd && (this instanceof Bullet || this instanceof Tank) && ScreenPartyHost.isServer)
			Game.eventsOut.add(new EventStatusEffectBegin(this, s, age, warmup));

		this.statusEffects.put(s, new StatusEffect.Instance(s, age, warmup, deterioration, duration));
	}

	public void updateStatusEffects()
	{
		for (StatusEffect s: this.statusEffects.keySet())
		{
			StatusEffect.Instance i = this.statusEffects.get(s);

			if (i.age < i.deteriorationAge && i.age + Panel.frameFrequency >= i.deteriorationAge && ScreenPartyHost.isServer && (this instanceof Bullet || this instanceof Tank))
			{
				Game.eventsOut.add(new EventStatusEffectDeteriorate(this, s, i.duration - i.deteriorationAge));
			}

			if (i.duration <= 0 || i.age + Panel.frameFrequency <= i.duration)
				i.age += Panel.frameFrequency;
			else
			{
				this.removeStatusEffects.add(s);

				if (this instanceof Bullet || this instanceof Tank)
					Game.eventsOut.add(new EventStatusEffectEnd(this, s));
			}
		}

		for (StatusEffect s: this.removeStatusEffects)
		{
			this.statusEffects.remove(s);
		}

		removeStatusEffects.clear();
	}

	public double getAttributeValue(AttributeModifier.Type type, double value)
	{
		for (AttributeModifier a : attributes)
		{
			if (!a.expired && a.type.equals(type))
			{
				value = a.getValue(value);
			}
		}

		for (StatusEffect s : this.statusEffects.keySet())
		{
			value = this.statusEffects.get(s).getValue(value, type);
		}

		return value;
	}

	public AttributeModifier getAttribute(AttributeModifier.Type type)
	{
		AttributeModifier best = null;
		double bestTime = Double.MIN_VALUE;

		for (AttributeModifier a : attributes)
		{
			if (!a.expired && a.type.equals(type))
			{
				if (a.deteriorationAge - a.age > bestTime || a.deteriorationAge <= 0)
				{
					bestTime = a.deteriorationAge - a.age;
					best = a;

					if (a.deteriorationAge <= 0)
						bestTime = Double.MAX_VALUE;
				}
			}
		}

		for (StatusEffect s : this.statusEffects.keySet())
		{
			StatusEffect.Instance i = this.statusEffects.get(s);

			if (i != null)
			{
				for (AttributeModifier a : s.attributeModifiers)
				{
					if (a.type.equals(type))
					{
						if (i.deteriorationAge - i.age > bestTime || a.deteriorationAge <= 0)
						{
							bestTime = i.deteriorationAge - i.age;
							best = new AttributeModifier(a.type, a.effect, a.value);
							best.warmupAge = i.warmupAge;
							best.deteriorationAge = i.deteriorationAge;
							best.age = i.age;
							best.duration = i.duration;

							if (a.deteriorationAge <= 0)
								bestTime = Double.MAX_VALUE;
						}
					}
				}
			}
		}

		return best;
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

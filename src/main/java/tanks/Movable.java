package tanks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import tanks.effect.AttributeModifier;
import tanks.effect.EffectManager;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.leveleditor.selector.SelectorTeam;
import tanks.tank.IAvoidObject;
import tanks.tank.NameTag;
import tanks.tankson.MetadataProperty;
import tanks.tankson.Property;

import java.lang.reflect.Field;

public abstract class Movable extends SolidGameObject implements IDrawableForInterface
{
	public ObjectArraySet<Chunk> prevChunks = new ObjectArraySet<>();
	private EffectManager em;

	public double lastPosX, lastPosY, lastPosZ = 0;
	public double vX, vY, vZ = 0;
	public double lastFinalVX, lastFinalVY, lastFinalVZ;
	public double lastVX, lastVY, lastVZ;
	public double lastOriginalVX, lastOriginalVY, lastOriginalVZ;

    private double lastSize = Integer.MAX_VALUE;

	public double age = 0;

	public boolean destroy = false;
	public boolean dealsDamage = true;

	public NameTag nameTag;
	public boolean showName = false;

	public boolean affectedByFrameFrequency = true;

	public boolean skipNextUpdate = false;

	public int drawLevel = 3;
	public boolean isRemote = false;
	public boolean managedMotion = true;

	@MetadataProperty(id = "team", name = "Team", selector = SelectorTeam.selector_name, image = "team.png", keybind = "editor.team")
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
        refreshFaces();

		double frameFrequency = affectedByFrameFrequency ? Panel.frameFrequency : 1;
		this.lastVX = (this.posX - this.lastPosX) / frameFrequency;
		this.lastVY = (this.posY - this.lastPosY) / frameFrequency;
		this.lastVZ = (this.posZ - this.lastPosZ) / frameFrequency;

		this.lastOriginalVX = this.vX;
		this.lastOriginalVY = this.vY;
		this.lastOriginalVZ = this.vZ;

		this.lastPosX = this.posX;
		this.lastPosY = this.posY;
		this.lastPosZ = this.posZ;
	}

    public void postUpdate()
    {
        updateChunks();
    }

	/** Cached list for checking chunks that the movable has just left */
	private static final ObjectArrayList<Chunk> leaveChunks = new ObjectArrayList<>();

	public void updateChunks()
	{
        boolean changed = collisionChanged();
		if (!changed && posX == lastPosX && posY == lastPosY && getSize() == lastSize)
			return;

        lastSize = getSize();

		if (changed && this instanceof IAvoidObject)
			Game.avoidObjects.add((IAvoidObject) this);

        refreshFaces();
    }

    public void refreshFaces()
    {
        removeFacesFromChunks();
        updateFaces();
        addFacesToChunks();
    }

    public void addFacesToChunks()
    {
        for (Chunk c : getTouchingChunks())
            c.faces.addFaces(this);
    }

    public void removeFacesFromChunks()
    {
        ObjectArrayList<Chunk> cache = getTouchingChunks();

        for (Chunk c : cache)
        {
            if (prevChunks.add(c))
                onEnterChunk(c);
            c.faces.removeFaces(this);
        }

        leaveChunks.clear();
        for (Chunk c : prevChunks)
        {
            if (!cache.contains(c))
            {
                onLeaveChunk(c);
                leaveChunks.add(c);
            }
        }
        prevChunks.removeAll(leaveChunks);
    }

    public void onEnterChunk(Chunk c)
	{
		c.addMovable(this, false);
	}

	public void onLeaveChunk(Chunk c)
	{
		c.removeMovable(this);
	}

	public ObjectArrayList<Chunk> getTouchingChunks()
	{
		double bound = getSize() / 2;
		return Chunk.getChunksInRange(
            Math.min(lastPosX, posX) - bound,
            Math.min(lastPosY, posY) - bound,
            Math.max(lastPosX, posX) + bound,
            Math.max(lastPosY, posY) + bound
        );
	}

	public void update()
	{
		double frameFrequency = affectedByFrameFrequency ? Panel.frameFrequency : 1;

		if (!destroy)
		{
			em().update();

			double vX2 = this.vX;
			double vY2 = this.vY;
			double vZ2 = this.vZ;

			if (this.managedMotion)
			{
				// Apply velocity modifiers
				vX2 = em.getAttributeValue(AttributeModifier.velocity, vX2);
				vY2 = em.getAttributeValue(AttributeModifier.velocity, vY2);
				vZ2 = em.getAttributeValue(AttributeModifier.velocity, vZ2);

				this.lastFinalVX = vX2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
				this.lastFinalVY = vY2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
				this.lastFinalVZ = vZ2 * ScreenGame.finishTimer / ScreenGame.finishTimerMax;

				this.posX += this.lastFinalVX * frameFrequency;
				this.posY += this.lastFinalVY * frameFrequency;
				this.posZ += this.lastFinalVZ * frameFrequency;
			}
		}
	}

	public void initEffectManager(EffectManager em)
	{

	}

	/** Alias for {@link #getEffectManager()} */
	public EffectManager em()
	{
		return getEffectManager();
	}

	public EffectManager getEffectManager()
	{
		if (em == null)
		{
			em = new EffectManager(this);
			initEffectManager(em);
		}
		return em;
	}

	public void setMotionInDirection(double x, double y, double velocity)
	{
        double dx = x - this.posX;
        double dy = y - this.posY;
        double d = Math.sqrt(dx * dx + dy * dy);
        if (d == 0)
            return;

		this.vX = velocity * dx / d;
		this.vY = velocity * dy / d;
	}

	public void setMotionAwayFromDirection(double x, double y, double velocity)
	{
		setMotionInDirection(x, y, -velocity);
	}

	public void setMotionInDirectionWithOffset(double x, double y, double velocity, double a)
	{
		double angle = getAngleInDirection(x, y);
		angle += a;
		this.vX = velocity * Math.cos(angle);
		this.vY = velocity * Math.sin(angle);
	}

	public double getAngleInDirection(double x, double y)
	{
		return Movable.getPolarDirection(x - this.posX, y - this.posY);
	}

	public double getPolarDirection()
	{
        return getPolarDirection(this.vX, this.vY);
	}

	public double getPolarPitch()
	{
		return fastAtan(this.vZ / this.getSpeed());
	}

	public double getLastPolarDirection()
	{
		return getPolarDirection(this.lastVX, this.lastVY);
	}

	public void setPolarMotion(double angle, double velocity)
	{
		this.vX = velocity * Math.cos(angle);
		this.vY = velocity * Math.sin(angle);
    }

	public void set3dPolarMotion(double angle1, double angle2, double velocity)
	{
		this.vX = velocity * Math.cos(angle1) * Math.cos(angle2);
		this.vY = velocity * Math.sin(angle1) * Math.cos(angle2);
		this.vZ = velocity * Math.sin(angle2);
	}

	public void addPolarMotion(double angle, double velocity)
	{
		this.vX += velocity * Math.cos(angle);
		this.vY += velocity * Math.sin(angle);
	}

	public void add3dPolarMotion(double angle1, double angle2, double velocity)
	{
		this.vX += velocity * Math.cos(angle1) * Math.cos(angle2);
		this.vY += velocity * Math.sin(angle1) * Math.cos(angle2);
		this.vZ += velocity * Math.sin(angle2);
	}

	public void moveInDirection(double x, double y, double amount)
	{
		this.posX += amount * x;
		this.posY += amount * y;
	}

	public void moveInAngle(double a, double amount)
	{
		this.posX += amount * Math.cos(a);
		this.posY += amount * Math.sin(a);
	}

	public static double getSpeed(double vX, double vY)
	{
		return Math.sqrt(vX * vX + vY * vY);
	}

	public static double getPolarPitch(double vX, double vY, double vZ)
	{
		return fastAtan(vZ / getSpeed(vX, vY));
	}

	public double getSpeed()
	{
		return Math.sqrt(this.vX * this.vX + this.vY * this.vY);
	}

	public double getLastSpeed()
	{
		return Math.sqrt(this.lastVX * this.lastVX + this.lastVY * this.lastVY);
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

	public static double[] getLocationInDirection(double angle, double distance)
	{
		return new double[]{distance * Math.cos(angle), distance * Math.sin(angle)};
	}

	public abstract void draw();

	public double getSize()
	{
		return 0;
	}

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

	/** Field to cache the movable array for reuse */
	private static final ObjectArrayList<Movable> movableOut = new ObjectArrayList<>();

    public static ObjectArrayList<Movable> getCircleCollision(GameObject self)
    {
        movableOut.clear();
        double x = self.posX, y = self.posY;

        for (Chunk c : Chunk.getChunksInRadius(x, y, self.getSize()))
            for (Movable m : c.movables)
                if (m != self && !m.skipNextUpdate && !m.destroy &&
                    GameObject.withinRadius(self, m, (self.getSize() + m.getSize()) / 2))
                    movableOut.add(m);


        return movableOut;
    }

    public static ObjectArrayList<Movable> getSquareCollision(GameObject self)
    {
        movableOut.clear();
        double bound = self.getSize() / 2 + Game.tile_size / 2;
        double x = self.posX, y = self.posY;

        for (Chunk c : Chunk.getChunksInRange(x - bound, y - bound, x + bound, y + bound))
        {
            for (Movable m : c.movables)
            {
                if (m != self && !m.skipNextUpdate && !m.destroy && Math.abs(m.posX - x) < (self.getSize() + m.getSize()) / 2
                    && Math.abs(m.posY - y) < (self.getSize() + m.getSize()) / 2)
                    movableOut.add(m);
            }
        }
        return movableOut;
    }

	/** Expects all pixel coordinates.
	 * @return all the movables within the specified range */
	public static ObjectArrayList<Movable> getMovablesInRange(double x1, double y1, double x2, double y2)
	{
		movableOut.clear();
        for (Chunk c : Chunk.getChunksInRange(x1, y1, x2, y2))
            for (Movable m : c.movables)
                if (Game.isOrdered(true, x1, m.posX, x2) && Game.isOrdered(true, y1, m.posY, y2))
                    movableOut.add(m);
		return movableOut;
	}


	/** Expects all pixel coordinates.
	 * @return all the movables within a certain radius of the position */
	public static ObjectArrayList<Movable> getMovablesInRadius(double posX, double posY, double radius)
	{
		movableOut.clear();
        for (Chunk c : Chunk.getChunksInRadius(posX, posY, radius))
            for (Movable m : c.movables)
                if (Movable.sqDistBetw(m.posX, m.posY, posX, posY) < radius * radius)
                    movableOut.add(m);
		return movableOut;
	}

	public static Movable findMovable(double x, double y)
	{
		ObjectArrayList<Movable> movables = Movable.getMovablesInRadius(x, y, 1);
		if (!movables.isEmpty())
			return movables.get(0);
		return null;
	}

	public void drawForInterface(double x, double y)
	{
		this.drawAt(x, y);
	}

	public void setEffectManager(EffectManager em)
	{
		this.em = em;
	}

	public void randomize()
	{
		try
		{
			for (Field f: this.getClass().getFields())
			{
				if (f.getAnnotation(Property.class) == null || Math.random() < 0.999)
					continue;

				if (f.getType().equals(double.class))
					f.set(this, (double) (f.get(this)) * Math.random() * 1.5 + 0.5);
				else if (f.getType().equals(int.class))
					f.set(this, (int) ((int)(f.get(this)) * Math.random() * 1.5 + 0.5));
				else if (f.getType().isEnum())
				{
					Enum[] els = ((Enum) f.get(this)).getClass().getEnumConstants();
					f.set(this, els[(int) (Math.random() * els.length)]);
				}
				else if (Movable.class.isAssignableFrom(f.getType()) && f.get(this) != null)
				{
					((Movable) (f.get(this))).randomize();
				}
			}
		}
		catch (Exception e)
		{
			Game.exitToCrash(e);
		}
	}
}

package tanks.tank;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.TextWithStyling;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.*;

import java.util.*;
import java.util.stream.Collectors;

public class Ray2
{
	public static double min_trace_size = 5;

	public static int chunksAdded;
	/** Caches the chunks to avoid creating new temp objects */
	public static Chunk[] chunkCache = new Chunk[40];
	/** Caches the ray to avoid creating new temp objects */
	public static Ray2 cacheRay = new Ray2();

	public double size = 10;
	public double tankHitSizeMul = 1;

	public int bounces, bouncyBounces = 100;
	public double posX, posY, vX, vY, angle;
	public double startX, startY;

	public int maxChunkCheck = Integer.MAX_VALUE;

	public boolean enableBounciness = true;
	public boolean asBullet = true;
	public boolean ignoreTanks = false, ignoreBullets = true;
	public boolean ignoreDestructible = false;
	public boolean ignoreShootThrough = false;

	public boolean trace = Game.traceAllRays;
	public boolean dotted = false;

	public double speed = 10;
	public double range;

	public double age = 0;
	public int traceAge;

	public Tank tank, targetTank;
	public double targetTankSizeMul = 0;

	public DoubleArrayList bounceX = new DoubleArrayList();
	public DoubleArrayList bounceY = new DoubleArrayList();

	public double targetX, targetY;
	public boolean acquiredTarget = false;

    /** Set this and use {@linkplain #chunkComparator} to sort the chunks by manhattan distance from this chunk */
    private static Chunk startingChunk = Chunk.zeroChunk;
    /** Method references don't allocate new objects; however lambdas do (they create temp classes) */
    private static int keyExtractor(Chunk c) { return c.manhattanDist(startingChunk); }
    /** Comparator that sorts the chunks by manhattan distance from {@linkplain #startingChunk} */
    private static final Comparator<Chunk> chunkComparator = Comparator.comparingInt(Ray2::keyExtractor);

	/** Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}  */
	public static Ray2 newRay(double x, double y, double angle, int bounces, Tank tank)
	{
		return newRay(x, y, angle, bounces, tank, 10);
	}

	/** Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}  */
	public static Ray2 newRay(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		return cacheRay.set(x, y, angle, bounces, tank, speed);
	}

	/** Creates another ray with the properties of the last ray.<br>
	 * To set custom properties on your ray copy:
	 * <blockquote><pre>
	 *     Ray copy = Ray.newRay(params).copy()
	 * </pre></blockquote>
	 * */
	public Ray2 copy()
	{
		return new Ray2().set(posX, posY, angle, bounces, tank, speed);
	}

	private Ray2() {}

	public Ray2 set(double x, double y, double angle, int bounces, Tank tank, double speed)
	{
		this.vX = speed * Math.cos(angle);
		this.vY = speed * Math.sin(angle);
		this.angle = angle;

		this.posX = this.startX = x;
		this.posY = this.startY = y;
		this.bounces = bounces;
		this.bouncyBounces = 100;
		setSize(10).setMaxChunks(Integer.MAX_VALUE);

		this.trace = Game.traceAllRays;
		this.dotted = false;
		this.enableBounciness = true;
		this.ignoreTanks = false;
		this.ignoreBullets = true;
		this.asBullet = true;
		this.ignoreDestructible = false;
		this.ignoreShootThrough = false;

		this.age = 0;
		this.range = 0;
		this.tankHitSizeMul = 1;
		this.acquiredTarget = false;
		this.tank = tank.getBottomLevelPossessing();

		this.bounceX.clear();
		this.bounceY.clear();

		return this;
	}

	public Movable getTarget(double mul, Tank targetTank)
	{
		this.targetTank = targetTank;
		this.targetTankSizeMul = mul;
		return this.getTarget();
	}

	public boolean isInSight(Movable target)
	{
		return setVelocity(target.posX - this.posX, target.posY - this.posY).getTarget() == target;
	}

	public Ray2 setVelocity(double vX, double vY)
	{
		this.vX = vX;
		this.vY = vY;
		this.angle = Movable.getPolarDirection(vX, vY);
		return this;
	}

	public Ray2 setShootThrough(boolean shootThrough)
	{
		this.ignoreShootThrough = shootThrough;
		return this;
	}

	public Ray2 setExplosive(boolean explosive)
	{
		this.ignoreDestructible = explosive;
		return this;
	}

	public Ray2 setBouncyBounces(int bouncyBounces)
	{
		this.bouncyBounces = bouncyBounces;
		return this;
	}

	public Ray2 setAsBullet(boolean testBulletCollision)
	{
		this.asBullet = testBulletCollision;
		return this;
	}

	public Ray2 setMaxChunks(int maxChunks)
	{
		this.maxChunkCheck = maxChunks;
		return this;
	}

	public Ray2 setTrace(boolean trace, boolean dotted)
	{
		this.trace = trace;
		this.dotted = dotted;
		return this;
	}

	@SuppressWarnings("unused")
	public Ray2 setMaxDistance(double distance)
	{
		setMaxChunks((int) (distance / Game.tile_size / Chunk.chunkSize + 1));
		return this;
	}

	public Ray2 setRange(double range)
	{
		this.range = range;
		return this;
	}

	public Ray2 setSize(double size)
	{
		this.size = size;
		return this;
	}

	public Ray2 moveOut(double amount)
	{
		this.posX += this.vX * amount;
		this.posY += this.vY * amount;
		return this;
	}

	public static final Result result = new Result();

	public Movable getTarget()
	{
		double remainder = 0;
		acquiredTarget = true;

		if (testInsideObstacle(posX, posY))
			return null;

		if (!ignoreTanks)
		{
			Chunk c = Chunk.getChunk(posX, posY);
			if (c == null)
				return null;

			for (Movable m : c.movables)
			{
				if (!(m instanceof Tank) || m == this.tank)
					continue;

				Tank t = (Tank) m;
				if (this.posX + this.size / 2 >= t.posX - t.size / 2 &&
						this.posX - this.size / 2 <= t.posX + t.size / 2 &&
						this.posY + this.size / 2 >= t.posY - t.size / 2 &&
						this.posY - this.size / 2 <= t.posY + t.size / 2)
					return t;
			}
		}

		boolean firstBounce = this.targetTank == null;

		while (this.bounces >= 0 && this.bouncyBounces >= 0)
		{
			Chunk current = Chunk.getChunk(posX, posY);
			if (current == null)
				return null;

			checkFaceList(current, firstBounce);

			this.age += result.t;

			firstBounce = false;

			if (result.collisionFace == null)
				return null;

			double dx = result.collisionX - posX, dy = result.collisionY - posY;

			if (this.range > 0)
			{
				double dist = Math.sqrt(dx * dx + dy * dy);
				if (this.range < dist)
				{
					result.collisionX = posX + dx * range / dist;
					result.collisionY = posY + dy * range / dist;
					dx = result.collisionX - posX;
					dy = result.collisionY - posY;
					this.bounces = -1;
				}
				else
					this.range -= dist;
			}

			if (trace && ScreenGame.isUpdatingGame())
			{
				double steps = (Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) / Math.max(this.size, 2) * 10 + 1);

				if (dotted)
					steps /= 2;

				double s;
				for (s = remainder; s <= steps; s++)
				{
					double x = posX + dx * s / steps;
					double y = posY + dy * s / steps;

					this.traceAge++;

					double frac = 1 / (1 + this.traceAge / 100.0);
					double z = this.tank.size / 2 + this.tank.turretSize / 2 * frac + (Game.tile_size / 4) * (1 - frac);
					if (Game.screen instanceof ScreenGame && !ScreenGame.finished)
					{
						Game.effects.add(Effect.createNewEffect(x, y, z, Effect.EffectType.ray)
								.setSize(Math.max(this.size, min_trace_size)));
					}
				}

				remainder = s - steps;
			}

			this.posX = result.collisionX;
			this.posY = result.collisionY;

			if (Chunk.debug && trace)
			{
                debugTexts.add(new DebugText(
                    "@", bounces, bouncyBounces, 16,
                    posX, posY
                ));

                String symbol = result.collisionFace.direction.isNonZeroY() ? "|" : "-";
                debugTexts.add(new DebugText(
                    symbol, bounces, bouncyBounces, 16,
                    result.collisionFace.startX, result.collisionFace.startY
                ));
                debugTexts.add(new DebugText(
                    symbol, bounces, bouncyBounces, 16,
                    result.collisionFace.endX, result.collisionFace.endY
                ));
			}

            if (Panel.panel.ageFrames % 50 == 0)
                detectAndFixErrors();

			ISolidObject2 obj = result.collisionFace.owner;
			if (obj instanceof Movable)
			{
				this.targetX = result.collisionX;
				this.targetY = result.collisionY;
				bounceX.add(result.collisionX);
				bounceY.add(result.collisionY);

				return (Movable) obj;
			}

			if (obj instanceof Obstacle && ((Obstacle) obj).bouncy)
				this.bouncyBounces--;
			else if (obj instanceof Obstacle && !((Obstacle) obj).allowBounce)
				this.bounces = -1;
			else
				this.bounces--;

			bounceX.add(result.collisionX);
			bounceY.add(result.collisionY);

			if (this.bounces >= 0)
			{
				if (result.corner)
				{
					this.vX = -this.vX;
					this.vY = -this.vY;
				}
				else if (!result.collisionFace.direction.isNonZeroX())
					this.vY = -this.vY;
				else
					this.vX = -this.vX;

				this.angle = Movable.getPolarDirection(this.vX, this.vY);
			}
		}

		return null;
	}

    private static final ObjectArrayList<Chunk> errorChunkCache = new ObjectArrayList<>();

    /** Shouldn't be triggered 99% of the time */
    protected void detectAndFixErrors()
    {
        if (Ray2.result.collisionFace == null)
            return;

        boolean error = false;
        ISolidObject2 so = Ray2.result.collisionFace.owner;
        if (!(so instanceof GameObject))
            return;

        double posX = ((GameObject) so).posX;
        double posY = ((GameObject) so).posY;
        String name = null;
        errorChunkCache.clear();

        if (so instanceof Obstacle)
        {
            Chunk current = Chunk.getChunk(posX, posY);
            error = !current.obstacles.contains(so);
            if (error)
            {
                errorChunkCache.add(current);
                name = ((Obstacle) so).name;
            }
        }
        else if (so instanceof Movable)
        {
            ObjectArrayList<Chunk> chunks = ((Movable) so).getTouchingChunks();
            error = chunks.stream().anyMatch(c -> !c.movables.contains(so));
            if (error)
            {
                errorChunkCache.addAll(chunks);
                name = so instanceof Tank ? ((Tank) so).name : so.toString();
            }
        }

        if (error)
        {
            System.err.println("Ray collision face owner " + name + " not in chunk " +
                errorChunkCache.stream().map(c -> "(" + c.chunkX + ", " + c.chunkY + ")")
                    .collect(Collectors.joining(", ")));
            if (Game.currentLevel != null)
                Game.currentLevel.reloadTiles();
        }
    }

    public void checkFaceList(Chunk current, boolean firstBounce)
	{
		if (current == null)
			return;

		int totalChunksChecked = 0;

		chunkCheck:
		for (int chunksChecked = 0; chunksChecked < maxChunkCheck; chunksChecked++)
		{
			double moveXBase = Chunk.chunkSize * Game.tile_size * Math.cos(angle);
			double moveYBase = Chunk.chunkSize * Game.tile_size * Math.sin(angle);
			double moveX = moveXBase * chunksChecked, moveXPrev = moveXBase * Math.max(0, chunksChecked - 1);
			double moveY = moveYBase * chunksChecked, moveYPrev = moveYBase * Math.max(0, chunksChecked - 1);

			chunksAdded = 0;

            startingChunk = current;

			// move forward one chunk in the ray's direction
			Chunk mid = chunksChecked > 0 ? Chunk.getChunk(posX + moveX, posY + moveY) : null;
			// add chunk in front (use current chunk for distance comparison)
			addChunk(mid);

			// if the ray moved diagonally, add the chunks on the sides (use current chunk for distance comparison)
			if (mid == null || current.manhattanDist(mid) > 1)
			{
				addChunk(Chunk.getChunk(posX + moveXPrev, posY + moveY));
				addChunk(Chunk.getChunk(posX + moveX, posY + moveYPrev));
			}

			if (chunksAdded == 0)
				break;

			// sort the chunks by distance from the current chunk
			Arrays.sort(chunkCache, 0, chunksAdded, chunkComparator);

			for (int i = 0; i < chunksAdded; i++)
			{
				Chunk chunk = chunkCache[i];
				if (chunk == null)
					continue;

				totalChunksChecked++;

				if (Chunk.debug && trace)
				{
                    // displays the order of chunks checked and locations that the ray checked
                    debugTexts.add(new DebugText(
                        "" + totalChunksChecked, bounces, bouncyBounces, 48,
                        (chunk.chunkX + 0.5) * Chunk.chunkSize * Game.tile_size + (totalChunksChecked * 15),
                        (chunk.chunkY + 0.5) * Chunk.chunkSize * Game.tile_size
                    ));

					debugTexts.add(new DebugText(
                        (vX > 0 ? "+x" : "-x") + (vY > 0 ? "+y" : "-y"), bounces, bouncyBounces, 16,
                        posX + moveX, posY + moveY
                    ));

					if (mid == null || current.manhattanDist(mid) > 1)
					{
                        debugTexts.add(new DebugText(
                            vY > 0 ? "+x" : "-x", bounces, bouncyBounces, 16,
                            posX + moveX, posY
                        ));
                        debugTexts.add(new DebugText(
                            vX > 0 ? "+y" : "-y", bounces, bouncyBounces, 16,
                            posX, posY + moveY
                        ));
					}
				}

				checkCollisionIn(result, chunk.faces, firstBounce);

				if (result.collisionFace != null)
					break chunkCheck;
			}
		}
	}

	public boolean testInsideObstacle(double x, double y)
	{
		return isInsideObstacle(x - size / 2, y - size / 2) ||
				isInsideObstacle(x + size / 2, y - size / 2) ||
				isInsideObstacle(x + size / 2, y + size / 2) ||
				isInsideObstacle(x - size / 2, y + size / 2);
	}

	public void checkCollisionIn(Result result, Chunk.FaceList faceList, boolean firstBounce)
	{
		Face2 collisionFace = null;
		double t = Double.MAX_VALUE;
		boolean corner = false;
		double collisionX = 0, collisionY = 0;

		if (vX > 0)
		{
			for (Face2 f : faceList.leftFaces)
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;
				if (f.owner != null && f.owner == targetTank)
					size *= targetTankSizeMul;

				if (passesThrough(f, firstBounce) || f.startX < this.posX + size / 2)
					continue;

				double y = (f.startX - size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX - size / 2 - this.posX) / vX;
					collisionX = f.startX - size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}
		else if (vX < 0)
		{
			for (Face2 f : faceList.rightFaces)
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f, firstBounce) || f.startX > this.posX - size / 2)
					continue;

				double y = (f.startX + size / 2 - this.posX) * vY / vX + this.posY;
				if (y >= f.startY - size / 2 && y <= f.endY + size / 2)
				{
					t = (f.startX + size / 2 - this.posX) / vX;
					collisionX = f.startX + size / 2;
					collisionY = y;
					collisionFace = f;
					break;
				}
			}
		}

		if (vY > 0)
		{
			for (Face2 f : faceList.topFaces)
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f, firstBounce) || f.startY < this.posY + size / 2)
					continue;

				double x = (f.startY - size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY - size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY - size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}
		else if (vY < 0)
		{
			for (Face2 f : faceList.bottomFaces)
			{
				double size = this.size;

				if (f.owner instanceof Movable)
					size *= tankHitSizeMul;

				if (passesThrough(f, firstBounce) || f.startY > this.posY - size / 2)
					continue;

				double x = (f.startY + size / 2 - this.posY) * vX / vY + this.posX;
				if (x >= f.startX - size / 2 && x <= f.endX + size / 2)
				{
					double t1 = (f.startY + size / 2 - this.posY) / vY;

					if (t1 == t)
						corner = true;
					else if (t1 < t)
					{
						collisionX = x;
						collisionY = f.startY + size / 2;
						collisionFace = f;
						t = t1;
					}
					break;
				}
			}
		}

		result.set(t, collisionX, collisionY, collisionFace, corner);
	}

	public boolean passesThrough(Face2 f, boolean firstBounce)
	{
		if ((asBullet ? !f.solidBullet : !f.solidTank) ||
				(f.owner == tank && firstBounce))
			return true;

		if (f.owner instanceof Obstacle && !((Obstacle) f.owner).bouncy)
		{
			Obstacle o = (Obstacle) f.owner;
			return (this.ignoreDestructible && o.destructible) || (this.ignoreShootThrough && o.shouldShootThrough);
		}

		return (ignoreTanks && f.owner instanceof Tank) || (ignoreBullets && f.owner instanceof Bullet);
	}

    private static final ObjectArrayList<DebugText> debugTexts = new ObjectArrayList<>();
    private static class DebugText
    {
        public TextWithStyling text;
        public double posX, posY;

        public DebugText(String text, int bounces, int bouncyBounces, int fontSize, double posX, double posY)
        {
            double[] col = Game.getRainbowColor((5 - bounces + 100 - bouncyBounces) * 0.2);
            this.text = new TextWithStyling(text, col[0], col[1], col[2], fontSize);
            this.posX = posX;
            this.posY = posY;
        }

        public void draw()
        {
            text.drawText(posX, posY);
        }
    }

    public static void drawDebug()
    {
        for (DebugText t : debugTexts)
            t.draw();
        debugTexts.clear();
    }

	public static final class Result
	{
		private double t, collisionX, collisionY;
		private Face2 collisionFace;
		private boolean corner;

		public void set(double t, double collisionX, double collisionY, Face2 collisionFace, boolean corner)
		{
			this.t = t;
			this.collisionX = collisionX;
			this.collisionY = collisionY;
			this.collisionFace = collisionFace;
			this.corner = corner;
		}
	}

	public double getDist()
	{
		this.bounceX.add(this.posX);
		this.bounceY.add(this.posY);

		if (!acquiredTarget)
			this.getTarget();

		return Math.sqrt(getSquaredFinalDist());
	}

	public double getTargetDist(double mul, Tank m)
	{
		return Math.sqrt(getSquaredTargetDist(mul, m));
	}

	public double getSquaredTargetDist(double mul, Tank m)
	{
		this.bounceX.add(0, this.posX);
		this.bounceY.add(0, this.posY);

		if (this.getTarget(mul, m) != m)
			return -1;

		return getSquaredFinalDist();
	}

	private double getSquaredFinalDist()
	{
		double dist = 0;
		for (int i = 0; i < this.bounceX.size() - 1; i++)
			dist += Math.pow(this.bounceX.getDouble(i + 1) - this.bounceX.getDouble(i), 2) + Math.pow(this.bounceY.getDouble(i + 1) - this.bounceY.getDouble(i), 2);

		if (this.bounces >= 0)
			dist += Chunk.chunkToPixel(maxChunkCheck);

		return dist;
	}

	private void addChunk(Chunk c)
	{
		if (c == null || (chunksAdded > 0 && c == chunkCache[chunksAdded - 1]))
			return;

		chunkCache[chunksAdded++] = c;
	}

	public double getAngleInDirection(double x, double y)
	{
		x -= this.posX;
		y -= this.posY;

		return Movable.getPolarDirection(x, y);
	}

	public boolean isInsideObstacle(double x, double y)
	{
		Obstacle o = Game.getObstacle(x, y);
		return o != null && collision(o) && !(ignoreShootThrough && o.shouldShootThrough) && !(ignoreDestructible && o.destructible);
	}

	public boolean collision(Obstacle o)
	{
		return asBullet ? o.bulletCollision : o.tankCollision;
	}
}

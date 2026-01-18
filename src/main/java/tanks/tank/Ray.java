package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.TextWithStyling;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Ray extends GameObject
{
    public static double min_trace_size = 5;

    public static int chunksAdded;
    /**
     * Caches the chunks to avoid creating new temp objects
     */
    public static Chunk[] chunkCache = new Chunk[40];
    /**
     * Caches the ray to avoid creating new temp objects
     */
    public static Ray cacheRay = new Ray();

    public double size, tankHitSizeMul;

    public int bounces, bouncyBounces;
    public double vX, vY, angle;
    public double startX, startY;

    public boolean enableBounciness, asBullet, trace, dotted;
    public boolean ignoreTanks, ignoreBullets, ignoreDestructible, ignoreShootThrough;

    public double speed, age, range;
    public int maxChunkCheck, traceAge;

    public Tank tank, targetTank;
    public double targetTankSizeMul;

    public ArrayList<Double> bounceX = new ArrayList<>();
    public ArrayList<Double> bounceY = new ArrayList<>();
    public double targetX, targetY;
    public boolean acquiredTarget = false;

    /**
     * Set this and use {@linkplain #chunkComparator} to sort the chunks by manhattan distance from this chunk
     */
    private static Chunk startingChunk = Chunk.zeroChunk;

    /**
     * Method references don't allocate new objects; however lambdas do (they create temp classes)
     */
    private static int keyExtractor(Chunk c)
    {
        return c.manhattanDist(startingChunk);
    }

    /**
     * Comparator that sorts the chunks by manhattan distance from {@linkplain #startingChunk}
     */
    private static final Comparator<Chunk> chunkComparator = (o1, o2) -> keyExtractor(o1) - keyExtractor(o2);

    /**
     * Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}
     */
    public static Ray newRay(double x, double y, double angle, int bounces, Tank tank)
    {
        return newRay(x, y, angle, bounces, tank, 10);
    }

    /**
     * Should be consumed immediately via getTarget or getDist. Otherwise, use {@linkplain #copy()}
     */
    public static Ray newRay(double x, double y, double angle, int bounces, Tank tank, double speed)
    {
        return cacheRay.set(x, y, angle, bounces, tank, speed);
    }

    /**
     * Creates another ray with the properties of the last ray.<br>
     * To set custom properties on your ray copy:
     * <blockquote><pre>
     *     Ray copy = Ray.newRay(params).copy()
     * </pre></blockquote>
     *
     */
    public Ray copy()
    {
        return new Ray().set(posX, posY, angle, bounces, tank, speed);
    }

    protected Ray()
    {
    }

    public Ray set(double x, double y, double angle, int bounces, Tank tank, double speed)
    {
        this.vX = speed * Math.cos(angle);
        this.vY = speed * Math.sin(angle);
        this.angle = angle;

        this.posX = this.startX = x;
        this.posY = this.startY = y;
        this.bounces = bounces;
        this.bouncyBounces = 100;
        setSize(10).setMaxChunks(100);

        this.trace = Game.traceAllRays;
        this.dotted = false;
        this.enableBounciness = true;
        this.ignoreTanks = false;
        this.ignoreBullets = true;
        this.asBullet = true;
        this.ignoreDestructible = false;
        this.ignoreShootThrough = false;

        this.age = 0;
        this.range = Double.MAX_VALUE;
        this.tankHitSizeMul = 1;
        this.acquiredTarget = false;
        this.tank = tank.getBottomLevelPossessing();

        this.bounceX.clear();
        this.bounceY.clear();

        return this;
    }

    private static final ArrayList<Chunk> errorChunkCache = new ArrayList<>();

    public static ErrorHandler<GameObject, Collection<Chunk>> ghostFaceHandler = new ErrorHandler<GameObject, Collection<Chunk>>(50, 2)
    {
        @Override
        public Collection<Chunk> containsErrors(GameObject obj)
        {
            errorChunkCache.clear();
            if (obj instanceof Obstacle)
            {
                Chunk c = Chunk.getChunk(obj.posX, obj.posY);
                if (c != null && !c.obstacles.contains(obj))
                {
                    errorChunkCache.add(c);
                    return errorChunkCache;
                }
            }
            else if (obj instanceof Movable)
            {
                for (Chunk c : ((Movable) obj).getCurrentChunks())
                {
                    if (!c.movables.contains(obj))
                        errorChunkCache.add(c);
                }
                if (!errorChunkCache.isEmpty())
                    return errorChunkCache;
            }
            return noErrorReturnValue();
        }

        @Override
        public void handleError(GameObject obj, Collection<Chunk> info)
        {
            System.err.printf("-----Ray collision face owner error-----%n" +
                    "%s not in %s%n",
                gameObjectString(obj),
                info.stream().map(Chunk::toString)
                .collect(Collectors.joining(", "))
            );
            if (!Game.disableErrorFixing && Game.currentLevel != null)
                Game.currentLevel.reloadTiles();
        }
    };

    public Movable getTarget(double mul, Tank targetTank)
    {
        this.targetTank = targetTank;
        this.targetTankSizeMul = mul;
        return this.getTarget();
    }

    public boolean isInSight(Movable target)
    {
        return setBouncyBounces(0).setAngleInDirection(target.posX, target.posY).getTarget() == target;
    }

    @Override
    public double getSize()
    {
        return size;
    }

    public Ray setAngleInDirection(double posX, double posY)
    {
        this.angle = getAngleInDirection(posX, posY);
        this.vX = speed * Math.cos(angle);
        this.vY = speed * Math.sin(angle);
        return this;
    }

    public Ray setVelocity(double vX, double vY)
    {
        this.vX = vX;
        this.vY = vY;
        this.angle = Movable.getPolarDirection(vX, vY);
        return this;
    }

    public Ray setShootThrough(boolean shootThrough)
    {
        this.ignoreShootThrough = shootThrough;
        return this;
    }

    public Ray setExplosive(boolean explosive)
    {
        this.ignoreDestructible = explosive;
        return this;
    }

    public Ray setBouncyBounces(int bouncyBounces)
    {
        this.bouncyBounces = bouncyBounces;
        return this;
    }

    public Ray setAsBullet(boolean testBulletCollision)
    {
        this.asBullet = testBulletCollision;
        return this;
    }

    public Ray setMaxChunks(int maxChunks)
    {
        this.maxChunkCheck = maxChunks;
        return this;
    }

    public Ray setTrace(boolean trace, boolean dotted)
    {
        this.trace = trace;
        this.dotted = dotted;
        return this;
    }

    public Ray setRange(double distance)
    {
        range = distance;
        return setMaxChunks((int) (distance / Game.tile_size / Chunk.chunkSize + 2));
    }

    public Ray setSize(double size)
    {
        this.size = size;
        return this;
    }

    public Ray moveOut(double amount)
    {
        this.posX += this.vX * amount;
        this.posY += this.vY * amount;
        return this;
    }

    public static final Result result = new Result();
    private static final Result tempResult = new Result();

    public Movable getTarget()
    {
        acquiredTarget = true;

        if (isOutOfBounds() || testInsideObstacle())
            return null;

        if (!ignoreTanks)
        {
            for (Movable m : Movable.getSquareCollision(this))
            {
                if (m instanceof Tank && m != this.tank)
                    return m;
            }
        }

        this.bounceX.add(posX);
        this.bounceY.add(posY);

        boolean firstBounce = this.targetTank == null;
        Movable target = null;

        while (this.bounces >= 0 && this.bouncyBounces >= 0)
        {
            totalChunksChecked = 0;
            Chunk current = Chunk.getChunk(posX, posY);
            if (current == null)
                break;

            checkCollision(current, firstBounce);

            this.age += result.t;

            firstBounce = false;

            if (result.collisionFace == null)
                break;

            double dx = result.collisionX - posX, dy = result.collisionY - posY;

            double dist = Math.sqrt(dx * dx + dy * dy);
            if (this.range < dist)
            {
                result.collisionX = posX + dx * range / dist;
                result.collisionY = posY + dy * range / dist;
                this.bounces = -1;
            }
            else
                this.range -= dist;

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

            ISolidObject obj = result.collisionFace.owner;
            if (obj instanceof GameObject)
                ghostFaceHandler.checkForErrors((GameObject) obj);

            if (obj instanceof Movable)
            {
                this.targetX = result.collisionX;
                this.targetY = result.collisionY;
                bounceX.add(result.collisionX);
                bounceY.add(result.collisionY);

                target = (Movable) obj;
                break;
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

        renderTraceEffect();
        return target;
    }

    public void renderTraceEffect()
    {
        if (!trace || !ScreenGame.isUpdatingGame())
            return;

        if (bounceX.size() == 1)
        {
            double boundedRange = Math.min(Game.tile_size * 75, Math.min(maxChunkCheck * Chunk.chunkSize * Game.tile_size, range));
            bounceX.add(posX + Math.cos(angle) * boundedRange);
            bounceY.add(posY + Math.sin(angle) * boundedRange);
        }

        for (int i = 1; i < bounceX.size(); i++)
        {
            double prevX = bounceX.get(i - 1);
            double prevY = bounceY.get(i - 1);
            double dx = bounceX.get(i) - prevX;
            double dy = bounceY.get(i) - prevY;
            double steps = (Math.sqrt((Math.pow(dx, 2) + Math.pow(dy, 2)) / (1 + Math.pow(this.vX, 2) + Math.pow(this.vY, 2))) / Math.max(this.size, 2) * 10 + 1);

            if (dotted)
                steps /= 2;

            double s;
            for (s = 0; s <= steps; s++)
            {
                double x = prevX + dx * s / steps;
                double y = prevY + dy * s / steps;

                this.traceAge++;

                double frac = 1 / (1 + this.traceAge / 100.0);
                double z = this.tank.size / 2 + this.tank.turretSize / 2 * frac + (Game.tile_size / 4) * (1 - frac);
                if (Game.screen instanceof ScreenGame && !ScreenGame.finished)
                {
                    Game.effects.add(Effect.createNewEffect(x, y, z, Effect.EffectType.ray)
                        .setSize(Math.max(this.size, min_trace_size)));
                }
            }
        }
    }

    private int totalChunksChecked = 0;

    public void checkCollision(Chunk current, boolean firstBounce)
    {
        if (current == null)
            return;

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
            // add chunk in front
            addChunk(mid);

            // if the ray moved diagonally, add the chunks on the sides
            if (mid == null || current.manhattanDist(mid) > 1)
            {
                addChunk(Chunk.getChunk(posX + moveXPrev, posY + moveY));
                addChunk(Chunk.getChunk(posX + moveX, posY + moveYPrev));
            }

            if (Chunk.debug && trace && totalChunksChecked > 0)
            {
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

            if (chunksAdded == 0)
                break;

            // sort the chunks by distance from the current chunk
            Arrays.sort(chunkCache, 0, chunksAdded, chunkComparator);

            for (int i = 0; i < chunksAdded; i++)
            {
                Chunk chunk = chunkCache[i];
                if (chunk == null)
                    continue;

                checkCollisionInChunk(result, chunk, firstBounce);

                if (result.collisionFace != null)
                {
                    double x = result.collisionX, y = result.collisionY, bound = size / 2;
                    for (Chunk c : Chunk.getChunksInRange(x - bound, y - bound, x + bound, y + bound))
                    {
                        if (c == chunk)
                            continue;

                        checkCollisionInChunk(tempResult, c, firstBounce);
                        if (tempResult.collisionFace != null && tempResult.t < result.t)
                            result.set(tempResult.t, tempResult.collisionX, tempResult.collisionY, tempResult.collisionFace, tempResult.corner);
                    }
                    break chunkCheck;
                }
            }
        }
    }

    public boolean isOutOfBounds()
    {
        return posX <= 0 || posX > Game.currentSizeX * Game.tile_size || posY <= 0 || posY > Game.currentSizeY * Game.tile_size;
    }

    public boolean testInsideObstacle()
    {
        return isInsideObstacle(posX - size / 2, posY - size / 2) ||
            isInsideObstacle(posX + size / 2, posY - size / 2) ||
            isInsideObstacle(posX + size / 2, posY + size / 2) ||
            isInsideObstacle(posX - size / 2, posY + size / 2);
    }

    public void checkCollisionInChunk(Result result, Chunk chunk, boolean firstBounce)
    {
        Face collisionFace = null;
        double t = Double.MAX_VALUE;
        boolean corner = false;
        double collisionX = 0, collisionY = 0;

        if (vX > 0)
        {
            for (Face f : chunk.faces.leftFaces)
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

                    if (outOfRange(collisionX, collisionY))
                        continue;

                    collisionFace = f;
                    break;
                }
            }
        }
        else if (vX < 0)
        {
            for (Face f : chunk.faces.rightFaces)
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

                    if (outOfRange(collisionX, collisionY))
                        continue;

                    collisionFace = f;
                    break;
                }
            }
        }

        if (vY > 0)
        {
            for (Face f : chunk.faces.topFaces)
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

                        if (outOfRange(collisionX, collisionY))
                            continue;

                        collisionFace = f;
                        t = t1;
                    }
                    break;
                }
            }
        }
        else if (vY < 0)
        {
            for (Face f : chunk.faces.bottomFaces)
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

                        if (outOfRange(collisionX, collisionY))
                            continue;

                        collisionFace = f;
                        t = t1;
                    }
                    break;
                }
            }
        }

        totalChunksChecked++;

        if (Chunk.debug && trace)
        {
            // displays the order of chunks checked and locations that the ray checked
            debugTexts.add(new DebugText(
                "" + totalChunksChecked, bounces, bouncyBounces, 48,
                (chunk.chunkX + 0.5) * Chunk.chunkSize * Game.tile_size + (totalChunksChecked * 15),
                (chunk.chunkY + 0.5) * Chunk.chunkSize * Game.tile_size
            ));
        }

        result.set(t, collisionX, collisionY, collisionFace, corner);
    }

    public boolean outOfRange(double collisionX, double collisionY)
    {
        return GameObject.sqDistBetw(collisionX, collisionY, posX, posY) > range * range;
    }

    public boolean passesThrough(Face f, boolean firstBounce)
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

    private static final ArrayList<DebugText> debugTexts = new ArrayList<>();

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
        private Face collisionFace;
        private boolean corner;

        public void set(double t, double collisionX, double collisionY, Face collisionFace, boolean corner)
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
            dist += Math.pow(this.bounceX.get(i + 1) - this.bounceX.get(i), 2) + Math.pow(this.bounceY.get(i + 1) - this.bounceY.get(i), 2);

        if (this.bounces >= 0)
            dist += Chunk.chunkToGame(maxChunkCheck);

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

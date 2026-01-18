package tanks;

import basewindow.IBatchRenderableObject;
import tanks.obstacle.Face;
import tanks.obstacle.ISolidObject;
import tanks.obstacle.Obstacle;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class Chunk
{
    private static final int TILE_DEPTH_VARIATION = 10;
    public static Level defaultLevel = new Level("{28,18|,|,}");
    public static final Chunk zeroChunk = new Chunk();
    public static boolean debug = false;

    public static HashMap<Integer, Chunk> chunkMap = new HashMap<>();
    public static ArrayList<Chunk> chunkList = new ArrayList<>();
    private static final ArrayList<Chunk> chunkCache = new ArrayList<>();
    public static int chunkSize = 8;

    private static final HashSet<Chunk> dirtyChunks = new HashSet<>();

    public final Level level;
    public final int chunkX, chunkY;
    public Face[] borderFaces = new Face[4];
    public final HashSet<Obstacle> obstacles = new HashSet<>();
    public final HashSet<Movable> movables = new HashSet<>();

    /**
     * Stores faces of Movables, which are updated every frame
     */
    public final FaceList faces = new FaceList(this);
    public final Tile[][] tileGrid = new Tile[chunkSize][chunkSize];

    /**
     * The variable that caches the previous call to {@link Chunk#getChunk}
     */
    private static Chunk prevChunk;

    public Chunk(Level l, Random r, int x, int y)
    {
        this.chunkX = x;
        this.chunkY = y;
        this.level = l;

        for (int i = 0; i < chunkSize; i++)
        {
            for (int j = 0; j < chunkSize; j++)
            {
                if (tileGrid[i][j] == null)
                    tileGrid[i][j] = setTileColor(l, r, new Tile());
            }
        }
    }

    private Chunk()
    {
        this.level = null;
        this.chunkX = 0;
        this.chunkY = 0;
    }

    /**
     * Iterates in a diamond shape (like BFS) outwards until the manhattan distance traveled is >= maxChunks.
     *
     * @return the chunks within the range
     */
    public static ArrayList<Chunk> iterateOutwards(int tileX, int tileY, int maxChunks)
    {
        chunkCache.clear();
        queue.clear();
        visited.clear();

        Chunk start = Chunk.getChunk(tileX, tileY);

        if (start != null)
        {
            queue.add(start);

            while (!queue.isEmpty())
            {
                Chunk c = queue.remove();
                for (int i = 0; i < 4; i++)
                {
                    int newX = c.chunkX + Direction.X[i];
                    int newY = c.chunkY + Direction.Y[i];
                    Chunk next = Chunk.getChunkCoords(newX, newY);
                    if (next != null && start.manhattanDist(next) < maxChunks && visited.add(next))
                    {
                        chunkCache.add(next);
                        queue.add(next);
                    }
                }
            }
        }

        return chunkCache;
    }


    /**
     * Adds a level border on the specified side of the chunk, where rays will collide off of.
     *
     * @param dir The side of the chunk to add the border on
     * @param l   The level to get the border coordinates from
     */
    public void addBorderFace(Direction dir, Level l)
    {
        int side = dir.index();
        Face f = new Face(null,
            convert(chunkX + Face.x1[side], l, true),
            convert(chunkY + Face.y1[side], l, false),
            convert(chunkX + Face.x2[side], l, true),
            convert(chunkY + Face.y2[side], l, false),
            dir, true, true);
        borderFaces[side] = f;
        faces.getSide(dir.opposite().index()).add(f);
    }

    /**
     * Helper to convert chunk coordinates to game coordinates and clamp it to the level size.
     */
    private static double convert(int chunk, Level l, boolean isX)
    {
        return Math.max(isX ? l.startX : l.startY, Math.min(isX ? l.sizeX : l.sizeY, chunk * Chunk.chunkSize)) * Game.tile_size;
    }

    public static boolean initialized()
    {
        return !Chunk.chunkList.isEmpty();
    }

    /**
     * Iterates in a diamond shape (like BFS) outwards until the manhattan distance traveled is >= maxChunks.
     *
     * @return the chunks within the range
     */
    public static ArrayList<Chunk> iterateOutwards(double posX, double posY, int maxChunks)
    {
        return iterateOutwards((int) (posX / Game.tile_size), (int) (posY / Game.tile_size), maxChunks);
    }

    static Queue<Chunk> queue = new LinkedList<>();
    static HashSet<Chunk> visited = new HashSet<>();

    public static double chunkToGame(double chunkPos)
    {
        return chunkPos * Chunk.chunkSize * Game.tile_size;
    }

    public static void handleDirtyChunks()
    {
        if (dirtyChunks.isEmpty())
            return;

        for (Chunk c : dirtyChunks)
            c.faces.sort();

        dirtyChunks.clear();
    }

    public int manhattanDist(Chunk other)
    {
        return Math.abs(chunkX - other.chunkX) + Math.abs(chunkY - other.chunkY);
    }

    public void addMovable(Movable m)
    {
        if (m == null)
            return;

        movables.add(m);
        m.prevChunks.add(this);
        faces.addFaces(m);
    }

    public void removeMovable(Movable m)
    {
        if (m == null)
            return;

        movables.remove(m);
        faces.removeFaces(m);
    }

    @Override
    public String toString()
    {
        return String.format(
            "C(%d, %d)[%.0f, %.0f]-[%.0f, %.0f]",
            this.chunkX, this.chunkY,
            Chunk.chunkToGame(this.chunkX), Chunk.chunkToGame(this.chunkY),
            Chunk.chunkToGame(this.chunkX + 1), Chunk.chunkToGame(this.chunkY + 1)
        );
    }

    public void markDirty()
    {
        dirtyChunks.add(this);
    }

    public void addObstacle(Obstacle o)
    {
        if (o == null)
            return;

        setObstacle(gameToPosInChunk(o.posX), gameToPosInChunk(o.posY), o);
        obstacles.add(o);
        faces.addFaces(o);
    }

    public void removeObstacle(Obstacle o)
    {
        if (o == null)
            return;

        tileGrid[gameToPosInChunk(o.posX)][gameToPosInChunk(o.posY)].remove(o);
        obstacles.remove(o);
        faces.removeFaces(o);
    }

    public static ArrayList<Chunk> getChunksInRange(double x1, double y1, double x2, double y2)
    {
        return getChunksInRange((int) (x1 / Game.tile_size), (int) (y1 / Game.tile_size),
            (int) (x2 / Game.tile_size), (int) (y2 / Game.tile_size));
    }

    public static ArrayList<Chunk> getChunksInRange(int tx1, int ty1, int tx2, int ty2)
    {
        int x1 = tx1 / chunkSize, y1 = ty1 / chunkSize, x2 = tx2 / chunkSize, y2 = ty2 / chunkSize;
        chunkCache.clear();
        for (Chunk c : chunkList)
        {
            if (Game.isOrdered(true, x1, c.chunkX, x2)
                && Game.isOrdered(true, y1, c.chunkY, y2))
                chunkCache.add(c);
        }
        return chunkCache;
    }

    public static Chunk runIfChunkPresent(int tileX, int tileY, Consumer<Chunk> tc)
    {
        Chunk c = getChunk(tileX, tileY);
        if (c != null)
            tc.accept(c);
        return c;
    }

    public static Chunk runIfChunkPresent(double posX, double posY, Consumer<Chunk> tc)
    {
        return runIfChunkPresent((int) (posX / Game.tile_size), (int) (posY / Game.tile_size), tc);
    }

    public static Tile runIfTilePresent(int tileX, int tileY, Consumer<Tile> tc)
    {
        Tile t = getTile(tileX, tileY);
        if (t != null)
            tc.accept(t);
        return t;
    }

    public static void runIfTilePresent(double posX, double posY, Consumer<Tile> tc)
    {
        Tile t = getTile(posX, posY);
        if (t != null)
            tc.accept(t);
    }

    private static Tile returnTile(Tile tile)
    {
        return tile;
    }

    public static <K> K getChunkIfPresent(int tileX, int tileY, K fallback, Function<Chunk, K> func)
    {
        Chunk c = getChunk(tileX, tileY);
        if (c == null) return fallback;
        return func.apply(c);
    }

    public static <K> K getChunkIfPresent(double posX, double posY, K fallback, Function<Chunk, K> func)
    {
        return getChunkIfPresent(((int) (posX / Game.tile_size)), ((int) (posY / Game.tile_size)), fallback, func);
    }

    public static Tile getOrDefault(int tileX, int tileY)
    {
        return getOrElse(tileX, tileY, Tile.fallbackTile);
    }

    public static Tile getOrElse(int tileX, int tileY, Tile fallback)
    {
        return getIfPresent(tileX, tileY, fallback, Chunk::returnTile);
    }

    public static <K> K getIfPresent(double tileX, double tileY, K fallback, Function<Tile, K> func)
    {
        return getIfPresent(((int) (tileX / Game.tile_size)), ((int) (tileY / Game.tile_size)), fallback, func);
    }

    public static <K> K get(double tileX, double tileY, Function<Tile, K> func)
    {
        Tile t = getTile(tileX, tileY);
        if (t == null) throw new RuntimeException("not present");
        return func.apply(t);
    }

    public static <K> K getIfPresent(int tileX, int tileY, K fallback, Function<Tile, K> func)
    {
        Tile t = getTile(tileX, tileY);
        if (t == null) return fallback;
        return func.apply(t);
    }

    /**
     * Expects all pixel coordinates.
     */
    public static ArrayList<Chunk> getChunksInRadius(double x1, double y1, double radius)
    {
        return getChunksInRadius((int) (x1 / Game.tile_size), (int) (y1 / Game.tile_size), radius / Game.tile_size);
    }

    /**
     * Expects all tile coordinates.
     */
    public static ArrayList<Chunk> getChunksInRadius(int tx1, int ty1, double radius)
    {
        chunkCache.clear();
        double x1 = (double) tx1 / chunkSize, y1 = (double) ty1 / chunkSize, cRad = Math.ceil(radius / chunkSize) + 1;
        for (Chunk chunk : chunkList)
        {
            if ((chunk.chunkX - x1) * (chunk.chunkX - x1) +
                (chunk.chunkY - y1) * (chunk.chunkY - y1) <= cRad * cRad)
                chunkCache.add(chunk);
        }
        return chunkCache;
    }

    public static Tile setTileColor(Level l, Random r, Tile t)
    {
        t.colR = l.color.red + (Game.fancyTerrain ? r.nextDouble() * l.colorVar.red : 0);
        t.colG = l.color.green + (Game.fancyTerrain ? r.nextDouble() * l.colorVar.green : 0);
        t.colB = l.color.blue + (Game.fancyTerrain ? r.nextDouble() * l.colorVar.blue : 0);
        t.depth = Game.fancyTerrain && Game.enable3dBg ? r.nextDouble() * TILE_DEPTH_VARIATION : 0;
        return t;
    }

    public static void update()
    {

    }

    /**
     * @param pix Coordinate in pixels
     * @return The tile position relative to the top left corner of the chunk the coordinate is in
     */
    public static int gameToPosInChunk(double pix)
    {
        return (int) (pix / Game.tile_size) % chunkSize;
    }

    /**
     * Automatically converts to chunk coordinates.
     */
    public Tile getChunkTile(int posX, int posY)
    {
        if (posX < 0 || posY < 0)
            return null;

        return tileGrid[posX % chunkSize][posY % chunkSize];
    }

    public void setObstacle(int x, int y, Obstacle o)
    {
        Tile t = tileGrid[x][y];
        if (t.canPlaceOn(o))
            t.add(o);
    }

    /**
     * Expects tile coordinates.
     */
    public static Tile getTile(int tileX, int tileY)
    {
        Chunk c = getChunk(tileX, tileY);
        if (c == null)
            return null;
        return c.getChunkTile(tileX, tileY);
    }

    /**
     * Expects pixel coordinates.
     */
    public static Tile getTile(double posX, double posY)
    {
        Chunk c = getChunk(posX, posY);
        if (c == null)
            return null;
        return c.getChunkTile(posX, posY);
    }

    /**
     * Automatically converts to tile coordinates and then chunk coordinates.
     */
    public Tile getChunkTile(double posX, double posY)
    {
        if (posX < 0 || posX >= Game.currentSizeX * Game.tile_size || posY < 0 || posY >= Game.currentSizeY * Game.tile_size)
            return null;

        return tileGrid[gameToPosInChunk(posX)][gameToPosInChunk(posY)];
    }

    public static double addCoords(double chunk, double tile)
    {
        return chunk * chunkSize + tile;
    }

    public static void drawDebugStuff()
    {
        if (!debug)
            return;

        Drawing.drawing.setColor(255, 255, 0, 128);

        for (Chunk c : chunkList)
        {
            int i = 0;
            for (Face f : c.borderFaces)
            {
                if (f != null)
                {
                    Drawing.drawing.setColor(50, 50, 255);
                    drawClampedRect(
                        Game.currentLevel != null ? Game.currentLevel : defaultLevel,
                        f.startX, f.startY,
                        f.endX, f.endY
                    );
                }
                else
                {
                    double x = c.chunkX * chunkSize * Game.tile_size;
                    double y = c.chunkY * chunkSize * Game.tile_size;
                    double sX = chunkSize * Game.tile_size;
                    double sY = chunkSize * Game.tile_size;

                    Drawing.drawing.setColor(255, 255, 0);
                    drawClampedRect(
                        Game.currentLevel != null ? Game.currentLevel : defaultLevel,
                        x + sX * Face.x1[i],
                        y + sY * Face.y1[i],
                        x + sX * (Face.x2[i] - Face.x1[i]),
                        y + sY * (Face.y2[i] - Face.y1[i])
                    );
                }
                i += 1;
            }
        }

        Drawing.drawing.setColor(255, 255, 255);
//        for (Movable m : Game.movables)
//            Drawing.drawing.drawText(m.posX, m.posY, m.getCurrentChunks().stream().map(c -> "(" + c.chunkX + ", " + c.chunkY + ")")
//                .collect(Collectors.joining(", ")));
    }

    /**
     * Given a rectangle's bounding box, clamps it to the level borders and draws it.
     * Also ensures a line width of 2.
     */
    private static void drawClampedRect(Level l, double x1, double y1, double x2, double y2)
    {
        double sX = Math.max(1 / Drawing.drawing.scale, Math.min(l.sizeX * Game.tile_size - x1, x2 - x1));
        double sY = Math.max(1 / Drawing.drawing.scale, Math.min(l.sizeY * Game.tile_size - y1, y2 - y1));
        Drawing.drawing.fillRect(x1 + sX / 2, y1 + sY / 2, sX, sY);
    }

    public static void initialize()
    {
        populateChunks(defaultLevel);
    }

    public static void populateChunks(Level l)
    {
        populateChunks(l, true);
    }

    public static void populateChunks(Level l, boolean clear)
    {
        if (clear)
        {
            chunkMap.clear();
            chunkList.clear();
            prevChunk = null;
        }

        int startX = l.startX / chunkSize, startY = l.startY / chunkSize;
        int sX = l.sizeX / chunkSize + 1, sY = l.sizeY / chunkSize + 1;
        Random r = new Random(l.tilesRandomSeed);

        for (int x = 0; x < sX; x++)
            for (int y = 0; y < sY; y++)
                addChunk(x + startX, y + startY, new Chunk(l, r, x, y));
    }

    /**
     * Expects pixel coordinates.
     */
    public static Chunk getChunk(double posX, double posY)
    {
        return getChunk((int) (posX / Game.tile_size), (int) (posY / Game.tile_size));
    }

    /**
     * Expects tile coordinates.
     */
    public static Chunk getChunk(int tileX, int tileY)
    {
        return getChunkCoords(tileX / Chunk.chunkSize, tileY / Chunk.chunkSize);
    }

    /**
     * Expects chunk coordinates.
     */
    public static Chunk getChunkCoords(int chunkX, int chunkY)
    {
        if (prevChunk != null && prevChunk.chunkX == chunkX && prevChunk.chunkY == chunkY)
            return prevChunk;

        Chunk c = chunkMap.get(encodeChunkCoords(chunkX, chunkY));
        if (c != null)
            prevChunk = c;
        return c;
    }

    public static Chunk addChunk(int chunkX, int chunkY, Chunk c)
    {
        chunkList.add(c);
        return chunkMap.put(encodeChunkCoords(chunkX, chunkY), c);
    }

    public static int f(int i)
    {
        return 1664525 * i + 1013904223;
    }

    public static int encodeChunkCoords(int chunkX, int chunkY)
    {
        return f(f(chunkX) + chunkY);
    }

    public static class FaceList
    {
        public final Chunk chunk;

        /**
         * dynamic x, static y
         */
        public final ArrayList<Face> topFaces = new ArrayList<>();
        /**
         * dynamic x, static y
         */
        public final ArrayList<Face> bottomFaces = new ArrayList<>();
        /**
         * static x, dynamic y
         */
        public final ArrayList<Face> leftFaces = new ArrayList<>();
        /**
         * static x, dynamic y
         */
        public final ArrayList<Face> rightFaces = new ArrayList<>();

        public FaceList(Chunk chunk)
        {
            this.chunk = chunk;
        }

        public boolean addFaces(ISolidObject s)
        {
            if (s.disableRayCollision())
                return false;

            Face[] faces = s.getFaces();
            boolean changed = false;
            for (int i = 0; i < 4; i++)
            {
                if (faces[i].valid)
                    changed |= getSide(i).add(faces[i]);
            }
            if (changed)
                chunk.markDirty();
            return changed;
        }

        public boolean removeFaces(ISolidObject s)
        {
            if (s.disableRayCollision())
                return false;

            Face[] faces = s.getFaces();
            boolean changed = false;
            for (int i = 0; i < 4; i++)
                changed |= getSide(i).remove(faces[i]);
            return changed;
        }

        public void sort()
        {
            // Do not replace with <list>.sort(). This breaks the iOS compiler.
            Collections.sort(topFaces);
            Collections.sort(bottomFaces, Collections.reverseOrder());
            Collections.sort(leftFaces);
            Collections.sort(rightFaces, Collections.reverseOrder());

            for (int i = 0; i < 4; i++)
            {
                ArrayList<Face> f = getSide(i);
                for (int j = 1; j < f.size(); j++)
                {
                    if (f.get(j) == null || f.get(j).equals(f.get(j - 1)))
                        f.remove(j--);
                }
            }
        }

        public ArrayList<Face> getSide(int side)
        {
            switch (side)
            {
                case 0:
                    return topFaces;
                case 1:
                    return rightFaces;
                case 2:
                    return bottomFaces;
                case 3:
                    return leftFaces;
                default:
                    throw new RuntimeException("Invalid side: " + side);
            }
        }

        public void clear()
        {
            topFaces.clear();
            bottomFaces.clear();
            leftFaces.clear();
            rightFaces.clear();
        }
    }

    public static class Tile implements IBatchRenderableObject
    {
        public static Tile fallbackTile = new Tile();

        public Obstacle fullObstacle, surfaceObstacle, extraObstacle;
        public double colR, colG, colB, depth;

        /**
         * For use in level loading only
         */
        public boolean solidTank;

        public double height()
        {
            return obstacle() != null ? obstacle().getTileHeight() : -1000;
        }

        public double tileDepth()
        {
            return depth;
        }

        public double edgeDepth()
        {
            return obstacle() != null ? obstacle().getEdgeDrawDepth() : 0;
        }

        public double groundHeight()
        {
            return obstacle() != null && obstacle().replaceTiles ? obstacle().getGroundHeight() : depth;
        }

        public boolean tankSolid()
        {
            return obstacle() != null && obstacle().tankCollision;
        }

        public boolean bulletSolid()
        {
            return obstacle() != null && obstacle().bulletCollision;
        }

        public Obstacle obstacle()
        {
            return fullObstacle != null ? fullObstacle : surfaceObstacle != null ? surfaceObstacle : extraObstacle;
        }

        public void remove(Obstacle o)
        {
            if (fullObstacle == o)
                fullObstacle = null;
            else if (surfaceObstacle == o)
                surfaceObstacle = null;
            else if (extraObstacle == o)
                extraObstacle = null;
        }

        public void add(Obstacle o)
        {
            o.baseGroundHeight = depth;
            if (o.type == Obstacle.ObstacleType.full || o.type == Obstacle.ObstacleType.top)
                fullObstacle = o;
            else if (o.type == Obstacle.ObstacleType.ground)
                surfaceObstacle = o;
            else if (o.type == Obstacle.ObstacleType.extra)
                extraObstacle = o;
            else
                throw new RuntimeException("New obstacle type added! Make sure to add it to Chunk.Tile.add()");
        }

        public boolean canPlaceOn(GameObject o)
        {
            if (!(o instanceof Obstacle))
                return obstacle() == null || !obstacle().tankCollision;

            Obstacle.ObstacleType t = ((Obstacle) o).type;
            boolean canPlaceUnder = fullObstacle == null || obstacle().type == Obstacle.ObstacleType.top || obstacle().type == Obstacle.ObstacleType.extra;

            if (t == Obstacle.ObstacleType.full || t == Obstacle.ObstacleType.top)
                return fullObstacle == null;
            if (t == Obstacle.ObstacleType.ground)
                return canPlaceUnder && surfaceObstacle == null;
            if (t == Obstacle.ObstacleType.extra)
                return canPlaceUnder && extraObstacle == null;

            throw new RuntimeException("New obstacle type added! Make sure to add it to Chunk.Tile.canPlaceOn()");
        }

        @Override
        public String toString()
        {
            return "Tile(" +
                "fullObstacle=" + fullObstacle +
                ", surfaceObstacle=" + surfaceObstacle +
                ", extraObstacle=" + extraObstacle +
                ')';
        }
    }
}

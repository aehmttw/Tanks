package tanks;

import basewindow.IBatchRenderableObject;
import tanks.obstacle.Obstacle;

import java.util.Random;

public class Chunk
{
    private static final int TILE_DEPTH_VARIATION = 10;
    public static Level defaultLevel = new Level("{28,18|,|,}");

    public static void runIfTilePresent(int tileX, int tileY, Consumer<Tile> tc)
    {
        Tile t = getTile(tileX, tileY);
        if (t != null)
            tc.accept(t);
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

    public static Tile getOrDefault(int tileX, int tileY)
    {
        return getOrFallback(tileX, tileY, Tile.fallbackTile);
    }

    public static Tile getOrFallback(int tileX, int tileY, Tile fallback)
    {
        return getIfPresent(tileX, tileY, fallback, Chunk::returnTile);
    }

    public static <K> K getIfPresent(double tileX, double tileY, K fallback, Function<Tile, K> func)
    {
        return getIfPresent(((int) (tileX / Game.tile_size)), ((int) (tileY / Game.tile_size)), fallback, func);
    }

    public static <K> K getIfPresent(int tileX, int tileY, K fallback, Function<Tile, K> func)
    {
        Tile t = getTile(tileX, tileY);
        if (t == null) return fallback;
        return func.apply(t);
    }

    public static Tile getTile(double x, double y)
    {
        return getTile((int) (x / Game.tile_size), (int) (y / Game.tile_size));
    }

    public static Tile getTile(int x, int y)
    {
        if (x < 0 || y < 0 || x >= Game.currentSizeX || y >= Game.currentSizeY)
            return null;
        return Game.tiles[x][y];
    }

    public static void populateChunks()
    {
        populateChunks(defaultLevel);
    }

    public static void populateChunks(Level l)
    {
        Game.tiles = new Tile[l.sizeX][l.sizeY];

        int var = Game.fancyTerrain ? 1 : 0;

        Random tilesRandom = new Random(0);
        for (int i = 0; i < l.sizeX; i++)
        {
            for (int j = 0; j < l.sizeY; j++)
            {
                Tile t = new Tile();
                t.colR = (l.colorR + tilesRandom.nextDouble() * l.colorVarR * var);
                t.colG = (l.colorG + tilesRandom.nextDouble() * l.colorVarG * var);
                t.colB = (l.colorB + tilesRandom.nextDouble() * l.colorVarB * var);
                t.depth = Game.enable3dBg ? tilesRandom.nextDouble() * TILE_DEPTH_VARIATION * var : 0;
                Game.tiles[i][j] = t;
            }
        }
    }

    public static class Tile implements IBatchRenderableObject
    {
        public static Tile fallbackTile = new Tile();

        public Obstacle fullObstacle, surfaceObstacle, extraObstacle;
        public double colR, colG, colB, depth;

        /** For use in level loading only */
        public boolean solidTank;

        public double drawDepth()
        {
            return obstacle() == null ? depth : 0;
        }

        public double height()
        {
            return obstacle() != null ? obstacle().getTileHeight() : -1000;
        }

        public double edgeDepth()
        {
            return obstacle() != null ? obstacle().getEdgeDrawDepth() : 0;
        }

        public double groundHeight()
        {
            return obstacle() != null ? obstacle().getGroundHeight() : 0;
        }

        public boolean solid()
        {
            return (fullObstacle != null && fullObstacle.bulletCollision) ||
                    (surfaceObstacle != null && surfaceObstacle.bulletCollision) ||
                    (extraObstacle != null && extraObstacle.bulletCollision);
        }

        public boolean unbreakable()
        {
            return obstacle() != null && !obstacle().shouldShootThrough;
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
            if (o.type == Obstacle.ObstacleType.full || o.type == Obstacle.ObstacleType.top)
                fullObstacle = o;
            else if (o.type == Obstacle.ObstacleType.ground)
                surfaceObstacle = o;
            else if (o.type == Obstacle.ObstacleType.extra)
                extraObstacle = o;
            else
                throw new RuntimeException("New obstacle type added! Make sure to add it to Chunk.Tile.add()");
        }
    }
}

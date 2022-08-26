package tanks.modapi;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.Item;
import tanks.modapi.events.EventLoadMapLevel;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

import java.util.ArrayList;
import java.util.HashSet;

public class MapLoader
{
    /**
     * Distance (in tiles) before loading a new level onto the map
     */
    public static int tilesBeforeLoad = 20;
    public int loadDistance;

    public Section[][] grid;
    public Section current;
    public boolean loaded = false;
    public boolean isRemote = false;
    public int spawnX;
    public int spawnY;
    public int currentX;
    public int currentY;

    protected Tank targetTank;
    protected Player targetPlayer;
    protected static double redrawCounter = 0;
    protected HashSet<Section> visited = new HashSet<>();

    public MapLoader(String[][] levelStrings, int spawnX, int spawnY)
    {
        this.grid = new Section[levelStrings.length][levelStrings[0].length];

        for (int y = 0; y < levelStrings.length; y++)
        {
            for (int x = 0; x < levelStrings[0].length; x++)
            {
                int totalX = x == 0 ? 0 : this.grid[y][x - 1].totalX;
                int totalY = y == 0 ? 0 : this.grid[y - 1][x].totalY;

                this.grid[y][x] = new Section(new Level(levelStrings[y][x]), totalX, totalY);
            }
        }

        setVars(spawnX, spawnY);
    }

    public MapLoader(Level[][] grid, int spawnX, int spawnY)
    {
        this.grid = new Section[grid.length][grid[0].length];

        for (int y = 0; y < grid.length; y++)
        {
            for (int x = 0; x < grid[0].length; x++)
            {
                int totalX = x == 0 ? 0 : this.grid[y][x - 1].totalX;
                int totalY = y == 0 ? 0 : this.grid[y - 1][x].totalY;

                this.grid[y][x] = new Section(grid[y][x], totalX, totalY);
            }
        }

        setVars(spawnX, spawnY);
    }

    public MapLoader(int sizeX, int sizeY, int spawnX, int spawnY)
    {
        this.isRemote = true;
        this.grid = new Section[sizeY][sizeX];
        this.currentX = spawnX;
        this.currentY = spawnY;
    }

    public void load()
    {
        if (loaded)
            return;

        loaded = true;

        this.loadDistance = tilesBeforeLoad;
        tilesBeforeLoad = 20;

        if (!this.isRemote)
        {
            this.current = this.grid[spawnY][spawnX];
            this.current.level.loadLevel();
            this.current.loaded = true;
        }

        Game.screen = new ScreenGame();
    }

    public void update()
    {
        if (!loaded)
            return;

        for (Player p : Game.players)
        {
            targetPlayer = p;
            targetTank = p.tank;

            setCurrentPos();

            visited.clear();
            handleCloseTiles(currentX, currentY);
        }

        if (redrawCounter > 0)
            redrawCounter -= Panel.frameFrequency;

        if (redrawCounter < 0)
        {
            redrawCounter = 0;
            Drawing.drawing.forceRedrawTerrain();
        }
    }

    private void setVars(int spawnX, int spawnY)
    {
        this.spawnX = spawnX;
        this.spawnY = spawnY;

        for (Section[] sections : this.grid)
        {
            for (Section s : sections)
                s.grid = this.grid;
        }
    }

    private void setCurrentPos()
    {
        for (currentX = 0; currentX < this.grid[0].length; currentX++)
        {
            this.current = this.grid[currentY][currentX];

            if (targetTank.posX <= this.current.totalX * Game.tile_size)
                break;
        }

        for (currentY = 0; currentY < this.grid.length; currentY++)
        {
            this.current = this.grid[currentY][currentX];

            if (targetTank.posY <= this.current.totalY * Game.tile_size)
                break;
        }

        Level.currentLightIntensity = this.current.level.light;
        Level.currentShadowIntensity = this.current.level.shadow;
    }

    private void handleCloseTiles(int x, int y)
    {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int i = 0; i < 4; i++)
        {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (!Game.lessThan(-1, newX, this.grid[0].length) || !Game.lessThan(-1, newY, this.grid[1].length))
                continue;

            Section tile = this.grid[newY][newX];

            int offX = newX - currentX;
            int offY = newY - currentY;

            int compareX = offX >= 0 ? tile.startX : tile.totalX;
            int compareY = offY >= 0 ? tile.startY : tile.totalY;

            int distX = (int) Math.abs(targetTank.posX - compareX * 50);
            int distY = (int) Math.abs(targetTank.posY - compareY * 50);

            if ((offX == 0 || distX <= loadDistance * 50) && (offY == 0 || distY <= loadDistance * 50) && !tile.loaded)
            {
                tile.load();
                handleCloseTiles(newX, newY);
            }
            if ((offX != 0 && distX >= loadDistance * 100 ||
                    offY != 0 && distY >= loadDistance * 100) && tile.loaded)
            {
                tile.unload();
            }
            else if (tile.loaded && !visited.contains(tile))
            {
                visited.add(tile);
                handleCloseTiles(newX, newY);
            }

            if (!tile.loaded && (distX >= loadDistance * 150 || distY >= loadDistance * 150))
                tile.moveObjects();
        }
    }

    public static class Section
    {
        public Level level;
        public Section[][] grid;
        public boolean loaded = false;

        public ArrayList<Obstacle> obstacles = new ArrayList<>();
        public ArrayList<Movable> movables = new ArrayList<>();
        public boolean saved = false;

        public int sizeX;
        public int sizeY;
        public int startX;
        public int startY;
        public int totalX;
        public int totalY;

        public Section(Level l, int prevX, int prevY)
        {
            this.level = l;

            level.sizeX = Integer.parseInt(level.screen[0]);
            level.sizeY = Integer.parseInt(level.screen[1]);

            sizeX = level.sizeX;
            sizeY = level.sizeY;
            startX = prevX;
            startY = prevY;
            totalX = sizeX + prevX;
            totalY = sizeY + prevY;
        }

        public void load()
        {
            if (loaded)
                return;
            else
                loaded = true;

            if (saved)
            {
                Game.obstacles.addAll(obstacles);
                Game.movables.addAll(movables);
                loadTiles(level);
            }
            else
            {
                Game.currentSizeX = Math.max(Game.currentSizeX, totalX);
                Game.currentSizeY = Math.max(Game.currentSizeY, totalY);
                loadTiles(level);

                ArrayList[] newObjects = loadLevelWithOffset(level, startX, startY);
                obstacles.addAll(newObjects[0]);
                movables.addAll(newObjects[1]);
                saved = true;
            }
        }

        public void unload()
        {
            if (loaded)
                loaded = false;
            else
                return;

            for (Obstacle o : Game.obstacles)
            {
                if (Game.lessThan(startX, o.posX / 50 - 0.5, totalX) && Game.lessThan(startY, o.posY / 50 - 0.5, totalY))
                {
                    obstacles.add(o);
                    Game.obstacleMap[(int) (o.posX / 50)][(int) (o.posY / 50)] = null;
                }
            }

            for (Movable m : Game.movables)
            {
                if (!(!(m instanceof Tank) || m instanceof TankPlayer || m instanceof TankPlayerRemote) &&
                        Game.lessThan(startX, m.posX / 50 - 0.5, totalX) && Game.lessThan(startY, m.posY / 50 - 0.5, totalY))
                    movables.add(m);
            }

            Game.movables.removeAll(movables);
            Game.obstacles.removeAll(obstacles);

            for (int tileX = startX; tileX < totalX; tileX++)
            {
                for (int tileY = startY; tileY < totalY; tileY++)
                    Game.tileDrawables[tileX][tileY] = new ObstacleEmpty(tileX, tileY);
            }
        }

        public void moveObjects()
        {

        }

        public static void loadTiles(Level level)
        {
            double[][] tilesR = Game.tilesR;
            double[][] tilesG = Game.tilesG;
            double[][] tilesB = Game.tilesB;
            double[][] tilesDepth = Game.tilesDepth;
            Obstacle[][] drawables = Game.tileDrawables;
            Obstacle[][] obstacleMap = Game.obstacleMap;

            Game.tilesR = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesG = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesB = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tilesDepth = new double[Game.currentSizeX][Game.currentSizeY];
            Game.tileDrawables = new Obstacle[Game.currentSizeX][Game.currentSizeY];

            for (int i = 0; i < drawables.length; i++)
                System.arraycopy(drawables[i], 0, Game.tileDrawables[i], 0, drawables[0].length);

            for (int i = 0; i < drawables.length; i++)
                System.arraycopy(obstacleMap[i], 0, Game.obstacleMap[i], 0, obstacleMap[0].length);

            for (int i = 0; i < Game.currentSizeX; i++)
            {
                for (int j = 0; j < Game.currentSizeY; j++)
                {
                    if (i < tilesR.length && j < tilesR[0].length)
                    {
                        Game.tilesR[i][j] = tilesR[i][j];
                        Game.tilesG[i][j] = tilesG[i][j];
                        Game.tilesB[i][j] = tilesB[i][j];
                        Game.tilesDepth[i][j] = tilesDepth[i][j];
                    }
                    else
                    {
                        Game.tilesR[i][j] = (level.colorR + Math.random() * level.colorVarR);
                        Game.tilesG[i][j] = (level.colorG + Math.random() * level.colorVarG);
                        Game.tilesB[i][j] = (level.colorB + Math.random() * level.colorVarB);
                        Game.tilesDepth[i][j] = Math.random() * 10;
                    }
                }
            }

            Game.game.heightGrid = new double[Game.currentSizeX][Game.currentSizeY];
            Game.game.groundHeightGrid = new double[Game.currentSizeX][Game.currentSizeY];

            Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.obstacleMap = new Obstacle[Game.currentSizeX][Game.currentSizeY];

            for (Obstacle o : Game.obstacles)
            {
                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                {
                    if (o.bulletCollision)
                        Game.game.solidGrid[x][y] = true;

                    Game.obstacleMap[x][y] = o;
                }
            }

            Drawing.drawing.setScreenBounds(Game.currentSizeX * Game.tile_size, Game.currentSizeY * Game.tile_size);
        }

        public static ArrayList[] loadLevelWithOffset(Level level, int offsetX, int offsetY)
        {
            ArrayList<Obstacle> obstacles = new ArrayList<>();
            ArrayList<Movable> movables = new ArrayList<>();

            Game.eventsOut.add(new EventLoadMapLevel(level, offsetX, offsetY));

            Game.currentLevel = level;
            Game.currentLevelString = level.levelString;

            ScreenGame.finishTimer = ScreenGame.finishTimerMax;

            if (level.enableTeams)
            {
                level.tankTeams = new Team[level.teams.length];

                for (int i = 0; i < level.teams.length; i++)
                {
                    String[] t = level.teams[i].split("-");

                    if (t.length >= 5)
                        level.tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]), Double.parseDouble(t[2]), Double.parseDouble(t[3]), Double.parseDouble(t[4]));
                    else if (t.length >= 2)
                        level.tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]));
                    else
                        level.tankTeams[i] = new Team(t[0]);

                    if (level.disableFriendlyFire)
                        level.tankTeams[i].friendlyFire = false;

                    level.teamsMap.put(t[0], level.tankTeams[i]);

                    level.teamsList.add(level.tankTeams[i]);
                }
            }
            else
            {
                if (level.disableFriendlyFire)
                {
                    level.teamsMap.put("ally", Game.playerTeamNoFF);
                    level.teamsMap.put("enemy", Game.enemyTeamNoFF);
                }
                else
                {
                    level.teamsMap.put("ally", Game.playerTeam);
                    level.teamsMap.put("enemy", Game.enemyTeam);
                }
            }

            Level.currentCloudCount = (int) (Math.random() * (double) level.sizeX / 10.0D + Math.random() * (double) level.sizeY / 10.0D);

            if (level.screen.length >= 5)
            {
                level.colorR = Integer.parseInt(level.screen[2]);
                level.colorG = Integer.parseInt(level.screen[3]);
                level.colorB = Integer.parseInt(level.screen[4]);

                if (level.screen.length >= 8)
                {
                    level.colorVarR = Math.min(255 - level.colorR, Integer.parseInt(level.screen[5]));
                    level.colorVarG = Math.min(255 - level.colorG, Integer.parseInt(level.screen[6]));
                    level.colorVarB = Math.min(255 - level.colorB, Integer.parseInt(level.screen[7]));
                }
            }

            if (level.screen.length >= 9)
            {
                int length = Integer.parseInt(level.screen[8]) * 100;

                if (length > 0)
                {
                    level.timed = true;
                    level.timer = length;
                }
            }

            if (level.screen.length >= 11)
            {
                level.light = Integer.parseInt(level.screen[9]) / 100.0;
                level.shadow = Integer.parseInt(level.screen[10]) / 100.0;
            }

            for (Item i : level.shop)
                i.importProperties();

            for (Item i : level.startingItems)
                i.importProperties();

            if (!((level.obstaclesPos.length == 1 && level.obstaclesPos[0].equals("")) || level.obstaclesPos.length == 0))
            {
                for (String pos : level.obstaclesPos)
                {
                    String[] obs = pos.split("-");

                    String[] xPos = obs[0].split("\\.\\.\\.");

                    double startX;
                    double endX;

                    startX = Double.parseDouble(xPos[0]);
                    endX = startX;

                    if (xPos.length > 1)
                        endX = Double.parseDouble(xPos[1]);

                    String[] yPos = obs[1].split("\\.\\.\\.");

                    double startY;
                    double endY;

                    startY = Double.parseDouble(yPos[0]);
                    endY = startY;

                    if (yPos.length > 1)
                        endY = Double.parseDouble(yPos[1]);

                    String name = "normal";

                    if (obs.length >= 3)
                        name = obs[2];

                    String meta = null;

                    if (obs.length >= 4)
                        meta = obs[3];

                    startX += offsetX;
                    endX += offsetX;
                    startY += offsetY;
                    endY += offsetY;

                    for (double x = startX; x <= endX; x++)
                    {
                        for (double y = startY; y <= endY; y++)
                        {
                            Obstacle o = Game.registryObstacle.getEntry(name).getObstacle(x, y);

                            if (meta != null)
                                o.setMetadata(meta);

                            Game.obstacles.add(o);
                            obstacles.add(o);
                        }
                    }
                }
            }

            Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
            Game.obstacleMap = new Obstacle[Game.currentSizeX][Game.currentSizeY];

            for (Obstacle o: Game.obstacles)
            {
                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
                {
                    if (o.bulletCollision)
                        Game.game.solidGrid[x][y] = true;

                    if (o.bulletCollision && !o.shouldShootThrough)
                        Game.game.unbreakableGrid[x][y] = true;

                    Game.obstacleMap[x][y] = o;
                }
            }

            if (!level.preset[2].equals(""))
            {
                for (String s : level.tanks)
                {
                    String[] tank = s.split("-");
                    double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]) + offsetX);
                    double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]) + offsetY);
                    String type = tank[2].toLowerCase();
                    double angle = 0;

                    if (type.equals("player"))
                        continue;

                    if (tank.length >= 4)
                        angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

                    Team team = Game.enemyTeam;

                    if (level.disableFriendlyFire)
                        team = Game.enemyTeamNoFF;

                    if (level.enableTeams)
                    {
                        if (tank.length >= 5)
                            team = level.teamsMap.get(tank[4]);
                        else
                            team = null;
                    }

                    Tank t = Game.registryTank.getEntry(type).getTank(x, y, angle);
                    t.team = team;
                    Game.movables.add(t);
                    movables.add(t);
                }
            }
            redrawCounter = 10;

            return new ArrayList[] {obstacles, movables};
        }
    }

    public static class ObstacleEmpty extends Obstacle
    {
        public ObstacleEmpty(double posX, double posY)
        {
            super("empty", posX, posY);
            this.allowBounce = false;
        }

        @Override
        public void draw()
        {
        }

        @Override
        public void drawTile(double r, double g, double b, double depth, double extra)
        {
        }

        @Override
        public boolean wasRedrawn()
        {
            return false;
        }
    }
}

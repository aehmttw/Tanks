package tanks;

import basewindow.Color;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.gui.screen.leveleditor.selector.SelectorTeam;
import tanks.item.Item;
import tanks.network.ServerHandler;
import tanks.network.event.*;
import tanks.obstacle.*;
import tanks.registry.RegistryTank;
import tanks.tank.*;
import tanks.tankson.*;

import java.util.*;

@TanksONable("level")
public class Level
{
    public String levelString;

    /**
     * Tanks IR representation is a way to store tank instances in a dense format.
     * Parentheses denote required values and brackets denote the start and end of a list.
     * Asterisks indicate that the parameter can be repeated, separated by commas.<br>
     * [[(X),(Y),(Name),(Angle),(Team)]*]
     *
     * */
    @Property(id = "tank_pos", name = "Tank positions")
    public ArrayList<ArrayList<String>> tanksIR;

    public ArrayList<Tank> tanks;
    ArrayList<Tank> tanksToRemove;
    public Team[] tankTeams;
    public ArrayList<Obstacle> obstacles;

    @Property(id = "obstacles", name = "Obstacles")
    public ArrayList<ArrayList<String>> obstaclesIR;

    public static Color currentColor = new Color(235, 207, 166);
    public static Color currentColorVar = new Color(235, 207, 166);
    public static double currentLightIntensity = 1;
    public static double currentShadowIntensity = 0.5;
    public static Color currentLightColor = new Color(255, 255, 255);

    public static int currentCloudCount = 0;

    public static Random random = new Random();

    @Property(id = "editable", name = "Editable")
    public boolean editable = true;
    public boolean remote = false;
    public boolean preview = false;

    @Property(id = "timer", name = "Timer")
    public double timer = -1;

    public int startX;
    public int startY;
    @Property(id = "size_x", name = "Size X")
    public int sizeX;
    @Property(id = "size_y", name = "Size Y")
    public int sizeY;

    @Property(id = "color", name = "Color")
    public Color color = new Color(235, 207, 166);
    @Property(id = "color_var", name = "Color variation")
    public Color colorVar = new Color(20, 20, 20);

    public int tilesRandomSeed = (int) (Math.random() * Integer.MAX_VALUE);

    @Property(id = "light", name = "Light")
    public double light = 1.0;
    @Property(id = "shadow", name = "Shadow")
    public double shadow = 0.5;
    @Property(id = "light_color", name = "Light color")
    public Color lightColor = new Color(255, 255, 255);

    @Property(id = "teams", name = "Teams")
    public NamedList<Team> teamsMap = new NamedList<>();

    public ArrayList<Integer> availablePlayerSpawns = new ArrayList<>();

    public ArrayList<Double> playerSpawnsX = new ArrayList<>();
    public ArrayList<Double> playerSpawnsY = new ArrayList<>();
    public ArrayList<Double> playerSpawnsAngle = new ArrayList<>();
    public ArrayList<Team> playerSpawnsTeam = new ArrayList<>();

    public ArrayList<Player> includedPlayers = new ArrayList<>();

    @Property(id = "coins", name = "Coins")
    public int startingCoins;
    @Property(id = "Shop", name = "Shop")
    public ArrayList<Item.ShopItem> shop = new ArrayList<>();
    @Property(id = "items", name = "Starting items")
    public ArrayList<Item.ItemStack<?>> startingItems = new ArrayList<>();
    @Property(id = "builds", name = "Player builds")
    public ArrayList<TankPlayer.ShopTankBuild> playerBuilds = new ArrayList<>();

    // Saved on the client to keep track of what each item is
    public int clientStartingCoins;
    public ArrayList<Item.ShopItem> clientShop = new ArrayList<>();
    public ArrayList<Item.ItemStack<?>> clientStartingItems = new ArrayList<>();

    @Property(id = "custom_tanks", name = "Custom tanks")
    public ArrayList<TankAIControlled> customTanks;

    public LinkedHashMap<String, Integer> itemNumbers = new LinkedHashMap<>();

    public double startTime = 400;
    public boolean disableFriendlyFire = false;

    public boolean synchronizeMusic = false;
    public int beatBlocks = 0;

    public HashMap<String, Tank> tankLookupTable = null;

    public Level()
    {
        obstacles = new ArrayList<Obstacle>();
        this.tanks = new ArrayList<Tank>();
        this.customTanks = new ArrayList<>();

        this.remote = false;
        this.disableFriendlyFire = false;
    }

    /**
     * Parses a level string in either the TanksON or the legacy format, and initializes it.
     * Use this for any level string that comes from a file, a crusade, the network, or a player -
     * the {@link Level#Level(String)} constructors only understand the legacy format.
     */
    public static Level fromString(String level)
    {
        return fromString(level, new ArrayList<>(), false, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public static Level fromString(String level, boolean remote)
    {
        return fromString(level, new ArrayList<>(), remote, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public static Level fromString(String level, ArrayList<TankAIControlled> customTanks)
    {
        return fromString(level, customTanks, false, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public static Level fromString(String level, ArrayList<TankAIControlled> customTanks, boolean remote, boolean disableFriendlyFire)
    {
        if (!isTanksON(level))
            return new Level(level, customTanks, remote, disableFriendlyFire);

        Level l = parseTanksON(level);
        l.init(customTanks, remote, disableFriendlyFire);
        return l;
    }

    /**
     * Parses a level string in either format, for inspecting or rewriting a level rather than
     * playing it. Unlike {@link #fromString}, a TanksON level is left uninitialized, so its
     * obstacles and tanks are not instantiated. (A legacy level is still parsed in full.)
     */
    public static Level parse(String level)
    {
        if (!isTanksON(level))
            return new Level(level);

        return parseTanksON(level);
    }

    protected static Level parseTanksON(String level)
    {
        Level l = (Level) Serializer.fromTanksON(level);
        l.levelString = level;
        return l;
    }

    /** Whether a level string is in the TanksON format, as opposed to the legacy format. */
    public static boolean isTanksON(String level)
    {
        if (level == null)
            return false;

        int i = 0;
        while (i < level.length())
        {
            char c = level.charAt(i);

            if (Character.isWhitespace(c))
                i++;
            else if (c == '/' && i + 1 < level.length() && level.charAt(i + 1) == '*')
            {
                // Skip the TanksON shebang, or any other leading comment
                int end = level.indexOf("*/", i + 2);
                if (end < 0)
                    return false;

                i = end + 2;
            }
            else
                break;
        }

        if (i >= level.length() || level.charAt(i) != '{')
            return false;

        // A pretty-printed level breaks the line after the brace
        i++;
        while (i < level.length() && Character.isWhitespace(level.charAt(i)))
            i++;

        // A TanksON level opens with a key, while a legacy level opens with its dimensions
        return i < level.length() && level.charAt(i) == '"';
    }

    /**
     * Levels which don't name their own teams share the global default team objects.
     * The level editor can edit teams, so give the level its own copies of them first.
     */
    public void populateDefaultTeams()
    {
        for (Map.Entry<String, Team> e: this.teamsMap.entrySet())
        {
            Team t = e.getValue();

            if (t != Game.playerTeam && t != Game.enemyTeam && t != Game.playerTeamNoFF && t != Game.enemyTeamNoFF)
                continue;

            Team copy = new Team(t.name, t.friendlyFire);
            e.setValue(copy);

            for (Movable m: Game.movables)
            {
                if (m.team == t)
                    m.team = copy;
            }
        }
    }

    public void commonInit()
    {
        if (ScreenPartyHost.isServer)
            this.startTime = Game.partyStartTime;

        if (TankModels.tank != null && this.playerBuilds.isEmpty())
            this.playerBuilds.add(new TankPlayer.ShopTankBuild());

        // Item numbers are how items are referred to over the network
        this.itemNumbers.clear();

        for (int i = 0; i < this.shop.size(); i++)
        {
            this.itemNumbers.put(this.shop.get(i).itemStack.item.name, i + 1);
        }

        for (int i = 0; i < this.startingItems.size(); i++)
        {
            this.itemNumbers.put(this.startingItems.get(i).item.name, this.shop.size() + i + 1);
        }

        if (ScreenPartyLobby.isClient)
        {
            this.clientStartingCoins = this.startingCoins;
            this.clientStartingItems = this.startingItems;
            this.clientShop = this.shop;

            this.startingCoins = 0;
            this.startingItems = new ArrayList<>();
        }
    }

    public void init()
    {
        init(false);
    }

    public void init(boolean remote)
    {
        init(new ArrayList<>(), remote, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public void init(ArrayList<TankAIControlled> customTanks)
    {
        init(customTanks, false, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public void init(ArrayList<TankAIControlled> customTanks, boolean remote, boolean disableFriendlyFire)
    {
        this.remote = remote;
        this.disableFriendlyFire = disableFriendlyFire;
        this.customTanks.addAll(customTanks);

        if (teamsMap.isEmpty())
        {
            if (disableFriendlyFire)
            {
                teamsMap.put("ally", Game.playerTeamNoFF);
                teamsMap.put("enemy", Game.enemyTeamNoFF);
            }
            else
            {
                teamsMap.put("ally", Game.playerTeam);
                teamsMap.put("enemy", Game.enemyTeam);
            }
        }
        else if (disableFriendlyFire)
        {
            // These teams came out of the level itself, so they're ours to turn friendly fire off on
            for (Team t: teamsMap.values())
                t.friendlyFire = false;
        }

        for (ArrayList<String> obs: obstaclesIR)
        {
            String[] xs = obs.get(0).split(":");
            double startX = Double.parseDouble(xs[0]);
            double endX = (xs.length > 1 ? Double.parseDouble(xs[1]) : startX) + 1;
            String[] ys = obs.get(1).split(":");
            double startY = Double.parseDouble(ys[0]);
            double endY = (ys.length > 1 ? Double.parseDouble(ys[1]) : startY) + 1;

            // A legacy level leaves the name out for a plain block, and save() writes it
            // as empty, so both mean "normal" here
            String name = obs.size() >= 3 && !obs.get(2).isEmpty() ? obs.get(2) : "normal";

            for (double x = startX; x < endX; x++)
            {
                for (double y = startY; y < endY; y++)
                {
                    Obstacle o = Game.registryObstacle.getEntry(name).getObstacle(x, y);

                    if (obs.size() >= 4)
                        o.setMetadata(obs.get(3));

                    if (o instanceof ObstacleBeatBlock)
                    {
                        this.synchronizeMusic = true;
                        this.beatBlocks |= (int) ((ObstacleBeatBlock) o).beatFrequency;
                    }

                    obstacles.add(o);
                }
            }
        }

        int currentCrusadeID = 0;

        LinkedHashMap<String, TankAIControlled> customTanksMap = new LinkedHashMap<>();
        for (TankAIControlled t: this.customTanks)
            customTanksMap.put(t.name, t);

        tanksToRemove = new ArrayList<>();

        for (ArrayList<String> tankIR: tanksIR)
        {
            String[] tank = new String[tankIR.size()];
            tankIR.toArray(tank);
            double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]));
            double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]));
            String type = tank[2].toLowerCase();
            double angle = 0;

            StringBuilder metadata = new StringBuilder();
            for (int i = 3; i < tank.length; i++)
            {
                metadata.append(tank[i]);
                if (i < tank.length - 1)
                    metadata.append("-");
            }

            if (tank.length >= 4)
                angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

            Team team = null;

            if (tank.length >= 5)
                team = teamsMap.get(tank[4]);

            Tank t;
            if (type.equals("player"))
            {
                this.playerSpawnsX.add(x);
                this.playerSpawnsY.add(y);
                this.playerSpawnsAngle.add(angle);
                this.playerSpawnsTeam.add(team);

                continue;
            }

            if (customTanksMap.get(type) != null)
                t = customTanksMap.get(type).instantiate(type, x, y, angle);
            else
                t = Game.registryTank.getEntry(type).getTank(x, y, angle);

            t.crusadeID = currentCrusadeID;
            currentCrusadeID++;

            Level l = Game.currentLevel;
            Game.currentLevel = this;
            if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks && Crusade.currentCrusade.retry && !Crusade.currentCrusade.livingTankIDs.contains(t.crusadeID))
                tanksToRemove.add(t);
            else
                t.setMetadata(metadata.toString());
            Game.currentLevel = l;

            if (remote)
                this.tanks.add(new TankRemote(t));
            else
            {
                this.tanks.add(t);
                setSolidTank((int) Double.parseDouble(tank[0]), (int) Double.parseDouble(tank[1]), true);
            }
        }

        this.commonInit();
    }

    protected static ArrayList<String> getJsonObjects(String s)
    {
        int depth = 0;
        int last = 0;
        ArrayList<String> out = new ArrayList<>();
        for (int i = 0; i < s.length(); i++)
        {
            if (s.charAt(i) == '/' && s.charAt(i + 1) == '*')
                last = i;

            if (s.charAt(i) == '{' || s.charAt(i) == '[')
            {
                if (depth == 0)
                    if (i - 2 < 0 || s.charAt(i - 1) != '/' || s.charAt(i - 2) != '*')
                        last = i;

                depth++;
            }
            else if (s.charAt(i) == '}' || s.charAt(i) == ']')
            {
                depth--;
                if (depth == 0 && !s.substring(last, i + 1).trim().isEmpty())
                {
                    out.add(s.substring(last, i + 1));
                    last = i + 1;
                }
            }

        }
        return out;
    }

    public void loadLevel()
    {
        loadLevel(null);
    }

    public void loadLevel(ILevelPreviewScreen sc)
    {
        Game.playerTank = null;
        Game.currentLevel = this;
        Game.currentLevelString = this.levelString;
        Chunk.populateChunks(this, true);

        if (Game.deterministicMode)
            random = new Random(Game.seed);
        else
            random = new Random(tilesRandomSeed);

        if (ScreenPartyHost.isServer)
            ScreenPartyHost.includedPlayers.clear();
        else if (ScreenPartyLobby.isClient)
            ScreenPartyLobby.includedPlayers.clear();

        if (sc == null)
            Obstacle.draw_size = 0;
        else
            Obstacle.draw_size = 50;

        Chunk.Tile ft = Chunk.Tile.fallbackTile;
        ft.colR = color.red;
        ft.colG = color.green;
        ft.colB = color.blue;

        if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
            Game.eventsOut.add(new EventLoadLevel(this));

        Tank.currentID = 0;
        Tank.freeIDs.clear();

        ScreenGame.finishedQuick = false;

        ScreenGame.finished = false;
        ScreenGame.finishTimer = ScreenGame.finishTimerMax;

        currentCloudCount = (int) (Math.random() * (double) this.sizeX / 10.0D + Math.random() * (double) this.sizeY / 10.0D);

        if (sc instanceof ScreenLevelEditor)
        {
            ScreenLevelEditor s = (ScreenLevelEditor) sc;

            s.level = this;

            s.selectedTiles = new boolean[sizeX][sizeY];
            Game.movables.remove(Game.playerTank);
        }

        for (Obstacle o: obstacles)
            Game.addObstacle(o, false);

        for (Tank t: tanks)
        {
            if (sc != null)
                t.drawAge = 50;
            Game.movables.add(t);
        }

        this.availablePlayerSpawns.clear();

        int playerCount = 1;
        if (ScreenPartyHost.isServer && ScreenPartyHost.server != null && sc == null)
            playerCount = Game.players.size();

        if (!this.includedPlayers.isEmpty())
            playerCount = this.includedPlayers.size();
        else
            this.includedPlayers.addAll(Game.players);

        int extraSpawns = 0;
        if (playerCount > playerSpawnsX.size() && !playerSpawnsX.isEmpty())
        {
            extraSpawns = playerCount / playerSpawnsX.size() - 1;

            if (playerCount % playerSpawnsX.size() != 0)
                extraSpawns++;
        }

        int spawns = playerSpawnsX.size();

        for (int i = 0; i < spawns; i++)
        {
            int spawnsLeft = extraSpawns;
            ArrayList<Integer> extraSpawnsX = new ArrayList<>();
            ArrayList<Integer> extraSpawnsY = new ArrayList<>();

            boolean[][] explored = new boolean[this.sizeX][this.sizeY];
            boolean[][] blacklist = new boolean[this.sizeX][this.sizeY];

            ArrayList<Tile> queue = new ArrayList<>();
            queue.add(new Tile((int) (playerSpawnsX.get(i) / Game.tile_size), (int) (playerSpawnsY.get(i) / Game.tile_size)));

            while (!queue.isEmpty() && spawnsLeft > 0)
            {
                boolean stop = false;

                Tile t = queue.remove(0);

                for (int j: t.sidesOrder)
                {
                    Tile t1;

                    if (j == 0)
                        t1 = new Tile(t.posX - 1, t.posY);
                    else if (j == 1)
                        t1 = new Tile(t.posX + 1, t.posY);
                    else if (j == 2)
                        t1 = new Tile(t.posX, t.posY - 1);
                    else
                        t1 = new Tile(t.posX, t.posY + 1);

                    if (t1.posX >= 0 && t1.posX < this.sizeX && t1.posY >= 0 && t1.posY < this.sizeY &&
                        !Game.isTankSolid(t1.posX, t1.posY) && !isSolidTank(t1.posX, t1.posY) && !explored[t1.posX][t1.posY])
                    {
                        explored[t1.posX][t1.posY] = true;

                        t1.age = t.age + 1;

                        extraSpawnsX.add(t1.posX);
                        extraSpawnsY.add(t1.posY);

                        if (!blacklist[t1.posX][t1.posY] && (t1.age == 3 && Math.random() < 0.333 || t1.age == 4 && Math.random() < 0.5 || t1.age >= 5))
                        {
                            spawnsLeft--;
                            t1.age = 0;

                            playerSpawnsX.add((t1.posX + 0.5) * Game.tile_size);
                            playerSpawnsY.add((t1.posY + 0.5) * Game.tile_size);
                            playerSpawnsTeam.add(playerSpawnsTeam.get(i));
                            playerSpawnsAngle.add(playerSpawnsAngle.get(i));

                            setSolidTank(t1.posX, t1.posY, true);

                            for (int x = Math.max(t1.posX - 1, 0); x <= Math.min(t1.posX + 1, this.sizeX - 1); x++)
                            {
                                for (int y = Math.max(t1.posY - 1, 0); y <= Math.min(t1.posY + 1, this.sizeY - 1); y++)
                                {
                                    blacklist[x][y] = true;
                                }
                            }

                            if (spawnsLeft <= 0)
                            {
                                stop = true;
                                break;
                            }
                        }

                        queue.add(t1);
                    }
                }

                if (stop)
                    break;
            }

            while (spawnsLeft > 0)
            {
                if (extraSpawnsX.isEmpty())
                    break;

                int in = (int) (Math.random() * extraSpawnsX.size());
                int x = extraSpawnsX.remove(in);
                int y = extraSpawnsY.remove(in);

                if (!isSolidTank(x, y))
                {
                    playerSpawnsX.add((x + 0.5) * Game.tile_size);
                    playerSpawnsY.add((y + 0.5) * Game.tile_size);
                    playerSpawnsTeam.add(playerSpawnsTeam.get(i));
                    playerSpawnsAngle.add(playerSpawnsAngle.get(i));
                    spawnsLeft--;
                }
            }
        }

        for (Movable m: Game.movables)
        {
            if (m instanceof Tank)
            {
                Tank t = (Tank) m;
                // Don't do this in your code! We only want to dynamically generate tank IDs on level load!
                t.networkID = Tank.nextFreeNetworkID();
                Tank.idMap.put(t.networkID, t);
            }
        }

        playerCount = Math.min(playerCount, this.includedPlayers.size());

        if (sc == null && !preview)
        {
            for (int i = 0; i < playerCount; i++)
            {
                if (this.availablePlayerSpawns.isEmpty())
                {
                    for (int j = 0; j < this.playerSpawnsTeam.size(); j++)
                    {
                        this.availablePlayerSpawns.add(j);
                    }
                }

                int spawn = this.availablePlayerSpawns.remove((int) (Math.random() * this.availablePlayerSpawns.size()));

                double x = this.playerSpawnsX.get(spawn);
                double y = this.playerSpawnsY.get(spawn);
                double angle = this.playerSpawnsAngle.get(spawn);
                Team team = this.playerSpawnsTeam.get(spawn);

                if (ScreenPartyHost.isServer)
                    Game.addPlayerTank(this.includedPlayers.get(i), x, y, angle, team);
                else if (!remote)
                {
                    TankPlayer tank = new TankPlayer(x, y, angle);

                    TankPlayer.ShopTankBuild build = this.playerBuilds.get(0);
                    if (Crusade.crusadeMode)
                    {
                        ArrayList<TankPlayer.ShopTankBuild> builds = Crusade.currentCrusade.getBuildsShop();
                        for (TankPlayer.ShopTankBuild shopTankBuild: builds)
                        {
                            if (shopTankBuild.name.equals(Game.player.buildName))
                                build = shopTankBuild;
                        }
                    }
                    build.clonePropertiesTo(tank);
                    Game.playerTank = tank;
                    Game.player.buildName = tank.buildName;
                    Game.playerTank.health = tank.baseHealth;
                    tank.team = team;
                    tank.registerNetworkID();
                    Game.movables.add(tank);
                }
            }
        }
        else
        {
            for (int i = 0; i < playerSpawnsTeam.size(); i++)
            {
                TankSpawnMarker t = new TankSpawnMarker("player", this.playerSpawnsX.get(i), this.playerSpawnsY.get(i), this.playerSpawnsAngle.get(i));
                t.team = this.playerSpawnsTeam.get(i);
                t.drawAge = 50;
                Game.movables.add(t);

                if (sc != null)
                    sc.getSpawns().add(t);
            }

            if (sc instanceof ScreenLevelEditor)
                ((ScreenLevelEditor) sc).movePlayer = (sc.getSpawns().size() <= 1);
        }

        if (Crusade.crusadeMode && Crusade.currentCrusade.retry)
        {
            for (Tank t: tanksToRemove)
            {
                INetworkEvent e = new EventTankRemove(t, false);
                Game.removeMovables.add(t);
                Game.eventsOut.add(e);
            }
        }

        if (sc instanceof ScreenLevelEditor)
        {
            ScreenLevelEditor s = (ScreenLevelEditor) sc;
            this.populateDefaultTeams();

            s.teams = new ArrayList<>(teamsMap.values());
            if (s.teams.size() > 0)
            {
                s.currentMetadata.put(SelectorTeam.player_selector_name, s.teams.get(0));
                s.currentMetadata.put(SelectorTeam.selector_name, s.teams.get(Math.min(s.teams.size() - 1, 1)));
            }
        }

        this.reloadTiles();

        if (!Crusade.crusadeMode && ScreenPartyHost.isServer)
            broadcastBuilds(this.playerBuilds);

        if (Game.playerTank != null && ScreenPartyHost.isServer)
            Game.playerTank.updateAbilities();

        if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
            Game.eventsOut.add(new EventEnterLevel());
    }

    public static void broadcastBuilds(ArrayList<TankPlayer.ShopTankBuild> builds)
    {
        for (Player p: Game.players)
        {
            p.ownedBuilds.add(builds.get(0).name);

            boolean found = false;
            for (TankPlayer.ShopTankBuild b: builds)
            {
                if (b.name.equals(p.buildName))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
                p.buildName = builds.get(0).name;
        }

        for (ServerHandler h: ScreenPartyHost.server.connections)
        {
            if (h.player != null)
            {
                for (String s: h.player.ownedBuilds)
                {
                    h.queueEvent(new EventPurchaseBuild(s));
                }

                for (int n = 0; n < builds.size(); n++)
                {
                    TankPlayer.ShopTankBuild s = builds.get(n);
                    if (s.name.equals(h.player.buildName) || !Crusade.crusadeMode)
                    {
                        h.queueEvent(new EventPlayerSetBuild(n));

                        for (ServerHandler h1: ScreenPartyHost.server.connections)
                        {
                            if (h1.player != null && Team.isAllied(h1.player.tank, h.player.tank))
                                h1.queueEvent(new EventPlayerRevealBuild(h.player.tank.networkID, n));
                        }

                        if (Team.isAllied(Game.playerTank, h.player.tank) && h.player.tank instanceof TankPlayable)
                        {
                            s.clonePropertiesTo((TankPlayable) h.player.tank);
                            h.player.tank.health = s.baseHealth;
                        }

                        break;
                    }
                }
            }
        }

        if (Game.playerTank != null)
        {
            for (int i = 0; i < builds.size(); i++)
            {
                TankPlayer.ShopTankBuild s = builds.get(i);
                if (s.name.equals(Game.player.buildName) || !Crusade.crusadeMode)
                {
                    s.clonePropertiesTo(Game.playerTank);
                    Game.playerTank.health = s.baseHealth;
                    Game.player.buildName = s.name;

                    for (ServerHandler h1: ScreenPartyHost.server.connections)
                    {
                        if (h1.player != null && Team.isAllied(h1.player.tank, Game.playerTank))
                            h1.queueEvent(new EventPlayerRevealBuild(Game.playerTank.networkID, i));
                    }
                    break;
                }
            }
        }
    }

    public boolean isSolidTank(int x, int y)
    {
        return Chunk.getIfPresent(x, y, false, tile -> tile.solidTank);
    }

    public void setSolidTank(int x, int y, boolean solid)
    {
        Chunk.runIfTilePresent(x, y, tile -> tile.solidTank = solid);
    }

    public void reloadTiles()
    {
        Game.currentSizeX = (int) (sizeX * Game.bgResMultiplier);
        Game.currentSizeY = (int) (sizeY * Game.bgResMultiplier);

        currentColor = color;

        currentColorVar = colorVar;

        currentLightIntensity = light;
        currentShadowIntensity = shadow;
        currentLightColor = lightColor;

        Drawing.drawing.setScreenBounds(Game.tile_size * sizeX, Game.tile_size * sizeY);
        Chunk.populateChunks(this);
        addLevelBorders();

        for (Obstacle o: Game.obstacles)
            o.postOverride();
        for (Movable m: Game.movables)
            m.refreshFacesAndChunks();
        for (Obstacle o: Game.obstacles)
            o.refreshFacesAndChunks();

        ScreenLevelEditor s = null;

        if (Game.screen instanceof ScreenLevelEditor)
            s = (ScreenLevelEditor) Game.screen;
        else if (Game.screen instanceof ScreenLevelEditorOverlay)
            s = ((ScreenLevelEditorOverlay) Game.screen).editor;

        if (s != null)
            s.selectedTiles = new boolean[Game.currentSizeX][Game.currentSizeY];
    }

    public void addLevelBorders()
    {
        // Do not use forEach. This breaks the iOS compiler.
        for (Chunk c: Chunk.getChunksInRange(0, 0, sizeX, 0))
            c.addBorderFace(Direction.up, this);
        for (Chunk c: Chunk.getChunksInRange(sizeX, 0, sizeX, sizeY))
            c.addBorderFace(Direction.right, this);
        for (Chunk c: Chunk.getChunksInRange(0, sizeY, sizeX, sizeY))
            c.addBorderFace(Direction.down, this);
        for (Chunk c: Chunk.getChunksInRange(0, 0, 0, sizeY))
            c.addBorderFace(Direction.left, this);
    }

    public static class Tile
    {
        public int posX;
        public int posY;
        public int age = 0;
        public int[] sidesOrder = new int[4];

        public Tile(int x, int y)
        {
            this.posX = x;
            this.posY = y;

            ArrayList<Integer> sides = new ArrayList<>();
            sides.add(0);
            sides.add(1);
            sides.add(2);
            sides.add(3);

            int i = 0;

            while (!sides.isEmpty())
            {
                int s = sides.remove((int) (Math.random() * sides.size()));
                sidesOrder[i] = s;
                i++;
            }
        }
    }

    public static boolean isDark()
    {
        return Level.currentColor.getPerceivedBrightness() <= 127 ||
                Level.currentLightIntensity * Level.currentLightColor.getPerceivedBrightness() <= 127;
    }

    public Tank lookupTank(String name)
    {
        if (Game.screen instanceof ScreenGame)
        {
            if (this.tankLookupTable == null)
            {
                this.tankLookupTable = new HashMap<>();

                for (RegistryTank.TankEntry e: Game.registryTank.tankEntries)
                {
                    this.tankLookupTable.put(e.name, e.getTank(0, 0, 0));
                }

                for (TankAIControlled t: this.customTanks)
                {
                    this.tankLookupTable.put(t.name, t);
                }
            }

            return this.tankLookupTable.get(name);
        }
        else
        {
            RegistryTank.TankEntry e = Game.registryTank.getEntry(name);
            if (TankUnknown.class.isAssignableFrom(e.tank))
            {
                for (TankAIControlled t: this.customTanks)
                {
                    if (t.name.equals(name))
                        return t;
                }

                return null;
            }
            else
                return e.getTank(0, 0, 0);
        }
    }

    public boolean isLarge()
    {
        return !(this.sizeX * this.sizeY <= 100000 && this.tanks.size() < 500);
    }

    public String stripFormatting()
    {
        String jsoncString = this.levelString;
        if (jsoncString == null || jsoncString.isEmpty())
        {
            return jsoncString;
        }

        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;

        for (int i = 0; i < jsoncString.length(); i++)
        {
            char c = jsoncString.charAt(i);

            if (inString)
            {
                sb.append(c);
                if (c == '"')
                {
                    // A quote is unescaped if it's not preceded by an odd number of backslashes.
                    int backslashes = 0;
                    for (int j = i - 1; j >= 0; j--)
                    {
                        if (jsoncString.charAt(j) == '\\')
                            backslashes++;
                        else
                            break;
                    }

                    if (backslashes % 2 == 0)
                        inString = false;
                }
            }
            else if (inSingleLineComment)
            {
                if (c == '\n')
                    inSingleLineComment = false;
            }
            else if (inMultiLineComment)
            {
                // Check for end of multi-line comment: */
                if (c == '*' && i + 1 < jsoncString.length() && jsoncString.charAt(i + 1) == '/')
                {
                    inMultiLineComment = false;
                    i++; // Also skip the '/'
                }
            }
            else
            {
                // Not in any special state, check for transitions or valid characters
                if (c == '"')
                {
                    inString = true;
                    sb.append(c);
                }
                else if (c == '/' && i + 1 < jsoncString.length())
                {
                    char next = jsoncString.charAt(i + 1);
                    if (next == '/')
                    {
                        inSingleLineComment = true;
                        i++; // Also skip the second '/'
                    }
                    else if (next == '*')
                    {
                        inMultiLineComment = true;
                        i++; // Also skip the '*'
                    }
                    else
                    {
                        // It's a valid character (e.g. in a URL), not a comment start.
                        sb.append(c);
                    }
                }
                else if (!Character.isWhitespace(c))
                {
                    sb.append(c);
                }
                // else, it's insignificant whitespace, so we ignore it
            }
        }

        String ir = sb.toString();
        ArrayList<String> objects = getJsonObjects(ir);
        String out = "";
        for (String o: objects)
        {
            out += "\n" + o;
        }

        return out.substring(1);
    }

    public String save()
    {
        this.obstaclesIR.clear();
        this.tanksIR.clear();
        ArrayList<Obstacle> unmarked = (ArrayList<Obstacle>) Game.obstacles.clone();
        String[][][] obstacles = new String[Game.registryObstacle.obstacleEntries.size()][this.sizeX][this.sizeY];

        for (int h = 0; h < Game.registryObstacle.obstacleEntries.size(); h++)
        {
            for (int i = 0; i < Game.obstacles.size(); i++)
            {
                Obstacle o = Game.obstacles.get(i);
                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                if (x < obstacles[h].length && x >= 0 && y < obstacles[h][0].length && y >= 0 && o.name.equals(Game.registryObstacle.getEntry(h).name))
                {
                    obstacles[h][x][y] = o.getMetadata();

                    unmarked.remove(o);
                }
            }

            //compression
            for (int i = 0; i < this.sizeX; i++)
            {
                for (int j = 0; j < this.sizeY; j++)
                {
                    if (obstacles[h][i][j] != null)
                    {
                        String stack = obstacles[h][i][j];

                        int xLength = 0;

                        while (true)
                        {
                            xLength += 1;

                            if (i + xLength >= obstacles[h].length)
                                break;
                            else if (!Objects.equals(obstacles[h][i + xLength][j], stack))
                                break;
                        }


                        int yLength = 0;

                        while (true)
                        {
                            yLength += 1;

                            if (j + yLength >= obstacles[h][0].length)
                                break;
                            else if (!Objects.equals(obstacles[h][i][j + yLength], stack))
                                break;
                        }

                        String name = "";
                        String obsName = Game.registryObstacle.obstacleEntries.get(h).name;

                        if (!obsName.equals("normal") || !stack.equals("1.0"))
                            name = obsName;

                        if (xLength >= yLength)
                        {
                            ArrayList<String> obs = new ArrayList<>();
                            if (xLength == 1)
                            {
                                obs.add(Integer.toString(i));
                                obs.add(Integer.toString(j));
                                obs.add(name);
                            }
                            else
                            {
                                obs.add(Integer.toString(i) + ":" + Integer.toString(i + xLength - 1));
                                obs.add(Integer.toString(j));
                                obs.add(name);
                            }

                            if (!stack.isEmpty())
                                obs.add(stack);

                            this.obstaclesIR.add(obs);

                            for (int z = 0; z < xLength; z++)
                            {
                                obstacles[h][i + z][j] = null;
                            }
                        }
                        else
                        {
                            ArrayList<String> obs = new ArrayList<>();
                            obs.add(Integer.toString(i));
                            obs.add(Integer.toString(j) + ":" + Integer.toString(j + yLength - 1));
                            obs.add(name);

                            if (!stack.isEmpty())
                                obs.add(stack);

                            this.obstaclesIR.add(obs);

                            for (int z = 0; z < yLength; z++)
                            {
                                obstacles[h][i][j + z] = null;
                            }
                        }
                    }
                }
            }
        }

        for (Obstacle obstacle: unmarked)
        {
            ArrayList<String> obs = new ArrayList<>();
            obs.add(Integer.toString((int) (obstacle.posX / Game.tile_size)));
            obs.add(Integer.toString((int) (obstacle.posY / Game.tile_size)));
            obs.add(obstacle.name);

            if (obstacle instanceof ObstacleUnknown && ((ObstacleUnknown) obstacle).metadata != null)
                obs.add(((ObstacleUnknown) obstacle).metadata);
            else
            {
                String meta = obstacle.getMetadata();
                if (!meta.isEmpty())
                    obs.add(meta);
            }


            this.obstaclesIR.add(obs);
        }

        for (int i = 0; i < Game.movables.size(); i++)
        {
            if (Game.movables.get(i) instanceof Tank)
            {
                Tank t = (Tank) Game.movables.get(i);
                int x = (int) (t.posX / Game.tile_size);
                int y = (int) (t.posY / Game.tile_size);
                int angle = (int) (t.angle * 2 / Math.PI);

                ArrayList<String> tank = new ArrayList<>();
                tank.add(Integer.toString(x));
                tank.add(Integer.toString(y));
                tank.add(t.name);
                tank.add(Integer.toString(angle));

                if (t.team != null)
                    tank.add(t.team.name);

                this.tanksIR.add(tank);
            }
        }

        this.levelString = Serializer.toTanksON(this);
        return this.levelString;
    }

    // Legacy level strings, from before levels were saved as TanksON

    public Level(String level)
    {
        this(level, false);
    }

    public Level(String level, boolean remote)
    {
        this(level, new ArrayList<>(), remote, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    public Level(String level, ArrayList<TankAIControlled> customTanks)
    {
        this(level, customTanks, false, ScreenPartyHost.isServer && Game.disablePartyFriendlyFire);
    }

    /**
     * A level string is structured like this:<br>
     * (parentheses signify required parameters, and square brackets signify optional parameters.<br>
     * Asterisks indicate that the parameter can be repeated, separated by commas.
     * Do not include these in the level string.)<br>
     * {(SizeX),(SizeY),[(Red),(Green),(Blue)],[(RedNoise),(GreenNoise),(BlueNoise)]|[(ObstacleX)-(ObstacleY)-[ObstacleMetadata]]*|[(TankX)-(TankY)-(TankType)-[TankAngle]-[TeamName]]*|[(TeamName)-[FriendlyFire]-[(Red)-(Green)-(Blue)]]*}
     */
    public Level(String level, ArrayList<TankAIControlled> customTanks, boolean remote, boolean disableFriendlyFire)
    {
        obstacles = new ArrayList<>();
        this.tanks = new ArrayList<>();
        this.obstaclesIR = new ArrayList<>();
        this.tanksIR = new ArrayList<>();

        this.disableFriendlyFire = disableFriendlyFire;
        this.remote = remote;

        this.customTanks = customTanks;
        String[] preset = new String[0];
        String[] screen = new String[0];
        String[] obstaclesPos = new String[0];
        String[] tanks = new String[0];
        String[] teams;

        // Levels from before teams existed leave them out of the level string;
        // their tanks are given the default teams below so that the rest of the
        // game can assume every level has teams.
        boolean teamsDeclared = false;

        this.levelString = level.replaceAll("\u0000", "");

        //Look Ahead Split (keeping the delimiter with the associated block)
        String[] blocks = this.levelString.split("(?=(level|items|shop|coins|tanks|builds)\n)");

        for (String s: blocks)
        {
            if (s.isEmpty())
            {
                // do nothing
            }
            else if (s.startsWith("items\n"))
            {
                s = s.substring("items\n".length());
                ArrayList<String> objects = getJsonObjects(s);
                for (String o: objects)
                    this.startingItems.add(Item.ItemStack.fromString(null, o));
            }
            else if (s.startsWith("shop\n"))
            {
                s = s.substring("shop\n".length());
                ArrayList<String> objects = getJsonObjects(s);
                for (String o: objects)
                {
                    this.shop.add(Item.ShopItem.fromString(o));
                }
            }
            else if (s.startsWith("coins\n"))
            {
                s = s.substring("coins\n".length());
                this.startingCoins = (int) Double.parseDouble(s.trim());
            }
            else if (s.startsWith("tanks\n"))
            {
                s = s.substring("tanks\n".length());
                ArrayList<String> objects = getJsonObjects(s);
                for (String o: objects)
                {
                    TankAIControlled t = TankAIControlled.fromString(o);
                    if (t != null)
                        this.customTanks.add(t);
                }
            }
            else if (s.startsWith("builds\n"))
            {
                s = s.substring("builds\n".length());
                ArrayList<String> objects = getJsonObjects(s);
                for (String o: objects)
                {
                    TankPlayer.ShopTankBuild t = TankPlayer.ShopTankBuild.fromString(o);
                    t.enableTertiaryColor = true;
                    this.playerBuilds.add(t);
                }
            }
            else
            {
                if (s.startsWith("level\n"))
                {
                    s = s.substring("level\n".length());
                }
                preset = s.substring(s.indexOf('{') + 1, s.indexOf('}')).split("\\|");
                screen = preset[0].split(",");
                obstaclesPos = preset[1].split(",");
                tanks = preset[2].split(",");

                if (preset.length >= 4)
                {
                    teamsDeclared = true;
                    teams = preset[3].split(",");
                    tankTeams = new Team[teams.length];

                    for (int i = 0; i < teams.length; i++)
                    {
                        String[] t = teams[i].split("-");

                        if (t.length >= 5)
                            tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]), Double.parseDouble(t[2]), Double.parseDouble(t[3]), Double.parseDouble(t[4]));
                        else if (t.length >= 2)
                            tankTeams[i] = new Team(t[0], Boolean.parseBoolean(t[1]));
                        else
                            tankTeams[i] = new Team(t[0]);

                        if (disableFriendlyFire)
                            tankTeams[i].friendlyFire = false;

                        teamsMap.put(t[0], tankTeams[i]);
                    }
                }
                else
                {
                    if (disableFriendlyFire)
                    {
                        teamsMap.put("ally", Game.playerTeamNoFF);
                        teamsMap.put("enemy", Game.enemyTeamNoFF);
                    }
                    else
                    {
                        teamsMap.put("ally", Game.playerTeam);
                        teamsMap.put("enemy", Game.enemyTeam);
                    }
                }

                if (screen[0].startsWith("*"))
                {
                    editable = false;
                    screen[0] = screen[0].substring(1);
                }
            }
        }

        if (TankModels.tank != null && playerBuilds.isEmpty())
        {
            TankPlayer.ShopTankBuild tp = new TankPlayer.ShopTankBuild();
            playerBuilds.add(tp);
        }

        sizeX = (int) Double.parseDouble(screen[0]);
        sizeY = (int) Double.parseDouble(screen[1]);

        if (screen.length >= 5)
        {
            color.set((int) Double.parseDouble(screen[2]), (int) Double.parseDouble(screen[3]), (int) Double.parseDouble(screen[4]));

            if (screen.length >= 8)
            {
                int colorVarR = Math.min(255 - (int) color.red, (int) Double.parseDouble(screen[5]));
                int colorVarG = Math.min(255 - (int) color.green, (int) Double.parseDouble(screen[6]));
                int colorVarB = Math.min(255 - (int) color.blue, (int) Double.parseDouble(screen[7]));
                colorVar.set(colorVarR, colorVarG, colorVarB);
            }
        }

        if (screen.length >= 9)
        {
            int length = (int) Double.parseDouble(screen[8]) * 100;

            if (length > 0)
                this.timer = length;
        }

        if (screen.length >= 11)
        {
            light = (int) Double.parseDouble(screen[9]) / 100.0;
            shadow = (int) Double.parseDouble(screen[10]) / 100.0;
        }

        if (screen.length >= 14)
            lightColor.set(Double.parseDouble(screen[11]), Double.parseDouble(screen[12]), Double.parseDouble(screen[13]));

        if (!((obstaclesPos.length == 1 && obstaclesPos[0].isEmpty()) || obstaclesPos.length == 0))
        {
            for (String obstaclesPo: obstaclesPos)
            {
                String[] obs = obstaclesPo.split("-");

                ArrayList<String> obsIR = new ArrayList<>();
                obsIR.add(obs[0].replace("...", ":")); //X Coordinate (in sliced notation)
                obsIR.add(obs[1].replace("...", ":")); //Y Coordinate (in sliced notation)
                if (obs.length >= 3)
                    obsIR.add(obs[2]); //Name
                if (obs.length >= 4)
                    obsIR.add(obs[3]); //Metadata
                obstaclesIR.add(obsIR);



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

                for (double x = startX; x <= endX; x++)
                {
                    for (double y = startY; y <= endY; y++)
                    {
                        Obstacle o = Game.registryObstacle.getEntry(name).getObstacle(x, y);

                        if (meta != null)
                            o.setMetadata(meta);

                        if (o instanceof ObstacleBeatBlock)
                        {
                            this.synchronizeMusic = true;
                            this.beatBlocks |= (int) ((ObstacleBeatBlock) o).beatFrequency;
                        }

                        obstacles.add(o);
                    }
                }
            }
        }

        int currentCrusadeID = 0;
        LinkedHashMap<String, TankAIControlled> customTanksMap = new LinkedHashMap<>();
        for (TankAIControlled t: this.customTanks)
            customTanksMap.put(t.name, t);

        tanksToRemove = new ArrayList<>();

        if (!preset[2].isEmpty())
        {
            for (String s: tanks)
            {
                String[] tank = s.split("-");

                if (!teamsDeclared)
                    tank = withDefaultTeam(tank);

                ArrayList<String> tankIR = new ArrayList<>();
                tankIR.add(tank[0]); //X Coordinate
                tankIR.add(tank[1]); //Y Coordinate
                tankIR.add(tank[2]); //Name
                tankIR.add(tank[3]); //Angle
                tankIR.add(tank[4]); //Team
                tanksIR.add(tankIR);

                double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]));
                double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]));
                String type = tank[2].toLowerCase();
                double angle = 0;

                StringBuilder metadata = new StringBuilder();
                for (int i = 3; i < tank.length; i++)
                {
                    metadata.append(tank[i]);
                    if (i < tank.length - 1)
                        metadata.append("-");
                }

                if (tank.length >= 4)
                    angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

                Team team = null;

                if (tank.length >= 5)
                    team = teamsMap.get(tank[4]);

                Tank t;
                if (type.equals("player"))
                {
                    this.playerSpawnsX.add(x);
                    this.playerSpawnsY.add(y);
                    this.playerSpawnsAngle.add(angle);
                    this.playerSpawnsTeam.add(team);

                    continue;
                }

                if (customTanksMap.get(type) != null)
                    t = customTanksMap.get(type).instantiate(type, x, y, angle);
                else
                    t = Game.registryTank.getEntry(type).getTank(x, y, angle);

                t.crusadeID = currentCrusadeID;
                currentCrusadeID++;

                Level l = Game.currentLevel;
                Game.currentLevel = this;
                if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks && Crusade.currentCrusade.retry && !Crusade.currentCrusade.livingTankIDs.contains(t.crusadeID))
                    tanksToRemove.add(t);
                else
                    t.setMetadata(metadata.toString());
                Game.currentLevel = l;

                if (remote)
                    this.tanks.add(new TankRemote(t));
                else
                {
                    this.tanks.add(t);
                    setSolidTank((int) Double.parseDouble(tank[0]), (int) Double.parseDouble(tank[1]), true);
                }
            }
        }

        this.commonInit();
    }

    /**
     * Gives a legacy tank entry, from a level string with no teams section, the team it would
     * have been put on before levels could name their own teams.
     */
    protected static String[] withDefaultTeam(String[] tank)
    {
        if (tank.length >= 5)
            return tank;

        String[] out = new String[5];
        System.arraycopy(tank, 0, out, 0, tank.length);

        if (tank.length < 4)
            out[3] = "0";

        out[4] = tank[2].equalsIgnoreCase("player") ? "ally" : "enemy";
        return out;
    }
}

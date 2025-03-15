package tanks;

import tanks.bullet.DefaultBullets;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.gui.screen.leveleditor.selector.SelectorTeam;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.network.event.*;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleBeatBlock;
import tanks.registry.RegistryTank;
import tanks.tank.*;

import java.util.*;

public class Level 
{
	public String levelString;

	public String[] preset;
	public String[] screen;
	public String[] obstaclesPos;
	public String[] tanks;
	public String[] teams;

	public Team[] tankTeams;
	public boolean enableTeams = false;

	public static double currentColorR = 235;
	public static double currentColorG = 207;
	public static double currentColorB = 166;

	public static double currentColorVarR = 235;
	public static double currentColorVarG = 207;
	public static double currentColorVarB = 166;

	public static double currentLightIntensity = 1;
	public static double currentShadowIntensity = 0.5;

	public static int currentCloudCount = 0;

	public static Random random = new Random();

	public boolean editable = true;
	public boolean remote = false;
	public boolean preview = false;

	public boolean timed = false;
	public double timer;

	public int sizeX;
	public int sizeY;

	public int colorR = 235;
	public int colorG = 207;
	public int colorB = 166;

	public int colorVarR = 20;
	public int colorVarG = 20;
	public int colorVarB = 20;

	public int tilesRandomSeed = 0;

	public double light = 1.0;
	public double shadow = 0.5;

	public HashMap<String, Team> teamsMap = new HashMap<>();

	public ArrayList<Team> teamsList = new ArrayList<>();

	public ArrayList<Integer> availablePlayerSpawns = new ArrayList<>();

	public ArrayList<Double> playerSpawnsX = new ArrayList<>();
	public ArrayList<Double> playerSpawnsY = new ArrayList<>();
	public ArrayList<Double> playerSpawnsAngle = new ArrayList<>();
	public ArrayList<Team> playerSpawnsTeam = new ArrayList<>();

	public ArrayList<Player> includedPlayers = new ArrayList<>();

	public int startingCoins;
	public ArrayList<Item.ShopItem> shop = new ArrayList<>();
	public ArrayList<Item.ItemStack<?>> startingItems = new ArrayList<>();
	public ArrayList<TankPlayer.ShopTankBuild> playerBuilds = new ArrayList<>();

	// Saved on the client to keep track of what each item is
	public int clientStartingCoins;
	public ArrayList<Item.ShopItem> clientShop = new ArrayList<>();
	public ArrayList<Item.ItemStack<?>> clientStartingItems = new ArrayList<>();

	public ArrayList<TankAIControlled> customTanks = new ArrayList<>();

	public HashMap<String, Integer> itemNumbers = new HashMap<>();

	public double startTime = 400;
	public boolean disableFriendlyFire = false;

	public boolean synchronizeMusic = false;
	public int beatBlocks = 0;

	public HashMap<String, Tank> tankLookupTable = null;

	/**
	 * A level string is structured like this:
	 * (parentheses signify required parameters, and square brackets signify optional parameters. 
	 * Asterisks indicate that the parameter can be repeated, separated by commas
	 * Do not include these in the level string.)
	 * {(SizeX),(SizeY),[(Red),(Green),(Blue)],[(RedNoise),(GreenNoise),(BlueNoise)]|[(ObstacleX)-(ObstacleY)-[ObstacleMetadata]]*|[(TankX)-(TankY)-(TankType)-[TankAngle]-[TeamName]]*|[(TeamName)-[FriendlyFire]-[(Red)-(Green)-(Blue)]]*}
	 */
	public Level(String level)
	{
		if (ScreenPartyHost.isServer)
			this.startTime = Game.partyStartTime;

		this.levelString = level.replaceAll("\u0000", "");

		int parsing = 0;

		String[] lines = this.levelString.split("\n");

		for (String s: lines)
		{
			switch (s.toLowerCase())
			{
				case "level":
					parsing = 0;
					break;
				case "items":
					parsing = 1;
					break;
				case "shop":
					parsing = 2;
					break;
				case "coins":
					parsing = 3;
					break;
				case "tanks":
					parsing = 4;
					break;
				case "builds":
					parsing = 5;
					break;
				default:
					if (parsing == 0)
					{
						preset = s.substring(s.indexOf('{') + 1, s.indexOf('}')).split("\\|");
						screen = preset[0].split(",");
						obstaclesPos = preset[1].split(",");
						tanks = preset[2].split(",");

						if (preset.length >= 4)
						{
							teams = preset[3].split(",");
							enableTeams = true;
						}

						if (screen[0].startsWith("*"))
						{
							editable = false;
							screen[0] = screen[0].substring(1);
						}
					}
					else if (parsing == 4)
					{
						TankAIControlled t = TankAIControlled.fromString(s);
						if (t != null)
							this.customTanks.add(t);
					}
					else if (parsing == 5)
					{
						TankPlayer.ShopTankBuild t = TankPlayer.ShopTankBuild.fromString(s);
						t.enableTertiaryColor = true;
						this.playerBuilds.add(t);
					}
					else
					{
						if (parsing == 1)
							this.startingItems.add(Item.ItemStack.fromString(null, s));
						else if (parsing == 2)
							this.shop.add(Item.ShopItem.fromString(s));
						else
							this.startingCoins = Integer.parseInt(s);
					}
					break;
			}
		}

		if (playerBuilds.isEmpty())
		{
			TankPlayer.ShopTankBuild tp = new TankPlayer.ShopTankBuild();
			playerBuilds.add(tp);
		}

		if (ScreenPartyHost.isServer && Game.disablePartyFriendlyFire)
			this.disableFriendlyFire = true;

		sizeX = Integer.parseInt(screen[0]);
		sizeY = Integer.parseInt(screen[1]);

		if (screen.length >= 5)
		{
			colorR = Integer.parseInt(screen[2]);
			colorG = Integer.parseInt(screen[3]);
			colorB = Integer.parseInt(screen[4]);

			if (screen.length >= 8)
			{
				colorVarR = Math.min(255 - colorR, Integer.parseInt(screen[5]));
				colorVarG = Math.min(255 - colorG, Integer.parseInt(screen[6]));
				colorVarB = Math.min(255 - colorB, Integer.parseInt(screen[7]));
			}
		}

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
			this.shop = new ArrayList<>();
		}
	}

	public void loadLevel()
	{
		loadLevel(null);
	}

	public void loadLevel(boolean remote)
	{
		loadLevel(null, remote);
	}

	public void loadLevel(ILevelPreviewScreen s)
	{
		loadLevel(s, false);
	}

	public void loadLevel(ILevelPreviewScreen sc, boolean remote)
	{
		int currentCrusadeID = 0;

		if (Game.deterministicMode)
			random = new Random(Game.seed);
		else
			random = new Random();

		if (ScreenPartyHost.isServer)
			ScreenPartyHost.includedPlayers.clear();
		else if (ScreenPartyLobby.isClient)
			ScreenPartyLobby.includedPlayers.clear();

		if (sc == null)
			Obstacle.draw_size = 0;
		else
			Obstacle.draw_size = 50;

		this.remote = remote;

		if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
			Game.eventsOut.add(new EventLoadLevel(this));

		LinkedHashMap<String, TankAIControlled> customTanksMap = new LinkedHashMap<>();
		for (TankAIControlled t : this.customTanks)
			customTanksMap.put(t.name, t);

		Tank.currentID = 0;
		Tank.freeIDs.clear();

		Game.currentLevel = this;
		Game.currentLevelString = this.levelString;

		ScreenGame.finishedQuick = false;

		ScreenGame.finished = false;
		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		if (enableTeams)
		{
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

				teamsList.add(tankTeams[i]);
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

		currentCloudCount = (int) (Math.random() * (double) this.sizeX / 10.0D + Math.random() * (double) this.sizeY / 10.0D);

		if (screen.length >= 9)
		{
			int length = Integer.parseInt(screen[8]) * 100;

			if (length > 0)
			{
				this.timed = true;
				this.timer = length;
			}
		}

		if (screen.length >= 11)
		{
			light = Integer.parseInt(screen[9]) / 100.0;
			shadow = Integer.parseInt(screen[10]) / 100.0;
		}

		if (sc instanceof ScreenLevelEditor)
		{
			ScreenLevelEditor s = (ScreenLevelEditor) sc;

			s.level = this;

			s.selectedTiles = new boolean[sizeX][sizeY];
			Game.movables.remove(Game.playerTank);
		}

		this.reloadTiles();

		if (!((obstaclesPos.length == 1 && obstaclesPos[0].isEmpty()) || obstaclesPos.length == 0))
		{
			for (String obstaclesPo : obstaclesPos)
			{
				String[] obs = obstaclesPo.split("-");

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

						Game.obstacles.add(o);
					}
				}
			}
		}

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		boolean[][] solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Obstacle o : Game.obstacles)
		{
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				Game.game.solidGrid[x][y] = true;

				if (!o.shouldShootThrough)
					Game.game.unbreakableGrid[x][y] = true;
			}

			if (o.tankCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
				solidGrid[x][y] = true;
		}

		boolean[][] tankGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Movable m : Game.movables)
		{
			if (m instanceof Tank)
			{
				int x = (int) (m.posX / Game.tile_size);
				int y = (int) (m.posY / Game.tile_size);

				if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
				{
					tankGrid[x][y] = true;
				}
			}
		}

		ArrayList<Tank> tanksToRemove = new ArrayList<>();

		if (!preset[2].isEmpty())
		{
			for (String s : tanks)
			{
				String[] tank = s.split("-");
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

				Team team = Game.enemyTeam;

				if (this.disableFriendlyFire)
					team = Game.enemyTeamNoFF;

				if (enableTeams)
				{
					if (tank.length >= 5)
						team = teamsMap.get(tank[4]);
					else
						team = null;
				}

				Tank t;
				if (type.equals("player"))
				{
					if (team == Game.enemyTeam)
						team = Game.playerTeam;

					if (team == Game.enemyTeamNoFF)
						team = Game.playerTeamNoFF;

					this.playerSpawnsX.add(x);
					this.playerSpawnsY.add(y);
					this.playerSpawnsAngle.add(angle);
					this.playerSpawnsTeam.add(team);

					int x1 = (int) Double.parseDouble(tank[0]);
					int y1 = (int) Double.parseDouble(tank[1]);

					if (x1 >= 0 && y1 >= 0 && x1 < tankGrid.length && y1 < tankGrid[0].length)
						tankGrid[x1][y1] = true;

					continue;
				}

				if (customTanksMap.get(type) != null)
					t = customTanksMap.get(type).instantiate(type, x, y, angle);
				else
					t = Game.registryTank.getEntry(type).getTank(x, y, angle);

				t.crusadeID = currentCrusadeID;
				currentCrusadeID++;

				if (Crusade.crusadeMode && !Crusade.currentCrusade.respawnTanks && Crusade.currentCrusade.retry && !Crusade.currentCrusade.livingTankIDs.contains(t.crusadeID))
					tanksToRemove.add(t);
				else
					t.setMetadata(metadata.toString());

				// Don't do this in your code! We only want to dynamically generate tank IDs on level load!
				t.networkID = Tank.nextFreeNetworkID();
				Tank.idMap.put(t.networkID, t);

				if (sc != null)
					t.drawAge = 50;

				if (remote)
					Game.movables.add(new TankRemote(t));
				else
					Game.movables.add(t);
			}
		}

		if (sc instanceof ScreenLevelEditor)
		{
			ScreenLevelEditor s = (ScreenLevelEditor) sc;
			if (!enableTeams)
			{
				enableTeams = true;

				Team player = new Team(Game.playerTeam.name);
				Team enemy = new Team(Game.enemyTeam.name);

				for (Movable m : Game.movables)
				{
					if (m.team == Game.playerTeam)
						m.team = player;
					else if (m.team == Game.enemyTeam)
						m.team = enemy;
				}

				this.teamsList.add(enemy);
				this.teamsList.add(player);
			}

			s.teams = this.teamsList;
			if (s.teams.size() > 0)
			{
				s.currentMetadata.put(SelectorTeam.selector_name, s.teams.get(0));
				s.currentMetadata.put(SelectorTeam.player_selector_name, s.teams.get(Math.min(s.teams.size() - 1, 1)));
			}
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

			boolean[][] explored = new boolean[Game.currentSizeX][Game.currentSizeY];
			boolean[][] blacklist = new boolean[Game.currentSizeX][Game.currentSizeY];

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

					if (t1.posX >= 0 && t1.posX < Game.currentSizeX && t1.posY >= 0 && t1.posY < Game.currentSizeY &&
							!solidGrid[t1.posX][t1.posY] && !tankGrid[t1.posX][t1.posY] && !explored[t1.posX][t1.posY])
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

							tankGrid[t1.posX][t1.posY] = true;

							for (int x = Math.max(t1.posX - 1, 0); x <= Math.min(t1.posX + 1, Game.currentSizeX - 1); x++)
							{
								for (int y = Math.max(t1.posY - 1, 0); y <= Math.min(t1.posY + 1, Game.currentSizeY - 1); y++)
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

				if (!tankGrid[x][y])
				{
					playerSpawnsX.add((x + 0.5) * Game.tile_size);
					playerSpawnsY.add((y + 0.5) * Game.tile_size);
					playerSpawnsTeam.add(playerSpawnsTeam.get(i));
					playerSpawnsAngle.add(playerSpawnsAngle.get(i));
					spawnsLeft--;
				}
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
						for (TankPlayer.ShopTankBuild shopTankBuild : builds)
						{
							if (shopTankBuild.name.equals(Game.player.buildName))
								build = shopTankBuild;
						}
					}
					build.clonePropertiesTo(tank);
					Game.playerTank = tank;
					Game.player.buildName = tank.buildName;
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

		if (!remote && sc == null || (sc instanceof ScreenLevelEditor))
			Game.eventsOut.add(new EventEnterLevel());
	}

	public void reloadTiles()
	{
		Game.currentSizeX = (int) (sizeX * Game.bgResMultiplier);
		Game.currentSizeY = (int) (sizeY * Game.bgResMultiplier);

		currentColorR = colorR;
		currentColorG = colorG;
		currentColorB = colorB;

		currentColorVarR = colorVarR;
		currentColorVarG = colorVarG;
		currentColorVarB = colorVarB;

		currentLightIntensity = light;
		currentShadowIntensity = shadow;

		Game.tilesR = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesG = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesB = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesDepth = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesFlash = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tileDrawables = new Obstacle[Game.currentSizeX][Game.currentSizeY];

		Random tilesRandom = new Random(this.tilesRandomSeed);
		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				if (Game.fancyTerrain)
				{
					Game.tilesR[i][j] = (colorR + tilesRandom.nextDouble() * colorVarR);
					Game.tilesG[i][j] = (colorG + tilesRandom.nextDouble() * colorVarG);
					Game.tilesB[i][j] = (colorB + tilesRandom.nextDouble() * colorVarB);
					double rand = tilesRandom.nextDouble() * 10;
					Game.tilesDepth[i][j] = Game.enable3dBg ? rand : 0;
				}
				else
				{
					Game.tilesR[i][j] = colorR;
					Game.tilesG[i][j] = colorG;
					Game.tilesB[i][j] = colorB;
					Game.tilesDepth[i][j] = 0;
				}
			}
		}

		Game.game.heightGrid = new double[Game.currentSizeX][Game.currentSizeY];
		Game.game.groundHeightGrid = new double[Game.currentSizeX][Game.currentSizeY];
		Game.game.groundEdgeHeightGrid = new double[Game.currentSizeX][Game.currentSizeY];
		Drawing.drawing.setScreenBounds(Game.tile_size * sizeX, Game.tile_size * sizeY);

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Obstacle o: Game.obstacles)
		{
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				Game.game.solidGrid[x][y] = true;

				if (!o.shouldShootThrough)
					Game.game.unbreakableGrid[x][y] = true;
			}
		}

		ScreenLevelEditor s = null;
		
		if (Game.screen instanceof ScreenLevelEditor)
			s = (ScreenLevelEditor) Game.screen;
		else if (Game.screen instanceof ScreenLevelEditorOverlay)
			s = ((ScreenLevelEditorOverlay) Game.screen).editor;

		if (s != null)
			s.selectedTiles = new boolean[Game.currentSizeX][Game.currentSizeY];
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
		return Level.currentColorR * 0.2126 + Level.currentColorG * 0.7152 + Level.currentColorB * 0.0722 <= 127 || currentLightIntensity <= 0.5;
	}

	public Tank lookupTank(String name)
	{
		if (Game.screen instanceof ScreenGame)
		{
			if (this.tankLookupTable == null)
			{
				this.tankLookupTable = new HashMap<>();

				for (RegistryTank.TankEntry e : Game.registryTank.tankEntries)
				{
					this.tankLookupTable.put(e.name, e.getTank(0, 0, 0));
				}

				for (TankAIControlled t : this.customTanks)
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
				for (TankAIControlled t : this.customTanks)
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
}

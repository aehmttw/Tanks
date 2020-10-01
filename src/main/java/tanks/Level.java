package tanks;

import tanks.event.EventCreatePlayer;
import tanks.event.EventEnterLevel;
import tanks.event.EventLoadLevel;
import tanks.gui.Button;
import tanks.gui.screen.*;
import tanks.hotbar.item.Item;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryObstacle;
import tanks.registry.RegistryTank;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankRemote;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;
import java.util.HashMap;

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

	public boolean editable = true;
	public boolean remote = false;
	public boolean preview = false;

	public HashMap<String, Team> teamsMap = new HashMap<String, Team>();

	public ArrayList<Team> teamsList = new ArrayList<Team>();

	public ArrayList<Integer> availablePlayerSpawns = new ArrayList<Integer>();

	public ArrayList<Double> playerSpawnsX = new ArrayList<Double>();
	public ArrayList<Double> playerSpawnsY = new ArrayList<Double>();
	public ArrayList<Double> playerSpawnsAngle = new ArrayList<Double>();
	public ArrayList<Team> playerSpawnsTeam = new ArrayList<Team>();

	public ArrayList<Player> includedPlayers = new ArrayList<Player>();

	public int startingCoins;
	public ArrayList<Item> shop = new ArrayList<Item>();
	public ArrayList<Item> startingItems = new ArrayList<Item>();

	/**
	 * A level string is structured like this:
	 * (parentheses signify required parameters, and square brackets signify optional parameters. 
	 * Asterisks indicate that the parameter can be repeated, separated by commas
	 * Do not include these in the level string.)
	 * {(SizeX),(SizeY),[(Red),(Green),(Blue)],[(RedNoise),(GreenNoise),(BlueNoise)]|[(ObstacleX)-(ObstacleY)-[ObstacleMetadata]]*|[(TankX)-(TankY)-(TankType)-[TankAngle]-[TeamName]]*|[(TeamName)-[FriendlyFire]-[(Red)-(Green)-(Blue)]]*}
	 */
	public Level(String level)
	{
		this.levelString = level.replaceAll("\u0000", "");

		int parsing = 0;

		for (String s: this.levelString.split("\n"))
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
					else if (!ScreenPartyLobby.isClient)
					{
						if (parsing == 1)
							this.startingItems.add(Item.parseItem(null, s));
						else if (parsing == 2)
							this.shop.add(Item.parseItem(null, s));
						else if (parsing == 3)
							this.startingCoins = Integer.parseInt(s);
					}

					break;
			}
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
		if (ScreenPartyHost.isServer)
			ScreenPartyHost.includedPlayers.clear();
		else if (ScreenPartyLobby.isClient)
			ScreenPartyLobby.includedPlayers.clear();

		if (sc == null)
			Obstacle.draw_size = 0;
		else
			Obstacle.draw_size = 50;

		this.remote = remote;

		if (!remote && sc == null || (sc instanceof ScreenLevelBuilder))
			Game.eventsOut.add(new EventLoadLevel(this));

		ArrayList<EventCreatePlayer> playerEvents = new ArrayList<EventCreatePlayer>();

		Tank.currentID = 0;
		Tank.freeIDs.clear();

		RegistryTank.loadRegistry(Game.homedir);
		RegistryObstacle.loadRegistry(Game.homedir);

		Game.currentLevel = this;
		Game.currentLevelString = this.levelString;

		ScreenGame.finishedQuick = false;

		ScreenGame.finished = false;
		ScreenGame.finishTimer = ScreenGame.finishTimerMax;

		int sX = Integer.parseInt(screen[0]);
		int sY = Integer.parseInt(screen[1]);

		int r = 235;
		int g = 207;
		int b = 166;

		int dr = 20;
		int dg = 20;
		int db = 20;

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

				teamsMap.put(t[0], tankTeams[i]);

				teamsList.add(tankTeams[i]);
			}
		}
		else
		{
			teamsMap.put("ally", Game.playerTeam);
			teamsMap.put("enemy", Game.enemyTeam);
		}

		if (screen.length >= 5)
		{
			r = Integer.parseInt(screen[2]);
			g = Integer.parseInt(screen[3]);
			b = Integer.parseInt(screen[4]);

			if (screen.length >= 8)
			{
				dr = Math.min(255 - r, Integer.parseInt(screen[5]));
				dg = Math.min(255 - g, Integer.parseInt(screen[6]));
				db = Math.min(255 - b, Integer.parseInt(screen[7]));
			}
		}

		if (sc instanceof ScreenLevelBuilder)
		{
			ScreenLevelBuilder s = (ScreenLevelBuilder) sc;

			s.sizeX.inputText = sX + "";
			s.sizeY.inputText = sY + "";

			s.width = sX;
			s.height = sY;
			s.selectedTiles = new boolean[sX][sY];

			s.r = r;
			s.g = g;
			s.b = b;
			s.dr = dr;
			s.dg = dg;
			s.db = db;
			s.editable = this.editable;
			Game.movables.remove(Game.playerTank);

			s.colorRed.inputText = r + "";
			s.colorGreen.inputText = g + "";
			s.colorBlue.inputText = b + "";
			s.colorVarRed.inputText = dr + "";
			s.colorVarGreen.inputText = dg + "";
			s.colorVarBlue.inputText = db + "";

			s.colorVarRed.maxValue = 255 - r;
			s.colorVarGreen.maxValue = 255 - g;
			s.colorVarBlue.maxValue = 255 - b;

			if (!editable)
			{
				s.play.posY += 60;
				s.delete.posY -= 60;
				s.quit.posY -= 60;
			}

			if (!enableTeams)
			{
				this.teamsList.add(Game.playerTeam);
				this.teamsList.add(Game.enemyTeam);
			}

			for (int i = 0; i < this.teamsList.size(); i++)
			{
				final int j = i;
				Team t = this.teamsList.get(i);
				Button buttonToAdd = new Button(0, 0, 350, 40, t.name, new Runnable()
				{
					@Override
					public void run() 
					{
						s.teamName.inputText = t.name;
						s.lastTeamButton = j;
						s.editTeamMenu = true;
						s.selectedTeam = t;
						if (s.selectedTeam.friendlyFire)
							s.teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.onText;
						else
							s.teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.offText;
					}
				}
						);
				s.teamEditButtons.add(buttonToAdd);

				Button buttonToAdd2 = new Button(0, 0, 350, 40, t.name, new Runnable()
				{
					@Override
					public void run() 
					{
						s.setEditorTeam(j);
					}
				}
						);

				s.teamSelectButtons.add(buttonToAdd2);


			}

			s.teams = this.teamsList;

			s.sortButtons();
		}

		Game.currentSizeX = (int) (sX * Game.bgResMultiplier);
		Game.currentSizeY = (int) (sY * Game.bgResMultiplier);

		currentColorR = r;
		currentColorG = g;
		currentColorB = b;

		Game.tilesR = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesG = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesB = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tilesDepth = new double[Game.currentSizeX][Game.currentSizeY];
		Game.tileDrawables = new Obstacle[Game.currentSizeX][Game.currentSizeY];

		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				Game.tilesR[i][j] = (r + Math.random() * dr);
				Game.tilesG[i][j] = (g + Math.random() * dg);
				Game.tilesB[i][j] = (b + Math.random() * db);
				Game.tilesDepth[i][j] = Math.random() * 10;
			}
		}

		Drawing.drawing.setScreenBounds(Game.tile_size * sX, Game.tile_size * sY);

		if (!((obstaclesPos.length == 1 && obstaclesPos[0].equals("")) || obstaclesPos.length == 0)) 
		{
			for (int i = 0; i < obstaclesPos.length; i++)
			{
				String[] obs = obstaclesPos[i].split("-");

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

						Game.obstacles.add(o);
					}
				}
			}
		}

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		boolean[][] solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];


		for (Obstacle o: Game.obstacles)
		{
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
				Game.game.solidGrid[x][y] = true;

			if (o.tankCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
				solidGrid[x][y] = true;
		}

		boolean[][] tankGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Movable m: Game.movables)
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

		if (!preset[2].equals(""))
		{
			for (int i = 0; i < tanks.length; i++)
			{
				String[] tank = tanks[i].split("-");
				double x = Game.tile_size * (0.5 + Double.parseDouble(tank[0]));
				double y = Game.tile_size * (0.5 + Double.parseDouble(tank[1]));
				String type = tank[2].toLowerCase();
				double angle = 0;

				if (tank.length >= 4)
					angle = (Math.PI / 2 * Double.parseDouble(tank[3]));

				Team team = Game.enemyTeam;
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

					this.playerSpawnsX.add(x);
					this.playerSpawnsY.add(y);
					this.playerSpawnsAngle.add(angle);
					this.playerSpawnsTeam.add(team);
					tankGrid[(int) Double.parseDouble(tank[0])][(int) Double.parseDouble(tank[1])] = true;

					continue;
				}
				else
				{
					t = Game.registryTank.getEntry(type).getTank(x, y, angle);
				}

				t.team = team;

				if (remote)
					Game.movables.add(new TankRemote(t));
				else
					Game.movables.add(t);
			}
		}

		this.availablePlayerSpawns.clear();

		int playerCount = 1;
		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null && sc == null)
			playerCount += ScreenPartyHost.server.connections.size();

		if (this.includedPlayers.size() > 0)
			playerCount = this.includedPlayers.size();
		else
		{
			this.includedPlayers.addAll(Game.players);
		}

		int extraSpawns = 0;
		if (playerCount > playerSpawnsX.size() && playerSpawnsX.size() > 0)
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

		if (sc == null && !preview)
		{
			for (int i = 0; i < playerCount; i++)
			{
				if (this.availablePlayerSpawns.size() == 0)
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
				{
					EventCreatePlayer e = new EventCreatePlayer(this.includedPlayers.get(i), x, y, angle, team);
					playerEvents.add(e);
					Game.eventsOut.add(e);
				}
				else if (!remote)
				{
					TankPlayer tank = new TankPlayer(x, y, angle);
					Game.playerTank = tank;
					tank.team = team;
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
				Game.movables.add(t);

				if (sc != null)
					sc.getSpawns().add(t);
			}

			if (sc instanceof ScreenLevelBuilder)
				((ScreenLevelBuilder) sc).movePlayer = (sc.getSpawns().size() <= 1);
		}

		for (EventCreatePlayer e: playerEvents)
			e.execute();

		if (!remote && sc == null || (sc instanceof ScreenLevelBuilder))
			Game.eventsOut.add(new EventEnterLevel());
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
			sides.add(1);
			sides.add(2);
			sides.add(3);
			sides.add(4);

			int i = 0;

			while (!sides.isEmpty())
			{
				int s = sides.remove((int) (Math.random() * sides.size()));
				sidesOrder[i] = s;
				i++;
			}
		}
	}
}

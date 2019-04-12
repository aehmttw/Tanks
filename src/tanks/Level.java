package tanks;

import java.util.ArrayList;
import java.util.HashMap;

import tanks.tank.Tank;
import tanks.tank.TankPlayer;

public class Level 
{
	String level;
	
	String[] preset;
	String[] screen;
	String[] obstaclesPos;
	String[] tanks;
	String[] teams;

	Team[] tankTeams;
	boolean enableTeams = false;

	public static double currentColorR = 235;
	public static double currentColorG = 207;
	public static double currentColorB = 166;

	public boolean editable = true;

	public HashMap<String, Team> teamsMap = new HashMap<String, Team>();
	public ArrayList<Team> teamsList = new ArrayList<Team>();

	/**
	 * A level string is structured like this:
	 * (parentheses signify required parameters, and square brackets signify optional parameters. 
	 * Asterisks indicate that the parameter can be repeated, separated by commas
	 * Do not include these in the level string.)
	 * {(SizeX),(SizeY),[(Red),(Green),(Blue)],[(RedNoise),(GreenNoise),(BlueNoise)]|[(ObstacleX)-(ObstacleY)]*|[(TankX)-(TankY)-(TankType)-[TankAngle]-[TeamName]]*|[(TeamName)-[FriendlyFire]-[(Red)-(Green)-(Blue)]]*}
	 */
	public Level(String level)
	{		
		this.level = level;
		
		preset = level.split("\\{")[1].split("\\}")[0].split("\\|");

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
		else
			editable = true;
	}

	public void loadLevel()
	{
		loadLevel(null);
	}

	public void loadLevel(ScreenLevelBuilder s)
	{
		RegistryTank.loadRegistry(Game.homedir);
		RegistryObstacle.loadRegistry(Game.homedir);
		
		Game.currentLevel = this.level;

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

		if (s != null)
		{
			s.sizeX.inputText = sX + "";
			s.sizeY.inputText = sY + "";

			s.width = sX;
			s.height = sY;

			s.r = r;
			s.g = g;
			s.b = b;
			s.dr = dr;
			s.dg = dg;
			s.db = db;
			s.editable = this.editable;
			Game.movables.remove(Game.player);

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
							s.teamFriendlyFire.text = "Friendly fire: on";
						else
							s.teamFriendlyFire.text = "Friendly fire: off";
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

		for (int i = 0; i < Game.currentSizeX; i++)
		{
			for (int j = 0; j < Game.currentSizeY; j++)
			{
				Game.tilesR[i][j] = (r + Math.random() * dr);
				Game.tilesG[i][j] = (g + Math.random() * dg);
				Game.tilesB[i][j] = (b + Math.random() * db);
			}
		}

		Drawing.drawing.setScreenBounds(Game.tank_size * sX, Game.tank_size * sY);

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

				for (double x = startX; x <= endX; x++)
				{
					for (double y = startY; y <= endY; y++)
					{
						Game.obstacles.add(Game.registryObstacle.getEntry(name).getObstacle(x, y));
					}
				}
			}
		}
		
		if (!preset[2].equals(""))
		{
			for (int i = 0; i < tanks.length; i++)
			{
				String[] tank = tanks[i].split("-");
				double x = Game.tank_size * (0.5 + Double.parseDouble(tank[0]));
				double y = Game.tank_size * (0.5 + Double.parseDouble(tank[1]));
				String type = tank[2].toLowerCase();
				double angle = 0;
				if (tank.length >= 4)
					angle = (Math.PI / 2 * Double.parseDouble(tank[3]));
				
				Tank t;
				if (type.equals("player"))
				{
					t = new TankPlayer(x, y, angle);					
					Game.player = (TankPlayer) t;
					t.team = Game.playerTeam;
				}
				else
				{
					t = Game.registryTank.getEntry(type).getTank(x, y, angle);
					t.team = Game.enemyTeam;
				}

				if (enableTeams)
				{
					if (tank.length >= 5)
						t.team = teamsMap.get(tank[4]);
					else
						t.team = null;
				}

				Game.movables.add(t);
			}
		}
	}
}

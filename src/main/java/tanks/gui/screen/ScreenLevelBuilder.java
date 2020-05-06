package tanks.gui.screen;

import basewindow.BaseFile;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.event.EventCreatePlayer;
import tanks.event.INetworkEvent;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.TextBox;
import tanks.obstacle.Obstacle;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenLevelBuilder extends Screen implements ILevelPreviewScreen
{
	public Placeable currentPlaceable = Placeable.enemyTank;
	public int tankNum = 0;
	public int obstacleNum = 0;
	public int teamNum = 1;
	public int playerTeamNum = 0;
	public boolean reloadNewLevel;
	public Tank mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	public int mouseTankOrientation = 0;
	public Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	public boolean paused = true;
	public boolean optionsMenu = false;
	public boolean sizeMenu = false;
	public boolean colorMenu = false;
	public boolean teamsMenu = false;
	public boolean editTeamMenu = false;
	public boolean teamColorMenu = false;
	public boolean objectMenu;
	public boolean selectTeamMenu;
	public boolean rotateTankMenu;
	public boolean confirmDeleteMenu = false;
	public double clickCooldown = 0;
	public Team selectedTeam;
	public int teamPageRows = 6;
	public int teamPage = 0;
	public String teamSelectTitle = "";
	public int tankButtonPage = 0;
	public int obstacleButtonPage = 0;
	public boolean editable = true;
	public ArrayList<Team> teams = new ArrayList<Team>();
	public ArrayList<Button> teamEditButtons = new ArrayList<Button>();
	public ArrayList<Button> teamSelectButtons = new ArrayList<Button>();
	public ArrayList<ButtonObject> tankButtons = new ArrayList<ButtonObject>();
	public ArrayList<ButtonObject> obstacleButtons = new ArrayList<ButtonObject>();
	public ArrayList<TankSpawnMarker> spawns = new ArrayList<TankSpawnMarker>();
	public int objectButtonRows = 3;
	public int objectButtonCols = 10;
	public int lastTeamButton;
	public int r = 235;
	public int g = 207;
	public int b = 166;
	public int dr = 20;
	public int dg = 20;
	public int db = 20;
	public int width = Game.currentSizeX;
	public int height = Game.currentSizeY;
	public String name;
	public boolean movePlayer = true;
	public boolean touchEraseMode = false;

	public ButtonObject movePlayerButton = new ButtonObject(new TankPlayer(0, 0, 0), Drawing.drawing.interfaceSizeX / 2 - 50, Drawing.drawing.interfaceSizeY / 2, 75, 75, new Runnable()
	{
		@Override
		public void run()
		{
			movePlayer = true;
		}
	}, "Move the player");

	public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), Drawing.drawing.interfaceSizeX / 2 + 50, Drawing.drawing.interfaceSizeY / 2, 75, 75, new Runnable()
	{
		@Override
		public void run()
		{
			movePlayer = false;
		}
	}, "Add multiple player spawn points");

	public Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Edit", new Runnable()
	{
		@Override
		public void run()
		{
			clickCooldown = 20;
			paused = false;
		}
	});

	public Button play = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 120), 350, 40, "Play", new Runnable()
	{
		@Override
		public void run()
		{
			play();
		}
	}
	);

	public Button playUnavailable = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 120), 350, 40, "Play", "You must add a player---spawn point to play!");

	public Button options = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 0), 350, 40, "Options", new Runnable()
	{
		@Override
		public void run()
		{
			optionsMenu = true;
		}
	}
	);

	public Button colorOptions = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 60), 350, 40, "Background colors", new Runnable()
	{
		@Override
		public void run()
		{
			colorMenu = true;
		}
	}
	);

	public Button sizeOptions = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2), 350, 40, "Level size", new Runnable()
	{
		@Override
		public void run()
		{
			sizeMenu = true;
		}
	}
	);

	public Button teamsOptions = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), 350, 40, "Teams", new Runnable()
	{
		@Override
		public void run()
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
				((ScreenLevelBuilder)Game.screen).teamsMenu = true;
			}

			teamsMenu = true;
		}
	}
	);

	public Button back1 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 120), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			optionsMenu = false;
		}
	}
	);

	public Button back2 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 180), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			colorMenu = false;
		}
	}
	);

	public Button back3 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 120), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			sizeMenu = false;
		}
	}
	);

	public Button back4 = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			teamsMenu = false;
		}
	}
	);

	public Button back5 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			reload();
			((ScreenLevelBuilder)Game.screen).optionsMenu = true;
			((ScreenLevelBuilder)Game.screen).teamsMenu = true;
			((ScreenLevelBuilder)Game.screen).selectedTeam = null;
		}
	}
	);

	public Button back6 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			teamColorMenu = false;
		}
	}
	);

	public Button back7 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			selectTeamMenu = false;
		}
	}
	);

	public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), 350, 40, "Delete level", new Runnable()
	{
		@Override
		public void run()
		{
			confirmDeleteMenu = true;
		}
	}
	);

	public Button cancelDelete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), 350, 40, "No", new Runnable()
	{
		@Override
		public void run()
		{
			confirmDeleteMenu = false;
		}
	}
	);

	public Button confirmDelete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2), 350, 40, "Yes", new Runnable()
	{
		@Override
		public void run()
		{
			BaseFile file = Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + name);

			Game.cleanUp();

			while (file.exists())
			{
				file.delete();
			}

			Game.screen = new ScreenSavedLevels();
		}
	}
	);

	public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 120), 350, 40, "Exit", new Runnable()
	{
		@Override
		public void run()
		{
			save();

			Game.cleanUp();
			Game.screen = new ScreenSavedLevels();
		}
	}
	);

	public Button nextTeamPage = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			teamPage++;
		}
	}
	);

	public Button previousTeamPage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			teamPage--;
		}
	}
	);

	public Button nextTankPage = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			tankButtonPage++;
		}
	}
	);

	public Button previousTankPage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			tankButtonPage--;
		}
	}
	);

	public Button nextObstaclePage = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			obstacleButtonPage++;
		}
	}
	);

	public Button previousObstaclePage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 180, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			obstacleButtonPage--;
		}
	}
	);

	public Button exitObjectMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Ok", new Runnable()
	{
		@Override
		public void run()
		{
			objectMenu = false;
			paused = false;
			clickCooldown = 20;
		}
	}
	);

	public Button rotateTankButton = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Tank orientation", new Runnable()
	{
		@Override
		public void run()
		{
			rotateTankMenu = true;
		}
	}
	);

	public Button selectTeam = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Team: ", new Runnable()
	{
		@Override
		public void run()
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
			}

			ScreenLevelBuilder s = ((ScreenLevelBuilder)Game.screen);
			s.currentPlaceable = currentPlaceable;
			s.objectMenu = true;
			s.selectTeamMenu = true;

			if (currentPlaceable == Placeable.enemyTank)
				s.teamSelectTitle = "Select tank team";
			else if (currentPlaceable == Placeable.playerTank)
				s.teamSelectTitle = "Select player team";
		}
	}
	);
	public Button deleteTeam = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Delete team", new Runnable()
	{
		@Override
		public void run()
		{
			teams.remove(selectedTeam);
			reload();
			((ScreenLevelBuilder)Game.screen).paused = true;
			((ScreenLevelBuilder)Game.screen).optionsMenu = true;
			((ScreenLevelBuilder)Game.screen).teamsMenu = true;
		}
	}
	);
	public Button teamFriendlyFire = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Friendly fire: on", new Runnable()
	{
		@Override
		public void run()
		{
			selectedTeam.friendlyFire = !selectedTeam.friendlyFire;
			if (selectedTeam.friendlyFire)
				teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.onText;
			else
				teamFriendlyFire.text = "Friendly fire: " + ScreenOptions.offText;
		}
	}
	);
	public Button rotateUp = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 100, 75, 75, "Up", new Runnable()
	{
		@Override
		public void run()
		{
			mouseTankOrientation = 3;
		}
	}
	);
	public Button rotateRight = new Button(Drawing.drawing.interfaceSizeX / 2 + 100, Drawing.drawing.interfaceSizeY / 2, 75, 75, "Right", new Runnable()
	{
		@Override
		public void run()
		{
			mouseTankOrientation = 0;
		}
	}
	);
	public Button rotateDown = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 100, 75, 75, "Down", new Runnable()
	{
		@Override
		public void run()
		{
			mouseTankOrientation = 1;
		}
	}
	);
	public Button rotateLeft = new Button(Drawing.drawing.interfaceSizeX / 2 - 100, Drawing.drawing.interfaceSizeY / 2, 75, 75, "Left", new Runnable()
	{
		@Override
		public void run()
		{
			mouseTankOrientation = 2;
		}
	}
	);
	public Button back8 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 75, 75, "Done", new Runnable()
	{
		@Override
		public void run()
		{
			rotateTankMenu = false;
		}
	}
	);
	public Button teamColorEnabled = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, 350, 40, "Team color: off", new Runnable()
	{
		@Override
		public void run()
		{
			selectedTeam.enableColor = !selectedTeam.enableColor;
			if (selectedTeam.enableColor)
				teamColorEnabled.text = "Team color: " + ScreenOptions.onText;
			else
				teamColorEnabled.text = "Team color: " + ScreenOptions.offText;
		}
	}
	);
	public Button placePlayer = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 - 180, 350, 40, "Player", new Runnable()
	{
		@Override
		public void run()
		{
			currentPlaceable = Placeable.playerTank;
			mouseTank = new TankPlayer(0, 0, 0);
		}
	}
	);
	public Button placeEnemy = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, 350, 40, "Tank", new Runnable()
	{
		@Override
		public void run()
		{
			currentPlaceable = Placeable.enemyTank;
			mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
		}
	}
	);
	public Button placeObstacle = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 - 180, 350, 40, "Block", new Runnable()
	{
		@Override
		public void run()
		{
			currentPlaceable = Placeable.obstacle;
		}
	}
	);

	Button pause = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
		}
	}
	);

	Button menu = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			objectMenu = true;
		}
	}
	);

	Button playMobile = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			play();
		}
	}
	);

	Button place = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			touchEraseMode = false;
		}
	}
	);

	Button erase = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			touchEraseMode = true;
		}
	}
	);


	public TextBox levelName;
	public TextBox sizeX;
	public TextBox sizeY;
	public TextBox colorRed;
	public TextBox colorGreen;
	public TextBox colorBlue;
	public TextBox colorVarRed;
	public TextBox colorVarGreen;
	public TextBox colorVarBlue;
	public TextBox teamName;
	public Button newTeam = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "New team", new Runnable()
	{
		@Override
		public void run()
		{
			Team t = new Team(System.currentTimeMillis() + "");
			teams.add(t);
			reload();
			((ScreenLevelBuilder)Game.screen).teamsMenu = true;
			((ScreenLevelBuilder)Game.screen).editTeamMenu = true;
			((ScreenLevelBuilder)Game.screen).teamName.inputText = t.name;
			((ScreenLevelBuilder)Game.screen).selectedTeam = ((ScreenLevelBuilder)Game.screen).teams.get(teams.size() - 1);
		}
	}
	);
	public TextBox teamRed;
	public TextBox teamGreen;
	public TextBox teamBlue;
	public Button teamColor = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Team color", new Runnable()
	{
		@Override
		public void run()
		{
			teamColorMenu = true;
			teamRed.inputText = (int) selectedTeam.teamColorR + "";
			teamGreen.inputText = (int) selectedTeam.teamColorG + "";
			teamBlue.inputText = (int) selectedTeam.teamColorB + "";

			if (selectedTeam.enableColor)
				teamColorEnabled.text = "Team color: " + ScreenOptions.onText;
			else
				teamColorEnabled.text = "Team color: " + ScreenOptions.offText;
		}
	}
	);
	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);
	public ScreenLevelBuilder(String lvlName)
	{
		this(lvlName, true);
	}

	public ScreenLevelBuilder(String lvlName, boolean reload)
	{
		if (!Game.game.window.touchscreen)
			this.screenHint = "Press space to access the object menu";

		this.reloadNewLevel = reload;

		for (int i = 0; i < drawables.length; i++)
		{
			drawables[i] = new ArrayList<IDrawable>();
		}

		Obstacle.draw_size = Obstacle.obstacle_size;

		Game.game.window.validScrollDown = false;
		Game.game.window.validScrollUp = false;

		this.name = lvlName;

		if (this.teams.size() == 0)
			mouseTank.team = null;
		else
			mouseTank.team = this.teams.get(teamNum);

		levelName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, 350, 40, "Level name", new Runnable()
		{
			@Override
			public void run()
			{
				BaseFile file = Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + lvlName);

				if (levelName.inputText.length() > 0 && !Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + levelName.inputText + ".tanks").exists())
				{
					if (file.exists())
					{
						file.renameTo(Game.homedir + ScreenSavedLevels.levelDir + "/" + levelName.inputText + ".tanks");
					}

					while (file.exists())
					{
						file.delete();
					}

					name = levelName.inputText + ".tanks";
				}
				else
				{
					levelName.inputText = name.split("\\.")[0];
				}

			}

		}
				, lvlName.split("\\.")[0]);

		levelName.enableSpaces = false;
		levelName.enableCaps = true;

		sizeX = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Width", new Runnable()
		{
			@Override
			public void run()
			{
				if (sizeX.inputText.length() <= 0)
					sizeX.inputText = width + "";
				else
					width = Integer.parseInt(sizeX.inputText);

				reload();
				((ScreenLevelBuilder)Game.screen).sizeMenu = true;
			}

		}
				, width + "");

		sizeX.allowLetters = false;
		sizeX.allowSpaces = false;
		sizeX.maxChars = 3;
		sizeX.maxValue = 200;
		sizeX.minValue = 1;
		sizeX.checkMaxValue = true;
		sizeX.checkMinValue = true;

		sizeY = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Height", new Runnable()
		{
			@Override
			public void run()
			{
				if (sizeY.inputText.length() <= 0)
					sizeY.inputText = height + "";
				else
					height = Integer.parseInt(sizeY.inputText);

				reload();
				((ScreenLevelBuilder)Game.screen).sizeMenu = true;
			}

		}
				, height + "");

		sizeY.allowLetters = false;
		sizeY.allowSpaces = false;
		sizeY.maxChars = 3;
		sizeY.maxValue = 200;
		sizeY.minValue = 1;
		sizeY.checkMaxValue = true;
		sizeY.checkMinValue = true;

		colorRed = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Red", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorRed.inputText.length() <= 0)
					colorRed.inputText = colorRed.previousInputText;

				r = Integer.parseInt(colorRed.inputText);

				colorVarRed.maxValue = 255 - r;
				colorVarRed.performValueCheck();

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, r + "");

		colorRed.allowLetters = false;
		colorRed.allowSpaces = false;
		colorRed.maxChars = 3;
		colorRed.maxValue = 255;
		colorRed.checkMaxValue = true;

		colorGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Green", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorGreen.inputText.length() <= 0)
					colorGreen.inputText = colorGreen.previousInputText;

				g = Integer.parseInt(colorGreen.inputText);

				colorVarGreen.maxValue = 255 - g;
				colorVarGreen.performValueCheck();

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, g + "");

		colorGreen.allowLetters = false;
		colorGreen.allowSpaces = false;
		colorGreen.maxChars = 3;
		colorGreen.maxValue = 255;
		colorGreen.checkMaxValue = true;

		colorBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorBlue.inputText.length() <= 0)
					colorBlue.inputText = colorBlue.previousInputText;

				b = Integer.parseInt(colorBlue.inputText);

				colorVarBlue.maxValue = 255 - b;
				colorVarBlue.performValueCheck();

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, b + "");

		colorBlue.allowLetters = false;
		colorBlue.allowSpaces = false;
		colorBlue.maxChars = 3;
		colorBlue.maxValue = 255;
		colorBlue.checkMaxValue = true;

		colorVarRed = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Red noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarRed.inputText.length() <= 0)
					colorVarRed.inputText = colorVarRed.previousInputText;

				dr = Integer.parseInt(colorVarRed.inputText);

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, dr + "");

		colorVarRed.allowLetters = false;
		colorVarRed.allowSpaces = false;
		colorVarRed.maxChars = 3;
		colorVarRed.checkMaxValue = true;

		colorVarGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Green noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarGreen.inputText.length() <= 0)
					colorVarGreen.inputText = colorVarGreen.previousInputText;

				dg = Integer.parseInt(colorVarGreen.inputText);

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, dg + "");

		colorVarGreen.allowLetters = false;
		colorVarGreen.allowSpaces = false;
		colorVarGreen.maxChars = 3;
		colorVarGreen.checkMaxValue = true;

		colorVarBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarBlue.inputText.length() <= 0)
					colorVarBlue.inputText = colorVarBlue.previousInputText;

				db = Integer.parseInt(colorVarBlue.inputText);

				reload(true);
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
				, db + "");

		colorVarBlue.allowLetters = false;
		colorVarBlue.allowSpaces = false;
		colorVarBlue.maxChars = 3;
		colorVarBlue.checkMaxValue = true;

		teamName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, 350, 40, "Team name", new Runnable()
		{
			@Override
			public void run()
			{
				boolean duplicate = false;

				for (int i = 0; i < teams.size(); i++)
				{
					if (teamName.inputText.equals(teams.get(i).name))
					{
						duplicate = true;
						break;
					}
				}

				if (teamName.inputText.length() <= 0 || duplicate)
					teamName.inputText = selectedTeam.name;
				else
				{
					selectedTeam.name = teamName.inputText;
					teamEditButtons.get(lastTeamButton).text = teamName.inputText;
				}
			}

		}
				, "");

		teamName.lowerCase = true;

		teamRed = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, 350, 40, "Red", new Runnable()
		{
			@Override
			public void run()
			{
				if (teamRed.inputText.length() <= 0)
					teamRed.inputText = "0";

				selectedTeam.teamColorR = Integer.parseInt(teamRed.inputText);
			}

		}
				, "");

		teamRed.allowLetters = false;
		teamRed.allowSpaces = false;
		teamRed.maxChars = 3;
		teamRed.maxValue = 255;
		teamRed.checkMaxValue = true;

		teamGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Green", new Runnable()
		{
			@Override
			public void run()
			{
				if (teamGreen.inputText.length() <= 0)
					teamGreen.inputText = "0";

				selectedTeam.teamColorG = Integer.parseInt(teamGreen.inputText);
			}

		}
				, "");

		teamGreen.allowLetters = false;
		teamGreen.allowSpaces = false;
		teamGreen.maxChars = 3;
		teamGreen.maxValue = 255;
		teamGreen.checkMaxValue = true;

		teamBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue", new Runnable()
		{
			@Override
			public void run()
			{
				if (teamBlue.inputText.length() <= 0)
					teamBlue.inputText = "0";

				selectedTeam.teamColorB = Integer.parseInt(teamBlue.inputText);
			}

		}
				, "");

		teamBlue.allowLetters = false;
		teamBlue.allowSpaces = false;
		teamBlue.maxChars = 3;
		teamBlue.maxValue = 255;
		teamBlue.checkMaxValue = true;

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + lvlName);

		if (!file.exists() && reloadNewLevel)
		{
			this.teams.add(new Team("ally"));
			this.teams.add(new Team("enemy"));

			TankSpawnMarker t = new TankSpawnMarker("player",Game.tank_size / 2, Game.tank_size / 2, 0);
			t.team = this.teams.get(0);
			Game.movables.add(t);
			this.spawns.add(t);

			this.reload();
			ScreenLevelBuilder s = (ScreenLevelBuilder) Game.screen;
		}

		for (int i = 0; i < Game.registryTank.tankEntries.size(); i++)
		{
			int rows = objectButtonRows;
			int cols = objectButtonCols;
			int index = i % (rows * cols);
			double x = Drawing.drawing.interfaceSizeX / 2 - 450 + 100 * (index % cols);
			double y = Drawing.drawing.interfaceSizeY / 2 - 100 + 100 * ((index / cols) % rows);

			final int j = i;

			Tank t = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);

			ButtonObject b = new ButtonObject(t, x, y, 75, 75, new Runnable()
			{
				@Override
				public void run()
				{
					tankNum = j;
					mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
				}
			}
					, t.description);

			if (t.description.equals(""))
				b.enableHover = false;

			this.tankButtons.add(b);
		}

		for (int i = 0; i < Game.registryObstacle.obstacleEntries.size(); i++)
		{
			int rows = objectButtonRows;
			int cols = objectButtonCols;
			int index = i % (rows * cols);
			double x = Drawing.drawing.interfaceSizeX / 2 - 450 + 100 * (index % cols);
			double y = Drawing.drawing.interfaceSizeY / 2 - 100 + 100 * ((index / cols) % rows);

			final int j = i;

			Obstacle o = Game.registryObstacle.obstacleEntries.get(i).getObstacle(x, y);
			ButtonObject b = new ButtonObject(o, x, y, 75, 75, new Runnable()
			{
				@Override
				public void run()
				{
					obstacleNum = j;
					mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
				}
			}
					, o.description);

			if (o.description.equals(""))
				b.enableHover = false;

			this.obstacleButtons.add(b);
		}
	}

	public void sortButtons()
	{
		int yoffset = -150;

		for (int i = 0; i < this.teamEditButtons.size(); i++)
		{
			int page = i / (teamPageRows * 3);
			int offset = 0;

			if (page * teamPageRows * 3 + teamPageRows < this.teamEditButtons.size())
				offset = -190;

			if (page * teamPageRows * 3 + teamPageRows * 2 < this.teamEditButtons.size())
				offset = -380;

			this.teamEditButtons.get(i).posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % teamPageRows) * 60;

			if (i / teamPageRows % 3 == 0)
				this.teamEditButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset;
			else if (i / teamPageRows % 3 == 1)
				this.teamEditButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380;
			else
				this.teamEditButtons.get(i).posX = Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2;

			this.teamSelectButtons.get(i).posX = this.teamEditButtons.get(i).posX;
			this.teamSelectButtons.get(i).posY = this.teamEditButtons.get(i).posY;
		}
	}

	@Override
	public void update()
	{
		if (Game.enable3d)
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.replaceTiles)
					o.postOverride();
			}

		clickCooldown = Math.max(0, clickCooldown - Panel.frameFrequency);

		if (this.paused)
		{
			if (this.confirmDeleteMenu)
			{
				this.cancelDelete.update();
				this.confirmDelete.update();
			}
			else if (this.objectMenu)
			{
				if (this.selectTeamMenu)
				{
					for (int i = teamPage * teamPageRows * 3; i < Math.min(teamPage * teamPageRows * 3 + teamPageRows * 3, teamSelectButtons.size()); i++)
					{
						teamSelectButtons.get(i).update();
						teamSelectButtons.get(i).enabled = true;
					}

					if (currentPlaceable == Placeable.playerTank)
						teamSelectButtons.get(playerTeamNum).enabled = false;
					else if (currentPlaceable == Placeable.enemyTank)
						teamSelectButtons.get(teamNum).enabled = false;

					if (teamSelectButtons.size() > (1 + teamPage) * teamPageRows * 3)
						this.nextTeamPage.update();

					if (teamPage > 0)
						this.previousTeamPage.update();

					this.back7.update();
				}
				else if (this.rotateTankMenu)
				{
					this.rotateUp.enabled = true;
					this.rotateDown.enabled = true;
					this.rotateLeft.enabled = true;
					this.rotateRight.enabled = true;

					if (this.mouseTankOrientation == 0)
						this.rotateRight.enabled = false;
					else if (this.mouseTankOrientation == 1)
						this.rotateDown.enabled = false;
					else if (this.mouseTankOrientation == 2)
						this.rotateLeft.enabled = false;
					else
						this.rotateUp.enabled = false;

					this.rotateUp.update();
					this.rotateLeft.update();
					this.rotateDown.update();
					this.rotateRight.update();

					this.back8.update();
				}
				else
				{
					this.placePlayer.enabled = (this.currentPlaceable != Placeable.playerTank);
					this.placeEnemy.enabled = (this.currentPlaceable != Placeable.enemyTank);
					this.placeObstacle.enabled = (this.currentPlaceable != Placeable.obstacle);

					this.exitObjectMenu.update();

					this.placePlayer.update();
					this.placeEnemy.update();
					this.placeObstacle.update();

					if (currentPlaceable == Placeable.enemyTank)
					{
						if (this.teamNum >= this.teams.size())
							this.teamNum = 0;

						this.selectTeam.text = "Team: " + this.teams.get(this.teamNum).name;
						this.selectTeam.update();
						this.rotateTankButton.update();
					}

					if (currentPlaceable == Placeable.playerTank)
					{
						if (this.playerTeamNum >= this.teams.size())
							this.playerTeamNum = 0;

						this.selectTeam.text = "Team: " + this.teams.get(this.playerTeamNum).name;
						this.selectTeam.update();
						this.rotateTankButton.update();
					}

					if (currentPlaceable == Placeable.playerTank)
					{
						this.playerSpawnsButton.enabled = this.movePlayer;
						this.movePlayerButton.enabled = !this.movePlayer;

						this.playerSpawnsButton.update();
						this.movePlayerButton.update();
					}
					else if (currentPlaceable == Placeable.enemyTank)
					{
						for (int i = 0; i < tankButtons.size(); i++)
						{
							tankButtons.get(i).enabled = tankNum != i;

							if (i / (objectButtonCols * objectButtonRows) == tankButtonPage)
								tankButtons.get(i).update();
						}

						if ((tankButtons.size() - 1) / (objectButtonRows * objectButtonCols) > tankButtonPage)
							nextTankPage.update();

						if (tankButtonPage > 0)
							previousTankPage.update();
					}
					else if (currentPlaceable == Placeable.obstacle)
					{
						for (int i = 0; i < obstacleButtons.size(); i++)
						{
							obstacleButtons.get(i).enabled = obstacleNum != i;

							if (i / (objectButtonCols * objectButtonRows) == obstacleButtonPage)
								obstacleButtons.get(i).update();
						}

						if ((obstacleButtons.size() - 1) / (objectButtonRows * objectButtonCols) > obstacleButtonPage)
							nextObstaclePage.update();

						if (obstacleButtonPage > 0)
							previousObstaclePage.update();
					}
				}
			}
			else if (!this.optionsMenu)
			{
				if (editable)
				{
					resume.update();
					options.update();
				}

				delete.update();
				quit.update();

				if (spawns.size() > 0)
					play.update();
				else
					playUnavailable.update();
			}
			else
			{
				if (this.sizeMenu)
				{
					this.sizeX.update();
					this.sizeY.update();
					this.back3.update();
				}
				else if (this.colorMenu)
				{
					this.colorRed.update();
					this.colorGreen.update();
					this.colorBlue.update();
					this.colorVarRed.update();
					this.colorVarGreen.update();
					this.colorVarBlue.update();
					this.back2.update();
				}
				else if (this.teamsMenu)
				{
					if (this.editTeamMenu)
					{
						if (this.teamColorMenu)
						{
							back6.update();
							if (selectedTeam.enableColor)
							{
								teamRed.update();
								teamGreen.update();
								teamBlue.update();
							}

							teamColorEnabled.update();
						}
						else
						{
							deleteTeam.update();
							teamName.update();
							teamFriendlyFire.update();
							teamColor.update();
							back5.update();
						}
					}
					else
					{
						for (int i = teamPage * teamPageRows * 3; i < Math.min(teamPage * teamPageRows * 3 + teamPageRows * 3, teamEditButtons.size()); i++)
						{
							teamEditButtons.get(i).update();
						}

						back4.update();
						newTeam.update();

						if (teamPage > 0)
							previousTeamPage.update();

						if (teamEditButtons.size() > (1 + teamPage) * teamPageRows * 3)
							nextTeamPage.update();
					}
				}
				else
				{
					this.levelName.update();
					this.back1.update();
					this.colorOptions.update();
					this.sizeOptions.update();
					this.teamsOptions.update();
				}
			}
		}
		else if (Game.game.window.touchscreen)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100;
			else
				hStep = 100;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			pause.update();

			menu.posX = pause.posX - hStep;
			menu.posY = pause.posY + vStep;
			menu.update();

			playMobile.posX = pause.posX - hStep * 2;
			playMobile.posY = pause.posY + vStep * 2;
			playMobile.update();

			if (touchEraseMode)
			{
				place.enabled = true;
				erase.enabled = false;
			}
			else
			{
				place.enabled = false;
				erase.enabled = true;
			}

			place.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50;
			place.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50;
			place.update();

			erase.posX = place.posX + hStep;
			erase.posY = place.posY + vStep;
			erase.update();
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE) && !paused)
		{
			this.paused = true;
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE) && editable)
		{
			if (confirmDeleteMenu)
				confirmDeleteMenu = false;
			else if (colorMenu || sizeMenu)
			{
				sizeMenu = false;
				colorMenu = false;
			}
			else if (teamsMenu)
			{
				if (editTeamMenu)
				{
					if (teamColorMenu)
						teamColorMenu = false;
					else
						editTeamMenu = false;
				}
				else
					teamsMenu = false;
			}
			else if (optionsMenu)
				optionsMenu = false;
			else
			{
				if (this.rotateTankMenu)
					this.rotateTankMenu = false;
				else if (this.editTeamMenu)
					this.editTeamMenu = false;
				else
					this.objectMenu = false;

				this.paused = false;
			}

			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_SPACE) && editable && (!paused || objectMenu))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_SPACE);
			this.paused = !this.paused;
			this.objectMenu = !this.objectMenu;
			this.selectTeamMenu = false;
			this.rotateTankMenu = false;
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).update();
		}

		if (this.paused)
			return;

		boolean up = false;
		boolean down = false;

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_DOWN))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_DOWN);
			down = true;
		}
		else if (Game.game.window.validScrollDown)
		{
			Game.game.window.validScrollDown = false;
			down = true;
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_UP))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_UP);
			up = true;
		}
		else if (Game.game.window.validScrollUp)
		{
			Game.game.window.validScrollUp = false;
			up = true;
		}

		if (down && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = (tankNum + 1) % Game.registryTank.tankEntries.size();
			Tank t = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}
		else if (down && currentPlaceable == Placeable.obstacle)
		{
			obstacleNum = (obstacleNum + 1) % Game.registryObstacle.obstacleEntries.size();
			mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
		}

		if (up && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size()) % Game.registryTank.tankEntries.size();
			Tank t = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}
		else if (up && currentPlaceable == Placeable.obstacle)
		{
			obstacleNum = ((obstacleNum - 1) + Game.registryObstacle.obstacleEntries.size()) % Game.registryObstacle.obstacleEntries.size();
			mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
		}

		if ((up || down) && !(up && down))
			this.movePlayer = !this.movePlayer;

		boolean right = false;
		boolean left = false;

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_RIGHT))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_RIGHT);
			right = true;
		}
		else if (Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_5))
		{
			Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_5);
			right = true;
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_LEFT))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_LEFT);
			left = true;
		}
		else if (Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_4))
		{
			Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_4);
			left = true;
		}

		if (right)
		{
			if (currentPlaceable == Placeable.enemyTank)
			{
				currentPlaceable = Placeable.obstacle;
			}
			else if (currentPlaceable == Placeable.obstacle)
			{
				currentPlaceable = Placeable.playerTank;
				mouseTank = new TankPlayer(0, 0, 0);
			}
			else if (currentPlaceable == Placeable.playerTank)
			{
				currentPlaceable = Placeable.enemyTank;
				mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			}
		}

		if (left)
		{
			if (currentPlaceable == Placeable.playerTank)
			{
				currentPlaceable = Placeable.obstacle;
			}
			else if (currentPlaceable == Placeable.obstacle)
			{
				currentPlaceable = Placeable.enemyTank;
				mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			}
			else if (currentPlaceable == Placeable.enemyTank)
			{
				currentPlaceable = Placeable.playerTank;
				mouseTank = new TankPlayer(0, 0, 0);
			}
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_MINUS))
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
				((ScreenLevelBuilder) Game.screen).optionsMenu = false;
				((ScreenLevelBuilder) Game.screen).paused = false;
			}
			else
			{
				Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_MINUS);

				if (currentPlaceable == Placeable.enemyTank)
					teamNum = (teamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1);
				else if (currentPlaceable == Placeable.playerTank)
					playerTeamNum = (playerTeamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1);
			}
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_EQUAL))
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
				((ScreenLevelBuilder) Game.screen).optionsMenu = false;
				((ScreenLevelBuilder) Game.screen).paused = false;
			}
			else
			{
				Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_EQUAL);

				if (currentPlaceable == Placeable.enemyTank)
					teamNum = (teamNum + 1) % (this.teams.size() + 1);
				else if (currentPlaceable == Placeable.playerTank)
					playerTeamNum = (playerTeamNum + 1) % (this.teams.size() + 1);
			}
		}

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getMouseX();
			double my = Drawing.drawing.getMouseY();

			boolean[] handled = checkMouse(mx, my,
					Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1),
					Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_2),
					Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1),
					Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_2));

			if (handled[0])
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);

			if (handled[1])
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_2);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.equals("") || p.tag.equals("levelbuilder"))
				{
					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.toGameCoordsY(Drawing.drawing.getInterfacePointerY(p.y));

					boolean[] handled = checkMouse(mx, my, true, false, !p.tag.equals("levelbuilder"), false);

					if (handled[0])
						p.tag = "levelbuilder";
				}
			}
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER) && this.spawns.size() > 0)
		{
			this.play();
		}

		if (mouseTank != null)
		{
			mouseTank.angle = Math.PI * this.mouseTankOrientation * 0.5;
		}

		Game.effects.removeAll(Game.removeEffects);
		Game.removeEffects.clear();

		Game.movables.removeAll(Game.removeMovables);
		Game.removeMovables.clear();

		Game.obstacles.removeAll(Game.removeObstacles);
		Game.removeObstacles.clear();
	}

	//Make the mx and my be game coords, not interface coords
	public boolean[] checkMouse(double mx, double my, boolean left, boolean right, boolean validLeft, boolean validRight)
	{
		boolean[] handled = new boolean[]{false, false};
		double posX = Math.round(mx / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;
		double posY = Math.round(my / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2;
		mouseTank.posX = posX;
		mouseTank.posY = posY;
		mouseObstacle.posX = posX;
		mouseObstacle.posY = posY;

		if (currentPlaceable == Placeable.enemyTank)
		{
			if (this.teamNum == this.teams.size())
				mouseTank.team = null;
			else
				mouseTank.team = this.teams.get(teamNum);
		}
		else if (currentPlaceable == Placeable.playerTank)
		{
			if (this.playerTeamNum == this.teams.size())
				mouseTank.team = null;
			else
				mouseTank.team = this.teams.get(playerTeamNum);
		}

		if (mouseTank.posX > 0 && mouseTank.posY > 0 && mouseTank.posX < Game.tank_size * Game.currentSizeX && mouseTank.posY < Game.tank_size * Game.currentSizeX)
		{
			if (right || (Game.game.window.touchscreen && touchEraseMode && left))
			{
				boolean skip = false;

				if (validRight || (Game.game.window.touchscreen && touchEraseMode && validLeft))
				{
					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);
						if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && !(this.spawns.contains(m) && this.spawns.size() <= 1))
						{
							skip = true;
							Game.removeMovables.add(m);
							Drawing.drawing.playVibration("click");

							this.spawns.remove(m);

							for (int z = 0; z < 100; z++)
							{
								Effect e = Effect.createNewEffect(m.posX, m.posY, ((Tank) m).size / 2, Effect.EffectType.piece);
								double var = 50;
								e.colR = Math.min(255, Math.max(0, ((Tank) m).colorR + Math.random() * var - var / 2));
								e.colG = Math.min(255, Math.max(0, ((Tank) m).colorG + Math.random() * var - var / 2));
								e.colB = Math.min(255, Math.max(0, ((Tank) m).colorB + Math.random() * var - var / 2));

								if (Game.enable3d)
									e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * 2);
								else
									e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2);

								e.maxAge /= 2;
								Game.effects.add(e);
							}

							break;
						}
					}
				}

				for (int i = 0; i < Game.obstacles.size(); i++)
				{
					Obstacle m = Game.obstacles.get(i);
					if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
					{
						skip = true;
						Game.removeObstacles.add(m);
						Drawing.drawing.playVibration("click");
						break;
					}
				}

				if (!Game.game.window.touchscreen && !skip && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && validRight)
				{
					this.mouseTankOrientation += 1;
					this.mouseTankOrientation = this.mouseTankOrientation % 4;
				}

				if (Game.game.window.touchscreen)
					handled[0] = true;

				handled[1] = true;
			}

			if (!touchEraseMode && clickCooldown <= 0 && (validLeft || (left && currentPlaceable == Placeable.obstacle && this.mouseObstacle.draggable)))
			{
				boolean skip = false;

				if ((mouseObstacle.tankCollision || currentPlaceable != Placeable.obstacle))
				{
					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);
						if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
						{
							if ((m instanceof TankSpawnMarker && this.spawns.size() <= 1) || !validLeft)
								skip = true;
							else
								Game.removeMovables.add(m);

							break;
						}
					}
				}

				for (int i = 0; i < Game.obstacles.size(); i++)
				{
					Obstacle m = Game.obstacles.get(i);
					if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
					{
						if (!validRight)
							skip = true;
						else
							Game.removeObstacles.add(m);

						break;
					}
				}


				if (!skip)
				{
					if (currentPlaceable == Placeable.enemyTank)
					{
						Tank t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
						t.team = mouseTank.team;
						Game.movables.add(t);
						Drawing.drawing.playVibration("click");
					}
					else if (currentPlaceable == Placeable.playerTank)
					{
						if (this.movePlayer)
						{
							for (int i = 0; i < Game.movables.size(); i++)
							{
								Movable m = Game.movables.get(i);

								if (m instanceof TankSpawnMarker)
									Game.removeMovables.add(m);
							}

							this.spawns.clear();
						}

						TankSpawnMarker t = new TankSpawnMarker("player", mouseTank.posX, mouseTank.posY, mouseTank.angle);
						t.team = mouseTank.team;
						this.spawns.add(t);
						Game.movables.add(t);
						Drawing.drawing.playVibration("click");

						if (this.movePlayer)
							t.drawAge = 50;
					}
					else if (currentPlaceable == Placeable.obstacle)
					{
						Obstacle o = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
						o.colorR = mouseObstacle.colorR;
						o.colorG = mouseObstacle.colorG;
						o.colorB = mouseObstacle.colorB;
						o.posX = mouseObstacle.posX;
						o.posY = mouseObstacle.posY;
						mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(o.posX, o.posY);
						Game.obstacles.add(o);
						Drawing.drawing.playVibration("click");
					}
				}

				handled[0] = true;
				handled[1] = true;
			}
		}

		return handled;
	}

	public void save()
	{
		StringBuilder level = new StringBuilder("{");

		if (!this.editable)
			level.append("*");

		level.append(width).append(",").append(height).append(",").append(r).append(",").append(g).append(",").append(b).append(",").append(dr).append(",").append(dg).append(",").append(db).append("|");

		ArrayList<Obstacle> unmarked = (ArrayList<Obstacle>) Game.obstacles.clone();
		boolean[][][] obstacles = new boolean[Game.registryObstacle.obstacleEntries.size()][width][height];

		for (int h = 0; h < Game.registryObstacle.obstacleEntries.size(); h++)
		{
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);
				int x = (int) (o.posX / Game.tank_size);
				int y = (int) (o.posY / Game.tank_size);

				if (x < obstacles[h].length && x >= 0 && y < obstacles[h][0].length && y >= 0 && o.name.equals(Game.registryObstacle.getEntry(h).name))
				{
					obstacles[h][x][y] = true;
					unmarked.remove(o);
				}

				//level += x + "-" + y + ",";
			}

			//compression
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					if (obstacles[h][i][j])
					{
						int xLength = 0;

						while (true)
						{
							xLength += 1;

							if (i + xLength >= obstacles[h].length)
								break;
							else if (!obstacles[h][i + xLength][j])
								break;
						}


						int yLength = 0;

						while (true)
						{
							yLength += 1;

							if (j + yLength >= obstacles[h][0].length)
								break;
							else if (!obstacles[h][i][j + yLength])
								break;
						}

						String name = "";
						String obsName = Game.registryObstacle.obstacleEntries.get(h).name;

						if (!obsName.equals("normal"))
							name = "-" + obsName;

						if (xLength >= yLength)
						{
							if (xLength == 1)
								level.append(i).append("-").append(j).append(name).append(",");
							else
								level.append(i).append("...").append(i + xLength - 1).append("-").append(j).append(name).append(",");

							for (int z = 0; z < xLength; z++)
							{
								obstacles[h][i + z][j] = false;
							}
						}
						else
						{
							level.append(i).append("-").append(j).append("...").append(j + yLength - 1).append(name).append(",");

							for (int z = 0; z < yLength; z++)
							{
								obstacles[h][i][j + z] = false;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < unmarked.size(); i++)
		{
			level.append((int)(unmarked.get(i).posX / Obstacle.obstacle_size)).append("-").append((int)(unmarked.get(i).posY / Obstacle.obstacle_size));
			level.append("-").append(unmarked.get(i).name).append(",");
		}

		if (level.charAt(level.length() - 1) == ',')
		{
			level = new StringBuilder(level.substring(0, level.length() - 1));
		}

		level.append("|");

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Tank)
			{
				Tank t = (Tank)Game.movables.get(i);
				int x = (int) (t.posX / Game.tank_size);
				int y = (int) (t.posY / Game.tank_size);
				int angle = (int) (t.angle * 2 / Math.PI);

				level.append(x).append("-").append(y).append("-").append(t.name).append("-").append(angle);

				if (t.team != null)
					level.append("-").append(t.team.name);

				level.append(",");
			}
		}

		if (Game.movables.size() == 0)
		{
			level.append("|");
		}

		level = new StringBuilder(level.substring(0, level.length() - 1));

		level.append("|");

		for (int i = 0; i < teams.size(); i++)
		{
			Team t = teams.get(i);
			level.append(t.name).append("-").append(t.friendlyFire);
			if (t.enableColor)
				level.append("-").append(t.teamColorR).append("-").append(t.teamColorG).append("-").append(t.teamColorB);

			level.append(",");
		}

		level = new StringBuilder(level.substring(0, level.length() - 1));

		level.append("}");

		Game.currentLevelString = level.toString();

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + name);
		if (file.exists())
		{
			if (!editable)
			{
				return;
			}

			file.delete();
		}

		try
		{
			file.create();

			file.startWriting();
			file.println(level.toString());
			file.stopWriting();
		}
		catch (IOException e)
		{
			Game.exitToCrash(e);
		}

	}

	public void reload()
	{
		reload(false);
	}

	public void reload(boolean colorChange)
	{
		save();

		int sX = Game.currentSizeX;
		int sY = Game.currentSizeY;

		double[][] r = Game.tilesR.clone();
		double[][] g = Game.tilesG.clone();
		double[][] b = Game.tilesB.clone();
		double[][] h = Game.tilesDepth.clone();

		Game.movables.clear();
		Game.obstacles.clear();

		ScreenLevelBuilder s = new ScreenLevelBuilder(name);
		Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);

		s.optionsMenu = true;
		s.tankNum = tankNum;
		s.obstacleNum = obstacleNum;
		s.teamNum = teamNum;
		s.mouseTank = mouseTank;
		s.mouseObstacle = mouseObstacle;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank)
				((Tank) m).drawAge = Game.tank_size;
		}

		if (!colorChange && sX == Game.currentSizeX && sY == Game.currentSizeY)
		{
			Game.tilesR = r;
			Game.tilesG = g;
			Game.tilesB = b;
			Game.tilesDepth = h;
		}

		Game.screen = s;
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		if (!paused && !Game.game.window.touchscreen)
		{
			if (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)
			{
				mouseTank.drawOutline();
				mouseTank.drawTeam();

				if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
				{
					Drawing.drawing.setColor(255, 255, 255, 127);
					Drawing.drawing.drawImage("/player_spawn.png", mouseTank.posX, mouseTank.posY, mouseTank.size, mouseTank.size);
				}
			}
			else if (currentPlaceable == Placeable.obstacle)
				mouseObstacle.drawOutline();
		}

		for (Effect e: Game.belowEffects)
			drawables[0].add(e);

		for (Movable m: Game.movables)
			drawables[m.drawLevel].add(m);

		for (Obstacle o: Game.obstacles)
			drawables[o.drawLevel].add(o);

		for (Effect e: Game.effects)
			drawables[7].add(e);

		for (int i = 0; i < this.drawables.length; i++)
		{
			if (i == 5 && Game.enable3d)
			{
				Drawing drawing = Drawing.drawing;
				Drawing.drawing.setColor(174, 92, 16);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Obstacle.obstacle_size / 2, 0, drawing.sizeX + Obstacle.obstacle_size * 2, Obstacle.obstacle_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Obstacle.obstacle_size / 2, 0, drawing.sizeX + Obstacle.obstacle_size * 2, Obstacle.obstacle_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Obstacle.obstacle_size / 2, drawing.sizeY / 2, 0, Obstacle.obstacle_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Obstacle.obstacle_size / 2, drawing.sizeY / 2, 0, Obstacle.obstacle_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			for (IDrawable d: this.drawables[i])
			{
				d.draw();

				if (d instanceof Movable)
					((Movable) d).drawTeam();
			}

			drawables[i].clear();
		}

		if (this.paused)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (this.confirmDeleteMenu)
			{
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.setInterfaceFontSize(24);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Are you sure you want to delete the level?");

				this.cancelDelete.draw();
				this.confirmDelete.draw();
			}
			else if (this.objectMenu)
			{
				if (this.selectTeamMenu)
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.setInterfaceFontSize(24);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.teamSelectTitle);
					for (int i = teamPage * teamPageRows * 3; i < Math.min(teamPage * teamPageRows * 3 + teamPageRows * 3, teamEditButtons.size()); i++)
					{
						teamSelectButtons.get(i).draw();
					}

					if (teamSelectButtons.size() > (1 + teamPage) * teamPageRows * 3)
						this.nextTeamPage.update();

					if (teamPage > 0)
						this.previousTeamPage.update();

					back7.draw();
				}
				else if (this.rotateTankMenu)
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.setInterfaceFontSize(24);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Select tank orientation");

					this.rotateUp.draw();
					this.rotateLeft.draw();
					this.rotateDown.draw();
					this.rotateRight.draw();

					this.back8.draw();
				}
				else
				{
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.setInterfaceFontSize(24);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 240, "Object menu");

					this.placePlayer.draw();
					this.placeEnemy.draw();
					this.placeObstacle.draw();

					if (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)
					{
						this.selectTeam.draw();
						this.rotateTankButton.draw();
					}

					this.exitObjectMenu.draw();

					if (currentPlaceable == Placeable.playerTank)
					{
						this.playerSpawnsButton.draw();
						this.movePlayerButton.draw();

						if (this.movePlayer)
							this.drawMobileTooltip(this.movePlayerButton.hoverTextRaw);
						else
							this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRaw);

					}
					else if (currentPlaceable == Placeable.enemyTank)
					{
						for (int i = tankButtons.size() - 1; i >= 0; i--)
						{
							if (i / (objectButtonCols * objectButtonRows) == tankButtonPage)
								tankButtons.get(i).draw();
						}

						if ((tankButtons.size() - 1) / (objectButtonRows * objectButtonCols) > tankButtonPage)
							nextTankPage.draw();

						if (tankButtonPage > 0)
							previousTankPage.draw();

						this.drawMobileTooltip(this.tankButtons.get(this.tankNum).hoverTextRaw);
					}
					else if (currentPlaceable == Placeable.obstacle)
					{
						for (int i = obstacleButtons.size() - 1; i >= 0; i--)
						{
							if (i / (objectButtonCols * objectButtonRows) == obstacleButtonPage)
								obstacleButtons.get(i).draw();
						}

						if ((obstacleButtons.size() - 1) / (objectButtonRows * objectButtonCols) > obstacleButtonPage)
							nextObstaclePage.draw();

						if (obstacleButtonPage > 0)
							previousObstaclePage.draw();

						this.drawMobileTooltip(this.obstacleButtons.get(this.obstacleNum).hoverTextRaw);
					}
				}
			}
			else if (!this.optionsMenu)
			{
				if (editable)
				{
					resume.draw();
					options.draw();
				}

				delete.draw();
				quit.draw();

				if (spawns.size() > 0)
					play.draw();
				else
					playUnavailable.draw();

				Drawing.drawing.setInterfaceFontSize(24);
				Drawing.drawing.setColor(0, 0, 0);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Level menu");
			}
			else
			{
				if (this.sizeMenu)
				{
					this.sizeX.draw();
					this.sizeY.draw();
					this.back3.draw();
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Level size");
				}
				else if (this.colorMenu)
				{
					this.colorRed.draw();
					this.colorGreen.draw();
					this.colorBlue.draw();
					this.colorVarRed.draw();
					this.colorVarGreen.draw();
					this.colorVarBlue.draw();
					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Background colors");
					this.back2.draw();
				}
				else if (this.teamsMenu)
				{
					if (this.editTeamMenu)
					{
						if (this.teamColorMenu)
						{
							back6.draw();
							if (selectedTeam.enableColor)
							{
								teamRed.draw();
								teamGreen.draw();
								teamBlue.draw();
							}
							teamColorEnabled.draw();
							Drawing.drawing.setColor(0, 0, 0);
							Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Team color: " + this.selectedTeam.name);
						}
						else
						{
							back5.draw();
							teamName.draw();
							teamColor.draw();
							deleteTeam.draw();
							teamFriendlyFire.draw();
							Drawing.drawing.setColor(0, 0, 0);
							Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.selectedTeam.name);
						}
					}
					else
					{
						for (int i = teamPage * teamPageRows * 3; i < Math.min(teamPage * teamPageRows * 3 + teamPageRows * 3, teamEditButtons.size()); i++)
						{
							teamEditButtons.get(i).draw();
						}

						back4.draw();
						newTeam.draw();

						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Teams");

						if (teamPage > 0)
							previousTeamPage.draw();

						if (teamEditButtons.size() > (1 + teamPage) * teamPageRows * 3)
							nextTeamPage.draw();
					}
				}
				else
				{
					this.levelName.draw();
					this.back1.draw();
					this.colorOptions.draw();
					this.sizeOptions.draw();
					this.teamsOptions.draw();

					Drawing.drawing.setColor(0, 0, 0);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Level options");
				}
			}
		}

		if (!paused && Game.game.window.touchscreen)
		{
			pause.draw();
			Drawing.drawing.drawInterfaceImage("/pause.png", pause.posX, pause.posY, 40, 40);

			menu.draw();
			Drawing.drawing.drawInterfaceImage("/menu.png", menu.posX, menu.posY, 30, 30);

			playMobile.draw();
			Drawing.drawing.drawInterfaceImage("/play.png", playMobile.posX, playMobile.posY, 30, 30);

			place.draw();
			Drawing.drawing.drawInterfaceImage("/pencil.png", place.posX, place.posY, 40, 40);

			erase.draw();
			Drawing.drawing.drawInterfaceImage("/eraser.png", erase.posX, erase.posY, 40, 40);
		}
	}

	public void play()
	{
		this.save();
		this.replaceSpawns();
		Game.screen = new ScreenGame(this.name);
	}

	public void replaceSpawns()
	{
		int playerCount = 1;
		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			playerCount += ScreenPartyHost.server.connections.size();

		ArrayList<Integer> availablePlayerSpawns = new ArrayList<Integer>();
		ArrayList<INetworkEvent> playerEvents = new ArrayList<INetworkEvent>();

		for (int i = 0; i < playerCount; i++)
		{
			if (availablePlayerSpawns.size() == 0)
			{
				for (int j = 0; j < this.spawns.size(); j++)
				{
					availablePlayerSpawns.add(j);
				}
			}

			int spawn = availablePlayerSpawns.remove((int) (Math.random() * availablePlayerSpawns.size()));

			double x = this.spawns.get(spawn).posX;
			double y = this.spawns.get(spawn).posY;
			double angle = this.spawns.get(spawn).angle;
			Team team = this.spawns.get(spawn).team;

			if (ScreenPartyHost.isServer)
			{
				EventCreatePlayer e = new EventCreatePlayer(Game.players.get(i), x, y, angle, team);
				playerEvents.add(e);
				Game.eventsOut.add(e);
			}
			else if (!ScreenPartyLobby.isClient)
			{
				TankPlayer tank = new TankPlayer(x, y, angle);

				if (spawns.size() <= 1)
					tank.drawAge = Game.tank_size;

				Game.playerTank = tank;
				tank.team = team;
				Game.movables.add(tank);
			}
		}

		for (INetworkEvent e: playerEvents)
		{
			e.execute();
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof TankSpawnMarker)
				Game.removeMovables.add(m);
		}
	}

	public void setEditorTeam(int i)
	{
		if (this.currentPlaceable == Placeable.playerTank)
			this.playerTeamNum = i;
		else
			this.teamNum = i;
	}

	@Override
	public ArrayList<TankSpawnMarker> getSpawns()
	{
		return this.spawns;
	}

	public void drawMobileTooltip(String text)
	{
		if (!Game.game.window.touchscreen)
			return;

		Drawing.drawing.setColor(0, 0, 0, 127);
		Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, 1120, 60);

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.setColor(255, 255, 255);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, text.replace("---", " "));
	}

	public enum Placeable {enemyTank, playerTank, obstacle}
}

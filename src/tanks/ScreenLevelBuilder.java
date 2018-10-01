package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import tanks.tank.Tank;
import tanks.tank.TankPlayer;

public class ScreenLevelBuilder extends Screen
{
	enum Placeable {enemyTank, playerTank, obstacle}

	Placeable currentPlaceable = Placeable.enemyTank;
	int tankNum = 0;
	int obstacleNum = 0;
	int teamNum = 1;
	int playerTeamNum = 0;

	boolean reloadNewLevel = true;


	Tank mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	boolean paused = true;
	boolean optionsMenu = false;
	boolean sizeMenu = false;
	boolean colorMenu = false;
	boolean teamsMenu = false;
	boolean editTeamMenu = false;
	boolean teamColorMenu = false;
	Team selectedTeam;

	int rows = 6;
	int page = 0;

	public boolean editable = true; 

	ArrayList<Team> teams = new ArrayList<Team>();
	ArrayList<Button> teamButtons = new ArrayList<Button>();
	int lastTeamButton;

	int r = 235;
	int g = 207;
	int b = 166;

	int dr = 20;
	int dg = 20;
	int db = 20;

	int width = Game.currentSizeX;
	int height = Game.currentSizeY;

	String name;

	Button resume = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 30, 350, 40, "Edit", new Runnable()
	{
		@Override
		public void run() 
		{
			paused = false;
		}
	}
			);

	Button play = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 - 90), 350, 40, "Play", new Runnable()
	{
		@Override
		public void run() 
		{
			save(true);

			Game.screen = new ScreenGame(name);
		}
	}
			);

	Button options = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 30), 350, 40, "Options", new Runnable()
	{
		@Override
		public void run() 
		{
			optionsMenu = true;
		}
	}
			);

	Button colorOptions = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 - 60), 350, 40, "Background colors", new Runnable()
	{
		@Override
		public void run() 
		{
			colorMenu = true;
		}
	}
			);

	Button sizeOptions = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2), 350, 40, "Level size", new Runnable()
	{
		@Override
		public void run() 
		{
			sizeMenu = true;
		}
	}
			);

	Button teamsOptions = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 60), 350, 40, "Teams", new Runnable()
	{
		@Override
		public void run() 
		{
			if (teams.size() != teamButtons.size())
			{
				reload();
				((ScreenLevelBuilder)Game.screen).teamsMenu = true;
			}

			teamsMenu = true;
		}
	}
			);

	Button back1 = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 120), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			optionsMenu = false;
		}
	}
			);

	Button back2 = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 180), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			colorMenu = false;
		}
	}
			);

	Button back3 = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 120), 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			sizeMenu = false;
		}
	}
			);

	Button back4 = new Button(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			teamsMenu = false;
		}
	}
			);

	Button back5 = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
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

	Button back6 = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", new Runnable()
	{
		@Override
		public void run() 
		{
			teamColorMenu = false;
		}
	}
			);

	Button quit = new Button(Drawing.interfaceSizeX / 2, (int) (Drawing.interfaceSizeY / 2 + 90), 350, 40, "Exit", new Runnable()
	{
		@Override
		public void run() 
		{
			save();

			Game.exitToTitle();
			Game.screen = new ScreenSavedLevels();
		}
	}
			);

	Button next = new Button(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", new Runnable()
	{
		@Override
		public void run() 
		{
			page++;
		}
	}
			);

	Button previous = new Button(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", new Runnable()
	{
		@Override
		public void run() 
		{
			page--;
		}
	}
			);

	Button deleteTeam = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 240, 350, 40, "Delete team", new Runnable()
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

	Button teamFriendlyFire = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Friendly fire: on", new Runnable()
	{
		@Override
		public void run() 
		{
			selectedTeam.friendlyFire = !selectedTeam.friendlyFire;
			if (selectedTeam.friendlyFire)
				teamFriendlyFire.text = "Friendly fire: on";
			else
				teamFriendlyFire.text = "Friendly fire: off";
		}
	}
			);

	Button teamColor = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2, 350, 40, "Team color", new Runnable()
	{
		@Override
		public void run() 
		{
			teamColorMenu = true;
			teamRed.inputText = selectedTeam.teamColor.getRed() + "";
			teamGreen.inputText = selectedTeam.teamColor.getGreen() + "";
			teamBlue.inputText = selectedTeam.teamColor.getBlue() + "";

			if (selectedTeam.enableColor)
				teamColorEnabled.text = "Team color: on";
			else
				teamColorEnabled.text = "Team color: off";
		}
	}
			);

	Button newTeam = new Button(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 300, 350, 40, "New team", new Runnable()
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

	Button teamColorEnabled = new Button(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 180, 350, 40, "Team color: off", new Runnable()
	{
		@Override
		public void run() 
		{
			selectedTeam.enableColor = !selectedTeam.enableColor;
			if (selectedTeam.enableColor)
				teamColorEnabled.text = "Team color: on";
			else
				teamColorEnabled.text = "Team color: off";
		}
	}
			);

	TextBox levelName;
	TextBox sizeX;
	TextBox sizeY;
	TextBox colorRed;
	TextBox colorGreen;
	TextBox colorBlue;
	TextBox colorVarRed;
	TextBox colorVarGreen;
	TextBox colorVarBlue;
	TextBox teamName;

	TextBox teamRed;
	TextBox teamGreen;
	TextBox teamBlue;

	public ScreenLevelBuilder(String lvlName)
	{
		this(lvlName, true);
	}

	public ScreenLevelBuilder(String lvlName, boolean reload)
	{		
		this.reloadNewLevel = reload;

		Obstacle.draw_size = Obstacle.obstacle_size;

		InputScroll.validScrollDown = false;
		InputScroll.validScrollUp = false;

		this.name = lvlName;

		if (this.teams.size() == 0)
			mouseTank.team = null;
		else
			mouseTank.team = this.teams.get(teamNum);

		levelName = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 120, 350, 40, "Level name", new Runnable()
		{
			@Override
			public void run() 
			{
				File file = new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + lvlName);

				if (levelName.inputText.length() > 0 && !new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + levelName.inputText + ".tanks").exists())
				{
					if (file.exists())
						file.renameTo(new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + levelName.inputText + ".tanks"));

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

		sizeX = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Width", new Runnable()
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
		sizeX.checkMaxValue = true;

		sizeY = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Height", new Runnable()
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
		sizeY.checkMaxValue = true;

		colorRed = new TextBox(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Red", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorRed.inputText.length() <= 0)
					colorRed.inputText = "0";

				r = Integer.parseInt(colorRed.inputText);

				colorVarRed.maxValue = 255 - r;
				colorVarRed.performMaxValueCheck();

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, r + "");

		colorRed.allowLetters = false;
		colorRed.allowSpaces = false;
		colorRed.maxChars = 3;
		colorRed.maxValue = 255;
		colorRed.checkMaxValue = true;

		colorGreen = new TextBox(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Green", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorGreen.inputText.length() <= 0)
					colorGreen.inputText = "0";

				g = Integer.parseInt(colorGreen.inputText);

				colorVarGreen.maxValue = 255 - g;
				colorVarGreen.performMaxValueCheck();

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, g + "");

		colorGreen.allowLetters = false;
		colorGreen.allowSpaces = false;
		colorGreen.maxChars = 3;
		colorGreen.maxValue = 255;
		colorGreen.checkMaxValue = true;

		colorBlue = new TextBox(Drawing.interfaceSizeX / 2 - 190, Drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorBlue.inputText.length() <= 0)
					colorBlue.inputText = "0";

				b = Integer.parseInt(colorBlue.inputText);

				colorVarBlue.maxValue = 255 - b;
				colorVarBlue.performMaxValueCheck();

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, b + "");

		colorBlue.allowLetters = false;
		colorBlue.allowSpaces = false;
		colorBlue.maxChars = 3;
		colorBlue.maxValue = 255;
		colorBlue.checkMaxValue = true;

		colorVarRed = new TextBox(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Red Noise", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorVarRed.inputText.length() <= 0)
					colorVarRed.inputText = "0";

				dr = Integer.parseInt(colorVarRed.inputText);

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, dr + "");

		colorVarRed.allowLetters = false;
		colorVarRed.allowSpaces = false;
		colorVarRed.maxChars = 3;
		colorVarRed.checkMaxValue = true;

		colorVarGreen = new TextBox(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Green Noise", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorVarGreen.inputText.length() <= 0)
					colorVarGreen.inputText = "0";

				dg = Integer.parseInt(colorVarGreen.inputText);

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, dg + "");

		colorVarGreen.allowLetters = false;
		colorVarGreen.allowSpaces = false;
		colorVarGreen.maxChars = 3;
		colorVarGreen.checkMaxValue = true;

		colorVarBlue = new TextBox(Drawing.interfaceSizeX / 2 + 190, Drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue Noise", new Runnable()
		{
			@Override
			public void run() 
			{
				if (colorVarBlue.inputText.length() <= 0)
					colorVarBlue.inputText = "0";

				db = Integer.parseInt(colorVarBlue.inputText);

				reload();
				((ScreenLevelBuilder)Game.screen).colorMenu = true;
			}

		}
		, db + "");

		colorVarBlue.allowLetters = false;
		colorVarBlue.allowSpaces = false;
		colorVarBlue.maxChars = 3;
		colorVarBlue.checkMaxValue = true;

		teamName = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 120, 350, 40, "Team name", new Runnable()
		{
			@Override
			public void run() 
			{
				boolean duplicate = false;

				for (int i = 0; i < teams.size(); i++)
				{
					if (teamName.inputText.equals(teams.get(i).name))
						duplicate = true;
				}

				if (teamName.inputText.length() <= 0 || duplicate)
					teamName.inputText = selectedTeam.name;
				else
				{
					selectedTeam.name = teamName.inputText;
					teamButtons.get(lastTeamButton).text = teamName.inputText;
				}
			}

		}
		, "");

		teamName.lowerCase = true;
		//

		teamRed = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 60, 350, 40, "Red", new Runnable()
		{
			@Override
			public void run() 
			{
				if (teamRed.inputText.length() <= 0)
					teamRed.inputText = "0";

				int r = Integer.parseInt(teamRed.inputText);

				selectedTeam.teamColor = new Color(r, selectedTeam.teamColor.getGreen(), selectedTeam.teamColor.getBlue());
			}

		}
		, "");

		teamRed.allowLetters = false;
		teamRed.allowSpaces = false;
		teamRed.maxChars = 3;
		teamRed.maxValue = 255;
		teamRed.checkMaxValue = true;

		teamGreen = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 30, 350, 40, "Green", new Runnable()
		{
			@Override
			public void run() 
			{
				if (teamGreen.inputText.length() <= 0)
					teamGreen.inputText = "0";

				int g = Integer.parseInt(teamGreen.inputText);

				selectedTeam.teamColor = new Color(selectedTeam.teamColor.getRed(), g, selectedTeam.teamColor.getBlue());
			}

		}
		, "");

		teamGreen.allowLetters = false;
		teamGreen.allowSpaces = false;
		teamGreen.maxChars = 3;
		teamGreen.maxValue = 255;
		teamGreen.checkMaxValue = true;

		teamBlue = new TextBox(Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 + 120, 350, 40, "Blue", new Runnable()
		{
			@Override
			public void run() 
			{
				if (teamBlue.inputText.length() <= 0)
					teamBlue.inputText = "0";

				int b = Integer.parseInt(teamBlue.inputText);

				selectedTeam.teamColor = new Color(selectedTeam.teamColor.getRed(), selectedTeam.teamColor.getGreen(), b);
			}

		}
		, "");

		teamBlue.allowLetters = false;
		teamBlue.allowSpaces = false;
		teamBlue.maxChars = 3;
		teamBlue.maxValue = 255;
		teamBlue.checkMaxValue = true;

		File file = new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + lvlName);
		if (!file.exists() && reloadNewLevel)
		{
			this.teams.add(new Team("ally"));
			this.teams.add(new Team("enemy"));
			Game.player = new TankPlayer(Game.tank_size / 2, Game.tank_size / 2, 0);
			Game.player.team = this.teams.get(0);
			Game.movables.add(Game.player);
			this.reload();
		}
	}

	public void sortButtons()
	{
		int yoffset = -150;

		for (int i = 0; i < this.teamButtons.size(); i++)
		{
			int page = i / (rows * 3);
			int offset = 0;

			if (page * rows * 3 + rows < this.teamButtons.size())
				offset = -190;

			if (page * rows * 3 + rows * 2 < this.teamButtons.size())
				offset = -380;

			this.teamButtons.get(i).posY = Drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 60;

			if (i / rows % 3 == 0)
				this.teamButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset;
			else if (i / rows % 3 == 1)
				this.teamButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset + 380;
			else
				this.teamButtons.get(i).posX = Drawing.interfaceSizeX / 2 + offset + 380 * 2;
		}
	}

	@Override
	public void update()
	{		
		if (InputKeyboard.keys.contains(KeyEvent.VK_ESCAPE) && editable)
		{
			if (!Panel.pausePressed )
			{
				if (colorMenu || sizeMenu)
				{
					sizeMenu = false;
					colorMenu = false;
				}
				else if (optionsMenu)
					optionsMenu = false;
				else
					this.paused = !this.paused;
			}

			Panel.pausePressed = true;
		}
		else
			Panel.pausePressed = false;

		if (this.paused)	
		{
			if (!this.optionsMenu)
			{
				if (editable)
				{
					resume.update();
					options.update();
				}

				quit.update();
				play.update();
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
						for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, teamButtons.size()); i++)
						{
							teamButtons.get(i).update();
						}

						back4.update();
						newTeam.update();

						if (page > 0)
							previous.update();

						if (teamButtons.size() > (1 + page) * rows * 3)
							next.update();
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

			return;
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).update();
		}

		for (int i = 0; i < Game.removeEffects.size(); i++)
		{
			Game.effects.remove(Game.removeEffects.get(i));
		}

		Game.removeEffects.clear();

		boolean up = false;
		boolean down = false;

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_DOWN))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_DOWN);
			down = true;
		}
		else if (InputScroll.validScrollDown)
		{
			InputScroll.validScrollDown = false;
			down = true;
		}

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_UP))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_UP);
			up = true;
		}
		else if (InputScroll.validScrollUp)
		{
			InputScroll.validScrollUp = false;
			up = true;
		}

		if (down && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = (tankNum + 1) % Game.registryTank.tankEntries.size();
			Tank t = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			t.angle = mouseTank.angle;
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}
		else if (down && currentPlaceable == Placeable.obstacle)
		{
			obstacleNum = (obstacleNum + 1) % Game.registryObstacle.obstacleEntries.size();
			Obstacle o = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
			mouseObstacle = o;
		}

		if (up && currentPlaceable == Placeable.enemyTank)
		{
			tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size()) % Game.registryTank.tankEntries.size();
			Tank t = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
			t.angle = mouseTank.angle;
			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
		}
		else if (up && currentPlaceable == Placeable.obstacle)
		{
			obstacleNum = ((obstacleNum - 1) + Game.registryObstacle.obstacleEntries.size()) % Game.registryObstacle.obstacleEntries.size();
			Obstacle o = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
			mouseObstacle = o;
		}

		boolean right = false;
		boolean left = false;

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_RIGHT))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_RIGHT);
			right = true;
		}
		else if (InputMouse.b5ClickValid)
		{
			InputMouse.b5ClickValid = false;
			right = true;
		}

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_LEFT))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_LEFT);
			left = true;
		}
		else if (InputMouse.b4ClickValid)
		{
			InputMouse.b4ClickValid = false;
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

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_MINUS))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_MINUS);

			if (currentPlaceable == Placeable.enemyTank)
				teamNum = (teamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1); 
			else if (currentPlaceable == Placeable.playerTank)
				playerTeamNum = (playerTeamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1); 

		}

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_EQUALS))
		{
			InputKeyboard.validKeys.remove((Integer)KeyEvent.VK_EQUALS);

			if (currentPlaceable == Placeable.enemyTank)
				teamNum = (teamNum + 1) % (this.teams.size() + 1); 
			else if (currentPlaceable == Placeable.playerTank)
				playerTeamNum = (playerTeamNum + 1) % (this.teams.size() + 1); 
		}

		mouseTank.posX = Math.max(Game.tank_size / 2, Math.min(Game.currentSizeX * Game.tank_size - Game.tank_size / 2, Math.round(Game.window.getMouseX() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2));
		mouseTank.posY = Math.max(Game.tank_size / 2, Math.min(Game.currentSizeY * Game.tank_size - Game.tank_size / 2, Math.round(Game.window.getMouseY() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2));
		mouseObstacle.posX = Math.max(Game.tank_size / 2, Math.min(Game.currentSizeX * Game.tank_size - Game.tank_size / 2, Math.round(Game.window.getMouseX() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2));
		mouseObstacle.posY = Math.max(Game.tank_size / 2, Math.min(Game.currentSizeY * Game.tank_size - Game.tank_size / 2, Math.round(Game.window.getMouseY() / Game.tank_size + 0.5) * Game.tank_size - Game.tank_size / 2));

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

		if (InputMouse.rClick)
		{
			boolean skip = false;

			if (InputMouse.rClickValid)
			{
				for (int i = 0; i < Game.movables.size(); i++)
				{
					Movable m = Game.movables.get(i);
					if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && m != Game.player)
					{
						skip = true;
						Game.movables.remove(i);

						for (int z = 0; z < 100; z++)
						{
							Effect e = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
							int var = 50;
							e.col = new Color((int) Math.min(255, Math.max(0, ((Tank)m).color.getRed() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, ((Tank)m).color.getGreen() + Math.random() * var - var / 2)), (int) Math.min(255, Math.max(0, ((Tank)m).color.getBlue() + Math.random() * var - var / 2)));
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
					Game.obstacles.remove(i);
					break;
				}
			}

			if (!skip && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && InputMouse.rClickValid)
			{
				mouseTank.angle += Math.PI / 2;
				mouseTank.angle = mouseTank.angle % (Math.PI * 2);
			}

			InputMouse.rClickValid = false;
		}

		if (InputMouse.lClickValid || (InputMouse.lClick && currentPlaceable == Placeable.obstacle))
		{
			boolean skip = false;

			for (int i = 0; i < Game.movables.size(); i++)
			{
				Movable m = Game.movables.get(i);
				if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
				{
					if (m == Game.player || !InputMouse.lClickValid)
						skip = true;
					else
						Game.movables.remove(i);

					break;
				}
			}

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle m = Game.obstacles.get(i);
				if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
				{
					if (!InputMouse.lClickValid)
						skip = true;
					else
						Game.obstacles.remove(i);

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
				}
				else if (currentPlaceable == Placeable.playerTank)
				{	
					Game.player.posX = mouseTank.posX;
					Game.player.posY = mouseTank.posY;
					Game.player.angle = mouseTank.angle;
					Game.player.team = mouseTank.team;
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					Obstacle o = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
					o.color = mouseObstacle.color;
					o.posX = mouseObstacle.posX;
					o.posY = mouseObstacle.posY;
					mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
					Game.obstacles.add(o);
				}
			}

			InputMouse.lClickValid = false;
		}

		if (InputKeyboard.validKeys.contains(KeyEvent.VK_ENTER))
		{
			this.save(true);		
			Game.screen = new ScreenGame(this.name);
		}
	}

	public void save()
	{
		save(false);
	}

	public void save(boolean force)
	{	
		String level = "{";

		if (!this.editable)
			level += "*";

		level += (width + "," + height + "," + r + "," + g + "," + b + "," + dr + "," + dg + "," + db + "|");

		boolean[][][] obstacles = new boolean[Game.registryObstacle.obstacleEntries.size()][width][height];

		for (int h = 0; h < Game.registryObstacle.obstacleEntries.size(); h++)
		{
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					obstacles[h][i][j] = false;
				}
			}

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);
				int x = (int) (o.posX / Game.tank_size);
				int y = (int) (o.posY / Game.tank_size);

				if (x < obstacles[h].length && x >= 0 && y < obstacles[h][0].length && y >= 0 && o.name.equals(Game.registryObstacle.getEntry(h).name))
					obstacles[h][x][y] = true;
				
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
								level += i + "-" + j + name + ",";
							else
								level += i + "..." + (i + xLength - 1) + "-" + j + name + ",";

							for (int z = 0; z < xLength; z++)
							{
								obstacles[h][i + z][j] = false;
							}
						}
						else
						{
							level += i + "-" + j + "..." + (j + yLength - 1) + name + ",";

							for (int z = 0; z < yLength; z++)
							{
								obstacles[h][i][j + z] = false;
							}
						}
					}
				}
			}
		}
		
		if (Game.obstacles.size() == 0) 
		{
			level += "|";
		}

		level = level.substring(0, level.length() - 1);
		level += "|";

		for (int i = 0; i < Game.movables.size(); i++)
		{
			if (Game.movables.get(i) instanceof Tank)
			{
				Tank t = (Tank)Game.movables.get(i);
				int x = (int) (t.posX / Game.tank_size);
				int y = (int) (t.posY / Game.tank_size);
				int angle = (int) (t.angle * 2 / Math.PI);

				level += x + "-" + y + "-" + t.name + "-" + angle;

				if (t.team != null)
					level += "-" + t.team.name;

				level += ",";
			}
		}

		if (Game.movables.size() == 0) 
		{
			level += "|";
		}

		level = level.substring(0, level.length() - 1);

		level += "|";

		for (int i = 0; i < teams.size(); i++)
		{
			Team t = teams.get(i);
			level += t.name + "-" + t.friendlyFire;
			if (t.enableColor)
				level += "-" + t.teamColor.getRed() + "-" + t.teamColor.getGreen() + "-" + t.teamColor.getBlue();

			level += ",";
		}

		level = level.substring(0, level.length() - 1);

		level += "}";

		Game.currentLevel = level;

		File file = new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + name);
		if (file.exists())
		{
			if (!editable)
			{
				return;
			}

			file.delete();
		}

		if (Game.movables.size() > 1 || Game.obstacles.size() > 0 || force)
		{
			try
			{
				file.createNewFile();

				PrintWriter pw = new PrintWriter(new PrintStream(file));
				pw.println(level);
				pw.close();
			}
			catch (IOException e)
			{
				Game.exitToCrash(e);
			}
		}
	}

	public void reload()
	{
		save(true);
		Game.movables.clear();
		Game.obstacles.clear();
		ScreenLevelBuilder s = new ScreenLevelBuilder(name);
		Game.loadLevel(new File(Game.homedir + ScreenSavedLevels.levelDir + "/" + name), s);
		s.optionsMenu = true;
		Game.screen = s;
	}

	@Override
	public void draw(Graphics g)
	{
		this.drawDefaultBackground(g);

		g.setColor(Color.black);
		//	g.drawString(test, 20, 20);

		if (!paused)
		{
			if (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)
			{
				mouseTank.drawOutline(g);
				mouseTank.drawTeam(g);
			}
			else if (currentPlaceable == Placeable.obstacle)
				mouseObstacle.drawOutline(g);
		}

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Game.movables.get(i).draw(g);
			Game.movables.get(i).drawTeam(g);
		}

		for (int i = 0; i < Game.obstacles.size(); i++)
		{
			Game.obstacles.get(i).draw(g);
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).draw(g);
		}

		if (this.paused)	
		{
			g.setColor(new Color(127, 178, 228, 64));
			g.fillRect(0, 0, (int) (Game.window.getSize().getWidth()) + 1, (int) (Game.window.getSize().getHeight()) + 1);

			if (!this.optionsMenu)
			{
				if (editable)
				{
					resume.draw(g);
					options.draw(g);
				}

				quit.draw(g);
				play.draw(g);

				g.setColor(Color.black);
				Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Level menu");
			}
			else
			{
				if (this.sizeMenu)
				{
					this.sizeX.draw(g);
					this.sizeY.draw(g);
					this.back3.draw(g);
					g.setColor(Color.black);
					Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Level size");
				}
				else if (this.colorMenu)
				{
					this.colorRed.draw(g);
					this.colorGreen.draw(g);
					this.colorBlue.draw(g);
					this.colorVarRed.draw(g);
					this.colorVarGreen.draw(g);
					this.colorVarBlue.draw(g);
					Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 150, "Background colors");
					this.back2.draw(g);
				}
				else if (this.teamsMenu)
				{
					if (this.editTeamMenu)
					{
						if (this.teamColorMenu)
						{
							back6.draw(g);
							if (selectedTeam.enableColor)
							{
								teamRed.draw(g);
								teamGreen.draw(g);
								teamBlue.draw(g);
							}
							teamColorEnabled.draw(g);
							Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 210, "Team color: " + this.selectedTeam.name);
						}
						else
						{
							back5.draw(g);
							teamName.draw(g);
							teamColor.draw(g);
							deleteTeam.draw(g);
							teamFriendlyFire.draw(g);
							Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 210, this.selectedTeam.name);
						}
					}
					else
					{
						for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, teamButtons.size()); i++)
						{
							teamButtons.get(i).draw(g);
						}

						back4.draw(g);
						newTeam.draw(g);

						Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 210, "Teams");

						if (page > 0)
							previous.draw(g);

						if (teamButtons.size() > (1 + page) * rows * 3)
							next.draw(g);
					}
				}
				else
				{
					this.levelName.draw(g);
					this.back1.draw(g);
					this.colorOptions.draw(g);
					this.sizeOptions.draw(g);
					this.teamsOptions.draw(g);

					g.setColor(Color.black);
					Drawing.drawInterfaceText(g, Drawing.interfaceSizeX / 2, Drawing.interfaceSizeY / 2 - 210, "Level options");
				}
			}
		}
	}
}

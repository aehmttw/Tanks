package tanks.gui.screen;

import basewindow.BaseFile;
import basewindow.InputPoint;
import tanks.*;
import tanks.event.EventCreatePlayer;
import tanks.event.INetworkEvent;
import tanks.gui.*;
import tanks.hotbar.item.Item;
import tanks.obstacle.Obstacle;
import tanks.registry.RegistryItem;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenLevelBuilder extends Screen implements ILevelPreviewScreen, IItemScreen
{
	public ArrayList<Action> actions = new ArrayList<Action>();
	public ArrayList<Action> redoActions = new ArrayList<Action>();
	public int redoLength = -1;

	public Placeable currentPlaceable = Placeable.enemyTank;
	public int tankNum = 0;
	public int obstacleNum = 0;
	public int teamNum = 1;
	public int playerTeamNum = 0;
	public boolean reloadNewLevel;
	public Tank mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	public int mouseTankOrientation = 0;
	public tanks.obstacle.Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	public double mouseObstacleHeight = 1;
	public boolean stagger = false;
	public boolean oddStagger = false;
	public int mouseObstacleGroup = 0;
	public boolean paused = true;
	public boolean optionsMenu = false;
	public boolean sizeMenu = false;
	public boolean colorMenu = false;
	public boolean teamsMenu = false;
	public boolean editTeamMenu = false;
	public boolean teamColorMenu = false;
	public boolean itemMenu = false;
	public boolean startingItemMenu = false;
	public boolean shopMenu = false;
	public boolean objectMenu;
	public boolean selectTeamMenu;
	public boolean rotateTankMenu;
	public boolean metadataMenu;
	public boolean quickExit = false;
	public boolean confirmDeleteMenu = false;
	public double clickCooldown = 0;
	public Team selectedTeam;
	public String teamSelectTitle = "";
	public int tankButtonPage = 0;
	public int obstacleButtonPage = 0;
	public boolean editable = true;
	public ArrayList<Team> teams = new ArrayList<Team>();
	public ArrayList<Button> teamEditButtons = new ArrayList<Button>();
	public ArrayList<Button> teamSelectButtons = new ArrayList<Button>();
	public ButtonList teamEditList;
	public ButtonList teamSelectList;
	public ButtonList shopList;
	public ButtonList startingItemsList;
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
	public boolean eraseMode = false;
	public boolean changeCameraMode = false;
	public boolean selectMode = false;
	public double selectX1;
	public double selectY1;
	public double selectX2;
	public double selectY2;
	public boolean selectHeld = false;
	public boolean selectInverted = false;
	public boolean selection = false;
	public boolean selectAdd = true;
	public boolean selectSquare = false;
	public boolean[][] selectedTiles;
	public boolean showControls = true;
	public double controlsSizeMultiplier = 0.75;

	public boolean panDown;
	public double panX;
	public double panY;
	public double panCurrentX;
	public double panCurrentY;
	public double zoomCurrentX;
	public double zoomCurrentY;
	public boolean zoomDown;
	public double zoomDist;
	public double offsetX;
	public double offsetY;
	public double zoom = 1;
	public int validZoomFingers = 0;

	public double fontBrightness = 0;

	public int startingCoins;
	public ArrayList<Item> shop = new ArrayList<Item>();
	public ArrayList<Item> startingItems = new ArrayList<Item>();

	public Selector itemSelector;

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

	public Button resume = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Edit", new Runnable()
	{
		@Override
		public void run()
		{
			clickCooldown = 20;
			paused = false;
		}
	});

	public Button play = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 120), this.objWidth, this.objHeight, "Play", new Runnable()
	{
		@Override
		public void run()
		{
			play();
		}
	}
	);

	public Button playUnavailable = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 120), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

	public Button options = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 0), this.objWidth, this.objHeight, "Options", new Runnable()
	{
		@Override
		public void run()
		{
			optionsMenu = true;
		}
	}
	);

	public Button colorOptions = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 - 90), this.objWidth, this.objHeight, "Background colors", new Runnable()
	{
		@Override
		public void run()
		{
			colorMenu = true;
		}
	}
	);

	public Button sizeOptions = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 30, this.objWidth, this.objHeight, "Level size", new Runnable()
	{
		@Override
		public void run()
		{
			sizeMenu = true;
		}
	}
	);

	public Button teamsOptions = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 30), this.objWidth, this.objHeight, "Teams", new Runnable()
	{
		@Override
		public void run()
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
				((ScreenLevelBuilder) Game.screen).teamsMenu = true;
			}

			teamsMenu = true;
		}
	}
	);

	public Button itemOptions = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Items", new Runnable()
	{
		@Override
		public void run()
		{
			itemMenu = true;
		}
	}
	);

	public Button editShop = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "Shop", new Runnable()
	{
		@Override
		public void run()
		{
			shopMenu = true;
		}
	}
	);

	public Button editStartingItems = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "Starting items", new Runnable()
	{
		@Override
		public void run()
		{
			startingItemMenu = true;
		}
	}
	);

	public Button addItem = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Add item", new Runnable()
	{
		@Override
		public void run()
		{
			ScreenSelector s = new ScreenSelector(itemSelector, Game.screen);
			s.drawBehindScreen = true;
			Game.screen = s;
		}
	}
	);

	public Button back1 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 150), this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			optionsMenu = false;
		}
	}
	);

	public Button back2 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 180), this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			colorMenu = false;
		}
	}
	);

	public Button back3 = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 120), this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			sizeMenu = false;
		}
	}
	);

	public Button back4 = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			teamsMenu = false;
		}
	}
	);

	public Button back5 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			reload();
			((ScreenLevelBuilder) Game.screen).optionsMenu = true;
			((ScreenLevelBuilder) Game.screen).teamsMenu = true;
			((ScreenLevelBuilder) Game.screen).selectedTeam = null;
		}
	}
	);

	public Button back6 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			teamColorMenu = false;
		}
	}
	);

	public Button back7 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Done", new Runnable()
	{
		@Override
		public void run()
		{
			selectTeamMenu = false;

			if (quickExit)
			{
				clickCooldown = 20;
				quickExit = false;
				objectMenu = false;
				paused = false;
			}
		}
	}
	);

	public Button delete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), this.objWidth, this.objHeight, "Delete level", new Runnable()
	{
		@Override
		public void run()
		{
			confirmDeleteMenu = true;
		}
	}
	);

	public Button cancelDelete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 60), this.objWidth, this.objHeight, "No", new Runnable()
	{
		@Override
		public void run()
		{
			confirmDeleteMenu = false;
		}
	}
	);

	public Button confirmDelete = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2), this.objWidth, this.objHeight, "Yes", new Runnable()
	{
		@Override
		public void run()
		{
			BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name);

			Game.cleanUp();

			while (file.exists())
			{
				file.delete();
			}

			Game.screen = new ScreenSavedLevels();
		}
	}
	);

	public Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, (int) (Drawing.drawing.interfaceSizeY / 2 + 120), this.objWidth, this.objHeight, "Exit", new Runnable()
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

	public Button nextTankPage = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			tankButtonPage++;
		}
	}
	);

	public Button previousTankPage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			tankButtonPage--;
		}
	}
	);

	public Button nextObstaclePage = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Next page", new Runnable()
	{
		@Override
		public void run()
		{
			obstacleButtonPage++;
		}
	}
	);

	public Button previousObstaclePage = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 180, this.objWidth, this.objHeight, "Previous page", new Runnable()
	{
		@Override
		public void run()
		{
			obstacleButtonPage--;
		}
	}
	);

	public Button exitObjectMenu = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Ok", new Runnable()
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

	public Button rotateTankButton = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Tank orientation", new Runnable()
	{
		@Override
		public void run()
		{
			rotateTankMenu = true;
		}
	}
	);

	public Button metadataButton = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			groupID.inputText = mouseObstacleGroup + "";
			metadataMenu = true;
		}
	}
	);

	public Button selectTeam = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Team: ", new Runnable()
	{
		@Override
		public void run()
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
			}

			ScreenLevelBuilder s = ((ScreenLevelBuilder) Game.screen);
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

	public Button deleteTeam = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Delete team", new Runnable()
	{
		@Override
		public void run()
		{
			teams.remove(selectedTeam);
			reload();
			((ScreenLevelBuilder) Game.screen).paused = true;
			((ScreenLevelBuilder) Game.screen).optionsMenu = true;
			((ScreenLevelBuilder) Game.screen).teamsMenu = true;
		}
	}
	);

	public Button teamFriendlyFire = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Friendly fire: on", new Runnable()
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

	public Button increaseHeight = new Button(Drawing.drawing.interfaceSizeX / 2 + 100, Drawing.drawing.interfaceSizeY / 2, 60, 60, "+", new Runnable()
	{
		@Override
		public void run()
		{
			mouseObstacleHeight += 0.5;
		}
	}
	);

	public Button decreaseHeight = new Button(Drawing.drawing.interfaceSizeX / 2 - 100, Drawing.drawing.interfaceSizeY / 2, 60, 60, "-", new Runnable()
	{
		@Override
		public void run()
		{
			mouseObstacleHeight -= 0.5;
		}
	}
	);

	public Button staggering = new Button(Drawing.drawing.interfaceSizeX / 2 + 200, Drawing.drawing.interfaceSizeY / 2, 60, 60, "", new Runnable()
	{
		@Override
		public void run()
		{
			if (!stagger)
			{
				mouseObstacleHeight = Math.max(mouseObstacleHeight, 1);
				stagger = true;
			}
			else if (!oddStagger)
			{
				mouseObstacleHeight = Math.max(mouseObstacleHeight, 1);
				oddStagger = true;
			}
			else
			{
				oddStagger = false;
				stagger = false;
			}
		}
	}, " --- "
	);

	public Button increaseID = new Button(Drawing.drawing.interfaceSizeX / 2 + 250, Drawing.drawing.interfaceSizeY / 2, 60, 60, "+", new Runnable()
	{
		@Override
		public void run()
		{
			mouseObstacleGroup += 1;
			groupID.inputText = mouseObstacleGroup + "";
			groupID.previousInputText = mouseObstacleGroup + "";
			mouseObstacle.setMetadata(mouseObstacleGroup + "");
		}
	}
	);

	public Button decreaseID = new Button(Drawing.drawing.interfaceSizeX / 2 - 250, Drawing.drawing.interfaceSizeY / 2, 60, 60, "-", new Runnable()
	{
		@Override
		public void run()
		{
			mouseObstacleGroup -= 1;
			groupID.inputText = mouseObstacleGroup + "";
			groupID.previousInputText = mouseObstacleGroup + "";
			mouseObstacle.setMetadata(mouseObstacleGroup + "");
		}
	}
	);

	public Button back8 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 75, 75, "Done", new Runnable()
	{
		@Override
		public void run()
		{
			rotateTankMenu = false;

			if (quickExit)
			{
				clickCooldown = 20;
				quickExit = false;
				objectMenu = false;
				paused = false;
			}
		}
	}
	);

	public Button back9 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Done", new Runnable()
	{
		@Override
		public void run()
		{
			metadataMenu = false;

			if (quickExit)
			{
				quickExit = false;
				objectMenu = false;
				paused = false;
				clickCooldown = 20;
			}
		}
	}
	);

	public Button back10 = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			startingItemMenu = false;
			shopMenu = false;
		}
	}
	);

	public Button back11 = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			itemMenu = false;
		}
	}
	);

	public Button teamColorEnabled = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, this.objWidth, this.objHeight, "Team color: off", new Runnable()
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

	public Button placePlayer = new Button(Drawing.drawing.interfaceSizeX / 2 - 380, Drawing.drawing.interfaceSizeY / 2 - 180, this.objWidth, this.objHeight, "Player", new Runnable()
	{
		@Override
		public void run()
		{
			currentPlaceable = Placeable.playerTank;
			mouseTank = new TankPlayer(0, 0, 0);
		}
	}
	);

	public Button placeEnemy = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 180, this.objWidth, this.objHeight, "Tank", new Runnable()
	{
		@Override
		public void run()
		{
			currentPlaceable = Placeable.enemyTank;
			mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
		}
	}
	);
	public Button placeObstacle = new Button(Drawing.drawing.interfaceSizeX / 2 + 380, Drawing.drawing.interfaceSizeY / 2 - 180, this.objWidth, this.objHeight, "Block", new Runnable()
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
	}, "Level menu (" + Game.game.input.editorPause.getInputs() + ")"
	);

	Button menu = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			objectMenu = true;
		}
	}, "Object menu (" + Game.game.input.editorObjectMenu.getInputs() + ")"
	);

	public Button recenter = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 90, 300, 35, "Re-center", new Runnable()
	{
		@Override
		public void run()
		{
			zoom = 1;
			offsetX = 1;
			offsetY = 1;
		}
	}
	);

	Button playControl = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			play();
		}
	}, "Play (" + Game.game.input.editorPlay.getInputs() + ")"
	);

	Button place = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			selectMode = false;
			changeCameraMode = false;
			eraseMode = false;
		}
	}, "Build (" + Game.game.input.editorBuild.getInputs() + ")"
	);

	Button erase = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			selectMode = false;
			changeCameraMode = false;
			eraseMode = true;
		}
	}, "Erase (" + Game.game.input.editorErase.getInputs() + ")"
	);

	Button panZoom = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			changeCameraMode = true;
		}
	}, "Adjust camera (" + Game.game.input.editorCamera.getInputs() + ")"
	);

	Button select = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			selectMode = true;
			changeCameraMode = false;
		}
	}, "Select (" + Game.game.input.editorSelect.getInputs() + ")"
	);

	Button undo = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			Action a = actions.remove(actions.size() - 1);
			a.undo();
			redoActions.add(a);
			redoLength = actions.size();
		}
	}, "Undo (" + Game.game.input.editorUndo.getInputs() + ")"
	);

	Button redo = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			Action a = redoActions.remove(redoActions.size() - 1);
			a.redo();
			actions.add(a);
			redoLength = actions.size();
		}
	}, "Redo (" + Game.game.input.editorRedo.getInputs() + ")"
	);

	Button rotateShortcut = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			objectMenu = true;
			rotateTankMenu = true;
			quickExit = true;
		}
	}, "Tank orientation (" + Game.game.input.editorRotate.getInputs() + ")"
	);

	Button teamShortcut = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			if (teams.size() != teamEditButtons.size())
			{
				reload();
			}

			ScreenLevelBuilder s = ((ScreenLevelBuilder) Game.screen);
			s.paused = true;
			s.objectMenu = true;
			s.currentPlaceable = currentPlaceable;
			s.selectTeamMenu = true;

			if (currentPlaceable == Placeable.enemyTank)
				s.teamSelectTitle = "Select tank team";
			else if (currentPlaceable == Placeable.playerTank)
				s.teamSelectTitle = "Select player team";

			s.quickExit = true;
		}
	}, "Tank team (" + Game.game.input.editorTeam.getInputs() + ")"
	);

	Button metadataShortcut = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			paused = true;
			objectMenu = true;
			metadataMenu = true;
			quickExit = true;
			groupID.inputText = mouseObstacleGroup + "";
		}
	}, ""
	);

	Button selectSquareToggle = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			selectSquare = !selectSquare;
		}
	}, "Lock square selecting (Hold: " + Game.game.input.editorHoldSquare.getInputs() + ", Toggle: " + Game.game.input.editorLockSquare.getInputs() + ")"
	);

	Button selectAddToggle = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			selectAdd = !selectAdd;
		}
	}, "Toggle select/deselect (" + Game.game.input.editorSelectAddToggle.getInputs() + ")"
	);

	Button selectClear = new Button(0, -1000, 70, 70, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.game.window.pressedKeys.clear();
			Game.game.window.pressedButtons.clear();

			clearSelection();
		}
	}, "Clear selection (" + Game.game.input.editorDeselect.getInputs() + ")"
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
	public TextBox groupID;
	public TextBox editCoins;

	public Button newTeam = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 300, this.objWidth, this.objHeight, "New team", new Runnable()
	{
		@Override
		public void run()
		{
			Team t = new Team(System.currentTimeMillis() + "");
			teams.add(t);
			reload();
			((ScreenLevelBuilder) Game.screen).teamsMenu = true;
			((ScreenLevelBuilder) Game.screen).editTeamMenu = true;
			((ScreenLevelBuilder) Game.screen).teamName.inputText = t.name;
			((ScreenLevelBuilder) Game.screen).selectedTeam = ((ScreenLevelBuilder) Game.screen).teams.get(teams.size() - 1);
		}
	}
	);
	public TextBox teamRed;
	public TextBox teamGreen;
	public TextBox teamBlue;
	public Button teamColor = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Team color", new Runnable()
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
		if (Game.game.window.touchscreen)
			controlsSizeMultiplier = 1.0;

		place.sizeX *= controlsSizeMultiplier;
		place.sizeY *= controlsSizeMultiplier;
		place.fullInfo = true;

		erase.sizeX *= controlsSizeMultiplier;
		erase.sizeY *= controlsSizeMultiplier;
		erase.fullInfo = true;

		panZoom.sizeX *= controlsSizeMultiplier;
		panZoom.sizeY *= controlsSizeMultiplier;
		panZoom.fullInfo = true;

		select.sizeX *= controlsSizeMultiplier;
		select.sizeY *= controlsSizeMultiplier;
		select.fullInfo = true;

		pause.sizeX *= controlsSizeMultiplier;
		pause.sizeY *= controlsSizeMultiplier;
		pause.fullInfo = true;

		menu.sizeX *= controlsSizeMultiplier;
		menu.sizeY *= controlsSizeMultiplier;
		menu.fullInfo = true;

		playControl.sizeX *= controlsSizeMultiplier;
		playControl.sizeY *= controlsSizeMultiplier;
		playControl.fullInfo = true;

		undo.sizeX *= controlsSizeMultiplier;
		undo.sizeY *= controlsSizeMultiplier;
		undo.fullInfo = true;

		redo.sizeX *= controlsSizeMultiplier;
		redo.sizeY *= controlsSizeMultiplier;
		redo.fullInfo = true;

		metadataShortcut.sizeX *= controlsSizeMultiplier;
		metadataShortcut.sizeY *= controlsSizeMultiplier;
		metadataShortcut.fullInfo = true;

		teamShortcut.sizeX *= controlsSizeMultiplier;
		teamShortcut.sizeY *= controlsSizeMultiplier;
		teamShortcut.fullInfo = true;

		rotateShortcut.sizeX *= controlsSizeMultiplier;
		rotateShortcut.sizeY *= controlsSizeMultiplier;
		rotateShortcut.fullInfo = true;

		selectClear.sizeX *= controlsSizeMultiplier;
		selectClear.sizeY *= controlsSizeMultiplier;
		selectClear.fullInfo = true;

		selectAddToggle.sizeX *= controlsSizeMultiplier;
		selectAddToggle.sizeY *= controlsSizeMultiplier;
		selectAddToggle.fullInfo = true;

		selectSquareToggle.sizeX *= controlsSizeMultiplier;
		selectSquareToggle.sizeY *= controlsSizeMultiplier;
		selectSquareToggle.fullInfo = true;

		staggering.imageSizeX = 40;
		staggering.imageSizeY = 40;
		staggering.fullInfo = true;

		this.nextObstaclePage.image = "play.png";
		this.nextObstaclePage.imageSizeX = 25;
		this.nextObstaclePage.imageSizeY = 25;
		this.nextObstaclePage.imageXOffset = 145;

		this.previousObstaclePage.image = "play.png";
		this.previousObstaclePage.imageSizeX = -25;
		this.previousObstaclePage.imageSizeY = 25;
		this.previousObstaclePage.imageXOffset = -145;

		this.nextTankPage.image = "play.png";
		this.nextTankPage.imageSizeX = 25;
		this.nextTankPage.imageSizeY = 25;
		this.nextTankPage.imageXOffset = 145;

		this.previousTankPage.image = "play.png";
		this.previousTankPage.imageSizeX = -25;
		this.previousTankPage.imageSizeY = 25;
		this.previousTankPage.imageXOffset = -145;


		this.enableMargins = false;

		this.reloadNewLevel = reload;

		for (int i = 0; i < drawables.length; i++)
		{
			drawables[i] = new ArrayList<IDrawable>();
		}

		tanks.obstacle.Obstacle.draw_size = Game.tile_size;

		Game.game.window.validScrollDown = false;
		Game.game.window.validScrollUp = false;

		this.name = lvlName;

		if (this.teams.size() == 0)
			mouseTank.team = null;
		else
			mouseTank.team = this.teams.get(teamNum);

		levelName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, this.objWidth, this.objHeight, "Level name", new Runnable()
		{
			@Override
			public void run()
			{
				BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + lvlName);

				String input = levelName.inputText.replace(" ", "_");
				if (levelName.inputText.length() > 0 && !Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + input + ".tanks").exists())
				{
					if (file.exists())
					{
						file.renameTo(Game.homedir + Game.levelDir + "/" + input + ".tanks");
					}

					while (file.exists())
					{
						file.delete();
					}

					name = input + ".tanks";

					reload(false);
				}
				else
				{
					levelName.inputText = name.split("\\.")[0].replace("_", " ");
				}

			}

		}
				, lvlName.split("\\.")[0].replace("_", " "));

		levelName.enableCaps = true;

		sizeX = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Width", new Runnable()
		{
			@Override
			public void run()
			{
				if (sizeX.inputText.length() <= 0)
					sizeX.inputText = width + "";
				else
					width = Integer.parseInt(sizeX.inputText);

				reload();
				((ScreenLevelBuilder) Game.screen).sizeMenu = true;
			}

		}
				, width + "");

		sizeX.allowLetters = false;
		sizeX.allowSpaces = false;
		sizeX.maxChars = 3;
		sizeX.maxValue = 400;
		sizeX.minValue = 1;
		sizeX.checkMaxValue = true;
		sizeX.checkMinValue = true;

		groupID = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 15, this.objWidth, this.objHeight, "Group ID", new Runnable()
		{
			@Override
			public void run()
			{
				if (groupID.inputText.length() <= 0)
					groupID.inputText = mouseObstacleGroup + "";
				else
					mouseObstacleGroup = Integer.parseInt(groupID.inputText);

				mouseObstacle.setMetadata(mouseObstacleGroup + "");
			}

		}
				, mouseObstacleGroup + "");

		groupID.allowLetters = false;
		groupID.allowSpaces = false;
		groupID.maxChars = 9;
		groupID.minValue = 0;
		groupID.checkMaxValue = true;
		groupID.checkMinValue = true;

		sizeY = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Height", new Runnable()
		{
			@Override
			public void run()
			{
				if (sizeY.inputText.length() <= 0)
					sizeY.inputText = height + "";
				else
					height = Integer.parseInt(sizeY.inputText);

				reload();
				((ScreenLevelBuilder) Game.screen).sizeMenu = true;
			}

		}
				, height + "");

		sizeY.allowLetters = false;
		sizeY.allowSpaces = false;
		sizeY.maxChars = 3;
		sizeY.maxValue = 400;
		sizeY.minValue = 1;
		sizeY.checkMaxValue = true;
		sizeY.checkMinValue = true;

		colorRed = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Red", new Runnable()
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
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, r + "");

		colorRed.allowLetters = false;
		colorRed.allowSpaces = false;
		colorRed.maxChars = 3;
		colorRed.maxValue = 255;
		colorRed.checkMaxValue = true;

		colorGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Green", new Runnable()
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
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, g + "");

		colorGreen.allowLetters = false;
		colorGreen.allowSpaces = false;
		colorGreen.maxChars = 3;
		colorGreen.maxValue = 255;
		colorGreen.checkMaxValue = true;

		colorBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Blue", new Runnable()
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
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, b + "");

		colorBlue.allowLetters = false;
		colorBlue.allowSpaces = false;
		colorBlue.maxChars = 3;
		colorBlue.maxValue = 255;
		colorBlue.checkMaxValue = true;

		colorVarRed = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Red noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarRed.inputText.length() <= 0)
					colorVarRed.inputText = colorVarRed.previousInputText;

				dr = Integer.parseInt(colorVarRed.inputText);

				reload(true);
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, dr + "");

		colorVarRed.allowLetters = false;
		colorVarRed.allowSpaces = false;
		colorVarRed.maxChars = 3;
		colorVarRed.checkMaxValue = true;

		colorVarGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Green noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarGreen.inputText.length() <= 0)
					colorVarGreen.inputText = colorVarGreen.previousInputText;

				dg = Integer.parseInt(colorVarGreen.inputText);

				reload(true);
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, dg + "");

		colorVarGreen.allowLetters = false;
		colorVarGreen.allowSpaces = false;
		colorVarGreen.maxChars = 3;
		colorVarGreen.checkMaxValue = true;

		colorVarBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Blue noise", new Runnable()
		{
			@Override
			public void run()
			{
				if (colorVarBlue.inputText.length() <= 0)
					colorVarBlue.inputText = colorVarBlue.previousInputText;

				db = Integer.parseInt(colorVarBlue.inputText);

				reload(true);
				((ScreenLevelBuilder) Game.screen).colorMenu = true;
			}

		}
				, db + "");

		colorVarBlue.allowLetters = false;
		colorVarBlue.allowSpaces = false;
		colorVarBlue.maxChars = 3;
		colorVarBlue.checkMaxValue = true;

		teamName = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 120, this.objWidth, this.objHeight, "Team name", new Runnable()
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

		teamRed = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Red", new Runnable()
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

		teamGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 30, this.objWidth, this.objHeight, "Green", new Runnable()
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

		teamBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Blue", new Runnable()
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

		editCoins = new TextBox(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Starting coins", new Runnable()
		{
			@Override
			public void run()
			{
				if (editCoins.inputText.length() <= 0)
					editCoins.inputText = "0";

				startingCoins = Integer.parseInt(editCoins.inputText);
			}

		}
				,  this.startingCoins + "");

		editCoins.allowLetters = false;
		editCoins.allowSpaces = false;
		editCoins.maxChars = 9;
		editCoins.checkMaxValue = true;

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + lvlName);

		if (!file.exists() && reloadNewLevel)
		{
			this.teams.add(new Team("ally"));
			this.teams.add(new Team("enemy"));

			TankSpawnMarker t = new TankSpawnMarker("player", Game.tile_size / 2, Game.tile_size / 2, 0);
			t.team = this.teams.get(0);
			Game.movables.add(t);
			this.spawns.add(t);

			this.reload();
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

			tanks.obstacle.Obstacle o = Game.registryObstacle.obstacleEntries.get(i).getObstacle(x, y);
			ButtonObject b = new ButtonObject(o, x, y, 75, 75, new Runnable()
			{
				@Override
				public void run()
				{
					obstacleNum = j;
					mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);

					if (mouseObstacle.enableGroupID)
						mouseObstacle.setMetadata(mouseObstacleGroup + "");
				}
			}
					, o.description);

			if (o.description.equals(""))
				b.enableHover = false;

			this.obstacleButtons.add(b);
		}

		String[] itemNames = new String[Game.registryItem.itemEntries.size()];
		for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
		{
			RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
			itemNames[i] = r.name;
		}

		itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, new Runnable()
		{
			@Override
			public void run()
			{
				Item i = Game.registryItem.getEntry(itemSelector.options[itemSelector.selectedOption]).getItem();

				if (startingItemMenu)
					startingItems.add(i);
				else if (shopMenu)
					shop.add(i);

				ScreenEditItem s = new ScreenEditItem(i, (IItemScreen) Game.screen);
				s.drawBehindScreen = true;
				Game.screen = s;
			}
		});

		itemSelector.quick = true;
	}

	public void sortButtons()
	{
		this.teamSelectList = new ButtonList(teamSelectButtons, 0, 0, -30);
		this.teamEditList = new ButtonList(teamEditButtons, 0, 0, -30);
	}

	@Override
	public void update()
	{
		if (Level.currentColorR + Level.currentColorG + Level.currentColorB < 127 * 3)
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;


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
					for (Button b: teamSelectButtons)
					{
						b.enabled = true;
					}

					if (currentPlaceable == Placeable.playerTank)
						teamSelectButtons.get(playerTeamNum).enabled = false;
					else if (currentPlaceable == Placeable.enemyTank)
						teamSelectButtons.get(teamNum).enabled = false;

					this.teamSelectList.update();

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
				else if (metadataMenu)
				{
					if (mouseObstacle.enableStacking)
					{
						this.increaseHeight.enabled = this.mouseObstacleHeight < 4;
						this.decreaseHeight.enabled = this.mouseObstacleHeight > 0.5;

						if (stagger)
							this.decreaseHeight.enabled = this.mouseObstacleHeight > 1;

						this.increaseHeight.update();
						this.decreaseHeight.update();
						this.staggering.update();

						if (!stagger)
						{
							this.staggering.image = "nostagger.png";
							this.staggering.hoverText[0] = "Blocks will all be placed";
							this.staggering.hoverText[1] = "with the same height";
						}
						else if (oddStagger)
						{
							this.staggering.image = "oddstagger.png";
							this.staggering.hoverText[0] = "Every other block on the grid";
							this.staggering.hoverText[1] = "will be half a block shorter";
						}
						else
						{
							this.staggering.image = "evenstagger.png";
							this.staggering.hoverText[0] = "Every other block on the grid";
							this.staggering.hoverText[1] = "will be half a block shorter";
						}
					}
					else if (mouseObstacle.enableGroupID)
					{
						this.increaseID.enabled = this.mouseObstacleGroup < 999999999;
						this.decreaseID.enabled = this.mouseObstacleGroup > 0;

						this.increaseID.update();
						this.decreaseID.update();
						this.groupID.update();
					}

					this.back9.update();
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
						if (this.teamNum >= this.teams.size() + 1)
							this.teamNum = 0;

						if (this.teamNum == this.teams.size())
							this.selectTeam.text = "No team";
						else
							this.selectTeam.text = "Team: " + this.teams.get(this.teamNum).name;

						this.selectTeam.update();
						this.rotateTankButton.update();
					}

					if (currentPlaceable == Placeable.playerTank)
					{
						if (this.playerTeamNum >= this.teams.size() + 1)
							this.playerTeamNum = 0;

						if (this.playerTeamNum == this.teams.size())
							this.selectTeam.text = "No team";
						else
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

						if (mouseObstacle.enableStacking)
						{
							this.metadataButton.text = "Block height: " + mouseObstacleHeight;
							this.metadataButton.update();
						}
						else if (mouseObstacle.enableGroupID)
						{
							this.metadataButton.text = "Group ID: " + mouseObstacleGroup;
							this.metadataButton.update();
						}
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
						this.teamEditList.update();

						back4.update();
						newTeam.update();
					}
				}
				else if (this.itemMenu)
				{
					if (this.startingItemMenu)
					{
						this.startingItemsList.update();
						this.back10.update();
						this.addItem.update();
					}
					else if (this.shopMenu)
					{
						this.shopList.update();
						this.back10.update();
						this.addItem.update();
					}
					else
					{
						this.editCoins.update();
						this.editShop.update();
						this.editStartingItems.update();
						this.back11.update();
					}
				}
				else
				{
					this.levelName.update();
					this.back1.update();
					this.colorOptions.update();
					this.sizeOptions.update();
					this.teamsOptions.update();
					this.itemOptions.update();
				}
			}
		}
		else if (showControls)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight;
			double vStep = 0;
			double hStep = 0;

			if (vertical)
				vStep = 100 * controlsSizeMultiplier;
			else
				hStep = 100 * controlsSizeMultiplier;

			pause.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 * controlsSizeMultiplier - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			pause.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50 * controlsSizeMultiplier;
			pause.update();

			menu.posX = pause.posX - hStep;
			menu.posY = pause.posY + vStep;
			menu.update();

			playControl.enabled = spawns.size() > 0;
			playControl.posX = pause.posX - hStep * 2;
			playControl.posY = pause.posY + vStep * 2;
			playControl.update();

			if (changeCameraMode)
			{
				place.enabled = true;
				erase.enabled = true;
				select.enabled = true;
				panZoom.enabled = false;
			}
			else if (selectMode)
			{
				place.enabled = true;
				erase.enabled = true;
				select.enabled = false;
				panZoom.enabled = true;
			}
			else if (eraseMode)
			{
				place.enabled = true;
				erase.enabled = false;
				select.enabled = true;
				panZoom.enabled = true;
			}
			else
			{
				place.enabled = false;
				erase.enabled = true;
				select.enabled = true;
				panZoom.enabled = true;
			}

			place.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * controlsSizeMultiplier;
			place.posY = -((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2 + 50 * controlsSizeMultiplier;
			place.update();

			erase.posX = place.posX + hStep;
			erase.posY = place.posY + vStep;
			erase.update();

			select.posX = erase.posX + hStep;
			select.posY = erase.posY + vStep;
			select.update();

			panZoom.posX = select.posX + hStep;
			panZoom.posY = select.posY + vStep;
			panZoom.update();

			undo.enabled = actions.size() > 0;
			undo.posX = -(Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale + 50 * controlsSizeMultiplier;
			undo.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
					+ Drawing.drawing.interfaceSizeY - 50 * controlsSizeMultiplier;
			undo.update();

			redo.enabled = redoActions.size() > 0;
			redo.posX = undo.posX + hStep;
			redo.posY = undo.posY - vStep;
			redo.update();

			metadataShortcut.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 * controlsSizeMultiplier - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			metadataShortcut.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
					+ Drawing.drawing.interfaceSizeY - 50 * controlsSizeMultiplier;

			if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableStacking)
				metadataShortcut.hoverText[0] = "Obstacle height (" + Game.game.input.editorHeight.getInputs() + ")";

			if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableGroupID)
				metadataShortcut.hoverText[0] = "Obstacle group ID (" + Game.game.input.editorGroupID.getInputs() + ")";

			rotateShortcut.posX = metadataShortcut.posX;
			rotateShortcut.posY = metadataShortcut.posY;

			teamShortcut.posX = metadataShortcut.posX - hStep;
			teamShortcut.posY = metadataShortcut.posY - vStep;

			selectSquareToggle.posX = metadataShortcut.posX;
			selectSquareToggle.posY = metadataShortcut.posY;

			selectAddToggle.posX = selectSquareToggle.posX - hStep;
			selectAddToggle.posY = selectSquareToggle.posY - vStep;

			selectClear.posX = playControl.posX - hStep;
			selectClear.posY = playControl.posY + vStep;

			if (selection)
				selectClear.update();

			if (selectMode)
			{
				selectSquareToggle.update();

				if (selection)
					selectAddToggle.update();
			}
			else if (!eraseMode && !changeCameraMode)
			{
				if (currentPlaceable == Placeable.obstacle && (mouseObstacle.enableGroupID || mouseObstacle.enableStacking))
					metadataShortcut.update();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					rotateShortcut.update();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					teamShortcut.update();
			}
		}

		if (Game.game.input.editorPause.isValid() && !paused)
		{
			this.paused = true;
			Game.game.input.editorPause.invalidate();
		}

		if (Game.game.input.editorPause.isValid() && editable)
		{
			if (confirmDeleteMenu)
				confirmDeleteMenu = false;
			else if (colorMenu || sizeMenu)
			{
				sizeMenu = false;
				colorMenu = false;
			}
			else if (shopMenu || startingItemMenu)
			{
				shopMenu = false;
				startingItemMenu = false;
			}
			else if (itemMenu)
				itemMenu = false;
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
				else if (this.selectTeamMenu)
					this.selectTeamMenu = false;
				else if (this.metadataMenu)
					this.metadataMenu = false;
				else
					this.objectMenu = false;

				this.paused = false;
			}

			Game.game.input.editorPause.invalidate();
		}

		if (Game.game.input.editorObjectMenu.isValid() && editable && (!paused || objectMenu))
		{
			Game.game.input.editorObjectMenu.invalidate();
			this.paused = !this.paused;
			this.objectMenu = !this.objectMenu;
			this.selectTeamMenu = false;
			this.rotateTankMenu = false;
			this.metadataMenu = false;
		}

		for (int i = 0; i < Game.effects.size(); i++)
		{
			Game.effects.get(i).update();
		}

		if (this.paused)
			return;

		if (Game.game.input.editorRevertCamera.isValid())
		{
			zoom = 1;
			offsetX = 0;
			offsetY = 0;

			Game.game.input.editorRevertCamera.invalidate();
		}

		if (changeCameraMode)
		{
			if (Game.game.input.editorZoomOut.isPressed())
				zoom *= Math.pow(0.99, Panel.frameFrequency);
			else if (Game.game.window.validScrollDown)
			{
				Game.game.window.validScrollDown = false;
				zoom *= 0.975;
			}

			if (Game.game.input.editorZoomIn.isPressed())
				zoom *= Math.pow(1.01, Panel.frameFrequency);
			else if (Game.game.window.validScrollUp)
			{
				Game.game.window.validScrollUp = false;
				zoom *= 1.025;
			}

			if (Game.game.window.touchscreen)
				recenter.update();
		}
		else if (!selectMode && !eraseMode)
		{
			boolean up = false;
			boolean down = false;

			if (Game.game.input.editorNextType.isValid())
			{
				Game.game.input.editorNextType.invalidate();
				down = true;
			}
			else if (Game.game.window.validScrollDown)
			{
				Game.game.window.validScrollDown = false;
				down = true;
			}

			if (Game.game.input.editorPrevType.isValid())
			{
				Game.game.input.editorPrevType.invalidate();
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

				if (mouseObstacle.enableGroupID)
					mouseObstacle.setMetadata(mouseObstacleGroup + "");
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

				if (mouseObstacle.enableGroupID)
					mouseObstacle.setMetadata(mouseObstacleGroup + "");
			}

			if ((up || down) && !(up && down))
				this.movePlayer = !this.movePlayer;

			boolean right = false;
			boolean left = false;

			if (Game.game.input.editorNextObj.isValid())
			{
				Game.game.input.editorNextObj.invalidate();
				right = true;
			}

			if (Game.game.input.editorPrevObj.isValid())
			{
				Game.game.input.editorPrevObj.invalidate();
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

			if (Game.game.input.editorPrevMeta.isValid())
			{
				if (teams.size() != teamEditButtons.size())
				{
					reload();
					((ScreenLevelBuilder) Game.screen).optionsMenu = false;
					((ScreenLevelBuilder) Game.screen).paused = false;
				}
				else
				{
					Game.game.input.editorPrevMeta.invalidate();

					if (currentPlaceable == Placeable.enemyTank)
						teamNum = (teamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1);
					else if (currentPlaceable == Placeable.playerTank)
						playerTeamNum = (playerTeamNum - 1 + this.teams.size() + 1) % (this.teams.size() + 1);
					else if (currentPlaceable == Placeable.obstacle)
					{
						if (mouseObstacle.enableStacking)
						{
							mouseObstacleHeight = Math.max(mouseObstacleHeight - 0.5, 0.5);

							if (stagger)
								mouseObstacleHeight = Math.max(mouseObstacleHeight, 1);
						}
						else if (mouseObstacle.enableGroupID)
						{
							mouseObstacleGroup = Math.max(mouseObstacleGroup - 1, 0);
							mouseObstacle.setMetadata(mouseObstacleGroup + "");
						}
					}
				}
			}

			if (Game.game.input.editorNextMeta.isValid())
			{
				if (teams.size() != teamEditButtons.size())
				{
					reload();
					((ScreenLevelBuilder) Game.screen).optionsMenu = false;
					((ScreenLevelBuilder) Game.screen).paused = false;
				}
				else
				{
					Game.game.input.editorNextMeta.invalidate();

					if (currentPlaceable == Placeable.enemyTank)
						teamNum = (teamNum + 1) % (this.teams.size() + 1);
					else if (currentPlaceable == Placeable.playerTank)
						playerTeamNum = (playerTeamNum + 1) % (this.teams.size() + 1);
					else if (currentPlaceable == Placeable.obstacle)
					{
						if (mouseObstacle.enableStacking)
							mouseObstacleHeight = Math.min(mouseObstacleHeight + 0.5, 4);
						else if (mouseObstacle.enableGroupID)
						{
							mouseObstacleGroup = Math.min(mouseObstacleGroup + 1, 999999999);
							mouseObstacle.setMetadata(mouseObstacleGroup + "");
						}
					}
				}
			}
		}

		double prevZoom = zoom;
		double prevOffsetX = offsetX;
		double prevOffsetY = offsetY;

		boolean prevPanDown = panDown;
		boolean prevZoomDown = zoomDown;

		panDown = false;
		zoomDown = false;

		validZoomFingers = 0;
		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getMouseX();
			double my = Drawing.drawing.getMouseY();

			boolean[] handled = checkMouse(mx, my,
					Game.game.input.editorUse.isPressed(),
					Game.game.input.editorAction.isPressed(),
					Game.game.input.editorUse.isValid(),
					Game.game.input.editorAction.isValid());

			if (handled[0])
				Game.game.input.editorUse.invalidate();

			if (handled[1])
				Game.game.input.editorAction.invalidate();
		}
		else
		{
			boolean input = false;

			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.equals("") || p.tag.equals("levelbuilder"))
				{
					input = true;

					double mx = Drawing.drawing.toGameCoordsX(Drawing.drawing.getInterfacePointerX(p.x));
					double my = Drawing.drawing.toGameCoordsY(Drawing.drawing.getInterfacePointerY(p.y));

					boolean[] handled = checkMouse(mx, my, true, false, !p.tag.equals("levelbuilder"), false);

					if (handled[0])
						p.tag = "levelbuilder";
				}
			}

			if (!input)
				checkMouse(0, 0, false, false, false, false);

			if (validZoomFingers == 0)
				panDown = false;
		}

		if (!zoomDown && panDown)
		{
			if (prevPanDown && !prevZoomDown)
			{
				offsetX += panCurrentX - panX;
				offsetY += panCurrentY - panY;
			}

			panX = panCurrentX;
			panY = panCurrentY;
		}

		if (zoomDown)
		{
			double x = (panCurrentX + zoomCurrentX) / 2;
			double y = (panCurrentY + zoomCurrentY) / 2;
			double d = Math.sqrt(Math.pow(Drawing.drawing.toInterfaceCoordsX(panCurrentX) - Drawing.drawing.toInterfaceCoordsX(zoomCurrentX), 2)
					+ Math.pow(Drawing.drawing.toInterfaceCoordsY(panCurrentY) - Drawing.drawing.toInterfaceCoordsY(zoomCurrentY), 2));

			if (prevZoomDown)
			{
				offsetX += x - panX;
				offsetY += y - panY;
				zoom *= d / zoomDist;
			}

			panX = x;
			panY = y;
			zoomDist = d;
		}

		zoom = Math.max(0.75, Math.min(Math.max(2 / (Drawing.drawing.unzoomedScale / Drawing.drawing.interfaceScale), 1), zoom));

		offsetX = Math.min(Game.currentSizeX * Game.tile_size / 2, Math.max(-Game.currentSizeX * Game.tile_size / 2, offsetX));
		offsetY = Math.min(Game.currentSizeY * Game.tile_size / 2, Math.max(-Game.currentSizeY * Game.tile_size / 2, offsetY));

		if ((zoom == 0.75 || zoom == 2 / (Drawing.drawing.unzoomedScale / Drawing.drawing.interfaceScale)) && prevZoom != zoom)
			Drawing.drawing.playVibration("click");

		if (Math.abs(offsetX) == Game.currentSizeX * Game.tile_size / 2 && prevOffsetX != offsetX)
			Drawing.drawing.playVibration("click");

		if (Math.abs(offsetY) == Game.currentSizeY * Game.tile_size / 2 && prevOffsetY != offsetY)
			Drawing.drawing.playVibration("click");


		if (Game.game.input.editorPlay.isValid() && this.spawns.size() > 0)
		{
			this.play();
		}

		if (Game.game.input.editorDeselect.isValid())
		{
			this.clearSelection();
		}

		if (Game.game.input.editorToggleControls.isValid())
		{
			this.showControls = !this.showControls;
			Game.game.input.editorToggleControls.invalidate();
		}

		if (Game.game.input.editorSelect.isValid())
		{
			if (this.changeCameraMode)
			{
				this.selectMode = true;
				this.changeCameraMode = false;
			}
			else
			{
				this.selectMode = !this.selectMode;
				this.changeCameraMode = false;
			}

			Game.game.input.editorSelect.invalidate();
		}

		if (Game.game.input.editorCamera.isValid())
		{
			this.changeCameraMode = !this.changeCameraMode;
			Game.game.input.editorCamera.invalidate();
		}

		if (Game.game.input.editorBuild.isValid())
		{
			this.changeCameraMode = false;
			this.eraseMode = false;
			this.selectMode = false;
			Game.game.input.editorBuild.invalidate();
		}

		if (Game.game.input.editorErase.isValid())
		{
			this.changeCameraMode = false;
			this.eraseMode = true;
			this.selectMode = false;
			Game.game.input.editorBuild.invalidate();
		}

		if (Game.game.input.editorHeight.isValid() && mouseObstacle.enableStacking && currentPlaceable == Placeable.obstacle)
		{
			Game.game.input.editorHeight.invalidate();
			this.metadataShortcut.function.run();
		}

		if (Game.game.input.editorGroupID.isValid() && mouseObstacle.enableGroupID && currentPlaceable == Placeable.obstacle)
		{
			Game.game.input.editorGroupID.invalidate();
			this.metadataShortcut.function.run();
		}

		if (Game.game.input.editorTeam.isValid() && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank))
		{
			Game.game.input.editorTeam.invalidate();
			this.teamShortcut.function.run();
		}

		if (Game.game.input.editorRotate.isValid() && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank))
		{
			Game.game.input.editorRotate.invalidate();
			this.rotateShortcut.function.run();
		}

		if (redoActions.size() > 0 && redoLength != actions.size())
		{
			redoActions.clear();
			redoLength = -1;
		}

		if (Game.game.input.editorUndo.isValid() && actions.size() > 0)
		{
			Action a = actions.remove(actions.size() - 1);
			a.undo();
			redoActions.add(a);
			redoLength = actions.size();
			Game.game.input.editorUndo.invalidate();
		}

		if (Game.game.input.editorRedo.isValid() && redoActions.size() > 0)
		{
			Action a = redoActions.remove(redoActions.size() - 1);
			a.redo();
			actions.add(a);
			redoLength = actions.size();
			Game.game.input.editorRedo.invalidate();
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

	public boolean[] checkMouse(double mx, double my, boolean left, boolean right, boolean validLeft, boolean validRight)
	{
		boolean[] handled = new boolean[]{false, false};

		double posX = Math.round((mx - offsetX) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
		double posY = Math.round((my - offsetY) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
		mouseTank.posX = posX;
		mouseTank.posY = posY;
		mouseObstacle.posX = posX;
		mouseObstacle.posY = posY;

		if (changeCameraMode)
		{
			if (validLeft)
			{
				if (validZoomFingers == 0)
				{
					panDown = true;
					panCurrentX = mx;
					panCurrentY = my;
				}
				else if (validZoomFingers == 1)
				{
					zoomDown = true;
					zoomCurrentX = mx;
					zoomCurrentY = my;
				}

				validZoomFingers++;
			}
		}
		else if (selectMode)
		{
			if (!selection)
				selectAdd = true;

			boolean pressed = left || right;
			boolean valid = validLeft || validRight;

			if (valid)
			{
				selectX1 = clampTileX(mouseObstacle.posX);
				selectY1 = clampTileY(mouseObstacle.posY);
				selectHeld = true;
				handled[0] = true;
				handled[1] = true;

				Drawing.drawing.playVibration("selectionChanged");
			}

			if (pressed && selectHeld)
			{
				double prevSelectX2 = selectX2;
				double prevSelectY2 = selectY2;

				selectX2 = clampTileX(mouseObstacle.posX);
				selectY2 = clampTileY(mouseObstacle.posY);

				if (prevSelectX2 != selectX2 || prevSelectY2 != selectY2)
					Drawing.drawing.playVibration("selectionChanged");
			}

			if (selectSquare || Game.game.input.editorHoldSquare.isPressed())
			{
				double size = Math.min(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
				selectX2 = Math.signum(selectX2 - selectX1) * size + selectX1;
				selectY2 = Math.signum(selectY2 - selectY1) * size + selectY1;
			}

			if (!pressed && selectHeld)
			{
				Drawing.drawing.playVibration("click");
				selectHeld = false;

				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				ArrayList<Integer> px = new ArrayList<>();
				ArrayList<Integer> py = new ArrayList<>();

				for (double x = lowX; x <= highX; x += Game.tile_size)
				{
					for (double y = lowY; y <= highY; y += Game.tile_size)
					{
						if (selectedTiles[(int)(x / Game.tile_size)][(int)(y / Game.tile_size)] == selectInverted)
						{
							px.add((int)(x / Game.tile_size));
							py.add((int)(y / Game.tile_size));
						}

						selectedTiles[(int)(x / Game.tile_size)][(int)(y / Game.tile_size)] = !selectInverted;
					}
				}

				this.actions.add(new Action.ActionSelectTiles(this, !selectInverted, px, py));

				this.refreshSelection();
			}
			else
			{
				if (selection && Game.game.input.editorSelectAddToggle.isValid())
				{
					Game.game.input.editorSelectAddToggle.invalidate();
					selectAddToggle.function.run();
				}

				selectInverted = selection && ((!selectAdd && !right) || (selectAdd && right));
			}

			if (Game.game.input.editorLockSquare.isValid())
			{
				Game.game.input.editorLockSquare.invalidate();
				selectSquareToggle.function.run();
			}
		}
		else
		{
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

			int x = (int) (mouseObstacle.posX / Game.tile_size);
			int y = (int) (mouseObstacle.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				if (selectedTiles[x][y] && (validLeft || validRight) && !(this.currentPlaceable == Placeable.playerTank && this.movePlayer))
				{
					double ox = mouseObstacle.posX;
					double oy = mouseObstacle.posY;

					ArrayList<Action> actions = this.actions;
					this.actions = new ArrayList<>();

					for (int i = 0; i < selectedTiles.length; i++)
					{
						for (int j = 0; j < selectedTiles[i].length; j++)
						{
							if (selectedTiles[i][j])
							{
								mouseObstacle.posX = (i + 0.5) * Game.tile_size;
								mouseObstacle.posY = (j + 0.5) * Game.tile_size;

								mouseTank.posX = mouseObstacle.posX;
								mouseTank.posY = mouseObstacle.posY;

								handled = handlePlace(handled, left, right, validLeft, validRight, true);
							}
						}
					}

					if (this.actions.size() > 0)
					{
						Action a = new Action.ActionGroup(this, this.actions);
						actions.add(a);
						Drawing.drawing.playVibration("click");
					}

					this.actions = actions;


					mouseObstacle.posX = ox;
					mouseObstacle.posY = oy;

					mouseTank.posX = ox;
					mouseTank.posY = oy;
				}
				else
					handled = handlePlace(handled, left, right, validLeft, validRight, false);
			}
		}

		return handled;
	}

	public boolean[] handlePlace(boolean[] handled, boolean left, boolean right, boolean validLeft, boolean validRight, boolean batch)
	{
		if (mouseTank.posX > 0 && mouseTank.posY > 0 && mouseTank.posX < Game.tile_size * Game.currentSizeX && mouseTank.posY < Game.tile_size * Game.currentSizeY)
		{
			if (right || (eraseMode && left))
			{
				boolean skip = false;

				if (validRight || (eraseMode && validLeft))
				{
					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);
						if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && !(this.spawns.contains(m) && this.spawns.size() <= 1))
						{
							skip = true;

							if (m instanceof TankSpawnMarker)
							{
								this.spawns.remove(m);
								this.actions.add(new Action.ActionPlayerSpawn(this, (TankSpawnMarker) m, false));
							}
							else
								this.actions.add(new Action.ActionTank((Tank) m, false));

							Game.removeMovables.add(m);

							if (!batch)
							{
								Drawing.drawing.playVibration("click");

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
						this.actions.add(new Action.ActionObstacle(m, false));
						Game.removeObstacles.add(m);

						if (!batch)
							Drawing.drawing.playVibration("click");

						break;
					}
				}

				if (!batch && !Game.game.window.touchscreen && !skip && (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && validRight)
				{
					this.mouseTankOrientation += 1;
					this.mouseTankOrientation = this.mouseTankOrientation % 4;
				}

				if (Game.game.window.touchscreen)
					handled[0] = true;

				handled[1] = true;
			}

			if (!eraseMode && clickCooldown <= 0 && (validLeft || (left && currentPlaceable == Placeable.obstacle && this.mouseObstacle.draggable)))
			{
				boolean skip = false;

				if (mouseObstacle.tankCollision || currentPlaceable != Placeable.obstacle)
				{
					for (int i = 0; i < Game.movables.size(); i++)
					{
						Movable m = Game.movables.get(i);
						if (m.posX == mouseTank.posX && m.posY == mouseTank.posY)
						{
							skip = true;
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
						{
							if (m.tankCollision || mouseObstacle.tankCollision || m.getClass() == mouseObstacle.getClass() || (mouseObstacle.isSurfaceTile && m.isSurfaceTile))
							{
								skip = true;
								break;
							}
						}
						else
						{
							this.actions.add(new Action.ActionObstacle(m, false));
							Game.removeObstacles.add(m);
						}
					}
				}

				if (!skip)
				{
					if (currentPlaceable == Placeable.enemyTank)
					{
						Tank t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
						t.team = mouseTank.team;
						this.actions.add(new Action.ActionTank(t, true));
						Game.movables.add(t);

						if (!batch)
							Drawing.drawing.playVibration("click");
					}
					else if (currentPlaceable == Placeable.playerTank)
					{
						ArrayList<TankSpawnMarker> spawnsClone = (ArrayList<TankSpawnMarker>) spawns.clone();
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

						if (this.movePlayer)
							this.actions.add(new Action.ActionMovePlayer(this, spawnsClone, t));
						else
							this.actions.add(new Action.ActionPlayerSpawn(this, t, true));

						Game.movables.add(t);

						if (!batch)
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

						if (o.enableStacking)
						{
							o.stackHeight = mouseObstacleHeight;

							if (this.stagger)
							{
								if ((((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == 1 && !this.oddStagger)
										|| (((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == 0 && this.oddStagger))
									o.stackHeight -= 0.5;
							}
						}
						else if (o.enableGroupID)
							o.setMetadata("" + mouseObstacleGroup);

						mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(o.posX, o.posY);

						if (mouseObstacle.enableGroupID)
							mouseObstacle.setMetadata(mouseObstacleGroup + "");

						this.actions.add(new Action.ActionObstacle(o, true));
						Game.obstacles.add(o);

						if (!batch)
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
		double[][][] obstacles = new double[Game.registryObstacle.obstacleEntries.size()][width][height];

		for (int i = 0; i < obstacles.length; i++)
		{
			for (int j = 0; j < obstacles[i].length; j++)
			{
				for (int k = 0; k < obstacles[i][j].length; k++)
				{
					obstacles[i][j][k] = -1;
				}
			}
		}

		for (int h = 0; h < Game.registryObstacle.obstacleEntries.size(); h++)
		{
			Obstacle obs = Game.registryObstacle.obstacleEntries.get(h).getObstacle(0, 0);

			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);
				int x = (int) (o.posX / Game.tile_size);
				int y = (int) (o.posY / Game.tile_size);

				if (x < obstacles[h].length && x >= 0 && y < obstacles[h][0].length && y >= 0 && o.name.equals(Game.registryObstacle.getEntry(h).name))
				{
					obstacles[h][x][y] = o.stackHeight;

					if (o.enableGroupID)
						obstacles[h][x][y] = o.groupID;

					unmarked.remove(o);
				}

				//level += x + "-" + y + ",";
			}

			//compression
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					if (obstacles[h][i][j] >= 0)
					{
						double stack = obstacles[h][i][j];

						int xLength = 0;

						while (true)
						{
							xLength += 1;

							if (i + xLength >= obstacles[h].length)
								break;
							else if (obstacles[h][i + xLength][j] != stack)
								break;
						}


						int yLength = 0;

						while (true)
						{
							yLength += 1;

							if (j + yLength >= obstacles[h][0].length)
								break;
							else if (obstacles[h][i][j + yLength] != stack)
								break;
						}

						String name = "";
						String obsName = Game.registryObstacle.obstacleEntries.get(h).name;

						if (!obsName.equals("normal") || stack != 1)
							name = "-" + obsName;

						if (xLength >= yLength)
						{
							if (xLength == 1)
								level.append(i).append("-").append(j).append(name);
							else
								level.append(i).append("...").append(i + xLength - 1).append("-").append(j).append(name);

							if ((obs.enableStacking && stack != 1) || (obs.enableGroupID && stack != 0))
								level.append("-").append(stack);

							level.append(",");

							for (int z = 0; z < xLength; z++)
							{
								obstacles[h][i + z][j] = -1;
							}
						}
						else
						{
							level.append(i).append("-").append(j).append("...").append(j + yLength - 1).append(name);

							if ((obs.enableStacking && stack != 1) || (obs.enableGroupID && stack != 0))
								level.append("-").append(stack);

							level.append(",");

							for (int z = 0; z < yLength; z++)
							{
								obstacles[h][i][j + z] = -1;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < unmarked.size(); i++)
		{
			level.append((int)(unmarked.get(i).posX / Game.tile_size)).append("-").append((int)(unmarked.get(i).posY / Game.tile_size));
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
				Tank t = (Tank) Game.movables.get(i);
				int x = (int) (t.posX / Game.tile_size);
				int y = (int) (t.posY / Game.tile_size);
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

		for (Item i: this.shop)
			i.exportProperties();

		for (Item i: this.startingItems)
			i.exportProperties();

		if (this.startingCoins > 0)
			level.append("\ncoins\n").append(this.startingCoins);

		if (!this.shop.isEmpty())
		{
			level.append("\nshop");

			for (Item i : this.shop)
				level.append("\n").append(i.toString());
		}

		if (!this.startingItems.isEmpty())
		{
			level.append("\nitems");

			for (Item i : this.startingItems)
				level.append("\n").append(i.toString());
		}

		Game.currentLevelString = level.toString();

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name);
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

		this.colorRed.enabled = false;
		this.colorGreen.enabled = false;
		this.colorBlue.enabled = false;

		this.colorVarRed.enabled = false;
		this.colorVarGreen.enabled = false;
		this.colorVarBlue.enabled = false;

		this.sizeX.enabled = false;
		this.sizeY.enabled = false;

		Panel.selectedTextBox = null;

		ScreenLevelBuilder s = new ScreenLevelBuilder(name);
		Game.loadLevel(Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name), s);

		s.currentPlaceable = this.currentPlaceable;

		s.optionsMenu = true;
		s.tankNum = tankNum;
		s.obstacleNum = obstacleNum;
		s.teamNum = teamNum;
		s.mouseTank = mouseTank;
		s.mouseObstacle = mouseObstacle;
		s.mouseObstacleGroup = mouseObstacleGroup;
		s.mouseObstacleHeight = mouseObstacleHeight;
		s.stagger = stagger;
		s.oddStagger = oddStagger;

		s.colorRed.glowEffects = this.colorRed.glowEffects;
		s.colorGreen.glowEffects = this.colorGreen.glowEffects;
		s.colorBlue.glowEffects = this.colorBlue.glowEffects;
		s.colorVarRed.glowEffects = this.colorVarRed.glowEffects;
		s.colorVarGreen.glowEffects = this.colorVarGreen.glowEffects;
		s.colorVarBlue.glowEffects = this.colorVarBlue.glowEffects;

		s.sizeX.glowEffects = this.sizeX.glowEffects;
		s.sizeY.glowEffects = this.sizeY.glowEffects;

		for (int i = 0; i < Game.movables.size(); i++)
		{
			Movable m = Game.movables.get(i);

			if (m instanceof Tank)
				((Tank) m).drawAge = Game.tile_size;
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

		for (Effect e: Game.tracks)
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
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
				Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
			}

			for (IDrawable d: this.drawables[i])
			{
				d.draw();

				if (d instanceof Movable)
					((Movable) d).drawTeam();
			}

			if (Game.superGraphics)
			{
				for (int j = 0; j < this.drawables[i].size(); j++)
				{
					IDrawable d = this.drawables[i].get(j);

					if (d instanceof IDrawableWithGlow)
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			drawables[i].clear();
		}

		if (!paused && !Game.game.window.touchscreen && !changeCameraMode && !selectMode)
		{
			if (eraseMode)
			{
				Drawing.drawing.setColor(255, 0, 0, 64);

				if (Game.enable3d)
					Drawing.drawing.fillBox(mouseObstacle.posX, mouseObstacle.posY, 0, Game.tile_size, Game.tile_size, Game.tile_size, (byte) 64);
				else
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else if (currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)
			{
				mouseTank.drawOutline();
				mouseTank.drawTeam();

				if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
				{
					Drawing.drawing.setColor(255, 255, 255, 127);
					Drawing.drawing.drawImage("player_spawn.png", mouseTank.posX, mouseTank.posY, mouseTank.size, mouseTank.size);
				}
			}
			else if (currentPlaceable == Placeable.obstacle)
			{
				if (mouseObstacle.enableStacking && Game.enable3d)
				{
					Drawing.drawing.setColor(255, 255, 255, 64);
					Drawing.drawing.fillBox(mouseObstacle.posX, mouseObstacle.posY, 0, Game.tile_size, Game.tile_size, mouseObstacleHeight * Game.tile_size, (byte) 64);
				}

				mouseObstacle.drawOutline();

				if (mouseObstacleHeight != 1.0 && mouseObstacle.enableStacking)
				{
					Drawing.drawing.setFontSize(16);
					Drawing.drawing.drawText(mouseObstacle.posX, mouseObstacle.posY, mouseObstacleHeight + "");
				}
			}
		}

		double extra = Math.sin(System.currentTimeMillis() * Math.PI / 1000.0) * 25;
		Drawing.drawing.setColor(230 + extra, 230 + extra, 230 + extra, 128);

		for (int i = 0; i < selectedTiles.length; i++)
		{
			for (int j = 0; j < selectedTiles[i].length; j++)
			{
				if (selectedTiles[i][j])
					Drawing.drawing.fillRect((i + 0.5) * Game.tile_size, (j + 0.5) * Game.tile_size, Game.tile_size, Game.tile_size);
			}
		}

		if (changeCameraMode && !paused)
		{
			Drawing.drawing.setColor(0, 0, 0, 127);

			Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 120, 500, 150);

			Drawing.drawing.setColor(255, 255, 255);

			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 120 - 30, "Drag to pan");

			if (Game.game.window.touchscreen)
			{
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 120 - 0, "Pinch to zoom");
				recenter.draw();
			}
			else
			{
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 120 - 0, "Scroll or press " + Game.game.input.editorZoomIn.getInputs() + " or " + Game.game.input.editorZoomOut.getInputs() + " to zoom");
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 120 + 30, "Press " + Game.game.input.editorRevertCamera.getInputs() + " to re-center");
			}
		}

		if (selectMode && !changeCameraMode && !this.paused)
		{
			if (!selectInverted)
				Drawing.drawing.setColor(255, 255, 255, 127);
			else
				Drawing.drawing.setColor(0, 0, 0, 127);

			if (!selectHeld)
			{
				if (!Game.game.window.touchscreen)
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else
			{
				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				for (double x = lowX; x <= highX; x += Game.tile_size)
				{
					for (double y = lowY; y <= highY; y += Game.tile_size)
					{
						Drawing.drawing.fillRect(x, y, Game.tile_size, Game.tile_size);
					}
				}
			}
		}

		if (Game.screen instanceof IOverlayScreen)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);
		}

		if (this.paused && !(Game.screen instanceof IOverlayScreen))
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			//Drawing.drawing.setColor(0, 0, 0, 127);

			Game.game.window.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

			if (this.confirmDeleteMenu)
			{
				Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
				Drawing.drawing.setInterfaceFontSize(24);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Are you sure you want to delete the level?");

				this.cancelDelete.draw();
				this.confirmDelete.draw();
			}
			else if (this.objectMenu)
			{
				if (this.selectTeamMenu)
				{
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
					Drawing.drawing.setInterfaceFontSize(24);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, this.teamSelectTitle);

					this.teamSelectList.draw();

					back7.draw();
				}
				else if (this.rotateTankMenu)
				{
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
					Drawing.drawing.setInterfaceFontSize(24);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Select tank orientation");

					this.rotateUp.draw();
					this.rotateLeft.draw();
					this.rotateDown.draw();
					this.rotateRight.draw();

					this.back8.draw();
				}
				else if (this.metadataMenu)
				{
					if (mouseObstacle.enableStacking)
					{
						Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
						Drawing.drawing.setInterfaceFontSize(24);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Block height");

						Drawing.drawing.setColor(0, 0, 0, 127);

						Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 500, 150);

						Drawing.drawing.setColor(255, 255, 255);
						Drawing.drawing.setInterfaceFontSize(36);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, mouseObstacleHeight + "");

						this.increaseHeight.draw();
						this.decreaseHeight.draw();
						this.staggering.draw();

						Drawing.drawing.setInterfaceFontSize(12);
						Drawing.drawing.drawInterfaceText(staggering.posX, staggering.posY - 40, "Staggering");

					}
					else if (mouseObstacle.enableGroupID)
					{
						Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
						Drawing.drawing.setInterfaceFontSize(24);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Group ID");

						Drawing.drawing.setColor(0, 0, 0, 127);

						Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 600, 75);

						this.increaseID.draw();
						this.decreaseID.draw();
						this.groupID.draw();
					}

					this.back9.draw();

				}
				else
				{
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
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

						if (mouseObstacle.enableStacking || mouseObstacle.enableGroupID)
							this.metadataButton.draw();

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
				Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
				Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Level menu");
			}
			else
			{
				if (this.sizeMenu)
				{
					this.sizeY.draw();
					this.sizeX.draw();
					this.back3.draw();
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 150, "Level size");
				}
				else if (this.colorMenu)
				{
					this.colorBlue.draw();
					this.colorGreen.draw();
					this.colorRed.draw();
					this.colorVarBlue.draw();
					this.colorVarGreen.draw();
					this.colorVarRed.draw();
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
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
								teamBlue.draw();
								teamGreen.draw();
								teamRed.draw();
							}
							teamColorEnabled.draw();
							Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
							Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Team color: " + this.selectedTeam.name);
						}
						else
						{
							back5.draw();
							teamName.draw();
							teamColor.draw();
							deleteTeam.draw();
							teamFriendlyFire.draw();
							Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
							Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, this.selectedTeam.name);
						}
					}
					else
					{
						this.teamEditList.draw();

						back4.draw();
						newTeam.draw();

						Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Teams");
					}
				}
				else if (this.itemMenu)
				{
					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
					Drawing.drawing.setInterfaceFontSize(24);

					if (this.startingItemMenu)
					{
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Starting items");
						this.startingItemsList.draw();
						this.back10.draw();
						this.addItem.draw();
					}
					else if (this.shopMenu)
					{
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 270, "Shop items");
						this.shopList.draw();
						this.back10.draw();
						this.addItem.draw();
					}
					else
					{
						Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Items");
						this.editCoins.draw();
						this.editShop.draw();
						this.editStartingItems.draw();
						this.back11.draw();
					}
				}
				else
				{
					this.levelName.draw();
					this.back1.draw();
					this.itemOptions.draw();
					this.colorOptions.draw();
					this.sizeOptions.draw();
					this.teamsOptions.draw();

					Drawing.drawing.setColor(fontBrightness, fontBrightness, fontBrightness);
					Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 240, "Level options");
				}
			}
		}

		if (!paused && showControls)
		{
			pause.image = "pause.png";
			pause.imageSizeX = 40 * controlsSizeMultiplier;
			pause.imageSizeY = 40 * controlsSizeMultiplier;

			menu.image = "menu.png";
			menu.imageSizeX = 50 * controlsSizeMultiplier;
			menu.imageSizeY = 50 * controlsSizeMultiplier;

			playControl.image = "play.png";
			playControl.imageSizeX = 30 * controlsSizeMultiplier;
			playControl.imageSizeY = 30 * controlsSizeMultiplier;

			place.image = "pencil.png";
			place.imageSizeX = 40 * controlsSizeMultiplier;
			place.imageSizeY = 40 * controlsSizeMultiplier;

			erase.image = "eraser.png";
			erase.imageSizeX = 40 * controlsSizeMultiplier;
			erase.imageSizeY = 40 * controlsSizeMultiplier;

			panZoom.image = "zoom_pan.png";
			panZoom.imageSizeX = 40 * controlsSizeMultiplier;
			panZoom.imageSizeY = 40 * controlsSizeMultiplier;

			select.image = "select.png";
			select.imageSizeX = 40 * controlsSizeMultiplier;
			select.imageSizeY = 40 * controlsSizeMultiplier;

			undo.image = "undo.png";
			undo.imageSizeX = 40 * controlsSizeMultiplier;
			undo.imageSizeY = 40 * controlsSizeMultiplier;

			redo.image = "redo.png";
			redo.imageSizeX = 40 * controlsSizeMultiplier;
			redo.imageSizeY = 40 * controlsSizeMultiplier;

			selectClear.image = "select_clear.png";
			selectClear.imageSizeX = 40 * controlsSizeMultiplier;
			selectClear.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectAdd)
				selectAddToggle.image = "select_add.png";
			else
				selectAddToggle.image = "select_remove.png";

			selectAddToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectAddToggle.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectSquare)
				selectSquareToggle.image = "square_locked.png";
			else
				selectSquareToggle.image = "square_unlocked.png";

			selectSquareToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectSquareToggle.imageSizeY = 40 * controlsSizeMultiplier;

			if (mouseObstacle.enableStacking)
				metadataShortcut.image = "obstacle_height.png";
			else if (mouseObstacle.enableGroupID)
				metadataShortcut.image = "id.png";

			metadataShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			metadataShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			rotateShortcut.image = "rotate_tank.png";
			rotateShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			rotateShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			teamShortcut.image = "team.png";
			teamShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			teamShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			playControl.draw();
			menu.draw();
			pause.draw();

			panZoom.draw();
			select.draw();
			erase.draw();
			place.draw();

			undo.draw();
			redo.draw();

			if (selection)
				selectClear.draw();

			if (selectMode)
			{
				selectSquareToggle.draw();

				if (selection)
					selectAddToggle.draw();
			}
			else if (!eraseMode && !changeCameraMode)
			{
				if (currentPlaceable == Placeable.obstacle && (mouseObstacle.enableStacking || mouseObstacle.enableGroupID))
					metadataShortcut.draw();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					rotateShortcut.draw();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					teamShortcut.draw();
			}
		}
	}

	public void play()
	{
		this.save();
		this.replaceSpawns();

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		for (Obstacle o: Game.obstacles)
		{
			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (o.bulletCollision && x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				Game.game.solidGrid[x][y] = true;
			}
		}

		Game.screen = new ScreenGame(this.name);
		Game.player.hotbar.coins = this.startingCoins;
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
					tank.drawAge = Game.tile_size;

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

	public void clearSelection()
	{
		selection = false;

		ArrayList<Integer> x = new ArrayList<>();
		ArrayList<Integer> y = new ArrayList<>();

		for (int i = 0; i < selectedTiles.length; i++)
		{
			for (int j = 0; j < selectedTiles[i].length; j++)
			{
				if (selectedTiles[i][j])
				{
					x.add(i);
					y.add(j);
				}

				selectedTiles[i][j] = false;
			}
		}

		this.actions.add(new Action.ActionSelectTiles(this, false, x, y));

	}

	public void refreshSelection()
	{
		selection = false;

		for (int x = 0; x < Game.currentSizeX; x++)
		{
			for (int y = 0; y < Game.currentSizeY; y++)
			{
				if (selectedTiles[x][y])
					selection = true;
			}
		}
	}

	public double clampTileX(double x)
	{
		return Math.max(Game.tile_size / 2, Math.min((Game.currentSizeX - 0.5) * Game.tile_size, x));
	}

	public double clampTileY(double y)
	{
		return Math.max(Game.tile_size / 2, Math.min((Game.currentSizeY - 0.5) * Game.tile_size, y));
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

	@Override
	public void removeItem(Item i)
	{
		if (this.shopMenu)
		{
			this.shop.remove(i);
			this.refreshItemButtons(this.shop, this.shopList);
		}
		else if (this.startingItemMenu)
		{
			this.startingItems.remove(i);
			this.refreshItemButtons(this.startingItems, this.startingItemsList);
		}
	}

	@Override
	public void refreshItems()
	{
		if (this.shopMenu)
			this.refreshItemButtons(this.shop, this.shopList);
		else if (this.startingItemMenu)
			this.refreshItemButtons(this.startingItems, this.startingItemsList);
	}

	public enum Placeable {enemyTank, playerTank, obstacle}

	@Override
	public double getOffsetX()
	{
		return offsetX - Game.currentSizeX * Game.tile_size / 2 + Panel.windowWidth / Drawing.drawing.scale / 2;
	}

	@Override
	public double getOffsetY()
	{
		return offsetY - Game.currentSizeY * Game.tile_size / 2 + (Panel.windowHeight - Drawing.drawing.statsHeight) / Drawing.drawing.scale / 2;
	}

	@Override
	public double getScale()
	{
		return Drawing.drawing.unzoomedScale * zoom;
	}

	public void refreshItemButtons(ArrayList<Item> items, ButtonList buttons)
	{
		buttons.buttons.clear();

		for (int i = 0; i < items.size(); i++)
		{
			int j = i;

			Button b = new Button(0, 0, this.objWidth, this.objHeight, items.get(i).name, new Runnable()
			{
				@Override
				public void run()
				{
					ScreenEditItem s = new ScreenEditItem(items.get(j), (IItemScreen) Game.screen);
					s.drawBehindScreen = true;
					Game.screen = s;
				}
			});

			b.image = items.get(j).icon;
			b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
			b.imageSizeX = b.sizeY;
			b.imageSizeY = b.sizeY;

			int p = items.get(i).price;
			String price = p + " ";
			if (p == 0)
				price = "Free!";
			else if (p == 1)
				price += "coin";
			else
				price += "coins";

			b.subtext = price;

			buttons.buttons.add(b);
		}

		buttons.sortButtons();
	}

	static abstract class Action
	{
		public abstract void undo();
		public abstract void redo();

		static class ActionObstacle extends Action
		{
			public boolean add;
			public Obstacle obstacle;

			public ActionObstacle(Obstacle o, boolean add)
			{
				this.obstacle = o;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
					Game.removeObstacles.add(this.obstacle);
				else
					Game.obstacles.add(this.obstacle);
			}

			@Override
			public void redo()
			{
				if (!add)
					Game.removeObstacles.add(this.obstacle);
				else
					Game.obstacles.add(this.obstacle);
			}
		}

		static class ActionTank extends Action
		{
			public boolean add;
			public Tank tank;

			public ActionTank(Tank t, boolean add)
			{
				this.tank = t;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
					Game.removeMovables.add(this.tank);
				else
					Game.movables.add(this.tank);
			}

			@Override
			public void redo()
			{
				if (!add)
					Game.removeMovables.add(this.tank);
				else
					Game.movables.add(this.tank);
			}
		}

		static class ActionPlayerSpawn extends Action
		{
			public ScreenLevelBuilder screenLevelBuilder;
			public boolean add;
			public TankSpawnMarker tank;

			public ActionPlayerSpawn(ScreenLevelBuilder s, TankSpawnMarker t, boolean add)
			{
				this.screenLevelBuilder = s;
				this.tank = t;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelBuilder.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelBuilder.spawns.add(this.tank);
				}
			}

			@Override
			public void redo()
			{
				if (!add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelBuilder.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelBuilder.spawns.add(this.tank);
				}
			}
		}

		static class ActionMovePlayer extends Action
		{
			public ScreenLevelBuilder screenLevelBuilder;
			public ArrayList<TankSpawnMarker> oldSpawns;
			public TankSpawnMarker newSpawn;

			public ActionMovePlayer(ScreenLevelBuilder s, ArrayList<TankSpawnMarker> o, TankSpawnMarker n)
			{
				this.screenLevelBuilder = s;
				this.oldSpawns = o;
				this.newSpawn = n;
			}

			@Override
			public void undo()
			{
				Game.removeMovables.add(newSpawn);
				screenLevelBuilder.spawns.clear();

				for (TankSpawnMarker t: oldSpawns)
				{
					screenLevelBuilder.spawns.add(t);
					Game.movables.add(t);
				}
			}

			@Override
			public void redo()
			{
				Game.removeMovables.addAll(oldSpawns);

				screenLevelBuilder.spawns.clear();

				Game.movables.add(newSpawn);
				screenLevelBuilder.spawns.add(newSpawn);
			}
		}

		static class ActionSelectTiles extends Action
		{
			public ScreenLevelBuilder screenLevelBuilder;
			public ArrayList<Integer> x;
			public ArrayList<Integer> y;
			public boolean select;

			public ActionSelectTiles(ScreenLevelBuilder s, boolean select, ArrayList<Integer> x, ArrayList<Integer> y)
			{
				this.screenLevelBuilder = s;
				this.select = select;
				this.x = x;
				this.y = y;
			}

			@Override
			public void undo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelBuilder.selectedTiles[this.x.get(i)][this.y.get(i)] = !select;
				}

				screenLevelBuilder.refreshSelection();
			}

			@Override
			public void redo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelBuilder.selectedTiles[this.x.get(i)][this.y.get(i)] = select;
				}

				screenLevelBuilder.refreshSelection();
			}
		}

		static class ActionGroup extends Action
		{
			public ScreenLevelBuilder screenLevelBuilder;
			public ArrayList<Action> actions;

			public ActionGroup(ScreenLevelBuilder s, ArrayList<Action> actions)
			{
				this.screenLevelBuilder = s;
				this.actions = actions;
			}

			@Override
			public void undo()
			{
				for (Action a: this.actions)
					a.undo();
			}

			@Override
			public void redo()
			{
				for (Action a: this.actions)
					a.redo();
			}
		}
	}
}

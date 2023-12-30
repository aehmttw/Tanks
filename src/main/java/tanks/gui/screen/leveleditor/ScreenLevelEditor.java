package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import basewindow.InputPoint;
import tanks.*;
import tanks.network.event.INetworkEvent;
import tanks.gui.*;
import tanks.gui.screen.*;
import tanks.hotbar.item.Item;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleUnknown;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ScreenLevelEditor extends Screen implements ILevelPreviewScreen
{
	public ArrayList<Action> actions = new ArrayList<>();
	public ArrayList<Action> redoActions = new ArrayList<>();
	public int redoLength = -1;

	public Placeable currentPlaceable = Placeable.enemyTank;

	public int tankPage = 0;
	public int obstaclePage = 0;

	public int tankNum = 0;
	public int obstacleNum = 0;
	public int teamNum = 1;
	public int playerTeamNum = 0;
	public Tank mouseTank = Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	public int mouseTankOrientation = 0;
	public Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	public double mouseObstacleHeight = 1;
	public double mouseObstacleStartHeight = 0;
	public boolean stagger = false;
	public boolean oddStagger = false;
	public int mouseObstacleGroup = 0;
	public boolean paused = false;
	public boolean objectMenu = false;

	public double clickCooldown = 0;
	public ArrayList<Team> teams = new ArrayList<>();
	public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

	public Level level;
	public String name;
	public boolean movePlayer = true;
	public boolean eraseMode = false;
	public boolean changeCameraMode = false;
	public boolean selectMode = false;
	public boolean pasteMode = false;
	public boolean symmetrySelectMode = false;

	public double selectX1;
	public double selectY1;
	public double selectX2;
	public double selectY2;

	public enum SymmetryType {none, flipHorizontal, flipVertical, flipBoth, flip8, rot180, rot90};
	public SymmetryType symmetryType = SymmetryType.none;
	public double symmetryX1;
	public double symmetryY1;
	public double symmetryX2;
	public double symmetryY2;

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

	public HashSet<String> prevTankMusics = new HashSet<>();
	public HashSet<String> tankMusics = new HashSet<>();

	public ArrayList<Object> clipboard = new ArrayList<>();

	public double fontBrightness = 0;

	Button pause = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.screen = new OverlayEditorMenu(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Level menu (%s)", Game.game.input.editorPause.getInputs()
	);

	Button menu = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		objectMenu = true;
		Game.screen = new OverlayObjectMenu(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Object menu (%s)", Game.game.input.editorObjectMenu.getInputs()
	);

	public Button recenter = new Button(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 1.5, 300, 35, "Re-center", new Runnable()
	{
		@Override
		public void run()
		{
			zoom = 1;
			offsetX = 0;
			offsetY = 0;
		}
	}
	);

	Button playControl = new Button(0, -1000, 70, 70, "", () ->
	{
		clickCooldown = 20;
		play();
	}, "Play (%s)", Game.game.input.editorPlay.getInputs()
	);

	Button place = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = false;
		changeCameraMode = false;
		eraseMode = false;
		pasteMode = false;
	}, "Build (%s)", Game.game.input.editorBuild.getInputs()
	);

	Button erase = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = false;
		changeCameraMode = false;
		eraseMode = true;
		pasteMode = false;

	}, "Erase (%s)", Game.game.input.editorErase.getInputs()
	);

	Button panZoom = new Button(0, -1000, 70, 70, "", () -> changeCameraMode = true, "Adjust camera (%s)", Game.game.input.editorCamera.getInputs());

	Button select = new Button(0, -1000, 70, 70, "", () ->
	{
		selectMode = true;
		changeCameraMode = false;
		pasteMode = false;
	}, "Select (%s)", Game.game.input.editorSelect.getInputs()
	);

	Button undo = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		Action a = actions.remove(actions.size() - 1);
		a.undo();
		redoActions.add(a);
		redoLength = actions.size();
	}, "Undo (%s)", Game.game.input.editorUndo.getInputs()
	);

	Button redo = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		Action a = redoActions.remove(redoActions.size() - 1);
		a.redo();
		actions.add(a);
		redoLength = actions.size();
	}, "Redo (%s)", Game.game.input.editorRedo.getInputs()
	);

	Button copy = new Button(0, -1000, 70, 70, "", () ->
	{
		this.copy(false);
	}, "Copy (%s)", Game.game.input.editorCopy.getInputs());

	Button cut = new Button(0, -1000, 70, 70, "", () ->
	{
		this.copy(true);
	}, "Cut (%s)", Game.game.input.editorCut.getInputs());

	Button paste = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectMode = false;
		changeCameraMode = false;
		eraseMode = false;
		pasteMode = true;
	}, "Paste (%s)", Game.game.input.editorPaste.getInputs() );

	Button rotateShortcut = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.screen = new OverlayRotateTank(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Tank orientation (%s)", Game.game.input.editorRotate.getInputs()
	);

	Button teamShortcut = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.screen = new OverlaySelectTeam(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Tank team (%s)", Game.game.input.editorTeam.getInputs()
	);

	Button heightShortcut = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.screen = new OverlayBlockHeight(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Block height (%s)", Game.game.input.editorHeight.getInputs()
	);

	Button groupShortcut = new Button(0, -1000, 70, 70, "", () ->
	{
		paused = true;
		Game.screen = new OverlayBlockGroupID(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Block group ID (%s)", Game.game.input.editorGroupID.getInputs()
	);

	Button selectSquareToggle = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectSquare = !selectSquare;
	}, "Lock square selecting (Hold: %s, Toggle: %s)", Game.game.input.editorHoldSquare.getInputs(), Game.game.input.editorLockSquare.getInputs()
	);

	Button selectAddToggle = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		selectAdd = !selectAdd;
	}, "Toggle select/deselect (%s)", Game.game.input.editorSelectAddToggle.getInputs()
	);

	Button selectClear = new Button(0, -1000, 70, 70, "", () ->
	{
		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();

		clearSelection();
	}, "Clear selection (%s)", Game.game.input.editorDeselect.getInputs()
	);

	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	public ScreenLevelEditor(String lvlName, Level level)
	{
		this.selfBatch = false;
		this.drawDarkness = false;

		this.music = "battle_editor.ogg";
		this.musicID = "editor";

		this.allowClose = false;

		if (Game.game.window.touchscreen)
			controlsSizeMultiplier = 1.0;

		this.level = level;

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

		paste.sizeX *= controlsSizeMultiplier;
		paste.sizeY *= controlsSizeMultiplier;
		paste.fullInfo = true;

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

		heightShortcut.sizeX *= controlsSizeMultiplier;
		heightShortcut.sizeY *= controlsSizeMultiplier;
		heightShortcut.fullInfo = true;

		groupShortcut.sizeX *= controlsSizeMultiplier;
		groupShortcut.sizeY *= controlsSizeMultiplier;
		groupShortcut.fullInfo = true;

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

		copy.sizeX *= controlsSizeMultiplier;
		copy.sizeY *= controlsSizeMultiplier;
		copy.fullInfo = true;

		cut.sizeX *= controlsSizeMultiplier;
		cut.sizeY *= controlsSizeMultiplier;
		cut.fullInfo = true;

		this.enableMargins = false;

		for (int i = 0; i < drawables.length; i++)
		{
			drawables[i] = new ArrayList<>();
		}

		Obstacle.draw_size = Game.tile_size;

		Game.game.window.validScrollDown = false;
		Game.game.window.validScrollUp = false;

		this.name = lvlName;

		if (this.teams.size() == 0)
			mouseTank.team = null;
		else
			mouseTank.team = this.teams.get(teamNum);
	}

	public void updateMusic(boolean tanks)
	{
		this.prevTankMusics.clear();
		this.prevTankMusics.addAll(this.tankMusics);
		this.tankMusics.clear();

		if (tanks)
		{
			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !m.destroy)
				{
					this.tankMusics.addAll(((Tank) m).musicTracks);
				}
			}
		}

		for (String m : this.prevTankMusics)
		{
			if (!this.tankMusics.contains(m))
				Drawing.drawing.removeSyncedMusic(m, 500);
		}

		for (String m : this.tankMusics)
		{
			if (!this.prevTankMusics.contains(m))
				Drawing.drawing.addSyncedMusic(m, Game.musicVolume * 0.5f, true, 500);
		}
	}

	@Override
	public void update()
	{
		if (Level.isDark())
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;

		clickCooldown = Math.max(0, clickCooldown - Panel.frameFrequency);

		this.updateMusic(true);

		if (showControls)
		{
			boolean vertical = Drawing.drawing.interfaceScale * Drawing.drawing.interfaceSizeY >= Game.game.window.absoluteHeight - Drawing.drawing.statsHeight - 0.001;
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
				paste.enabled = true;
			}
			else if (pasteMode)
			{
				place.enabled = true;
				erase.enabled = true;
				select.enabled = true;
				panZoom.enabled = true;
				paste.enabled = false;
			}
			else if (selectMode)
			{
				place.enabled = true;
				erase.enabled = true;
				select.enabled = false;
				panZoom.enabled = true;
				paste.enabled = true;
			}
			else if (eraseMode)
			{
				place.enabled = true;
				erase.enabled = false;
				select.enabled = true;
				panZoom.enabled = true;
				paste.enabled = true;
			}
			else
			{
				place.enabled = false;
				erase.enabled = true;
				select.enabled = true;
				panZoom.enabled = true;
				paste.enabled = true;
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

			paste.posX = panZoom.posX + hStep;
			paste.posY = panZoom.posY + vStep;

			if (clipboard.size() > 0)
				paste.update();

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

			heightShortcut.posX = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2
					+ Drawing.drawing.interfaceSizeX - 50 * controlsSizeMultiplier - Game.game.window.getEdgeBounds() / Drawing.drawing.interfaceScale;
			heightShortcut.posY = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2
					+ Drawing.drawing.interfaceSizeY - 50 * controlsSizeMultiplier;

			groupShortcut.posX = heightShortcut.posX;
			groupShortcut.posY = heightShortcut.posY;

			rotateShortcut.posX = heightShortcut.posX;
			rotateShortcut.posY = heightShortcut.posY;

			teamShortcut.posX = heightShortcut.posX - hStep;
			teamShortcut.posY = heightShortcut.posY - vStep;

			selectSquareToggle.posX = heightShortcut.posX;
			selectSquareToggle.posY = heightShortcut.posY;

			selectAddToggle.posX = selectSquareToggle.posX - hStep;
			selectAddToggle.posY = selectSquareToggle.posY - vStep;

			if (Game.game.window.touchscreen)
			{
				copy.posX = pause.posX - vStep;
				copy.posY = pause.posY + hStep;
			}
			else
			{
				copy.posX = playControl.posX - hStep;
				copy.posY = playControl.posY + vStep;
			}

			cut.posX = copy.posX - hStep;
			cut.posY = copy.posY + vStep;

			selectClear.posX = cut.posX - hStep;
			selectClear.posY = cut.posY + vStep;

			if (selection)
			{
				selectClear.update();
				copy.update();
				cut.update();
			}

			if (selectMode && !changeCameraMode && !pasteMode)
			{
				selectSquareToggle.update();

				if (selection)
					selectAddToggle.update();
			}
			else if (!eraseMode && !changeCameraMode && !pasteMode)
			{
				if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableStacking)
					heightShortcut.update();

				if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableGroupID)
					groupShortcut.update();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					rotateShortcut.update();

				if (currentPlaceable == Placeable.playerTank || currentPlaceable == Placeable.enemyTank)
					teamShortcut.update();
			}
		}

		if (Game.game.input.editorPause.isValid() && this.level.editable)
		{
			this.paused = true;
			Game.game.input.editorPause.invalidate();
			Game.screen = new OverlayEditorMenu(Game.screen, this);
		}

		if (Game.game.input.editorObjectMenu.isValid() && this.level.editable && (!paused || objectMenu))
		{
			Game.game.input.editorObjectMenu.invalidate();
			this.paused = true;
			this.objectMenu = true;
			Game.screen = new OverlayObjectMenu(Game.screen, this);
		}

		if (Game.game.input.editorPaste.isValid() && !this.clipboard.isEmpty())
		{
			if (this.pasteMode)
				this.paste();
			else
				paste.function.run();

			Game.game.input.editorPaste.invalidate();
		}

		if (Game.game.input.editorCopy.isValid() && selection)
		{
			Game.game.input.editorCopy.invalidate();
			copy.function.run();
		}

		if (Game.game.input.editorCut.isValid() && selection)
		{
			Game.game.input.editorCut.invalidate();
			cut.function.run();
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
		else if (!selectMode && !eraseMode && !pasteMode)
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
				tankNum = (tankNum + 1) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
				this.refreshMouseTank();
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
				tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size() + this.level.customTanks.size()) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
				this.refreshMouseTank();
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
					((TankPlayer) mouseTank).setDefaultColor();
				}
				else if (currentPlaceable == Placeable.playerTank)
				{
					currentPlaceable = Placeable.enemyTank;
					this.refreshMouseTank();
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
					this.refreshMouseTank();
				}
				else if (currentPlaceable == Placeable.enemyTank)
				{
					currentPlaceable = Placeable.playerTank;
					mouseTank = new TankPlayer(0, 0, 0);
					((TankPlayer) mouseTank).setDefaultColor();
				}
			}

			if (Game.game.input.editorPrevMeta.isValid())
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

			if (Game.game.input.editorNextMeta.isValid())
			{
				Game.game.input.editorNextMeta.invalidate();

				if (currentPlaceable == Placeable.enemyTank)
					teamNum = (teamNum + 1) % (this.teams.size() + 1);
				else if (currentPlaceable == Placeable.playerTank)
					playerTeamNum = (playerTeamNum + 1) % (this.teams.size() + 1);
				else if (currentPlaceable == Placeable.obstacle)
				{
					if (mouseObstacle.enableStacking)
						mouseObstacleHeight = Math.min(mouseObstacleHeight + 0.5, Obstacle.default_max_height);
					else if (mouseObstacle.enableGroupID)
					{
						mouseObstacleGroup = Math.min(mouseObstacleGroup + 1, 999999999);
						mouseObstacle.setMetadata(mouseObstacleGroup + "");
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

			panX = Drawing.drawing.getMouseX();
			panY = Drawing.drawing.getMouseY();
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
			Game.game.input.play.invalidate();
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
			if (this.changeCameraMode || this.pasteMode)
				this.selectMode = true;
			else
				this.selectMode = !this.selectMode;

			this.changeCameraMode = false;
			this.pasteMode = false;

			Game.game.input.editorSelect.invalidate();
		}

		if (Game.game.input.editorCamera.isValid())
		{
			this.changeCameraMode = !this.changeCameraMode;
			Game.game.input.editorCamera.invalidate();
		}

		if (Game.game.input.editorPaste.isValid() && !pasteMode && !this.clipboard.isEmpty())
		{
			this.changeCameraMode = false;
			this.eraseMode = false;
			this.selectMode = false;
			this.pasteMode = true;
			Game.game.input.editorPaste.invalidate();
		}

		if (Game.game.input.editorBuild.isValid())
		{
			this.changeCameraMode = false;
			this.eraseMode = false;
			this.selectMode = false;
			this.pasteMode = false;
			Game.game.input.editorBuild.invalidate();
		}

		if (Game.game.input.editorErase.isValid())
		{
			this.changeCameraMode = false;
			this.eraseMode = true;
			this.selectMode = false;
			this.pasteMode = false;
			Game.game.input.editorErase.invalidate();
		}

		if (Game.game.input.editorHeight.isValid() && mouseObstacle.enableStacking && currentPlaceable == Placeable.obstacle)
		{
			Game.game.input.editorHeight.invalidate();
			this.heightShortcut.function.run();
		}

		if (Game.game.input.editorGroupID.isValid() && mouseObstacle.enableGroupID && currentPlaceable == Placeable.obstacle)
		{
			Game.game.input.editorGroupID.invalidate();
			this.groupShortcut.function.run();
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

		for (Obstacle o: Game.removeObstacles)
		{
			o.removed = true;
			Drawing.drawing.terrainRenderer.remove(o);

			int x = (int) (o.posX / Game.tile_size);
			int y = (int) (o.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY && Game.enable3d)
			{
				Game.redrawGroundTiles.add(new int[]{x, y});

				if (o.bulletCollision)
				{
					Game.game.solidGrid[x][y] = false;
					Game.game.unbreakableGrid[x][y] = false;
				}
			}

			Game.obstacles.remove(o);
		}

		Game.removeObstacles.clear();
	}

	public boolean[] checkMouse(double mx, double my, boolean left, boolean right, boolean validLeft, boolean validRight)
	{
		boolean[] handled = new boolean[]{false, false};

		double posX = Math.round((mx) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
		double posY = Math.round((my) / Game.tile_size + 0.5) * Game.tile_size - Game.tile_size / 2;
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
		else if (selectMode && !pasteMode)
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

				if (selectSquare || Game.game.input.editorHoldSquare.isPressed())
				{
					double size = Math.min(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
					selectX2 = Math.signum(selectX2 - selectX1) * size + selectX1;
					selectY2 = Math.signum(selectY2 - selectY1) * size + selectY1;
				}

				if (prevSelectX2 != selectX2 || prevSelectY2 != selectY2)
					Drawing.drawing.playVibration("selectionChanged");
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
		else if (symmetrySelectMode && !pasteMode)
		{
			if (validLeft)
			{
				selectX1 = clampTileX(mouseObstacle.posX);
				selectY1 = clampTileY(mouseObstacle.posY);
				selectHeld = true;
				handled[0] = true;
				handled[1] = true;

				Drawing.drawing.playVibration("selectionChanged");
			}

			if (left && selectHeld)
			{
				double prevSelectX2 = selectX2;
				double prevSelectY2 = selectY2;

				selectX2 = clampTileX(mouseObstacle.posX);
				selectY2 = clampTileY(mouseObstacle.posY);

				if (symmetryType == SymmetryType.flip8 || symmetryType == SymmetryType.rot90)
				{
					double size = Math.min(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
					selectX2 = Math.signum(selectX2 - selectX1) * size + selectX1;
					selectY2 = Math.signum(selectY2 - selectY1) * size + selectY1;
				}

				if (prevSelectX2 != selectX2 || prevSelectY2 != selectY2)
					Drawing.drawing.playVibration("selectionChanged");
			}

			if (!left && selectHeld)
			{
				Drawing.drawing.playVibration("click");
				selectHeld = false;

				double lowX = Math.min(selectX1, selectX2);
				double highX = Math.max(selectX1, selectX2);
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.max(selectY1, selectY2);

				this.symmetryX1 = lowX;
				this.symmetryX2 = highX;
				this.symmetryY1 = lowY;
				this.symmetryY2 = highY;
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
		return handlePlace(handled, left, right, validLeft, validRight, batch, false);
	}

	public boolean[] handlePlace(boolean[] handled, boolean left, boolean right, boolean validLeft, boolean validRight, boolean batch, boolean paste)
	{
		ArrayList<Double> posX = new ArrayList<>();
		ArrayList<Double> posY = new ArrayList<>();
		ArrayList<Double> orientations = new ArrayList<>();

		double originalX = mouseTank.posX;
		double originalY = mouseTank.posY;
		double originalOrientation = mouseTank.orientation;

		posX.add(mouseTank.posX);
		posY.add(mouseTank.posY);
		orientations.add(mouseTank.orientation);

		if (mouseTank.posX >= symmetryX1 && mouseTank.posX <= symmetryX2 && mouseTank.posY >= symmetryY1 && mouseTank.posY <= symmetryY2)
		{
			if (symmetryType == SymmetryType.flipHorizontal || symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flip8)
			{
				for (int i = 0; i < posX.size(); i++)
				{
					posY.add(symmetryY1 + (symmetryY2 - posY.get(i)));
					posX.add(posX.get(i));

					if (orientations.get(i) == 1 || orientations.get(i) == 3)
						orientations.add((orientations.get(i) + 2) % 4);
					else
						orientations.add(orientations.get(i));
				}
			}

			if (symmetryType == SymmetryType.flipVertical || symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flip8)
			{
				for (int i = 0; i < posX.size(); i++)
				{
					posX.add(symmetryX1 + (symmetryX2 - posX.get(i)));
					posY.add(posY.get(i));

					if (orientations.get(i) == 0 || orientations.get(i) == 2)
						orientations.add((orientations.get(i) + 2) % 4);
					else
						orientations.add(orientations.get(i));
				}
			}
		}

		for (int oi = 0; oi < orientations.size(); oi++)
		{
			mouseTank.posX = posX.get(oi);
			mouseTank.posY = posY.get(oi);
			mouseObstacle.posX = mouseTank.posX;
			mouseObstacle.posY = mouseTank.posY;
			mouseTank.orientation = orientations.get(oi);

			if (mouseTank.posX > 0 && mouseTank.posY > 0 && mouseTank.posX < Game.tile_size * Game.currentSizeX && mouseTank.posY < Game.tile_size * Game.currentSizeY)
			{
				if (validLeft && pasteMode && !paste)
				{
					paste();

					mouseTank.posX = originalX;
					mouseTank.posY = originalY;
					mouseTank.orientation = originalOrientation;

					mouseObstacle.posX = originalX;
					mouseObstacle.posY = originalY;

					return new boolean[]{true, true};
				}

				if (!pasteMode && (right || (eraseMode && left)))
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

									for (int z = 0; z < 100 * Game.effectMultiplier; z++)
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

				if (!eraseMode && clickCooldown <= 0 && (validLeft || (!pasteMode && left && currentPlaceable == Placeable.obstacle && this.mouseObstacle.draggable)))
				{
					boolean skip = false;

					double mx = mouseTank.posX;
					double my = mouseTank.posY;

					if (currentPlaceable == Placeable.obstacle)
					{
						mx = mouseObstacle.posX;
						my = mouseObstacle.posY;
					}

					if (mouseObstacle.tankCollision || currentPlaceable != Placeable.obstacle)
					{
						for (int i = 0; i < Game.movables.size(); i++)
						{
							Movable m = Game.movables.get(i);
							if (m.posX == mx && m.posY == my)
							{
								skip = true;
								break;
							}
						}
					}

					for (int i = 0; i < Game.obstacles.size(); i++)
					{
						Obstacle m = Game.obstacles.get(i);
						if (m.posX == mx && m.posY == my)
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
							Tank t;

							if (paste)
								t = mouseTank;
							else
							{
								if (tankNum < Game.registryTank.tankEntries.size())
									t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
								else
									t = ((TankAIControlled) mouseTank).instantiate(mouseTank.name, mouseTank.posX, mouseTank.posY, mouseTank.angle);
							}

							t.team = mouseTank.team;
							this.actions.add(new Action.ActionTank(t, true));
							Game.movables.add(t);

							if (!batch)
								Drawing.drawing.playVibration("click");
						}
						else if (currentPlaceable == Placeable.playerTank)
						{
							ArrayList<TankSpawnMarker> spawnsClone = (ArrayList<TankSpawnMarker>) spawns.clone();
							if (this.movePlayer && !paste)
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

							if (this.movePlayer && !paste)
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
							Obstacle o;

							if (paste)
								o = mouseObstacle;
							else
								o = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);

							o.colorR = mouseObstacle.colorR;
							o.colorG = mouseObstacle.colorG;
							o.colorB = mouseObstacle.colorB;
							o.posX = mouseObstacle.posX;
							o.posY = mouseObstacle.posY;

							if (o.enableStacking)
							{
								o.stackHeight = mouseObstacleHeight;
								o.startHeight = mouseObstacleStartHeight;

								if (this.stagger && !paste)
								{
									if ((((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == 1 && !this.oddStagger)
											|| (((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == 0 && this.oddStagger))
										o.stackHeight -= 0.5;
								}
							}
							else if (o.enableGroupID)
								o.setMetadata(mouseObstacleGroup + "");

							mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(o.posX, o.posY);

							if (mouseObstacle.enableGroupID)
								mouseObstacle.setMetadata(mouseObstacleGroup + "");

							this.actions.add(new Action.ActionObstacle(o, true));
							Game.addObstacle(o);

							if (!batch)
								Drawing.drawing.playVibration("click");
						}
					}

					handled[0] = true;
					handled[1] = true;
				}
			}
		}

		return handled;
	}

	public void paste()
	{
		Drawing.drawing.playVibration("click");

		ArrayList<Action> actions = this.actions;
		this.actions = new ArrayList<>();

		boolean[] handled = new boolean[2];

		Tank prevMouseTank = mouseTank;
		Obstacle prevMouseObstacle = mouseObstacle;
		Placeable placeable = currentPlaceable;

		double mx = prevMouseObstacle.posX;
		double my = prevMouseObstacle.posY;

		for (Object o : this.clipboard)
		{
			if (o instanceof Obstacle)
			{
				currentPlaceable = Placeable.obstacle;

				try
				{
					Obstacle n = (Obstacle) o.getClass().getConstructor(String.class, double.class, double.class).newInstance(((Obstacle) o).name, (((Obstacle) o).posX + prevMouseObstacle.posX) / 50 - 0.5, (((Obstacle) o).posY + prevMouseObstacle.posY) / 50 - 0.5);
					n.groupID = ((Obstacle) o).groupID;
					n.stackHeight = ((Obstacle) o).stackHeight;
					n.startHeight = ((Obstacle) o).startHeight;
					mouseObstacle = n;
					mouseObstacleHeight = n.stackHeight;
					mouseObstacleStartHeight = n.startHeight;
					mouseObstacleGroup = n.groupID;
					mouseTank.posX = mouseObstacle.posX;
					mouseTank.posY = mouseObstacle.posY;

					handlePlace(handled, true, false, true, false, true, true);
				}
				catch (Exception e)
				{
					Game.exitToCrash(e.getCause());
				}
			}

			else if (o instanceof Tank)
			{
				currentPlaceable = Placeable.enemyTank;

				try
				{
					Tank n;

					if (o.getClass().equals(TankAIControlled.class))
					{
						n = new TankAIControlled(((TankAIControlled) o).name, ((Tank) o).posX + prevMouseObstacle.posX, ((Tank) o).posY + prevMouseObstacle.posY, ((TankAIControlled) o).size, ((TankAIControlled) o).colorR, ((TankAIControlled) o).colorG, ((TankAIControlled) o).colorB, ((TankAIControlled) o).angle, ((TankAIControlled) o).shootAIType);
						((TankAIControlled) o).cloneProperties((TankAIControlled) n);
					}
					else
						n = (Tank) o.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(((Tank) o).name, ((Tank) o).posX + prevMouseObstacle.posX, ((Tank) o).posY + prevMouseObstacle.posY, ((Tank) o).angle);

					n.team = ((Tank) o).team;
					n.destroy = ((Tank) o).destroy;
					mouseTank = n;

					if (n instanceof TankSpawnMarker || n instanceof TankPlayer)
						currentPlaceable = Placeable.playerTank;

					handlePlace(handled, true, false, true, false, true, true);
					//Game.movables.add(n);
				}
				catch (Exception e)
				{
					Game.exitToCrash(e);
				}
			}

			prevMouseObstacle.posX = mx;
			prevMouseObstacle.posY = my;
			prevMouseTank.posX = mx;
			prevMouseTank.posY = my;
		}

		currentPlaceable = placeable;
		mouseTank = prevMouseTank;
		mouseObstacle = prevMouseObstacle;

		mouseObstacleHeight = mouseObstacle.stackHeight;
		mouseObstacleStartHeight = mouseObstacle.startHeight;

		ArrayList<Action> tempActions = this.actions;
		this.actions = actions;

		this.actions.add(new Action.ActionPaste(this, tempActions));
	}

	public void save()
	{
		StringBuilder level = new StringBuilder("{");

		if (!this.level.editable)
			level.append("*");

		level.append(this.level.sizeX).append(",").append(this.level.sizeY).append(",").append(this.level.colorR).append(",").append(this.level.colorG).append(",").append(this.level.colorB).append(",").append(this.level.colorVarR).append(",").append(this.level.colorVarG).append(",").append(this.level.colorVarB)
				.append(",").append((int) (this.level.timer / 100)).append(",").append((int) Math.round(this.level.light * 100)).append(",").append((int) Math.round(this.level.shadow * 100)).append("|");

		ArrayList<Obstacle> unmarked = (ArrayList<Obstacle>) Game.obstacles.clone();
		double[][][] obstacles = new double[Game.registryObstacle.obstacleEntries.size()][this.level.sizeX][this.level.sizeY];

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
			for (int i = 0; i < this.level.sizeX; i++)
			{
				for (int j = 0; j < this.level.sizeY; j++)
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
			level.append("-").append(unmarked.get(i).name);

			Obstacle u = unmarked.get(i);
			if (u instanceof ObstacleUnknown && ((ObstacleUnknown) u).metadata != null)
				level.append("-").append(((ObstacleUnknown) u).metadata);
			else if (u.enableStacking)
				level.append("-").append(u.stackHeight);
			else if (u.enableGroupID)
				level.append("-").append(u.groupID);


			level.append(",");
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

		for (Item i: this.level.shop)
			i.exportProperties();

		for (Item i: this.level.startingItems)
			i.exportProperties();

		if (this.level.startingCoins > 0)
			level.append("\ncoins\n").append(this.level.startingCoins);

		if (!this.level.shop.isEmpty())
		{
			level.append("\nshop");

			for (Item i : this.level.shop)
				level.append("\n").append(i.toString());
		}

		if (!this.level.startingItems.isEmpty())
		{
			level.append("\nitems");

			for (Item i : this.level.startingItems)
				level.append("\n").append(i.toString());
		}

		if (!this.level.customTanks.isEmpty())
		{
			level.append("\ntanks");

			for (Tank t : this.level.customTanks)
				level.append("\n").append(t.toString());
		}

		Game.currentLevelString = level.toString();

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + name);
		if (file.exists())
		{
			if (!this.level.editable)
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

	@Override
	public void draw()
	{
		if (Level.isDark())
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;

		for (Obstacle o: Game.obstacles)
		{
			o.baseGroundHeight = Game.sampleGroundHeight(o.posX, o.posY);
		}

		if (Game.enable3d)
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.replaceTiles)
					o.postOverride();

				int x = (int) (o.posX / Game.tile_size);
				int y = (int) (o.posY / Game.tile_size);

				if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
					Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
			}

		Drawing.drawing.setColor(174, 92, 16);

		double mul = 1;
		if (Game.angledView)
			mul = 2;

		Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
				mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

		this.drawDefaultBackground();

		for (Movable m: Game.movables)
			drawables[m.drawLevel].add(m);

		if (Game.enable3d)
		{
			for (int i = 0; i < drawables.length; i++)
			{
				for (Obstacle o : Game.obstacles)
				{
					if (o.drawLevel == i && !o.batchDraw)
					{
						drawables[i].add(o);
					}
				}
			}
		}
		else
		{
			for (Obstacle o : Game.obstacles)
				drawables[o.drawLevel].add(o);
		}

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

			if (Game.glowEnabled)
			{
				for (int j = 0; j < this.drawables[i].size(); j++)
				{
					IDrawable d = this.drawables[i].get(j);

					if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
						((IDrawableWithGlow) d).drawGlow();
				}
			}

			drawables[i].clear();
		}

		if (!paused && !Game.game.window.touchscreen)
		{
			if (eraseMode && !selectMode && !changeCameraMode)
			{
				Drawing.drawing.setColor(255, 0, 0, 64);

				if (Game.enable3d)
					Drawing.drawing.fillBox(mouseObstacle.posX, mouseObstacle.posY, 0, Game.tile_size, Game.tile_size, Game.tile_size, (byte) 64);
				else
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else if (pasteMode && !changeCameraMode)
			{
				Drawing.drawing.setColor(255, 255, 255, 127);
				Drawing.drawing.drawImage("icons/paste.png", mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);

				for (Object o: this.clipboard)
				{
					if (o instanceof Obstacle)
					{
						Obstacle ob = (Obstacle) o;
						Drawing.drawing.setColor(ob.colorR, ob.colorG, ob.colorB, 64);
						Drawing.drawing.fillRect(ob.posX + mouseTank.posX, ob.posY + mouseTank.posY, /*0,*/ Game.tile_size, Game.tile_size/*, ((Obstacle) o).stackHeight * Game.tile_size, (byte) 64*/);
					}
					else if (o instanceof Tank)
					{
						((Tank) o).drawOutlineAt(((Tank) o).posX + mouseTank.posX, ((Tank) o).posY + mouseTank.posY);
					}
				}
			}
			else if ((currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank)  && !selectMode && !changeCameraMode)
			{
				mouseTank.drawOutline();
				mouseTank.drawTeam();

				if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
				{
					Drawing.drawing.setColor(0, 200, 255, 127);
					Drawing.drawing.drawImage("emblems/player_spawn.png", mouseTank.posX, mouseTank.posY, mouseTank.size * 0.7, mouseTank.size * 0.7);
				}
			}
			else if (currentPlaceable == Placeable.obstacle && !selectMode && !changeCameraMode)
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

		if (selectedTiles != null)
		{
			for (int i = 0; i < selectedTiles.length; i++)
			{
				for (int j = 0; j < selectedTiles[i].length; j++)
				{
					if (selectedTiles[i][j])
						Drawing.drawing.fillRect((i + 0.5) * Game.tile_size, (j + 0.5) * Game.tile_size, Game.tile_size, Game.tile_size);
				}
			}
		}

		if (changeCameraMode && !paused)
		{
			Drawing.drawing.setColor(0, 0, 0, 127);

			Drawing.drawing.fillInterfaceRect(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2, 500 * objWidth / 350, 150 * objHeight / 40);

			Drawing.drawing.setColor(255, 255, 255);

			Drawing.drawing.setInterfaceFontSize(this.textSize);
			Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - this.objYSpace / 2, "Drag to pan");

			if (Game.game.window.touchscreen)
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Pinch to zoom");
				recenter.draw();
			}
			else
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Scroll or press %s or %s to zoom", Game.game.input.editorZoomIn.getInputs(), Game.game.input.editorZoomOut.getInputs());
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 + this.objYSpace / 2, "Press %s to re-center", Game.game.input.editorRevertCamera.getInputs());
			}
		}

		if ((selectMode || symmetrySelectMode) && !changeCameraMode && !pasteMode && !this.paused)
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

				Drawing.drawing.setColor(255, 255, 255);

				if (symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flipHorizontal || symmetryType == SymmetryType.rot180 || symmetryType == SymmetryType.rot90 || symmetryType == SymmetryType.flip8)
					Drawing.drawing.fillRect((selectX2 + selectX1) / 2, (selectY2 + selectY1) / 2, (selectX2 - selectX1), 10);

				if (symmetryType == SymmetryType.flipBoth || symmetryType == SymmetryType.flipVertical || symmetryType == SymmetryType.rot90 || symmetryType == SymmetryType.flip8)
					Drawing.drawing.fillRect((selectX2 + selectX1) / 2, (selectY2 + selectY1) / 2, 10, (selectX2 - selectX1));
			}
		}

		if (Game.screen instanceof IOverlayScreen || this.paused)
		{
			Drawing.drawing.setColor(127, 178, 228, 64);
			//Drawing.drawing.setColor(0, 0, 0, 127);
			Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);
		}

		if (!paused && showControls)
		{
			pause.image = "icons/pause.png";
			pause.imageSizeX = 40 * controlsSizeMultiplier;
			pause.imageSizeY = 40 * controlsSizeMultiplier;

			menu.image = "icons/menu.png";
			menu.imageSizeX = 50 * controlsSizeMultiplier;
			menu.imageSizeY = 50 * controlsSizeMultiplier;

			playControl.image = "icons/play.png";
			playControl.imageSizeX = 30 * controlsSizeMultiplier;
			playControl.imageSizeY = 30 * controlsSizeMultiplier;

			place.image = "icons/pencil.png";
			place.imageSizeX = 40 * controlsSizeMultiplier;
			place.imageSizeY = 40 * controlsSizeMultiplier;

			erase.image = "icons/eraser.png";
			erase.imageSizeX = 40 * controlsSizeMultiplier;
			erase.imageSizeY = 40 * controlsSizeMultiplier;

			panZoom.image = "icons/zoom_pan.png";
			panZoom.imageSizeX = 40 * controlsSizeMultiplier;
			panZoom.imageSizeY = 40 * controlsSizeMultiplier;

			select.image = "icons/select.png";
			select.imageSizeX = 40 * controlsSizeMultiplier;
			select.imageSizeY = 40 * controlsSizeMultiplier;

			undo.image = "icons/undo.png";
			undo.imageSizeX = 40 * controlsSizeMultiplier;
			undo.imageSizeY = 40 * controlsSizeMultiplier;

			redo.image = "icons/redo.png";
			redo.imageSizeX = 40 * controlsSizeMultiplier;
			redo.imageSizeY = 40 * controlsSizeMultiplier;

			selectClear.image = "icons/select_clear.png";
			selectClear.imageSizeX = 40 * controlsSizeMultiplier;
			selectClear.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectAdd)
				selectAddToggle.image = "icons/select_add.png";
			else
				selectAddToggle.image = "icons/select_remove.png";

			selectAddToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectAddToggle.imageSizeY = 40 * controlsSizeMultiplier;

			if (selectSquare)
				selectSquareToggle.image = "icons/square_locked.png";
			else
				selectSquareToggle.image = "icons/square_unlocked.png";

			selectSquareToggle.imageSizeX = 40 * controlsSizeMultiplier;
			selectSquareToggle.imageSizeY = 40 * controlsSizeMultiplier;

			heightShortcut.image = "icons/obstacle_height.png";
			heightShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			heightShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			groupShortcut.image = "icons/id.png";
			groupShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			groupShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			rotateShortcut.image = "icons/rotate_tank.png";
			rotateShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			rotateShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			teamShortcut.image = "icons/team.png";
			teamShortcut.imageSizeX = 50 * controlsSizeMultiplier;
			teamShortcut.imageSizeY = 50 * controlsSizeMultiplier;

			copy.image = "icons/copy.png";
			copy.imageSizeX = 50 * controlsSizeMultiplier;
			copy.imageSizeY = 50 * controlsSizeMultiplier;

			cut.image = "icons/cut.png";
			cut.imageSizeX = 60 * controlsSizeMultiplier;
			cut.imageSizeY = 60 * controlsSizeMultiplier;

			paste.image = "icons/paste.png";
			paste.imageSizeX = 50 * controlsSizeMultiplier;
			paste.imageSizeY = 50 * controlsSizeMultiplier;

			playControl.draw();
			menu.draw();
			pause.draw();

			panZoom.draw();
			select.draw();
			erase.draw();
			place.draw();

			undo.draw();
			redo.draw();

			if (clipboard.size() > 0)
				paste.draw();

			if (selection)
			{
				selectClear.draw();
				copy.draw();
				cut.draw();
			}

			if (selectMode && !changeCameraMode && !pasteMode)
			{
				selectSquareToggle.draw();

				if (selection)
					selectAddToggle.draw();
			}
			else if (!eraseMode && !changeCameraMode && !pasteMode)
			{
				if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableStacking)
					heightShortcut.draw();

				if (currentPlaceable == Placeable.obstacle && mouseObstacle.enableGroupID)
					groupShortcut.draw();

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

		if (Game.enable3d)
			for (int i = 0; i < Game.obstacles.size(); i++)
			{
				Obstacle o = Game.obstacles.get(i);

				if (o.replaceTiles)
					o.postOverride();
			}

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

		Game.currentLevel = new Level(Game.currentLevelString);
		Game.currentLevel.timed = level.timer > 0;
		Game.currentLevel.timer = level.timer;

		Game.resetNetworkIDs();
		for (Movable m: Game.movables)
		{
			if (m instanceof Tank)
				((Tank) m).registerNetworkID();
		}

		Game.screen = new ScreenGame(this.name);
		Game.player.hotbar.coins = this.level.startingCoins;
	}

	public void replaceSpawns()
	{
		int playerCount = 1;
		if (ScreenPartyHost.isServer && ScreenPartyHost.server != null)
			playerCount += ScreenPartyHost.server.connections.size();

		ArrayList<Integer> availablePlayerSpawns = new ArrayList<>();
		ArrayList<INetworkEvent> playerEvents = new ArrayList<>();

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
				Game.addPlayerTank(Game.players.get(i), x, y, angle, team);
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
				{
					selection = true;
					break;
				}
			}
		}
	}

	public void copy(boolean cut)
	{
		double smallestX = Double.MAX_VALUE;
		double smallestY = Double.MAX_VALUE;

		ArrayList<Tank> tanks = new ArrayList<>();
		ArrayList<Obstacle> obstacles = new ArrayList<>();

		this.clipboard = new ArrayList<>();
		for (int i = 0; i < this.level.sizeX; i++)
		{
			for (int j = 0; j < this.level.sizeY; j++)
			{
				if (this.selectedTiles[i][j])
				{
					if (i < smallestX)
						smallestX = i;

					if (j < smallestY)
						smallestY = j;
				}
			}
		}

		for (Obstacle o : Game.obstacles)
		{
			int x = (int) ((o.posX - 25) / 50);
			int y = (int) ((o.posY - 25) / 50);

			if (x >= 0 && y >= 0 && x < this.selectedTiles.length && y < this.selectedTiles[0].length && this.selectedTiles[x][y])
			{
				try
				{
					Obstacle n = o.getClass().getConstructor(String.class, double.class, double.class).newInstance(o.name, (int) (o.posX / 50 - 0.5), (int) (o.posY / 50 - 0.5));

					if (o.enableStacking)
					{
						n.stackHeight = o.stackHeight;
						n.startHeight = o.startHeight;
					}

					if (o.enableGroupID)
						n.setMetadata(o.groupID + "");

					this.clipboard.add(n);

					if (cut)
					{
						obstacles.add(o);
						Game.removeObstacles.add(o);
					}

				}
				catch (Exception e)
				{
					Game.exitToCrash(e);
				}
			}
		}

		for (Movable t : Game.movables)
		{
			if (!(t instanceof Tank))
				continue;

			int x = (int) ((t.posX - 25) / 50);
			int y = (int) ((t.posY - 25) / 50);

			if (x >= 0 && y >= 0 && x < this.selectedTiles.length && y < this.selectedTiles[0].length && this.selectedTiles[x][y])
			{
				try
				{
					Tank n;

					if (t.getClass().equals(TankAIControlled.class))
					{
						n = new TankAIControlled(((TankAIControlled) t).name, t.posX, t.posY, ((TankAIControlled) t).size, ((TankAIControlled) t).colorR, ((TankAIControlled) t).colorG, ((TankAIControlled) t).colorB, ((TankAIControlled) t).angle, ((TankAIControlled) t).shootAIType);
						((TankAIControlled) t).cloneProperties((TankAIControlled) n);
					}
					else
						n = (Tank) t.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(((Tank) t).name, t.posX, t.posY, ((Tank) t).angle);

					n.team = t.team;
					n.destroy = t.destroy;
					this.clipboard.add(n);

					if (cut)
					{
						if (t instanceof TankSpawnMarker && this.spawns.size() > 1)
						{
							this.spawns.remove(t);
							tanks.add((Tank) t);
							Game.removeMovables.add(t);
						}
						else if (!(t instanceof TankSpawnMarker))
						{
							tanks.add((Tank) t);
							Game.removeMovables.add(t);
						}
					}

				}
				catch (Exception e)
				{
					Game.exitToCrash(e);
				}
			}
		}

		smallestX = smallestX * Game.tile_size + 25;
		smallestY = smallestY * Game.tile_size + 25;

		for (Object o : this.clipboard)
		{
			if (o instanceof Obstacle)
			{
				((Obstacle) o).posX -= smallestX;
				((Obstacle) o).posY -= smallestY;
			}

			else if (o instanceof Tank)
			{
				((Tank) o).posX -= smallestX;
				((Tank) o).posY -= smallestY;
			}
		}

		this.clearSelection();

		if (cut)
			actions.add(new Action.ActionCut(tanks, obstacles, (Action.ActionSelectTiles) this.actions.remove(this.actions.size() - 1)));

		if (!this.clipboard.isEmpty())
			this.pasteMode = true;
	}

	public void refreshMouseTank()
	{
		Tank t;
		if (tankNum < Game.registryTank.tankEntries.size())
			t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
		else
			t = this.level.customTanks.get(tankNum - Game.registryTank.tankEntries.size()).instantiate(mouseTank.name, mouseTank.posX, mouseTank.posY, mouseTank.angle);
		t.drawAge = mouseTank.drawAge;
		mouseTank = t;
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

	public static void refreshItemButtons(ArrayList<Item> items, ButtonList buttons, boolean omitPrice)
	{
		buttons.buttons.clear();

		for (int i = 0; i < items.size(); i++)
		{
			int j = i;

			Button b = new Button(0, 0, 350, 40, items.get(i).name, () ->
			{
				ScreenItemEditor s = new ScreenItemEditor(items.get(j), (IItemScreen) Game.screen, omitPrice, true);
				s.drawBehindScreen = true;
				Game.screen = s;
			});

			b.image = items.get(j).icon;
			b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
			b.imageSizeX = b.sizeY;
			b.imageSizeY = b.sizeY;

			if (!omitPrice)
			{
				int p = items.get(i).price;

				if (p == 0)
					b.setSubtext("Free!");
				else if (p == 1)
					b.setSubtext("1 coin");
				else
					b.setSubtext("%d coins", p);
			}

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
					Game.addObstacle(this.obstacle);
			}

			@Override
			public void redo()
			{
				if (!add)
					Game.removeObstacles.add(this.obstacle);
				else
					Game.addObstacle(this.obstacle);
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
			public ScreenLevelEditor screenLevelEditor;
			public boolean add;
			public TankSpawnMarker tank;

			public ActionPlayerSpawn(ScreenLevelEditor s, TankSpawnMarker t, boolean add)
			{
				this.screenLevelEditor = s;
				this.tank = t;
				this.add = add;
			}

			@Override
			public void undo()
			{
				if (add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelEditor.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelEditor.spawns.add(this.tank);
				}
			}

			@Override
			public void redo()
			{
				if (!add)
				{
					Game.removeMovables.add(this.tank);
					screenLevelEditor.spawns.remove(this.tank);
				}
				else
				{
					Game.movables.add(this.tank);
					screenLevelEditor.spawns.add(this.tank);
				}
			}
		}

		static class ActionMovePlayer extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<TankSpawnMarker> oldSpawns;
			public TankSpawnMarker newSpawn;

			public ActionMovePlayer(ScreenLevelEditor s, ArrayList<TankSpawnMarker> o, TankSpawnMarker n)
			{
				this.screenLevelEditor = s;
				this.oldSpawns = o;
				this.newSpawn = n;
			}

			@Override
			public void undo()
			{
				Game.removeMovables.add(newSpawn);
				screenLevelEditor.spawns.clear();

				for (TankSpawnMarker t: oldSpawns)
				{
					screenLevelEditor.spawns.add(t);
					Game.movables.add(t);
				}
			}

			@Override
			public void redo()
			{
				Game.removeMovables.addAll(oldSpawns);

				screenLevelEditor.spawns.clear();

				Game.movables.add(newSpawn);
				screenLevelEditor.spawns.add(newSpawn);
			}
		}

		static class ActionSelectTiles extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Integer> x;
			public ArrayList<Integer> y;
			public boolean select;

			public ActionSelectTiles(ScreenLevelEditor s, boolean select, ArrayList<Integer> x, ArrayList<Integer> y)
			{
				this.screenLevelEditor = s;
				this.select = select;
				this.x = x;
				this.y = y;
			}

			@Override
			public void undo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = !select;
				}

				screenLevelEditor.refreshSelection();
			}

			@Override
			public void redo()
			{
				for (int i = 0; i < this.x.size(); i++)
				{
					screenLevelEditor.selectedTiles[this.x.get(i)][this.y.get(i)] = select;
				}

				screenLevelEditor.refreshSelection();
			}
		}

		static class ActionGroup extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Action> actions;

			public ActionGroup(ScreenLevelEditor s, ArrayList<Action> actions)
			{
				this.screenLevelEditor = s;
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

		static class ActionDeleteCustomTank extends Action
		{
			public ScreenLevelEditor screenLevelEditor;
			public ArrayList<Action> actions;
			public TankAIControlled tank;

			public ActionDeleteCustomTank(ScreenLevelEditor s, ArrayList<Action> actions, TankAIControlled t)
			{
				this.screenLevelEditor = s;
				this.actions = actions;
				this.tank = t;
			}

			@Override
			public void undo()
			{
				for (Action a: this.actions)
					a.undo();

				this.screenLevelEditor.level.customTanks.add(this.tank);
			}

			@Override
			public void redo()
			{
				for (Action a: this.actions)
					a.redo();

				this.screenLevelEditor.level.customTanks.remove(this.tank);
			}
		}


		static class ActionPaste extends Action
		{
			public ScreenLevelEditor levelEditor;
			public ArrayList<Action> actions;

			public ActionPaste(ScreenLevelEditor s, ArrayList<Action> actions)
			{
				this.levelEditor = s;
				this.actions = actions;
			}

			@Override
			public void undo()
			{
				for (int i = this.actions.size() - 1; i >= 0; i--)
					this.actions.get(i).undo();
			}

			@Override
			public void redo()
			{
				for (Action a: actions)
					a.redo();
			}
		}

		static class ActionCut extends Action
		{
			public ArrayList<Tank> tanks;
			public ArrayList<Obstacle> obstacles;
			public ActionSelectTiles deselect;

			public ActionCut(ArrayList<Tank> tanks, ArrayList<Obstacle> obstacles, ActionSelectTiles deselect)
			{
				this.tanks = tanks;
				this.obstacles = obstacles;
				this.deselect = deselect;
			}

			@Override
			public void undo()
			{
				for (Obstacle o: this.obstacles)
				{
					Game.addObstacle(o);
				}

				Game.movables.addAll(this.tanks);
				this.deselect.undo();
			}

			@Override
			public void redo()
			{
				for (int i = 0; i < Game.obstacles.size(); i++)
				{
					Game.removeObstacles.addAll(this.obstacles);
				}

				for (int i = 0; i < Game.movables.size(); i++)
				{
					if (Game.movables.get(i) instanceof Tank)
					{
						for (Tank o : this.tanks)
						{
							if (Game.movables.get(i).equals(o))
								Game.movables.remove(i);
						}
					}
				}

				this.deselect.redo();
			}
		}
	}

	@Override
	public void onAttemptClose()
	{
		paused = true;
		Game.screen = new OverlayConfirmSave(Game.screen, this);
	}

	@Override
	public void setupLights()
	{
		for (Obstacle o: Game.obstacles)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.startHeight + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Movable o: Game.movables)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ + 25) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}

		for (Effect o: Game.effects)
		{
			if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
			{
				double[] l = ((IDrawableLightSource) o).getLightInfo();
				l[0] = Drawing.drawing.gameToAbsoluteX(o.posX, 0);
				l[1] = Drawing.drawing.gameToAbsoluteY(o.posY, 0);
				l[2] = (o.posZ) * Drawing.drawing.scale;
				Panel.panel.lights.add(l);
			}
		}
	}

}
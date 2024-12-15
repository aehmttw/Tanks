package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.Panel;
import tanks.gui.screen.leveleditor.EditorButtons.EditorButton;
import tanks.gui.screen.leveleditor.selector.LevelEditorSelector;
import tanks.gui.screen.leveleditor.selector.SelectorStackHeight;
import tanks.gui.Button;
import tanks.gui.screen.*;
import tanks.item.Item;
import tanks.network.event.INetworkEvent;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleBeatBlock;
import tanks.obstacle.ObstacleUnknown;
import tanks.registry.RegistryObstacle;
import tanks.registry.RegistryTank;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.io.IOException;
import java.util.*;


@SuppressWarnings({"unused"})
public class ScreenLevelEditor extends Screen implements ILevelPreviewScreen
{
	public HashMap<String, LevelEditorSelector<?>> selectors = new HashMap<>();
	public Placeable currentPlaceable = Placeable.enemyTank;
	public EditorClipboard[] clipboards = new EditorClipboard[5];
	public EditorClipboard clipboard = new EditorClipboard();
	public int selectedNum = 0;

	public ArrayList<EditorAction> undoActions = new ArrayList<>();
	public ArrayList<EditorAction> redoActions = new ArrayList<>();
	public int undoCount = 1;
	public int redoCount = 1;
	public int redoLength = -1;

	public int tankPage = 0;
	public int obstaclePage = 0;

	public int tankNum = 0;
	public int obstacleNum = 0;
	public Tank mouseTank = currentPlaceable == Placeable.playerTank ? new TankPlayer(0, 0, 0).setDefaultColor() : Game.registryTank.getEntry(tankNum).getTank(0, 0, 0);
	public int mouseTankOrientation = 0;
	public Obstacle mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
	public Obstacle hoverObstacle = null;
	public EditorButtons buttons = new EditorButtons(this);
	public boolean stagger = false;
	public boolean oddStagger = false;
	public boolean paused = false;
	public boolean objectMenu = false;
	public boolean modified = false;

	public double clickCooldown = 0;
	public ArrayList<Team> teams = new ArrayList<>();
	public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

	public ArrayList<TankPlayer> playerTankBuilds = new ArrayList<>();

	public Level level;
	public String name;

	public boolean movePlayer = true;

	public enum EditorMode { build, erase, camera, select, picker, paste }
	public EditorMode currentMode = EditorMode.build;

	public boolean symmetrySelectMode = false;

	public enum BuildTool {normal, circle, rectangle, line}
	public BuildTool buildTool = BuildTool.normal;
	public enum SelectTool {normal, wand_contiguous, wand_discontiguous}
	public SelectTool selectTool = SelectTool.normal;

	public Shape prevSelectShape;
	public double selectX1, selectY1, selectX2, selectY2;

	public HashMap<String, EditorButtons.EditorButton> addedShortcutButtons = new HashMap<>();
	public boolean initialized = false;

	public SymmetryType symmetryType = SymmetryType.none;
	public double symmetryX1, symmetryY1, symmetryX2, symmetryY2;

	public boolean selectHeld = false;
	public boolean selectInverted = false;
	public boolean selection = false;
	public boolean heightBlocksSelected = false;
	public boolean lockAdd = true;
	public boolean lockSquare = false;
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
	public HashSet<String> overlayMusics = new HashSet<>();
	public double mouseObstacleStartHeight;

	public double fontBrightness = 0;

	public HashMap<Float, Obstacle>[][] grid = new HashMap[Game.currentSizeX][Game.currentSizeY];

	EditorButton pause = new EditorButton(buttons.topRight, "pause.png", 40, 40, () ->
	{
		paused = true;
		Game.screen = new OverlayEditorMenu(Game.screen, (ScreenLevelEditor) Game.screen);
	}, "Pause (%s)", Game.game.input.editorPause
	);

	EditorButton menu = new EditorButton(buttons.topRight, "menu.png", 50, 50, () ->
	{
		this.paused = true;
		this.objectMenu = true;
		Game.screen = new OverlayObjectMenu(Game.screen, this);
	},
			() -> false, () -> this.level.editable && (!paused || objectMenu),
			"Object menu (%s)", Game.game.input.editorObjectMenu
	);
	EditorButton playControl = new EditorButton(buttons.topRight, "play.png", 30, 30,
			this::play, () -> this.spawns.isEmpty(), "Play (%s)", Game.game.input.editorPlay);

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

	EditorButton place = new EditorButton(buttons.topLeft, "pencil.png", 40, 40,
			new Runnable()
			{
				@Override
				public void run()
				{
					if (currentMode != EditorMode.build)
						currentMode = EditorMode.build;
					else
					{
						place.option = 0;
						place.setOption();
					}
				}
			},
			new ToBooleanFunction()
			{
				@Override
				public boolean apply()
				{
					if (currentMode == EditorMode.build)
					{
						if (!place.showSubButtons)
							return true;
						else
							return buildTool == BuildTool.normal;
					}

					return false;
				}
			},
			"Build (%s)", Game.game.input.editorBuild
	)
			.addSubButtons(
					new EditorButton("square.png", 40, 40, () -> {this.currentMode = EditorMode.build; buildTool = BuildTool.rectangle;}, () -> buildTool == BuildTool.rectangle, "Square tool (%s)", Game.game.input.editorSquare),
					new EditorButton("circle.png", 40, 40, () -> {this.currentMode = EditorMode.build; buildTool = BuildTool.circle;}, () -> buildTool == BuildTool.circle, "Circle tool (%s)", Game.game.input.editorCircle),
					new EditorButton("line.png", 40, 40, () -> {this.currentMode = EditorMode.build; buildTool = BuildTool.line;}, () -> buildTool == BuildTool.line, "Line tool (%s)", Game.game.input.editorLine)
			)
			.onReset(() -> buildTool = BuildTool.normal);

	EditorButton erase = new EditorButton(buttons.topLeft, "eraser.png", 40, 40,
			() -> this.currentMode = EditorMode.erase,
			() -> this.currentMode == EditorMode.erase,
			"Erase (%s)", Game.game.input.editorErase
	);

	EditorButton panZoom = new EditorButton(buttons.topLeft, "zoom_pan.png", 40, 40,
			() -> this.currentMode = EditorMode.camera,
			() -> this.currentMode == EditorMode.camera
			, "Adjust camera (%s)", Game.game.input.editorCamera
	);

	EditorButton select = new EditorButton(buttons.topLeft, "select.png", 40, 40,
			new Runnable()
			{
				@Override
				public void run()
				{
					if (currentMode != EditorMode.select)
						currentMode = EditorMode.select;
					else
					{
						select.option = 0;
						select.setOption();
					}
				}
			},
			new ToBooleanFunction()
			{
				@Override
				public boolean apply()
				{
					if (currentMode == EditorMode.select)
					{
						if (!select.showSubButtons)
							return true;
						else
							return selectTool == SelectTool.normal;
					}

					return false;
				}
			}
			, "Select (%s)", Game.game.input.editorSelect
	)
			.addSubButtons(new EditorButton("wand.png", 40, 40, () -> {this.currentMode = EditorMode.select; selectTool = SelectTool.wand_contiguous;}, () -> selectTool == SelectTool.wand_contiguous, "Contiguous wand tool (%s)", Game.game.input.editorWand),
					new EditorButton("wand_discontiguous.png", 40, 40, () -> {this.currentMode = EditorMode.select; selectTool = SelectTool.wand_discontiguous;}, () -> selectTool == SelectTool.wand_discontiguous, "Non-contiguous wand tool (%s)", Game.game.input.editorWandDiscontiguous))
			.onReset(() -> selectTool = SelectTool.normal);

	EditorButton grab = new EditorButton(buttons.topLeft, "eyedropper.png", 50, 50,
			() -> this.currentMode = EditorMode.picker,
			() -> this.currentMode == EditorMode.picker
			, "Picker (%s)", Game.game.input.editorPickBlock
	);

	EditorButton undo = new EditorButton(buttons.bottomLeft, "undo.png", 40, 40, () ->
	{
		if (undoActions.isEmpty())
			return;

		int s = undoActions.size();
		for (int i = 0; i < Math.min(s, undoCount); i++)
		{
			EditorAction a = undoActions.remove(undoActions.size() - 1);
			a.undo();
			redoActions.add(a);
			redoLength = undoActions.size();

			if (a instanceof EditorAction.ActionPaste)
				break;
		}

	}, () -> undoActions.isEmpty(), "Undo (%s)", Game.game.input.editorUndo
	)
			.addSubButtons(
					new EditorButton("undo10.png", 50, 50, new Runnable()
					{
						@Override
						public void run()
						{
							undoCount = 10;
							undo.function.run();
							undoCount = 1;
						}
					}, () -> false, "Undo 10x (Shift %s)", Game.game.input.editorUndo),
					new EditorButton("undo50.png", 50, 50, new Runnable()
					{
						@Override
						public void run()
						{
							undoCount = 50;
							undo.function.run();
							undoCount = 1;
						}
					}, () -> false, "Undo 50x (Alt %s)", Game.game.input.editorUndo)
			).setSubButtonsAsOptions(false);

	EditorButton redo = new EditorButton(buttons.bottomLeft, "redo.png", 40, 40, () ->
	{
		if (redoActions.isEmpty())
			return;

		int s = redoActions.size();
		for (int i = 0; i < Math.min(s, redoCount); i++)
		{
			EditorAction a = redoActions.remove(redoActions.size() - 1);
			a.redo();
			undoActions.add(a);
			redoLength = undoActions.size();

			if (a instanceof EditorAction.ActionPaste)
				break;
		}
	}, () -> redoActions.isEmpty(), "Redo (%s)", Game.game.input.editorRedo
	)
			.addSubButtons(
					new EditorButton("redo10.png", 50, 50, new Runnable()
					{
						@Override
						public void run()
						{
							redoCount = 10;
							redo.function.run();
							redoCount = 1;
						}
					}, () -> false, "Redo 10x (Shift %s)", Game.game.input.editorRedo),
					new EditorButton("redo50.png", 50, 50, new Runnable()
					{
						@Override
						public void run()
						{
							redoCount = 50;
							redo.function.run();
							redoCount = 1;
						}
					}, () -> false, "Redo 50x (Alt %s)", Game.game.input.editorRedo)
			).setSubButtonsAsOptions(false);

	EditorButton copy = new EditorButton(buttons.topRight, "copy.png", 50, 50, () -> this.copy(false),
			() -> false, () -> selection, "Copy (%s)", Game.game.input.editorCopy);
	EditorButton cut = new EditorButton(buttons.topRight, "cut.png", 50, 50, () -> this.copy(true),
			() -> false, () -> selection, "Cut (%s)", Game.game.input.editorCut);
	EditorButton paste = new EditorButton(buttons.topLeft, "paste.png", 50, 50, () ->
	{
		if (this.currentMode == EditorMode.paste)
			this.paste();

		Game.game.window.pressedKeys.clear();
		Game.game.window.pressedButtons.clear();
		currentMode = EditorMode.paste;
	}, () -> this.currentMode == EditorMode.paste || clipboard.isEmpty(), () -> !clipboard.isEmpty(), "Paste (%s)", Game.game.input.editorPaste
	);

	EditorButton flipHoriz = new EditorButton(buttons.topRight, "flip_horizontal.png", 50, 50, () -> clipboard.flipHorizontal(),
			() -> false, () -> this.currentMode == EditorMode.paste, "Flip horizontal (%s)", Game.game.input.editorFlipHoriz);
	EditorButton flipVert = new EditorButton(buttons.topRight, "flip_vertical.png", 50, 50, () -> clipboard.flipVertical(),
			() -> false, () -> this.currentMode == EditorMode.paste, "Flip vertical (%s)", Game.game.input.editorFlipVert);
	EditorButton rotate = new EditorButton(buttons.topRight, "rotate_obstacle.png", 50, 50, () -> clipboard.rotate(),
			() -> false, () -> this.currentMode == EditorMode.paste, "Rotate clockwise (%s)", Game.game.input.editorRotateClockwise);

	EditorButton selectSquareToggle = new EditorButton(buttons.bottomRight, "square_unlocked.png", 40, 40, () ->
            lockSquare = !lockSquare, () -> false, () -> this.currentMode == EditorMode.select, "", Game.game.input.editorLockSquare
	).setDescription("Lock square selecting (Hold: %s, Toggle: %s)", Game.game.input.editorHoldSquare.getInputs(), Game.game.input.editorLockSquare.getInputs());

	EditorButton selectAddToggle = new EditorButton(buttons.bottomRight, "select_add.png", 40, 40, () ->
            lockAdd = !lockAdd, () -> false, () -> this.currentMode == EditorMode.select, "Toggle select/deselect (%s)", Game.game.input.editorSelectAddToggle
	);

	EditorButton selectClear = new EditorButton(buttons.bottomRight, "select_clear.png", 40, 40, this::clearSelection, () -> !selection, () -> this.currentMode == EditorMode.select || selection, "Clear selection (%s)", Game.game.input.editorDeselect);

	@SuppressWarnings("unchecked")
	protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

	public ScreenLevelEditor(String lvlName, Level level)
	{
		Game.playerTank = new TankPlayer(0, 0, 0);
		this.playerTankBuilds.add((TankPlayer) Game.playerTank);

		this.selfBatch = false;
		this.drawDarkness = false;

		this.music = "battle_editor.ogg";
		this.musicID = "editor";

		this.allowClose = true;

		if (Game.game.window.touchscreen)
			controlsSizeMultiplier = 1.0;

		this.level = level;
		this.enableMargins = false;

		for (int i = 0; i < drawables.length; i++)
		{
			drawables[i] = new ArrayList<>();
		}

		Obstacle.draw_size = Game.tile_size;

		Game.game.window.validScrollDown = false;
		Game.game.window.validScrollUp = false;

		this.name = lvlName;
	}

	public void updateMusic(boolean tanks)
	{
		if (Game.screen instanceof ScreenGame)
			return;

		this.prevTankMusics.clear();
		this.prevTankMusics.addAll(this.tankMusics);
		this.tankMusics.clear();

		this.tankMusics.addAll(this.overlayMusics);
		this.overlayMusics.clear();

		if (tanks)
		{
			for (Movable m : Game.movables)
			{
				if (m instanceof Tank && !m.destroy)
				{
					this.tankMusics.addAll(((Tank) m).musicTracks);
				}
			}

			Game.currentLevel.beatBlocks = 0;

			for (Obstacle o: Game.obstacles)
			{
				if (o instanceof ObstacleBeatBlock)
				{
					Game.currentLevel.synchronizeMusic = true;
					Game.currentLevel.beatBlocks |= (int) ((ObstacleBeatBlock) o).beatFrequency;
					break;
				}
			}

			if (Game.currentLevel.beatBlocks > 0)
			{
				this.tankMusics.add("beatblocks/beat_blocks.ogg");
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

	public boolean grab()
	{
		Game.game.input.editorPickBlock.invalidate();

		Tank t = null;
		for (Movable m: Game.movables)
		{
			if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank)
			{
				t = (Tank) m;
				break;
			}
		}

		if (t != null)
		{
			if (t.name.equals("player"))
			{
				this.currentPlaceable = Placeable.playerTank;
				this.refreshMouseObject();

				((TankPlayer) mouseTank).setDefaultColor();
				return true;
			}
			else
			{
				for (int i = 0; i < Game.registryTank.tankEntries.size(); i++)
				{
					RegistryTank.TankEntry e = Game.registryTank.getEntry(i);
					if (e.name.equals(t.name))
					{
						tankNum = i;
						this.currentPlaceable = Placeable.enemyTank;
						this.refreshMouseObject();
						return true;
					}
				}

				for (int i = 0; i < this.level.customTanks.size(); i++)
				{
					if (this.level.customTanks.get(i).name.equals(t.name))
					{
						tankNum = Game.registryTank.tankEntries.size() + i;
						this.currentPlaceable = Placeable.enemyTank;
						this.refreshMouseObject();
						return true;
					}
				}
			}
		}

		Obstacle o = null;
		for (Obstacle o1: Game.obstacles)
		{
			if (o1.posX == mouseObstacle.posX && o1.posY == mouseObstacle.posY)
			{
				o = o1;
				break;
			}
		}

		if (o != null)
		{
			int i = 0;
			for (RegistryObstacle.ObstacleEntry entry : Game.registryObstacle.obstacleEntries)
			{
				if (entry.obstacle.equals(o.getClass()))
					break;
				i++;
			}

			this.currentPlaceable = Placeable.obstacle;
			obstacleNum = i;
			this.refreshMouseObject();
			mouseObstacle.setMetadata(o.getMetadata());
			mouseObstacle.updateSelectors();
			return true;
		}

		return false;
	}

	@Override
	public void update()
	{
		if (!initialized)
			initialize();

		if (Level.isDark())
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;

		if (clipboard == null)
			clipboard = new EditorClipboard();

		allowClose = this.undoActions.isEmpty() && !modified;
		clickCooldown = Math.max(0, clickCooldown - Panel.frameFrequency);

		if (grab.keybind.isValid())
		{
			if (!this.grab())
				this.currentMode = EditorMode.picker;
			else
				this.currentMode = EditorMode.build;
		}

		if (undoActions.size() > 0 && undo.keybind.isValid())
		{
			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT))
				this.undoCount = 10;

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_ALT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_ALT))
				this.undoCount = 50;

			if (this.undoCount > 1)
			{
				this.undo.function.run();
				this.undoCount = 1;
				this.undo.keybind.invalidate();
			}
		}

		if (redoActions.size() > 0 && redo.keybind.isValid())
		{
			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT))
				this.redoCount = 10;

			if (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_ALT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT_ALT))
				this.redoCount = 50;

			if (this.redoCount > 1)
			{
				this.redo.function.run();
				this.redoCount = 1;
				this.redo.keybind.invalidate();
			}
		}

		buttons.update();

		this.updateMusic(true);

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

		if (Game.game.input.editorPaste.isValid() && !clipboard.isEmpty())
		{
			if (this.currentMode == EditorMode.paste)
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

		for (Effect e : Game.effects)
			e.update();

		if (this.paused)
			return;

		(currentPlaceable == Placeable.obstacle ? mouseObstacle : mouseTank).forAllSelectors(s ->
		{
			if (s.keybind != null && s.keybind.isValid())
			{
				s.keybind.invalidate();
				s.onSelect();
			}
		});

		if (Game.game.input.editorRevertCamera.isValid())
		{
			zoom = 1;
			offsetX = 0;
			offsetY = 0;

			Game.game.input.editorRevertCamera.invalidate();
		}

		if (currentMode == EditorMode.camera || Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_ALT))
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
		else if (currentMode == EditorMode.build)
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
				this.refreshMouseObject();
			}
			else if (down && currentPlaceable == Placeable.obstacle)
			{
				obstacleNum = (obstacleNum + 1) % Game.registryObstacle.obstacleEntries.size();
				this.refreshMouseObject();
			}

			if (up && currentPlaceable == Placeable.enemyTank)
			{
				tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size() + this.level.customTanks.size()) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
				this.refreshMouseObject();

			}
			else if (up && currentPlaceable == Placeable.obstacle)
			{
				obstacleNum = ((obstacleNum - 1) + Game.registryObstacle.obstacleEntries.size()) % Game.registryObstacle.obstacleEntries.size();
				this.refreshMouseObject();
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
					this.currentPlaceable = Placeable.obstacle;
					this.mouseObstacle.initSelectors(this);
					this.mouseObstacle.forAllSelectors(LevelEditorSelector::addShortcutButton);
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					this.currentPlaceable = Placeable.playerTank;
					this.mouseTank = new TankPlayer(0, 0, 0);
					this.mouseTank.initSelectors(this);
					this.mouseTank.forAllSelectors(LevelEditorSelector::addShortcutButton);

					((TankPlayer) mouseTank).setDefaultColor();
				}
				else if (currentPlaceable == Placeable.playerTank)
				{
					this.currentPlaceable = Placeable.enemyTank;
					this.refreshMouseObject();
				}
			}

			if (left)
			{
				if (currentPlaceable == Placeable.playerTank)
				{
					this.currentPlaceable = Placeable.obstacle;
					this.refreshMouseObject();
				}
				else if (currentPlaceable == Placeable.obstacle)
				{
					this.currentPlaceable = Placeable.enemyTank;
					this.refreshMouseObject();
				}
				else if (currentPlaceable == Placeable.enemyTank)
				{
					this.currentPlaceable = Placeable.playerTank;
					this.refreshMouseObject();
				}
			}

			updateMetadata();
		}
		else if (currentMode == EditorMode.select)
			updateMetadata();

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

			for (int i : Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				if (p.tag.isEmpty() || p.tag.equals("levelbuilder"))
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

		double px = Drawing.drawing.toInterfaceCoordsX(panCurrentX);
		double py = Drawing.drawing.toInterfaceCoordsY(panCurrentY);

		if (!zoomDown && panDown)
		{
			if (prevPanDown && !prevZoomDown)
			{
				offsetX += panCurrentX - panX;
				offsetY += panCurrentY - panY;
			}

			panX = Drawing.drawing.toGameCoordsX(px);
			panY = Drawing.drawing.toGameCoordsY(py);
		}


		if (zoomDown)
		{
			double zx = Drawing.drawing.toInterfaceCoordsX(zoomCurrentX);
			double zy = Drawing.drawing.toInterfaceCoordsY(zoomCurrentY);
			double d = Math.sqrt(Math.pow(px - zx, 2) + Math.pow(py - zy, 2));

			if (prevZoomDown)
			{
				zoom *= d / zoomDist;
				zoom = Math.max(0.75, Math.min(Math.max(2 / (Drawing.drawing.unzoomedScale / Drawing.drawing.interfaceScale), 1), zoom));
				Drawing.drawing.scale = getScale();

				panCurrentX = Drawing.drawing.toGameCoordsX(px);
				panCurrentY = Drawing.drawing.toGameCoordsY(py);
				zoomCurrentX = Drawing.drawing.toGameCoordsX(zx);
				zoomCurrentY = Drawing.drawing.toGameCoordsY(zy);

				double x = (panCurrentX + zoomCurrentX) / 2;
				double y = (panCurrentY + zoomCurrentY) / 2;
				offsetX += x - panX;
				offsetY += y - panY;
			}

			panCurrentX = Drawing.drawing.toGameCoordsX(px);
			panCurrentY = Drawing.drawing.toGameCoordsY(py);
			zoomCurrentX = Drawing.drawing.toGameCoordsX(zx);
			zoomCurrentY = Drawing.drawing.toGameCoordsY(zy);

			double x = (panCurrentX + zoomCurrentX) / 2;
			double y = (panCurrentY + zoomCurrentY) / 2;
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


		if (Game.game.input.editorPlay.isValid() && !this.spawns.isEmpty())
		{
			this.play();
			Game.game.input.play.invalidate();
		}

		if (Game.game.input.editorDeselect.isValid())
		{
			if (selection)
			{
				this.clearSelection();
			}
			else if (Game.game.window.shift)
			{
				Arrays.fill(clipboards, null);
				clipboard = new EditorClipboard();
			}
		}

		if (Game.game.input.editorToggleControls.isValid())
		{
			this.showControls = !this.showControls;
			Game.game.input.editorToggleControls.invalidate();
		}

		if (Game.game.input.editorPaste.isValid() && this.currentMode != EditorMode.paste && !clipboard.isEmpty())
		{
			this.currentMode = EditorMode.paste;
			Game.game.input.editorPaste.invalidate();
		}

		if (!redoActions.isEmpty() && redoLength != undoActions.size())
		{
			redoActions.clear();
			redoLength = -1;
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
				Game.redrawGroundTiles.add(new Game.GroundTile(x, y));

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

	public void updateMetadata()
	{
		if (Game.game.input.editorPrevMeta.isValid())
		{
			Game.game.input.editorPrevMeta.invalidate();
			if (currentMode == EditorMode.select && heightBlocksSelected)
				adjustSelectionHeight(-1);
			else if (currentMode == EditorMode.build)
				changeMetadata(-1);
		}

		if (Game.game.input.editorNextMeta.isValid())
		{
			Game.game.input.editorNextMeta.invalidate();
			if (currentMode == EditorMode.select && heightBlocksSelected)
				adjustSelectionHeight(1);
			else if (currentMode == EditorMode.build)
				changeMetadata(1);
		}
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

		if (currentMode == EditorMode.picker)
		{
			if (validLeft)
			{
				handled[0] = true;
				if (grab())
					this.currentMode = EditorMode.build;
			}
		}
		else if (currentMode == EditorMode.camera || (Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_ALT) && validLeft))
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
		else if (currentMode == EditorMode.select && (selectTool == SelectTool.wand_contiguous || selectTool == SelectTool.wand_discontiguous) && (validLeft || validRight))
		{
			if (validRight)
				selectInverted = !selectInverted;
			magicSelect((int) (clampTileX(mouseObstacle.posX) / Game.tile_size), (int) (clampTileY(mouseObstacle.posY) / Game.tile_size), selectTool == SelectTool.wand_contiguous);

			if (validRight)
			{
				selectInverted = !selectInverted;
				handled[1] = true;
			}
			else
				handled[0] = true;
		}
		else if ((currentMode == EditorMode.select || (specialBuildTool() && !right)))
		{
			if (!selection)
				lockAdd = true;

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

				if (lockSquare || Game.game.input.editorHoldSquare.isPressed())
				{
					double size = Math.max(Math.abs(selectX2 - selectX1), Math.abs(selectY2 - selectY1));
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
				double highX = Math.min((Game.currentSizeX - 0.5) * Game.tile_size, Math.max(selectX1, selectX2));
				double lowY = Math.min(selectY1, selectY2);
				double highY = Math.min((Game.currentSizeY - 0.5) * Game.tile_size, Math.max(selectY1, selectY2));

				if (currentMode == EditorMode.select && selectTool == SelectTool.normal)
					newSelection(lowX, highX, lowY, highY);
				else
					shapeFromSelection(handled, lowX, highX, lowY, highY);
			}
			else
                selectInverted = selection && ((!lockAdd && !right) || (lockAdd && right));
		}
		else if (symmetrySelectMode && currentMode != EditorMode.paste)
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
			int x = (int) (mouseObstacle.posX / Game.tile_size);
			int y = (int) (mouseObstacle.posY / Game.tile_size);

			if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
			{
				if (selectedTiles[x][y] && (validLeft || validRight) && !(currentPlaceable == Placeable.playerTank && this.movePlayer))
				{
					double ox = mouseObstacle.posX;
					double oy = mouseObstacle.posY;

					ArrayList<EditorAction> actions = this.undoActions;
					this.undoActions = new ArrayList<>();

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

					if (!this.undoActions.isEmpty())
					{
						EditorAction a = new EditorAction.ActionGroup(this, this.undoActions);
						actions.add(a);
						Drawing.drawing.playVibration("click");
					}

					this.undoActions = actions;

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

	public void shapeFromSelection(boolean[] handled, double lowX, double highX, double lowY, double highY)
	{
		Shape s = Shape.getShape(buildTool, lowX, highX, lowY, highY, selectX1 < selectX2, selectY1 < selectY2);

		ArrayList<EditorAction> prevActions = this.undoActions;
		this.undoActions = new ArrayList<>();

		for (int i = 0; i < s.size(); i++)
		{
			double x = s.xs.get(i), y = s.ys.get(i);

			mouseTank.posX = mouseObstacle.posX = (x + 0.5) * Game.tile_size;
			mouseTank.posY = mouseObstacle.posY = (y + 0.5) * Game.tile_size;

			handlePlace(handled, true, false, true, false, true, false);
		}

		prevActions.add(new EditorAction.ActionPaste(this, this.undoActions));
		this.undoActions = prevActions;
	}

	public static class Shape
	{
		static int[] rectX = {0, 1, 0, 1, 0, 0, 1, 1};
		static int[] rectY = {0, 0, 1, 1, 0, 1, 0, 1};

		public final ArrayList<Integer> xs = new ArrayList<>();
		public final ArrayList<Integer> ys = new ArrayList<>();

		private Shape() {}

		public int size()
		{
			return xs.size();
		}

		public static Shape getShape(BuildTool tool, double lowX, double highX, double lowY, double highY, boolean xa, boolean ya)
		{
			Shape s = new Shape();

			int lx = (int) (lowX / Game.tile_size);
			int hx = (int) (highX / Game.tile_size);
			int ly = (int) (lowY / Game.tile_size);
			int hy = (int) (highY / Game.tile_size);
			int width = hx-lx, length = hy-ly;
			boolean direction = xa;
			if (ya)
				direction = !direction;

			if (tool == BuildTool.rectangle || (tool == BuildTool.line && (width == 0 || length == 0)))
			{
				for (int i = 0; i < rectX.length; i += 2)
				{
					for (int x = lx + width * rectX[i]; x <= lx + width * rectX[i+1]; x++)
					{
						for (int y = ly + length * rectY[i]; y <= ly + length * rectY[i+1]; y++)
						{
							s.xs.add(x);
							s.ys.add(y);
						}
					}
				}
			}
			else if (tool == BuildTool.circle)
			{
				for (double t = 0; t < Math.PI * 2; t += Math.PI / Math.max(width, length) / 2)
				{
					int x = (int) (lx + Math.cos(t) * 0.5 * width + width / 2);
					int y = (int) (ly + Math.sin(t) * 0.5 * length + length / 2);
					if (x == hx) x--;
					if (y == hy) y--;

					s.xs.add(x);
					s.ys.add(y);
				}
			}
			else if (tool == BuildTool.line)
			{
				for (double x = 0; x <= width; x += 1. / length)
				{
					int y = (int) ((double) length/width * (direction ? -1 : 1) * x + ly + (direction ? length : 0));
					s.xs.add((int) (x + lx));
					s.ys.add(y);
				}
			}

			return s;
		}
	}


	public void newSelection(double lowX, double highX, double lowY, double highY)
	{
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

		this.undoActions.add(new EditorAction.ActionSelectTiles(this, !selectInverted, px, py));

		this.refreshSelection();
	}

	public void initialize()
	{
		if (!this.initialized)
		{
			this.initialized = true;
			this.clickCooldown = 50;

			if (this.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
			{
				this.mouseObstacle.initSelectors(this);
				this.mouseObstacle.forAllSelectors(LevelEditorSelector::addShortcutButton);
			}
			else
			{
				this.mouseTank.initSelectors(this);
				this.mouseTank.forAllSelectors(LevelEditorSelector::addShortcutButton);
			}

			this.cloneSelectorProperties();
		}

		selectSquareToggle.moveToBottom();
		selectAddToggle.moveToBottom();
		selectClear.moveToBottom();
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
				if (validLeft && currentMode == EditorMode.paste && !paste)
				{
					paste();

					mouseTank.posX = originalX;
					mouseTank.posY = originalY;
					mouseTank.orientation = originalOrientation;

					mouseObstacle.posX = originalX;
					mouseObstacle.posY = originalY;

					return new boolean[]{true, true};
				}

				if ((currentMode == EditorMode.build && right) || (currentMode == EditorMode.erase && (left || right)))
				{
					boolean skip = false;

					if (validRight || (currentMode == EditorMode.erase && validLeft))
					{
						for (Movable m : Game.movables)
						{
							if (m.posX == mouseTank.posX && m.posY == mouseTank.posY && m instanceof Tank && !(this.spawns.contains(m) && this.spawns.size() == 1))
							{
								skip = true;

								if (m instanceof TankSpawnMarker)
								{
									this.spawns.remove(m);
									this.undoActions.add(new EditorAction.ActionPlayerSpawn(this, (TankSpawnMarker) m, false));
								}
								else
									this.undoActions.add(new EditorAction.ActionTank((Tank) m, false));

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
							this.undoActions.add(new EditorAction.ActionObstacle(m, false));
							Game.removeObstacles.add(m);

							if (!batch)
								Drawing.drawing.playVibration("click");

							break;
						}
					}

					if (!batch && !Game.game.window.touchscreen && !skip && validRight)
					{
						int add = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) ? -1 : 1;

						if (currentPlaceable == Placeable.obstacle)
						{
							if (mouseObstacle.selectorCount() > 1)
								mouseObstacle.selectors.get(1).changeMeta(add);
						}
						else if (mouseTank.selectorCount() > 1)
							mouseTank.selectors.get(1).changeMeta(add);
					}

					if (Game.game.window.touchscreen)
						handled[0] = true;

					handled[1] = true;
				}

				if (currentMode != EditorMode.erase && clickCooldown <= 0 && (validLeft || (currentMode != EditorMode.paste && left && currentPlaceable == Placeable.obstacle && this.mouseObstacle.draggable)))
				{
					boolean skip = false;

					double mx = mouseTank.posX;
					double my = mouseTank.posY;

					if (currentPlaceable == Placeable.obstacle)
					{
						mx = mouseObstacle.posX;
						my = mouseObstacle.posY;
					}

					if (mouseObstacleStartHeight < 1 && mouseObstacle.type == Obstacle.ObstacleType.full || currentPlaceable != Placeable.obstacle)
					{
						for (Movable m : Game.movables)
						{
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
								if (m.getClass() == mouseObstacle.getClass() || !Obstacle.canPlaceOn(mouseObstacle.type, m.type))
								{
									skip = true;
									break;
								}
							}
							else
							{
								this.undoActions.add(new EditorAction.ActionObstacle(m, false));
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

							t.cloneAllSelectors(mouseTank);

							this.undoActions.add(new EditorAction.ActionTank(t, true));
							Game.movables.add(t);

							if (!batch)
								Drawing.drawing.playVibration("click");
						}
						else if (currentPlaceable == Placeable.playerTank)
						{
							ArrayList<TankSpawnMarker> spawnsClone = (ArrayList<TankSpawnMarker>) spawns.clone();
							if (this.movePlayer && !paste)
							{
								for (Movable m : Game.movables)
								{
									if (m instanceof TankSpawnMarker)
										Game.removeMovables.add(m);
								}

								this.spawns.clear();
							}

							TankSpawnMarker t = new TankSpawnMarker("player", mouseTank.posX, mouseTank.posY, mouseTank.angle);
							t.registerSelectors();
							t.cloneAllSelectors(mouseTank);
							t.updateSelectors();

							this.spawns.add(t);

							if (this.movePlayer && !paste)
								this.undoActions.add(new EditorAction.ActionMovePlayer(this, spawnsClone, t));
							else
								this.undoActions.add(new EditorAction.ActionPlayerSpawn(this, t, true));

							Game.movables.add(t);

							if (!batch)
								Drawing.drawing.playVibration("click");

							if (this.movePlayer)
								t.drawAge = 50;
						}
						else if (currentPlaceable == Placeable.obstacle)
						{
							Obstacle o = !paste ? Game.registryObstacle.getEntry(obstacleNum)
									.getObstacle(mouseObstacle.posX / Game.tile_size - 0.5, mouseObstacle.posY / Game.tile_size - 0.5)
									: mouseObstacle;
							o.startHeight = mouseObstacleStartHeight;
							o.initSelectors(this);

							o.cloneAllSelectors(mouseObstacle);

							if (o.enableStacking)
							{
								SelectorStackHeight s = (SelectorStackHeight) o.getSelector("stack_height");
								s.number = mouseObstacle.stackHeight;

								if (this.stagger && !paste)
								{
									if ((((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == (this.oddStagger ? 1 : 0)))
										s.number -= 0.5;
								}
							}

							this.undoActions.add(new EditorAction.ActionObstacle(o, true));
							double x = o.posX;
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

	public boolean[] handlePlace(boolean[] handled, boolean left, boolean right, boolean validLeft, boolean validRight, boolean batch)
	{
		return handlePlace(handled, left, right, validLeft, validRight, batch, false);
	}

	public void save()
	{
		this.save(this.name);
	}

	public void save(String levelName)
	{
		StringBuilder level = new StringBuilder("{");

		if (!this.level.editable)
			level.append("*");

		level.append(this.level.sizeX).append(",").append(this.level.sizeY).append(",").append(this.level.colorR).append(",").append(this.level.colorG).append(",").append(this.level.colorB).append(",").append(this.level.colorVarR).append(",").append(this.level.colorVarG).append(",").append(this.level.colorVarB)
				.append(",").append((int) (this.level.timer / 100)).append(",").append((int) Math.round(this.level.light * 100)).append(",").append((int) Math.round(this.level.shadow * 100)).append("|");

		ArrayList<Obstacle> unmarked = (ArrayList<Obstacle>) Game.obstacles.clone();
		String[][][] obstacles = new String[Game.registryObstacle.obstacleEntries.size()][this.level.sizeX][this.level.sizeY];

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
					obstacles[h][x][y] = o.getMetadata();

					unmarked.remove(o);
				}

				//level += x + "-" + y + ",";
			}

			//compression
			for (int i = 0; i < this.level.sizeX; i++)
			{
				for (int j = 0; j < this.level.sizeY; j++)
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
							name = "-" + obsName;

						if (xLength >= yLength)
						{
							if (xLength == 1)
								level.append(i).append("-").append(j).append(name);
							else
								level.append(i).append("...").append(i + xLength - 1).append("-").append(j).append(name);

							if ((obs.enableStacking && !("1.0".equals(stack))) || (obs.enableGroupID && !("0".equals(stack))))
								level.append("-").append(stack);

							level.append(",");

							for (int z = 0; z < xLength; z++)
							{
								obstacles[h][i + z][j] = null;
							}
						}
						else
						{
							level.append(i).append("-").append(j).append("...").append(j + yLength - 1).append(name);

							if ((obs.enableStacking && !("1.0".equals(stack))) || (obs.enableGroupID && !("0".equals(stack))))
								level.append("-").append(stack);

							level.append(",");

							for (int z = 0; z < yLength; z++)
							{
								obstacles[h][i][j + z] = null;
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
			else
			{
				if (u.enableGroupID)
					level.append("-").append(u.groupID);

				if (u.enableStacking)
					level.append("-").append(u.stackHeight);
			}


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

		if (this.level.startingCoins > 0)
			level.append("\ncoins\n").append(this.level.startingCoins);

		if (!this.level.shop.isEmpty())
		{
			level.append("\nshop");

			for (Item.ShopItem i : this.level.shop)
				level.append("\n").append(i.toString());
		}

		if (!this.level.startingItems.isEmpty())
		{
			level.append("\nitems");

			for (Item.ItemStack<?> i : this.level.startingItems)
				level.append("\n").append(i.toString());
		}

		if (!this.level.customTanks.isEmpty())
		{
			level.append("\ntanks");

			for (Tank t : this.level.customTanks)
				level.append("\n").append(t.toString());
		}

		Game.currentLevelString = level.toString();

		BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + levelName);
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

	public void paste()
	{
		Drawing.drawing.playVibration("click");

		ArrayList<EditorAction> actions = this.undoActions;
		this.undoActions = new ArrayList<>();

		boolean[] handled = new boolean[2];

		Tank prevMouseTank = mouseTank;
		Obstacle prevMouseObstacle = mouseObstacle;
		Placeable placeable = currentPlaceable;

		double mx = prevMouseObstacle.posX;
		double my = prevMouseObstacle.posY;

		int prevSize = Game.movables.size() + Game.obstacles.size();

		for (Obstacle o : clipboard.obstacles)
		{
			currentPlaceable = Placeable.obstacle;

			try
			{
				Obstacle n = o.getClass().getConstructor(String.class, double.class, double.class).newInstance(o.name, (o.posX + mx) / Game.tile_size - 0.5, (o.posY + my) / Game.tile_size - 0.5);
				n.selectors = o.selectors;
				n.groupID = o.groupID;
				n.stackHeight = o.stackHeight;
				n.startHeight = o.startHeight;
				mouseObstacle = n;
				mouseObstacle.stackHeight = n.stackHeight;
                mouseObstacleStartHeight = n.startHeight;
				mouseTank.posX = mouseObstacle.posX;
				mouseTank.posY = mouseObstacle.posY;

				handlePlace(handled, true, false, true, false, true, true);

				mouseTank.posX = mouseObstacle.posX;
				mouseTank.posY = mouseObstacle.posY;
			}
			catch (Exception e)
			{
				Game.exitToCrash(e.getCause());
			}
		}

		mouseObstacle = prevMouseObstacle;    	// this line is important! debugging this took at least 1.5 hrs

		for (Tank t : clipboard.tanks)
		{
			currentPlaceable = Placeable.enemyTank;

			try
			{
				Tank n;

				if (t.getClass().equals(TankAIControlled.class))
				{
					n = new TankAIControlled(t.name, t.posX + mx, t.posY + my, t.size, t.colorR, t.colorG, t.colorB, t.angle, ((TankAIControlled) t).shootAIType);
					((TankAIControlled) t).cloneProperties((TankAIControlled) n);
				}
				else
					n = t.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(t.name, t.posX + mx, t.posY + my, t.angle);

				n.team = t.team;
				n.destroy = t.destroy;
				mouseTank = n;

				if (n instanceof TankSpawnMarker || n instanceof TankPlayer)
					currentPlaceable = Placeable.playerTank;

				handlePlace(handled, true, false, true, false, true, true);
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

		currentPlaceable = placeable;
		mouseTank = prevMouseTank;
		mouseObstacle = prevMouseObstacle;

		mouseObstacleStartHeight = mouseObstacle.startHeight;

		ArrayList<EditorAction> tempActions = this.undoActions;
		this.undoActions = actions;

		if (Game.movables.size() + Game.obstacles.size() > prevSize)
			this.undoActions.add(new EditorAction.ActionPaste(this, tempActions));
	}

	@Override
	public void draw()
	{
		if (Level.isDark())
			this.fontBrightness = 255;
		else
			this.fontBrightness = 0;

//		windowTitle = (allowClose ? "" : "*");

		if (Panel.panel.continuation == null)
		{
			for (Obstacle o : Game.obstacles)
                o.baseGroundHeight = Game.sampleGroundHeight(o.posX, o.posY);

			if (Game.enable3d)
				Game.recomputeHeightGrid();

			Drawing.drawing.setColor(174, 92, 16);

			double mul = 1;
			if (Game.angledView)
				mul = 2;

			Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
					mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
		}

		this.drawDefaultBackground();

		for (Movable m: Game.movables)
			drawables[m.drawLevel].add(m);

		mouseTank.updateSelectors();
		mouseObstacle.updateSelectors();

		for (Obstacle o : Game.obstacles)
		{
			if (!o.batchDraw || !Game.enable3d)
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
			if (currentMode == EditorMode.erase)
			{
				Drawing.drawing.setColor(255, 0, 0, 64, 0.3);

				if (Game.enable3d)
				{
					if (hoverObstacle == null)
						Drawing.drawing.fillBox(mouseObstacle.posX, mouseObstacle.posY, 0, Game.tile_size, Game.tile_size, Game.tile_size, (byte) 64);
					else
						hoverObstacle.draw3dOutline(255, 0, 0, 64);
				}
				else
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
			}
			else if (currentMode == EditorMode.paste)
			{
				Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
				Drawing.drawing.drawImage("icons/paste.png", mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);

				for (Obstacle o : clipboard.obstacles)
				{
					Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB, 64, 0.5);
					Drawing.drawing.fillRect(o.posX + mouseTank.posX, o.posY + mouseTank.posY, /*0,*/ Game.tile_size, Game.tile_size/*, ((Obstacle) o).stackHeight * Game.tile_size, (byte) 64*/);
				}

				for (Tank t : clipboard.tanks)
                    t.drawOutlineAt(t.posX + mouseTank.posX, t.posY + mouseTank.posY);
			}
			else if (currentMode == EditorMode.picker)
			{
				Drawing.drawing.setColor(0, 0, 0, 127);
				Drawing.drawing.drawRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size, 10, 0);
				Drawing.drawing.setColor(255, 255, 255, 255, 1);
				Drawing.drawing.drawImage("icons/eyedropper.png", mouseObstacle.posX - Game.tile_size / 2, mouseObstacle.posY - Game.tile_size / 2, Game.tile_size, Game.tile_size);
			}
			else if ((currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && currentMode == EditorMode.build)
			{
				mouseTank.drawOutline();
				mouseTank.drawTeam();

				if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
				{
					Drawing.drawing.setColor(0, 200, 255, 127);
					Drawing.drawing.drawImage("emblems/player_spawn.png", mouseTank.posX, mouseTank.posY, mouseTank.size * 0.7, mouseTank.size * 0.7);
				}
			}
			else if (currentPlaceable == Placeable.obstacle && currentMode == EditorMode.build)
			{
				int x = (int) (mouseObstacle.posX / Game.tile_size);
				int y = (int) (mouseObstacle.posY / Game.tile_size);

				if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY /*&&
						(Game.getObstacle(x, y) == null || (Game.getSurfaceObstacle(x, y) == null &&
								Game.getObstacle(x, y).isSurfaceTile != mouseObstacle.isSurfaceTile && !mouseObstacle.tankCollision))*/)
				{
					mouseObstacle.startHeight = mouseObstacleStartHeight;

					if (Game.enable3d)
					{
						if (Game.isOrdered(-1, x, Game.currentSizeX) && Game.isOrdered(-1, y, Game.currentSizeY) /*&&
								(Game.getObstacle(x, y) == null || !Game.isOrdered(Game.getObstacle(x, y).startHeight, mouseObstacle.stackHeight + mouseObstacleStartHeight, Game.getObstacle(x, y).stackHeight))*/)
							mouseObstacle.draw3dOutline(mouseObstacle.colorR, mouseObstacle.colorG, mouseObstacle.colorB, 100);
					}

					mouseObstacle.drawOutline();

					if (mouseObstacle.hasCustomSelectors())
					{
						Drawing.drawing.setFontSize(16);
						Drawing.drawing.setColor(mouseObstacle.colorR / 2, mouseObstacle.colorG / 2, mouseObstacle.colorB / 2, 255);
						Drawing.drawing.drawText(mouseObstacle.posX, mouseObstacle.posY, mouseObstacle.selectors.get(0).getMetadata());
					}
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

		if (currentMode == EditorMode.camera && !paused)
		{
			Drawing.drawing.setColor(0, 0, 0, 127);

			Drawing.drawing.drawPopup(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2, 500 * objWidth / 350, 150 * objHeight / 40);

			Drawing.drawing.setColor(255, 255, 255);

			Drawing.drawing.setInterfaceFontSize(this.textSize);
			Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - this.objYSpace / 2, "Drag to pan");

			if (Game.game.window.touchscreen)
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Pinch to zoom");
				recenter.draw();
//				if (panDown)
//				{
//					Drawing.drawing.setColor(255, 255, 255);
//					Drawing.drawing.setInterfaceFontSize(24);
//					Drawing.drawing.fillOval(this.panCurrentX, this.panCurrentY, 40, 40);
//					Drawing.drawing.drawInterfaceText(100, 100, (int) this.panCurrentX + " " + (int) this.panCurrentY);
//				}
//				Drawing.drawing.setColor(255, 0, 255);
//				Drawing.drawing.fillOval(this.panX, this.panY, 40, 40);
//				Drawing.drawing.drawInterfaceText(100, 200, (int) this.panX + " " + (int) this.panY);
//				if (zoomDown)
//				{
//					Drawing.drawing.setColor(0, 255, 255);
//					Drawing.drawing.fillOval(this.zoomCurrentX, this.zoomCurrentY, 40, 40);
//					Drawing.drawing.drawInterfaceText(100, 300, (int) this.zoomCurrentX + " " + (int) this.zoomCurrentY);
//				}
			}
			else
			{
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 - 0, "Scroll or press %s or %s to zoom", Game.game.input.editorZoomIn.getInputs(), Game.game.input.editorZoomOut.getInputs());
				Drawing.drawing.displayInterfaceText(this.centerX, Drawing.drawing.interfaceSizeY - this.objYSpace * 2 + this.objYSpace / 2, "Press %s to re-center", Game.game.input.editorRevertCamera.getInputs());
			}
		}

		if ((currentMode == EditorMode.select || symmetrySelectMode || specialBuildTool()) && !this.paused)
		{
			double lowX = Math.min(selectX1, selectX2);
			double highX = Math.max(selectX1, selectX2);
			double lowY = Math.min(selectY1, selectY2);
			double highY = Math.max(selectY1, selectY2);

			if (currentMode == EditorMode.select)
				previewSelection(lowX, highX, lowY, highY, extra);
			else
				previewShape(lowX, highX, lowY, highY);
		}

		buttons.draw();

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

			if (lockAdd)
				selectAddToggle.image = "icons/select_add.png";
			else
				selectAddToggle.image = "icons/select_remove.png";

			if (lockSquare)
				selectSquareToggle.image = "icons/square_locked.png";
			else
				selectSquareToggle.image = "icons/square_unlocked.png";
		}
	}

	public void changeMetadata(int add)
	{
		if (currentPlaceable == Placeable.obstacle)
		{
			if (mouseObstacle.selectorCount() > 0)
				mouseObstacle.selectors.get(0).changeMeta(add);
		}
		else if (mouseTank.selectorCount() > 0)
			mouseTank.selectors.get(0).changeMeta(add);
	}

	public void previewSelection(double lowX, double highX, double lowY, double highY, double extra)
	{
		if (!selectInverted)
			Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
		else
			Drawing.drawing.setColor(0, 0, 0, 127, 0.3);

		if (!selectHeld)
		{
			if (!Game.game.window.touchscreen)
			{
				if (hoverObstacle == null)
					Drawing.drawing.fillRect(mouseObstacle.posX, mouseObstacle.posY, Game.tile_size, Game.tile_size);
				else
					hoverObstacle.draw3dOutline(230 + extra, 230 + extra, 230 + extra, 128);
			}
		}
		else
		{
			for (double x = lowX; x <= highX; x += Game.tile_size)
			{
				for (double y = lowY; y <= highY; y += Game.tile_size)
				{
					int gridX = (int) (x / Game.tile_size);
					int gridY = (int) (y / Game.tile_size);

					if (Game.enable3d)
					{
//						if (Game.getObstacle(gridX, gridY) != null)
//						{
//							Game.getObstacle(gridX, gridY).draw3dOutline(230 + extra, 230 + extra, 230 + extra, 128);
//						}
//						else
						{
							if (!selectInverted)
								Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
							else
								Drawing.drawing.setColor(0, 0, 0, 127, 0.3);

							// For now, 2d because we don't have grid lookup
							Drawing.drawing.fillRect(x, y, Game.tile_size, Game.tile_size);
						}
					}
					else
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

	public void magicSelect(int x, int y, boolean contiguous)
	{
		String obstacleName = null;
		for (Obstacle o : Game.obstacles)
		{
			if ((int) (o.posX / Game.tile_size) == x && (int) (o.posY / Game.tile_size) == y)
			{
				obstacleName = o.name;
				break;
			}
		}

		boolean[][] obstacleGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		ArrayList<Integer> xPos = new ArrayList<>();
		ArrayList<Integer> yPos = new ArrayList<>();

		for (Obstacle o : Game.obstacles)
		{
			int i = (int) (o.posX / Game.tile_size);
			int j = (int) (o.posY / Game.tile_size);

			if (i >= 0 && i < Game.currentSizeX && j >= 0 && j < Game.currentSizeY)
			{
				if (obstacleName == null || o.name.equals(obstacleName))
					obstacleGrid[i][j] = true;
			}
		}

		if (!contiguous)
		{
			for (int i = 0; i < obstacleGrid.length; i++)
			{
				for (int j = 0; j < obstacleGrid[i].length; j++)
				{
					if (obstacleGrid[i][j] == (obstacleName != null) && selectedTiles[i][j] == selectInverted)
					{
						selectedTiles[i][j] = !selectInverted;
						xPos.add(i);
						yPos.add(j);
					}
				}
			}
		}
		else
		{
			Deque<int[]> points = new ArrayDeque<>();
			points.add(new int[]{x, y});
			boolean[][] explored = new boolean[Game.currentSizeX][Game.currentSizeY];

			while (!points.isEmpty())
			{
				int[] next = points.removeFirst();

				int i = next[0];
				int j = next[1];
				if (i >= 0 && i < Game.currentSizeX && j >= 0 && j < Game.currentSizeY && !explored[next[0]][next[1]])
				{
					explored[i][j] = true;

					if (obstacleGrid[i][j] != (obstacleName == null))
					{
						if (selectedTiles[i][j] == selectInverted)
						{
							selectedTiles[i][j] = !selectInverted;
							xPos.add(i);
							yPos.add(j);
						}

						points.add(new int[]{i - 1, j});
						points.add(new int[]{i + 1, j});
						points.add(new int[]{i, j - 1});
						points.add(new int[]{i, j + 1});
					}
				}
			}
		}

		if (xPos.size() > 0)
		{
			this.undoActions.add(new EditorAction.ActionSelectTiles(this, !selectInverted, xPos, yPos));
			this.refreshSelection();
		}

//		boolean obs = Game.getObstacle(x, y) != null || Game.getSurfaceObstacle(x, y) == null;
//		Obstacle o = obs ? Game.getObstacle(x, y) : Game.getSurfaceObstacle(x, y);
//		Class<? extends Obstacle> cls = o != null ? o.getClass() : null;
//		ArrayList<Integer> xs = new ArrayList<>(), ys = new ArrayList<>();
//		ArrayDeque<Point> deque = new ArrayDeque<>();
//		deque.add(new Point(x, y));
//
//		selection = true;
//
//		while (!deque.isEmpty())
//		{
//			Point p = deque.pop();
//			for (int i = 0; i < 4; i++)
//			{
//				int newX = p.x + Game.dirX[i];
//				int newY = p.y + Game.dirY[i];
//
//				if (newX < 0 || newX >= Game.currentSizeX || newY < 0 || newY >= Game.currentSizeY || selectedTiles[newX][newY] ||
//						Math.abs(newX - x) + Math.abs(newY - y) > 50)
//					continue;
//
//				Obstacle o1 = obs ? Game.getObstacle(newX, newY) : Game.getSurfaceObstacle(newX, newY);
//				if (cls == null)
//				{
//					if (o1 != null)
//						continue;
//				}
//				else
//				{
//					if (o1 == null || !o1.getClass().equals(cls))
//						continue;
//				}
//
//				xs.add(newX);
//				ys.add(newY);
//				selectedTiles[newX][newY] = true;
//				deque.add(new Point(newX, newY));
//			}
//		}
//
//		this.undoActions.add(new EditorAction.ActionSelectTiles(this, true, xs, ys));
	}

	public void previewShape(double lowX, double highX, double lowY, double highY)
	{
		if (!selectHeld)
			return;

		Shape s = Shape.getShape(buildTool, lowX, highX, lowY, highY, selectX1 < selectX2, selectY1 < selectY2);
		for (int i = 0; i < s.size(); i++)
		{
			if (currentPlaceable == Placeable.enemyTank)
				mouseTank.drawOutlineAt((s.xs.get(i) + 0.5) * Game.tile_size, (s.ys.get(i) + 0.5) * Game.tile_size);
			else
				mouseObstacle.drawOutlineAt((s.xs.get(i) + 0.5) * Game.tile_size, (s.ys.get(i) + 0.5) * Game.tile_size);
		}
	}

	public boolean specialBuildTool()
	{
		return currentMode == EditorMode.build && buildTool != BuildTool.normal && currentPlaceable != Placeable.playerTank;
	}


	public void cloneSelectorProperties()
	{
		cloneSelectorProperties(true);
	}

	public void cloneSelectorProperties(boolean forward)
	{
		GameObject o = (currentPlaceable == Placeable.obstacle ? mouseObstacle : mouseTank);
		o.initSelectors(this);

		o.forAllSelectors(s ->
		{
			LevelEditorSelector<?> s1 = this.selectors.get(s.id);
			if (s1 != null)
			{
				if (forward)
					s.cloneProperties(s1);
				else
					s1.cloneProperties(s);
			}
			else if (s.modified())
				this.selectors.put(s.id, s);
		});
	}

	public void play()
	{
//		if (this.modified)
			this.save();

		this.replaceSpawns();

		Game.game.solidGrid = new boolean[Game.currentSizeX][Game.currentSizeY];
		Game.game.unbreakableGrid = new boolean[Game.currentSizeX][Game.currentSizeY];

		Game.currentLevel = new Level(Game.currentLevelString);
		Game.currentLevel.timed = level.timer > 0;
		Game.currentLevel.timer = level.timer;

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

			if (o instanceof ObstacleBeatBlock)
			{
				Game.currentLevel.synchronizeMusic = true;
				Game.currentLevel.beatBlocks |= (int) ((ObstacleBeatBlock) o).beatFrequency;
			}

			o.removed = false;
			if (o.replaceTiles)
				o.postOverride();
		}

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
			if (availablePlayerSpawns.isEmpty())
			{
				for (int j = 0; j < this.spawns.size(); j++)
                    availablePlayerSpawns.add(j);
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

		for (Movable m : Game.movables)
		{
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

		this.undoActions.add(new EditorAction.ActionSelectTiles(this, false, x, y));

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

		for (Obstacle o: Game.obstacles)
		{
			heightBlocksSelected = false;
			int i = (int) (o.posX / Game.tile_size);
			int j = (int) (o.posY / Game.tile_size);
			if (i >= 0 && i < Game.currentSizeX && j >= 0 && j < Game.currentSizeY && selectedTiles[i][j])
			{
				if (selectedTiles[i][j])
				{
					SelectorStackHeight s = (SelectorStackHeight) o.getSelector("stack_height");
					if (s != null)
					{
						heightBlocksSelected = true;
						break;
					}
				}
			}
		}
	}

	public void adjustSelectionHeight(int num)
	{
		this.undoActions.add(new EditorAction.ActionChangeHeight(this, num));
		for (Obstacle o : Game.obstacles)
		{
			int i = (int) (o.posX / Game.tile_size);
			int j = (int) (o.posY / Game.tile_size);

			if (i >= 0 && i < Game.currentSizeX && j >= 0 && j < Game.currentSizeY && selectedTiles[i][j])
			{
				SelectorStackHeight s = (SelectorStackHeight) o.getSelector("stack_height");
				if (s == null)
					continue;

				s.changeMeta(num);

				if (Game.enable3d)
				{
					Drawing.drawing.terrainRenderer.remove(o);
					Game.redrawObstacles.add(o);
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

		clipboard = new EditorClipboard();

		for (Obstacle o : Game.obstacles)
		{
			int x = (int) ((o.posX - 25) / 50);
			int y = (int) ((o.posY - 25) / 50);

			if (x >= 0 && y >= 0 && x < this.selectedTiles.length && y < this.selectedTiles[0].length && this.selectedTiles[x][y])
			{
				try
				{
					Obstacle n = o.getClass().getConstructor(String.class, double.class, double.class).newInstance(o.name, (int) (o.posX / 50 - 0.5), (int) (o.posY / 50 - 0.5));
					n.cloneAllSelectors(o);
					clipboard.add(n);

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
					clipboard.add(n);

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

		clipboard.updateParams();

		this.clearSelection();

		if (cut)
			undoActions.add(new EditorAction.ActionCut(tanks, obstacles, (EditorAction.ActionSelectTiles) this.undoActions.remove(this.undoActions.size() - 1)));

		if (!clipboard.isEmpty())
		{
			this.currentMode = EditorMode.paste;
		}

		clipboards[selectedNum] = clipboard;
	}

	public void refreshMouseObject()
	{
		if (this.currentPlaceable == Placeable.enemyTank)
		{
			Tank t;
			if (tankNum < Game.registryTank.tankEntries.size())
				t = Game.registryTank.getEntry(tankNum).getTank(mouseTank.posX, mouseTank.posY, mouseTank.angle);
			else
				t = this.level.customTanks.get(tankNum - Game.registryTank.tankEntries.size()).instantiate(mouseTank.name, mouseTank.posX, mouseTank.posY, mouseTank.angle);

			t.drawAge = mouseTank.drawAge;
			mouseTank = t;
			this.mouseTank.initSelectors(this);
			this.mouseTank.forAllSelectors(LevelEditorSelector::addShortcutButton);
		}
		else if (this.currentPlaceable == Placeable.obstacle)
		{
			this.mouseObstacle.forAllSelectors(s -> this.selectors.put(s.id, s));
			this.mouseObstacle = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
			this.mouseObstacle.initSelectors(this);
			this.mouseObstacle.forAllSelectors(s ->
			{
				LevelEditorSelector<?> s1 = this.selectors.get(s.id);
				s.gameObject = this.mouseObstacle;

				if (s1 == null)
					this.selectors.put(s.id, s);
				else
					s.cloneProperties(s1);
			});
			this.mouseObstacle.forAllSelectors(LevelEditorSelector::addShortcutButton);
		}
		else if (this.currentPlaceable == Placeable.playerTank)
		{
			this.mouseTank = new TankPlayer(0, 0, 0);
			this.mouseTank.initSelectors(this);
			this.mouseTank.forAllSelectors(LevelEditorSelector::addShortcutButton);
			((TankPlayer) mouseTank).setDefaultColor();
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

	public enum SymmetryType
	{none, flipHorizontal, flipVertical, flipBoth, flip8, rot180, rot90}

	@Override
	public void onAttemptClose()
	{
		paused = true;
		Game.screen = new OverlayConfirmSave(Game.screen, this);
	}

	@Override
	public void setupLights()
	{
		ScreenGame.setupGameLights();
	}
}
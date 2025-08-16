package tanks.gui.screen.leveleditor;

import basewindow.*;
import tanks.*;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.EditorButtons.EditorButton;
import tanks.gui.screen.leveleditor.selector.MetadataSelector;
import tanks.item.Item;
import tanks.network.event.INetworkEvent;
import tanks.obstacle.*;
import tanks.registry.*;
import tanks.tank.*;
import tanks.tankson.Serializer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@SuppressWarnings("unused")
public class ScreenLevelEditor extends Screen implements ILevelPreviewScreen
{
    public Placeable currentPlaceable = Placeable.enemyTank;
    public int tankNum = 0;
    public int obstacleNum = 0;

    public GameObject mousePlaceable = new TankDummy("dummy", 0, 0, 0);
    public HashMap<String, Object> currentMetadata = new HashMap<>();

    public EditorClipboard clipboard = new EditorClipboard();

    public ArrayList<EditorAction> undoActions = new ArrayList<>();
    public ArrayList<EditorAction> redoActions = new ArrayList<>();
    public int undoCount = 1;
    public int redoCount = 1;
    public int redoLength = -1;

    public int tankPage = 0;
    public int obstaclePage = 0;

    public Obstacle hoverObstacle = null;
    public EditorButtons buttons = new EditorButtons(this);

    public boolean stagger = false;
    public boolean oddStagger = false;
    public boolean paused = false;

    public boolean objectMenu = false;

    public double clickCooldown = 0;
    public ArrayList<Team> teams = new ArrayList<>();
    public ArrayList<TankSpawnMarker> spawns = new ArrayList<>();

    public Level level;
    public String name;

    public boolean movePlayer = true;

    public enum EditorMode
    {build, erase, camera, select, picker, paste}

    public EditorMode previousMode = EditorMode.build;
    public EditorMode currentMode = EditorMode.build;

    public boolean symmetrySelectMode = false;

    public enum BuildTool
    {normal, circle, rectangle, line}

    public BuildTool buildTool = BuildTool.normal;

    public enum SelectTool
    {normal, wand_contiguous, wand_discontiguous}

    public SelectTool selectTool = SelectTool.normal;

    public Shape prevSelectShape;
    public double selectX1, selectY1, selectX2, selectY2;

    public ArrayList<EditorButton> shortcutButtons = new ArrayList<>();
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

    public double fontBrightness = 0;

    public boolean modified = true;

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
                    setMode(EditorMode.build);
                else if (place.option != 0)
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
            new EditorButton("square.png", 40, 40, () ->
            {
                this.setMode(EditorMode.build);
                buildTool = BuildTool.rectangle;
            }, () -> buildTool == BuildTool.rectangle, "Square tool (%s)", Game.game.input.editorSquare),
            new EditorButton("circle.png", 40, 40, () ->
            {
                this.setMode(EditorMode.build);
                buildTool = BuildTool.circle;
            }, () -> buildTool == BuildTool.circle, "Circle tool (%s)", Game.game.input.editorCircle),
            new EditorButton("line.png", 40, 40, () ->
            {
                this.setMode(EditorMode.build);
                buildTool = BuildTool.line;
            }, () -> buildTool == BuildTool.line, "Line tool (%s)", Game.game.input.editorLine)
        )
        .onReset(() -> buildTool = BuildTool.normal);

    EditorButton erase = new EditorButton(buttons.topLeft, "eraser.png", 40, 40,
        () -> this.setMode(EditorMode.erase),
        () -> this.currentMode == EditorMode.erase,
        "Erase (%s)", Game.game.input.editorErase
    );

    EditorButton panZoom = new EditorButton(buttons.topLeft, "zoom_pan.png", 40, 40,
        () -> setMode(EditorMode.camera),
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
                    setMode(EditorMode.select);
                else if (select.option != 0)
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
        .addSubButtons(new EditorButton("wand.png", 40, 40, () ->
            {
                this.setMode(EditorMode.select);
                selectTool = SelectTool.wand_contiguous;
            }, () -> selectTool == SelectTool.wand_contiguous, "Contiguous wand tool (%s)", Game.game.input.editorWand),
            new EditorButton("wand_discontiguous.png", 40, 40, () ->
            {
                this.setMode(EditorMode.select);
                selectTool = SelectTool.wand_discontiguous;
            }, () -> selectTool == SelectTool.wand_discontiguous, "Non-contiguous wand tool (%s)", Game.game.input.editorWandDiscontiguous))
        .onReset(() -> selectTool = SelectTool.normal);

    EditorButton grab = new EditorButton(buttons.topLeft, "eyedropper.png", 50, 50,
        () -> this.setMode(EditorMode.picker),
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
        setMode(EditorMode.paste);
    }, () -> this.currentMode == EditorMode.paste || clipboard.isEmpty(), () -> !clipboard.isEmpty(), "Paste (%s)", Game.game.input.editorPaste
    );

    EditorButton flipHoriz = new EditorButton(buttons.bottomRight, "flip_horizontal.png", 50, 50, () -> clipboard.flipHorizontal(),
        () -> false, () -> this.currentMode == EditorMode.paste, "Flip horizontal (%s)", Game.game.input.editorFlipHoriz);
    EditorButton flipVert = new EditorButton(buttons.bottomRight, "flip_vertical.png", 50, 50, () -> clipboard.flipVertical(),
        () -> false, () -> this.currentMode == EditorMode.paste, "Flip vertical (%s)", Game.game.input.editorFlipVert);
    EditorButton rotate = new EditorButton(buttons.bottomRight, "rotate_obstacle.png", 50, 50, () -> clipboard.rotate(),
        () -> false, () -> this.currentMode == EditorMode.paste, "Rotate clockwise (%s)", Game.game.input.editorRotateClockwise);

    EditorButton selectSquareToggle = new EditorButton(buttons.bottomRight, "square_unlocked.png", 40, 40, () ->
        lockSquare = !lockSquare, () -> false, () -> this.currentMode == EditorMode.select, "", Game.game.input.editorLockSquare
    ).setDescription("Lock square selecting (Hold: %s, Toggle: %s)", Game.game.input.editorHoldSquare.getInputs(), Game.game.input.editorLockSquare.getInputs());

    EditorButton selectAddToggle = new EditorButton(buttons.bottomRight, "select_add.png", 40, 40, () ->
        lockAdd = !lockAdd, () -> false, () -> this.currentMode == EditorMode.select, "Toggle select/deselect (%s)", Game.game.input.editorSelectAddToggle
    );

    EditorButton selectClear = new EditorButton(buttons.bottomRight, "select_clear.png", 40, 40, this::clearSelection, () -> !selection, () -> this.currentMode == EditorMode.select || selection, "Clear selection (%s)", Game.game.input.editorDeselect);

    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[]) (new ArrayList[10]);

    public ScreenLevelEditor(String lvlName, Level level)
    {
        this.selfBatch = false;
        this.drawDarkness = false;

        this.music = "battle_editor.ogg";
        this.musicID = "editor";

        // todo
        this.allowClose = false;

        if (Game.game.window.touchscreen)
            controlsSizeMultiplier = 1.0;

        this.level = level;
        this.enableMargins = false;

        this.setMousePlaceable();

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<>();
        }

        Obstacle.draw_size = Game.tile_size;

        Game.game.window.validScrollDown = false;
        Game.game.window.validScrollUp = false;

        this.name = lvlName;
    }

    public void setMode(EditorMode mode)
    {
        if (previousMode == EditorMode.paste && clipboard.isEmpty())
            return;

        if (this.currentMode != mode)
            this.previousMode = this.currentMode;

        this.currentMode = mode;
        this.setMousePlaceable();
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

            for (Obstacle o : Game.obstacles)
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

        Tank t = Tank.findTank(mousePlaceable.posX, mousePlaceable.posY);

        if (t != null)
        {
            if (t.name.equals("player"))
            {
                this.currentPlaceable = Placeable.playerTank;
                this.setMousePlaceable();
                setMousePlaceableMetadata(t.getMetadata());

                ((TankPlayer) mousePlaceable).setDefaultColor();
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
                        this.setMousePlaceable();
                        setMousePlaceableMetadata(t.getMetadata());
                        return true;
                    }
                }

                for (int i = 0; i < this.level.customTanks.size(); i++)
                {
                    if (this.level.customTanks.get(i).name.equals(t.name))
                    {
                        tankNum = Game.registryTank.tankEntries.size() + i;
                        this.currentPlaceable = Placeable.enemyTank;
                        this.setMousePlaceable();
                        setMousePlaceableMetadata(t.getMetadata());
                        return true;
                    }
                }
            }
        }

        Obstacle o = Game.getObstacle(mousePlaceable.posX, mousePlaceable.posY);

        if (o == null)
            return false;

        int i = 0;
        for (RegistryObstacle.ObstacleEntry entry : Game.registryObstacle.obstacleEntries)
        {
            if (entry.obstacle.equals(o.getClass()))
                break;
            i++;
        }

        this.currentPlaceable = Placeable.obstacle;
        obstacleNum = i;
        this.setMousePlaceable();
        setMousePlaceableMetadata(o.getMetadata());
        return true;
    }

    public void setMousePlaceableMetadata(String meta)
    {
        mousePlaceable.setMetadata(meta);
        for (String s : mousePlaceable.getMetadataProperties().keySet())
        {
            currentMetadata.put(s, mousePlaceable.getMetadataProperty(s).getMetadata(mousePlaceable));
        }
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

        if (mousePlaceable instanceof Tank)
            ((Tank) mousePlaceable).angle = ((Tank) mousePlaceable).orientation;

//		allowClose = this.undoActions.isEmpty() && !modified;
        clickCooldown = Math.max(0, clickCooldown - Panel.frameFrequency);

        if (grab.keybind.isValid())
        {
            if (!this.grab())
                this.currentMode = EditorMode.picker;
            else
                this.currentMode = EditorMode.build;
        }

        if (!undoActions.isEmpty() && undo.keybind.isValid())
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

        if (!redoActions.isEmpty() && redo.keybind.isValid())
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

        this.buttons.bottomRight.removeAll(this.shortcutButtons);

        if (this.currentMode == EditorMode.build)
            this.buttons.bottomRight.addAll(this.shortcutButtons);

        if (Game.game.input.editorPaste.isValid() && !clipboard.isEmpty())
        {
            if (this.currentMode == EditorMode.paste)
                this.paste();
            else
                paste.function.run();

            Game.game.input.editorPaste.invalidate();
        }

        if (this.currentMode == EditorMode.camera && Game.game.input.editorCamera.isValid())
        {
            Game.game.input.editorCamera.invalidate();
            this.setMode(this.previousMode);
        }

        if (this.currentMode == EditorMode.erase && Game.game.input.editorErase.isValid())
        {
            Game.game.input.editorErase.invalidate();
            this.setMode(this.previousMode);
        }

        if (this.currentMode == EditorMode.camera && Game.game.input.editorCamera.isValid())
        {
            Game.game.input.editorCamera.invalidate();
            this.setMode(this.previousMode);
        }

        if (this.currentMode == EditorMode.build && Game.game.input.editorBuild.isValid() && place.option == 0)
        {
            Game.game.input.editorBuild.invalidate();
            this.setMode(this.previousMode);
        }

        if (this.currentMode == EditorMode.select && Game.game.input.editorSelect.isValid() && select.option == 0)
        {
            Game.game.input.editorSelect.invalidate();
            this.setMode(this.previousMode);
        }

        this.buttons.refreshButtons();
        this.buttons.update();

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
                this.setMousePlaceable();
            }
            else if (down && currentPlaceable == Placeable.obstacle)
            {
                obstacleNum = (obstacleNum + 1) % Game.registryObstacle.obstacleEntries.size();
                this.setMousePlaceable();
            }

            if (up && currentPlaceable == Placeable.enemyTank)
            {
                tankNum = ((tankNum - 1) + Game.registryTank.tankEntries.size() + this.level.customTanks.size()) % (Game.registryTank.tankEntries.size() + this.level.customTanks.size());
                this.setMousePlaceable();

            }
            else if (up && currentPlaceable == Placeable.obstacle)
            {
                obstacleNum = ((obstacleNum - 1) + Game.registryObstacle.obstacleEntries.size()) % Game.registryObstacle.obstacleEntries.size();
                this.setMousePlaceable();
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
                    this.setMousePlaceable();
                }
                else if (currentPlaceable == Placeable.obstacle)
                {
                    this.currentPlaceable = Placeable.playerTank;
                    this.setMousePlaceable();
                }
                else if (currentPlaceable == Placeable.playerTank)
                {
                    this.currentPlaceable = Placeable.enemyTank;
                    this.setMousePlaceable();
                }
            }

            if (left)
            {
                if (currentPlaceable == Placeable.playerTank)
                {
                    this.currentPlaceable = Placeable.obstacle;
                    this.setMousePlaceable();
                }
                else if (currentPlaceable == Placeable.obstacle)
                {
                    this.currentPlaceable = Placeable.enemyTank;
                    this.setMousePlaceable();
                }
                else if (currentPlaceable == Placeable.enemyTank)
                {
                    this.currentPlaceable = Placeable.playerTank;
                    this.setMousePlaceable();
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
                Game.game.input.editorUse.isPressed() && !Game.game.input.editorAction.isPressed(),
                Game.game.input.editorAction.isPressed(),
                Game.game.input.editorUse.isValid() && !Game.game.input.editorAction.isPressed(),
                Game.game.input.editorAction.isValid());

            if (Game.game.input.editorUse.isPressed() && Game.game.input.editorAction.isPressed())
                Game.game.input.editorAction.unpress();

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

        double px = Drawing.drawing.gameToInterfaceCoordsX(panCurrentX);
        double py = Drawing.drawing.gameToInterfaceCoordsY(panCurrentY);

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
            double zx = Drawing.drawing.gameToInterfaceCoordsX(zoomCurrentX);
            double zy = Drawing.drawing.gameToInterfaceCoordsY(zoomCurrentY);
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
        }

        if (Game.game.input.editorToggleControls.isValid())
        {
            this.showControls = !this.showControls;
            Game.game.input.editorToggleControls.invalidate();
        }

        if (Game.game.input.editorPaste.isValid() && this.currentMode != EditorMode.paste && !clipboard.isEmpty())
        {
            this.setMode(EditorMode.paste);
            Game.game.input.editorPaste.invalidate();
        }

        if (!redoActions.isEmpty() && redoLength != undoActions.size())
        {
            redoActions.clear();
            redoLength = -1;
        }

        ScreenGame.handleRemovals();
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
        mousePlaceable.posX = posX;
        mousePlaceable.posY = posY;

        if (currentMode == EditorMode.picker)
        {
            if (validLeft)
            {
                handled[0] = true;
                if (grab())
                {
                    Drawing.drawing.playVibration("heavyClick");
                    this.setMode(EditorMode.build);
                }
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
            magicSelect((int) (clampTileX(mousePlaceable.posX) / Game.tile_size), (int) (clampTileY(mousePlaceable.posY) / Game.tile_size), selectTool == SelectTool.wand_contiguous);

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
                selectX1 = clampTileX(mousePlaceable.posX);
                selectY1 = clampTileY(mousePlaceable.posY);
                selectHeld = true;
                handled[0] = true;
                handled[1] = true;

                Drawing.drawing.playVibration("selectionChanged");
            }

            if (pressed && selectHeld)
            {
                double prevSelectX2 = selectX2;
                double prevSelectY2 = selectY2;

                selectX2 = clampTileX(mousePlaceable.posX);
                selectY2 = clampTileY(mousePlaceable.posY);

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
                selectX1 = clampTileX(mousePlaceable.posX);
                selectY1 = clampTileY(mousePlaceable.posY);
                selectHeld = true;
                handled[0] = true;
                handled[1] = true;

                Drawing.drawing.playVibration("selectionChanged");
            }

            if (left && selectHeld)
            {
                double prevSelectX2 = selectX2;
                double prevSelectY2 = selectY2;

                selectX2 = clampTileX(mousePlaceable.posX);
                selectY2 = clampTileY(mousePlaceable.posY);

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
            int x = (int) (mousePlaceable.posX / Game.tile_size);
            int y = (int) (mousePlaceable.posY / Game.tile_size);

            if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY)
            {
                if (selectedTiles[x][y] && (validLeft || validRight) && !(currentPlaceable == Placeable.playerTank && this.movePlayer))
                {
                    double ox = mousePlaceable.posX;
                    double oy = mousePlaceable.posY;

                    ArrayList<EditorAction> actions = this.undoActions;
                    this.undoActions = new ArrayList<>();

                    for (int i = 0; i < selectedTiles.length; i++)
                    {
                        for (int j = 0; j < selectedTiles[i].length; j++)
                        {
                            if (selectedTiles[i][j])
                            {
                                mousePlaceable.posX = (i + 0.5) * Game.tile_size;
                                mousePlaceable.posY = (j + 0.5) * Game.tile_size;

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

                    mousePlaceable.posX = ox;
                    mousePlaceable.posY = oy;
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

            mousePlaceable.posX = (x + 0.5) * Game.tile_size;
            mousePlaceable.posY = (y + 0.5) * Game.tile_size;

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

        protected Shape()
        {
        }

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
            int width = hx - lx, length = hy - ly;
            boolean direction = xa;
            if (ya)
                direction = !direction;

            if (tool == BuildTool.rectangle || (tool == BuildTool.line && (width == 0 || length == 0)))
            {
                for (int i = 0; i < rectX.length; i += 2)
                {
                    for (int x = lx + width * rectX[i]; x <= lx + width * rectX[i + 1]; x++)
                    {
                        for (int y = ly + length * rectY[i]; y <= ly + length * rectY[i + 1]; y++)
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
                    int y = (int) ((double) length / width * (direction ? -1 : 1) * x + ly + (direction ? length : 0));
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
                if (selectedTiles[(int) (x / Game.tile_size)][(int) (y / Game.tile_size)] == selectInverted)
                {
                    px.add((int) (x / Game.tile_size));
                    py.add((int) (y / Game.tile_size));
                }

                selectedTiles[(int) (x / Game.tile_size)][(int) (y / Game.tile_size)] = !selectInverted;
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

            this.setMousePlaceable();
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

        double originalX = mousePlaceable.posX;
        double originalY = mousePlaceable.posY;
        double originalOrientation = mousePlaceable instanceof Tank ? ((Tank) mousePlaceable).orientation : 0;

        posX.add(mousePlaceable.posX);
        posY.add(mousePlaceable.posY);
        orientations.add(originalOrientation);

        if (mousePlaceable.posX >= symmetryX1 && mousePlaceable.posX <= symmetryX2 && mousePlaceable.posY >= symmetryY1 && mousePlaceable.posY <= symmetryY2)
            handleSymmetry(posX, posY, orientations);

        for (int oi = 0; oi < orientations.size(); oi++)
        {
            mousePlaceable.posX = posX.get(oi);
            mousePlaceable.posY = posY.get(oi);

            if (mousePlaceable instanceof Tank)
                ((Tank) mousePlaceable).orientation = orientations.get(oi);

            if (mousePlaceable.posX > 0 && mousePlaceable.posY > 0 && mousePlaceable.posX < Game.tile_size * Game.currentSizeX && mousePlaceable.posY < Game.tile_size * Game.currentSizeY)
            {
                if (validLeft && currentMode == EditorMode.paste && !paste)
                    return handlePaste(originalX, originalY, originalOrientation);

                if ((currentMode == EditorMode.build && right) || (currentMode == EditorMode.erase && (left || right)))
                    handleErase(handled, validLeft, validRight, batch);

                if (currentMode != EditorMode.erase && clickCooldown <= 0 && (validLeft || (currentMode != EditorMode.paste && left && currentPlaceable == Placeable.obstacle && this.mousePlaceable.draggable)))
                    handleBuild(handled, validRight, batch, paste);
            }
        }

        return handled;
    }

    protected void handleBuild(boolean[] handled, boolean validRight, boolean batch, boolean paste)
    {
        double mx = mousePlaceable.posX;
        double my = mousePlaceable.posY;

        if (currentPlaceable == Placeable.obstacle)
        {
            mx = mousePlaceable.posX;
            my = mousePlaceable.posY;
        }


        boolean skip = false;

        if (!(mousePlaceable instanceof Obstacle && !((Obstacle) mousePlaceable).tankCollision))
            skip = checkForMovable(mx, my);

        if (skip) return;
        skip = checkForObstacle(validRight, mx, my);

        handled[0] = true;
        handled[1] = true;

        if (skip) return;

        if (currentPlaceable == Placeable.enemyTank)
            placeEnemyTank(batch, paste);
        else if (currentPlaceable == Placeable.playerTank)
            placePlayerTank(batch, paste);
        else if (currentPlaceable == Placeable.obstacle)
            placeObstacle(batch, paste);
    }

    protected boolean checkForObstacle(boolean validRight, double mx, double my)
    {
        Chunk.Tile t = Chunk.getTile(mx, my);
        if (t == null)
            return false;

        Obstacle o = t.obstacle();

        if (validRight)
        {
            this.undoActions.add(new EditorAction.ActionObstacle(o, false));
            Game.removeObstacles.add(o);
            return false;
        }
        else
        {
            return !t.canPlaceOn(mousePlaceable);
        }
    }

    protected static boolean checkForMovable(double mx, double my)
    {
        return Movable.findMovable(mx, my) != null;
    }

    protected void placeObstacle(boolean batch, boolean paste)
    {
        Obstacle o = !paste ? Game.registryObstacle.getEntry(obstacleNum)
            .getObstacle(mousePlaceable.posX / Game.tile_size - 0.5, mousePlaceable.posY / Game.tile_size - 0.5)
            : (Obstacle) mousePlaceable;
        o.setMetadata(mousePlaceable.getMetadata());

        if (o instanceof ObstacleStackable)
        {
            boolean evenTile = ((int) (o.posX / Game.tile_size) + (int) (o.posY / Game.tile_size)) % 2 == 0;
            if (this.stagger && evenTile == !oddStagger && !paste)
                ((ObstacleStackable) o).stackHeight -= 0.5;
            o.refreshMetadata();
        }

        this.undoActions.add(new EditorAction.ActionObstacle(o, true));
        Game.addObstacle(o);

        if (!batch)
            Drawing.drawing.playVibration("click");
    }

    protected void placePlayerTank(boolean batch, boolean paste)
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

        TankSpawnMarker t = new TankSpawnMarker("player", mousePlaceable.posX, mousePlaceable.posY, 0);
        t.setMetadata(mousePlaceable.getMetadata());
        t.angle = t.orientation;

        this.spawns.add(t);

        if (this.movePlayer && !paste)
            this.undoActions.add(new EditorAction.ActionMovePlayer(this, spawnsClone, t));
        else
            this.undoActions.add(new EditorAction.ActionPlayerSpawn(this, t, true));

        Game.addMovable(t);

        if (!batch)
            Drawing.drawing.playVibration("click");

        if (this.movePlayer)
            t.drawAge = 50;
    }

    protected void placeEnemyTank(boolean batch, boolean paste)
    {
        Tank t;

        if (paste)
            t = (Tank) mousePlaceable;
        else
        {
            if (tankNum < Game.registryTank.tankEntries.size())
                t = Game.registryTank.getEntry(tankNum).getTank(mousePlaceable.posX, mousePlaceable.posY, ((Tank) mousePlaceable).angle);
            else
                t = ((TankAIControlled) mousePlaceable).instantiate(((TankAIControlled) mousePlaceable).name, mousePlaceable.posX, mousePlaceable.posY, ((TankAIControlled) mousePlaceable).angle);
        }

        t.setMetadata(mousePlaceable.getMetadata());

        this.undoActions.add(new EditorAction.ActionTank(t, true));
        Game.addMovable(t);

        if (!batch)
            Drawing.drawing.playVibration("click");
    }

    protected void handleErase(boolean[] handled, boolean validLeft, boolean validRight, boolean batch)
    {
        boolean skip = false;

        if (validRight || (currentMode == EditorMode.erase && validLeft))
        {
            for (Movable m : Game.movables)
            {
                if (m.posX == mousePlaceable.posX && m.posY == mousePlaceable.posY && m instanceof Tank && !(this.spawns.contains(m) && this.spawns.size() == 1))
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
                        createEraseEffect(m);

                    break;
                }
            }
        }

        for (int i = 0; i < Game.obstacles.size(); i++)
        {
            Obstacle m = Game.obstacles.get(i);
            if (m.posX == mousePlaceable.posX && m.posY == mousePlaceable.posY)
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
            int add = Game.game.window.shift ? -1 : 1;

            MetadataSelector s = mousePlaceable.getSecondaryMetadataProperty();
            if (s != null)
                s.changeMetadata(this, mousePlaceable, add);
        }

        if (Game.game.window.touchscreen)
            handled[0] = true;

        handled[1] = true;
    }

    protected static void createEraseEffect(Movable m)
    {
        Drawing.drawing.playVibration("click");

        for (int z = 0; z < 100 * Game.effectMultiplier; z++)
        {
            Effect e = Effect.createNewEffect(m.posX, m.posY, ((Tank) m).size / 2, Effect.EffectType.piece);
            double var = 50;
            e.colR = Math.min(255, Math.max(0, ((Tank) m).color.red + Math.random() * var - var / 2));
            e.colG = Math.min(255, Math.max(0, ((Tank) m).color.green + Math.random() * var - var / 2));
            e.colB = Math.min(255, Math.max(0, ((Tank) m).color.blue + Math.random() * var - var / 2));

            if (Game.enable3d)
                e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * 2);
            else
                e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2);

            e.maxAge /= 2;
            Game.effects.add(e);
        }
    }

    protected boolean[] handlePaste(double originalX, double originalY, double originalOrientation)
    {
        paste();

        mousePlaceable.posX = originalX;
        mousePlaceable.posY = originalY;

        if (mousePlaceable instanceof Tank)
            ((Tank) mousePlaceable).orientation = originalOrientation;

        return new boolean[]{true, true};
    }

    protected void handleSymmetry(ArrayList<Double> posX, ArrayList<Double> posY, ArrayList<Double> orientations)
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

        level.append(this.level.sizeX).append(",").append(this.level.sizeY).append(",").append(this.level.color.red).append(",").append(this.level.color.green).append(",").append(this.level.color.blue).append(",").append(this.level.colorVar.red).append(",").append(this.level.colorVar.green).append(",").append(this.level.colorVar.blue)
            .append(",").append((int) (this.level.timer / 100)).append(",").append((int) Math.round(this.level.light * 100)).append(",").append((int) Math.round(this.level.shadow * 100)).append("|");

        ArrayList<Obstacle> unmarked = (ArrayList<Obstacle>) Game.obstacles.clone();
        String[][][] obstacles = new String[Game.registryObstacle.obstacleEntries.size()][this.level.sizeX][this.level.sizeY];

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

                            if (!stack.isEmpty())
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

                            if (!stack.isEmpty())
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

        for (Obstacle obstacle : unmarked)
        {
            level.append((int) (obstacle.posX / Game.tile_size)).append("-").append((int) (obstacle.posY / Game.tile_size));
            level.append("-").append(obstacle.name);

            if (obstacle instanceof ObstacleUnknown && ((ObstacleUnknown) obstacle).metadata != null)
                level.append("-").append(((ObstacleUnknown) obstacle).metadata);
            else
            {
                String meta = obstacle.getMetadata();
                if (!meta.isEmpty())
                    level.append("-").append(meta);
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

        if (Game.movables.isEmpty())
            level.append("|");

        level = new StringBuilder(level.substring(0, level.length() - 1));

        level.append("|");

        for (Team t : teams)
        {
            level.append(t.name).append("-").append(t.friendlyFire);
            if (t.enableColor)
                level.append("-").append(t.teamColor.red).append("-").append(t.teamColor.green).append("-").append(t.teamColor.blue);

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

        if (!this.level.playerBuilds.isEmpty() && !(this.level.playerBuilds.size() == 1 && Serializer.equivalent(this.level.playerBuilds.get(0), new TankPlayer.ShopTankBuild())))
        {
            level.append("\nbuilds");

            for (Tank t : this.level.playerBuilds)
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
            else
            {
                try
                {
                    Level oldLevel = new Level(new String(Files.readAllBytes(Paths.get(file.path))));
                    if (oldLevel.stripFormatting().equals(Game.currentLevelString))
                        return;
                }
                catch (IOException e)
                {
                    Game.exitToCrash(e);
                }
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

        GameObject prevMousePlaceable = mousePlaceable;
        Placeable placeable = currentPlaceable;

        double mx = prevMousePlaceable.posX;
        double my = prevMousePlaceable.posY;

        int prevSize = Game.movables.size() + Game.obstacles.size();

        for (Obstacle o : clipboard.obstacles)
        {
            currentPlaceable = Placeable.obstacle;

            try
            {
                Obstacle n = o.getClass().getConstructor(String.class, double.class, double.class).newInstance(o.name, (o.posX + mx) / Game.tile_size - 0.5, (o.posY + my) / Game.tile_size - 0.5);
                n.setMetadata(o.getMetadata());
                mousePlaceable = n;

                handlePlace(handled, true, false, true, false, true, true);
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }

        mousePlaceable = prevMousePlaceable;        // this line is important! debugging this took at least 1.5 hrs

        for (Tank t : clipboard.tanks)
        {
            currentPlaceable = Placeable.enemyTank;

            try
            {
                Tank n;

                if (t.getClass().equals(TankAIControlled.class))
                {
                    n = new TankAIControlled(t.name, t.posX + mx, t.posY + my, t.size, t.color.red, t.color.green, t.color.blue, t.angle, ((TankAIControlled) t).shootAIType);
                    ((TankAIControlled) t).cloneProperties((TankAIControlled) n);
                }
                else
                    n = t.getClass().getConstructor(String.class, double.class, double.class, double.class).newInstance(t.name, t.posX + mx, t.posY + my, t.angle);

                n.team = t.team;
                n.destroy = t.destroy;
                mousePlaceable = n;

                if (n instanceof TankSpawnMarker || n instanceof TankPlayer)
                    currentPlaceable = Placeable.playerTank;

                handlePlace(handled, true, false, true, false, true, true);
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        }

        prevMousePlaceable.posX = mx;
        prevMousePlaceable.posY = my;

        currentPlaceable = placeable;
        mousePlaceable = prevMousePlaceable;

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
            Drawing.drawing.setColor(174, 92, 16);

            double mul = 1;
            if (Game.angledView)
                mul = 2;

            Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
                mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);
        }

        this.drawDefaultBackground();

        for (Movable m : Game.movables)
            drawables[m.drawLevel].add(m);

        for (Obstacle o : Game.obstacles)
        {
            if (!o.batchDraw || !Game.enable3d)
                drawables[o.drawLevel].add(o);
        }

        for (Effect e : Game.effects)
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

            for (IDrawable d : this.drawables[i])
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

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255, 64);
        else
            Drawing.drawing.setColor(0, 0, 0, 64);

        Drawing.drawing.drawImage("level_center.png", Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, 50, 50);


        if (!paused && !Game.game.window.touchscreen)
        {
            if (currentMode == EditorMode.erase)
            {
                Drawing.drawing.setColor(255, 0, 0, 64, 0.3);

                if (Game.enable3d)
                {
                    if (hoverObstacle == null)
                        Drawing.drawing.fillBox(mousePlaceable.posX, mousePlaceable.posY, 0, Game.tile_size, Game.tile_size, Game.tile_size, (byte) 64);
                    else
                        hoverObstacle.draw3dOutline(255, 0, 0, 64);
                }
                else
                    Drawing.drawing.fillRect(mousePlaceable.posX, mousePlaceable.posY, Game.tile_size, Game.tile_size);
            }
            else if (currentMode == EditorMode.paste)
            {
                Drawing.drawing.setColor(255, 255, 255, 127, 0.3);
                Drawing.drawing.drawImage("icons/paste.png", mousePlaceable.posX, mousePlaceable.posY, Game.tile_size, Game.tile_size);

                for (Obstacle o : clipboard.obstacles)
                {
                    Drawing.drawing.setColor(o.colorR, o.colorG, o.colorB, 64, 0.5);

                    Drawing.drawing.fillRect(o.posX + mousePlaceable.posX, o.posY + mousePlaceable.posY, /*0,*/ Game.tile_size, Game.tile_size/*, ((Obstacle) o).stackHeight * Game.tile_size, (byte) 64*/);
                }

                for (Tank t : clipboard.tanks)
                    t.drawOutlineAt(t.posX + mousePlaceable.posX, t.posY + mousePlaceable.posY);
            }
            else if (currentMode == EditorMode.picker)
            {
                Drawing.drawing.setColor(0, 0, 0, 127);
                Drawing.drawing.drawRect(mousePlaceable.posX, mousePlaceable.posY, Game.tile_size, Game.tile_size, 10, 0);
                Drawing.drawing.setColor(255, 255, 255, 255, 1);
                Drawing.drawing.drawImage("icons/eyedropper.png", mousePlaceable.posX - Game.tile_size / 2, mousePlaceable.posY - Game.tile_size / 2, Game.tile_size, Game.tile_size);
            }
            else if ((currentPlaceable == Placeable.enemyTank || currentPlaceable == Placeable.playerTank) && currentMode == EditorMode.build)
            {
                ((Tank) mousePlaceable).drawOutline();
                ((Tank) mousePlaceable).drawTeam();

                if (currentPlaceable == Placeable.playerTank && !this.movePlayer)
                {
                    Drawing.drawing.setColor(0, 200, 255, 127);

                    if (!Game.enable3d)
                        Drawing.drawing.drawImage("emblems/player_spawn.png", mousePlaceable.posX, mousePlaceable.posY, ((Tank) mousePlaceable).size * 0.7, ((Tank) mousePlaceable).size * 0.7);
                    else
                        Drawing.drawing.drawImage("emblems/player_spawn.png", mousePlaceable.posX, mousePlaceable.posY, Game.tile_size * 0.82, ((Tank) mousePlaceable).size * 0.5, ((Tank) mousePlaceable).size * 0.5);
                }
            }
            else if (currentPlaceable == Placeable.obstacle && currentMode == EditorMode.build)
            {
                int x = (int) (mousePlaceable.posX / Game.tile_size);
                int y = (int) (mousePlaceable.posY / Game.tile_size);

                if (x >= 0 && x < Game.currentSizeX && y >= 0 && y < Game.currentSizeY /*&&
						(Game.getObstacle(x, y) == null || (Game.getSurfaceObstacle(x, y) == null &&
								Game.getObstacle(x, y).isSurfaceTile != mousePlaceable.isSurfaceTile && !mousePlaceable.tankCollision))*/)
                {
                    Obstacle mouseObstacle = (Obstacle) mousePlaceable;
                    if (Game.enable3d)
                    {
                        if (Game.isOrdered(-1, x, Game.currentSizeX) && Game.isOrdered(-1, y, Game.currentSizeY) /*&&
								(Game.getObstacle(x, y) == null || !Game.isOrdered(Game.getObstacle(x, y).startHeight, mousePlaceable.stackHeight + mousePlaceableStartHeight, Game.getObstacle(x, y).stackHeight))*/)
                            mouseObstacle.draw3dOutline(mouseObstacle.colorR, mouseObstacle.colorG, mouseObstacle.colorB, 100);
                    }

                    mouseObstacle.drawOutline();

                    if (mouseObstacle.getPrimaryMetadataProperty() != null)
                    {
                        Drawing.drawing.setFontSize(16);
                        Drawing.drawing.setColor(mouseObstacle.colorR / 2, mouseObstacle.colorG / 2, mouseObstacle.colorB / 2, 255);
                        Drawing.drawing.drawText(mousePlaceable.posX, mousePlaceable.posY, mouseObstacle.getPrimaryMetadataProperty().getMetadataDisplayString(mousePlaceable));
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
        MetadataSelector m = mousePlaceable.getPrimaryMetadataProperty();

        if (m != null)
            m.changeMetadata(this, mousePlaceable, add);
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
                    Drawing.drawing.fillRect(mousePlaceable.posX, mousePlaceable.posY, Game.tile_size, Game.tile_size);
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
        Obstacle selected = Game.getObstacle(x, y);
        String obstacleName = selected != null ? selected.name : null;

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

        if (!xPos.isEmpty())
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
                ((Tank) mousePlaceable).drawOutlineAt((s.xs.get(i) + 0.5) * Game.tile_size, (s.ys.get(i) + 0.5) * Game.tile_size);
            else
                ((Obstacle) mousePlaceable).drawOutlineAt((s.xs.get(i) + 0.5) * Game.tile_size, (s.ys.get(i) + 0.5) * Game.tile_size);
        }
    }

    public boolean specialBuildTool()
    {
        return currentMode == EditorMode.build && buildTool != BuildTool.normal && currentPlaceable != Placeable.playerTank;
    }

    public void play()
    {
        this.save();
        this.replaceSpawns();
        Game.currentLevel.reloadTiles();

        Game.currentLevel = new Level(Game.currentLevelString);
        Game.currentLevel.tilesRandomSeed = level.tilesRandomSeed;
        Game.currentLevel.timed = level.timer > 0;
        Game.currentLevel.timer = level.timer;

        for (Obstacle o : Game.obstacles)
        {
            if (o instanceof ObstacleBeatBlock)
            {
                Game.currentLevel.synchronizeMusic = true;
                Game.currentLevel.beatBlocks |= (int) ((ObstacleBeatBlock) o).beatFrequency;
            }
        }

        Game.resetNetworkIDs();
        for (Movable m : Game.movables)
        {
            if (m instanceof Tank)
                ((Tank) m).registerNetworkID();
        }

        Game.screen = new ScreenGame(this.name);
        Game.player.hotbar.coins = this.level.startingCoins;

        Game.currentLevel.playerBuilds.get(0).clonePropertiesTo(Game.playerTank);
        Game.player.buildName = Game.currentLevel.playerBuilds.get(0).buildName;
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
                Game.addMovable(tank);
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

        for (Obstacle o : Game.obstacles)
        {
            heightBlocksSelected = false;
            int i = (int) (o.posX / Game.tile_size);
            int j = (int) (o.posY / Game.tile_size);
            if (i >= 0 && i < Game.currentSizeX && j >= 0 && j < Game.currentSizeY && selectedTiles[i][j] &&
                o.getMetadataProperty("stack_height") != null)
            {
                heightBlocksSelected = true;
                break;
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
                MetadataSelector s = o.getMetadataProperty("stack_height");
                if (s == null)
                    continue;

                s.changeMetadata(this, o, num);

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
                    n.setMetadata(o.getMetadata());
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
                        n = new TankAIControlled(((TankAIControlled) t).name, t.posX, t.posY, ((TankAIControlled) t).size, ((TankAIControlled) t).color.red, ((TankAIControlled) t).color.green, ((TankAIControlled) t).color.blue, ((TankAIControlled) t).angle, ((TankAIControlled) t).shootAIType);
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
            this.setMode(EditorMode.paste);
        }
    }

    public void createShortcutButton(MetadataSelector s)
    {
        InputBindingGroup i = Game.game.inputBindings.get(s.metadataProperty.keybind());
        EditorButton e = new EditorButton(shortcutButtons, s.metadataProperty.image(), 50, 50, () ->
        {
            this.paused = true;
            this.objectMenu = true;
            s.openEditorOverlay(this);
        }, "", i);
        e.setDescription(s.metadataProperty.name() + " (%s)", i.getInputs());
    }

    public void setMousePlaceable()
    {
        double prevMousePlaceableX = mousePlaceable.posX;
        double prevMousePlaceableY = mousePlaceable.posY;

        if (this.currentPlaceable == Placeable.enemyTank)
        {
            Tank t;
            if (tankNum < Game.registryTank.tankEntries.size())
                t = Game.registryTank.getEntry(tankNum).getTank(mousePlaceable.posX, mousePlaceable.posY, 0);
            else
            {
                TankAIControlled t1 = this.level.customTanks.get(tankNum - Game.registryTank.tankEntries.size());
                t = t1.instantiate(t1.name, mousePlaceable.posX, mousePlaceable.posY, t1.angle);
            }

            t.drawAge = 100;
            mousePlaceable = t;
        }
        else if (this.currentPlaceable == Placeable.obstacle)
        {
            this.mousePlaceable = Game.registryObstacle.getEntry(obstacleNum).getObstacle(0, 0);
        }
        else if (this.currentPlaceable == Placeable.playerTank)
        {
            this.mousePlaceable = new TankPlayer(0, 0, 0).setDefaultColor();
        }

        this.buttons.bottomRight.removeAll(this.shortcutButtons);
        this.shortcutButtons.clear();

        MetadataSelector primary = mousePlaceable.getPrimaryMetadataProperty();
        MetadataSelector secondary = mousePlaceable.getSecondaryMetadataProperty();

        if (primary != null)
            createShortcutButton(primary);

        if (secondary != null)
            createShortcutButton(secondary);

        for (MetadataSelector a : mousePlaceable.getMetadataProperties().values())
        {
            if (a != null)
            {
                if (currentMetadata.get(a.id) != null)
                    a.setMetadata(null, this.mousePlaceable, currentMetadata.get(a.id));

                if (!a.id.equals(mousePlaceable.primaryMetadataID) && !a.id.equals(mousePlaceable.secondaryMetadataID))
                    createShortcutButton(a);
            }
        }

        if (mousePlaceable instanceof Tank)
            ((Tank) mousePlaceable).angle = ((Tank) mousePlaceable).orientation;

        if (this.currentMode == EditorMode.build)
            this.buttons.bottomRight.addAll(this.shortcutButtons);

        this.buttons.refreshButtons();

        this.mousePlaceable.refreshMetadata();
        mousePlaceable.posX = prevMousePlaceableX;
        mousePlaceable.posY = prevMousePlaceableY;
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

    public enum Placeable
    {enemyTank, playerTank, obstacle}

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

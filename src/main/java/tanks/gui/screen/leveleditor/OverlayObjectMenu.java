package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.GameObject;
import tanks.Movable;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.*;
import tanks.gui.screen.leveleditor.selector.MetadataSelector;
import tanks.obstacle.Obstacle;
import tanks.tank.*;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;

public class OverlayObjectMenu extends ScreenLevelEditorOverlay implements ITankScreen, IRenamableScreen
{
    public Button primaryMetadataButton;
    public Button secondaryMetadataButton;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public boolean refreshButtons = false;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public ArrayList<Button> obstacleButtons = new ArrayList<>();

    public Runnable drawEditTank = () -> this.editTank.draw();

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> editor.tankPage++);

    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> editor.tankPage--);

    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> editor.tankPage = 0);

    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> editor.tankPage = (tankButtons.size() - 1) / objectButtonRows / objectButtonCols);

    public Button nextObstaclePage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> editor.obstaclePage++);

    public Button previousObstaclePage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> editor.obstaclePage--);

    public Button firstObstaclePage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> editor.obstaclePage = 0);

    public Button lastObstaclePage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> editor.obstaclePage = (obstacleButtons.size() - 1) / objectButtonRows / objectButtonCols);


    public Button exitObjectMenu = new Button(this.centerX, this.centerY + 240, 350, 40, "Ok", () ->
    {
        Game.screen = editor;
        editor.paused = false;
        editor.clickCooldown = 20;
    }
    );

    public Button playerItems = new Button(this.centerX - this.objXSpace / 2, this.centerY + 120, 350, 40, "Player items", () -> Game.screen = new OverlayPlayerItems(Game.screen, editor));

    public Button playerBuilds = new Button(this.centerX + this.objXSpace / 2, this.centerY + 120, 350, 40, "Player builds", () -> Game.screen = new ScreenEditorPlayerTankBuild(new ArrayListIndexPointer<>(editor.playerTankBuilds, 0), editor));

    public Button placePlayer = new Button(this.centerX - 380, this.centerY - 180, 350, 40, "Player", () ->
    {
        this.editor.currentPlaceable = ScreenLevelEditor.Placeable.playerTank;
        this.editor.setMousePlaceable();
        this.loadButtons();
    }
    );

    public Button placeEnemy = new Button(this.centerX, this.centerY - 180, 350, 40, "Tank", () ->
    {
        this.editor.currentPlaceable = ScreenLevelEditor.Placeable.enemyTank;
        this.editor.setMousePlaceable();
        this.loadButtons();
    }
    );

    public Button placeObstacle = new Button(this.centerX + 380, this.centerY - 180, 350, 40, "Block", () ->
    {
        this.editor.currentPlaceable = ScreenLevelEditor.Placeable.obstacle;
        this.editor.setMousePlaceable();
        this.loadButtons();
    });

    public Button editTank = new Button(0, 0, 40, 40, "", () ->
    {
        TankAIControlled t = editor.level.customTanks.get(editor.tankNum - Game.registryTank.tankEntries.size());
        Pointer<TankAIControlled> p = new ArrayListIndexPointer<>(editor.level.customTanks, editor.tankNum - Game.registryTank.tankEntries.size());
        ScreenEditorTank s = new ScreenEditorTank(p, this);
        s.onComplete = () ->
        {
            if (p.get() == null)
                this.removeTank(t);

            this.refreshTanks(t);
        };
        Game.screen = s;
        editor.modified = true;
    }, "Edit custom tank");

    public Button sort = new Button(this.centerX + this.objXSpace * 1.5 - 40, this.centerY + this.objYSpace * 3, 40, 40, "", () ->
    {
        editor.level.customTanks.sort(this::compareTo);
        Game.screen = new OverlayObjectMenu(previous, editor);
    }, "Sort tanks (irreversible)");

    public ButtonObject movePlayerButton;
    public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), this.centerX + 50, this.centerY, 75, 75, () -> editor.movePlayer = false, "Add multiple player spawn points");

    public OverlayObjectMenu(Screen previous, ScreenLevelEditor editor)
    {
        super(previous, editor);

        this.musicInstruments = true;

        TankPlayer tp = new TankPlayer(0, 0, 0);
        tp.setDefaultColor();
        movePlayerButton = new ButtonObject(tp, this.centerX - 50, this.centerY, 75, 75, () -> editor.movePlayer = true, "Move the player");

        sort.imageSizeX = 25;
        sort.imageSizeY = 25;
        sort.image = "icons/sort_alphabetical.png";
        sort.fullInfo = true;

        this.loadButtons();

        playerItems.imageXOffset = -155;
        playerItems.imageSizeX = 30;
        playerItems.imageSizeY = 30;
        playerItems.image = "item.png";

        int count = Game.registryTank.tankEntries.size() + this.editor.level.customTanks.size();
        int rows = objectButtonRows;
        int cols = objectButtonCols;

        for (int i = 0; i <= count; i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Tank t;

            if (i < Game.registryTank.tankEntries.size())
            {
                t = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);
            }
            else if (i == count)
            {
                Button b = new Button(x, y, 50, 50, "+", () ->
                {
                    ScreenAddSavedTank s = new ScreenAddSavedTank(this);
                    s.drawBehindScreen = true;
                    editor.modified = true;
                    Game.screen = s;
                }, "Create a new custom tank!");
                this.tankButtons.add(b);
                b.fullInfo = true;

                continue;
            }
            else
                t = this.editor.level.customTanks.get(i - Game.registryTank.tankEntries.size());

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                editor.tankNum = j;
                editor.setMousePlaceable();
                this.loadButtons();
            }
                    , t.description);

            if (t.description.isEmpty())
                b.enableHover = false;

            this.tankButtons.add(b);
        }

        for (int i = 0; i < Game.registryObstacle.obstacleEntries.size(); i++)
        {
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Obstacle o = Game.registryObstacle.obstacleEntries.get(i).getObstacle(x, y);
            ButtonObject b = new ButtonObject(o, x, y, 75, 75, () ->
            {
                editor.obstacleNum = j;
                editor.setMousePlaceable();
                this.loadButtons();
            }
                    , o.description);

            if (o.description.isEmpty())
                b.enableHover = false;

            this.obstacleButtons.add(b);
        }

        this.nextObstaclePage.image = "icons/forward.png";
        this.nextObstaclePage.imageSizeX = 25;
        this.nextObstaclePage.imageSizeY = 25;
        this.nextObstaclePage.imageXOffset = 145;

        this.previousObstaclePage.image = "icons/back.png";
        this.previousObstaclePage.imageSizeX = 25;
        this.previousObstaclePage.imageSizeY = 25;
        this.previousObstaclePage.imageXOffset = -145;

        this.nextTankPage.image = "icons/forward.png";
        this.nextTankPage.imageSizeX = 25;
        this.nextTankPage.imageSizeY = 25;
        this.nextTankPage.imageXOffset = 145;

        this.previousTankPage.image = "icons/back.png";
        this.previousTankPage.imageSizeX = 25;
        this.previousTankPage.imageSizeY = 25;
        this.previousTankPage.imageXOffset = -145;

        this.lastObstaclePage.image = "icons/last.png";
        this.lastObstaclePage.imageSizeX = 20;
        this.lastObstaclePage.imageSizeY = 20;
        this.lastObstaclePage.imageXOffset = 0;

        this.firstObstaclePage.image = "icons/first.png";
        this.firstObstaclePage.imageSizeX = 20;
        this.firstObstaclePage.imageSizeY = 20;
        this.firstObstaclePage.imageXOffset = 0;

        this.lastTankPage.image = "icons/last.png";
        this.lastTankPage.imageSizeX = 20;
        this.lastTankPage.imageSizeY = 20;
        this.lastTankPage.imageXOffset = 0;

        this.firstTankPage.image = "icons/first.png";
        this.firstTankPage.imageSizeX = 20;
        this.firstTankPage.imageSizeY = 20;
        this.firstTankPage.imageXOffset = 0;

        this.editTank.image = "icons/pencil.png";
        this.editTank.imageSizeX = 25;
        this.editTank.imageSizeY = 25;
        this.editTank.fullInfo = true;
    }

    public void loadButtons()
    {
        this.primaryMetadataButton = null;
        this.secondaryMetadataButton = null;

        MetadataSelector s1 = editor.mousePlaceable.getPrimaryMetadataProperty();
        MetadataSelector s2 = editor.mousePlaceable.getSecondaryMetadataProperty();

        if (s1 != null)
        {
            this.primaryMetadataButton = new Button(this.exitObjectMenu.posX - this.objXSpace, this.exitObjectMenu.posY, this.objWidth, this.objHeight, "", () ->
            {
                this.refreshButtons = true;
                s1.openEditorOverlay(editor);
            });

            if (!s1.metadataProperty.image().isEmpty())
            {
                this.primaryMetadataButton.image = "icons/" + s1.metadataProperty.image();
                this.primaryMetadataButton.imageXOffset = -this.objWidth / 2 + objHeight / 2;
                this.primaryMetadataButton.imageSizeX = this.objHeight * 0.8;
                this.primaryMetadataButton.imageSizeY = this.objHeight * 0.8;
                this.primaryMetadataButton.setText("%s: %s", Translation.translate(s1.metadataProperty.name()), s1.getMetadataDisplayString(editor.mousePlaceable));
            }
        }

        if (s2 != null)
        {
            this.secondaryMetadataButton = new Button(this.exitObjectMenu.posX + this.objXSpace, this.exitObjectMenu.posY, this.objWidth, this.objHeight, "", () ->
            {
                this.refreshButtons = true;
                s2.openEditorOverlay(editor);
            });

            this.secondaryMetadataButton.setText("%s: %s", Translation.translate(s2.metadataProperty.name()), s2.getMetadataDisplayString(editor.mousePlaceable));

            if (!s2.metadataProperty.image().isEmpty())
            {
                this.secondaryMetadataButton.image = "icons/" + s2.metadataProperty.image();
                this.secondaryMetadataButton.imageXOffset = -this.objWidth / 2 + objHeight / 2;
                this.secondaryMetadataButton.imageSizeX = this.objHeight * 0.8;
                this.secondaryMetadataButton.imageSizeY = this.objHeight * 0.8;
            }
        }
    }

    public void update()
    {
        if (this.refreshButtons)
            this.loadButtons();

        this.refreshButtons = false;

        this.placePlayer.enabled = (this.editor.currentPlaceable != ScreenLevelEditor.Placeable.playerTank);
        this.placeEnemy.enabled = (this.editor.currentPlaceable != ScreenLevelEditor.Placeable.enemyTank);
        this.placeObstacle.enabled = (this.editor.currentPlaceable != ScreenLevelEditor.Placeable.obstacle);

        this.exitObjectMenu.update();

        this.placePlayer.update();
        this.placeEnemy.update();
        this.placeObstacle.update();

        if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.enabled = editor.movePlayer;
            this.movePlayerButton.enabled = !editor.movePlayer;

            this.playerSpawnsButton.update();
            this.movePlayerButton.update();
            this.playerItems.update();

            this.playerBuilds.enabled = false;
            this.playerBuilds.update();
        }
        else if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            for (int i = 0; i < this.tankButtons.size(); i++)
            {
                Button b = this.tankButtons.get(i);
                b.enabled = editor.tankNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.tankPage)
                    b.update();
            }

            nextTankPage.enabled = (this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > editor.tankPage;
            previousTankPage.enabled = editor.tankPage > 0;

            if (this.tankButtons.size() > this.objectButtonRows * this.objectButtonCols)
            {
                nextTankPage.update();
                previousTankPage.update();

                if ((tankButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
                {
                    lastTankPage.update();
                    firstTankPage.update();
                }
            }
        }
        else if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            for (int i = 0; i < this.obstacleButtons.size(); i++)
            {
                this.obstacleButtons.get(i).enabled = editor.obstacleNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.obstaclePage)
                    this.obstacleButtons.get(i).update();
            }

            this.nextObstaclePage.enabled = ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > editor.obstaclePage);
            this.previousObstaclePage.enabled = (editor.obstaclePage > 0);
            this.lastObstaclePage.enabled = this.nextObstaclePage.enabled;
            this.firstObstaclePage.enabled = this.previousObstaclePage.enabled;

            if ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > editor.obstaclePage)
                nextObstaclePage.update();

            if (editor.obstaclePage > 0)
                previousObstaclePage.update();
        }

        if (primaryMetadataButton != null)
            primaryMetadataButton.update();

        if (secondaryMetadataButton != null)
            secondaryMetadataButton.update();

        if (editor.tankNum >= Game.registryTank.tankEntries.size())
        {
            Button b = this.tankButtons.get(editor.tankNum);
            this.editTank.posX = b.posX + 35;
            this.editTank.posY = b.posY + 35;
            this.editTank.update();
        }

        HashMap<String, MetadataSelector> h = editor.mousePlaceable.getMetadataProperties();
        for (MetadataSelector s: h.values())
        {
            InputBindingGroup i = Game.game.inputBindings.get(s.metadataProperty.keybind());
            if (i != null && i.isValid())
            {
                i.invalidate();
                s.openEditorOverlay(editor);
            }
        }

        super.update();
    }

    public void draw()
    {
        super.draw();

        if (Game.screen != this)
            return;

        if (editor.tankNum >= Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size())
            editor.tankNum = Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size() - 1;

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 240, "Object menu");

        this.placePlayer.draw();
        this.placeEnemy.draw();
        this.placeObstacle.draw();

        this.exitObjectMenu.draw();

        if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.draw();
            this.movePlayerButton.draw();
            this.playerItems.draw();
            this.playerBuilds.draw();

            if (this.editor.movePlayer)
                this.drawMobileTooltip(this.movePlayerButton.hoverTextRawTranslated);
            else
                this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRawTranslated);

        }
        else if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            if (this.tankButtons.size() > this.objectButtonRows * this.objectButtonCols)
            {
                nextTankPage.draw();
                previousTankPage.draw();

                if ((tankButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
                {
                    lastTankPage.draw();
                    firstTankPage.draw();
                }

                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, nextTankPage.posY,
                        Translation.translate("Page %d of %d", (editor.tankPage + 1), (tankButtons.size() / (objectButtonCols * objectButtonRows) + Math.min(1, tankButtons.size() % (objectButtonCols * objectButtonRows)))));

            }

            for (int i = tankButtons.size() - 1; i >= 0; i--)
            {
                if (i / (objectButtonCols * objectButtonRows) == editor.tankPage)
                {
                    if (editor.tankNum >= Game.registryTank.tankEntries.size() && !tankButtons.get(i).enabled && tankButtons.get(i) instanceof ButtonObject)
                    {
                        if (this.editTank.selected)
                            ((ButtonObject) tankButtons.get(i)).tempDisableHover = true;

                        ((ButtonObject) tankButtons.get(i)).drawBeforeTooltip = drawEditTank;
                    }

                    tankButtons.get(i).draw();
                }
            }

            this.drawMobileTooltip(this.tankButtons.get(editor.tankNum).hoverTextRawTranslated);
        }
        else if (this.editor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            if (nextObstaclePage.enabled || previousObstaclePage.enabled)
            {
                nextObstaclePage.draw();
                previousObstaclePage.draw();

                if ((obstacleButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
                {
                    lastObstaclePage.draw();
                    firstObstaclePage.draw();
                }

                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, nextTankPage.posY,
                        Translation.translate("Page %d of %d", (editor.obstaclePage + 1), (obstacleButtons.size() / (objectButtonCols * objectButtonRows) + Math.min(1, obstacleButtons.size() % (objectButtonCols * objectButtonRows)))));
            }

            for (int i = this.obstacleButtons.size() - 1; i >= 0; i--)
            {
                if (i / (this.objectButtonCols * this.objectButtonRows) == editor.obstaclePage)
                    this.obstacleButtons.get(i).draw();
            }

            this.drawMobileTooltip(this.obstacleButtons.get(this.editor.obstacleNum).hoverTextRawTranslated);
        }

        if (primaryMetadataButton != null)
            primaryMetadataButton.draw();

        if (secondaryMetadataButton != null)
            secondaryMetadataButton.draw();
    }

    public void drawMobileTooltip(String text)
    {
        if (!Game.game.window.touchscreen)
            return;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY - 300, 1120, 60);

        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 300, text.replace("---", " "));
    }

    @Override
    public Pointer<TankAIControlled> addTank(TankAIControlled t, boolean select)
    {
        this.editor.level.customTanks.add(t);

        if (select)
        {
            this.editor.tankNum = this.editor.level.customTanks.size() + Game.registryTank.tankEntries.size() - 1;
            this.editor.setMousePlaceable();
        }

        return new ArrayListIndexPointer<>(this.editor.level.customTanks, this.editor.level.customTanks.size() - 1);
    }

    @Override
    public void removeTank(TankAIControlled t)
    {
        this.editor.level.customTanks.remove(t);
        ArrayList<EditorAction> actions = new ArrayList<>();

        for (int i = 0; i < Game.movables.size(); i++)
        {
            Movable m = Game.movables.get(i);
            if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(t.name))
            {
                actions.add(new EditorAction.ActionTank((Tank) m, false));
                Game.movables.remove(i);
                i--;
            }
        }

        this.editor.undoActions.add(new EditorAction.ActionDeleteCustomTank(this.editor, actions, t));
    }

    @Override
    public void refreshTanks(TankAIControlled t)
    {
        Game.screen = new OverlayObjectMenu(this.previous, this.editor);

        String name = this.editor.mousePlaceable instanceof Tank ? ((Tank) this.editor.mousePlaceable).name : "";

        if (this.editor.mousePlaceable instanceof TankAIControlled)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(name))
                    t.cloneProperties((TankAIControlled) m);
            }
        }

        if (this.editor.tankNum >= this.editor.level.customTanks.size() + Game.registryTank.tankEntries.size())
            this.editor.tankNum--;

        this.editor.setMousePlaceable();
    }

    public int compareTo(TankAIControlled t1, TankAIControlled t2)
    {
        int i = Double.compare(t1.size, t2.size);
        if (i != 0)
            return i;
        if (t1.name == null || t2.name == null)
            return 0;

        return t1.name.compareTo(t2.name);
    }

    @Override
    public boolean rename(String from, String to)
    {
        for (TankAIControlled t1: this.editor.level.customTanks)
        {
            if (!t1.name.equals(from) && t1.name.equals(to))
                return false;
        }

        if (!TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(to).tank))
            return false;

        if (to.equals("player"))
            return false;

        for (TankAIControlled t1: this.editor.level.customTanks)
        {
            if (t1.renameLinkedTank(from, to))
            {
                for (Movable m : Game.movables)
                {
                    if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(t1.name))
                        t1.cloneProperties((TankAIControlled) m);
                }
            }
        }
        return true;
    }
}

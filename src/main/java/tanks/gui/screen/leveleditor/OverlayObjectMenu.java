package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.*;
import tanks.obstacle.ObstacleBeatBlock;
import tanks.tank.*;
import tanks.tankson.ArrayListIndexPointer;
import tanks.tankson.Pointer;
import tanks.translation.Translation;

import java.util.ArrayList;

public class OverlayObjectMenu extends ScreenLevelEditorOverlay implements ITankScreen, IRenamableScreen
{
    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public ArrayList<Button> tankButtons = new ArrayList<>();
    public ArrayList<Button> obstacleButtons = new ArrayList<>();

    public Runnable drawEditTank = () -> this.editTank.draw();

    public Button nextTankPage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> screenLevelEditor.tankPage++);

    public Button previousTankPage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> screenLevelEditor.tankPage--);

    public Button firstTankPage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> screenLevelEditor.tankPage = 0);

    public Button lastTankPage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> screenLevelEditor.tankPage = (tankButtons.size() - 1) / objectButtonRows / objectButtonCols);

    public Button nextObstaclePage = new Button(this.centerX + 290, this.centerY + 60 * 3, 350, 40, "Next page", () -> screenLevelEditor.obstaclePage++);

    public Button previousObstaclePage = new Button(this.centerX - 290, this.centerY + 60 * 3, 350, 40, "Previous page", () -> screenLevelEditor.obstaclePage--);

    public Button firstObstaclePage = new Button(this.centerX - 500, this.centerY + 60 * 3, 40, 40, "", () -> screenLevelEditor.obstaclePage = 0);

    public Button lastObstaclePage = new Button(this.centerX + 500, this.centerY + 60 * 3, 40, 40, "", () -> screenLevelEditor.obstaclePage = (obstacleButtons.size() - 1) / objectButtonRows / objectButtonCols);


    public Button exitObjectMenu = new Button(this.centerX, this.centerY + 240, 350, 40, "Ok", () ->
    {
        Game.screen = screenLevelEditor;
        screenLevelEditor.paused = false;
        screenLevelEditor.clickCooldown = 20;
    }
    );

    public Button rotateTankButton = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "Tank orientation", () -> Game.screen = new OverlayRotateTank(Game.screen, screenLevelEditor));

    public Button editHeight = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", () -> Game.screen = new OverlayBlockHeight(Game.screen, screenLevelEditor));

    public Button editGroupID = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", () -> Game.screen = new OverlayBlockGroupID(Game.screen, screenLevelEditor));

    public Button editBeatPattern = new Button(this.centerX + 380, this.centerY + 240, 350, 40, "", () -> Game.screen = new OverlayBeatBlockPattern(Game.screen, screenLevelEditor));

    public Button selectTeam = new Button(this.centerX + 380, this.centerY + 240, 350, 40, "", () -> Game.screen = new OverlayTeams(Game.screen, screenLevelEditor));

    public Button playerItems = new Button(this.centerX, this.centerY + 120, 350, 40, "Player items", () -> Game.screen = new OverlayPlayerItems(Game.screen, screenLevelEditor));

    public Button placePlayer = new Button(this.centerX - 380, this.centerY - 180, 350, 40, "Player", () ->
    {
        screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.playerTank;
        screenLevelEditor.mouseTank = new TankPlayer(0, 0, 0);
        ((TankPlayer) screenLevelEditor.mouseTank).setDefaultColor();
    }
    );

    public Button placeEnemy = new Button(this.centerX, this.centerY - 180, 350, 40, "Tank", () ->
    {
        screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.enemyTank;
        this.screenLevelEditor.refreshMouseTank();
    }
    );

    public Button placeObstacle = new Button(this.centerX + 380, this.centerY - 180, 350, 40, "Block", () -> screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.obstacle);

    public Button editTank = new Button(0, 0, 40, 40, "", () ->
    {
        TankAIControlled t = screenLevelEditor.level.customTanks.get(screenLevelEditor.tankNum - Game.registryTank.tankEntries.size());
        Pointer<TankAIControlled> p = new ArrayListIndexPointer<>(screenLevelEditor.level.customTanks, screenLevelEditor.tankNum - Game.registryTank.tankEntries.size());
        ScreenEditorTank s = new ScreenEditorTank(p, this);
        s.onComplete = () ->
        {
            if (p.get() == null)
                this.removeTank(t);

            this.refreshTanks(t);
        };
        Game.screen = s;
    }, "Edit custom tank");

    public ButtonObject movePlayerButton;

    public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), this.centerX + 50, this.centerY, 75, 75, () -> screenLevelEditor.movePlayer = false, "Add multiple player spawn points");

    public OverlayObjectMenu(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        this.musicInstruments = true;

        TankPlayer tp = new TankPlayer(0, 0, 0);
        tp.setDefaultColor();
        movePlayerButton = new ButtonObject(tp, this.centerX - 50, this.centerY, 75, 75, () -> screenLevelEditor.movePlayer = true, "Move the player");

        rotateTankButton.imageXOffset = -155;
        rotateTankButton.imageSizeX = 30;
        rotateTankButton.imageSizeY = 30;
        rotateTankButton.image = "icons/rotate_tank.png";

        selectTeam.imageXOffset = -150;
        selectTeam.imageSizeX = 30;
        selectTeam.imageSizeY = 30;
        selectTeam.image = "icons/team.png";

        editHeight.imageXOffset = -155;
        editHeight.imageSizeX = 30;
        editHeight.imageSizeY = 30;
        editHeight.image = "icons/obstacle_height.png";

        editGroupID.imageXOffset = -155;
        editGroupID.imageSizeX = 30;
        editGroupID.imageSizeY = 30;
        editGroupID.image = "icons/id.png";

        editBeatPattern.imageXOffset = -155;
        editBeatPattern.imageSizeX = 30;
        editBeatPattern.imageSizeY = 30;
        editBeatPattern.image = "icons/obstacle_beat.png";


        playerItems.imageXOffset = -155;
        playerItems.imageSizeX = 30;
        playerItems.imageSizeY = 30;
        playerItems.image = "item.png";

        int count = Game.registryTank.tankEntries.size() + this.screenLevelEditor.level.customTanks.size();

        for (int i = 0; i <= count; i++)
        {
            int rows = objectButtonRows;
            int cols = objectButtonCols;
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Tank t;

            if (i < Game.registryTank.tankEntries.size())
                t = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);
            else if (i == count)
            {
                Button b = new Button(x, y, 50, 50, "+", () ->
                {
                    ScreenAddSavedTank s = new ScreenAddSavedTank(this);
                    s.drawBehindScreen = true;
                    Game.screen = s;
                }, "Create a new custom tank!");
                this.tankButtons.add(b);
                b.fullInfo = true;

                continue;
            }
            else
                t = this.screenLevelEditor.level.customTanks.get(i - Game.registryTank.tankEntries.size());

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, () ->
            {
                screenLevelEditor.tankNum = j;

                if (j < Game.registryTank.tankEntries.size())
                    screenLevelEditor.mouseTank = Game.registryTank.getEntry(screenLevelEditor.tankNum).getTank(0, 0, 0);
                else
                    screenLevelEditor.mouseTank = ((TankAIControlled) t).instantiate(t.name, 0, 0, 0);
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
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            tanks.obstacle.Obstacle o = Game.registryObstacle.obstacleEntries.get(i).getObstacle(x, y);
            ButtonObject b = new ButtonObject(o, x, y, 75, 75, () ->
            {
                screenLevelEditor.obstacleNum = j;
                screenLevelEditor.mouseObstacle = Game.registryObstacle.getEntry(screenLevelEditor.obstacleNum).getObstacle(0, 0);

                if (screenLevelEditor.mouseObstacle.enableGroupID)
                    screenLevelEditor.mouseObstacle.setMetadata(screenLevelEditor.mouseObstacleGroup + "");
            }
                    , o.description);

            if (o.description.equals(""))
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

    public void update()
    {
        if (screenLevelEditor.tankNum >= Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size())
            screenLevelEditor.tankNum = Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size() - 1;

        this.placePlayer.enabled = (screenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.playerTank);
        this.placeEnemy.enabled = (screenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.enemyTank);
        this.placeObstacle.enabled = (screenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.obstacle);

        this.exitObjectMenu.update();

        this.placePlayer.update();
        this.placeEnemy.update();
        this.placeObstacle.update();

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            if (screenLevelEditor.teamNum >= screenLevelEditor.teams.size() + 1)
                screenLevelEditor.teamNum = 0;

            this.selectTeam.update();
            this.rotateTankButton.update();
        }

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            if (screenLevelEditor.playerTeamNum >= screenLevelEditor.teams.size() + 1)
                screenLevelEditor.playerTeamNum = 0;

            this.selectTeam.update();
            this.rotateTankButton.update();
            this.playerItems.update();
        }

        if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.enabled = screenLevelEditor.movePlayer;
            this.movePlayerButton.enabled = !screenLevelEditor.movePlayer;

            this.playerSpawnsButton.update();
            this.movePlayerButton.update();
        }
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            int pageCount = (this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols);
            if (screenLevelEditor.tankPage > pageCount)
                screenLevelEditor.tankPage = pageCount;

            for (int i = 0; i < this.tankButtons.size(); i++)
            {
                this.tankButtons.get(i).enabled = screenLevelEditor.tankNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == screenLevelEditor.tankPage)
                    this.tankButtons.get(i).update();
            }

            this.nextTankPage.enabled = (pageCount > screenLevelEditor.tankPage);
            this.previousTankPage.enabled = (screenLevelEditor.tankPage > 0);
            this.lastTankPage.enabled = this.nextTankPage.enabled;
            this.firstTankPage.enabled = this.previousTankPage.enabled;
            if (nextTankPage.enabled || previousTankPage.enabled)
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
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            for (int i = 0; i < this.obstacleButtons.size(); i++)
            {
                this.obstacleButtons.get(i).enabled = screenLevelEditor.obstacleNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == screenLevelEditor.obstaclePage)
                    this.obstacleButtons.get(i).update();
            }

            this.nextObstaclePage.enabled = ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > screenLevelEditor.obstaclePage);
            this.previousObstaclePage.enabled = (screenLevelEditor.obstaclePage > 0);
            this.lastObstaclePage.enabled = this.nextObstaclePage.enabled;
            this.firstObstaclePage.enabled = this.previousObstaclePage.enabled;
            if (nextObstaclePage.enabled || previousObstaclePage.enabled)
            {
                nextObstaclePage.update();
                previousObstaclePage.update();

                if ((obstacleButtons.size() - 1) / objectButtonRows / objectButtonCols >= 2)
                {
                    lastObstaclePage.update();
                    firstObstaclePage.update();
                }
            }
            if (screenLevelEditor.mouseObstacle.enableStacking)
                this.editHeight.update();

            if (screenLevelEditor.mouseObstacle.enableGroupID)
            {
                if (screenLevelEditor.mouseObstacle instanceof ObstacleBeatBlock)
                    this.editBeatPattern.update();
                else
                    this.editGroupID.update();
            }
        }

        if (screenLevelEditor.tankNum >= Game.registryTank.tankEntries.size())
        {
            Button b = this.tankButtons.get(screenLevelEditor.tankNum);
            this.editTank.posX = b.posX + 35;
            this.editTank.posY = b.posY + 35;
            this.editTank.update();
        }

        super.update();
    }

    public void draw()
    {
        super.draw();

        if (Game.screen != this)
            return;

        if (screenLevelEditor.tankNum >= Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size())
            screenLevelEditor.tankNum = Game.registryTank.tankEntries.size() + Game.currentLevel.customTanks.size() - 1;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1200, 600);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1180, 580);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 240, "Object menu");

        this.placePlayer.draw();
        this.placeEnemy.draw();
        this.placeObstacle.draw();

        if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank || this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            int teamNum = screenLevelEditor.teamNum;

            if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
                teamNum = screenLevelEditor.playerTeamNum;

            if (teamNum == screenLevelEditor.teams.size())
                this.selectTeam.setText("No team");
            else
                this.selectTeam.setText("Team: %s", (Object) screenLevelEditor.teams.get(teamNum).name);

            this.selectTeam.draw();
            this.rotateTankButton.draw();
        }

        this.exitObjectMenu.draw();

        if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.draw();
            this.movePlayerButton.draw();
            this.playerItems.draw();

            if (this.screenLevelEditor.movePlayer)
                this.drawMobileTooltip(this.movePlayerButton.hoverTextRawTranslated);
            else
                this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRawTranslated);

        }
        else if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            if (nextTankPage.enabled || previousTankPage.enabled)
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
                        Translation.translate("Page %d of %d", (screenLevelEditor.tankPage + 1), (tankButtons.size() / (objectButtonCols * objectButtonRows) + Math.min(1, tankButtons.size() % (objectButtonCols * objectButtonRows)))));

            }

            for (int i = tankButtons.size() - 1; i >= 0; i--)
            {
                if (i / (objectButtonCols * objectButtonRows) == screenLevelEditor.tankPage)
                {
                    if (screenLevelEditor.tankNum >= Game.registryTank.tankEntries.size() && !tankButtons.get(i).enabled && tankButtons.get(i) instanceof ButtonObject)
                    {
                        if (this.editTank.selected)
                            ((ButtonObject) tankButtons.get(i)).tempDisableHover = true;

                        ((ButtonObject) tankButtons.get(i)).drawBeforeTooltip = drawEditTank;
                    }

                    tankButtons.get(i).draw();
                }
            }

            this.drawMobileTooltip(this.tankButtons.get(screenLevelEditor.tankNum).hoverTextRawTranslated);
        }
        else if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
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
                        Translation.translate("Page %d of %d", (screenLevelEditor.obstaclePage + 1), (obstacleButtons.size() / (objectButtonCols * objectButtonRows) + Math.min(1, obstacleButtons.size() % (objectButtonCols * objectButtonRows)))));
            }

            for (int i = this.obstacleButtons.size() - 1; i >= 0; i--)
            {
                if (i / (this.objectButtonCols * this.objectButtonRows) == screenLevelEditor.obstaclePage)
                    this.obstacleButtons.get(i).draw();
            }

            if (screenLevelEditor.mouseObstacle.enableStacking)
            {
                this.editHeight.setText("Block height: %.1f", screenLevelEditor.mouseObstacleHeight);
                this.editHeight.draw();
            }

            if (screenLevelEditor.mouseObstacle.enableGroupID)
            {
                if (screenLevelEditor.mouseObstacle instanceof ObstacleBeatBlock)
                {
                    this.editBeatPattern.setText("Beat pattern: %s", (Object) ((int) Math.pow(2, (screenLevelEditor.mouseObstacle.groupID) / 2) + ((screenLevelEditor.mouseObstacle.groupID % 2 == 0) ? "a" : "b")));
                    this.editBeatPattern.draw();
                }
                else
                {
                    this.editGroupID.setText("Group ID: %d", screenLevelEditor.mouseObstacleGroup);
                    this.editGroupID.draw();
                }
            }

            this.drawMobileTooltip(this.obstacleButtons.get(this.screenLevelEditor.obstacleNum).hoverTextRawTranslated);
        }
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
        this.screenLevelEditor.level.customTanks.add(t);

        if (select)
        {
            this.screenLevelEditor.tankNum = this.screenLevelEditor.level.customTanks.size() + Game.registryTank.tankEntries.size() - 1;
            this.screenLevelEditor.refreshMouseTank();
        }

        return new ArrayListIndexPointer<>(this.screenLevelEditor.level.customTanks, this.screenLevelEditor.level.customTanks.size() - 1);
    }

    @Override
    public void removeTank(TankAIControlled t)
    {
        ArrayList<ScreenLevelEditor.Action> actions = new ArrayList<>();

        for (TankAIControlled t1: this.screenLevelEditor.level.customTanks)
        {
            if (t1.renameLinkedTank(t.name, null))
            {
                for (Movable m : Game.movables)
                {
                    if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(t1.name))
                        t1.cloneProperties((TankAIControlled) m);
                }
            }
        }

        for (int i = 0; i < Game.movables.size(); i++)
        {
            Movable m = Game.movables.get(i);
            if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(t.name))
            {
                actions.add(new ScreenLevelEditor.Action.ActionTank((Tank) m, false));
                Game.movables.remove(i);
                i--;
            }
        }

        this.screenLevelEditor.actions.add(new ScreenLevelEditor.Action.ActionDeleteCustomTank(this.screenLevelEditor, actions, t));
    }

    @Override
    public void refreshTanks(TankAIControlled t)
    {
        Game.screen = new OverlayObjectMenu(this.previous, this.screenLevelEditor);

        String name = this.screenLevelEditor.mouseTank.name;

        if (this.screenLevelEditor.mouseTank instanceof TankAIControlled)
        {
            for (Movable m : Game.movables)
            {
                if (m instanceof TankAIControlled && ((TankAIControlled) m).name.equals(name))
                    t.cloneProperties((TankAIControlled) m);
            }
        }

        if (this.screenLevelEditor.tankNum >= this.screenLevelEditor.level.customTanks.size() + Game.registryTank.tankEntries.size())
            this.screenLevelEditor.tankNum--;

        this.screenLevelEditor.refreshMouseTank();
    }

    @Override
    public boolean rename(String from, String to)
    {
        for (TankAIControlled t1: this.screenLevelEditor.level.customTanks)
        {
            if (!t1.name.equals(from) && t1.name.equals(to))
                return false;
        }

        if (!TankUnknown.class.isAssignableFrom(Game.registryTank.getEntry(to).tank))
            return false;

        if (to.equals("player"))
            return false;

        for (TankAIControlled t1: this.screenLevelEditor.level.customTanks)
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

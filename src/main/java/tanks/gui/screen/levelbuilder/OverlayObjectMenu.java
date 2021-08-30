package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.Screen;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class OverlayObjectMenu extends ScreenLevelBuilderOverlay
{
    public int tankButtonPage = 0;
    public int obstacleButtonPage = 0;

    public int objectButtonRows = 3;
    public int objectButtonCols = 10;

    public ArrayList<ButtonObject> tankButtons = new ArrayList<ButtonObject>();
    public ArrayList<ButtonObject> obstacleButtons = new ArrayList<ButtonObject>();

    public Button nextTankPage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            tankButtonPage++;
        }
    }
    );

    public Button previousTankPage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            tankButtonPage--;
        }
    }
    );

    public Button nextObstaclePage = new Button(this.centerX + 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Next page", new Runnable()
    {
        @Override
        public void run()
        {
            obstacleButtonPage++;
        }
    }
    );

    public Button previousObstaclePage = new Button(this.centerX - 190, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Previous page", new Runnable()
    {
        @Override
        public void run()
        {
            obstacleButtonPage--;
        }
    }
    );

    public Button exitObjectMenu = new Button(this.centerX, this.centerY + 240, 350, 40, "Ok", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = screenLevelEditor;
            screenLevelEditor.paused = false;
            screenLevelEditor.clickCooldown = 20;
        }
    }
    );

    public Button rotateTankButton = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "Tank orientation", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayRotateTank(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button editHeight = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayBlockHeight(Game.screen, screenLevelEditor);

        }
    }
    );

    public Button editGroupID = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayBlockGroupID(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button selectTeam = new Button(this.centerX + 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlaySelectTeam(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button placePlayer = new Button(this.centerX - 380, this.centerY - 180, 350, 40, "Player", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.playerTank;
            screenLevelEditor.mouseTank = new TankPlayer(0, 0, 0);
        }
    }
    );

    public Button placeEnemy = new Button(this.centerX, this.centerY - 180, 350, 40, "Tank", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.enemyTank;
            screenLevelEditor.mouseTank = Game.registryTank.getEntry( screenLevelEditor.tankNum).getTank(0, 0, 0);
        }
    }
    );
    public Button placeObstacle = new Button(this.centerX + 380, this.centerY - 180, 350, 40, "Block", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.currentPlaceable = ScreenLevelEditor.Placeable.obstacle;
        }
    }
    );

    public ButtonObject movePlayerButton = new ButtonObject(new TankPlayer(0, 0, 0), this.centerX - 50, this.centerY, 75, 75, new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.movePlayer = true;
        }
    }, "Move the player");

    public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), this.centerX + 50, this.centerY, 75, 75, new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.movePlayer = false;
        }
    }, "Add multiple player spawn points");

    public OverlayObjectMenu(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        for (int i = 0; i < Game.registryTank.tankEntries.size(); i++)
        {
            int rows = objectButtonRows;
            int cols = objectButtonCols;
            int index = i % (rows * cols);
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            Tank t = Game.registryTank.tankEntries.get(i).getTank(x, y, 0);

            ButtonObject b = new ButtonObject(t, x, y, 75, 75, new Runnable()
            {
                @Override
                public void run()
                {
                    screenLevelEditor.tankNum = j;
                    screenLevelEditor.mouseTank = Game.registryTank.getEntry(screenLevelEditor.tankNum).getTank(0, 0, 0);
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
            double x = this.centerX - 450 + 100 * (index % cols);
            double y = this.centerY - 100 + 100 * ((index / cols) % rows);

            final int j = i;

            tanks.obstacle.Obstacle o = Game.registryObstacle.obstacleEntries.get(i).getObstacle(x, y);
            ButtonObject b = new ButtonObject(o, x, y, 75, 75, new Runnable()
            {
                @Override
                public void run()
                {
                    screenLevelEditor.obstacleNum = j;
                    screenLevelEditor.mouseObstacle = Game.registryObstacle.getEntry(screenLevelEditor.obstacleNum).getObstacle(0, 0);

                    if (screenLevelEditor.mouseObstacle.enableGroupID)
                        screenLevelEditor.mouseObstacle.setMetadata(screenLevelEditor.mouseObstacleGroup + "");
                }
            }
                    , o.description);

            if (o.description.equals(""))
                b.enableHover = false;

            this.obstacleButtons.add(b);
        }

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
    }

    public void update()
    {
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
            for (int i = 0; i < this.tankButtons.size(); i++)
            {
                this.tankButtons.get(i).enabled = screenLevelEditor.tankNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == tankButtonPage)
                    this.tankButtons.get(i).update();
            }

            if ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankButtonPage)
                nextTankPage.update();

            if (tankButtonPage > 0)
                previousTankPage.update();
        }
        else if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            for (int i = 0; i < this.obstacleButtons.size(); i++)
            {
                this.obstacleButtons.get(i).enabled = screenLevelEditor.obstacleNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == obstacleButtonPage)
                    this.obstacleButtons.get(i).update();
            }

            if ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > obstacleButtonPage)
                nextObstaclePage.update();

            if (obstacleButtonPage > 0)
                previousObstaclePage.update();

            if (screenLevelEditor.mouseObstacle.enableStacking)
            {
                this.editHeight.update();
            }
            else if (screenLevelEditor.mouseObstacle.enableGroupID)
            {
                this.editGroupID.update();
            }
        }

        super.update();
    }

    public void draw()
    {
        super.draw();
        Drawing.drawing.setColor(this.screenLevelEditor.fontBrightness, this.screenLevelEditor.fontBrightness, this.screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 240, "Object menu");

        this.placePlayer.draw();
        this.placeEnemy.draw();
        this.placeObstacle.draw();

        if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank || this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            int teamNum = screenLevelEditor.teamNum;

            if (screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
                teamNum = screenLevelEditor.playerTeamNum;

            if (teamNum == screenLevelEditor.teams.size())
                this.selectTeam.text = "No team";
            else
                this.selectTeam.text = "Team: " + screenLevelEditor.teams.get(teamNum).name;

            this.selectTeam.draw();
            this.rotateTankButton.draw();
        }

        this.exitObjectMenu.draw();

        if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.playerTank)
        {
            this.playerSpawnsButton.draw();
            this.movePlayerButton.draw();

            if (this.screenLevelEditor.movePlayer)
                this.drawMobileTooltip(this.movePlayerButton.hoverTextRaw);
            else
                this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRaw);

        }
        else if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.enemyTank)
        {
            if ((tankButtons.size() - 1) / (objectButtonRows * objectButtonCols) > tankButtonPage)
                nextTankPage.draw();

            if (tankButtonPage > 0)
                previousTankPage.draw();

            for (int i = tankButtons.size() - 1; i >= 0; i--)
            {
                if (i / (objectButtonCols * objectButtonRows) == tankButtonPage)
                    tankButtons.get(i).draw();
            }

            this.drawMobileTooltip(this.tankButtons.get(screenLevelEditor.tankNum).hoverTextRaw);
        }
        else if (this.screenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle)
        {
            if ((this.obstacleButtons.size() - 1) / (objectButtonRows * objectButtonCols) > obstacleButtonPage)
                nextObstaclePage.draw();

            if (obstacleButtonPage > 0)
                previousObstaclePage.draw();

            for (int i = this.obstacleButtons.size() - 1; i >= 0; i--)
            {
                if (i / (this.objectButtonCols * this.objectButtonRows) == obstacleButtonPage)
                    this.obstacleButtons.get(i).draw();
            }

            if (screenLevelEditor.mouseObstacle.enableStacking)
            {
                this.editHeight.text = "Block height: " + screenLevelEditor.mouseObstacleHeight;
                this.editHeight.draw();
            }
            else if (screenLevelEditor.mouseObstacle.enableGroupID)
            {
                this.editGroupID.text = "Group ID: " + screenLevelEditor.mouseObstacleGroup;
                this.editGroupID.draw();
            }

            this.drawMobileTooltip(this.obstacleButtons.get(this.screenLevelEditor.obstacleNum).hoverTextRaw);
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
}

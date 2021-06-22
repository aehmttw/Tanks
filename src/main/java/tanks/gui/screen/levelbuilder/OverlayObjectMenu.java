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
            Game.screen = screenLevelBuilder;
            screenLevelBuilder.paused = false;
            screenLevelBuilder.clickCooldown = 20;
        }
    }
    );

    public Button rotateTankButton = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "Tank orientation", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayRotateTank(Game.screen, screenLevelBuilder);
        }
    }
    );

    public Button editHeight = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayBlockHeight(Game.screen, screenLevelBuilder);

        }
    }
    );

    public Button editGroupID = new Button(this.centerX - 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayBlockGroupID(Game.screen, screenLevelBuilder);
        }
    }
    );

    public Button selectTeam = new Button(this.centerX + 380, this.centerY + 240, 350, 40, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlaySelectTeam(Game.screen, screenLevelBuilder);
        }
    }
    );

    public Button placePlayer = new Button(this.centerX - 380, this.centerY - 180, 350, 40, "Player", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.currentPlaceable = ScreenLevelBuilder.Placeable.playerTank;
            screenLevelBuilder.mouseTank = new TankPlayer(0, 0, 0);
        }
    }
    );

    public Button placeEnemy = new Button(this.centerX, this.centerY - 180, 350, 40, "Tank", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.currentPlaceable = ScreenLevelBuilder.Placeable.enemyTank;
            screenLevelBuilder.mouseTank = Game.registryTank.getEntry( screenLevelBuilder.tankNum).getTank(0, 0, 0);
        }
    }
    );
    public Button placeObstacle = new Button(this.centerX + 380, this.centerY - 180, 350, 40, "Block", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.currentPlaceable = ScreenLevelBuilder.Placeable.obstacle;
        }
    }
    );

    public ButtonObject movePlayerButton = new ButtonObject(new TankPlayer(0, 0, 0), this.centerX - 50, this.centerY, 75, 75, new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.movePlayer = true;
        }
    }, "Move the player");

    public ButtonObject playerSpawnsButton = new ButtonObject(new TankSpawnMarker("player", 0, 0, 0), this.centerX + 50, this.centerY, 75, 75, new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.movePlayer = false;
        }
    }, "Add multiple player spawn points");

    public OverlayObjectMenu(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

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
                    screenLevelBuilder.tankNum = j;
                    screenLevelBuilder.mouseTank = Game.registryTank.getEntry(screenLevelBuilder.tankNum).getTank(0, 0, 0);
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
                    screenLevelBuilder.obstacleNum = j;
                    screenLevelBuilder.mouseObstacle = Game.registryObstacle.getEntry(screenLevelBuilder.obstacleNum).getObstacle(0, 0);

                    if (screenLevelBuilder.mouseObstacle.enableGroupID)
                        screenLevelBuilder.mouseObstacle.setMetadata(screenLevelBuilder.mouseObstacleGroup + "");
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
        this.placePlayer.enabled = (screenLevelBuilder.currentPlaceable != ScreenLevelBuilder.Placeable.playerTank);
        this.placeEnemy.enabled = (screenLevelBuilder.currentPlaceable != ScreenLevelBuilder.Placeable.enemyTank);
        this.placeObstacle.enabled = (screenLevelBuilder.currentPlaceable != ScreenLevelBuilder.Placeable.obstacle);

        this.exitObjectMenu.update();

        this.placePlayer.update();
        this.placeEnemy.update();
        this.placeObstacle.update();

        if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank)
        {
            if (screenLevelBuilder.teamNum >= screenLevelBuilder.teams.size() + 1)
                screenLevelBuilder.teamNum = 0;

            this.selectTeam.update();
            this.rotateTankButton.update();
        }

        if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
        {
            if (screenLevelBuilder.playerTeamNum >= screenLevelBuilder.teams.size() + 1)
                screenLevelBuilder.playerTeamNum = 0;

            this.selectTeam.update();
            this.rotateTankButton.update();
        }

        if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
        {
            this.playerSpawnsButton.enabled = screenLevelBuilder.movePlayer;
            this.movePlayerButton.enabled = !screenLevelBuilder.movePlayer;

            this.playerSpawnsButton.update();
            this.movePlayerButton.update();
        }
        else if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank)
        {
            for (int i = 0; i < this.tankButtons.size(); i++)
            {
                this.tankButtons.get(i).enabled = screenLevelBuilder.tankNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == tankButtonPage)
                    this.tankButtons.get(i).update();
            }

            if ((this.tankButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > tankButtonPage)
                nextTankPage.update();

            if (tankButtonPage > 0)
                previousTankPage.update();
        }
        else if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.obstacle)
        {
            for (int i = 0; i < this.obstacleButtons.size(); i++)
            {
                this.obstacleButtons.get(i).enabled = screenLevelBuilder.obstacleNum != i;

                if (i / (this.objectButtonCols * this.objectButtonRows) == obstacleButtonPage)
                    this.obstacleButtons.get(i).update();
            }

            if ((this.obstacleButtons.size() - 1) / (this.objectButtonRows * this.objectButtonCols) > obstacleButtonPage)
                nextObstaclePage.update();

            if (obstacleButtonPage > 0)
                previousObstaclePage.update();

            if (screenLevelBuilder.mouseObstacle.enableStacking)
            {
                this.editHeight.update();
            }
            else if (screenLevelBuilder.mouseObstacle.enableGroupID)
            {
                this.editGroupID.update();
            }
        }

        super.update();
    }

    public void draw()
    {
        super.draw();
        Drawing.drawing.setColor(this.screenLevelBuilder.fontBrightness, this.screenLevelBuilder.fontBrightness, this.screenLevelBuilder.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 240, "Object menu");

        this.placePlayer.draw();
        this.placeEnemy.draw();
        this.placeObstacle.draw();

        if (this.screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank || this.screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
        {
            int teamNum = screenLevelBuilder.teamNum;

            if (screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
                teamNum = screenLevelBuilder.playerTeamNum;

            if (teamNum == screenLevelBuilder.teams.size())
                this.selectTeam.text = "No team";
            else
                this.selectTeam.text = "Team: " + screenLevelBuilder.teams.get(teamNum).name;

            this.selectTeam.draw();
            this.rotateTankButton.draw();
        }

        this.exitObjectMenu.draw();

        if (this.screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.playerTank)
        {
            this.playerSpawnsButton.draw();
            this.movePlayerButton.draw();

            if (this.screenLevelBuilder.movePlayer)
                this.drawMobileTooltip(this.movePlayerButton.hoverTextRaw);
            else
                this.drawMobileTooltip(this.playerSpawnsButton.hoverTextRaw);

        }
        else if (this.screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.enemyTank)
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

            this.drawMobileTooltip(this.tankButtons.get(screenLevelBuilder.tankNum).hoverTextRaw);
        }
        else if (this.screenLevelBuilder.currentPlaceable == ScreenLevelBuilder.Placeable.obstacle)
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

            if (screenLevelBuilder.mouseObstacle.enableStacking)
            {
                this.editHeight.text = "Block height: " + screenLevelBuilder.mouseObstacleHeight;
                this.editHeight.draw();
            }
            else if (screenLevelBuilder.mouseObstacle.enableGroupID)
            {
                this.editGroupID.text = "Group ID: " + screenLevelBuilder.mouseObstacleGroup;
                this.editGroupID.draw();
            }

            this.drawMobileTooltip(this.obstacleButtons.get(this.screenLevelBuilder.obstacleNum).hoverTextRaw);
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

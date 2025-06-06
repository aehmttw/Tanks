package tanks.gui.screen;

import tanks.*;
import tanks.gui.Button;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenCrusadePreviewLevel extends Screen implements ILevelPreviewScreen
{
    public String level;
    public Screen previous;
    public Crusade crusade;
    public DisplayLevel levelDisplay;

    public int index;

    public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();
            Game.screen = previous;
        }
    });

    public Button next = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Next", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            String level = crusade.levels.get(index + 1).levelString;

            ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(crusade, level, index + 1, previous);
            Level l = new Level(level);
            l.customTanks = crusade.customTanks;
            l.loadLevel(s);
            Game.screen = s;
        }
    });

    public Button prev = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 150, this.objWidth, this.objHeight, "Previous", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            String level = crusade.levels.get(index - 1).levelString;

            ScreenCrusadePreviewLevel s = new ScreenCrusadePreviewLevel(crusade, level, index - 1, previous);
            Level l = new Level(level);
            l.customTanks = crusade.customTanks;
            l.loadLevel(s);
            Game.screen = s;
        }
    });

    public ScreenCrusadePreviewLevel(Crusade crusade, String level, int index, Screen s)
    {
        super(350, 40, 380, 60);

        if (!(ScreenPartyHost.isServer || ScreenPartyLobby.isClient))
        {
            prev.posY += 40;
            back.posY += 40;
            next.posY += 40;
        }

        this.levelDisplay = new DisplayLevel();

        this.music = "menu_4.ogg";
        this.musicID = "menu";

        this.index = index;
        this.crusade = crusade;
        this.level = level;

        this.previous = s;

        next.enabled = index < crusade.levels.size() - 1;
        prev.enabled = index > 0;

        this.next.image = "icons/arrow_down.png";
        this.next.imageSizeX = 25;
        this.next.imageSizeY = 25;
        this.next.imageXOffset = 145;

        this.prev.image = "icons/arrow_up.png";
        this.prev.imageSizeX = 25;
        this.prev.imageSizeY = 25;
        this.prev.imageXOffset = 145;
    }

    @Override
    public void update()
    {
        this.back.update();

        if (Game.enable3d)
            Game.recomputeHeightGrid();

        if (Game.game.input.editorPause.isValid())
        {
            back.function.run();
            Game.game.input.editorPause.invalidate();
        }

        if (Game.game.window.validScrollUp)
        {
            Game.game.window.validScrollUp = false;
            if (this.prev.enabled)
                this.prev.function.run();
        }
        else if (Game.game.window.validScrollDown)
        {
            Game.game.window.validScrollDown = false;
            if (this.next.enabled)
                this.next.function.run();
        }
        else
        {
            this.next.update();
            this.prev.update();
        }
    }



    @Override
    public void draw()
    {
        this.levelDisplay.draw();

        double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX + extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY / 2,
                400, height);

        double posX = Drawing.drawing.interfaceSizeX - 200;

        for (int i = 0; i < 7; i++)
        {
            if (index - i - 1 < 0)
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 - (i + 1) * 40 - 60, (index - i) + ". " + crusade.levels.get(index - i - 1).levelName.replace("_", " "));
        }

        for (int i = 0; i < 7; i++)
        {
            if (index + i + 1 >= crusade.levels.size())
                break;

            Drawing.drawing.setColor(255, 255, 255, 200 - i * 30);
            Drawing.drawing.setInterfaceFontSize(this.textSize);
            Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 + (i + 1) * 40 - 60, (index + i + 2) + ". " + crusade.levels.get(index + i + 1).levelName.replace("_", " "));
        }

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(posX, Drawing.drawing.interfaceSizeY / 2 - 60, 380, 40);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawInterfaceText(posX, Drawing.drawing.interfaceSizeY / 2 - 60, (index + 1) + ". " + crusade.levels.get(index).levelName.replace("_", " "));

        this.back.draw();
        this.next.draw();
        this.prev.draw();
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.levelDisplay.spawns;
    }
}

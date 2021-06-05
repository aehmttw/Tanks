package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

public class OverlayEditorMenu extends ScreenLevelBuilderOverlay
{
    public Button resume = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Edit", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    });

    public Button play = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2), this.objWidth, this.objHeight, "Play", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.play();
        }
    }
    );

    public Button playUnavailable = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

    public Button options = new Button(this.centerX, (int) (this.centerY + 0), this.objWidth, this.objHeight, "Options", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptions(Game.screen, screenLevelBuilder);
        }
    }
    );

    public Button quit = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelBuilder.save();

            Game.cleanUp();
            Game.screen = new ScreenSavedLevels();
        }
    }
    );

    public Button delete = new Button(this.centerX, (int) (this.centerY + this.objYSpace), this.objWidth, this.objHeight, "Delete level", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayConfirmDelete(Game.screen, screenLevelBuilder);
        }
    }
    );

    public OverlayEditorMenu(Screen previous, ScreenLevelBuilder screenLevelBuilder)
    {
        super(previous, screenLevelBuilder);

        if (!screenLevelBuilder.level.editable)
        {
            play.posY += 60;
            delete.posY -= 60;
            quit.posY -= 60;
        }

    }

    public void update()
    {
        if (screenLevelBuilder.level.editable)
        {
            resume.update();
            options.update();
        }

        delete.update();
        quit.update();

        if (screenLevelBuilder.spawns.size() > 0)
            play.update();
        else
            playUnavailable.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        if (screenLevelBuilder.level.editable)
        {
            resume.draw();
            options.draw();
        }

        delete.draw();
        quit.draw();

        if (screenLevelBuilder.spawns.size() > 0)
            play.draw();
        else
            playUnavailable.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness, screenLevelBuilder.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level menu");
    }
}

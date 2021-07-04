package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;
import tanks.rpc.RichPresenceEvent;

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
            screenLevelEditor.play();
            Game.game.discordRPC.update(RichPresenceEvent.SINGLEPLAYER, RichPresenceEvent.CUSTOM_LEVEL);
        }
    }
    );

    public Button playUnavailable = new Button(this.centerX, (int) (this.centerY - this.objYSpace * 2), this.objWidth, this.objHeight, "Play", "You must add a player---spawn point to play!");

    public Button options = new Button(this.centerX, (int) (this.centerY + 0), this.objWidth, this.objHeight, "Options", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptions(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button quit = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Exit", new Runnable()
    {
        @Override
        public void run()
        {
            screenLevelEditor.save();

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
            Game.screen = new OverlayConfirmDelete(Game.screen, screenLevelEditor);
        }
    }
    );

    public OverlayEditorMenu(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        if (!screenLevelEditor.level.editable)
        {
            play.posY += 60;
            delete.posY -= 60;
            quit.posY -= 60;
        }

        Game.game.discordRPC.update(RichPresenceEvent.SINGLEPLAYER, RichPresenceEvent.LEVEL_EDITOR);

    }

    public void update()
    {
        if (screenLevelEditor.level.editable)
        {
            resume.update();
            options.update();
        }

        delete.update();
        quit.update();

        if (screenLevelEditor.spawns.size() > 0)
            play.update();
        else
            playUnavailable.update();

        super.update();

        if (Game.game.input.editorPlay.isValid() && screenLevelEditor.spawns.size() > 0)
        {
            screenLevelEditor.play();
            Game.game.input.play.invalidate();
        }
    }

    public void draw()
    {
        super.draw();

        if (screenLevelEditor.level.editable)
        {
            resume.draw();
            options.draw();
        }

        delete.draw();
        quit.draw();

        if (screenLevelEditor.spawns.size() > 0)
            play.draw();
        else
            playUnavailable.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level menu");
    }
}

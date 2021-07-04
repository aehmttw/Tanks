package tanks.gui.screen.levelbuilder;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptions extends ScreenLevelBuilderOverlay
{
    public TextBox levelName;

    public Button back = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            escape();
        }
    }
    );

    public Button colorOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Background colors", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsColor(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button sizeOptions = new Button(this.centerX  - this.objXSpace / 2, this.centerY - this.objYSpace * 1, this.objWidth, this.objHeight, "Level size", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsSize(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button timerOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Time limit", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsTimer(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button lightingOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Lighting", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsLighting(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button teamsOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Teams", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsTeams(Game.screen, screenLevelEditor);
        }
    }
    );

    public Button itemOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Items", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new OverlayLevelOptionsItems(Game.screen, screenLevelEditor);
        }
    }
    );

    public OverlayLevelOptions(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        levelName = new TextBox(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Level name", new Runnable()
        {
            @Override
            public void run()
            {
                BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + screenLevelEditor.name);

                String input = levelName.inputText.replace(" ", "_");
                if (levelName.inputText.length() > 0 && !Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + input + ".tanks").exists())
                {
                    if (file.exists())
                    {
                        file.renameTo(Game.homedir + Game.levelDir + "/" + input + ".tanks");
                    }

                    while (file.exists())
                    {
                        file.delete();
                    }

                    screenLevelEditor.name = input + ".tanks";

                    //screenLevelBuilder.reload(false);
                }
                else
                {
                    levelName.inputText =  screenLevelEditor.name.split("\\.")[0].replace("_", " ");
                }

            }

        }
                ,  screenLevelEditor.name.split("\\.")[0].replace("_", " "));

        levelName.enableCaps = true;

        lightingOptions.enabled = Game.framework != Game.Framework.libgdx;
    }

    public void update()
    {
        this.levelName.update();
        this.back.update();

        this.sizeOptions.update();
        this.colorOptions.update();
        this.lightingOptions.update();

        this.teamsOptions.update();
        this.itemOptions.update();
        this.timerOptions.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        this.levelName.draw();
        this.back.draw();

        this.timerOptions.draw();
        this.itemOptions.draw();
        this.teamsOptions.draw();

        this.lightingOptions.draw();
        this.colorOptions.draw();
        this.sizeOptions.draw();

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level options");
    }
}

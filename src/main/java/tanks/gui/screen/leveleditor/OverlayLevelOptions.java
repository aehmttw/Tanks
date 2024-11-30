package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayLevelOptions extends ScreenLevelEditorOverlay
{
    public TextBox levelName;

    public Button back = new Button(this.centerX, (int) (this.centerY + this.objYSpace * 2), this.objWidth, this.objHeight, "Back", this::escape);

    public Button colorOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Background colors", () -> Game.screen = new OverlayLevelOptionsColor(Game.screen, editor));

    public Button sizeOptions = new Button(this.centerX  - this.objXSpace / 2, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Level size", () -> Game.screen = new OverlayLevelOptionsSize(Game.screen, editor));

    public Button timerOptions = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Time limit", () -> Game.screen = new OverlayLevelOptionsTimer(Game.screen, editor));

    public Button lightingOptions = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "Lighting", () -> Game.screen = new OverlayLevelOptionsLighting(Game.screen, editor));

    public OverlayLevelOptions(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        levelName = new TextBox(this.centerX, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Level name", () ->
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
                ,  screenLevelEditor.name.split("\\.")[0].replace("_", " "));

        levelName.enableCaps = true;
        screenLevelEditor.modified = true;
    }

    public void update()
    {
        this.levelName.update();
        this.back.update();

        this.sizeOptions.update();
        this.colorOptions.update();
        this.lightingOptions.update();

        this.timerOptions.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        this.levelName.draw();
        this.back.draw();

        this.timerOptions.draw();

        this.lightingOptions.draw();
        this.colorOptions.draw();
        this.sizeOptions.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Level options");
    }
}

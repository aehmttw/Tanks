package tanks.gui.screen.leveleditor;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.gui.screen.Screen;

public class OverlayCloneLevel extends ScreenLevelEditorOverlay
{
    public TextBox levelName;

    public Button saveCopy = new Button(this.centerX, this.centerY + this.objYSpace * 0.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            String n = levelName.inputText.replace(" ", "_") + ".tanks";
            editor.save(n);

            saveCopy.enabled = false;
            saveCopy.setText("Level copy saved!");
        }
    });

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", this::escape);

    public OverlayCloneLevel(Screen previous, ScreenLevelEditor screenLevelEditor)
    {
        super(previous, screenLevelEditor);

        levelName = new TextBox(this.centerX, this.centerY - this.objYSpace * 0.5, this.objWidth, this.objHeight, "Level save name", () ->
        {
            if (levelName.inputText.equals(""))
                levelName.inputText = levelName.previousInputText;
            updateCloneButton();
        }
                , screenLevelEditor.name.replace("_", " ").split("\\.")[0]);

        levelName.enableCaps = true;
        updateCloneButton();
    }

    public void updateCloneButton()
    {
        String n = levelName.inputText.replace(" ", "_") + ".tanks";
        BaseFile file = Game.game.fileManager.getFile(Game.homedir + Game.levelDir + "/" + n);

        if (n.equals(editor.name))
        {
            saveCopy.setText("Pick a name for the copy!");
            saveCopy.enabled = false;
        }
        else if (file.exists())
        {
            saveCopy.setText("Pick a different name...");
            saveCopy.enabled = false;
        }
        else
        {
            saveCopy.setText("Save copy");
            saveCopy.enabled = true;
        }
    }

    public void update()
    {
        this.back.update();
        this.saveCopy.update();
        this.levelName.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(editor.fontBrightness, editor.fontBrightness, editor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Make a copy");

        this.back.draw();
        this.saveCopy.draw();
        this.levelName.draw();
    }
}

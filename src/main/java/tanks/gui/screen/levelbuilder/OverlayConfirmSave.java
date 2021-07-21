package tanks.gui.screen.levelbuilder;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenSavedLevels;

public class OverlayConfirmSave extends ScreenLevelBuilderOverlay {

    public Button saveExit = new Button(this.centerX, this.centerY - this.objYSpace - 20, this.objWidth, this.objHeight, "Yes", () -> {
        screenLevelEditor.save();

        Game.cleanUp();
        Game.screen = new ScreenSavedLevels();
    });

    public Button noSaveExit = new Button(this.centerX, this.centerY - this.objYSpace + 40, this.objWidth, this.objHeight, "No", () -> {
        Game.cleanUp();
        Game.screen = new ScreenSavedLevels();
    });

    public Button cancel = new Button(this.centerX, this.centerY - this.objYSpace + 100, this.objWidth, this.objHeight, "Cancel", () -> Game.screen = previous);

    public OverlayConfirmSave(Screen previous, ScreenLevelEditor screenLevelEditor) {
        super(previous, screenLevelEditor);
    }

    @Override
    public void update() {
        super.update();

        saveExit.update();
        noSaveExit.update();
        cancel.update();
    }

    @Override
    public void draw() {
        super.draw();

        Drawing.drawing.setColor(screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness, screenLevelEditor.fontBrightness);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 200, "This level has unsaved changes.");
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 160, "Do you want to save?");

        saveExit.draw();
        noSaveExit.draw();
        cancel.draw();
    }
}

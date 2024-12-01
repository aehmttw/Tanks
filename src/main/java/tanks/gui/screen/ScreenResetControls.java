package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;

public class ScreenResetControls extends Screen
{
    public Screen previous;

    public Button reset = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, this.objWidth, this.objHeight, "Reset controls", () ->
    {
        for (InputBindingGroup i: Game.game.inputBindings)
        {
            i.reset();
            Game.screen = ScreenOverlayControls.lastControlsScreen;
        }
    }
    );

    public Button cancel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "No", () -> Game.screen = ScreenOverlayControls.lastControlsScreen
    );

    public ScreenResetControls(Screen s)
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
        this.previous = s;
    }

    @Override
    public void update()
    {
        reset.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        this.previous.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 800 * this.objWidth / 350, 400 * this.objHeight / 40);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.textSize);

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Are you sure you want to reset all controls?");

        reset.draw();
        cancel.draw();
    }
}

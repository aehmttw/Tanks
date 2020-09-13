package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;

public class ScreenResetControls extends Screen
{
    public Button reset = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, 350, 40, "Reset controls", new Runnable()
    {
        @Override
        public void run()
        {
            for (InputBindingGroup i: Game.game.inputBindings)
            {
                i.reset();
                Game.screen = ScreenOverlayControls.lastControlsScreen;
            }
        }
    }
    );

    public Button cancel = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, 350, 40, "No", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = ScreenOverlayControls.lastControlsScreen;
        }
    }
    );

    public ScreenResetControls()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";
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
        this.drawDefaultBackground();

        reset.draw();
        cancel.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Are you sure you want to reset all controls?");
    }
}

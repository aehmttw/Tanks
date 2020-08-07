package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOverlayControls
{
    public static Screen lastControlsScreen = new ScreenControlsGame();

    public Button game = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 90, 350, 40, "Game", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenControlsGame();
        }
    });

    public Button tank = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 30, 350, 40, "Tank", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenControlsTank();
        }
    });

    public Button hotbar = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 30, 350, 40, "Hotbar", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenControlsHotbar();
        }
    });

    public Button editor = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 90, 350, 40, "Editor", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenControlsEditor();
        }
    });

    Button reset = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 290, 350, 40, "Reset", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenResetControls();
        }
    }
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 350, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.input.save();
            Game.screen = new ScreenOptionsInputDesktop();
        }
    }
    );

    public void update()
    {
        game.enabled = !(Game.screen instanceof ScreenControlsGame);
        tank.enabled = !(Game.screen instanceof ScreenControlsTank);
        hotbar.enabled = !(Game.screen instanceof ScreenControlsHotbar);
        editor.enabled = !(Game.screen instanceof ScreenControlsEditor);

        lastControlsScreen = Game.screen;

        game.update();
        tank.update();
        hotbar.update();
        editor.update();
        reset.update();
        back.update();
    }

    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 127);

        double extraWidth = (Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeX) / 2;
        double height = (Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale;

        Drawing.drawing.fillInterfaceRect(-extraWidth / 2, Drawing.drawing.interfaceSizeY / 2, extraWidth, height);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX / 3, height);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 350, "Controls");

        game.draw();
        tank.draw();
        hotbar.draw();
        editor.draw();
        reset.draw();
        back.draw();
    }
}

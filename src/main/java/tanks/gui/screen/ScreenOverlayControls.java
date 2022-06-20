package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOverlayControls
{
    public static Screen lastControlsScreen = new ScreenControlsGame();

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public Button game = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 120, this.objWidth, this.objHeight, "Game", () -> Game.screen = new ScreenControlsGame());

    public Button camera = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Camera", () -> Game.screen = new ScreenControlsCamera());

    public Button tank = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 0, this.objWidth, this.objHeight, "Tank", () -> Game.screen = new ScreenControlsTank());

    public Button hotbar = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "Hotbar", () -> Game.screen = new ScreenControlsHotbar());

    public Button editor = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 120, this.objWidth, this.objHeight, "Editor", () -> Game.screen = new ScreenControlsEditor());

    Button reset = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 290, this.objWidth, this.objHeight, "Reset", () -> Game.screen = new ScreenResetControls()
    );

    Button back = new Button(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 + 350, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.game.input.save();
        Game.screen = new ScreenOptionsInputDesktop();
    }
    );

    public void update()
    {
        Screen s = Game.screen;
        if (s instanceof ScreenBindInput)
            s = ((ScreenBindInput) s).previous;

        game.enabled = !(s instanceof ScreenControlsGame);
        camera.enabled = !(s instanceof ScreenControlsCamera);
        tank.enabled = !(s instanceof ScreenControlsTank);
        hotbar.enabled = !(s instanceof ScreenControlsHotbar);
        editor.enabled = !(s instanceof ScreenControlsEditor);

        lastControlsScreen = Game.screen;

        game.update();
        camera.update();
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
        Drawing.drawing.setInterfaceFontSize(Game.screen.titleSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 6, Drawing.drawing.interfaceSizeY / 2 - 350, "Controls");

        game.draw();
        camera.draw();
        tank.draw();
        hotbar.draw();
        editor.draw();
        reset.draw();
        back.draw();
    }
}

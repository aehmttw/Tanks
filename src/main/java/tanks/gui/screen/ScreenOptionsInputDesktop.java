package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;

public class ScreenOptionsInputDesktop extends Screen
{
    public static final String mouseTargetText = "Mouse target: ";
    public static final String constrainMouseText = "Constrain mouse: ";

    public static ScreenOverlayControls overlay = new ScreenOverlayControls();

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenOptions()
    );

    Button mouseTarget = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Panel.showMouseTarget = !Panel.showMouseTarget;

            if (Panel.showMouseTarget)
                mouseTarget.setText(mouseTargetText, ScreenOptions.onText);
            else
                mouseTarget.setText(mouseTargetText, ScreenOptions.offText);

            Game.game.window.setShowCursor(!Panel.showMouseTarget);
        }
    },
            "When enabled, your mouse pointer---will be replaced by a target");

    Button constrainMouse = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 60, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.constrainMouse = !Game.constrainMouse;

            if (Game.constrainMouse)
                constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
            else
                constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
        }
    },
            "Disallows your mouse pointer from---leaving the window while playing");


    Button controls = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.objWidth, this.objHeight, "Controls", () -> Game.screen = ScreenOverlayControls.lastControlsScreen
    );

    public ScreenOptionsInputDesktop()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Panel.showMouseTarget)
            mouseTarget.setText(mouseTargetText, ScreenOptions.onText);
        else
            mouseTarget.setText(mouseTargetText, ScreenOptions.offText);

        if (Game.constrainMouse)
            constrainMouse.setText(constrainMouseText, ScreenOptions.onText);
        else
            constrainMouse.setText(constrainMouseText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        mouseTarget.update();
        back.update();
        controls.update();
        constrainMouse.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        constrainMouse.draw();
        mouseTarget.draw();
        back.draw();
        controls.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Input options and controls");
    }

}

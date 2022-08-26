package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.InputSelector;

public class ScreenControlsCamera extends Screen
{
    InputSelector toggleZoom = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Toggle zoom", Game.game.input.zoom);

    InputSelector switchPerspective = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Switch perspective", Game.game.input.perspective);
    InputSelector zoomIn = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Zoom in", Game.game.input.zoomIn);
    InputSelector zoomOut = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Zoom out", Game.game.input.zoomOut);
    InputSelector zoomAuto = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Toggle automatic zoom", Game.game.input.zoomAuto);
    InputSelector yaw = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Camera yaw", Game.game.input.followingCamPitch);
    public ScreenControlsCamera()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        toggleZoom.update();
        switchPerspective.update();
        zoomIn.update();
        zoomOut.update();
        zoomAuto.update();

        if (Game.followingCam || Game.followingCamEnabled)
            yaw.update();

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        toggleZoom.draw();
        switchPerspective.draw();
        zoomOut.draw();
        zoomIn.draw();
        zoomAuto.draw();

        if (Game.followingCam || Game.followingCamEnabled)
            yaw.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Camera controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}

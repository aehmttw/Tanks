package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsGame extends Screen
{
    InputSelector pause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 315, 700, 40, "Pause", Game.game.input.pause);
    InputSelector play = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Play", Game.game.input.play);
    InputSelector toggleZoom = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Toggle zoom", Game.game.input.zoom);
    InputSelector zoomIn = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Zoom in", Game.game.input.zoomIn);
    InputSelector zoomOut = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Zoom out", Game.game.input.zoomOut);
    InputSelector chat = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Chat", Game.game.input.chat);
    InputSelector hidePause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Hide/show pause menu", Game.game.input.hidePause);
    InputSelector fullscreen = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 315, 700, 40, "Toggle fullscreen", Game.game.input.fullscreen);

    public ScreenControlsGame()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        pause.update();
        play.update();
        toggleZoom.update();
        zoomIn.update();
        zoomOut.update();
        chat.update();
        hidePause.update();
        fullscreen.update();

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        fullscreen.draw();
        hidePause.draw();
        chat.draw();
        toggleZoom.draw();
        zoomOut.draw();
        zoomIn.draw();
        play.draw();
        pause.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 400, "Game controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}

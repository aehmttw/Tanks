package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.InputSelector;

public class ScreenControlsGame extends Screen
{
    InputSelector pause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 225, 700, 40, "Pause", Game.game.input.pause);
    InputSelector play = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Play", Game.game.input.play);
    InputSelector chat = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Chat", Game.game.input.chat);
    InputSelector hidePause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Hide/show pause menu", Game.game.input.hidePause);
    InputSelector fullscreen = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Toggle fullscreen", Game.game.input.fullscreen);
    InputSelector screenshot = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 225, 700, 40, "Take screenshot", Game.game.input.screenshot);

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
        chat.update();
        hidePause.update();
        fullscreen.update();
        screenshot.update();

        ScreenOverlayControls.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        screenshot.draw();
        fullscreen.draw();
        hidePause.draw();
        chat.draw();
        play.draw();
        pause.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Game controls");

        ScreenOverlayControls.overlay.draw();
    }

}

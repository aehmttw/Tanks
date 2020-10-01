package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.InputSelector;

public class ScreenControlsGame extends Screen
{
    InputSelector pause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 135, 700, 40, "Pause", Game.game.input.pause);
    InputSelector toggleZoom = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 45, 700, 40, "Toggle zoom", Game.game.input.zoom);
    InputSelector chat = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 45, 700, 40, "Chat", Game.game.input.chat);
    InputSelector hidePause = new InputSelector(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 + 135, 700, 40, "Hide/show pause menu", Game.game.input.hidePause);

    public ScreenControlsGame()
    {
        this.music = "tomato_feast_1_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        pause.update();
        toggleZoom.update();
        chat.update();
        hidePause.update();

        ScreenOptionsInputDesktop.overlay.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        hidePause.draw();
        chat.draw();
        toggleZoom.draw();
        pause.draw();


        Drawing.drawing.setInterfaceFontSize(24);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX * 2 / 3, Drawing.drawing.interfaceSizeY / 2 - 350, "Game controls");

        ScreenOptionsInputDesktop.overlay.draw();
    }

}

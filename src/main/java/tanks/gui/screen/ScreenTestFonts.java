package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTestFonts extends Screen
{
    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenDebug()
    );

    public String boxText = "Draw box: ";

    Button box = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.game.window.fontRenderer.drawBox = !Game.game.window.fontRenderer.drawBox;

            if (Game.game.window.fontRenderer.drawBox)
                box.setText(boxText, ScreenOptions.onText);
            else
                box.setText(boxText, ScreenOptions.offText);
        }
    });

    public ScreenTestFonts()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.game.window.fontRenderer.drawBox)
            box.setText(boxText, ScreenOptions.onText);
        else
            box.setText(boxText, ScreenOptions.offText);
    }

    @Override
    public void update()
    {
        back.update();
        box.update();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 300, "Font alignment test");

        Drawing.drawing.setColor(80, 80, 80);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1300, 40);

        Drawing.drawing.setColor(120, 120, 120);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1300, 20);

        Drawing.drawing.setColor(160, 160, 160);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY, 1300, 4);

        Drawing.drawing.setColor(255, 0, 0);
        Drawing.drawing.fillInterfaceRect(this.centerX, this.centerY - 150, 4, 4);
        Drawing.drawing.setColor(255, 255, 255, 127);
        Drawing.drawing.setInterfaceFontSize(100);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - 150, "0");

        for (int i = 4; i < 66; i += 2)
        {
            Drawing.drawing.setColor(255, 0, 0);
            Drawing.drawing.fillInterfaceRect(this.centerX + (i - 35) * 20, this.centerY, 4, 4);
            Drawing.drawing.setColor(255, 255, 255, 127);
            Drawing.drawing.setInterfaceFontSize(i);
            Drawing.drawing.drawInterfaceText(this.centerX + (i - 35) * 20, this.centerY, "0");
        }

        back.draw();
        box.draw();
    }
}

package tanks.gui.screen;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenTestRainbow extends Screen
{
    public static final float rect_count = 80;
    public static int lastHovered = -1;

    public Button back = new Button(this.centerX, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTestDebug());

    public ScreenTestRainbow()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        if (Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1))
            lastHovered = -1;

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3, "Rainbow test");

        for (int i = 0; i < rect_count; i++)
        {
            double x = this.centerX + (i - rect_count / 2) * (1000 / rect_count);
            double[] col = Game.getRainbowColor(i / rect_count);

            double mx = Drawing.drawing.getInterfaceMouseX();
            double my = Drawing.drawing.getInterfaceMouseY();

            if (lessThan(x - 500 / rect_count, mx, x + 500 / rect_count) && lessThan(this.centerY - 75, my, this.centerY))
                lastHovered = i;

            if (lastHovered == i)
            {
                Drawing.drawing.setColor(0, 0, 0, 75);
                Drawing.drawing.setInterfaceFontSize(16);
                Drawing.drawing.drawPopup(x, this.centerY + 75, 200, 80, 10, 5);
                Drawing.drawing.displayUncenteredInterfaceText(x - 80, this.centerY + 55, "RGB: (%.0f, %.0f, %.0f)", col[0], col[1], col[2]);
                Drawing.drawing.displayUncenteredInterfaceText(x - 80, this.centerY + 80, "Value: %.2f", i / rect_count);

                for (int j = 0; j < 3; j++)
                {
                    if (col[j] > 50)
                        col[j] -= 50;
                }
            }

            Drawing.drawing.setColor(col[0], col[1], col[2]);
            Drawing.drawing.fillRect(this, x, this.centerY - 25, 1000 / rect_count, 75);
        }

        back.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawRect(this.centerX - 6, this.centerY - 25, 1000, 75);
    }

    private static boolean lessThan(double a, double b, double c)
    {
        return (a <= b && b <= c) || (c <= b && b <= a);
    }
}

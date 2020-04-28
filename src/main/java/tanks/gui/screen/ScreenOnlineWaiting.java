package tanks.gui.screen;

import tanks.Drawing;
import tanks.Panel;

public class ScreenOnlineWaiting extends Screen
{
    public double age = 0;

    double size;
    double size2;
    double size3;

    @Override
    public void update()
    {
        age += Panel.frameFrequency;

        size = Math.min(1, age / 50);
        size2 = Math.min(1, Math.max(0, age / 50 - 0.25));
        size3 = Math.min(1, Math.max(0, age / 50 - 0.5));
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        if (age > 50)
        {
            Drawing.drawing.setColor(0, 0, 0, Math.min(255, (age / 100) * 255 - 50));
            Drawing.drawing.setInterfaceFontSize(24);
            Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 90, "Waiting for server...");
        }

        Drawing.drawing.setColor(0, 0, 0);
        drawSpinny(100, 4, 0.3, 45 * size, 1 * size);
        drawSpinny(99, 3, 0.5, 30 * size2, 0.75 * size2);
        drawSpinny(100, 2, 0.7, 15 * size3, 0.5 * size3);
    }

    public void drawSpinny(int max, int parts, double speed, double size, double dotSize)
    {
        for (int i = 0; i < max; i++)
        {
            double frac = (System.currentTimeMillis() / 1000.0 * speed + i / 100.0) % 1;
            double s = (i % (max * 1.0 / parts)) / 10.0 * parts;
            Drawing.drawing.fillOval(Drawing.drawing.interfaceSizeX / 2 + size * Math.cos(frac * Math.PI * 2),
                    Drawing.drawing.interfaceSizeY / 2 - 25 + size * Math.sin(frac * Math.PI * 2),
                    s * dotSize, s * dotSize);
        }
    }
}

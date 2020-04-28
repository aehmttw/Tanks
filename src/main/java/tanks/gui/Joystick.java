package tanks.gui;

import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

public class Joystick implements IDrawable
{
    public double basePosX;
    public double basePosY;

    public double posX;
    public double posY;
    public double size;

    public double inputAngle;
    public double inputIntensity;

    public double rawIntensity;
    public double prevIntensity;

    public double[] clickIntensities = new double[]{0.2};
    public String[] clickVibrations = new String[]{"selectionChanged"};

    public double colorR = 0;
    public double colorG = 150;
    public double colorB = 255;
    public double colorA = 127;

    public int activeInput = -1;
    public int prevInput = -1;

    public boolean snap = false;
    public boolean mobile = true;

    public String name = "joystick";
    public int domain = 0;

    public Joystick(double x, double y, double size)
    {
        this.basePosX = x;
        this.basePosY = y;
        this.posX = x;
        this.posY = y;
        this.size = size;
    }

    public void update()
    {
        this.prevInput = this.activeInput;
        prevIntensity = rawIntensity;
        inputIntensity = 0;

        if (!Game.game.window.touchPoints.containsKey(activeInput) || ScreenGame.finished)
        {
            if (snap || this.domain > 0)
            {
                this.posX = this.basePosX;
                this.posY = this.basePosY;
            }

            this.activeInput = -1;
        }

        for (int i: Game.game.window.touchPoints.keySet())
        {
            InputPoint p = Game.game.window.touchPoints.get(i);
            double px = Drawing.drawing.getInterfacePointerX(p.x);
            double py = Drawing.drawing.getInterfacePointerY(p.y);

            double distSq = Math.pow(px - this.posX, 2) + Math.pow(py - this.posY, 2);
            if (!ScreenGame.finished && (p.tag.equals("") &&
                    (distSq <= Math.pow(this.size / 2 * 1.4, 2) || this.domain == 1 && px < Drawing.drawing.interfaceSizeX / 2 || this.domain == 2 && px >= Drawing.drawing.interfaceSizeX / 2))
                    || (this.activeInput == i && p.tag.equals(this.name)))
            {
                if (this.activeInput == -1 && (this.snap || this.mobile || this.domain > 0))
                {
                    this.posX = px;
                    this.posY = py;
                    distSq = 0;
                }

                p.tag = this.name;
                this.activeInput = i;

                double dx = px - this.posX;
                double dy = py - this.posY;

                double angle = 0;
                if (dx > 0)
                    angle = Math.atan(dy/dx);
                else if (dx < 0)
                    angle = Math.atan(dy/dx) + Math.PI;
                else
                {
                    if (dy > 0)
                        angle = Math.PI / 2;
                    else if (dy < 0)
                        angle = Math.PI * 3 / 2;
                }

                this.inputAngle = (Math.PI * 2 + angle) % (Math.PI * 2);

                double dist = Math.sqrt(distSq);

                if (dist > size / 2 * 1.4 && this.mobile)
                {
                    this.posX += Math.cos(this.inputAngle) * (dist - size / 2 * 1.4);
                    this.posY += Math.sin(this.inputAngle) * (dist - size / 2 * 1.4);
                }

                this.inputIntensity = Math.min(1, 2 * dist / size);
                this.rawIntensity = 2 * dist / size;
            }
        }

        if (this.activeInput != -1 && this.prevInput != -1)
        {
            for (int i = 0; i < clickIntensities.length; i++)
            {
                double clickIntensity = clickIntensities[i];
                if ((prevIntensity > clickIntensity && rawIntensity <= clickIntensity) || (prevIntensity <= clickIntensity && rawIntensity > clickIntensity))
                    Drawing.drawing.playVibration(clickVibrations[i]);
            }
        }
    }

    @Override
    public void draw()
    {
        double alpha = 1;
        if (activeInput == -1)
            alpha = 0.5;

        double frac = Obstacle.draw_size / Obstacle.obstacle_size;

        Drawing.drawing.setColor(colorR, colorG, colorB, colorA * alpha);
        Drawing.drawing.fillInterfaceOval(this.posX, this.posY, this.size * frac, this.size * frac);

        double x = posX + inputIntensity * Math.cos(inputAngle) * size / 2;
        double y = posY + inputIntensity * Math.sin(inputAngle) * size / 2;
        Drawing.drawing.fillInterfaceOval(x, y, this.size / 4 * frac, this.size / 4 * frac);
    }
}

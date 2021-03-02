package tanks.bullet;

import tanks.*;

public class Laser implements IDrawable
{
    public double backX;
    public double backY;
    public double backZ;

    public double frontX;
    public double frontY;
    public double frontZ;

    public double age = 0;
    public double maxAge = 20;

    public boolean expired = false;

    public double width;
    public double angle;

    public double colorR;
    public double colorG;
    public double colorB;

    public boolean frontCircle = true;
    public boolean backCircle = true;
    public boolean showOutsides = true;

    public Laser(double backX, double backY, double backZ, double frontX, double frontY, double frontZ, double width, double angle, double colR, double colG, double colB)
    {
        this.angle = angle;
        this.backX = backX;
        this.backY = backY;
        this.backZ = backZ;
        this.frontX = frontX;
        this.frontY = frontY;
        this.frontZ = frontZ;
        this.width = width;

        this.colorR = colR;
        this.colorG = colG;
        this.colorB = colB;
    }

    @Override
    public void draw()
    {
        double ox = Math.cos(this.angle + Math.PI / 2);
        double oy = Math.sin(this.angle + Math.PI / 2);

        double frac = this.age / this.maxAge;

        boolean depth = Game.enable3d;

        if (!expired)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);

            if (Game.framework == Game.Framework.swing)
            {
                double dist = Math.sqrt(Math.pow(this.frontX - this.backX, 2) + Math.pow(this.frontY - this.backY, 2));

                for (int i = 0; i < dist; i++)
                {
                    double f = i / dist;
                    Drawing.drawing.fillOval(this.frontX * f + this.backX * (1 - f), this.frontY * f + this.backY * (1 - f), (1 - frac) * 2 * width, (1 - frac) * 2 * width);
                }
            }
            else
            {
                if (frontCircle || showOutsides)
                {
                    Game.game.window.setBatchMode(true, false, depth);

                    for (int i = 10; i < 30; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                        Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                        Drawing.drawing.addVertex(this.frontX, this.frontY, this.frontZ);
                    }

                    Game.game.window.setBatchMode(false, false, depth);
                }

                Game.game.window.setBatchMode(true, true, depth);

                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, ox * width * (1 - frac), oy * width * (1 - frac), 0);
                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, -ox * width * (1 - frac), -oy * width * (1 - frac), 0);

                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, -ox * width * (1 - frac), -oy * width * (1 - frac), 0);
                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, ox * width * (1 - frac), oy * width * (1 - frac), 0);

                Game.game.window.setBatchMode(false, true, depth);

                if (backCircle || showOutsides)
                {
                    Game.game.window.setBatchMode(true, false, depth);

                    for (int i = 30; i < 50; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                        Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                        Drawing.drawing.addVertex(this.backX, this.backY, this.backZ);
                    }

                    Game.game.window.setBatchMode(false, false, depth);
                }
            }
        }

        if (Game.superGraphics)
            drawGlow();
    }

    public void drawGlow()
    {
        double ox = Math.cos(this.angle + Math.PI / 2);
        double oy = Math.sin(this.angle + Math.PI / 2);

        double frac = this.age / this.maxAge;
        double mul = 4;

        boolean depth = Game.enable3d;

        if (!expired)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);

            if (frontCircle || showOutsides)
            {
                Game.game.window.setBatchMode(true, false, depth, true);

                for (int i = 10; i < 30; i++)
                {
                    Drawing.drawing.setColor(0, 0, 0, 255, 1);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
                    Drawing.drawing.addVertex(this.frontX, this.frontY, this.frontZ);
                }

                Game.game.window.setBatchMode(false, false, depth, true);
            }

            Game.game.window.setBatchMode(true, true, depth, true);

            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, 0, 0, 0);
            Drawing.drawing.setColor(0, 0, 0, 255, 1);
            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * -ox * width * (1 - frac), mul * -oy * width * (1 - frac), 0);
            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * -ox * width * (1 - frac), mul * -oy * width * (1 - frac), 0);
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, 0, 0, 0);

            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, 0, 0, 0);
            Drawing.drawing.setColor(0, 0, 0, 255, 1);
            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * ox * width * (1 - frac), mul * oy * width * (1 - frac), 0);
            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * ox * width * (1 - frac), mul * oy * width * (1 - frac), 0);
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, 0, 0, 0);

            Game.game.window.setBatchMode(false, true, depth, true);

            if (backCircle || showOutsides)
            {
                Game.game.window.setBatchMode(true, false, depth, true);

                for (int i = 30; i < 50; i++)
                {
                    Drawing.drawing.setColor(0, 0, 0, 255, 1);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
                    Drawing.drawing.addVertex(this.backX, this.backY, this.backZ);
                }

                Game.game.window.setBatchMode(false, false, depth, true);
            }
        }
    }
}

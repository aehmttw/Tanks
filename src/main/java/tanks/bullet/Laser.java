package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class Laser extends Movable implements IDrawableWithGlow
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

    public boolean glows = true;

    public Tank tank1;
    public Tank tank2;

    public Laser(double backX, double backY, double backZ, double frontX, double frontY, double frontZ, double width, double angle, double colR, double colG, double colB)
    {
        super(backX, backY);

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

        this.drawLevel = 2;
    }

    @Override
    public void draw()
    {
        if (Game.movables.contains(this))
        {
            if (this.tank1 == null || this.tank2 == null || this.tank1.destroy || this.tank2.destroy || ScreenGame.finishedQuick)
                Game.removeMovables.add(this);
        }

        double ox = Math.cos(this.angle + Math.PI / 2);
        double oy = Math.sin(this.angle + Math.PI / 2);

        double frac = this.age / this.maxAge;

        boolean depth = Game.enable3d;

        if (!expired)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);

            if (frontCircle || showOutsides)
            {
                Game.game.window.shapeRenderer.setBatchMode(true, false, depth);

                for (int i = 10; i < 30; i++)
                {
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addVertex(this.frontX, this.frontY, this.frontZ);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, false, depth);
            }

            Game.game.window.shapeRenderer.setBatchMode(true, true, depth);

            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, ox * width * (1 - frac), oy * width * (1 - frac), 0);
            Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, -ox * width * (1 - frac), -oy * width * (1 - frac), 0);

            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, -ox * width * (1 - frac), -oy * width * (1 - frac), 0);
            Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, ox * width * (1 - frac), oy * width * (1 - frac), 0);

            Game.game.window.shapeRenderer.setBatchMode(false, true, depth);

            if (backCircle || showOutsides)
            {
                Game.game.window.shapeRenderer.setBatchMode(true, false, depth);

                for (int i = 30; i < 50; i++)
                {
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addVertex(this.backX, this.backY, this.backZ);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, false, depth);
            }
        }

        if (Game.glowEnabled)
            drawGlow();
    }

    public void drawGlow()
    {
        if (!glows)
            return;

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
                Game.game.window.shapeRenderer.setBatchMode(true, false, depth, true);

                for (int i = 10; i < 30; i++)
                {
                    Drawing.drawing.setColor(0, 0, 0, 255, 1);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.frontZ, mul * Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
                    Drawing.drawing.addVertex(this.frontX, this.frontY, this.frontZ);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, false, depth, true);
            }

            Game.game.window.shapeRenderer.setBatchMode(true, true, depth, true);

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

            Game.game.window.shapeRenderer.setBatchMode(false, true, depth, true);

            if (backCircle || showOutsides)
            {
                Game.game.window.shapeRenderer.setBatchMode(true, false, depth, true);

                for (int i = 30; i < 50; i++)
                {
                    Drawing.drawing.setColor(0, 0, 0, 255, 1);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * Math.cos(i / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin(i / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.backZ, mul * Math.cos((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), mul * Math.sin((i + 1) / 20.0 * Math.PI + angle) * width * (1 - frac), 0);
                    Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, 255, 1);
                    Drawing.drawing.addVertex(this.backX, this.backY, this.backZ);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, false, depth, true);
            }
        }
    }

    @Override
    public boolean isGlowEnabled()
    {
        return true;
    }
}

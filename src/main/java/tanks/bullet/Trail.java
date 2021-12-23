package tanks.bullet;

import tanks.*;
import tanks.gui.screen.ScreenGame;

public class Trail implements IDrawable
{
    public double backX;
    public double backY;

    public double frontX;
    public double frontY;

    public double currentLength;
    public double angle;

    public double maxLength;
    public double age;
    public double delay;

    public boolean spawning = true;
    public boolean expired = false;

    public double backWidth;
    public double frontWidth;

    public double frontR;
    public double frontG;
    public double frontB;
    public double frontA;

    public double backR;
    public double backG;
    public double backB;
    public double backA;

    public boolean glow;
    public double luminosity;

    public boolean frontCircle = true;
    public boolean backCircle = true;
    public boolean showOutsides = true;

    public double speed;

    public Movable movable;

    public Trail(Movable m, double backX, double backY, double backWidth, double frontWidth, double length, double angle,
                 double frontR, double frontG, double frontB, double frontA,
                 double backR, double backG, double backB, double backA,
                 boolean glow, double luminosity)
    {
        this.movable = m;
        this.speed = Math.sqrt(m.vX * m.vX + m.vY * m.vY);

        this.angle = angle;
        this.backX = backX;
        this.backY = backY;
        this.frontX = backX;
        this.frontY = backY;
        this.backWidth = backWidth;
        this.frontWidth = frontWidth;
        this.maxLength = length;

        this.frontR = frontR;
        this.frontG = frontG;
        this.frontB = frontB;
        this.frontA = frontA;

        this.backR = backR;
        this.backG = backG;
        this.backB = backB;
        this.backA = backA;
        this.luminosity = luminosity;

        this.glow = glow;
    }

    public double update(double trailLength)
    {
        if (this.movable.destroy)
            this.age += speed * Panel.frameFrequency;
        else
            this.age = trailLength;

        if (this.spawning)
        {
            this.frontX = movable.posX;
            this.frontY = movable.posY;
            this.currentLength = Math.sqrt(Math.pow(frontY - backY, 2) + Math.pow(frontX - backX, 2));
        }

        if (this.age - delay > this.maxLength || ScreenGame.finishTimer <= 0)
            this.expired = true;

        return this.currentLength;
    }

    @Override
    public void draw()
    {
        double opacity = ScreenGame.finishTimer / ScreenGame.finishTimerMax;

        double ox = Math.cos(this.angle + Math.PI / 2);
        double oy = Math.sin(this.angle + Math.PI / 2);

        double frac1 = (this.age - delay) / this.maxLength;
        double frac2 = (this.age + this.currentLength - delay) / this.maxLength;

        double frontWidth = this.backWidth * frac1 + this.frontWidth * (1 - frac1);
        double backWidth = this.backWidth * frac2 + this.frontWidth * (1 - frac2);

        if (delay - age > currentLength)
            return;

        boolean depth = Game.enable3d;

        if (!expired)
        {
            if (frac1 >= 0)
            {
                Drawing.drawing.setColor(
                        this.frontR * (1 - frac1) + this.backR * frac1,
                        this.frontG * (1 - frac1) + this.backG * frac1,
                        this.frontB * (1 - frac1) + this.backB * frac1,
                        (this.frontA * (1 - frac1) + this.backA * frac1) * opacity, this.luminosity);

                if (frontCircle || showOutsides)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                    for (int i = 30; i < 50; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * frontWidth, Math.sin(i / 20.0 * Math.PI + angle) * frontWidth, 0);
                        Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * frontWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * frontWidth, 0);
                        Drawing.drawing.addVertex(this.frontX, this.frontY, this.movable.posZ - 1);
                    }

                    Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                }

                Game.game.window.shapeRenderer.setBatchMode(true, true, depth, this.glow, false);

                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, ox * frontWidth, oy * frontWidth, 0);
                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, -ox * frontWidth, -oy * frontWidth, 0);
            }
            else
            {
                frontWidth = this.frontWidth;
                Drawing.drawing.setColor(this.frontR, this.frontG, this.frontB, this.frontA * opacity, this.luminosity);
                double frac3 = (delay - this.age) / this.currentLength;

                if (frontCircle)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                    for (int i = 30; i < 50; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3),  this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * frontWidth, Math.sin(i / 20.0 * Math.PI + angle) * frontWidth, 0);
                        Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3),  this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * frontWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * frontWidth, 0);
                        Drawing.drawing.addVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1);
                    }

                    Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                }

                Game.game.window.shapeRenderer.setBatchMode(true, true, depth, this.glow, false);

                Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1,ox * frontWidth, oy * frontWidth, 0);
                Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1,-ox * frontWidth, -oy * frontWidth, 0);
            }

            if (frac2 <= 1)
            {
                Drawing.drawing.setColor(
                        this.frontR * (1 - frac2) + this.backR * frac2,
                        this.frontG * (1 - frac2) + this.backG * frac2,
                        this.frontB * (1 - frac2) + this.backB * frac2,
                        (this.frontA * (1 - frac2) + this.backA * frac2) * opacity, this.luminosity);

                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, -ox * backWidth, -oy * backWidth, 0);
                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, ox * backWidth, oy * backWidth, 0);

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle || showOutsides)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                    for (int i = 10; i < 30; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * backWidth, Math.sin(i / 20.0 * Math.PI + angle) * backWidth, 0);
                        Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * backWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * backWidth, 0);
                        Drawing.drawing.addVertex(this.backX, this.backY, this.movable.posZ - 1);
                    }

                    Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                }
            }
            else
            {
                backWidth = this.backWidth;
                Drawing.drawing.setColor(this.backR, this.backG, this.backB, this.backA * opacity, this.luminosity);

                double frac3 = (this.age + this.currentLength - this.maxLength - delay) / this.currentLength;
                Drawing.drawing.addFacingVertex(this.frontX * frac3 + this.backX * (1 - frac3), this.frontY * frac3 + this.backY * (1 - frac3), this.movable.posZ - 1, -ox * backWidth, -oy * backWidth, 0);
                Drawing.drawing.addFacingVertex(this.frontX * frac3 + this.backX * (1 - frac3), this.frontY * frac3 + this.backY * (1 - frac3), this.movable.posZ - 1, ox * backWidth, oy * backWidth, 0);

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                    for (int i = 10; i < 30; i++)
                    {
                        Drawing.drawing.addFacingVertex(this.frontX * frac3 + this.backX * (1 - frac3), this.frontY * frac3 + this.backY * (1 - frac3), this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * backWidth, Math.sin(i / 20.0 * Math.PI + angle) * backWidth, 0);
                        Drawing.drawing.addFacingVertex(this.frontX * frac3 + this.backX * (1 - frac3), this.frontY * frac3 + this.backY * (1 - frac3), this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * backWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * backWidth, 0);
                        Drawing.drawing.addVertex(this.frontX * frac3 + this.backX * (1 - frac3), this.frontY * frac3 + this.backY * (1 - frac3), this.movable.posZ - 1);
                    }

                    Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                }
            }

            Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);
        }
    }
}

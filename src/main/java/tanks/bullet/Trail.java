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
    protected double frontAngleOffset;

    public double backR;
    public double backG;
    public double backB;
    public double backA;
    protected double backAngleOffset;

    public boolean glow;
    public double luminosity;

    public boolean frontCircle = true;
    public boolean backCircle = true;
    public boolean showOutsides = true;
    public boolean showOutsideFront = true;
    public boolean showOutsideBack = true;

    public double speed;

    public Movable movable;

    public Trail(Movable m, double speed, double backX, double backY, double backWidth, double frontWidth, double length, double angle,
                 double frontR, double frontG, double frontB, double frontA,
                 double backR, double backG, double backB, double backA,
                 boolean glow, double luminosity)
    {
        this.movable = m;
        this.speed = speed;

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

    public void setFrontAngleOffset(double offset)
    {
        this.frontCircle = false;
        this.showOutsideFront = false;
        this.frontAngleOffset = offset;
    }

    public void setBackAngleOffset(double offset)
    {
        this.backCircle = false;
        this.showOutsideBack = false;
        this.backAngleOffset = offset;
    }

    @Override
    public void draw()
    {
        double opacity = ScreenGame.finishTimer / ScreenGame.finishTimerMax;

        double ox = Math.cos(this.angle + Math.PI / 2);
        double oy = Math.sin(this.angle + Math.PI / 2);

        double oxFront = Math.cos(this.angle + this.frontAngleOffset + Math.PI / 2);
        double oyFront = Math.sin(this.angle + this.frontAngleOffset + Math.PI / 2);

        double oxBack = Math.cos(this.angle + this.backAngleOffset + Math.PI / 2);
        double oyBack = Math.sin(this.angle + this.backAngleOffset + Math.PI / 2);

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

                if (frontCircle || (showOutsides && showOutsideFront))
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

                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, oxFront * frontWidth, oyFront * frontWidth, 0);
                Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, -oxFront * frontWidth, -oyFront * frontWidth, 0);
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

                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, -oxBack * backWidth, -oyBack * backWidth, 0);
                Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, oxBack * backWidth, oyBack * backWidth, 0);

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle || (showOutsides && showOutsideBack))
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

    public void drawTube(double frontX, double frontY, double frontWidth, double frontR, double frontG, double frontB, double frontA, double backX, double backY, double backWidth, double backR, double backG, double backB, double backA)
    {
        Game.game.window.shapeRenderer.setBatchMode(true, true, Game.enable3d, this.glow, false);

        int polyCount = 20;
        for (int i = 0; i < polyCount - 1; i++)
        {
            double angle1 = Math.PI * 2 * i / polyCount;
            double angle2 = Math.PI * 2 * (i + 1) / polyCount;
            double vert1 = Math.sin(angle1);
            double vert2 = Math.sin(angle2);
            double hor1 = Math.cos(angle1);
            double hor2 = Math.cos(angle2);
            double z1 = vert1 + this.movable.posZ;
            double z2 = vert2 + this.movable.posZ;
            double oxFront = frontWidth * Math.cos(this.angle + this.frontAngleOffset + Math.PI / 2);
            double oyFront = frontWidth * Math.sin(this.angle + this.frontAngleOffset + Math.PI / 2);
            double oxBack = backWidth * Math.cos(this.angle + this.backAngleOffset + Math.PI / 2);
            double oyBack = backWidth * Math.sin(this.angle + this.backAngleOffset + Math.PI / 2);
            double frontX1 = frontX + oxFront * hor1;
            double frontX2 = frontX + oxFront * hor2;
            double frontY1 = frontY + oyFront * hor1;
            double frontY2 = frontY + oyFront * hor2;
            double backX1 = backX + oxBack * hor1;
            double backX2 = backX + oxBack * hor2;
            double backY1 = backY + oyBack * hor1;
            double backY2 = backY + oyBack * hor2;

            Drawing.drawing.setColor(frontR, frontG, frontB, frontA);
            Game.game.window.addVertex(frontX1, frontY1, z1);
            Drawing.drawing.setColor(backR, backG, backB, backA);
            Game.game.window.addVertex(backX1, backY1, z1);
            Game.game.window.addVertex(backX2, backY2, z2);
            Drawing.drawing.setColor(frontR, frontG, frontB, frontA);
            Game.game.window.addVertex(frontX2, frontY2, z2);
        }

        Game.game.window.shapeRenderer.setBatchMode(false, true, Game.enable3d, this.glow, false);
    }

    public void drawCap(double frontX, double frontY, double frontWidth, double angleOffset)
    {
        Game.game.window.shapeRenderer.setBatchMode(true, false, Game.enable3d, this.glow, false);

        int outerPolyCount = 20;

        for (int o = 0; o < outerPolyCount; o++)
        {
            int polyCount = (int) Math.ceil(20 * (1.0 - (1.0 * o / outerPolyCount)));
            int polyCount2 = (int) Math.ceil(20 * (1.0 - (1.0 * (o + 1) / outerPolyCount)));

            for (int i = 0; i < polyCount - 1; i++)
            {
                double angle1out = Math.PI * 2 * i / polyCount;
                double angle2out = Math.PI * 2 * (i + 1) / polyCount;

                int iIn = (int) (polyCount2 * (i * 1.0 / polyCount));
                int i2In = (int) (polyCount2 * ((i + 1.0) / polyCount));

                double angle1in = Math.PI * 2 * iIn / polyCount;
                double angle2in = Math.PI * 2 * i2In / polyCount;

                double vert1o = Math.sin(angle1out);
                double vert2o = Math.sin(angle2out);
                double hor1o = Math.cos(angle1out);
                double hor2o = Math.cos(angle2out);

                double vert1i = Math.sin(angle1in);
                double vert2i = Math.sin(angle2in);
                double hor1i = Math.cos(angle1in);
                double hor2i = Math.cos(angle2in);

                double z1o = vert1o + this.movable.posZ;
                double z2o = vert2o + this.movable.posZ;
                double z1i = vert1i + this.movable.posZ;
                double z2i = vert2i + this.movable.posZ;

                double ox = frontWidth * Math.cos(this.angle + angleOffset + Math.PI / 2);
                double oy = frontWidth * Math.sin(this.angle + angleOffset + Math.PI / 2);

                double outX1 = frontX + ox * hor1o;
                double outX2 = frontX + ox * hor2o;
                double outY1 = frontY + oy * hor1o;
                double outY2 = frontY + oy * hor2o;

                double inX1 = frontX + ox * hor1i;
                double inX2 = frontX + ox * hor2i;
                double inY1 = frontY + oy * hor1i;
                double inY2 = frontY + oy * hor2i;

                Game.game.window.addVertex(outX1, outY1, z1o);
                Game.game.window.addVertex(outX2, outY2, z2o);
                Game.game.window.addVertex(inX1, inY1, z1i);

                if (iIn != i2In)
                {
                    Game.game.window.addVertex(inX1, inY1, z1i);
                    Game.game.window.addVertex(inX2, inY2, z2i);
                    Game.game.window.addVertex(outX2, outY2, z2o);
                }
            }
        }

        Game.game.window.shapeRenderer.setBatchMode(false, false, Game.enable3d, this.glow, false);
    }
}

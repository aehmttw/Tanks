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

    public boolean trail3d = Game.followingCam;

    public double speed;

    public Movable movable;

    public Trail(Movable m, double speed, double backX, double backY,
                 double delay, double backWidth, double frontWidth, double length, double angle,
                 double frontR, double frontG, double frontB, double frontA,
                 double backR, double backG, double backB, double backA,
                 boolean glow, double luminosity, boolean frontCircle, boolean backCircle)
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
        this.delay = delay;

        this.frontR = frontR;
        this.frontG = frontG;
        this.frontB = frontB;
        this.frontA = frontA;

        this.backR = backR;
        this.backG = backG;
        this.backB = backB;
        this.backA = backA;
        this.luminosity = luminosity;

        this.frontCircle = frontCircle;
        this.backCircle = backCircle;

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
            double frac3 = (delay - this.age) / this.currentLength;

            if (frac1 >= 0)
            {
                frac3 = 0;
                Drawing.drawing.setColor(
                        this.frontR * (1 - frac1) + this.backR * frac1,
                        this.frontG * (1 - frac1) + this.backG * frac1,
                        this.frontB * (1 - frac1) + this.backB * frac1,
                        (this.frontA * (1 - frac1) + this.backA * frac1) * opacity, this.luminosity);

                if (frontCircle || (showOutsides && showOutsideFront))
                {
                    if (!trail3d)
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
                    else
                        drawCap3D(this.frontX, this.frontY, frontWidth, false);
                }

                if (!trail3d)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, true, depth, this.glow, false);

                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, oxFront * frontWidth, oyFront * frontWidth, 0);
                    Drawing.drawing.addFacingVertex(this.frontX, this.frontY, this.movable.posZ - 1, -oxFront * frontWidth, -oyFront * frontWidth, 0);
                }
            }
            else
            {
                frontWidth = this.frontWidth;
                Drawing.drawing.setColor(this.frontR, this.frontG, this.frontB, this.frontA * opacity, this.luminosity);

                if (frontCircle)
                {
                    if (!trail3d)
                    {
                        Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                        for (int i = 30; i < 50; i++)
                        {
                            Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * frontWidth, Math.sin(i / 20.0 * Math.PI + angle) * frontWidth, 0);
                            Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * frontWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * frontWidth, 0);
                            Drawing.drawing.addVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1);
                        }

                        Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                    }
                    else
                        drawCap3D(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), frontWidth, false);

                }

                if (!trail3d)
                {
                    Game.game.window.shapeRenderer.setBatchMode(true, true, depth, this.glow, false);

                    Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1, ox * frontWidth, oy * frontWidth, 0);
                    Drawing.drawing.addFacingVertex(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.movable.posZ - 1, -ox * frontWidth, -oy * frontWidth, 0);
                }
            }

            double r = Drawing.drawing.currentColorR;
            double g = Drawing.drawing.currentColorG;
            double b = Drawing.drawing.currentColorB;
            double a = Drawing.drawing.currentColorA;

            double frac4 = (this.age + this.currentLength - this.maxLength - delay) / this.currentLength;
            if (frac2 <= 1)
            {
                frac4 = 0;
                Drawing.drawing.setColor(
                        this.frontR * (1 - frac2) + this.backR * frac2,
                        this.frontG * (1 - frac2) + this.backG * frac2,
                        this.frontB * (1 - frac2) + this.backB * frac2,
                        (this.frontA * (1 - frac2) + this.backA * frac2) * opacity, this.luminosity);

                if (!trail3d)
                {
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, -oxBack * backWidth, -oyBack * backWidth, 0);
                    Drawing.drawing.addFacingVertex(this.backX, this.backY, this.movable.posZ - 1, oxBack * backWidth, oyBack * backWidth, 0);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle || (showOutsides && showOutsideBack))
                {
                    if (trail3d)
                        drawCap3D(this.backX, this.backY, backWidth, true);
                    else
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
            }
            else
            {
                backWidth = this.backWidth;
                Drawing.drawing.setColor(this.backR, this.backG, this.backB, this.backA * opacity, this.luminosity);

                if (!trail3d)
                {
                    Drawing.drawing.addFacingVertex(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.movable.posZ - 1, -ox * backWidth, -oy * backWidth, 0);
                    Drawing.drawing.addFacingVertex(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.movable.posZ - 1, ox * backWidth, oy * backWidth, 0);
                }

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle)
                {
                    if (trail3d)
                        drawCap3D(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), backWidth, true);
                    else
                    {
                        Game.game.window.shapeRenderer.setBatchMode(true, false, depth, this.glow, false);

                        for (int i = 10; i < 30; i++)
                        {
                            Drawing.drawing.addFacingVertex(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.movable.posZ - 1, Math.cos(i / 20.0 * Math.PI + angle) * backWidth, Math.sin(i / 20.0 * Math.PI + angle) * backWidth, 0);
                            Drawing.drawing.addFacingVertex(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.movable.posZ - 1, Math.cos((i + 1) / 20.0 * Math.PI + angle) * backWidth, Math.sin((i + 1) / 20.0 * Math.PI + angle) * backWidth, 0);
                            Drawing.drawing.addVertex(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.movable.posZ - 1);
                        }

                        Game.game.window.shapeRenderer.setBatchMode(false, false, depth, this.glow, false);
                    }
                }
            }

            Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

            if (trail3d)
                drawTube3D(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3),
                        frontWidth, frac1 >= 0 ? frontAngleOffset : 0, r, g, b, a,
                        this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4),
                        backWidth, frac2 <= 1 ? backAngleOffset : 0, Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA);
        }
    }

    public void drawTube3D(double frontX, double frontY, double frontWidth, double frontAngleOffset, double frontR, double frontG, double frontB, double frontA, double backX, double backY, double backWidth, double backAngleOffset, double backR, double backG, double backB, double backA)
    {
        Game.game.window.shapeRenderer.setBatchMode(true, true, Game.enable3d, this.glow, false);

        int polyCount = 20;
        for (int i = 0; i < polyCount; i++)
        {
            double angle1 = Math.PI * 2 * i / polyCount;
            double angle2 = Math.PI * 2 * (i + 1) / polyCount;
            double vert1 = Math.sin(angle1);
            double vert2 = Math.sin(angle2);
            double hor1 = Math.cos(angle1);
            double hor2 = Math.cos(angle2);
            double z1Front = vert1 * frontWidth + this.movable.posZ;
            double z2Front = vert2 * frontWidth + this.movable.posZ;
            double z1Back = vert1 * backWidth + this.movable.posZ;
            double z2Back = vert2 * backWidth + this.movable.posZ;
            double oxFront = frontWidth * Math.cos(this.angle + frontAngleOffset + Math.PI / 2);
            double oyFront = frontWidth * Math.sin(this.angle + frontAngleOffset + Math.PI / 2);
            double oxBack = backWidth * Math.cos(this.angle + backAngleOffset + Math.PI / 2);
            double oyBack = backWidth * Math.sin(this.angle + backAngleOffset + Math.PI / 2);
            double frontX1 = frontX + oxFront * hor1;
            double frontX2 = frontX + oxFront * hor2;
            double frontY1 = frontY + oyFront * hor1;
            double frontY2 = frontY + oyFront * hor2;
            double backX1 = backX + oxBack * hor1;
            double backX2 = backX + oxBack * hor2;
            double backY1 = backY + oyBack * hor1;
            double backY2 = backY + oyBack * hor2;

            Drawing.drawing.setColor(frontR, frontG, frontB, frontA);
            Drawing.drawing.addVertex(frontX1, frontY1, z1Front);
            Drawing.drawing.setColor(backR, backG, backB, backA);
            Drawing.drawing.addVertex(backX1, backY1, z1Back);
            Drawing.drawing.addVertex(backX2, backY2, z2Back);
            Drawing.drawing.setColor(frontR, frontG, frontB, frontA);
            Drawing.drawing.addVertex(frontX2, frontY2, z2Front);
        }

        Game.game.window.shapeRenderer.setBatchMode(false, true, Game.enable3d, this.glow, false);
    }

    public void drawCap3D(double frontX, double frontY, double frontWidth, boolean back)
    {
        Game.game.window.shapeRenderer.setBatchMode(true, false, Game.enable3d, this.glow, false);

        int outerPolyCount = 8;

        for (int o = 0; o < outerPolyCount; o++)
        {
            int polyCount = 20;//(int) Math.ceil(20 * (1.0 - (1.0 * o / outerPolyCount)));
            int polyCount2 = 20;//(int) Math.ceil(20 * (1.0 - (1.0 * (o + 1) / outerPolyCount)));

            double angleOffset = 0;
            if (back)
                angleOffset = Math.PI;

            double size = o * 1.0 / (outerPolyCount);
            double size2 = (o + 1.0) / (outerPolyCount);
            double fX = frontX + size * Math.cos(this.angle + angleOffset) * frontWidth;
            double fY = frontY + size * Math.sin(this.angle + angleOffset) * frontWidth;
            double fXi = frontX + size2 * Math.cos(this.angle + angleOffset) * frontWidth;
            double fYi = frontY + size2 * Math.sin(this.angle + angleOffset) * frontWidth;

            for (int i = 0; i < polyCount; i++)
            {
                double angle1out = Math.PI * 2 * i / polyCount;
                double angle2out = Math.PI * 2 * (i + 1) / polyCount;

                int iIn = (int) (polyCount2 * (i * 1.0 / polyCount));
                int i2In = (int) (polyCount2 * ((i + 1.0) / polyCount));

                double angle1in = Math.PI * 2 * iIn / polyCount2;
                double angle2in = Math.PI * 2 * i2In / polyCount2;

                double vert1o = Math.sin(angle1out);
                double vert2o = Math.sin(angle2out);
                double hor1o = Math.cos(angle1out);
                double hor2o = Math.cos(angle2out);

                double vert1i = Math.sin(angle1in);
                double vert2i = Math.sin(angle2in);
                double hor1i = Math.cos(angle1in);
                double hor2i = Math.cos(angle2in);

                double width1 = frontWidth * Math.sqrt(1 - Math.pow(o * 1.0 / outerPolyCount, 2));
                double width2 = frontWidth * Math.sqrt(1 - Math.pow((o + 1.0) / outerPolyCount, 2));
                double z1o = vert1o * width1 + this.movable.posZ;
                double z2o = vert2o * width1 + this.movable.posZ;
                double z1i = vert1i * width2 + this.movable.posZ;
                double z2i = vert2i * width2 + this.movable.posZ;

                double ox = Math.cos(this.angle + angleOffset + Math.PI / 2);
                double oy = Math.sin(this.angle + angleOffset + Math.PI / 2);

                double outX1 = fX + ox * width1 * hor1o;
                double outX2 = fX + ox * width1 * hor2o;
                double outY1 = fY + oy * width1 * hor1o;
                double outY2 = fY + oy * width1 * hor2o;

                double inX1 = fXi + ox * width2 * hor1i;
                double inX2 = fXi + ox * width2 * hor2i;
                double inY1 = fYi + oy * width2 * hor1i;
                double inY2 = fYi + oy * width2 * hor2i;

                Drawing.drawing.addVertex(outX1, outY1, z1o);
                Drawing.drawing.addVertex(outX2, outY2, z2o);
                Drawing.drawing.addVertex(inX1, inY1, z1i);

                if (iIn != i2In)
                {
                    Drawing.drawing.addVertex(inX1, inY1, z1i);
                    Drawing.drawing.addVertex(inX2, inY2, z2i);
                    Drawing.drawing.addVertex(outX2, outY2, z2o);
                }
            }
        }

        Game.game.window.shapeRenderer.setBatchMode(false, false, Game.enable3d, this.glow, false);
    }
}

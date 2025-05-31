package tanks.bullet;

import basewindow.Model;
import basewindow.transformation.AxisRotation;
import basewindow.transformation.Rotation;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.gui.screen.ScreenGame;

public class Trail3D extends Trail
{
    double backZ;
    double frontZ;
    public double pitch;

    protected double frontAngleOffsetPitch;
    protected double backAngleOffsetPitch;

    public static Model cap = Drawing.drawing.createModel("/models/cap/");

    public Trail3D(Movable m, double speed, double backX, double backY, double backZ,
                   double delay, double backWidth, double frontWidth, double length,
                   double angle, double pitch,
                   double frontR, double frontG, double frontB, double frontA,
                   double backR, double backG, double backB, double backA,
                   boolean glow, double luminosity,
                   boolean frontCircle, boolean backCircle)
    {
        super(m, speed, backX, backY, delay, backWidth, frontWidth, length, angle, frontR, frontG, frontB, frontA, backR, backG, backB, backA, glow, luminosity, frontCircle, backCircle);
        this.backZ = backZ;
        this.frontZ = backZ;
        this.pitch = pitch;
    }

    @Override
    public double update(double trailLength, boolean destroy)
    {
        if (destroy)
            this.age += speed * Panel.frameFrequency;
        else
            this.age = trailLength;

        if (this.spawning)
        {
            this.frontX = movable.posX;
            this.frontY = movable.posY;
            this.frontZ = movable.posZ;
            this.currentLength = Math.sqrt(Math.pow(frontY - backY, 2) + Math.pow(frontX - backX, 2) + Math.pow(frontZ - backZ, 2));
        }

        if (this.age - delay > this.maxLength || ScreenGame.finishTimer <= 0)
            this.expired = true;

        return this.currentLength;
    }

    public void setFrontAngleOffset(double offset, double pitch)
    {
        this.frontCircle = false;
        this.showOutsideFront = false;
        this.frontAngleOffset = offset;
        this.frontAngleOffsetPitch = pitch;
    }

    public void setBackAngleOffset(double offset, double pitch)
    {
        this.backCircle = false;
        this.showOutsideBack = false;
        this.backAngleOffset = offset;
        this.backAngleOffsetPitch = pitch;
    }

    @Override
    public void draw()
    {
        double opacity = ScreenGame.finishTimer / ScreenGame.finishTimerMax;

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
                    drawCap3D(this.frontX, this.frontY, this.frontZ, frontWidth, angle, pitch + Math.PI);
            }
            else
            {
                frontWidth = this.frontWidth;
                Drawing.drawing.setColor(this.frontR, this.frontG, this.frontB, this.frontA * opacity, this.luminosity);

                if (frontCircle)
                    drawCap3D(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.backZ * frac3 + this.frontZ * (1 - frac3), frontWidth, angle, pitch);
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

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle || (showOutsides && showOutsideBack))
                    drawCap3D(this.backX, this.backY, this.backZ, backWidth, angle, pitch);
            }
            else
            {
                backWidth = this.backWidth;
                Drawing.drawing.setColor(this.backR, this.backG, this.backB, this.backA * opacity, this.luminosity);

                Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

                if (backCircle)
                    drawCap3D(this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.frontZ * frac4 + this.backZ * (1 - frac4), backWidth, angle, pitch + Math.PI);
            }

            Game.game.window.shapeRenderer.setBatchMode(false, true, depth, this.glow, false);

            drawTube3D(this.backX * frac3 + this.frontX * (1 - frac3), this.backY * frac3 + this.frontY * (1 - frac3), this.backZ * frac3 + this.frontZ * (1 - frac3),
                        frontWidth, frac1 >= 0 ? frontAngleOffset : 0, frac1 >= 0 ? frontAngleOffsetPitch : 0,
                        r, g, b, a,
                        this.frontX * frac4 + this.backX * (1 - frac4), this.frontY * frac4 + this.backY * (1 - frac4), this.frontZ * frac4 + this.backZ * (1 - frac4),
                        backWidth, frac2 <= 1 ? backAngleOffset : 0, frac2 <= 1 ? backAngleOffsetPitch : 0,
                        Drawing.drawing.currentColorR, Drawing.drawing.currentColorG, Drawing.drawing.currentColorB, Drawing.drawing.currentColorA);
        }
    }

    // Welcome to math hell
    public void drawTube3D(double frontX, double frontY, double frontZ, double frontWidth,
                           double frontAngleOffset, double frontAngleOffsetPitch,
                           double frontR, double frontG, double frontB, double frontA,
                           double backX, double backY, double backZ, double backWidth,
                           double backAngleOffset, double backAngleOffsetPitch,
                           double backR, double backG, double backB, double backA)
    {
        Game.game.window.shapeRenderer.setBatchMode(true, true, Game.enable3d, this.glow, false);

        int polyCount = 20;
        for (int i = 0; i < polyCount; i++)
        {
            // Angle 1 and angle 2: angle and next angle around the tube
            double angle1 = Math.PI * 2 * i / polyCount;
            double angle2 = Math.PI * 2 * (i + 1) / polyCount;

            // sin/cos of pitch angles (up/down)
            double cosPitchFront = Math.cos(pitch + frontAngleOffsetPitch + Math.PI / 2);
            double cosPitchBack = Math.cos(pitch + backAngleOffsetPitch + Math.PI / 2);
            double sinPitchFront = Math.sin(pitch + frontAngleOffsetPitch + Math.PI / 2);
            double sinPitchBack = Math.sin(pitch + backAngleOffsetPitch + Math.PI / 2);

            // Vertical (+-z) and horizontal (+- x or y, depending on rotation) position as if the tube is facing horizontally
            double vert1 = Math.sin(angle1);
            double vert2 = Math.sin(angle2);
            double hor1 = Math.cos(angle1);
            double hor2 = Math.cos(angle2);

            // Heights of vertices - when pitch is zero (tube pointing up) it should be flat, hence sine
            double z1Front = frontZ + vert1 * sinPitchFront * frontWidth;
            double z2Front = frontZ + vert2 * sinPitchFront * frontWidth;
            double z1Back = backZ + vert1 * sinPitchBack * backWidth;
            double z2Back = backZ + vert2 * sinPitchBack * backWidth;

            // Offsets that determine how horizontal coordinate translates to x and y
            double oxFront = frontWidth * Math.cos(this.angle + frontAngleOffset + Math.PI / 2);
            double oyFront = frontWidth * Math.sin(this.angle + frontAngleOffset + Math.PI / 2);
            double oxBack = backWidth * Math.cos(this.angle + backAngleOffset + Math.PI / 2);
            double oyBack = backWidth * Math.sin(this.angle + backAngleOffset + Math.PI / 2);

            // Same thing, but for the other (perpendicular) horizontal coordinate
            double oxFrontDep = frontWidth * Math.cos(this.angle + frontAngleOffset);
            double oyFrontDep = frontWidth * Math.sin(this.angle + frontAngleOffset);
            double oxBackDep = backWidth * Math.cos(this.angle + backAngleOffset);
            double oyBackDep = backWidth * Math.sin(this.angle + backAngleOffset);

            // The 'other' (perpendicular) horizontal coordinate obtained by rotating the vertical circle by up/down pitch
            double vert1FrontRot = vert1 * cosPitchFront;
            double vert2FrontRot = vert2 * cosPitchFront;
            double vert1BackRot = vert1 * cosPitchBack;
            double vert2BackRot = vert2 * cosPitchBack;

            // Final coordinates
            double frontX1 = frontX + oxFront * hor1 + oxFrontDep * vert1FrontRot;
            double frontX2 = frontX + oxFront * hor2 + oxFrontDep * vert2FrontRot;
            double frontY1 = frontY + oyFront * hor1 + oyFrontDep * vert1FrontRot;
            double frontY2 = frontY + oyFront * hor2 + oyFrontDep * vert2FrontRot;
            double backX1 = backX + oxBack * hor1 + oxBackDep * vert1BackRot;
            double backX2 = backX + oxBack * hor2 + oxBackDep * vert2BackRot;
            double backY1 = backY + oyBack * hor1 + oyBackDep * vert1BackRot;
            double backY2 = backY + oyBack * hor2 + oyBackDep * vert2BackRot;

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

    AxisRotation[] rotations = new AxisRotation[]{new AxisRotation(Game.game.window, AxisRotation.Axis.roll, 0), new AxisRotation(Game.game.window, AxisRotation.Axis.pitch, 0)};
    public void drawCap3D(double x, double y, double z, double width, double angle, double pitch)
    {
        rotations[0].angle = -(angle - Math.PI / 2);
        rotations[1].angle = -(pitch);
        Game.game.window.setForceModelGlow(glow);
        Drawing.drawing.drawModel(cap, x, y, z, width, width, width, rotations);
        Game.game.window.setForceModelGlow(false);
    }
}

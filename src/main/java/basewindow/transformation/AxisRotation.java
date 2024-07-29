package basewindow.transformation;

import basewindow.BaseWindow;

public class AxisRotation extends Transformation
{
    public enum Axis {yaw, pitch, roll}

    public Axis axis;
    public double angle;

    public AxisRotation(BaseWindow window, Axis a, double angle)
    {
        super(window);
        this.axis = a;
        this.angle = angle;
    }

    public void apply()
    {
        this.applyToWindow();

        if (this.axis == Axis.yaw)
            transformYaw(window, angle);
        else if (this.axis == Axis.pitch)
            transformPitch(window, angle);
        else
            transformRoll(window, angle);
    }

    @Override
    public void applyToWindow()
    {

    }

    public static void transformYaw(BaseWindow window, double yaw)
    {
        transform(window, Math.cos(yaw), 0, -Math.sin(yaw), 0,  0, 1, 0, 0,  Math.sin(yaw), 0, Math.cos(yaw), 0,  0, 0, 0, 1);
    }

    public static void transformPitch(BaseWindow window, double pitch)
    {
        transform(window, 1, 0, 0, 0,  0, Math.cos(pitch), -Math.sin(pitch), 0,  0, Math.sin(pitch), Math.cos(pitch), 0,  0, 0, 0, 1);
    }

    public static void transformRoll(BaseWindow window, double roll)
    {
        transform(window, Math.cos(roll), -Math.sin(roll), 0, 0,  Math.sin(roll), Math.cos(roll), 0, 0,  0, 0, 1, 0,  0, 0, 0, 1);
    }
}

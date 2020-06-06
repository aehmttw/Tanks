package basewindow.transformation;

import basewindow.BaseWindow;

public abstract class Transformation
{
    public BaseWindow window;
    protected static double[] matrix = new double[16];

    public Transformation(BaseWindow window)
    {
        this.window = window;
    }

    public abstract void apply();

    protected static void transform(BaseWindow w,
                                    double a1, double a2, double a3, double a4,
                                    double b1, double b2, double b3, double b4,
                                    double c1, double c2, double c3, double c4,
                                    double d1, double d2, double d3, double d4)
    {
        matrix[0] = a1;
        matrix[1] = a2;
        matrix[2] = a3;
        matrix[3] = a4;

        matrix[4] = b1;
        matrix[5] = b2;
        matrix[6] = b3;
        matrix[7] = b4;

        matrix[8] = c1;
        matrix[9] = c2;
        matrix[10] = c3;
        matrix[11] = c4;

        matrix[12] = d1;
        matrix[13] = d2;
        matrix[14] = d3;
        matrix[15] = d4;

        w.transform(matrix);
    }
}

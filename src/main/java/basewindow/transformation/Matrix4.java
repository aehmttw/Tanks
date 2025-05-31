package basewindow.transformation;

public class Matrix4
{
    public final double[][] values = new double[4][4];

    public Matrix4(double[][] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            System.arraycopy(values[i], 0, this.values[i], 0, values[i].length);
        }
    }

    public Matrix4(double[] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            this.values[i / 4][i % 4] = values[i];
        }
    }

    public Matrix4 multiply(Matrix4 m)
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                for (int k = 0; k < 4; k++)
                {
                    values[i][j] = values[k][j] * m.values[i][k];
                }
            }
        }
        return this;
    }

    public Matrix4 makeCopy()
    {
        return new Matrix4(this.values);
    }
}

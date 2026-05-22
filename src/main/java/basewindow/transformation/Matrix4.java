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

    public Matrix4()
    {

    }

    public Matrix4(double[] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            this.values[i / 4][i % 4] = values[i];
        }
    }

    public static Matrix4 fromOpenGLArray(float[] gl)
    {
        double[][] m = new double[4][4];

        for (int row = 0; row < 4; row++)
        {
            for (int col = 0; col < 4; col++)
            {
                m[row][col] = gl[col * 4 + row];
            }
        }

        return new Matrix4(m);
    }

    public Matrix4 multiply(Matrix4 m)
    {
        double[][] result = new double[4][4];

        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                double acc = 0.0;
                for (int k = 0; k < 4; k++)
                {
                    acc += this.values[i][k] * m.values[k][j];
                }
                result[i][j] = acc;
            }
        }

        return new Matrix4(result);
    }

    public Matrix4 makeCopy()
    {
        return new Matrix4(this.values);
    }

    public Matrix4 inverse()
    {
        double[][] m = this.values;
        double[][] inv = new double[4][4];

        inv[0][0] =  m[1][1]*m[2][2]*m[3][3] - m[1][1]*m[2][3]*m[3][2] -
                m[2][1]*m[1][2]*m[3][3] + m[2][1]*m[1][3]*m[3][2] +
                m[3][1]*m[1][2]*m[2][3] - m[3][1]*m[1][3]*m[2][2];

        inv[0][1] = -m[0][1]*m[2][2]*m[3][3] + m[0][1]*m[2][3]*m[3][2] +
                m[2][1]*m[0][2]*m[3][3] - m[2][1]*m[0][3]*m[3][2] -
                m[3][1]*m[0][2]*m[2][3] + m[3][1]*m[0][3]*m[2][2];

        inv[0][2] =  m[0][1]*m[1][2]*m[3][3] - m[0][1]*m[1][3]*m[3][2] -
                m[1][1]*m[0][2]*m[3][3] + m[1][1]*m[0][3]*m[3][2] +
                m[3][1]*m[0][2]*m[1][3] - m[3][1]*m[0][3]*m[1][2];

        inv[0][3] = -m[0][1]*m[1][2]*m[2][3] + m[0][1]*m[1][3]*m[2][2] +
                m[1][1]*m[0][2]*m[2][3] - m[1][1]*m[0][3]*m[2][2] -
                m[2][1]*m[0][2]*m[1][3] + m[2][1]*m[0][3]*m[1][2];

        inv[1][0] = -m[1][0]*m[2][2]*m[3][3] + m[1][0]*m[2][3]*m[3][2] +
                m[2][0]*m[1][2]*m[3][3] - m[2][0]*m[1][3]*m[3][2] -
                m[3][0]*m[1][2]*m[2][3] + m[3][0]*m[1][3]*m[2][2];

        inv[1][1] =  m[0][0]*m[2][2]*m[3][3] - m[0][0]*m[2][3]*m[3][2] -
                m[2][0]*m[0][2]*m[3][3] + m[2][0]*m[0][3]*m[3][2] +
                m[3][0]*m[0][2]*m[2][3] - m[3][0]*m[0][3]*m[2][2];

        inv[1][2] = -m[0][0]*m[1][2]*m[3][3] + m[0][0]*m[1][3]*m[3][2] +
                m[1][0]*m[0][2]*m[3][3] - m[1][0]*m[0][3]*m[3][2] -
                m[3][0]*m[0][2]*m[1][3] + m[3][0]*m[0][3]*m[1][2];

        inv[1][3] =  m[0][0]*m[1][2]*m[2][3] - m[0][0]*m[1][3]*m[2][2] -
                m[1][0]*m[0][2]*m[2][3] + m[1][0]*m[0][3]*m[2][2] +
                m[2][0]*m[0][2]*m[1][3] - m[2][0]*m[0][3]*m[1][2];

        inv[2][0] =  m[1][0]*m[2][1]*m[3][3] - m[1][0]*m[2][3]*m[3][1] -
                m[2][0]*m[1][1]*m[3][3] + m[2][0]*m[1][3]*m[3][1] +
                m[3][0]*m[1][1]*m[2][3] - m[3][0]*m[1][3]*m[2][1];

        inv[2][1] = -m[0][0]*m[2][1]*m[3][3] + m[0][0]*m[2][3]*m[3][1] +
                m[2][0]*m[0][1]*m[3][3] - m[2][0]*m[0][3]*m[3][1] -
                m[3][0]*m[0][1]*m[2][3] + m[3][0]*m[0][3]*m[2][1];

        inv[2][2] =  m[0][0]*m[1][1]*m[3][3] - m[0][0]*m[1][3]*m[3][1] -
                m[1][0]*m[0][1]*m[3][3] + m[1][0]*m[0][3]*m[3][1] +
                m[3][0]*m[0][1]*m[1][3] - m[3][0]*m[0][3]*m[1][1];

        inv[2][3] = -m[0][0]*m[1][1]*m[2][3] + m[0][0]*m[1][3]*m[2][1] +
                m[1][0]*m[0][1]*m[2][3] - m[1][0]*m[0][3]*m[2][1] -
                m[2][0]*m[0][1]*m[1][3] + m[2][0]*m[0][3]*m[1][1];

        inv[3][0] = -m[1][0]*m[2][1]*m[3][2] + m[1][0]*m[2][2]*m[3][1] +
                m[2][0]*m[1][1]*m[3][2] - m[2][0]*m[1][2]*m[3][1] -
                m[3][0]*m[1][1]*m[2][2] + m[3][0]*m[1][2]*m[2][1];

        inv[3][1] =  m[0][0]*m[2][1]*m[3][2] - m[0][0]*m[2][2]*m[3][1] -
                m[2][0]*m[0][1]*m[3][2] + m[2][0]*m[0][2]*m[3][1] +
                m[3][0]*m[0][1]*m[2][2] - m[3][0]*m[0][2]*m[2][1];

        inv[3][2] = -m[0][0]*m[1][1]*m[3][2] + m[0][0]*m[1][2]*m[3][1] +
                m[1][0]*m[0][1]*m[3][2] - m[1][0]*m[0][2]*m[3][1] -
                m[3][0]*m[0][1]*m[1][2] + m[3][0]*m[0][2]*m[1][1];

        inv[3][3] =  m[0][0]*m[1][1]*m[2][2] - m[0][0]*m[1][2]*m[2][1] -
                m[1][0]*m[0][1]*m[2][2] + m[1][0]*m[0][2]*m[2][1] +
                m[2][0]*m[0][1]*m[1][2] - m[2][0]*m[0][2]*m[1][1];

        double det =
                m[0][0]*inv[0][0] + m[0][1]*inv[1][0] +
                        m[0][2]*inv[2][0] + m[0][3]*inv[3][0];

        if (det == 0)
            throw new RuntimeException("Matrix is not invertible");

        det = 1.0 / det;

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                inv[i][j] *= det;

        return new Matrix4(inv);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Matrix4[\n");

        for (int row = 0; row < 4; row++)
        {
            sb.append("  ");
            for (int col = 0; col < 4; col++)
            {
                sb.append(String.format("%10.6f", values[row][col]));
                if (col < 3) sb.append(" ");
            }
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    public float[] openGLMatrix = new float[16];
    public float[] toOpenGL()
    {
        int index = 0;

        for (int col = 0; col < 4; col++)
        {
            for (int row = 0; row < 4; row++)
            {
                openGLMatrix[index] = (float) this.values[row][col];
                index++;
            }
        }

        return openGLMatrix;
    }

}

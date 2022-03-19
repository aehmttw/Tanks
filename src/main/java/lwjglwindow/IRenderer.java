package lwjglwindow;

public interface IRenderer
{
    void setDrawMode(int type, boolean depthTest, boolean depthMask, int vertices);

    void vertex2d(double x, double y);

    void vertex3d(double x, double y, double z);
}

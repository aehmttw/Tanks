package basewindow;

public interface IModel
{
    void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest);
    void draw(double posX, double posY, double sX, double sY, double yaw);
    void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ);
}

package basewindow;

import basewindow.transformation.AxisRotation;

import java.util.HashMap;

public interface IModel
{
    void setSkin(HashMap<String, String> skins);
    void setSkin(String tex);
    void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, AxisRotation[] axisRotations, boolean depthTest);
    void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest);
    void draw(double posX, double posY, double sX, double sY, double yaw);
    void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ);
}

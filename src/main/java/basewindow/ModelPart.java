package basewindow;

import java.util.ArrayList;

public abstract class ModelPart implements IModel
{
    public Model.Material material = null;
    public Shape[] shapes;
    public Model parent;

    public static abstract class Shape
    {
        public double brightness = 1;
        public Point[] points;
        public Point[] texCoords;
        public Point[] normals;
        public double[][] colors;
    }

    public static class Point
    {
        public double x;
        public double y;
        public double z;

        public Model.Bone[] bones;
        public double[] boneWeights;

        public Point(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Triangle extends Shape
    {
        public Triangle(Point a, Point b, Point c, double brightness)
        {
            this.points = new Point[3];
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.brightness = brightness;
        }

        public Triangle(Point a, Point b, Point c, Point ta, Point tb, Point tc, Point na, Point nb, Point nc, double[] ca, double[] cb, double[] cc)
        {
            this.points = new Point[3];
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;

            this.texCoords = new Point[3];
            this.texCoords[0] = ta;
            this.texCoords[1] = tb;
            this.texCoords[2] = tc;

            this.normals = new Point[3];
            this.normals[0] = na;
            this.normals[1] = nb;
            this.normals[2] = nc;

            this.colors = new double[3][];
            this.colors[0] = ca;
            this.colors[1] = cb;
            this.colors[2] = cc;
        }
    }

    public abstract static class ShapeDrawer
    {
        public abstract void drawShape(ModelPart m, Shape s, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll);

        public abstract void drawShape(ModelPart m, Shape s, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw);

        public abstract void drawShape(ModelPart m, Shape s, double posX, double posY, double sX, double sY, double yaw);
    }

    //@Deprecated
    public static class Quad extends Shape
    {
        public Quad(Point a, Point b, Point c, Point d, double brightness)
        {
            this.points = new Point[4];
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.points[3] = d;
            this.brightness = brightness;
        }

        public Quad(Point a, Point b, Point c, Point d, Point ta, Point tb, Point tc, Point td)
        {
            this.points = new Point[4];
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.points[3] = d;

            this.texCoords = new Point[4];
            this.texCoords[0] = ta;
            this.texCoords[1] = tb;
            this.texCoords[2] = tc;
            this.texCoords[3] = td;
        }
    }

    public ModelPart(BaseWindow window)
    {
        this.setWindow(window);
    }

    public ModelPart(BaseWindow window, Model model, ArrayList<Shape> shapes, Model.Material material)
    {
        this.setWindow(window);
        this.parent = model;
        this.shapes = new Shape[shapes.size()];
        this.material = material;

        for (int i = 0; i < shapes.size(); i++)
        {
            this.shapes[i] = shapes.get(i);
        }
    }

    public void setWindow(BaseWindow w)
    {

    }

    public void processShapes()
    {

    }
}

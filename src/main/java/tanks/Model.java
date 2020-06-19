package tanks;

public class Model
{
    public Shape[] shapes;

    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
    {
        Game.game.window.setBatchMode(true, false, true);

        for (int i = 0; i < this.shapes.length; i++)
        {
            Game.game.window.setColor(
                    Drawing.drawing.currentColorR * this.shapes[i].brightness,
                    Drawing.drawing.currentColorG * this.shapes[i].brightness,
                    Drawing.drawing.currentColorB * this.shapes[i].brightness,
                    Drawing.drawing.currentColorA);

            this.shapes[i].draw(posX, posY, posZ, sX, sY, sZ, yaw);
        }

        Game.game.window.setBatchMode(false, false, true);
    }

    public void draw(double posX, double posY, double sX, double sY, double yaw)
    {
        draw(posX, posY, sX, sY, yaw, false);
    }

    public void draw(double posX, double posY, double sX, double sY, double yaw, boolean linked)
    {
        Game.game.window.setBatchMode(true, false, false);

        for (int i = 0; i < this.shapes.length; i++)
        {
            Game.game.window.setColor(
                    Drawing.drawing.currentColorR * this.shapes[i].brightness,
                    Drawing.drawing.currentColorG * this.shapes[i].brightness,
                    Drawing.drawing.currentColorB * this.shapes[i].brightness,
                    Drawing.drawing.currentColorA);

            this.shapes[i].draw(posX, posY, sX, sY, yaw);
        }

        Game.game.window.setBatchMode(false, false, false);
    }


    static abstract class Shape
    {
        public double brightness = 1;

        public abstract void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw);
        public abstract void draw(double posX, double posY, double sX, double sY, double yaw);
    }

    static class Point
    {
        public double x;
        public double y;
        public double z;

        public Point(double x, double y, double z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    static class Triangle extends Shape
    {
        public Point[] points = new Point[3];

        public Triangle(Point a, Point b, Point c, double brightness)
        {
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.brightness = brightness;
        }

        @Override
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            for (int i = 0; i < this.points.length; i++)
            {
                Game.game.window.addVertex(
                        (this.points[i].x * Math.cos(yaw) * sX - this.points[i].y * Math.sin(yaw) * sY) + posX,
                        (this.points[i].y * Math.cos(yaw) * sY + this.points[i].x * Math.sin(yaw) * sX) + posY,
                        this.points[i].z * sZ + posZ);
            }
        }

        @Override
        public void draw(double posX, double posY, double sX, double sY, double yaw)
        {
            for (int i = 0; i < this.points.length; i++)
            {
                Game.game.window.addVertex(
                        (this.points[i].x * Math.cos(yaw) * sX - this.points[i].y * Math.sin(yaw) * sY) + posX,
                        (this.points[i].y * Math.cos(yaw) * sY + this.points[i].x * Math.sin(yaw) * sX) + posY);
            }
        }
    }

    static class Quad extends Shape
    {
        public Point[] points = new Point[4];

        public Quad(Point a, Point b, Point c, Point d, double brightness)
        {
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.points[3] = d;
            this.brightness = brightness;
        }

        @Override
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            this.addVertex(0, posX, posY, posZ, sX, sY, sZ, yaw);
            this.addVertex(1, posX, posY, posZ, sX, sY, sZ, yaw);
            this.addVertex(2, posX, posY, posZ, sX, sY, sZ, yaw);

            this.addVertex(0, posX, posY, posZ, sX, sY, sZ, yaw);
            this.addVertex(3, posX, posY, posZ, sX, sY, sZ, yaw);
            this.addVertex(2, posX, posY, posZ, sX, sY, sZ, yaw);
        }

        @Override
        public void draw(double posX, double posY, double sX, double sY, double yaw)
        {
            this.addVertex(0, posX, posY, sX, sY, yaw);
            this.addVertex(1, posX, posY, sX, sY, yaw);
            this.addVertex(2, posX, posY, sX, sY, yaw);

            this.addVertex(0, posX, posY, sX, sY, yaw);
            this.addVertex(3, posX, posY, sX, sY, yaw);
            this.addVertex(2, posX, posY, sX, sY, yaw);
        }

        public void addVertex(int index, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            Game.game.window.addVertex(
                    (this.points[index].x * Math.cos(yaw) * sX - this.points[index].y * Math.sin(yaw) * sY) + posX,
                    (this.points[index].y * Math.cos(yaw) * sY + this.points[index].x * Math.sin(yaw) * sX) + posY,
                    this.points[index].z * sZ + posZ);
        }

        public void addVertex(int index, double posX, double posY, double sX, double sY, double yaw)
        {
            Game.game.window.addVertex(
                    (this.points[index].x * Math.cos(yaw) * sX - this.points[index].y * Math.sin(yaw) * sY) + posX,
                    (this.points[index].y * Math.cos(yaw) * sY + this.points[index].x * Math.sin(yaw) * sX) + posY);
        }
    }

    static class LegacySquare extends Shape
    {
        public double width;
        public double height;

        public LegacySquare(double width, double height)
        {
           this.width = width;
           this.height = height;
        }

        @Override
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            Game.game.window.fillRect(posX - sX * width / 2, posY - sY * height / 2, sX * width, sY * height);
        }

        @Override
        public void draw(double posX, double posY, double sX, double sY, double yaw)
        {
            Game.game.window.fillRect(posX - sX * width / 2, posY - sY * height / 2, sX * width, sY * height);
        }
    }

}

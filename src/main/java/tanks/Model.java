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
                    Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);

            this.shapes[i].draw(posX, posY, posZ, sX, sY, sZ, yaw);
        }

        Game.game.window.setBatchMode(false, false, true);
    }

    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
    {
        Game.game.window.setBatchMode(true, false, true);

        for (int i = 0; i < this.shapes.length; i++)
        {
            Game.game.window.setColor(
                    Drawing.drawing.currentColorR * this.shapes[i].brightness,
                    Drawing.drawing.currentColorG * this.shapes[i].brightness,
                    Drawing.drawing.currentColorB * this.shapes[i].brightness,
                    Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);

            this.shapes[i].draw(posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
        }

        Game.game.window.setBatchMode(false, false, true);
    }

    public void draw(double posX, double posY, double sX, double sY, double yaw)
    {
        Game.game.window.setBatchMode(true, false, false);

        for (int i = 0; i < this.shapes.length; i++)
        {
            Game.game.window.setColor(
                    Drawing.drawing.currentColorR * this.shapes[i].brightness,
                    Drawing.drawing.currentColorG * this.shapes[i].brightness,
                    Drawing.drawing.currentColorB * this.shapes[i].brightness,
                    Drawing.drawing.currentColorA, Drawing.drawing.currentGlow);

            this.shapes[i].draw(posX, posY, sX, sY, yaw);
        }

        Game.game.window.setBatchMode(false, false, false);
    }


    public static abstract class Shape
    {
        public double brightness = 1;

        public abstract void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw);
        public abstract void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll);
        public abstract void draw(double posX, double posY, double sX, double sY, double yaw);
    }

    public static class Point
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

    public abstract static class ShapeWithPoints extends Shape
    {
        public Point[] points;

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

        public void addVertex(int index, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
        {
            double a = Math.cos(-yaw);
            double b = Math.sin(-yaw);
            double c = Math.cos(-roll);
            double d = Math.sin(-roll);
            double e = Math.cos(-pitch);
            double f = Math.sin(-pitch);

            double x1 = e * a - b * d * f;
            double y1 = -a * d * f - e * b;
            double z1 = -c * f;
            double x2 = b * c;
            double y2 = a * c;
            double z2 = -d;
            double x3 = a * f + e * b * d;
            double y3 = e * a * d - b * f;
            double z3 = e * c;

            double ox = this.points[index].x * sX;
            double oy = this.points[index].y * sY;
            double oz = this.points[index].z * sZ;

            Game.game.window.addVertex(posX + ox * x1 + oy * x2 + oz * x3, posY + ox * y1 + oy * y2 + oz * y3, posZ + ox * z1 + oy * z2 + oz * z3);
        }
    }

    public static class Triangle extends ShapeWithPoints
    {
        public Triangle(Point a, Point b, Point c, double brightness)
        {
            this.points = new Point[3];
            this.points[0] = a;
            this.points[1] = b;
            this.points[2] = c;
            this.brightness = brightness;
        }

        @Override
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
        {
            for (int i = 0; i < this.points.length; i++)
            {
                this.addVertex(i, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            }
        }

        @Override
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            for (int i = 0; i < this.points.length; i++)
            {
                this.addVertex(i, posX, posY, posZ, sX, sY, sZ, yaw);
            }
        }

        @Override
        public void draw(double posX, double posY, double sX, double sY, double yaw)
        {
            for (int i = 0; i < this.points.length; i++)
            {
                this.addVertex(i, posX, posY, sX, sY, yaw);
            }
        }
    }

    public static class Quad extends ShapeWithPoints
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
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
        {
            this.addVertex(0, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            this.addVertex(1, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            this.addVertex(2, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);

            this.addVertex(0, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            this.addVertex(3, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            this.addVertex(2, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
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
    }

    public static class LegacySquare extends Shape
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
        public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
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

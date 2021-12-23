package lwjglwindow;

import basewindow.BaseWindow;
import basewindow.Model;
import basewindow.ModelPart;

import java.util.ArrayList;

public class ImmediateModeModelPart extends ModelPart
{
    public BaseWindow window;

    @Override
    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest)
    {
        window.shapeRenderer.setBatchMode(true, false, depthTest);

        double r = window.colorR * 255;
        double g = window.colorG * 255;
        double b = window.colorB * 255;

        boolean depthMask = true;
        for (Shape shape : this.shapes)
        {
            if (this.material != null)
            {
                if (this.material.depthMask != depthMask)
                {
                    window.shapeRenderer.setBatchMode(false, false, true);
                    window.shapeRenderer.setBatchMode(true, false, true, false, this.material.depthMask);
                    depthMask = this.material.depthMask;
                }

                window.setTexture(this.material.texture);
            }
            else
                window.stopTexture();

            window.setColor(r * shape.brightness, g * shape.brightness, b * shape.brightness, window.colorA * 255, window.glow * 255);
            window.shapeDrawer.drawShape(this, shape, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
        }

        window.shapeRenderer.setBatchMode(false, false, true);
    }

    @Override
    public void draw(double posX, double posY, double sX, double sY, double yaw)
    {
        window.shapeRenderer.setBatchMode(true, false, false);

        double r = window.colorR * 255;
        double g = window.colorG * 255;
        double b = window.colorB * 255;

        boolean depthMask = true;
        for (Shape shape : this.shapes)
        {
            if (this.material != null)
            {
                if (this.material.depthMask != depthMask)
                {
                    window.shapeRenderer.setBatchMode(false, false, true);
                    window.shapeRenderer.setBatchMode(true, false, true, false, this.material.depthMask);
                    depthMask = this.material.depthMask;
                }

                window.setTexture(this.material.texture);
            }
            else
                window.stopTexture();

            window.setColor(r * shape.brightness, g * shape.brightness, b * shape.brightness, window.colorA * 255, window.glow * 255);
            window.shapeDrawer.drawShape(this, shape, posX, posY, sX, sY, yaw);
        }

        window.shapeRenderer.setBatchMode(false, false, false);
    }

    public static class ImmediateModeShapeDrawer extends ShapeDrawer
    {
        public BaseWindow window;

        public ImmediateModeShapeDrawer(BaseWindow w)
        {
            this.window = w;
        }

        public void addVertex(ModelPart m, Shape s, int index, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            if (m.material != null)
                window.setTextureCoords(s.texCoords[index].x, s.texCoords[index].y);

            window.addVertex(
                    (s.points[index].x * Math.cos(yaw) * sX - s.points[index].y * Math.sin(yaw) * sY) + posX,
                    (s.points[index].y * Math.cos(yaw) * sY + s.points[index].x * Math.sin(yaw) * sX) + posY,
                    s.points[index].z * sZ + posZ);
        }

        public void addVertex(ModelPart m, Shape s, int index, double posX, double posY, double sX, double sY, double yaw)
        {
            if (m.material != null)
                window.setTextureCoords(s.texCoords[index].x, s.texCoords[index].y);

            window.addVertex(
                    (s.points[index].x * Math.cos(yaw) * sX - s.points[index].y * Math.sin(yaw) * sY) + posX,
                    (s.points[index].y * Math.cos(yaw) * sY + s.points[index].x * Math.sin(yaw) * sX) + posY);
        }

        public void addVertex(ModelPart m, Shape s, int index, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
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

            double ox = s.points[index].x * sX;
            double oy = s.points[index].y * sY;
            double oz = s.points[index].z * sZ;

            if (m.material != null)
                window.setTextureCoords(s.texCoords[index].x, s.texCoords[index].y);

            window.addVertex(posX + ox * x1 + oy * x2 + oz * x3, posY + ox * y1 + oy * y2 + oz * y3, posZ + ox * z1 + oy * z2 + oz * z3);
        }

        @Override
        public void drawShape(ModelPart m, Shape s, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll)
        {
            if (s instanceof Triangle)
            {
                for (int i = 0; i < s.points.length; i++)
                {
                    this.addVertex(m, s, i, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
                }
            }
            else if (s instanceof Quad)
            {
                this.addVertex(m, s, 0, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
                this.addVertex(m, s, 1, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
                this.addVertex(m, s, 2, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);

                this.addVertex(m, s, 0, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
                this.addVertex(m, s, 3, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
                this.addVertex(m, s, 2, posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll);
            }
        }

        @Override
        public void drawShape(ModelPart m, Shape s, double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw)
        {
            if (s instanceof Triangle)
            {
                for (int i = 0; i < s.points.length; i++)
                {
                    this.addVertex(m, s, i, posX, posY, posZ, sX, sY, sZ, yaw);
                }
            }
            else if (s instanceof Quad)
            {
                this.addVertex(m, s, 0, posX, posY, posZ, sX, sY, sZ, yaw);
                this.addVertex(m, s, 1, posX, posY, posZ, sX, sY, sZ, yaw);
                this.addVertex(m, s, 2, posX, posY, posZ, sX, sY, sZ, yaw);

                this.addVertex(m, s, 0, posX, posY, posZ, sX, sY, sZ, yaw);
                this.addVertex(m, s, 3, posX, posY, posZ, sX, sY, sZ, yaw);
                this.addVertex(m, s, 2, posX, posY, posZ, sX, sY, sZ, yaw);
            }
        }

        @Override
        public void drawShape(ModelPart m, Shape s, double posX, double posY, double sX, double sY, double yaw)
        {
            if (s instanceof Triangle)
            {
                for (int i = 0; i < s.points.length; i++)
                {
                    this.addVertex(m, s, i, posX, posY, sX, sY, yaw);
                }
            }
            else if (s instanceof Quad)
            {
                this.addVertex(m, s, 0, posX, posY, sX, sY, yaw);
                this.addVertex(m, s, 1, posX, posY, sX, sY, yaw);
                this.addVertex(m, s, 2, posX, posY, sX, sY, yaw);

                this.addVertex(m, s, 0, posX, posY, sX, sY, yaw);
                this.addVertex(m, s, 3, posX, posY, sX, sY, yaw);
                this.addVertex(m, s, 2, posX, posY, sX, sY, yaw);
            }
        }
    }

    public ImmediateModeModelPart(BaseWindow window)
    {
        super(window);
    }

    public ImmediateModeModelPart(BaseWindow window, Model model, ArrayList<Shape> shapes, Model.Material material)
    {
        super(window, model, shapes, material);
    }

    @Override
    public void setWindow(BaseWindow w)
    {
        this.window = w;
    }
}

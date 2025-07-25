package basewindow;

public class Color
{
    public double red;
    public double green;
    public double blue;
    public double alpha;

    public Color()
    {
        this.alpha = 255;
    }

    public Color(double r, double g, double b, double a)
    {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public Color set(Color c)
    {
        this.red = c.red;
        this.green = c.green;
        this.blue = c.blue;
        this.alpha = c.alpha;
        return this;
    }

    public Color set(double r, double g, double b)
    {
        return set(r, g, b, 255);
    }

    public Color set(double r, double g, double b, double a)
    {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
        return this;
    }

    public String toString()
    {
        return red + "/" + green + "/" + blue + "/" + alpha;
    }
}

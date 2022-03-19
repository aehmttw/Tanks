package basewindow;

public class InputPoint
{
    public double startX;
    public double startY;

    public double x;
    public double y;
    public String tag = "";
    public boolean valid = true;

    public InputPoint(double x, double y)
    {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
    }

}

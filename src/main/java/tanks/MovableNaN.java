package tanks;

public class MovableNaN extends Movable
{
    public MovableNaN(double x, double y)
    {
        super(x, y);
        Drawing.drawing.playSound("leave.ogg");
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 255);
        Drawing.drawing.fillOval(this.posX, this.posY, 100, 100);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setFontSize(60);
        Drawing.drawing.drawText(this.posX, this.posY, ":(");
    }
}

package tanks;

public class MovableNaN extends Movable
{
    public String sadFace = ":(";
    public MovableNaN(double x, double y)
    {
        super(x, y);
        Drawing.drawing.playSound("leave.ogg");

        if (Math.random() < 0.01)
        {
            int r = (int) (Math.random() * 5);

            if (r == 0)
                sadFace = ":)";
            else if (r == 1)
                sadFace = ":O";
            else if (r == 2)
                sadFace = ">:(";
            else if (r == 3)
                sadFace = ":3";
            else if (r == 4)
                sadFace = ";(";
        }

        if (Math.random() < 0.01)
            sadFace = sadFace.substring(0, sadFace.length() - 1) + "-" + sadFace.charAt(sadFace.length() - 1);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 255);
        Drawing.drawing.fillOval(this.posX, this.posY, 100, 100);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setFontSize(60);
        Drawing.drawing.drawText(this.posX, this.posY - 10, this.sadFace);

        Drawing.drawing.setFontSize(24);
        Drawing.drawing.drawText(this.posX, this.posY + 30, "NaN");

    }
}

package tanks.obstacle;

public class Face implements Comparable<Face>
{
    public double startX;
    public double startY;
    public double endX;
    public double endY;

    public boolean horizontal;
    public boolean positiveCollision;

    public boolean solidTank;
    public boolean solidBullet;

    public ISolidObject owner;

    public Face(ISolidObject o, double x1, double y1, double x2, double y2, boolean horizontal, boolean positiveCollision, boolean tank, boolean bullet)
    {
        this.owner = o;
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        this.horizontal = horizontal;
        this.positiveCollision = positiveCollision;

        this.solidTank = tank;
        this.solidBullet = bullet;
    }

    public int compareTo(Face f)
    {
        int cx = Double.compare(this.startX, f.startX);
        int cy = Double.compare(this.startY, f.startY);

        if (!horizontal)
            return cx != 0 ? cx : cy;
        return cy != 0 ? cy : cx;
    }

    public void update(double x1, double y1, double x2, double y2)
    {
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
    }

    public String toString()
    {
        if (this.horizontal)
            return this.startX + "-" + this.endX + " " + this.startY;
        else
            return this.startX + " " + this.startY + "-" + this.endY;
    }
}

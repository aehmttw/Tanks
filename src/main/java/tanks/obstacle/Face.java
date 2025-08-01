package tanks.obstacle;

import tanks.*;
import tanks.tank.*;

public class Face implements Comparable<Face>
{
    public double startX, startY, endX, endY;

    /**
     * The <code>startX</code> of a length 1 face, where index <code>i</code> of the array corresponds to
     * the {@linkplain Direction#index() direction index} of the face
     */
    public static int[] x1 = {0, 1, 0, 0};
    /**
     * The <code>startY</code> of a length 1 face, where index <code>i</code> of the array corresponds to
     * the {@linkplain Direction#index() direction index} of the face
     */
    public static int[] y1 = {0, 0, 1, 0};
    /**
     * The <code>endX</code> of a length 1 face, where index <code>i</code> of the array corresponds to
     * the {@linkplain Direction#index() direction index} of the face
     */
    public static int[] x2 = {1, 1, 1, 0};

    /**
     * The <code>endY</code> of a length 1 face, where index <code>i</code> of the array corresponds to
     * the {@linkplain Direction#index() direction index} of the face
     */
    public static int[] y2 = {0, 1, 1, 1};


    public ISolidObject owner;
    public final Direction direction;
    public boolean solidTank, solidBullet;
    public boolean valid = true;

    public Face(ISolidObject o, Direction direction, boolean tank, boolean bullet)
    {
        this.owner = o;
        this.direction = direction;
        this.solidTank = tank;
        this.solidBullet = bullet;
    }

    public Face(ISolidObject o, double x1, double y1, double x2, double y2, Direction direction, boolean tank, boolean bullet)
    {
        this(o, direction, tank, bullet);
        update(x1, y1, x2, y2, true, tank, bullet);
    }

    public static void drawDebug()
    {
        if (!Game.drawFaces)
            return;

        Drawing d = Drawing.drawing;
        for (Chunk c : Chunk.chunkList)
        {
            for (Face f : c.faces.topFaces)
            {
                if (shouldHide(f)) continue;
                d.setColor(150, 50, 50);
                d.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
            }

            for (Face f : c.faces.bottomFaces)
            {
                if (shouldHide(f)) continue;
                d.setColor(255, 50, 50);
                d.fillRect(0.5 * (f.endX + f.startX), f.startY, f.endX - f.startX, 5);
            }

            for (Face f : c.faces.leftFaces)
            {
                if (shouldHide(f)) continue;
                d.setColor(50, 50, 150);
                d.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
            }

            for (Face f : c.faces.rightFaces)
            {
                if (shouldHide(f)) continue;
                d.setColor(50, 50, 255);
                d.fillRect(f.startX, 0.5 * (f.endY + f.startY), 5, f.endY - f.startY);
            }
        }
    }

    public static boolean shouldHide(Face f)
    {
        return (f.owner instanceof Tank && (((Tank) f.owner).canHide && ((Tank) f.owner).hidden)) || (f.owner instanceof TankAIControlled && ((TankAIControlled) f.owner).invisible);
    }

    public int compareTo(Face f)
    {
        int cx = Double.compare(this.startX, f.startX);
        int cy = Double.compare(this.startY, f.startY);

        if (this.direction.isNonZeroX())
            return cx != 0 ? cx : cy;
        return cy != 0 ? cy : cx;
    }

    public void update(double x1, double y1, double x2, double y2)
    {
        update(x1, y1, x2, y2, true, this.solidTank, this.solidBullet);
    }

    public void update(double x1, double y1, double x2, double y2, boolean valid, boolean tank, boolean bullet)
    {
        this.startX = x1;
        this.startY = y1;
        this.endX = x2;
        this.endY = y2;
        this.valid = valid;
        this.solidTank = tank;
        this.solidBullet = bullet;

        validate();
    }

    public void validate()
    {
        if (!valid || (startX == endX && startY == endY))
            return;

        if (this.direction.isNonZeroY())
        {
            if (this.startX == this.endX)
                throw new RuntimeException("Face has zero width: " + this);
            if (this.startY != this.endY)
                throw new RuntimeException("Face is not horizontal: " + this);
        }
        else
        {
            if (this.startY == this.endY)
                throw new RuntimeException("Face has zero height: " + this);
            if (this.startX != this.endX)
                throw new RuntimeException("Face is not vertical: " + this);
        }
    }

    public String toString()
    {
        String ownerName = this.owner instanceof Obstacle ? ((Obstacle) this.owner).name : this.owner instanceof Tank ? ((Tank) this.owner).name : this.owner != null ? this.owner.getClass().getSimpleName() : "null";
        if (this.direction.isNonZeroY())
            return String.format("%.1f-%.1f %.1f  %s", this.startX, this.endX, this.startY, ownerName);
        else
            return String.format("%.1f %.1f-%.1f  %s", this.startX, this.startY, this.endY, ownerName);
    }
}

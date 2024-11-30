package tanks.editor;

import tanks.obstacle.Obstacle;
import tanks.tank.Tank;

import java.util.ArrayList;

public class EditorClipboard
{
    public ArrayList<Tank> tanks = new ArrayList<>();
    public ArrayList<Obstacle> obstacles = new ArrayList<>();

    public double centerX, centerY;
    public double minX;
    public double minY;
    public double maxX;
    public double maxY;

    public void add(Object o)
    {
        if (o instanceof Obstacle)
            obstacles.add((Obstacle) o);
        else
            tanks.add((Tank) o);
    }

    public void remove(Object o)
    {
        if (o instanceof Obstacle)
            obstacles.remove(o);
        else if (o instanceof Tank)
            tanks.remove(o);
    }

    public int size()
    {
        return tanks.size() + obstacles.size();
    }

    public boolean isEmpty()
    {
        return tanks.isEmpty() && obstacles.isEmpty();
    }

    public void updateParams()
    {
        minX = minY = 9999;
        maxX = maxY = -9999;

        for (Obstacle o : obstacles)
        {
            minX = Math.min(minX, o.posX);
            minY = Math.min(minY, o.posY);
            maxX = Math.max(maxX, o.posX);
            maxY = Math.max(maxY, o.posY);
        }

        for (Tank t : tanks)
        {
            minX = Math.min(minX, t.posX);
            minY = Math.min(minY, t.posY);
            maxX = Math.max(maxX, t.posX);
            maxY = Math.max(maxY, t.posY);
        }

        centerX = (maxX - minX) / 2;
        centerY = (maxY - minY) / 2;

        for (Obstacle o : obstacles)
        {
            o.posX -= minX;
            o.posY -= minY;
        }

        for (Tank t : tanks)
        {
            t.posX -= minX;
            t.posY -= minY;
        }
    }

    public void flipHorizontal()
    {
        for (Obstacle o : obstacles)
            o.posX = -(o.posX - centerX) + centerX;

        for (Tank t : tanks)
        {
            t.posX = -(t.posX - centerX) + centerX;
            if (t.angle % Math.PI == 0)
                t.angle = (t.angle + Math.PI) % (Math.PI * 2);
        }
    }

    public void flipVertical()
    {
        for (Obstacle o : obstacles)
            o.posY = -(o.posY - centerY) + centerY;

        for (Tank t : tanks)
        {
            t.posY = -(t.posY - centerY) + centerY;
            if (t.angle % Math.PI == Math.PI / 2)
                t.angle = (t.angle + Math.PI) % (Math.PI * 2);
        }
    }

    public void rotate()
    {
        for (Obstacle o : obstacles)
        {
            double x = o.posX - centerX, y = o.posY - centerY;
            o.posX = -y + centerX;
            o.posY = x + centerY;
        }

        for (Tank t : tanks)
        {
            double x = t.posX - centerX, y = t.posY - centerY;
            t.posX = -y + centerX;
            t.posY = x + centerY;
            t.angle += Math.PI / 2;
        }

        updateParams();
    }
}
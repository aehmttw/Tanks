package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.network.event.EventObstacleHit;

public class ObstacleBreakable extends ObstacleStackable
{
    public double fallAnimation = 0;
    public double lastFallAnimation = 0;

    public ObstacleBreakable(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.checkForObjects = true;
        this.shouldShootThrough = true;
        this.description = "A block which breaks when hit by a bullet";
    }

    public double[] getRandomColor()
    {
        double frac = Math.random() * 0.2 + 0.8;

        if (!Game.fancyTerrain)
            frac = 0.9;

        double[] col = new double[3];

        col[0] = frac * 246;
        col[1] = frac * 206;
        col[2] = frac * 135;

        return col;
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (m instanceof Bullet && this.fallAnimation <= 0)
        {
            this.reactToHit(m.posX, m.posY);
            Game.eventsOut.add(new EventObstacleHit(this, (Bullet) m));
        }
    }

    @Override
    public void reactToHit(double bx, double by)
    {
        this.setUpdate(true);
        double height = this.stackHeight;
        this.fallAnimation = 100;

        this.stackHeight = Math.min(this.stackHeight, 1);

        for (int i = 0; i < this.stackColorR.length - 1; i++)
        {
            this.stackColorR[i] = this.stackColorR[i + 1];
            this.stackColorG[i] = this.stackColorG[i + 1];
            this.stackColorB[i] = this.stackColorB[i + 1];
        }

        this.playDestroyAnimation(bx - (this.posX - bx) * 2, by - (this.posY - by) * 2, Game.tile_size);

        this.stackHeight = height - 1;

        if (this.stackHeight <= 0)
            Game.removeObstacles.add(this);
    }

    @Override
    public void update()
    {
        this.lastFallAnimation = this.fallAnimation;
        this.fallAnimation = Math.max(0, this.fallAnimation - Panel.frameFrequency * 2);

        if (this.fallAnimation != this.lastFallAnimation)
            Game.redrawObstacles.add(this);

        if (this.fallAnimation <= 0)
            this.setUpdate(false);
    }

    @Override
    public void draw()
    {
        if (this.stackHeight <= 0)
            return;

        Drawing drawing = Drawing.drawing;

        drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        double prevStackHeight = stackHeight;
        stackHeight = getTileHeight() / Game.tile_size;

        if (Game.enable3d)
            drawStacks();
        else
            drawing.fillRect(this, this.posX, this.posY, draw_size, draw_size);

        stackHeight = prevStackHeight;
    }

    public double getTileHeight()
    {
        if (Obstacle.draw_size < Game.tile_size)
            return 0;

        return (this.stackHeight + Math.pow(this.fallAnimation / 100, 2)) * Game.tile_size;
    }
}

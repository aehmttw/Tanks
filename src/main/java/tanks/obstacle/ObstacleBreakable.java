package tanks.obstacle;

import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.network.event.EventObstacleHit;

public class ObstacleBreakable extends Obstacle
{
    public double fallAnimation = 0;
    public double lastFallAnimation = 0;

    public ObstacleBreakable(String name, double posX, double posY)
    {
        super(name, posX, posY);

        double frac = Math.random() * 0.2 + 0.8;

        if (!Game.fancyTerrain)
            frac = 0.9;

        this.colorR = 246 * frac;
        this.colorG = 206 * frac;
        this.colorB = 135 * frac;

        this.checkForObjects = true;
        this.update = true;

        for (int i = 0; i < default_max_height; i++)
        {
            double frac2 = Math.random() * 0.2 + 0.8;

            if (!Game.fancyTerrain)
                frac2 = 0.9;

            this.stackColorR[i] = 246 * frac2;
            this.stackColorG[i] = 206 * frac2;
            this.stackColorB[i] = 135 * frac2;
        }

        this.shouldShootThrough = true;
        this.description = "A block which breaks when hit by a bullet";
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
        double height = this.stackHeight;
        this.fallAnimation = 100;

        this.stackHeight = Math.min(this.stackHeight, 1);

        double r = this.stackColorR[0];
        double g = this.stackColorG[0];
        double b = this.stackColorB[0];

        this.stackColorR[0] = this.stackColorR[this.stackColorR.length - 1];
        this.stackColorG[0] = this.stackColorG[this.stackColorR.length - 1];
        this.stackColorB[0] = this.stackColorB[this.stackColorR.length - 1];

        this.playDestroyAnimation(bx - (this.posX - bx) * 2, by - (this.posY - by) * 2, Game.tile_size);

        this.stackColorR[0] = r;
        this.stackColorG[0] = g;
        this.stackColorB[0] = b;

        for (int i = this.stackColorR.length - 1; i > 0; i--)
        {
            this.stackColorR[i] = this.stackColorR[i - 1];
            this.stackColorG[i] = this.stackColorG[i - 1];
            this.stackColorB[i] = this.stackColorB[i - 1];
        }

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
        //this.allowBounce = this.fallAnimation <= 0;
    }

    @Override
    public void draw()
    {
        if (this.stackHeight <= 0)
            return;

        Drawing drawing = Drawing.drawing;

        drawing.setColor(this.colorR, this.colorG, this.colorB, this.colorA);

        double offset = Math.pow(this.fallAnimation / 100, 2) * Game.tile_size;

        if (Game.enable3d)
        {
            for (int i = 0; i < Math.min(this.stackHeight, Obstacle.default_max_height); i++)
            {
                int in = default_max_height - 1 - i;
                drawing.setColor(this.stackColorR[in], this.stackColorG[in], this.stackColorB[in], this.colorA);

                byte option = 0;

//                if (Obstacle.draw_size >= Game.tile_size)
//                {
//                    if (i > 0)
//                        option += 1;
//
//                    if (i < Math.min(this.stackHeight, Obstacle.default_max_height) - 1)
//                        option += 2;
//                }

                double cutoff = -Math.min((i - 1 + stackHeight % 1.0) * Game.tile_size, 0);

                byte o;

                if (stackHeight % 1 == 0)
                {
                    o = (byte) (option | this.getOptionsByte(((i + 1) + stackHeight % 1.0) * Game.tile_size + offset));
                    drawing.fillBox(this, this.posX, this.posY, offset + i * Game.tile_size, draw_size, draw_size, draw_size, o);
                }
                else
                {
                    o = (byte) (option | this.getOptionsByte((i + stackHeight % 1.0) * Game.tile_size + offset));
                    drawing.fillBox(this, this.posX, this.posY, offset + (i - 1 + stackHeight % 1.0) * Game.tile_size + cutoff, draw_size, draw_size, draw_size - cutoff, o);
                }

                options[i] = o;
            }
        }
        else
            drawing.fillRect(this, this.posX, this.posY, draw_size, draw_size);
    }

    public double getTileHeight()
    {
        if (Obstacle.draw_size < Game.tile_size)
            return 0;

        return this.stackHeight * Game.tile_size - Math.pow(this.fallAnimation / 100, 2) * Game.tile_size;
    }

    public void drawTile(double r, double g, double b, double d, double extra)
    {
        if (Obstacle.draw_size < Game.tile_size || extra != 0 || this.fallAnimation > 0)
        {
            Drawing.drawing.setColor(r, g, b);
            Drawing.drawing.fillBox(this, this.posX, this.posY, -extra, Game.tile_size, Game.tile_size, extra + d * (1 - Obstacle.draw_size / Game.tile_size));
        }
    }
}

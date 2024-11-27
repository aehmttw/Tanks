package tanks.bullet;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemBullet;
import tanks.network.event.EventAddObstacleBullet;
import tanks.obstacle.Obstacle;
import tanks.tank.Crate;
import tanks.tank.Tank;

public class BulletBlock extends BulletArc
{
    public static String bullet_class_name = "block";

    public BulletBlock()
    {
        this.init();
        this.typeName = bullet_class_name;
        this.respectXRay = false;
    }

    public BulletBlock(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet ib)
    {
        super(x, y, t, affectsMaxLiveBullets, ib);
        this.init();
        this.typeName = bullet_class_name;
        this.respectXRay = false;
    }

    @Override
    public void setTargetLocation(double x, double y)
    {
        double angle = Math.random() * Math.PI * 2;
        double dx = x - this.posX;
        double dy = y - this.posY;
        double d = Math.sqrt(dx * dx + dy * dy);
        double s = Math.abs(this.speed);

        if (d > this.maxRange && this.maxRange > 0)
        {
            dx *= this.maxRange / d;
            dy *= this.maxRange / d;
            d = this.maxRange;
        }

        if (d < this.minRange)
        {
            dx *= this.minRange / d;
            dy *= this.minRange / d;
            d = this.minRange;
        }

        double offset = Math.random() * this.accuracySpreadCircle * d;

        double f = 1;
        if (d / s < this.minAirTime)
            f = d / (s * this.minAirTime);

        dx += Math.sin(angle) * offset;
        dy += Math.cos(angle) * offset;

        dx = (Math.min(Math.max((int)((this.posX + dx) / Game.tile_size), 0), Game.currentSizeX - 1) + 0.5) * Game.tile_size - this.posX;
        dy = (Math.min(Math.max((int)((this.posY + dy) / Game.tile_size), 0), Game.currentSizeY - 1) + 0.5) * Game.tile_size - this.posY;

        d = Math.sqrt(dx * dx + dy * dy);

        this.setMotionInDirection(this.posX + dx, this.posY + dy, s * f);
        this.speed *= f;
        this.vZ = d / s * 0.5 * BulletArc.gravity / f;
    }

    @Override
    public void onDestroy()
    {
        int x = (int) (this.posX / Game.tile_size);
        int y = (int) (this.posY / Game.tile_size);

        Obstacle o = new Obstacle("normal", x, y);
        o.colorR = this.outlineColorR;
        o.colorG = this.outlineColorG;
        o.colorB = this.outlineColorB;

        for (int i = 0; i < o.stackColorR.length; i++)
        {
            o.stackColorR[i] = this.outlineColorR;
            o.stackColorG[i] = this.outlineColorG;
            o.stackColorB[i] = this.outlineColorB;
        }

        o.update = true;
        o.shouldClip = true;
        o.clipFrames = 2;

        if (ScreenGame.finishedQuick)
            return;

        boolean found = x < 0 || y < 0 || x >= Game.currentSizeX || y >= Game.currentSizeY;

        if (!found)
        {
            for (Obstacle o1 : Game.obstacles)
            {
                if (o1.posX == o.posX && o1.posY == o.posY)
                {
                    found = true;
                    break;
                }
            }
        }

        if (!found)
            Game.addObstacle(o);
        else
        {
            Drawing.drawing.playGlobalSound("break_block.ogg");
            o.playDestroyAnimation(this.posX, this.posY, Game.tile_size);
        }

        Game.eventsOut.add(new EventAddObstacleBullet(o, !found));
    }

    @Override
    public void drawCursor(double frac, double x, double y)
    {
        double f2 = Math.max(0, (this.maxDestroyTimer - this.destroyTimer) / this.maxDestroyTimer);
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, frac * f2 * 255);
        Crate.fillOutlineRect(x, y, Game.tile_size * (2 - frac) * f2);
        Crate.fillOutlineRect(x, y, Game.tile_size * (frac) * f2);
    }

    @Override
    public void drawShadow()
    {

    }

    public void playArcPop()
    {
        Drawing.drawing.playGlobalSound("place_block.ogg");
    }


    @Override
    public void draw()
    {
        super.draw();

        if (this.delay > 0)
            return;

        double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;
        double size = Math.max(this.size, Game.tile_size - time / 2);
        if (this.destroy)
        {
            size = Game.tile_size * Math.max((this.maxDestroyTimer - this.destroyTimer) / this.maxDestroyTimer, 0);
        }

        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB);
        Drawing.drawing.fillBox(this.posX, this.posY, this.posZ - size / 2, size, size, size);
    }

    @Override
    public void addDestroyEffect()
    {

    }
}

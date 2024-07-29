package tanks.bullet.legacy;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.item.ItemBullet;
import tanks.network.event.EventBulletUpdateTarget;
import tanks.tank.Ray;
import tanks.tank.Tank;

public class BulletHoming extends Bullet
{
    public static String bullet_name = "homing";
    public Tank target = null;
    public Tank prevTarget = null;
    public double targetTime = 0;

    public BulletHoming(double x, double y, int bounces, Tank t, ItemBullet item)
    {
        super(x, y, bounces, t, item);
        this.name = bullet_name;
    }

    public void update()
    {
        Tank nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Movable m: Game.movables)
        {
            if (m instanceof Tank && !Team.isAllied(this, m) && !m.destroy)
            {
                Tank t = (Tank) m;
                double d = Movable.distanceBetween(this, m);

                if (d < nearestDist)
                {
                    nearestDist = d;
                    nearest = t;
                }
            }
        }

        double s = this.getSpeed();

        if (!this.isRemote)
        {
            this.target = null;
            if (nearest != null && !this.destroy)
            {
                double a = this.getAngleInDirection(nearest.posX, nearest.posY);
                Ray r = new Ray(this.posX, this.posY, a, 0, this.tank);

                if (r.getTarget() == nearest)
                {
                    this.addPolarMotion(a, Panel.frameFrequency / 5.5);
                    double s2 = this.getSpeed();
                    this.vX *= s / s2;
                    this.vY *= s / s2;

                    this.target = nearest;
                }
            }

            if (this.target != prevTarget)
            {
                Game.eventsOut.add(new EventBulletUpdateTarget(this));
            }
        }

        if (this.target != null)
        {
            double nX = this.vX / s;
            double nY = this.vY / s;

            if (Game.playerTank != null && !Game.playerTank.destroy && !ScreenGame.finishedQuick)
            {
                double d = Movable.distanceBetween(this, Game.playerTank);

                if (d <= 500)
                {
                    double dX = (this.posX - Game.playerTank.posX) / d;
                    double dY = (this.posY - Game.playerTank.posY) / d;

                    double v = 1 + ((nX * dX + nY * dY) * this.speed) / 15.0;

                    Drawing.drawing.playSound("wind.ogg", (float) (0.8 / (float) v + Math.random() * 0.1f - 0.05f), (float) Math.min(1, 100 / d));
                }
            }

            if (Game.bulletTrails && Math.random() < Panel.frameFrequency)
            {
                Effect e = Effect.createNewEffect(this.posX, this.posY, this.posZ, Effect.EffectType.piece);
                double var = 50;
                e.maxAge /= 2;

                double r1 = 255;
                double g1 = 120;
                double b1 = 0;

                e.colR = Math.min(255, Math.max(0, r1 + Math.random() * var - var / 2));
                e.colG = Math.min(255, Math.max(0, g1 + Math.random() * var - var / 2));
                e.colB = Math.min(255, Math.max(0, b1 + Math.random() * var - var / 2));

                if (Game.enable3d)
                    e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);
                else
                    e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() * this.size / 50.0 * 4);

                Game.effects.add(e);
            }

            this.targetTime += Panel.frameFrequency;
        }

        if (prevTarget != this.target)
            this.targetTime = 0;

        this.prevTarget = this.target;

        super.update();
    }

    public void draw()
    {
        super.draw();

        double limit = 50;
        if (!ScreenGame.finishedQuick && this.target != null)
        {
            double frac;

            frac = Math.min(targetTime / limit, 1);

            double s = (2 - frac) * 80;
            double d = Math.min((1 - this.destroyTimer / this.maxDestroyTimer) * 2, 1);

            Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, frac * 255 * d, 1);
            Drawing.drawing.drawImage(frac * Math.PI / 2 + this.getAngleInDirection(this.target.posX, this.target.posY), "cursor.png", this.target.posX, this.target.posY, s, s);

            if (Game.glowEnabled)
            {
                Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, frac * 255 * d, 1);
                Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 16, this.size * 16);
            }
        }
        Drawing.drawing.setColor(this.baseColorR, this.baseColorG, this.baseColorB, 255, 1);
    }
}

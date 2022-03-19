package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class ObstaclePath extends Obstacle
{
    public ObstaclePath(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.tankCollision = false;
        this.bulletCollision = false;
        this.destructible = false;
        this.checkForObjects = true;

        this.colorR = 140;
        this.colorG = 60;
        this.colorB = 30;

        this.description = "A dusty dirt path";
    }

    @Override
    public void draw()
    {
        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
            Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
    }

    @Override
    public void drawTile(double r, double g, double b, double depth, double extra)
    {
        Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
        Drawing.drawing.fillBox(this.posX, this.posY, 0, Game.tile_size, Game.tile_size, depth);
    }

    @Override
    public double getTileHeight() { return getGroundHeight(); }

    @Override
    public double getGroundHeight() { return Game.sampleGroundHeight(this.posX, this.posY); }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && m instanceof Tank && !ScreenGame.finished && Math.random() * Panel.frameFrequency <= Game.effectMultiplier / 5)
        {
            Tank t = (Tank) m;

            double a = m.getPolarDirection();
            Effect e1 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            Effect e2 = Effect.createNewEffect(m.posX, m.posY, Effect.EffectType.piece);
            e1.drawLayer = 1;
            e2.drawLayer = 1;
            e1.setPolarMotion(a - Math.PI / 2, t.size * 0.25);
            e2.setPolarMotion(a + Math.PI / 2, t.size * 0.25);
            e1.size = t.size / 5;
            e2.size = t.size / 5;
            e1.posX += e1.vX;
            e1.posY += e1.vY;
            e2.posX += e2.vX;
            e2.posY += e2.vY;
            e1.angle = a;
            e2.angle = a;
            e1.setPolarMotion(0, 0);
            e2.setPolarMotion(0, 0);

            double var = 20;
            e1.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e1.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e1.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            e2.colR = Math.min(255, Math.max(0, this.colorR - 20 + Math.random() * var - var / 2));
            e2.colG = Math.min(255, Math.max(0, this.colorG - 20 + Math.random() * var - var / 2));
            e2.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

            double angle = t.getPolarDirection() + Math.PI / 2;

            e1.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e1.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e1.vZ = Math.sqrt(t.vX * t.vX + t.vY * t.vY) / 2;
            e1.addPolarMotion(angle, (Math.random() - 0.5) * e1.vZ);

            e2.vX = -t.vX / 2 * (Math.random() * 0.6 + 0.7);
            e2.vY = -t.vY / 2 * (Math.random() * 0.6 + 0.7);
            e2.vZ = e1.vZ;
            e2.addPolarMotion(angle, (Math.random() - 0.5) * e2.vZ);

            e1.vZ *= (Math.random() * 0.6 + 0.4);
            e2.vZ *= (Math.random() * 0.6 + 0.4);

            e1.maxAge = 100 + Math.random() * 50;
            e2.maxAge = 100 + Math.random() * 50;

            Game.effects.add(e1);
            Game.effects.add(e2);
        }
    }
}

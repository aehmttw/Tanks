package tanks.obstacle;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.screen.ScreenGame;
import tanks.tank.Tank;

public class ObstacleBoostPanel extends Obstacle
{
    public double brightness = 0;
    public Effect glow;

    public ObstacleBoostPanel(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.drawLevel = 1;

        this.destructible = false;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;

        this.isSurfaceTile = true;
        this.update = true;

        this.colorR = 255;
        this.colorG = 180;
        this.colorB = 0;

        glow = Effect.createNewEffect(this.posX, this.posY, 0, Effect.EffectType.boostLight);

        this.description = "A panel which speeds---up tanks and bullets";
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        this.brightness = Math.min(this.brightness + Panel.frameFrequency * 8, 100);

        boolean effect = true;
        for (AttributeModifier am : m.attributes)
        {
            if (am.name.equals("boost_glow") && am.age < am.deteriorationAge)
                effect = false;
        }

        if (Game.playerTank != null && !Game.playerTank.destroy && effect && !(m instanceof Bullet && !((Bullet) m).playPopSound))
        {
            double distsq = Math.pow(this.posX - Game.playerTank.posX, 2) + Math.pow(this.posY - Game.playerTank.posY, 2);

            double radius = 250000;
            if (distsq <= radius)
            {
                Drawing.drawing.playSound("boost.ogg", 1, (float) ((radius - distsq) / radius));
            }
        }

        if (Game.effectsEnabled && !ScreenGame.finished && !(m instanceof Bullet && !((Bullet) m).playPopSound))
        {
            if (effect)
            {
                for (int i = 0; i < 25 * Game.effectMultiplier; i++)
                {
                    this.addEffect(m.posX, m.posY, 0.5);
                }
            }
            else if (Math.random() < Panel.frameFrequency * Game.effectMultiplier * 0.25)
                this.addEffect(m.posX, m.posY, 0);
        }
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        this.onObjectEntryLocal(m);

        double d = 1;

        if (m instanceof Tank)
            d = 3;

        AttributeModifier c = new AttributeModifier("boost_speed", "velocity", AttributeModifier.Operation.multiply, d);
        c.duration = 50;
        c.deteriorationAge = 10;
        m.addUnduplicateAttribute(c);

        AttributeModifier a = new AttributeModifier("boost_glow", "glow", AttributeModifier.Operation.multiply, 1);
        a.duration = 50;
        a.deteriorationAge = 10;
        m.addUnduplicateAttribute(a);

        AttributeModifier b = new AttributeModifier("boost_slip", "friction", AttributeModifier.Operation.multiply, -0.75);
        b.duration = 50;
        b.deteriorationAge = 10;
        m.addUnduplicateAttribute(b);
    }

    @Override
    public void draw()
    {
        double offset = 0;

        if (Game.fancyTerrain)
            offset = Math.sin((this.posX + this.posY + System.currentTimeMillis() / 50.0) / 10) * 40 + 40;

        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR - offset / 2, Math.min(this.colorG - offset + this.brightness, 255), this.colorB + this.brightness, 255, 1.0);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
        else
        {
            Drawing.drawing.setColor(this.colorR - offset / 2, Math.min(this.colorG - offset + this.brightness, 255), this.colorB + this.brightness, 255, 1.0);
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, 10);

            if (Game.glowEnabled)
            {
                glow.posX = this.posX;
                glow.posY = this.posY;
                glow.size = this.brightness;

                if (Game.screen instanceof ScreenGame)
                    ((ScreenGame) Game.screen).drawables[9].add(glow);
            }
        }
    }

    public void update()
    {
        this.brightness = Math.max(this.brightness - Panel.frameFrequency, 0);
    }

    public void addEffect(double x, double y, double extra)
    {
        Effect e = Effect.createNewEffect(x, y, Game.tile_size / 2, Effect.EffectType.piece);
        double var = 50;

        e.colR = Math.min(255, Math.max(0, this.colorR + Math.random() * var - var / 2));
        e.colG = Math.min(255, Math.max(0, this.colorG + Math.random() * var - var / 2));
        e.colB = Math.min(255, Math.max(0, this.colorB + Math.random() * var - var / 2));

        if (Game.enable3d)
            e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() + extra);
        else
            e.setPolarMotion(Math.random() * 2 * Math.PI, Math.random() + extra);

        Game.effects.add(e);
    }

    public double getTileHeight()
    {
        return 0;
    }

    public boolean colorChanged()
    {
        return !Drawing.drawing.isOutOfBounds(Drawing.drawing.gameToAbsoluteX(this.posX, Obstacle.draw_size), Drawing.drawing.gameToAbsoluteY(this.posY, Obstacle.draw_size));
    }

    public double getGroundHeight()
    {
        return 10;
    }
}

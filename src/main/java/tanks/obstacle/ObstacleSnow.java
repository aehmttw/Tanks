package tanks.obstacle;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventObstacleSnowMelt;
import tanks.gui.screen.ILevelPreviewScreen;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.tank.Tank;

public class ObstacleSnow extends Obstacle
{
    public double depth = 1;

    public double baseColorR;
    public double baseColorG;
    public double baseColorB;

    public double visualDepth = 1;

    public ObstacleSnow(String name, double posX, double posY)
    {
        super(name, posX, posY);

        if (Game.enable3d)
            this.drawLevel = 1;
        else
            this.drawLevel = 9;

        this.destructible = true;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;
        this.destroyEffect = Effect.EffectType.snow;

        double darkness = Math.random() * 20;
        this.colorR = 255 - darkness;
        this.colorG = 255 - darkness * 0.75;
        this.colorB = 255 - darkness * 0.5;
        this.baseColorR = this.colorR;
        this.baseColorG = this.colorG;
        this.baseColorB = this.colorB;

        this.description = "A thick, melting pile of snow that---slows tanks and bullets down";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (!ScreenPartyLobby.isClient && (m instanceof Tank || m instanceof Bullet))
        {
            AttributeModifier a = new AttributeModifier("snow_velocity", "velocity", AttributeModifier.Operation.multiply, -0.25);
            a.duration = 30;
            a.deteriorationAge = 20;
            m.addUnduplicateAttribute(a);

            AttributeModifier b = new AttributeModifier("show_friction", "friction", AttributeModifier.Operation.multiply, 4);
            b.duration = 10;
            b.deteriorationAge = 5;
            m.addUnduplicateAttribute(b);

            this.depth -= Panel.frameFrequency * 0.005;

            if (this.depth <= 0)
                Game.removeObstacles.add(this);

            Game.eventsOut.add(new EventObstacleSnowMelt(this.posX, this.posY, this.depth));
        }

        this.onObjectEntryLocal(m);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.fancyGraphics && !ScreenGame.finished)
        {
            double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));

            double mul = 0.25;

            if (m instanceof Bullet)
                mul *= 0.25;

            double amt = speed * mul * Panel.frameFrequency;

            if (amt < 1 && Math.random() < amt % 1)
                amt += 1;

            for (int i = 0; i < amt; i++)
            {
                Effect e = Effect.createNewEffect(m.posX, m.posY, m.posZ, Effect.EffectType.snow);
                e.colR = this.colorR;
                e.colG = this.colorG;
                e.colB = this.colorB;
                e.glowR = e.colR;
                e.glowG = e.colG;
                e.glowB = e.colB;
                e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, Math.random() * speed / 2);
                e.vX += m.vX;
                e.vY += m.vY;
                Game.effects.add(e);
            }
        }
    }

    @Override
    public void draw()
    {
        this.visualDepth = Math.min(this.visualDepth + Panel.frameFrequency / 255, 1);

        if (Game.screen instanceof ILevelPreviewScreen || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
        {
            this.visualDepth = 0.5;
        }

        if (ScreenGame.finishedQuick)
        {
            this.visualDepth = Math.max(0.5, this.visualDepth - Panel.frameFrequency / 127);
        }

        this.colorR = this.baseColorR * (this.depth + 4) / 5;
        this.colorG = this.baseColorG * (this.depth + 3) / 4;
        this.colorB = this.baseColorB * (this.depth + 2) / 3;

        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.depth * this.visualDepth * 255);
            Drawing.drawing.fillRect(this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
        else
        {
            double base = Game.sampleHeight(this.posX, this.posY);
            double z = this.depth * 0.8 * (Obstacle.draw_size - base);

            if (z > 0)
            {
                Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB);
                Drawing.drawing.fillBox(this.posX, this.posY, Game.sampleHeight(this.posX, this.posY), Game.tile_size, Game.tile_size, z * this.visualDepth);
            }
        }
    }
}

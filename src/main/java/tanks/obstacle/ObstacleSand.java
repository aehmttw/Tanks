package tanks.obstacle;

import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

public class ObstacleSand extends Obstacle
{
    public ObstacleSand(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.destructible = true;
        this.tankCollision = false;
        this.bulletCollision = false;
        this.checkForObjects = true;
        this.enableStacking = false;
        this.destroyEffect = Effect.EffectType.snow;
        this.destroyEffectAmount = 1.5;

        if (Math.random() > 0.1)
            this.stackHeight = 0.4;
        else
            this.stackHeight = 0.55;

        if (Game.fancyTerrain)
        {
            this.colorR = 233 - Math.random() * 20;
            this.colorG = 215 - Math.random() * 20;
            this.colorB = 188 - Math.random() * 20;
        }
        else
        {
            this.colorR = 233;
            this.colorG = 215;
            this.colorB = 188;
        }

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = this.colorR;
            this.stackColorB[i] = this.colorB;
            this.stackColorG[i] = this.colorG;
        }

        this.description = "A thick patch of sand---that slows tanks down";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (!ScreenPartyLobby.isClient && m instanceof Tank)
        {
            AttributeModifier a = new AttributeModifier("sand_velocity", "velocity", AttributeModifier.Operation.multiply, -0.25);
            a.duration = 30;
            a.deteriorationAge = 20;
            m.addUnduplicateAttribute(a);

            if (!(m instanceof TankAIControlled))
            {
                AttributeModifier b = new AttributeModifier("sand_friction", "friction", AttributeModifier.Operation.multiply, 4);
                b.duration = 10;
                b.deteriorationAge = 5;
                m.addUnduplicateAttribute(b);
            }
        }

        this.onObjectEntryLocal(m);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && !ScreenGame.finished && m instanceof Tank)
        {
            double speed = Math.sqrt((Math.pow(m.vX, 2) + Math.pow(m.vY, 2)));

            double mul = 0.0625 / 4;

            double amt = speed * mul * Panel.frameFrequency * Game.effectMultiplier;

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
                e.set3dPolarMotion((Math.random() - 0.5) * 2 * Math.PI, (Math.random() - 0.5) * Math.PI, Math.random() * speed);
                e.vX += m.vX;
                e.vY += m.vY;
                e.enableGlow = false;
                Game.effects.add(e);
            }
        }
    }

    public double getTileHeight()
    {
        return this.stackHeight;
    }
}

package tanks.obstacle;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.effect.AttributeModifier;
import tanks.effect.EffectManager;
import tanks.effect.StatusEffect;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.event.EventObstacleBoostPanelEffect;
import tanks.rendering.ShaderBoostPanel;
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

        this.type = ObstacleType.ground;

        this.colorR = 255;
        this.colorG = 180;
        this.colorB = 0;

        this.renderer = ShaderBoostPanel.class;

        glow = Effect.createNewEffect(this.posX, this.posY, 0, Effect.EffectType.boostLight);

        this.description = "A panel which speeds up tanks and bullets";
    }

    @Override
    public void draw3dOutline(double r, double g, double b, double a)
    {
        Drawing.drawing.setColor(r, g, b);
        Drawing.drawing.fillBox(this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, 10);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (ScreenGame.finishedQuick)
            return;

        this.brightness = Math.min(this.brightness + Panel.frameFrequency * 8, 100);

        if (Math.random() < Panel.frameFrequency * Game.effectMultiplier * 0.25)
            this.addEffect(m.posX, m.posY, 0);
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (ScreenGame.finishedQuick)
            return;

        setUpdate(true);

        this.onObjectEntryLocal(m);

        EffectManager effectManager = m.em();
        AttributeModifier am = effectManager.getAttribute(AttributeModifier.glow);

        boolean effect = am == null || (am.age >= am.deteriorationAge && am.deteriorationAge > 0);

        if (effect)
            addEntryEffect(m);

        if (m instanceof Tank)
            effectManager.addStatusEffect(StatusEffect.boost_tank, 0, 10, 50);
        else
            effectManager.addStatusEffect(StatusEffect.boost_bullet, 0, 10, 50);
    }

    public void addEntryEffect(Movable m)
    {
        if (ScreenPartyHost.isServer && (m instanceof Bullet || m instanceof Tank))
            Game.eventsOut.add(new EventObstacleBoostPanelEffect(m, this));

        if (Game.playerTank != null && !Game.playerTank.destroy && !(m instanceof Bullet && !((Bullet) m).playPopSound))
        {
            double distsq = Math.pow(this.posX - Game.playerTank.posX, 2) + Math.pow(this.posY - Game.playerTank.posY, 2);

            double radius = 250000;
            if (distsq <= radius)
                Drawing.drawing.playSound("boost.ogg", 1, (float) ((radius - distsq) / radius));
        }

        if (Game.effectsEnabled && !ScreenGame.finished && !(m instanceof Bullet && !((Bullet) m).playPopSound))
        {
            for (int i = 0; i < 25 * Game.effectMultiplier; i++)
                this.addEffect(m.posX, m.posY, 0.5);
        }
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
            Drawing.drawing.setColor(255, this.brightness, 0, (this.posX / Game.tile_size + this.posY / Game.tile_size) % 255);
            //Drawing.drawing.setColor(this.colorR - offset / 2, Math.min(this.colorG - offset + this.brightness, 255), this.colorB + this.brightness, 255, 1.0);
            Drawing.drawing.fillBox(this, this.posX, this.posY, 0, Obstacle.draw_size, Obstacle.draw_size, 10);
        }
    }

    public void update()
    {
        double prevBrightness = this.brightness;
        this.brightness = Math.max(this.brightness - Panel.frameFrequency, 0);

        if (prevBrightness != brightness)
            Game.redrawObstacles.add(this);
        else
            setUpdate(false);
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

    public double getGroundHeight()
    {
        return 10;
    }

}

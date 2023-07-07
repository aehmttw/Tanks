package tanks.obstacle;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.network.event.EventObstacleSnowMelt;
import tanks.gui.screen.*;
import tanks.tank.Tank;
import tanks.tank.TankAIControlled;

public class ObstacleSnow extends Obstacle
{
    public double depth = 1;

    public double baseColorR;
    public double baseColorG;
    public double baseColorB;

    public double visualDepth = 1;

    protected double finalHeight;
    protected double previousFinalHeight;

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
        this.destroyEffectAmount = 0.25;
        this.replaceTiles = false;

        double darkness = Math.random() * 20;

        if (!Game.fancyTerrain)
            darkness = 10;

        this.colorR = 255 - darkness;
        this.colorG = 255 - darkness * 0.75;
        this.colorB = 255 - darkness * 0.5;
        this.baseColorR = this.colorR;
        this.baseColorG = this.colorG;
        this.baseColorB = this.colorB;

        this.description = "A thick, melting pile of snow that slows tanks and bullets down";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (!ScreenPartyLobby.isClient && (m instanceof Tank || m instanceof Bullet))
        {
            m.addStatusEffect(StatusEffect.snow_velocity, 0, 20, 30);
            m.addStatusEffect(StatusEffect.snow_friction, 0, 5, 10);

            int amt = 5;
            int lastDepth = (int) Math.ceil(this.depth * amt);
            this.depth -= Panel.frameFrequency * 0.005;

            if (this.depth <= 0)
                Game.removeObstacles.add(this);

            if (lastDepth > Math.ceil(this.depth * amt))
                Game.eventsOut.add(new EventObstacleSnowMelt(this.posX, this.posY, this.depth));
        }

        this.onObjectEntryLocal(m);
    }

    @Override
    public void onObjectEntryLocal(Movable m)
    {
        if (Game.effectsEnabled && !ScreenGame.finished)
        {
            if (ScreenPartyLobby.isClient)
                this.depth = Math.max(0.05, this.depth - Panel.frameFrequency * 0.005);

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
        if (!Game.game.window.shapeRenderer.supportsBatching)
        {
            if (Game.screen instanceof ScreenGame && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient || !((ScreenGame) Game.screen).paused))
                this.visualDepth = Math.min(this.visualDepth + Panel.frameFrequency / 255, 1);

            if (Game.screen instanceof ILevelPreviewScreen || Game.screen instanceof IOverlayScreen || Game.screen instanceof ScreenGame && (!((ScreenGame) Game.screen).playing))
            {
                this.visualDepth = 0.5;
            }

            if (ScreenGame.finishedQuick && Game.screen instanceof ScreenGame && (ScreenPartyHost.isServer || ScreenPartyLobby.isClient || !((ScreenGame) Game.screen).paused))
            {
                this.visualDepth = Math.max(0.5, this.visualDepth - Panel.frameFrequency / 127);
            }
        }

        this.colorR = this.baseColorR * (this.depth + 4) / 5;
        this.colorG = this.baseColorG * (this.depth + 3) / 4;
        this.colorB = this.baseColorB * (this.depth + 2) / 3;

        if (!Game.enable3d)
        {
            Drawing.drawing.setColor(this.colorR, this.colorG, this.colorB, this.depth * this.visualDepth * 255);
            Drawing.drawing.fillRect(this, this.posX, this.posY, Obstacle.draw_size, Obstacle.draw_size);
        }
        else
        {
            double mul = 1;

            if (Game.game.window.shapeRenderer.supportsBatching && Obstacle.draw_size > 0 && Obstacle.draw_size < Game.tile_size)
                mul = 2;

            double base = this.baseGroundHeight;
            double z = Math.max(this.depth * 0.8 * (Obstacle.draw_size - base * (mul - 1)), 0);

            this.finalHeight = 0;

            if (z > 0)
            {
                this.finalHeight = z * this.visualDepth;
                int x = Math.min(Game.currentSizeX - 1, (int) Math.max(0, this.posX / Game.tile_size));
                int y = Math.min(Game.currentSizeY - 1, (int) Math.max(0, this.posY / Game.tile_size));

                double r = Game.tilesR[x][y];
                double g = Game.tilesG[x][y];
                double b = Game.tilesB[x][y];

                if (!Game.fancyTerrain)
                {
                    r = Level.currentColorR;
                    g = Level.currentColorG;
                    b = Level.currentColorB;
                }

                double frac = z / (this.depth * 0.8 * (Game.tile_size - base));
                Drawing.drawing.setColor(this.colorR * frac + r * (1 - frac), this.colorG * frac + g * (1 - frac), this.colorB * frac + b * (1 - frac));
                Drawing.drawing.setShrubberyMode();
                Drawing.drawing.fillBox(this, this.posX, this.posY, this.baseGroundHeight * mul, Game.tile_size, Game.tile_size, z * this.visualDepth, (byte) (this.getOptionsByte(this.getTileHeight()) + 1));
            }
        }
    }

    public byte getOptionsByte(double h)
    {
        return 0;
    }

    public double getTileHeight()
    {
        double shrubScale = 0.25;
        if (Game.screen instanceof ScreenGame)
            shrubScale = ((ScreenGame) Game.screen).shrubberyScale;

        return shrubScale * (this.finalHeight + this.baseGroundHeight);
    }

    public boolean positionChanged()
    {
        boolean r = this.previousFinalHeight != this.finalHeight;
        this.previousFinalHeight = this.finalHeight;

        return r;
    }
}

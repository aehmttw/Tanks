package tanks.tank;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.IExplodable;
import tanks.Movable;
import tanks.Team;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.network.event.EventExplosion;
import tanks.obstacle.Obstacle;

public class Explosion extends Movable
{
    public double damage;
    public boolean destroysObstacles;
    public boolean destroysBullets = true;

    public double radius;
    public Tank tank;
    public Item item;

    public double knockbackRadius;
    public double bulletKnockback;
    public double tankKnockback;

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank, Item item)
    {
        super(x, y);

        this.tank = tank;
        this.item = item;
        this.radius = radius;
        this.damage = damage;
        this.destroysObstacles = destroysObstacles;
        this.team = tank.team;
        this.isRemote = tank.isRemote;
    }

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank)
    {
        this(x, y, radius, damage, destroysObstacles, tank, null);
    }

    public Explosion(Mine m)
    {
        this(m.posX, m.posY, m.radius, m.damage, m.destroysObstacles, m.tank, m.item);
        this.knockbackRadius = m.knockbackRadius;
        this.bulletKnockback = m.bulletKnockback;
        this.tankKnockback = m.tankKnockback;
        this.destroysBullets = m.destroysBullets;
    }
    private double distanceFromObstacle(Obstacle obstacle) {
        return Math.pow(Math.abs(obstacle.posX - this.posX), 2) + Math.pow(Math.abs(obstacle.posY - this.posY), 2);
    }
    private void spawnExplosionEffects()
    {
        final double twoPi = Math.PI * 2;
        for (int j = 0; j < Math.min(800, 200 * this.radius / 125) * Game.effectMultiplier; j++) {
            double random = Math.random();
            Effect effect = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
            effect.maxAge /= 2;
            effect.colR = 255;
            effect.colG = (1 - random) * 155 + Math.random() * 100;
            effect.colB = 0;

            if (Game.enable3d)
                effect.set3dPolarMotion(Math.random() * twoPi, Math.asin(Math.random()), random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
            else
                effect.setPolarMotion(Math.random() * twoPi, random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
            Game.effects.add(effect);
        }
    }

    private void causeDestruction()
    {
        for (Movable movable : Game.movables)
        {
            if (!(movable instanceof IExplodable)) continue;
            IExplodable explodable = (IExplodable) movable;

            boolean friendlyFireEnabled = this.team == null || this.team.friendlyFire;
            boolean isAllied = Team.isAllied(this, movable);
            if (isAllied && !friendlyFireEnabled) continue;

            double distance = Movable.distanceBetween(this, movable);
//            System.out.println("distance: "+distance+", radius:"+radius);

            if (distance < this.knockbackRadius) explodable.applyExplosionKnockback(
                    this.getAngleInDirection(movable.posX, movable.posY),
                    (1 - distance / Math.pow(knockbackRadius + explodable.getSize() / 2, 2)),
                    this
            );

            if (distance > this.radius) continue;

            explodable.onExploded(this);
        }

        if (!this.destroysObstacles) return;

        double distanceCheckRadius = Math.pow(radius + Game.tile_size / 2, 2);

        for (Obstacle obstacle : Game.obstacles)
        {
            if (distanceFromObstacle(obstacle) > distanceCheckRadius) continue;

            obstacle.onExploded(this);
        }
    }

    public void explode()
    {
        Drawing.drawing.playSound("explosion.ogg", (float) (Mine.mine_radius / this.radius));

        if (Game.effectsEnabled) {
            spawnExplosionEffects();
        }

        this.destroy = true;

        if (!ScreenPartyLobby.isClient) {
            Game.eventsOut.add(new EventExplosion(this));
            causeDestruction();
        }

        Effect explosionCircle = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.explosion);
        explosionCircle.radius = Math.max(this.radius, 0);
        Game.effects.add(explosionCircle);
    }

    @Override
    public void draw()
    {

    }
}

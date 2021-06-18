package tanks.tank;

import basewindow.Model;
import tanks.*;
import tanks.bullet.Laser;
import tanks.event.EventTankMimicLaser;
import tanks.event.EventTankMimicTransform;
import tanks.gui.screen.ScreenGame;
import tanks.registry.RegistryTank;

public class TankMimic extends TankAIControlled
{
    public Tank possessingTank = null;

    public double reversionCooldown = 200;
    public double range = Game.tile_size * 12;

    public double timeToRevert = this.reversionCooldown;
    public Laser laser;

    public boolean canPossess = true;

    public static Model base_model;
    public static Model color_model;
    public static Model turret_model;
    public static Model turret_base_model;

    public TankMimic(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 255, 255, 255, angle, ShootAI.reflect);

        this.enableMineLaying = false;
        this.enableMovement = true;
        this.enablePathfinding = true;
        this.seekChance = 1;
        this.cooldownBase = 200;
        this.cooldownRandom = 400;
        this.enableMineLaying = true;

        this.baseModel = base_model;
        this.colorModel = color_model;
        this.turretModel = turret_model;
        this.turretBaseModel = turret_base_model;

        this.coinValue = 4;

        this.description = "A tank which mimics the---closest tank it sees";
    }

    @Override
    public void postUpdate()
    {
        this.updateTarget();
        this.tryPossess();
    }

    public void tryPossess()
    {
        if (!this.seesTargetEnemy || !this.hasTarget || this.targetEnemy == null || this.destroy || !this.canPossess)
            return;

        try
        {
            this.timeToRevert = this.reversionCooldown;

            Tank.freeIDs.add(this.networkID);
            Tank.idMap.remove(this.networkID);

            Class<? extends Movable> c = this.targetEnemy.getClass();

            boolean player = false;

            if (c.equals(TankRemote.class))
                c = ((TankRemote) this.targetEnemy).tank.getClass();

            if (c.equals(TankPlayer.class) || c.equals(TankPlayerRemote.class))
            {
                c = TankPlayerMimic.class;
                player = true;
            }

            Tank t = (Tank) c.getConstructor(String.class, double.class, double.class, double.class).newInstance(this.name, this.posX, this.posY, this.angle);
            t.vX = this.vX;
            t.vY = this.vY;
            t.team = this.team;
            t.health = this.health;
            t.orientation = this.orientation;
            t.drawAge = this.drawAge;
            this.possessingTank = t;
            t.possessor = this;
            t.skipNextUpdate = true;
            t.attributes = this.attributes;

            t.baseModel = this.baseModel;
            t.turretModel = this.turretModel;
            t.turretBaseModel = this.turretBaseModel;

            if (t.networkID != this.networkID)
            {
                Tank.freeIDs.add(t.networkID);
                Tank.idMap.remove(t.networkID);
                t.networkID = this.networkID;
            }

            Game.movables.add(t);
            Game.removeMovables.add(this);

            Drawing.drawing.playGlobalSound("transform.ogg");

            if (player)
            {
                this.possessingTank.colorR = 0;
                this.possessingTank.colorG = 150;
                this.possessingTank.colorB = 255;

                this.possessingTank.turret.colorR = Turret.calculateSecondaryColor(this.possessingTank.colorR);
                this.possessingTank.turret.colorG = Turret.calculateSecondaryColor(this.possessingTank.colorG);
                this.possessingTank.turret.colorB = Turret.calculateSecondaryColor(this.possessingTank.colorB);
            }

            for (RegistryTank.TankEntry e: Game.registryTank.tankEntries)
            {
                if (e.tank.equals(c))
                {
                    t.name = e.name;
                }
            }

            Game.eventsOut.add(new EventTankMimicTransform(this.possessingTank, player));

            if (Game.effectsEnabled)
            {
                for (int i = 0; i < 50 * Game.effectMultiplier; i++)
                {
                    Effect e = Effect.createNewEffect(this.posX, this.posY, this.size / 4, Effect.EffectType.piece);
                    double var = 50;
                    e.colR = Math.min(255, Math.max(0, this.possessingTank.colorR + Math.random() * var - var / 2));
                    e.colG = Math.min(255, Math.max(0, this.possessingTank.colorG + Math.random() * var - var / 2));
                    e.colB = Math.min(255, Math.max(0, this.possessingTank.colorB + Math.random() * var - var / 2));

                    if (Game.enable3d)
                        e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI, 1 + Math.random() * this.size / 50.0);
                    else
                        e.setPolarMotion(Math.random() * 2 * Math.PI, 1 + Math.random() * this.size / 50.0);

                    Game.effects.add(e);
                }
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void updateTarget()
    {
        double nearestDist = Double.MAX_VALUE;
        Movable nearest = null;
        this.hasTarget = false;

        for (int i = 0; i < Game.movables.size(); i++)
        {
            Movable m = Game.movables.get(i);

            if (m instanceof Tank && !(m instanceof TankMimic) && ((Tank) m).possessor == null && Movable.distanceBetween(m, this) < this.range && ((Tank) m).size == this.size && !m.destroy)
            {
                Ray r = new Ray(this.posX, this.posY, this.getAngleInDirection(m.posX, m.posY), 0, this);
                r.moveOut(5);
                if (r.getTarget() != m)
                    continue;

                double distance = Movable.distanceBetween(this, m);

                if (distance < nearestDist)
                {
                    this.hasTarget = true;
                    nearestDist = distance;
                    nearest = m;
                }
            }
        }

        this.targetEnemy = nearest;
        this.canPossess = this.targetEnemy != null;

        if (this.targetEnemy == null)
            super.updateTarget();
    }

    public boolean isInterestingPathTarget(Movable m)
    {
        return m instanceof Tank && !(m instanceof TankMimic) && ((Tank) m).size == this.size;
    }

    @Override
    public void updatePossessing()
    {
        if (this.possessingTank.destroy || this.destroy || ScreenGame.finishedQuick)
            return;

        this.updateTarget();

        Class<? extends Movable> c = null;

        Movable m = null;

        this.posX = this.possessingTank.posX;
        this.posY = this.possessingTank.posY;
        this.vX = this.possessingTank.vX;
        this.vY = this.possessingTank.vY;
        this.angle = this.possessingTank.angle;

        if (this.targetEnemy != null)
        {
            Ray r = new Ray(this.possessingTank.posX, this.possessingTank.posY, 0, 0, this);
            r.vX = this.targetEnemy.posX - this.possessingTank.posX;
            r.vY = this.targetEnemy.posY - this.possessingTank.posY;

            double ma = Math.sqrt(r.vX * r.vX + r.vY * r.vY) / r.speed;
            r.vX /= ma;
            r.vY /= ma;

            r.moveOut(5);

            m = r.getTarget(2, (Tank) this.targetEnemy);

            c = this.targetEnemy.getClass();

            if (c == TankPlayer.class || c == TankPlayerRemote.class)
                c = TankPlayerMimic.class;
        }

        if (this.targetEnemy == null || m != this.targetEnemy || this.targetEnemy.destroy || c != this.possessingTank.getClass() || Movable.distanceBetween(this, this.targetEnemy) > this.range)
            this.timeToRevert -= Panel.frameFrequency;
        else
            this.timeToRevert = this.reversionCooldown;

        if (this.timeToRevert <= 0)
        {
            Game.removeMovables.add(this.possessingTank);
            Tank.idMap.put(this.networkID, this);
            this.health = this.possessingTank.health;
            this.orientation = this.possessingTank.orientation;
            this.drawAge = this.possessingTank.drawAge;
            this.attributes = this.possessingTank.attributes;
            this.targetEnemy = null;
            Drawing.drawing.playGlobalSound("slowdown.ogg", 1);
            Game.movables.add(this);
            Game.removeMovables.add(this.possessingTank);
            this.skipNextUpdate = true;
            Game.eventsOut.add(new EventTankMimicTransform(this, false));

            this.tryPossess();
        }

        if (this.targetEnemy != null && !this.targetEnemy.destroy && !this.possessingTank.destroy && this.canPossess)
        {
            this.laser = new Laser(this.possessingTank.posX, this.possessingTank.posY, this.possessingTank.size / 2, this.targetEnemy.posX, this.targetEnemy.posY, ((Tank)this.targetEnemy).size / 2,
                    (this.range - Movable.distanceBetween(this.possessingTank, this.targetEnemy)) / this.range * 10, this.targetEnemy.getAngleInDirection(this.possessingTank.posX, this.possessingTank.posY),
                    ((Tank) this.targetEnemy).colorR, ((Tank) this.targetEnemy).colorG, ((Tank) this.targetEnemy).colorB);
            Game.movables.add(this.laser);
            Game.eventsOut.add(new EventTankMimicLaser(this.possessingTank, (Tank) this.targetEnemy, this.range));
        }
        else
            Game.eventsOut.add(new EventTankMimicLaser(this.possessingTank, null, this.range));
    }
}

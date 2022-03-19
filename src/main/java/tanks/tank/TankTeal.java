package tanks.tank;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Laser;
import tanks.event.EventTankMimicLaser;
import tanks.event.EventTankMimicTransform;
import tanks.gui.screen.ScreenGame;

public class TankTeal extends TankAIControlled
{
    public Tank possessingTank = null;

    public double reversionCooldown = 200;
    public double range = Game.tile_size * 5;

    public double timeToRevert = this.reversionCooldown;
    public Laser laser;
    public double hookSpeed = 1;

    public boolean canPossess = true;

    public TankTeal(String name, double x, double y, double angle)
    {
        super(name, x, y, Game.tile_size, 0, 128, 128, angle, ShootAI.reflect);

        this.enableMineLaying = false;
        this.enableMovement = true;
        this.enablePathfinding = true;
        this.seekChance = 1;
        this.cooldownBase = 50;
        this.cooldownRandom = 50;

        this.coinValue = 4;

        this.description = "A tank which can hook---and drag other tanks";
    }

    @Override
    public void postUpdate()
    {
        this.updateTarget();
        this.tryHook();
    }

    public void tryHook()
    {
        if (!this.seesTargetEnemy || !this.hasTarget || this.targetEnemy == null || this.destroy || !this.canPossess)
            return;

        try
        {
            this.timeToRevert = this.reversionCooldown;
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

            if (m instanceof Tank && !(m instanceof TankTeal) && ((Tank) m).possessor == null && Movable.distanceBetween(m, this) > this.range && ((Tank) m).size == this.size && !m.destroy)
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

                    if (Math.random() * Panel.frameFrequency < 0.04){
                        m.setMotionInDirection(this.posX, this.posY, this.hookSpeed);
                        this.hookSpeed += Panel.frameFrequency;
                    }
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
        return m instanceof Tank && !(m instanceof TankTeal) && ((Tank) m).size == this.size;
    }

    @Override
    public void updatePossessing()
    {
        if (this.possessingTank.destroy || this.destroy || ScreenGame.finishedQuick || this.positionLock)
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
            this.targetEnemy = null;
            Game.movables.add(this);
            Game.removeMovables.add(this.possessingTank);
            this.skipNextUpdate = true;
            Game.eventsOut.add(new EventTankMimicTransform(this, this, false));

            this.tryHook();
        }

        if (m != null && !m.destroy)
        {
            this.laser = new Laser(m.posX, m.posY, ((Tank) m).size / 2, this.posX, this.posY, this.size / 2,
                    200, this.getAngleInDirection(m.posX, m.posY),
                    this.colorR, this.colorG, this.colorB);
            Game.movables.add(this.laser);
            Game.eventsOut.add(new EventTankMimicLaser(this.possessingTank, (Tank) this.targetEnemy, this.range));
        }
        else
            Game.eventsOut.add(new EventTankMimicLaser(this.possessingTank, null, this.range));
    }
}

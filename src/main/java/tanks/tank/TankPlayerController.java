package tanks.tank;

import tanks.Game;
import tanks.Panel;
import tanks.item.Item;
import tanks.network.event.EventTankControllerUpdateC;

import java.util.Arrays;
import java.util.UUID;

public class TankPlayerController extends TankPlayer implements ILocalPlayerTank
{
    public UUID clientID;

    public boolean action1;
    public boolean action2;

    public double interpolatedOffX = 0;
    public double interpolatedOffY = 0;
    public double interpolatedProgress = interpolationTime;

    public double interpolatedPosX = this.posX;
    public double interpolatedPosY = this.posY;

    public int liveBullets;
    public int maxLiveBullets;
    public int liveMines;
    public int maxLiveMines;
    public double bulletCooldownBase;
    public double bulletCooldown;
    public double mineCooldownBase;
    public double mineCooldown;

    public boolean[] quickActions = new boolean[TankPlayer.max_abilities];

    public static final double interpolationTime = 25;

    public TankPlayerController(double x, double y, double angle, UUID id)
    {
        super(x, y, angle);
        this.clientID = id;
        this.isRemote = true;
    }

    @Override
    public void update()
    {
        this.interpolatedProgress = Math.min(this.interpolatedProgress + Panel.frameFrequency, interpolationTime);

        this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        this.action1 = false;
        this.action2 = false;

        this.bulletCooldown -= Panel.frameFrequency;
        if (this.bulletCooldown < 0)
            this.bulletCooldown = 0;

        super.update();

        this.interpolatedPosX = this.posX;
        this.interpolatedPosY = this.posY;

        this.posX = this.posX + this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY + this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        Game.eventsOut.add(new EventTankControllerUpdateC(this));
        Arrays.fill(this.quickActions, false);
    }

    @Override
    public void action(boolean right)
    {
        if (!right)
            this.action1 = true;
        else
            this.action2 = true;

        Item.ItemStack<?> p = this.getPrimaryAbility();
    }

    @Override
    public void quickAction(int click)
    {
        this.quickActions[click] = true;
    }

    @Override
    public void draw()
    {
        double realX = this.posX;
        double realY = this.posY;

        this.posX = this.interpolatedPosX;
        this.posY = this.interpolatedPosY;

        super.draw();

        this.posX = realX;
        this.posY = realY;
    }

    @Override
    public void preUpdate()
    {
        this.lastPosX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.lastPosY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.lastPosZ = this.posZ;
    }

    @Override
    public double getTouchCircleSize()
    {
        return this.touchCircleSize;
    }

    @Override
    public boolean showTouchCircle()
    {
        return this.drawTouchCircle;
    }

    @Override
    public double getDrawRangeMin() { return this.drawRangeMin; }

    @Override
    public double getDrawRangeMax() { return this.drawRangeMax; }

    @Override
    public double getDrawLifespan() { return this.drawLifespan; }

    @Override
    public boolean getShowTrace() { return this.drawTrace; }

    @Override
    public void setDrawRanges(double lifespan, double rangeMin, double rangeMax, boolean trace)
    {
        this.drawLifespan = lifespan;
        this.drawRangeMin = rangeMin;
        this.drawRangeMax = rangeMax;
        this.drawTrace = trace;
    }
}
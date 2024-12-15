package tanks.tank;

import basewindow.InputPoint;
import tanks.AttributeModifier;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Hotbar;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemRemote;
import tanks.network.event.EventTankControllerUpdateC;

import java.util.UUID;

public class TankPlayerController extends Tank implements ILocalPlayerTank
{
    public UUID clientID;

    public boolean action1;
    public boolean action2;

    public boolean drawTouchCircle = false;
    public double touchCircleSize = 400;
    public long prevTap = 0;

    public double interpolatedOffX = 0;
    public double interpolatedOffY = 0;
    public double interpolatedProgress = interpolationTime;

    public double interpolatedPosX = this.posX;
    public double interpolatedPosY = this.posY;

    public static final double interpolationTime = 25;

    protected double prevDistSq;

    protected long lastTrace = 0;

    protected double drawRangeMin = -1;
    protected double drawRangeMax = -1;
    protected double drawLifespan = -1;
    protected boolean drawTrace = true;

    public double mouseX;
    public double mouseY;

    public TankPlayerController(double x, double y, double angle, UUID id)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
        this.clientID = id;
        this.isRemote = true;
        this.angle = angle;
        this.orientation = angle;

        if (Game.nameInMultiplayer)
        {
            this.nameTag.name = Game.player.username;
            this.showName = true;
        }
    }

    @Override
    public void update()
    {
        this.bulletItem.cooldown = Math.max(0, this.bulletItem.cooldown - Panel.frameFrequency);
        this.interpolatedProgress = Math.min(this.interpolatedProgress + Panel.frameFrequency, interpolationTime);

        this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        boolean up = Game.game.input.moveUp.isPressed();
        boolean down = Game.game.input.moveDown.isPressed();
        boolean left = Game.game.input.moveLeft.isPressed();
        boolean right = Game.game.input.moveRight.isPressed();
        boolean trace = Game.game.input.aim.isPressed();

        double acceleration = this.acceleration * this.accelerationModifier;
        double maxVelocity = maxSpeed * maxSpeedModifier;

        if (Game.game.input.aim.isValid())
        {
            Game.game.input.aim.invalidate();

            long time = System.currentTimeMillis();

            TankPlayer.lockTrace = false;
            if (time - lastTrace <= 500)
            {
                lastTrace = 0;
                TankPlayer.lockTrace = true;
            }
            else
                lastTrace = time;
        }

        if (this.tookRecoil)
        {
            if (this.recoilSpeed <= this.maxSpeed * this.maxSpeedModifier * 1.0001)
            {
                this.tookRecoil = false;
                this.inControlOfMotion = true;
            }
            else
            {
                this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
                this.recoilSpeed *= Math.pow(1 - this.friction * this.frictionModifier, Panel.frameFrequency);
            }
        }
        else if (this.inControlOfMotion)
        {
            double x = 0;
            double y = 0;

            double a = -1;

            if (left)
                x -= 1;

            if (right)
                x += 1;

            if (up)
                y -= 1;

            if (down)
                y += 1;

            if (x == 1 && y == 0)
                a = 0;
            else if (x == 1 && y == 1)
                a = Math.PI / 4;
            else if (x == 0 && y == 1)
                a = Math.PI / 2;
            else if (x == -1 && y == 1)
                a = 3 * Math.PI / 4;
            else if (x == -1 && y == 0)
                a = Math.PI;
            else if (x == -1 && y == -1)
                a = 5 * Math.PI / 4;
            else if (x == 0 && y == -1)
                a = 3 * Math.PI / 2;
            else if (x == 1 && y == -1)
                a = 7 * Math.PI / 4;

            double intensity = 1;

            if (a < 0 && Game.game.window.touchscreen)
            {
                intensity = TankPlayer.controlStick.inputIntensity;

                if (intensity >= 0.2)
                    a = TankPlayer.controlStick.inputAngle;
            }

            if (a >= 0 && intensity >= 0.2)
            {
                if (Game.followingCam)
                    a += this.angle + Math.PI / 2;

                this.addPolarMotion(a, acceleration * Panel.frameFrequency);
            }

            if (a == -1)
            {
                this.vX *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);
                this.vY *= Math.pow(1 - (this.friction * this.frictionModifier), Panel.frameFrequency);

                if (Math.abs(this.vX) < 0.001)
                    this.vX = 0;

                if (Math.abs(this.vY) < 0.001)
                    this.vY = 0;
            }

            double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

            if (speed > maxVelocity)
                this.setPolarMotion(this.getPolarDirection(), maxVelocity);
        }

        boolean shoot = !Game.game.window.touchscreen && Game.game.input.shoot.isPressed();

        boolean mine = !Game.game.window.touchscreen && Game.game.input.mine.isPressed();

        Hotbar h = Game.player.hotbar;
        boolean hideShootStick = false;
        if (h.enabledItemBar && h.itemBar.selected >= 0)
        {
            Item.ItemStack<?> i = h.itemBar.slots[h.itemBar.selected];

            if (i.item instanceof ItemBullet)
                hideShootStick = ((ItemBullet) i.item).bullet instanceof BulletArc || ((ItemBullet) i.item).bullet instanceof BulletAirStrike;
            else if (i.item instanceof ItemRemote)
                hideShootStick = ((ItemRemote) i.item).hideShootStick;
        }

        TankPlayer.shootStickHidden = hideShootStick;

        boolean prevTouchCircle = this.drawTouchCircle;
        this.drawTouchCircle = false;
        if (Game.game.window.touchscreen)
        {
            if (TankPlayer.shootStickEnabled)
            {
                if (!Game.bulletLocked && !this.disabled && !this.destroy)
                    TankPlayer.mineButton.update();

                if (!hideShootStick)
                    TankPlayer.shootStick.update();
            }

            if (!Game.bulletLocked && !this.disabled && !this.destroy)
            {
                double distSq = 0;

                if (TankPlayer.shootStickEnabled && !hideShootStick)
                {
                    if (TankPlayer.mineButton.justPressed)
                        mine = true;

                    if (TankPlayer.shootStick.inputIntensity >= 0.2)
                    {
                        this.angle = TankPlayer.shootStick.inputAngle;
                        trace = true;

                        if (TankPlayer.shootStick.inputIntensity >= 1.0)
                            shoot = true;
                    }
                }

                if (!TankPlayer.shootStickEnabled || TankPlayer.shootStickHidden)
                {
                    for (int i : Game.game.window.touchPoints.keySet())
                    {
                        InputPoint p = Game.game.window.touchPoints.get(i);

                        if (!p.tag.equals("") && !p.tag.equals("aim") && !p.tag.equals("shoot"))
                            continue;

                        double px = Drawing.drawing.getInterfacePointerX(p.x);
                        double py = Drawing.drawing.getInterfacePointerY(p.y);

                        if (!Game.followingCam)
                        {
                            this.mouseX = Drawing.drawing.toGameCoordsX(px);
                            this.mouseY = Drawing.drawing.toGameCoordsY(py);
                            this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
                        }

                        distSq = Math.pow(px - Drawing.drawing.toInterfaceCoordsX(this.posX), 2)
                                + Math.pow(py - Drawing.drawing.toInterfaceCoordsY(this.posY), 2);

                        if (distSq <= Math.pow(this.touchCircleSize / 4, 2) || p.tag.equals("aim"))
                        {
                            p.tag = "aim";
                            this.drawTouchCircle = true;

                            if (!prevTouchCircle)
                            {
                                if (System.currentTimeMillis() - prevTap <= 500)
                                {
                                    Drawing.drawing.playVibration("heavyClick");
                                    mine = true;
                                    this.prevTap = 0;
                                }
                                else
                                    prevTap = System.currentTimeMillis();
                            }

                            trace = true;
                        }
                        else
                        {
                            shoot = true;
                            p.tag = "shoot";
                        }

                        double proximity = Math.pow(this.touchCircleSize / 2, 2);

                        if (p.tag.equals("aim") && ((distSq <= proximity && prevDistSq > proximity) || (distSq > proximity && prevDistSq <= proximity)))
                            Drawing.drawing.playVibration("selectionChanged");

                        if (distSq > proximity)
                            shoot = true;
                    }
                }

                this.prevDistSq = distSq;
            }
        }
        else if (!Game.followingCam)
        {
            this.mouseX = Drawing.drawing.getMouseX();
            this.mouseY = Drawing.drawing.getMouseY();
            this.angle = this.getAngleInDirection(this.mouseX, this.mouseY);
        }

        this.action1 = shoot;
        this.action2 = mine;

        if ((trace || TankPlayer.lockTrace) && !Game.bulletLocked && !this.disabled && Game.screen instanceof ScreenGame)
        {
            double lifespan = -1;
            double rangeMin = -1;
            double rangeMax = -1;
            boolean showTrace = true;

            Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);

            if (h.enabledItemBar && h.itemBar.selected >= 0)
            {
                Item.ItemStack<?> i = h.itemBar.slots[h.itemBar.selected];
                if (i instanceof ItemRemote.ItemStackRemote)
                {
                    ItemRemote ir = (ItemRemote) i.item;
                    if (ir.bounces >= 0)
                        r.bounces = ir.bounces;

                    lifespan = ir.lifespan > 0 ? ir.lifespan * this.getAttributeValue(AttributeModifier.bullet_speed, 1) + this.turretLength : 0;
                    rangeMin = ir.rangeMin;
                    rangeMax = ir.rangeMax;
                    showTrace = ir.showTrace;
                }
            }

            r.vX /= 2;
			r.vY /= 2;
			r.trace = true;
			r.dotted = true;
			r.moveOut(10 * this.size / Game.tile_size);

			if (rangeMax > 0)
				this.drawRangeMax = rangeMax;

			if (rangeMin > 0)
				this.drawRangeMin = rangeMin;

			if (lifespan > 0)
				this.drawLifespan = lifespan;

			if (showTrace)
				r.getTarget();
        }

        super.update();

        this.interpolatedPosX = this.posX;
        this.interpolatedPosY = this.posY;

        this.posX = this.posX + this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY + this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        Game.eventsOut.add(new EventTankControllerUpdateC(this));
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
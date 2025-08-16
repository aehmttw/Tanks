package tanks.bullet;

import basewindow.Model;
import basewindow.transformation.AxisRotation;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.ItemBullet;
import tanks.network.event.EventAddObstacleBullet;
import tanks.obstacle.ObstacleStackable;
import tanks.tank.Crate;
import tanks.tank.Tank;

public class BulletBlock extends BulletArc
{
    public static String bullet_class_name = "block";

    public static Model block = null;

    public double initialTime = -1;
    public double initialAngle = -1;
    public double finalAngle = -1;

    public double landingAdjustTimerMax = 5;
    public double landingAdjustTimer = 5;
    public double landingAdjustPosX;
    public double landingAdjustPosY;
    public double landingAdjustPosIX;
    public double landingAdjustPosIY;
    public double lastPitch;


    public boolean adjustingLanding = false;
    public boolean outOfBounds = false;

    protected boolean hasBounced = false;

    AxisRotation[] rotations = new AxisRotation[]{new AxisRotation(Game.game.window, AxisRotation.Axis.roll, 0), new AxisRotation(Game.game.window, AxisRotation.Axis.yaw, 0), new AxisRotation(Game.game.window, AxisRotation.Axis.roll, 0)};

    public BulletBlock()
    {
        this.init();
        this.typeName = bullet_class_name;
        this.respectXRay = false;
    }

    public BulletBlock(double x, double y, Tank t, boolean affectsMaxLiveBullets, ItemBullet.ItemStackBullet ib)
    {
        super(x, y, t, affectsMaxLiveBullets, ib);
        this.init();
        this.typeName = bullet_class_name;
        this.respectXRay = false;
    }

    @Override
    public void setTargetLocation(double x, double y)
    {
        double angle = Math.random() * Math.PI * 2;
        double dx = x - this.posX;
        double dy = y - this.posY;
        double d = Math.sqrt(dx * dx + dy * dy);
        double s = Math.abs(this.speed);

        if (d > this.maxRange && this.maxRange > 0)
        {
            dx *= this.maxRange / d;
            dy *= this.maxRange / d;
            d = this.maxRange;
        }

        if (d < this.minRange)
        {
            dx *= this.minRange / d;
            dy *= this.minRange / d;
            d = this.minRange;
        }

        double offset = Math.random() * this.accuracySpreadCircle * d;

        double f = 1;
        if (d / s < this.minAirTime)
            f = d / (s * this.minAirTime);

        dx += Math.sin(angle) * offset;
        dy += Math.cos(angle) * offset;

        dx = (Math.min(Math.max((int)((this.posX + dx) / Game.tile_size), 0), Game.currentSizeX - 1) + 0.5) * Game.tile_size - this.posX;
        dy = (Math.min(Math.max((int)((this.posY + dy) / Game.tile_size), 0), Game.currentSizeY - 1) + 0.5) * Game.tile_size - this.posY;

        d = Math.sqrt(dx * dx + dy * dy);

        this.setMotionInDirection(this.posX + dx, this.posY + dy, s * f);

        this.speed *= f;
        this.vZ = d / s * 0.5 * BulletArc.gravity / f;
    }

    @Override
    public void arcDestroy(boolean remainingBounces)
    {
        this.landingAdjustPosIX = this.posX;
        this.landingAdjustPosIY = this.posY;
        this.landingAdjustPosX = (0.5 + (int)(this.posX / Game.tile_size)) * Game.tile_size;
        this.landingAdjustPosY = (0.5 + (int)(this.posY / Game.tile_size)) * Game.tile_size;

        if (remainingBounces || this.homingSharpness != 0)
        {
            this.adjustingLanding = true;
            this.vX = 0;
            this.vY = 0;
            this.vZ = 0;
            this.destroyTimer = 0;

            this.destroy = false;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (this.outOfBounds)
            return;

        int x = (int) (this.posX / Game.tile_size);
        int y = (int) (this.posY / Game.tile_size);

        ObstacleStackable o = new ObstacleStackable("normal", x, y);
        o.colorR = this.originalOutlineColor.red;
        o.colorG = this.originalOutlineColor.green;
        o.colorB = this.originalOutlineColor.blue;

        for (int i = 0; i < o.stackColorR.length; i++)
        {
            o.stackColorR[i] = this.originalOutlineColor.red;
            o.stackColorG[i] = this.originalOutlineColor.green;
            o.stackColorB[i] = this.originalOutlineColor.blue;
        }

        o.setUpdate(true);
        o.shouldClip = true;
        o.clipFrames = 2;

        if (ScreenGame.finishedQuick)
            return;

        boolean canPlace = Chunk.getIfPresent(x, y, false, tile -> tile.canPlaceOn(o));

        if (canPlace)
        {
            Game.addObstacle(o);
        }
        else
        {
            Drawing.drawing.playGlobalSound("break.ogg");
            o.playDestroyAnimation(this.posX, this.posY, Game.tile_size);
        }

        Game.eventsOut.add(new EventAddObstacleBullet(o, canPlace));
    }

    @Override
    public void drawCursor(double frac, double x, double y)
    {
        double f2 = Math.max(0, (this.maxDestroyTimer - this.destroyTimer) / this.maxDestroyTimer);
        Drawing.drawing.setColor(this.outlineColor, frac * f2 * 255);
        Crate.fillOutlineRect(x, y, Game.tile_size * (2 - frac) * f2);
        Crate.fillOutlineRect(x, y, Game.tile_size * (frac) * f2);
    }

    @Override
    public double bounce()
    {
        double ht = super.bounce();

        this.vZ = -this.vZ * this.bounciness;
        if (!this.destroy)
        {
            double pz = Game.tile_size / 2;
            double time = (vZ + Math.sqrt(vZ * vZ + 2 * gravity * (pz - Game.tile_size / 2))) / gravity;
            double x = this.posX + this.vX * time;
            double y = this.posY + this.vY * time;
            this.maxRange = 0;
            this.minRange = 0;
            this.minAirTime = 0;

            // Floating point precision errors...
            x = Math.round(x * 100000) / 100000;
            y = Math.round(y * 100000) / 100000;

            if (x >= Game.tile_size / 2 && x <= (Game.currentSizeX - 0.5) * Game.tile_size &&
                    y >= Game.tile_size / 2 && y <= (Game.currentSizeY - 0.5) * Game.tile_size)
                this.setTargetLocation(x, y);

            this.posX -= this.vX * ht;
            this.posY -= this.vY * ht;
            this.posZ += -ht * this.vZ - gravity * ht * ht * 0.5;
            this.vZ += gravity * ht;
        }

        return ht;
    }

    @Override
    public void postBounce(double ht)
    {

    }

    @Override
    public void drawShadow()
    {

    }

    @Override
    public void playArcPop()
    {
        if (this.destroy || this.adjustingLanding)
            Drawing.drawing.playGlobalSound("slam.ogg");
        else
            Drawing.drawing.playGlobalSound("slam2.ogg");
    }


    @Override
    public void draw()
    {
        super.draw();

        if (this.delay > 0)
            return;

        double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;
        if (Double.isNaN(time))
            time = 0;

        double time2 = (this.vZ - Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;

        time = Math.max(0, time);

        if (this.initialTime < 0 || this.justBounced)
        {
            if (justBounced)
                hasBounced = true;
            else
            {
                this.initialAngle = this.getPolarDirection();
                long r = Math.round(this.initialAngle / Math.PI * 2);
                this.finalAngle = Math.PI * 0.5 * r;
                this.rotations[2].angle = (r % 2 * Math.PI / 2);
            }

            this.initialTime = time;
        }

        double it = Math.min(100, this.initialTime);
        double frac3 = Math.max(it + time2, 0) / it;
        double frac = Math.max(it - time, 0) / it;
        double size = frac * Game.tile_size + (1.0 - frac) * this.size;
        if (hasBounced)
            size = Game.tile_size;

        if (this.destroy)
        {
            size = Game.tile_size * Math.max((this.maxDestroyTimer - this.destroyTimer) / this.maxDestroyTimer, 0);
        }

        Drawing.drawing.setColor(this.outlineColor);

        // todo 2d
        if (Game.enable3d)
        {
            double frac2 = time / this.initialTime;
            double yaw = this.initialAngle * frac2 + this.finalAngle * (1.0 - frac2);

            if (this.bounces > 0)
                yaw = this.initialAngle;

            double pitch = ((1.0 - frac3) * (this.getPolarPitch() + Math.PI / 2) + frac3 * Math.PI) * (1.0 - frac);
            if (!adjustingLanding)
                this.lastPitch = pitch;
            else
                pitch = this.lastPitch;

            if (Double.isNaN(pitch))
                pitch = 0;

            double frac4 = this.landingAdjustTimer / this.landingAdjustTimerMax;
            pitch *= frac4;
            yaw = yaw * frac4 + this.finalAngle * (1 - frac4);

//        System.out.println(pitch);
            rotations[0].angle = -yaw;
            rotations[1].angle = -pitch;
            Drawing.drawing.drawModel(block, this.posX, this.posY, this.posZ, size, size, size, rotations);
        }
        else
        {
            double opacity = 1.0 - Math.min(this.posZ / 1000, 0.8);
            double sizeModifier = (1.0 - opacity) * 60 * (size / Bullet.bullet_size);
            Drawing.drawing.setColor(this.outlineColor, opacity * opacity * opacity * 255);
            Drawing.drawing.fillRect(this.posX, this.posY, size + sizeModifier, size + sizeModifier);
        }
        //Drawing.drawing.fillBox(this.posX, this.posY, this.posZ - size / 2, size, size, size);
    }

    @Override
    public void addDestroyEffect()
    {

    }

    @Override
    public void update()
    {
        if (this.adjustingLanding)
        {
            if (this.landingAdjustTimer > 0)
            {
                this.landingAdjustTimer -= Panel.frameFrequency;
                double frac = this.landingAdjustTimer / this.landingAdjustTimerMax;
                this.posX = this.landingAdjustPosIX * frac + this.landingAdjustPosX * (1 - frac);
                this.posY = this.landingAdjustPosIY * frac + this.landingAdjustPosY * (1 - frac);

                return;
            }

            this.posX = this.landingAdjustPosX;
            this.posY = this.landingAdjustPosY;
            this.bounces = 0;
            this.destroy = true;
        }

        super.update();
        if (!ScreenPartyLobby.isClient)
        {
            if (!(this.posX >= 0 && this.posX <= (Game.currentSizeX) * Game.tile_size &&
                    this.posY >= 0 && this.posY <= (Game.currentSizeY) * Game.tile_size) && !this.destroy)
            {
                this.destroy = true;
                Drawing.drawing.playGlobalSound("break.ogg");

                if (Game.enable3d)
                    ObstacleStackable.destroyAnimation3d(this.posX, this.posY, this.posZ - Game.tile_size / 2, this.posX - this.vX, this.posY - this.vY, Game.tile_size, Effect.EffectType.obstaclePiece3d, Game.tile_size, Game.effectMultiplier, this.originalOutlineColor.red, this.originalOutlineColor.green, this.originalOutlineColor.blue);

                this.outOfBounds = true;
            }
        }
    }

    @Override
    public void addTraceTarget(double ix, double iy)
    {
        super.addTraceTarget(ix, iy);

        Game.effects.add(Effect.createNewEffect((Math.round(this.posX / Game.tile_size - 0.5) + 0.5) * Game.tile_size, (Math.round(this.posY / Game.tile_size - 0.5) + 0.5) * Game.tile_size, Effect.EffectType.blockMarker));
    }

    @Override
    public boolean bounceTrace(double ix, double iy)
    {
        super.bounceTrace(ix, iy);

        double time = (this.vZ + Math.sqrt(this.vZ * this.vZ + 2 * gravity * (this.posZ - Game.tile_size / 2))) / gravity;
        double x = this.posX + this.vX * time;
        double y = this.posY + this.vY * time;
        x = Math.round(x * 100000) / 100000;
        y = Math.round(y * 100000) / 100000;

        double max = this.maxRange;
        double min = this.minRange;
        double ma = this.minAirTime;

        this.maxRange = 0;
        this.minRange = 0;
        this.minAirTime = 0;

        double ots = this.accuracySpreadCircle;
        this.accuracySpreadCircle = 0;

        if (x >= Game.tile_size / 2 && x <= (Game.currentSizeX - 0.5) * Game.tile_size &&
                y >= Game.tile_size / 2 && y <= (Game.currentSizeY - 0.5) * Game.tile_size)
            this.setTargetLocation(x, y);

        this.accuracySpreadCircle = ots;

        this.maxRange = max;
        this.minRange = min;
        this.minAirTime = ma;

        return false;
    }
}

package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;
import tanks.event.EventTankControllerUpdateAmmunition;
import tanks.event.EventTankControllerUpdateS;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.ItemBullet;

import java.util.ArrayList;

public class TankPlayerRemote extends Tank
{
    public double lastPosX;
    public double lastPosY;
    public double lastVX;
    public double lastVY;

    public double lastUpdateReportedTime = 0;

    public double lastUpdateTime = 0;
    public long startUpdateTime = -1;

    public ArrayList<Bullet> recentBullets = new ArrayList<Bullet>();

    public boolean forceMotion = false;

    public Player player;

    public double anticheatMaxTimeOffset = 5;
    public double anticheatMaxTime = 20;
    public double anticheatMaxDist = 20;

    public TankPlayerRemote(double x, double y, double angle, Player p)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
        this.player = p;
        this.showName = true;
        this.angle = angle;
        this.orientation = angle;

        this.liveBulletMax = 5;
        this.liveMinesMax = 2;
        this.standardUpdateEvent = false;
        this.player.tank = this;

        this.lastPosX = x;
        this.lastPosY = y;
    }

    @Override
    public void update()
    {
        super.update();

        if (this.cooldown > 0)
            this.cooldown -= Panel.frameFrequency;

        Game.eventsOut.add(new EventTankControllerUpdateS(this, this.forceMotion));
        this.forceMotion = false;

        if (this.hasCollided)
        {
            this.lastVX = this.vX;
            this.lastVY = this.vY;
            this.lastPosX = this.posX;
            this.lastPosY = this.posY;
        }

        if (this.tookRecoil)
        {
            if (this.recoilSpeed <= this.maxSpeed)
            {
                this.tookRecoil = false;
                this.inControlOfMotion = true;
            }
            else
            {
                this.setMotionInDirection(this.vX + this.posX, this.vY + this.posY, this.recoilSpeed);
                this.recoilSpeed *= Math.pow(1 - TankPlayer.base_deceleration * this.frictionModifier, Panel.frameFrequency);
            }
        }

        ItemBar b = this.player.hotbar.itemBar;
        if (Crusade.crusadeMode && b.selected != -1 && b.slots[b.selected] instanceof ItemBullet)
        {
            ItemBullet ib = (ItemBullet) b.slots[b.selected];
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, ib.liveBullets, ib.maxAmount, this.liveMines, this.liveMinesMax));
        }
        else
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, this.liveBullets, this.liveBulletMax, this.liveMines, this.liveMinesMax));
    }

    public void controllerUpdate(double x, double y, double vX, double vY, double angle, boolean action1, boolean action2, double time, long receiveTime)
    {
        //double time = (timeNow - this.lastUpdateTime) / 10.0;

        if (this.startUpdateTime == -1)
            this.startUpdateTime = receiveTime;

        this.lastUpdateTime = (receiveTime - this.startUpdateTime) / 10.0;
        this.lastUpdateReportedTime += time;

        if (this.lastUpdateReportedTime - this.lastUpdateTime > anticheatMaxTimeOffset)
        {
            time = time - this.lastUpdateReportedTime - this.lastUpdateTime - anticheatMaxTimeOffset;
            this.lastUpdateReportedTime = this.lastUpdateTime + anticheatMaxTimeOffset;
        }

        if (this.lastUpdateReportedTime - this.lastUpdateTime < -anticheatMaxTimeOffset)
        {
            time = time - (this.lastUpdateReportedTime - this.lastUpdateTime + anticheatMaxTimeOffset);
            this.lastUpdateReportedTime = this.lastUpdateTime - anticheatMaxTimeOffset;
        }

        if (this.inControlOfMotion)
        {
            if (time > anticheatMaxTime)
            {
                time = anticheatMaxTime;
                forceMotion = true;
            }

            double maxChangeVelocity = time * acceleration * accelerationModifier;

            double dVX = vX - this.lastVX;
            double dVY = vY - this.lastVY;
            double changeVelocitySq = dVX * dVX + dVY * dVY;

            if (changeVelocitySq > maxChangeVelocity * maxChangeVelocity * 1.00001
                    && !(Math.abs(this.getAngleInDirection(this.posX + this.lastVX, this.posY + this.lastVY) - this.getAngleInDirection(this.posX + vX, this.posY + vY)) < 0.0001
                    && Math.abs(vX) <= Math.abs(this.lastVX) && Math.abs(vY) <= Math.abs(this.lastVY)
                    && (Math.abs(vX) >= Math.abs(this.lastVX * Math.pow(1 - TankPlayer.base_deceleration * this.frictionModifier, time)) || Math.abs(this.lastVX) < 0.001)
                    && (Math.abs(vY) >= Math.abs(this.lastVY * Math.pow(1 - TankPlayer.base_deceleration * this.frictionModifier, time)) || Math.abs(this.lastVY) < 0.001)))
            {
                double changeVelocity = Math.sqrt(changeVelocitySq);

                forceMotion = true;
                dVX *= maxChangeVelocity / changeVelocity;
                dVY *= maxChangeVelocity / changeVelocity;

                vX = this.lastVX + dVX;
                vY = this.lastVY + dVY;
            }

            double speedSq = vX * vX + vY * vY;

            if (speedSq > Math.pow(this.maxSpeed * this.maxSpeedModifier, 2) * 1.00001)
            {
                forceMotion = true;
                double speed = Math.sqrt(speedSq);
                double maxSpeed = this.maxSpeed * this.maxSpeedModifier;

                vX *= maxSpeed / speed;
                vY *= maxSpeed / speed;
            }

            double vX2 = vX;
            double vY2 = vY;

            for (int i = 0; i < this.attributes.size(); i++)
            {
                AttributeModifier a = this.attributes.get(i);

                if (a.type.equals("velocity"))
                {
                    vX2 = a.getValue(vX2);
                    vY2 = a.getValue(vY2);
                }
            }

            double maxDist = 1;

            double ourPosX = (this.lastPosX + vX2 * time / 2);
            double ourPosY = (this.lastPosY + vY2 * time / 2);

            double dX2 = x - ourPosX;
            double dY2 = y - ourPosY;

            double distSq2 = dX2 * dX2 + dY2 * dY2;
            if (distSq2 > maxDist * maxDist)
            {
                forceMotion = true;
            }

            x = ourPosX;
            y = ourPosY;

            double dX = x - this.lastPosX;
            double dY = y - this.lastPosY;

            double distSq = dX * dX + dY * dY;
            if (distSq > anticheatMaxDist * anticheatMaxDist)
            {
                double dist = Math.sqrt(distSq);
                forceMotion = true;
                dX *= anticheatMaxDist / dist;
                dY *= anticheatMaxDist / dist;

                x = this.lastPosX + dX;
                y = this.lastPosY + dY;
            }
        }
        else
        {
            x = this.posX;
            y = this.posY;
            vX = this.vX;
            vY = this.vY;
        }

        //this.lastUpdateTime = timeNow;

        if (Game.screen instanceof ScreenGame)
        {
            if (!((ScreenGame) Game.screen).playing)
                return;

            /*if (frame != this.lastUpdateFrame)
                this.recentBullets.clear();
            else
            {
                for (Bullet recentBullet : this.recentBullets)
                {
                    recentBullet.moveOut(Math.max(0, time - this.lastUpdateTime) / 200.0);
                }
            }

            this.lastUpdateTime = time;
            this.lastUpdateFrame = frame;*/

            this.posX = x;
            this.posY = y;
            this.vX = vX;
            this.vY = vY;
            this.angle = angle;

            this.lastPosX = x;
            this.lastPosY = y;
            this.lastVX = vX;
            this.lastVY = vY;

            if (action1 && this.cooldown <= 0 && this.liveBullets < this.liveBulletMax && !this.disabled)
                this.shoot();

            if (action2 && this.cooldown <= 0 && this.liveMines < this.liveMinesMax && !this.disabled)
                this.layMine();
        }
    }

    public void layMine()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (Crusade.crusadeMode)
        {
            if (this.player.hotbar.itemBar.useItem(true))
                return;
        }

        Drawing.drawing.playGlobalSound("lay_mine.ogg");

        this.cooldown = 50;
        Mine m = new Mine(posX, posY, this);

        Game.movables.add(m);
    }

    public void shoot()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (Crusade.crusadeMode)
        {
            if (this.player.hotbar.itemBar.useItem(false))
                return;
        }

        this.cooldown = 20;

        fireBullet(25 / 4.0, 1, Bullet.BulletEffect.trail);
    }

    public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
    {
        Drawing.drawing.playGlobalSound("shoot.ogg");

        Bullet b = new Bullet(posX, posY, bounces, this);
        b.addPolarMotion(this.angle, speed);
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

        b.moveOut(25.0 / speed * 2);
        b.effect = effect;

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

        this.recentBullets.add(b);

        this.forceMotion = true;
        this.processRecoil(1);
    }

    public void fireBullet(Bullet b, double speed)
    {
        if (b.itemSound != null)
            Drawing.drawing.playGlobalSound(b.itemSound, (float) (Bullet.bullet_size / b.size));

        b.addPolarMotion(this.angle, speed);
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0 * b.recoil);

        b.moveOut(25.0 / speed * 2 * this.size / Game.tile_size);

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

        if (b.recoil != 0)
            this.forceMotion = true;

        this.processRecoil(b.recoil);
    }

    public void processRecoil(double recoil)
    {
        if (this.vX * this.vX + this.vY * this.vY > this.maxSpeed * this.maxSpeed)
        {
            this.tookRecoil = true;
            this.inControlOfMotion = false;
            this.recoilSpeed = 25.0 / 16.0 * recoil;
        }
    }

    public void addPolarMotion(double angle, double velocity)
    {
        double velX = velocity * Math.cos(angle);
        double velY = velocity * Math.sin(angle);

        this.vX += velX;
        this.vY += velY;
        this.lastVX += velX;
        this.lastVY += velY;
    }

    @Override
    public void onDestroy()
    {
        if (Crusade.crusadeMode)
            this.player.remainingLives--;
    }
}

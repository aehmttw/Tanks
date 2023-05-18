package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.network.event.*;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.hotbar.item.ItemEmpty;
import tanks.hotbar.item.ItemMine;

public class TankPlayerRemote extends Tank implements IServerPlayerTank
{
    public double lastPosX;
    public double lastPosY;
    public double lastVX;
    public double lastVY;

    public double mouseX;
    public double mouseY;

    public double lastUpdateReportedTime = 0;

    public double lastUpdateTime = 0;
    public long startUpdateTime = -1;
    public double ourTimeOffset = 0;

    public boolean forceMotion = true;
    public boolean recoil = false;

    public Player player;

    public static boolean checkMotion = false;
    public static boolean weakTimeCheck = false;

    public static final double anticheatStrongTimeOffset = 10;
    public static final double anticheatWeakTimeOffset = 100;

    public static double anticheatMaxTimeOffset = anticheatStrongTimeOffset;
    public double anticheatMaxTime = 20;
    public double anticheatMaxDist = 20;

    public double dXSinceFrame = 0;
    public double dYSinceFrame = 0;

    public double interpolationTime = 25;

    public double interpolatedOffX = 0;
    public double interpolatedOffY = 0;
    public double interpolatedProgress = interpolationTime;

    public double interpolatedPosX = this.posX;
    public double interpolatedPosY = this.posY;

    public int lastLiveBullets;
    public int lastMaxLiveBullets;
    public int lastLiveMines;
    public int lastMaxLiveMines;

    public TankPlayerRemote(double x, double y, double angle, Player p)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
        this.player = p;
        this.showName = true;
        this.angle = angle;
        this.orientation = angle;

        this.standardUpdateEvent = false;
        this.player.tank = this;

        this.lastPosX = x;
        this.lastPosY = y;
    }

    @Override
    public void update()
    {
        super.update();

        double reload = this.getAttributeValue(AttributeModifier.reload, 1);

        this.bullet.updateCooldown(reload);
        this.mine.updateCooldown(reload);

        Hotbar h = this.player.hotbar;
        if (h.enabledItemBar)
        {
            for (Item i: h.itemBar.slots)
            {
                if (i != null && !(i instanceof ItemEmpty))
                {
                    i.updateCooldown(reload);
                }
            }
        }

        Game.eventsOut.add(new EventTankControllerUpdateS(this, this.forceMotion, this.recoil));
        this.forceMotion = false;
        this.recoil = false;
        this.dXSinceFrame = 0;
        this.dYSinceFrame = 0;

        if (this.hasCollided)
        {
            this.lastVX = this.vX;
            this.lastVY = this.vY;
            //this.lastPosX = this.posX;
            //this.lastPosY = this.posY;
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

        this.refreshAmmo();

        this.interpolatedPosX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.interpolatedPosY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;
    }

    public void refreshAmmo()
    {
        ItemBar b = this.player.hotbar.itemBar;
        ItemBullet ib = this.bullet;
        ItemMine im = this.mine;

        if (b != null && this.player.hotbar.enabledItemBar && b.selected != -1)
        {
            if (b.slots[b.selected] instanceof ItemBullet)
                ib = (ItemBullet) b.slots[b.selected];
            else if (b.slots[b.selected] instanceof ItemMine)
                im = (ItemMine) b.slots[b.selected];
        }

        if (lastLiveBullets != ib.liveBullets || ib.maxLiveBullets != lastMaxLiveBullets || im.liveMines != lastLiveMines || im.maxLiveMines != lastMaxLiveMines)
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, ib.liveBullets, ib.maxLiveBullets, im.liveMines, im.maxLiveMines));

        lastLiveBullets = ib.liveBullets;
        lastLiveMines = im.liveMines;
        lastMaxLiveBullets = ib.maxLiveBullets;
        lastMaxLiveMines = im.maxLiveMines;
    }

    public void controllerUpdate(double x, double y, double vX, double vY, double angle, double mX, double mY, boolean action1, boolean action2, double time, long receiveTime)
    {
        if (checkMotion)
        {
            if (this.startUpdateTime == -1)
                this.startUpdateTime = receiveTime;

            double prevTime = this.lastUpdateReportedTime;
            this.lastUpdateTime = (receiveTime - this.startUpdateTime) / 10.0 + this.ourTimeOffset;
            this.lastUpdateReportedTime += time;

            if (this.lastUpdateReportedTime - this.lastUpdateTime > anticheatMaxTimeOffset)
            {
                time = this.lastUpdateTime + anticheatMaxTimeOffset - prevTime;
                this.lastUpdateReportedTime = this.lastUpdateTime + anticheatMaxTimeOffset;
            }
            else if (this.lastUpdateReportedTime - this.lastUpdateTime < -anticheatMaxTimeOffset)
            {
                time = this.lastUpdateTime - anticheatMaxTimeOffset - prevTime;
                this.lastUpdateReportedTime = this.lastUpdateTime - anticheatMaxTimeOffset;
            }

            this.ourTimeOffset += Math.min(Math.max(-anticheatMaxTimeOffset, this.lastUpdateReportedTime - this.lastUpdateTime), anticheatMaxTimeOffset) * 0.02;

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

                if (!this.hasCollided && changeVelocitySq > maxChangeVelocity * maxChangeVelocity * 1.00001
                        && !(Math.abs(this.getAngleInDirection(this.posX + this.lastVX, this.posY + this.lastVY) - this.getAngleInDirection(this.posX + vX, this.posY + vY)) < 0.0001
                        && Math.abs(vX) <= Math.abs(this.lastVX) && Math.abs(vY) <= Math.abs(this.lastVY)
                        && (Math.abs(vX) >= Math.abs(this.lastVX * Math.pow(1 - this.friction * this.frictionModifier, time)) || Math.abs(this.lastVX) < 0.001)
                        && (Math.abs(vY) >= Math.abs(this.lastVY * Math.pow(1 - this.friction * this.frictionModifier, time)) || Math.abs(this.lastVY) < 0.001)))
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

                double vX2 = this.getAttributeValue(AttributeModifier.velocity, vX * ScreenGame.finishTimer / ScreenGame.finishTimerMax);
                double vY2 = this.getAttributeValue(AttributeModifier.velocity, vY * ScreenGame.finishTimer / ScreenGame.finishTimerMax);

                double maxDist = 1;

                double ourPosX = (this.lastPosX + vX2 * time);
                double ourPosY = (this.lastPosY + vY2 * time);

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
                double prevDX = this.dXSinceFrame;
                double prevDY = this.dYSinceFrame;
                this.dXSinceFrame += dX;
                this.dYSinceFrame += dY;

                double distSq = Math.pow(this.dXSinceFrame, 2) + Math.pow(this.dYSinceFrame, 2);
                if (distSq > anticheatMaxDist * anticheatMaxDist)
                {
                    double dist = Math.sqrt(distSq);
                    forceMotion = true;
                    this.dXSinceFrame *= anticheatMaxDist / dist;
                    this.dYSinceFrame *= anticheatMaxDist / dist;

                    x = this.lastPosX + this.dXSinceFrame - prevDX;
                    y = this.lastPosY + this.dYSinceFrame - prevDY;
                }
            }
            else
            {
                x = this.posX;
                y = this.posY;
                vX = this.vX;
                vY = this.vY;
            }
        }

        if (Game.screen instanceof ScreenGame)
        {
            if (!((ScreenGame) Game.screen).playing)
                return;

            double iTime = Math.max(0.1, time);

            this.interpolatedOffX = x - (this.posX - this.interpolatedOffX * (this.interpolationTime - this.interpolatedProgress) / this.interpolationTime);
            this.interpolatedOffY = y - (this.posY - this.interpolatedOffY * (this.interpolationTime - this.interpolatedProgress) / this.interpolationTime);
            this.interpolatedProgress = 0;
            this.interpolationTime = iTime;

            this.posX = x;
            this.posY = y;
            this.vX = vX;
            this.vY = vY;
            this.angle = angle;
            this.mouseX = mX;
            this.mouseY = mY;

            this.lastPosX = x;
            this.lastPosY = y;
            this.lastVX = vX;
            this.lastVY = vY;

            if (action1 && !this.disabled)
                this.shoot();

            if (action2 && !this.disabled)
                this.layMine();
        }
    }

    @Override
    public void draw()
    {
        double realPosX = this.posX;
        double realPosY = this.posY;

        this.interpolatedProgress = Math.min(this.interpolatedProgress + Panel.frameFrequency, interpolationTime);

        this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        super.draw();

        this.posX = realPosX;
        this.posY = realPosY;
    }

    @Override
    public void drawTread()
    {
        double realPosX = this.posX;
        double realPosY = this.posY;

        this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        super.drawTread();

        this.posX = realPosX;
        this.posY = realPosY;
    }

    public void layMine(Mine m)
    {
        if (Game.bulletLocked || this.destroy)
            return;

        Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

        Game.eventsOut.add(new EventLayMine(m));
        Game.movables.add(m);

        if (Crusade.crusadeMode && Crusade.currentCrusade != null)
        {
            CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(this.getPlayer());
            cp.addItemUse(m.item);
        }
    }

    public void shoot()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (this.player.hotbar.enabledItemBar)
        {
            if (this.player.hotbar.itemBar.useItem(false))
                return;
        }

        this.bullet.attemptUse(this);
    }

    public void layMine()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (this.player.hotbar.enabledItemBar)
        {
            if (this.player.hotbar.itemBar.useItem(true))
                return;
        }

        this.mine.attemptUse(this);
    }

    public void fireBullet(Bullet b, double speed, double offset)
    {
        if (speed <= 0)
            speed = Double.MIN_NORMAL;

        if (b.itemSound != null)
        {
            Drawing.drawing.playGlobalSound(b.itemSound, (float) ((Bullet.bullet_size / b.size) * (1 - (Math.random() * 0.5) * b.pitchVariation)));
        }

        b.addPolarMotion(this.angle + offset, speed);
        b.speed = speed;

        double vX = this.vX;
        double vY = this.vY;
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * this.getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);

        if (b.moveOut)
            b.moveOut(50 / speed * this.size / Game.tile_size);

        b.setTargetLocation(this.mouseX, this.mouseY);

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

//        if (b.recoil != 0)
//            this.forceMotion = true;

        if (!this.hasCollided)
            this.recoil = true;

        Game.eventsOut.add(new EventTankControllerAddVelocity(this, this.vX - vX, this.vY - vY));

        if (Crusade.crusadeMode && Crusade.currentCrusade != null)
        {
            CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(this.getPlayer());
            cp.addItemUse(b.item);
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

        for (IFixedMenu m : ModAPI.menuGroup)
        {
            if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.deaths))
            {
                if (((Scoreboard) m).players.isEmpty())
                    ((Scoreboard) m).addTeamScore(this.team, 1);
                else
                    ((Scoreboard) m).addPlayerScore(this.player, 1);
            }
        }
    }

    public void drawName()
    {
        double realPosX = this.posX;
        double realPosY = this.posY;

        this.posX = this.posX - this.interpolatedOffX * (interpolationTime - interpolatedProgress) / interpolationTime;
        this.posY = this.posY - this.interpolatedOffY * (interpolationTime - interpolatedProgress) / interpolationTime;

        Drawing.drawing.setFontSize(this.nameTag.size);
        Drawing.drawing.setColor(this.secondaryColorR, this.secondaryColorG, this.secondaryColorB);

        if (Game.enable3d)
            Drawing.drawing.drawText(this.posX + this.nameTag.ox, this.posY + this.nameTag.oy, this.posZ + this.nameTag.oz, this.nameTag.name);
        else
            Drawing.drawing.drawText(this.posX + this.nameTag.ox, this.posY + this.nameTag.oy, this.nameTag.name);

        this.posX = realPosX;
        this.posY = realPosY;
    }

    @Override
    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public void setBufferCooldown(double value)
    {
        super.setBufferCooldown(value);

        Hotbar h = this.player.hotbar;
        if (h.enabledItemBar)
        {
            for (Item i: h.itemBar.slots)
            {
                if (i != null && !(i instanceof ItemEmpty))
                {
                    i.cooldown = Math.max(i.cooldown, value);
                }
            }
        }
    }
}

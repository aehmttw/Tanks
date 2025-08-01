package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
import tanks.bullet.DefaultItems;
import tanks.effect.AttributeModifier;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.hotbar.Hotbar;
import tanks.hotbar.ItemBar;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.network.ConnectedPlayer;
import tanks.network.event.*;

public class TankPlayerRemote extends TankPlayable implements IServerPlayerTank
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

    public double interpolationTime = 1;

    public double prevKnownPosX;
    public double prevKnownPosY;
    public double prevKnownVX;
    public double prevKnownVY;
    public double prevKnownVXFinal;
    public double prevKnownVYFinal;

    public double currentKnownPosX;
    public double currentKnownPosY;
    public double currentKnownVX;
    public double currentKnownVY;

    public double lastAngle;
    public double lastPitch;
    public double currentAngle;
    public double currentPitch;

    public double timeSinceRefresh = 0;

    public int lastLiveBullets;
    public int lastMaxLiveBullets;
    public int lastLiveMines;
    public int lastMaxLiveMines;

    public long lastUpdate;
    public double localAge;

    public double bufferCooldown = 0;
    public Item.ItemStack<?> lastItem = null;

    public TankPlayerRemote(double x, double y, double angle, Player p)
    {
        super(x, y);
        this.player = p;
        this.hasName = true;
        this.angle = angle;
        this.orientation = angle;

        this.standardUpdateEvent = false;
        this.managedMotion = false;
        this.player.tank = this;

        this.abilities.add(new ItemBullet.ItemStackBullet(this.player, DefaultItems.basic_bullet.getCopy(), 0));
        this.abilities.add(new ItemMine.ItemStackMine(this.player, DefaultItems.basic_mine.getCopy(), 0));

        this.lastPosX = x;
        this.lastPosY = y;
    }

    public void updateMovement()
    {
        if (this.localAge <= 0)
        {
            this.currentKnownPosX = this.posX;
            this.currentKnownPosY = this.posY;
            this.prevKnownPosX = this.posX;
            this.prevKnownPosY = this.posY;
        }

        this.timeSinceRefresh += Panel.frameFrequency;
        this.localAge += Panel.frameFrequency;

        super.update();

        double pvx = this.prevKnownVXFinal;
        double pvy = this.prevKnownVYFinal;
        double cvx = em().getAttributeValue(AttributeModifier.velocity, this.currentKnownVX) * ScreenGame.finishTimer / ScreenGame.finishTimerMax;
        double cvy = em().getAttributeValue(AttributeModifier.velocity, this.currentKnownVY) * ScreenGame.finishTimer / ScreenGame.finishTimerMax;

        this.posX = TankRemote.cubicInterpolationVelocity(this.prevKnownPosX, pvx, this.currentKnownPosX, cvx, this.timeSinceRefresh, this.interpolationTime);
        this.posY = TankRemote.cubicInterpolationVelocity(this.prevKnownPosY, pvy, this.currentKnownPosY, cvy, this.timeSinceRefresh, this.interpolationTime);

        //System.out.printf("t: %f %f | pos: %f %f -> %f %f, vel %f %f -> %f %f\n", this.timeSinceRefresh, this.interpolationTime, +this.prevKnownPosX, this.prevKnownPosY, this.currentKnownPosX, this.currentKnownPosY, this.prevKnownVX, this.prevKnownVY, this.currentKnownVX, this.currentKnownVY);
        //System.out.printf("pos: %f %f\n", this.posX, this.posY);

        double frac = Math.min(1, this.timeSinceRefresh / this.interpolationTime);
        this.vX = (1 - frac) * this.prevKnownVX + frac * this.currentKnownVX;
        this.vY = (1 - frac) * this.prevKnownVY + frac * this.currentKnownVY;

        this.lastFinalVX = (this.posX - super.lastPosX) / Panel.frameFrequency;
        this.lastFinalVY = (this.posY - super.lastPosY) / Panel.frameFrequency;

        double angDiff = GameObject.angleBetween(this.lastAngle, this.currentAngle);
        this.angle = this.lastAngle - frac * angDiff;
        this.pitch = (1 - frac) * this.lastPitch + frac * this.currentPitch;

        this.checkCollision();

        this.orientation = (this.orientation + Math.PI * 2) % (Math.PI * 2);

        if (!(Math.abs(this.posX - super.lastPosX) < 0.01 && Math.abs(this.posY - super.lastPosY) < 0.01) && !this.destroy && !ScreenGame.finished)
        {
            double dist = Math.sqrt(Math.pow(this.posX - super.lastPosX, 2) + Math.pow(this.posY - super.lastPosY, 2));

            double dir = Math.PI + this.getAngleInDirection(super.lastPosX, super.lastPosY);
            if (GameObject.absoluteAngleBetween(this.orientation, dir) <= GameObject.absoluteAngleBetween(this.orientation + Math.PI, dir))
                this.orientation -= GameObject.angleBetween(this.orientation, dir) / 20 * dist;
            else
                this.orientation -= GameObject.angleBetween(this.orientation + Math.PI, dir) / 20 * dist;
        }
    }

    @Override
    public void update()
    {
        this.updateMovement();

        this.bufferCooldown -= Panel.frameFrequency;
        double reload = em().getAttributeValue(AttributeModifier.reload, 1);

        for (Item.ItemStack<?> s: this.abilities)
        {
            s.player = this.player;
            s.updateCooldown(reload);
        }

        Hotbar h = this.player.hotbar;
        if (h.enabledItemBar)
        {
            for (Item.ItemStack<?> i: h.itemBar.slots)
            {
                if (i != null && !(i.isEmpty))
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
            //System.out.println("collision");
            this.lastVX = this.vX;
            this.lastVY = this.vY;
            //this.lastPosX = this.posX;
            //this.lastPosY = this.posY;

            this.prevKnownPosX = this.posX;
            this.prevKnownPosY = this.posY;
            this.prevKnownVX = this.vX;
            this.prevKnownVY = this.vY;
            this.prevKnownVXFinal = this.lastFinalVX;
            this.prevKnownVYFinal = this.lastFinalVY;
            this.lastAngle = this.angle;
            //this.interpolationTime -= this.timeSinceRefresh;
            this.timeSinceRefresh = 0;
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
    }

    public void refreshAmmo()
    {
        ItemBar b = this.player.hotbar.itemBar;
        ItemBullet.ItemStackBullet ib = null;
        ItemMine.ItemStackMine im = null;

        if (this.getPrimaryAbility() instanceof ItemBullet.ItemStackBullet)
            ib = (ItemBullet.ItemStackBullet) this.getPrimaryAbility();

        if (this.getSecondaryAbility() instanceof ItemMine.ItemStackMine)
            im = (ItemMine.ItemStackMine) this.getSecondaryAbility();

        int hotbarSlots = (this.player.hotbar.itemBar.showItems ? ItemBar.item_bar_size : 0);

        if (b != null && this.player.hotbar.enabledItemBar && b.selected != -1 && b.selected < hotbarSlots)
        {
            if (b.slots[b.selected] instanceof ItemBullet.ItemStackBullet)
                ib = (ItemBullet.ItemStackBullet) b.slots[b.selected];
            else if (b.slots[b.selected] instanceof ItemMine.ItemStackMine)
                im = (ItemMine.ItemStackMine) b.slots[b.selected];
        }

        int lb = ib == null ? 0 : ib.liveBullets;
        int lm = im == null ? 0 : im.liveMines;

        int mlb = ib == null ? 0 : ib.item.bullet.maxLiveBullets;
        int mlm = im == null ? 0 : im.item.mine.maxLiveMines;

        double cooldown = ib == null ? 0 : ib.cooldown;
        double cooldownBase = ib == null ? 0 : ib.item.cooldownBase;

        double cooldown2 = im == null ? 0 : im.cooldown;
        double cooldownBase2 = im == null ? 0 : im.item.cooldownBase;

        if (lastLiveBullets != lb || mlb != lastMaxLiveBullets || lm != lastLiveMines || mlm != lastMaxLiveMines)
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, lb, mlb, lm, mlm, cooldown, cooldownBase, cooldown2, cooldownBase2));

        lastLiveBullets = lb;
        lastLiveMines = lm;
        lastMaxLiveBullets = mlb;
        lastMaxLiveMines = mlm;
    }

    public void controllerUpdate(double x, double y, double vX, double vY, double angle, double mX, double mY, boolean action1, boolean action2, boolean[] quickActions, double time, long receiveTime)
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

                double vX2 = em().getAttributeValue(AttributeModifier.velocity, vX * ScreenGame.finishTimer / ScreenGame.finishTimerMax);
                double vY2 = em().getAttributeValue(AttributeModifier.velocity, vY * ScreenGame.finishTimer / ScreenGame.finishTimerMax);

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

            this.currentKnownPosX = x;
            this.currentKnownPosY = y;
            this.currentKnownVX = vX;
            this.currentKnownVY = vY;

            this.prevKnownPosX = this.posX;
            this.prevKnownPosY = this.posY;
            this.prevKnownVX = this.vX;
            this.prevKnownVY = this.vY;
            this.prevKnownVXFinal = this.lastFinalVX;
            this.prevKnownVYFinal = this.lastFinalVY;

            this.lastAngle = this.angle;
            this.lastPitch = this.pitch;
            this.currentAngle = angle;

            Item.ItemStack<?> ib = this.player.hotbar.itemBar.getSelectedAction(false);
            Bullet b = null;
            if (ib instanceof ItemBullet.ItemStackBullet)
                b = ((ItemBullet.ItemStackBullet) ib).item.bullet;

            if (!(b instanceof BulletArc || b instanceof BulletAirStrike))
                this.pitch -= GameObject.angleBetween(this.pitch, 0) / 10 * Panel.frameFrequency;

            if (b instanceof BulletArc)
            {
                double pitch = Math.atan(GameObject.distanceBetween(this.posX, this.posY, this.mouseX, this.mouseY) / b.speed * 0.5 * BulletArc.gravity / b.speed);
                this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
            }
            else if (b instanceof BulletAirStrike)
            {
                double pitch = Math.PI / 2;
                this.pitch -= GameObject.angleBetween(this.pitch, pitch) / 10 * Panel.frameFrequency;
            }

            this.currentPitch = this.pitch;

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

            long t = System.currentTimeMillis();
            this.interpolationTime = Math.min(100, (t - this.lastUpdate) / 10.0);
            this.lastUpdate = t;

            this.timeSinceRefresh = 0;
            this.lastUpdateTime = t;

            if (action1 && !this.disabled)
                this.action(false);

            if (action2 && !this.disabled)
                this.action(true);

            if (!this.disabled)
            {
                for (int i = 0; i < this.abilities.size(); i++)
                {
                    if (quickActions[i])
                        this.quickAction(i);
                }
            }

            this.posX = this.prevKnownPosX;
            this.posY = this.prevKnownPosY;
            this.vX = this.prevKnownVX;
            this.vY = this.prevKnownVY;
            this.angle = this.lastAngle;
        }
    }

    public void layMine(Mine m)
    {
        if (Game.bulletLocked || this.destroy)
            return;

        Drawing.drawing.playGlobalSound("lay_mine.ogg", (float) (Mine.mine_size / m.size));

        if (m.item.networkIndex >= 0)
        {
            Integer num = 0;
            if (Game.currentLevel != null)
                num = Game.currentLevel.itemNumbers.get(m.item.item.name);
            m.item.networkIndex = num == null ? 0 : num;
        }

        Game.eventsOut.add(new EventLayMine(m));
        Game.movables.add(m);

        if (Crusade.crusadeMode && Crusade.currentCrusade != null)
        {
            CrusadePlayer cp = Crusade.currentCrusade.getCrusadePlayer(this.getPlayer());
            cp.addItemUse(m.item);
        }
    }

    public void action(boolean right)
    {
        if (Game.bulletLocked || this.destroy)
            return;

        int a = right ? selectedSecondaryAbility : selectedPrimaryAbility;

        if (this.player.hotbar.itemBar.getSelectedAction(right) != this.lastItem && this.bufferCooldown > 0)
            return;

        if (this.player.hotbar.enabledItemBar)
        {
            if (this.player.hotbar.itemBar.useItem(right))
                return;
        }

        Item.ItemStack<?> s = right ? this.getSecondaryAbility() : this.getPrimaryAbility();
        if (s != null)
        {
            s.networkIndex = -a;
            if (s.attemptUse(this))
                Game.eventsOut.add(new EventUpdateTankAbility(this.player, right ? this.selectedSecondaryAbility : this.selectedPrimaryAbility));
        }
    }

    public void quickAction(int click)
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (this.abilities.size() > click)
        {
            this.getAbility(click).networkIndex = -click;

            if (this.getAbility(click) != this.lastItem && this.bufferCooldown > 0)
                return;

            if (this.getAbility(click).attemptUse(this))
                Game.eventsOut.add(new EventUpdateTankAbility(this.player, click));
        }
    }

    public void fireBullet(Bullet b, double speed, double offset)
    {
        if (speed <= 0)
            speed = Double.MIN_NORMAL;

        b.addPolarMotion(this.angle + offset, speed);
        b.speed = speed;

        double vX = this.vX;
        double vY = this.vY;
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 32.0 * b.recoil * em().getAttributeValue(AttributeModifier.recoil, 1) * b.frameDamageMultipler);

        if (b.moveOut)
            b.moveOut(50 * this.size / Game.tile_size);

        double mx = this.mouseX - this.posX;
        double my = this.mouseY - this.posY;

        double tx = Math.cos(offset) * mx + Math.sin(offset) * my;
        double ty = -Math.sin(offset) * mx + Math.cos(offset) * my;
        b.setTargetLocation(this.posX + tx, this.posY + ty);

        if (b.item.networkIndex >= 0)
        {
            Integer num = 0;
            if (Game.currentLevel != null)
                num = Game.currentLevel.itemNumbers.get(b.item.item.name);
            b.item.networkIndex = num == null ? 0 : num;
        }

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

//        if (b.recoil != 0)
//            this.forceMotion = true;

        if (!this.hasCollided && b.recoil != 0)
        {
            this.recoil = true;
            Game.eventsOut.add(new EventTankControllerAddVelocity(this, this.vX - vX, this.vY - vY, true));
        }

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

        if (Game.screen instanceof ScreenGame)
        {
            if (ScreenPartyHost.includedPlayers.contains(this.player.clientID))
            {
                ((ScreenGame) Game.screen).eliminatedPlayers.add(new ConnectedPlayer(this.player));
                Game.eventsOut.add(new EventUpdateEliminatedPlayers(((ScreenGame) Game.screen).eliminatedPlayers));
            }
            ((ScreenGame) Game.screen).onPlayerDeath(this.player);
        }
    }

    @Override
    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public void setBufferCooldown(Item.ItemStack<?> stack, double value)
    {
        this.lastItem = stack;
        this.bufferCooldown = value;
    }
}

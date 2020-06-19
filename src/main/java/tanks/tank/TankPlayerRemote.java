package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;
import tanks.event.EventTankControllerUpdateAmmunition;
import tanks.event.EventTankControllerUpdateS;
import tanks.hotbar.ItemBar;
import tanks.hotbar.ItemBullet;

import java.util.ArrayList;

public class TankPlayerRemote extends Tank
{
    public double lastPosX;
    public double lastPosY;
    public double lastVX;
    public double lastVY;

    public long lastUpdateTime = -1;
    public long lastUpdateFrame = -1;

    public ArrayList<Bullet> recentBullets = new ArrayList<Bullet>();

    public boolean forceMotion = false;

    public Player player;

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
    }

    @Override
    public void update()
    {
        super.update();

        if (this.cooldown > 0)
            this.cooldown -= Panel.frameFrequency;

        Game.eventsOut.add(new EventTankControllerUpdateS(this, this.forceMotion));
        this.forceMotion = false;

        ItemBar b = this.player.crusadeItemBar;
        if (Crusade.crusadeMode && b.selected != -1 && b.slots[b.selected] instanceof ItemBullet)
        {
            ItemBullet ib = (ItemBullet) b.slots[b.selected];
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, ib.liveBullets, ib.maxAmount, this.liveMines, this.liveMinesMax));
        }
        else
            Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.player.clientID, this.liveBullets, this.liveBulletMax, this.liveMines, this.liveMinesMax));
    }

    public void controllerUpdate(double x, double y, double vX, double vY, double angle, boolean action1, boolean action2, int frame, long time)
    {
        //Anticheat things will be added here

        if (frame != this.lastUpdateFrame)
            this.recentBullets.clear();
        else
        {
            for (Bullet recentBullet : this.recentBullets)
            {
                recentBullet.moveOut(Math.max(0, time - this.lastUpdateTime) / 200.0);
            }
        }

        this.lastUpdateTime = time;
        this.lastUpdateFrame = frame;

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

    public void layMine()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        if (Crusade.crusadeMode)
        {
            if (this.player.crusadeItemBar.useItem(true))
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
            if (this.player.crusadeItemBar.useItem(false))
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

        b.moveOut((int) (25.0 / speed * 2));
        b.effect = effect;

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

        this.recentBullets.add(b);

        this.forceMotion = true;
    }

    public void fireBullet(Bullet b, double speed)
    {
        if (b.itemSound != null)
            Drawing.drawing.playGlobalSound(b.itemSound, (float) (Bullet.bullet_size / b.size));

        b.addPolarMotion(this.angle, speed);
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0 * b.recoil);

        b.moveOut((int) (25.0 / speed * 2 * this.size / Game.tile_size));

        Game.eventsOut.add(new EventShootBullet(b));
        Game.movables.add(b);

        if (b.recoil != 0)
            this.forceMotion = true;
    }

    @Override
    public void onDestroy()
    {
        if (Crusade.crusadeMode)
            this.player.remainingLives--;
    }
}

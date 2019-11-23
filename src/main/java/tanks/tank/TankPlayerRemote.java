package tanks.tank;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.event.EventShootBullet;
import tanks.event.EventTankControllerUpdateAmmunition;
import tanks.event.EventTankControllerUpdateS;

import java.util.UUID;

public class TankPlayerRemote extends Tank
{
    public double lastPosX;
    public double lastPosY;
    public double lastVX;
    public double lastVY;

    public boolean forceMotion = false;

    public UUID clientID;

    public TankPlayerRemote(double x, double y, double angle, UUID id)
    {
        super("player", x, y, Game.tank_size, 0, 150, 255);
        this.clientID = id;
        this.showName = true;
        this.angle = angle;

        this.liveBulletMax = 5;
        this.liveMinesMax = 2;
        this.standardUpdateEvent = false;
    }

    @Override
    public void update()
    {
        super.update();

        if (this.cooldown > 0)
            this.cooldown -= Panel.frameFrequency;

        Game.eventsOut.add(new EventTankControllerUpdateS(this, this.forceMotion));
        this.forceMotion = false;

        Game.eventsOut.add(new EventTankControllerUpdateAmmunition(this.clientID, this.liveBullets, this.liveBulletMax, this.liveMines, this.liveMinesMax));
    }

    public void controllerUpdate(double x, double y, double vX, double vY, double angle, boolean action1, boolean action2)
    {
        //Anticheat things will be added here

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

        /*if (Panel.panel.hotbar.enabledItemBar)
        {
            if (Panel.panel.hotbar.currentItemBar.useItem(true))
                return;
        }*/

        Drawing.drawing.playSound("/lay-mine.wav");

        this.cooldown = 50;
        Mine m = new Mine(posX, posY, this);

        Game.movables.add(m);
    }

    public void shoot()
    {
        if (Game.bulletLocked || this.destroy)
            return;

        /*if (Panel.panel.hotbar.enabledItemBar)
        {
            if (Panel.panel.hotbar.currentItemBar.useItem(false))
                return;
        }*/

        this.cooldown = 20;

        fireBullet(25 / 4.0, 1, Bullet.BulletEffect.trail);
    }

    public void fireBullet(double speed, int bounces, Bullet.BulletEffect effect)
    {
        Drawing.drawing.playSound("/shoot.wav");

        Bullet b = new Bullet(posX, posY, bounces, this);
        b.addPolarMotion(this.angle, speed);
        this.addPolarMotion(b.getPolarDirection() + Math.PI, 25.0 / 16.0);

        b.moveOut((int) (25.0 / speed * 2));
        b.effect = effect;

        Game.eventsOut.add(new EventShootBullet(b));

        Game.movables.add(b);
    }
}

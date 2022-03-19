package tanks.obstacle;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.bullet.BulletFlame;
import tanks.event.EventLayMine;
import tanks.event.EventObstacleDestroy;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.tank.IAvoidObject;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

public class ObstacleExplosive extends Obstacle implements IAvoidObject
{
    public double timer = 25;
    public Tank trigger = Game.dummyTank;
    public Item itemTrigger = null;

    public ObstacleExplosive(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.draggable = false;
        this.destructible = true;
        this.allowBounce = false;
        this.colorR = 255;
        this.colorG = Math.random() * 40 + 80;
        this.colorB = 0;
        this.glow = 0.5;

        if (!Game.fancyTerrain)
            this.colorG = 100;

        for (int i = 0; i < default_max_height; i++)
        {
            this.stackColorR[i] = 255;
            this.stackColorG[i] = Math.random() * 40 + 80;
            this.stackColorB[i] = 0;

            if (!Game.fancyTerrain)
                this.stackColorG[i] = 100;
        }

        this.destroyEffectAmount = 0;
        this.checkForObjects = true;
        this.description = "A block which explodes upon contact";
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (this.trigger != Game.dummyTank)
            return;

        if ((m instanceof Bullet && !(m instanceof BulletFlame)) || m instanceof Tank)
        {
            if (m instanceof Bullet)
            {
                this.trigger = ((Bullet) m).tank;
                this.itemTrigger = ((Bullet) m).item;

                if (((Bullet) m).item == null)
                    this.itemTrigger = TankPlayer.default_bullet;
            }
            else
                this.trigger = (Tank) m;

            this.explode();
        }
    }

    @Override
    public void onDestroy(Movable m)
    {
        if (this.trigger != Game.dummyTank)
            return;

        if (!ScreenPartyLobby.isClient)
            this.update = true;

        if (m instanceof Mine)
        {
            this.trigger = ((Mine) m).tank;
            this.itemTrigger = ((Mine) m).item;

            if (((Mine) m).item == null)
                this.itemTrigger = TankPlayer.default_mine;
        }
    }

    @Override
    public void update()
    {
        if (this.timer <= 0)
            this.explode();

        this.timer -= Panel.frameFrequency;
    }

    public void explode()
    {
        if (ScreenPartyLobby.isClient)
            return;

        Mine mi = new Mine(this.posX, this.posY, 0, this.trigger);
        mi.item = this.itemTrigger;
        mi.radius *= (this.stackHeight - 1) / 2 + 1;
        Game.eventsOut.add(new EventLayMine(mi));
        Game.movables.add(mi);

        Game.removeObstacles.add(this);
        Game.eventsOut.add(new EventObstacleDestroy(this.posX, this.posY));
    }

    @Override
    public double getRadius()
    {
        return Game.tile_size * 1.25 * this.stackHeight;
    }
}

package tanks.obstacle;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.network.event.EventObstacleDestroy;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.rendering.ShaderExplosive;
import tanks.tank.*;

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
        this.colorR = 255;
        this.colorG = Math.random() * 40 + 80;
        this.colorB = 0;

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
        this.shouldShootThrough = true;
        this.description = "A block which explodes upon contact";

        this.renderer = ShaderExplosive.class;
    }

    @Override
    public void onObjectEntry(Movable m)
    {
        if (this.trigger != Game.dummyTank)
            return;

        if (m instanceof Bullet || m instanceof Tank)
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

        if (m instanceof Explosion)
        {
            this.trigger = ((Explosion) m).tank;
            this.itemTrigger = ((Explosion) m).item;

            if (((Explosion) m).item == null)
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

        Explosion e = new Explosion(this.posX, this.posY, this.getRadius(), 2, true, this.trigger, this.itemTrigger);
        e.explode();

        Game.removeObstacles.add(this);
        Game.eventsOut.add(new EventObstacleDestroy(this.posX, this.posY, this.name));
    }

    @Override
    public double getRadius()
    {
        return Mine.mine_radius * ((this.stackHeight - 1) / 2 + 1);
    }

    @Override
    public double getSeverity(double posX, double posY)
    {
        return Math.sqrt(Math.pow(posX - this.posX, 2) + Math.pow(posY - this.posY, 2));
    }
}

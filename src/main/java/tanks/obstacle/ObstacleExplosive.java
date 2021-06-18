package tanks.obstacle;

import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.event.EventLayMine;
import tanks.event.EventObstacleDestroy;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ObstacleExplosive extends Obstacle
{
    public double timer = 25;

    public ObstacleExplosive(String name, double posX, double posY)
    {
        super(name, posX, posY);

        this.draggable = false;
        this.destructible = true;
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
        if (m instanceof Bullet || m instanceof Tank)
        {
            this.explode();
        }
    }

    @Override
    public void onDestroy()
    {
        if (!ScreenPartyLobby.isClient)
            this.update = true;
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

        Mine mi = new Mine(this.posX, this.posY, 0, Game.dummyTank);
        mi.radius *= (this.stackHeight - 1) / 2 + 1;
        Game.eventsOut.add(new EventLayMine(mi));
        Game.movables.add(mi);

        Game.removeObstacles.add(this);
        Game.eventsOut.add(new EventObstacleDestroy(this.posX, this.posY));
    }
}

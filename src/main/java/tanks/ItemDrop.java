package tanks;

import tanks.hotbar.item.Item;
import tanks.tank.TankPlayer;

public class ItemDrop extends Movable
{
    public Item item;
    public double height;
    public double size = Game.tile_size * 1.5;
    public double destroyTime = 0;
    public double maxDestroyTime = 50;

    public ItemDrop(double x, double y, Item item)
    {
        super(x, y);

        this.item = item;
        this.drawLevel = 2;

        if (Game.enable3d && Game.enable3dBg && Game.fancyTerrain)
        {
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - size / 2, this.posY - size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + size / 2, this.posY - size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - size / 2, this.posY + size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + size / 2, this.posY + size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX, this.posY + size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX, this.posY - size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - size / 2, this.posY));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + size / 2, this.posY));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX, this.posY));
        }
    }

    @Override
    public void draw()
    {
        double size = this.size * (1 - this.destroyTime / this.maxDestroyTime);

        if (Game.enable3d)
        {
            for (int i = 0; i <= 8; i++)
            {
                Drawing.drawing.setColor(255 * i / 8.0, 255 * i / 8.0, 255 * i / 8.0, 255, 0.5);
                Drawing.drawing.drawImage("item.png", this.posX, this.posY, this.height + i, size, size);
            }
            Drawing.drawing.drawImage(this.item.icon, this.posX, this.posY + size / 8, this.height + 9, size / 2, size / 2);
        }
        else
        {
            Drawing.drawing.setColor(255, 255, 255, 255, 0.5);
            Drawing.drawing.drawImage("item.png", this.posX, this.posY, this.height, size, size);
            Drawing.drawing.drawImage(this.item.icon, this.posX, this.posY + size / 8, size / 2, size / 2);
        }
    }

    @Override
    public void update()
    {
        if (this.destroy)
        {
            this.destroyTime += Panel.frameFrequency;
            if (this.destroyTime > this.maxDestroyTime)
                Game.removeMovables.add(this);
        }
        else
        {
            for (Movable m: Game.movables)
            {
                if (m instanceof TankPlayer && Movable.distanceBetween(this, m) < this.size)
                {
                    boolean added = ((TankPlayer) m).player.hotbar.itemBar.addItem(this.item);

                    if (added)
                    {
                        this.destroy = true;
                        Drawing.drawing.playSound("bullet_explode.ogg", 1.6f);
                    }
                }
            }
        }
    }
}

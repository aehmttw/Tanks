package tanks;

import tanks.hotbar.item.Item;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;

public class ItemDrop extends Movable
{
    public Item item;
    public double height;
    public double size = Game.tile_size * 1.5;
    public double destroyTime = 0;
    public double maxDestroyTime = 50;
    public Tank pickup = null;

    public ItemDrop(double x, double y, Item item)
    {
        super(x, y);

        this.item = item;
        this.drawLevel = 2;

        if (Game.enable3d)
            this.drawLevel = 8;

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
        double frac = (this.destroyTime / this.maxDestroyTime);
        double size = this.size * (1 - frac);

        double px = this.posX;
        double py = this.posY + size / 8;
        double pz = this.posZ + this.height + 9;
        double s = this.size * Math.min(1, 2 - frac * 2);

        if (this.pickup != null)
        {
            px = px * (1 - frac) + pickup.posX * frac;
            py = py * (1 - frac) + pickup.posY * frac;

            double startHeight = this.height + 9;
            double endHeight = this.pickup.size / 2;
            pz = startHeight * (1 - frac) + endHeight * frac + this.pickup.size * (1 - Math.pow(2 * (frac - 0.5), 2));
        }
        else
            size = s;

        if (Game.enable3d)
        {
            for (int i = 0; i <= 8; i++)
            {
                Drawing.drawing.setColor(255 * i / 8.0, 255 * i / 8.0, 255 * i / 8.0, 255, 0.5);
                Drawing.drawing.drawImage("item.png", this.posX, this.posY, this.height + i, size, size);
            }

            Drawing.drawing.drawImage(this.item.icon, px, py, pz, s / 2, s / 2);
        }
        else
        {
            Drawing.drawing.setColor(255, 255, 255, 255, 0.5);
            Drawing.drawing.drawImage("item.png", this.posX, this.posY, this.height, size, size);
            Drawing.drawing.drawImage(this.item.icon, px, py, s / 2, s / 2);
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
                        this.pickup = (Tank) m;
                        this.destroy = true;
                        Drawing.drawing.playSound("bullet_explode.ogg", 1.6f);
                    }
                }
            }
        }
    }
}

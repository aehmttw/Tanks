package tanks.hotbar.item;

import tanks.Game;
import tanks.Player;
import tanks.gui.property.UIPropertyBoolean;
import tanks.gui.property.UIPropertyDouble;
import tanks.gui.property.UIPropertyInt;
import tanks.tank.Mine;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

public class ItemMine extends Item
{
    public static final String item_name = "mine";

    public double timer;
    public double triggeredTimer;
    public double radius;
    public double damage;
    public int maxAmount;
    public double cooldown;
    public double size;
    public boolean destroysObstacles;

    public int liveMines;

    public ItemMine(Player p)
    {
        super(p);
        this.rightClick = true;
        this.isConsumable = true;

        new UIPropertyDouble(this.properties, "fuse_length", 1000.0);
        new UIPropertyDouble(this.properties, "triggered_fuse", 50.0);
        new UIPropertyDouble(this.properties, "explosion_radius", Game.tile_size * 2.5);
        new UIPropertyDouble(this.properties, "damage", 2.0);
        new UIPropertyInt(this.properties, "max_live_mines", 2);
        new UIPropertyDouble(this.properties, "cooldown", 50.0);
        new UIPropertyDouble(this.properties, "size", Mine.mine_size);
        new UIPropertyBoolean(this.properties, "destroys_blocks", true);

        this.supportsHits = true;
    }

    public ItemMine()
    {
        this(null);
    }

    @Override
    public void use()
    {
        Tank t = this.getUser();

        Mine m = new Mine(t.posX, t.posY, this.timer, t, this);

        t.cooldown = this.cooldown;
        m.timer = this.timer;
        m.triggeredTimer = this.triggeredTimer;
        m.radius = this.radius;
        m.damage = this.damage;
        m.cooldown = this.cooldown;
        m.size = this.size;
        m.destroysObstacles = this.destroysObstacles;

        if (t instanceof TankPlayerRemote)
            ((TankPlayerRemote) t).layMine(m);
        else if (t instanceof TankPlayer)
            ((TankPlayer) t).layMine(m);

        this.stackSize--;

        if (this.stackSize <= 0)
            this.destroy = true;
    }

    @Override
    public boolean usable()
    {
        Tank t = this.getUser();
        return t != null && (this.maxAmount <= 0 || this.liveMines < this.maxAmount) && !(t.cooldown > 0) && this.stackSize > 0;
    }

    @Override
    public String toString()
    {
        return super.toString() + "," + item_name + "," + timer + "," + triggeredTimer + "," + radius + "," + damage + "," + maxAmount + "," + cooldown + "," + size + "," + destroysObstacles;
    }

    @Override
    public void fromString(String s)
    {
        String[] p = s.split(",");

        this.timer = Double.parseDouble(p[0]);
        this.triggeredTimer = Double.parseDouble(p[1]);
        this.radius = Double.parseDouble(p[2]);
        this.damage = Double.parseDouble(p[3]);
        this.maxAmount = Integer.parseInt(p[4]);
        this.cooldown = Double.parseDouble(p[5]);
        this.size = Double.parseDouble(p[6]);
        this.destroysObstacles = Boolean.parseBoolean(p[7]);
    }

    @Override
    public void importProperties()
    {
        super.importProperties();

        this.setProperty("fuse_length", this.timer);
        this.setProperty("triggered_fuse", this.triggeredTimer);
        this.setProperty("explosion_radius", this.radius);
        this.setProperty("damage", this.damage);
        this.setProperty("max_live_mines", this.maxAmount);
        this.setProperty("cooldown", this.cooldown);
        this.setProperty("size", this.size);
        this.setProperty("destroys_blocks", this.destroysObstacles);
    }

    @Override
    public String getTypeName()
    {
        return "Mine";
    }

    @Override
    public void exportProperties()
    {
        super.exportProperties();

        this.timer = (double) this.getProperty("fuse_length");
        this.triggeredTimer = (double) this.getProperty("triggered_fuse");
        this.radius = (double) this.getProperty("explosion_radius");
        this.damage = (double) this.getProperty("damage");
        this.maxAmount = (int) this.getProperty("max_live_mines");
        this.cooldown = (double) this.getProperty("cooldown");
        this.size = (double) this.getProperty("size");
        this.destroysObstacles = (boolean) this.getProperty("destroys_blocks");
    }
}

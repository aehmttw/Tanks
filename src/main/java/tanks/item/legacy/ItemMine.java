package tanks.item.legacy;

import tanks.Game;
import tanks.Player;
import tanks.gui.property.UIPropertyBoolean;
import tanks.gui.property.UIPropertyDouble;
import tanks.gui.property.UIPropertyInt;
import tanks.tank.Mine;
import tanks.tank.Tank;

public class ItemMine extends Item
{
    public static final String item_name = "mine";

    public double timer;
    public double triggeredTimer;
    public double radius;
    public double damage;
    public int maxLiveMines;
    public double cooldownBase;
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
    public void use(Tank t)
    {
        Mine m = new Mine(t.posX, t.posY, this.timer, t, this);

        this.setOtherItemsCooldown();
        this.cooldown = this.cooldownBase;
        m.timer = this.timer;
        m.triggeredTimer = this.triggeredTimer;
        m.radius = this.radius;
        m.damage = this.damage;
        m.size = this.size;
        m.destroysObstacles = this.destroysObstacles;

        t.layMine(m);

        if (!this.unlimitedStack)
            this.stackSize--;

        if (this.stackSize <= 0)
            this.destroy = true;
    }

    @Override
    public boolean usable(Tank t)
    {
        return t != null && (this.maxLiveMines <= 0 || this.liveMines < this.maxLiveMines) && !(this.cooldown > 0) && this.stackSize > 0;
    }

    @Override
    public String convertToString()
    {
        return super.convertToString() + "," + item_name + "," + timer + "," + triggeredTimer + "," + radius + "," + damage + "," + maxLiveMines + "," + cooldownBase + "," + size + "," + destroysObstacles;
    }

    @Override
    public void fromString(String s)
    {
        String[] p = s.split(",");

        this.timer = Double.parseDouble(p[0]);
        this.triggeredTimer = Double.parseDouble(p[1]);
        this.radius = Double.parseDouble(p[2]);
        this.damage = Double.parseDouble(p[3]);
        this.maxLiveMines = Integer.parseInt(p[4]);
        this.cooldownBase = Double.parseDouble(p[5]);
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
        this.setProperty("max_live_mines", this.maxLiveMines);
        this.setProperty("cooldown", this.cooldownBase);
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
        this.maxLiveMines = (int) this.getProperty("max_live_mines");
        this.cooldownBase = (double) this.getProperty("cooldown");
        this.size = (double) this.getProperty("size");
        this.destroysObstacles = (boolean) this.getProperty("destroys_blocks");
    }
}

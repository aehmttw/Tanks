package tanks.hotbar.item;

import tanks.*;
import tanks.gui.property.UIPropertyDouble;
import tanks.network.event.EventTankUpdateHealth;
import tanks.tank.Tank;

public class ItemShield extends Item
{
    public static final String item_name = "shield";

    public double amount;
    public double max;
    public double cooldownBase;

    public ItemShield(Player p)
    {
        super(p);
        this.rightClick = true;
        this.isConsumable = true;

        new UIPropertyDouble(this.properties, "health_boost", 1.0);
        new UIPropertyDouble(this.properties, "max_extra_health", 5.0);
        new UIPropertyDouble(this.properties, "cooldown", 50.0);
    }

    public ItemShield()
    {
        this(null);
    }

    @Override
    public void use(Tank t)
    {
        t.health += amount;

        if (t.health > max)
            t.health = max;

        Game.eventsOut.add(new EventTankUpdateHealth(t));

        Drawing.drawing.playGlobalSound("shield.ogg");

        if (Crusade.crusadeMode && Crusade.currentCrusade != null && this.player != null)
            Crusade.currentCrusade.getCrusadePlayer(this.player).addItemUse(this);

        this.stackSize--;

        if (this.stackSize <= 0)
            this.destroy = true;

        this.setOtherItemsCooldown();
        this.cooldown = this.cooldownBase;

        if (t.health > 6 && (int) (t.health - amount) != (int) (t.health))
        {
            Effect e = Effect.createNewEffect(t.posX, t.posY, t.posZ + t.size * 0.75, Effect.EffectType.shield);
            e.size = t.size;
            e.radius = t.health - 1;
            Game.effects.add(e);
        }
    }

    @Override
    public boolean usable(Tank t)
    {
        return (this.max <= 0 || t.health < this.max) && this.stackSize > 0 && this.cooldown <= 0;
    }

    @Override
    public String convertToString()
    {
        return super.convertToString() + "," + item_name + "," + amount + "," + max + "," + cooldownBase;
    }

    @Override
    public void fromString(String s)
    {
        String[] p = s.split(",");

        this.amount = Double.parseDouble(p[0]);
        this.max = Double.parseDouble(p[1]);
        this.cooldownBase = Double.parseDouble(p[2]);
    }

    @Override
    public void importProperties()
    {
        super.importProperties();

        this.setProperty("health_boost", this.amount);
        this.setProperty("max_extra_health", this.max);
        this.setProperty("cooldown", this.cooldownBase);
    }

    @Override
    public String getTypeName()
    {
        return "Shield";
    }

    @Override
    public void exportProperties()
    {
        super.exportProperties();

        this.amount = (double) this.getProperty("health_boost");
        this.max = (double) this.getProperty("max_extra_health");
        this.cooldownBase = (double) this.getProperty("cooldown");
    }
}

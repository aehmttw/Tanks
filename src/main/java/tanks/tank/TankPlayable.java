package tanks.tank;

import tanks.Game;
import tanks.bullet.Bullet;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tankson.ICopyable;
import tanks.tankson.Property;
import tanks.tankson.Serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class TankPlayable extends Tank implements ICopyable<TankPlayable>
{
    @TankBuildProperty @Property(id = "abilities", name = "Abilities", category = TankPropertyCategory.abilities)
    public ArrayList<Item.ItemStack<?>> abilities = new ArrayList<>();

    public int selectedPrimaryAbility = 0;
    public int selectedSecondaryAbility = 1;

    public static Bullet default_bullet;
    public static Mine default_mine;

    public static String default_bullet_name = "Basic bullet";
    public static String default_mine_name = "Basic mine";

    public TankPlayable(double x, double y)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
    }

    public void addDefaultAbilities()
    {
        this.abilities.add(new ItemBullet.ItemStackBullet(null, new ItemBullet(default_bullet.clonePropertiesTo(new Bullet())), 0));
        this.abilities.add(new ItemMine.ItemStackMine(null, new ItemMine(default_mine.clonePropertiesTo(new Mine())), 0));

        this.abilities.get(0).item.name = default_bullet_name;
        this.abilities.get(1).item.name = default_mine_name;
        this.abilities.get(1).item.cooldownBase = 50;
    }

    public void updateAbilities()
    {
        if (this.getAbility(this.selectedPrimaryAbility) != null && this.getAbility(this.selectedPrimaryAbility).item.rightClick)
        {
            for (int i = 0; i < this.abilities.size(); i++)
            {
                if (!this.abilities.get(i).item.rightClick)
                    this.selectedPrimaryAbility = i;
            }
        }

        if (this.getAbility(this.selectedSecondaryAbility) != null && !this.getAbility(this.selectedSecondaryAbility).item.rightClick)
        {
            for (int i = 0; i < this.abilities.size(); i++)
            {
                if (this.abilities.get(i).item.rightClick)
                    this.selectedSecondaryAbility = i;
            }
        }
    }

    @Override
    public String toString()
    {
        return Serializer.toTanksON(this);
    }

    @Override
    public TankPlayable clonePropertiesTo(TankPlayable m)
    {
        try
        {
            for (Field f : m.getClass().getFields())
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.miscType() != Property.MiscType.color)
                {
                    try
                    {
                        Object v = f.get(this);
                        if (v instanceof ICopyable)
                            f.set(m, ((ICopyable<?>) v).getCopy());
                        else
                            f.set(m, v);
                    }
                    catch (Exception ignored) { }
                }
            }

            m.abilities = new ArrayList<>();
            for (Item.ItemStack<?> s: this.abilities)
            {
                m.abilities.add(s.getCopy());
            }

            m.emblemR = m.secondaryColorR;
            m.emblemG = m.secondaryColorG;
            m.emblemB = m.secondaryColorB;
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        m.updateAbilities();

        return m;
    }

    public Item.ItemStack<?> getAbility(int index)
    {
        if (index < this.abilities.size() && index >= 0)
            return this.abilities.get(index);
        else
            return null;
    }

    public Item.ItemStack<?> getPrimaryAbility()
    {
        if (selectedPrimaryAbility < this.abilities.size() && selectedPrimaryAbility >= 0 && !this.abilities.get(this.selectedPrimaryAbility).item.rightClick)
            return this.abilities.get(this.selectedPrimaryAbility);
        else
            return null;
    }

    public Item.ItemStack<?> getSecondaryAbility()
    {
        if (selectedSecondaryAbility < this.abilities.size() && selectedSecondaryAbility >= 0 && this.abilities.get(this.selectedSecondaryAbility).item.rightClick)
            return this.abilities.get(this.selectedSecondaryAbility);
        else
            return null;
    }
}

package tanks.tank;

import basewindow.Color;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
import tanks.bullet.DefaultItems;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemMine;
import tanks.tankson.ICopyable;
import tanks.tankson.Property;
import tanks.tankson.Serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static tanks.tank.TankPropertyCategory.*;
import static tanks.tank.TankPropertyCategory.appearanceEmblem;

public abstract class TankPlayable extends Tank implements ICopyable<TankPlayable>
{
    @TankBuildProperty @Property(id = "abilities", name = "Abilities", category = TankPropertyCategory.abilities)
    public ArrayList<Item.ItemStack<?>> abilities = new ArrayList<>();

    public int selectedPrimaryAbility = 0;
    public int selectedSecondaryAbility = 1;

    public String buildName = "player";

    @Property(category = appearanceBody, id = "override_color1", name = "Override color", miscType = Property.MiscType.colorRGB)
    public boolean overridePrimaryColor = false;
    @Property(category = appearanceTurretBarrel, id = "override_color2", name = "Override color", miscType = Property.MiscType.colorRGB)
    public boolean overrideSecondaryColor = false;
    @Property(category = appearanceTurretBase, id = "override_color3", name = "Override color", miscType = Property.MiscType.colorRGB)
    public boolean overrideTertiaryColor = false;
    @Property(category = appearanceEmblem, id = "override_color_emblem", name = "Override color", miscType = Property.MiscType.colorRGB)
    public boolean overrideEmblemColor = false;

    public Color[] savedColors = new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255), new Color(0, 0, 0, 255)};

    public TankPlayable(double x, double y)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
        this.enableTertiaryColor = true;
    }

    public void addDefaultAbilities()
    {
        this.abilities.add(new ItemBullet.ItemStackBullet(null, DefaultItems.basic_bullet.getCopy(), 0));
        this.abilities.add(new ItemMine.ItemStackMine(null, DefaultItems.basic_mine.getCopy(), 0));

        this.description = "The default player build: decent speed with basic bullets and basic mines";
    }

    public void updateAbilities()
    {
        if (this.selectedPrimaryAbility >= this.abilities.size())
            this.selectedPrimaryAbility = -1;

        if (this.selectedSecondaryAbility >= this.abilities.size())
            this.selectedSecondaryAbility = -1;

        if (this.getAbility(this.selectedPrimaryAbility) != null && this.getAbility(this.selectedPrimaryAbility).item.rightClick)
        {
            boolean found = false;
            for (int i = 0; i < this.abilities.size(); i++)
            {
                if (!this.abilities.get(i).item.rightClick)
                {
                    this.selectedPrimaryAbility = i;
                    found = true;
                }
            }

            if (!found)
                this.selectedPrimaryAbility = -1;
        }

        if (this.getAbility(this.selectedSecondaryAbility) != null && !this.getAbility(this.selectedSecondaryAbility).item.rightClick)
        {
            boolean found = false;
            for (int i = 0; i < this.abilities.size(); i++)
            {
                if (this.abilities.get(i).item.rightClick)
                {
                    this.selectedSecondaryAbility = i;
                    found = true;
                }
            }

            if (!found)
                this.selectedSecondaryAbility = -1;
        }

        for (int i = 0; i < this.abilities.size(); i++)
        {
            if (this.abilities.get(i).item.rightClick && this.selectedSecondaryAbility < 0)
                this.selectedSecondaryAbility = i;

            if (!this.abilities.get(i).item.rightClick && this.selectedPrimaryAbility < 0)
                this.selectedPrimaryAbility = i;
        }
    }

    @Override
    public String toString()
    {
        return Serializer.toTanksON(this);
    }

    /** Fully copies properties unlike "clone" - useful for making a template from another template */
    public TankPlayable copyPropertiesTo(TankPlayable m)
    {
        try
        {
            for (Field f : m.getClass().getFields())
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null)
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
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        m.health = m.baseHealth;

        m.updateAbilities();

        return m;
    }

    @Override
    public TankPlayable clonePropertiesTo(TankPlayable m)
    {
        try
        {
            for (Field f : m.getClass().getFields())
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.miscType() != Property.MiscType.colorRGBA && p.miscType() != Property.MiscType.colorRGB)
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

            m.restoreColors();
            m.enableTertiaryColor = true;

            if (this.overridePrimaryColor)
                m.color.set(this.color);

            if (this.overrideSecondaryColor)
                m.secondaryColor.set(this.secondaryColor);

            if (this.overrideTertiaryColor)
                m.tertiaryColor.set(this.tertiaryColor);

            if (this.overrideEmblemColor)
                m.emblemColor.set(this.emblemColor);

            m.buildName = this.name;
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        m.health = m.baseHealth;

        m.updateAbilities();

        return m;
    }

    public TankAIControlled clonePropertiesTo(TankAIControlled m)
    {
        String name = m.name;
        try
        {
            for (Field f : m.getClass().getFields())
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.miscType() != Property.MiscType.colorRGBA && p.miscType() != Property.MiscType.colorRGB)
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

            if (this.maxSpeed <= 0)
                m.enableMovement = false;

            m.enableTertiaryColor = true;

            if (this.overridePrimaryColor)
                m.color.set(this.color);

            if (this.overrideSecondaryColor)
                m.secondaryColor.set(this.secondaryColor);

            if (this.overrideTertiaryColor)
                m.tertiaryColor.set(this.tertiaryColor);

            if (this.overrideEmblemColor)
                m.emblemColor.set(this.emblemColor);

            m.shootAIType = TankAIControlled.ShootAI.none;
            m.enableMineLaying = false;

            boolean foundBullet = false;
            boolean foundMine = false;

            if (m instanceof TankPlayerBot)
            {
                ((TankPlayerBot) m).abilities = new ArrayList<>();
                for (Item.ItemStack<?> i: this.abilities)
                {
                    ((TankPlayerBot) m).abilities.add(i.getCopy());
                }
            }
            else
            {
                for (Item.ItemStack<?> i : this.abilities)
                {
                    if (i instanceof ItemBullet.ItemStackBullet && !foundBullet)
                    {
                        foundBullet = true;
                        Bullet b = ((ItemBullet.ItemStackBullet) i).item.bullet;

                        if (b instanceof BulletArc || b instanceof BulletAirStrike)
                            m.shootAIType = TankAIControlled.ShootAI.straight;
                        else if (b.homingSharpness > 0)
                            m.shootAIType = TankAIControlled.ShootAI.homing;
                        else if (b.bounces > 1)
                            m.shootAIType = TankAIControlled.ShootAI.reflect;
                        else if (b.bounces > 0)
                            m.shootAIType = TankAIControlled.ShootAI.alternate;
                        else
                            m.shootAIType = TankAIControlled.ShootAI.straight;

                        m.cooldownBase = i.item.cooldownBase;
                        m.cooldownRandom = 0;
                        m.setBullet(b);
                    }
                    else if (i instanceof ItemMine.ItemStackMine && !foundMine)
                    {
                        foundMine = true;
                        Mine n = ((ItemMine.ItemStackMine) i).item.mine;

                        m.enableMineLaying = true;
                        m.setMine(n);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        if (m instanceof TankPlayerBot)
            m.name = name;

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

    public void saveColors()
    {
        this.savedColors[0].set(this.color);
        this.savedColors[1].set(this.secondaryColor);
        this.savedColors[2].set(this.tertiaryColor);
        this.savedColors[3].set(this.emblemColor);
    }

    public void restoreColors()
    {
        this.color.set(savedColors[0]);
        this.secondaryColor.set(savedColors[1]);
        this.tertiaryColor.set(savedColors[2]);
        this.emblemColor.set(savedColors[3]);
    }
}

package tanks.gui.screen;

import basewindow.Color;
import tanks.Drawing;
import tanks.Game;
import tanks.Movable;
import tanks.Panel;
import tanks.bullet.BulletEffect;
import tanks.gui.Button;
import tanks.tank.TankPlayable;
import tanks.tankson.FieldPointer;
import tanks.tankson.Property;
import tanks.tankson.TanksONable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ScreenOverlaySelectPower
{
    int powerCount = 3;

    int powersPerOption = (int) (Math.random() * 2 + 1);
    ArrayList<Modifier> powers = new ArrayList<>();
    ArrayList<Modifier> powers2 = new ArrayList<>();

    public ArrayList<Button> powerButtons = new ArrayList<>();
    public double age = 0;
    public double musicTime = 0;
    public String music;
    public String musicID;
    public HashSet<String> syncedTracks;

    public ScreenOverlaySelectPower(ScreenGame s)
    {
        this.music = Game.screen.music;
        this.musicID = Game.screen.musicID;
        this.syncedTracks = s.tankMusics;

        Game.screen.music = "ready_music_3.ogg";
        Game.screen.musicID = null;
        musicTime = Game.game.window.soundPlayer.getMusicPos();
        Drawing.drawing.playSound("select_power.ogg");

        if (Game.playerTank != null && !Game.playerTank.destroy)
        {
            for (int i = 0; i < powerCount; i++)
            {
                Modifier<?> m = getRandomModifier(Game.playerTank);
                if (m == null)
                    continue;

                powers.add(m);

                Modifier<?> m2 = null;
                if (powersPerOption >= 2)
                {
                    m2 = getRandomModifier(Game.playerTank);
                    if (m2 == null)
                        continue;

                    powers2.add(m2);
                }

                Modifier finalM = m;
                Modifier finalM2 = m2;

                powerButtons.add(new Button(s.centerX - (i - (powerCount - 1) / 2.0) * s.objXSpace, s.centerY + s.objYSpace * 1.5 + (powersPerOption - 1) * s.objYSpace * 2.5,
                        350, 40, "Select this modifier", () ->
                {
                    finalM.apply();
                    if (finalM2 != null)
                        finalM2.apply();

                    s.powerSelection = null;

                    if (music != null)
                    {
                        Drawing.drawing.playMusic(music, Game.musicVolume, true, musicID, 0);
                        for (String s1 : syncedTracks)
                            Drawing.drawing.addSyncedMusic(s1, Game.musicVolume, true, 0);
                        Game.game.window.soundPlayer.setMusicPos((float) this.musicTime);
                    }

                    Game.playerTank.setBufferCooldown(null, 20);
                    for (Movable mm: Game.movables)
                    {
                        if (!(mm instanceof TankPlayable))
                        {
                            for (int n = 0; n < powersPerOption; n++)
                            {
                                Modifier d = getRandomModifier(mm);
                                if (d != null)
                                    d.apply();
                            }
                        }
                    }
                }));
            }
        }
    }

    public void update()
    {
        this.age += Panel.frameFrequency;

        for (Button b: this.powerButtons)
        {
            b.enabled = this.age > 100;
            b.update();
        }
    }

    public void draw()
    {
        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(Game.screen.centerX, Game.screen.centerY, 1200, 600);

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(32);
        Drawing.drawing.drawInterfaceText(Game.screen.centerX, Game.screen.centerY - Game.screen.objYSpace * 4.5, "Select an upgrade...");

        for (int i = 0; i < this.powers.size(); i++)
        {
            double x = powerButtons.get(i).posX;
            double y = powerButtons.get(i).posY;

            for (int p = 0; p < powersPerOption; p++)
            {
                ArrayList<Modifier> powers = this.powers;
                Drawing.drawing.setColor(255, 255, 255);
                Drawing.drawing.setInterfaceFontSize(18);

                if (p == 1)
                {
                    powers = this.powers2;
                    Drawing.drawing.drawInterfaceText(x, y, "-- and --");
                }


                for (int j = 0; j < powers.get(i).prefixes.length; j++)
                {
                    Drawing.drawing.drawInterfaceText(x, y - Game.screen.objYSpace * (2 + (powers.get(i).prefixes.length - j) / 2.5), powers.get(i).prefixes[j]);
                }

                Drawing.drawing.setInterfaceFontSize(32);
                Drawing.drawing.drawInterfaceText(x, y - Game.screen.objYSpace * 2, powers.get(i).getPropString());
                Drawing.drawing.setInterfaceFontSize(24);
                Drawing.drawing.drawInterfaceText(x, y - Game.screen.objYSpace * 1, powers.get(i).getEffectText());

                y -= Game.screen.objYSpace * 4;
            }

            powerButtons.get(i).draw();
        }
    }

    public static class PrefixedFieldPointer<T>
    {
        public String[] prefixes;
        public FieldPointer<T> property;

        public PrefixedFieldPointer(FieldPointer<T> prop, String[] pres)
        {
            this.property = prop;
            this.prefixes = pres;
        }
    }

    public static abstract class Modifier<T>
    {
        public PrefixedFieldPointer<T> prefixedProperty;
        public FieldPointer<T> property;
        public String[] prefixes;

        public Modifier(PrefixedFieldPointer<T> p)
        {
            this.property = p.property;
            this.prefixedProperty = p;
            this.prefixes = p.prefixes;
        }

        public abstract void apply();

        public abstract String getEffectText();
        
        public String getPropString()
        {
            return property.getField().getAnnotation(Property.class).name();
        }
    }

    public static class ModifierDouble extends Modifier<Double>
    {
        boolean add = false;
        double amount;

        public ModifierDouble(PrefixedFieldPointer<Double> p)
        {
            super(p);

            int exp = (int) (Math.pow(Math.random(), 4) * 8) + 1;
            amount = Math.pow(2, Math.random() * exp - exp / 2.0);

            if (p.property.get() == 0 || Math.random() < 0.3)
            {
                double amt = p.property.get();

                add = true;

                int pow = (int) (Math.random() * 4);

                double min = p.property.getField().getAnnotation(Property.class).minValue();
                double max = p.property.getField().getAnnotation(Property.class).maxValue();
                min = Math.max(amt - Math.pow(10, pow), min);
                max = Math.min(amt + Math.pow(10, pow), max);
                amount = Math.random() * (max - min) + min - amt;
            }
        }

        @Override
        public void apply()
        {
            if (add)
                this.property.set(this.property.get() + amount);
            else
                this.property.set(this.property.get() * amount);
        }

        @Override
        public String getEffectText()
        {
            if (add)
            {
                if (amount < 0)
                    return "Decrease by " + String.format("%.2f", -amount);
                else
                    return "Increase by " + String.format("%.2f", amount);
            }
            else
            {
                int p = (int) ((amount - 1) * 100);
                if (p < 0)
                    return "Decrease by " + -p + "%";
                else
                    return "Increase by " + p + "%";
            }
        }
    }

    public static class ModifierInteger extends Modifier<Integer>
    {
        int amount;

        public ModifierInteger(PrefixedFieldPointer<Integer> p)
        {
            super(p);
            int amt = p.property.get();
            double min = p.property.getField().getAnnotation(Property.class).minValue();
            double max = p.property.getField().getAnnotation(Property.class).maxValue();
            int pow = (int) (Math.pow(Math.random(), 4) * 8) + 1;
            min = Math.max(amt - Math.pow(5, pow), min);
            max = Math.min(amt + Math.pow(5, pow), max);
            amount = (int) (Math.random() * (max - min) + min - amt);
        }

        @Override
        public void apply()
        {
            this.property.set((int) (this.property.get() + amount));
        }

        @Override
        public String getEffectText()
        {
            if (amount < 0)
                return "Decrease by " + -amount;
            else
                return "Increase by " + amount;
        }
    }

    public static class ModifierEnum extends Modifier<Enum>
    {
        int option;

        public ModifierEnum(PrefixedFieldPointer<Enum> p)
        {
            super(p);
            this.option = (int) (p.property.get().getClass().getEnumConstants().length * Math.random());
        }

        @Override
        public void apply()
        {
            Enum[] els = ((Enum) property.get()).getClass().getEnumConstants();
            property.set(els[(int) (Math.random() * els.length)]);
        }

        @Override
        public String getEffectText()
        {
            Enum[] els = ((Enum) property.get()).getClass().getEnumConstants();
            return "Set to " + els[option].name();
        }
    }

    public static class ModifierSound extends Modifier<String>
    {
        String option;

        public ModifierSound(PrefixedFieldPointer<String> p)
        {
            super(p);
            ArrayList<String> sounds = Game.game.fileManager.getInternalFileContents("/sounds/all_sounds.txt");
            this.option = sounds.get((int) (Math.random() * sounds.size()));
        }

        @Override
        public void apply()
        {
            property.set(option);
        }

        @Override
        public String getEffectText()
        {
            return "Set to " + Game.formatString(option.replace(".ogg", ""));
        }
    }

    public static class ModifierBoolean extends Modifier<Boolean>
    {
        public ModifierBoolean(PrefixedFieldPointer<Boolean> p)
        {
            super(p);
        }

        @Override
        public void apply()
        {
            property.set(!property.get());
        }

        @Override
        public String getEffectText()
        {
            if (property.get())
                return "Disable";
            else
                return "Enable";
        }
    }

    public static PrefixedFieldPointer<?> getRandomProperty(Movable o)
    {
        try
        {
            if (o.properties == null)
            {
                o.properties = new ArrayList<>();
                populateProperties(o, new String[0], new HashSet<>(), o.properties);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (o.properties.size() == 0)
            return null;

        return o.properties.get((int) (Math.random() * o.properties.size()));
    }

    public static Modifier<?> getRandomModifier(Movable o)
    {
        PrefixedFieldPointer<?> p = getRandomProperty(o);
        if (p == null)
            return null;

        Modifier m = null;
        if (p.property.get() instanceof Double)
            m = new ModifierDouble((PrefixedFieldPointer<Double>) p);
        else if (p.property.get() instanceof Integer)
            m = new ModifierInteger((PrefixedFieldPointer<Integer>) p);
        else if (p.property.get() instanceof Boolean)
            m = new ModifierBoolean((PrefixedFieldPointer<Boolean>) p);
        else if (p.property.get().getClass().isEnum())
            m = new ModifierEnum((PrefixedFieldPointer<Enum>) p);
        else if (p.property.get() instanceof String)
            m = new ModifierSound((PrefixedFieldPointer<String>) p);

        return m;
    }

    public static void populateProperties(Object o, String[] prefixes, HashSet<Object> seen, ArrayList<PrefixedFieldPointer<?>> properties) throws Exception
    {
        if (seen.contains(o))
            return;

        seen.add(o);

        for (Field f: o.getClass().getFields())
        {
            if (f.getAnnotation(Property.class) != null && !f.getType().equals(Color.class) && !((o.getClass().equals(BulletEffect.class) || f.getAnnotation(Property.class).name().toLowerCase().contains("color"))))
            {
                Class<?> t = f.getType();

                if (f.getType().getAnnotation(TanksONable.class) != null && f.get(o) != null)
                {
                    String[] p = new String[prefixes.length + 1];
                    for (int i = 0; i < prefixes.length; i++)
                        p[i] = prefixes[i];

                    p[prefixes.length] = f.getAnnotation(Property.class).name();
                    populateProperties(f.get(o), p, seen, properties);
                }
                else if (t.equals(ArrayList.class))
                {
                    ArrayList<Object> l = (ArrayList<Object>) f.get(o);
                    int n = 0;
                    for (Object o1: l)
                    {
                        n++;
                        String[] p = new String[prefixes.length + 1];
                        for (int i = 0; i < prefixes.length; i++)
                            p[i] = prefixes[i];

                        p[prefixes.length] = f.getAnnotation(Property.class).name() + " #" + n;

                        populateProperties(o1, p, seen, properties);
                    }
                }
//                else if (t.equals(Color.class) && Math.random() < 0.05)
//                {
//                    String[] p = new String[prefixes.length + 1];
//                    for (int i = 0; i < prefixes.length; i++)
//                        p[i] = prefixes[i];
//                    p[prefixes.length] = f.getAnnotation(Property.class).name();
//                    if (f.getAnnotation(Property.class).name().isEmpty())
//                        throw new RuntimeException(f.getAnnotation(Property.class).id());
//                    Color c = (Color) f.get(o);
//                    properties.add(new PrefixedFieldPointer<>(new FieldPointer<>(c, Color.class.getField("red")), p));
//                    properties.add(new PrefixedFieldPointer<>(new FieldPointer<>(c, Color.class.getField("green")), p));
//                    properties.add(new PrefixedFieldPointer<>(new FieldPointer<>(c, Color.class.getField("blue")), p));
//
//                    if (f.getAnnotation(Property.class).miscType() != Property.MiscType.colorRGB)
//                        properties.add(new PrefixedFieldPointer<>(new FieldPointer<>(c, Color.class.getField("alpha")), p));
//                }
                else if (t.equals(boolean.class) || t.equals(int.class) || t.equals(double.class) || t.isEnum() || f.getAnnotation(Property.class).miscType() == Property.MiscType.bulletSound)
                    properties.add(new PrefixedFieldPointer<>(new FieldPointer<>(o, f), prefixes));
            }
        }
    }
}

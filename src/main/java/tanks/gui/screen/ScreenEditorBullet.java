package tanks.gui.screen;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Player;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.bullet.BulletGas;
import tanks.bullet.BulletPropertyCategory;
import tanks.gui.*;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.registry.RegistryBullet;
import tanks.tank.Tank;
import tanks.tank.Turret;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class ScreenEditorBullet extends ScreenEditorTanksONable<Bullet>
{
    public Selector bulletTypes = new Selector(this.centerX + 300, 60, this.objWidth, this.objHeight, "Bullet type", Game.registryBullet.getEntryNames(), () -> {});

    public Tab col1;
    public Tab col2;
    public Tab col3;

    public Button col1Button;
    public Button col2Button;
    public Button col3Button;

    public Button load = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Load from template", () ->
    {
        Game.screen = new ScreenAddSavedItem(this, (b) ->
        {
            this.setTarget(((ItemBullet) b.item).bullet);
            Game.screen = this;
        }, "Bullet", ItemBullet.class);
    }
    );

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", () ->
    {
        Game.screen = new ScreenSaveUnnamedItem(this);
    }
    );

    public ScreenEditorBullet(Pointer<Bullet> bullet, Screen screen)
    {
        super(bullet, screen);
        bulletTypes.images = Game.registryBullet.getImageNames();
        for (int i = 0; i < Game.registryBullet.bulletEntries.size(); i++)
        {
            RegistryBullet.BulletEntry e = Game.registryBullet.bulletEntries.get(i);
            if (e.name.equals(this.target.get().typeName))
                bulletTypes.selectedOption = i;
        }

        bulletTypes.function = () ->
        {
            try
            {
                Bullet b = Game.registryBullet.bulletEntries.get(bulletTypes.selectedOption).bullet.newInstance();
                target.get().clonePropertiesTo(b);
                setTarget(b);
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }
        };

//        bullet.get().initTrails();
        this.title = "Edit %s";
        this.objName = "bullet";
    }

    public class TabFiring extends ScreenEditorTanksONable<Bullet>.Tab
    {
        public TabFiring(ScreenEditorTanksONable<Bullet> screen, String name, String category)
        {
            super(screen, name, category);
        }

        @Override
        public void addFields()
        {
            this.uiElements.clear();
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.category().equals(this.category))
                {
                    ITrigger el = screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);

                    if (p.id().equals("sound_pitch") || p.id().equals("sound_pitch_variation") || p.id().equals("sound_volume"))
                    {
                        ((TextBox) el).silent = true;
                        Runnable func = ((TextBox) el).function;
                        Bullet t = target.get();
                        ((TextBox) el).function = () -> { func.run(); Drawing.drawing.playSound(t.shotSound, (float) (t.pitch + (Math.random() - 0.5) * t.pitchVariation), (float) t.soundVolume); };
                    }

                    this.uiElements.add(el);
                }
            }
        }
    }

    public class TabTravel extends ScreenEditorTanksONable<Bullet>.Tab
    {
        public TabTravel(ScreenEditorTanksONable<Bullet> screen, String name, String category)
        {
            super(screen, name, category);
        }

        @Override
        public void addFields()
        {
            this.uiElements.clear();
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.category().equals(this.category))
                {
                    ITrigger el = screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);

                    if ((p.id().equals("range") || p.id().equals("lifespan")) && target.get() instanceof BulletArc)
                        continue;

                    this.uiElements.add(el);
                }
            }
        }
    }

    public class TabAppearance extends ScreenEditorTanksONable<Bullet>.Tab
    {
        public ArrayList<Effect> effects = new ArrayList<>();
        public ArrayList<Effect> removeEffects = new ArrayList<>();
        Random rand = new Random();
        double margin = screen.centerX - screen.objWidth / 2 + screen.objHeight / 2 - screen.objXSpace / 2;

        public TabAppearance(ScreenEditorBullet screen, String name, String category)
        {
            super(screen, name, category);
        }

        @Override
        public void addFields()
        {
            boolean gas = target.get() instanceof BulletGas;

            this.uiElements.clear();
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.category().equals(this.category))
                {
                    ITrigger el = screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);
                    if (gas)
                    {
                        if (p.id().equals("size"))
                            ((TextBox) el).setText("Start size");
                    }

                    this.uiElements.add(el);
                }
            }
        }

        @Override
        public void sortUIElements()
        {
            boolean gas = target.get() instanceof BulletGas;
            int in = 0;
            if (gas)
            {
                col1.name = "Start color";
                col2.name = "End color";
            }
            else
            {
                col1.name = "Base color";
                col2.name = "Outline color";
            }

            col3Button = null;
            for (ScreenEditorTanksONable<Bullet>.Tab t: this.subMenus)
            {
                if (t == col3 && !gas)
                    continue;

                Button b = new Button(0, 0, 350, 40, t.name, () -> screen.setTab(t));
                if (t == col1)
                    col1Button = b;
                else if (t == col2)
                    col2Button = b;
                else if (t == col3)
                    col3Button = b;

                this.uiElements.add(in, b);
                in++;
            }

            while (in % 4 != 0)
            {
                this.uiElements.add(in, new EmptySpace());
                in++;
            }

            for (int i = 0; i < this.uiElements.size(); i++)
            {
                int page = i / (rows * 3);
                int offset = 0;

                if (page * rows * 3 + rows < this.uiElements.size())
                    offset = -190;

                if (page * rows * 3 + rows * 2 < this.uiElements.size())
                    offset = -380;

                double posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + (i % rows) * 90;

                if (i / rows % 3 == 0)
                    this.uiElements.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset, posY);
                else if (i / rows % 3 == 1)
                    this.uiElements.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset + 380, posY);
                else
                    this.uiElements.get(i).setPosition(Drawing.drawing.interfaceSizeX / 2 + offset + 380 * 2, posY);
            }

            for (ScreenEditorTanksONable<Bullet>.Tab t: this.subMenus)
            {
                t.sortUIElements();
            }
        }

        @Override
        public void drawUIElements()
        {
            Bullet bullet = screen.target.get();

            if (!Game.game.window.drawingShadow)
            {
                for (Effect e : this.effects)
                {
                    e.update();

                    if (e.age > e.maxAge)
                        removeEffects.add(e);
                }

                effects.removeAll(removeEffects);
                removeEffects.clear();

                for (Effect f : this.effects)
                {
                    f.draw();
                }

                for (Effect f : this.effects)
                {
                    f.drawGlow();
                }
            }

            bullet.drawForInterface(centerX, Drawing.drawing.interfaceSizeX * 0.6, centerY + objYSpace * 4, Math.min(100, bullet.size), effects, rand, Player.default_primary, Player.default_secondary);

            super.drawUIElements();

            Drawing.drawing.setInterfaceFontSize(24);
            if (bullet.overrideOutlineColor)
                Drawing.drawing.setColor(bullet.outlineColor);
            else
                Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));

            Drawing.drawing.fillInterfaceOval(margin, col2Button.posY, 30, 30);

            if (!(bullet instanceof BulletGas))
            {
                Drawing.drawing.setColor(200, 200, 200);
                Drawing.drawing.fillInterfaceOval(margin, col2Button.posY, 24, 24);
                Drawing.drawing.fillInterfaceOval(margin, col1Button.posY, 30, 30);
            }

            if (!bullet.overrideOutlineColor)
            {
                Drawing.drawing.setColor(150, 150, 150);
                Drawing.drawing.drawInterfaceText(margin, col2Button.posY, "-");
            }

            if (bullet.overrideBaseColor)
                Drawing.drawing.setColor(bullet.baseColor);
            else
                Drawing.drawing.setColor(0, 150, 255);

            Drawing.drawing.fillInterfaceOval(margin, col1Button.posY, 24, 24);

            if (!bullet.overrideBaseColor)
            {
                Drawing.drawing.setColor(200, 200, 200);
                Drawing.drawing.drawInterfaceText(margin, col1Button.posY, "-");
            }

        }
    }

    public class TabColorPicker extends ScreenEditorTanksONable<Bullet>.Tab
    {
        public ArrayList<Effect> effects = new ArrayList<>();
        public ArrayList<Effect> removeEffects = new ArrayList<>();
        Random rand = new Random();

        public int colorIndex;
        public SelectorColor colorPicker;

        String enableColorText = "Custom color: ";
        Button enableColor = new Button(0, 0, this.screen.objWidth, this.screen.objHeight, "", new Runnable()
        {
            @Override
            public void run()
            {
                boolean enable = false;
                Bullet b = screen.target.get();

                if (colorIndex == 1)
                {
                    b.overrideBaseColor = !b.overrideBaseColor;
                    enable = b.overrideBaseColor;
                }
                else if (colorIndex == 2)
                {
                    b.overrideOutlineColor = !b.overrideOutlineColor;
                    enable = b.overrideOutlineColor;
                }

                setColorText(enable);
            }
        },
                "If off, the color will be picked based---on the tank which shot the bullet");

        public void setColorText(boolean enable)
        {
            if (enable)
                enableColor.setText(enableColorText, ScreenOptions.onText);
            else
                enableColor.setText(enableColorText, ScreenOptions.offText);
        }

        public TabColorPicker(ScreenEditorBullet screen, ScreenEditorTanksONable<Bullet>.Tab parent, String name, String category, int colorIndex)
        {
            super(screen, parent, name, category);
            this.colorIndex = colorIndex;
        }

        public TabColorPicker(ScreenEditorBullet screen, String name, String category, int colorIndex)
        {
            this(screen, null, name, category, colorIndex);
        }

        @Override
        public void set()
        {
            super.set();

            Bullet bullet = screen.target.get();
            this.setColorText(bullet.overrideBaseColor);

            if (colorIndex == 2)
                this.setColorText(bullet.overrideOutlineColor);
        }

        @Override
        public void sortUIElements()
        {
            if (this.colorIndex == 1 || this.colorIndex == 2)
                this.uiElements.add(enableColor);

            if (this.colorPicker != null)
                this.uiElements.add(this.colorPicker);

            super.sortUIElements();

            if (this.colorPicker != null)
                this.uiElements.remove(this.colorPicker);
        }

        @Override
        public void updateUIElements()
        {
            Bullet bullet = screen.target.get();
            super.updateUIElements();

            if ((this.colorIndex == 1 && bullet.overrideBaseColor) || (this.colorIndex == 2 && bullet.overrideOutlineColor) || this.colorIndex == 3)
                this.colorPicker.update();
        }

        @Override
        public void draw()
        {
            Bullet bullet = screen.target.get();
            if (!Game.game.window.drawingShadow)
            {
                for (Effect e : this.effects)
                {
                    e.update();

                    if (e.age > e.maxAge)
                        removeEffects.add(e);
                }

                effects.removeAll(removeEffects);
                removeEffects.clear();

                for (Effect f : this.effects)
                {
                    f.draw();
                }

                for (Effect f : this.effects)
                {
                    f.drawGlow();
                }
            }

            bullet.drawForInterface(centerX, Drawing.drawing.interfaceSizeX * 0.6, centerY + objYSpace * 4, Math.min(100, bullet.size), effects, rand, Player.default_primary, Player.default_secondary);
            super.draw();

        }

        @Override
        public void drawUIElements()
        {
            Bullet bullet = screen.target.get();
            if ((this.colorIndex == 1 && bullet.overrideBaseColor) || (this.colorIndex == 2 && bullet.overrideOutlineColor) || this.colorIndex == 3)
                this.colorPicker.draw();

            super.drawUIElements();
        }

        @Override
        public void addFields()
        {
            this.uiElements.clear();
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);

                if (p != null && p.category().equals(this.category))
                {
                    ITrigger t = screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);

                    if (p.miscType() != Property.MiscType.colorRGB)
                    {
                        this.uiElements.add(t);
                        for (int i = 1; i < t.getSize(); i++)
                        {
                            this.uiElements.add(new EmptySpace());
                        }
                    }
                    else if (t instanceof SelectorColor)
                        this.colorPicker = (SelectorColor) t;
                }
            }
        }
    }

    @Override
    public void setupTabs()
    {
        Tab appearance = new TabAppearance(this, "Appearance", BulletPropertyCategory.appearance);

        col1 = new TabColorPicker(this, appearance, "", BulletPropertyCategory.appearanceBaseColor, 1);
        col2 = new TabColorPicker(this, appearance, "", BulletPropertyCategory.appearanceOutlineColor, 2);
        col3 = new TabColorPicker(this, appearance, "Color noise", BulletPropertyCategory.appearanceNoiseColor, 3);

        new TabFiring(this, "Firing", BulletPropertyCategory.firing);
        new TabTravel(this, "Movement", BulletPropertyCategory.travel);
        new Tab(this, "Impact", BulletPropertyCategory.impact);

        this.iconPrefix = "bulleteditor";
        this.setTab(appearance);
    }

    @Override
    public void update()
    {
        super.update();

        if (this.target.get() != null)
            this.bulletTypes.update();

        if (Game.screen == this)
        {
            this.load.update();
            this.save.update();
        }
    }

    @Override
    public void draw()
    {
        super.draw();

        if (this.target.get() != null)
            this.bulletTypes.draw();

        if (Game.screen == this)
        {
            this.load.draw();
            this.save.draw();
        }
    }
}

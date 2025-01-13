package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.bullet.Bullet;
import tanks.bullet.BulletArc;
import tanks.bullet.BulletGas;
import tanks.bullet.BulletPropertyCategory;
import tanks.gui.*;
import tanks.item.ItemBullet;
import tanks.registry.RegistryBullet;
import tanks.tank.Turret;
import tanks.tankson.FieldPointer;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.lang.reflect.Field;

public class ScreenEditorBullet extends ScreenEditorTanksONable<Bullet>
{
    public Selector bulletTypes = new Selector(this.centerX + 300, 60, this.objWidth, this.objHeight, "Bullet type", Game.registryBullet.getEntryNames(), () -> {});

    public Tab col1;
    public Tab col2;
    public Tab col3;
    public Tab glow;

    public Button col1Button;
    public Button col2Button;
    public Button col3Button;
    public Button glowButton;

    public Button load = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Load from template", () ->
            Game.screen = new ScreenAddSavedItem(this, (b) ->
            {
                this.setTarget(((ItemBullet) b.item).bullet);
                Game.screen = this;
            }, "Bullet", ItemBullet.class)
    );

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", () ->
            Game.screen = new ScreenSaveUnnamedItem(this)
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
                        if (p.id().equals("effect"))
                            continue;
                        else if (p.id().equals("size"))
                            ((TextBox) el).setText("Start size");
                    }
                    else
                    {
                        if (p.id().equals("effect"))
                            ((Selector) el).images = new String[]{"bullet_large.png", "bullet_normal.png", "bullet_air.png", "bullet_fire.png", "bullet_dark_fire.png", "bullet_fire_trail.png", "bullet_freeze.png", "bullet_boost.png"};
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
                if (t == glow)
                    glowButton = b;
                else if (t == col1)
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
            super.drawUIElements();
            Bullet bullet = screen.target.get();

            double s = Game.tile_size * 0.8;
            Drawing.drawing.setColor(80, 80, 80);
            Drawing.drawing.fillInterfaceOval(margin, glowButton.posY, s * 1.5, s * 1.5);
            if (bullet.overrideOutlineColor)
                Drawing.drawing.setColor(bullet.outlineColorR * bullet.glowIntensity, bullet.outlineColorG * bullet.glowIntensity, bullet.outlineColorB * bullet.glowIntensity, 255, 1);
            else
                Drawing.drawing.setColor(Turret.calculateSecondaryColor(0) * bullet.glowIntensity, Turret.calculateSecondaryColor(150) * bullet.glowIntensity, Turret.calculateSecondaryColor(255) * bullet.glowIntensity, 255, 1);

            Drawing.drawing.fillInterfaceGlow(margin, glowButton.posY, s * 1.5 * bullet.glowSize / 4, s * 1.5 * bullet.glowSize / 4);

            Drawing.drawing.setInterfaceFontSize(24);
            if (bullet.overrideOutlineColor)
                Drawing.drawing.setColor(bullet.outlineColorR, bullet.outlineColorG, bullet.outlineColorB);
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
                Drawing.drawing.setColor(bullet.baseColorR, bullet.baseColorG, bullet.baseColorB);
            else
                Drawing.drawing.setColor(0, 150, 255);

            Drawing.drawing.fillInterfaceOval(margin, col1Button.posY, 24, 24);

            if (!bullet.overrideBaseColor)
            {
                Drawing.drawing.setColor(200, 200, 200);
                Drawing.drawing.drawInterfaceText(margin, col1Button.posY, "-");
            }

            if (bullet.overrideOutlineColor)
                Drawing.drawing.setColor(bullet.outlineColorR, bullet.outlineColorG, bullet.outlineColorB);
            else
                Drawing.drawing.setColor(Turret.calculateSecondaryColor(0), Turret.calculateSecondaryColor(150), Turret.calculateSecondaryColor(255));
        }
    }

    public class TabColorPicker extends ScreenEditorTanksONable<Bullet>.Tab
    {
        public int colorIndex;

        public TextBoxSlider colorRed;
        public TextBoxSlider colorGreen;
        public TextBoxSlider colorBlue;

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
                    colorRed.inputText = (int) colorRed.value + "";
                    colorGreen.inputText = (int) colorGreen.value + "";
                    colorBlue.inputText = (int) colorBlue.value + "";
                }
                else if (colorIndex == 2)
                {
                    b.overrideOutlineColor = !b.overrideOutlineColor;
                    enable = b.overrideOutlineColor;
                    colorRed.inputText = (int) colorRed.value + "";
                    colorGreen.inputText = (int) colorGreen.value + "";
                    colorBlue.inputText = (int) colorBlue.value + "";
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

            colorRed = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight, "Red", () ->
            {
                Bullet b = screen.target.get();

                if (colorRed.inputText.length() <= 0)
                    colorRed.inputText = colorRed.previousInputText;

                int red = Integer.parseInt(colorRed.inputText);

                if (colorIndex == 1)
                    b.baseColorR = red;
                else if (colorIndex == 2)
                    b.outlineColorR = red;
                else if (colorIndex == 3)
                    ((BulletGas) b).noiseR = red;
            }
                    , 0, 0, 255, 1);

            colorRed.allowLetters = false;
            colorRed.allowSpaces = false;
            colorRed.maxChars = 3;
            colorRed.maxValue = 255;
            colorRed.checkMaxValue = true;
            colorRed.integer = true;

            colorGreen = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight, "Green", () ->
            {
                Bullet b = screen.target.get();

                if (colorGreen.inputText.length() <= 0)
                    colorGreen.inputText = colorGreen.previousInputText;

                int green = Integer.parseInt(colorGreen.inputText);

                if (colorIndex == 1)
                    b.baseColorG = green;
                else if (colorIndex == 2)
                    b.outlineColorG = green;
                else if (colorIndex == 3)
                    ((BulletGas) b).noiseG = green;
            }
                    , 0, 0, 255, 1);

            colorGreen.allowLetters = false;
            colorGreen.allowSpaces = false;
            colorGreen.maxChars = 3;
            colorGreen.maxValue = 255;
            colorGreen.checkMaxValue = true;
            colorGreen.integer = true;

            colorBlue = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight, "Blue", () ->
            {
                Bullet b = screen.target.get();

                if (colorBlue.inputText.length() <= 0)
                    colorBlue.inputText = colorBlue.previousInputText;

                int blue = Integer.parseInt(colorBlue.inputText);

                if (colorIndex == 1)
                    b.baseColorB = blue;
                else if (colorIndex == 2)
                    b.outlineColorB = blue;
                else if (colorIndex == 3)
                    ((BulletGas) b).noiseB = blue;
            }
                    , 0, 0, 255, 1);

            colorBlue.allowLetters = false;
            colorBlue.allowSpaces = false;
            colorBlue.maxChars = 3;
            colorBlue.maxValue = 255;
            colorBlue.checkMaxValue = true;
            colorBlue.integer = true;
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
            int r = (int) bullet.baseColorR;
            int g = (int) bullet.baseColorG;
            int b = (int) bullet.baseColorB;
            this.setColorText(bullet.overrideBaseColor);

            if (colorIndex == 2)
            {
                r = (int) bullet.outlineColorR;
                g = (int) bullet.outlineColorG;
                b = (int) bullet.outlineColorB;
                this.setColorText(bullet.overrideOutlineColor);
            }
            else if (colorIndex == 3)
            {
                r = (int) ((BulletGas) bullet).noiseR;
                g = (int) ((BulletGas) bullet).noiseG;
                b = (int) ((BulletGas) bullet).noiseB;
            }

            this.colorRed.value = r;
            this.colorGreen.value = g;
            this.colorBlue.value = b;

            this.colorRed.inputText = r + "";
            this.colorGreen.inputText = g + "";
            this.colorBlue.inputText = b + "";
        }

        @Override
        public void sortUIElements()
        {
            if (this.colorIndex == 1 || this.colorIndex == 2)
                this.uiElements.add(enableColor);

            this.uiElements.add(colorRed);
            this.uiElements.add(colorGreen);
            this.uiElements.add(colorBlue);

            super.sortUIElements();

            this.uiElements.remove(colorRed);
            this.uiElements.remove(colorGreen);
            this.uiElements.remove(colorBlue);
        }

        @Override
        public void updateUIElements()
        {
            Bullet bullet = screen.target.get();
            super.updateUIElements();

            if ((this.colorIndex == 1 && bullet.overrideBaseColor) || (this.colorIndex == 2 && bullet.overrideOutlineColor) || this.colorIndex == 3)
            {
                this.colorRed.update();
                this.colorGreen.update();
                this.colorBlue.update();
            }
        }

        @Override
        public void drawUIElements()
        {
            Bullet bullet = screen.target.get();
            this.updateColors();

            if ((this.colorIndex == 1 && bullet.overrideBaseColor) || (this.colorIndex == 2 && bullet.overrideOutlineColor) || this.colorIndex == 3)
            {
                this.colorRed.draw();
                this.colorGreen.draw();
                this.colorBlue.draw();
            }

            super.drawUIElements();
        }

        public void updateColors()
        {
            colorRed.r1 = 0;
            colorRed.r2 = 255;
            colorRed.g1 = colorGreen.value;
            colorRed.g2 = colorGreen.value;
            colorRed.b1 = colorBlue.value;
            colorRed.b2 = colorBlue.value;

            colorGreen.r1 = colorRed.value;
            colorGreen.r2 = colorRed.value;
            colorGreen.g1 = 0;
            colorGreen.g2 = 255;
            colorGreen.b1 = colorBlue.value;
            colorGreen.b2 = colorBlue.value;

            colorBlue.r1 = colorRed.value;
            colorBlue.r2 = colorRed.value;
            colorBlue.g1 = colorGreen.value;
            colorBlue.g2 = colorGreen.value;
            colorBlue.b1 = 0;
            colorBlue.b2 = 255;

            if (!colorRed.selected)
                colorRed.function.run();

            if (!colorGreen.selected)
                colorGreen.function.run();

            if (!colorBlue.selected)
                colorBlue.function.run();
        }
    }

    public class TabGlow extends Tab
    {
        public TabGlow(ScreenEditorBullet screen, ScreenEditorTanksONable<Bullet>.Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabGlow(ScreenEditorBullet screen, String name, String category)
        {
            super(screen, name, category);
        }

        public void set()
        {
            super.set();
        }
    }

    @Override
    public void setupTabs()
    {
        Tab appearance = new TabAppearance(this, "Appearance", BulletPropertyCategory.appearance);

        col1 = new TabColorPicker(this, appearance, "", "color", 1);
        col2 = new TabColorPicker(this, appearance, "", "color", 2);
        col3 = new TabColorPicker(this, appearance, "Color noise", "color", 3);

        new TabFiring(this, "Firing", BulletPropertyCategory.firing);
        new TabTravel(this, "Movement", BulletPropertyCategory.travel);
        new Tab(this, "Impact", BulletPropertyCategory.impact);
        glow = new TabGlow(this, appearance, "Glow", BulletPropertyCategory.appearanceGlow);

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

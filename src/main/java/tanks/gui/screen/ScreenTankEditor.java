package tanks.gui.screen;

import basewindow.BaseFile;
import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.*;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.registry.RegistryModelTank;
import tanks.tank.*;
import tanks.translation.Translation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ScreenTankEditor extends Screen implements IItemScreen
{
    public Tab currentTab;
    public ArrayList<Tab> topLevelMenus = new ArrayList<>();
    public ArrayList<Button> topLevelButtons = new ArrayList<>();

    public TankAIControlled tank;
    public Field[] fields;

    public Item lastItem;
    public Field lastItemField;
    public ScreenEditItem lastItemScreen;
    public SelectorDrawable lastItemButton;
    public String message = null;

    public boolean drawBehindScreen;

    public ITankScreen tankScreen;

    public HashSet<String> prevTankMusics = new HashSet<>();
    public HashSet<String> tankMusics = new HashSet<>();

    public Button delete = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Delete tank", () ->
    {
        this.resetLayout();
        Game.screen = (Screen) this.tankScreen;
        this.tankScreen.removeTank(this.tank);
        this.tankScreen.refreshTanks(this.tank);
        for (String m : this.tankMusics)
        {
            Drawing.drawing.removeSyncedMusic(m, 500);
        }
    }
    );

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", new Runnable()
    {
        @Override
        public void run()
        {
            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.tankDir + "/" + tank.name.replace(" ", "_") + ".tanks");

            if (!f.exists())
            {
                try
                {
                    f.create();
                    f.startWriting();
                    f.println(tank.toString());
                    f.stopWriting();

                    message = "Tank added to templates!";
                }
                catch (IOException e)
                {
                    Game.exitToCrash(e);
                }
            }
            else
                message = "A tank template with this name already exists!";
        }
    }
    );

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Done", () ->
    {
        this.resetLayout();
        Game.screen = (Screen) this.tankScreen;
        for (String m : this.tankMusics)
        {
            Drawing.drawing.removeSyncedMusic(m, 500);
        }

        this.tankScreen.refreshTanks(this.tank);
    }
    );

    @Override
    public void setupLayoutParameters()
    {
        this.interfaceScaleZoomOverride = 1;
        centerX = Drawing.drawing.baseInterfaceSizeX / 2;
        centerY = Drawing.drawing.baseInterfaceSizeY / 2;
        Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX;
        Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY;
    }

    public void resetLayout()
    {
        Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;
        Drawing.drawing.interfaceSizeX = Drawing.drawing.interfaceSizeX / Drawing.drawing.interfaceScaleZoom;
        Drawing.drawing.interfaceSizeY = Drawing.drawing.interfaceSizeY / Drawing.drawing.interfaceScaleZoom;
    }

    public ScreenTankEditor(TankAIControlled t, ITankScreen screen)
    {
        super(350, 40, 380, 60);

        this.allowClose = false;

        this.tankScreen = screen;
        this.tank = t;

        this.music = ((Screen)screen).music;
        this.musicID = ((Screen)screen).musicID;

        t.bullet.className = ItemBullet.classMap2.get(t.bullet.bulletClass);

        this.fields = TankAIControlled.class.getFields();

        if (Game.framework == Game.Framework.libgdx)
        {
            Field[] fields2 = new Field[this.fields.length];
            for (int i = 0; i < fields2.length; i++)
            {
                fields2[i] = fields[fields.length - 1 - i];
            }
            this.fields = fields2;
        }

        Arrays.sort(this.fields, (o1, o2) ->
        {
            Class<?> c1 = o1.getDeclaringClass();
            Class<?> c2 = o2.getDeclaringClass();

            if (c1.equals(c2))
                return 0;
            else if (c1.isAssignableFrom(c2))
                return -1;
            else
                return 1;
        });

        Tab general = new TabGeneral(this, "General", TankProperty.Category.general);
        Tab appearance = new TabAppearance(this, "Appearance", TankProperty.Category.appearanceGeneral);
        Tab movement = new Tab(this, "Movement", TankProperty.Category.movementGeneral);
        Tab firing = new Tab(this, "Firing", TankProperty.Category.firingGeneral);
        Tab mines = new Tab(this, "Mines", TankProperty.Category.mines);
        Tab spawning = new Tab(this, "Spawning", TankProperty.Category.spawning);
        Tab transformation = new Tab(this, "Transformation", TankProperty.Category.transformationGeneral);
        Tab lastStand = new Tab(this, "Last stand", TankProperty.Category.lastStand);

        new TabPartPicker(this, appearance, "Emblem", TankProperty.Category.appearanceEmblem, 4);
        new TabPartPicker(this, appearance, "Turret base", TankProperty.Category.appearanceTurretBase, 3);
        new TabPartPicker(this, appearance, "Turret barrel", TankProperty.Category.appearanceTurretBarrel, 2);
        new TabPartPicker(this, appearance, "Tank body", TankProperty.Category.appearanceBody, 1);
        new TabPartPicker(this, appearance, "Tank treads", TankProperty.Category.appearanceTreads, 2);
        new TabGlow(this, appearance, "Glow", TankProperty.Category.appearanceGlow);
        new Tab(this, appearance, "Tracks", TankProperty.Category.appearanceTracks);


        Tab idle = new Tab(this, movement, "Idle movement", TankProperty.Category.movementIdle);
        Tab avoid = new Tab(this, movement, "Threat avoidance", TankProperty.Category.movementAvoid);
        Tab pathfinding = new Tab(this, movement, "Pathfinding", TankProperty.Category.movementPathfinding);
        Tab onSight = new Tab(this, movement, "Movement on sight", TankProperty.Category.movementOnSight);

        Tab fireBehavior = new Tab(this, firing, "Firing behavior", TankProperty.Category.firingBehavior);
        Tab firePattern = new Tab(this, firing, "Firing pattern", TankProperty.Category.firingPattern);

        new Tab(this, transformation, "On line of sight", TankProperty.Category.transformationOnSight);
        new Tab(this, transformation, "On low hitpoints", TankProperty.Category.transformationOnHealth);
        new Tab(this, transformation, "Mimic", TankProperty.Category.transformationMimic);

        this.currentTab = general;

        this.sortTopLevelTabs();
    }

    @Override
    public void addItem(Item i)
    {
        try
        {
            i.exportProperties();
            this.lastItem = i;
            this.lastItemField.set(this.tank, i);
            final Field itemField = this.lastItemField;
            final SelectorDrawable ib = this.lastItemButton;
            this.lastItemButton.function = () ->
            {
                this.lastItem = i;
                this.lastItemField = itemField;
                ScreenEditItem editItem = new ScreenEditItem(i, this, true, true);
                editItem.delete.setText("Load from template");
                Game.screen = editItem;
                this.lastItemScreen = editItem;
                this.lastItemButton = ib;
            };
            this.lastItemButton.function.run();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void removeItem(Item i)
    {
        Game.screen = new ScreenAddSavedItem(this, this.lastItemButton, (Class<? extends Item>) this.lastItemField.getType());
    }

    @Override
    public void refreshItems()
    {
        this.lastItemButton.image = this.lastItem.icon;
        this.lastItemButton.imageXOffset = - this.lastItemButton.sizeX / 2 + this.lastItemButton.sizeY / 2 + 10;
        this.lastItemButton.imageSizeX = this.lastItemButton.sizeY;
        this.lastItemButton.imageSizeY = this.lastItemButton.sizeY;
        this.lastItemButton.optionText = this.lastItem.name;
    }

    public static class Tab
    {
        public ScreenTankEditor screen;
        public ArrayList<Tab> subMenus = new ArrayList<>();
        public ArrayList<ITrigger> uiElements = new ArrayList<>();
        public String name;
        public TankProperty.Category category;
        public Tab parent;

        public int rows = 4;
        public int yoffset = -120;
        public int page = 0;

        public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", () -> page++);

        public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", () -> page--);

        public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", () -> {screen.setTab(screen.currentTab.parent);});

        public Tab(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            this(screen, null, name, category);
        }

        public Tab(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            if (parent == null)
                screen.topLevelMenus.add(this);
            else
                parent.addSubMenu(this);

            this.parent = parent;
            this.screen = screen;
            this.name = name;
            this.category = category;

            this.next.image = "icons/forward.png";
            this.next.imageSizeX = 25;
            this.next.imageSizeY = 25;
            this.next.imageXOffset = 145;

            this.previous.image = "icons/back.png";
            this.previous.imageSizeX = 25;
            this.previous.imageSizeY = 25;
            this.previous.imageXOffset = -145;

            this.addFields();
        }

        public void set()
        {

        }

        public void addFields()
        {
            for (Field f: this.screen.fields)
            {
                TankProperty p = f.getAnnotation(TankProperty.class);
                if (p != null && p.category() == this.category && p.miscType() != TankProperty.MiscType.color)
                {
                    if (p.miscType() == TankProperty.MiscType.description)
                    {
                        TextBox t = (TextBox) screen.getUIElementForField(f, p, screen.tank);
                        t.posX = this.screen.centerX;
                        t.posY = this.screen.centerY + 270;
                        t.enableCaps = true;
                        t.allowSpaces = true;
                        t.enableSpaces = true;
                        t.enablePunctuation = true;
                        t.maxChars = 100;
                        t.sizeX *= 3;
                        ((TabGeneral) this).description = t;
                    }
                    else if (!(p.miscType() == TankProperty.MiscType.music && Game.framework == Game.Framework.libgdx))
                        this.uiElements.add(screen.getUIElementForField(f, p, screen.tank));
                }
            }
        }

        public void addSubMenu(Tab t)
        {
            this.subMenus.add(t);
        }

        public void sortUIElements()
        {
            int in = 0;
            for (Tab t: this.subMenus)
            {
                this.uiElements.add(in, new Button(0, 0, 350, 40, t.name, () -> screen.setTab(t)));
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

            for (Tab t: this.subMenus)
            {
                t.sortUIElements();
            }
        }

        public Tab getRoot()
        {
            if (this.parent == null)
                return this;

            return this.parent.getRoot();
        }

        public void updateUIElements()
        {
            for (int i = page * rows * 3; i < Math.min(page * rows * 3 + rows * 3, this.uiElements.size()); i++)
            {
                this.uiElements.get(i).update();
            }
        }

        public void update()
        {
            this.updateUIElements();

            if (parent != null)
                back.update();

            if (rows * 3 < uiElements.size())
            {
                previous.update();
                next.update();
            }
        }

        public void drawUIElements()
        {
            for (int i = Math.min(page * rows * 3 + rows * 3, this.uiElements.size()) - 1; i >= page * rows * 3; i--)
            {
                this.uiElements.get(i).draw();
            }
        }

        public void draw()
        {
            this.drawUIElements();

            previous.enabled = page > 0;
            next.enabled = (uiElements.size() > (1 + page) * rows * 3);

            if (parent != null)
                back.draw();

            if (rows * 3 < uiElements.size())
            {
                previous.draw();
                next.draw();

                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                Drawing.drawing.setInterfaceFontSize(this.screen.textSize);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 200,
                        "Page %d of %d", (page + 1), (uiElements.size() / (rows * 3) + Math.min(1, uiElements.size() % (rows * 3))));
            }
        }
    }

    public static class TabGeneral extends Tab
    {
        public TextBox description;

        public TabGeneral(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            super(screen, name, category);
        }

        public TabGeneral(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        @Override
        public void updateUIElements()
        {
            super.updateUIElements();
            this.description.update();
        }

        @Override
        public void drawUIElements()
        {
            super.drawUIElements();
            this.description.draw();
        }
    }

    public static class TabWithPreview extends Tab
    {
        public TankPlayer preview;

        public TabWithPreview(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        public TabWithPreview(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            super(screen, name, category);
        }

        public void set()
        {
            this.preview = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, 0);

            this.preview.posX = this.screen.centerX;
            this.preview.posY = this.screen.centerY - 30;
        }

        @Override
        public void updateUIElements()
        {
            super.updateUIElements();
        }

        @Override
        public void drawUIElements()
        {
            this.preview.baseModel = this.screen.tank.baseModel;
            this.preview.colorModel = this.screen.tank.colorModel;
            this.preview.turretBaseModel = this.screen.tank.turretBaseModel;
            this.preview.turretModel = this.screen.tank.turretModel;
            this.preview.size = this.screen.tank.size;
            this.preview.turretSize = this.screen.tank.turretSize;
            this.preview.turretLength = this.screen.tank.turretLength;
            this.preview.emblem = this.screen.tank.emblem;
            this.preview.colorR = this.screen.tank.colorR;
            this.preview.colorG = this.screen.tank.colorG;
            this.preview.colorB = this.screen.tank.colorB;
            this.preview.secondaryColorR = this.screen.tank.secondaryColorR;
            this.preview.secondaryColorG = this.screen.tank.secondaryColorG;
            this.preview.secondaryColorB = this.screen.tank.secondaryColorB;
            this.preview.tertiaryColorR = this.screen.tank.tertiaryColorR;
            this.preview.tertiaryColorG = this.screen.tank.tertiaryColorG;
            this.preview.tertiaryColorB = this.screen.tank.tertiaryColorB;
            this.preview.emblemR = this.screen.tank.emblemR;
            this.preview.emblemG = this.screen.tank.emblemG;
            this.preview.emblemB = this.screen.tank.emblemB;
            this.preview.luminance = this.screen.tank.luminance;
            this.preview.glowIntensity = this.screen.tank.glowIntensity;
            this.preview.glowSize = this.screen.tank.glowSize;
            this.preview.lightSize = this.screen.tank.lightSize;
            this.preview.lightIntensity = this.screen.tank.lightIntensity;
            this.preview.bullet = this.screen.tank.bullet;
            this.preview.multipleTurrets = this.screen.tank.multipleTurrets;

            if (this.preview.size > Game.tile_size * 2)
                this.preview.size = Game.tile_size * 2;

            this.preview.enableTertiaryColor = this.screen.tank.enableTertiaryColor;

            this.preview.size *= 2;
            this.preview.invulnerable = true;
            this.preview.drawAge = 50;
            this.preview.depthTest = false;

            this.preview.drawTank(true, true);

            super.drawUIElements();
        }
    }

    public static class TabGlow extends TabWithPreview
    {
        public TabGlow(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        public TabGlow(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            super(screen, name, category);
        }

        public void set()
        {
            super.set();
            preview.posX = screen.centerX + screen.objXSpace / 2;
            preview.posY += 60;
        }
    }

    public static class TabPartPicker extends TabWithPreview
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

                if (colorIndex == 2)
                {
                    screen.tank.enableSecondaryColor = !screen.tank.enableSecondaryColor;
                    enable = screen.tank.enableSecondaryColor;
                    colorRed.inputText = (int) colorRed.value + "";
                    colorGreen.inputText = (int) colorGreen.value + "";
                    colorBlue.inputText = (int) colorBlue.value + "";
                }
                else if (colorIndex == 3)
                {
                    screen.tank.enableTertiaryColor = !screen.tank.enableTertiaryColor;
                    enable = screen.tank.enableTertiaryColor;
                    colorRed.inputText = (int) colorRed.value + "";
                    colorGreen.inputText = (int) colorGreen.value + "";
                    colorBlue.inputText = (int) colorBlue.value + "";
                }

                setColorText(enable);
            }
        },
                "If off, an color will automatically---be picked for this part");

        public void setColorText(boolean enable)
        {
            if (enable)
                enableColor.setText(enableColorText, ScreenOptions.onText);
            else
                enableColor.setText(enableColorText, ScreenOptions.offText);
        }

        public TabPartPicker(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category, int colorIndex)
        {
            super(screen, parent, name, category);
            this.colorIndex = colorIndex;

            colorRed = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight, "Red", () ->
            {
                if (colorRed.inputText.length() <= 0)
                    colorRed.inputText = colorRed.previousInputText;

                int red = Integer.parseInt(colorRed.inputText);

                if (colorIndex == 1)
                    this.screen.tank.colorR = red;
                else if (colorIndex == 2)
                    this.screen.tank.secondaryColorR = red;
                else if (colorIndex == 3)
                    this.screen.tank.tertiaryColorR = red;
                else if (colorIndex == 4)
                    this.screen.tank.emblemR = red;
            }
                    , 0, 0, 255, 1);

            colorRed.allowLetters = false;
            colorRed.allowSpaces = false;
            colorRed.maxChars = 3;
            colorRed.maxValue = 255;
            colorRed.checkMaxValue = true;
            colorRed.integer = true;

            colorGreen = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight,"Green", () ->
            {
                if (colorGreen.inputText.length() <= 0)
                    colorGreen.inputText = colorGreen.previousInputText;

                int green = Integer.parseInt(colorGreen.inputText);

                if (colorIndex == 1)
                    this.screen.tank.colorG = green;
                else if (colorIndex == 2)
                    this.screen.tank.secondaryColorG = green;
                else if (colorIndex == 3)
                    this.screen.tank.tertiaryColorG = green;
                else if (colorIndex == 4)
                    this.screen.tank.emblemG = green;
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
                if (colorBlue.inputText.length() <= 0)
                    colorBlue.inputText = colorBlue.previousInputText;

                int blue = Integer.parseInt(colorBlue.inputText);

                if (colorIndex == 1)
                    this.screen.tank.colorB = blue;
                else if (colorIndex == 2)
                    this.screen.tank.secondaryColorB = blue;
                else if (colorIndex == 3)
                    this.screen.tank.tertiaryColorB = blue;
                else if (colorIndex == 4)
                    this.screen.tank.emblemB = blue;
            }
                    , 0, 0, 255, 1);

            colorBlue.allowLetters = false;
            colorBlue.allowSpaces = false;
            colorBlue.maxChars = 3;
            colorBlue.maxValue = 255;
            colorBlue.checkMaxValue = true;
            colorBlue.integer = true;
        }

        public TabPartPicker(ScreenTankEditor screen, String name, TankProperty.Category category, int colorIndex)
        {
            this(screen, null, name, category, colorIndex);
        }

        @Override
        public void set()
        {
            super.set();

            int r = (int) this.screen.tank.colorR;
            int g = (int) this.screen.tank.colorG;
            int b = (int) this.screen.tank.colorB;

            if (colorIndex == 2)
            {
                r = (int) this.screen.tank.secondaryColorR;
                g = (int) this.screen.tank.secondaryColorG;
                b = (int) this.screen.tank.secondaryColorB;
            }
            else if (colorIndex == 3)
            {
                r = (int) this.screen.tank.tertiaryColorR;
                g = (int) this.screen.tank.tertiaryColorG;
                b = (int) this.screen.tank.tertiaryColorB;
            }
            else if (colorIndex == 4)
            {
                r = (int) this.screen.tank.emblemR;
                g = (int) this.screen.tank.emblemG;
                b = (int) this.screen.tank.emblemB;
            }

            if (colorIndex == 2)
                this.setColorText(this.screen.tank.enableSecondaryColor);
            else if (colorIndex == 3)
                this.setColorText(this.screen.tank.enableTertiaryColor);

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
            while (this.uiElements.size() < 8)
                this.uiElements.add(new EmptySpace());

            if (this.colorIndex == 2 || this.colorIndex == 3)
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
            super.updateUIElements();

            if (!this.screen.tank.enableSecondaryColor)
            {
                this.screen.tank.secondaryColorR = (int) Turret.calculateSecondaryColor(this.screen.tank.colorR);
                this.screen.tank.secondaryColorG = (int) Turret.calculateSecondaryColor(this.screen.tank.colorG);
                this.screen.tank.secondaryColorB = (int) Turret.calculateSecondaryColor(this.screen.tank.colorB);
            }

            if (!this.screen.tank.enableTertiaryColor)
            {
                this.screen.tank.tertiaryColorR = (int) ((this.screen.tank.colorR + this.screen.tank.secondaryColorR) / 2);
                this.screen.tank.tertiaryColorG = (int) ((this.screen.tank.colorG + this.screen.tank.secondaryColorG) / 2);
                this.screen.tank.tertiaryColorB = (int) ((this.screen.tank.colorB + this.screen.tank.secondaryColorB) / 2);
            }

            if (this.colorIndex == 1 || (this.colorIndex == 4 && this.screen.tank.emblem != null) || (this.colorIndex == 2 && this.screen.tank.enableSecondaryColor) ||  (this.colorIndex == 3 && this.screen.tank.enableTertiaryColor))
            {
                this.colorRed.update();
                this.colorGreen.update();
                this.colorBlue.update();
            }
        }

        @Override
        public void drawUIElements()
        {
            this.updateColors();

            if (this.colorIndex == 1 || (this.colorIndex == 4 && this.screen.tank.emblem != null) || (this.colorIndex == 2 && this.screen.tank.enableSecondaryColor) ||  (this.colorIndex == 3 && this.screen.tank.enableTertiaryColor))
            {
                this.colorRed.draw();
                this.colorGreen.draw();
                this.colorBlue.draw();
            }

            if (this.colorIndex == 2)
            {
                Drawing.drawing.setInterfaceFontSize(24);
                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                Drawing.drawing.displayInterfaceText(this.screen.centerX, this.screen.centerY + 200, "Note: Tank treads and turret barrel share colors.");
                Drawing.drawing.displayInterfaceText(this.screen.centerX, this.screen.centerY + 230, "Tanks on a team with a color will use that color for their treads.");
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

            if (!screen.tank.enableSecondaryColor && this.colorIndex == 2)
            {
                colorRed.value = screen.tank.secondaryColorR;
                colorGreen.value = screen.tank.secondaryColorG;
                colorBlue.value = screen.tank.secondaryColorB;
            }
            else if (!screen.tank.enableTertiaryColor && this.colorIndex == 3)
            {
                colorRed.value = screen.tank.tertiaryColorR;
                colorGreen.value = screen.tank.tertiaryColorG;
                colorBlue.value = screen.tank.tertiaryColorB;
            }
            else
            {
                if (!colorRed.selected)
                    colorRed.function.run();

                if (!colorGreen.selected)
                    colorGreen.function.run();

                if (!colorBlue.selected)
                    colorBlue.function.run();
            }
        }
    }

    public static class TabAppearance extends TabWithPreview
    {
        double margin = screen.centerX - screen.objXSpace / 2 - screen.objWidth / 2 + 30;
        double margin2 = screen.centerX - screen.objXSpace / 2;
        double margin3 = screen.centerX + screen.objXSpace / 2;

        double space = 60;

        public TabAppearance(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        public TabAppearance(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            super(screen, name, category);
        }

        @Override
        public void sortUIElements()
        {
            int in = 0;
            for (Tab t: this.subMenus)
            {
                this.uiElements.add(in, new Button(0, 0, 350, 40, t.name, () -> screen.setTab(t)));
                in++;
            }

            for (int i = 0; i < this.uiElements.size(); i++)
            {
                if (i < in)
                    this.uiElements.get(i).setPosition(margin2, Drawing.drawing.interfaceSizeY / 2 + yoffset + i * 60);
                else
                    this.uiElements.get(i).setPosition(margin3, Drawing.drawing.interfaceSizeY / 2 + yoffset + (i - in + 3) * 90);
            }

            for (Tab t: this.subMenus)
            {
                t.sortUIElements();
            }
        }

        @Override
        public void set()
        {
            super.set();
            this.preview.posX = margin3;
            this.preview.posY = Drawing.drawing.interfaceSizeY / 2 + yoffset + 90;
        }

        @Override
        public void drawUIElements()
        {
            super.drawUIElements();

            double turretBaseR = (screen.tank.secondaryColorR + screen.tank.colorR) / 2;
            double turretBaseG = (screen.tank.secondaryColorG + screen.tank.colorG) / 2;
            double turretBaseB = (screen.tank.secondaryColorB + screen.tank.colorB) / 2;
            if (screen.tank.enableTertiaryColor)
            {
                turretBaseR = screen.tank.tertiaryColorR;
                turretBaseG = screen.tank.tertiaryColorG;
                turretBaseB = screen.tank.tertiaryColorB;
            }

            double s = Game.tile_size * 0.8;

            if (screen.tank.emblem != null)
            {
                Drawing.drawing.setColor(screen.tank.emblemR, screen.tank.emblemG, screen.tank.emblemB);
                Drawing.drawing.drawInterfaceImage(screen.tank.emblem, margin, screen.centerY + 60 - space * 3, s, s);
            }

            double offmul = 1;

            if (Game.framework == Game.Framework.libgdx)
                offmul = 0;

            Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(screen.tank.turretBaseModel, margin, screen.centerY + 60 - space * 2 + 4 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(screen.tank.secondaryColorR, screen.tank.secondaryColorG, screen.tank.secondaryColorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(screen.tank.turretModel, margin, screen.centerY + 60 - space * 1, 0, s, s, s);

            Drawing.drawing.setColor(screen.tank.colorR, screen.tank.colorG, screen.tank.colorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(screen.tank.colorModel, margin, screen.centerY + 60 - space * 0 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(screen.tank.secondaryColorR, screen.tank.secondaryColorG, screen.tank.secondaryColorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(screen.tank.baseModel, margin, screen.centerY + 60 + space * 1 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(80, 80, 80);
            Drawing.drawing.fillInterfaceOval(margin, screen.centerY + 60 + space * 2, s * 1.5, s * 1.5);
            Drawing.drawing.setColor(screen.tank.secondaryColorR * preview.glowIntensity, screen.tank.secondaryColorG * preview.glowIntensity, screen.tank.secondaryColorB * preview.glowIntensity, 255, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * preview.glowSize / 4, s * 1.5 * preview.glowSize / 4);
            Drawing.drawing.setColor(255, 255, 255, 255 * preview.lightIntensity, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * preview.lightSize / 4, s * 1.5 * preview.lightSize / 4, false, true);

            Drawing.drawing.setColor(0, 0, 0, 64);
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    Drawing.drawing.fillInterfaceRect(margin + (i - 1) * s * this.screen.tank.trackSpacing, screen.centerY + 60 + space * 3 + (j - 0.5) * s * 0.6, s / 5, s / 5);
                }
            }
        }
    }

    public String[] formatDescription(TankProperty p)
    {
        ArrayList<String> text = Drawing.drawing.wrapText(p.desc(), 300, 12);
        String[] s = new String[text.size()];
        return text.toArray(s);
    }

    public ITrigger getUIElementForField(Field f, TankProperty p, TankAIControlled tank)
    {
        try
        {
            if (f.getType().equals(int.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get(tank) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get(tank) + "";
                        else
                            f.set(tank, Integer.parseInt(t.inputText));
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p);
                t.enableHover = !p.desc().equals("");
                t.maxChars = 9;
                t.allowNegatives = true;
                t.allowLetters = false;
                t.allowSpaces = false;

                return t;
            }
            else if (f.getType().equals(double.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get(tank) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get(tank) + "";
                        else
                            f.set(tank, Double.parseDouble(t.inputText));
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            t.inputText = f.get(tank) + "";
                        }
                        catch (IllegalAccessException ex)
                        {
                            Game.exitToCrash(ex);
                        }
                    }
                };

                t.hoverText = formatDescription(p);
                t.enableHover = !p.desc().equals("");
                t.allowDoubles = true;
                t.allowNegatives = true;
                t.allowLetters = false;
                t.allowSpaces = false;

                return t;
            }
            else if (p.miscType().equals(TankProperty.MiscType.emblem))
            {
                final String[] emblems = RegistryModelTank.toStringArray(Game.registryModelTank.tankEmblems);
                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), emblems, () -> {}, "");

                String selected = (String) f.get(tank);
                int selIndex = 0;
                for (int i = 0; i < emblems.length; i++)
                {
                    if ((selected == null && emblems[i] == null) ||
                            (!(selected != null && emblems[i] == null) &&
                            !(selected == null && emblems[i] != null) &&
                            emblems[i].equals(selected)))
                    {
                        selIndex = i;
                    }
                }

                t.selectedOption = selIndex;

                t.function = () ->
                {
                    try
                    {
                        f.set(tank, emblems[t.selectedOption]);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p);
                t.images = emblems;
                t.imageR = 127;
                t.imageG = 180;
                t.imageB = 255;
                return t;
            }
            else if (f.getType().equals(String.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get(tank) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0 && p.miscType() != TankProperty.MiscType.description)
                            t.inputText = f.get(tank) + "";
                        else
                            f.set(tank, t.inputText);
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p);
                t.enableHover = !p.desc().equals("");
                t.lowerCase = true;
                t.allowSpaces = true;
                t.enableSpaces = false;

                return t;
            }
            else if (f.getType().equals(boolean.class))
            {
                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, p.name(), new String[]{"Yes", "No"}, () -> {}, "");

                if ((boolean) f.get(tank))
                    t.selectedOption = 0;
                else
                    t.selectedOption = 1;

                t.function = () ->
                {
                    try
                    {
                        f.set(tank, t.selectedOption == 0);
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p);
                return t;
            }
            else if (f.getType().isEnum())
            {
                Class<? extends Enum> e = (Class<? extends Enum>) f.getType();
                Enum<?>[] values = e.getEnumConstants();
                String[] options = new String[values.length];
                for (int i = 0; i < options.length; i++)
                {
                    options[i] = Game.formatString(values[i].name());
                }

                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, p.name(), options, () -> {}, "");
                t.selectedOption = ((Enum<?>) f.get(tank)).ordinal();

                t.function = () ->
                {
                    try
                    {
                        f.set(tank, values[t.selectedOption]);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p);
                return t;
            }
            else if (Item.class.isAssignableFrom(f.getType()))
            {
                Item i = (Item) f.get(tank);
                i.importProperties();
                SelectorDrawable b = new SelectorDrawable(0, 0, 350, 40, p.name(), () -> {});
                b.function = () ->
                {
                    this.lastItem = i;
                    this.lastItemField = f;
                    this.resetLayout();
                    ScreenEditItem editItem = new ScreenEditItem(i, this);
                    editItem.delete.setText("Load from template");
                    Game.screen = editItem;
                    this.lastItemScreen = editItem;
                    this.lastItemButton = b;
                };
                b.enableHover = !p.desc().equals("");
                b.hoverText = formatDescription(p);
                b.image = i.icon;
                b.imageXOffset = - b.sizeX / 2 + b.sizeY / 2 + 10;
                b.imageSizeX = b.sizeY;
                b.imageSizeY = b.sizeY;
                b.optionText = i.name;

                return b;
            }
            else if (Tank.class.isAssignableFrom(f.getType()))
            {
                SelectorDrawable b = new SelectorDrawable(0, 0, 350, 40, p.name(), () -> {});
                b.tank = (Tank) f.get(tank);
                b.function = () ->
                {
                    ArrayList<TankAIControlled> tanks = new ArrayList<>();

                    if (this.tankScreen instanceof OverlayObjectMenu)
                        tanks = ((OverlayObjectMenu) this.tankScreen).screenLevelEditor.level.customTanks;

                    this.resetLayout();

                    ScreenSelectorTank s = new ScreenSelectorTank("Select " + p.name().toLowerCase(), b.tank, this, tanks, (t) ->
                    {
                        try
                        {
                            f.set(tank, t);
                            b.tank = t;

                            if (b.tank != null)
                                b.optionText = b.tank.name;
                            else
                                b.optionText = "\u00A7127000000255none";
                        }
                        catch (IllegalAccessException e)
                        {
                            Game.exitToCrash(e);
                        }
                    }, true);

                    s.drawBehindScreen = true;
                    Game.screen = s;
                };

                if (b.tank != null)
                    b.optionText = b.tank.name;
                else
                    b.optionText = "\u00A7127000000255none";

                b.enableHover = !p.desc().equals("");
                b.hoverText = formatDescription(p);

                return b;
            }
            else if (IModel.class.isAssignableFrom(f.getType()))
            {
                IModel[] models = null;
                String[] modelDirs;

                if (p.miscType().equals(TankProperty.MiscType.baseModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankBaseModels);
                else if (p.miscType().equals(TankProperty.MiscType.colorModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankColorModels);
                else if (p.miscType().equals(TankProperty.MiscType.turretBaseModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.turretBaseModels);
                else if (p.miscType().equals(TankProperty.MiscType.turretModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.turretModels);

                String selected = f.get(tank).toString();
                int selIndex = 0;
                modelDirs = new String[models.length];
                for (int i = 0; i < models.length; i++)
                {
                    modelDirs[i] = models[i].toString();
                    if (models[i].toString().equals(selected))
                        selIndex = i;
                }

                final IModel[] finalModels = models;

                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), modelDirs, () -> {}, "");
                t.selectedOption = selIndex;

                t.function = () ->
                {
                    try
                    {
                        f.set(tank, finalModels[t.selectedOption]);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p);
                t.models = models;

                return t;
            }
            else if (p.miscType() == TankProperty.MiscType.music)
            {
                HashSet<String> a = ((HashSet<String>) f.get(tank));
                ArrayList<String> musics = new ArrayList<>();

                for (HashSet<String> s: Game.registryTank.tankMusics.values())
                {
                    for (String m: s)
                    {
                        if (!musics.contains(m))
                            musics.add(m);
                    }
                }

                Collections.sort(musics, (o1, o2) -> o1.compareTo(o2));

                for (int i = 1; i <= 8; i++)
                {
                    musics.add("arcade/rampage" + i + ".ogg");
                }

                String[] musicsArray = new String[musics.size()];
                boolean[] selectedMusicsArray = new boolean[musics.size()];
                for (int i = 0; i < musics.size(); i++)
                {
                    musicsArray[i] = musics.get(i);
                    if (a.contains(musicsArray[i]))
                        selectedMusicsArray[i] = true;
                }

                SelectorMusic s = new SelectorMusic(0, 0, 350, 40, p.name(), musicsArray, () ->
                {
                    tank.musicTracks.clear();
                    for (int i = 0; i < musicsArray.length; i++)
                    {
                        if (selectedMusicsArray[i])
                            tank.musicTracks.add(musicsArray[i]);
                    }
                }, this);

                s.selectedOptions = selectedMusicsArray;
                return s;
            }
            else if (p.miscType() == TankProperty.MiscType.spawnedTanks)
            {
                SelectorDrawable s = new SelectorDrawable(0, 0, 350, 40, p.name());
                ArrayList<TankAIControlled.SpawnedTankEntry> a = ((ArrayList<TankAIControlled.SpawnedTankEntry>) f.get(tank));

                s.function = () ->
                {
                    this.resetLayout();
                    ScreenArrayListSelector sc = new ScreenArrayListSelector(this, "Select " + p.name().toLowerCase());
                    Game.screen = sc;

                    ArrayList<ScreenArrayListSelector.Entry> entries = new ArrayList<>();
                    for (TankAIControlled.SpawnedTankEntry e: a)
                    {
                        entries.add(getSpawnedTankEntry(e, sc));
                    }

                    a.clear();
                    s.multiTanks.clear();
                    s.tank = null;
                    s.optionText = Translation.translate("\u00A7127000000255none");

                    sc.setContent(entries,
                    () -> getSpawnedTankEntry(new TankAIControlled.SpawnedTankEntry((TankAIControlled) Game.registryTank.getEntry("dummy").getTank(0, 0, 0), 1.0), sc),
                    (entry) ->
                    {
                        SelectorDrawable sel = ((SelectorDrawable) entry.element1);
                        a.add(new TankAIControlled.SpawnedTankEntry((TankAIControlled) sel.tank, Double.parseDouble(((TextBox) entry.element2).inputText)));
                        s.multiTanks.add(sel.tank);
                        s.tank = sel.tank;
                        if (s.multiTanks.size() == 1)
                            s.optionText = sel.tank.name;
                        else
                            s.optionText = "";
                    });
                };
                s.enabled = true;

                s.optionText = Translation.translate("\u00A7127000000255none");
                for (TankAIControlled.SpawnedTankEntry e: a)
                {
                    s.multiTanks.add(e.tank);
                    s.tank = e.tank;
                    if (s.multiTanks.size() == 1)
                        s.optionText = e.tank.name;
                    else
                        s.optionText = "";
                }

                s.enableHover = !p.desc().equals("");
                s.hoverText = formatDescription(p);

                return s;
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return new Button(0, 0, 350, 40, p.name(), "This option is not available yet");
    }

    public ScreenArrayListSelector.Entry getSpawnedTankEntry(TankAIControlled.SpawnedTankEntry e, ScreenArrayListSelector s)
    {
        SelectorDrawable b = new SelectorDrawable(0, 0, 350, 40, "Spawned tank", () -> {});
        b.tank = e.tank;
        b.function = () ->
        {
            ArrayList<TankAIControlled> tanks = new ArrayList<>();

            if (this.tankScreen instanceof OverlayObjectMenu)
                tanks = ((OverlayObjectMenu) this.tankScreen).screenLevelEditor.level.customTanks;

            ScreenSelectorTank screen = new ScreenSelectorTank("Select spawned tank", b.tank, Game.screen, tanks, (t) ->
            {
                e.tank = t;
                b.tank = t;

                if (b.tank != null)
                    b.optionText = b.tank.name;
                else
                    b.optionText = "\u00A7127000000255none";
            }, false);

            screen.drawBehindScreen = true;
            Game.screen = screen;
        };

        if (b.tank != null)
            b.optionText = b.tank.name;
        else
            b.optionText = "\u00A7127000000255none";

        TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, "Spawn weight", () -> {}, e.weight + "", "Bigger numbers relative to---other spawned tanks increase---the likelihood of this tank---being spawned");
        t.function = () ->
        {
            if (t.inputText.length() == 0)
                t.inputText = e.weight + "";
            else
                e.weight = Double.parseDouble(t.inputText);
        };

        t.allowDoubles = true;
        t.allowLetters = false;
        t.allowSpaces = false;

        return new ScreenArrayListSelector.Entry(b, t, s);
    }

    public void sortTopLevelTabs()
    {
        int i = 0;
        int j = 0;
        for (Tab t: topLevelMenus)
        {
            Button b = new Button(300 * (i - 1.5) + this.centerX, 90 + j * this.objYSpace, 280, 40, t.name, () -> setTab(t));
            b.image = "tankeditor/" + t.name.toLowerCase().replace(" ", "_") + ".png";
            b.drawImageShadow = true;
            b.imageSizeX = 50;
            b.imageSizeY = 50;
            b.imageXOffset = -105;

            if (t.name.equals("Transformation"))
            {
                b.imageSizeX = 40;
                b.imageSizeY = 40;
                b.imageXOffset = -115;
            }

            this.topLevelButtons.add(b);
            t.sortUIElements();

            i++;

            if (i >= 4)
            {
                i -= 4;
                j++;
            }
        }
    }

    public void setTab(Tab t)
    {
        t.set();
        this.currentTab = t;
    }

    @Override
    public void update()
    {
        this.setupLayoutParameters();

        if (this.tankScreen instanceof ScreenLevelEditorOverlay)
        {
            this.updateMusic();
        }

        if (this.message != null)
            this.dismissMessage.update();
        else
        {
            for (Button b : this.topLevelButtons)
            {
                b.enabled = !currentTab.getRoot().name.equals(b.text);
                b.update();
            }

            this.currentTab.update();

            this.quit.update();
            this.delete.update();
            this.save.update();

            if (Game.game.input.editorPause.isValid())
            {
                Game.game.input.editorPause.invalidate();
                this.quit.function.run();
            }

        }
    }

    public void updateMusic()
    {
        ((ScreenLevelEditorOverlay) tankScreen).screenLevelEditor.updateMusic(false);

        this.prevTankMusics.clear();
        this.prevTankMusics.addAll(this.tankMusics);
        this.tankMusics.clear();
        this.tankMusics.addAll(this.tank.musicTracks);

        for (String m : this.prevTankMusics)
        {
            if (!this.tankMusics.contains(m))
                Drawing.drawing.removeSyncedMusic(m, 500);
        }

        for (String m : this.tankMusics)
        {
            if (!this.prevTankMusics.contains(m))
                Drawing.drawing.addSyncedMusic(m, Game.musicVolume * 0.5f, true, 500);
        }
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        //Drawing.drawing.setColor(127, 178, 228, 64);
        //Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

        if (Game.screen != this)
            return;
        else
            this.setupLayoutParameters();

        if (this.message != null)
        {
            this.dismissMessage.draw();

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.setInterfaceFontSize(Drawing.drawing.textSize);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.message);
        }
        else
        {
            double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
            double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, 105, width, 210);

            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY + extraHeight / 2, width, extraHeight);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 60, width, 120);

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.displayInterfaceText(this.centerX, 30, "Edit tank");

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);
            Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);

            for (Button b : this.topLevelButtons)
            {
                b.draw();
            }

            this.currentTab.draw();

            this.quit.draw();
            this.delete.draw();
            this.save.draw();
        }
    }

    @Override
    public void onAttemptClose()
    {
        ((Screen)this.tankScreen).onAttemptClose();
    }
}

package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Consumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Bullet;
import tanks.gui.*;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.registry.RegistryItem;
import tanks.tank.*;
import tanks.tankson.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class ScreenEditorPlayerTankBuild extends ScreenEditorTanksONable<TankPlayer>
{
    public boolean writeTank(Tank t)
    {
        return this.writeTank(t, false);
    }

    public boolean writeTank(Tank t, boolean overwrite)
    {
        BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.tankDir + "/" + t.name.replace(" ", "_") + ".tanks");

        if (!f.exists() || overwrite)
        {
            try
            {
                if (!f.exists())
                    f.create();

                f.startWriting();
                f.println(t.toString());
                f.stopWriting();

                return true;
            }
            catch (IOException e)
            {
                Game.exitToCrash(e);
            }
        }

        return false;
    }

    public void writeTankAndShowConfirmation(TankPlayer t, boolean overwrite)
    {
//        if (this.writeTank(t, overwrite))
//        {
//            HashSet<String> linked = new HashSet<>();
//            t.getAllLinkedTankNames(linked);
//            ArrayList<Tank> copied = new ArrayList<>();
//            ArrayList<Tank> notCopied = new ArrayList<>();
//
//            for (String s: linked)
//            {
//                Tank t1 = Game.currentLevel.lookupTank(s);
//                if (writeTank(t1))
//                    copied.add(t1);
//                else
//                    notCopied.add(t1);
//            }
//
//            Game.screen = new ScreenTankSavedInfo(this, t, copied, notCopied);
//        }
//        else
//            Game.screen = new ScreenTankSaveOverwrite(this, t);
    }

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template"
            /*, () ->
    {
        TankPlayer t = target.get();
        this.writeTankAndShowConfirmation(t, false);
    }*/
    );

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

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

    @Override
    public void setupTabs()
    {
        Tab general = new TabGeneral(this, "General", TankPropertyCategory.general);
        Tab appearance = new TabAppearance(this, "Appearance", TankPropertyCategory.appearanceGeneral);
        Tab movement = new TabTankBuild(this, "Movement", TankPropertyCategory.movementGeneral);
        Tab abilities = new TabAbilities(this, "Abilities", TankPropertyCategory.abilities);

        new TabWithPreview(this, appearance, "Emblem", TankPropertyCategory.appearanceEmblem);
        new TabWithPreview(this, appearance, "Turret base", TankPropertyCategory.appearanceTurretBase);
        new TabWithPreview(this, appearance, "Turret barrel", TankPropertyCategory.appearanceTurretBarrel);
        new TabWithPreview(this, appearance, "Tank body", TankPropertyCategory.appearanceBody);
        new TabWithPreview(this, appearance, "Tank treads", TankPropertyCategory.appearanceTreads);
        new TabGlow(this, appearance, "Glow", TankPropertyCategory.appearanceGlow);
        new TabTankBuild(this, appearance, "Tracks", TankPropertyCategory.appearanceTracks);

        this.currentTab = general;
        this.iconPrefix = "tankeditor";

        this.delete.function = () ->
        {
            setTarget(null);

            this.quit.function.run();

            if (this.prevScreen instanceof ScreenEditorTanksONable<?> || this.prevScreen instanceof ScreenSelectorArraylist)
            {
                if (!target.nullable)
                    target.cast().set(new TankReference("dummy"));

                ScreenSelectorTankReference s = new ScreenSelectorTankReference(this.objName, this.target.cast(), this.prevScreen);
                s.onComplete = this.onComplete;
                Game.screen = s;
            }
        };

        if (!(this.prevScreen instanceof ScreenEditorTanksONable<?>))
            this.deleteText = "Delete";

        if (this.prevScreen instanceof ScreenSelectorArraylist)
        {
            this.deleteText = "Select tank to link";
            this.showDeleteObj = false;
        }
    }

    public ScreenEditorPlayerTankBuild(Pointer<TankPlayer> t, Screen screen)
    {
        super(t, screen);

        this.allowClose = false;

        this.controlsMusic = true;

        this.title = "Edit %s";
        this.objName = "player build";
    }

    public class TabTankBuild extends Tab
    {
        public TabTankBuild(ScreenEditorPlayerTankBuild screen, String name, String category)
        {
            super(screen, name, category);
        }

        public TabTankBuild(ScreenEditorPlayerTankBuild screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public void addFields()
        {
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                TankBuildProperty p1 = f.getAnnotation(TankBuildProperty.class);

                if (p1 != null && p != null && ((p1.category().equals("default") && p.category().equals(this.category)) || p1.category().equals(this.category)) && p.miscType() != Property.MiscType.color)
                    this.uiElements.add(screen.getUIElementForField(new FieldPointer<>(target.get(), f), p));
            }
        }
    }

    public class TabGeneral extends Tab
    {
        public TextBox description;

        public TabGeneral(ScreenEditorPlayerTankBuild screen, String name, String category)
        {
            super(screen, name, category);
        }

        public TabGeneral(ScreenEditorPlayerTankBuild screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public void addFields()
        {
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                TankBuildProperty p1 = f.getAnnotation(TankBuildProperty.class);

                if (p1 != null && p != null && ((p1.category().equals("default") && p.category().equals(this.category)) || p1.category().equals(this.category)) && p.miscType() != Property.MiscType.color)
                {
                    if (p.miscType() == Property.MiscType.description)
                    {
                        TextBox t = (TextBox) screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);
                        t.posX = this.screen.centerX;
                        t.posY = this.screen.centerY + 270;
                        t.enableCaps = true;
                        t.allowSpaces = true;
                        t.enableSpaces = true;
                        t.enablePunctuation = true;
                        t.maxChars = 100;
                        t.sizeX *= 3;
                        this.description = t;
                    }
                    else
                        this.uiElements.add(screen.getUIElementForField(new FieldPointer<>(target.get(), f), p));
                }
            }
        }

        @Override
        public void updateUIElements()
        {
            super.updateUIElements();

            if (this.description != null)
                this.description.update();
        }

        @Override
        public void drawUIElements()
        {
            super.drawUIElements();

            if (this.description != null)
                this.description.draw();
        }
    }

    public class TabWithPreview extends TabTankBuild
    {
        public TankPlayer preview;

        public TabWithPreview(ScreenEditorPlayerTankBuild screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabWithPreview(ScreenEditorPlayerTankBuild screen, String name, String category)
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
        public void sortUIElements()
        {
            while (this.uiElements.size() < 12)
                this.uiElements.add(new EmptySpace());

            super.sortUIElements();
        }

        @Override
        public void updateUIElements()
        {
            super.updateUIElements();
        }

        @Override
        public void drawUIElements()
        {
            TankPlayer tank = this.screen.target.get();
            this.preview.baseModel = tank.baseModel;
            this.preview.colorModel = tank.colorModel;
            this.preview.turretBaseModel = tank.turretBaseModel;
            this.preview.turretModel = tank.turretModel;
            this.preview.size = tank.size;
            this.preview.turretSize = tank.turretSize;
            this.preview.turretLength = tank.turretLength;
            this.preview.emblem = tank.emblem;
            this.preview.colorR = tank.colorR;
            this.preview.colorG = tank.colorG;
            this.preview.colorB = tank.colorB;
            this.preview.secondaryColorR = tank.secondaryColorR;
            this.preview.secondaryColorG = tank.secondaryColorG;
            this.preview.secondaryColorB = tank.secondaryColorB;
            this.preview.tertiaryColorR = tank.tertiaryColorR;
            this.preview.tertiaryColorG = tank.tertiaryColorG;
            this.preview.tertiaryColorB = tank.tertiaryColorB;
            this.preview.emblemR = tank.secondaryColorR;
            this.preview.emblemG = tank.secondaryColorG;
            this.preview.emblemB = tank.secondaryColorB;
            this.preview.luminance = tank.luminance;
            this.preview.glowIntensity = tank.glowIntensity;
            this.preview.glowSize = tank.glowSize;
            this.preview.lightSize = tank.lightSize;
            this.preview.lightIntensity = tank.lightIntensity;
            this.preview.multipleTurrets = tank.multipleTurrets;

            if (this.preview.size > Game.tile_size * 2)
                this.preview.size = Game.tile_size * 2;

            this.preview.enableTertiaryColor = tank.enableTertiaryColor;

            this.preview.size *= 2;
            this.preview.invulnerable = true;
            this.preview.drawAge = 50;
            this.preview.depthTest = false;

            this.preview.drawTank(true, Game.enable3d);

            super.drawUIElements();
        }
    }

    public class TabGlow extends TabWithPreview
    {
        public TabGlow(ScreenEditorPlayerTankBuild screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabGlow(ScreenEditorPlayerTankBuild screen, String name, String category)
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

    public class TabAppearance extends TabWithPreview
    {
        double margin = screen.centerX - screen.objXSpace / 2 - screen.objWidth / 2 + 30;
        double margin2 = screen.centerX - screen.objXSpace / 2;
        double margin3 = screen.centerX + screen.objXSpace / 2;

        double space = 60;

        public TabAppearance(ScreenEditorPlayerTankBuild screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabAppearance(ScreenEditorPlayerTankBuild screen, String name, String category)
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

            TankPlayer tank = screen.target.get();
            double turretBaseR = tank.tertiaryColorR;
            double turretBaseG = tank.tertiaryColorG;
            double turretBaseB = tank.tertiaryColorB;

            double s = Game.tile_size * 0.8;

            if (tank.emblem != null)
            {
                Drawing.drawing.setColor(tank.secondaryColorR, tank.secondaryColorG, tank.secondaryColorB);
                Drawing.drawing.drawInterfaceImage(tank.emblem, margin, screen.centerY + 60 - space * 3, s, s);
            }

            double offmul = 1;

            if (Game.framework == Game.Framework.libgdx)
                offmul = 0;

            Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(tank.turretBaseModel, margin, screen.centerY + 60 - space * 2 + 4 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(tank.secondaryColorR, tank.secondaryColorG, tank.secondaryColorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(tank.turretModel, margin, screen.centerY + 60 - space * 1, 0, s, s, s);

            Drawing.drawing.setColor(tank.colorR, tank.colorG, tank.colorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(tank.colorModel, margin, screen.centerY + 60 - space * 0 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(tank.secondaryColorR, tank.secondaryColorG, tank.secondaryColorB, 255, 0.5);
            Drawing.drawing.drawInterfaceModel2D(tank.baseModel, margin, screen.centerY + 60 + space * 1 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(80, 80, 80);
            Drawing.drawing.fillInterfaceOval(margin, screen.centerY + 60 + space * 2, s * 1.5, s * 1.5);
            Drawing.drawing.setColor(tank.secondaryColorR * preview.glowIntensity, tank.secondaryColorG * preview.glowIntensity, tank.secondaryColorB * preview.glowIntensity, 255, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * preview.glowSize / 4, s * 1.5 * preview.glowSize / 4);
            Drawing.drawing.setColor(255, 255, 255, 255 * preview.lightIntensity, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * preview.lightSize / 4, s * 1.5 * preview.lightSize / 4, false, true);

            Drawing.drawing.setColor(0, 0, 0, 64);
            for (int i = 0; i < 3; i++)
            {
                for (int j = 0; j < 2; j++)
                {
                    Drawing.drawing.fillInterfaceRect(margin + (i - 1) * s * tank.trackSpacing, screen.centerY + 60 + space * 3 + (j - 0.5) * s * 0.6, s / 5, s / 5);
                }
            }
        }
    }

    public class TabAbilities extends TabTankBuild
    {
        public ArrayList<Button> deleteButtons = new ArrayList<>();
        public Selector itemSelector;

        public Button create = new Button(screen.centerX, -1000, 60, 60, "+", () ->
        {
            itemSelector.setScreen();
        });

        public TabAbilities(ScreenEditorPlayerTankBuild screen, String name, String category)
        {
            super(screen, name, category);
            this.rows += 1;

            String[] itemNames = new String[Game.registryItem.itemEntries.size()];
            String[] itemImages = new String[Game.registryItem.itemEntries.size()];

            for (int i = 0; i < Game.registryItem.itemEntries.size(); i++)
            {
                RegistryItem.ItemEntry r = Game.registryItem.getEntry(i);
                itemNames[i] = r.name;
                itemImages[i] = r.image;
            }

            itemSelector = new Selector(0, 0, 0, 0, "item type", itemNames, () ->
            {
                Consumer<Item.ItemStack<?>> addItem = (Item.ItemStack<?> i) ->
                {
                    target.get().abilities.add(i);

                    ScreenEditorItem s = new ScreenEditorItem(new ArrayListIndexPointer<>(target.get().abilities, target.get().abilities.size() - 1), screen);
                    s.onComplete = () ->
                    {
                        uiElements.clear();
                        addFields();
                        sortUIElements();
                    };
                    s.showLoadFromTemplate = true;
                    Game.screen = s;
                };

                Game.screen = new ScreenAddSavedItem(screen, addItem, Game.formatString(itemSelector.options[itemSelector.selectedOption]), Game.registryItem.getEntry(itemSelector.selectedOption).item);
            });

            itemSelector.images = itemImages;
            itemSelector.quick = true;
        }

        @Override
        public void addFields()
        {
            this.deleteButtons.clear();

            TankPlayer t = target.get();
            for (int i = 0; i < t.abilities.size(); i++)
            {
                int j = i;
                Property p = new Property()
                {
                    @Override public Class<? extends Annotation> annotationType() { return Property.class; }
                    @Override public String id() { return "ability_" + (j + 1); }
                    @Override public String name() { return "Ability " + (j + 1); }
                    @Override public String desc() { return ""; }
                    @Override public String category() { return ""; }
                    @Override public MiscType miscType() { return MiscType.none; }
                    @Override public boolean nullable() { return false; }
                    @Override public double minValue() { return 0; }
                    @Override public double maxValue() { return 0; }
                };
                SelectorDrawable s = (SelectorDrawable) getUIElementForField(new ArrayListIndexPointer<>(t.abilities, i), p);
                s.sizeX *= 1.5;
                s.imageXOffset = - s.sizeX / 2 + s.sizeY / 2;
                this.uiElements.add(s);

                Button delete = new Button(-1000, -1000, 60, 60, "x", () ->
                {
                    t.abilities.remove(j);
                    uiElements.clear();
                    addFields();
                    sortUIElements();
                });

                delete.textOffsetY = -2.5;

                delete.bgColR = 160;
                delete.bgColG = 160;
                delete.bgColB = 160;

                delete.selectedColR = 255;
                delete.selectedColG = 0;
                delete.selectedColB = 0;

                delete.textColR = 255;
                delete.textColG = 255;
                delete.textColB = 255;

                deleteButtons.add(delete);
            }

            if (t.abilities.size() < TankPlayer.max_abilities)
                this.uiElements.add(create);
        }

        public void update()
        {
            super.update();

            for (int i = 0; i < this.deleteButtons.size(); i++)
            {
                Button b = this.deleteButtons.get(i);
                b.update();
            }
        }

        public void draw()
        {
            super.draw();
            for (int i = 0; i < this.deleteButtons.size(); i++)
            {
                Button b = this.deleteButtons.get(i);
                SelectorDrawable d = ((SelectorDrawable) this.uiElements.get(i));
                b.posX = d.posX - screen.objXSpace * 0.85;
                b.posY = d.posY - screen.objHeight / 4;
                b.draw();
            }
        }
    }

    @Override
    public void addMusicTracks()
    {
        if (this.target.get() != null && (Game.screen == this || Game.screen instanceof ScreenSelectorArraylist || Game.screen instanceof ScreenSelectorTankReference))
            this.musics.addAll(this.target.get().musicTracks);
    }

    @Override
    public void updateOverlay()
    {
        if (!this.target.nullable)
            this.delete.update();

        this.save.update();
    }

    @Override
    public void drawOverlay()
    {
        if (!this.target.nullable)
            this.delete.draw();

        this.save.draw();
    }
}


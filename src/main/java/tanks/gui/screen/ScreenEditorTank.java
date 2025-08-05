package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.*;
import tanks.bullet.Bullet;
import tanks.gui.*;
import tanks.tank.*;
import tanks.tankson.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class ScreenEditorTank extends ScreenEditorTanksONable<TankAIControlled>
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

    public void writeTankAndShowConfirmation(TankAIControlled t, boolean overwrite)
    {
        if (this.writeTank(t, overwrite))
        {
            HashSet<String> linked = new HashSet<>();
            t.getAllLinkedTankNames(linked);
            ArrayList<Tank> copied = new ArrayList<>();
            ArrayList<Tank> notCopied = new ArrayList<>();

            for (String s: linked)
            {
                Tank t1 = Game.currentLevel.lookupTank(s);
                if (writeTank(t1))
                    copied.add(t1);
                else
                    notCopied.add(t1);
            }

            Game.screen = new ScreenTankSavedInfo(this, t, copied, notCopied);
        }
        else
            Game.screen = new ScreenTankSaveOverwrite(this, t);
    }

    public Button save = new Button(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Save to template", () ->
    {
        TankAIControlled t = target.get();
        this.writeTankAndShowConfirmation(t, false);
    }
    );

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

    @SuppressWarnings("unused")
    @Override
    public void setupTabs()
    {
        Tab general = new TabGeneral(this, "General", TankPropertyCategory.general);
        Tab appearance = new TabAppearance(this, "Appearance", TankPropertyCategory.appearanceGeneral);
        Tab movement = new Tab(this, "Movement", TankPropertyCategory.movementGeneral);
        Tab firing = new TabFiring(this, "Firing", TankPropertyCategory.firingGeneral);
        Tab mines = new Tab(this, "Mines", TankPropertyCategory.mines);
        Tab spawning = new Tab(this, "Spawning", TankPropertyCategory.spawning);
        Tab transformation = new Tab(this, "Transformation", TankPropertyCategory.transformationGeneral);
        Tab lastStand = new Tab(this, "Last stand", TankPropertyCategory.lastStand);

        new TabPartPicker(this, appearance, "Emblem", TankPropertyCategory.appearanceEmblem, 4);
        new TabPartPicker(this, appearance, "Turret base", TankPropertyCategory.appearanceTurretBase, 3);
        new TabPartPicker(this, appearance, "Turret barrel", TankPropertyCategory.appearanceTurretBarrel, 2);
        new TabPartPicker(this, appearance, "Tank body", TankPropertyCategory.appearanceBody, 1);
        new TabPartPicker(this, appearance, "Tank treads", TankPropertyCategory.appearanceTreads, 2);
        new TabGlow(this, appearance, "Glow", TankPropertyCategory.appearanceGlow);
        new Tab(this, appearance, "Tracks", TankPropertyCategory.appearanceTracks);


        Tab idle = new Tab(this, movement, "Idle movement", TankPropertyCategory.movementIdle);
        Tab avoid = new Tab(this, movement, "Threat avoidance", TankPropertyCategory.movementAvoid);
        Tab pathfinding = new Tab(this, movement, "Pathfinding", TankPropertyCategory.movementPathfinding);
        Tab onSight = new Tab(this, movement, "Movement on sight", TankPropertyCategory.movementOnSight);

        Tab fireBehavior = new Tab(this, firing, "Firing behavior", TankPropertyCategory.firingBehavior);
        Tab firePattern = new Tab(this, firing, "Firing pattern", TankPropertyCategory.firingPattern);

        new Tab(this, transformation, "On line of sight", TankPropertyCategory.transformationOnSight);
        new Tab(this, transformation, "On low hitpoints", TankPropertyCategory.transformationOnHealth);
        new Tab(this, transformation, "Mimic", TankPropertyCategory.transformationMimic);

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

    public ScreenEditorTank(Pointer<TankAIControlled> t, Screen screen)
    {
        super(t, screen);

        this.allowClose = false;

        this.controlsMusic = true;

        this.title = "Edit %s";
        this.objName = "tank";
    }

    public class TabGeneral extends Tab
    {
        public TextBox description;

        public TabGeneral(ScreenEditorTank screen, String name, String category)
        {
            super(screen, name, category);
        }

        public TabGeneral(ScreenEditorTank screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public void addFields()
        {
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.category().equals(this.category))
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

    public class TabFiring extends Tab
    {
        public ArrayList<Effect> effects = new ArrayList<>();
        public ArrayList<Effect> removeEffects = new ArrayList<>();
        Random rand = new Random();

        public TabFiring(ScreenEditorTanksONable<TankAIControlled> screen, String name, String category)
        {
            super(screen, name, category);
        }

        @Override
        public void draw()
        {
            Tank t = screen.target.get();
            Bullet bullet = screen.target.get().getBullet();
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

            bullet.drawForInterface(centerX, Drawing.drawing.interfaceSizeX * 0.6, centerY + objYSpace * 4, Math.min(100, bullet.size), effects, rand, t.color, t.secondaryColor);
            super.draw();
        }
    }

    public class TabWithPreview extends Tab
    {
        public TankAIControlled preview;

        public TabWithPreview(ScreenEditorTank screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabWithPreview(ScreenEditorTank screen, String name, String category)
        {
            super(screen, name, category);
        }

        public void set()
        {
            this.preview = new TankAIControlled("preview", Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, Game.tile_size, 0, 0, 0, 0, TankAIControlled.ShootAI.none);

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
            TankAIControlled tank = this.screen.target.get();
            this.preview.baseModel = tank.baseModel;
            this.preview.colorModel = tank.colorModel;
            this.preview.turretBaseModel = tank.turretBaseModel;
            this.preview.turretModel = tank.turretModel;
            this.preview.baseSkin = tank.baseSkin;
            this.preview.colorSkin = tank.colorSkin;
            this.preview.turretBaseSkin = tank.turretBaseSkin;
            this.preview.turretSkin = tank.turretSkin;
            this.preview.size = tank.size;
            this.preview.turretSize = tank.turretSize;
            this.preview.turretLength = tank.turretLength;
            this.preview.emblem = tank.emblem;
            this.preview.color.set(tank.color);
            this.preview.secondaryColor.set(tank.secondaryColor);
            this.preview.tertiaryColor.set(tank.tertiaryColor);
            this.preview.emblemColor.set(tank.emblemColor);
            this.preview.luminance = tank.luminance;
            this.preview.glowIntensity = tank.glowIntensity;
            this.preview.glowSize = tank.glowSize;
            this.preview.lightSize = tank.lightSize;
            this.preview.lightIntensity = tank.lightIntensity;
            this.preview.setBullet(tank.getBullet());
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
        public TabGlow(ScreenEditorTank screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabGlow(ScreenEditorTank screen, String name, String category)
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

    public class TabPartPicker extends TabWithPreview
    {
        public int colorIndex;
        public SelectorColor colorPicker;

        String enableColorText = "Custom color: ";
        Button enableColor = new Button(0, 0, this.screen.objWidth, this.screen.objHeight, "", new Runnable()
        {
            @Override
            public void run()
            {
                boolean enable = false;
                TankAIControlled tank = screen.target.get();

                if (colorIndex == 2)
                {
                    tank.enableSecondaryColor = !tank.enableSecondaryColor;
                    enable = tank.enableSecondaryColor;
                }
                else if (colorIndex == 3)
                {
                    tank.enableTertiaryColor = !tank.enableTertiaryColor;
                    enable = tank.enableTertiaryColor;
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

        public TabPartPicker(ScreenEditorTank screen, Tab parent, String name, String category, int colorIndex)
        {
            super(screen, parent, name, category);
            this.colorIndex = colorIndex;
        }

        public TabPartPicker(ScreenEditorTank screen, String name, String category, int colorIndex)
        {
            this(screen, null, name, category, colorIndex);
        }

        @Override
        public void set()
        {
            super.set();
            TankAIControlled tank = screen.target.get();
            this.colorPicker.updateColors();

            if (colorIndex == 2)
                this.setColorText(tank.enableSecondaryColor);
            else if (colorIndex == 3)
                this.setColorText(tank.enableTertiaryColor);
        }

        @Override
        public void sortUIElements()
        {
            while (this.uiElements.size() < 8)
                this.uiElements.add(new EmptySpace());

            if (this.colorIndex == 2 || this.colorIndex == 3)
                this.uiElements.add(enableColor);

            this.uiElements.add(this.colorPicker);
            super.sortUIElements();
            this.uiElements.remove(this.colorPicker);
        }

        @Override
        public void updateUIElements()
        {
            super.updateUIElements();
            TankAIControlled tank = screen.target.get();

            if (this.colorIndex == 1 || (this.colorIndex == 4 && tank.emblem != null) || (this.colorIndex == 2 && tank.enableSecondaryColor) ||  (this.colorIndex == 3 && tank.enableTertiaryColor))
                this.colorPicker.update();

            if (!tank.enableSecondaryColor)
                Turret.setSecondary(tank.color, tank.secondaryColor);
        }

        @Override
        public void drawUIElements()
        {
            this.updateColors();
            TankAIControlled tank = screen.target.get();

            if (this.colorIndex == 1 || (this.colorIndex == 4 && tank.emblem != null) || (this.colorIndex == 2 && tank.enableSecondaryColor) ||  (this.colorIndex == 3 && tank.enableTertiaryColor))
            {
                this.colorPicker.draw();
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

            try
            {
                if (this.colorIndex == 2 && this.colorPicker == null)
                {
                    Field f = Tank.class.getField("secondaryColor");
                    this.colorPicker = (SelectorColor) screen.getUIElementForField(new FieldPointer<>(target.get(), f), f.getAnnotation(Property.class));
                }
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }

        }


        public void updateColors()
        {
            TankAIControlled tank = screen.target.get();
        }
    }

    public class TabAppearance extends TabWithPreview
    {
        double margin = screen.centerX - screen.objXSpace / 2 - screen.objWidth / 2 + 30;
        double margin2 = screen.centerX - screen.objXSpace / 2;
        double margin3 = screen.centerX + screen.objXSpace / 2;

        double space = 60;

        public TabAppearance(ScreenEditorTank screen, Tab parent, String name, String category)
        {
            super(screen, parent, name, category);
        }

        public TabAppearance(ScreenEditorTank screen, String name, String category)
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

            TankAIControlled tank = screen.target.get();
            double turretBaseR = (tank.secondaryColor.red + tank.color.red) / 2;
            double turretBaseG = (tank.secondaryColor.green + tank.color.green) / 2;
            double turretBaseB = (tank.secondaryColor.blue + tank.color.blue) / 2;
            if (tank.enableTertiaryColor)
            {
                turretBaseR = tank.tertiaryColor.red;
                turretBaseG = tank.tertiaryColor.green;
                turretBaseB = tank.tertiaryColor.blue;
            }

            double s = Game.tile_size * 0.8;

            if (tank.emblem != null)
            {
                Drawing.drawing.setColor(tank.emblemColor);
                Drawing.drawing.drawInterfaceImage(tank.emblem, margin, screen.centerY + 60 - space * 3, s, s);
            }

            double offmul = 1;

            if (Game.framework == Game.Framework.libgdx)
                offmul = 0;

            Drawing.drawing.setColor(turretBaseR, turretBaseG, turretBaseB, 255, 0.5);
            tank.turretBaseModel.setSkin(tank.turretBaseSkin.turretBase);
            Drawing.drawing.drawInterfaceModel2D(tank.turretBaseModel, margin, screen.centerY + 60 - space * 2 + 4 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(tank.secondaryColor, 255, 0.5);
            tank.turretModel.setSkin(tank.turretSkin.turret);
            Drawing.drawing.drawInterfaceModel2D(tank.turretModel, margin, screen.centerY + 60 - space * 1, 0, s, s, s);

            Drawing.drawing.setColor(tank.color, 255, 0.5);
            tank.colorModel.setSkin(tank.colorSkin.color);
            Drawing.drawing.drawInterfaceModel2D(tank.colorModel, margin, screen.centerY + 60 - space * 0 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(tank.secondaryColor, 255, 0.5);
            tank.baseModel.setSkin(tank.baseSkin.base);
            Drawing.drawing.drawInterfaceModel2D(tank.baseModel, margin, screen.centerY + 60 + space * 1 + 7 * offmul, 0, s, s, s);

            Drawing.drawing.setColor(80, 80, 80);
            Drawing.drawing.fillInterfaceOval(margin, screen.centerY + 60 + space * 2, s * 1.5, s * 1.5);
            Drawing.drawing.setColor(tank.secondaryColor.red * preview.glowIntensity, tank.secondaryColor.green * preview.glowIntensity, tank.secondaryColor.blue * preview.glowIntensity, 255, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * Math.min(2, preview.glowSize / 4), s * 1.5 * Math.min(2, preview.glowSize / 4));
            Drawing.drawing.setColor(255, 255, 255, 255 * preview.lightIntensity, 1);
            Drawing.drawing.fillInterfaceGlow(margin, screen.centerY + 60 + space * 2, s * 1.5 * Math.min(2, preview.lightSize / 4), s * 1.5 * Math.min(2, preview.lightSize / 4), false, true);

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

    @Override
    public void addTabButton(Tab t, Button b)
    {
        if (t.name.equals("Transformation"))
        {
            b.imageSizeX = 40;
            b.imageSizeY = 40;
            b.imageXOffset = -115;
        }

        this.topLevelButtons.add(b);
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

    @Override
    public void validateChangedProperty(Pointer<?> f, Property p, Object oldValue)
    {
        TankAIControlled t = target.get();
        if (p.id().equals("cooldown_base"))
        {
            double bc = t.bulletItem.item.cooldownBase;
            if ((double) oldValue >= bc && (double) f.cast().get() < bc)
            {
                Game.screen = new ScreenInfo(Game.screen, "Note!",
                        new String[]{"The base tank cooldown you picked is",
                                "less than the tank's bullet's cooldown.", "",
                                "The greater cooldown value will be used."});
            }
        }
        else if (p.id().equals("enable_predictive_firing"))
        {
            if ((boolean) f.cast().get())
            {
                if (t.shootAIType != TankAIControlled.ShootAI.straight && t.shootAIType != TankAIControlled.ShootAI.alternate)
                {
                    Game.screen = new ScreenInfo(Game.screen, "Note!",
                            new String[]{"Predictive firing is only effective",
                                    "with straight or alternate aiming behavior."});
                }
            }
        }
    }
}


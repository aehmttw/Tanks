package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.*;
import tanks.tank.*;
import tanks.tankson.Pointer;
import tanks.tankson.Property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

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
        Tab movement = new Tab(this, "Movement", TankPropertyCategory.movementGeneral);
        Tab firing = new Tab(this, "Firing", TankPropertyCategory.firingGeneral);
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
                if (p != null && p.category().equals(this.category) && p.miscType() != Property.MiscType.color)
                {
                    if (p.miscType() == Property.MiscType.description)
                    {
                        TextBox t = (TextBox) screen.getUIElementForField(f, p, target);
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
                        this.uiElements.add(screen.getUIElementForField(f, p, target));
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

    public class TabWithPreview extends Tab
    {
        public TankPlayer preview;

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
            TankAIControlled tank = this.screen.target.get();
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
            this.preview.emblemR = tank.emblemR;
            this.preview.emblemG = tank.emblemG;
            this.preview.emblemB = tank.emblemB;
            this.preview.luminance = tank.luminance;
            this.preview.glowIntensity = tank.glowIntensity;
            this.preview.glowSize = tank.glowSize;
            this.preview.lightSize = tank.lightSize;
            this.preview.lightIntensity = tank.lightIntensity;
            this.preview.setBullet(tank.bullet);
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
                TankAIControlled tank = screen.target.get();

                if (colorIndex == 2)
                {
                    tank.enableSecondaryColor = !tank.enableSecondaryColor;
                    enable = tank.enableSecondaryColor;
                    colorRed.inputText = (int) colorRed.value + "";
                    colorGreen.inputText = (int) colorGreen.value + "";
                    colorBlue.inputText = (int) colorBlue.value + "";
                }
                else if (colorIndex == 3)
                {
                    tank.enableTertiaryColor = !tank.enableTertiaryColor;
                    enable = tank.enableTertiaryColor;
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

        public TabPartPicker(ScreenEditorTank screen, Tab parent, String name, String category, int colorIndex)
        {
            super(screen, parent, name, category);
            this.colorIndex = colorIndex;

            colorRed = new TextBoxSlider(0, 0, this.screen.objWidth, this.screen.objHeight, "Red", () ->
            {
                TankAIControlled tank = screen.target.get();
                if (colorRed.inputText.length() <= 0)
                    colorRed.inputText = colorRed.previousInputText;

                int red = Integer.parseInt(colorRed.inputText);

                if (colorIndex == 1)
                    tank.colorR = red;
                else if (colorIndex == 2)
                    tank.secondaryColorR = red;
                else if (colorIndex == 3)
                    tank.tertiaryColorR = red;
                else if (colorIndex == 4)
                    tank.emblemR = red;
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
                TankAIControlled tank = screen.target.get();

                if (colorGreen.inputText.length() <= 0)
                    colorGreen.inputText = colorGreen.previousInputText;

                int green = Integer.parseInt(colorGreen.inputText);

                if (colorIndex == 1)
                    tank.colorG = green;
                else if (colorIndex == 2)
                    tank.secondaryColorG = green;
                else if (colorIndex == 3)
                    tank.tertiaryColorG = green;
                else if (colorIndex == 4)
                    tank.emblemG = green;
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
                TankAIControlled tank = screen.target.get();

                if (colorBlue.inputText.length() <= 0)
                    colorBlue.inputText = colorBlue.previousInputText;

                int blue = Integer.parseInt(colorBlue.inputText);

                if (colorIndex == 1)
                    tank.colorB = blue;
                else if (colorIndex == 2)
                    tank.secondaryColorB = blue;
                else if (colorIndex == 3)
                    tank.tertiaryColorB = blue;
                else if (colorIndex == 4)
                    tank.emblemB = blue;
            }
                    , 0, 0, 255, 1);

            colorBlue.allowLetters = false;
            colorBlue.allowSpaces = false;
            colorBlue.maxChars = 3;
            colorBlue.maxValue = 255;
            colorBlue.checkMaxValue = true;
            colorBlue.integer = true;
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

            int r = (int) tank.colorR;
            int g = (int) tank.colorG;
            int b = (int) tank.colorB;

            if (colorIndex == 2)
            {
                r = (int) tank.secondaryColorR;
                g = (int) tank.secondaryColorG;
                b = (int) tank.secondaryColorB;
            }
            else if (colorIndex == 3)
            {
                r = (int) tank.tertiaryColorR;
                g = (int) tank.tertiaryColorG;
                b = (int) tank.tertiaryColorB;
            }
            else if (colorIndex == 4)
            {
                r = (int) tank.emblemR;
                g = (int) tank.emblemG;
                b = (int) tank.emblemB;
            }

            if (colorIndex == 2)
                this.setColorText(tank.enableSecondaryColor);
            else if (colorIndex == 3)
                this.setColorText(tank.enableTertiaryColor);

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
            TankAIControlled tank = screen.target.get();

            if (!tank.enableSecondaryColor)
            {
                tank.secondaryColorR = (int) Turret.calculateSecondaryColor(tank.colorR);
                tank.secondaryColorG = (int) Turret.calculateSecondaryColor(tank.colorG);
                tank.secondaryColorB = (int) Turret.calculateSecondaryColor(tank.colorB);
            }

            if (!tank.enableTertiaryColor)
            {
                tank.tertiaryColorR = (int) ((tank.colorR + tank.secondaryColorR) / 2);
                tank.tertiaryColorG = (int) ((tank.colorG + tank.secondaryColorG) / 2);
                tank.tertiaryColorB = (int) ((tank.colorB + tank.secondaryColorB) / 2);
            }

            if (this.colorIndex == 1 || (this.colorIndex == 4 && tank.emblem != null) || (this.colorIndex == 2 && tank.enableSecondaryColor) ||  (this.colorIndex == 3 && tank.enableTertiaryColor))
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
            TankAIControlled tank = screen.target.get();

            if (this.colorIndex == 1 || (this.colorIndex == 4 && tank.emblem != null) || (this.colorIndex == 2 && tank.enableSecondaryColor) ||  (this.colorIndex == 3 && tank.enableTertiaryColor))
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
            TankAIControlled tank = screen.target.get();
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

            if (!tank.enableSecondaryColor && this.colorIndex == 2)
            {
                colorRed.value = tank.secondaryColorR;
                colorGreen.value = tank.secondaryColorG;
                colorBlue.value = tank.secondaryColorB;
            }
            else if (!tank.enableTertiaryColor && this.colorIndex == 3)
            {
                colorRed.value = tank.tertiaryColorR;
                colorGreen.value = tank.tertiaryColorG;
                colorBlue.value = tank.tertiaryColorB;
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
            double turretBaseR = (tank.secondaryColorR + tank.colorR) / 2;
            double turretBaseG = (tank.secondaryColorG + tank.colorG) / 2;
            double turretBaseB = (tank.secondaryColorB + tank.colorB) / 2;
            if (tank.enableTertiaryColor)
            {
                turretBaseR = tank.tertiaryColorR;
                turretBaseG = tank.tertiaryColorG;
                turretBaseB = tank.tertiaryColorB;
            }

            double s = Game.tile_size * 0.8;

            if (tank.emblem != null)
            {
                Drawing.drawing.setColor(tank.emblemR, tank.emblemG, tank.emblemB);
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
}


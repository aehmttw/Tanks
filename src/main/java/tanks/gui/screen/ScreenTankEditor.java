package tanks.gui.screen;

import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.*;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemBullet;
import tanks.registry.RegistryModelTank;
import tanks.tank.TankAIControlled;
import tanks.tank.TankPlayer;
import tanks.tank.TankProperty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

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

    public boolean drawBehindScreen;

    public ITankScreen tankScreen;

    public Button delete = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6, this.objWidth, this.objHeight, "Delete tank", () ->
    {
        Game.screen = (Screen) this.tankScreen;
        this.tankScreen.removeTank(this.tank);
        this.tankScreen.refreshTanks();
    }
    );

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 6, this.objWidth, this.objHeight, "Done", () ->
    {
        Game.screen = (Screen) this.tankScreen;
        this.tankScreen.refreshTanks();
    }
    );

    public ScreenTankEditor(TankAIControlled t, ITankScreen screen)
    {
        this.allowClose = false;

        this.tankScreen = screen;
        this.tank = t;

        this.music = ((Screen)screen).music;
        this.musicID = ((Screen)screen).musicID;

        t.bullet.className = ItemBullet.classMap2.get(t.bullet.bulletClass);

        this.fields = TankAIControlled.class.getFields();
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

        Tab general = new Tab(this, "General", TankProperty.Category.general);
        Tab appearance = new TabAppearance(this, "Appearance", TankProperty.Category.appearanceGeneral);
        Tab movement = new Tab(this, "Movement", TankProperty.Category.movementGeneral);
        Tab firing = new Tab(this, "Firing", TankProperty.Category.firingGeneral);
        Tab mines = new Tab(this, "Mines", TankProperty.Category.mines);
        Tab spawning = new Tab(this, "Spawning", TankProperty.Category.spawning);
        Tab transformation = new Tab(this, "Transformation", TankProperty.Category.transformationGeneral);
        Tab lastStand = new Tab(this, "Last stand", TankProperty.Category.lastStand);

        Tab model = new TabModel(this, appearance, "Tank model", TankProperty.Category.appearanceModel);
        Tab color = new TabColor(this, appearance, "Tank color", TankProperty.Category.appearanceColor);

        Tab idle = new Tab(this, movement, "Idle movement", TankProperty.Category.movementIdle);
        Tab avoid = new Tab(this, movement, "Threat avoidance", TankProperty.Category.movementAvoid);
        Tab pathfinding = new Tab(this, movement, "Pathfinding", TankProperty.Category.movementPathfinding);
        Tab onSight = new Tab(this, movement, "Movement on sight", TankProperty.Category.movementOnSight);

        Tab fireBehavior = new Tab(this, firing, "Firing behavior", TankProperty.Category.firingBehavior);
        Tab firePattern = new Tab(this, firing, "Firing pattern", TankProperty.Category.firingPattern);

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

            this.addFields(TankAIControlled.class);
        }

        public void set()
        {

        }

        public void addFields(Class<?> c)
        {
            for (Field f: this.screen.fields)
            {
                TankProperty p = f.getAnnotation(TankProperty.class);
                if (p != null && p.category() == this.category)
                {
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

    public static class TabColor extends Tab
    {
        public ScreenOverlayTankColorPicker color;

        public TabColor(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        public TabColor(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            this(screen, null, name, category);
        }

        public void set()
        {
            this.color = new ScreenOverlayTankColorPicker(screen.tank, 120);
            this.color.preview.posY += 190;
        }

        @Override
        public void updateUIElements()
        {
            this.color.update();
        }

        @Override
        public void drawUIElements()
        {
            this.color.draw();
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
            this.preview.draw();
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
            this.preview.bullet = this.screen.tank.bullet;

            if (this.preview.size > Game.tile_size * 3)
                this.preview.size = Game.tile_size * 3;

            this.preview.size *= 2;
            this.preview.invulnerable = true;
            this.preview.drawAge = 50;
            this.preview.depthTest = false;

            this.preview.draw();

            super.drawUIElements();
        }
    }

    public static class TabAppearance extends TabWithPreview
    {
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
            this.yoffset = 180;
            super.sortUIElements();
        }
    }

    public static class TabModel extends TabWithPreview
    {
        public TabModel(ScreenTankEditor screen, Tab parent, String name, TankProperty.Category category)
        {
            super(screen, parent, name, category);
        }

        public TabModel(ScreenTankEditor screen, String name, TankProperty.Category category)
        {
            super(screen, name, category);
        }

        @Override
        public void sortUIElements()
        {
            this.uiElements.add(4, new EmptySpace());
            this.uiElements.add(5, new EmptySpace());
            this.uiElements.add(6, new EmptySpace());
            this.uiElements.add(7, new EmptySpace());

            super.sortUIElements();
        }

        @Override
        public void set()
        {
            super.set();
            this.preview.posX = this.screen.centerX;
            this.preview.posY = this.screen.centerY;
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
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p);
                t.enableHover = !p.desc().equals("");
                t.allowDoubles = true;
                t.allowLetters = false;
                t.allowSpaces = false;

                return t;
            }
            else if (p.miscType().equals(TankProperty.MiscType.emblem))
            {
                final String[] emblems = RegistryModelTank.toStringArray(Game.registryModelTank.tankEmblems);
                ImageSelector t = new ImageSelector(0, 0, this.objWidth, this.objHeight, p.name(), emblems, () -> {}, "");

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

                return t;
            }
            else if (f.getType().equals(String.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get(tank) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
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
            else if (IModel.class.isAssignableFrom(f.getType()))
            {
                IModel[] models = null;
                String[] modelDirs;

                if (p.miscType().equals(TankProperty.MiscType.base))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankBaseModels);
                else if (p.miscType().equals(TankProperty.MiscType.color))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankColorModels);
                else if (p.miscType().equals(TankProperty.MiscType.turretBase))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.turretBaseModels);
                else if (p.miscType().equals(TankProperty.MiscType.turret))
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

                ImageSelector t = new ImageSelector(0, 0, this.objWidth, this.objHeight, p.name(), modelDirs, () -> {}, "");
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
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return new Button(0, 0, 350, 40, p.name(), "This option is not available yet");
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
        if (Game.game.input.editorPause.isValid())
        {
            Game.game.input.editorPause.invalidate();
            this.quit.function.run();
        }

        for (Button b: this.topLevelButtons)
        {
            b.enabled = !currentTab.getRoot().name.equals(b.text);
            b.update();
        }

        this.currentTab.update();

        this.quit.update();
        this.delete.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(127, 178, 228, 64);
        Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth + 1, Game.game.window.absoluteHeight + 1);

        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, 105, width, 210);

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.displayInterfaceText(this.centerX, 30, "Edit tank");

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);

        for (Button b: this.topLevelButtons)
        {
            b.draw();
        }

        this.currentTab.draw();

        this.quit.draw();
        this.delete.draw();
    }

    @Override
    public void onAttemptClose()
    {
        ((Screen)this.tankScreen).onAttemptClose();
    }
}

package tanks.gui.screen;

import basewindow.Color;
import basewindow.IModel;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.bullet.Bullet;
import tanks.bullet.BulletEffect;
import tanks.bullet.Trail;
import tanks.gui.*;
import tanks.gui.screen.leveleditor.ScreenLevelEditorOverlay;
import tanks.item.Item;
import tanks.registry.RegistryModelTank;
import tanks.tank.*;
import tanks.tankson.*;
import tanks.translation.Translation;

import java.lang.reflect.Field;
import java.util.*;

public abstract class ScreenEditorTanksONable<T> extends Screen implements IBlankBackgroundScreen, IScreenWithCompletion
{
    public Tab currentTab;
    public ArrayList<Tab> topLevelMenus = new ArrayList<>();
    public ArrayList<Tab> allTabs = new ArrayList<>();
    public ArrayList<Button> topLevelButtons = new ArrayList<>();

    public String message = null;
    public String[] messageParams = new String[0];

    public boolean drawBehindScreen;

    public String title = "Edit";
    public String objName = "thing";
    public String iconPrefix = "editor";
    public String deleteText = "Remove";
    public boolean showDeleteObj = true;

    public Screen prevScreen;
    public Pointer<T> target;
    public boolean forceDisplayTabs = false;

    public Field[] fields;

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

    public Runnable onComplete = null;

    public HashSet<String> prevMusics = new HashSet<>();
    public HashSet<String> musics = new HashSet<>();

    public boolean controlsMusic = false;

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Done", () ->
    {
        this.clearMusicTracks();
        this.resetLayout();

        Game.screen = this.prevScreen;

        if (this.onComplete != null)
            this.onComplete.run();
    }
    );

    public Button delete = new Button(this.centerX - this.objXSpace, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "", () -> setTarget(null));
    public Button create = new Button(this.centerX, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "", () ->
    {
        try
        {
            setTarget(target.getType().newInstance());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    });

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

    public ScreenEditorTanksONable(Pointer<T> t, Screen screen)
    {
        super(350, 40, 380, 60);
        this.target = t;

        this.allowClose = false;

        this.prevScreen = screen;

        this.music = screen.music;
        this.musicID = screen.musicID;

        this.setupTabs();
        this.resetFields();
        this.resetTabs();
    }

    /**
     * Add all the tabs here and make sure to set the default tab!
     * You can also set icon prefix for editor icon folder
     */
    public abstract void setupTabs();

    public void setTarget(T value)
    {
        this.target.set(value);
        this.resetFields();
        this.resetTabs();
    }

    public void resetFields()
    {
        T t = target.get();
        if (t == null)
            return;

        this.fields = t.getClass().getFields();

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
    }

    public void resetTabs()
    {
        for (Tab t: allTabs)
        {
            t.uiElements.clear();

            if (target.get() != null)
                t.addFields();
        }

        this.sortTopLevelTabs();
    }

    public class Tab
    {
        public ScreenEditorTanksONable<T> screen;
        public ArrayList<Tab> subMenus = new ArrayList<>();
        public ArrayList<ITrigger> uiElements = new ArrayList<>();
        public String name;
        public String category;
        public Tab parent;

        public int rows = 4;
        public int yoffset = -120;
        public int page = 0;

        public Button next = new Button(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Next page", () -> page++);

        public Button previous = new Button(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 240, 350, 40, "Previous page", () -> page--);

        public Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 300, 350, 40, "Back", () -> {screen.setTab(screen.currentTab.parent);});

        public Tab(ScreenEditorTanksONable<T> screen, String name, String category)
        {
            this(screen, null, name, category);
        }

        public Tab(ScreenEditorTanksONable<T> screen, Tab parent, String name, String category)
        {
            if (parent == null)
                screen.topLevelMenus.add(this);
            else
                parent.addSubMenu(this);

            screen.allTabs.add(this);

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
        }

        public void set()
        {

        }

        public void addFields()
        {
            this.uiElements.clear();
            for (Field f: this.screen.fields)
            {
                Property p = f.getAnnotation(Property.class);
                if (p != null && p.category().equals(this.category) && p.miscType() != Property.MiscType.color)
                {
                    ITrigger t = screen.getUIElementForField(new FieldPointer<>(target.get(), f), p);
                    this.uiElements.add(t);
                    for (int i = 1; i < t.getSize(); i++)
                    {
                        this.uiElements.add(new EmptySpace());
                    }
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
            if (parent != null)
                back.draw();

            previous.enabled = page > 0;
            next.enabled = (uiElements.size() > (1 + page) * rows * 3);

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

            this.drawUIElements();
        }

        public void drawOver()
        {

        }
    }

    public String[] formatDescription(String desc)
    {
        ArrayList<String> text = Drawing.drawing.wrapText(desc, 300, 12);
        String[] s = new String[text.size()];
        return text.toArray(s);
    }

    public ITrigger getUIElementForField(Pointer<?> f, Property p)
    {
        try
        {
            if (f.getType().equals(int.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get() + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get() + "";
                        else
                        {
                            int old = (int) f.cast().get();
                            f.cast().set((int) Double.parseDouble(t.inputText));
                            validateChangedProperty(f, p, old);
                        }

                        t.inputText = f.get().toString();
                    }
                    catch (NumberFormatException e)
                    {
                        t.inputText = t.previousInputText;
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p.desc());
                t.enableHover = !p.desc().equals("");
                t.maxChars = 11;
                t.allowLetters = false;
                t.allowSpaces = false;
                t.minValue = (int) p.minValue();
                t.maxValue = (int) p.maxValue();
                t.checkMinValue = true;
                t.checkMaxValue = true;
                t.allowNegatives = t.minValue < 0;

                return t;
            }
            else if (f.getType().equals(double.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () ->
                {
                }, f.get() + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get() + "";
                        else
                        {
                            double old = (double) f.cast().get();
                            f.cast().set(Double.parseDouble(t.inputText));
                            validateChangedProperty(f, p, old);
                        }

                        t.inputText = f.get().toString();
                    }
                    catch (Exception e)
                    {
                        t.inputText = f.get() + "";
                    }
                };

                t.hoverText = formatDescription(p.desc());
                t.enableHover = !p.desc().equals("");
                t.allowDoubles = true;
                t.allowLetters = false;
                t.allowSpaces = false;
                t.minValue = p.minValue();
                t.maxValue = p.maxValue();
                t.checkMinValue = true;
                t.checkMaxValue = true;
                t.allowNegatives = t.minValue < 0;

                return t;
            }
            else if (p.miscType().equals(Property.MiscType.emblem))
            {
                final String[] emblems = RegistryModelTank.toStringArray(Game.registryModelTank.tankEmblems);
                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), emblems, () -> {}, "");

                String selected = (String) f.get();
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
                        Object old = f.get();
                        f.cast().set(emblems[t.selectedOption]);
                        validateChangedProperty(f, p, old);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
                t.images = emblems;
                t.imageR = 127;
                t.imageG = 180;
                t.imageB = 255;
                return t;
            }
            else if (p.miscType() == Property.MiscType.bulletSound)
            {
                ArrayList<String> sounds = Game.game.fileManager.getInternalFileContents("/sounds/bullet_sounds.txt");
                String[] soundsFormatted = new String[sounds.size()];

                for (int i = 0; i < sounds.size(); i++)
                {
                    soundsFormatted[i] = Game.formatString(sounds.get(i).replace(".ogg", ""));
                }

                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, p.name(), soundsFormatted, () -> {}, "");
                t.selectedOption = sounds.indexOf((String) f.get());
                t.sounds = new String[sounds.size()];
                sounds.toArray(t.sounds);

                t.function = () ->
                {
                    try
                    {
                        Object old = f.get();
                        f.cast().set(sounds.get(t.selectedOption));
                        validateChangedProperty(f, p, old);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
                return t;
            }
            else if (p.miscType() == Property.MiscType.itemIcon)
            {
                ArrayList<String> icons = new ArrayList<>(Arrays.asList("item.png", "bullet_normal.png", "bullet_mini.png", "bullet_large.png", "bullet_fire.png", "bullet_fire_trail.png", "bullet_dark_fire.png", "bullet_flame.png",
                        "bullet_laser.png", "bullet_healing.png", "bullet_electric.png", "bullet_freeze.png", "bullet_arc.png", "bullet_block.png", "bullet_explosive.png", "bullet_boost.png", "bullet_air.png", "bullet_homing.png",
                        "mine.png",
                        "shield.png", "shield_gold.png"));
                String[] iconsArray = new String[icons.size()];
                icons.toArray(iconsArray);

                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), iconsArray, () -> {});
                t.drawImages = true;
                t.selectedOption = icons.indexOf(f.get());

                t.function = () ->
                {
                    Object old = f.get();
                    f.cast().set(icons.get(t.selectedOption));
                    validateChangedProperty(f, p, old);
                };

                return t;
            }
            else if (f.getType().equals(boolean.class))
            {
                SelectorDrawable t = new SelectorDrawable(0, 0, this.objWidth, this.objHeight, p.name(), () ->
                {
                }, "");

                t.function = () ->
                {
                    try
                    {
                        Pointer<Boolean> b = f.cast();
                        Object old = f.get();
                        b.set(!b.get());
                        validateChangedProperty(f, p, old);
                        t.optionText = b.get() ? "Yes" : "No";
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };
                Pointer<Boolean> b = f.cast();
                t.optionText = b.get() ? "Yes" : "No";
                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
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

                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, p.name(), options, () ->
                {
                }, "");
                t.selectedOption = ((Enum<?>) f.get()).ordinal();

                t.function = () ->
                {
                    try
                    {
                        Object old = f.get();
                        f.cast().set(values[t.selectedOption]);
                        validateChangedProperty(f, p, old);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
                return t;
            }
            else if (Color.class.isAssignableFrom(f.getType()))
            {
                return new SelectorColor(0, 0, this.objWidth, this.objHeight, p.name(), this.objYSpace * 1.5, (Color) f.get(), !p.miscType().equals(Property.MiscType.alphaless));
            }
            else if (ITanksONEditable.class.isAssignableFrom(f.getType()))
            {
                return this.getTanksONSelector(f.cast(), p.name(), p.desc());
            }
            else if (IModel.class.isAssignableFrom(f.getType()))
            {
                IModel[] models = null;
                String[] modelDirs;

                if (p.miscType().equals(Property.MiscType.baseModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankBaseModels);
                else if (p.miscType().equals(Property.MiscType.colorModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.tankColorModels);
                else if (p.miscType().equals(Property.MiscType.turretBaseModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.turretBaseModels);
                else if (p.miscType().equals(Property.MiscType.turretModel))
                    models = RegistryModelTank.toModelArray(Game.registryModelTank.turretModels);

                String selected = f.get().toString();
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
                        Object old = f.get();
                        ((Pointer<IModel>) f).set(finalModels[t.selectedOption]);
                        validateChangedProperty(f, p, old);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
                t.models = models;

                return t;
            }
            else if (p.miscType().equals(Property.MiscType.baseModel) || p.miscType().equals(Property.MiscType.colorModel) || p.miscType().equals(Property.MiscType.turretBaseModel) || p.miscType().equals(Property.MiscType.turretModel))
            {
                ArrayList<TankModels.TankSkin> skins = null;
                IModel m;

                if (p.miscType().equals(Property.MiscType.baseModel))
                {
                    skins = Game.registryModelTank.getBaseSkins();
                    m = TankModels.skinnedTankModel.base;
                }
                else if (p.miscType().equals(Property.MiscType.colorModel))
                {
                    skins = Game.registryModelTank.getColorSkins();
                    m = TankModels.skinnedTankModel.color;
                }
                else if (p.miscType().equals(Property.MiscType.turretBaseModel))
                {
                    skins = Game.registryModelTank.getTurretBaseSkins();
                    m = TankModels.skinnedTankModel.turretBase;
                }
                else
                {
                    skins = Game.registryModelTank.getTurretSkins();
                    m = TankModels.skinnedTankModel.turret;
                }

                String selected = ((TankModels.TankSkin)(f.get())).name;
                int selIndex = 0;
                IModel[] previews = new IModel[skins.size()];
                final TankModels.TankSkin[] finalSkins = new TankModels.TankSkin[skins.size()];
                String[] skinNames = new String[finalSkins.length];

                for (int i = 0; i < skins.size(); i++)
                {
                    skinNames[i] = skins.get(i).name;
                    if (skins.get(i).name.equals(selected))
                        selIndex = i;

                    previews[i] = Drawing.drawing.createModel(m.toString());
                    if (p.miscType().equals(Property.MiscType.baseModel))
                    {
                        skins = Game.registryModelTank.getBaseSkins();
                        previews[i].setSkin(skins.get(i).base);
                        finalSkins[i] = skins.get(i);
                    }
                    else if (p.miscType().equals(Property.MiscType.colorModel))
                    {
                        skins = Game.registryModelTank.getColorSkins();
                        previews[i].setSkin(skins.get(i).color);
                        finalSkins[i] = skins.get(i);
                    }
                    else if (p.miscType().equals(Property.MiscType.turretBaseModel))
                    {
                        skins = Game.registryModelTank.getTurretBaseSkins();
                        previews[i].setSkin(skins.get(i).turretBase);
                        finalSkins[i] = skins.get(i);
                    }
                    else
                    {
                        skins = Game.registryModelTank.getTurretSkins();
                        previews[i].setSkin(skins.get(i).turret);
                        finalSkins[i] = skins.get(i);
                    }
                }

                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), skinNames, () -> {}, "");
                t.selectedOption = selIndex;

                t.function = () ->
                {
                    try
                    {
                        Object old = f.get();
                        ((Pointer<TankModels.TankSkin>) f).set(finalSkins[t.selectedOption]);
                        validateChangedProperty(f, p, old);
                    }
                    catch (Exception ex)
                    {
                        Game.exitToCrash(ex);
                    }
                };

                t.enableHover = !p.desc().equals("");
                t.hoverText = formatDescription(p.desc());
                t.models = previews;

                return t;
            }
            else if (f.getType().equals(String.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () -> {}, f.get() + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (p.miscType() == Property.MiscType.name && this.prevScreen instanceof IRenamableScreen)
                        {
                            if (((IRenamableScreen) this.prevScreen).rename((String) f.get(), t.inputText))
                            {
                                f.cast().set(t.inputText);
                                ArrayList<ITrigger> oldEls = new ArrayList<>(this.currentTab.uiElements);
                                this.resetTabs();
                                this.currentTab.uiElements = oldEls;
                            }
                            else
                            {
                                this.message = "That name is already in use, please pick another one";
                                t.inputText = (String) f.get();
                            }
                        }
                        else
                        {
                            Object old = f.get();
                            f.cast().set(t.inputText);
                            validateChangedProperty(f, p, old);
                        }
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p.desc());
                t.enableHover = !p.desc().equals("");

                if (p.miscType() == Property.MiscType.complexString)
                {
                    t.allowAll = true;
                }
                else
                {
                    t.lowerCase = true;
                    t.allowSpaces = true;
                    t.enableSpaces = false;
                }

                return t;
            }
            else if (p.miscType() == Property.MiscType.music)
            {
                HashSet<String> a = ((HashSet<String>) f.get());
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
                    a.clear();
                    for (int i = 0; i < musicsArray.length; i++)
                    {
                        if (selectedMusicsArray[i])
                            a.add(musicsArray[i]);
                    }
                }, this);

                boolean[] old = s.selectedOptions;
                s.selectedOptions = selectedMusicsArray;
                validateChangedProperty(f, p, old);

                return s;
            }
            else if (p.miscType() == Property.MiscType.spawnedTanks)
            {
                SelectorDrawable s = new SelectorDrawable(0, 0, 350, 40, p.name());
                ArrayList<TankAIControlled.SpawnedTankEntry> a = ((ArrayList<TankAIControlled.SpawnedTankEntry>) f.get());

                s.function = () ->
                {
                    this.resetLayout();
                    ScreenSelectorArraylist sc = new ScreenSelectorArraylist(this, "Edit " + p.name().toLowerCase());
                    Game.screen = sc;

                    ArrayList<ScreenSelectorArraylist.Entry> entries = new ArrayList<>();
                    for (TankAIControlled.SpawnedTankEntry e: a)
                    {
                        entries.add(getSpawnedTankEntry(e, sc));
                    }

                    a.clear();
                    s.multiTanks.clear();
                    s.tank = null;
                    s.optionText = Translation.translate("\u00A7127000000255none");
                    sc.setContent(entries,
                            () -> getSpawnedTankEntry(new TankAIControlled.SpawnedTankEntry(new TankReference("dummy"), 1), sc),
                            (entry) ->
                            {
                                SelectorDrawable sel = ((SelectorDrawable) entry.element1);
                                a.add(new TankAIControlled.SpawnedTankEntry((ITankField) sel.value, Double.parseDouble(((TextBox) entry.element2).inputText)));
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
                    s.multiTanks.add(e.tank.resolve());
                    s.tank = e.tank.resolve();
                    s.value = e.tank;
                    if (s.multiTanks.size() == 1)
                        s.optionText = e.tank.getName();
                    else
                        s.optionText = "";
                }

                s.enableHover = !p.desc().equals("");
                s.hoverText = formatDescription(p.desc());

                return s;
            }

        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        return new Button(0, 0, 350, 40, p.name(), "This option is not available yet");
    }

    public void validateChangedProperty(Pointer<?> f, Property p, Object oldValue)
    {

    }

    public SelectorDrawable getTanksONSelector(Pointer<ITanksONEditable> p, String name, String desc)
    {
        SelectorDrawable b = new SelectorDrawable(0, 0, 350, 40, name, () -> {});
        b.function = () ->
        {
            this.resetLayout();

            Screen s = null;
            if (Bullet.class.isAssignableFrom(p.getType()))
                s = new ScreenEditorBullet(p.cast(), Game.screen);
            else if (Mine.class.isAssignableFrom(p.getType()))
                s = new ScreenEditorMine(p.cast(), Game.screen);
            else if (Explosion.class.isAssignableFrom(p.getType()))
                s = new ScreenEditorExplosion(p.cast(), Game.screen);
            else if (TankPlayer.class.isAssignableFrom(p.getType()))
                s = new ScreenEditorPlayerTankBuild(p.cast(), Game.screen);
            else if (BulletEffect.class.isAssignableFrom(p.getType()))
                s = new ScreenEditorBulletEffect(p.cast(), Game.screen);
            else if (ITankField.class.isAssignableFrom(p.getType()))
            {
                if (p.get() != null && TankAIControlled.class.isAssignableFrom(p.get().getClass()))
                {
                    ScreenEditorTank ss = new ScreenEditorTank(p.cast(), Game.screen);
                    ss.objName = name.toLowerCase();
                    s = ss;
                    this.clearMusicTracks();
                }
                else
                    s = new ScreenSelectorTankReference(name.toLowerCase(), p.cast(), Game.screen);
            }
            else if (Item.ItemStack.class.isAssignableFrom(p.getType()))
            {
                ScreenEditorItem ss = new ScreenEditorItem(p.cast(), Game.screen);
                ss.showLoadFromTemplate = true;
                s = ss;
            }

            Game.screen = s;
            ((IScreenWithCompletion) s).setOnComplete(() ->
            {
                b.tank = null;

                ITanksONEditable o = p.get();
                b.value = p.get();
                if (o == null)
                    b.optionText = "\u00A7127000000255none";
                else if (ITankField.class.isAssignableFrom(p.getType()))
                {
                    b.tank = ((ITankField) p.get()).resolve();
                    b.optionText = o.getName();
                }
                else if (BulletEffect.class.isAssignableFrom(p.getType()))
                {
                    b.bulletEffect = (BulletEffect) p.get();
                    b.optionText = "";
                }
                else
                {
                    if (Item.ItemStack.class.isAssignableFrom(p.getType()))
                        b.image = ((Item.ItemStack<?>) p.get()).item.icon;

                    b.optionText = Game.formatString(o.getName());
                }
            });
        };
        b.enableHover = !desc.equals("");
        b.hoverText = formatDescription(desc);

        if (Tank.class.isAssignableFrom(p.getType()))
            b.tank = (Tank) p.get();

        ITanksONEditable o = p.get();
        if (o == null)
            b.optionText = "\u00A7127000000255none";
        else if (ITankField.class.isAssignableFrom(p.getType()))
        {
            b.tank = ((ITankField) p.get()).resolve();
            b.optionText = o.getName();
        }
        else if (BulletEffect.class.isAssignableFrom(p.getType()))
        {
            b.bulletEffect = (BulletEffect) p.get();
            b.optionText = "";
        }
        else
        {
            if (Item.ItemStack.class.isAssignableFrom(p.getType()))
            {
                b.imageXOffset = -b.sizeX / 2 + b.sizeY;
                b.imageSizeX = 40;
                b.imageSizeY = 40;
                b.image = ((Item.ItemStack<?>) p.get()).item.icon;
            }

            b.optionText = Game.formatString(o.getName());
        }

        return b;
    }

    public ScreenSelectorArraylist.Entry getSpawnedTankEntry(TankAIControlled.SpawnedTankEntry e, ScreenSelectorArraylist s)
    {
        SelectorDrawable b = null;
        try
        {
            b = getTanksONSelector(new FieldPointer<>(e, e.getClass().getField("tank")), Translation.translate("Spawned tank").toLowerCase(), "");
            b.setText("Spawned tank");
            b.tank = e.tank == null ? null : e.tank.resolve();
            b.value = e.tank;

            if (b.tank != null)
                b.optionText = b.tank.name;
            else
                b.optionText = "\u00A7127000000255none";

        }
        catch (NoSuchFieldException ex)
        {
            Game.exitToCrash(ex);
        }

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

        return new ScreenSelectorArraylist.Entry(b, t, s);
    }

    public void sortTopLevelTabs()
    {
        int i = 0;
        int j = 0;

        double pos = 120;
        if (topLevelMenus.size() >= 5)
            pos = 90;

        double size = this.objWidth;
        double space = this.objXSpace;

        if (topLevelMenus.size() >= 4)
        {
            size = 280;
            space = 300;
        }

        this.topLevelButtons.clear();
        for (Tab t: topLevelMenus)
        {
            Button b = new Button(space * (i - Math.min(topLevelMenus.size() - 1, 3) / 2.0) + this.centerX, pos + this.objYSpace * j, size, 40, t.name, () -> setTab(t));
            b.image = this.iconPrefix + "/" + t.name.toLowerCase().replace(" ", "_") + ".png";
            b.drawImageShadow = true;
            b.imageSizeX = 50;
            b.imageSizeY = 50;
            b.imageXOffset = -105;

            this.addTabButton(t, b);
            t.sortUIElements();

            i++;

            if (i == 4)
            {
                i = 0;
                j++;
            }
        }
    }

    public void addTabButton(Tab t, Button b)
    {
        this.topLevelButtons.add(b);
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

        this.updateMusic();

        if (this.message != null)
            this.dismissMessage.update();
        else
        {
            if (this.target.get() != null)
            {
                if (this.topLevelMenus.size() > 1 || this.forceDisplayTabs)
                {
                    for (Button b : this.topLevelButtons)
                    {
                        b.enabled = currentTab == null || !currentTab.getRoot().name.equals(b.text);
                        b.update();
                    }
                }
            }

            if (this.currentTab != null)
                this.currentTab.update();

            if (this.target.get() == null)
                this.create.update();
            else if (this.target.nullable)
                this.delete.update();

            this.quit.update();

            this.updateOverlay();

            if (Game.game.input.editorPause.isValid())
            {
                Game.game.input.editorPause.invalidate();
                this.quit.function.run();
            }
        }
    }

    public void updateOverlay()
    {

    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        create.setText("Create new %s", (Object) objName);

        if (showDeleteObj)
            delete.setText(deleteText + " %s", (Object) objName);
        else
            delete.setText(deleteText);

        if (Game.screen != this && !(Game.screen instanceof ScreenEditorItem))
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
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 60, this.message, this.messageParams);
        }
        else
        {
            if (this.target.get() != null)
            {
                if (this.currentTab != null)
                    this.currentTab.draw();

                Drawing.drawing.setInterfaceFontSize(this.titleSize);
                if (Level.isDark())
                    Drawing.drawing.setColor(255, 255, 255);
                else
                    Drawing.drawing.setColor(0, 0, 0);

                if (this.currentTab != null)
                    Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);
            }

            double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
            double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

            Drawing.drawing.setColor(0, 0, 0, 127);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, 105, width, 210);

            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY + extraHeight / 2, width, extraHeight);
            Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 60, width, 120);

            if (this.target.get() != null)
            {
                if (this.topLevelButtons.size() > 1 || this.forceDisplayTabs)
                {
                    for (Button b : this.topLevelButtons)
                    {
                        b.enabled = currentTab == null || !currentTab.getRoot().name.equals(b.text);
                        b.draw();
                    }
                }
            }

            Drawing.drawing.setInterfaceFontSize(this.titleSize);
            Drawing.drawing.setColor(255, 255, 255);

            this.drawTitle();

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            this.quit.draw();

            if (this.target.get() == null)
            {
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "There is no %s set", this.objName);
                this.create.draw();
            }
            else if (this.target.nullable)
                this.delete.draw();

            this.drawOverlay();

            if (this.target.get() != null)
            {
                if (this.currentTab != null)
                    this.currentTab.drawOver();
            }
        }
    }

    public void drawTitle()
    {
        double pos = 60;
        if ((topLevelButtons.size() <= 1 && !this.forceDisplayTabs) || this.target.get() == null)
            pos = 100;
        else if (topLevelButtons.size() >= 5)
            pos = 30;
        Drawing.drawing.displayInterfaceText(this.centerX, pos, this.title, this.objName);
    }

    public void drawOverlay()
    {

    }

    @Override
    public void onAttemptClose()
    {
        this.prevScreen.onAttemptClose();
    }

    public void updateMusic()
    {
        if (!this.controlsMusic)
            return;

        if (prevScreen instanceof ScreenLevelEditorOverlay)
            ((ScreenLevelEditorOverlay) prevScreen).editor.updateMusic(false);

        this.prevMusics.clear();
        this.prevMusics.addAll(this.musics);
        this.musics.clear();
        this.addMusicTracks();

        for (String m : this.prevMusics)
        {
            if (!this.musics.contains(m))
                Drawing.drawing.removeSyncedMusic(m, 500);
        }

        for (String m : this.musics)
        {
            if (!this.prevMusics.contains(m))
                Drawing.drawing.addSyncedMusic(m, Game.musicVolume * 0.5f, true, 500);
        }
    }

    public void addMusicTracks()
    {

    }

    public void clearMusicTracks()
    {
        for (String m : this.musics)
        {
            Drawing.drawing.removeSyncedMusic(m, 500);
        }

        this.musics.clear();
    }

    @Override
    public void setOnComplete(Runnable r)
    {
        this.onComplete = r;
    }

    @Override
    public Runnable getOnComplete()
    {
        return this.onComplete;
    }
}

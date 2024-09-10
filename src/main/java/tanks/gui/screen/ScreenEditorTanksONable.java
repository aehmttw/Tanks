package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.bullet.Bullet;
import tanks.gui.*;
import tanks.tank.Explosion;
import tanks.tank.Mine;
import tanks.tankson.FieldPointer;
import tanks.tankson.ITanksONEditable;
import tanks.tankson.Property;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ScreenEditorTanksONable<T> extends Screen implements IBlankBackgroundScreen
{
    public Tab currentTab;
    public ArrayList<Tab> topLevelMenus = new ArrayList<>();
    public ArrayList<Tab> allTabs = new ArrayList<>();
    public ArrayList<Button> topLevelButtons = new ArrayList<>();

    public String message = null;

    public boolean drawBehindScreen;

    public String title = "Edit";
    public String objName = "thing";
    public String iconPrefix = "editor";

    public Screen prevScreen;
    public FieldPointer<T> target;

    public Field[] fields;

    public Button dismissMessage = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + Drawing.drawing.objHeight, Drawing.drawing.objWidth, Drawing.drawing.objHeight, "Ok", () -> message = null);

    public Runnable onComplete = null;

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 6.5, this.objWidth, this.objHeight, "Done", () ->
    {
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

    public ScreenEditorTanksONable(FieldPointer<T> t, Screen screen)
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
                if (p != null && p.category().equals(this.category))
                {
                    this.uiElements.add(screen.getUIElementForField(f, p, screen.target));
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

            this.drawUIElements();

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
        }
    }

    public String[] formatDescription(Property p)
    {
        ArrayList<String> text = Drawing.drawing.wrapText(p.desc(), 300, 12);
        String[] s = new String[text.size()];
        return text.toArray(s);
    }

    public ITrigger getUIElementForField(Field f, Property p, FieldPointer<?> tp)
    {
        try
        {
            Object target = tp.get();
            if (f.getType().equals(int.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () ->
                {
                }, f.get(target) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get(target) + "";
                        else
                            f.set(target, (int) Double.parseDouble(t.inputText));

                        t.inputText = f.get(target).toString();
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                t.hoverText = formatDescription(p);
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
                }, f.get(target) + "", "");
                t.function = () ->
                {
                    try
                    {
                        if (t.inputText.length() == 0)
                            t.inputText = f.get(target) + "";
                        else
                            f.set(target, Double.parseDouble(t.inputText));

                        t.inputText = f.get(target).toString();
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            t.inputText = f.get(target) + "";
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
                t.allowLetters = false;
                t.allowSpaces = false;
                t.minValue = p.minValue();
                t.maxValue = p.maxValue();
                t.checkMinValue = true;
                t.checkMaxValue = true;
                t.allowNegatives = t.minValue < 0;

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
                t.selectedOption = sounds.indexOf(f.get(target));
                t.sounds = new String[sounds.size()];
                sounds.toArray(t.sounds);

                t.function = () ->
                {
                    try
                    {
                        f.set(target, sounds.get(t.selectedOption));
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
            else if (p.miscType() == Property.MiscType.itemIcon)
            {
                ArrayList<String> icons = new ArrayList<>(Arrays.asList("item.png", "bullet_normal.png", "bullet_mini.png", "bullet_large.png", "bullet_fire.png", "bullet_fire_trail.png", "bullet_dark_fire.png", "bullet_flame.png",
                        "bullet_laser.png", "bullet_healing.png", "bullet_electric.png", "bullet_freeze.png", "bullet_arc.png", "bullet_explosive.png", "bullet_boost.png", "bullet_air.png", "bullet_homing.png",
                        "mine.png",
                        "shield.png", "shield_gold.png"));
                String[] iconsArray = new String[icons.size()];
                icons.toArray(iconsArray);

                SelectorImage t = new SelectorImage(0, 0, this.objWidth, this.objHeight, p.name(), iconsArray, () -> {});
                t.drawImages = true;
                t.selectedOption = icons.indexOf(f.get(target));

                t.function = () ->
                {
                    try
                    {
                        f.set(target, icons.get(t.selectedOption));
                    }
                    catch (IllegalAccessException e)
                    {
                        Game.exitToCrash(e);
                    }
                };

                return t;
            }
            else if (f.getType().equals(String.class))
            {
                TextBox t = new TextBox(0, 0, this.objWidth, this.objHeight, p.name(), () ->
                {
                }, f.get(target) + "", "");
                t.function = () ->
                {
                    try
                    {
                        f.set(target, t.inputText);
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
                SelectorDrawable t = new SelectorDrawable(0, 0, this.objWidth, this.objHeight, p.name(), () ->
                {
                }, "");

                t.function = () ->
                {
                    try
                    {
                        f.set(target, !(boolean) f.get(target));
                        t.optionText = (boolean) f.get(target) ? "Yes" : "No";
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                };
                t.optionText = (boolean) f.get(target) ? "Yes" : "No";
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

                Selector t = new Selector(0, 0, this.objWidth, this.objHeight, p.name(), options, () ->
                {
                }, "");
                t.selectedOption = ((Enum<?>) f.get(target)).ordinal();

                t.function = () ->
                {
                    try
                    {
                        f.set(target, values[t.selectedOption]);
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
            else if (ITanksONEditable.class.isAssignableFrom(f.getType()))
            {
                SelectorDrawable b = new SelectorDrawable(0, 0, 350, 40, p.name(), () -> {});
                b.function = () ->
                {
                    this.resetLayout();

                    ScreenEditorTanksONable<?> s = null;
                    FieldPointer<?> fp = new FieldPointer<>(target, f);
                    if (f.getType().isAssignableFrom(Bullet.class))
                        s = new ScreenEditorBullet((FieldPointer<Bullet>) fp, Game.screen);
                    else if (f.getType().isAssignableFrom(Mine.class))
                        s = new ScreenEditorMine((FieldPointer<Mine>) fp, Game.screen);
                    else if (f.getType().isAssignableFrom(Explosion.class))
                        s = new ScreenEditorExplosion((FieldPointer<Explosion>) fp, Game.screen);

                    Game.screen = s;
                    s.onComplete = () ->
                    {
                        try
                        {
                            ITanksONEditable o = (ITanksONEditable) f.get(this.target.get());
                            if (o == null)
                                b.optionText = "\u00A7127000000255none";
                            else
                                b.optionText = Game.formatString(o.getName());
                        }
                        catch (IllegalAccessException e)
                        {
                            throw new RuntimeException(e);
                        }
                    };
                };
                b.enableHover = !p.desc().equals("");
                b.hoverText = formatDescription(p);

                ITanksONEditable o = (ITanksONEditable) f.get(this.target.get());
                if (o == null)
                    b.optionText = "\u00A7127000000255none";
                else
                    b.optionText = Game.formatString(o.getName());

                return b;
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

        double pos = 120;
        if (topLevelButtons.size() >= 5)
            pos = 90;

        this.topLevelButtons.clear();
        for (Tab t: topLevelMenus)
        {
            Button b = new Button(300 * (i - 1.5) + this.centerX, pos + this.objYSpace * j, 280, 40, t.name, () -> setTab(t));
            b.image = this.iconPrefix + "/" + t.name.toLowerCase().replace(" ", "_") + ".png";
            b.drawImageShadow = true;
            b.imageSizeX = 50;
            b.imageSizeY = 50;
            b.imageXOffset = -105;

            this.topLevelButtons.add(b);
            t.sortUIElements();

            i++;

            if (i == 4)
            {
                i = 0;
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

        if (this.message != null)
            this.dismissMessage.update();
        else
        {
            if (this.topLevelMenus.size() > 1)
            {
                for (Button b : this.topLevelButtons)
                {
                    b.enabled = currentTab == null || !currentTab.getRoot().name.equals(b.text);
                    b.update();
                }
            }

            if (this.target.get() == null)
                this.create.update();
            else if (this.target.nullable)
                this.delete.update();

            if (this.currentTab != null)
                this.currentTab.update();

            this.quit.update();

            if (Game.game.input.editorPause.isValid())
            {
                Game.game.input.editorPause.invalidate();
                this.quit.function.run();
            }

        }
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        create.setText("Create new %s", (Object) objName);
        delete.setText("Remove %s", (Object) objName);

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

            double pos = 60;
            if (topLevelButtons.size() <= 1)
                pos = 100;
            else if (topLevelButtons.size() >= 5)
                pos = 30;

            Drawing.drawing.displayInterfaceText(this.centerX, pos, this.title, this.objName);

            if (Level.isDark())
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            if (this.target.get() != null)
            {
                if (this.currentTab != null)
                    Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 200, this.currentTab.name);

                if (this.topLevelButtons.size() > 1)
                {
                    for (Button b : this.topLevelButtons)
                    {
                        b.draw();
                    }
                }

                if (this.currentTab != null)
                    this.currentTab.draw();
            }

            this.quit.draw();

            if (this.target.get() == null)
            {
                Drawing.drawing.setInterfaceFontSize(this.textSize);
                Drawing.drawing.displayInterfaceText(this.centerX, this.centerY, "There is no %s set", this.objName);
                this.create.draw();
            }
            else if (this.target.nullable)
                this.delete.draw();
        }
    }

    @Override
    public void onAttemptClose()
    {
        this.prevScreen.onAttemptClose();
    }
}

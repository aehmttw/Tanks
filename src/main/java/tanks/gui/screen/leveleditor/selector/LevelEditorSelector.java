package tanks.gui.screen.leveleditor.selector;

import tanks.Consumer;
import tanks.GameObject;
import tanks.gui.screen.leveleditor.EditorButtons.EditorButton;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;
import tanks.obstacle.Obstacle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A selector that is added to <code>GameObject</code>s.
 * <p>
 * It controls what is displayed in the left and right next to the "Ok" button in the object menu,
 * as well as the bottom right buttons in the editor.
 * <p>
 * These selectors must be registered, via {@link GameObject#registerSelector(LevelEditorSelector[]) GameObject.registerSelector}.
 *
 * @param <T> the type of <code>GameObject</code> (<code>Tank</code>, <code>Obstacle</code>, etc.)
 *            that the selector is applied to.
 */
public abstract class LevelEditorSelector<T extends GameObject>
{
    public static ArrayList<Consumer<GameObject>> addSelFuncRegistry = new ArrayList<>();

    public Position position;
    public Position shortcutPos = Position.editor_bottom_right;
    public ScreenLevelEditor editor;
    public OverlayObjectMenu objectMenu;

    public GameObject gameObject;
    public Class<? extends GameObject> objCls;
    public String property, objectProperty;
    public Field propField, objPropField;
    public Object prevObject = null;

    public String id = "";
    public String title = "";
    public String description = null;

    /** The result of {@link #getButton()} is stored in this variable. */
    public Button button;
    public String buttonText = "Sample button";
    public String image = null;

    public boolean init = false;
    public boolean modified = false;
    protected boolean updated = false;

    /** The result of {@link #addShortcutButton()} is stored in this variable.*/
    public EditorButton shortcutButton;
    public InputBindingGroup keybind = null;

    /**
     * Registers the function in the <code>func</code> parameter to be called
     * whenever a <code>GameObject</code> is instantiated and its selectors are added.
     */
    @SuppressWarnings("unused")
    public static void onAddSelector(Consumer<GameObject> func)
    {
        addSelFuncRegistry.add(func);
    }

    public void init()
    {

    }

    public abstract void onSelect();

    /**
     * The button to display in the selector's specified position.
     */
    public Button getButton()
    {
        Button b = new Button(0, 0, editor.objWidth, editor.objHeight, buttonText, this::onSelect);

        b.imageXOffset = -155;
        b.imageSizeX = 30;
        b.imageSizeY = 30;
        b.image = image;

        return b;
    }

    public void addShortcutButton()
    {
        shortcutButton = editor.addedShortcutButtons.get(id);
        ArrayList<EditorButton> pos = getLocation(shortcutPos);

        if (shortcutButton != null)
            pos.remove(shortcutButton);

        shortcutButton = new EditorButton(pos, image.replace("icons/", ""),
                50, 50, this::onShortcut, () -> false, this::gameObjectSelected, description, keybind);
        editor.addedShortcutButtons.put(id, shortcutButton);

        editor.buttons.refreshButtons();

        if (position != Position.object_menu_left && position != Position.object_menu_right)
            button = shortcutButton;
    }

    public void onShortcut()
    {
        editor.paused = true;
        onSelect();
    }

    public void update()
    {
        if (!this.init)
            return;

        updated = true;

        try
        {
            Object sel = getPropertyBase();
            Object obj = getObjectProp();

            if (sel instanceof Number)      // nice one java
                sel = ((Number) sel).doubleValue();
            if (obj instanceof Number)
                obj = ((Number) obj).doubleValue();

            if (!Objects.equals(sel, prevObject))
            {
                if (objPropField.getType() == int.class && sel instanceof Double)
                    objPropField.setInt(gameObject, ((Double) sel).intValue());
                else if (!objPropField.getType().isPrimitive())
                    objPropField.set(gameObject, objPropField.getType().cast(sel));
                else
                    objPropField.set(gameObject, sel);

                gameObject.onPropertySet(this);
                prevObject = sel;
            }
            else if (!Objects.equals(sel, obj))
            {
                modified = true;
                setProperty(obj);
                prevObject = obj;
                onPropertySet();
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public void onPropertySet() {}

    public String getMetadata()
    {
        try
        {
            return propField.get(this).toString();
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public abstract void setMetadata(String data);

    /**
     * The <code>add</code> parameter takes two values:<br>
     * -1: If the editor's prev. meta keybind was pressed or if Shift+RMB was pressed.<br>
     * 1: If the editor's next meta keybind was pressed or if RMB was pressed.
     */
    protected abstract void changeMetadata(int add);

    public void changeMeta(int add)
    {
        changeMetadata(add);
        update();
        ScreenLevelEditor.selectors.put(this.id, this);
    }

    public void load() {}

    /** Sets this selector's metadata to <code>cloneFrom</code>'s metadata. */
    public void cloneProperties(LevelEditorSelector<T> cloneFrom)
    {
        if (this == cloneFrom)
            return;     // weirdest bug ever

        if (cloneFrom.editor != null)
            this.editor = cloneFrom.editor;

        if (cloneFrom.objectMenu != null)
            this.objectMenu = cloneFrom.objectMenu;

        if (!this.init)
            this.baseInit();

        if (editor != null)
            this.button = getButton();
        this.modified = true;

        cloneFrom.update();
        this.setMetadata(cloneFrom.getMetadata());
        this.update();
    }

    public boolean modified()
    {
        if (!updated)
            update();

        return modified;
    }

    public void baseInit()
    {
        if (this.init)
            return;

        this.init = true;

        this.init();

        if (!this.image.startsWith("icons/"))
            this.image = "icons/" + this.image;

        if (description == null)
        {
            description = title;

            if (keybind != null)
                description += " (" + keybind.getInputs() + ")";
        }

        this.objCls = gameObject.getClass();
    }

    public void setProperty(Object o)
    {
        try
        {
            propField.set(this, o);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object getProperty()
    {
        try
        {
            return propField.get(this);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object getPropertyBase()
    {
        if (initPropFields()) return null;
        return getProperty();
    }

    private boolean initPropFields()
    {
        if (property == null || objectProperty == null)
        {
            System.err.println("Warning: Neither property or objectProperty should be null.");
            return true;
        }

        if (propField == null)
        {
            try
            {
                this.propField = this.getClass().getField(property);
                this.objPropField = this.objCls.getField(objectProperty);
            }
            catch (NoSuchFieldException e)
            {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public Object getObjectProp()
    {
        if (initPropFields()) return null;

        try
        {
            return this.objPropField.get(gameObject);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean gameObjectSelected()
    {
        if (editor.currentMode != ScreenLevelEditor.EditorMode.build)   // no buildMode variable
            return false;

        if (gameObject instanceof Obstacle)
            return ScreenLevelEditor.currentPlaceable == ScreenLevelEditor.Placeable.obstacle && editor.mouseObstacle.getSelector(this.id) != null;
        return ScreenLevelEditor.currentPlaceable != ScreenLevelEditor.Placeable.obstacle && editor.mouseTank.getSelector(this.id) != null;
    }

    public ArrayList<EditorButton> getLocation(Position p)
    {
        if (p == Position.editor_top_left)
            return editor.buttons.topLeft;

        if (p == Position.editor_top_right)
            return editor.buttons.topRight;

        if (p == Position.editor_bottom_left)
            return editor.buttons.bottomLeft;

        if (p == Position.editor_bottom_right)
            return editor.buttons.bottomRight;

        return null;
    }

    public void copyBase()
    {
        this.prevObject = getObjectProp();
        setProperty(prevObject);
    }

    public enum Position
    {editor_top_left, editor_top_right, editor_bottom_left, editor_bottom_right, object_menu_left, object_menu_right}
}

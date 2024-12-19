package tanks.gui.screen.leveleditor.selector;

import tanks.BiConsumer;
import tanks.Function;
import tanks.Game;
import tanks.GameObject;
import tanks.gui.ButtonList;
import tanks.gui.screen.leveleditor.OverlaySelectChoice;
import tanks.gui.screen.leveleditor.ScreenLevelEditor;

import java.lang.reflect.Field;
import java.util.ArrayList;

/** @param <V> The class of the object being selected. (e.g. {@code Team} for team selectors) */
public class SelectorChoice<V> extends MetadataSelector
{
    public ArrayList<V> choices = new ArrayList<>();
    public V selectedChoice;
    public int selectedIndex;
    public ButtonList buttonList;
    public boolean addNoneChoice = false;
    public Function<V, String> description = v -> null;
    public BiConsumer<ScreenLevelEditor, V> onEdit = null;

    public SelectorChoice(Field f)
    {
        super(f);
    }

    @Override
    public void openEditorOverlay(ScreenLevelEditor editor)
    {
        Game.screen = new OverlaySelectChoice<>(Game.screen, editor, this);
    }

    @Override
    public void changeMetadata(ScreenLevelEditor e, GameObject o, int add)
    {
        selectedIndex += add;
        setChoice(e, o, selectedIndex);
    }

    public String choiceToString(V choice)
    {
        return choice != null ? choice.toString() : "";
    }

    public void setChoice(ScreenLevelEditor e, GameObject o, int index)
    {
        if (addNoneChoice)
        {
            if (index < -1)
                index += choices.size() + 1;
            else if (index >= choices.size())
                index = -1;
        }
        else
            index = (index + choices.size()) % choices.size();

        if (index > -1)
            selectedChoice = choices.get(index);
        else
            selectedChoice = null;

        selectedIndex = index;
        this.setMetadata(e, o, selectedChoice);
    }
}

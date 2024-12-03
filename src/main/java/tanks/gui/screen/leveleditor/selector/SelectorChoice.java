package tanks.gui.screen.leveleditor.selector;

import tanks.Consumer;
import tanks.Function;
import tanks.Game;
import tanks.GameObject;
import tanks.gui.ButtonList;
import tanks.gui.screen.leveleditor.OverlaySelectChoice;

import java.util.ArrayList;
import java.util.Objects;

/** @param <V> The class of the object being selected. (e.g. {@code Team} for team selectors) */
public class SelectorChoice<T extends GameObject, V> extends LevelEditorSelector<T>
{
    public ArrayList<V> choices = new ArrayList<>();
    public V selectedChoice;
    public ButtonList buttonList;
    public int selectedIndex = 0;
    public boolean addNoneChoice = false;
    public Function<V, String> description = v -> null;
    public Consumer<V> onEdit = null;

    @Override
    public void baseInit()
    {
        if (this.init)
            return;

        this.id = "choice";
        this.title = "Choice Selector";
        this.property = "selectedChoice";

        super.baseInit();
    }

    @Override
    public void onSelect()
    {
        Game.screen = new OverlaySelectChoice<>(Game.screen, editor, this);
    }

    @Override
    public String getMetadata()
    {
        return choiceToString(selectedChoice);
    }

    @Override
    public void setMetadata(String data)
    {
        if (addNoneChoice && data.equals(choiceToString(null)))
        {
            setChoice(-1);
            return;
        }

        int i = 0;
        for (V choice : choices)
        {
            if (data.equals(choiceToString(choice)))
                setChoice(i);
            i++;
        }
    }

    @Override
    public void onPropertySet()
    {
        int i = 0;
        for (V choice : choices)
        {
            if (Objects.equals(selectedChoice, choice))
                setChoice(i);
            i++;
        }
    }

    @Override
    public void changeMetadata(int add)
    {
        selectedIndex += add;
        setChoice(selectedIndex);
    }

    @Override
    public void load()
    {
        this.button.setText(this.buttonText, choiceToString(selectedChoice));
    }

    public String choiceToString(V choice)
    {
        return choice != null ? choice.toString() : "";
    }

    public void setChoice(int index)
    {
        setChoice(index, true);
    }

    public void setChoice(int index, boolean modify)
    {
        if (modify && selectedIndex != index)
            modified = true;

        if (addNoneChoice)
        {
            if (index < -1)
                index += choices.size() + 1;
            else if (index >= choices.size())
                index = -1;
        }
        else
            index = (index + choices.size()) % choices.size();

        selectedIndex = index;

        if (index > -1)
            selectedChoice = choices.get(index);
        else
            selectedChoice = null;
    }
}

package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.Game;
import tanks.editor.selector.ChoiceSelector;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlaySelectChoice<V> extends ScreenLevelEditorOverlay
{
    public ChoiceSelector<?, ?> selector;
    public ArrayList<Button> choiceButtons = new ArrayList<>();
    public Button hoveredButton = null;
    public V hoveredChoice;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape
    );

    public Button edit;

    public OverlaySelectChoice(Screen previous, ScreenLevelEditor screenLevelEditor, ChoiceSelector<?, V> selector)
    {
        super(previous, screenLevelEditor);

        this.selector = selector;
        edit = new Button(0, 0, 32, 32, "", () -> selector.onEdit.accept(hoveredChoice), "Edit");
        edit.fullInfo = true;
        edit.image = "icons/pencil.png";
        edit.imageSizeX = edit.imageSizeY = 20;

        int i = 0;
        for (V b : selector.choices)
        {
            final int j = i;
            choiceButtons.add(new Button(0, 0, 350, 40, selector.choiceToString(b),
                    () -> selector.setChoice(j), selector.description.apply(b)));
            i++;
        }

        if (selector.addNoneChoice)
            choiceButtons.add(new Button(0, 0, 350, 40, "\u00A7127000000255none",
                    () -> selector.setChoice(-1), selector.description.apply(null)));

        selector.buttonList = new ButtonList(choiceButtons, 0, 0, -30);
    }

    public void update()
    {
        for (Button b : choiceButtons)
            b.enabled = true;

        if (selector.onEdit != null)
        {
            int i = 0;
            for (Button b : choiceButtons)
            {
                if (b.selected && i < choiceButtons.size() - 1)
                {
                    hoveredButton = b;
                    hoveredChoice = (V) selector.choices.get(i);
                    edit.posX = b.posX + b.sizeX / 2 - edit.sizeX / 2 - 10;
                    edit.posY = b.posY;
                    edit.update();
                    break;
                }
                i++;
            }
        }

        if (selector.selectedChoice != null)
            choiceButtons.get(selector.selectedIndex).enabled = false;
        else
            choiceButtons.get(choiceButtons.size() - 1).enabled = false;

        selector.buttonList.update();
        this.back.update();

        super.update();
    }

    public void draw()
    {
        super.draw();

        Drawing.drawing.setColor(0, 0, 0, 127);
        Drawing.drawing.drawPopup(centerX, centerY + 15,1200, 750, 20, 5);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, selector.title);

        selector.buttonList.draw();

        if (hoveredButton != null)
            edit.draw();

        back.draw();
    }
}

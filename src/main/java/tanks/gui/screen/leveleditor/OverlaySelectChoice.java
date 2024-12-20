package tanks.gui.screen.leveleditor;

import tanks.Drawing;
import tanks.gui.screen.leveleditor.selector.SelectorChoice;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.screen.Screen;

import java.util.ArrayList;

public class OverlaySelectChoice<V> extends ScreenLevelEditorOverlay
{
    public SelectorChoice<?, ?> selector;
    public ArrayList<Button> choiceButtons = new ArrayList<>();
    public Button hoveredButton = null;
    public V hoveredChoice;

    public Button back = new Button(this.centerX, this.centerY + 300, 350, 40, "Done", this::escape);

    public Button edit;
    public int editSelected = -1;

    public OverlaySelectChoice(Screen previous, ScreenLevelEditor screenLevelEditor, SelectorChoice<?, V> selector)
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
        selector.buttonList.manualDarkMode = true;

        if (selector.addNoneChoice)
            selector.buttonList.fixedLastElements = 1;
    }

    public void update()
    {
        for (Button b : choiceButtons)
            b.enabled = true;

        hoveredButton = null;
        if (selector.onEdit != null && !this.selector.buttonList.reorder)
        {
            int i = 0;

            editSelected = -1;
            int page = this.selector.buttonList.page;
            int count = this.selector.buttonList.rows * this.selector.buttonList.columns;
            for (Button b : choiceButtons)
            {
                if ((b.selected || (i >= page * count && i < (page + 1) * count && i == selector.selectedIndex)) && i < choiceButtons.size() - 1)
                {
                    hoveredButton = b;
                    hoveredChoice = (V) selector.choices.get(i);
                    edit.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                    edit.posY = b.posY;
                    edit.update();
                    if (edit.selected)
                        editSelected = i;
                }
                i++;
            }
        }

        if (selector.choice() != null)
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
        Drawing.drawing.drawPopup(this.centerX, this.centerY, 1200, 720);
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 270, selector.title);

        selector.buttonList.draw();

        if (!this.selector.buttonList.reorder)
        {
            int i = 0;

            int page = this.selector.buttonList.page;
            int count = this.selector.buttonList.rows * this.selector.buttonList.columns;
            for (Button b : choiceButtons)
            {
                if ((b.selected || (i >= page * count && i < (page + 1) * count && i == selector.selectedIndex)) && i < choiceButtons.size() - 1)
                {
                    hoveredButton = b;
                    hoveredChoice = (V) selector.choices.get(i);
                    edit.posX = b.posX + b.sizeX / 2 - b.sizeY / 2;
                    edit.posY = b.posY;
                    edit.selected = editSelected == i;
                    edit.infoSelected = edit.selected;
                    edit.draw();
                }
                i++;
            }
        }

        back.draw();
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Level;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.SelectorColor;
import tanks.gui.SelectorItemIcon;

import java.util.ArrayList;

public class ScreenOverlayIconColors
{
    public SelectorItemIcon selector;
    public ScreenSelector selectorScreen;
    public double offset;

    public ButtonList colorButtons;

    public SelectorColor colorSelector;

    public int selectedLayer = 0;

    public ScreenOverlayIconColors(ScreenSelector s, SelectorItemIcon sel, double offset)
    {
        this.selector = sel;
        this.selectorScreen = s;
        this.offset = offset;
        this.refreshButtons();
    }

    public void refreshButtons()
    {
        ArrayList<Button> colorButtons = new ArrayList<>();

        Button b2 = null;
        Button b1 = new Button(0, 0, 45, 45, "", () ->
        {
            this.selectedLayer = 0;
            this.colorSelector = null;
        });
        b1.imageSizeX = 45;
        b1.imageSizeY = 45;
        b1.itemIcon = selector.selectedIcon;
        colorButtons.add(b1);

        int c1 = 1;
        if (selector.selectedIcon.colors != null)
        {
            for (int i = 0; i < selector.selectedIcon.colors.size(); i++)
            {
                if (selector.selectedIcon.alphas.get(i) || selector.selectedIcon.colors.get(i).alpha >= 255)
                {
                    int finalI = i;
                    int c2 = c1;
                    Button b = new Button(0, 0, 45, 45, "", () ->
                    {
                        this.colorSelector = new SelectorColor(selectorScreen.centerX + selectorScreen.objXSpace / 2, selectorScreen.centerY - selectorScreen.objYSpace * 3, selectorScreen.objWidth, selectorScreen.objHeight, "Layer",
                                selectorScreen.objYSpace * 1.5, selector.selectedIcon.colors.get(finalI), selector.selectedIcon.alphas.get(finalI));
                        this.selectedLayer = c2;
                    });
                    b.imageSizeX = 45;
                    b.imageSizeY = 45;
                    b.image = selector.selectedIcon.baseName + "_" + (i + 1) + ".png";
                    colorButtons.add(b);
                    c1++;
                }
            }

            b2 = new Button(0, 0, 45, 45, "", () ->
            {
                selector.selectedIcon.resetColors();
                colorSelector.updateColors();
            }, "Reset colors to default");
            b2.imageSizeX = 45;
            b2.imageSizeY = 45;
            b2.fullInfo = true;
            b2.image = "reset_colors.png";
            colorButtons.add(b2);
        }

        this.colorButtons = new ButtonList(colorButtons, 0, 0, this.offset + this.selectorScreen.objYSpace * 0.5);
        this.colorButtons.setRowsAndColumns(1, 15);
        this.colorButtons.buttonHeight *= 1.5;
        this.colorButtons.buttonWidth = this.colorButtons.buttonHeight;
        this.colorButtons.buttonXSpace = this.colorButtons.buttonWidth + 30;
        this.colorButtons.hideText = true;
        this.colorButtons.sortButtons();

        if (b2 != null)
        {
            b1.posX -= 20;
            b2.posX += 20;
        }

        if (selector.selectedIcon.colors != null)
        {
            int c = 1;
            for (int i = 0; i < selector.selectedIcon.colors.size(); i++)
            {
                if (selector.selectedIcon.alphas.get(i) || selector.selectedIcon.colors.get(i).alpha >= 255)
                {
                    colorButtons.get(c).imageColor = selector.selectedIcon.colors.get(i);
                    c++;
                }
            }
        }
    }

    public void update()
    {
        this.colorButtons.update();

        for (int i = 0; i < this.colorButtons.buttons.size(); i++)
        {
            this.colorButtons.buttons.get(i).enabled = i != selectedLayer;
        }

        if (this.colorSelector != null)
            this.colorSelector.update();
    }

    public void draw()
    {
        double x = this.selectorScreen.centerX;
        double y = this.selectorScreen.centerY + offset;
        Drawing.drawing.setInterfaceFontSize(this.selectorScreen.textSize);

        if (Level.isDark() || Panel.darkness > 64)
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.displayInterfaceText(x, y - 10, "Adjust icon colors");
        this.colorButtons.draw();

        if (this.colorSelector != null)
        {
            this.colorSelector.draw();

            Drawing.drawing.setInterfaceFontSize(this.selectorScreen.titleSize);

            if (Level.isDark() || Panel.darkness > 64)
                Drawing.drawing.setColor(255, 255, 255);
            else
                Drawing.drawing.setColor(0, 0, 0);

            Drawing.drawing.displayInterfaceText(this.selectorScreen.centerX, this.selectorScreen.centerY - this.selectorScreen.objYSpace * 4.5, "Select icon layer color");

            Drawing.drawing.setColor(this.colorButtons.buttons.get(this.selectedLayer).imageColor);
            Drawing.drawing.drawInterfaceImage(this.colorButtons.buttons.get(this.selectedLayer).image,
                    selectorScreen.centerX - selectorScreen.objXSpace / 2, this.selectorScreen.centerY - selectorScreen.objYSpace * 2.5,
                    128, 128);

            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawInterfaceImage(this.selector.selectedIcon,
                    selectorScreen.centerX - selectorScreen.objXSpace / 2, this.selectorScreen.centerY + selectorScreen.objYSpace * 0.5,
                    128, 128);
        }

        Button first = this.colorButtons.buttons.get(0);
        Button last = this.colorButtons.buttons.get(this.colorButtons.buttons.size() - 1);

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.selectorScreen.textSize / 2);
        Drawing.drawing.displayInterfaceText(first.posX, first.posY - first.sizeY * 0.70, "Change icon");

        if (this.colorButtons.buttons.size() > 1)
        {
            Drawing.drawing.displayInterfaceText(last.posX, first.posY - first.sizeY * 0.70, "Reset colors");
            Drawing.drawing.displayInterfaceText(this.selectorScreen.centerX, first.posY - first.sizeY * 0.70, "Icon layers");

            Drawing.drawing.setColor(0, 0, 0, 64);
            Drawing.drawing.fillRect((first.posX + this.colorButtons.buttons.get(1).posX) / 2,
                    first.posY, 2, first.sizeY);

            Drawing.drawing.fillRect((last.posX + this.colorButtons.buttons.get(this.colorButtons.buttons.size() - 2).posX) / 2,
                    first.posY, 2, first.sizeY);



        }
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;

public class ScreenTestShapes extends Screen
{
    public double length = 200;
    public double width = 200;
    public double borderRadius = 20;
    public double borderWidth = 50;

    public ScreenTestShapes()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenTestDebug());

    TextBoxSlider shapeLength = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Length", new Runnable()
    {
        @Override
        public void run()
        {
            length = shapeLength.value;
        }
    }, 200, 20, this.objXSpace * 0.95, 10);

    TextBoxSlider shapeWidth = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Width", new Runnable()
    {
        @Override
        public void run()
        {
            width = shapeWidth.value;
        }
    }, 200, 20, 200, 10);

    TextBoxSlider radius = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Border Radius", new Runnable()
    {
        @Override
        public void run()
        {
            borderRadius = radius.value;
        }
    }, 20, 0, 100, 2);

    TextBoxSlider bWidth = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Border Width", new Runnable()
    {
        @Override
        public void run()
        {
            borderWidth = bWidth.value;
        }
    }, 50, 1, 200, 2);

    @Override
    public void update()
    {
        shapeLength.update();
        shapeWidth.update();
        radius.update();
        bWidth.update();
        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setColor(50, 50, 50, 128);
        Drawing.drawing.drawRect(this.centerX - this.objXSpace / 2 - 10, this.centerY - this.objYSpace * 2 + 20, length, width, borderWidth, borderRadius);
        Drawing.drawing.fillRoundedRect(this.centerX + this.objXSpace / 2 + 10, this.centerY - this.objYSpace * 2 + 20, length, width, borderRadius);
        Drawing.drawing.setColor(255, 0, 0, 128);

        double size = Math.min(Math.min(10, length - 15), Math.min(10, width - 15));
        Drawing.drawing.fillOval(this.centerX - this.objXSpace / 2 - 10, this.centerY - this.objYSpace * 2 + 20, size, size);
        Drawing.drawing.fillOval(this.centerX + this.objXSpace / 2 + 10, this.centerY - this.objYSpace * 2 + 20, size, size);
        Drawing.drawing.fillOval(this.centerX - 45, this.centerY + this.objYSpace / 2, 30, 30);

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5.5, "Test shapes");
        Drawing.drawing.setInterfaceFontSize(18);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5 + 10, "The shape is aligned with the tile grid, so");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5 + 35, "turn 3D Ground off to test alignment.");

        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.displayUncenteredInterfaceText(this.centerX - 20, this.centerY + this.objYSpace / 2 - 10,"= Center");

        shapeLength.draw();
        shapeWidth.draw();
        radius.draw();
        bWidth.draw();
        back.draw();
    }
}

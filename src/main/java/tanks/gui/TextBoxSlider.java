package tanks.gui;

import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;

public class TextBoxSlider extends TextBox
{
    public double value;
    public double min;
    public double max;
    public double interval;
    public boolean integer = true;

    public double r1 = 255;
    public double g1 = 255;
    public double b1 = 255;

    public double r2 = 255;
    public double g2 = 255;
    public double b2 = 255;

    public InputPoint slidePoint = null;
    public boolean sliding = false;
    public boolean sliderHover = false;

    public TextBoxSlider(double x, double y, double sX, double sY, String text, Runnable f, double defaultText, double min, double max, double interval)
    {
        super(x, y, sX, sY, text, f, defaultText + "");
        this.value = defaultText;
        this.min = min;
        this.max = max;
        this.interval = interval;

        if (this.inputText.endsWith(".0"))
            this.inputText = this.inputText.substring(0, this.inputText.length() - 2);

        this.allowLetters = false;
        this.allowSpaces = false;
        this.maxValue = max;
        this.minValue = min;
        this.checkMaxValue = true;
    }

    @Override
    public void draw()
    {
        Drawing drawing = Drawing.drawing;

        drawing.setInterfaceFontSize(this.sizeY * 0.6);

        if (Game.glowEnabled)
            drawTallGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);

        drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
        drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - sizeY * 3 / 4, sizeX - sizeY, sizeY);
        drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);
        drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);

        drawing.fillInterfaceRect(posX, posY - 15, sizeX, sizeY * 3 / 4);

        double m = 0.8;

        double start1 = posX - sizeX / 2 + sizeY / 2;
        double end2 = posX + sizeX / 2 - sizeY / 2;

        double end1 = start1 * 0.25 + end2 * 0.75 - sizeY / 2;
        double start2 = start1 * 0.25 + end2 * 0.75 + sizeY / 2;

        double mid1 = (start1 + end1) / 2;
        double mid2 = (start2 + end2) / 2;

        double frac = Math.max(Math.min((this.value - this.min) / (this.max - this.min), 1), 0);
        double x = start1 * (1 - frac) + end1 * frac;

        if (Game.glowEnabled)
        {
            if (selected)
                Button.drawGlow(mid2, this.posY + 3.5, end2 - start2 - this.sizeY * (-m), this.sizeY * m, 0.55, 0, 0, 0, 160, false);
            else if (hover && !Game.game.window.touchscreen)
                Button.drawGlow(mid2, this.posY + 5, end2 - start2 - this.sizeY * (-m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
            else
                Button.drawGlow(mid2, this.posY + 5, end2 - start2 - this.sizeY * (-m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

            if (sliding)
                Button.drawGlow(mid1, this.posY + 3.5, end1 - start1 - this.sizeY * (-m), this.sizeY * m, 0.55, 0, 0, 0, 160, false);
            else if (sliderHover)
                Button.drawGlow(mid1, this.posY + 5, end1 - start1 - this.sizeY * (-m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
            else
                Button.drawGlow(mid1, this.posY + 5, end1 - start1 - this.sizeY * (-m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

            if (this.lastFrame == Panel.panel.ageFrames - 1 && !Game.game.window.drawingShadow)
            {
                for (Effect e : this.glowEffects)
                {
                    e.drawGlow();
                    e.draw();
                }
            }
        }

        drawing.setColor(r1, g1, b1);
        drawing.fillInterfaceOval(start1, posY, sizeY * m, sizeY * m);
        drawing.setColor(r2, g2, b2);
        drawing.fillInterfaceOval(end1, posY, sizeY * m, sizeY * m);

        Game.game.window.shapeRenderer.setBatchMode(true, true, false, false);
        drawing.setColor(r1, g1, b1);
        Drawing.drawing.addInterfaceVertex(start1, posY - sizeY * m / 2, 0);
        Drawing.drawing.addInterfaceVertex(start1, posY + sizeY * m / 2, 0);

        drawing.setColor(r2, g2, b2);
        Drawing.drawing.addInterfaceVertex(end1, posY + sizeY * m / 2, 0);
        Drawing.drawing.addInterfaceVertex(end1, posY - sizeY * m / 2, 0);
        Game.game.window.shapeRenderer.setBatchMode(false, true, false, false);

        if (this.min < this.max)
        {
            if (Game.glowEnabled)
                Button.drawGlow(x, this.posY + 2.5, this.sizeY * m, this.sizeY * m, 0.6, 0, 0, 0, 100, false);

            if (Game.glowEnabled)
                drawing.setColor((this.bgColorR + this.colorR) / 2, (this.bgColorG + this.colorG) / 2, (this.bgColorB + this.colorB) / 2);
            else
                drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);

            drawing.fillInterfaceOval(x, this.posY, this.sizeY * m * m, this.sizeY * m * m);
            //drawing.setColor(this.colorR, this.colorG, this.colorB);

            if (Game.glowEnabled)
                Button.drawGlow(x, this.posY + 1.5, this.sizeY * m * m, this.sizeY * m * m, 0.6, 0, 0, 0, 100, false);

            drawing.setColor(this.r1 * (1 - frac) + this.r2 * frac, this.g1 * (1 - frac) + this.g2 * frac, this.b1 * (1 - frac) + this.b2 * frac);

            drawing.fillInterfaceOval(x, this.posY, this.sizeY * m * m * m, this.sizeY * m * m * m);
        }

        if (selected)
        {
            if (this.inputText.length() >= this.maxChars)
                drawing.setColor(this.selectedFullColorR, this.selectedFullColorG, this.selectedFullColorB);
            else
                drawing.setColor(this.selectedColorR, this.selectedColorG, this.selectedColorB);
        }
        else if (hover && !Game.game.window.touchscreen)
            drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
        else
            drawing.setColor(this.colorR, this.colorG, this.colorB);

        drawing.fillInterfaceOval(start2, posY, sizeY * m, sizeY * m);
        drawing.fillInterfaceOval(end2, posY, sizeY * m, sizeY * m);
        drawing.fillInterfaceRect(mid2, posY, end2 - start2, sizeY * m);

        drawing.setColor(0, 0, 0);

        drawing.drawInterfaceText(posX, posY - sizeY * 13 / 16, translatedLabelText);

        this.drawInput();
    }

    public void drawInput()
    {
        double start1 = posX - sizeX / 2 + sizeY / 2;
        double end2 = posX + sizeX / 2 - sizeY / 2;
        double start2 = start1 * 0.25 + end2 * 0.75 + sizeY / 2;
        double mid2 = (start2 + end2) / 2;

        if (selected)
            Drawing.drawing.drawInterfaceText(mid2, posY, inputText + "\u00a7127127127255_");
        else
            Drawing.drawing.drawInterfaceText(mid2, posY, inputText);
    }

    public boolean checkMouse(double mx, double my, boolean down, boolean valid, InputPoint p)
    {
        boolean handled = false;

        if (Game.game.window.touchscreen)
        {
            sizeX += 20;
            sizeY += 20;
        }

        double start1 = posX - sizeX / 2 + sizeY / 2;
        double end2 = posX + sizeX / 2 - sizeY / 2;

        this.hover = mx > start1 * 0.25 + end2 * 0.75 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;

        if (hover && valid && enabled)
        {
            if (!selected)
            {
                handled = true;
                selected = true;
                this.previousInputText = this.inputText;

                TextBox prev = Panel.selectedTextBox;

                Panel.selectedTextBox = this;

                if (prev != null)
                    prev.submit();

                Drawing.drawing.playVibration("click");
                Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
                Game.game.window.getRawTextKeys().clear();
            }
        }

        this.sliderHover = mx > posX - sizeX / 2 && mx <= start1 * 0.25 + end2 * 0.75 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;

        if (this.sliding || (sliderHover && valid))
        {
            if (!this.sliding)
            {
                if (Panel.selectedTextBox != null)
                    Panel.selectedTextBox.submit();

                Drawing.drawing.playVibration("click");
                Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
                Game.game.window.getRawTextKeys().clear();
            }

            this.slidePoint = p;
            this.sliding = true;
            handled = true;

            double end1 = start1 * 0.25 + end2 * 0.75 - sizeY / 2;
            this.value = (int) (Math.min(1, Math.max(0, mx - start1) / (end1 - start1)) * (max - min) / interval) * interval + min;

            if (integer)
                this.inputText = (int) this.value + "";
            else
                this.inputText = this.value + "";
        }

        if (Game.game.window.touchscreen)
        {
            sizeX -= 20;
            sizeY -= 20;
        }

        if (!down && this.sliding)
        {
            this.submit();
            this.sliding = false;
        }

        return handled;
    }

    public void update()
    {
        super.update();

        if (this.slidePoint != null && !Game.game.window.touchPoints.containsValue(this.slidePoint))
        {
            this.sliding = false;
            this.slidePoint = null;
            this.submit();
        }
    }

    public void submit()
    {
        this.value = Double.parseDouble(this.inputText);
        super.submit();
    }

    public boolean shouldAddEffect()
    {
        return (this.hover || this.sliderHover) && !this.selected && !this.sliding && this.enabled && !Game.game.window.touchscreen;
    }
}

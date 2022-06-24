package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

public class ScreenOverlayTankColorPicker extends Screen
{
    public TankPlayer preview = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, 0);

    public Tank tank;
    public boolean enableSecondaryColor;
    
    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;

    public TextBoxSlider colorRed2;
    public TextBoxSlider colorGreen2;
    public TextBoxSlider colorBlue2;

    public String secondaryColorText = "Second color: ";

    Button enableSecondary = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            enableSecondaryColor = !enableSecondaryColor;

            if (enableSecondaryColor)
                enableSecondary.setText(secondaryColorText, ScreenOptions.onText);
            else
                enableSecondary.setText(secondaryColorText, ScreenOptions.offText);
        }
    },
            "Allows you to pick---a custom secondary color");


    public ScreenOverlayTankColorPicker(Tank t, double yOffset)
    {
        this.tank = t;
        this.enableSecondaryColor = (int) t.secondaryColorR != (int) Turret.calculateSecondaryColor(t.colorR) || (int) t.secondaryColorG != (int) Turret.calculateSecondaryColor(t.colorG) || (int) t.secondaryColorB != (int) Turret.calculateSecondaryColor(t.colorB);
        this.enableSecondary.posY += yOffset;

        if (Drawing.drawing.interfaceScaleZoom > 1)
            preview.posY += Game.tile_size;

        if (enableSecondaryColor)
            enableSecondary.setText(secondaryColorText, ScreenOptions.onText);
        else
            enableSecondary.setText(secondaryColorText, ScreenOptions.offText);

        colorRed = new TextBoxSlider(this.centerX - this.objXSpace / 2, yOffset + this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Primary red", () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            this.tank.colorR = Integer.parseInt(colorRed.inputText);
        }
                , this.tank.colorR, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX - this.objXSpace / 2, yOffset + this.centerY, this.objWidth, this.objHeight, "Primary green", () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            this.tank.colorG = Integer.parseInt(colorGreen.inputText);
        }
                , this.tank.colorG, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX - this.objXSpace / 2, yOffset + this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Primary blue", () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            this.tank.colorB = Integer.parseInt(colorBlue.inputText);
        }
                , this.tank.colorB, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;

        colorRed2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, yOffset + this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Secondary red", () ->
        {
            if (colorRed2.inputText.length() <= 0)
                colorRed2.inputText = colorRed2.previousInputText;

            tank.secondaryColorR = Integer.parseInt(colorRed2.inputText);
        }
                , tank.secondaryColorR, 0, 255, 1);

        colorRed2.allowLetters = false;
        colorRed2.allowSpaces = false;
        colorRed2.maxChars = 3;
        colorRed2.maxValue = 255;
        colorRed2.checkMaxValue = true;
        colorRed2.integer = true;

        colorGreen2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, yOffset + this.centerY + 0, this.objWidth, this.objHeight, "Secondary green", () ->
        {
            if (colorGreen2.inputText.length() <= 0)
                colorGreen2.inputText = colorGreen2.previousInputText;

            this.tank.secondaryColorG = Integer.parseInt(colorGreen2.inputText);
        }
                , this.tank.secondaryColorG, 0, 255, 1);

        colorGreen2.allowLetters = false;
        colorGreen2.allowSpaces = false;
        colorGreen2.maxChars = 3;
        colorGreen2.maxValue = 255;
        colorGreen2.checkMaxValue = true;
        colorGreen2.integer = true;

        colorBlue2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, yOffset + this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Secondary blue", () ->
        {
            if (colorBlue2.inputText.length() <= 0)
                colorBlue2.inputText = colorBlue2.previousInputText;

            this.tank.secondaryColorB = Integer.parseInt(colorBlue2.inputText);
        }
                , this.tank.secondaryColorB, 0, 255, 1);

        colorBlue2.allowLetters = false;
        colorBlue2.allowSpaces = false;
        colorBlue2.maxChars = 3;
        colorBlue2.maxValue = 255;
        colorBlue2.checkMaxValue = true;
        colorBlue2.integer = true;

        this.preview.baseModel = this.tank.baseModel;
        this.preview.colorModel = this.tank.colorModel;
        this.preview.turretBaseModel = this.tank.turretBaseModel;
        this.preview.turretModel = this.tank.turretModel;
        this.preview.size = this.tank.size;
        this.preview.turretSize = this.tank.turretSize;
        this.preview.turretLength = this.tank.turretLength;
        this.preview.emblem = this.tank.emblem;
        this.preview.bullet = this.tank.bullet;

        if (this.preview.size > Game.tile_size * 1.5)
            this.preview.size = Game.tile_size * 1.5;

        this.preview.size *= 1.5 * Drawing.drawing.interfaceScaleZoom;
        this.preview.invulnerable = true;
        this.preview.drawAge = 50;
        this.preview.depthTest = false;

        this.setupButtons(true);

        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.preview.posX -= 480;
            this.preview.posY -= 20;
        }
        else
        {
            this.colorRed.posY -= this.objYSpace / 2;
            this.colorGreen.posY -= this.objYSpace / 2;
            this.colorBlue.posY -= this.objYSpace / 2;
            this.colorRed2.posY -= this.objYSpace / 2;
            this.colorGreen2.posY -= this.objYSpace / 2;
            this.colorBlue2.posY -= this.objYSpace / 2;
            this.enableSecondary.posY -= this.objYSpace / 2;
        }
    }

    @Override
    public void update()
    {
        colorRed.update();
        colorGreen.update();
        colorBlue.update();

        this.setupButtons(false);

        enableSecondary.update();

        preview.colorR = colorRed.value;
        preview.colorG = colorGreen.value;
        preview.colorB = colorBlue.value;

        if (!enableSecondaryColor)
        {
            preview.secondaryColorR = Turret.calculateSecondaryColor(colorRed.value);
            preview.secondaryColorG = Turret.calculateSecondaryColor(colorGreen.value);
            preview.secondaryColorB = Turret.calculateSecondaryColor(colorBlue.value);
        }
        else
        {
            preview.secondaryColorR = colorRed2.value;
            preview.secondaryColorG = colorGreen2.value;
            preview.secondaryColorB = colorBlue2.value;
        }
    }

    public void setupButtons(boolean initial)
    {
        if (this.enableSecondaryColor)
        {
            if (!initial)
            {
                colorRed2.update();
                colorGreen2.update();
                colorBlue2.update();
            }

            colorRed.posX = this.centerX - this.objXSpace / 2;
            colorGreen.posX = this.centerX - this.objXSpace / 2;
            colorBlue.posX = this.centerX - this.objXSpace / 2;
        }
        else
        {
            colorRed.posX = this.centerX;
            colorGreen.posX = this.centerX;
            colorBlue.posX = this.centerX;

            this.tank.secondaryColorR = (int) Turret.calculateSecondaryColor(this.tank.colorR);
            this.tank.secondaryColorG = (int) Turret.calculateSecondaryColor(this.tank.colorG);
            this.tank.secondaryColorB = (int) Turret.calculateSecondaryColor(this.tank.colorB);

            colorRed2.inputText = (int) this.tank.secondaryColorR + "";
            colorGreen2.inputText = (int) this.tank.secondaryColorG + "";
            colorBlue2.inputText = (int) this.tank.secondaryColorB + "";
            colorRed2.value = (int) this.tank.secondaryColorR;
            colorGreen2.value = (int) this.tank.secondaryColorG;
            colorBlue2.value = (int) this.tank.secondaryColorB;
        }

        colorRed.r1 = 0;
        colorRed.r2 = 255;
        colorRed.g1 = colorGreen.value;
        colorRed.g2 = colorGreen.value;
        colorRed.b1 = colorBlue.value;
        colorRed.b2 = colorBlue.value;

        colorGreen.r1 = colorRed.value;
        colorGreen.r2 = colorRed.value;
        colorGreen.g1 = 0;
        colorGreen.g2 = 255;
        colorGreen.b1 = colorBlue.value;
        colorGreen.b2 = colorBlue.value;

        colorBlue.r1 = colorRed.value;
        colorBlue.r2 = colorRed.value;
        colorBlue.g1 = colorGreen.value;
        colorBlue.g2 = colorGreen.value;
        colorBlue.b1 = 0;
        colorBlue.b2 = 255;

        colorRed2.r1 = 0;
        colorRed2.r2 = 255;
        colorRed2.g1 = colorGreen2.value;
        colorRed2.g2 = colorGreen2.value;
        colorRed2.b1 = colorBlue2.value;
        colorRed2.b2 = colorBlue2.value;

        colorGreen2.r1 = colorRed2.value;
        colorGreen2.r2 = colorRed2.value;
        colorGreen2.g1 = 0;
        colorGreen2.g2 = 255;
        colorGreen2.b1 = colorBlue2.value;
        colorGreen2.b2 = colorBlue2.value;

        colorBlue2.r1 = colorRed2.value;
        colorBlue2.r2 = colorRed2.value;
        colorBlue2.g1 = colorGreen2.value;
        colorBlue2.g2 = colorGreen2.value;
        colorBlue2.b1 = 0;
        colorBlue2.b2 = 255;

    }

    @Override
    public void draw()
    {
        colorBlue.draw();
        colorGreen.draw();
        colorRed.draw();

        if (this.enableSecondaryColor)
        {
            colorBlue2.draw();
            colorGreen2.draw();
            colorRed2.draw();
        }

        enableSecondary.draw();

        preview.draw();
    }
}

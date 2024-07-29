package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBoxSlider;
import tanks.network.event.EventSendTankColors;
import tanks.network.event.EventUpdateTankColors;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

public class ScreenOptionsPlayerColor extends Screen
{
    TankPlayer preview = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, 0);

    public TextBoxSlider colorRed;
    public TextBoxSlider colorGreen;
    public TextBoxSlider colorBlue;

    public TextBoxSlider colorRed2;
    public TextBoxSlider colorGreen2;
    public TextBoxSlider colorBlue2;

    public TextBoxSlider colorRed3;
    public TextBoxSlider colorGreen3;
    public TextBoxSlider colorBlue3;

    protected boolean updateColorForParty = false;

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
            Game.screen = new ScreenOptions();
        else
            Game.screen = new ScreenOptionsPersonalize();
    }
    );

    Button addColor = new Button(this.centerX + this.objXSpace * 0.3, this.centerY + this.objYSpace * 2.75, this.objHeight * 1.5, this.objHeight * 1.5, "+", new Runnable()
    {
        @Override
        public void run()
        {
            if (!Game.player.enableSecondaryColor)
                Game.player.enableSecondaryColor = true;
            else
                Game.player.enableTertiaryColor = true;

            updateColorForParty = true;
        }
    });

    Button removeColor = new Button(this.centerX - this.objXSpace * 0.3, this.centerY + this.objYSpace * 2.75, this.objHeight * 1.5, this.objHeight * 1.5, "-", new Runnable()
    {
        @Override
        public void run()
        {
            if (!Game.player.enableTertiaryColor)
                Game.player.enableSecondaryColor = false;
            else
                Game.player.enableTertiaryColor = false;

            updateColorForParty = true;
        }
    });

    Button resetColor = new Button(0, 0, this.objHeight * 1.5, this.objHeight * 1.5, "", new Runnable()
    {
        @Override
        public void run()
        {
            colorRed.value = 0;
            colorGreen.value = 150;
            colorBlue.value = 255;
            colorRed.inputText = (int) colorRed.value + "";
            colorGreen.inputText = (int) colorGreen.value + "";
            colorBlue.inputText = (int) colorBlue.value + "";
            Game.player.colorR = (int) colorRed.value;
            Game.player.colorG = (int) colorGreen.value;
            Game.player.colorB = (int) colorBlue.value;

            updateColorForParty = true;
        }
    }, "Reset to default colors");

    public void updateColorParty()
    {
        if (ScreenPartyHost.isServer)
            Game.eventsOut.add(new EventUpdateTankColors(Game.player));
        else if (ScreenPartyLobby.isClient)
            Game.eventsOut.add(new EventSendTankColors(Game.player));
    }

    public ScreenOptionsPlayerColor()
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
            preview.posY += Game.tile_size;

        resetColor.image = "icon.png";
        resetColor.imageSizeX = resetColor.sizeX * 0.6;
        resetColor.imageSizeY = resetColor.sizeY * 0.6;
        resetColor.imageXOffset = resetColor.sizeX * 0.08;
        resetColor.imageYOffset = 0;
        resetColor.fullInfo = true;

        this.addColor1();
        this.addColor2();
        this.addColor3();

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
            this.colorRed3.posY -= this.objYSpace / 2;
            this.colorGreen3.posY -= this.objYSpace / 2;
            this.colorBlue3.posY -= this.objYSpace / 2;
            this.addColor.posY -= this.objYSpace / 2;
            this.removeColor.posY -= this.objYSpace / 2;
        }

        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    public void addColor1()
    {
        colorRed = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Primary red", () ->
        {
            if (colorRed.inputText.length() <= 0)
                colorRed.inputText = colorRed.previousInputText;

            Game.player.colorR = Integer.parseInt(colorRed.inputText);
            updateColorParty();
        }
                , Game.player.colorR, 0, 255, 1);

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;
        colorRed.integer = true;

        colorGreen = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Primary green", () ->
        {
            if (colorGreen.inputText.length() <= 0)
                colorGreen.inputText = colorGreen.previousInputText;

            Game.player.colorG = Integer.parseInt(colorGreen.inputText);
            updateColorParty();
        }
                , Game.player.colorG, 0, 255, 1);

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;
        colorGreen.integer = true;

        colorBlue = new TextBoxSlider(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Primary blue", () ->
        {
            if (colorBlue.inputText.length() <= 0)
                colorBlue.inputText = colorBlue.previousInputText;

            Game.player.colorB = Integer.parseInt(colorBlue.inputText);
            updateColorParty();
        }
                , Game.player.colorB, 0, 255, 1);

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;
        colorBlue.integer = true;
    }

    public void addColor2()
    {
        colorRed2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Secondary red", () ->
        {
            if (colorRed2.inputText.length() <= 0)
                colorRed2.inputText = colorRed2.previousInputText;

            Game.player.colorR2 = Integer.parseInt(colorRed2.inputText);
            updateColorParty();
        }
                , Game.player.colorR2, 0, 255, 1);

        colorRed2.allowLetters = false;
        colorRed2.allowSpaces = false;
        colorRed2.maxChars = 3;
        colorRed2.maxValue = 255;
        colorRed2.checkMaxValue = true;
        colorRed2.integer = true;

        colorGreen2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY + 0, this.objWidth, this.objHeight, "Secondary green", () ->
        {
            if (colorGreen2.inputText.length() <= 0)
                colorGreen2.inputText = colorGreen2.previousInputText;

            Game.player.colorG2 = Integer.parseInt(colorGreen2.inputText);
            updateColorParty();
        }
                , Game.player.colorG2, 0, 255, 1);

        colorGreen2.allowLetters = false;
        colorGreen2.allowSpaces = false;
        colorGreen2.maxChars = 3;
        colorGreen2.maxValue = 255;
        colorGreen2.checkMaxValue = true;
        colorGreen2.integer = true;

        colorBlue2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Secondary blue", () ->
        {
            if (colorBlue2.inputText.length() <= 0)
                colorBlue2.inputText = colorBlue2.previousInputText;

            Game.player.colorB2 = Integer.parseInt(colorBlue2.inputText);
            updateColorParty();
        }
                , Game.player.colorB2, 0, 255, 1);

        colorBlue2.allowLetters = false;
        colorBlue2.allowSpaces = false;
        colorBlue2.maxChars = 3;
        colorBlue2.maxValue = 255;
        colorBlue2.checkMaxValue = true;
        colorBlue2.integer = true;
    }

    public void addColor3()
    {
        colorRed3 = new TextBoxSlider(this.centerX + this.objXSpace, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Tertiary red", () ->
        {
            if (colorRed3.inputText.length() <= 0)
                colorRed3.inputText = colorRed3.previousInputText;

            Game.player.colorR3 = Integer.parseInt(colorRed3.inputText);
            updateColorParty();
        }
                , Game.player.colorR3, 0, 255, 1);

        colorRed3.allowLetters = false;
        colorRed3.allowSpaces = false;
        colorRed3.maxChars = 3;
        colorRed3.maxValue = 255;
        colorRed3.checkMaxValue = true;
        colorRed3.integer = true;

        colorGreen3 = new TextBoxSlider(this.centerX + this.objXSpace, this.centerY + 0, this.objWidth, this.objHeight, "Tertiary green", () ->
        {
            if (colorGreen3.inputText.length() <= 0)
                colorGreen3.inputText = colorGreen3.previousInputText;

            Game.player.colorG3 = Integer.parseInt(colorGreen3.inputText);
            updateColorParty();
        }
                , Game.player.colorG3, 0, 255, 1);

        colorGreen3.allowLetters = false;
        colorGreen3.allowSpaces = false;
        colorGreen3.maxChars = 3;
        colorGreen3.maxValue = 255;
        colorGreen3.checkMaxValue = true;
        colorGreen3.integer = true;

        colorBlue3 = new TextBoxSlider(this.centerX + this.objXSpace, this.centerY + this.objYSpace * 1.5, this.objWidth, this.objHeight, "Tertiary blue", () ->
        {
            if (colorBlue3.inputText.length() <= 0)
                colorBlue3.inputText = colorBlue3.previousInputText;

            Game.player.colorB3 = Integer.parseInt(colorBlue3.inputText);
            updateColorParty();
        }
                , Game.player.colorB3, 0, 255, 1);

        colorBlue3.allowLetters = false;
        colorBlue3.allowSpaces = false;
        colorBlue3.maxChars = 3;
        colorBlue3.maxValue = 255;
        colorBlue3.checkMaxValue = true;
        colorBlue3.integer = true;
    }

    @Override
    public void update()
    {
        addColor.update();

        if (!Game.player.enableSecondaryColor && !(Game.player.colorR == 0 && Game.player.colorG == 150 && Game.player.colorB == 255))
            resetColor.update();
        else
            removeColor.update();

        this.setupButtons(false);

        back.update();

        addColor.enabled = !Game.player.enableTertiaryColor;
        removeColor.enabled = Game.player.enableSecondaryColor;
        resetColor.posX = removeColor.posX;
        resetColor.posY = removeColor.posY;

        preview.colorR = colorRed.value;
        preview.colorG = colorGreen.value;
        preview.colorB = colorBlue.value;

        if (!Game.player.enableSecondaryColor)
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

        preview.enableTertiaryColor = Game.player.enableTertiaryColor;
        if (!Game.player.enableTertiaryColor)
        {
            preview.tertiaryColorR = (colorRed.value + colorRed2.value) / 2;
            preview.tertiaryColorG = (colorGreen.value + colorGreen2.value) / 2;
            preview.tertiaryColorB = (colorBlue.value + colorBlue2.value) / 2;
        }
        else
        {
            preview.tertiaryColorR = colorRed3.value;
            preview.tertiaryColorG = colorGreen3.value;
            preview.tertiaryColorB = colorBlue3.value;
        }

        if (this.updateColorForParty)
        {
            updateColorParty();
            this.updateColorForParty = false;
        }
    }

    public void setupButtons(boolean initial)
    {
        if (Game.player.enableSecondaryColor)
        {
            if (Game.player.enableTertiaryColor)
            {
                colorRed.posX = this.centerX - this.objXSpace;
                colorGreen.posX = this.centerX - this.objXSpace;
                colorBlue.posX = this.centerX - this.objXSpace;
                colorRed2.posX = this.centerX;
                colorGreen2.posX = this.centerX;
                colorBlue2.posX = this.centerX;
            }
            else
            {
                colorRed.posX = this.centerX - this.objXSpace / 2;
                colorGreen.posX = this.centerX - this.objXSpace / 2;
                colorBlue.posX = this.centerX - this.objXSpace / 2;
                colorRed2.posX =  this.centerX + this.objXSpace / 2;
                colorGreen2.posX = this.centerX + this.objXSpace / 2;
                colorBlue2.posX = this.centerX + this.objXSpace / 2;
            }
        }
        else
        {
            colorRed.posX = this.centerX;
            colorGreen.posX = this.centerX;
            colorBlue.posX = this.centerX;
        }

        if (!Game.player.enableSecondaryColor)
        {
            Game.player.colorR2 = (int) Turret.calculateSecondaryColor(Game.player.colorR);
            Game.player.colorG2 = (int) Turret.calculateSecondaryColor(Game.player.colorG);
            Game.player.colorB2 = (int) Turret.calculateSecondaryColor(Game.player.colorB);
            colorRed2.inputText = Game.player.colorR2 + "";
            colorGreen2.inputText = Game.player.colorG2 + "";
            colorBlue2.inputText = Game.player.colorB2 + "";
            colorRed2.value = Game.player.colorR2;
            colorGreen2.value = Game.player.colorG2;
            colorBlue2.value = Game.player.colorB2;
        }
        else if (!initial)
        {
            colorRed2.update();
            colorGreen2.update();
            colorBlue2.update();
        }

        if (!Game.player.enableTertiaryColor)
        {
            Game.player.colorR3 = (Game.player.colorR + Game.player.colorR2) / 2;
            Game.player.colorG3 = (Game.player.colorG + Game.player.colorG2) / 2;
            Game.player.colorB3 = (Game.player.colorB + Game.player.colorB2) / 2;
            colorRed3.inputText = Game.player.colorR3 + "";
            colorGreen3.inputText = Game.player.colorG3 + "";
            colorBlue3.inputText = Game.player.colorB3 + "";
            colorRed3.value = Game.player.colorR3;
            colorGreen3.value = Game.player.colorG3;
            colorBlue3.value = Game.player.colorB3;
        }
        else if (!initial)
        {
            colorRed3.update();
            colorGreen3.update();
            colorBlue3.update();
        }

        if (!initial)
        {
            colorRed.update();
            colorGreen.update();
            colorBlue.update();
        }

        Game.player.colorR = (int) colorRed.value;
        Game.player.colorG = (int) colorGreen.value;
        Game.player.colorB = (int) colorBlue.value;

        Game.player.colorR2 = (int) colorRed2.value;
        Game.player.colorG2 = (int) colorGreen2.value;
        Game.player.colorB2 = (int) colorBlue2.value;

        Game.player.colorR3 = (int) colorRed3.value;
        Game.player.colorG3 = (int) colorGreen3.value;
        Game.player.colorB3 = (int) colorBlue3.value;

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

        colorRed3.r1 = 0;
        colorRed3.r2 = 255;
        colorRed3.g1 = colorGreen3.value;
        colorRed3.g2 = colorGreen3.value;
        colorRed3.b1 = colorBlue3.value;
        colorRed3.b2 = colorBlue3.value;

        colorGreen3.r1 = colorRed3.value;
        colorGreen3.r2 = colorRed3.value;
        colorGreen3.g1 = 0;
        colorGreen3.g2 = 255;
        colorGreen3.b1 = colorBlue3.value;
        colorGreen3.b2 = colorBlue3.value;

        colorBlue3.r1 = colorRed3.value;
        colorBlue3.r2 = colorRed3.value;
        colorBlue3.g1 = colorGreen3.value;
        colorBlue3.g2 = colorGreen3.value;
        colorBlue3.b1 = 0;
        colorBlue3.b2 = 255;
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        addColor.enabled = !Game.player.enableTertiaryColor;
        removeColor.enabled = Game.player.enableSecondaryColor;
        resetColor.posX = removeColor.posX;
        resetColor.posY = removeColor.posY;

        back.draw();

        colorBlue.draw();
        colorGreen.draw();
        colorRed.draw();

        if (Game.player.enableSecondaryColor)
        {
            colorBlue2.draw();
            colorGreen2.draw();
            colorRed2.draw();
        }

        if (Game.player.enableTertiaryColor)
        {
            colorBlue3.draw();
            colorGreen3.draw();
            colorRed3.draw();
        }

        addColor.draw();
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, addColor.posY, "Colors: %d", Game.player.enableSecondaryColor ? (Game.player.enableTertiaryColor ? 3 : 2) : 1);

        if (!Game.player.enableSecondaryColor && !(Game.player.colorR == 0 && Game.player.colorG == 150 && Game.player.colorB == 255))
            resetColor.draw();
        else
            removeColor.draw();

        preview.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Tank color");
    }
}

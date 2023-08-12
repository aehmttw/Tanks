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

    public String secondaryColorText = "Second color: ";

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () ->
    {
        if (ScreenPartyHost.isServer || ScreenPartyLobby.isClient)
            Game.screen = new ScreenOptions();
        else
            Game.screen = new ScreenOptionsPersonalize();
    }
    );

    Button enableSecondary = new Button(this.centerX, this.centerY + this.objYSpace * 2.5, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.player.enableSecondaryColor = !Game.player.enableSecondaryColor;

            if (Game.player.enableSecondaryColor)
                enableSecondary.setText(secondaryColorText, ScreenOptions.onText);
            else
                enableSecondary.setText(secondaryColorText, ScreenOptions.offText);
        }
    },
            "Allows you to pick---a custom secondary color");


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

        if (Game.player.enableSecondaryColor)
            enableSecondary.setText(secondaryColorText, ScreenOptions.onText);
        else
            enableSecondary.setText(secondaryColorText, ScreenOptions.offText);

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

        colorRed2 = new TextBoxSlider(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace * 1.5, this.objWidth, this.objHeight, "Secondary red", () ->
        {
            if (colorRed2.inputText.length() <= 0)
                colorRed2.inputText = colorRed2.previousInputText;

            Game.player.turretColorR = Integer.parseInt(colorRed2.inputText);
            updateColorParty();
        }
                , Game.player.turretColorR, 0, 255, 1);

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

            Game.player.turretColorG = Integer.parseInt(colorGreen2.inputText);
            updateColorParty();
        }
                , Game.player.turretColorG, 0, 255, 1);

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

            Game.player.turretColorB = Integer.parseInt(colorBlue2.inputText);
            updateColorParty();
        }
                , Game.player.turretColorB, 0, 255, 1);

        colorBlue2.allowLetters = false;
        colorBlue2.allowSpaces = false;
        colorBlue2.maxChars = 3;
        colorBlue2.maxValue = 255;
        colorBlue2.checkMaxValue = true;
        colorBlue2.integer = true;

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

        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        colorRed.update();
        colorGreen.update();
        colorBlue.update();

        this.setupButtons(false);

        back.update();

        enableSecondary.update();

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
    }

    public void setupButtons(boolean initial)
    {
        if (Game.player.enableSecondaryColor)
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

            Game.player.turretColorR = (int) Turret.calculateSecondaryColor(Game.player.colorR);
            Game.player.turretColorG = (int) Turret.calculateSecondaryColor(Game.player.colorG);
            Game.player.turretColorB = (int) Turret.calculateSecondaryColor(Game.player.colorB);

            colorRed2.inputText = Game.player.turretColorR + "";
            colorGreen2.inputText = Game.player.turretColorG + "";
            colorBlue2.inputText = Game.player.turretColorB + "";
            colorRed2.value = Game.player.turretColorR;
            colorGreen2.value = Game.player.turretColorG;
            colorBlue2.value = Game.player.turretColorB;
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
        this.drawDefaultBackground();

        colorBlue.draw();
        colorGreen.draw();
        colorRed.draw();

        if (Game.player.enableSecondaryColor)
        {
            colorBlue2.draw();
            colorGreen2.draw();
            colorRed2.draw();
        }

        enableSecondary.draw();

        preview.draw();

        back.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Tank color");
    }
}

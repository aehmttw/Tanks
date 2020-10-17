package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

public class ScreenOptionsMultiplayerColor extends Screen
{
    TankPlayer preview = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, 0);

    public TextBox colorRed;
    public TextBox colorGreen;
    public TextBox colorBlue;

    public TextBox colorRed2;
    public TextBox colorGreen2;
    public TextBox colorBlue2;

    public String secondaryColorText = "Second color: ";

    Button back = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 240, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptionsMultiplayer();
        }
    }
    );

    Button enableSecondary = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 + 150, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.player.enableSecondaryColor = !Game.player.enableSecondaryColor;

            if (Game.player.enableSecondaryColor)
                enableSecondary.text = secondaryColorText + ScreenOptions.onText;
            else
                enableSecondary.text = secondaryColorText + ScreenOptions.offText;
        }
    },
            "Allows you to pick---a custom secondary color");


    public ScreenOptionsMultiplayerColor()
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
            preview.posY += Game.tile_size;

        if (Game.player.enableSecondaryColor)
            enableSecondary.text = secondaryColorText + ScreenOptions.onText;
        else
            enableSecondary.text = secondaryColorText + ScreenOptions.offText;

        colorRed = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Primary red", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorRed.inputText.length() <= 0)
                    colorRed.inputText = colorRed.previousInputText;

                Game.player.colorR = Integer.parseInt(colorRed.inputText);
            }

        }
                , Game.player.colorR + "");

        colorRed.allowLetters = false;
        colorRed.allowSpaces = false;
        colorRed.maxChars = 3;
        colorRed.maxValue = 255;
        colorRed.checkMaxValue = true;

        colorGreen = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 - 0, this.objWidth, this.objHeight, "Primary green", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorGreen.inputText.length() <= 0)
                    colorGreen.inputText = colorGreen.previousInputText;

                Game.player.colorG = Integer.parseInt(colorGreen.inputText);
            }

        }
                , Game.player.colorG + "");

        colorGreen.allowLetters = false;
        colorGreen.allowSpaces = false;
        colorGreen.maxChars = 3;
        colorGreen.maxValue = 255;
        colorGreen.checkMaxValue = true;

        colorBlue = new TextBox(Drawing.drawing.interfaceSizeX / 2 - 190, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Primary blue", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorBlue.inputText.length() <= 0)
                    colorBlue.inputText = colorBlue.previousInputText;

                Game.player.colorB = Integer.parseInt(colorBlue.inputText);
            }

        }
                , Game.player.colorB + "");

        colorBlue.allowLetters = false;
        colorBlue.allowSpaces = false;
        colorBlue.maxChars = 3;
        colorBlue.maxValue = 255;
        colorBlue.checkMaxValue = true;

        colorRed2 = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 - 90, this.objWidth, this.objHeight, "Secondary red", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorRed2.inputText.length() <= 0)
                    colorRed2.inputText = colorRed2.previousInputText;

                Game.player.turretColorR = Integer.parseInt(colorRed2.inputText);
            }

        }
                , Game.player.turretColorR + "");

        colorRed2.allowLetters = false;
        colorRed2.allowSpaces = false;
        colorRed2.maxChars = 3;
        colorRed2.maxValue = 255;
        colorRed2.checkMaxValue = true;

        colorGreen2 = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 0, this.objWidth, this.objHeight, "Secondary green", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorGreen2.inputText.length() <= 0)
                    colorGreen2.inputText = colorGreen2.previousInputText;

                Game.player.turretColorG = Integer.parseInt(colorGreen2.inputText);
            }

        }
                , Game.player.turretColorG + "");

        colorGreen2.allowLetters = false;
        colorGreen2.allowSpaces = false;
        colorGreen2.maxChars = 3;
        colorGreen2.maxValue = 255;
        colorGreen2.checkMaxValue = true;

        colorBlue2 = new TextBox(Drawing.drawing.interfaceSizeX / 2 + 190, Drawing.drawing.interfaceSizeY / 2 + 90, this.objWidth, this.objHeight, "Secondary blue", new Runnable()
        {
            @Override
            public void run()
            {
                if (colorBlue2.inputText.length() <= 0)
                    colorBlue2.inputText = colorBlue2.previousInputText;

                Game.player.turretColorB = Integer.parseInt(colorBlue2.inputText);
            }

        }
                , Game.player.turretColorB + "");

        colorBlue2.allowLetters = false;
        colorBlue2.allowSpaces = false;
        colorBlue2.maxChars = 3;
        colorBlue2.maxValue = 255;
        colorBlue2.checkMaxValue = true;

        this.preview.size *= 1.5 * Drawing.drawing.interfaceScaleZoom;
        this.preview.turret.length *= 1.5 * Drawing.drawing.interfaceScaleZoom;
        this.preview.invulnerable = true;
        this.preview.drawAge = 50;
        this.preview.depthTest = false;

        this.setupButtons(true);

        this.music = "tomato_feast_1_options.ogg";
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

        preview.colorR = Game.player.colorR;
        preview.colorG = Game.player.colorG;
        preview.colorB = Game.player.colorB;

        preview.turret.colorR = Game.player.turretColorR;
        preview.turret.colorG = Game.player.turretColorG;
        preview.turret.colorB = Game.player.turretColorB;
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

            colorRed.posX = Drawing.drawing.interfaceSizeX / 2 - 190;
            colorGreen.posX = Drawing.drawing.interfaceSizeX / 2 - 190;
            colorBlue.posX = Drawing.drawing.interfaceSizeX / 2 - 190;
        }
        else
        {
            colorRed.posX = Drawing.drawing.interfaceSizeX / 2;
            colorGreen.posX = Drawing.drawing.interfaceSizeX / 2;
            colorBlue.posX = Drawing.drawing.interfaceSizeX / 2;

            Game.player.turretColorR = (int) Turret.calculateSecondaryColor(Game.player.colorR);
            Game.player.turretColorG = (int) Turret.calculateSecondaryColor(Game.player.colorG);
            Game.player.turretColorB = (int) Turret.calculateSecondaryColor(Game.player.colorB);

            colorRed2.inputText = Game.player.turretColorR + "";
            colorGreen2.inputText = Game.player.turretColorG + "";
            colorBlue2.inputText = Game.player.turretColorB + "";
        }
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

        Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2 - 210, "Multiplayer tank color");
    }
}

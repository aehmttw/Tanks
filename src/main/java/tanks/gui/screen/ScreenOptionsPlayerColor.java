package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SelectorColor;
import tanks.gui.TextBoxSlider;
import tanks.network.event.EventSendTankColors;
import tanks.network.event.EventUpdateTankColors;
import tanks.tank.TankPlayer;
import tanks.tank.Turret;

public class ScreenOptionsPlayerColor extends Screen
{
    TankPlayer preview = new TankPlayer(Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2 - 315 * Drawing.drawing.interfaceScaleZoom, 0);

    public SelectorColor color1;
    public SelectorColor color2;
    public SelectorColor color3;

    protected boolean updateColorForParty = false;

    public double yPos = this.centerY - this.objYSpace * 1.75;

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
            color1.colorRed.value = 0;
            color1.colorGreen.value = 150;
            color1.colorBlue.value = 255;
            color1.colorRed.inputText = (int) color1.colorRed.value + "";
            color1.colorGreen.inputText = (int) color1.colorGreen.value + "";
            color1.colorBlue.inputText = (int) color1.colorBlue.value + "";
            Game.player.color.red = (int) color1.colorRed.value;
            Game.player.color.green = (int) color1.colorGreen.value;
            Game.player.color.blue = (int) color1.colorBlue.value;

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

        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            this.preview.posX -= 480;
            this.preview.posY -= 20;
        }
        else
        {
            this.yPos -= this.objYSpace / 2;
            this.addColor.posY -= this.objYSpace / 2;
            this.removeColor.posY -= this.objYSpace / 2;
        }

        this.addColor();

        this.preview.size *= 1.5 * Drawing.drawing.interfaceScaleZoom;
        this.preview.invulnerable = true;
        this.preview.drawAge = 50;
        this.preview.depthTest = false;

        this.setupButtons();

        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    public void addColor()
    {
        color1 = new SelectorColor(this.centerX - this.objXSpace, this.yPos, this.objWidth, this.objHeight, "Primary", this.objYSpace * 1.75, Game.player.color, false);
        color2 = new SelectorColor(this.centerX, this.yPos, this.objWidth, this.objHeight, "Secondary", this.objYSpace * 1.75, Game.player.color2, false);
        color3 = new SelectorColor(this.centerX + this.objXSpace, this.yPos, this.objWidth, this.objHeight, "Tertiary", this.objYSpace * 1.75, Game.player.color3, false);
    }

    @Override
    public void update()
    {
        addColor.update();

        if (!Game.player.enableSecondaryColor && !(Game.player.color.red == 0 && Game.player.color.green == 150 && Game.player.color.blue == 255))
            resetColor.update();
        else
            removeColor.update();

        this.setupButtons();

        back.update();

        addColor.enabled = !Game.player.enableTertiaryColor;
        removeColor.enabled = Game.player.enableSecondaryColor;
        resetColor.posX = removeColor.posX;
        resetColor.posY = removeColor.posY;

        color1.update();

        if (Game.player.enableSecondaryColor)
            color2.update();

        if (Game.player.enableTertiaryColor)
            color3.update();

        if (this.updateColorForParty)
        {
            updateColorParty();
            this.updateColorForParty = false;
        }
    }

    public void setupButtons()
    {
        if (Game.player.enableSecondaryColor)
        {
            if (Game.player.enableTertiaryColor)
            {
                color1.setPosition(this.centerX - this.objXSpace, this.yPos);
                color2.setPosition(this.centerX, this.yPos);
            }
            else
            {
                color1.setPosition(this.centerX - this.objXSpace / 2, this.yPos);
                color2.setPosition(this.centerX + this.objXSpace / 2, this.yPos);
            }
        }
        else
            color1.setPosition(this.centerX, this.yPos);

        if (!Game.player.enableSecondaryColor)
        {
            Turret.setSecondary(Game.player.color, Game.player.color2);
            color2.updateColors();
        }

        if (!Game.player.enableTertiaryColor)
        {
            Turret.setTertiary(Game.player.color, Game.player.color2, Game.player.color3);
            color3.updateColors();
        }
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

        color1.draw();

        if (Game.player.enableSecondaryColor)
            color2.draw();

        if (Game.player.enableTertiaryColor)
            color3.draw();

        addColor.draw();
        Drawing.drawing.setInterfaceFontSize(this.textSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, addColor.posY, "Colors: %d", Game.player.enableSecondaryColor ? (Game.player.enableTertiaryColor ? 3 : 2) : 1);

        if (!Game.player.enableSecondaryColor && !(Game.player.color.red == 0 && Game.player.color.green == 150 && Game.player.color.blue == 255))
            resetColor.draw();
        else
            removeColor.draw();

        preview.color.set(Game.player.color);
        preview.secondaryColor.set(Game.player.color2);
        preview.tertiaryColor.set(Game.player.color3);
        preview.enableTertiaryColor = Game.player.enableTertiaryColor;

        preview.draw();

        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Tank color");
    }
}

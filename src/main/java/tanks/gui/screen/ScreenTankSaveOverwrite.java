package tanks.gui.screen;

import basewindow.BaseFile;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.tank.TankAIControlled;

public class ScreenTankSaveOverwrite extends Screen implements IBlankBackgroundScreen
{
    public ScreenEditorTank previous;
    public TankAIControlled tank;
    public TankAIControlled oldTank;

    public ButtonObject tankButton;
    public ButtonObject oldTankButton;

    public ScreenTankSaveOverwrite(ScreenEditorTank s, TankAIControlled tank)
    {
        this.previous = s;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;
        this.tank = tank;

        try
        {
            BaseFile f = Game.game.fileManager.getFile(Game.homedir + Game.tankDir + "/" + tank.name + ".tanks");
            f.startReading();
            String t = f.nextLine();
            f.stopReading();
            this.oldTank = TankAIControlled.fromString(t);
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        this.tankButton = new ButtonObject(tank, this.centerX - 100, this.centerY, 75, 75);
        this.tankButton.setHoverTextUntranslated(tank.description + "------\u00A7000255255255New tank to be saved to template");
        this.tankButton.disabledColA = 0;
        this.tankButton.enabled = false;

        this.oldTankButton = new ButtonObject(oldTank, this.centerX + 100, this.centerY, 75, 75);
        this.oldTankButton.setHoverTextUntranslated(oldTank.description + "------\u00A7000255000255Old template to be overwritten");
        this.oldTankButton.disabledColA = 0;
        this.oldTankButton.enabled = false;
    }

    public Button replace = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Replace template", () ->
    {
        this.previous.writeTankAndShowConfirmation(tank, true);
    }
    );

    public Button quit = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Back", () ->
    {
        Game.screen = this.previous;
    }
    );

    @Override
    public void update()
    {
        this.replace.update();
        this.quit.update();

        this.tankButton.update();
        this.oldTankButton.update();
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity));
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.textSize);

        if (Level.isDark())
            Drawing.drawing.setColor(255, 255, 255);
        else
            Drawing.drawing.setColor(0, 0, 0);

        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "A tank template with this name already exists!");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Would you like to replace it?");

        Drawing.drawing.setInterfaceFontSize(this.textSize * 0.75);
        Drawing.drawing.displayInterfaceText(this.tankButton.posX, this.tankButton.posY + 50, "New template");
        Drawing.drawing.displayInterfaceText(this.oldTankButton.posX, this.oldTankButton.posY + 50, "Old template");

        Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "->");

        this.replace.draw();
        this.quit.draw();

        this.oldTankButton.draw();
        this.tankButton.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize * 3);
        Drawing.drawing.setColor(255, 0, 0);
        Drawing.drawing.drawInterfaceText(this.oldTankButton.posX, this.oldTankButton.posY - 8, "x");
    }
}

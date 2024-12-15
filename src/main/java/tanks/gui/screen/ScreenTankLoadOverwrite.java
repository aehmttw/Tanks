package tanks.gui.screen;

import tanks.BiConsumer;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.gui.Button;
import tanks.gui.ButtonObject;
import tanks.gui.screen.leveleditor.OverlayObjectMenu;
import tanks.tank.TankAIControlled;
import tanks.tankson.Pointer;

import java.util.ArrayList;

public class ScreenTankLoadOverwrite extends Screen implements IBlankBackgroundScreen
{
    public OverlayObjectMenu previous;
    public TankAIControlled tank;
    public Pointer<TankAIControlled> oldTank;
    public ArrayList<TankAIControlled> customTanks;
    public ArrayList<TankAIControlled> savedTanks;
    public BiConsumer<TankAIControlled, Pointer<TankAIControlled>> editFunc;

    public ButtonObject tankButton;
    public ButtonObject oldTankButton;

    public ScreenTankLoadOverwrite(OverlayObjectMenu ts, TankAIControlled tank, Pointer<TankAIControlled> oldTank, BiConsumer<TankAIControlled, Pointer<TankAIControlled>> editFunc, ArrayList<TankAIControlled> customTanks, ArrayList<TankAIControlled> savedTanks)
    {
        this.previous = ts;
        this.music = this.previous.music;
        this.musicID = this.previous.musicID;
        this.tank = tank;
        this.editFunc = editFunc;
        this.oldTank = oldTank;
        this.customTanks = customTanks;
        this.savedTanks = savedTanks;

        this.tankButton = new ButtonObject(tank, this.centerX - 100, this.centerY, 75, 75);
        this.tankButton.setHoverTextUntranslated(tank.description + "------\u00A7000255255255New tank to be added to level");
        this.tankButton.disabledColA = 0;
        this.tankButton.enabled = false;

        TankAIControlled oldInst = oldTank.get();
        this.oldTankButton = new ButtonObject(oldInst, this.centerX + 100, this.centerY, 75, 75);
        this.oldTankButton.setHoverTextUntranslated(oldInst + "------\u00A7000255000255Old tank already in level");
        this.oldTankButton.disabledColA = 0;
        this.oldTankButton.enabled = false;
    }

    public Button copy = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Add a copy", () ->
    {
        Game.currentLevel.customTanks = this.savedTanks;
        String name = System.currentTimeMillis() + "";
        this.previous.rename(this.tank.name, name);
        this.tank.name = name;
        Game.currentLevel.customTanks = this.customTanks;
        this.editFunc.accept(this.tank, null);
    }
    );

    public Button replace = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Replace tank", () ->
            this.editFunc.accept(this.tank, this.oldTank)
    );

    public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 3, this.objWidth, this.objHeight, "Back", () ->
            Game.screen = this.previous
    );

    @Override
    public void update()
    {
        this.copy.update();
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
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2.5, "A tank with this name already exists in the level!");
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 2, "Would you like to replace it, or add a copy?");

        Drawing.drawing.setInterfaceFontSize(this.textSize * 0.75);
        Drawing.drawing.displayInterfaceText(this.tankButton.posX, this.tankButton.posY + 50, "New tank");
        Drawing.drawing.displayInterfaceText(this.oldTankButton.posX, this.oldTankButton.posY + 50, "Old tank");

        Drawing.drawing.setInterfaceFontSize(this.textSize * 2);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "->");

        this.replace.draw();
        this.copy.draw();
        this.quit.draw();

        this.oldTankButton.draw();
        this.tankButton.draw();

        Drawing.drawing.setInterfaceFontSize(this.textSize * 3);
        Drawing.drawing.setColor(255, 0, 0);
        Drawing.drawing.drawInterfaceText(this.oldTankButton.posX, this.oldTankButton.posY - 8, "x");
    }
}

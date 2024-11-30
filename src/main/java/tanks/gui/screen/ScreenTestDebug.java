package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.tank.Tank;

public class ScreenTestDebug extends Screen
{
    Button keyboardTest = new Button(this.centerX - this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Test keyboard", () -> Game.screen = new ScreenTestKeyboard());

    Button textboxTest = new Button(this.centerX - this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Test text boxes", () -> Game.screen = new ScreenTestTextbox());

    Button modelTest = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Test models", () -> Game.screen = new ScreenTestModel(Tank.health_model));

    Button fontTest = new Button(this.centerX + this.objXSpace / 2, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Test fonts", () -> Game.screen = new ScreenTestFonts());

    Button fireworks = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Test fireworks", () -> Game.screen = new ScreenTestFireworks());

    Button shapeTest = new Button(this.centerX + this.objXSpace / 2, this.centerY, this.objWidth, this.objHeight, "Test shapes", () -> Game.screen = new ScreenTestShapes());

    Button rainbowTest = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace, this.objWidth, this.objHeight, "Test rainbow", () -> Game.screen = new ScreenTestRainbow());

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenDebug());

    public ScreenTestDebug()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";
    }

    @Override
    public void update()
    {
        keyboardTest.update();
        textboxTest.update();
        modelTest.update();
        fontTest.update();
        shapeTest.update();
        fireworks.update();
        rainbowTest.update();

        back.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - 210, "Test stuff");

        modelTest.draw();
        keyboardTest.draw();
        textboxTest.draw();
        fontTest.draw();
        fireworks.draw();
        shapeTest.draw();
        rainbowTest.draw();

        back.draw();
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Effect;
import tanks.Game;
import tanks.Panel;

public class ScreenArcadeBonuses extends Screen
{
    public double age = 0;

    @Override
    public void update()
    {
        Panel.darkness = Math.min(Panel.darkness + Panel.frameFrequency * 1.5, 191);
        this.age += Panel.frameFrequency;

        for (Effect e: Game.effects)
            e.update();

        Game.effects.removeAll(Game.removeEffects);
    }

    @Override
    public void draw()
    {
        double heightFrac = Math.min(1, age / 50);
        double yPos = Drawing.drawing.interfaceSizeY * (1 - heightFrac / 2);
        Drawing.drawing.setColor(0, 0, 0, 127 * heightFrac);
        Drawing.drawing.fillInterfaceRect(this.centerX, yPos, this.objWidth * 2, this.objHeight * 5);
        Drawing.drawing.fillInterfaceRect(this.centerX, yPos, this.objWidth * 2 - 20, this.objHeight * 5 - 20);

        Drawing.drawing.setColor(255, 255, 255, 255 * heightFrac);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.displayInterfaceText(this.centerX, yPos - this.objHeight * 2, "Bonus points");


        for (Effect e: Game.effects)
            e.draw();

        for (Effect e: Game.effects)
            e.drawGlow();
    }
}

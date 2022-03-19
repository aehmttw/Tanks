package tanks.modapi.modlevels.Battle_Tanks_3.movables;

import basewindow.InputCodes;
import tanks.Colors;
import tanks.Game;
import tanks.modapi.CustomMovable;
import tanks.modapi.ModAPI;
import tanks.modapi.menus.FixedText;

public class Journal extends CustomMovable
{
    public boolean closeEnough = false;
    public boolean collected = false;
    boolean changed = false;

    public Journal()
    {
        super(71 *50+25, 18 *50+25);

        this.setDrawInstructions("    Drawing.drawing.setColor(226, 161, 64);" +
                                 "    Drawing.drawing.fillBox(_r1, _r2, 25, 40, 50, 15);" +
                                 "    Drawing.drawing.setColor(255, 255, 255);" +
                                 "    Drawing.drawing.fillBox(_r3, _r2, 28, 30, 50, 9);" +
                                 "    Drawing.drawing.setFontSize(5);" +
                                 "    Drawing.drawing.drawText(_r1, 915, 43, \"Development of\");" +
                                 "    Drawing.drawing.drawText(_r1, 925, 43, \"Formula IV\");",

                this.posX, this.posY, this.posX + 5
        );

        ModAPI.addObject(this);
    }

    @Override
    public void update()
    {
        this.vX *= 0.8;
        this.vY *= 0.8;

        super.update();

        closeEnough = !ModAPI.withinRange(this.posX / 50 - 0.5, this.posY / 50 - 0.5, 3).isEmpty();

        if (closeEnough)
        {
            if (!changed)
            {
                ModAPI.clearMenuGroup();
                ModAPI.displayText(FixedText.types.actionbar, Colors.white + "Press the \u00a7200200200255e" + Colors.white + " key to collect the journal!");
                changed = true;
            }

            if (Game.game.window.pressedKeys.contains(InputCodes.KEY_E))
            {
                Game.removeMovables.add(this);
                collected = true;

                ModAPI.menuGroup.clear();
                ModAPI.displayText(FixedText.types.actionbar, "Mission: Blow the factory up!");
            }
        }
    }
}

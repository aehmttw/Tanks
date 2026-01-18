package tanks.gui.screen;

import tanks.Game;
import tanks.gui.Button;

public class ScreenWaitingCancelable extends ScreenWaiting
{
    public Button cancel = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "Cancel", () -> Game.screen = previous);

    public ScreenWaitingCancelable(String message)
    {
        super(message);
    }

    @Override
    public void update()
    {
        super.update();

        if (age > 50)
            cancel.update();
    }


    @Override
    public void draw()
    {
        super.draw();

        if (age > 50)
            cancel.draw();
    }
}

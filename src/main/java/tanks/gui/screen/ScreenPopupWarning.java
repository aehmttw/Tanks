package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

import java.util.ArrayList;

public class ScreenPopupWarning extends Screen
{
    public Runnable ok;
    public String title, message;

    public Screen previous;
    public Button okButton = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 3,
            this.objWidth, this.objHeight, "Continue", () ->
    {
        ok.run();
        Game.screen = previous;
    });
    public Button cancel = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 3,
            this.objWidth, this.objHeight, "Cancel", () -> Game.screen = previous);

    public ScreenPopupWarning(Screen previous, String title, String message, Runnable ok)
    {
        this.previous = previous;
        this.music = previous.music;
        this.musicID = previous.musicID;
        this.ok = ok;
        this.title = title;
        this.message = message;
    }

    @Override
    public void update()
    {
        okButton.update();
        cancel.update();
    }

    @Override
    public void draw()
    {
        previous.draw();

        Drawing.drawing.setColor(0, 0, 0, 200);
        Drawing.drawing.drawPopup(this.centerX, this.centerY, this.objXSpace * 2.5, this.objYSpace * 10);
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - objYSpace * 3, title);
        this.textSize = 20;
        Drawing.drawing.setInterfaceFontSize(this.textSize);

        ArrayList<String> lines = Drawing.drawing.wrapText(message, this.objXSpace * 2.25, textSize);
        int i = 0;
        for (String s : lines)
            Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - objYSpace * 2 + (i++) * (this.textSize * 1.75), s);

        okButton.draw();
        cancel.draw();
    }

    public ScreenPopupWarning setContinueText(String text)
    {
        this.okButton.setText(text);
        return this;
    }
}

package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenSteamWorkshopAgreement extends Screen
{
    public ScreenSteamWorkshopAgreement()
    {
        this.music = "menu_1.ogg";
        this.musicID = "menu";
    }

    Button back = new Button(this.centerX - this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Back", () -> Game.screen = new ScreenSteamWorkshop());

    Button agree = new Button(this.centerX + this.objXSpace / 2, this.centerY + this.objYSpace * 4, this.objWidth, this.objHeight, "Agree and continue", () ->
    {
        Game.screen = new ScreenSteamWorkshop();
        Game.screen = new ScreenShareLevel();
        Game.agreedToWorkshopAgreement = true;
    });

    @Override
    public void update()
    {
        back.update();
        agree.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 4, "Steam workshop upload agreement");

        Drawing.drawing.setInterfaceFontSize(this.textSize);

        double x = this.centerX - this.objXSpace * 1.1;
        Drawing.drawing.displayInterfaceText(x, this.centerY - this.objYSpace * 2, false, "The Steam Workshop for Tanks: The Crusades is intended to be");
        Drawing.drawing.displayInterfaceText(x, this.centerY - this.objYSpace * 1.5, false, "a place to share your creations to an audience of all ages.");

        Drawing.drawing.displayInterfaceText(x, this.centerY - this.objYSpace * 0.5, false, "Please ensure that everything you upload is appropriately");
        Drawing.drawing.displayInterfaceText(x, this.centerY + this.objYSpace * 0, false, "family friendly and respectful to other players!");
        Drawing.drawing.displayInterfaceText(x, this.centerY + this.objYSpace * 0.5, false, "Creations deemed inappropriate or offensive may be removed.");

        Drawing.drawing.displayInterfaceText(x, this.centerY + this.objYSpace * 1.5, false, "Thank you for your understanding.");
        Drawing.drawing.displayInterfaceText(x, this.centerY + this.objYSpace * 2, false, "The Tanks community looks forward to seeing what you've made!");

        agree.draw();
        back.draw();
    }
}

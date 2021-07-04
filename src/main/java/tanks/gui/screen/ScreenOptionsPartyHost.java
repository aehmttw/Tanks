package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.TextBox;
import tanks.tank.TankPlayerRemote;

public class ScreenOptionsPartyHost extends Screen
{
    public static final String anticheatText = "Anticheat: ";
    public static final String disableFriendlyFireText = "Friendly fire: ";

    public static final String weakText = "\u00A7200100000255weak";
    public static final String strongText = "\u00A7000200000255strong";

    public static final String defaultText = "\u00A7000200000255default";
    public static final String disabledText = "\u00A7200000000255off";

    Button anticheat = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            if (!TankPlayerRemote.checkMotion)
            {
                TankPlayerRemote.checkMotion = true;
                TankPlayerRemote.weakTimeCheck = false;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatStrongTimeOffset;
            }
            else if (!TankPlayerRemote.weakTimeCheck)
            {
                TankPlayerRemote.weakTimeCheck = true;
                TankPlayerRemote.anticheatMaxTimeOffset = TankPlayerRemote.anticheatWeakTimeOffset;
            }
            else
                TankPlayerRemote.checkMotion = false;

            if (!TankPlayerRemote.checkMotion)
                anticheat.text = anticheatText + ScreenOptions.offText;
            else if (!TankPlayerRemote.weakTimeCheck)
                anticheat.text = anticheatText + strongText;
            else
                anticheat.text = anticheatText + weakText;
        }
    },
            "When this option is enabled---while hosting a party,---other players' positions and---velocities will be checked---and corrected if invalid.------Weaker settings work better---with less stable connections.");

    Button disableFriendlyFire = new Button(this.centerX, this.centerY - this.objYSpace * 0, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.disablePartyFriendlyFire = !Game.disablePartyFriendlyFire;

            if (Game.disablePartyFriendlyFire)
                disableFriendlyFire.text = disableFriendlyFireText + disabledText;
            else
                disableFriendlyFire.text = disableFriendlyFireText + defaultText;
        }
    },
            "Disables all friendly fire in the party.---Useful for co-op in bigger parties.");

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
            {
                Game.screen = ScreenPartyHost.activeScreen;
                ScreenOptions.saveOptions(Game.homedir);
            }
            else
                Game.screen = new ScreenOptionsMultiplayer();
        }
    }
    );

    TextBox timer;

    public ScreenOptionsPartyHost()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (!TankPlayerRemote.checkMotion)
            anticheat.text = anticheatText + ScreenOptions.offText;
        else if (!TankPlayerRemote.weakTimeCheck)
            anticheat.text = anticheatText + strongText;
        else
            anticheat.text = anticheatText + weakText;

        if (Game.disablePartyFriendlyFire)
            disableFriendlyFire.text = disableFriendlyFireText + disabledText;
        else
            disableFriendlyFire.text = disableFriendlyFireText + defaultText;

        timer = new TextBox(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "Countdown time", new Runnable()
        {
            @Override
            public void run()
            {
                if (timer.inputText.length() == 0)
                    timer.inputText = Game.partyStartTime / 100.0 + "";
                else
                    Game.partyStartTime = Double.parseDouble(timer.inputText) * 100;
            }
        }, Game.partyStartTime / 100.0 + "", "The wait time in seconds after---all players are ready before---the battle begins.");

        timer.maxValue = 60;
        timer.maxChars = 4;
        timer.checkMaxValue = true;
        timer.allowDoubles = true;
        timer.allowLetters = false;
        timer.allowSpaces = false;
    }

    @Override
    public void update()
    {
        back.update();
        timer.update();
        anticheat.update();
        disableFriendlyFire.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        back.draw();
        anticheat.draw();
        disableFriendlyFire.draw();
        timer.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Party host options");
    }

}

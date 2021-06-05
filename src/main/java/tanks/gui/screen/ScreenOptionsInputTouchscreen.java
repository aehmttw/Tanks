package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.tank.TankPlayer;

public class ScreenOptionsInputTouchscreen extends Screen
{
    public static final String vibrationsText = "Vibrations: ";
    public static final String mobileText = "Mobile joystick: ";
    public static final String snapText = "Snap joystick: ";
    public static final String dualJoysticksText = "Joystick mode: ";

    public static final String singleText = "\u00A7000100200255single";
    public static final String dualText = "\u00A7200100000255dual";

    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    }
    );

    Button test = new Button(this.centerX, this.centerY + this.objYSpace * 2, this.objWidth, this.objHeight, "Test controls in tutorial", new Runnable()
    {
        @Override
        public void run()
        {
            ScreenOptions.saveOptions(Game.homedir);
            new Tutorial().loadTutorial(false, Game.game.window.touchscreen);
        }
    });


    Button vibrations = new Button(this.centerX, this.centerY + this.objYSpace * 1, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            Game.enableVibrations = !Game.enableVibrations;

            if (Game.enableVibrations)
                vibrations.text = vibrationsText + ScreenOptions.onText;
            else
                vibrations.text = vibrationsText + ScreenOptions.offText;
        }
    },
            "When enabled, your device---will vibrate a little as---feedback for interacting with---joysticks, buttons, etc...------Not supported on all devices");

    Button mobile = new Button(this.centerX, this.centerY - this.objYSpace, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            TankPlayer.controlStick.mobile = !TankPlayer.controlStick.mobile;
            TankPlayer.controlStick.posX = TankPlayer.controlStick.basePosX;
            TankPlayer.controlStick.posY = TankPlayer.controlStick.basePosY;

            if (TankPlayer.controlStick.mobile)
                mobile.text = mobileText + ScreenOptions.onText;
            else
                mobile.text = mobileText + ScreenOptions.offText;

            TankPlayer.controlStickMobile = TankPlayer.controlStick.mobile;
        }
    },
            "When enabled, the movement joystick---can be dragged around the screen.---It will also jump to your---finger when you select it.");

    Button snap = new Button(this.centerX, this.centerY, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            TankPlayer.controlStick.snap = !TankPlayer.controlStick.snap;
            TankPlayer.controlStick.posX = TankPlayer.controlStick.basePosX;
            TankPlayer.controlStick.posY = TankPlayer.controlStick.basePosY;

            if (TankPlayer.controlStick.snap)
                snap.text = snapText + ScreenOptions.onText;
            else
                snap.text = snapText + ScreenOptions.offText;

            TankPlayer.controlStickSnap = TankPlayer.controlStick.snap;
        }
    },
            "When enabled, the movement joystick---will return to its initial position---upon being released. It will also---jump to your finger when you---select it.");

    Button dualJoysticks = new Button(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "", new Runnable()
    {
        @Override
        public void run()
        {
            TankPlayer.controlStick.posX = TankPlayer.controlStick.basePosX;
            TankPlayer.controlStick.posY = TankPlayer.controlStick.basePosY;

            TankPlayer.setShootStick(!TankPlayer.shootStickEnabled);

            if (!TankPlayer.shootStickEnabled)
            {
                dualJoysticks.text = dualJoysticksText + singleText;
                snap.enabled = true;

                if (TankPlayer.controlStick.snap)
                    snap.text = snapText + ScreenOptions.onText;
                else
                    snap.text = snapText + ScreenOptions.offText;
            }
            else
            {
                dualJoysticks.text = dualJoysticksText + dualText;
                snap.enabled = false;
                snap.text = snapText + ScreenOptions.onText;
            }
        }
    },
            "Single joystick: Use one joystick---to move, and tap on the screen to shoot.---Double tap the player to lay a mine.------Dual joysticks: Use a second joystick---to shoot, includes a mine button.");


    public ScreenOptionsInputTouchscreen()
    {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        vibrations.enabled = Game.game.window.vibrationsEnabled;

        if (!Game.game.window.vibrationsEnabled)
            Game.enableVibrations = false;

        if (Game.enableVibrations)
            vibrations.text = vibrationsText + ScreenOptions.onText;
        else
            vibrations.text = vibrationsText + ScreenOptions.offText;

        if (TankPlayer.controlStick.snap)
            snap.text = snapText + ScreenOptions.onText;
        else
            snap.text = snapText + ScreenOptions.offText;

        if (TankPlayer.controlStick.mobile)
            mobile.text = mobileText + ScreenOptions.onText;
        else
            mobile.text = mobileText + ScreenOptions.offText;

        if (!TankPlayer.shootStickEnabled)
        {
            dualJoysticks.text = dualJoysticksText + singleText;
            snap.enabled = true;

            if (TankPlayer.controlStick.snap)
                snap.text = snapText + ScreenOptions.onText;
            else
                snap.text = snapText + ScreenOptions.offText;
        }
        else
        {
            dualJoysticks.text = dualJoysticksText + dualText;
            snap.enabled = false;
            snap.text = snapText + ScreenOptions.onText;
        }
    }

    @Override
    public void update()
    {
        back.update();
        dualJoysticks.update();
        mobile.update();
        snap.update();
        vibrations.update();
        test.update();
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        back.draw();
        vibrations.draw();
        snap.draw();
        mobile.draw();
        dualJoysticks.draw();
        test.draw();

        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Input options");
    }

}

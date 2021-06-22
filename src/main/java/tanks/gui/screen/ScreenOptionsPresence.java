package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.rpc.RichPresence;
import tanks.rpc.RichPresenceEvent;

public class ScreenOptionsPresence extends Screen{
    Button back = new Button(this.centerX, this.centerY + this.objYSpace * 3.5, this.objWidth, this.objHeight, "Back", new Runnable() {
        @Override
        public void run()
        {
            Game.screen = new ScreenOptions();
        }
    });

    Button enable = new Button(this.centerX, this.centerY - this.objYSpace * 2, this.objWidth, this.objHeight, "Presence", new Runnable() {
        @Override
        public void run() {
            if (Game.game.presenceEnabled) {
                Game.game.discordRPC.exit();
                Game.game.presenceEnabled = false;
                enable.text = "Presence: \u00A7255000000255Disabled";
            } else {
                Game.game.presenceEnabled = true;
                Game.game.discordRPC = new RichPresence();
                Game.game.discordRPC.update(RichPresenceEvent.OPTIONS_MENU);
                enable.text = "Presence: \u00A7000255000255Enabled";
            }
        }
    }, "If Rich Presence is Enabled,---friends on Discord can see---that you are currently playing---Tanks: The Crusades");

    public ScreenOptionsPresence() {
        this.music = "menu_options.ogg";
        this.musicID = "menu";

        if (Game.game.presenceEnabled)
            enable.text = "Presence: \u00A7000255000255Enabled";
        else
            enable.text = "Presence: \u00A7255000000255Disabled";
    }

    @Override
    public void update() {
        enable.update();

        back.update();
    }

    @Override
    public void draw() {
        this.drawDefaultBackground();

        enable.draw();

        back.draw();
        Drawing.drawing.setInterfaceFontSize(this.titleSize);
        Drawing.drawing.setColor(0, 0, 0);
        Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 3.5, "Discord Rich Presence Options");
    }
}

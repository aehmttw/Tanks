package tanks.gui.screen;

import basewindow.ModelPart;
import tanks.*;
import tanks.minigames.Minigame;
import tanks.tank.*;

import java.util.ArrayList;

public class Tutorial extends Minigame
{
    public int step = 0;
    public double stepAnimation = 0;

    public boolean touchscreen = false;

    public TankDummy dummy1;
    public TankDummy dummy2;
    public TankDummy dummy3;
    public TankDummy dummy4;
    public TankDummy dummy5;

    public TankBrown brown;
    public TankGray gray;

    public ModelPart arrow;

    public int prevDummiesDestroyed = 0;

    public boolean dummy1alive = true;
    public boolean dummy2alive = true;
    public boolean dummy3alive = true;
    public boolean dummy4alive = true;
    public boolean dummy5alive = true;
    public boolean brownAlive = true;
    public boolean grayAlive = true;

    public Tutorial()
    {
        super("{50,18,235,207,166,20,20,20|" +
                "27-10...14,37-7...11,12-0...13-hard,13...22-7-hard,27-0...9-hard,27-15...17-hard,37-0...6-hard,37-12...17-hard,23...26-7-hole|" +
                "5-7-player-1}");

        this.customLevelEnd = true;
    }

    public boolean levelEnded()
    {
        return false;
    }

    public void loadTutorial(boolean initial, boolean touchscreen)
    {
        this.loadLevel();
        Game.currentLevel = this;

        if (initial)
            Panel.panel.zoomTimer = 100;

        this.touchscreen = touchscreen;

        Drawing.drawing.movingCamera = true;
        ScreenInterlevel.tutorialInitial = initial;
        ScreenInterlevel.tutorial = true;

        if (initial)
            Game.screen = new ScreenTutorialGame();
        else
            Game.screen = new ScreenGame();

        ((ScreenGame) Game.screen).tutorial = this;

        this.dummy1 = this.addDummy(25, 15, 2);
        this.dummy2 = this.addDummy(25, 11, 2);
        this.dummy3 = this.addDummy(20, 9, 1);
        this.dummy4 = this.addDummy(15, 9, 1);

        this.dummy5 = this.addDummy(15, 2, 0);

        this.brown = new TankBrown("brown", 32.5 * Game.tile_size, 2.5 * Game.tile_size, Math.PI / 2);
        this.brown.team = Game.enemyTeam;

        this.gray = new TankGray("gray", 47.5 * Game.tile_size, 9.5 * Game.tile_size, Math.PI);
        this.gray.team = Game.enemyTeam;

        arrow = Drawing.drawing.createModel();
        arrow.shapes = new ModelPart.Shape[2];
        arrow.shapes[0] = new ModelPart.Triangle(new ModelPart.Point(-1, 0, 0), new ModelPart.Point(-1.75, 0, 0), new ModelPart.Point(-2, 0.25, 0), 1);
        arrow.shapes[1] = new ModelPart.Triangle(new ModelPart.Point(-1, 0, 0), new ModelPart.Point(-1.75, 0, 0), new ModelPart.Point(-2, -0.25, 0), 1);
    }

    public void update()
    {
        this.customLevelEnd = step <= 8 && !Game.playerTank.destroy;

        if (Game.playerTank.destroy)
            return;

        int prevStep = step;

        stepAnimation = Math.min(1, stepAnimation + Panel.frameFrequency / 25);

        int dummiesDestroyed = 0;

        ArrayList<Tank> destroyedDummies = new ArrayList<>();
        if (dummy1.destroy)
        {
            if (dummy1alive)
                destroyedDummies.add(dummy1);

            dummiesDestroyed++;
            dummy1alive = false;
        }

        if (dummy2.destroy)
        {
            if (dummy2alive)
                destroyedDummies.add(dummy2);

            dummiesDestroyed++;
            dummy2alive = false;
        }

        if (dummy3.destroy)
        {
            if (dummy3alive)
                destroyedDummies.add(dummy3);

            dummiesDestroyed++;
            dummy3alive = false;
        }

        if (dummy4.destroy)
        {
            if (dummy4alive)
                destroyedDummies.add(dummy4);

            dummiesDestroyed++;
            dummy4alive = false;
        }

        if (dummy5.destroy)
        {
            if (dummy5alive)
                destroyedDummies.add(dummy5);

            dummiesDestroyed++;
            dummy5alive = false;
        }

        if (brown.destroy)
        {
            if (brownAlive)
                destroyedDummies.add(brown);

            dummiesDestroyed++;
            brownAlive = false;
        }

        if (gray.destroy)
        {
            if (grayAlive)
                destroyedDummies.add(gray);

            dummiesDestroyed++;
            grayAlive = false;
        }

        int i = this.prevDummiesDestroyed;

        for (Tank t: destroyedDummies)
        {
            i++;
            Effect e = Effect.createNewEffect(t.posX, t.posY, t.size / 2, Effect.EffectType.tutorialProgress);
            e.radius = i;
            Game.effects.add(e);

            if (i <= 4)
                Drawing.drawing.playSound("hit_chain.ogg", (float) (Math.pow(2, (i - 1.0) / 12)));
        }

        this.prevDummiesDestroyed = dummiesDestroyed;

        if (step == 0 && !withinRange(5, 7, 4))
            step = 1;
        else if (step == 1 && withinRange(12, 15.5, 2))
            step = 2;
        else if (step == 2 && dummy1.destroy && dummy2.destroy && dummy3.destroy && dummy4.destroy)
            step = 3;
        else if (step == 3 && dummy5.destroy)
            step = 4;
        else if (step == 4 || step == 5)
        {
            step = 4;

            for (Movable m: Game.movables)
            {
                if (m instanceof Mine)
                {
                    step = 5;
                    break;
                }
            }

            if (!Game.game.solidGrid[27][10] || !Game.game.solidGrid[27][11] || !Game.game.solidGrid[27][12] || !Game.game.solidGrid[27][13] || !Game.game.solidGrid[27][14])
                step = 6;
        }
        else if (step == 6 && brown.destroy)
            step = 7;
        else if (step == 7 && Game.player.hotbar.persistent)
            step = 8;
        else if (step == 8 && this.gray.destroy)
            step = 9;

        if (prevStep < step)
        {
            Drawing.drawing.playSound("rampage.ogg", (float) (Math.pow(2, (prevStep * 1.0) / 12)));

            if (step == 2)
            {
                Drawing.drawing.addSyncedMusic("arcade/rampage1.ogg", Game.musicVolume, true, 500);
                Game.movables.add(new Crate(this.dummy1));
                Game.movables.add(new Crate(this.dummy2));
                Game.movables.add(new Crate(this.dummy3));
                Game.movables.add(new Crate(this.dummy4));
            }
            else if (step == 3)
            {
                Drawing.drawing.addSyncedMusic("arcade/rampage2.ogg", Game.musicVolume, true, 500);
                Game.movables.add(new Crate(this.dummy5));
            }
            else if (step == 4)
            {
                Game.movables.add(new Crate(this.brown));
                Drawing.drawing.addSyncedMusic("arcade/rampage3.ogg", Game.musicVolume, true, 500);
            }
            else if (step == 6)
            {
                Drawing.drawing.addSyncedMusic("arcade/rampage4.ogg", Game.musicVolume, true, 500);
                Drawing.drawing.addSyncedMusic("arcade/rampage5.ogg", Game.musicVolume, true, 500);
            }
            else if (step == 8)
            {
                Game.movables.add(new Crate(this.gray));
                Drawing.drawing.addSyncedMusic("arcade/rampage6.ogg", Game.musicVolume, true, 500);
                Drawing.drawing.addSyncedMusic("arcade/rampage7.ogg", Game.musicVolume, true, 500);
            }

            stepAnimation = 0;
        }
        else if (prevStep > step)
            Drawing.drawing.playSound("leave.ogg");
    }

    public void drawTutorial()
    {
        if (ScreenGame.finishedQuick || Game.game.window.drawingShadow)
            return;

        if (step == 0)
        {
            String input = "the keyboard.";

            if (touchscreen)
                input = "the blue joystick.";

            drawTopBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "Welcome to Tanks!");
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "You can control the tank in the circle using " + input);

            Drawing.drawing.setColor(0, 0, 0, this.stepAnimation * 255);

            if (touchscreen)
            {
                Drawing.drawing.drawInterfaceText(TankPlayer.controlStick.posX + TankPlayer.controlStick.size * 0.6, TankPlayer.controlStick.posY, "Use this stick to move your tank!", false);
            }
            else
            {
                Drawing.drawing.setFontSize(24 * stepAnimation);
                Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY - 140 * stepAnimation, "Move up:");
                Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY - 110 * stepAnimation, Game.game.input.moveUp.getInputs());

                Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY + 110 * stepAnimation, "Move down:");
                Drawing.drawing.drawText(Game.playerTank.posX, Game.playerTank.posY + 140 * stepAnimation, Game.game.input.moveDown.getInputs());

                Drawing.drawing.drawText(Game.playerTank.posX - 150 * stepAnimation, Game.playerTank.posY - 15 * stepAnimation, "Move left:");
                Drawing.drawing.drawText(Game.playerTank.posX - 150 * stepAnimation, Game.playerTank.posY + 15 * stepAnimation, Game.game.input.moveLeft.getInputs());

                Drawing.drawing.drawText(Game.playerTank.posX + 150 * stepAnimation, Game.playerTank.posY - 15 * stepAnimation, "Move right:");
                Drawing.drawing.drawText(Game.playerTank.posX + 150 * stepAnimation, Game.playerTank.posY + 15 * stepAnimation, Game.game.input.moveRight.getInputs());
            }

            Drawing.drawing.setColor(255, 127, 0, 64 * stepAnimation);
            Drawing.drawing.fillOval(Game.tile_size * 5.5, Game.tile_size * 7.5, Game.tile_size * 8 * stepAnimation, Game.tile_size * 8 * stepAnimation);
        }
        else if (step == 1)
        {
            Drawing.drawing.setColor(255, 127, 0, 64 * stepAnimation);
            Drawing.drawing.fillOval(12.5 * Game.tile_size, 16 * Game.tile_size, Game.tile_size * 3 * stepAnimation, Game.tile_size * 3 * stepAnimation);
            this.drawArrow(Game.playerTank.posX, Game.playerTank.posY, 12.5 * Game.tile_size, 16 * Game.tile_size, Game.playerTank.getAngleInDirection(12.5 * Game.tile_size, 16 * Game.tile_size));

            Drawing.drawing.setColor(0, 0, 0, this.stepAnimation * 255);
            Drawing.drawing.setFontSize(24 * this.stepAnimation);

            String s = "Move over here to continue";
            Drawing.drawing.drawText(7 * Game.tile_size, 16 * Game.tile_size, s);
        }
        else if (step == 2)
        {
            drawTopBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, this.stepAnimation * 255);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            if (touchscreen)
            {
                if (TankPlayer.shootStickEnabled)
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "Swipe on the \u00A7255127000255orange joystick\u00A7255255255255 in the direction you want to shoot!");
                else
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "\u00A7255127000255Tap\u00A7255255255255 somewhere to shoot in that direction!");
            }
            else
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "Press \u00A7255127000255" + Game.game.input.shoot.getInputs() + "\u00A7255255255255 to shoot toward your mouse!");

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "Destroy all enemy tanks to win!");

            Drawing.drawing.setFontSize(24 * this.stepAnimation);
            Drawing.drawing.setColor(0, 0, 0, 255  * this.stepAnimation);
            Drawing.drawing.drawText(20 * Game.tile_size, 13.5 * Game.tile_size, "Shoot these enemy tanks!");

            Drawing.drawing.setColor(255, 127, 0, 64);

            if (!dummy1.destroy)
                Drawing.drawing.fillOval(dummy1.posX, dummy1.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);

            if (!dummy2.destroy)
                Drawing.drawing.fillOval(dummy2.posX, dummy2.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);

            if (!dummy3.destroy)
                Drawing.drawing.fillOval(dummy3.posX, dummy3.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);

            if (!dummy4.destroy)
                Drawing.drawing.fillOval(dummy4.posX, dummy4.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);
        }
        else if (step == 3)
        {
            Drawing.drawing.setFontSize(24 * this.stepAnimation);
            Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);
            Drawing.drawing.drawText(22 * Game.tile_size, 2.5 * Game.tile_size, "Aim and bounce your bullets on");
            Drawing.drawing.drawText(22 * Game.tile_size, 3.5 * Game.tile_size, "a wall to destroy this tank!");

            Drawing.drawing.setColor(255, 127, 0, 64 * this.stepAnimation);
            Drawing.drawing.fillOval(dummy5.posX, dummy5.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);
            this.drawArrow(Game.playerTank.posX, Game.playerTank.posY, dummy5.posX, dummy5.posY, Game.playerTank.getAngleInDirection(dummy5.posX, dummy5.posY));

            drawBottomBar(250 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            if (touchscreen)
            {
                if (TankPlayer.shootStickEnabled)
                {
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 3.5, "Drag around the \u00A7255127000255orange joystick\u00A7255255255255 to aim.");
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Drag your finger out of the joystick to shoot!");
                }
                else
                {
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 3.5, "\u00A7000150255255Tap the player\u00A7255255255255 and drag around the \u00A7255127000255orange circle\u00A7255255255255 to aim.");
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Pull your finger out of the \u00A7255127000255orange circle\u00A7255255255255 to shoot!");
                }
            }
            else
            {
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 3.5, "Press and hold \u00A7000150255255" + Game.game.input.aim.getInputs() + "\u00A7255255255255 to aim.");
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Press \u00A7255127000255" + Game.game.input.shoot.getInputs() + "\u00A7255255255255 to shoot!");
            }

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 1.5, "You can have up to 5 bullets on screen at one time.");
        }
        else if (step == 4)
        {
            Drawing.drawing.setFontSize(24 * this.stepAnimation);
            Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);

            Drawing.drawing.drawText(23.5 * Game.tile_size, 12 * Game.tile_size, "Place a mine next to this");
            Drawing.drawing.drawText(23.5 * Game.tile_size, 13 * Game.tile_size, "brown wall to destroy it!");

            drawBottomBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            if (touchscreen)
            {
                if (TankPlayer.shootStickEnabled)
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Press the \u00A7255255000255yellow button\u00A7255255255255 to lay a mine!");
                else
                    Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Double-tap \u00A7000150255255your tank\u00A7255255255255 to lay a mine!");
            }
            else
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Press \u00A7255127000255" + Game.game.input.mine.getInputs() + "\u00A7255255255255 to lay a mine!");

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 1.5, "Mines can destroy nearby tanks and brown blocks.");
        }
        else if (step == 5)
        {
            Drawing.drawing.setFontSize(24 * this.stepAnimation);
            Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);

            for (Movable m : Game.movables)
            {
                if (m instanceof Mine)
                {
                    Drawing.drawing.setColor(255, 0, 0, 127 * this.stepAnimation);
                    Drawing.drawing.fillOval(m.posX, m.posY, ((Mine) m).radius * 2 * this.stepAnimation, ((Mine) m).radius * 2 * this.stepAnimation);

                    Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);
                    Drawing.drawing.drawText(m.posX, m.posY + 50 * this.stepAnimation, "Stand back!");
                    Drawing.drawing.drawText(m.posX, m.posY + 100 * this.stepAnimation, "Your mines can destroy you!");
                }
            }

            drawTopBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "A mine will explode after 10 seconds, if another tank gets near, or if shot.");
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "You can have up to 2 mines on screen at a time.");
        }
        else if (step == 6)
        {
            Drawing.drawing.setFontSize(24 * this.stepAnimation);
            Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);
            Drawing.drawing.drawText(32.5 * Game.tile_size, 3.5 * Game.tile_size, "Watch out! This brown");
            Drawing.drawing.drawText(32.5 * Game.tile_size, 4.5 * Game.tile_size, "tank will shoot at you!");

            Drawing.drawing.drawText(32.5 * Game.tile_size, 6.5 * Game.tile_size, "Shoot it before");
            Drawing.drawing.drawText(32.5 * Game.tile_size, 7.5 * Game.tile_size, "it destroys you!");

            Drawing.drawing.setColor(255, 127, 0, 64 * this.stepAnimation);
            Drawing.drawing.fillOval(brown.posX, brown.posY, Game.tile_size * 2 * this.stepAnimation, Game.tile_size * 2 * this.stepAnimation);
            this.drawArrow(Game.playerTank.posX, Game.playerTank.posY, brown.posX, brown.posY, Game.playerTank.getAngleInDirection(brown.posX, brown.posY));

            drawBottomBar(200);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 2.5, "Avoid all bullets and mines, including your own, as they can destroy you.");
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 1.5, "If your tank is destroyed, you will have to start the level over!");
        }
        else if (step == 7)
        {
            drawTopBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);

            if (touchscreen)
            {
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "and remaining enemy tanks by pressing the \u00A7255127000255bottom arrow\u00A7255255255255!");
                Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 1.5, "Press the arrow below!");
                Drawing.drawing.setColor(255, 127, 0, 64 * this.stepAnimation);
                Drawing.drawing.fillInterfaceOval(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 6, 150 * this.stepAnimation, 75 * this.stepAnimation);
                Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            }
            else
            {
                Drawing.drawing.setInterfaceFontSize(32 * this.stepAnimation);
                Drawing.drawing.setColor(0, 0, 0, 255 * this.stepAnimation);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - Game.tile_size * 1.5, "Press " + Game.game.input.hotbarToggle.getInputs() + "!");
                Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
                Drawing.drawing.setInterfaceFontSize(24 * this.stepAnimation);
                Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "and remaining enemy tanks by pressing \u00A7255127000255" + Game.game.input.hotbarToggle.getInputs() + "\u00A7000000000255!");
            }

            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "You can see your health, available bullets and mines,");

        }
        else if (step == 8)
        {
            drawTopBar(200 * this.stepAnimation);
            Drawing.drawing.setColor(255, 255, 255, 255 * this.stepAnimation);
            Drawing.drawing.setInterfaceFontSize(24);

            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 1.5, "Now finish off the battle by destroying that gray tank,");
            Drawing.drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, Game.tile_size * 2.5, "but watch out - it can move!");

        }
    }

    public boolean withinRange(double x, double y, double size)
    {
        return Math.pow(Game.playerTank.posX - (x * 50 + 25), 2) + Math.pow(Game.playerTank.posY - (y * 50 + 25), 2) <= (size * 50) * (size * 50);
    }

    public void drawTopBar(double height)
    {
        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127 * this.stepAnimation);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, -extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, height / 2, width, height);
    }

    public void drawBottomBar(double height)
    {
        double extraHeight = ((Game.game.window.absoluteHeight - Drawing.drawing.statsHeight) / Drawing.drawing.interfaceScale - Drawing.drawing.interfaceSizeY) / 2;
        double width = Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale;

        Drawing.drawing.setColor(0, 0, 0, 127 * this.stepAnimation);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY + extraHeight / 2, width, extraHeight);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - height / 2, width, height);
    }

    public void drawArrow(double px, double py, double x, double y, double angle)
    {
        if (Drawing.drawing.interfaceScaleZoom > 1)
        {
            double dist = Math.sqrt(Math.pow(px - x, 2) + Math.pow(py - y, 2));

            if (dist >= 400)
            {
                Drawing.drawing.setColor(255, 127, 0, 127);
                Drawing.drawing.drawModel(arrow, px + Math.cos(angle) * 400, py + Math.sin(angle) * 400, 100, 100, angle);
            }
        }
    }


    public TankDummy addDummy(double x, double y, double angle)
    {
        TankDummy t = new TankDummy("dummy", (x + 0.5) * Game.tile_size, (y + 0.5) * Game.tile_size, angle * Math.PI / 2);
        t.team = Game.enemyTeam;
        //Game.addTank(t);

        return t;
    }
}

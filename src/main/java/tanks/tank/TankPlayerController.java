package tanks.tank;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventTankControllerUpdateC;

import java.util.UUID;

public class TankPlayerController extends Tank implements IPlayerTank
{
    public UUID clientID;

    public boolean action1;
    public boolean action2;

    public boolean drawTouchCircle = false;
    public double touchCircleSize = 400;
    public long prevTap = 0;

    protected double prevDistSq;

    public TankPlayerController(double x, double y, double angle, UUID id)
    {
        super("player", x, y, Game.tile_size, 0, 150, 255);
        this.clientID = id;
        this.isRemote = true;
        this.angle = angle;
        this.orientation = angle;
    }

    @Override
    public void update()
    {
        boolean up = Game.game.window.pressedKeys.contains(InputCodes.KEY_UP) || Game.game.window.pressedKeys.contains(InputCodes.KEY_W);
        boolean down = Game.game.window.pressedKeys.contains(InputCodes.KEY_DOWN) || Game.game.window.pressedKeys.contains(InputCodes.KEY_S);
        boolean left = Game.game.window.pressedKeys.contains(InputCodes.KEY_LEFT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_A);
        boolean right = Game.game.window.pressedKeys.contains(InputCodes.KEY_RIGHT) || Game.game.window.pressedKeys.contains(InputCodes.KEY_D);
        boolean trace = Game.game.window.pressedKeys.contains(InputCodes.KEY_PERIOD) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_4);

        double acceleration = accel;
        double maxVelocity = maxV;

        double x = 0;
        double y = 0;

        double a = -1;

        if (left)
            x -= 1;

        if (right)
            x += 1;

        if (up)
            y -= 1;

        if (down)
            y += 1;

        if (x == 1 && y == 0)
            a = 0;
        else if (x == 1 && y == 1)
            a = Math.PI / 4;
        else if (x == 0 && y == 1)
            a = Math.PI / 2;
        else if (x == -1 && y == 1)
            a = 3 * Math.PI / 4;
        else if (x == -1 && y == 0)
            a = Math.PI;
        else if (x == -1 && y == -1)
            a = 5 * Math.PI / 4;
        else if (x == 0 && y == -1)
            a = 3 * Math.PI / 2;
        else if (x == 1 && y == -1)
            a = 7 * Math.PI / 4;

        double intensity = 1;

        if (a < 0 && Game.game.window.touchscreen)
        {
            intensity = TankPlayer.controlStick.inputIntensity;

            if (intensity > 0.2)
                a = TankPlayer.controlStick.inputAngle;
        }

        if (a >= 0 && intensity > 0.2)
            this.addPolarMotion(a, acceleration * Panel.frameFrequency);
        else
        {
            this.vX *= Math.pow(0.95, Panel.frameFrequency);
            this.vY *= Math.pow(0.95, Panel.frameFrequency);

            if (Math.abs(this.vX) < 0.001)
                this.vX = 0;

            if (Math.abs(this.vY) < 0.001)
                this.vY = 0;
        }

        double speed = Math.sqrt(this.vX * this.vX + this.vY * this.vY);

        if (speed > maxVelocity)
            this.setPolarMotion(this.getPolarDirection(), maxVelocity);

        boolean shoot = false;
        if (!Game.game.window.touchscreen && (Game.game.window.pressedKeys.contains(InputCodes.KEY_SPACE) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1)))
            shoot = true;

        boolean mine = false;
        if (!Game.game.window.touchscreen && (Game.game.window.pressedKeys.contains(InputCodes.KEY_ENTER) || Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_2)))
            mine = true;

        boolean prevTouchCircle = this.drawTouchCircle;
        this.drawTouchCircle = false;
        if (Game.game.window.touchscreen)
        {
            if (!Game.bulletLocked && !this.disabled && !this.destroy)
            {
                double distSq = 0;

                if (TankPlayer.shootStickEnabled)
                {
                    TankPlayer.mineButton.update();

                    if (TankPlayer.mineButton.justPressed)
                        mine = true;

                    TankPlayer.shootStick.update();

                    if (TankPlayer.shootStick.inputIntensity >= 0.2)
                    {
                        this.angle = TankPlayer.shootStick.inputAngle;
                        trace = true;

                        if (TankPlayer.shootStick.inputIntensity >= 1.0)
                            shoot = true;
                    }
                }
                else
                {
                    for (int i : Game.game.window.touchPoints.keySet())
                    {
                        InputPoint p = Game.game.window.touchPoints.get(i);

                        if (!p.tag.equals("") && !p.tag.equals("aim") && !p.tag.equals("shoot"))
                            continue;

                        double px = Drawing.drawing.getInterfacePointerX(p.x);
                        double py = Drawing.drawing.getInterfacePointerY(p.y);

                        this.angle = this.getAngleInDirection(Drawing.drawing.toGameCoordsX(px),
                                Drawing.drawing.toGameCoordsY(py));

                        distSq = Math.pow(px - Drawing.drawing.toInterfaceCoordsX(this.posX), 2)
                                + Math.pow(py - Drawing.drawing.toInterfaceCoordsY(this.posY), 2);

                        if (distSq <= Math.pow(this.touchCircleSize / 4, 2) || p.tag.equals("aim"))
                        {
                            p.tag = "aim";
                            this.drawTouchCircle = true;

                            if (!prevTouchCircle)
                            {
                                if (System.currentTimeMillis() - prevTap <= 500)
                                {
                                    Drawing.drawing.playVibration("heavyClick");
                                    mine = true;
                                    this.prevTap = 0;
                                }
                                else
                                    prevTap = System.currentTimeMillis();
                            }

                            trace = true;
                        }
                        else
                        {
                            shoot = true;
                            p.tag = "shoot";
                        }

                        double proximity = Math.pow(this.touchCircleSize / 2, 2);

                        if (p.tag.equals("aim") && ((distSq <= proximity && prevDistSq > proximity) || (distSq > proximity && prevDistSq <= proximity)))
                            Drawing.drawing.playVibration("selectionChanged");

                        if (distSq > proximity)
                            shoot = true;
                    }
                }

                this.prevDistSq = distSq;
            }
        }
        else
            this.angle = this.getAngleInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY());

        this.action1 = shoot;
        this.action2 = mine;

        if (trace && !Game.bulletLocked && !this.disabled)
        {
            Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);
            r.vX /= 2;
            r.vY /= 2;
            r.trace = true;
            r.dotted = true;
            r.moveOut(10 * this.size / Game.tile_size);
            r.getTarget();
        }

        super.update();

        Game.eventsOut.add(new EventTankControllerUpdateC(this));
    }

    @Override
    public double getTouchCircleSize()
    {
        return this.touchCircleSize;
    }

    @Override
    public boolean showTouchCircle()
    {
        return this.drawTouchCircle;
    }
}

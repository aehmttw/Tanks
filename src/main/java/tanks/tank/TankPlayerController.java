package tanks.tank;

import org.lwjgl.glfw.GLFW;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventTankControllerUpdateC;

import java.util.UUID;

public class TankPlayerController extends Tank
{
    public UUID clientID;

    public boolean action1;
    public boolean action2;

    public TankPlayerController(double x, double y, double angle, UUID id)
    {
        super("player", x, y, Game.tank_size, 0, 150, 255);
        this.clientID = id;
        this.isRemote = true;
        this.angle = angle;
    }

    @Override
    public void update()
    {
        boolean up = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_UP) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_W);
        boolean down = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_DOWN) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_S);
        boolean left = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_LEFT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_A);
        boolean right = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_RIGHT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_D);
        boolean trace = Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_PERIOD) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_4);

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

        if (a >= 0)
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

        if (this.cooldown > 0)
            this.cooldown -= Panel.frameFrequency;

        this.action1 = (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_SPACE) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1));

        this.action2 = (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_ENTER) || Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_2));

        this.angle = this.getAngleInDirection(Drawing.drawing.getMouseX(), Drawing.drawing.getMouseY());

        if (trace && !Game.bulletLocked)
        {
            Ray r = new Ray(this.posX, this.posY, this.angle, 1, this);
            r.vX /= 2;
            r.vY /= 2;
            r.trace = true;
            r.dotted = true;
            r.moveOut(10);
            r.getTarget();
        }

        super.update();

        Game.eventsOut.add(new EventTankControllerUpdateC(this));
    }
}

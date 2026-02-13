package lwjglwindow;

import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLGamepad;
import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.sdl.SDL_Event;
import tanks.Panel;
import tanks.gui.ScreenElement;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;

public class Controller
{
    public LWJGLWindow window;

    public Controller(LWJGLWindow window)
    {
        this.window = window;
    }

    double baseX = 0;

    double baseY = 0;

    boolean resetGyro = true;

    Queue<Float> xs = new LinkedList<>();

    Queue<Float> ys = new LinkedList<>();

    long last = 0;

    SDL_Event event = SDL_Event.malloc();

    protected void updateJoysticks()
    {
        while (SDLEvents.SDL_PollEvent(event))
        {
            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_ADDED)
            {
                long l = SDLGamepad.SDL_OpenGamepad(event.gdevice().which());

                Panel.notifications.add(new ScreenElement.Notification(SDLGamepad.SDL_GetGamepadVendor(l) + " " + SDLGamepad.SDL_GetGamepadName(l) + " is now connected!", 1000, 400));
                SDLGamepad.SDL_SetGamepadLED(l, (byte) 255, (byte) 255, (byte) 255);
                if (SDLGamepad.SDL_GetGamepadName(l).contains("(R)"))
                    SDLGamepad.SDL_SetGamepadSensorEnabled(l, SDLSensor.SDL_SENSOR_GYRO, true);
//                SDLGamepad.SDL_SetGamepadSensorEnabled(l, SDLSensor.SDL_SENSOR_GYRO_L, true);
//                SDLGamepad.SDL_SetGamepadSensorEnabled(l, SDLSensor.SDL_SENSOR_GYRO_R, true);
                System.out.println(SDLGamepad.SDL_GamepadSensorEnabled(l, SDLSensor.SDL_SENSOR_GYRO));
            }

            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_REMOVED)
            {
                Panel.notifications.add(new ScreenElement.Notification("Controller has disconnected!", 1000, 400));
            }

            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_BUTTON_DOWN)
            {
                window.stickEnabled = true;
                Panel.notifications.add(new ScreenElement.Notification(event.gbutton().which() + " " + SDLGamepad.SDL_GetGamepadStringForButton(event.gbutton().button())));

                if (event.gbutton().button() == 16)
                {
                    window.absoluteMouseX = window.absoluteWidth / 2;
                    window.absoluteMouseY = window.absoluteHeight / 2;
                    glfwSetCursorPos(window.window, window.absoluteWidth / 2, window.absoluteHeight / 2);
                }

                if (event.gbutton().button() == 0)
                {
                    window.validPressedButtons.add(1);
                    window.pressedButtons.add(1);
                }

                if (event.gbutton().button() == 18)
                {
                    window.validPressedButtons.add(0);
                    window.pressedButtons.add(0);
                }
//                System.out.println("down " + event.gbutton().button());
            }

            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_SENSOR_UPDATE)
            {
                FloatBuffer fb = event.gsensor().data();
                float z = fb.get();
                float x = fb.get();
                float y = fb.get();

                xs.add(x);
                ys.add(y);
//                System.out.println(x + " " + y);

                if (xs.size() > 1000)
                {
                    xs.remove();
                    ys.remove();
                }

                if (resetGyro && xs.size() >= 1000)
                {
                    double totalX = 0;
                    for (float x1 : xs)
                    {
                        totalX += x1;
                    }

                    double totalY = 0;
                    for (float y1 : ys)
                    {
                        totalY += y1;
                    }

                    baseX = -totalX / xs.size();
                    baseY = -totalY / ys.size();
                    System.out.println(">>>>> " + baseX + " " + baseY);
                    resetGyro = false;
                    xs.clear();
                    ys.clear();
                }

                double x2 = (x + baseX) * -20;
                double y2 = (y + baseY) * -20;
//                System.out.println(x2 + " " + y2);

                if (Math.abs(x) < 0.01 && Math.abs(y) < 0.01)
                {
                    x2 = 0;
                    y2 = 0;
                }

//                long t = System.currentTimeMillis();
//                System.out.println(t - last);000
//                System.out.println(event.gsensor().which() + " " + x2 + " " + y2);
//                last = t;

                if (window.stickEnabled)
                {
                    window.absoluteMouseX = window.absoluteMouseX + x2;
                    window.absoluteMouseY = window.absoluteMouseY + y2;
                }
                glfwSetCursorPos(window.window, window.absoluteMouseX, window.absoluteMouseY);


//                System.out.println(event.gsensor().sensor() + " " + x + " " + y + " " + z);
            }


            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_BUTTON_UP)
            {
//                System.out.println("up " + event.gbutton().button());
                if (event.gbutton().button() == 0)
                {
                    window.validPressedButtons.remove((Integer) 1);
                    window.pressedButtons.remove((Integer) 1);
                }

                if (event.gbutton().button() == 18)
                {
                    window.validPressedButtons.remove((Integer) 0);
                    window.pressedButtons.remove((Integer) 0);
                }
            }

            if (event.type() == SDLEvents.SDL_EVENT_GAMEPAD_AXIS_MOTION)
            {
//                System.out.println("axis " + event.gaxis().axis() + " " + event.gaxis().value());
                double val = event.gaxis().value() / 32768.0;
                if (event.gaxis().axis() == 0)
                    window.stickY = val;
                else
                    window.stickX = -val;
            }
        }

    }
}

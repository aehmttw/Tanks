package swingwindow.input;

import swingwindow.SwingWindow;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import static java.awt.event.KeyEvent.*;
import static org.lwjgl.glfw.GLFW.*;

public class InputKeyboard implements KeyListener
{
    public static final HashMap<Integer, Integer> key_translations = new HashMap<>();
	public SwingWindow window;
	public InputKeyboard(SwingWindow w)
	{
		this.window = w;
		this.setupKeyMap();
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	    if (e.getKeyCode() == 0)
	        return;

        if (!window.rawTextInput.contains(e.getKeyCode()))
            window.rawTextInput.add(e.getKeyCode());

	    int code = translate(e.getKeyCode());

		if (!window.pressedKeys.contains(code))
		{
			window.pressedKeys.add(code);
			window.validPressedKeys.add(code);
		}

        if (!window.textPressedKeys.contains(code))
        {
            window.textPressedKeys.add(code);
            window.textValidPressedKeys.add(code);
        }
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
        window.rawTextInput.remove((Integer) e.getKeyCode());

        Integer code = translate(e.getKeyCode());

        window.pressedKeys.remove(code);
		window.validPressedKeys.remove(code);

        window.textPressedKeys.remove(code);
        window.textValidPressedKeys.remove(code);
	}

	public void setupKeyMap()
    {
        key_translations.put(VK_ESCAPE, GLFW_KEY_ESCAPE);
        key_translations.put(VK_F1, GLFW_KEY_F1);
        key_translations.put(VK_F2, GLFW_KEY_F2);
        key_translations.put(VK_F3, GLFW_KEY_F3);
        key_translations.put(VK_F4, GLFW_KEY_F4);
        key_translations.put(VK_F5, GLFW_KEY_F5);
        key_translations.put(VK_F6, GLFW_KEY_F6);
        key_translations.put(VK_F7, GLFW_KEY_F7);
        key_translations.put(VK_F8, GLFW_KEY_F8);
        key_translations.put(VK_F9, GLFW_KEY_F9);
        key_translations.put(VK_F10, GLFW_KEY_F10);
        key_translations.put(VK_F11, GLFW_KEY_F11);
        key_translations.put(VK_F12, GLFW_KEY_F12);
        key_translations.put(VK_BACK_QUOTE, GLFW_KEY_GRAVE_ACCENT);
        key_translations.put(VK_BACK_SPACE, GLFW_KEY_BACKSPACE);
        key_translations.put(VK_TAB, GLFW_KEY_TAB);
        key_translations.put(VK_ENTER, GLFW_KEY_ENTER);
        key_translations.put(VK_SHIFT, GLFW_KEY_LEFT_SHIFT);
        key_translations.put(VK_CONTROL, GLFW_KEY_LEFT_CONTROL);
        key_translations.put(VK_ALT, GLFW_KEY_LEFT_ALT);
        key_translations.put(VK_META, GLFW_KEY_LEFT_SUPER);
        key_translations.put(VK_CONTEXT_MENU, GLFW_KEY_MENU);
        key_translations.put(VK_UP, GLFW_KEY_UP);
        key_translations.put(VK_DOWN, GLFW_KEY_DOWN);
        key_translations.put(VK_LEFT, GLFW_KEY_LEFT);
        key_translations.put(VK_RIGHT, GLFW_KEY_RIGHT);
    }

    public static int translate(int key)
    {
        Integer code = key_translations.get(key);
        if (code == null)
            code = key;
        return code;
    }
}

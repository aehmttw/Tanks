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
    public static final HashMap<Integer, Integer> key_untranslations = new HashMap<>();

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
        registerTranslation(VK_ESCAPE, GLFW_KEY_ESCAPE);
        registerTranslation(VK_F1, GLFW_KEY_F1);
        registerTranslation(VK_F2, GLFW_KEY_F2);
        registerTranslation(VK_F3, GLFW_KEY_F3);
        registerTranslation(VK_F4, GLFW_KEY_F4);
        registerTranslation(VK_F5, GLFW_KEY_F5);
        registerTranslation(VK_F6, GLFW_KEY_F6);
        registerTranslation(VK_F7, GLFW_KEY_F7);
        registerTranslation(VK_F8, GLFW_KEY_F8);
        registerTranslation(VK_F9, GLFW_KEY_F9);
        registerTranslation(VK_F10, GLFW_KEY_F10);
        registerTranslation(VK_F11, GLFW_KEY_F11);
        registerTranslation(VK_F12, GLFW_KEY_F12);
        registerTranslation(VK_BACK_QUOTE, GLFW_KEY_GRAVE_ACCENT);
        registerTranslation(VK_BACK_SPACE, GLFW_KEY_BACKSPACE);
        registerTranslation(VK_TAB, GLFW_KEY_TAB);
        registerTranslation(VK_ENTER, GLFW_KEY_ENTER);
        registerTranslation(VK_SHIFT, GLFW_KEY_LEFT_SHIFT);
        registerTranslation(VK_CONTROL, GLFW_KEY_LEFT_CONTROL);
        registerTranslation(VK_ALT, GLFW_KEY_LEFT_ALT);
        registerTranslation(VK_META, GLFW_KEY_LEFT_SUPER);
        registerTranslation(VK_CONTEXT_MENU, GLFW_KEY_MENU);
        registerTranslation(VK_UP, GLFW_KEY_UP);
        registerTranslation(VK_DOWN, GLFW_KEY_DOWN);
        registerTranslation(VK_LEFT, GLFW_KEY_LEFT);
        registerTranslation(VK_RIGHT, GLFW_KEY_RIGHT);
        registerTranslation(VK_SPACE, GLFW_KEY_SPACE);
    }

    public void registerTranslation(Integer a, Integer b)
    {
        key_translations.put(a, b);
        key_untranslations.put(b, a);
    }

    public static int translate(int key)
    {
        Integer code = key_translations.get(key);
        if (code == null)
            code = key;
        return code;
    }

    public static int untranslate(int key)
    {
        Integer code = key_untranslations.get(key);
        if (code == null)
            code = key;
        return code;
    }
}

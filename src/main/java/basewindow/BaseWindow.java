package basewindow;

import basewindow.transformation.Transformation;
import basewindow.transformation.Translation;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseWindow
{
    public BaseFontRenderer fontRenderer;

    public boolean angled = false;

    public double pointWidth = -1;
    public double pointHeight = -1;

    public double absoluteWidth;
    public double absoluteHeight;
    public double absoluteDepth;

    public double absoluteMouseX;
    public double absoluteMouseY;

    public boolean fullscreen;

    public HashMap<Integer, InputPoint> touchPoints = new HashMap<Integer, InputPoint>();

    public ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
    public ArrayList<Integer> validPressedKeys = new ArrayList<Integer>();

    public ArrayList<Integer> textPressedKeys = new ArrayList<Integer>();
    public ArrayList<Integer> textValidPressedKeys = new ArrayList<Integer>();

    public ArrayList<Integer> pressedButtons = new ArrayList<Integer>();
    public ArrayList<Integer> validPressedButtons = new ArrayList<Integer>();

    public boolean validScrollUp;
    public boolean validScrollDown;

    public String os = "";
    public boolean mac = false;

    public boolean vsync;
    public boolean showMouseOnLaunch;

    public boolean touchscreen = false;
    public boolean showKeyboard = false;
    public double keyboardOffset = 0;
    public double keyboardFraction = 1;

    public ArrayList<Long> framesList = new ArrayList<Long>();
    public ArrayList<Double> frameFrequencies = new ArrayList<Double>();
    public long lastFrame = System.currentTimeMillis();
    public double frameFrequency = 1;

    public String name;

    public IDrawer drawer;
    public IUpdater updater;
    public IWindowHandler windowHandler;

    public ArrayList<Transformation> transformations = new ArrayList<Transformation>();

    public double yaw = 0;
    public double pitch = 0;
    public double roll = 0;

    public double xOffset = 0;
    public double yOffset = 0;
    public double zOffset = 0;

    public Transformation baseTransformation = new Translation(this, -0.5, -0.5, -1);

    public BaseSoundPlayer soundPlayer;
    public boolean soundsEnabled = false;

    public BaseVibrationPlayer vibrationPlayer;
    public boolean vibrationsEnabled = false;

    public boolean antialiasingSupported = false;
    public boolean antialiasingEnabled = false;

    public static final HashMap<Integer, String> keyNames = new HashMap<>();

    public BasePlatformHandler platformHandler;

    public BaseWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
    {
        this.name = name;
        this.absoluteWidth = x;
        this.absoluteHeight = y;
        this.absoluteDepth = z;
        this.updater = u;
        this.drawer = d;
        this.vsync = vsync;
        this.windowHandler = w;
        this.showMouseOnLaunch = showMouse;

        if (System.getProperties().toString().contains("Mac OS X"))
            mac = true;

        this.setupKeyCodes();
    }

    public void startTiming()
    {
        long milliTime = System.currentTimeMillis();

        this.framesList.add(milliTime);

        ArrayList<Long> removeList = new ArrayList<Long>();

        for (Long l : this.framesList)
        {
            if (milliTime - l > 1000)
                removeList.add(l);
        }

        for (Long l : removeList)
        {
            this.framesList.remove(l);
        }
    }

    public void stopTiming()
    {
        long time = System.currentTimeMillis();
        long lastFrameTime = lastFrame;
        lastFrame = time;

        double freq =  (time - lastFrameTime) / 10.0;
        frameFrequencies.add(freq);

        if (frameFrequencies.size() > 5)
        {
            frameFrequencies.remove(0);
        }

        double totalFrequency = 0;

        for (Double frequency : frameFrequencies)
        {
            totalFrequency += frequency;
        }

        frameFrequency = Math.max(0, totalFrequency / frameFrequencies.size());
        //frameFrequency = freq;
    }

    public abstract void run();

    public abstract void setShowCursor(boolean show);

    public abstract void setCursorLocked(boolean locked);

    public abstract void setCursorPos(double x, double y);

    public abstract void setFullscreen(boolean enabled);

    public abstract void setIcon(String icon);

    public abstract void fillOval(double x, double y, double sX, double sY);

    public abstract void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillGlow(double x, double y, double sX, double sY);

    public abstract void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillGlow(double x, double y, double sX, double sY, boolean shade);

    public abstract void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade);

    public abstract void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade);

    public abstract void setColor(double r, double g, double b, double a);

    public abstract void setColor(double r, double g, double b);

    public abstract void drawOval(double x, double y, double sX, double sY);

    public abstract void drawOval(double x, double y, double z, double sX, double sY);

    public abstract void fillRect(double x, double y, double sX, double sY);

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ);

    public abstract void fillBox(double x, double y, double z, double sX, double sY, double sZ, byte options);

    public abstract void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4);

    public abstract void fillQuadBox(double x1, double y1,
                                     double x2, double y2,
                                     double x3, double y3,
                                     double x4, double y4,
                                     double z, double sZ,
                                     byte options);

    public abstract void drawRect(double x, double y, double sX, double sY);

    public abstract void setUpPerspective();

    public abstract void applyTransformations();

    public abstract void loadPerspective();

    public abstract void drawImage(double x, double y, double sX, double sY, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest);

    public abstract void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest);

    public abstract String getClipboard();

    public abstract void setClipboard(String s);

    public abstract void setVsync(boolean enable);

    public abstract ArrayList<Integer> getRawTextKeys();

    public abstract String getKeyText(int key);

    public abstract String getTextKeyText(int key);

    public abstract int translateKey(int key);

    public abstract int translateTextKey(int key);

    public abstract void transform(double[] matrix);

    public abstract double getEdgeBounds();

    public abstract void setBatchMode(boolean enabled, boolean quads, boolean depth);

    public abstract void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow);

    public abstract void addVertex(double x, double y, double z);

    public abstract void addVertex(double x, double y);

    public abstract void openLink(URL url) throws Exception;

    public void setupKeyCodes()
    {
        keyNames.put(InputCodes.KEY_UNKNOWN, "Unknown key");
        keyNames.put(InputCodes.KEY_SPACE, "Space");
        keyNames.put(InputCodes.KEY_WORLD_1, "World 1");
        keyNames.put(InputCodes.KEY_WORLD_2, "World 2");
        keyNames.put(InputCodes.KEY_ESCAPE, "Escape");
        keyNames.put(InputCodes.KEY_ENTER, "Enter");
        keyNames.put(InputCodes.KEY_TAB, "Tab");
        keyNames.put(InputCodes.KEY_BACKSPACE, "Backspace");
        keyNames.put(InputCodes.KEY_INSERT, "Insert");
        keyNames.put(InputCodes.KEY_DELETE, "Delete");
        keyNames.put(InputCodes.KEY_RIGHT, "Right");
        keyNames.put(InputCodes.KEY_LEFT, "Left");
        keyNames.put(InputCodes.KEY_DOWN, "Down");
        keyNames.put(InputCodes.KEY_UP, "Up");
        keyNames.put(InputCodes.KEY_PAGE_UP, "Page up");
        keyNames.put(InputCodes.KEY_PAGE_DOWN, "Page down");
        keyNames.put(InputCodes.KEY_HOME, "Home");
        keyNames.put(InputCodes.KEY_END, "End");
        keyNames.put(InputCodes.KEY_CAPS_LOCK, "Caps lock");
        keyNames.put(InputCodes.KEY_SCROLL_LOCK, "Scroll lock");
        keyNames.put(InputCodes.KEY_NUM_LOCK, "Num lock");
        keyNames.put(InputCodes.KEY_PRINT_SCREEN, "Print screen");
        keyNames.put(InputCodes.KEY_PAUSE, "Pause");
        keyNames.put(InputCodes.KEY_F1, "F1");
        keyNames.put(InputCodes.KEY_F2, "F2");
        keyNames.put(InputCodes.KEY_F3, "F3");
        keyNames.put(InputCodes.KEY_F4, "F4");
        keyNames.put(InputCodes.KEY_F5, "F5");
        keyNames.put(InputCodes.KEY_F6, "F6");
        keyNames.put(InputCodes.KEY_F7, "F7");
        keyNames.put(InputCodes.KEY_F8, "F8");
        keyNames.put(InputCodes.KEY_F9, "F9");
        keyNames.put(InputCodes.KEY_F10, "F10");
        keyNames.put(InputCodes.KEY_F11, "F11");
        keyNames.put(InputCodes.KEY_F12, "F12");
        keyNames.put(InputCodes.KEY_F13, "F13");
        keyNames.put(InputCodes.KEY_F14, "F14");
        keyNames.put(InputCodes.KEY_F15, "F15");
        keyNames.put(InputCodes.KEY_F16, "F16");
        keyNames.put(InputCodes.KEY_F17, "F17");
        keyNames.put(InputCodes.KEY_F18, "F18");
        keyNames.put(InputCodes.KEY_F19, "F19");
        keyNames.put(InputCodes.KEY_F20, "F20");
        keyNames.put(InputCodes.KEY_F21, "F21");
        keyNames.put(InputCodes.KEY_F22, "F22");
        keyNames.put(InputCodes.KEY_F23, "F23");
        keyNames.put(InputCodes.KEY_F24, "F24");
        keyNames.put(InputCodes.KEY_F25, "F25");
        keyNames.put(InputCodes.KEY_KP_0, "Keypad 0");
        keyNames.put(InputCodes.KEY_KP_1, "Keypad 1");
        keyNames.put(InputCodes.KEY_KP_2, "Keypad 2");
        keyNames.put(InputCodes.KEY_KP_3, "Keypad 3");
        keyNames.put(InputCodes.KEY_KP_4, "Keypad 4");
        keyNames.put(InputCodes.KEY_KP_5, "Keypad 5");
        keyNames.put(InputCodes.KEY_KP_6, "Keypad 6");
        keyNames.put(InputCodes.KEY_KP_7, "Keypad 7");
        keyNames.put(InputCodes.KEY_KP_8, "Keypad 8");
        keyNames.put(InputCodes.KEY_KP_9, "Keypad 9");
        keyNames.put(InputCodes.KEY_KP_DECIMAL, "Keypad decimal");
        keyNames.put(InputCodes.KEY_KP_DIVIDE, "Keypad divide");
        keyNames.put(InputCodes.KEY_KP_MULTIPLY, "Keypad multiply");
        keyNames.put(InputCodes.KEY_KP_SUBTRACT, "Keypad subtract");
        keyNames.put(InputCodes.KEY_KP_ADD, "Keypad add");
        keyNames.put(InputCodes.KEY_KP_ENTER, "Keypad enter");
        keyNames.put(InputCodes.KEY_KP_EQUAL, "Keypad equal");
        keyNames.put(InputCodes.KEY_LEFT_SHIFT, "Left shift");
        keyNames.put(InputCodes.KEY_LEFT_CONTROL, "Left control");
        keyNames.put(InputCodes.KEY_LEFT_ALT, "Left alt");
        keyNames.put(InputCodes.KEY_LEFT_SUPER, "Left super");
        keyNames.put(InputCodes.KEY_RIGHT_SHIFT, "Right shift");
        keyNames.put(InputCodes.KEY_RIGHT_CONTROL, "Right control");
        keyNames.put(InputCodes.KEY_RIGHT_ALT, "Right alt");
        keyNames.put(InputCodes.KEY_RIGHT_SUPER, "Right super");
        keyNames.put(InputCodes.KEY_MENU, "Menu");
    }
}
package basewindow;

import basewindow.transformation.Transformation;
import basewindow.transformation.Translation;

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

    public HashMap<Integer, InputPoint> touchPoints = new HashMap<Integer, InputPoint>();

    public ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
    public ArrayList<Integer> validPressedKeys = new ArrayList<Integer>();

    public ArrayList<Integer> textPressedKeys = new ArrayList<Integer>();
    public ArrayList<Integer> textValidPressedKeys = new ArrayList<Integer>();

    public ArrayList<Integer> pressedButtons = new ArrayList<Integer>();
    public ArrayList<Integer> validPressedButtons = new ArrayList<Integer>();

    public boolean validScrollUp;
    public boolean validScrollDown;

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
    }

    public abstract void run();

    public abstract void setShowCursor(boolean show);

    public abstract void fillOval(double x, double y, double sX, double sY);

    public abstract void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest);

    public abstract void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest);

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

    public abstract void drawImage(double x, double y, double sX, double sY, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled);

    public abstract void setUpPerspective();

    public abstract void applyTransformations();

    public abstract void loadPerspective();

    public abstract void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled);

    public abstract void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest);

    public abstract String getClipboard();

    public abstract void setClipboard(String s);

    public abstract void setVsync(boolean enable);

    public abstract ArrayList<Integer> getRawTextKeys();

    public abstract String getKeyText(int key);

    public abstract int translateKey(int key);

    public abstract int translateTextKey(int key);

    public abstract void transform(double[] matrix);

    public abstract double getEdgeBounds();
}
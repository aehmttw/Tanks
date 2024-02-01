package basewindow;

import basewindow.transformation.ScaleAboutPoint;
import basewindow.transformation.Shear;
import basewindow.transformation.Transformation;
import basewindow.transformation.Translation;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BaseWindow
{
    protected ArrayList<String> overrideLocations = new ArrayList<>();

    public BaseShapeRenderer shapeRenderer;
    public BaseFontRenderer fontRenderer;

    public boolean angled = false;

    public double pointWidth = -1;
    public double pointHeight = -1;

    public double absoluteWidth;
    public double absoluteHeight;
    public double absoluteDepth;

    public double clipMultiplier = 100;
    public double clipDistMultiplier = 1;

    public boolean hasResized;

    public double absoluteMouseX;
    public double absoluteMouseY;

    public boolean constrainMouse;

    public double colorR;
    public double colorG;
    public double colorB;
    public double colorA;
    public double glow;

    public boolean fullscreen;

    public HashMap<Integer, InputPoint> touchPoints = new HashMap<>();

    public ArrayList<Integer> pressedKeys = new ArrayList<>();
    public ArrayList<Integer> validPressedKeys = new ArrayList<>();

    public ArrayList<Integer> textPressedKeys = new ArrayList<>();
    public ArrayList<Integer> textValidPressedKeys = new ArrayList<>();

    public ArrayList<Integer> pressedButtons = new ArrayList<>();
    public ArrayList<Integer> validPressedButtons = new ArrayList<>();

    public ArrayList<Character> inputCodepoints = new ArrayList<>();

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

    public ArrayList<Long> framesList = new ArrayList<>();
    public long lastFrame = System.nanoTime();
    public double frameFrequency = 1;

    public String name;

    public IDrawer drawer;
    public IUpdater updater;
    public IWindowHandler windowHandler;

    public ArrayList<Transformation> transformations = new ArrayList<>();

    public double yaw = 0;
    public double pitch = 0;
    public double roll = 0;

    public double xOffset = 0;
    public double yOffset = 0;
    public double zOffset = 0;

    public Transformation[] baseTransformations = new Transformation[]{new Translation(this, -0.5, -0.5, -1)};
    public Transformation[] lightBaseTransformation = new Transformation[]{new ScaleAboutPoint(this, 0.8, 0.8, 0.8, 0.5, 0.5, 0.5), new Shear(this, 0, 0, 0, 0, 0.5, 0.5)};
    public double[] lightVec = new double[]{-0.66666666, 0.66666666, -0.33333333};

    public BaseSoundPlayer soundPlayer;
    public boolean soundsEnabled = false;

    public BaseVibrationPlayer vibrationPlayer;
    public boolean vibrationsEnabled = false;

    public boolean antialiasingSupported = false;
    public boolean antialiasingEnabled = false;

    public boolean drawingShadow = false;

    public static final HashMap<Integer, String> keyNames = new HashMap<>();

    public BasePlatformHandler platformHandler;

    public ModelPart.ShapeDrawer shapeDrawer;

    public ShaderGroup shaderDefault;

    public ShaderBones shaderBaseBones;
    public ShaderShadowMapBones shaderShadowMapBones;

    public ShaderGroup currentShaderGroup;

    public ShaderProgram currentShader;

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

        this.framesList.add(System.nanoTime());

        ArrayList<Long> removeList = new ArrayList<>();

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
        long time = System.nanoTime();
        long lastFrameTime = lastFrame;
        lastFrame = time;

        frameFrequency = Math.max(0, (time - lastFrameTime) / 10000000.0);
    }

    public abstract void run();

    public abstract void setShowCursor(boolean show);

    public abstract void setCursorLocked(boolean locked);

    public abstract void setCursorPos(double x, double y);

    public abstract void setFullscreen(boolean enabled);

    public abstract void setOverrideLocations(ArrayList<String> loc, BaseFileManager fileManager);

    public abstract void setIcon(String icon);

    public abstract void setColor(double r, double g, double b, double a, double glow);

    public abstract void setColor(double r, double g, double b, double a);

    public abstract void setColor(double r, double g, double b);

    public abstract void setUpPerspective();

    public abstract void applyTransformations();

    public abstract void loadPerspective();

    public abstract void clearDepth();

    public abstract String getClipboard();

    public abstract void setClipboard(String s);

    public abstract void setVsync(boolean enable);

    public abstract ArrayList<Character> getRawTextKeys();

    public abstract String getKeyText(int key);

    public abstract String getTextKeyText(int key);

    public abstract int translateKey(int key);

    public abstract int translateTextKey(int key);

    public abstract void transform(double[] matrix);

    public abstract void calculateBillboard();

    public abstract double getEdgeBounds();

    public abstract void createImage(String image, InputStream in);

    public abstract void setUpscaleImages(boolean upscaleImages);

    public abstract void setTextureCoords(double u, double v);

    public abstract void setTexture(String image);

    public abstract void stopTexture();

    public abstract void addVertex(double x, double y, double z);

    public abstract void addVertex(double x, double y);

    public abstract void openLink(URL url) throws Exception;

    public abstract void setResolution(int x, int y);

    public abstract void setShadowQuality(double quality);

    public abstract double getShadowQuality();

    public abstract void setLighting(double light, double glowLight, double shadow, double glowShadow);

    public abstract void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess);

    public abstract void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess, double minBound, double maxBound, boolean enableNegative);

    public abstract void disableMaterialLights();

    public abstract void setCelShadingSections(float sections);

    public abstract void createLights(ArrayList<double[]> lights, double scale);

    public abstract void addMatrix();

    public abstract void removeMatrix();

    public abstract void setMatrixProjection();

    public abstract void setMatrixModelview();

    public abstract ModelPart createModelPart();

    public abstract ModelPart createModelPart(Model model, ArrayList<ModelPart.Shape> shapes, Model.Material material);

    public abstract PosedModel createPosedModel(Model m);

    public abstract BaseStaticBatchRenderer createStaticBatchRenderer(ShaderGroup shader, boolean color, String texture, boolean normal, int vertices);

    public abstract BaseShapeBatchRenderer createShapeBatchRenderer();

    public abstract BaseShapeBatchRenderer createShapeBatchRenderer(ShaderGroup shader);

    public abstract BaseShaderUtil getShaderUtil(ShaderProgram p);

    public void setShader(ShaderBase s)
    {
        ShaderBase old = null;
        if (this.currentShaderGroup != null)
            old = this.currentShaderGroup.shaderBase;

        this.currentShaderGroup = s.group;
        this.currentShader = s;
        s.set();

        if (old != null)
            s.copyUniformsFrom(old, ShaderBase.class);
    }

    public void setShader(ShaderShadowMap s)
    {
        ShaderShadowMap old = null;
        if (this.currentShaderGroup != null)
            old = this.currentShaderGroup.shaderShadowMap;

        this.currentShaderGroup = s.group;
        this.currentShader = s;
        s.set();

        if (old != null)
            s.copyUniformsFrom(old, ShaderShadowMap.class);
    }

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
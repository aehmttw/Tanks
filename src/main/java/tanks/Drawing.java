package tanks;

import basewindow.*;
import basewindow.transformation.AxisRotation;
import tanks.gui.Button;
import tanks.gui.Joystick;
import tanks.gui.screen.ScreenGame;
import tanks.item.ItemIcon;
import tanks.network.event.EventPlaySound;
import tanks.obstacle.Obstacle;
import tanks.rendering.TerrainRenderer;
import tanks.rendering.TrackRenderer;
import tanks.tank.TankPlayer;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.HashMap;

public class Drawing
{
    protected static boolean initialized = false;
    private final double[] reusableGamePointer = new double[2];

    public double sizeX = 1400;
    public double sizeY = 900;

    public double playerX = sizeX / 2;
    public double playerY = sizeY / 2;

    public double lastPlayerX = sizeX / 2;
    public double lastPlayerY = sizeY / 2;

    public double lastSwitchedPlayerX = sizeX / 2;
    public double lastSwitchedPlayerY = sizeY / 2;
    public double spectateTransitionTime = 0;
    public double spectateTransitionTimeBase = 200;

    public double interfaceScaleZoomDefault = 1;
    public double interfaceScaleZoom = 1;
    public double baseInterfaceSizeX = 1400;
    public double baseInterfaceSizeY = 900;
    public double interfaceSizeX = baseInterfaceSizeX / interfaceScaleZoom;
    public double interfaceSizeY = baseInterfaceSizeY / interfaceScaleZoom;

    public double scale = 1;
    public double unzoomedScale = 1;
    public double interfaceScale = 1;

    public boolean enableMovingCamera = false;
    public boolean enableMovingCameraX = false;
    public boolean enableMovingCameraY = false;

    public int statsHeight = 0;
    public boolean enableStats = false;

    public boolean movingCamera = true;

    public static Drawing drawing;
    public double textSize = 24;
    public double titleSize = 30;

    public double objWidth = 350;
    public double objHeight = 40;
    public double objXSpace = 380;
    public double objYSpace = 60;

    public double fontSize = 1;

    public double currentColorR;
    public double currentColorG;
    public double currentColorB;
    public double currentColorA;
    public double currentGlow = 1;

    public boolean disableFaceRemoval = true;

    public TerrainRenderer terrainRenderer;
    public TrackRenderer trackRenderer;

    public HashMap<String, Model> modelsByDir = new HashMap<>();

    public String[] tooltip = null;

    private Drawing()
    {
    }

    public static void initialize()
    {
        if (!initialized)
            drawing = new Drawing();

        initialized = true;
    }

    public void showStats(boolean stats)
    {
        this.enableStats = stats;

        if (this.enableStats)
            this.statsHeight = 40;
        else
            this.statsHeight = 0;
    }

    public void setInterfaceScaleZoom(double value)
    {
        this.interfaceScaleZoomDefault = value;
        this.interfaceScaleZoom = value;
        this.interfaceSizeX = baseInterfaceSizeX / interfaceScaleZoom;
        this.interfaceSizeY = baseInterfaceSizeY / interfaceScaleZoom;

        TankPlayer.controlStick = new Joystick(150, Drawing.drawing.interfaceSizeY - 150, 200);
        TankPlayer.shootStick = new Joystick(Drawing.drawing.interfaceSizeX - 150, Drawing.drawing.interfaceSizeY - 150, 200);
        TankPlayer.mineButton = new Button(Drawing.drawing.interfaceSizeX - 300, Drawing.drawing.interfaceSizeY - 75, 60, 60, "",
            () -> Drawing.drawing.playVibration("heavyClick"));

        TankPlayer.shootStick.clickIntensities[0] = 1.0;
        TankPlayer.shootStick.mobile = false;
        TankPlayer.shootStick.snap = true;
        TankPlayer.shootStick.colorR = 255;
        TankPlayer.shootStick.colorB = 0;
        TankPlayer.shootStick.name = "aim";
        TankPlayer.mineButton.silent = true;

        if (value > 1)
            ScreenGame.shopOffset = -100;
    }

    public void setColor(double r, double g, double b)
    {
        Game.game.window.setColor(r, g, b);

        this.currentColorR = r;
        this.currentColorG = g;
        this.currentColorB = b;
        this.currentColorA = 255;
        this.currentGlow = 0;
    }

    public void setColor(double r, double g, double b, double a)
    {
        Game.game.window.setColor(r, g, b, a);

        this.currentColorR = r;
        this.currentColorG = g;
        this.currentColorB = b;
        this.currentColorA = a;
        this.currentGlow = 0;
    }

    public void setColor(double r, double g, double b, double a, double glow)
    {
        Game.game.window.setColor(r, g, b, a, glow);

        this.currentColorR = r;
        this.currentColorG = g;
        this.currentColorB = b;
        this.currentColorA = a;
        this.currentGlow = glow;
    }

    public void setColor(Color c)
    {
        Game.game.window.setColor(c.red, c.green, c.blue, c.alpha);

        this.currentColorR = c.red;
        this.currentColorG = c.green;
        this.currentColorB = c.blue;
        this.currentColorA = c.alpha;
        this.currentGlow = 0;
    }

    public void setColor(Color c, double a)
    {
        Game.game.window.setColor(c.red, c.green, c.blue, a);

        this.currentColorR = c.red;
        this.currentColorG = c.green;
        this.currentColorB = c.blue;
        this.currentColorA = a;
        this.currentGlow = 0;
    }

    public void setColor(Color c, double a, double glow)
    {
        Game.game.window.setColor(c.red, c.green, c.blue, a, glow);

        this.currentColorR = c.red;
        this.currentColorG = c.green;
        this.currentColorB = c.blue;
        this.currentColorA = a;
        this.currentGlow = glow;
    }

    public void fillOval(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillOval(double x, double y, double z, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        this.fillOval(x, y, z, sizeX, sizeY, true, true);
    }

    public void fillOval(double x, double y, double z, double sizeX, double sizeY, double oZ)
    {
        this.bindGameCamera();

        this.fillOval(x, y, z, sizeX, sizeY, oZ, true, true);
    }

    public void fillOval(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
        else
            Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
    }

    public void fillOval(double x, double y, double z, double sizeX, double sizeY, double oZ, boolean depthTest, boolean facing)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, oZ, depthTest);
        else
            Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
    }

    public void fillGlow(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, sizeX, sizeY, false);
    }

    public void fillGlow(double x, double y, double sizeX, double sizeY, boolean shade)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, sizeX, sizeY, shade, false);
    }

    public void fillGlow(double x, double y, double sizeX, double sizeY, boolean shade, boolean light)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade, light);
    }

    public void fillGlow(double x, double y, double z, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, z, sizeX, sizeY, true, true, false);
    }

    public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean shade)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, z, sizeX, sizeY, true, true, shade);
    }

    public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, z, sizeX, sizeY, depthTest, facing, false);
    }

    public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade)
    {
        this.bindGameCamera();

        this.fillGlow(x, y, z, sizeX, sizeY, depthTest, facing, shade, false);
    }

    public void fillGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade, boolean light)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
        else
            Game.game.window.shapeRenderer.fillGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
    }

    public void fillLargeGlow(double x, double y, double sizeX, double sizeY, boolean shade, boolean light)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade, light);
    }

    public void fillLargeGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade, boolean light)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
        else
            Game.game.window.shapeRenderer.fillGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
    }

    public void fillForcedOval(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
        else
            Game.game.window.shapeRenderer.fillOval(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
    }

    public void fillForcedOval(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillPartialInterfaceOval(double x, double y, double sizeX, double sizeY, double start, double end)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x - sizeX / 2;
        double drawY = y - sizeY / 2;

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;
        Game.game.window.shapeRenderer.fillPartialOval(drawX, drawY, drawSizeX, drawSizeY, start, end);
    }

    public void fillForcedGlow(double x, double y, double z, double sizeX, double sizeY, boolean depthTest, boolean facing, boolean shade, boolean light)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;

        if (Game.game.window.angled && facing)
            Game.game.window.shapeRenderer.fillFacingGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
        else
            Game.game.window.shapeRenderer.fillGlow(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest, shade, light);
    }

    public void drawOval(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawOval(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillRect(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        fillRoundedRect(x, y, sizeX, sizeY, 0);
    }

    public void fillRect(IBatchRenderableObject o, double x, double y, double sizeX, double sizeY)
    {
        this.fillRect(x, y, sizeX, sizeY);
    }

    public void fillRect(double x, double y, double z, double sizeX, double sizeY, boolean depthTest)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double dZ = z;
        Game.game.window.shapeRenderer.fillRect(drawX, drawY, dZ, drawSizeX, drawSizeY, depthTest);
    }

    public void fillRoundedRect(double x, double y, double sizeX, double sizeY, double borderRadius)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillRoundedRect(drawX, drawY, drawSizeX, drawSizeY, borderRadius);
    }

    public void drawImage(String img, double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, false);
    }

    public void drawImage(double rotation, String img, double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
    }

    public void drawImage(String img, double x, double y, double z, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double drawZ = z;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, false);
    }

    public void drawImage(String img, double x, double y, double z, double sizeX, double sizeY, boolean depth)
    {
        this.bindGameCamera();

        drawImage(img, x, y, z, sizeX, sizeY, depth, 0, 0, 1, 1);
    }

    public void drawImage(String img, double x, double y, double z, double sizeX, double sizeY, boolean depth, double u1, double v1, double u2, double v2)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double drawZ = z;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, u1, v1, u2, v2, "/images/" + img, false, depth);
    }

    public void drawImage(double rotation, String img, double x, double y, double z, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double drawZ = z;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
    }


    public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        this.bindGameCamera();

        double dX1 = x1;
        double dX2 = x2;
        double dX3 = x3;
        double dX4 = x4;

        double dY1 = y1;
        double dY2 = y2;
        double dY3 = y3;
        double dY4 = y4;

        Game.game.window.shapeRenderer.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
    }

    public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ)
    {
        this.bindGameCamera();

        fillQuadBox(x1, y1, x2, y2, x3, y3, x4, y4, z, sZ, (byte) 0);
    }

    public void fillQuadBox(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double z, double sZ, byte options)
    {
        this.bindGameCamera();

        double dX1 = x1;
        double dX2 = x2;
        double dX3 = x3;
        double dX4 = x4;

        double dY1 = y1;
        double dY2 = y2;
        double dY3 = y3;
        double dY4 = y4;

        double dZ = z;
        double dsZ = sZ;

        Game.game.window.shapeRenderer.fillQuadBox(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4, dZ, dsZ, options);
    }

    public void fillInterfaceQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        if (!this.bindInterfaceCamera())
            return;

        double dX1 = x1;
        double dX2 = x2;
        double dX3 = x3;
        double dX4 = x4;

        double dY1 = y1;
        double dY2 = y2;
        double dY3 = y3;
        double dY4 = y4;

        Game.game.window.shapeRenderer.fillQuad(dX1, dY1, dX2, dY2, dX3, dY3, dX4, dY4);
    }

    public boolean isGameRenderPath()
    {
        return Game.game.window.currentCamera == Game.game.window.mainRenderPasses.sceneCamera ||
                Game.game.window.currentCamera == Game.game.window.mainRenderPasses.shadowCamera;
    }

    /** Selects the configured scene camera, or the light camera during shadow rendering. */
    public void bindGameCamera()
    {
        BaseWindow window = Game.game.window;
        if (window.mainRenderPasses.drawingShadow)
        {
            int size = window.mainRenderPasses.getShadowMapSize();
            window.bindCamera(window.mainRenderPasses.shadowCamera, 0, 0, size, size);
        }
        else
            this.bindViewportCamera(window.mainRenderPasses.sceneCamera);
    }

    /** Prepares logical interface coordinates before any scene or UI drawing begins. */
    public void prepareInterfaceCamera()
    {
        BaseWindow window = Game.game.window;
        window.interfaceCameraLogicalWidth = this.baseInterfaceSizeX;
        window.interfaceCameraLogicalHeight = this.baseInterfaceSizeY;
        window.interfaceCameraHeightOffset = this.statsHeight;
        window.interfaceCamera.clearAdjustments();
        window.interfaceCamera.getOrientation().setIdentity();
        window.interfaceCamera.resetToBounds(0, window.interfaceCameraLogicalWidth,
                0, window.interfaceCameraLogicalHeight, window.absoluteDepth);
        window.interfaceCamera.setAspectRatio(window.absoluteWidth / Math.max(1, window.absoluteHeight - this.statsHeight));
        window.interfaceCamera.setViewportBottomExtension(Game.screen.drawBehindInfoBar ?
                this.statsHeight / Math.max(1, window.absoluteHeight - this.statsHeight) : 0);
    }

    /**
     * Selects the configured interface camera. Returns false during shadow generation,
     * when callers must skip interface geometry rather than draw it into the shadow map.
     */
    public boolean bindInterfaceCamera()
    {
        if (Game.game.window.mainRenderPasses.drawingShadow)
            return false;

        this.bindViewportCamera(Game.game.window.interfaceCamera);
        return true;
    }

    private void bindViewportCamera(Camera camera)
    {
        BaseWindow window = Game.game.window;
        int viewportY = 0;
        // The offscreen scene is composited into the stats-bar viewport afterward.
        boolean offscreenScene = window.currentRenderPass == window.mainRenderPasses.drawPass &&
                window.mainRenderPasses.drawToFramebuffer;
        if (!offscreenScene && window.absoluteHeight > 0)
        {
            double heightOffset = camera == window.mainRenderPasses.sceneCamera ? window.sceneCameraHeightOffset : window.interfaceCameraHeightOffset;
            if (camera == window.interfaceCamera && Game.screen.drawBehindInfoBar)
                heightOffset = 0;
            viewportY = (int) Math.round(heightOffset * window.frameBufferHeight / window.absoluteHeight);
        }

        window.bindCamera(camera, 0, viewportY, window.frameBufferWidth,
                Math.max(1, window.frameBufferHeight - viewportY));
    }

    public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        this.bindGameCamera();

        fillBox(x, y, z, sizeX, sizeY, sizeZ, (byte) 0, null);
    }

    public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, String texture)
    {
        this.bindGameCamera();

        fillBox(x, y, z, sizeX, sizeY, sizeZ, (byte) 0, texture);
    }

    public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
    {
        this.bindGameCamera();

        fillBox(x, y, z, sizeX, sizeY, sizeZ, options, null);
    }

    /**
     * Options byte:
     * <p>
     * 0: default
     * <p>
     * +1 hide behind face
     * +2 hide front face
     * +4 hide bottom face
     * +8 hide top face
     * +16 hide left face
     * +32 hide right face
     * <p>
     * +64 draw on top
     */
    public void fillBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options, String texture)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);
        double drawZ = z;

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;
        double drawSizeZ = sizeZ;

        if (!Game.followingCam && !Game.angledView && !disableFaceRemoval)
        {
            if (drawX < Game.game.window.absoluteWidth / 2)
                options = (byte) (options | 16);

            if (drawX > Game.game.window.absoluteWidth / 2)
                options = (byte) (options | 32);

            if (drawY < Game.game.window.absoluteHeight / 2)
                options = (byte) (options | 8);

            if (drawY > Game.game.window.absoluteHeight / 2)
                options = (byte) (options | 4);
        }

        Game.game.window.shapeRenderer.fillBox(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, options, texture);
    }

    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sizeX, double sizeY, double sizeZ)
    {
        fillBox(o, x, y, z, sizeX, sizeY, sizeZ, (byte) 0);
    }

    public void fillBox(IBatchRenderableObject o, double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
    {
        this.terrainRenderer.addBox(o, x - sizeX / 2, y - sizeY / 2, z, sizeX, sizeY, sizeZ, options, false);
    }

    public void fillForcedBox(double x, double y, double z, double sizeX, double sizeY, double sizeZ, byte options)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);
        double drawZ = z;

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;
        double drawSizeZ = sizeZ;

        Game.game.window.shapeRenderer.fillBox(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, options, null);
    }

    /** Draws the flat background outside the play area, rising to z=0 as the level appears. */
    public void fillLevelBackground(double levelSizeX, double levelSizeY)
    {
        this.bindGameCamera();

        Camera.Bounds bounds = Game.game.window.currentCamera.getBoundsAtDepth(0);
        double padding = Game.tile_size;
        double backgroundZ = Obstacle.draw_size - Game.tile_size;

        double left = Math.min(-padding, bounds.left - padding);
        double right = Math.max(levelSizeX + padding, bounds.right + padding);
        double top = Math.min(-padding, bounds.top - padding);
        double bottom = Math.max(levelSizeY + padding, bounds.bottom + padding);

        byte topFaceOnly = (byte) (BaseShapeRenderer.hide_neg_z | BaseShapeRenderer.hide_pos_y |
                BaseShapeRenderer.hide_neg_y | BaseShapeRenderer.hide_neg_x | BaseShapeRenderer.hide_pos_x);
        this.fillForcedBox((left + right) / 2, top / 2, backgroundZ, right - left, -top, 0, topFaceOnly);
        this.fillForcedBox((left + right) / 2, (levelSizeY + bottom) / 2, backgroundZ, right - left, bottom - levelSizeY, 0, topFaceOnly);
        this.fillForcedBox(left / 2, levelSizeY / 2, backgroundZ, -left, levelSizeY, 0, topFaceOnly);
        this.fillForcedBox((levelSizeX + right) / 2, levelSizeY / 2, backgroundZ, right - levelSizeX, levelSizeY, 0, topFaceOnly);
    }

    /** Draws the one-tile-wide level perimeter, from z=0 to Obstacle.draw_size. */
    public void fillLevelBorders(double levelSizeX, double levelSizeY)
    {
        this.bindGameCamera();

        double padding = Game.tile_size;
        double wallHeight = Obstacle.draw_size;

        this.fillForcedBox(levelSizeX / 2, -padding / 2, 0, levelSizeX + padding * 2, padding, wallHeight, (byte) 0);
        this.fillForcedBox(levelSizeX / 2, levelSizeY + padding / 2, 0, levelSizeX + padding * 2, padding, wallHeight, (byte) 0);
        this.fillForcedBox(-padding / 2, levelSizeY / 2, 0, padding, levelSizeY, wallHeight, (byte) 0);
        this.fillForcedBox(levelSizeX + padding / 2, levelSizeY / 2, 0, padding, levelSizeY, wallHeight, (byte) 0);
    }

    public void drawModel(IModel m, double x, double y, double width, double height, double angle)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);

        double drawSizeX = width;
        double drawSizeY = height;

        m.draw(drawX, drawY, drawSizeX, drawSizeY, angle);
    }

    public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, double angle)
    {
        this.bindGameCamera();

        drawModel(m, x, y, z, width, height, depth, angle, true);
    }

    public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, double angle, boolean depthTest)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);
        double drawZ = z;

        double drawSizeX = width;
        double drawSizeY = height;
        double drawSizeZ = depth;

        m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, angle, 0, 0, true);
    }

    public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw, double pitch, double roll)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);
        double drawZ = z;

        double drawSizeX = width;
        double drawSizeY = height;
        double drawSizeZ = depth;

        m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, yaw, pitch, roll, true);
    }

    public void drawModel(IModel m, double x, double y, double z, double width, double height, double depth, AxisRotation[] axisRotations)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);
        double drawZ = z;

        double drawSizeX = width;
        double drawSizeY = height;
        double drawSizeZ = depth;

        m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, axisRotations, true);
    }

    public void drawInterfaceModel(IModel m, double x, double y, double width, double height, double angle)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawSizeX = width;
        double drawSizeY = height;

        m.draw(drawX, drawY, drawSizeX, drawSizeY, angle);
    }

    public void drawInterfaceModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw)
    {
        if (!this.bindInterfaceCamera())
            return;

        this.drawInterfaceModel(m, x, y, z, width, height, depth, yaw, 0, 0);
    }

    public void drawInterfaceModel(IModel m, double x, double y, double z, double width, double height, double depth, double yaw, double pitch, double roll)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawZ = z;

        double drawSizeX = width;
        double drawSizeY = height;
        double drawSizeZ = depth;

        m.draw(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ, yaw, pitch, roll, true);
    }

    public void drawInterfaceModel2D(IModel m, double x, double y, double z, double width, double height, double depth)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawZ = z;

        double drawSizeX = width;
        double drawSizeY = height;
        double drawSizeZ = depth;

        m.draw2D(drawX, drawY, drawZ, drawSizeX, drawSizeY, drawSizeZ);
    }

    public void drawRect(double x, double y, double sizeX, double sizeY)
    {
        this.bindGameCamera();

        drawRect(x, y, sizeX, sizeY, 1, 0);
    }

    public void drawRect(double x, double y, double sizeX, double sizeY, double borderWidth)
    {
        this.bindGameCamera();

        drawRect(x, y, sizeX, sizeY, borderWidth, 0);
    }

    public void drawRect(double x, double y, double sizeX, double sizeY, double borderWidth, double borderRadius)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        double drawBorderWidth = borderWidth;
        double drawBorderRadius = borderRadius;

        Game.game.window.shapeRenderer.drawRect(drawX, drawY, drawSizeX, drawSizeY, drawBorderWidth, drawBorderRadius);
    }

    public void fillInterfaceOval(double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillInterfaceOval(double x, double y, double z, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;
        double drawZ = z;

        Game.game.window.shapeRenderer.fillOval(drawX, drawY, drawZ, drawSizeX, drawSizeY, false);
    }

    public void fillInterfacePartialRing(double x, double y, double size, double thickness, double frac)
    {
        if (!this.bindInterfaceCamera())
            return;

        fillInterfacePartialRing(x, y, size, thickness, 0, frac);
    }

    public void fillInterfacePartialRing(double x, double y, double size, double thickness, double start, double frac)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawSize = size;
        double drawThickness = thickness;

        Game.game.window.shapeRenderer.fillPartialRing(drawX, drawY, drawSize, drawThickness, start, start + frac);
    }

    public void fillPartialRing(double x, double y, double z, double size, double thickness, double start, double frac)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);
        double drawZ = z;
        double drawSize = size;
        double drawThickness = thickness;

        Game.game.window.shapeRenderer.fillPartialRing(drawX, drawY, drawZ, drawSize, drawThickness, start, start + frac);
    }

    public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillInterfaceGlow(double x, double y, double z, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;
        double drawZ = z;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawZ, drawSizeX, drawSizeY, false);
    }

    public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY, boolean shade)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade);
    }

    public void fillInterfaceGlow(double x, double y, double sizeX, double sizeY, boolean shade, boolean light)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillGlow(drawX, drawY, drawSizeX, drawSizeY, shade, light);
    }

    public void addInterfaceVertexRotated(double x, double y, double z, double ox, double oy, double rot)
    {
        if (!this.bindInterfaceCamera())
            return;

        Drawing.drawing.addInterfaceVertex(x + Math.cos(rot) * ox + Math.sin(rot) * oy, y - Math.sin(rot) * ox + Math.cos(rot) * oy, z);
    }

    public void fillInterfaceGlowSparkle(double x, double y, double z, double size, double angle)
    {
        if (!this.bindInterfaceCamera())
            return;

        double size2 = size * 0.05;
        Game.game.window.shapeRenderer.setBatchMode(true, true, false, true);

        double r = this.currentColorR;
        double g = this.currentColorG;
        double b = this.currentColorB;
        double a = this.currentColorA;

        Drawing.drawing.setColor(r, g, b, 0);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, size, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, size, angle);
        Drawing.drawing.setColor(r, g, b, a);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

        Drawing.drawing.setColor(r, g, b, 0);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, -size, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, -size, angle);
        Drawing.drawing.setColor(r, g, b, a);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

        angle += Math.PI / 2;
        Drawing.drawing.setColor(r, g, b, 0);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, size, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, size, angle);
        Drawing.drawing.setColor(r, g, b, a);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

        Drawing.drawing.setColor(r, g, b, 0);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, -size, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, -size, angle);
        Drawing.drawing.setColor(r, g, b, a);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, size2, 0, angle);
        Drawing.drawing.addInterfaceVertexRotated(x, y, z, -size2, 0, angle);

        Game.game.window.shapeRenderer.setBatchMode(false, true, false, false);

    }

    public void drawInterfaceOval(double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawOval(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillInterfaceRect(double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void fillInterfaceViewportRect()
    {
        if (!this.bindInterfaceCamera())
            return;

        this.fillInterfaceRect(
                this.interfaceSizeX / 2,
                this.interfaceSizeY / 2,
                Game.game.window.interfaceCamera.getCurrentSizeX() + 1,
                Game.game.window.interfaceCamera.getCurrentSizeY() + 1
        );
    }

    public void fillInterfaceRoundedRect(double x, double y, double sizeX, double sizeY, double borderRadius)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillRoundedRect(drawX, drawY, drawSizeX, drawSizeY, borderRadius);
    }

    public void fillInterfaceProgressRect(double x, double y, double sizeX, double sizeY, double progress)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);
        double drawSizeX = (sizeX * progress);
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.fillRect(drawX, drawY, drawSizeX, drawSizeY);
    }

    public void drawInterfaceImage(ItemIcon img, double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        img.drawInterfaceImage(x, y, sizeX, sizeY);
    }

    public void drawInterfaceImage(String img, double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        boolean text = img.startsWith("text:");

        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        if (text)
        {
            img = img.substring(5);
            sizeX = Game.game.window.fontRenderer.getStringSizeX(drawSizeX / 36, img);
            sizeY = Game.game.window.fontRenderer.getStringSizeY(drawSizeY / 36, img);
        }

        double drawX = (x - sizeX / 2);
        double drawY = (y - sizeY / 2);

        if (!text)
        {
            Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, false);
        }
        else
        {
            Drawing.drawing.setColor(0, 0, 0);
            Game.game.window.fontRenderer.drawString(drawX, drawY, drawSizeX / 36, drawSizeY / 36, img);
        }
    }

    public void drawInterfaceImage(double rotation, String img, double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
    }

    public void drawInterfaceImage(double rotation, String img, double x, double y, double z, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawZ = z;
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawImage(drawX, drawY, drawZ, drawSizeX, drawSizeY, "/images/" + img, rotation, false);
    }

    public void drawInterfaceRect(double x, double y, double sizeX, double sizeY)
    {
        if (!this.bindInterfaceCamera())
            return;

        drawInterfaceRect(x, y, sizeX, sizeY, 0, 0);
    }

    public void drawInterfaceRect(double x, double y, double sizeX, double sizeY, double lineWidth)
    {
        if (!this.bindInterfaceCamera())
            return;

        drawInterfaceRect(x, y, sizeX, sizeY, lineWidth, 0);
    }

    public void drawInterfaceRect(double x, double y, double sizeX, double sizeY, double lineWidth, double borderRadius)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = (x - sizeX / 2 - lineWidth);
        double drawY = (y - sizeY / 2 - lineWidth);
        double drawSizeX = sizeX;
        double drawSizeY = sizeY;

        Game.game.window.shapeRenderer.drawRect(drawX, drawY, drawSizeX, drawSizeY, lineWidth, borderRadius);
    }

    public double getInterfaceTextWidth(String s)
    {
        double width = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s);

        return width;
    }

    public double getInterfaceTextHeight(String s)
    {
        double height = Game.game.window.fontRenderer.getStringSizeY(Drawing.drawing.fontSize, s);

        return height;
    }

    public void drawText(double x, double y, String text)
    {
        this.bindGameCamera();

        double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
        double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);

        Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
    }

    public void drawText(double x, double y, double z, String text)
    {
        this.bindGameCamera();

        drawText(x, y, z, text, true);
    }

    public void drawText(double x, double y, double z, String text, boolean depth)
    {
        this.bindGameCamera();

        double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
        double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

        double drawX = gameToAbsoluteX(x, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);
        double drawZ = z;

        Game.game.window.fontRenderer.drawString(drawX, drawY, drawZ, this.fontSize, this.fontSize, text, depth);
    }

    public void drawText(double x, double y, double z, String text, boolean rightAligned, boolean depth)
    {
        this.bindGameCamera();

        double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
        double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

        double offX = sizeX;

        if (!rightAligned)
            offX = 0;

        double drawX = gameToAbsoluteX(x - offX, sizeX);
        double drawY = gameToAbsoluteY(y, sizeY);
        double drawZ = z;

        Game.game.window.fontRenderer.drawString(drawX, drawY, drawZ, this.fontSize, this.fontSize, text, depth);
    }

    public void drawInterfaceText(double x, double y, String text)
    {
        if (!this.bindInterfaceCamera())
            return;

        double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
        double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

        double drawX = x - sizeX / 2;
        double drawY = y - sizeY / 2;

        Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);

        if (Game.game.window.fontRenderer.drawBox)
        {
            Drawing.drawing.setColor(0, 255, 0);
            Game.game.window.shapeRenderer.drawRect(drawX, drawY, sizeX, sizeY);
        }
    }

    public void drawInterfaceText(double x, double y, String text, boolean rightAligned)
    {
        if (!this.bindInterfaceCamera())
            return;

        double sizeX = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
        double sizeY = Game.game.window.fontRenderer.getStringSizeY(this.fontSize, text);

        double offX = sizeX;

        if (!rightAligned)
            offX = 0;

        double drawX = x - offX;
        double drawY = y - sizeY / 2;
        Game.game.window.fontRenderer.drawString(drawX, drawY, this.fontSize, this.fontSize, text);
    }

    public void drawUncenteredInterfaceText(double x, double y, String text)
    {
        if (!this.bindInterfaceCamera())
            return;

        Game.game.window.fontRenderer.drawString(x, y, this.fontSize, this.fontSize, text);
    }

    public void displayInterfaceText(double x, double y, String text, Object... objects)
    {
        if (!this.bindInterfaceCamera())
            return;

        drawInterfaceText(x, y, Translation.translate(text, objects));
    }

    public void displayInterfaceText(double x, double y, boolean rightAligned, String text, Object... objects)
    {
        if (!this.bindInterfaceCamera())
            return;

        drawInterfaceText(x, y, Translation.translate(text, objects), rightAligned);
    }

    public void displayUncenteredInterfaceText(double x, double y, String text, Object... objects)
    {
        if (!this.bindInterfaceCamera())
            return;

        drawUncenteredInterfaceText(x, y, Translation.translate(text, objects));
    }

    public double getStringWidth(String s)
    {
        double width = Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s);

        return width;
    }

    public double getStringHeight(String s)
    {
        double height = Game.game.window.fontRenderer.getStringSizeY(Drawing.drawing.fontSize, s);

        return height;
    }

    public void setFontSize(double size)
    {
        this.fontSize = size / 36.0;
    }

    public void setInterfaceFontSize(double size)
    {
        this.fontSize = size / 36.0;
    }

    public void setBoundedInterfaceFontSize(double maxFontSize, double width, String text)
    {
        this.setInterfaceFontSize(maxFontSize);

        if (width > 0)
        {
            double sizeRaw = Game.game.window.fontRenderer.getStringSizeX(this.fontSize, text);
            if (sizeRaw > 0)
                this.setInterfaceFontSize(maxFontSize * Math.min(1, width / sizeRaw));
        }
    }

    public void addVertex(double x, double y, double z)
    {
        this.bindGameCamera();

        double drawX = gameToAbsoluteX(x, 0);
        double drawY = gameToAbsoluteY(y, 0);
        double drawZ = z;

        if (Game.enable3d)
            Game.game.window.addVertex(drawX, drawY, drawZ);
        else
            Game.game.window.addVertex(drawX, drawY);
    }

    public void addInterfaceVertex(double x, double y, double z)
    {
        if (!this.bindInterfaceCamera())
            return;

        double drawX = x;
        double drawY = y;
        double drawZ = z;

        if (Game.enable3d)
            Game.game.window.addVertex(drawX, drawY, drawZ);
        else
            Game.game.window.addVertex(drawX, drawY);
    }

    public void addFacingVertex(double x, double y, double z, double sX, double sY, double sZ)
    {
        this.bindGameCamera();

        if (Game.enable3d)
        {
            double[][] inverse = Game.game.window.mainRenderPasses.sceneCamera.inverseView.values;
            addVertex(x + inverse[0][0] * sX + inverse[0][1] * sY + inverse[0][2] * sZ,
                    y + inverse[1][0] * sX + inverse[1][1] * sY + inverse[1][2] * sZ,
                    z + inverse[2][0] * sX + inverse[2][1] * sY + inverse[2][2] * sZ);
        }
        else
            addVertex(x + sX, y + sY, z + sZ);
    }

    public void setLightingUI()
    {
        this.setLighting(Level.currentLightIntensity, Math.max(Level.currentLightIntensity * 0.75, Level.currentShadowIntensity), Level.currentLightColor);
    }

    public void setLighting(double light, double shadow)
    {
        Game.drawer.lightsPass.light = light;
        Game.drawer.lightsPass.shadow = shadow;
        Game.game.window.mainRenderPasses.light = light;
        Game.game.window.mainRenderPasses.shadow = shadow;
    }

    public void setLighting(double light, double shadow, Color color)
    {
        this.setLighting(light, shadow);
        Game.drawer.lightsPass.setLightColor(color);
        Game.game.window.mainRenderPasses.setLightColor(color);
    }

    public void drawTooltip(String[] text)
    {
        if (!this.bindInterfaceCamera())
            return;

        this.tooltip = text;
    }

    public void drawTooltip(String[] text, double x, double y)
    {
        if (!this.bindInterfaceCamera())
            return;

        double xPadding = 16;
        double yPadding = 8;

        setInterfaceFontSize(14);

        double sizeX = 0;
        double sizeY = 0;
        for (String s: text)
        {
            sizeX = Math.max(sizeX, this.getInterfaceTextWidth(s));
            sizeY = Math.max(sizeY, this.getInterfaceTextHeight(s));
        }

        double popupWidth = sizeX + xPadding * 2;
        double popupHeight = sizeY + yPadding * 2 * text.length;
        double minX = this.getInterfaceEdgeX(false);
        double maxX = this.getInterfaceEdgeX(true);
        double minY = this.getInterfaceEdgeY(false);
        double maxY = this.getInterfaceEdgeY(true);

        if (x + popupWidth > maxX)
            x -= x + popupWidth - maxX;

        if (x < minX)
            x = minX;

        if (y + popupHeight > maxY)
            y -= y + popupHeight - maxY;

        if (y < minY)
            y = minY;

        double drawX = x + popupWidth / 2.0;
        double drawY = y + popupHeight / 2.0;

        setColor(0, 0, 0, 127);
        drawPopup(drawX, drawY, popupWidth, popupHeight, 5, 4);

        setColor(255, 255, 255);
        for (int i = 0; i < text.length; i++)
        {
            drawUncenteredInterfaceText(x + xPadding, y + yPadding * (2 * i + 1), text[i]);
        }

    }

    public void drawPopup(double x, double y, double sX, double sY)
    {
        if (!this.bindInterfaceCamera())
            return;

        this.drawPopup(x, y, sX, sY, 10, 5);
    }

    public void drawPopup(double x, double y, double sX, double sY, double borderWidth, double borderRadius)
    {
        if (!this.bindInterfaceCamera())
            return;

        fillInterfaceRoundedRect(x, y, sX - borderWidth * 2, sY - borderWidth * 2, borderRadius);
        fillInterfaceRoundedRect(x, y, sX, sY, borderRadius);
        Drawing.drawing.setColor(255, 255, 255);
    }

    public void drawConcentricPopup(double x, double y, double sX, double sY, double borderWidth, double borderRadius)
    {
        if (!this.bindInterfaceCamera())
            return;

        fillInterfaceRoundedRect(x, y, sX - borderWidth * 2, sY - borderWidth * 2, borderRadius * (1.0 - (borderWidth / borderRadius)));
        fillInterfaceRoundedRect(x, y, sX, sY, borderRadius);
    }

    public void playMusic(String sound, float volume, boolean looped, String id, long fadeTime)
    {
        if (Game.game.window.soundsEnabled && Game.musicEnabled)
            Game.game.window.soundPlayer.playMusic("/music/" + sound, volume, looped, id, fadeTime);
    }

    public void playMusic(String sound, float volume, boolean looped, String id, long fadeTime, boolean stoppable)
    {
        if (Game.game.window.soundsEnabled && Game.musicEnabled)
            Game.game.window.soundPlayer.playMusic("/music/" + sound, volume, looped, id, fadeTime, stoppable);
    }

    public void stopMusic()
    {
        if (Game.game.window.soundsEnabled)
            Game.game.window.soundPlayer.stopMusic();
    }

    public void addSyncedMusic(String sound, float volume, boolean looped, long fadeTime)
    {
        if (Game.game.window.soundsEnabled && Game.musicEnabled && Game.enableLayeredMusic)
            Game.game.window.soundPlayer.addSyncedMusic("/music/" + sound, volume, looped, fadeTime);
    }

    public void removeSyncedMusic(String sound, long fadeTime)
    {
        if (Game.game.window.soundsEnabled && Game.musicEnabled && Game.enableLayeredMusic)
            Game.game.window.soundPlayer.removeSyncedMusic("/music/" + sound, fadeTime);
    }

    public void playSound(String sound)
    {
        if (Game.game.window.soundsEnabled && Game.soundsEnabled)
            Game.game.window.soundPlayer.playSound("/sounds/" + sound, 1, Game.soundVolume);
    }

    public void playSound(String sound, float pitch)
    {
        if (Game.game.window.soundsEnabled && Game.soundsEnabled)
            Game.game.window.soundPlayer.playSound("/sounds/" + sound, pitch, Game.soundVolume);
    }

    public void playSound(String sound, float volume, boolean asMusic)
    {
        if (Game.game.window.soundsEnabled)
        {
            if (asMusic)
            {
                if (Game.musicEnabled)
                    Game.game.window.soundPlayer.playSound("/music/" + sound, 1.0f, volume * Game.musicVolume);
            }
            else if (Game.soundsEnabled)
                Game.game.window.soundPlayer.playSound("/sound/" + sound, 1.0f, volume * Game.soundVolume);
        }
    }

    public void playSound(String sound, float pitch, float volume)
    {
        if (Game.game.window.soundsEnabled && Game.soundsEnabled)
            Game.game.window.soundPlayer.playSound("/sounds/" + sound, pitch, volume * Game.soundVolume);
    }

    public void playGlobalSound(String sound)
    {
        this.playSound(sound);
        Game.eventsOut.add(new EventPlaySound(sound, 1, 1));
    }

    public void playGlobalSound(String sound, float pitch)
    {
        this.playSound(sound, pitch);
        Game.eventsOut.add(new EventPlaySound(sound, pitch, 1));
    }

    public void playGlobalSound(String sound, float pitch, float volume)
    {
        this.playSound(sound, pitch, volume);
        Game.eventsOut.add(new EventPlaySound(sound, pitch, volume));
    }

    public void playVibration(String vibration)
    {
        if (!Game.game.window.vibrationsEnabled || !Game.enableVibrations)
            return;

        switch (vibration)
        {
            case "click":
                Game.game.window.vibrationPlayer.click();
                break;
            case "heavyClick":
                Game.game.window.vibrationPlayer.heavyClick();
                break;
            case "selectionChanged":
                Game.game.window.vibrationPlayer.selectionChanged();
                break;
            default:
                break;
        }
    }

    public double toGameCoordsX(double x)
    {
        return absoluteToGameX(interfaceToAbsoluteX(x));
    }

    public double toGameCoordsY(double y)
    {
        return absoluteToGameY(interfaceToAbsoluteY(y));
    }

    public double gameToInterfaceCoordsX(double x)
    {
        return absoluteToInterfaceX(gameToAbsoluteX(x, 0));

    }

    public double gameToInterfaceCoordsY(double y)
    {
        return absoluteToInterfaceY(gameToAbsoluteY(y, 0));

    }

    public double getMouseX()
    {
        if (Game.angledView)
            return getGameMouseX();

        return toGameCoordsX(getInterfaceMouseX());
    }

    public double getMouseY()
    {
        if (Game.angledView)
            return getGameMouseY();

        return toGameCoordsY(getInterfaceMouseY());
    }

    /** Returns game X at z=0 using window X and the current mouse Y. */
    public double getGamePointerX(double x)
    {
        getGamePointer(x, Game.game.window.absoluteMouseY, this.reusableGamePointer);
        return this.reusableGamePointer[0];
    }

    /** Returns game Y at z=0 using window Y and the current mouse X. */
    public double getGamePointerY(double y)
    {
        getGamePointer(Game.game.window.absoluteMouseX, y, this.reusableGamePointer);
        return this.reusableGamePointer[1];
    }

    /** Converts both window-pixel pointer coordinates to game coordinates on z=0. */
    public void getGamePointer(double x, double y, double[] result)
    {
        double width = Math.max(1, Game.game.window.absoluteWidth);
        double height = Math.max(1, Game.game.window.absoluteHeight - statsHeight);
        Game.game.window.mainRenderPasses.sceneCamera.getWorldPointAtDepth(x / width, y / height, 0, result);
    }

    public double getGameMouseX()
    {
        return getGamePointerX(Game.game.window.absoluteMouseX);
    }

    public double getGameMouseY()
    {
        return getGamePointerY(Game.game.window.absoluteMouseY);
    }

    public double getInterfaceMouseX()
    {
        return (Game.game.window.absoluteMouseX - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale;
    }

    public double getInterfaceMouseY()
    {
        return (Game.game.window.absoluteMouseY - Math.max(0, Panel.windowHeight - this.statsHeight - interfaceSizeY * interfaceScale) / 2) / interfaceScale;
    }

    public double getInterfacePointerX(double x)
    {
        return (x - Math.max(0, Panel.windowWidth - interfaceSizeX * interfaceScale) / 2) / interfaceScale;
    }

    public double getInterfacePointerY(double y)
    {
        return (y - Math.max(0, Panel.windowHeight - this.statsHeight - interfaceSizeY * interfaceScale) / 2) / interfaceScale;
    }

    public void setScreenBounds(double x, double y)
    {
        sizeX = x;
        sizeY = y;
        Game.currentSizeX = (int) (x / Game.tile_size);
        Game.currentSizeY = (int) (y / Game.tile_size);
    }

    public double getPlayerOffsetX()
    {
        if (!enableMovingCameraX && !Game.followingCam)
            return 0;

        double x = this.getTrackedPlayerX();

        double result = (x - (Panel.windowWidth) / scale / 2);

        double margin = Obstacle.draw_size / Game.tile_size *
            Math.max(0, Math.min(Game.tile_size * 2, Game.currentSizeX * Game.tile_size * Drawing.drawing.scale - Panel.windowWidth)) / 2;

        boolean less = result < -margin;
        boolean greater = result + (Panel.windowWidth) / scale > sizeX + margin;

        if (scale * Game.currentSizeX * Game.tile_size + margin > Panel.windowWidth)
        {
            if (Game.followingCam)
                return -result * Panel.panel.zoomTimer;
            else if (less && !greater)
                return margin;
            else if (greater && !less)
                return -margin - (sizeX - (Panel.windowWidth) / scale);
            else
                return 0 - result;
        }
        else
        {
            if (Game.followingCam)
                return -result * Panel.panel.zoomTimer;
            else if (less && !greater)
                return margin;
            else if (greater && !less)
                return -margin;
            else
                return 0;
        }

        //return 0 - result;
    }

    public double getPlayerOffsetY()
    {
        if (!enableMovingCameraY && !Game.followingCam)
            return 0;

        double y = this.getTrackedPlayerY();

        double result = (y - (Panel.windowHeight - statsHeight) / scale / 2);

        double margin = Math.max(0, Math.min(Game.tile_size * 2, Game.currentSizeY * Game.tile_size *
            Drawing.drawing.scale - (Panel.windowHeight - Drawing.drawing.statsHeight))) / 2;

        boolean less = result < -margin;
        boolean greater = result + (Panel.windowHeight - statsHeight) / scale > sizeY + margin;

        if (scale * Game.currentSizeY * Game.tile_size + margin > Panel.windowHeight - statsHeight)
        {
            if (Game.followingCam)
                return -result * Panel.panel.zoomTimer;
            else if (less && !greater)
                return margin;
            else if (greater && !less)
                return -margin - (sizeY - (Panel.windowHeight - statsHeight) / scale);
            else
                return 0 - result;
        }
        else
        {
            if (Game.followingCam)
                return -result * Panel.panel.zoomTimer;
            else if (less && !greater)
                return margin;
            else if (greater && !less)
                return -margin;
            else
                return 0;
        }

        //return 0 - result;
    }

    public double getTrackedPlayerX()
    {
        double x = playerX;

        if (Panel.panel.pastPlayerTime.size() == 1)
            x = Panel.panel.pastPlayerX.get(0);
        else if (Panel.panel.pastPlayerTime.size() > 1)
        {
            double frac = Math.min(1, (Panel.panel.age - getTrackOffset() - Panel.panel.pastPlayerTime.get(0)) /
                    (Panel.panel.pastPlayerTime.get(1) - Panel.panel.pastPlayerTime.get(0)));
            x = Panel.panel.pastPlayerX.get(0) * (1 - frac) + Panel.panel.pastPlayerX.get(1) * frac;
        }

        lastPlayerX = x;
        return x;
    }

    public double getTrackedPlayerY()
    {
        double y = playerY;

        if (Panel.panel.pastPlayerTime.size() == 1)
            y = Panel.panel.pastPlayerY.get(0);
        else if (Panel.panel.pastPlayerTime.size() > 1)
        {
            double frac = Math.min(1, (Panel.panel.age - getTrackOffset() - Panel.panel.pastPlayerTime.get(0)) /
                    (Panel.panel.pastPlayerTime.get(1) - Panel.panel.pastPlayerTime.get(0)));
            y = Panel.panel.pastPlayerY.get(0) * (1 - frac) + Panel.panel.pastPlayerY.get(1) * frac;
        }

        lastPlayerY = y;
        return y;
    }

    public double getHorizontalInterfaceMargin()
    {
        double usableHeight = Math.max(1, Game.game.window.absoluteHeight - statsHeight);
        double aspect = Game.game.window.absoluteWidth / usableHeight;
        double baseAspect = Drawing.drawing.interfaceSizeX / Drawing.drawing.interfaceSizeY;

        if (aspect > baseAspect)
            return (Drawing.drawing.interfaceSizeY * aspect - Drawing.drawing.interfaceSizeX) / 2;

        return 0;
    }

    public double getVerticalInterfaceMargin()
    {
        double usableHeight = Math.max(1, Game.game.window.absoluteHeight - statsHeight);
        double aspect = Game.game.window.absoluteWidth / usableHeight;
        double baseAspect = Drawing.drawing.interfaceSizeX / Drawing.drawing.interfaceSizeY;

        if (aspect < baseAspect)
            return (Drawing.drawing.interfaceSizeX / aspect - Drawing.drawing.interfaceSizeY) / 2;

        return 0;
    }

    public double getInterfaceScaleFactor()
    {
        return Math.min(
                Game.game.window.absoluteWidth / Drawing.drawing.interfaceSizeX,
                Math.max(1, Game.game.window.absoluteHeight - statsHeight) / Drawing.drawing.interfaceSizeY
        );
    }

    /** Gets interface coordinate position of left/right edge of screen */
    public double getInterfaceEdgeX(boolean right)
    {
        double margin = this.getHorizontalInterfaceMargin();
        double edgeBounds = Game.game.window.getEdgeBounds() / this.getInterfaceScaleFactor();

        if (right)
            return margin + Drawing.drawing.interfaceSizeX - edgeBounds;
        else
            return -margin + edgeBounds;
    }

    /** Gets interface coordinate position of top/bottom edge of screen */
    public double getInterfaceEdgeY(boolean bottom)
    {
        double margin = this.getVerticalInterfaceMargin();

        if (bottom)
            return margin + Drawing.drawing.interfaceSizeY;
        else
            return -margin;
    }


    public double gameToAbsoluteX(double x, double sizeX)
    {
        if (this.isGameRenderPath())
            return x - sizeX / 2;

        if (Game.screen.enableMargins)
            return (scale * (x + Game.screen.getOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2);
        else
            return scale * (x + Game.screen.getOffsetX() - sizeX / 2);
    }

    public double gameToAbsoluteY(double y, double sizeY)
    {
        if (this.isGameRenderPath())
            return y - sizeY / 2;

        if (Game.screen.enableMargins)
            return (scale * (y + Game.screen.getOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2);
        else
            return scale * (y + Game.screen.getOffsetY() - sizeY / 2);
    }

    public double absoluteToGameX(double x)
    {
        if (Game.screen.enableMargins)
            return (x - Math.max(0, Panel.windowWidth - this.sizeX * scale) / 2) / scale - Game.screen.getOffsetX();
        else
            return x / scale - Game.screen.getOffsetX();
    }

    public double absoluteToGameY(double y)
    {
        if (Game.screen.enableMargins)
            return (y - Math.max(0, Panel.windowHeight - statsHeight - this.sizeY * scale) / 2) / scale - Game.screen.getOffsetY();
        else
            return y / scale - Game.screen.getOffsetY();
    }

    public double interfaceToAbsoluteX(double x)
    {
        return (x + this.getHorizontalInterfaceMargin()) * this.getInterfaceScaleFactor();
    }

    public double interfaceToAbsoluteY(double y)
    {
        return (y + this.getVerticalInterfaceMargin()) * this.getInterfaceScaleFactor();
    }

    public double absoluteToInterfaceX(double x)
    {
        return x / this.getInterfaceScaleFactor() - this.getHorizontalInterfaceMargin();
    }

    public double absoluteToInterfaceY(double y)
    {
        return y / this.getInterfaceScaleFactor() - this.getVerticalInterfaceMargin();
    }

    public double getTrackOffset()
    {
        if (Game.followingCam)
            return 0;
        else
            return 20;
    }

    public ArrayList<String> wrapText(String msg, double max, double fontSize)
    {
        Drawing.drawing.setInterfaceFontSize(fontSize);

        ArrayList<String> lines = new ArrayList<>();
        StringBuilder l = new StringBuilder();

        boolean first = true;
        for (String s: msg.split(" "))
        {
            if (s.equals("\n"))
            {
                lines.add(l.toString());
                l = new StringBuilder();
                first = true;
                continue;
            }

            if (Drawing.drawing.getInterfaceTextWidth(l + " " + s) <= max)
            {
                if (!first)
                    l.append(" ");

                l.append(s);
            }
            else if (Drawing.drawing.getInterfaceTextWidth(s) > max)
            {
                if (!first)
                    l.append(" ");

                for (char c: s.toCharArray())
                {
                    if (Drawing.drawing.getInterfaceTextWidth(l.toString() + c) > max)
                    {
                        lines.add(l.toString());
                        l = new StringBuilder();
                    }

                    l.append(c);
                }
            }
            else
            {
                lines.add(l.toString());
                l = new StringBuilder();
                l.append(s);
            }

            first = false;
        }

        if (l.length() > 0)
            lines.add(l.toString());

        return lines;
    }

    public ModelPart newModel()
    {
        return Game.game.window.createModelPart();
    }

    public Model getModel(String dir)
    {
        if (modelsByDir.get(dir) != null)
            return modelsByDir.get(dir);

        Model m = new Model(Game.game.window, Game.game.fileManager, dir);
        modelsByDir.put(dir, m);

        return m;
    }

    public Model createModel(String dir)
    {
        return new Model(Game.game.window, Game.game.fileManager, dir);
    }
}

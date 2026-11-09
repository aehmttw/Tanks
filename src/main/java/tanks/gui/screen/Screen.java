package tanks.gui.screen;

import basewindow.Camera;
import basewindow.IBatchRenderableObject;
import basewindow.transformation.Matrix4;
import tanks.*;
import tanks.gui.ScreenIntro;
import tanks.obstacle.Obstacle;
import tanks.rendering.StaticTerrainRenderer;

public abstract class Screen implements IBatchRenderableObject
{
    public String music = null;
    public String musicID = null;

    public String windowTitle = "";
    public String screenHint = "";
    public boolean showDefaultMouse = true;

    public boolean allowClose = true;

    public boolean enableMargins = true;
    public boolean drawDarkness = true;

    public double textSize = Drawing.drawing.textSize;
    public double titleSize = Drawing.drawing.titleSize;
    public double objWidth = Drawing.drawing.objWidth;
    public double objHeight = Drawing.drawing.objHeight;
    public double objXSpace = Drawing.drawing.objXSpace;
    public double objYSpace = Drawing.drawing.objYSpace;

    public double screenAge = 0;

    public double centerX = Drawing.drawing.interfaceSizeX / 2;
    public double centerY = Drawing.drawing.interfaceSizeY / 2;

    /** Game view-projection used for deferred-light depth reconstruction this frame. */
    public Matrix4 viewProjection;
    public int minBgWidth = 0;
    public int minBgHeight = 0;

    public boolean drawBgRect = true;
    /** Extend scene/UI visibility behind the info bar without changing their scale or layout. */
    public boolean drawBehindInfoBar = false;
    public boolean stageOnly = false;

    public double interfaceScaleZoomOverride = -1;

    protected boolean redrawn = false;
    public boolean drawn = false;

    public IBatchRenderableObject[][] tiles;

    public boolean drawDebugInternally = false;

    public double lastObsSize;

    public Screen()
    {
        this.setupLayoutParameters();
    }

    public Screen(double objWidth, double objHeight, double objXSpace, double objYSpace)
    {
        this.objWidth = objWidth;
        this.objHeight = objHeight;
        this.objXSpace = objXSpace;
        this.objYSpace = objYSpace;

        this.textSize = this.objHeight * 0.6;
        this.titleSize = this.textSize * 1.25;

        this.setupLayoutParameters();
    }

    public abstract void update();

    public abstract void draw();

    public void drawUI()
    {

    }

    public void drawPostMouse()
    {

    }

    public void drawDefaultBackground()
    {
        this.drawDefaultBackground(1);
    }

    public void drawDefaultBackground(double size)
    {
        if (Game.screen instanceof ScreenIntro && !(this instanceof ScreenIntro))
            return;

        if (!drawn)
            this.drawn = true;

        if (!(Game.screen instanceof IDarkScreen))
            Panel.darkness = Math.max(Panel.darkness - Panel.frameFrequency * 3, 0);

        this.setBasePerspective();

        Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);

        if (stageOnly && Drawing.drawing.terrainRenderer instanceof StaticTerrainRenderer)
            ((StaticTerrainRenderer) Drawing.drawing.terrainRenderer).stage();
        else
            Drawing.drawing.terrainRenderer.draw();

        if (!(this instanceof ScreenGame && !Game.enable3d))
            Drawing.drawing.trackRenderer.draw();

        if (this.drawDarkness && drawBgRect)
            this.drawGameDarknessOverlay();

        this.lastObsSize = Obstacle.draw_size;
    }

    /** Draws darkness in game coordinates so it always matches the active game viewport. */
    public void drawGameDarknessOverlay()
    {
        if (Game.game.window.mainRenderPasses.drawingShadow || Panel.darkness <= 0)
            return;

        Camera.Bounds bounds = Game.game.window.currentCamera.getBoundsAtDepth(0);
        Drawing.drawing.setColor(0, 0, 0, Panel.darkness);
        Game.game.window.shapeRenderer.fillRect(bounds.left, bounds.top, 0,
                bounds.right - bounds.left + 1, bounds.bottom - bounds.top + 1, false);
    }

    public void setBasePerspective()
    {
        double usableHeight = Math.max(1, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);
        double targetScale = Math.max(0.0001, Drawing.drawing.scale);
        double gameWidth = Drawing.drawing.sizeX;
        double gameHeight = Drawing.drawing.sizeY;
        boolean orthographic = Game.game.window.orthographic;
        Camera activeCamera = Game.game.window.mainRenderPasses.sceneCamera;

        if (Game.game.window.mainRenderPasses.drawingShadow)
        {
            activeCamera = Game.game.window.mainRenderPasses.shadowCamera;
            Game.game.window.currentCamera = activeCamera;
            return;
        }

        Game.game.window.sceneCameraHeightOffset = Game.screen.drawBehindInfoBar ? 0 : Drawing.drawing.statsHeight;
        activeCamera.setViewportBottomExtension((Drawing.drawing.statsHeight - Game.game.window.sceneCameraHeightOffset) / usableHeight);

        if (orthographic)
        {
            activeCamera.setProjectionType(Camera.Projection.orthographic);
            activeCamera.updateSize(0, Game.game.window.absoluteWidth, 0, usableHeight, Game.game.window.absoluteDepth);
            activeCamera.setAspectRatio(Game.game.window.absoluteWidth / usableHeight);
            activeCamera.setZoom(targetScale);
            activeCamera.posZ = Game.game.window.absoluteDepth / 2.0;
        }
        else
        {
            activeCamera.setProjectionType(Camera.Projection.perspective);
            activeCamera.updateSize(0, Game.game.window.absoluteWidth, 0, usableHeight, Game.game.window.absoluteDepth);
            activeCamera.setAspectRatio(Game.game.window.absoluteWidth / usableHeight);
            activeCamera.setZoom(targetScale);
        }

        double visibleWidth = activeCamera.getVisibleSizeX(0);
        double visibleHeight = activeCamera.getVisibleSizeY(0);
        double centeredVisibleWidth = Game.screen.enableMargins ? Math.min(visibleWidth, gameWidth) : visibleWidth;
        double centeredVisibleHeight = Game.screen.enableMargins ? Math.min(visibleHeight, gameHeight) : visibleHeight;

        activeCamera.posX = this.getCameraX(centeredVisibleWidth, centeredVisibleHeight);
        activeCamera.posY = this.getCameraY(centeredVisibleWidth, centeredVisibleHeight);
        // Camera modes build their pose from this neutral game view each frame.
        activeCamera.getOrientation().setIdentity();
        activeCamera.clearAdjustments();
        activeCamera.updateView();

        Game.game.window.currentCamera = activeCamera;
        Game.game.window.loadPerspective();
    }

    public double getOffsetX()
    {
        return 0;
    }

    public double getOffsetY()
    {
        return 0;
    }

    public double getCameraX(double visibleWidth, double visibleHeight)
    {
        return -this.getOffsetX() + visibleWidth / 2;
    }

    public double getCameraY(double visibleWidth, double visibleHeight)
    {
        return -this.getOffsetY() + visibleHeight / 2;
    }

    public double getScale()
    {
        return Drawing.drawing.unzoomedScale;
    }

    public void setupLayoutParameters()
    {
        resetLayout();
    }

    public void setUnscaledLayoutParameters()
    {
        this.objWidth = 350;
        this.objHeight = 40;
        this.objXSpace = 380;
        this.objYSpace = 60;

        this.textSize = this.objHeight * 0.6;
        this.titleSize = this.textSize * 1.25;
    }

    public void resetLayout()
    {
        Drawing.drawing.interfaceScaleZoom = Drawing.drawing.interfaceScaleZoomDefault;
        Drawing.drawing.interfaceSizeX = Drawing.drawing.baseInterfaceSizeX / Drawing.drawing.interfaceScaleZoom;
        Drawing.drawing.interfaceSizeY = Drawing.drawing.baseInterfaceSizeY / Drawing.drawing.interfaceScaleZoom;
        this.centerX = Drawing.drawing.interfaceSizeX / 2;
        this.centerY = Drawing.drawing.interfaceSizeY / 2;
    }

    public void onAttemptClose()
    {

    }

    public void onFocusChange(boolean isFocused)
    {

    }

    public void onFilesDropped(String... files)
    {

    }

    public void drawPointLights()
    {
        Game.game.window.currentCamera = Game.game.window.mainRenderPasses.sceneCamera;
        Game.game.window.loadPerspective();

        if ((this instanceof IConditionalOverlayScreen && !((IConditionalOverlayScreen) this).isOverlayEnabled()) || this instanceof IBlankBackgroundScreen)
            return;

        for (Obstacle o: Game.obstacles)
        {
            if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
            {
                Game.drawer.lightsPass.drawLight((IDrawableLightSource) o, Drawing.drawing.gameToAbsoluteX(o.posX, 0),  Drawing.drawing.gameToAbsoluteY(o.posY, 0), (o.posZ + Game.tile_size / 2) * Drawing.drawing.scale, ((IDrawableLightSource) o).getBrightness() * Drawing.drawing.scale);
            }
        }

        for (Movable m: Game.movables)
        {
            if (m instanceof IDrawableLightSource && ((IDrawableLightSource) m).lit())
            {
                Game.drawer.lightsPass.drawLight((IDrawableLightSource) m, Drawing.drawing.gameToAbsoluteX(m.posX, 0),  Drawing.drawing.gameToAbsoluteY(m.posY, 0), m.posZ * Drawing.drawing.scale, ((IDrawableLightSource) m).getBrightness() * Drawing.drawing.scale);
            }
        }

        for (Effect e: Game.effects)
        {
            if (e.lit())
                Game.drawer.lightsPass.drawLight(e, Drawing.drawing.gameToAbsoluteX(e.posX, 0),  Drawing.drawing.gameToAbsoluteY(e.posY, 0), e.posZ * Drawing.drawing.scale, e.getBrightness() * Drawing.drawing.scale);
        }

        Game.game.window.loadPerspective();
    }

    public Matrix4 getViewProjection()
    {
        if (this.viewProjection != null)
            return this.viewProjection;

        Camera camera = Game.game.window.mainRenderPasses.sceneCamera;
        return camera.projection.multiply(camera.view);
    }
}

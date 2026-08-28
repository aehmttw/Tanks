package tanks.gui.screen;

import basewindow.Camera;
import basewindow.IBatchRenderableObject;
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

    public boolean selfBatch = true;
    public boolean forceInBounds = false;
    public int minBgWidth = 0;
    public int minBgHeight = 0;

    public boolean drawBgRect = true;
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

        double frac = 0;

        if (this instanceof ScreenGame || this instanceof ILevelPreviewScreen || (this instanceof IOverlayScreen &&
            !(this instanceof IConditionalOverlayScreen && !((IConditionalOverlayScreen) this).isOverlayEnabled())))
            frac = Obstacle.draw_size / Game.tile_size;

        if (this.forceInBounds)
            frac = 0;

        if (drawBgRect && (!(this instanceof ScreenExit) && size >= 1 && (selfBatch || (!Game.fancyTerrain && !Game.enable3d))))
        {
            Drawing.drawing.setColor(174 * frac + (1 - frac) * Level.currentColor.red, 92 * frac + (1 - frac) * Level.currentColor.green, 16 * frac + (1 - frac) * Level.currentColor.blue);

            double mul = 1;
            if (Game.angledView)
                mul = 2;

            Drawing.drawing.fillShadedInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2,
                mul * Game.game.window.absoluteWidth / Drawing.drawing.interfaceScale, mul * Game.game.window.absoluteHeight / Drawing.drawing.interfaceScale);

            Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue, 255.0 * size);
            Drawing.drawing.fillBackgroundRect(this, Drawing.drawing.sizeX / 2, Drawing.drawing.sizeY / 2, Drawing.drawing.sizeX, Drawing.drawing.sizeY);
        }

        this.setBasePerspective();

        Drawing.drawing.setColor(Level.currentColor.red, Level.currentColor.green, Level.currentColor.blue);

        if (stageOnly && Drawing.drawing.terrainRenderer instanceof StaticTerrainRenderer)
            ((StaticTerrainRenderer) Drawing.drawing.terrainRenderer).stage();
        else
            Drawing.drawing.terrainRenderer.draw();

        if (!(this instanceof ScreenGame && !Game.enable3d))
            Drawing.drawing.trackRenderer.draw();

        if (this.drawDarkness && drawBgRect)
            this.drawWorldDarknessOverlay();

        this.lastObsSize = Obstacle.draw_size;
    }

    /** Draws darkness in world space so it always matches the active game viewport. */
    public void drawWorldDarknessOverlay()
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
        double worldWidth = Drawing.drawing.sizeX;
        double worldHeight = Drawing.drawing.sizeY;
        boolean orthographic = Game.game.window.orthographic;
        Camera activeCamera = Game.game.window.baseCamera;

        if (Game.game.window.mainRenderPasses.drawingShadow)
        {
            activeCamera = Game.game.window.shadowCamera;
            Game.game.window.currentCamera = activeCamera;
            return;
        }

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
        double centeredVisibleWidth = Game.screen.enableMargins ? Math.min(visibleWidth, worldWidth) : visibleWidth;
        double centeredVisibleHeight = Game.screen.enableMargins ? Math.min(visibleHeight, worldHeight) : visibleHeight;

        activeCamera.posX = this.getCameraX(centeredVisibleWidth, centeredVisibleHeight);
        activeCamera.posY = this.getCameraY(centeredVisibleWidth, centeredVisibleHeight);
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
        this.setBasePerspective();

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

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();
    }
}

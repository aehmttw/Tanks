package tanks.rendering;

import basewindow.Camera;
import basewindow.RenderPass;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class RenderPassUI extends RenderPass
{
    public final Camera interfaceCamera;
    public ShaderGroupUI shaderUI;
    private int viewportY;

    public RenderPassUI()
    {
        super(Game.game.window, "ui");
        Drawing drawing = Drawing.drawing;
        this.interfaceCamera = new Camera("interface", 0, drawing.baseInterfaceSizeX,
                0, drawing.baseInterfaceSizeY, this.window.absoluteDepth);
    }

    /** Prepares the camera for use in both scene and UI drawing. */
    public void prepareCamera()
    {
        Drawing drawing = Drawing.drawing;
        double usableHeight = Math.max(1, this.window.absoluteHeight - drawing.statsHeight);
        boolean drawBehindBar = Game.screen.drawBehindInfoBar;
        this.interfaceCamera.clearAdjustments();
        this.interfaceCamera.getOrientation().setIdentity();
        this.interfaceCamera.resetToBounds(0, drawing.baseInterfaceSizeX, 0, drawing.baseInterfaceSizeY, this.window.absoluteDepth);
        this.interfaceCamera.setAspectRatio(this.window.absoluteWidth / usableHeight);
        this.interfaceCamera.setViewportBottomExtension(drawBehindBar ? drawing.statsHeight / usableHeight : 0);

        this.viewportY = drawBehindBar || this.window.absoluteHeight <= 0 ? 0 :
                (int) Math.round(drawing.statsHeight * this.window.frameBufferHeight / this.window.absoluteHeight);
    }

    /** Binds interface coordinates without changing the current pass or draw target. */
    public boolean bindCamera()
    {
        if (this.window.mainRenderPasses.drawingShadow)
            return false;

        boolean offscreenScene = this.window.currentRenderPass == this.window.mainRenderPasses.drawPass &&
                this.window.mainRenderPasses.drawToFramebuffer;
        int y = offscreenScene ? 0 : this.viewportY;
        this.window.bindCamera(this.interfaceCamera, 0, y, this.window.frameBufferWidth,
                Math.max(1, this.window.frameBufferHeight - y));
        return true;
    }

    @Override
    public void draw()
    {
        super.draw();

        this.window.setShader(this.shaderUI);

        this.prepareCamera();
        this.bindCamera();

        Panel.panel.drawUI();
    }
}

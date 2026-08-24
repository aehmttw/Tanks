package tanks.rendering;

import basewindow.RenderPass;
import tanks.Game;
import tanks.Panel;
import tanks.Drawing;

public class RenderPassUI extends RenderPass
{
    public boolean initialized = false;
    public ShaderGroupUI shaderUI;

    public RenderPassUI()
    {
        super(Game.game.window, "ui");
    }

    @Override
    public void draw()
    {
        super.draw();

        this.window.setShader(this.shaderUI);

        if (!this.initialized)
        {
            this.initialized = true;
        }

        this.window.interfaceCameraLogicalWidth = Drawing.drawing.baseInterfaceSizeX;
        this.window.interfaceCameraLogicalHeight = Drawing.drawing.baseInterfaceSizeY;
        this.window.interfaceCameraHeightOffset = Drawing.drawing.statsHeight;
        this.window.interfaceCamera.resetToBounds(0, this.window.interfaceCameraLogicalWidth, 0, this.window.interfaceCameraLogicalHeight, this.window.absoluteDepth);
        this.window.interfaceCamera.setAspectRatio(this.window.absoluteWidth / Math.max(1, this.window.absoluteHeight - this.window.interfaceCameraHeightOffset));
        this.window.currentCamera = this.window.interfaceCamera;
        this.window.transformations.clear();
        this.window.loadPerspective();

        int viewportY = 0;
        if (this.window.absoluteHeight > 0)
            viewportY = (int) Math.round(Drawing.drawing.statsHeight * this.window.frameBufferHeight / this.window.absoluteHeight);

        int viewportHeight = Math.max(1, this.window.frameBufferHeight - viewportY);
        this.window.setViewport(0, viewportY, this.window.frameBufferWidth, viewportHeight);

        Panel.panel.drawUI();
        this.window.currentCamera = this.window.baseCamera;
    }
}

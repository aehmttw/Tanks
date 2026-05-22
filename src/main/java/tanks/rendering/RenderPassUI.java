package tanks.rendering;

import basewindow.RenderPass;
import tanks.Game;
import tanks.Panel;

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
            this.window.mainRenderPasses.setLighting(1.0, 1.0, 0.5, 1.0);
        }

        this.window.loadPerspective();
        this.window.setViewport(0, 0, this.window.frameBufferWidth, this.window.frameBufferHeight);

        Panel.panel.drawUI();
    }
}

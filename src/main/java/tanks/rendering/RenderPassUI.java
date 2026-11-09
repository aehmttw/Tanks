package tanks.rendering;

import basewindow.RenderPass;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;

public class RenderPassUI extends RenderPass
{
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

        Drawing.drawing.prepareInterfaceCamera();
        Drawing.drawing.bindInterfaceCamera();

        Panel.panel.drawUI();
    }
}

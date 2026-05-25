package tanks;

import basewindow.IDrawer;
import basewindow.RenderPass;
import tanks.extension.Extension;
import tanks.gui.Firework;
import tanks.rendering.*;

public class GameDrawer implements IDrawer
{
    public boolean initialized = false;
    public RenderPassPostLights lightsPass;
    public RenderPassUI uiPass;

    public void initialize()
    {
        try
        {
            Game.drawer = this;

            this.initialized = true;
            Game.game.window.mainRenderPasses.drawToFramebuffer = true;
            this.lightsPass = new RenderPassPostLights();

            ShaderPostLights lightsShader = new ShaderPostLights(this.lightsPass);
            this.lightsPass.lightsShader = lightsShader;
            lightsShader.initialize();

            ShaderPostLightingMix mixShader = new ShaderPostLightingMix(this.lightsPass);
            this.lightsPass.mixShader = mixShader;
            mixShader.initialize();

            this.uiPass = new RenderPassUI();
            this.uiPass.shaderUI = new ShaderUIDefault(uiPass);
            this.uiPass.shaderUI.initialize();

            Firework.shader = new ShaderFireworkExplosion();
            Firework.trailShader = new ShaderFireworkExplosionTrail();
            Firework.shader.initialize();
            Firework.trailShader.initialize();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }
    }

    @Override
    public void draw()
    {
        if (!initialized)
            this.initialize();

        Game.game.window.mainRenderPasses.draw();
        this.lightsPass.draw();
        this.uiPass.draw();
    }

    @Override
	public void drawSinglePass(RenderPass rp)
	{
		try
		{
            for (Extension e: Game.extensionRegistry.extensions)
                e.preDraw();

            Panel.panel.draw();

            for (Extension e: Game.extensionRegistry.extensions)
                e.draw();
        }
        catch (Throwable e)
        {
            if (e instanceof GameCrashedException)
                Game.displayCrashScreen(((GameCrashedException) e).originalException);
            else
                Game.displayCrashScreen(e);
        }
    }
}

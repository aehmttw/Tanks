package tanks;

import basewindow.*;
import tanks.extension.Extension;
import tanks.gui.Firework;
import tanks.gui.screen.DisplayCrusadeLevels;
import tanks.gui.screen.ICrusadePreviewScreen;
import tanks.rendering.*;

public class GameDrawer implements IDrawer
{
    public boolean initialized = false;
    public RenderPassPostLights lightsPass;
    public RenderPassUI uiPass;
    public ShaderShadowDepthDebug shadowDepthDebugShader;

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
            this.shadowDepthDebugShader = new ShaderShadowDepthDebug();
            this.shadowDepthDebugShader.initialize();

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
        try
        {
            if (!initialized)
                this.initialize();

            this.uiPass.prepareCamera();
            // The shadow pass derives its orthographic bounds from the scene camera, so
            // refresh it before the shadow map is generated rather than afterward.
            Game.screen.setBasePerspective();
            RenderPassGroupShadowDraw passes = Game.game.window.mainRenderPasses;
            Camera sceneCamera = passes.sceneCamera;
            DisplayCrusadeLevels preview = Game.previewCrusades && Game.screen instanceof ICrusadePreviewScreen ?
                    ((ICrusadePreviewScreen) Game.screen).getCrusadePreview() : null;
            if (preview != null)
                passes.sceneCamera = preview.prepareCamera();

            try
            {
                passes.drawToFramebuffer = Game.fancyLights;
                passes.draw();

                if (Game.fancyLights)
                    this.lightsPass.draw();
            }
            finally
            {
                passes.sceneCamera = sceneCamera;
            }

            this.uiPass.draw();
        }
        catch (Throwable e)
        {
            if (e instanceof GameCrashedException)
                Game.displayCrashScreen(((GameCrashedException) e).originalException);
            else
                Game.displayCrashScreen(e);
        }
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

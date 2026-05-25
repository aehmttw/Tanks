package tanks.rendering;

import basewindow.*;
import tanks.*;
import tanks.gui.screen.ScreenGame;
import tanks.obstacle.Obstacle;

public class RenderPassPostLights extends RenderPass
{
    protected float[] tempLightPos = new float[4];
    protected float[] tempLightColor = new float[3];
    protected float[] lightColor = new float[]{1f, 1f, 1f};

    public ShaderPostLights lightsShader;
    public ShaderPostLightingMix mixShader;
    public BaseFrameBuffer lightsFrameBuffer;

    public double light = 1.0;
    public double shadow = 0.5;

    public static Model sphere = Drawing.drawing.getModel("/models/sphere/");

    public RenderPassPostLights()
    {
        super(Game.game.window, "post_lights");
        this.lightsFrameBuffer = this.window.createFrameBuffer();
        this.lightsFrameBuffer.addColorTexture(this.window, 3, true);
        this.lightsFrameBuffer.initialize();
    }

    @Override
    public void draw()
    {
        super.draw();

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();

        this.lightsFrameBuffer.resizeToWindow(this.window);
        this.lightsFrameBuffer.bind();
        this.window.clearColor();

        this.window.setShader(lightsShader);

        this.lightsShader.depthTex.set(0);
        this.lightsShader.width.set((float) Game.game.window.frameBufferWidth);
        this.lightsShader.height.set((float) Game.game.window.frameBufferHeight);

        if (Game.screen instanceof ScreenGame)
            this.lightsShader.invProjection.set(((ScreenGame) Game.screen).projection.inverse().toOpenGL(), false);
        else
            this.lightsShader.invProjection.set(Game.game.window.getProjectionMatrix().inverse().toOpenGL(), false);

        Game.game.window.mainRenderPasses.drawPass.drawFrameBuffer.bindDepthTexture(0);
        this.window.setForceModelGlow(true);

        this.drawPointLights();

        this.window.setForceModelGlow(false);
        this.window.stopFrameBuffer();

        this.lightsFrameBuffer.bindColorTexture(this.window, 0, "lights");

//        this.window.currentRenderPass = this.window.mainRenderPasses.drawPass;
//        this.window.setShader(this.window.shaderDefault);
////        Game.game.window.shapeRenderer.drawImage(0, Game.game.window.absoluteHeight, Game.game.window.absoluteWidth, -Game.game.window.absoluteHeight, "lights", false);
//        Game.game.window.shapeRenderer.drawImage(100, 100, 200, 200, "lights", false);

        this.window.setShader(this.mixShader);
        this.mixShader.lightColor.set(this.lightColor);
        this.mixShader.baseLight.set((float) this.light);
        this.mixShader.shadowLight.set((float) this.shadow);
        this.mixShader.colorTex.set(0);
        this.mixShader.lightTex.set(1);
        this.mixShader.glowTex.set(2);
        this.mixShader.shadowTex.set(3);

        this.lightsFrameBuffer.bindColorTexture(0, 1);
        Game.game.window.mainRenderPasses.drawPass.drawFrameBuffer.bindColorTexture(1, 2);
        Game.game.window.mainRenderPasses.drawPass.drawFrameBuffer.bindColorTexture(2, 3);

        Game.game.window.shapeRenderer.drawImage(0, Game.game.window.absoluteHeight, Game.game.window.absoluteWidth, -Game.game.window.absoluteHeight, "image", false);
        //        Game.game.window.shapeRenderer.drawImage(0, Game.game.window.absoluteHeight, Game.game.window.absoluteWidth, -Game.game.window.absoluteHeight, "lights", false);

    }

    public void drawLight(IDrawableLightSource l, double posX, double posY, double posZ, double size)
    {
        Color c = l.getColor();
        tempLightPos[0] = (float) posX;
        tempLightPos[1] = (float) posY;
        tempLightPos[2] = (float) posZ;
        tempLightPos[3] = (float) size;

        tempLightColor[0] = (float) c.red / 255;
        tempLightColor[1] = (float) c.green / 255;
        tempLightColor[2] = (float) c.blue / 255;

        this.lightsShader.lightPos.set(tempLightPos);
        this.lightsShader.lightColor.set(tempLightColor);

        this.window.enableFrontFaceCulling();
        sphere.draw(posX, posY, posZ, size, size, size, 0, 0, 0, false);
        this.window.disableFaceCulling();
    }

    public void drawPointLights()
    {
        if (Game.screen instanceof ScreenGame)
            ((ScreenGame) Game.screen).setPerspective();

        for (Obstacle o: Game.obstacles)
        {
            if (o instanceof IDrawableLightSource && ((IDrawableLightSource) o).lit())
            {
                drawLight((IDrawableLightSource) o, Drawing.drawing.gameToAbsoluteX(o.posX, 0),  Drawing.drawing.gameToAbsoluteY(o.posY, 0), (o.posZ + Game.tile_size / 2) * Drawing.drawing.scale, ((IDrawableLightSource) o).getBrightness() * Drawing.drawing.scale);
            }
        }

        for (Movable m: Game.movables)
        {
            if (m instanceof IDrawableLightSource && ((IDrawableLightSource) m).lit())
            {
                drawLight((IDrawableLightSource) m, Drawing.drawing.gameToAbsoluteX(m.posX, 0),  Drawing.drawing.gameToAbsoluteY(m.posY, 0), m.posZ * Drawing.drawing.scale, ((IDrawableLightSource) m).getBrightness() * Drawing.drawing.scale);
            }
        }

        Game.game.window.transformations.clear();
        Game.game.window.loadPerspective();
    }
}

package tanks.rendering;

import basewindow.BaseFrameBuffer;
import basewindow.RenderPass;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.bullet.Trail3D;
import tanks.gui.screen.ScreenGame;

public class RenderPassPostLights extends RenderPass
{
    protected float[] tempLightPos = new float[4];
    protected float[] tempLightColor = new float[3];
    protected float[] baseLight = new float[]{1f, 1f, 1f};

    public ShaderPostLights lightsShader;
    public ShaderPostLightingMix mixShader;
    public BaseFrameBuffer lightsFrameBuffer;

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

        for (double[] d: Panel.panel.lights)
        {
            double size = d[3] * Game.tile_size * 4 * Drawing.drawing.scale;

            tempLightPos[0] = (float) d[0];
            tempLightPos[1] = (float) d[1];
            tempLightPos[2] = (float) d[2];
            tempLightPos[3] = (float) size;

            tempLightColor[0] = (float) d[4] / 255;
            tempLightColor[1] = (float) d[5] / 255;
            tempLightColor[2] = (float) d[6] / 255;

            this.lightsShader.lightPos.set(tempLightPos);
            this.lightsShader.lightColor.set(tempLightColor);

            this.window.enableBackFaceCulling();
            Trail3D.cap.draw(d[0], d[1], d[2], size, size, size, 0, 0, -Math.PI / 2, false);
            this.window.disableBackFaceCulling();
        }

        this.window.setForceModelGlow(false);
        this.window.stopFrameBuffer();

        this.lightsFrameBuffer.bindColorTexture(this.window, 0, "lights");

//        this.window.currentRenderPass = this.window.mainRenderPasses.drawPass;
//        this.window.setShader(this.window.shaderDefault);
////        Game.game.window.shapeRenderer.drawImage(0, Game.game.window.absoluteHeight, Game.game.window.absoluteWidth, -Game.game.window.absoluteHeight, "lights", false);
//        Game.game.window.shapeRenderer.drawImage(100, 100, 200, 200, "lights", false);

        this.window.setShader(this.mixShader);
        this.mixShader.baseLight.set(baseLight);
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
}

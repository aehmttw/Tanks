package tanks.rendering;

import basewindow.*;
import tanks.Game;

public abstract class ShaderGroupUI extends ShaderGroupSingleStage
{
    public ShaderGroupUI(RenderPass p)
    {
        super(Game.game.window, new ShaderUI(Game.game.window), "ui", p);
    }

    public ShaderGroupUI(String name)
    {
        super(Game.game.window, new ShaderUI(Game.game.window), name, Game.drawer.uiPass);
    }

    public static class ShaderUI extends ShaderProgram implements IBaseShader, ITextureShader, IBlendFuncShader
    {
        public Uniform1b texture;
        public Uniform1b vbo;
        public Uniform4f originalColor;
        public UniformSampler2D tex;
        public Uniform1i blendFunc;

        public BaseWindow window;

        public ShaderUI(BaseWindow window)
        {
            super(window);
            this.window = window;
        }

        @Override
        public void initializeUniforms()
        {
            this.tex.set(0);
            this.blendFunc.set(0);
        }

        public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
        {
            this.vbo.set(true);
            this.originalColor.set((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);

            this.window.vboRenderer.setVertexBuffer(vertexBufferID);
            this.window.vboRenderer.setColorBuffer(colorBufferID);
            this.window.vboRenderer.setTexCoordBuffer(texBufferID);
            this.window.vboRenderer.setNormalBuffer(normalBufferID);
            this.window.vboRenderer.drawVBO(numberIndices);

            this.vbo.set(false);
        }

        @Override
        public void setBlendFunc(int func)
        {
            this.blendFunc.set(func);
        }

        @Override
        public void setTexture(boolean on)
        {
            this.texture.set(on);
        }
    }
}


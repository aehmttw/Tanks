package basewindow;

public class ShaderBase extends ShaderProgram implements IBaseShader, IBlendFuncShader, IDepthShader, IGlowShader, ITextureShader
{
    public Uniform1b texture;
    public UniformSampler2D depthTexture;
    public UniformMatrix4 biasMatrix;
    public UniformMatrix4 lightViewProjectionMatrix;
    public Uniform1b depthtest;
    public Uniform1f glow;
    public Uniform1i shadowres;

    public Uniform1b shadow;
    public Uniform1b vbo;
    public Uniform4f originalColor;

    public UniformSampler2D tex;

    public Uniform1i blendFunc;

    public BaseWindow window;

    public ShaderBase(BaseWindow window)
    {
        super(window);
        this.window = window;
    }

    @Override
    public void initializeUniforms()
    {
        this.tex.set(0);
        this.depthTexture.set(1);
        this.blendFunc.set(0);
    }

    public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
    {
        this.vbo.set(true);
        this.originalColor.set((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);
        //this.useNormal.set(normalBufferID != 0);

        this.window.vboRenderer.setVertexBuffer(vertexBufferID);
        this.window.vboRenderer.setColorBuffer(colorBufferID);
        this.window.vboRenderer.setTexCoordBuffer(texBufferID);
        this.window.vboRenderer.setNormalBuffer(normalBufferID);
        this.window.vboRenderer.drawVBO(numberIndices);

        this.vbo.set(false);
        //this.useNormal.set(false);
    }

    @Override
    public String toString()
    {
        return this.group.name + "/base";
    }

    @Override
    public void setBlendFunc(int func)
    {
        this.blendFunc.set(func);
    }

    @Override
    public void setDepthTest(boolean on)
    {
        this.depthtest.set(on);
    }

    @Override
    public void setGlow(float glow)
    {
        this.glow.set(glow);
    }

    @Override
    public void setTexture(boolean on)
    {
        this.texture.set(on);
    }
}

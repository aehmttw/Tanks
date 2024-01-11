package basewindow;

public class ShaderBase extends ShaderProgram implements IBaseShader
{
    public Uniform1b texture;
    public Uniform1i depthTexture;
    public UniformMatrix4 biasMatrix;
    public UniformMatrix4 lightViewProjectionMatrix;
//    public Uniform3f lightVec;
    public Uniform1b depthtest;
    public Uniform1f glow;
    public Uniform1i shadowres;
    public Uniform1f light;
    public Uniform1f glowLight;
    public Uniform1f shade;
    public Uniform1f glowShade;
//    public Uniform1f edgeLight;
//    public Uniform1f edgeCutoff;
//    public Uniform1f minBrightness;
//    public Uniform1f maxBrightness;
//    public Uniform1b negativeBrightness;
//    public Uniform1b customLight;
//    public Uniform3f lightAmbient;
//    public Uniform3f lightDiffuse;
//    public Uniform3f lightSpecular;
//    public Uniform1f shininess;
//    public Uniform1f celsections;
    public Uniform1b shadow;
    public Uniform1b vbo;
    public Uniform4f originalColor;

    public Uniform1f width;
    public Uniform1f height;
    public Uniform1f depth;
    public Uniform1f scale;

    public Uniform1i lightsCount;
    public Uniform1i lightsTexSize;
    public Uniform1i lightsTexture;

//    public Uniform1b useNormal;

    public BaseWindow window;

    public ShaderBase(BaseWindow window)
    {
        super(window);
        this.window = window;
    }

//    @Override
//    public void initialize() throws Exception
//    {
//        this.setUp("/shaders/main.vert", new String[]{"/shaders/main_default.vert"},
//                "/shaders/main.frag", new String[]{"/shaders/main_default.frag"});
//    }

    @Override
    public void initializeUniforms()
    {
        this.depthTexture.set(1);
        this.lightsTexture.set(2);
    }

    public void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices)
    {
        this.vbo.set(true);
        this.originalColor.set((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);
        //this.useNormal.set(normalBufferID != 0);

        this.util.setVertexBuffer(vertexBufferID);
        this.util.setColorBuffer(colorBufferID);
        this.util.setTexCoordBuffer(texBufferID);
        this.util.setNormalBuffer(normalBufferID);
        this.util.drawVBO(numberIndices);

        this.vbo.set(false);
        //this.useNormal.set(false);
    }

    @Override
    public String toString()
    {
        return this.group.name + "/base";
    }
}

package basewindow;

import static basewindow.BaseShaderUtil.FLOAT;

public class ShaderBones extends ShaderBase implements IBaseShader, IBoneShader
{
    public Uniform1b bonesEnabled;
    public UniformMatrix4 boneMatrices;
    public Attribute bones;

    public ShaderBones(BaseWindow window)
    {
        super(window);
    }

    @Override
    public void initialize() throws Exception
    {
        this.setUp("/shaders/main.vert", new String[]{"/shaders/main_bones.vert"}, "/shaders/main.frag", new String[]{"/shaders/main_default.frag"});
    }

    @Override
    public void initializeUniforms()
    {
        this.depthTexture.set(1);
    }

    @Override
    public void initializeAttributeParameters()
    {
        this.bones.setDataType(FLOAT, 4);
    }

    @Override
    public void setBoneMatrices(float[] matrices, boolean transpose)
    {
        this.boneMatrices.set(matrices, transpose);
    }

    public void renderPosedVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int boneBufferID, int numberIndices)
    {
        this.vbo.set(true);
        this.originalColor.set((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);
        this.bonesEnabled.set(true);
        //this.useNormal.set(normalBufferID != 0);

        this.util.setVertexBuffer(vertexBufferID);
        this.util.setColorBuffer(colorBufferID);
        this.util.setTexCoordBuffer(texBufferID);
        this.util.setNormalBuffer(normalBufferID);
        this.util.setCustomBuffer(bones, boneBufferID, 4, FLOAT);
        this.util.drawVBO(numberIndices);

        this.vbo.set(false);
        //this.useNormal.set(false);
        this.bonesEnabled.set(false);
    }
}

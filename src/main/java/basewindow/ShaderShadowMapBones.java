package basewindow;

public class ShaderShadowMapBones extends ShaderShadowMap implements IBaseShader, IBoneShader
{
    public Uniform1b bonesEnabled;
    public UniformMatrix4 boneMatrices;
    public Attribute bones;

    public ShaderShadowMapBones(BaseWindow window)
    {
        super(window);
    }

//    @Override
//    public void initialize() throws Exception
//    {
//        this.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_bones.vert"}, "/shaders/shadow_map.frag", null);
//    }

    @Override
    public void setBoneMatrices(float[] matrices, boolean transpose)
    {
        this.boneMatrices.set(matrices, transpose);
    }

    public void renderPosedVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int boneBufferID, int numberIndices)
    {
        this.bonesEnabled.set(true);

        this.util.setVertexBuffer(vertexBufferID);
        this.util.setTexCoordBuffer(texBufferID);
        this.util.setCustomBuffer(bones, boneBufferID, 4);
        this.util.drawVBO(numberIndices);

        this.bonesEnabled.set(false);
    }
}

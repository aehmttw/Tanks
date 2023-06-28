package basewindow;

public interface IBoneShader
{
    void setBoneMatrices(float[] matrices, boolean transpose);

    void renderPosedVBO(int vertexVBO, int colorVBO, int texVBO, int normalVBO, int vboID, int count);
}

package basewindow;

public interface IBaseShader
{
    void renderVBO(int vertexBufferID, int colorBufferID, int texBufferID, int normalBufferID, int numberIndices);
}

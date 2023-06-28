package basewindow;

public abstract class BaseShaderUtil
{
    public static final int FLOAT = 5126;
    public static final int INT = 5124;

    public abstract void setUp(String vert, String[] vertHeaders, String frag, String[] fragHeaders) throws Exception;

    public abstract void setUp(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception;

    public abstract void setUpUniforms() throws InstantiationException, IllegalAccessException;

    public abstract ShaderProgram.Attribute getAttribute();

    public abstract void set();

    public abstract void setVertexBuffer(int id);

    public abstract void setColorBuffer(int id);

    public abstract void setTexCoordBuffer(int id);

    public abstract void setNormalBuffer(int id);

    public abstract void setCustomBuffer(ShaderProgram.Attribute attribute, int bufferID, int size, int type);

    public abstract void drawVBO(int numberIndices);
}

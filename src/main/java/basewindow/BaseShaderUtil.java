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
}

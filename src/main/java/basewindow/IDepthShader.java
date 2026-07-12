package basewindow;

// Shaders which support being aware of depth test mode from ShapeRenderer
public interface IDepthShader
{
    void setDepthTest(boolean on);
}

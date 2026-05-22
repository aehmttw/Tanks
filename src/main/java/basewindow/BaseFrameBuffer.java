package basewindow;

/**
 * A framebuffer is something you can draw to. It corresponds to an OpenGL construct.
 * You can capture depth information from a drawing pass, as well as multiple sets
 * of "color" information (which can really be anything)
 */
public abstract class BaseFrameBuffer
{
    /**
     * Set the framebuffer to capture depth information into a texture of given size
     * @param sizeX
     * @param sizeY
     */
    public abstract void createDepthTexture(int sizeX, int sizeY);

    /**
     * Set the framebuffer to capture written color information into a texture of given size
     * @param sizeX
     * @param sizeY
     * @param channels How many channels in the texture, 1-4 (R/RG/RGB/RGBA)
     * @param fp Whether to use floating point accuracy (for high dynamic range)
     */
    public abstract void addColorTexture(int sizeX, int sizeY, int channels, boolean fp);

    /**
     * Set the framebuffer to capture depth information into a texture with the same size as the window
     */
    public abstract void createDepthTexture(BaseWindow w);

    /**
     * Set the framebuffer to capture written color information into a texture with the same size as the window
     * @param channels How many color channels to capture, 1-4
     * @param fp Whether to use floating point accuracy (for high dynamic range)
     */
    public abstract void addColorTexture(BaseWindow w, int channels, boolean fp);


    /**
     * Recreate the framebuffer such that all of its textures are sized to the same size as the window.
     * This can be run every frame to ensure that window size changes are reflected in the framebuffer's size.
     */
    public abstract void resizeToWindow(BaseWindow w);

    /**
     * Must be run before the framebuffer is to be used after adding depth/color textures
     */
    public abstract void initialize();

    /**
     * When done using the framebuffer
     */
    public abstract void free();

    /**
     * Redirects the output of all OpenGL drawing to this framebuffer from now on.
     * To unbind, do BaseWindow.stopFrameBuffer
     */
    public abstract void bind();

    /**
     * Binds the depth texture associated with this framebuffer to the given OpenGL texture slot
     * for use in a shader.
     * @param target Texture slot to bind to
     */
    public abstract void bindDepthTexture(int target);

    /**
     * Binds the color texture associated with this framebuffer to the given OpenGL texture slot
     * for use in a shader.
     * @param which Which color texture to bind
     * @param target Texture slot to bind to
     */
    public abstract void bindColorTexture(int which, int target);

    /**
     * Binds the depth texture associated with this framebuffer to a given texture name, which can be used in drawImage
     * for use in a shader.
     * @param name Texture name to bind to
     */
    public abstract void bindDepthTexture(BaseWindow w, String name);

    /**
     * Binds the color texture associated with this framebuffer to a given texture name, which can be used in drawImage
     * for use in a shader.
     * @param which Which color texture to bind
     * @param name Texture name to bind to
     */
    public abstract void bindColorTexture(BaseWindow w, int which, String name);

}

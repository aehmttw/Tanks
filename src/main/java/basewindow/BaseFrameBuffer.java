package basewindow;

import org.lwjgl.opengl.GL13;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL20.*;

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
     * @param alpha Whether to capture alpha information too
     */
    public abstract void addColorTexture(int sizeX, int sizeY, boolean alpha);

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

}

package lwjglwindow;

import basewindow.BaseWindow;
import basewindow.Model;
import basewindow.ModelPart;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class VBOModelPart extends ModelPart
{
    public LWJGLWindow window;

    protected int colorVBO;

    protected int vertexVBO;
    protected int texVBO;

    @Override
    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest)
    {
        if (this.material.useDefaultDepthMask)
            window.setDrawOptions(depthTest, this.material.glow, this.window.colorA >= 1.0);
        else
            window.setDrawOptions(depthTest, this.material.glow, this.material.depthMask);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Rotation.transform(window, -pitch, -roll, -yaw);
        Scale.transform(window, sX, sY, sZ);

        if (this.material.texture != null)
            window.setTexture(this.material.texture, false);

        window.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.shapes.length * 3);
        window.disableTexture();

        glPopMatrix();

        window.disableDepthtest();
    }

    @Override
    public void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ)
    {
        if (this.material.useDefaultDepthMask)
            window.setDrawOptions(false, this.material.glow, this.window.colorA >= 1.0);
        else
            window.setDrawOptions(false, this.material.glow, this.material.depthMask);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Scale.transform(window, 1, 1, 0);
        Rotation.transform(window, 0, Math.PI * -3 / 4, 0);
        Rotation.transform(window, 0, 0, Math.PI / 4);
        Scale.transform(window, sX, sY, sZ);

        if (this.material.texture != null)
            window.setTexture(this.material.texture, false);

        window.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.shapes.length * 3);
        window.disableTexture();

        glPopMatrix();
    }

    @Override
    public void draw(double posX, double posY, double sX, double sY, double angle)
    {
        if (this.material.useDefaultDepthMask)
            window.setDrawOptions(false, this.material.glow, this.window.colorA >= 1.0);
        else
            window.setDrawOptions(false, this.material.glow, this.material.depthMask);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, 0);
        Rotation.transform(window, 0, 0, -angle);
        Scale.transform(window, sX, sY, 0);

        if (this.material.texture != null)
            window.setTexture(this.material.texture, false);

        window.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.shapes.length * 3);
        window.disableTexture();

        glPopMatrix();
    }

    @Override
    public void processShapes()
    {
        FloatBuffer vert = BufferUtils.createFloatBuffer(this.shapes.length * 9);
        FloatBuffer color = BufferUtils.createFloatBuffer(this.shapes.length * 12);
        FloatBuffer tex = BufferUtils.createFloatBuffer(this.shapes.length * 6);

        for (Shape s : this.shapes)
        {
            for (Point p : s.points)
            {
                vert.put((float) p.x);
                vert.put((float) p.y);
                vert.put((float) p.z);
            }

            for (Point p : s.texCoords)
            {
                tex.put((float) p.x);
                tex.put((float) p.y);
            }

            for (double[] p : s.colors)
            {
                color.put((float) (p[0] * this.material.colorR));
                color.put((float) (p[1] * this.material.colorG));
                color.put((float) (p[2] * this.material.colorB));
                color.put((float) (p[3] * this.material.colorA));
            }
        }

        vert.flip();
        color.flip();
        tex.flip();

        this.vertexVBO = window.createVBO();
        this.colorVBO = window.createVBO();

        window.vertexBufferData(this.vertexVBO, vert);
        window.vertexBufferData(this.colorVBO, color);

        if (this.material.texture != null)
        {
            this.texVBO = window.createVBO();
            window.vertexBufferData(this.texVBO, tex);
        }
    }

    public VBOModelPart(LWJGLWindow window)
    {
        super(window);
    }

    public VBOModelPart(BaseWindow window, Model model, ArrayList<Shape> shapes, Model.Material material)
    {
        super(window, model, shapes, material);
    }

    @Override
    public void setWindow(BaseWindow w)
    {
        this.window = (LWJGLWindow) w;
    }
}

package lwjglwindow;

import basewindow.BaseVBORenderer;
import basewindow.ShaderGroup;
import basewindow.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;

public class VBORenderer extends BaseVBORenderer
{
    public LWJGLWindow window;

    public VBORenderer(LWJGLWindow w)
    {
        this.window = w;
    }

    @Override
    public void setVertexBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glVertexPointer(3, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setColorBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_COLOR_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glColorPointer(4, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setTexCoordBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glTexCoordPointer(2, GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setNormalBuffer(int id)
    {
        if (id <= 0)
            return;

        GL11.glEnableClientState(GL_NORMAL_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, id);
        GL11.glNormalPointer(GL_FLOAT, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void setCustomBuffer(ShaderProgram.Attribute attribute, int bufferID, int size)
    {
        GL15.glBindBuffer(GL_ARRAY_BUFFER, bufferID);
        GL20.glEnableVertexAttribArray(attribute.id);
        GL20.glVertexAttribPointer(attribute.id, size, GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        this.enabledAttributes.add(attribute.id);
    }

    @Override
    public void setCustomBuffer(ShaderGroup.Attribute attribute, int bufferID, int size)
    {
        this.setCustomBuffer(attribute.passAttributes[window.currentShaderStage.index], bufferID, size);
    }

    @Override
    public void drawVBO(int numberIndices)
    {
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numberIndices);

        GL11.glDisableClientState(GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL_COLOR_ARRAY);

        ArrayList<Integer> attributes = this.enabledAttributes;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, attributesSize = attributes.size(); i < attributesSize; i++)
            glDisableVertexAttribArray(attributes.get(i));

        this.enabledAttributes.clear();
    }

}

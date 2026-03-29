package basewindow;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;

public abstract class BaseVBORenderer
{
    public ArrayList<Integer> enabledAttributes = new ArrayList<>();

    public abstract void setVertexBuffer(int id);

    public abstract void setColorBuffer(int id);

    public abstract void setTexCoordBuffer(int id);

    public abstract void setNormalBuffer(int id);

    public abstract void setCustomBuffer(ShaderProgram.Attribute attribute, int bufferID, int size);

    public abstract void setCustomBuffer(ShaderGroup.Attribute attribute, int bufferID, int size);

    public abstract void drawVBO(int numberIndices);
}

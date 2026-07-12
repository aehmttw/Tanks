package basewindow;

import java.util.ArrayList;

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

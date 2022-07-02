package lwjglwindow;

import basewindow.Model;
import basewindow.ModelPart;
import basewindow.PosedModel;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class VBOPosedModel extends PosedModel
{
    public HashMap<ModelPart, Integer> bonesVBOs = new HashMap<>();
    public float[] matrices;

    public VBOPosedModel(Model model)
    {
        super(model);

        for (ModelPart m: this.model.models)
        {
            int vbo = ((LWJGLWindow) this.model.window).createVBO();
            this.bonesVBOs.put(m, vbo);

            FloatBuffer bones = BufferUtils.createFloatBuffer(m.shapes.length * 12);

            for (ModelPart.Shape s : m.shapes)
            {
                for (ModelPart.Point p : s.points)
                {
                    for (int i = 0; i < 4; i++)
                    {
                        if (p.bones != null && i < p.bones.length)
                            bones.put((float) p.boneWeights[i] + p.bones[i].index);
                        else
                            bones.put((float) 0);
                    }
                }
            }

            bones.flip();

            ((LWJGLWindow) this.model.window).vertexBufferData(vbo, bones);
        }

        this.matrices = new float[this.model.bones.size() * 16];
    }

    @Override
    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest)
    {
        for (PoseBone b: this.bones)
        {
            b.computeMatrix();
            b.compileMatrix();
        }

        int in = 0;
        for (PoseBone bone : this.bones)
        {
            for (double v : bone.compiledMatrix)
            {
                this.matrices[in] = (float) v;
                in++;
            }
        }

        if (!this.model.window.drawingShadow)
            GL20.glUniformMatrix4fv(((LWJGLWindow)this.model.window).boneMatricesFlag, false, this.matrices);
        else
            GL20.glUniformMatrix4fv(((LWJGLWindow)this.model.window).shadowMapBoneMatricesFlag, false, this.matrices);

        for (ModelPart mo: this.model.models)
        {
            VBOModelPart m = (VBOModelPart) mo;
            LWJGLWindow window = (LWJGLWindow) this.model.window;

            if (m.material.useDefaultDepthMask)
                window.setDrawOptions(depthTest, m.material.glow);
            else
                window.setDrawOptions(depthTest, m.material.glow, m.material.depthMask);

            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
            Rotation.transform(window, yaw, pitch, roll);
            Scale.transform(window, sX, sY, sZ);

            if (m.material.texture != null)
                window.setTexture(m.material.texture, false);

            window.renderPosedVBO(m.vertexVBO, m.colorVBO, m.texVBO, this.bonesVBOs.get(m), m.shapes.length * 3);
            window.disableTexture();

            glPopMatrix();
        }

        ((LWJGLWindow)this.model.window).setDrawOptions(false, false, true);
    }

    @Override
    public void draw(double posX, double posY, double sX, double sY, double yaw)
    {
        for (ModelPart mo: this.model.models)
        {
            VBOModelPart m = (VBOModelPart) mo;
            LWJGLWindow window = (LWJGLWindow) this.model.window;

            if (m.material.useDefaultDepthMask)
                window.setDrawOptions(false, m.material.glow);
            else
                window.setDrawOptions(false, m.material.glow, m.material.depthMask);

            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, 0);
            Rotation.transform(window, yaw, 0, 0);
            Scale.transform(window, sX, sY, 0);

            if (m.material.texture != null)
                window.setTexture(m.material.texture, false);

            window.renderPosedVBO(m.vertexVBO, m.colorVBO, m.texVBO, this.bonesVBOs.get(m), m.shapes.length * 3);
            window.disableTexture();

            glPopMatrix();
        }
    }

    @Override
    public void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ)
    {

    }
}

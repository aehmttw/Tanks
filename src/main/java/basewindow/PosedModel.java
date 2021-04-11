package basewindow;

import java.util.HashMap;

public abstract class PosedModel implements IModel
{
    protected static double[] tempMatrix = new double[16];

    public Model model;
    public PoseBone[] bones;
    public HashMap<Model.Bone, PoseBone> boneMap = new HashMap<>();
    public HashMap<String, PoseBone> bonesByName = new HashMap<>();

    public PosedModel(Model model)
    {
        this.model = model;
        this.bones = new PoseBone[this.model.bones.size()];

        for (int i = 0; i < this.model.bones.size(); i++)
        {
            this.bones[i] = new PoseBone(this, this.model.bones.get(i));
            this.boneMap.put(this.model.bones.get(i), this.bones[i]);
            this.bonesByName.put(this.bones[i].bone.name, this.bones[i]);
        }
    }

    public static class PoseBone
    {
        public PosedModel posedModel;
        public Model.Bone bone;

        public double yaw;
        public double pitch;
        public double roll;

        public double offX;
        public double offY;
        public double offZ;

        public double[] matrix = new double[]{1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1};
        public double[] compiledMatrix = new double[]{1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1};

        public PoseBone(PosedModel p, Model.Bone b)
        {
            this.posedModel = p;
            this.bone = b;
        }

        public void computeMatrix()
        {
            for (int i = 0; i < this.matrix.length; i++)
            {
                if (i % 5 == 0)
                    this.matrix[i] = 1;
                else
                    this.matrix[i] = 0;
            }

            multiply(this.matrix, tempMatrix(Math.cos(roll), -Math.sin(roll), 0, 0,  Math.sin(roll), Math.cos(roll), 0, 0,  0, 0, 1, 0,  0, 0, 0, 1));
            multiply(this.matrix, tempMatrix(1, 0, 0, 0,  0, Math.cos(pitch), -Math.sin(pitch), 0,  0, Math.sin(pitch), Math.cos(pitch), 0,  0, 0, 0, 1));
            multiply(this.matrix, tempMatrix(Math.cos(yaw), 0, -Math.sin(yaw), 0,  0, 1, 0, 0,  Math.sin(yaw), 0, Math.cos(yaw), 0,  0, 0, 0, 1));

            multiply(this.matrix, tempMatrix(1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  offX, offY, offZ, 1));
        }

        public void compileMatrix()
        {
            if (this.bone.parent == null)
            {
                System.arraycopy(this.matrix, 0, this.compiledMatrix, 0, this.matrix.length);
            }
            else
            {
                for (int i = 0; i < this.compiledMatrix.length; i++)
                    this.compiledMatrix[i] = this.posedModel.boneMap.get(this.bone.parent).compiledMatrix[i];

                multiply(this.compiledMatrix, tempMatrix(1, 0, 0, 0, 0, 1, 0, 0,  0, 0, 1, 0,  -this.bone.posX, -this.bone.posY, -this.bone.posZ, 1));
                multiply(this.compiledMatrix, this.matrix);
                multiply(this.compiledMatrix, tempMatrix(1, 0, 0, 0, 0, 1, 0, 0,  0, 0, 1, 0,  this.bone.posX, this.bone.posY, this.bone.posZ, 1));
            }
        }
    }

    public static void multiply(double[] matrix, double[] multiplier)
    {
        double[] result = new double[16];

        for (int n = 0; n < result.length; n++)
        {
            int j = n % 4;
            int i = n / 4;

            for (int m = 0; m < 4; m++)
            {
                result[n] += matrix[m * 4 + j] * multiplier[m + i * 4];
            }

            result[0] = matrix[0] * multiplier[0] + matrix[4] * multiplier[1] + matrix[8] * multiplier[2] + matrix[12] * multiplier[3];
        }

        for (int i = 0; i < result.length; i++)
        {
            matrix[i] = result[i];
        }
    }

    public static double[] tempMatrix(double a1, double a2, double a3, double a4,
                                    double b1, double b2, double b3, double b4,
                                    double c1, double c2, double c3, double c4,
                                    double d1, double d2, double d3, double d4)
    {
        tempMatrix[0] = a1;
        tempMatrix[1] = a2;
        tempMatrix[2] = a3;
        tempMatrix[3] = a4;

        tempMatrix[4] = b1;
        tempMatrix[5] = b2;
        tempMatrix[6] = b3;
        tempMatrix[7] = b4;

        tempMatrix[8] = c1;
        tempMatrix[9] = c2;
        tempMatrix[10] = c3;
        tempMatrix[11] = c4;

        tempMatrix[12] = d1;
        tempMatrix[13] = d2;
        tempMatrix[14] = d3;
        tempMatrix[15] = d4;

        return tempMatrix;
    }
}

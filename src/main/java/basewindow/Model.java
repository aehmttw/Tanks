package basewindow;

import java.util.ArrayList;
import java.util.HashMap;

public class Model implements IModel
{
    public static Material defaultMaterial = new Material("");

    public ArrayList<ModelPart.Point> points;
    public ArrayList<ModelPart.Point> texCoords;
    public ArrayList<ModelPart.Point> normals;
    public ArrayList<double[]> colors;
    public HashMap<String, Material> materials;
    public ArrayList<Bone> bones;

    public ModelPart[] models;
    public BaseWindow window;

    public String file;

    public double[] bonesMatrix = new double[]{1, 0, 0,  0, 1, 0,  0, 0, 1};

    public Model(BaseWindow window, BaseFileManager fileManager, String dir)
    {
        this(window, dir, getModelFilesContent(fileManager, dir));
    }

    public static ArrayList<String> getModelFilesContent(BaseFileManager fileManager, String dir)
    {
        ArrayList<String> lines = fileManager.getInternalFileContents(dir + "model.mtl");

        if (lines == null)
            lines = fileManager.getInternalFileContents(dir + "model.obj");
        else
            lines.addAll(fileManager.getInternalFileContents(dir + "model.obj"));

        return lines;
    }

    public Model(BaseWindow window, String dir, ArrayList<String> lines)
    {
        this();

        this.file = dir;

        this.window = window;

        Material current = null;
        this.materials.put("default", Model.defaultMaterial);

        int bones = 0;
        for (String s : lines)
        {
            if (s.startsWith("newmtl "))
            {
                String n = s.split(" ")[1];
                current = new Material(n);
                materials.put(n, current);
            }

            if (s.startsWith("map_Ka "))
            {
                if (current != null)
                    current.texture = dir + s.split(" ")[1];
            }

            if (s.startsWith("dm "))
            {
                if (current != null)
                {
                    current.depthMask = Boolean.parseBoolean(s.split(" ")[1]);
                    current.useDefaultDepthMask = false;
                }
            }

            if (s.startsWith("col "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");
                    current.colorR = Double.parseDouble(parts[1]);
                    current.colorG = Double.parseDouble(parts[2]);
                    current.colorB = Double.parseDouble(parts[3]);

                    if (parts.length >= 5)
                        current.colorA = Double.parseDouble(parts[4]);
                }
            }

            if (s.startsWith("Ka "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;
                    current.ambient = new float[]{(float) Double.parseDouble(parts[1]), (float) Double.parseDouble(parts[2]), (float) Double.parseDouble(parts[3])};
                }
            }

            if (s.startsWith("Kd "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;
                    current.diffuse = new float[]{(float) Double.parseDouble(parts[1]), (float) Double.parseDouble(parts[2]), (float) Double.parseDouble(parts[3])};
                }
            }

            if (s.startsWith("Km "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;

                    float minR = (float) Double.parseDouble(parts[1]);
                    float minG = (float) Double.parseDouble(parts[2]);
                    float minB = (float) Double.parseDouble(parts[3]);

                    float maxR = (float) Double.parseDouble(parts[4]);
                    float maxG = (float) Double.parseDouble(parts[5]);
                    float maxB = (float) Double.parseDouble(parts[6]);

                    current.ambient = new float[]{(minR + maxR) / 2, (minG + maxG) / 2, (minB + maxB) / 2};
                    current.diffuse = new float[]{(maxR - minR) / 2, (maxG - minG) / 2, (maxB - minB) / 2};
                }
            }

            if (s.startsWith("Kmm "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;

                    float minR = (float) Double.parseDouble(parts[1]);
                    float minG = (float) Double.parseDouble(parts[2]);
                    float minB = (float) Double.parseDouble(parts[3]);

                    float maxR = (float) Double.parseDouble(parts[4]);
                    float maxG = (float) Double.parseDouble(parts[5]);
                    float maxB = (float) Double.parseDouble(parts[6]);

                    current.ambient = new float[]{minR, minG, minB};
                    current.diffuse = new float[]{(maxR - minR) / 2, (maxG - minG) / 2, (maxB - minB) / 2};
                }
            }

            if (s.startsWith("Ks "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;
                    current.specular = new float[]{(float) Double.parseDouble(parts[1]), (float) Double.parseDouble(parts[2]), (float) Double.parseDouble(parts[3])};
                }
            }

            if (s.startsWith("Ns "))
            {
                if (current != null)
                {
                    current.shininess = (float) Double.parseDouble(s.split(" ")[1]);
                }
            }

            if (s.startsWith("Kb "))
            {
                if (current != null)
                {
                    String[] parts = s.split(" ");

                    current.customLight = true;

                    float min = (float) Double.parseDouble(parts[1]);
                    float max = (float) Double.parseDouble(parts[2]);
                    boolean neg = Boolean.parseBoolean(parts[3]);

                    current.minBrightness = min;
                    current.maxBrightness = max;
                    current.negativeBrightness = neg;
                }
            }

            if (s.startsWith("cel "))
            {
                if (current != null)
                {
                    current.celSections = (float) Double.parseDouble(s.split(" ")[1]);
                }
            }

            if (s.startsWith("glow "))
            {
                if (current != null)
                {
                    current.glow = Boolean.parseBoolean(s.split(" ")[1]);
                }
            }

            if (s.startsWith("gm "))
            {
                String[] sections = s.split(" ");

                for (int i = 0; i < 9; i++)
                {
                    this.bonesMatrix[i] = Double.parseDouble(sections[i + 1]);
                }
            }

            if (s.startsWith("g "))
            {
                String[] sections = s.split(" ");
                this.bones.add(new Bone(sections[1], this.bonesMatrix, bones, Double.parseDouble(sections[2]), -Double.parseDouble(sections[3]), Double.parseDouble(sections[4])));
                bones++;
            }

            if (s.startsWith("gp "))
            {
                String[] sections = s.split(" ");
                this.bones.get(Integer.parseInt(sections[1]) - 1).setParent(this.bones.get(Integer.parseInt(sections[2]) - 1));
            }

            if (s.startsWith("v "))
            {
                String[] sections = s.split(" ");
                this.points.add(new ModelPart.Point(Double.parseDouble(sections[1]), -Double.parseDouble(sections[2]), Double.parseDouble(sections[3])));
            }

            if (s.startsWith("vt "))
            {
                String[] sections = s.split(" ");
                this.texCoords.add(new ModelPart.Point(Double.parseDouble(sections[1]), 1 - Double.parseDouble(sections[2]), 0));
            }

            if (s.startsWith("vn "))
            {
                String[] sections = s.split(" ");
                this.normals.add(new ModelPart.Point(Double.parseDouble(sections[1]), Double.parseDouble(sections[2]), Double.parseDouble(sections[3])));
            }

            if (s.startsWith("vc "))
            {
                String[] sections = s.split(" ");
                this.colors.add(new double[]{Double.parseDouble(sections[1]), Double.parseDouble(sections[2]), Double.parseDouble(sections[3]), Double.parseDouble(sections[4])});
            }

            if (s.startsWith("vg "))
            {
                String[] sections = s.split(" ");

                ModelPart.Point p = this.points.get(Integer.parseInt(sections[1]));
                int l = sections.length / 2 - 1;

                p.bones = new Bone[l];
                p.boneWeights = new double[l];

                int in = 0;
                double sum = 0;

                for (int i = 2; i < sections.length; i += 2)
                {
                    p.bones[in] = this.bones.get(Integer.parseInt(sections[i]) - 1);
                    p.boneWeights[in] = Double.parseDouble(sections[i + 1]);
                    sum += p.boneWeights[in];
                    in++;
                }
            }
        }

        ArrayList<ModelPart> parts = new ArrayList<>();
        ArrayList<ModelPart.Shape> shapes = new ArrayList<>();
        current = Model.defaultMaterial;

        boolean facesAdded = false;

        for (String s : lines)
        {
            if (s.startsWith("usemtl "))
            {
                if (facesAdded)
                {
                    ModelPart m = window.createModelPart(this, shapes, current);
                    parts.add(m);
                    m.processShapes();
                    shapes.clear();
                }

                current = materials.get(s.split(" ")[1]);
            }

            if (s.startsWith("f "))
            {
                facesAdded = true;
                String[] sections = s.split(" ");
                if (sections.length == 5)
                {
                    int[] v1 = objToIndex(sections[1].split("/"));
                    int[] v2 = objToIndex(sections[2].split("/"));
                    int[] v3 = objToIndex(sections[3].split("/"));
                    int[] v4 = objToIndex(sections[4].split("/"));
                    this.addTriangle(shapes, v1, v2, v3);
                    this.addTriangle(shapes, v1, v4, v3);
                }
                else if (sections.length == 4)
                {
                    int[] v1 = objToIndex(sections[1].split("/"));
                    int[] v2 = objToIndex(sections[2].split("/"));
                    int[] v3 = objToIndex(sections[3].split("/"));
                    this.addTriangle(shapes, v1, v2, v3);
                }
            }
        }

        ModelPart m = window.createModelPart(this, shapes, current);
        parts.add(m);
        m.processShapes();

        this.models = new ModelPart[parts.size()];

        int index = 0;

        for (ModelPart mo: parts)
        {
            if (mo.material == null || mo.material.depthMask)
            {
                this.models[index] = mo;
                index++;
            }
        }

        for (ModelPart mo: parts)
        {
            if (mo.material != null && !mo.material.depthMask)
            {
                this.models[index] = mo;
                index++;
            }
        }
    }

    public Model()
    {
        this.materials = new HashMap<>();

        this.points = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.colors = new ArrayList<>();
        this.bones = new ArrayList<>();

        this.points.add(new ModelPart.Point(0, 0, 0));
        this.texCoords.add(new ModelPart.Point(0, 0, 0));
        this.normals.add(new ModelPart.Point(0, 0, 0));
        this.colors.add(new double[]{1, 1, 1, 1});
    }

    public static int[] objToIndex(String[] s)
    {
        int[] i = new int[]{0, 0, 0, 0};

        for (int n = 0; n < s.length; n++)
        {
            if (!s[n].equals(""))
               i[n] = Integer.parseInt(s[n]);
        }

        return i;
    }

    public void addTriangle(ArrayList<ModelPart.Shape> shapes, int[] v1, int[] v2, int[] v3)
    {
        shapes.add(new ModelPart.Triangle(this.points.get(v1[0]), this.points.get(v2[0]), this.points.get(v3[0]),
                this.texCoords.get(v1[1]), this.texCoords.get(v2[1]), this.texCoords.get(v3[1]),
                this.normals.get(v1[2]), this.normals.get(v2[2]), this.normals.get(v3[2]),
                this.colors.get(v1[3]), this.colors.get(v2[3]), this.colors.get(v3[3])));
    }

    public void draw(double posX, double posY, double sX, double sY, double yaw)
    {
        for (ModelPart m: this.models)
            m.draw(posX, posY, sX, sY, yaw);
    }

    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest)
    {
        for (ModelPart m: this.models)
            m.draw(posX, posY, posZ, sX, sY, sZ, yaw, pitch, roll, depthTest);
    }

    public void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ)
    {
        for (ModelPart m: this.models)
            m.draw2D(posX, posY, posZ, sX, sY, sZ);
    }

    public static class Material
    {
        public String name;
        public String texture;
        public boolean depthMask = true;
        public boolean useDefaultDepthMask = true;
        public boolean glow = false;
        public boolean useNormals = false;

        public double colorR = 1;
        public double colorG = 1;
        public double colorB = 1;
        public double colorA = 1;

        public boolean customLight = false;
        public float[] ambient = new float[3];
        public float[] diffuse = new float[3];
        public float[] specular = new float[3];
        public float shininess = 1;
        public float celSections = 0;

        public double minBrightness = -1;
        public double maxBrightness = 1;
        public boolean negativeBrightness = true;

        public Material(String name)
        {
            this.name = name;
        }
    }

    public static class Bone
    {
        public double posX;
        public double posY;
        public double posZ;

        public double offX;
        public double offY;
        public double offZ;

        public Bone parent;
        public String name;

        public int index;

        public Bone(String name, int index, double x, double y, double z)
        {
            this.name = name;
            this.index = index;
            this.posX = x;
            this.posY = -y;
            this.posZ = z;
            this.offX = x;
            this.offY = -y;
            this.offZ = z;
        }

        public Bone(String name, double[] matrix, int index, double x, double y, double z)
        {
            this.name = name;
            this.index = index;
            this.posX = matrix[0] * x + matrix[1] * y + matrix[2] * z;
            this.posY = matrix[3] * x + matrix[4] * y + matrix[5] * z;
            this.posZ = matrix[6] * x + matrix[7] * y + matrix[8] * z;
            this.offX = posX;
            this.offY = posY;
            this.offZ = posZ;
        }

        public void setParent(Bone b)
        {
            this.parent = b;
            this.offX = this.posX - b.posX;
            this.offY = this.posY - b.posY;
            this.offZ = this.posZ - b.posZ;
        }
    }

    @Override
    public String toString()
    {
        return this.file;
    }
}

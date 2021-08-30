package basewindow;

import java.util.ArrayList;
import java.util.HashMap;

public class PosedModelPose implements IPosedModelFrame
{
    public HashMap<String, PosedBone> bones = new HashMap<>();

    public PosedModelPose(BaseFileManager fileManager, String file)
    {
        this(fileManager.getInternalFileContents(file));
    }

    public PosedModelPose(ArrayList<String> lines)
    {
        double time = 0;

        for (String line : lines)
        {
            if (line.startsWith("rotation "))
            {
                String[] s = line.split(" ");

                this.getBone(s[1]).rotation = new PosedBone.BoneRotationPose
                        (time, Math.toRadians(Double.parseDouble(s[2])), Math.toRadians(Double.parseDouble(s[3])), Math.toRadians(Double.parseDouble(s[4])));
            }
            else if (line.startsWith("translation "))
            {
                String[] s = line.split(" ");

                this.getBone(s[1]).translation = new PosedBone.BoneTranslationPose
                        (time, Double.parseDouble(s[2]), Double.parseDouble(s[3]), Double.parseDouble(s[4]));
            }
        }
    }

    protected PosedBone getBone(String name)
    {
        if (!this.bones.containsKey(name))
            this.bones.put(name, new PosedBone(this, name));

        return this.bones.get(name);
    }

    public void apply(PosedModel m, double frac)
    {
        for (PosedBone b: this.bones.values())
            b.apply(m, frac);
    }

    public static class PosedBone
    {
        public static BoneRotationPose defaultRotation = new BoneRotationPose(0, 0, 0, 0);
        public static BoneTranslationPose defaultTranslation = new BoneTranslationPose(0, 0, 0, 0);

        public PosedModelPose animation;
        public String name;
        public BoneRotationPose rotation = defaultRotation;
        public BoneTranslationPose translation = defaultTranslation;

        public PosedBone(PosedModelPose anim, String name)
        {
            this.name = name;
            this.animation = anim;
        }

        public void apply(PosedModel m, double frac)
        {
            PosedModel.PoseBone b = m.bonesByName.get(this.name);
            this.rotation.apply(b, frac);
            this.translation.apply(b, frac);
        }

        public static abstract class BonePose
        {
            public double time;

            public BonePose(double time)
            {
                this.time = time;
            }

            public abstract void apply(PosedModel.PoseBone b, double frac);
        }

        public static class BoneRotationPose extends BonePose
        {
            public double yaw;
            public double pitch;
            public double roll;

            public BoneRotationPose(double time, double yaw, double pitch, double roll)
            {
                super(time);
                this.yaw = yaw;
                this.pitch = pitch;
                this.roll = roll;
            }

            @Override
            public void apply(PosedModel.PoseBone b, double frac)
            {
                b.yaw += this.yaw * frac;
                b.pitch += this.pitch * frac;
                b.roll += this.roll * frac;
            }
        }

        public static class BoneTranslationPose extends BonePose
        {
            public double offX;
            public double offY;
            public double offZ;

            public BoneTranslationPose(double time, double offX, double offY, double offZ)
            {
                super(time);
                this.offX = offX;
                this.offY = offY;
                this.offZ = offZ;
            }

            @Override
            public void apply(PosedModel.PoseBone b, double frac)
            {
                b.offX += this.offX * frac;
                b.offY += this.offY * frac;
                b.offZ += this.offZ * frac;
            }
        }
    }
}

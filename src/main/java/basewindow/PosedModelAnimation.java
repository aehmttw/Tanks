package basewindow;

import java.util.ArrayList;
import java.util.HashMap;

public class PosedModelAnimation implements IPosedModelFrame
{
    public double duration;
    public boolean looped;
    public boolean cubic = true;

    public HashMap<String, AnimatedBone> bones = new HashMap<>();

    public PosedModelAnimation(BaseFileManager fileManager, String file)
    {
        this(fileManager.getInternalFileContents(file));
    }

    public PosedModelAnimation(ArrayList<String> lines)
    {
        double time = 0;

        for (String line : lines)
        {
            if (line.startsWith("duration "))
            {
                duration = 100 * Double.parseDouble(line.split(" ")[1]);
                looped = line.contains("looped");
            }
            else if (line.startsWith("time "))
                time = 100 * Double.parseDouble(line.split(" ")[1]);
            else if (line.startsWith("rotation "))
            {
                String[] s = line.split(" ");

                this.getBone(s[1]).rotationFrames.add(new AnimatedBone.AnimatedBoneRotationFrame
                        (time, Math.toRadians(Double.parseDouble(s[2])), Math.toRadians(Double.parseDouble(s[3])), Math.toRadians(Double.parseDouble(s[4]))));
            }
            else if (line.startsWith("translation "))
            {
                String[] s = line.split(" ");

                this.getBone(s[1]).translationFrames.add(new AnimatedBone.AnimatedBoneTranslationFrame
                        (time, Double.parseDouble(s[2]), -Double.parseDouble(s[3]), Double.parseDouble(s[4])));
            }
            else if (line.startsWith("interpolation "))
            {
                String[] s = line.split(" ");

                if (s[1].equals("linear"))
                    cubic = false;
                else if (s[1].equals("cubic"))
                    cubic = true;
            }
        }
    }

    protected AnimatedBone getBone(String name)
    {
        if (!this.bones.containsKey(name))
            this.bones.put(name, new AnimatedBone(this, name));

        return this.bones.get(name);
    }

    public void apply(PosedModel m, double time, double frac)
    {
        if (this.looped)
            time %= this.duration;

        for (AnimatedBone b: this.bones.values())
            b.apply(m, time, frac);
    }

    public static class AnimatedBone
    {
        public static AnimatedBoneRotationFrame defaultRotation = new AnimatedBoneRotationFrame(0, 0, 0, 0);
        public static AnimatedBoneTranslationFrame defaultTranslation = new AnimatedBoneTranslationFrame(0, 0, 0, 0);

        public PosedModelAnimation animation;
        public String name;
        public ArrayList<AnimatedBoneRotationFrame> rotationFrames = new ArrayList<>();
        public ArrayList<AnimatedBoneTranslationFrame> translationFrames = new ArrayList<>();

        public AnimatedBone(PosedModelAnimation anim, String name)
        {
            this.name = name;
            this.animation = anim;
        }

        public void apply(PosedModel m, double time, double frac)
        {
            PosedModel.PoseBone b = m.bonesByName.get(this.name);
            this.applyRotation(b, time, frac);
            this.applyTranslation(b, time, frac);
        }

        public void applyRotation(PosedModel.PoseBone b, double time, double frac)
        {
            if (this.rotationFrames.size() <= 0)
                return;

            int index = 0;
            int lastIndex = 0;

            AnimatedBoneRotationFrame last = defaultRotation;

            for (AnimatedBoneRotationFrame f : this.rotationFrames)
            {
                if (f.time <= time)
                {
                    last = f;
                    lastIndex = index;
                }
                else
                    break;

                index++;
            }

            AnimatedBoneRotationFrame next = this.rotationFrames.get(getIndex(lastIndex + 1, this.rotationFrames.size()));;

            if (next == last)
                last.apply(b, frac);
            else
            {
                double time1 = last.time;
                double time2 = next.time;

                double loopedTime = time;

                if (animation.looped)
                {
                    while (time2 < time1)
                        time2 += animation.duration;

                    while (loopedTime < time1)
                        loopedTime += animation.duration;
                }

                double frac2 = (loopedTime - time1) / (time2 - time1);

                if (this.animation.cubic)
                {
                    AnimatedBoneRotationFrame beforeLast = this.rotationFrames.get(getIndex(lastIndex - 1, this.rotationFrames.size()));
                    AnimatedBoneRotationFrame afterNext = this.rotationFrames.get(getIndex(lastIndex + 2, this.rotationFrames.size()));;

                    beforeLast.applyCubic(b, frac2, frac, 0);
                    last.applyCubic(b, frac2, frac, 1);
                    next.applyCubic(b, frac2, frac, 2);
                    afterNext.applyCubic(b, frac2, frac, 3);
                }
                else
                {
                    last.apply(b, frac * (1 - frac2));
                    next.apply(b, frac * frac2);
                }
            }
        }

        public void applyTranslation(PosedModel.PoseBone b, double time, double frac)
        {
            if (this.translationFrames.size() <= 0)
                return;

            int index = 0;
            int lastIndex = 0;

            AnimatedBoneTranslationFrame last = defaultTranslation;

            for (AnimatedBoneTranslationFrame f : this.translationFrames)
            {
                if (f.time <= time)
                {
                    last = f;
                    lastIndex = index;
                }
                else
                    break;

                index++;
            }

            AnimatedBoneTranslationFrame next = this.translationFrames.get(getIndex(lastIndex + 1, this.translationFrames.size()));;

            if (next == last)
                last.apply(b, frac);
            else
            {
                double time1 = last.time;
                double time2 = next.time;

                double loopedTime = time;

                if (animation.looped)
                {
                    while (time2 < time1)
                        time2 += animation.duration;

                    while (loopedTime < time1)
                        loopedTime += animation.duration;
                }

                double frac2 = (loopedTime - time1) / (time2 - time1);

                if (this.animation.cubic)
                {
                    AnimatedBoneTranslationFrame beforeLast = this.translationFrames.get(getIndex(lastIndex - 1, this.translationFrames.size()));
                    AnimatedBoneTranslationFrame afterNext = this.translationFrames.get(getIndex(lastIndex + 2, this.translationFrames.size()));;

                    beforeLast.applyCubic(b, frac2, frac, 0);
                    last.applyCubic(b, frac2, frac, 1);
                    next.applyCubic(b, frac2, frac, 2);
                    afterNext.applyCubic(b, frac2, frac, 3);
                }
                else
                {
                    last.apply(b, frac * (1 - frac2));
                    next.apply(b, frac * frac2);
                }
            }
        }

        public static int getIndex(int in, int l)
        {
            if (l == 0)
                return 0;

            while (in < 0)
                in += l;

            while (in >= l)
                in -= l;

            return in;
        }

        public static abstract class AnimatedBoneFrame
        {
            public double time;

            public AnimatedBoneFrame(double time)
            {
                this.time = time;
            }

            public abstract void apply(PosedModel.PoseBone b, double frac);

            public void applyCubic(PosedModel.PoseBone b, double frac, double frac2, int index)
            {
                if (index == 0)
                    this.apply(b, (-0.5 * Math.pow(frac, 3) + Math.pow(frac, 2) - 0.5 * frac) * frac2);
                else if (index == 1)
                    this.apply(b, (1.5 * Math.pow(frac, 3) - 2.5 * Math.pow(frac, 2) + 1) * frac2);
                else if (index == 2)
                    this.apply(b, (-1.5 * Math.pow(frac, 3) + 2 * Math.pow(frac, 2) + 0.5 * frac) * frac2);
                else if (index == 3)
                    this.apply(b, (0.5 * Math.pow(frac, 3) - 0.5 * Math.pow(frac, 2)) * frac2);
            }
        }

        public static class AnimatedBoneRotationFrame extends AnimatedBoneFrame
        {
            public double yaw;
            public double pitch;
            public double roll;

            public AnimatedBoneRotationFrame(double time, double yaw, double pitch, double roll)
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

        public static class AnimatedBoneTranslationFrame extends AnimatedBoneFrame
        {
            public double offX;
            public double offY;
            public double offZ;

            public AnimatedBoneTranslationFrame(double time, double x, double y, double z)
            {
                super(time);
                this.offX = x;
                this.offY = y;
                this.offZ = z;
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

package tanks.rendering;

import basewindow.BaseWindow;
import tanks.obstacle.ObstacleBeatBlock;

/**
 * Shader for the ground tiles under beat blocks
 */
public class ShaderGroundObstacleBeatBlock extends RendererShader implements IObstacleSizeShader, IUpdatedShader
{
    public Uniform1f obstacleSizeFrac;

    public ShaderGroundObstacleBeatBlock(BaseWindow w)
    {
        super(w, "ground_obstacle_beat");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_ground_obstacles.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_ground_obstacles.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public void setSize(float size)
    {
        this.obstacleSizeFrac.set(size);
    }

    @Override
    public void update(int num)
    {
        int freq = (int) Math.pow(2, num / 2);
        boolean alt = num % 2 == 1;
        int warningBeats = freq == 1 ? 3 : freq == 2 ? 2 : 0;
        double timeTillChange = ObstacleBeatBlock.timeTillChange(freq) % 600;
        boolean on = ObstacleBeatBlock.isOn(freq, alt);

        float small = 0.0f;
        float large = 1.0f;
        float size;
        if (warningBeats > 0)
        {
            if (on)
                size = (float) (small + (large - small) * Math.min(1, timeTillChange / 8));
            else
                size = (float) (small + (large - small) * (1.0 - Math.min(1, timeTillChange / 8)));
        }
        else
            size = (on ? large : small);

        setSize(size);
    }
}

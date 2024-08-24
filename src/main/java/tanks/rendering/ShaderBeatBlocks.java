package tanks.rendering;

import basewindow.BaseWindow;
import basewindow.OnlyBaseUniform;
import tanks.Game;
import tanks.obstacle.Obstacle;
import tanks.obstacle.ObstacleBeatBlock;

public class ShaderBeatBlocks extends RendererShader implements IUpdatedShader, IObstacleCenterCoordShader
{
    public Uniform1f obstacleSizeFrac;
    public Uniform1f outlineSizeFrac;

    @OnlyBaseUniform
    public Uniform1f flashFrac;

    public Attribute3f centerCoord;

    public ShaderBeatBlocks(BaseWindow w)
    {
        super(w, "beatblocks");
    }

    @Override
    public void initialize() throws Exception
    {
        this.shaderBase.setUp("/shaders/main.vert", new String[]{"/shaders/main_beat_blocks.vert"}, "/shaders/main.frag", null);
        this.shaderShadowMap.setUp("/shaders/shadow_map.vert", new String[]{"/shaders/main_beat_blocks.vert"}, "/shaders/shadow_map.frag", null);
    }

    @Override
    public Attribute3f getCenterCoord()
    {
        return this.centerCoord;
    }

    @Override
    public void update(int num)
    {
        int freq = (int) Math.pow(2, num / 2);
        boolean alt = num % 2 == 1;

        double timeTillChange = ObstacleBeatBlock.timeTillChange(freq) % (600 / freq);

        double beatTime = 37.5;
        int warningBeats = freq == 1 ? 3 : freq == 2 ? 2 : 0;
        float flash = 0f;

        if (timeTillChange < beatTime * warningBeats)
            flash = (float) Math.max(0, ((timeTillChange + 600.0) % beatTime) / beatTime - 0.5);

        boolean on = ObstacleBeatBlock.isOn(freq, alt);
        float f = (float) (Obstacle.draw_size / Game.tile_size);

        float small = 0.25f;
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

        if (!on)
            flash *= 2;

        this.flashFrac.set(flash);
        this.obstacleSizeFrac.set(f * (size * 0.95f));
        this.outlineSizeFrac.set(f * size);
    }
}

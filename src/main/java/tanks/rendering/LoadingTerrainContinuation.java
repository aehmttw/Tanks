package tanks.rendering;

public class LoadingTerrainContinuation extends RuntimeException
{
    public TerrainRenderer renderer;

    public LoadingTerrainContinuation(TerrainRenderer r)
    {
        this.renderer = r;
    }
}

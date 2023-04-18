package tanks;

public interface IDrawableLightSource extends IDrawable
{
    boolean lit();

    /**
     * @return A 7-length array whose 3 element is the brightness of the light. 4-6 are the light color.
     * The first 3 elements will be auto-populated with the object's position.
     * It is recommended to save an array and always return it instead of making a new one every time, to save on memory.
     */
    double[] getLightInfo();
}

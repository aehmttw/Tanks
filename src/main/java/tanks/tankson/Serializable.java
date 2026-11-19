package tanks.tankson;

public interface Serializable
{
    /** Any value TanksON can write: a string, a number, a list, or a map. */
    Object serialize();

    /** The value {@link #serialize()} wrote, as TanksON read it back. */
    Serializable deserialize(Object o);
}

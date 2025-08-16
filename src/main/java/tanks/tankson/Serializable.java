package tanks.tankson;

public interface Serializable
{
    String serialize();
    Serializable deserialize(String s);
}

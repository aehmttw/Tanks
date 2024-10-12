package tanks.tankson;

public interface Serializable {
    public String serialize();
    public Serializable deserialize(String s);
}

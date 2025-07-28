package tanks.obstacle;

public interface ISolidObject
{
	default boolean disableRayCollision() { return false; }

	Face[] getFaces();

	void updateFaces();
}

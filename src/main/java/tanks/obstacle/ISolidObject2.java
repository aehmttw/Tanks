package tanks.obstacle;

public interface ISolidObject2
{
	default boolean disableRayCollision() { return false; }

	Face2[] getFaces();

	void updateFaces();
}

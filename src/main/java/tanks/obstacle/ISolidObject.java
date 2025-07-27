package tanks.obstacle;

public interface ISolidObject
{
	default boolean disableRayCollision() {return false;}

	Face[] getHorizontalFaces();

	Face[] getVerticalFaces();

	Face[] getFaces();

	void updateFaces();
}

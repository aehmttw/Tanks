package tanks.obstacle;

public interface ISolidObject
{
	default boolean disableRayCollision() { return false; }

	/** Horizontal faces are top/bottom faces, as they span the X direction. */
	Face[] getHorizontalFaces();

	/** Vertical faces are left/right faces, as they span the Y direction. */
	Face[] getVerticalFaces();

	default boolean[] getValidHorizontalFaces(boolean unbreakable)
	{
		return null;
	}

	default boolean[] getValidVerticalFaces(boolean unbreakable)
	{
		return null;
	}
}

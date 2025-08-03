package tanks;

/**
 * Represents a direction in the game.
 * Each direction has an associated {@linkplain #x()} and {@linkplain #y()}, which represents the direction's vector.
 * Each direction has an index, specified in {@linkplain #index()}.
 * */
public enum Direction
{
    up, right, down, left, upLeft, upRight, downLeft, downRight;

    public static final int[] X = {0, 1, 0, -1, 0, 1, -1, 0, 1, -1}, Y = {-1, 0, 1, 0, 1, 0, 0, -1, 1, -1};

    protected static final Direction[] values = Direction.values();

    /** Returns true if {@linkplain #x()} is nonzero. */
    public boolean isNonZeroX()
    {
        return x() != 0;
    }

    /** Returns true if {@linkplain #y()} is nonzero. */
    public boolean isNonZeroY()
    {
        return y() != 0;
    }

    /** Returns true if {@linkplain #x()} and {@linkplain #y()} are both nonzero. */
    public boolean isDiagonal()
    {
        return x() != 0 && y() != 0;
    }

    /** Returns the direction opposite to this direction. */
    public Direction opposite()
    {
        int offset = ordinal() >= 4 ? 4 : 0;
        return values[(this.ordinal() - offset + 2) % 4 + offset];
    }

    /**
     *  Returns the index of the direction, where:<br>
     *  0 = up<br>
     *  1 = right<br>
     *  2 = down<br>
     *  3 = left<br>
     *  4 = upLeft<br>
     *  5 = upRight<br>
     *  6 = downLeft<br>
     *  7 = downRight
     * */
    public int index()
    {
        return this.ordinal();
    }

    /** Returns the x component of the direction's vector. */
    public int x()
    {
        return X[this.ordinal()];
    }

    /** Returns the y component of the direction's vector. */
    public int y()
    {
        return Y[this.ordinal()];
    }

    /**
     * Returns the direction with the specified index,
     * as specified in {@linkplain #index()}.
     * */
    public static Direction fromIndex(int i)
    {
        return values[i];
    }
}

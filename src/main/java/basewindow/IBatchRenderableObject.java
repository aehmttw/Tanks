package basewindow;

public interface IBatchRenderableObject
{
    boolean positionChanged();

    boolean colorChanged();

    boolean wasRedrawn();

    void setRedrawn(boolean b);
}

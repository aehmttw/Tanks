package basewindow;

import java.util.ArrayList;

public abstract class BaseFileManager
{
    protected ArrayList<String> overrideLocations = new ArrayList<>();

    public void setOverrideLocations(ArrayList<String> overrideLocations)
    {
        this.overrideLocations = overrideLocations;
    }

    public abstract BaseFile getFile(String file);

    public abstract ArrayList<String> getInternalFileContents(String file);
}

package basewindow;

import java.util.ArrayList;

public abstract class BaseFileManager
{
    public abstract BaseFile getFile(String file);

    public abstract ArrayList<String> getInternalFileContents(String file);
}

package basewindow;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public abstract class BaseFile
{
    public String path;

    public BaseFile(String path)
    {
        this.path = path;
    }

    public abstract boolean exists();

    public abstract boolean create() throws IOException;

    public abstract void renameTo(String name);

    public abstract void delete();

    public abstract ArrayList<String> getSubfiles() throws IOException;

    public abstract void startReading() throws FileNotFoundException;

    public abstract boolean hasNextLine();

    public abstract String nextLine();

    public abstract void stopReading();

    public abstract void startWriting() throws FileNotFoundException;

    public abstract void println(String s);

    public abstract void stopWriting();

    public abstract void mkdirs();

    public abstract long lastModified();
}

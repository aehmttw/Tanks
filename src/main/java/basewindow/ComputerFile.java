package basewindow;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ComputerFile extends BaseFile
{
    public File file;
    public BufferedReader scanner;
    public PrintWriter printWriter;

    public String nextLine = null;

    public ComputerFile(String path)
    {
        super(path);
        file = new File(path);
    }

    @Override
    public boolean exists()
    {
        return file.exists();
    }

    @Override
    public boolean create() throws IOException
    {
        return file.createNewFile();
    }

    @Override
    public void renameTo(String name)
    {
        file.renameTo(new File(name));
    }

    @Override
    public void delete()
    {
        file.delete();
    }

    @Override
    public ArrayList<String> getSubfiles() throws IOException
    {
        DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(this.path));
        ArrayList<String> files = new ArrayList<>();

        for (Path p: ds)
            files.add(p.toString());

        ds.close();

        return files;
    }

    @Override
    public void startReading() throws FileNotFoundException
    {
        scanner = new BufferedReader( new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
    }

    @Override
    public String nextLine() throws IOException
    {
        if (nextLine != null)
        {
            String s = nextLine;
            nextLine = null;
            return s;
        }

        return scanner.readLine();
    }

    @Override
    public boolean hasNextLine() throws IOException
    {
        if (nextLine != null)
            return true;

        nextLine = scanner.readLine();

        return nextLine != null;
    }

    @Override
    public void stopReading() throws IOException
    {
        scanner.close();
    }

    @Override
    public void startWriting() throws FileNotFoundException
    {
        printWriter = new PrintWriter(file);
    }

    @Override
    public void println(String s)
    {
        printWriter.println(s);
    }

    @Override
    public void stopWriting()
    {
        printWriter.close();
    }

    @Override
    public void mkdirs()
    {
        file.mkdirs();
    }

    @Override
    public long lastModified()
    {
        return file.lastModified();
    }
}

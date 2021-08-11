package basewindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class ComputerFile extends BaseFile
{
    public File file;
    public Scanner scanner;
    public PrintWriter printWriter;

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
        ArrayList<String> files = new ArrayList<String>();

        for (Path p: ds)
            files.add(p.toString());

        ds.close();

        return files;
    }

    @Override
    public void startReading() throws FileNotFoundException
    {
        scanner = new Scanner(this.file);
    }

    @Override
    public String nextLine()
    {
        return scanner.nextLine();
    }

    @Override
    public boolean hasNextLine()
    {
        return scanner.hasNextLine();
    }

    @Override
    public void stopReading()
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

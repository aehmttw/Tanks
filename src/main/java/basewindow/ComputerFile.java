package basewindow;

import java.io.*;
import java.nio.file.*;
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

        for (Path p : ds)
            files.add(p.toString());

        ds.close();

        return files;
    }

    @Override
    public void startReading() throws FileNotFoundException
    {
        scanner = new BufferedReader(new FileReader(file));
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

    @Override
    public boolean moveTo(String targetDir)
    {
        return moveTo(targetDir, false);
    }

    @Override
    public boolean moveTo(String targetDir, boolean override)
    {
        Path source = file.toPath();
        Path destinationDir = Paths.get(targetDir);
        // Build the target file path with the same filename
        Path target = destinationDir.resolve(source.getFileName());

        try
        {
            if (!override && Files.exists(target))
                return false;

            if (override)
                Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            else
                Files.move(source, target);

            return true;

        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}

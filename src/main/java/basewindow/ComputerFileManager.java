package basewindow;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ComputerFileManager extends BaseFileManager
{
    @Override
    public BaseFile getFile(String file)
    {
        return new ComputerFile(file);
    }

    @Override
    public ArrayList<String> getInternalFileContents(String file)
    {
        try
        {
            InputStream st = this.getResource(file);

            if (st == null)
                return null;

            Scanner s = new Scanner(new InputStreamReader(st));
            ArrayList<String> al = new ArrayList<>();

            while (s.hasNext())
            {
                al.add(s.nextLine());
            }

            s.close();
            return al;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public InputStream getResource(String path) throws FileNotFoundException
    {
        return getResource(this.overrideLocations, path);
    }

    public static InputStream getResource(ArrayList<String> overrideLocations, String path) throws FileNotFoundException
    {
        for (String overridesDir: overrideLocations)
        {
            File f = new File(overridesDir + path);

            if (f.exists())
                return new FileInputStream(f);
        }

        return ComputerFileManager.class.getResourceAsStream(path);
    }
}

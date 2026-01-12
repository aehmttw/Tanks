package basewindow;

import tanks.Game;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

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
        ArrayList<String> al = new ArrayList<>();

        try (InputStream st = this.getResource(file))
        {
            if (st == null)
                return al;

            BufferedReader reader = new BufferedReader(new InputStreamReader(st, StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null)
            {
                al.add(line);
            }

            return al;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return al;
        }
    }

    @Override
    public void openFileManager(String path)
    {
        String[] cmd;

        String url = "file:///" + path.replace("\\", "/");
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win"))
            cmd = new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
        else if (os.contains("mac"))
            cmd = new String[]{"open", url};
        else
            cmd = new String[]{"xdg-open", url};

        try
        {
            Runtime.getRuntime().exec(cmd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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

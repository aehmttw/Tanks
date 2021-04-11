package basewindow;

import java.io.InputStream;
import java.io.InputStreamReader;
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
        InputStream st = getClass().getResourceAsStream(file);

        if (st == null)
            return null;

        Scanner s = new Scanner(new InputStreamReader(st));
        ArrayList<String> al = new ArrayList<String>();

        while (s.hasNext())
        {
            al.add(s.nextLine());
        }

        s.close();
        return al;
    }
}

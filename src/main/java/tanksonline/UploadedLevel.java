package tanksonline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.UUID;

public class UploadedLevel
{
    public String name;
    public String level;
    public UUID creator;
    public long time;

    public enum UploadResult {success, nameTaken, error}

    public UploadedLevel(String name, String level, UUID creator, long time)
    {
        this.name = name;
        this.creator = creator;
        this.level = level;
        this.time = time;
    }

    public UploadedLevel(File f)
    {
        try
        {
            if (f.exists())
            {
                BufferedReader br = new BufferedReader(new FileReader(f));
                this.level = br.readLine();
                this.name = br.readLine();
                this.creator = UUID.fromString(br.readLine());
                this.time = Long.parseLong(br.readLine());
                br.close();
            }
            else
            {
                System.out.println("Attempted to load an inexistent level: " + f);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public UploadResult save(File f)
    {
        try
        {
            if (!f.exists())
            {
                f.createNewFile();

                PrintWriter pw = new PrintWriter(f);
                pw.println(level);
                pw.println(name);
                pw.println(creator);
                pw.println(time);
                pw.close();

                return UploadResult.success;
            }
            else
                return UploadResult.nameTaken;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return UploadResult.error;
        }
    }
}

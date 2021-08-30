package tanksonline;

import tanks.Game;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

public class PlayerMap
{
    public static final String username_file = "usernames.tanks";
    public static final String levels_dir = "levels";
    public static final String access_codes_dir = "accesscodes";

    public static final PlayerMap instance = new PlayerMap();

    protected final HashMap<UUID, TanksOnlinePlayer> players = new HashMap<UUID, TanksOnlinePlayer>();

    protected final ArrayList<UploadedLevel> levels = new ArrayList<UploadedLevel>();

    public void setupPlayer(UUID id, String name)
    {
        TanksOnlinePlayer p = players.get(id);

        if (p == null)
        {
            p = new TanksOnlinePlayer(id, name);
            players.put(id, p);
        }

        p.username = name;
    }

    public String getUsername(UUID id)
    {
        return players.get(id).username;
    }

    public UploadedLevel.UploadResult uploadLevel(UploadedLevel l)
    {
        File dir = new File(levels_dir + "/" + l.creator);

        if (!dir.exists())
            dir.mkdir();

        UploadedLevel.UploadResult state = l.save(new File(levels_dir + "/" + l.creator + "/" + l.name + ".tanks"));

        if (state == UploadedLevel.UploadResult.success)
        {
            synchronized (PlayerMap.instance)
            {
                players.get(l.creator).levels.add(0, l);
                levels.add(0, l);
            }
        }

        return state;
    }

    public void deleteLevel(UploadedLevel l)
    {
        players.get(l.creator).levels.remove(l);
        levels.remove(l);

        File file = new File(levels_dir + "/" + l.creator + "/" + l.name + ".tanks");

        while (file.exists())
        {
            file.delete();
        }
    }

    public ArrayList<UploadedLevel> getLevels()
    {
        return levels;
    }

    public ArrayList<UploadedLevel> getLevels(UUID player)
    {
        return players.get(player).levels;
    }

    public TanksOnlinePlayer getPlayer(UUID id)
    {
        return players.get(id);
    }

    public void save()
    {
        try
        {
            File file = new File(username_file);

            if (!file.exists())
                file.createNewFile();

            PrintWriter pw = new PrintWriter(file);

            for (UUID id: players.keySet())
            {
                if (players.get(id).registered)
                    pw.println(id + "=" + players.get(id).username);
            }

            pw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Game.logger.println(players.toString());
            System.out.println(players.toString());
        }
    }

    public void load()
    {
        File levelsFile = new File(levels_dir);
        levelsFile.mkdir();

        File accessCodesFile = new File(access_codes_dir);
        accessCodesFile.mkdir();


        File file = new File(username_file);

        synchronized (PlayerMap.instance)
        {
            if (file.exists())
            {
                try
                {
                    BufferedReader br = new BufferedReader(new FileReader(file));

                    while (true)
                    {
                        String in = br.readLine();

                        if (in == null)
                            break;

                        String[] sec = in.split("=");

                        try
                        {
                            if (sec.length >= 2)
                            {
                                UUID id = UUID.fromString(sec[0]);
                                String s = sec[1];
                                setupPlayer(id, s);
                                PlayerMap.instance.getPlayer(id).registered = true;
                            }
                        }
                        catch (Exception e)
                        {
                            System.out.println("Failed to parse: " + in);
                            e.printStackTrace();
                        }
                    }

                    br.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        try
        {
            DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(levels_dir));

            HashMap<Long, UploadedLevel> map = new HashMap<Long, UploadedLevel>();
            ArrayList<Long> times = new ArrayList<Long>();

            for (Path p : ds)
            {
                if (p.toString().contains(".DS_Store"))
                    continue;

                DirectoryStream<Path> ds2 = Files.newDirectoryStream(p);

                for (Path p2 : ds2)
                {
                    if (p2.toString().endsWith(".tanks"))
                    {
                        UploadedLevel l = new UploadedLevel(p2.toFile());
                        map.put(l.time, l);
                        times.add(l.time);
                    }
                }

                ds2.close();
            }

            times.sort(Comparator.naturalOrder());

            for (long t: times)
            {
                UploadedLevel l = map.get(t);
                players.get(l.creator).levels.add(0, l);
                levels.add(0, l);
            }

            ds.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(access_codes_dir));

            for (Path p : ds)
            {
                if (p.toString().endsWith(".tanks"))
                {
                    AccessCode a = new AccessCode(p.toFile());
                    AccessCode.accessCodes.put(a.id, a);
                }
            }

            ds.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

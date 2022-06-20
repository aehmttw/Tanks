package tanksonline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class AccessCode
{
    public static HashMap<UUID, AccessCode> accessCodes = new HashMap<>();

    public UUID id;
    public long expiration;
    public int maxUses;
    public String comment = "";
    public ArrayList<TanksOnlinePlayer> players = new ArrayList<>();

    public AccessCode(UUID id, long expiration, int maxUses)
    {
        this.id = id;
        this.expiration = expiration;
        this.maxUses = maxUses;
    }

    public AccessCode(UUID id, long expiration)
    {
        this.id = id;
        this.expiration = expiration;
    }

    public AccessCode(UUID id)
    {
        this.id = id;
        this.expiration = -1;
    }

    public AccessCode(File f)
    {
        try
        {
            if (f.exists())
            {
                BufferedReader br = new BufferedReader(new FileReader(f));
                this.id = UUID.fromString(br.readLine());
                this.expiration = Long.parseLong(br.readLine());
                this.maxUses = Integer.parseInt(br.readLine());
                this.comment = br.readLine();

                while (true)
                {
                    String s = br.readLine();

                    synchronized (PlayerMap.instance)
                    {
                        if (s != null)
                        {
                            TanksOnlinePlayer p = PlayerMap.instance.getPlayer(UUID.fromString(s));
                            this.players.add(p);
                            p.accessCode = this;
                        }
                        else
                            break;
                    }
                }

                br.close();
            }
            else
            {
                System.out.println("Attempted to load an inexistent access code: " + f);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean save(File f)
    {
        try
        {
            if (!f.exists())
            {
                f.createNewFile();
            }

            PrintWriter pw = new PrintWriter(f);
            pw.println(id);
            pw.println(expiration);
            pw.println(maxUses);
            pw.println(comment);

            for (TanksOnlinePlayer player : this.players)
            {
                pw.println(player.id);
            }

            pw.close();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean valid()
    {
        return System.currentTimeMillis() <= expiration || expiration < 0;
    }
}

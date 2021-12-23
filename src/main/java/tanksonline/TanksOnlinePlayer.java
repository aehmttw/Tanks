package tanksonline;

import java.util.ArrayList;
import java.util.UUID;

public class TanksOnlinePlayer
{
    public UUID id;
    public String username;
    public ArrayList<UploadedLevel> levels = new ArrayList<>();
    public AccessCode accessCode;
    public boolean registered = false;

    public TanksOnlinePlayer(UUID id, String username)
    {
        this.id = id;
        this.username = username;
    }

    @Override
    public String toString()
    {
        return "Player " + id + " with username " + username;
    }
}

package tanks.network.event;

import java.util.UUID;

import tanks.Game;
import tanks.Player;
import tanks.network.NetworkUtils;

import io.netty.buffer.ByteBuf;

public class EventSetupHotbar extends PersonalEvent
{
    public UUID playerID;
    public boolean items;
    public boolean coins;

    public EventSetupHotbar()
    {

    }

    public EventSetupHotbar(Player p)
    {
        this.playerID = p.clientID;
        this.items = p.hotbar.itemBar.showItems;
        this.coins = p.hotbar.enabledCoins;
    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeBoolean(this.items);
        b.writeBoolean(this.coins);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.items = b.readBoolean();
        this.coins = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && Game.player.clientID.equals(this.playerID))
        {
            Game.player.hotbar.itemBar.showItems = this.items;
            Game.player.hotbar.enabledCoins = this.coins;
        }
    }
}

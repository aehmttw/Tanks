package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.item.Item2;
import tanks.item.ItemBullet2;
import tanks.item.ItemEmpty2;
import tanks.item.ItemRemote2;
import tanks.item.legacy.Item;
import tanks.item.legacy.ItemBullet;
import tanks.item.legacy.ItemEmpty;
import tanks.item.legacy.ItemRemote;
import tanks.network.NetworkUtils;

import java.util.UUID;

public class EventSetItem extends PersonalEvent
{
    public String name;
    public UUID playerID;
    public int slot;
    public String texture;
    public int count;
    public int bounces = -1;
    public double range = -1;

    public EventSetItem()
    {

    }

    public EventSetItem(Player p, int slot, Item2.ItemStack<?> item)
    {
        this.playerID = p.clientID;
        this.slot = slot;

        if (item.item.icon == null)
            this.texture = "";
        else
            this.texture = item.item.icon;

        this.count = item.stackSize;
        this.name = item.item.name;

        if (item.item instanceof ItemBullet2)
        {
            ItemBullet2 i = (ItemBullet2) item.item;
            bounces = i.bullet.bounces;
            range = i.bullet.getLifespan();
        }

    }

    @Override
    public void write(ByteBuf b)
    {
        NetworkUtils.writeString(b, this.playerID.toString());
        b.writeInt(this.slot);
        NetworkUtils.writeString(b, this.texture);
        b.writeInt(this.count);
        NetworkUtils.writeString(b, this.name);
        b.writeInt(this.bounces);
        b.writeDouble(this.range);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.playerID = UUID.fromString(NetworkUtils.readString(b));
        this.slot = b.readInt();
        this.texture = NetworkUtils.readString(b);
        this.count = b.readInt();
        this.name = NetworkUtils.readString(b);
        this.bounces = b.readInt();
        this.range = b.readDouble();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            ItemRemote2 i = new ItemRemote2();
            i.icon = this.texture;
            i.name = this.name;
            i.bounces = this.bounces;
            i.lifeSpan = this.range;

            Item2.ItemStack<?> s = new ItemRemote2.ItemStackRemote(Game.player, i, 0);
            s.stackSize = this.count;

            if (s.stackSize == 0)
                s = new ItemEmpty2.ItemStackEmpty();

            Game.player.hotbar.itemBar.slots[slot] = s;
        }
    }
}

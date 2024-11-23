package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.item.Item;
import tanks.item.ItemBullet;
import tanks.item.ItemEmpty;
import tanks.item.ItemRemote;
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

    public double lifespan = -1;
    public double rangeMin = -1;
    public double rangeMax = -1;
    public boolean showTrace = true;

    public EventSetItem()
    {

    }

    public EventSetItem(Player p, int slot, Item.ItemStack<?> item)
    {
        this.playerID = p.clientID;
        this.slot = slot;

        if (item.item.icon == null)
            this.texture = "";
        else
            this.texture = item.item.icon;

        this.count = item.stackSize;
        this.name = item.item.name;

        if (item.destroy)
            this.count = -1;

        if (item.item instanceof ItemBullet)
        {
            ItemBullet i = (ItemBullet) item.item;
            bounces = i.bullet.bounces;
            lifespan = i.bullet.lifespan * i.bullet.speed;
            rangeMin = i.bullet.getRangeMin();
            rangeMax = i.bullet.getRangeMax();
            showTrace = i.bullet.showTrace;
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

        b.writeDouble(this.lifespan);
        b.writeDouble(this.rangeMin);
        b.writeDouble(this.rangeMax);
        b.writeBoolean(this.showTrace);
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

        this.lifespan = b.readDouble();
        this.rangeMin = b.readDouble();
        this.rangeMax = b.readDouble();
        this.showTrace = b.readBoolean();
    }

    @Override
    public void execute()
    {
        if (this.clientID == null && this.playerID.equals(Game.clientID))
        {
            ItemRemote i = new ItemRemote();
            i.icon = this.texture.equals("") ? null : this.texture;
            i.name = this.name;
            i.bounces = this.bounces;

            i.lifespan = this.lifespan;
            i.rangeMin = this.rangeMin;
            i.rangeMax = this.rangeMax;
            i.showTrace = this.showTrace;

            Item.ItemStack<?> s = new ItemRemote.ItemStackRemote(Game.player, i, 0);
            s.stackSize = this.count;

            if (s.stackSize < 0)
                s = new ItemEmpty.ItemStackEmpty();

            Game.player.hotbar.itemBar.slots[slot] = s;
        }
    }
}

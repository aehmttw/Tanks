package tanks.network.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.Player;
import tanks.item.Item;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayerRemote;

import java.util.ArrayList;

public class EventSetSelectedItems extends PersonalEvent
{
    public int slot;
    public int primary;
    public int secondary;

    public EventSetSelectedItems()
    {

    }

    public EventSetSelectedItems(int slot, int p, int s)
    {
        this.slot = slot;
        this.primary = p;
        this.secondary = s;
    }

    @Override
    public void write(ByteBuf b)
    {
        b.writeInt(this.slot);
        b.writeInt(this.primary);
        b.writeInt(this.secondary);
    }

    @Override
    public void read(ByteBuf b)
    {
        this.slot = b.readInt();
        this.primary = b.readInt();
        this.secondary = b.readInt();
    }

    @Override
    public void execute()
    {
        if (this.clientID != null)
        {
            for (int i = 0; i < Game.players.size(); i++)
            {
                Player p = Game.players.get(i);
                if (p.clientID.equals(this.clientID))
                {
                    if (p.hotbar.itemBar != null)
                    {
                        p.hotbar.itemBar.setItemLocal(-1);
                        p.hotbar.itemBar.setItemLocal(slot);
                        if (p.tank instanceof TankPlayable)
                        {
                            ArrayList<Item.ItemStack<?>> abilities = ((TankPlayable) p.tank).abilities;
                            if (primary >= 0 && primary < abilities.size() && !abilities.get(primary).item.rightClick)
                                p.hotbar.itemBar.selectedPrimaryAbility = primary;
                            if (secondary >= 0 && secondary < abilities.size() && abilities.get(secondary).item.rightClick)
                                p.hotbar.itemBar.selectedSecondaryAbility = secondary;
                        }
                    }

                    if (p.tank instanceof TankPlayerRemote)
                        ((TankPlayerRemote) p.tank).refreshAmmo();
                }
            }
        }
        else
        {
            if (Game.player.hotbar.itemBar != null)
            {
                Game.player.hotbar.itemBar.selectedPrimaryAbility = primary;
                Game.player.hotbar.itemBar.selectedSecondaryAbility = secondary;
                Game.player.hotbar.itemBar.selected = slot;
            }
        }
    }
}

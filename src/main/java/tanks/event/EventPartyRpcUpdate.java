package tanks.event;

import io.netty.buffer.ByteBuf;
import tanks.Game;
import tanks.rpc.RichPresenceEvent;

public class EventPartyRpcUpdate extends PersonalEvent{
    public int screen;

    public EventPartyRpcUpdate(int screen) {
        this.screen = screen;
    }

    @Override
    public void write(ByteBuf b) {
        b.writeInt(this.screen);
    }

    @Override
    public void read(ByteBuf b) {
        this.screen = b.readInt();
    }

    @Override
    public void execute() {
        Game.game.discordRPC.update(RichPresenceEvent.MULTIPLAYER, this.screen);
    }
}

package tanks.replay;

import io.netty.buffer.ByteBuf;
import tanks.Drawing;
import tanks.Game;
import tanks.Level;
import tanks.Movable;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.network.NetworkEventMap;
import tanks.network.NetworkUtils;
import tanks.network.event.INetworkEvent;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerController;
import tanks.tank.TankRemote;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ReplayEvents
{
    public static class Tick implements IReplayEvent
    {
        public ArrayList<INetworkEvent> events;
        public double ms;
        public double mouseX, mouseY;

        public Tick setParams(ArrayList<INetworkEvent> events, double ms)
        {
            this.events = events;
            this.ms = ms;
            this.mouseX = Drawing.drawing.getMouseX();
            this.mouseY = Drawing.drawing.getMouseY();
            return this;
        }

        @Override
        public void write(ByteBuf b)
        {
            b.writeInt(events.size()).writeDouble(ms);
            b.writeDouble(mouseX).writeDouble(mouseY);
            for (INetworkEvent e : events)
            {
                b.writeInt(NetworkEventMap.get(e.getClass()));
                e.write(b);
            }
        }

        @Override
        public void read(ByteBuf b)
        {
            int eventCnt = b.readInt();
            ms = b.readDouble();
            events = new ArrayList<>();
            mouseX = b.readDouble();
            mouseY = b.readDouble();
            try
            {
                for (int i = 0; i < eventCnt; i++)
                {
                    INetworkEvent e = NetworkEventMap.get(b.readInt()).getConstructor().newInstance();
                    e.read(b);
                    events.add(e);
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void execute()
        {
            for (INetworkEvent e : events)
                e.execute();
        }

        @Override
        public void drawDebug(double x, double y)
        {
            IReplayEvent.super.drawDebug(x, y - 40);
            String text = "Event types: " + String.join(", ", events.stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.toSet()));
            Drawing.drawing.setBoundedInterfaceFontSize(16, Drawing.drawing.interfaceSizeX - 50, text);
            Drawing.drawing.drawInterfaceText(x, y - 20, text, true);
            Drawing.drawing.drawInterfaceText(x, y, String.format("Ms: %.2f", ms), true);
        }

        @Override
        public double delay()
        {
            return ms;
        }
    }

    public static class LevelChange implements IReplayEvent
    {
        public String levelString;

        public LevelChange setLS(String levelString)
        {
            this.levelString = levelString;
            return this;
        }

        @Override
        public void write(ByteBuf b)
        {
            NetworkUtils.writeString(b, levelString);
        }

        @Override
        public void read(ByteBuf b)
        {
            levelString = NetworkUtils.readString(b);
        }

        @Override
        public void execute()
        {
            Game.cleanUp();
            ScreenPartyHost.isServer = true;
            new Level(levelString).loadLevel(true);
            ScreenPartyHost.isServer = false;

            ArrayList<Movable> add = new ArrayList<>(), remove = new ArrayList<>();
            for (Movable m : Game.movables)
            {
                if (!(m instanceof TankPlayerController || m instanceof TankPlayer))
                    continue;

                add.add(new TankRemote((Tank) m));
                remove.add(m);
            }
            Game.movables.addAll(add);
            Game.movables.removeAll(remove);

            Game.screen = new ScreenGame();
            ((ScreenGame) Game.screen).playingReplay = true;
            Game.playerTank.destroy = true;
        }
    }

    public interface IReplayEvent
    {
        void write(ByteBuf b);
        void read(ByteBuf b);
        void execute();

        default double delay()
        {
            return 0;
        }
        default void drawDebug(double x, double y)
        {
            Drawing.drawing.drawInterfaceText(x, y, getClass().getSimpleName(), true);
        }
    }
}

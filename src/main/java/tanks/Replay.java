package tanks;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkEventMap;
import tanks.network.NetworkUtils;
import tanks.network.event.INetworkEvent;
import tanks.network.event.IStackableEvent;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class Replay
{
    public static HashMap<Integer, Class<? extends IReplayEvent>> replayEventMap = new HashMap<>();
    public static HashMap<Class<? extends IReplayEvent>, Integer> replayIDMap = new HashMap<>();
    static
    {
        registerEvent(Tick.class);
        registerEvent(LevelChange.class);
    }

    public static int eventID = 0;
    public static void registerEvent(Class<? extends IReplayEvent> event)
    {
        int p = eventID++;
        replayEventMap.put(p, event);
        replayIDMap.put(event, p);
    }

    public static boolean isRecording;
    public static Replay currentReplay;
    public static int deltaCS = 100 / 30;

    public Level prevLevel;
    public String name = "test";
    public double lastAge;
    public ArrayList<IReplayEvent> events = new ArrayList<>();

    public static class Tick extends IReplayEvent
    {
        public ArrayList<INetworkEvent> events;

        public Tick()
        {
            this.events = new ArrayList<>();
        }

        @Override
        public void write(ByteBuf b)
        {
            b.writeInt(events.size());
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
            int ms = b.readInt();
            try
            {
                for (int i = 0; i < eventCnt; i++)
                    NetworkEventMap.get(b.readInt()).getConstructor().newInstance().read(b);
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
    }

    public static class LevelChange extends IReplayEvent
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
            new Level(levelString).loadLevel(true);
            Game.screen = new ScreenGame();
        }
    }

    public abstract static class IReplayEvent
    {
        abstract void write(ByteBuf b);
        abstract void read(ByteBuf b);
        abstract void execute();
    }

    public void play()
    {

    }

    public void updateReplay(ArrayList<INetworkEvent> eventsThisFrame)
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g == null || g.paused || !g.playing)
            return;

        double now = g.gameAge;
        if (now - lastAge <= deltaCS)
            eventsThisFrame.removeIf(t -> t instanceof IStackableEvent);

        if (eventsThisFrame.isEmpty())
            return;

        if (prevLevel != Game.currentLevel)
            events.add(new LevelChange().setLS(Game.currentLevel.levelString));

        Tick t = new Tick(eventsThisFrame);
        prevLevel = Game.currentLevel;

        events.add(t);
        lastAge = now;
    }

    public void save()
    {
        try
        {
            File f = new File(Game.homedir + Game.replaysDir + name + ".tanks");
            assert f.exists() || f.createNewFile();

            int bytesWritten = name.length();
            try (FileOutputStream out = new FileOutputStream(f))
            {
                ByteBuf buf = Unpooled.buffer();
                NetworkUtils.writeString(buf, name);
                buf.writeInt(events.size());
                for (IReplayEvent event : events)
                    event.write(buf);

                bytesWritten += out.getChannel().write(buf.nioBuffer());
            }
            catch (Exception e)
            {
                Game.exitToCrash(e);
            }

            System.out.println("Replay saved at " + f.getPath());
            System.out.println("Size: " + bytesWritten / 1024 + " KB");
        }
        catch (IOException e)
        {
            Game.exitToCrash(e);
        }
    }

    public static Replay read(String replayName)
    {
        Replay r = new Replay();
        File f = new File(Game.homedir + Game.replaysDir + replayName + ".tanks");
        int readBytes = 0;

        try (FileInputStream in = new FileInputStream(f))
        {
            FileChannel channel = in.getChannel();
            int size = (int) channel.size();
            ByteBuf buf = Unpooled.buffer().alloc().directBuffer(size, size);
            readBytes += buf.writeBytes(channel, 0, size);
            r.name = NetworkUtils.readString(buf);
            int prevBytes = buf.readableBytes();
            int eventCnt = buf.readInt();

            for (int i = 0; i < eventCnt; i++)
            {
                IReplayEvent event = replayEventMap.get(buf.readInt()).getConstructor().newInstance();
                event.read(buf);
                r.events.add(event);
            }
            readBytes += prevBytes - buf.readableBytes();
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        System.out.println("Replay read from " + f.getPath());
        System.out.println("Size: " + readBytes / 1024 + " KB");

        return r;
    }

    public static void toggleRecording()
    {
        isRecording = !isRecording;
        if (isRecording)
        {
            currentReplay = new Replay();
        }
        else
        {
            currentReplay.save();
            currentReplay = null;
        }
    }
}

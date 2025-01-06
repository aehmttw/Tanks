package tanks;

import basewindow.InputCodes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import tanks.gui.ScreenElement;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.network.NetworkUtils;
import tanks.network.event.INetworkEvent;
import tanks.network.event.IStackableEvent;
import tanks.replay.ReplayEvents.*;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Currency;
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

    public static double frameFreq;
    public static boolean isRecording;
    public static Replay currentReplay, currentPlaying;
    public static double deltaCS = 100 / 30.;

    public double age = 0;
    public int pos = 0;

    public Level prevLevel;
    public String name = "test";
    public double lastAge;
    public ArrayList<IReplayEvent> events = new ArrayList<>();

    public static void draw()
    {
        Replay r = Replay.currentPlaying;

        if (r == null)
            return;

        double x = Drawing.drawing.getInterfaceEdgeX(true) - 10, y = Drawing.drawing.getInterfaceEdgeY(true) - 20;
        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.setInterfaceFontSize(16);

        IReplayEvent event = r.getCurrentEvent();
        IReplayEvent prev = r.getPrevEvent();

        if (event != null)
            event.drawDebug(x, y);

        if (event instanceof Tick)
        {
            double mx = ((Tick) event).mouseX, my = ((Tick) event).mouseY;
            double prevMX = mx, prevMY = my;

            if (prev instanceof Tick)
            {
                prevMX = ((Tick) prev).mouseX;
                prevMY = ((Tick) prev).mouseY;
            }

            double percent = r.getTickDelta();

            int br = Level.isDark() ? 200 : 100;
            Drawing.drawing.setColor(br, br, br);
            Drawing.drawing.drawImage("cursor.png", interp(prevMX, mx, percent), interp(prevMY, my, percent), 50, 50);
        }
    }

    public static void preUpdate()
    {
        if (currentPlaying == null)
            return;

        frameFreq = Panel.frameFrequency;
    }

    public double getTickDelta()
    {
        return (age - lastAge) / (getCurrentEvent().delay() * 0.1);
    }

    public IReplayEvent getCurrentEvent()
    {
        if (pos >= events.size())
            return null;
        return events.get(pos);
    }

    public IReplayEvent getPrevEvent()
    {
        if (!Game.isOrdered(true, 0, pos - 1, events.size() - 1))
            return null;
        return events.get(pos - 1);
    }

    public static void update()
    {
        if (currentPlaying != null)
        {
            currentPlaying.play();
            currentPlaying.updateControls();

            if (currentPlaying.finished())
            {
                Panel.notifs.add(new ScreenElement.Notification("Finished playing recording"));
                currentPlaying = null;
            }
        }

        ScreenGame g = ScreenGame.getInstance();
        if ((g != null && g.playingReplay) && currentPlaying == null)
            Game.exitToTitle();
    }

    public boolean finished()
    {
        return pos >= events.size();
    }

    public void loadAndPlay()
    {
        Replay.currentPlaying = this;
    }

    public void shiftFrame(int frame)
    {
        pos += frame;
        getCurrentEvent().execute();

        if (Game.screen instanceof ScreenGame)
        {
            boolean prev = ((ScreenGame) Game.screen).paused;
            ((ScreenGame) Game.screen).paused = false;
            Game.screen.update();
            ((ScreenGame) Game.screen).paused = prev;
        }

        lastAge += getCurrentEvent().delay() * 0.1;
        age = lastAge;
    }

    public void shiftPosition(double ms)
    {
        if (ms < 0)
        {
            while (ms < 0 && pos > 0)
                ms += events.get(pos--).delay();
        }
        else
        {
            while (ms > 0 && pos < events.size() - 1)
                ms -= events.get(pos++).delay();
        }
    }

    public void play()
    {
        ScreenGame g = ScreenGame.getInstance();
        if (g != null && (!g.playing || g.paused))
            return;

        age += frameFreq;
        playNextTick();
    }

    InputBindingGroup left = new InputBindingGroup("replay.left", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_LEFT));
    InputBindingGroup right = new InputBindingGroup("replay.right", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_RIGHT));
    InputBindingGroup leftFrame = new InputBindingGroup("replay.lf", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_COMMA));
    InputBindingGroup rightFrame = new InputBindingGroup("replay.rf", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_PERIOD));

    public void updateControls()
    {
        if (left.isValid())
        {
            left.invalidate();
            shiftPosition(-1000);
        }
        if (right.isValid())
        {
            right.invalidate();
            shiftPosition(1000);
        }
        if (leftFrame.isValid())
        {
            leftFrame.invalidate();
            shiftFrame(-1);
        }
        if (rightFrame.isValid())
        {
            rightFrame.invalidate();
            shiftFrame(1);
        }
    }

    public void playNextTick()
    {
        if (pos >= events.size())
            return;

        try
        {
            IReplayEvent event = getCurrentEvent();
            while (event != null && age - lastAge > event.delay() * 0.1)
            {
                event.execute();
                lastAge += event.delay() * 0.1;
                pos++;
                event = getCurrentEvent();
            }
        }
        catch (Exception e)
        {
            currentPlaying = null;
            throw new RuntimeException(e);
        }
    }

    public void updateRecording(ArrayList<INetworkEvent> eventsThisFrame)
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

        Tick t = new Tick().setParams(eventsThisFrame, (now - lastAge) * 10);
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
                {
                    buf.writeInt(replayIDMap.get(event.getClass()));
                    event.write(buf);
                }

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

        int size = 0;
        try (FileInputStream in = new FileInputStream(f))
        {
            FileChannel channel = in.getChannel();
            size = (int) channel.size();
            ByteBuf buf = Unpooled.buffer().alloc().directBuffer(size, size);
            buf.writeBytes(channel, 0, size);
            r.name = NetworkUtils.readString(buf);
            int eventCnt = buf.readInt();

            for (int i = 0; i < eventCnt; i++)
            {
                int eventID = buf.readInt();
                IReplayEvent event = replayEventMap.get(eventID).getConstructor().newInstance();
                event.read(buf);
                r.events.add(event);
            }
        }
        catch (Exception e)
        {
            Game.exitToCrash(e);
        }

        System.out.println("Replay read from " + f.getPath());
        System.out.println("Size: " + size / 1024 + " KB");

        return r;
    }

    public static void toggleRecording()
    {
        isRecording = !isRecording;
        currentPlaying = null;
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

    public static double interp(double start, double end, double percentage)
    {
        return start + (end - start) * percentage;
    }
}

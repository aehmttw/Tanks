package tanks.gui.screen;

import tanks.*;
import tanks.event.EventShareLevel;
import tanks.event.online.EventUploadLevel;
import tanks.gui.Button;
import tanks.gui.ChatBox;
import tanks.gui.ChatMessage;
import tanks.obstacle.Obstacle;
import tanks.tank.TankSpawnMarker;

import java.util.ArrayList;

public class ScreenPreviewShareLevel extends Screen implements ILevelPreviewScreen, IPartyMenuScreen
{
    public String name;
    public Level level;
    public Screen screen;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<TankSpawnMarker>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, 350, 40, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            Game.screen = screen;
        }
    });


    public Button upload = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, 350, 40, "Share", new Runnable()
    {
        @Override
        public void run()
        {
            if (ScreenPartyHost.isServer)
            {
                Game.screen = ScreenPartyHost.activeScreen;
                EventShareLevel e = new EventShareLevel(level, name);
                e.clientID = Game.clientID;
                Game.eventsIn.add(e);
            }
            else
            {
                Game.screen = new ScreenPartyLobby();
                Game.eventsOut.add(new EventShareLevel(level, name));
            }

            Game.cleanUp();
        }
    });


    @SuppressWarnings("unchecked")
    protected ArrayList<IDrawable>[] drawables = (ArrayList<IDrawable>[])(new ArrayList[10]);

    public ScreenPreviewShareLevel(String name, Screen s)
    {
        this.music = "tomato_feast_4.ogg";
        this.musicID = "menu";

        this.name = name;

        for (int i = 0; i < drawables.length; i++)
        {
            drawables[i] = new ArrayList<IDrawable>();
        }

        Obstacle.draw_size = Game.tile_size;
        this.screen = s;
    }

    @Override
    public void update()
    {
        this.back.update();
        this.upload.update();

        if (Game.enable3d)
            for (int i = 0; i < Game.obstacles.size(); i++)
            {
                Obstacle o = Game.obstacles.get(i);

                if (o.replaceTiles)
                    o.postOverride();
            }

        if (ScreenPartyHost.isServer)
            ScreenPartyHost.chatbox.update();
        else if (ScreenPartyLobby.isClient)
            ScreenPartyLobby.chatbox.update();
    }

    public void drawLevel()
    {
        for (Effect e: Game.tracks)
            drawables[0].add(e);

        for (Movable m: Game.movables)
            drawables[m.drawLevel].add(m);

        for (Obstacle o: Game.obstacles)
            drawables[o.drawLevel].add(o);

        for (Effect e: Game.effects)
            drawables[7].add(e);

        for (int i = 0; i < this.drawables.length; i++)
        {
            if (i == 5 && Game.enable3d)
            {
                Drawing drawing = Drawing.drawing;
                Drawing.drawing.setColor(174, 92, 16);
                Drawing.drawing.fillForcedBox(drawing.sizeX / 2, -Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(drawing.sizeX / 2, Drawing.drawing.sizeY + Game.tile_size / 2, 0, drawing.sizeX + Game.tile_size * 2, Game.tile_size, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(-Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
                Drawing.drawing.fillForcedBox(drawing.sizeX + Game.tile_size / 2, drawing.sizeY / 2, 0, Game.tile_size, drawing.sizeY, Obstacle.draw_size, (byte) 0);
            }

            for (IDrawable d: this.drawables[i])
            {
                d.draw();

                if (d instanceof Movable)
                    ((Movable) d).drawTeam();
            }

            drawables[i].clear();
        }
    }

    @Override
    public void draw()
    {
        this.drawDefaultBackground();
        this.drawLevel();
        this.back.draw();
        this.upload.draw();

        ChatBox chatbox = null;
        ArrayList<ChatMessage> chat = null;

        if (ScreenPartyHost.isServer)
        {
            chatbox = ScreenPartyHost.chatbox;
            chat = ScreenPartyHost.chat;
        }
        else if (ScreenPartyLobby.isClient)
        {
            chatbox = ScreenPartyLobby.chatbox;
            chat = ScreenPartyLobby.chat;
        }

        chatbox.draw();

        Drawing.drawing.setColor(0, 0, 0);
        long time = System.currentTimeMillis();
        for (int i = 0; i < chat.size(); i++)
        {
            ChatMessage c = chat.get(i);
            if (time - c.time <= 30000 || chatbox.selected)
            {
                Drawing.drawing.drawInterfaceText(20, Drawing.drawing.interfaceSizeY - i * 30 - 70, c.message, false);
            }
        }
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}

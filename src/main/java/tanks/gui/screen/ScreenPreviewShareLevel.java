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

public class ScreenPreviewShareLevel extends Screen implements ILevelPreviewScreen
{
    public String name;
    public Level level;
    public Screen screen;

    public ArrayList<TankSpawnMarker> spawns = new ArrayList<TankSpawnMarker>();

    public Button back = new Button(Drawing.drawing.interfaceSizeX - 580, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Back", new Runnable()
    {
        @Override
        public void run()
        {
            Game.cleanUp();

            Game.screen = screen;
        }
    });


    public Button upload = new Button(Drawing.drawing.interfaceSizeX - 200, Drawing.drawing.interfaceSizeY - 90, this.objWidth, this.objHeight, "Share", new Runnable()
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
        super(350, 40, 380, 60);

        this.music = "menu_4.ogg";
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

                int x = (int) (o.posX / Game.tile_size);
                int y = (int) (o.posY / Game.tile_size);

                if (!(!Game.fancyTerrain || !Game.enable3d || x < 0 || x >= Game.currentSizeX || y < 0 || y >= Game.currentSizeY))
                    Game.game.heightGrid[x][y] = Math.max(o.getTileHeight(), Game.game.heightGrid[x][y]);
            }
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

            if (Game.glowEnabled)
            {
                for (IDrawable d : this.drawables[i])
                {
                    if (d instanceof IDrawableWithGlow && ((IDrawableWithGlow) d).isGlowEnabled())
                        ((IDrawableWithGlow) d).drawGlow();
                }
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
    }

    @Override
    public ArrayList<TankSpawnMarker> getSpawns()
    {
        return this.spawns;
    }
}

package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletFlame;
import tanks.event.EventChat;
import tanks.event.EventMineChangeTimer;
import tanks.event.EventMineExplode;
import tanks.event.EventUpdateCoins;
import tanks.gui.ChatMessage;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemMine;
import tanks.modapi.ModAPI;
import tanks.modapi.ModLevel;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.Scoreboard;
import tanks.obstacle.Obstacle;

import java.util.ArrayList;
import java.util.HashMap;

public class Mine extends Movable implements IAvoidObject
{
    public static double mine_size = 30;

    public double timer;
    public double size = mine_size;
    public double outlineColorR;
    public double outlineColorG;
    public double outlineColorB;
    public double height = 0;

    public double triggeredTimer = 50;
    public double damage = 2;
    public boolean destroysObstacles = true;

    public double radius = Game.tile_size * 2.5;
    public Tank tank;
    public Item item;
    public boolean exploded = false;
    public double cooldown = 0;
    public int lastBeep = Integer.MAX_VALUE;

    public int networkID;

    public static int currentID = 0;
    public static ArrayList<Integer> freeIDs = new ArrayList<>();
    public static HashMap<Integer, Mine> idMap = new HashMap<>();

    public Mine(double x, double y, double timer, Tank t)
    {
        this(x, y, timer, t, null);
    }

    public Mine(double x, double y, double timer, Tank t, ItemMine item)
    {
        super(x, y);

        if (t != null)
            this.posZ = t.posZ;

        if (this.posZ == 0)
            this.posZ = Game.sampleGroundHeight(this.posX, this.posY);

        this.timer = timer;
        this.drawLevel = 2;
        this.tank = t;

        this.item = item;

        if (!ScreenPartyLobby.isClient)
        {
            if (this.item == null)
                t.liveMines++;
            else
                ((ItemMine) this.item).liveMines++;
        }

        this.team = t.team;
        double[] outlineCol = Team.getObjectColor(t.colorR, t.colorG, t.colorB, t);
        this.outlineColorR = outlineCol[0];
        this.outlineColorG = outlineCol[1];
        this.outlineColorB = outlineCol[2];

        if (!ScreenPartyLobby.isClient)
        {
            if (freeIDs.size() > 0)
                this.networkID = freeIDs.remove(0);
            else
            {
                this.networkID = currentID;
                currentID++;
            }

            idMap.put(this.networkID, this);
        }

        for (FixedMenu m : ModAPI.menuGroup)
        {
            if (m instanceof Scoreboard && ((Scoreboard) m).objectiveType.equals(Scoreboard.objectiveTypes.mines_placed))
            {
                if (((Scoreboard) m).players.isEmpty())
                    ((Scoreboard) m).addTeamScore(this.team, 1);

                else if (this.tank instanceof TankPlayer)
                    ((Scoreboard) m).addPlayerScore(((TankPlayer) this.tank).player, 1);

                else if (this.tank instanceof TankPlayerRemote)
                    ((Scoreboard) m).addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
            }
        }
    }

    public Mine(double x, double y, Tank t)
    {
        this(x, y, 1000, t);
    }

    @Override
    public void draw()
    {
        Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 0.5);

        if (Game.enable3d && Game.enable3dBg && Game.fancyTerrain)
        {
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY - this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX - this.size / 2, this.posY + this.size / 2));
            this.height = Math.max(this.height, Game.sampleTerrainGroundHeight(this.posX + this.size / 2, this.posY + this.size / 2));
        }

        if (Game.enable3d)
        {
            for (double i = height; i < height + 6; i++)
            {
                double frac = ((i - height + 1) / 6 + 1) / 2;
                Drawing.drawing.setColor(this.outlineColorR * frac, this.outlineColorG  * frac, this.outlineColorB * frac, 255, 0.5);
                Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + i + 1.5, this.size, this.size, true, false);
            }

            Drawing.drawing.setColor(this.outlineColorR, this.outlineColorG, this.outlineColorB, 255, 1);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.posZ + height + 1, this.size * 4, this.size * 4, true, false);
        }
        else
        {
            Drawing.drawing.fillOval(this.posX, this.posY, this.size, this.size);

            if (Game.glowEnabled)
                Drawing.drawing.fillGlow(this.posX, this.posY, this.size * 4, this.size * 4);
        }

        Drawing.drawing.setColor(255, Math.min(1000, this.timer) / 1000.0 * 255, 0, 255, 0.5);

        if (timer < 150 && ((int) timer % 20) / 10 == 1)
            Drawing.drawing.setColor(255, 255, 0, 255, 0.5);

        if (Game.enable3d)
            Drawing.drawing.fillOval(this.posX, this.posY, this.posZ + height + 7.5, this.size * 0.8, this.size * 0.8, true, false);
        else
            Drawing.drawing.fillOval(this.posX, this.posY, this.size * 0.8, this.size * 0.8);
    }

    @Override
    public void update()
    {
        this.timer -= Panel.frameFrequency;

        if (this.timer < 0)
            this.timer = 0;

        if ((this.timer <= 0 || destroy) && !ScreenPartyLobby.isClient)
            this.explode();

        int beepTime = ((int)this.timer / 10);
        if (this.timer <= 150 && beepTime % 2 == 1 && this.lastBeep != beepTime && this.tank == Game.playerTank)
        {
            Drawing.drawing.playSound("beep.ogg", 1f, 0.25f);
            this.lastBeep = beepTime;
        }

        super.update();

        boolean enemyNear = false;
        boolean allyNear = false;
        for (Movable m: Game.movables)
        {
            if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(radius, 2))
            {
                if (m instanceof Tank && !m.destroy && ((Tank) m).targetable)
                {
                    if (Team.isAllied(m, this.tank))
                        allyNear = true;
                    else
                        enemyNear = true;
                }
            }
        }

        if (enemyNear && !allyNear && this.timer > this.triggeredTimer && !this.isRemote)
        {
            this.timer = this.triggeredTimer;
            Game.eventsOut.add(new EventMineChangeTimer(this));
        }
    }

    public void explode()
    {
        Drawing.drawing.playSound("explosion.ogg", (float) (mine_size / this.size));
        this.exploded = true;

        if (Game.effectsEnabled)
        {
            for (int j = 0; j < 200 * this.radius / 125 * Game.effectMultiplier; j++)
            {
                double random = Math.random();
                Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
                e.maxAge /= 2;
                e.colR = 255;
                e.colG = (1 - random) * 155 + Math.random() * 100;
                e.colB = 0;

                if (Game.enable3d)
                    e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.random() * Math.PI / 2, random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                else
                    e.setPolarMotion(Math.random() * 2 * Math.PI, random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                Game.effects.add(e);
            }
        }

        this.destroy = true;

        if (!ScreenPartyLobby.isClient)
        {
            Game.eventsOut.add(new EventMineExplode(this));

            for (Movable m: Game.movables)
            {
                if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(radius, 2))
                {
                    if (m instanceof Tank && !m.destroy && ((Tank) m).getDamageMultiplier(this) > 0)
                    {
                        if (!(Team.isAllied(this, m) && !this.team.friendlyFire) && !ScreenGame.finishedQuick)
                        {
                            Tank t = (Tank) m;

                            boolean kill = t.damage(this.damage, this);

                            if (kill)
                            {
                                for (FixedMenu menu : ModAPI.menuGroup)
                                {
                                    if (menu instanceof Scoreboard s && s.objectiveType.equals(Scoreboard.objectiveTypes.kills))
                                    {
                                        if (!s.teams.isEmpty())
                                            s.addTeamScore(this.tank.team, 1);

                                        else if (this.tank instanceof TankPlayer && !s.players.isEmpty())
                                            s.addPlayerScore(((TankPlayer) this.tank).player, 1);

                                        else if (this.tank instanceof TankPlayerRemote && !s.players.isEmpty())
                                            s.addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
                                    }
                                }

                                if (Game.currentLevel instanceof ModLevel l && l.enableKillMessages && ScreenPartyHost.isServer)
                                {
                                    String message = l.generateKillMessage(t, this.tank, false);
                                    ScreenPartyHost.chat.add(0, new ChatMessage(message));
                                    Game.eventsOut.add(new EventChat(message));
                                }


                                if (this.tank.equals(Game.playerTank))
                                {
                                    if (t instanceof TankPlayer || t instanceof TankPlayerRemote)
                                    {
                                        if (Game.currentGame != null)
                                            Game.player.hotbar.coins += Game.currentGame.playerKillCoins;
                                        else
                                            Game.player.hotbar.coins += Game.currentLevel.playerKillCoins;
                                    }

                                    else
                                        Game.player.hotbar.coins += t.coinValue;
                                }
                                else if (this.tank instanceof TankPlayerRemote tank && (Crusade.crusadeMode || Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0))
                                {
                                    if (t instanceof TankPlayer || t instanceof TankPlayerRemote)
                                    {
                                        if (Game.currentGame != null)
                                            tank.player.hotbar.coins += Game.currentGame.playerKillCoins;
                                        else
                                            tank.player.hotbar.coins += Game.currentLevel.playerKillCoins;
                                    }
                                    Game.eventsOut.add(new EventUpdateCoins(tank.player));
                                }
                            }
                            else
                                Drawing.drawing.playGlobalSound("damage.ogg");
                        }
                    }
                    else if (m instanceof Mine && !m.destroy)
                    {
                        if (((Mine) m).timer > 10 && !this.isRemote)
                        {
                            ((Mine) m).timer = 10;
                            Game.eventsOut.add(new EventMineChangeTimer((Mine) m));
                        }
                    }
                    else if (m instanceof Bullet && !(m instanceof BulletFlame) && !m.destroy)
                    {
                        m.destroy = true;
                    }
                }
            }
        }

        if (this.destroysObstacles)
        {
            for (Obstacle o: Game.obstacles)
            {
                if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius, 2) && o.destructible && !Game.removeObstacles.contains(o))
                {
                    o.onDestroy(this);
                    o.playDestroyAnimation(this.posX, this.posY, this.radius);
                }
            }
        }

        if (!ScreenPartyLobby.isClient)
        {
            if (!(this.item instanceof ItemMine))
                this.tank.liveMines--;
            else
                ((ItemMine) this.item).liveMines--;
        }

        Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.mineExplosion);
        e.radius = Math.max(this.radius - Game.tile_size * 0.5, 0);
        Game.effects.add(e);

        Game.removeMovables.add(this);

        freeIDs.add(this.networkID);
        idMap.remove(this.networkID);
    }

    @Override
    public double getRadius()
    {
        return this.radius;
    }
}

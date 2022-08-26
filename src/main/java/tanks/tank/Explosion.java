package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.event.*;
import tanks.gui.ChatMessage;
import tanks.modapi.menus.FixedMenu;
import tanks.modapi.menus.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.modapi.ModAPI;
import tanks.modapi.ModLevel;
import tanks.obstacle.Obstacle;

public class Explosion extends Movable
{
    public double damage;
    public boolean destroysObstacles;

    public double radius;
    public Tank tank;
    public Item item;

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank, Item item)
    {
        super(x, y);

        this.tank = tank;
        this.item = item;
        this.radius = radius;
        this.damage = damage;
        this.destroysObstacles = destroysObstacles;
        this.team = tank.team;
        this.isRemote = tank.isRemote;
    }

    public Explosion(double x, double y, double radius, double damage, boolean destroysObstacles, Tank tank)
    {
        this(x, y, radius, damage, destroysObstacles, tank, null);
    }

    public Explosion(Mine m)
    {
        this(m.posX, m.posY, m.radius, m.damage, m.destroysObstacles, m.tank, m.item);
    }

    public void explode()
    {
        Drawing.drawing.playSound("explosion.ogg", (float) (Mine.mine_radius / this.radius));

        if (Game.effectsEnabled)
        {
            for (int j = 0; j < Math.min(800, 200 * this.radius / 125) * Game.effectMultiplier; j++)
            {
                double random = Math.random();
                Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.piece);
                e.maxAge /= 2;
                e.colR = 255;
                e.colG = (1 - random) * 155 + Math.random() * 100;
                e.colB = 0;

                if (Game.enable3d)
                    e.set3dPolarMotion(Math.random() * 2 * Math.PI, Math.asin(Math.random()), random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                else
                    e.setPolarMotion(Math.random() * 2 * Math.PI, random * (this.radius - Game.tile_size / 2) / Game.tile_size * 2);
                Game.effects.add(e);
            }
        }

        this.destroy = true;

        if (!ScreenPartyLobby.isClient)
        {
            Game.eventsOut.add(new EventExplosion(this));

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
                                if (Game.currentGame != null)
                                    Game.currentGame.onKill(this.tank, t);

                                if (Game.currentLevel instanceof ModLevel)
                                {
                                    ((ModLevel) Game.currentLevel).onKill(this.tank, t);

                                    for (FixedMenu menu : ModAPI.menuGroup)
                                    {
                                        if (menu instanceof Scoreboard && ((Scoreboard) menu).objectiveType.equals(Scoreboard.objectiveTypes.kills))
                                        {
                                            if (!((Scoreboard) menu).teams.isEmpty())
                                                ((Scoreboard) menu).addTeamScore(this.tank.team, 1);

                                            else if (this.tank instanceof TankPlayer && !((Scoreboard) menu).players.isEmpty())
                                                ((Scoreboard) menu).addPlayerScore(((TankPlayer) this.tank).player, 1);

                                            else if (this.tank instanceof TankPlayerRemote && !((Scoreboard) menu).players.isEmpty())
                                                ((Scoreboard) menu).addPlayerScore(((TankPlayerRemote) this.tank).player, 1);
                                        }
                                    }

                                    if (((ModLevel) Game.currentLevel).enableKillMessages && ScreenPartyHost.isServer)
                                    {
                                        String message = ((ModLevel) Game.currentLevel).generateKillMessage(t, this.tank, false);
                                        ScreenPartyHost.chat.add(0, new ChatMessage(message));
                                        Game.eventsOut.add(new EventChat(message));
                                    }
                                }


                                if (this.tank.equals(Game.playerTank))
                                {
                                    if (Game.currentLevel instanceof ModLevel && (t instanceof TankPlayer || t instanceof TankPlayerRemote))
                                        Game.player.hotbar.coins += ((ModLevel) Game.currentLevel).playerKillCoins;
                                    else
                                        Game.player.hotbar.coins += t.coinValue;
                                }
                                else if (this.tank instanceof TankPlayerRemote && (Crusade.crusadeMode || Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0))
                                {
                                    if (t instanceof TankPlayer || t instanceof TankPlayerRemote)
                                    {
                                        if (Game.currentLevel instanceof ModLevel && ((ModLevel) Game.currentLevel).playerKillCoins > 0)
                                            ((TankPlayerRemote) this.tank).player.hotbar.coins += ((ModLevel) Game.currentLevel).playerKillCoins;
                                        else
                                            ((TankPlayerRemote) this.tank).player.hotbar.coins += t.coinValue;
                                    }
                                    Game.eventsOut.add(new EventUpdateCoins(((TankPlayerRemote) this.tank).player));
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
                    else if (m instanceof Bullet && !m.destroy)
                    {
                        m.destroy = true;
                    }
                }
            }
        }

        if (this.destroysObstacles && !ScreenPartyLobby.isClient)
        {
            for (Obstacle o: Game.obstacles)
            {
                if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius, 2) && o.destructible && !Game.removeObstacles.contains(o))
                {
                    o.onDestroy(this);
                    o.playDestroyAnimation(this.posX, this.posY, this.radius);
                    Game.eventsOut.add(new EventObstacleDestroy(o.posX, o.posY, this.posX, this.posY, this.radius));
                }
            }
        }

        Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.explosion);
        e.radius = Math.max(this.radius - Game.tile_size * 0.5, 0);
        Game.effects.add(e);
    }

    @Override
    public void draw()
    {

    }
}

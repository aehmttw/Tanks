package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.minigames.Arcade;
import tanks.network.event.*;
import tanks.gui.ChatMessage;
import tanks.gui.IFixedMenu;
import tanks.gui.Scoreboard;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.minigames.Minigame;
import tanks.obstacle.Obstacle;

public class Explosion extends Movable
{
    public double damage;
    public boolean destroysObstacles;
    public boolean destroysBullets = true;

    public double radius;
    public Tank tank;
    public Item item;

    public double knockbackRadius;
    public double bulletKnockback;
    public double tankKnockback;

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
        this.knockbackRadius = m.knockbackRadius;
        this.bulletKnockback = m.bulletKnockback;
        this.tankKnockback = m.tankKnockback;
        this.destroysBullets = m.destroysBullets;
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
                double size = 0;
                if (m instanceof Tank)
                    size = ((Tank) m).size;
                else if (m instanceof Mine)
                    size = ((Mine) m).size;
                else if (m instanceof Bullet)
                    size = ((Bullet) m).size;

                double distSq = Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2);
                if (distSq < Math.pow(knockbackRadius + size / 2, 2))
                {
                    double power = (1 - distSq / Math.pow(knockbackRadius + size / 2, 2));
                    if (m instanceof Bullet)
                    {
                        double angle = this.getAngleInDirection(m.posX, m.posY);
                        m.addPolarMotion(angle, power * this.bulletKnockback * Math.pow(Bullet.bullet_size, 2) / Math.max(1, Math.pow(((Bullet) m).size, 2)));
                        ((Bullet) m).collisionX = m.posX;
                        ((Bullet) m).collisionY = m.posY;
                        ((Bullet) m).addTrail();
                    }
                    else if (m instanceof Tank)
                    {
                        double angle = this.getAngleInDirection(m.posX, m.posY);
                        m.addPolarMotion(angle, power * this.tankKnockback * Math.pow(Game.tile_size, 2) / Math.max(1, Math.pow(((Tank) m).size, 2)));
                        Tank t = (Tank) m;
                        t.recoilSpeed = m.getSpeed();
                        if (t.recoilSpeed > t.maxSpeed)
                        {
                            t.inControlOfMotion = false;
                            t.tookRecoil = true;
                        }
                    }
                }

                if (Math.pow(Math.abs(m.posX - this.posX), 2) + Math.pow(Math.abs(m.posY - this.posY), 2) < Math.pow(radius + size / 2, 2))
                {
                    if (m instanceof Tank && !m.destroy && ((Tank) m).getDamageMultiplier(this) > 0)
                    {
                        if (!(Team.isAllied(this, m) && !this.team.friendlyFire) && !ScreenGame.finishedQuick)
                        {
                            Tank t = (Tank) m;
                            boolean kill = t.damage(this.damage, this);

                            if (kill)
                            {
                                if (Game.currentLevel instanceof Minigame)
                                {
                                    ((Minigame) Game.currentLevel).onKill(this.tank, t);

                                    for (IFixedMenu menu : ModAPI.menuGroup)
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

                                    if (((Minigame) Game.currentLevel).enableKillMessages && ScreenPartyHost.isServer)
                                    {
                                        String message = ((Minigame) Game.currentLevel).generateKillMessage(t, this.tank, false);
                                        ScreenPartyHost.chat.add(0, new ChatMessage(message));
                                        Game.eventsOut.add(new EventChat(message));
                                    }
                                }

                                if (this.tank.equals(Game.playerTank))
                                {
                                    if (Game.currentLevel instanceof Minigame && (t instanceof TankPlayer || t instanceof TankPlayerRemote))
                                        Game.player.hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
                                    else
                                        Game.player.hotbar.coins += t.coinValue;
                                }
                                else if (this.tank instanceof TankPlayerRemote && (Crusade.crusadeMode || Game.currentLevel.shop.size() > 0 || Game.currentLevel.startingItems.size() > 0))
                                {
                                    if (t instanceof TankPlayer || t instanceof TankPlayerRemote)
                                    {
                                        if (Game.currentLevel instanceof Minigame && ((Minigame) Game.currentLevel).playerKillCoins > 0)
                                            ((TankPlayerRemote) this.tank).player.hotbar.coins += ((Minigame) Game.currentLevel).playerKillCoins;
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
                    else if (m instanceof Bullet && !m.destroy && this.destroysBullets)
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
                if (Math.pow(Math.abs(o.posX - this.posX), 2) + Math.pow(Math.abs(o.posY - this.posY), 2) < Math.pow(radius + Game.tile_size / 2, 2) && o.destructible && !Game.removeObstacles.contains(o))
                {
                    o.onDestroy(this);
                    o.playDestroyAnimation(this.posX, this.posY, this.radius);
                    Game.eventsOut.add(new EventObstacleDestroy(o.posX, o.posY, o.name, this.posX, this.posY, this.radius + Game.tile_size / 2));
                }
            }
        }

        Effect e = Effect.createNewEffect(this.posX, this.posY, Effect.EffectType.explosion);
        e.radius = Math.max(this.radius, 0);
        Game.effects.add(e);
    }

    @Override
    public void draw()
    {

    }
}

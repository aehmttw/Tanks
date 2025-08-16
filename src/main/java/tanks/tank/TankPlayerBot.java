package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
import tanks.attribute.AttributeModifier;
import tanks.gui.screen.ScreenGame;
import tanks.hotbar.Hotbar;
import tanks.item.*;
import tanks.network.ConnectedPlayer;
import tanks.network.event.EventUpdateEliminatedPlayers;

import java.util.ArrayList;

public class TankPlayerBot extends TankPurple implements IServerPlayerTank
{
    public Player player;
    public ArrayList<Item.ItemStack<?>> abilities = new ArrayList<>();

    public double mineSelectTimerBase = 500;
    public double bulletSelectTimerBase = 500;

    public double mineSelectTimer = 0;
    public double bulletSelectTimer = 0;

    public TankPlayerBot(double x, double y, double angle, Player p)
    {
        super("player", x, y, angle);
        this.color.set(TankPlayer.default_primary_color);
        this.hasName = true;
        this.player = p;

        this.turretAimSpeed *= 2;

        this.bulletItem.networkIndex = -1;
        this.mineItem.networkIndex = -2;
        this.seekChance *= 5;

        this.abilities.add(this.bulletItem);
        this.abilities.add(this.mineItem);

        this.bulletAvoidBehavior = BulletAvoidBehavior.aggressive_dodge;
        this.nameTag.name = p.username;

        this.cooldown = 0;
    }

    @Override
    public Player getPlayer()
    {
        return this.player;
    }

    @Override
    public void updateStart()
    {
        double reload = em().getAttributeValue(AttributeModifier.reload, 1);

        Hotbar h = this.player.hotbar;
        if (h.enabledItemBar)
        {
            for (Item.ItemStack<?> i: h.itemBar.slots)
            {
                if (i != null && !i.isEmpty)
                {
                    i.player = this.player;
                    i.updateCooldown(reload);
                }
            }

            for (int i = 0; i < h.itemBar.slots.length; i++)
            {
                if (h.itemBar.slots[i].destroy)
                    h.itemBar.slots[i] = new ItemEmpty.ItemStackEmpty();
            }
        }

        for (int i = 0; i < this.player.hotbar.itemBar.slots.length; i++)
        {
            Item.ItemStack<?> s = this.player.hotbar.itemBar.slots[i];
            Integer num = Game.currentLevel.itemNumbers.get(s.item.name);
            s.networkIndex = num == null ? 0 : num;
            if (s instanceof ItemShield.ItemStackShield && Math.random() < 0.01 * Panel.frameFrequency)
                s.attemptUse(this);
        }

        this.bulletSelectTimer -= Panel.frameFrequency;
        this.mineSelectTimer -= Panel.frameFrequency;

        if (this.bulletSelectTimer <= 0 || this.bulletItem.destroy)
            this.selectBullet();

        if (this.mineSelectTimer <= 0 || this.mineItem.destroy)
            this.selectMine();
    }

    @Override
    public void fireBullet(Bullet b, double speed, double offset)
    {
        super.fireBullet(b, speed, offset);
        this.bulletSelectTimer = this.bulletSelectTimerBase;
    }

    @Override
    public void layMine(Mine m)
    {
        super.layMine(m);
        this.mineSelectTimer = this.mineSelectTimerBase;
    }

    public void selectBullet()
    {
        this.bulletSelectTimer = this.bulletSelectTimerBase;

        boolean hasAllies = false;
        for (Movable m: Game.movables)
        {
            if (m instanceof Tank && m != this && Team.isAllied(m, this))
            {
                hasAllies = true;
                break;
            }
        }

        ArrayList<ItemBullet.ItemStackBullet> bullets = new ArrayList<>();

        for (int i = 0; i < this.player.hotbar.itemBar.slots.length; i++)
        {
            Item.ItemStack<?> s = this.player.hotbar.itemBar.slots[i];

            if (s instanceof ItemBullet.ItemStackBullet)
            {
                Bullet b = ((ItemBullet) (s.item)).bullet;

                if (!(!hasAllies && b.damage <= 0 && !b.freezing && b.bulletHitKnockback == 0 && b.tankHitKnockback == 0 && b.hitStun <= 0))
                    bullets.add((ItemBullet.ItemStackBullet) s);
            }
        }

        int netIndex = -1;
        for (Item.ItemStack<?> i: this.abilities)
        {
            i.networkIndex = netIndex;
            netIndex--;

            if (i instanceof ItemBullet.ItemStackBullet && !i.destroy)
                bullets.add((ItemBullet.ItemStackBullet) i);
        }

        if (bullets.isEmpty())
            this.shootAIType = ShootAI.none;
        else
            this.setBulletItem(bullets.get((int) (Math.random() * bullets.size())));
    }

    public void selectMine()
    {
        this.mineSelectTimer = this.mineSelectTimerBase;

        ArrayList<ItemMine.ItemStackMine> mines = new ArrayList<>();

        for (int i = 0; i < this.player.hotbar.itemBar.slots.length; i++)
        {
            Item.ItemStack<?> s = this.player.hotbar.itemBar.slots[i];

            if (s instanceof ItemMine.ItemStackMine)
                mines.add((ItemMine.ItemStackMine) s);
        }

        int netIndex = -1;
        for (Item.ItemStack<?> i: this.abilities)
        {
            i.networkIndex = netIndex;
            netIndex--;

            if (i instanceof ItemMine.ItemStackMine && !i.destroy)
                mines.add((ItemMine.ItemStackMine) i);
        }

        if (mines.isEmpty())
            this.enableMineLaying = false;
        else
            this.setMineItem(mines.get((int) (Math.random() * mines.size())));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (Crusade.crusadeMode)
            this.player.remainingLives--;

        if (Game.screen instanceof ScreenGame)
        {
            ((ScreenGame) Game.screen).eliminatedPlayers.add(new ConnectedPlayer(this.player));
            Game.eventsOut.add(new EventUpdateEliminatedPlayers(((ScreenGame) Game.screen).eliminatedPlayers));
            ((ScreenGame) Game.screen).onPlayerDeath(this.player);
        }
    }

    @Override
    public void draw()
    {
        super.draw();

//        Drawing.drawing.setColor(255, 255, 255);
//        Drawing.drawing.drawImage(this.bulletItem.item.icon, this.posX, this.posY, 50, 50, 50);
//
//        Drawing.drawing.setFontSize(16);
//        Drawing.drawing.drawText(this.posX + 25, this.posY + 25, 50, this.bulletItem.stackSize + "");
//
//        Drawing.drawing.drawText(this.posX - 25, this.posY + 25, 50, this.player.hotbar.coins + "");
//
//        Drawing.drawing.drawText(this.posX, this.posY + 35, 50, this.player.ownedBuilds.size() + "");

    }

    public void setBulletItem(ItemBullet.ItemStackBullet i)
    {
        Bullet b = i.item.bullet;

        if (b instanceof BulletArc || b instanceof BulletAirStrike)
            this.shootAIType = ShootAI.straight;
        else if (b.homingSharpness > 0)
            this.shootAIType = ShootAI.homing;
        else if (b.bounces > 1)
            this.shootAIType = ShootAI.reflect;
        else if (b.bounces > 0)
            this.shootAIType = ShootAI.alternate;
        else
            this.shootAIType = ShootAI.straight;

        this.cooldownBase = i.item.cooldownBase;
        this.cooldownRandom = 0;
        this.bulletItem = i;
        this.setBullet(i.item.bullet);
        this.bulletItem.item.cooldownBase = this.cooldownBase;
        i.player = this.player;
    }

    public void setMineItem(ItemMine.ItemStackMine i)
    {
        this.enableMineLaying = true;
        this.mineItem = i;
        double cooldown = i.item.cooldownBase;
        this.setMine(i.item.mine);
        this.mineItem.item.cooldownBase = cooldown;
        i.player = this.player;
    }
}

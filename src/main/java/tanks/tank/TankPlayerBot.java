package tanks.tank;

import tanks.*;
import tanks.bullet.Bullet;
import tanks.bullet.BulletAirStrike;
import tanks.bullet.BulletArc;
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

    public TankPlayerBot(double x, double y, double angle, Player p)
    {
        super("player", x, y, angle);
        this.colorR = 0;
        this.colorG = 150;
        this.colorB = 255;
        this.showName = true;
        this.player = p;

        this.turretAimSpeed *= 2;

        this.bulletItem.networkIndex = 0;
        this.mineItem.networkIndex = -1;
        this.seekChance *= 5;

        this.abilities.add(this.bulletItem);
        this.abilities.add(this.mineItem);

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
        double reload = this.getAttributeValue(AttributeModifier.reload, 1);

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

        boolean foundBullet = false;
        boolean foundMine = false;
        for (int i = 0; i < this.player.hotbar.itemBar.slots.length; i++)
        {
            Item.ItemStack<?> s = this.player.hotbar.itemBar.slots[i];
            Integer num = Game.currentLevel.itemNumbers.get(s.item.name);
            s.networkIndex = num == null ? 0 : num;
            if (s instanceof ItemShield.ItemStackShield)
            {
                s.attemptUse(this);
            }

            if (s instanceof ItemBullet.ItemStackBullet && !foundBullet)
            {
                foundBullet = true;
                this.setBulletItem((ItemBullet.ItemStackBullet) s);
            }

            if (s instanceof ItemMine.ItemStackMine && !foundMine)
            {
                foundMine = true;
                this.setMineItem((ItemMine.ItemStackMine) s);
            }
        }

        int netIndex = 0;
        for (Item.ItemStack<?> i: this.abilities)
        {
            i.networkIndex = netIndex;
            netIndex--;
            if (i instanceof ItemBullet.ItemStackBullet && !foundBullet)
            {
                foundBullet = true;
                this.setBulletItem((ItemBullet.ItemStackBullet) i);
            }
            else if (i instanceof ItemMine.ItemStackMine && !foundMine)
            {
                foundMine = true;
                this.setMineItem((ItemMine.ItemStackMine) i);
            }
        }

        if (!foundBullet)
            this.shootAIType = ShootAI.none;

        if (!foundMine)
            this.enableMineLaying = false;
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

        Drawing.drawing.setColor(255, 255, 255);
        Drawing.drawing.drawImage(this.bulletItem.item.icon, this.posX, this.posY, 50, 50, 50);

        Drawing.drawing.setFontSize(16);
        Drawing.drawing.drawText(this.posX + 25, this.posY + 25, 50, this.bulletItem.stackSize + "");

        Drawing.drawing.drawText(this.posX - 25, this.posY + 25, 50, this.player.hotbar.coins + "");

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
        this.bullet = i.item.bullet;
        i.player = this.player;
    }

    public void setMineItem(ItemMine.ItemStackMine i)
    {
        this.enableMineLaying = true;
        this.mineItem = i;
        this.mine = i.item.mine;
        i.player = this.player;
    }
}

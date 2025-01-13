package tanks.hotbar;

import tanks.*;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.item.Item;
import tanks.item.ItemEmpty;
import tanks.minigames.Arcade;
import tanks.network.ServerHandler;
import tanks.network.event.EventSetItem;
import tanks.network.event.EventSetItemBarSlot;
import tanks.tank.TankPlayable;
import tanks.tank.TankPlayer;

public class ItemBar
{
	public static int size = 50; // The slot size.
	public static int count_margin_right = 26; // Item number's distance from right.
	public static int count_margin_bottom = 35; // Item number's distance from bottom.
	public static int gap = 60; // Gap between slots.
	public static int bar_margin = 60; // Bar's distance from bottom.

	public static double slotBgA = 127;
	public static double abilitySlotBgA = 64;

	public static double slotSelectedR = 255;
	public static double slotSelectedG = 128;
	public static double slotSelectedB = 0;

	public static double slotPrimarySelectedR = 0;
	public static double slotPrimarySelectedG = 75;
	public static double slotPrimarySelectedB = 127;

	public static double slotSecondarySelectedR = 127;
	public static double slotSecondarySelectedG = 0;
	public static double slotSecondarySelectedB = 0;

	public static double itemCountR = 255;
	public static double itemCountG = 255;
	public static double itemCountB = 255;

	public static final int item_bar_size = 5;

	public Item.ItemStack<?>[] slots = new Item.ItemStack[item_bar_size];
	public Button[] slotButtons = new Button[item_bar_size + TankPlayer.max_abilities];

	public double selectedTimer = 0;
	public String selectedText = "";
	public String selectedIcon = null;

	public int selected = -1;

	public double lastItemSwitch = -1000;
	public double age;

	public boolean showItems = false;

	public Player player;

	protected ItemEmpty.ItemStackEmpty defaultItemEmpty = new ItemEmpty.ItemStackEmpty();

	public ItemBar(Player p)
	{
		for (int i = 0; i < slots.length; i++)
			slots[i] = new ItemEmpty.ItemStackEmpty();

		this.player = p;

		for (int i = 0; i < this.slotButtons.length; i++)
		{
			final int j = i;
			this.slotButtons[i] = new Button(0, 0, size + 2.5, size * 1.5, "", () -> setItem(j));
		}
	}

	public boolean addItem(Item.ItemStack<?> item)
	{
		Item.ItemStack<?> i = item.getCopy();
		i.player = this.player;
		int emptyAmount = 0;
		for (Item.ItemStack<?> slot : this.slots)
		{
			if (slot.item.name.equals(i.item.name) || slot.isEmpty)
				emptyAmount += i.maxStackSize - slot.stackSize;
		}

		if (emptyAmount < i.stackSize && i.maxStackSize > 0)
			return false;

		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].item.name.equals(i.item.name) && ((this.slots[x].stackSize >= this.slots[x].maxStackSize && this.slots[x].maxStackSize > 0) || this.slots[x].stackSize == 0))
				continue;

			if (this.slots[x].item.name.equals(i.item.name))
			{
				if (this.slots[x].stackSize + i.stackSize <= this.slots[x].maxStackSize || this.slots[x].maxStackSize <= 0)
				{
					if (i.stackSize == 0)
						this.slots[x].stackSize = 0;
					else
						this.slots[x].stackSize += i.stackSize;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					return true;
				}
				else
				{
					int remaining = this.slots[x].stackSize + i.stackSize - this.slots[x].maxStackSize;
					this.slots[x].stackSize = this.slots[x].maxStackSize;
					i.stackSize = remaining;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					this.addItem(i);
					return true;
				}
			}
		}

		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].isEmpty)
			{
				if (i.stackSize <= i.maxStackSize || i.maxStackSize <= 0)
				{
					this.slots[x] = i;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					return true;
				}
				else
				{
					int remaining = i.stackSize - i.maxStackSize;
					this.slots[x] = i.getCopy();
					this.slots[x].stackSize = this.slots[x].maxStackSize;
					i.stackSize = remaining;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					this.addItem(i);
					return true;
				}
			}
		}
		return true;
	}

	public boolean useItem(boolean rightClick)
	{
		if (selected == -1)
			return false;

		if (slots[selected].isEmpty)
			return false;

		if (slots[selected].item.rightClick != rightClick)
			return false;

		slots[selected].attemptUse();

		boolean destroy = false;
		if (slots[selected].destroy)
		{
			destroy = true;
			slots[selected] = new ItemEmpty.ItemStackEmpty();
			this.selectedIcon = null;
			this.lastItemSwitch = this.age;
		}

		if (this.player != Game.player)
			Game.eventsOut.add(new EventSetItem(this.player, this.selected, this.slots[this.selected]));

		if (destroy && Game.currentLevel instanceof Arcade)
		{
			selected = -1;

			if (ScreenPartyHost.isServer)
			{
				for (ServerHandler sh : ScreenPartyHost.server.connections)
				{
					if (sh.player.equals(this.player))
						sh.events.add(new EventSetItemBarSlot(-1));
				}
			}
		}

		return true;
	}

	public Item.ItemStack<?> getSelectedItem(boolean rightClick)
	{
		if (selected == -1)
			return null;

		if (slots[selected].isEmpty)
			return null;

		if (slots[selected].item.rightClick != rightClick)
			return null;

		return slots[selected];
	}

	public void update()
	{
		this.age += Panel.frameFrequency;
		checkKey(Game.game.input.hotbarDeselect, -1);

		for (int i = 0; i < Game.game.input.hotbarBindings.length; i++)
		{
			checkKey(Game.game.input.hotbarBindings[i], i);
		}

		int items = this.showItems ? item_bar_size : 0;

		if (this.player.hotbar.persistent || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).shopScreen))
		{
			TankPlayer p = ((TankPlayer) this.player.tank);
			int count = items + p.abilities.size();
			for (int i = 0; i < count; i++)
			{
				this.slotButtons[i].enabled = Game.game.window.touchscreen;

				Button b = this.slotButtons[i];
				b.posX = ((i - (count - 1) / 2.0) * gap) + (Drawing.drawing.interfaceSizeX / 2);
				b.posY = Drawing.drawing.getInterfaceEdgeY(true) - bar_margin - this.player.hotbar.verticalOffset;
				b.update();
			}
		}

		if (Game.game.window.validScrollUp && items > 0)
		{
			this.setItem(((this.selected - 1) + items) % items);
			Game.game.window.validScrollUp = false;
		}

		if (Game.game.window.validScrollDown && items > 0)
		{
			this.setItem(((this.selected + 1) + items) % items);
			Game.game.window.validScrollDown = false;
		}

		this.selectedTimer = Math.max(0, this.selectedTimer - Panel.frameFrequency);
	}

	public void checkKey(InputBindingGroup input, int index)
	{
		if (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).paused)
			return;

		if (input.isValid())
		{
			this.setItem(index);
			input.invalidate();
		}
	}

	public void setItemLocal(int index)
	{
		if (this.player.hotbar != null)
		{
			this.player.hotbar.hidden = false;

			if (!Game.game.window.touchscreen)
				this.player.hotbar.hideTimer = 500;
		}

		if (!(this.player.tank instanceof TankPlayable))
			return;

		TankPlayable p = ((TankPlayable) (this.player.tank));

		int items = this.showItems ? item_bar_size : 0;
		if (index < items)
		{
			this.selected = (this.selected == index ? -1 : index);

			if (this.selected < 0)
			{
				this.selectedText = "";
				this.selectedIcon = null;
			}
			else
			{
				this.selectedText = this.slots[this.selected].item.name;
				this.selectedIcon = this.slots[this.selected].item.icon;
			}
		}
		else if (index - items < p.abilities.size())
		{
			selected = -1;
			Item.ItemStack<?> s = p.abilities.get(index - items);

			if (s.destroy)
			{
				this.selectedText = "Item depleted!";
				this.selectedIcon = null;
			}
			else
			{
				this.selectedText = s.item.name;
				this.selectedIcon = s.item.icon;
			}

			if (s.item.rightClick)
				p.selectedSecondaryAbility = index - items;
			else
				p.selectedPrimaryAbility = index - items;
		}
		else
			return;

		this.selectedTimer = 300;
		this.lastItemSwitch = this.age;

	}

	public void setItem(int index)
	{
		this.setItemLocal(index);

		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventSetItemBarSlot(index));
	}

	public void draw()
	{
		int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - bar_margin + this.player.hotbar.percentHidden - this.player.hotbar.verticalOffset);
		int items = this.showItems ? item_bar_size : 0;

		double slotBgBrightness = 0;

		if (Level.isDark())
			slotBgBrightness = 255;

		TankPlayer p = ((TankPlayer) this.player.tank);
		int count = items + p.abilities.size();
		for (int i = 0; i < count; i++)
		{
			Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);

			double x = (((i - (count - 1) / 2.0) * gap) + (Drawing.drawing.interfaceSizeX / 2));
			double eb = slotBgBrightness / 2;

			if (i == selected)
				Drawing.drawing.setColor(slotSelectedR, slotSelectedG, slotSelectedB, (100 - this.player.hotbar.percentHidden) * 2.55);
			else if (i - items == p.selectedPrimaryAbility)
			{
				double a = (selected >= 0 && !(slots[selected].item instanceof ItemEmpty) && !slots[selected].item.rightClick) ? 0.5 : 1;
				Drawing.drawing.setColor(slotPrimarySelectedR + eb, slotPrimarySelectedG + eb, slotPrimarySelectedB + eb, (100 - this.player.hotbar.percentHidden) * 1.27 * a);
			}
			else if (i - items == p.selectedSecondaryAbility)
			{
				double a = (selected >= 0 && !(slots[selected].item instanceof ItemEmpty) && slots[selected].item.rightClick) ? 0.5 : 1;
				Drawing.drawing.setColor(slotSecondarySelectedR + eb, slotSecondarySelectedG + eb, slotSecondarySelectedB + eb, (100 - this.player.hotbar.percentHidden) * 1.27 * a);
			}
			else if (i >= items)
				Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, abilitySlotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);

			if (i >= items)
				Drawing.drawing.fillInterfaceOval(x, y, size, size);
			else
				Drawing.drawing.fillInterfaceRect(x, y, size, size);

			Drawing.drawing.setColor(255, 255, 255, (100 - this.player.hotbar.percentHidden) * 2.55);

			Item.ItemStack<?> s = null;
			if (i < items)
				s = slots[i];
			else if (i - items < p.abilities.size())
				s = p.abilities.get(i - items);

			if (s != null)
			{
				if (s.destroy)
				{
					Drawing.drawing.setColor(255, 255, 255, 127);
					Drawing.drawing.drawInterfaceImage("noitem.png", x, y, size, size);
				}
				else if (s.item.icon != null)
					Drawing.drawing.drawInterfaceImage(s.item.icon, x, y, size, size);

				if (s.stackSize > 0)
				{
					Drawing.drawing.setColor(itemCountR, itemCountG, itemCountB, (100 - this.player.hotbar.percentHidden) * 2.55);

					int extra = 0;

					if (s.stackSize > 9999)
					{
						Drawing.drawing.setInterfaceFontSize(12);
						extra = 3;
					}
					else
						Drawing.drawing.setInterfaceFontSize(18);

					Drawing.drawing.drawInterfaceText(x + size - count_margin_right, y + extra + size - count_margin_bottom, Integer.toString(s.stackSize), true);
					Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);
				}
			}
		}

		if (Level.isDark())
			Drawing.drawing.setColor(255, 255, 255, Math.min(this.selectedTimer * 2.55 * 2, 255) * (100 - this.player.hotbar.percentHidden) * 0.01);
		else
			Drawing.drawing.setColor(0, 0, 0, Math.min(this.selectedTimer * 2.55 * 2, 255) * (100 - this.player.hotbar.percentHidden) * 0.01);

		Drawing.drawing.setInterfaceFontSize(24);
		Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, y - 70, this.selectedText);

		if ((this.player.hotbar.persistent || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).shopScreen)) && this.player.hotbar.percentHidden <= 0)
		{
			for (int i = 0; i < this.slotButtons.length; i++)
			{
				if (this.slotButtons[i].selected && !Game.game.window.touchscreen)
				{
					InputBindingGroup g = Game.game.input.hotbarBindings[i];

					if (g == null)
						continue;

					Drawing.drawing.setColor(255, 127, 0);
					Drawing.drawing.fillInterfaceGlow(this.slotButtons[i].posX, this.slotButtons[i].posY, 100, 100, true);

					if (Level.isDark())
						Drawing.drawing.setColor(0, 0, 0);
					else
						Drawing.drawing.setColor(255, 255, 255);

					String s = g.getInputs();

					if (s.length() > 1)
						Drawing.drawing.setInterfaceFontSize(24);
					else
						Drawing.drawing.setInterfaceFontSize(32);

					Drawing.drawing.drawInterfaceText(this.slotButtons[i].posX, this.slotButtons[i].posY, g.getInputs());
				}
			}
		}
	}

	public void drawOverlay()
	{
		if (this.age - lastItemSwitch < 200)
		{
			if (Game.playerTank != null && !Game.playerTank.destroy)
			{
				double a = 1;
				String icon = this.selectedIcon;

				if (this.selectedIcon == null)
				{
					a = 0.5;
					icon = "noitem.png";
				}

				Drawing.drawing.setColor(255, 255, 255, Math.min(1, 2 - (this.age - this.lastItemSwitch) / 100.0) * 255 * a);

				if (Game.enable3d)
					Drawing.drawing.drawImage(icon, Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size, Game.tile_size, Game.tile_size);
				else
					Drawing.drawing.drawImage(icon, Game.playerTank.posX, Game.playerTank.posY, Game.tile_size, Game.tile_size);
			}
		}
	}
}

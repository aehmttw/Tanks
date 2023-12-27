package tanks.hotbar;

import tanks.*;
import tanks.gui.screen.ScreenPartyHost;
import tanks.minigames.Arcade;
import tanks.network.Server;
import tanks.network.ServerHandler;
import tanks.network.event.EventSetItem;
import tanks.network.event.EventSetItemBarSlot;
import tanks.gui.Button;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemEmpty;

public class ItemBar
{
	public static int size = 50; // The slot size.
	public static int count_margin_right = 26; // Item number's distance from right.
	public static int count_margin_bottom = 35; // Item number's distance from bottom.
	public static int gap = 75; // Gap between slots.
	public static int bar_margin = 60; // Bar's distance from bottom.

	public static double slotBgA = 127;

	public static double slotSelectedR = 255;
	public static double slotSelectedG = 128;
	public static double slotSelectedB = 0;

	public static double itemCountR = 255;
	public static double itemCountG = 255;
	public static double itemCountB = 255;

	public Item[] slots = new Item[5];
	public Button[] slotButtons = new Button[5];

	public double selectedTimer = 0;

	public int selected = -1;

	public double lastItemSwitch = -1000;
	public double age;

	public Player player;

	protected ItemEmpty defaultItemEmpty = new ItemEmpty();

	public ItemBar(Player p)
	{
		for (int i = 0; i < slots.length; i++)
			slots[i] = new ItemEmpty();

		this.player = p;

		for (int i = 0; i < this.slotButtons.length; i++)
		{
			final int j = i;
			this.slotButtons[i] = new Button(0, 0, size + 2.5, size * 1.5, "", () -> setItem(j));
		}
	}

	public boolean addItem(Item item)
	{
		Item i = Item.parseItem(this.player, item.toString());
		int emptyAmount = 0;
		for (Item slot : this.slots)
		{
			if (slot.name.equals(i.name) || slot instanceof ItemEmpty)
				emptyAmount += i.maxStackSize - slot.stackSize;
		}

		if (emptyAmount < i.stackSize)
			return false;

		for (int x = 0; x < this.slots.length; x++)
		{
			if (this.slots[x].name.equals(i.name) && this.slots[x].stackSize >= this.slots[x].maxStackSize)
				continue;

			if (this.slots[x].name.equals(i.name))
			{
				if (this.slots[x].stackSize + i.stackSize <= this.slots[x].maxStackSize)
				{
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
			if (this.slots[x] instanceof ItemEmpty)
			{
				if (i.stackSize <= i.maxStackSize)
				{
					this.slots[x] = i;

					if (this.player != Game.player)
						Game.eventsOut.add(new EventSetItem(this.player, x, this.slots[x]));

					return true;
				}
				else
				{
					int remaining = i.stackSize - i.maxStackSize;
					this.slots[x] = Item.parseItem(this.player, i.toString());
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

		if (slots[selected] instanceof ItemEmpty)
			return false;

		if (slots[selected].rightClick != rightClick)
			return false;

		slots[selected].attemptUse();

		boolean destroy = false;
		if (slots[selected].destroy)
		{
			destroy = true;
			slots[selected] = new ItemEmpty();
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

	public Item getSelectedItem(boolean rightClick)
	{
		if (selected == -1)
			return null;

		if (slots[selected] instanceof ItemEmpty)
			return null;

		if (slots[selected].rightClick != rightClick)
			return null;

		return slots[selected];
	}

	public void update()
	{
		this.age += Panel.frameFrequency;
		checkKey(Game.game.input.hotbarDeselect, -1);
		checkKey(Game.game.input.hotbar1, 0);
		checkKey(Game.game.input.hotbar2, 1);
		checkKey(Game.game.input.hotbar3, 2);
		checkKey(Game.game.input.hotbar4, 3);
		checkKey(Game.game.input.hotbar5, 4);

		if (this.player.hotbar.persistent || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).shopScreen))
		{
			for (int i = 0; i < this.slotButtons.length; i++)
			{
				this.slotButtons[i].enabled = Game.game.window.touchscreen;

				Button b = this.slotButtons[i];
				b.posX = ((i - 2) * gap) + (Drawing.drawing.interfaceSizeX / 2);
				b.posY = Drawing.drawing.getInterfaceEdgeY(true) - bar_margin - this.player.hotbar.verticalOffset;
				b.update();
			}
		}

		if (Game.game.window.validScrollUp)
		{
			this.setItem(((this.selected - 1) + this.slots.length) % this.slots.length);
			Game.game.window.validScrollUp = false;
		}

		if (Game.game.window.validScrollDown)
		{
			this.setItem(((this.selected + 1) + this.slots.length) % this.slots.length);
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

	public void setItem(int index)
	{
		if (this.player.hotbar != null)
		{
			this.player.hotbar.hidden = false;

			if (!Game.game.window.touchscreen)
				this.player.hotbar.hideTimer = 500;
		}

		this.selected = (this.selected == index ? -1 : index);
		this.selectedTimer = 300;
		this.lastItemSwitch = this.age;

		if (ScreenPartyLobby.isClient)
			Game.eventsOut.add(new EventSetItemBarSlot(this.selected));
	}

	public void draw()
	{
		int y = (int) (Drawing.drawing.getInterfaceEdgeY(true) - bar_margin + this.player.hotbar.percentHidden - this.player.hotbar.verticalOffset);

		double slotBgBrightness = 0;

		if (Level.isDark())
			slotBgBrightness = 255;

		for (int i = -2; i <= 2; i++)
		{
			Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);

			int x = (int) ((i * gap) + (Drawing.drawing.interfaceSizeX / 2));

			Drawing.drawing.fillInterfaceRect(x, y, size, size);

			if (i + 2 == selected)
			{
				Drawing.drawing.setColor(slotSelectedR, slotSelectedG, slotSelectedB, (100 - this.player.hotbar.percentHidden) * 2.55);
				Drawing.drawing.fillInterfaceRect(x, y, size, size);
				Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, slotBgA);
			}

			Drawing.drawing.setColor(255, 255, 255, (100 - this.player.hotbar.percentHidden) * 2.55);
			if (slots[i + 2].icon != null)
				Drawing.drawing.drawInterfaceImage(slots[i + 2].icon, x, y, size, size);

			if (slots[i + 2] != null)
			{
				Item item = slots[i + 2];
				if (item.stackSize > 1)
				{
					Drawing.drawing.setColor(itemCountR, itemCountG, itemCountB, (100 - this.player.hotbar.percentHidden) * 2.55);

					int extra = 0;

					if (item.stackSize > 9999)
					{
						Drawing.drawing.setInterfaceFontSize(12);
						extra = 3;
					}
					else
						Drawing.drawing.setInterfaceFontSize(18);

					Drawing.drawing.drawInterfaceText(x + size - count_margin_right, y + extra + size - count_margin_bottom, Integer.toString(item.stackSize), true);
					Drawing.drawing.setColor(slotBgBrightness, slotBgBrightness, slotBgBrightness, slotBgA * (100 - this.player.hotbar.percentHidden) / 100.0);
				}
			}
		}

		if (selected >= 0 && slots[selected] != null)
		{
			if (Level.isDark())
				Drawing.drawing.setColor(255, 255, 255, Math.min(this.selectedTimer * 2.55 * 2, 255) * (100 - this.player.hotbar.percentHidden) * 0.01);
			else
				Drawing.drawing.setColor(0, 0, 0, Math.min(this.selectedTimer * 2.55 * 2, 255) * (100 - this.player.hotbar.percentHidden) * 0.01);

			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(Drawing.drawing.interfaceSizeX / 2, y - 70, this.slots[selected].name);
		}

		if ((this.player.hotbar.persistent || (Game.screen instanceof ScreenGame && ((ScreenGame) Game.screen).shopScreen)) && this.player.hotbar.percentHidden <= 0)
		{
			for (int i = 0; i < this.slotButtons.length; i++)
			{
				if (this.slotButtons[i].selected && !Game.game.window.touchscreen)
				{
					InputBindingGroup g = null;

					if (i == 0)
						g = Game.game.input.hotbar1;
					else if (i == 1)
						g = Game.game.input.hotbar2;
					else if (i == 2)
						g = Game.game.input.hotbar3;
					else if (i == 3)
						g = Game.game.input.hotbar4;
					else if (i == 4)
						g = Game.game.input.hotbar5;

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
			Item i = defaultItemEmpty;
			if (selected >= 0)
				i = this.slots[this.selected];

			if (Game.playerTank != null && !Game.playerTank.destroy)
			{
				Drawing.drawing.setColor(255, 255, 255, Math.min(1, 2 - (this.age - this.lastItemSwitch) / 100.0) * 255);

				String icon = i.icon;

				if (i.icon == null)
				{
					Drawing.drawing.setColor(255, 255, 255, Math.min(1, 2 - (this.age - this.lastItemSwitch) / 100.0) * 127);
					icon = "noitem.png";
				}

				if (Game.enable3d)
					Drawing.drawing.drawImage(icon, Game.playerTank.posX, Game.playerTank.posY, Game.playerTank.size, Game.tile_size, Game.tile_size);
				else
					Drawing.drawing.drawImage(icon, Game.playerTank.posX, Game.playerTank.posY, Game.tile_size, Game.tile_size);
			}
		}
	}
}

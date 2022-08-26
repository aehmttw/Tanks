package tanks.modapi;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.event.EventSortNPCShopButtons;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyHost;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemRemote;
import tanks.modapi.events.*;
import tanks.modapi.menus.NPCMesssage;
import tanks.tank.NameTag;
import tanks.tank.TankDummy;
import tanks.tank.Turret;

import java.util.ArrayList;
import java.util.Arrays;

import static tanks.gui.screen.ScreenGame.shopOffset;

public class TankNPC extends TankDummy
{
    /**
     * Not recommended; it may bring back old network speeds if any event-sending functions are repeatedly called.
     */
    public static boolean disableEventCooldown = false;
    public static final InputBindingGroup select = new InputBindingGroup("viewNPC", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_E));

    public String[] messages = null;
    public String tagName;
    public ButtonList npcShopList;
    public ArrayList<Item> shopItems;

    public boolean overrideDisplayState = false;
    public boolean isChatting = false;
    public boolean closeEnough = false;

    public String currentLine = "";
    public boolean draw = false;
    public int messageNum = 0;
    public double counter = 0;

    public final TankDummy icon = new TankDummy("icon", 200, 60, ModAPI.right);
    public NPCMesssage messager;

    public double eventCooldown = 0;

    public TankNPC(String name, double x, double y, double angle, String messages, double r, double g, double b)
    {
        this(name, x, y, angle, messages, "", r, g, b);
    }

    public TankNPC(String name, double x, double y, double angle, String messages, String tagName, double r, double g, double b)
    {
        this(name, x, y, angle, messages, tagName, r, g, b, r, g, b, Game.currentLevel.shop);
    }

    public TankNPC(String name, double x, double y, double angle, String messages, String tagName, double r, double g, double b, Item... shop)
    {
        this(name, x, y, angle, messages, tagName, r, g, b, r, g, b, new ArrayList<>(Arrays.asList(shop)));
    }

    public TankNPC(String name, double x, double y, double angle, String messages, String tagName, double r, double g, double b, double nameR, double nameG, double nameB, Item... shop)
    {
        this(name, x, y, angle, messages, tagName, r, g, b, nameR, nameG, nameB, new ArrayList<>(Arrays.asList(shop)));
    }

    public TankNPC(String name, double x, double y, double angle, String messages, String tagName, double r, double g, double b, ArrayList<Item> shop)
    {
        this(name, x, y, angle, messages, tagName, r, g, b, r, g, b, shop);
    }

    public TankNPC(String name, double x, double y, double angle, String messages, String tagName, double r, double g, double b, double nameR, double nameG, double nameB, ArrayList<Item> shop)
    {
        super(name, x, y * 50 + 25, angle);

        if (messages != null && messages.length() > 0)
            this.messages = messages.split("\n");

        this.shopItems = shop;
        this.tagName = tagName;
        this.showName = tagName != null && tagName.length() > 0;

        if (this.showName)
            this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, tagName, nameR, nameG, nameB);

        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
        this.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
        this.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
        this.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);

        this.invulnerable = true;
        this.mandatoryKill = false;

        icon.colorR = r;
        icon.colorG = g;
        icon.colorB = b;
        icon.secondaryColorR = Turret.calculateSecondaryColor(this.colorR);
        icon.secondaryColorG = Turret.calculateSecondaryColor(this.colorG);
        icon.secondaryColorB = Turret.calculateSecondaryColor(this.colorB);

        this.messager = new NPCMesssage(this);
        ModAPI.menuGroup.add(this.messager);
    }

    public void initShop(ArrayList<Item> shop)
    {
        Game.eventsOut.add(new EventClearNPCShop(this.networkID));
        ArrayList<Button> shopItemButtons = new ArrayList<>();

        for (int i = 0; i < shop.size(); i++)
        {
            final int j = i;
            Item item = shop.get(j);
            if (item instanceof ItemRemote)
                continue;

            String price = item.price + " ";
            if (item.price == 0)
                price = "Free!";
            else if (item.price == 1)
                price += "coin";
            else
                price += "coins";

            Button b = new Button(0, 0, Drawing.drawing.objWidth, 40, item.name, () -> {
                if (!ScreenPartyLobby.isClient)
                {
                    int pr = shop.get(j).price;
                    if (Game.player.hotbar.coins >= pr)
                    {
                        if (Game.player.hotbar.itemBar.addItem(shop.get(j)))
                            Game.player.hotbar.coins -= pr;
                    }
                }
                else
                    Game.eventsOut.add(new EventPurchaseNPCItem(j, this.networkID));

                Game.game.window.pressedButtons.remove(InputCodes.MOUSE_BUTTON_1);
            });

            b.image = item.icon;
            b.imageXOffset = -145;
            b.imageSizeX = 30;
            b.imageSizeY = 30;
            b.subtext = price;

            shopItemButtons.add(b);

            Game.eventsOut.add(new EventAddNPCShopItem(i, item.name, price, item.price, item.icon, this.networkID));
        }

        this.npcShopList = new ButtonList(shopItemButtons, 0, 0, (int) shopOffset, -30);
        Game.eventsOut.add(new EventSortNPCShopButtons(this.networkID));
    }

    @Override
    public void draw()
    {
        super.draw();

        if (this.messages != null && !overrideDisplayState && closeEnough)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawImage("talk.png", this.posX, this.posY - 50, 64, 64);
        }
    }

    @Override
    public void update()
    {
        super.update();

        this.eventCooldown -= Panel.frameFrequency;
        closeEnough = ModAPI.withinRange((this.posX - 25) / 50, (this.posY - 25) / 50, 3).contains(Game.playerTank);

        if (((ScreenGame) Game.screen).npcShopScreen && !closeEnough)
            ((ScreenGame) Game.screen).npcShopScreen = false;

        if (!overrideDisplayState)
        {
            if (closeEnough && select.isValid())
            {
                select.invalidate();
                if (!isChatting)
                    isChatting = true;

                else if (this.currentLine.length() < messages[messageNum].length())
                    this.currentLine = messages[messageNum];

                else if (messageNum + 1 == messages.length)
                {
                    isChatting = false;
                    messageNum = 0;
                }
                else
                {
                    messageNum++;
                    initMessageScreen();
                }
            }

            if (isChatting && closeEnough)
                draw = true;
            else
            {
                isChatting = false;
                draw = false;
                messageNum = 0;
            }
        }

        if (this.draw && (!((ScreenGame) Game.screen).paused || ScreenPartyHost.isServer || ScreenPartyLobby.isClient) && this.messages != null && this.messages[messageNum] != null)
        {
            if (this.counter <= 0)
            {
                if (this.currentLine.length() < this.messages[this.messageNum].length())
                    this.currentLine += this.messages[this.messageNum].charAt(this.currentLine.length());

                this.counter = 3;
            }
            this.counter -= Panel.frameFrequency * 4;
        }
    }

    public void setMessages(String message)
    {
        this.messages = message.split("\n");
        this.initMessageScreen();

        Game.eventsOut.add(new EventChangeNPCMessage(this));
    }

    public void setOverrideState(boolean displayState)
    {
        setOverrideState(displayState, true);
    }

    public void setOverrideState(boolean displayState, boolean override)
    {
        if (disableEventCooldown || this.eventCooldown <= 0)
        {
            this.eventCooldown = 3;

            this.overrideDisplayState = override;
            this.draw = displayState;

            if (this.draw)
                initMessageScreen();

            Game.eventsOut.add(new EventOverrideNPCState(this));
        }
    }

    public void initMessageScreen()
    {
        if (this.messages == null || this.messages[messageNum] == null)
            return;

        if (this.messages[this.messageNum].equals("/shop"))
        {
            this.initShop(this.shopItems);

            ((ScreenGame) Game.screen).npcShopScreen = true;
            ((ScreenGame) Game.screen).npcShopList = this.npcShopList;

            if (Game.followingCam)
                Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

            return;
        }

        this.currentLine = String.valueOf(this.messages[messageNum].charAt(0));
    }
}

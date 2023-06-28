package tanks.tank;

import basewindow.InputCodes;
import tanks.Drawing;
import tanks.Game;
import tanks.ModAPI;
import tanks.Panel;
import tanks.network.event.EventAddNPCShopItem;
import tanks.network.event.EventClearNPCShop;
import tanks.network.event.EventPurchaseNPCItem;
import tanks.network.event.EventSortNPCShopButtons;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenPartyLobby;
import tanks.hotbar.item.Item;
import tanks.hotbar.item.ItemRemote;

import java.util.ArrayList;
import java.util.Arrays;

import static tanks.gui.screen.ScreenGame.shopOffset;

/**
 * @see TankDummy
 */
public class TankNPC extends TankDummy
{
    public static final InputBindingGroup select = new InputBindingGroup("viewNPC", new InputBinding(InputBinding.InputType.keyboard, InputCodes.KEY_E));

    public String[] messages;
    public String tagName;
    public boolean draw = false;
    public ButtonList npcShopList;
    public ArrayList<Item> shopItems;
    private double counter = 0;
    private String currentLine = "";
    private boolean isChatting = false;
    private boolean closeEnough = false;
    private int messageNum = 0;
    private boolean setScreen = false;
    private final TankDummy icon = new TankDummy("icon", 200, 60, 0);

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

        this.targetable = false;

        this.messages = messages.split("\n");
        this.shopItems = shop;
        this.tagName = tagName;
        this.showName = tagName.length() > 0;
        this.nameTag = new NameTag(this, 0, this.size / 7 * 5, this.size / 2, tagName);

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

            Button b = new Button(0, 0, Drawing.drawing.objWidth, 40, item.name, () ->
            {
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

        if (closeEnough)
        {
            Drawing.drawing.setColor(255, 255, 255);
            Drawing.drawing.drawImage("chat.png", this.posX, this.posY - 50, 64, 64);
        }
    }

    @Override
    public void update()
    {
        super.update();

        closeEnough = ModAPI.withinRange((this.posX - 25) / 50, (this.posY - 25) / 50, 3).contains(Game.playerTank);

        if (((ScreenGame) Game.screen).npcShopScreen && !closeEnough)
            ((ScreenGame) Game.screen).npcShopScreen = false;

        if (closeEnough && select.isValid())
        {
            select.invalidate();
            setScreen = false;
            if (!isChatting)
                isChatting = true;

            else if (this.currentLine.length() < messages[messageNum].length())
            {
                this.currentLine = messages[messageNum];
                setScreen = true;
            }
            else if (messageNum + 1 == messages.length)
            {
                isChatting = false;
                messageNum = 0;
            }
            else
                messageNum++;
        }

        if (isChatting && closeEnough)
            draw = true;
        else
        {
            isChatting = false;
            draw = false;
        }
    }

    public void drawMessage()
    {
        if (!setScreen)
        {
            if (messages[messageNum].equals("/shop"))
            {
                this.initShop(this.shopItems);

                ((ScreenGame) Game.screen).npcShopScreen = true;
                ((ScreenGame) Game.screen).npcShopList = npcShopList;

                if (Game.followingCam)
                    Game.game.window.setCursorPos(Panel.windowWidth / 2, Panel.windowHeight / 2);

                return;
            }
            setScreen = true;
            this.currentLine = String.valueOf(messages[messageNum].charAt(0));
        }

        if (messages[messageNum].equals("/shop"))
            return;

        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 8, Drawing.drawing.interfaceSizeX * 0.95, 200);

        //Drawing.drawing.setColor(185, 104, 50);
        //ModAPI.fixedShapes.fillRect(20, 50, 100, 100);

        //Drawing.drawing.setColor(135, 54, 0);
        Drawing.drawing.setColor(0, 0, 0, 128);
        Drawing.drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX * 0.025 + 100, Drawing.drawing.interfaceSizeY / 8, 150, 150);

        icon.drawForInterface(Drawing.drawing.interfaceSizeX * 0.025 + 80, Drawing.drawing.interfaceSizeY / 8, 1.75);

        Drawing.drawing.setFontSize(24);
        Drawing.drawing.setColor(255, 255, 255);

        if (counter <= 0)
        {
            if (this.currentLine.length() < messages[messageNum].length())
                this.currentLine += messages[messageNum].charAt(this.currentLine.length());

            counter = 3;
        }
        counter -= Panel.frameFrequency * 4;

        ArrayList<String> lines = Drawing.drawing.wrapText(this.currentLine, Drawing.drawing.interfaceSizeX - 75, 24);
        for (int i = 0; i < lines.size(); i++)
            Drawing.drawing.drawUncenteredInterfaceText(250, 40 * i + 50, lines.get(i));
    }
}

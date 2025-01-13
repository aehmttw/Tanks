package tanks.gui.screen;

import com.codedisaster.steamworks.SteamUGCDetails;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.*;
import tanks.translation.Translation;

import java.util.ArrayList;
import java.util.Objects;

public class ScreenWorkshopCreations extends Screen
{
	public String title = "Online levels";

	public ButtonList creations;

	public static int page = 0;
	public int lastLoadedLevels = 0;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () ->
	{
		Game.screen = new ScreenSteamWorkshop();
	}
	);

	SearchBox search = new SearchBox(this.centerX, this.centerY - this.objYSpace * 4, this.objWidth * 1.25, this.objHeight, "Search", new Runnable()
	{
		@Override
		public void run()
		{
			String s = null;
			if (!search.inputText.isEmpty())
				s = search.inputText;

			if (!Objects.equals(Game.steamNetworkHandler.workshop.searchText, s))
			{
				Game.screen = new ScreenWorkshopSearchWaiting();
				Game.steamNetworkHandler.workshop.search(Game.steamNetworkHandler.workshop.searchType, 0, 18, Game.steamNetworkHandler.workshop.searchUser, search.inputText, Game.steamNetworkHandler.workshop.searchByScore);
			}
		}
	}, "");

	Button sort = new Button(this.centerX - this.objXSpace / 2 * 1.35, this.centerY - this.objYSpace * 4, this.objHeight, this.objHeight, "", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenWorkshopSearchWaiting();
			Game.steamNetworkHandler.workshop.search("Level", 0, 18, Game.steamNetworkHandler.workshop.searchUser, Game.steamNetworkHandler.workshop.searchText, !Game.steamNetworkHandler.workshop.searchByScore);

			if (!Game.steamNetworkHandler.workshop.searchByScore)
				sort.setHoverText("Sorting by upload date");
			else
				sort.setHoverText("Sorting by vote score");
		}
	}, "Sorting by upload date");

	public ScreenWorkshopCreations()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		if (Game.steamNetworkHandler.workshop.searchUser != null)
		{
			String n = Game.steamNetworkHandler.friends.knownUsernamesByID.get(Game.steamNetworkHandler.workshop.searchUser.getAccountID());
			if (n != null)
				title = Translation.translate("%s's creations", n);
		}
		this.initializeLevels();

		if (Game.steamNetworkHandler.workshop.searchText != null)
			search.inputText = Game.steamNetworkHandler.workshop.searchText;
	}

	public void initializeLevels()
	{
		ArrayList<Button> buttons = new ArrayList<>();

		for (int i = 0; i < Game.steamNetworkHandler.workshop.totalResults; i++)
		{
			buttons.add(new Button(0, 0, this.objWidth, this.objHeight, "Loading..."));
		}

		this.creations = new ButtonList(buttons, ScreenWorkshopCreations.page, (int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2));
	}

	@Override
	public void update()
	{
		if (this.creations.buttons.size() < Game.steamNetworkHandler.workshop.publishedFiles.keySet().size())
			this.initializeLevels();

		this.creations.update();
		this.quit.update();

		if (ScreenWorkshopCreations.page != this.creations.page)
		{
			ScreenWorkshopCreations.page = this.creations.page;
			Game.steamNetworkHandler.workshop.search("Level", page * this.creations.columns * this.creations.rows, (page + 1) * this.creations.columns * this.creations.rows - 1, Game.steamNetworkHandler.workshop.searchUser, Game.steamNetworkHandler.workshop.searchText, Game.steamNetworkHandler.workshop.searchByScore);
		}

		if (this.lastLoadedLevels != Game.steamNetworkHandler.workshop.publishedFiles.size())
		{
			for (int i: Game.steamNetworkHandler.workshop.publishedFiles.keySet())
			{
				Button b = this.creations.buttons.get(i);
				if (!b.enabled)
				{
					b.enabled = true;
					SteamUGCDetails d = Game.steamNetworkHandler.workshop.publishedFiles.get(i);
					b.text = d.getTitle();
					b.setSubtext("\u00A7000200000255+%d \u00A7200000000255-%d", d.getVotesUp(), d.getVotesDown());
					b.function = () ->
					{
						Game.screen = new ScreenWaitingCancelable("Downloading level...");
						Game.steamNetworkHandler.workshop.download(d);
					};
				}
			}

			this.lastLoadedLevels = Game.steamNetworkHandler.workshop.publishedFiles.size();
		}

		this.sort.imageSizeX = 25;
		this.sort.imageSizeY = 25;
		this.sort.fullInfo = true;

		if (Game.steamNetworkHandler.workshop.searchUser == null)
		{
			search.update();
			sort.update();
		}
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.creations.draw();
		this.quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.textSize / 2);
		Drawing.drawing.setColor(0, 0, 0);
		for (int i = Math.min(creations.page * creations.rows * creations.columns + creations.rows * creations.columns, creations.buttons.size()) - 1; i >= creations.page * creations.rows * creations.columns; i--)
		{
			Button b = creations.buttons.get(i);
			SteamUGCDetails d = Game.steamNetworkHandler.workshop.publishedFiles.get(i);

			if (Game.steamNetworkHandler.workshop.searchUser == null)
			{
				if (d != null && Game.steamNetworkHandler.friends.knownUsernamesByID.containsKey(d.getOwnerID().getAccountID()))
					Drawing.drawing.drawInterfaceText(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + b.sizeY * 0.325, Game.steamNetworkHandler.friends.knownUsernamesByID.get(d.getOwnerID().getAccountID()), false);
			}
			else
			{
				if (d.getTags().toLowerCase().contains("level"))
					Drawing.drawing.drawInterfaceText(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + b.sizeY * 0.325, "Level", false);
				else if (d.getTags().toLowerCase().contains("crusade"))
					Drawing.drawing.drawInterfaceText(b.posX - b.sizeX / 2 + b.sizeY / 2, b.posY + b.sizeY * 0.325, "Crusade", false);
			}
		}

		if (creations.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
		}

		if (Game.steamNetworkHandler.workshop.searchUser == null)
		{
			search.draw();
			sort.draw();
		}

		if (!Game.steamNetworkHandler.workshop.searchByScore)
		{
			sort.setHoverText("Sorting by upload date");
			sort.image = "icons/sort_chronological.png";
		}
		else
		{
			sort.setHoverText("Sorting by vote score");
			sort.image = "icons/sort_alphabetical.png";
		}

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.displayInterfaceText(this.centerX, this.centerY - this.objYSpace * 5, this.title);
	}

	@Override
	public void setupLayoutParameters()
	{
		if (Drawing.drawing.interfaceScaleZoom > 1 && ScreenPartyHost.isServer)
			this.centerY -= this.objYSpace / 2;
	}
}

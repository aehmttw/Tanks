package tanks.gui.screen;

import com.codedisaster.steamworks.SteamUGCDetails;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.ButtonList;
import tanks.gui.SavedFilesList;
import tanks.gui.SearchBox;

import java.util.ArrayList;

public class ScreenWorkshopLevels extends Screen
{
	public String title = "Online levels";

	public ButtonList levels;

	public static int page = 0;
	public int lastLoadedLevels = 0;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", () ->
            Game.screen = new ScreenSteamWorkshop()
	);

	public ScreenWorkshopLevels()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		this.initializeLevels();
	}

	public void initializeLevels()
	{
		ArrayList<Button> buttons = new ArrayList<>();

		for (int i = 0; i < Game.steamNetworkHandler.workshop.totalResults; i++)
		{
			buttons.add(new Button(0, 0, this.objWidth, this.objHeight, "Loading..."));
		}

		this.levels = new ButtonList(buttons, ScreenWorkshopLevels.page, (int) (this.centerX - Drawing.drawing.interfaceSizeX / 2), (int) (-30 + this.centerY - Drawing.drawing.interfaceSizeY / 2));
	}

	@Override
	public void update()
	{
		if (this.levels.buttons.size() < Game.steamNetworkHandler.workshop.publishedFiles.keySet().size())
			this.initializeLevels();

		this.levels.update();
		this.quit.update();

		if (ScreenWorkshopLevels.page != this.levels.page)
		{
			ScreenWorkshopLevels.page = this.levels.page;
			Game.steamNetworkHandler.workshop.search("Level", page * this.levels.columns * this.levels.rows, (page + 1) * this.levels.columns * this.levels.rows - 1);
		}

		if (this.lastLoadedLevels != Game.steamNetworkHandler.workshop.publishedFiles.size())
		{
			for (int i: Game.steamNetworkHandler.workshop.publishedFiles.keySet())
			{
				Button b = this.levels.buttons.get(i);
				if (!b.enabled)
				{
					b.enabled = true;
					SteamUGCDetails d = Game.steamNetworkHandler.workshop.publishedFiles.get(i);
					b.text = d.getTitle();
					b.setSubtext("\u00A7000200000255+%d \u00A7200000000255-%d", d.getVotesUp(), d.getVotesDown());
					b.function = () ->
					{
						Game.screen = new ScreenWaiting("Downloading level...");
						Game.steamNetworkHandler.workshop.download(d);
					};
				}
			}

			this.lastLoadedLevels = Game.steamNetworkHandler.workshop.publishedFiles.size();
		}
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		this.levels.draw();
		this.quit.draw();

		if (levels.buttons.size() <= 0)
		{
			Drawing.drawing.setColor(0, 0, 0);
			Drawing.drawing.setInterfaceFontSize(24);
			Drawing.drawing.drawInterfaceText(this.centerX, this.centerY, "No levels found");
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

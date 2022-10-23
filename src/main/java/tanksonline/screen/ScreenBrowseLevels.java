package tanksonline.screen;

import tanks.Game;
import tanks.network.event.online.EventAddButton;
import tanks.network.event.online.EventRemoveButton;
import tanks.gui.Button;
import tanks.gui.screen.ScreenOnline;
import tanksonline.PlayerMap;
import tanksonline.TanksOnlineServerHandler;
import tanksonline.UploadedLevel;

import java.util.ArrayList;

public class ScreenBrowseLevels extends ScreenLayout
{
	public String title;

	int rows = 6;
	int yoffset = -150;
	static int page = 0;

	Button quit = new Button(sizeX / 2, sizeY / 2 + 300, 350, 40, "Back", () ->
	{
		ScreenHome s = new ScreenHome(player);
		s.setScreen();
	}
	);

	Button next = new Button(sizeX / 2 + 190, sizeY / 2 + 240, 350, 40, "Next page", () ->
	{
		page++;
		updateScreen();
	}
	);

	Button previous = new Button(sizeX / 2 - 190, sizeY / 2 + 240, 350, 40, "Previous page", () ->
	{
		page--;
		updateScreen();
	}
	);

	ArrayList<Button> levelButtons = new ArrayList<>();

	public ScreenBrowseLevels(TanksOnlineServerHandler p, String title, ArrayList<UploadedLevel> levelsArray)
	{
		super(p);

		this.title = title;

		this.music = "menu_3.ogg";
		this.musicID = "menu";

		this.texts.add(new ScreenOnline.Text(title, sizeX / 2, sizeY / 2 - 210, 24, 0));

		for (int i = 0; i < rows * 3 + 2; i++)
			this.buttons.add(null);

		quit.wait = true;

		this.buttons.add(quit);

		ArrayList<UploadedLevel> levels = new ArrayList<>(levelsArray);

		synchronized (PlayerMap.instance)
		{
			for (UploadedLevel l : levels)
			{
				Button b = new Button(0, 0, 350, 40, l.name.replace("_", " "), () ->
				{
					ScreenDownloadLevel s = new ScreenDownloadLevel(player, l);
					s.setScreen();

				}
						, "Uploaded by: " + PlayerMap.instance.getUsername(l.creator) + "---" + Game.timeInterval(l.time, System.currentTimeMillis()) + " ago");

				b.wait = true;
				levelButtons.add(b);
			}
		}

		for (int i = 0; i < levelButtons.size(); i++)
		{
			int page = i / (rows * 3);
			int offset = 0;

			if (page * rows * 3 + rows < levelButtons.size())
				offset = -190;

			if (page * rows * 3 + rows * 2 < levelButtons.size())
				offset = -380;

			levelButtons.get(i).posY = sizeY / 2 + yoffset + (i % rows) * 60;

			if (i / rows % 3 == 0)
				levelButtons.get(i).posX = sizeX / 2 + offset;
			else if (i / rows % 3 == 1)
				levelButtons.get(i).posX = sizeX / 2 + offset + 380;
			else
				levelButtons.get(i).posX = sizeX / 2 + offset + 380 * 2;
		}
	}

	public void updateScreen()
	{
		int in = 0;
		for (int i = page * rows * 3 + rows * 3 - 1; i >= page * rows * 3; i--)
		{
			if (i >= this.levelButtons.size())
				this.buttons.set(in, null);
			else
				this.buttons.set(in, this.levelButtons.get(i));

			in++;
		}

		if (page > 0)
			this.buttons.set(rows * 3, previous);
		else
			this.buttons.set(rows * 3, null);

		if (levelButtons.size() > (1 + page) * rows * 3)
			this.buttons.set(rows * 3 + 1, next);
		else
			this.buttons.set(rows * 3 + 1, null);

		for (int i = 0; i < 3 * rows + 2; i++)
		{
			if (this.buttons.get(i) == null)
				this.player.sendEvent(new EventRemoveButton(i));
			else
				this.player.sendEvent(new EventAddButton(i, this.buttons.get(i)));
		}
	}

	@Override
	public void setScreen()
	{
		super.setScreen();
		this.updateScreen();
	}
}

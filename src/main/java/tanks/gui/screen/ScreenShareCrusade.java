package tanks.gui.screen;

import tanks.Crusade;
import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;
import tanks.gui.SavedFilesList;

public class ScreenShareCrusade extends Screen
{
	public SavedFilesList crusades;

	public Button quit = new Button(this.centerX, this.centerY + this.objYSpace * 5, this.objWidth, this.objHeight, "Back", new Runnable()
	{
		@Override
		public void run()
		{
			Game.screen = new ScreenShareSelect();
		}
	}
			);

	public ScreenShareCrusade()
	{
		super(350, 40, 380, 60);

		this.music = "menu_4.ogg";
		this.musicID = "menu";

		crusades = new SavedFilesList(Game.homedir + Game.crusadeDir, ScreenCrusades.page, 0, -60,
				(name, file) ->
				{
					Game.screen = new ScreenCrusadePreview(new Crusade(file, name), Game.screen, true);

				}, (file) -> null);
	}

	@Override
	public void update()
	{
		crusades.update();
		quit.update();

		ScreenCrusades.page = crusades.page;
	}

	@Override
	public void draw()
	{
		this.drawDefaultBackground();

		crusades.draw();
		quit.draw();

		Drawing.drawing.setInterfaceFontSize(this.titleSize);
		Drawing.drawing.setColor(0, 0, 0);
		Drawing.drawing.drawInterfaceText(this.centerX, this.centerY - this.objYSpace * 4.5, "Share crusade");
	}

	@Override
	public void setupLayoutParameters()
	{
		this.centerY -= this.objYSpace / 2;
	}
}

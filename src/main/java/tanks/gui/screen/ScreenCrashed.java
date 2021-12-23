package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenCrashed extends Screen
{
	public String sadFace = ":(";

	public ScreenCrashed()
	{
		super(350, 40, 380, 60);

		if (Math.random() < 0.01)
			sadFace = ":)";
	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100, this.objWidth, this.objHeight, "Exit the game", () -> System.exit(0));

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 160, this.objWidth, this.objHeight, "Return to title", Game::exitToTitle);

	@Override
	public void update()
	{
		this.quit.update();
		this.exit.update();
	}

	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 0, 255);
		Game.game.window.shapeRenderer.fillRect(0, 0, Game.game.window.absoluteWidth, Game.game.window.absoluteHeight - Drawing.drawing.statsHeight);

		drawing.setColor(255, 255, 255);
		drawing.setInterfaceFontSize(100);

		if (Drawing.drawing.interfaceScaleZoom > 1)
			drawing.drawInterfaceText(50, 100, sadFace);
		else
			drawing.drawInterfaceText(100, 100, sadFace);

		drawing.setInterfaceFontSize(48);
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran into a problem!");

		drawing.setInterfaceFontSize(24);

		drawing.displayInterfaceText(50, 200, false, "You may return to the game if you wish,");
		drawing.displayInterfaceText(50, 230, false, "but be warned that things may become unstable.");
		drawing.displayInterfaceText(50, 260, false, "If you see this screen again, restart the game.");
		drawing.displayInterfaceText(50, 290, false, "Also, you may want to report this crash!");

		drawing.displayInterfaceText(50, 350,  false, "Crash details:");
		drawing.drawInterfaceText(50, 380, Game.crashMessage, false);
		drawing.drawInterfaceText(50, 410, Game.crashLine, false);

		drawing.displayInterfaceText(50, 470,  false, "Check the crash report file for more information: ");
		drawing.drawInterfaceText(50, 500, Game.homedir.replace("\\", "/") + Game.crashesPath + Game.crashTime + ".crash", false);

		this.quit.draw();
		this.exit.draw();
	}

}

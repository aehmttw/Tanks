package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;
import tanks.gui.Button;

public class ScreenOutOfMemory extends Screen
{
	public ScreenOutOfMemory()
	{

	}

	Button exit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 100, this.objWidth, this.objHeight, "Exit the game", () -> System.exit(0)
	);

	Button quit = new Button(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY - 160, this.objWidth, this.objHeight, "Return to title", Game::exitToTitle
	);

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
		drawing.setColor(0, 127, 0);
		drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX * 1.2, Drawing.drawing.interfaceSizeY * 1.2);				

		drawing.setColor(255, 255, 255);
		drawing.setInterfaceFontSize(100);
		drawing.drawInterfaceText(100, 100, ":(");

		drawing.setInterfaceFontSize(48);
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran out of memory!");

		drawing.setInterfaceFontSize(24);
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 200, "This could have happened due to not allocating enough memory to Tanks");
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 230, "or due to a memory leak bug in the program that needs to be fixed.");

		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 290, "You may return to the game if you wish,");
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 320, "but be warned that things may become unstable.");

		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 380, "You may want to restart the game and allocate more");
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 410, "memory, with the Java launch argument -Xmx[memory],");
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 440, "where -Xmx100M allocates 100 MB of memory (should be sufficient).");

		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 500, "If you see this screen again even after allocating more");
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 530, "memory, there is likely a bug, so please report it!");


		this.quit.draw();
		this.exit.draw();
	}

}

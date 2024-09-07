package tanks.gui.screen;

import tanks.Drawing;
import tanks.Game;

public class ScreenOutOfMemory extends ScreenCrashed
{
	public ScreenOutOfMemory()
	{

	}

	@Override
	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 127, 0);
		drawing.fillInterfaceRect(Drawing.drawing.interfaceSizeX / 2, Drawing.drawing.interfaceSizeY / 2, Drawing.drawing.interfaceSizeX * 1.2, Drawing.drawing.interfaceSizeY * 1.2);				

		drawing.setColor(255, 255, 255);
		drawing.setInterfaceFontSize(100);
		if (Drawing.drawing.interfaceScaleZoom > 1)
			drawing.drawInterfaceText(50, 100, sadFace);
		else
			drawing.drawInterfaceText(100, 100, sadFace);

		drawing.setInterfaceFontSize(48);
		drawing.displayInterfaceText(Drawing.drawing.interfaceSizeX / 2, 100, "Oh noes! Tanks ran out of memory!");

		drawing.setInterfaceFontSize(24);
		drawing.displayInterfaceText(50, 200, false, "This could have happened due to not allocating enough memory to Tanks,");
		drawing.displayInterfaceText(50, 230, false, "or due to a memory leak bug in the program that needs to be fixed.");
		drawing.displayInterfaceText(50, 260, false, "Very large levels may require more allocated memory to be playable.");

		drawing.displayInterfaceText(50, 320, false, "You may return to the game if you wish, but be warned that things may become unstable.");

		drawing.displayInterfaceText(50, 380, false, "You may want to restart the game and allocate more memory, with the Java launch argument");
		drawing.displayInterfaceText(50, 410, false, "-Xmx[memory], where -Xmx1G allocates 1 GB of memory (which should be sufficient).");

		drawing.displayInterfaceText(50, 470, false, "If you see this screen again even after allocating more");
		drawing.displayInterfaceText(50, 500, false, "memory, there is likely a bug, so please report it!");

		int extensions = Game.extensionRegistry.extensions.size();
		String extText = extensions == 0 ? "" : extensions == 1 ? " (with 1 extension)" : " (with " + extensions + " extensions)";

		drawing.setInterfaceFontSize(24);
		drawing.displayInterfaceText(50, 560,  false, "Game version: " + Game.version + extText);

		this.chatroom.draw();
		this.quit.draw();
		this.exit.draw();
	}

}

package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import org.lwjgl.glfw.GLFW;

public class ChatBox extends TextBox
{
	public int key;
	public String defaultText;
	public boolean persistent = true;

	public ChatBox(double x, double y, double sX, double sY, int key, String defaultText, Runnable f) 
	{
		super(x, y, sX, sY, "", f, "");
		this.enableCaps = true;
		this.enablePunctuation = true;
		this.maxChars = 84 - Game.username.length();

		this.colorR = 200;
		this.colorG = 200;
		this.colorB = 200;

		this.hoverColorR = 255;
		this.hoverColorG = 255;
		this.hoverColorB = 255;

		this.selectedColorR = 255;
		this.selectedColorG = 255;
		this.selectedColorB = 255;

		this.selectedFullColorR = 255;
		this.selectedFullColorG = 0;
		this.selectedFullColorB = 0;

		this.inputText = defaultText;
		this.defaultText = defaultText;

		this.key = key;

	}

	public void update(boolean persistent)
	{
		boolean prevPersistent = this.persistent;
		this.persistent = persistent;
		this.update();
		this.persistent = prevPersistent;	
	}

	public void update()
	{
		
		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		hover = this.persistent && mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && Game.game.window.validPressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1) && !selected)
		{
			this.inputText = "";
			selected = true;
			Game.game.window.validPressedButtons.remove((Integer)GLFW.GLFW_MOUSE_BUTTON_1);
		}

		if (!this.selected && Game.game.window.validPressedKeys.contains(key))
		{
			Game.game.window.validPressedKeys.remove((Integer)key);
			this.selected = true;
			this.inputText = "";
		}

		if (this.selected && Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_ESCAPE))
		{
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_ESCAPE);
			this.selected = false;
			this.inputText = this.defaultText;
		}

		if (this.selected && Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_ENTER))
		{
			if (this.inputText.length() > 0)
				this.function.run();

			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_ENTER);
			Game.game.window.pressedKeys.remove((Integer)GLFW.GLFW_KEY_ENTER);

			this.selected = false;
			this.inputText = this.defaultText;
		}

		if (this.selected)
			this.checkKeys();
	}
	
	public void draw(boolean persistent)
	{
		boolean prevPersistent = this.persistent;
		this.persistent = persistent;
		this.draw();
		this.persistent = prevPersistent;	
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;

		if (this.selected)
		{	
			if (this.inputText.length() >= this.maxChars)
				drawing.setColor(this.selectedFullColorR, this.selectedFullColorG, this.selectedFullColorB, 127);
			else
				drawing.setColor(this.selectedColorR, this.selectedColorG, this.selectedColorB, 127);

			drawing.fillInterfaceRect(this.posX, this.posY, this.sizeX, this.sizeY);

			drawing.setColor(0, 0, 0);
			drawing.setInterfaceFontSize(24);
			drawing.drawInterfaceText(this.posX - this.sizeX / 2 + 10, this.posY, 
					"\u00a7127127127255" + Game.username + ": \u00a7000000000255" + this.inputText + "\u00a7127127127255_", false);
		}
		else if (this.persistent)
		{
			if (this.hover)
				drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB, 127);
			else
				drawing.setColor(this.colorR, this.colorG, this.colorB, 127);

			drawing.fillInterfaceRect(this.posX, this.posY, this.sizeX, this.sizeY);
			
			drawing.setColor(0, 0, 0);
			drawing.setInterfaceFontSize(24);
			drawing.drawInterfaceText(this.posX - this.sizeX / 2 + 10, this.posY, this.inputText, false);
		}
	}
}

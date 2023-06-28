package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.Panel;
import tanks.gui.input.InputBinding;
import tanks.gui.input.InputBindingGroup;
import tanks.gui.screen.Screen;
import tanks.gui.screen.ScreenInfo;

public class ChatBox extends TextBox
{
	public InputBindingGroup input;
	public String defaultText;
	public String defaultTextColor = "\u00A7127127127255";

	public boolean persistent = true;

	public ChatBox(double x, double y, double sX, double sY, InputBindingGroup input, Runnable f)
	{
		super(x, y, sX, sY, "", f, "");
		this.enableCaps = true;
		this.enablePunctuation = true;
		this.maxChars = 1000;

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

		String defaultText = "Click here or press " + Game.game.input.chat.getInputs() + " to send a chat message";
		if (Game.game.window.touchscreen)
			defaultText = "Click here to send a chat message";

		this.inputText = defaultText;
		this.defaultText = defaultText;

		this.input = input;
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
		if (Game.screen instanceof ScreenInfo)
			return;

		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
		}
		else
		{
			for (int i: Game.game.window.touchPoints.keySet())
			{
				InputPoint p = Game.game.window.touchPoints.get(i);

				double mx = Drawing.drawing.getInterfacePointerX(p.x);
				double my = Drawing.drawing.getInterfacePointerY(p.y);

				if (p.tag.equals(""))
				{
					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "textbox";
				}
			}
		}

		if (!this.selected && this.input.isValid() && Panel.selectedTextBox == null)
		{
			this.input.invalidate();
			Game.game.window.getRawTextKeys().clear();

			if (Panel.selectedTextBox != null)
				Panel.selectedTextBox.submit();

			Panel.selectedTextBox = this;

			this.selected = true;
			this.inputText = "";

			Drawing.drawing.playVibration("click");
			Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
		}

		if (this.selected && Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
		{
			if (this.inputText.trim().length() > 0)
				this.function.run();

			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ENTER);
			Game.game.window.pressedKeys.remove((Integer) InputCodes.KEY_ENTER);
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);

			this.selected = false;
			Panel.selectedTextBox = null;
			this.inputText = this.defaultText;
			Game.game.window.showKeyboard = false;

			Drawing.drawing.playVibration("click");
			Drawing.drawing.playSound("destroy.ogg", 2f);
		}

		if (this.selected && Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE))
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
			this.selected = false;
			Panel.selectedTextBox = null;
			this.inputText = this.defaultText;
			Game.game.window.showKeyboard = false;

			Drawing.drawing.playVibration("click");
			Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		}

		if (this.selected)
		{
			double frac = Math.max(0, Math.round(Drawing.drawing.interfaceScale * (this.posY + 30) + Math.max(0, Panel.windowHeight - Drawing.drawing.statsHeight
					- Drawing.drawing.interfaceSizeY * Drawing.drawing.interfaceScale) / 2) - Game.game.window.absoluteHeight * Game.game.window.keyboardFraction)
					/ Game.game.window.absoluteHeight;
			Game.game.window.keyboardOffset = Math.min(frac, Game.game.window.keyboardOffset + 0.04 * Panel.frameFrequency * frac);
			Game.game.window.showKeyboard = true;
			this.checkKeys();
		}
	}

	public boolean checkMouse(double mx, double my, boolean valid)
	{
		boolean handled = false;
		hover = this.persistent && mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;
		clearSelected = selected && mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (Game.game.window.touchscreen)
			hover = hover || mx > posX - sizeX / 2 && mx < posX - sizeX / 2 + sizeY && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && valid && Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1) && !selected)
		{
			this.inputText = "";

			if (Panel.selectedTextBox != null)
				Panel.selectedTextBox.submit();

			Panel.selectedTextBox = this;

			selected = true;
			handled = true;
			Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);
			Game.game.window.getRawTextKeys().clear();
			Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
			Drawing.drawing.playVibration("click");
		}

		if (clearSelected && valid)
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
			this.selected = false;
			Panel.selectedTextBox = null;
			this.inputText = this.defaultText;
			handled = true;
			Game.game.window.showKeyboard = false;
			Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
			Drawing.drawing.playVibration("click");
		}

		return handled;
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

			this.drawBox();

			drawing.setColor(0, 0, 0);
			drawing.setInterfaceFontSize(this.sizeY * 0.6);

			String name = this.defaultTextColor + Game.player.username;
			String s = name + ": \u00a7000000000255" + this.inputText + "\u00a7127127127255_";

			double limit = Drawing.drawing.interfaceSizeX - 80;
			if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s) / Drawing.drawing.interfaceScale > limit)
			{
				for (int i = 0; i < s.length(); i++)
				{
					if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, s.substring(i)) / Drawing.drawing.interfaceScale < limit)
					{
						s = s.substring(i);

						if (i <= name.length())
							s = this.defaultTextColor + s;
						break;
					}
				}

				drawing.drawInterfaceText(this.posX + this.sizeX / 2 - 50, this.posY, s, true);
			}
			else
				drawing.drawInterfaceText(this.posX - this.sizeX / 2 + 10, this.posY, s, false);


			if (!clearSelected || Game.game.window.touchscreen)
				drawing.setColor(160, 160, 160);
			else
				drawing.setColor(255, 0, 0);

			drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
			drawing.setColor(255, 255, 255);
			drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY - 2.5, "x");
		}
		else if (this.persistent)
		{
			if (this.hover && !Game.game.window.touchscreen)
				drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB, 127);
			else
				drawing.setColor(this.colorR, this.colorG, this.colorB, 127);

			this.drawBox();

			drawing.setColor(0, 0, 0);
			drawing.setInterfaceFontSize(this.sizeY * 0.6);

			if (this.inputText.equals(this.defaultText))
				drawing.drawInterfaceText(this.posX - this.sizeX / 2 + 10, this.posY, this.defaultTextColor + this.defaultText, false);
			else
				drawing.drawInterfaceText(this.posX - this.sizeX / 2 + 10, this.posY, this.inputText, false);
		}
		else if (Game.game.window.touchscreen)
		{
			drawing.setColor(this.colorR, this.colorG, this.colorB, 127);
			drawing.fillInterfaceOval(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, this.sizeY, this.sizeY);
			drawing.drawInterfaceImage("chat.png", this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, 0.8 * this.sizeY, 0.8 * this.sizeY);
		}
	}

	public void drawBox()
	{
		double xPad = -40;
		Drawing.drawing.fillInterfaceRect(this.posX, this.posY, this.sizeX + xPad, this.sizeY);

		Game.game.window.shapeRenderer.setBatchMode(true, false, false);

		for (int j = 0; j < 30; j++)
		{
			Drawing.drawing.addInterfaceVertex(this.posX - this.sizeX / 2 - xPad / 2, this.posY, 0);
			Drawing.drawing.addInterfaceVertex(this.posX - this.sizeX / 2 - xPad / 2 + Math.cos((j + 15) / 30.0 * Math.PI) * (this.sizeY) / 2,
					this.posY + Math.sin((j + 15) / 30.0 * Math.PI) * (this.sizeY) / 2, 0);
			Drawing.drawing.addInterfaceVertex(this.posX - this.sizeX / 2 - xPad / 2 + Math.cos((j + 16) / 30.0 * Math.PI) * (this.sizeY) / 2,
					this.posY + Math.sin((j + 16) / 30.0 * Math.PI) * (this.sizeY) / 2, 0);
		}

		for (int j = 0; j < 30; j++)
		{
			Drawing.drawing.addInterfaceVertex(this.posX + this.sizeX / 2 + xPad / 2, this.posY, 0);
			Drawing.drawing.addInterfaceVertex(this.posX + this.sizeX / 2 + xPad / 2 + Math.cos((j + 45) / 30.0 * Math.PI) * (this.sizeY) / 2,
					this.posY + Math.sin((j + 15) / 30.0 * Math.PI) * (this.sizeY) / 2, 0);
			Drawing.drawing.addInterfaceVertex(this.posX + this.sizeX / 2 + xPad / 2 + Math.cos((j + 46) / 30.0 * Math.PI) * (this.sizeY) / 2,
					this.posY + Math.sin((j + 16) / 30.0 * Math.PI) * (this.sizeY) / 2, 0);
		}

		Game.game.window.shapeRenderer.setBatchMode(false, false, false);
	}
}

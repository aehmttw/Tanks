package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.Panel;
import tanks.gui.screen.ScreenInfo;

import java.util.ArrayList;

public class TextBox implements IDrawable, ITrigger
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String labelText;

	public String previousInputText;
	public String inputText;

	public boolean enableHover = false;
	public String[] hoverText;
	public String hoverTextRaw = "";

	public boolean hover = false;

	public boolean selected = false;
	public boolean infoSelected = false;
	public boolean clearSelected = false;

	public boolean enableSpaces = true;
	public boolean allowSpaces = true;
	public boolean allowLetters = true;
	public boolean allowNumbers = true;
	public boolean allowColons = false;
	public boolean allowAll = false;
	public boolean allowDots = false;
	public boolean enablePunctuation = false;
	public boolean checkMaxValue = false;
	public boolean checkMinValue = false;
	public boolean allowNegatives = false;
	public boolean allowDoubles = false;

	public boolean lowerCase = false;
	public boolean enableCaps = false;

	public int maxChars = 18;
	public int maxValue = Integer.MAX_VALUE;
	public int minValue = Integer.MIN_VALUE;

	public double colorR = 255;
	public double colorG = 255;
	public double colorB = 255;
	public double bgColorR = 200;
	public double bgColorG = 200;
	public double bgColorB = 200;
	public double hoverColorR = 240;
	public double hoverColorG = 240;
	public double hoverColorB = 255;
	public double selectedColorR = 220;
	public double selectedColorG = 255;
	public double selectedColorB = 220;
	public double selectedFullColorR = 255;
	public double selectedFullColorG = 238;
	public double selectedFullColorB = 220;

	public boolean enabled = true;

	/** If set to true and is part of an online service, pressing the button sends the player to a loading screen*/
	public boolean wait = false;

	/** For online service use with changing interface scales
	 * -1 = left
	 * 0 = middle
	 * 1 = right*/
	public int xAlignment = 0;

	/** For online service use with changing interface scales
	 * -1 = top
	 * 0 = middle
	 * 1 = bottom*/
	public int yAlignment = 0;

	public TextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText)
	{
		this.posX = x;
		this.posY = y;
		this.function = f;

		this.sizeX = sX;
		this.sizeY = sY;
		this.labelText = text;

		this.inputText = defaultText;
		this.previousInputText = defaultText;
	}

	public TextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText, String hoverText)
	{
		this(x, y, sX, sY, text, f, defaultText);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
		this.hoverTextRaw = hoverText;
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;

		drawing.setInterfaceFontSize(24);

		drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - 30, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - 30, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - 30, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - 15, sizeX, 30);

		if (selected)
		{
			if (this.inputText.length() >= this.maxChars)
				drawing.setColor(this.selectedFullColorR, this.selectedFullColorG, this.selectedFullColorB);
			else
				drawing.setColor(this.selectedColorR, this.selectedColorG, this.selectedColorB);
		}
		else if (hover && !Game.game.window.touchscreen)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else
			drawing.setColor(this.colorR, this.colorG, this.colorB);

		double m = 0.8;
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY * m);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

		drawing.setColor(0, 0, 0);

		drawing.drawInterfaceText(posX, posY - 30, labelText);

		this.drawInput();

		if (enableHover)
		{
			if (infoSelected && !Game.game.window.touchscreen)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 1 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
			}
		}

		if (selected && inputText.length() > 0)
		{
			if (!clearSelected || Game.game.window.touchscreen)
				drawing.setColor(255, 0, 0);
			else
				drawing.setColor(255, 127, 127);

			drawing.fillInterfaceOval(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
			drawing.setColor(255, 255, 255);
			drawing.setInterfaceFontSize(24);
			drawing.drawInterfaceText(this.posX + 2 - this.sizeX / 2 + this.sizeY / 2 - 1, this.posY - 1, "x");
		}
	}

	public void drawInput()
	{
		if (selected)
			Drawing.drawing.drawInterfaceText(posX, posY, inputText + "\u00a7127127127255_");
		else
			Drawing.drawing.drawInterfaceText(posX, posY, inputText);
	}

	public void update()
	{
		if (!Game.game.window.touchscreen)
		{
			double mx = Drawing.drawing.getInterfaceMouseX();
			double my = Drawing.drawing.getInterfaceMouseY();

			boolean handled = checkMouse(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));

			if (handled)
				Game.game.window.validPressedButtons.remove((Integer) InputCodes.MOUSE_BUTTON_1);

			checkDeselect(mx, my, Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1));
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

				checkDeselect(mx, my, p.valid);
			}
		}

		if (selected)
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

		if (Game.game.window.touchscreen)
		{
			sizeX += 20;
			sizeY += 20;
		}

		hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - 30 && my < posY + sizeY / 2;

		infoSelected = mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;
		clearSelected = selected && mx < posX - sizeX / 2 + sizeY && mx > posX - sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && valid && enabled)
		{
			if (infoSelected && enableHover && Game.game.window.touchscreen)
			{
				handled = true;
				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
				Game.screen = new ScreenInfo(Game.screen, this.labelText, this.hoverText);
			}
			else if (!selected)
			{
				handled = true;
				selected = true;
				this.previousInputText = this.inputText;

				TextBox prev = Panel.selectedTextBox;

				Panel.selectedTextBox = this;

				if (prev != null)
					prev.submit();

				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
				Game.game.window.getRawTextKeys().clear();
			}
		}

		if (clearSelected && valid && inputText.length() > 0)
		{
			handled = true;
			this.clear();
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}

		return handled;
	}

	public void clear()
	{
		Drawing.drawing.playVibration("click");
		Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		this.inputText = "";
	}

	public void checkDeselect(double mx, double my, boolean valid)
	{
		if (Game.game.window.touchscreen)
		{
			sizeX += 20;
			sizeY += 20;
		}

		boolean hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - 30 && my < posY + sizeY / 2;

		if (((!hover && valid)) && selected)
		{
			this.submit();
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}
	}

	public void submit()
	{
		Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ENTER);
		Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);

		this.performValueCheck();
		function.run();
		this.previousInputText = this.inputText;
		Drawing.drawing.playSound("destroy.ogg", 2f);
		Drawing.drawing.playVibration("click");
		selected = false;
		Game.game.window.showKeyboard = false;
		Panel.selectedTextBox = null;
	}

	public void checkKeys()
	{
		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ENTER))
		{
			this.submit();
			return;
		}

		if (Game.game.window.validPressedKeys.contains(InputCodes.KEY_ESCAPE) && selected)
		{
			Game.game.window.validPressedKeys.remove((Integer) InputCodes.KEY_ESCAPE);
			selected = false;
			Panel.selectedTextBox = null;
			this.inputText = this.previousInputText;
			Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
			Game.game.window.showKeyboard = false;
		}

		Game.game.window.pressedKeys.clear();
		Game.game.window.validPressedKeys.clear();

		if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_SUPER) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_SUPER))
		{
			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_C))
			{
				Game.game.window.textPressedKeys.clear();
				Game.game.window.textValidPressedKeys.clear();
				Game.game.window.getRawTextKeys().clear();

				Game.game.window.setClipboard(this.inputText);
			}

			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_V))
			{
				Game.game.window.textPressedKeys.clear();
				Game.game.window.textValidPressedKeys.clear();
				Game.game.window.getRawTextKeys().clear();

				String s = Game.game.window.getClipboard();

				for (int i = 0; i < s.length(); i++)
				{
					this.inputKey(0, s.substring(i, i + 1).toLowerCase(), Character.isUpperCase(s.charAt(i)));
				}
			}

			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_BACKSPACE) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_DELETE))
			{
				Game.game.window.textPressedKeys.clear();
				Game.game.window.textValidPressedKeys.clear();
				Game.game.window.getRawTextKeys().clear();

				this.clear();
			}
		}

		boolean caps = (this.enableCaps && (Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_SHIFT) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_SHIFT)));

		ArrayList<Integer> texts = Game.game.window.getRawTextKeys();

		for (int key : texts)
		{
			String text = Game.game.window.getTextKeyText(key);

			if (text == null && key == InputCodes.KEY_SPACE)
				text = " ";

			inputKey(Game.game.window.translateTextKey(key), text, caps);
		}

		texts.clear();
	}

	public void performValueCheck()
	{
		try
		{
			if (checkMaxValue)
				if (Integer.parseInt(inputText) > this.maxValue)
					inputText = this.maxValue + "";

			if (checkMinValue)
				if (Integer.parseInt(inputText) < this.minValue)
					inputText = this.minValue + "";
		}
		catch (Exception ignored) {}
	}

	public void inputKey(int key, String text, boolean caps)
	{
		if (this.enableCaps && (key == InputCodes.KEY_LEFT_SHIFT || key == InputCodes.KEY_RIGHT_SHIFT))
			return;

		if (key == InputCodes.KEY_SPACE)
			text = " ";

		if (key == InputCodes.KEY_BACKSPACE || key == '\b')
			inputText = inputText.substring(0, Math.max(0, inputText.length() - 1));

		else if (text != null && inputText.length() + text.length() <= maxChars)
		{
			if (text.equals(" "))
			{
				if (allowSpaces)
				{
					if (enableSpaces)
						inputText += " ";
					else
						inputText += "_";
				}
			}
			else
			{
				if (allowAll)
				{
					inputText += text;
					return;
				}

				if (allowDots)
				{
					if (".".contains(text))
						inputText += text;
				}

				if (allowNegatives && inputText.length() == 0)
				{
					if ("-".contains(text))
						inputText += text;
				}

				if (allowDoubles && !inputText.contains("."))
				{
					if (".".contains(text))
						inputText += text;
				}

				if (enablePunctuation)
				{
					if (enableCaps && caps && "1234567890-=[]\\;',./`".contains(text))
					{
						if ("1".contains(text))
							inputText += "!";
						else if ("2".contains(text))
							inputText += "@";
						else if ("3".contains(text))
							inputText += "#";
						else if ("4".contains(text))
							inputText += "$";
						else if ("5".contains(text))
							inputText += "%";
						else if ("6".contains(text))
							inputText += "^";
						else if ("7".contains(text))
							inputText += "&";
						else if ("8".contains(text))
							inputText += "*";
						else if ("9".contains(text))
							inputText += "(";
						else if ("0".contains(text))
							inputText += ")";
						else if ("-".contains(text))
							inputText += "_";
						else if ("=".contains(text))
							inputText += "+";
						else if ("`".contains(text))
							inputText += "~";
						else if ("[".contains(text))
							inputText += "{";
						else if ("]".contains(text))
							inputText += "}";
						else if ("\\".contains(text))
							inputText += "|";
						else if (";".contains(text))
							inputText += ":";
						else if ("'".contains(text))
							inputText += "\"";
						else if (",".contains(text))
							inputText += "<";
						else if (".".contains(text))
							inputText += ">";
						else if ("/".contains(text))
							inputText += "?";

						return;
					}
					else if ("-=[]\\;',./`!@#$%^&*()_+~{}|:\"<>?".contains(text))
						inputText += text;
				}
				else if (allowColons)
				{
					if (";".contains(text) || ":".contains(text))
						inputText += ":";
				}

				if (allowLetters)
				{
					if ("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(text))
					{
						if (enableCaps)
						{
							if (caps)
								inputText += text.toUpperCase();
							else
								inputText += text;
						}
						else if (lowerCase)
							inputText += text.toLowerCase();
						else
							inputText += text.toUpperCase();
					}
				}

				if (allowNumbers)
				{
					if ("1234567890".contains(text))
					{
						inputText += text;
					}
				}
			}
		}
	}

	@Override
	public void setPosition(double x, double y)
	{
		this.posX = x;
		this.posY = y;
	}
}

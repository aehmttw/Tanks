package tanks.gui;

import tanks.Drawing;
import tanks.Game;
import org.lwjgl.glfw.GLFW;

public class TextBox 
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

	public boolean hover = false;

	public boolean selected = false;
	public boolean infoSelected = false;

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
	public double selectedFullColorG = 220;
	public double selectedFullColorB = 220;

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
		else if (hover)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else
			drawing.setColor(this.colorR, this.colorG, this.colorB);

		//drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);
		double m = 0.8;
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY * m);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

		drawing.setColor(0, 0, 0);

		drawing.drawInterfaceText(posX, posY - 30, labelText);

		if (selected)
			drawing.drawInterfaceText(posX, posY, inputText + "\u00a7127127127255_");
		else
			drawing.drawInterfaceText(posX, posY, inputText);

		if (enableHover)
		{
			if (infoSelected)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + 2 + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
			}
		}
	}

	public void update()
	{
		double mx = Drawing.drawing.getInterfaceMouseX();
		double my = Drawing.drawing.getInterfaceMouseY();

		hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - 30 && my < posY + sizeY / 2;

		infoSelected = mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && Game.game.window.validPressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1) && !selected)
		{
			this.inputText = "";
			selected = true;
			Drawing.drawing.playSound("bounce.ogg", 0.5f, 0.7f);
			Game.game.window.validPressedButtons.remove((Integer)GLFW.GLFW_MOUSE_BUTTON_1);
		}

		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_ENTER) || (!hover && Game.game.window.pressedButtons.contains(GLFW.GLFW_MOUSE_BUTTON_1) && selected))
		{
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_ENTER);

			this.performValueCheck();
			function.run();
			this.previousInputText = this.inputText;
			Drawing.drawing.playSound("destroy.ogg", 2f);
			selected = false;
		}

		if (Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_ESCAPE) && selected)
		{
			Game.game.window.validPressedKeys.remove((Integer)GLFW.GLFW_KEY_ESCAPE);
			selected = false;
			this.inputText = this.previousInputText;
			Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		}

		if (selected)
		{
			this.checkKeys();
		}

	}

	public void checkKeys()
	{
		boolean caps = false;

		if (this.enableCaps && (Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_LEFT_SHIFT) || Game.game.window.pressedKeys.contains(GLFW.GLFW_KEY_RIGHT_SHIFT)))
			caps = true;

		for (int i = 0; i < Game.game.window.validPressedKeys.size(); i++)
		{
			int key = Game.game.window.validPressedKeys.get(i);
			if (this.enableCaps && (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT))
				continue;
			
			Game.game.window.pressedKeys.remove((Integer)key);

			String text = (GLFW.glfwGetKeyName(Game.game.window.validPressedKeys.get(i), 0));

			if (text == null && Game.game.window.validPressedKeys.contains(GLFW.GLFW_KEY_SPACE))
				text = " ";
			
			if (Game.game.window.validPressedKeys.get(i).equals(GLFW.GLFW_KEY_BACKSPACE))
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
						continue;
					}

					if (allowDots)
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
							
							continue;
						}
						else if ("-=[]\\;',./`".contains(text))
							inputText += text;
					}
					else if (allowColons)
					{
						if (";".contains(text))
							inputText += ":";
					}

					if (allowLetters)
					{
						if ("abcdefghijklmnopqrstuvwxyz".contains(text))
						{
							if (enableCaps)
							{
								if (caps)
									inputText += text.toUpperCase();
								else
									inputText += text.toLowerCase();
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

		Game.game.window.validPressedKeys.clear();
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
}

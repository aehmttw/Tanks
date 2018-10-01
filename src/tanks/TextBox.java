package tanks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class TextBox 
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String labelText;
	public String inputText = "";

	public boolean enableHover = false;
	public String[] hoverText;

	public boolean hover = false;

	public boolean selected = false;

	boolean enableSpaces = true;
	boolean allowSpaces = true;
	boolean allowLetters = true;
	boolean allowNumbers = true;
	boolean allowAll = false;
	boolean checkMaxValue = false;
	
	boolean lowerCase = false;
	
	int maxChars = 15;
	int maxValue = Integer.MAX_VALUE;

	Color color = new Color(255, 255, 255);
	Color hoverColor = new Color(240, 240, 255);
	Color selectedColor = new Color(220, 255, 220);
	Color selectedFullColor = new Color(255, 220, 220);

	public TextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText)
	{
		this.posX = x;
		this.posY = y;
		this.function = f;

		this.sizeX = sX;
		this.sizeY = sY;
		this.labelText = text;

		this.inputText = defaultText;
	}

	public TextBox(double x, double y, double sX, double sY, String text, Runnable f, String defaultText, String hoverText)
	{
		this(x, y, sX, sY, text, f, defaultText);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
	}

	public void draw(Graphics g)
	{
		Drawing.setInterfaceFontSize(g, 24);

		if (selected)
		{
			if (this.inputText.length() >= this.maxChars)
				g.setColor(this.selectedFullColor);
			else
				g.setColor(this.selectedColor);
		}
		else if (hover)
			g.setColor(this.hoverColor);
		else
			g.setColor(this.color);

		Drawing.fillInterfaceRect(g, posX, posY, sizeX, sizeY);

		g.setColor(Color.black);

		Drawing.drawInterfaceText(g, posX, posY - 30, labelText);

		if (selected)
			Drawing.drawInterfaceText(g, posX, posY + 5, inputText + "_");
		else
			Drawing.drawInterfaceText(g, posX, posY + 5, inputText);

		if (enableHover)
		{
			if (hover)
			{
				g.setColor(Color.blue);
				Drawing.fillInterfaceOval(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				g.setColor(Color.white);
				Drawing.drawInterfaceText(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 5, "i");
				Drawing.drawTooltip(g, this.hoverText);
			}
			else
			{
				g.setColor(new Color(0, 150, 255));
				Drawing.fillInterfaceOval(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				g.setColor(Color.white);
				Drawing.drawInterfaceText(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 5, "i");
			}
		}
	}

	public void update()
	{
		double mx = Game.window.getInterfaceMouseX();
		double my = Game.window.getInterfaceMouseY();

		if (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2)
			hover = true;
		else
			hover = false;

		if (hover && InputMouse.lClickValid && !selected)
		{
			this.inputText = "";
			selected = true;
			InputMouse.lClickValid = false;
		}

		if (!hover && InputMouse.lClick && selected)
		{
			function.run();
			selected = false;
		}

		if (selected)
		{
			for (int i = 0; i < InputKeyboard.validKeys.size(); i++)
			{
				String text = (KeyEvent.getKeyText(InputKeyboard.validKeys.get(i)));

				if (InputKeyboard.validKeys.get(i).equals(KeyEvent.VK_BACK_SPACE))
					inputText = inputText.substring(0, Math.max(0, inputText.length() - 1));
				else if (inputText.length() + text.length() <= maxChars)
				{
					if (text.equals("\u2423") || text.equals("Space"))
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

						if (allowLetters)
						{
							if ("ABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(text))
							{
								if (lowerCase)
									inputText += text.toLowerCase();
								else
									inputText += text;
							}
						}

						if (allowNumbers)
						{
							if ("1234567890".contains(text))
								inputText += text;

							performMaxValueCheck();
						}
					}
				}
			}

			InputKeyboard.validKeys.clear();
		}
	}
	
	public void performMaxValueCheck()
	{
		if (checkMaxValue)
			if (Integer.parseInt(inputText) > this.maxValue)
				inputText = this.maxValue + "";
	}
}

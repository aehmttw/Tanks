package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.*;
import tanks.gui.screen.ScreenInfo;
import tanks.translation.Translation;

import java.util.ArrayList;

public class TextBox implements IDrawable, ITrigger
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;

	public String rawLabelText;
	public String labelText;
	public String translatedLabelText;

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

	public int maxChars = 40;
	public double maxValue = Integer.MAX_VALUE;
	public double minValue = Integer.MIN_VALUE;

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
	public double selectedFullColorG = 255;
	public double selectedFullColorB = 220;
	public double selectedFullFlashColorR = 255;
	public double selectedFullFlashColorG = 200;
	public double selectedFullFlashColorB = 200;
	public double flashAnimation = 0;

	public long lastFrame;
	public double effectTimer;
	public ArrayList<Effect> glowEffects = new ArrayList<>();

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
		this.setText(text);

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

		drawing.setInterfaceFontSize(this.sizeY * 0.6);

		if (Game.glowEnabled)
			drawTallGlow(this.posX, this.posY + 5, this.sizeX, this.sizeY, sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);

		drawing.setColor(this.bgColorR, this.bgColorG, this.bgColorB);
		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - sizeY * 3 / 4, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY - sizeY * 3 / 4, sizeY, sizeY);

		drawing.fillInterfaceRect(posX, posY - 15, sizeX, sizeY * 3 / 4);

		double m = 0.8;

		if (Game.glowEnabled)
		{
			if (selected)
				Button.drawGlow(this.posX, this.posY + 3.5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.55, 0, 0, 0, 160, false);
			else if (hover && !Game.game.window.touchscreen)
				Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.65, 0, 0, 0, 80, false);
			else
				Button.drawGlow(this.posX, this.posY + 5, this.sizeX - this.sizeY * (1 - m), this.sizeY * m, 0.6, 0, 0, 0, 100, false);

			if (this.lastFrame == Panel.panel.ageFrames - 1)
			{
				for (Effect e : this.glowEffects)
				{
					e.drawGlow();
					e.draw();
				}
			}
		}

		if (selected)
		{
			if (this.inputText.length() >= this.maxChars)
			{
				drawing.setColor(this.selectedFullColorR * (1 - this.flashAnimation) + this.selectedFullFlashColorR * flashAnimation,
						this.selectedFullColorG * (1 - this.flashAnimation) + this.selectedFullFlashColorG * flashAnimation,
						this.selectedFullColorB * (1 - this.flashAnimation) + this.selectedFullFlashColorB * flashAnimation);
			}
			else
				drawing.setColor(this.selectedColorR, this.selectedColorG, this.selectedColorB);
		}
		else if (hover && !Game.game.window.touchscreen && this.enabled)
			drawing.setColor(this.hoverColorR, this.hoverColorG, this.hoverColorB);
		else if (this.enabled)
			drawing.setColor(this.colorR, this.colorG, this.colorB);
		else
			drawing.setColor((this.colorR + this.bgColorR) / 2, (this.colorG + this.bgColorG) / 2, (this.colorB + this.bgColorB) / 2);

		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY * m);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY * m, sizeY * m);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY * m, sizeY * m);

		drawing.setColor(0, 0, 0);

		drawing.drawInterfaceText(posX, posY - sizeY * 13 / 16, translatedLabelText);

		this.drawInput();

		if (enableHover)
		{
			if (Game.glowEnabled && !Game.game.window.drawingShadow)
			{
				if (infoSelected && !Game.game.window.touchscreen)
				{
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
					Drawing.drawing.setColor(0, 0, 255);
					Drawing.drawing.fillInterfaceGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
				}
				else
					Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			}

			if (infoSelected && !Game.game.window.touchscreen)
			{
				drawing.setColor(0, 0, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
				drawing.drawTooltip(this.hoverText);
			}
			else
			{
				drawing.setColor(0, 150, 255);
				drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				drawing.setColor(255, 255, 255);
				drawing.drawInterfaceText(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, "i");
			}
		}

		if (selected && Game.game.window.touchscreen)
		{
			Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY - sizeY * 13 / 16 + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			Button.drawGlow(this.posX + this.sizeX / 2 - this.sizeY * 3 / 2, this.posY - sizeY * 13 / 16 + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
		}

		if (selected && inputText.length() > 0)
		{
			if (Game.glowEnabled)
			{
				if (clearSelected && !Game.game.window.touchscreen)
				{
					Button.drawGlow(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.7, 0, 0, 0, 80, false);
					Drawing.drawing.setColor(127, 0, 0);
					Drawing.drawing.fillInterfaceGlow(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, this.sizeY * 9 / 4, this.sizeY * 9 / 4);
				}
				else
					Button.drawGlow(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY + 2.5, this.sizeY * 3 / 4, this.sizeY * 3 / 4, 0.6, 0, 0, 0, 100, false);
			}

			if (!clearSelected || Game.game.window.touchscreen)
				drawing.setColor(160, 160, 160);
			else
				drawing.setColor(255, 0, 0);

			drawing.fillInterfaceOval(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);

			drawing.setColor(255, 255, 255);

			drawing.setInterfaceFontSize(this.sizeY * 0.6);
			drawing.drawInterfaceText(this.posX - this.sizeX / 2 + this.sizeY / 2, this.posY - 2.5, "x");
		}

		if (selected && Game.game.window.touchscreen)
		{
			drawing.setColor(255, 255, 255);
			drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY - sizeY * 13 / 16, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
			drawing.drawInterfaceImage("icons/paste.png", this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY - sizeY * 13 / 16, this.sizeY * 1 / 2, this.sizeY * 1 / 2);

			drawing.fillInterfaceOval(this.posX + this.sizeX / 2 - this.sizeY * 3 / 2, this.posY - sizeY * 13 / 16, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
			drawing.drawInterfaceImage("icons/copy.png", this.posX + this.sizeX / 2 - this.sizeY * 3 / 2, this.posY - sizeY * 13 / 16, this.sizeY * 1 / 2, this.sizeY * 1 / 2);
		}
	}

	public void drawInput()
	{
		double size = this.sizeY * 0.6;
		if (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, inputText) / Drawing.drawing.interfaceScale > this.sizeX - 80)
			Drawing.drawing.setInterfaceFontSize(size * (this.sizeX - 80) / (Game.game.window.fontRenderer.getStringSizeX(Drawing.drawing.fontSize, inputText) / Drawing.drawing.interfaceScale));

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

			boolean handled = checkMouse(mx, my, Game.game.window.pressedButtons.contains(InputCodes.MOUSE_BUTTON_1), Game.game.window.validPressedButtons.contains(InputCodes.MOUSE_BUTTON_1), null);

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

				boolean handled = checkMouse(mx, my, true, p.valid && p.tag.equals(""), p);

				if (handled)
					p.tag = "textbox";

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

		if (Game.glowEnabled && !Game.game.window.drawingShadow)
		{
			if (this.lastFrame < Panel.panel.ageFrames - 1)
				this.glowEffects.clear();

			this.lastFrame = Panel.panel.ageFrames;

			for (int i = 0; i < this.glowEffects.size(); i++)
			{
				Effect e = this.glowEffects.get(i);
				e.update();

				if (e.age > e.maxAge)
				{
					this.glowEffects.remove(i);
					i--;
				}
			}

			if (this.shouldAddEffect())
			{
				this.addEffect();
			}
		}

		if (this.selected)
			Panel.selectedTextBox = this;

		this.flashAnimation = Math.max(0, this.flashAnimation - Panel.frameFrequency / 25);
	}

	public void addEffect()
	{
		this.effectTimer += 0.25 * (this.sizeX + this.sizeY) / 400 * Math.random() * Game.effectMultiplier;

		while (this.effectTimer >= 0.4 / Panel.frameFrequency)
		{
			this.effectTimer -= 0.4 / Panel.frameFrequency;
			Button.addEffect(this.posX, this.posY, this.sizeX - this.sizeY * (1 - 0.8), this.sizeY * 0.8, this.glowEffects);
		}
	}

	public boolean shouldAddEffect()
	{
		return this.hover && !this.selected && this.enabled && !Game.game.window.touchscreen;
	}

	public boolean checkMouse(double mx, double my, boolean down, boolean valid, InputPoint p)
	{
		boolean handled = false;

		if (Game.game.window.touchscreen)
		{
			sizeX += 20;
			sizeY += 20;
		}

		hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;

		infoSelected = mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;
		clearSelected = selected && mx < posX - sizeX / 2 + sizeY && mx > posX - sizeX / 2 && my > posY - sizeY / 2 && my < posY + sizeY / 2;

		if (hover && valid && enabled)
		{
			if (infoSelected && enableHover && Game.game.window.touchscreen)
			{
				handled = true;
				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
				Game.screen = new ScreenInfo(Game.screen, this.translatedLabelText, this.hoverText);
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
			if (selected && valid && mx > posX + sizeX / 2 - sizeY && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 13 / 16 && my < posY + sizeY / 2 - sizeY * 13 / 16)
			{
				this.paste();
				handled = true;
				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
			}

			if (selected && valid && mx > posX + sizeX / 2 - sizeY * 2 && mx < posX + sizeX / 2 - sizeY && my > posY - sizeY / 2 - sizeY * 13 / 16 && my < posY + sizeY / 2 - sizeY * 13 / 16)
			{
				this.copy();
				handled = true;
				Drawing.drawing.playVibration("click");
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
			}
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

		boolean hover = mx > posX - sizeX / 2 && mx < posX + sizeX / 2 && my > posY - sizeY / 2 - sizeY * 3 / 4 && my < posY + sizeY / 2;

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

		if (Game.glowEnabled)
		{
			this.submitEffect();
		}
	}

	public void submitEffect()
	{
		for (int i = 0; i < 0.2 * (this.sizeX + this.sizeY) * Game.effectMultiplier; i++)
			Button.addEffect(this.posX, this.posY, this.sizeX - this.sizeY * (1 - 0.8), this.sizeY * 0.8, this.glowEffects, Math.random() * 4, 0.8, 0.25);
	}

	public void revert()
	{
		selected = false;
		Panel.selectedTextBox = null;
		this.inputText = this.previousInputText;
		Drawing.drawing.playSound("bounce.ogg", 0.25f, 0.7f);
		Game.game.window.showKeyboard = false;
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
			this.revert();
		}

		ArrayList<Character> texts = Game.game.window.getRawTextKeys();

		Game.game.window.pressedKeys.clear();
		Game.game.window.validPressedKeys.clear();

		if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_CONTROL) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_LEFT_SUPER) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_RIGHT_SUPER))
		{
			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_C))
			{
				this.copy();
			}

			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_V))
			{
				this.paste();
			}

			if (Game.game.window.textPressedKeys.contains(InputCodes.KEY_BACKSPACE) || Game.game.window.textPressedKeys.contains(InputCodes.KEY_DELETE))
			{
				Game.game.window.textPressedKeys.clear();
				Game.game.window.textValidPressedKeys.clear();
				Game.game.window.getRawTextKeys().clear();

				this.clear();
			}
		}

		for (char key : texts)
		{
			inputKey(key);
		}

		texts.clear();
	}

	public void performValueCheck()
	{
		try
		{
			if (checkMaxValue)
				if (Integer.parseInt(inputText) > this.maxValue)
					inputText = (int) this.maxValue + "";

			if (checkMinValue)
				if (Integer.parseInt(inputText) < this.minValue)
					inputText = (int) this.minValue + "";
		}
		catch (Exception ignored) {}
	}

	public void inputKey(char key)
	{
		if (key == '\b')
			inputText = inputText.substring(0, Math.max(0, inputText.length() - 1));
		else if (this.inputText.length() < this.maxChars)
		{
			if (key == ' ')
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
				if (allowAll && Game.game.window.fontRenderer.supportsChar(key))
				{
					inputText += key;
					return;
				}

				if (allowDots)
				{
					if (key == '.')
						inputText += key;
				}

				if (allowNegatives && inputText.length() == 0)
				{
					if ('-' == key)
						inputText += key;
				}

				if (allowDoubles && !inputText.contains("."))
				{
					if ('.' == key)
						inputText += key;
				}

				if (enablePunctuation)
				{
					if ("-=[]\\;',./`!@#$%^&*()_+~{}|:\"<>?".contains(key + ""))
						inputText += key;
				}
				else if (allowColons)
				{
					if (';' == key || ':' == key)
						inputText += ':';
				}

				if (allowLetters)
				{
					if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".contains(key + ""))
					{
						if (enableCaps)
						{
							inputText += key;
						}
						else if (lowerCase)
							inputText += Character.toLowerCase(key);
						else
							inputText += Character.toUpperCase(key);
					}
				}

				if (allowNumbers)
				{
					if ("1234567890".contains(key + ""))
					{
						inputText += key;
					}
				}
			}
		}
		else
		{
			this.flashAnimation = 1;
		}
	}

	public void copy()
	{
		Game.game.window.textPressedKeys.clear();
		Game.game.window.textValidPressedKeys.clear();
		Game.game.window.getRawTextKeys().clear();

		Game.game.window.setClipboard(this.inputText);
	}

	public void paste()
	{
		Game.game.window.textPressedKeys.clear();
		Game.game.window.textValidPressedKeys.clear();
		Game.game.window.getRawTextKeys().clear();

		String s = Game.game.window.getClipboard();

		for (int i = 0; i < s.length(); i++)
		{
			this.inputKey(s.charAt(i));
		}
	}

	@Override
	public void setPosition(double x, double y)
	{
		this.posX = x;
		this.posY = y;
	}

	public static void drawTallGlow(double posX, double posY, double sizeX, double sizeY, double extra, double size, double r, double g, double b, double a, boolean glow)
	{
		Game.game.window.shapeRenderer.setBatchMode(true, true, false, glow);

		Drawing drawing = Drawing.drawing;
		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - sizeY * size - extra, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - sizeY * size - extra, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - extra, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - extra, 0);

		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY + sizeY * size, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY + sizeY * size, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);

		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 - size * sizeY, posY - extra, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 - size * sizeY, posY, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - extra, 0);

		drawing.setColor(0, 0, 0, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + size * sizeY, posY - extra, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + size * sizeY, posY, 0);
		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - extra, 0);


		drawing.setColor(r, g, b, a);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - extra, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - extra, 0);
		drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);
		drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);

		Game.game.window.shapeRenderer.setBatchMode(false, true, false, glow);
		Game.game.window.shapeRenderer.setBatchMode(true, false, false, glow);

		for (int i = 0; i < 15; i++)
		{
			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 15) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 15) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 16) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 16) / 30.0 * Math.PI) * size, 0);

			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i + 1) / 30.0 * Math.PI) * size, posY + sizeY * Math.sin((i + 1) / 30.0 * Math.PI) * size, 0);

			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2, posY - extra, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 30) / 30.0 * Math.PI) * size, posY - extra + sizeY * Math.sin((i + 30) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX - sizeX / 2 + sizeY / 2 + sizeY * Math.cos((i + 31) / 30.0 * Math.PI) * size, posY - extra + sizeY * Math.sin((i + 31) / 30.0 * Math.PI) * size, 0);

			drawing.setColor(r, g, b, a);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2, posY - extra, 0);
			drawing.setColor(0, 0, 0, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i + 45) / 30.0 * Math.PI) * size, posY - extra + sizeY * Math.sin((i + 45) / 30.0 * Math.PI) * size, 0);
			drawing.addInterfaceVertex(posX + sizeX / 2 - sizeY / 2 + sizeY * Math.cos((i + 46) / 30.0 * Math.PI) * size, posY - extra + sizeY * Math.sin((i + 46) / 30.0 * Math.PI) * size, 0);
		}

		Game.game.window.shapeRenderer.setBatchMode(false, false, false, glow);
	}

	public void setText(String text)
	{
		this.rawLabelText = text;
		this.labelText = text;
		this.translatedLabelText = Translation.translate(text);
	}

	public void setText(String text, String text2)
	{
		this.rawLabelText = text + text2;
		this.labelText = text + text2;
		this.translatedLabelText = Translation.translate(text) + Translation.translate(text2);
	}

	public void setText(String text, Object... objects)
	{
		this.rawLabelText = text;
		this.labelText = String.format(text, objects);
		this.translatedLabelText = Translation.translate(text, objects);
	}

	public void setTextArgs(Object... objects)
	{
		this.labelText = String.format(this.rawLabelText, objects);
		this.translatedLabelText = Translation.translate(this.rawLabelText, objects);
	}

}

package tanks.gui;

import basewindow.InputCodes;
import basewindow.InputPoint;
import tanks.Drawing;
import tanks.Game;
import tanks.IDrawable;
import tanks.gui.screen.ScreenInfo;

public class Button implements IDrawable
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String text;

	public boolean enableHover = false;
	public String[] hoverText;
	public String hoverTextRaw = "";

	public boolean selected = false;
	public boolean infoSelected = false;

	public boolean justPressed = false;

	public boolean enabled = true;

	public double disabledColR = 200;
	public double disabledColG = 200;
	public double disabledColB = 200;

	public double unselectedColR = 255;
	public double unselectedColG = 255;
	public double unselectedColB = 255;

	public double selectedColR = 240;
	public double selectedColG = 240;
	public double selectedColB = 255;

	public double textColR = 0;
	public double textColG = 0;
	public double textColB = 0;

	public double textOffsetX = 0;
	public double textOffsetY = 0;

	public boolean silent = false;

	/** If set to true and is part of an online service, pressing the button sends the player to a loading screen*/
	public boolean wait = false;

	public Button(double x, double y, double sX, double sY, String text, Runnable f)
	{
		this.function = f;

		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;
	}

	public Button(double x, double y, double sX, double sY, String text, Runnable f, String hoverText)
	{
		this(x, y, sX, sY, text, f);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
		this.hoverTextRaw = hoverText;
	}

	public Button(double x, double y, double sX, double sY, String text)
	{
		this.posX = x;
		this.posY = y;
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;

		this.enabled = false;
	}

	public Button(double x, double y, double sX, double sY, String text, String hoverText)
	{
		this(x, y, sX, sY, text);

		this.enableHover = true;
		this.hoverText = hoverText.split("---");
		this.hoverTextRaw = hoverText;
	}

	public void draw()
	{
		Drawing drawing = Drawing.drawing;
		drawing.setInterfaceFontSize(24);

		if (!enabled)
			drawing.setColor(this.disabledColR, this.disabledColG, this.disabledColB);

		else if (selected && !Game.game.window.touchscreen)
			drawing.setColor(this.selectedColR, this.selectedColG, this.selectedColB);
		else
			drawing.setColor(this.unselectedColR, this.unselectedColG, this.unselectedColB);

		//drawing.fillInterfaceRect(posX, posY, sizeX, sizeY);

		drawing.fillInterfaceRect(posX, posY, sizeX - sizeY, sizeY);
		drawing.fillInterfaceOval(posX - sizeX / 2 + sizeY / 2, posY, sizeY, sizeY);
		drawing.fillInterfaceOval(posX + sizeX / 2 - sizeY / 2, posY, sizeY, sizeY);

		drawing.setColor(this.textColR, this.textColG, this.textColB);
		drawing.drawInterfaceText(posX + this.textOffsetX, posY + this.textOffsetY, text);

		if (enableHover)
		{
			if (infoSelected && !Game.game.window.touchscreen)
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
		this.justPressed = false;

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

				if (p.tag.equals(""))
				{
					double mx = Drawing.drawing.getInterfacePointerX(p.x);
					double my = Drawing.drawing.getInterfacePointerY(p.y);

					boolean handled = checkMouse(mx, my, p.valid);

					if (handled)
						p.tag = "button";
				}
			}
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

		selected = (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);
		infoSelected = (mx > posX + sizeX/2 - sizeY && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2);

		if (selected && valid)
		{
			if (infoSelected && this.enableHover && Game.game.window.touchscreen)
			{
				handled = true;
				Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
				Drawing.drawing.playVibration("click");
				Game.screen = new ScreenInfo(Game.screen, this.text, this.hoverText);
			}
			else if (enabled)
			{
				handled = true;
				function.run();
				this.justPressed = true;

				if (!this.silent)
				{
					Drawing.drawing.playSound("bullet_explode.ogg", 2f, 0.3f);
					Drawing.drawing.playVibration("click");
				}
			}
		}

		if (Game.game.window.touchscreen)
		{
			sizeX -= 20;
			sizeY -= 20;
		}

		return handled;
	}
}

package tanks;

import java.awt.Color;
import java.awt.Graphics;

public class Button 
{
	public Runnable function;
	public double posX;
	public double posY;
	public double sizeX;
	public double sizeY;
	public String text;

	public boolean enableHover = false;
	public String[] hoverText;

	public boolean selected = false;

	public boolean clicked = false;
	
	public boolean enabled = true;

	Color disabledCol = new Color(200, 200, 200);
	Color unselectedCol = new Color(255, 255, 255);
	Color selectedCol = new Color(240, 240, 255);

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
	}

	public void draw(Graphics g)
	{
		Drawing.setInterfaceFontSize(g, 24);
		Drawing drawing = Drawing.window;
		
		if (!enabled)
			g.setColor(this.disabledCol);	
		
		else if (selected)
			g.setColor(this.selectedCol);
		else
			g.setColor(this.unselectedCol);

		drawing.fillInterfaceRect(g, posX, posY, sizeX, sizeY);

		g.setColor(Color.black);
		drawing.drawInterfaceText(g, posX, posY + 5, text);

		if (enableHover)
		{
			if (selected)
			{
				g.setColor(Color.blue);
				drawing.fillInterfaceOval(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				g.setColor(Color.white);
				drawing.drawInterfaceText(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 5, "i");
				Drawing.drawTooltip(g, this.hoverText);
			}
			else
			{
				g.setColor(new Color(0, 150, 255));
				drawing.fillInterfaceOval(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY, this.sizeY * 3 / 4, this.sizeY * 3 / 4);
				g.setColor(Color.white);
				drawing.drawInterfaceText(g, this.posX + this.sizeX / 2 - this.sizeY / 2, this.posY + 5, "i");
			}
		}
	}

	public void update()
	{
		double mx = Game.window.getInterfaceMouseX();
		double my = Game.window.getInterfaceMouseY();

		if (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2)
			selected = true;
		else
			selected = false;

		if (selected && InputMouse.lClickValid && !clicked && enabled)
		{
			function.run();
			clicked = true;
			InputMouse.lClickValid = false;
		}

		if (!(selected && InputMouse.lClick))
			clicked = false;
	}
}

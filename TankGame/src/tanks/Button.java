package tanks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class Button 
{
	Runnable function;
	double posX;
	double posY;
	double sizeX;
	double sizeY;
	String text;
	
	boolean enableHover = false;
	String[] hoverText;
	
	boolean selected = false;
	
	boolean clicked = false;
	
	Color unselectedCol = new Color(255, 255, 255);
	Color selectedCol = new Color(240, 240, 255);

	public Button(double sX, double sY, String text, Runnable f)
	{
		this.function = f;
		
		this.sizeX = sX;
		this.sizeY = sY;
		this.text = text;
	}
	
	public Button(double sX, double sY, String text, Runnable f, String hoverText)
	{
		this(sX, sY, text, f);
		this.enableHover = true;
		this.hoverText = hoverText.split("---");
	}
	
	public void drawUpdate(Graphics g, int x, int y)
	{
		this.posX = x;
		this.posY = y;
		
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Screen.scale)));

		double mx = Game.gamescreen.getMouseX();
		double my = Game.gamescreen.getMouseY();

		if (mx > posX - sizeX/2 && mx < posX + sizeX/2 && my > posY - sizeY/2  && my < posY + sizeY/2)
			selected = true;
		else
			selected = false;
		
		if (selected && MouseInputListener.lClickValid && !clicked)
		{
			function.run();
			clicked = true;
			MouseInputListener.lClickValid = false;
		}
		
		if (!(selected && MouseInputListener.lClick))
			clicked = false;
		
		if (selected)
			g.setColor(selectedCol);
		else
			g.setColor(unselectedCol);
		
		Screen.fillRect(g, posX, posY, sizeX, sizeY);
		
		g.setColor(Color.black);
		Screen.drawText(g, posX, posY + 5, text);
		
		if (selected && enableHover)
		{
			Screen.drawTooltip(g, this.hoverText);
		}
	}
}

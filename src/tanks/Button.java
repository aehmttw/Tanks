package tanks;

import java.awt.Color;
import java.awt.Font;
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
	
	public void draw(Graphics g, int x, int y)
	{
		this.posX = x;
		this.posY = y;
		
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (24 * Window.scale)));
		
		if (selected)
			g.setColor(this.selectedCol);
		else
			g.setColor(this.unselectedCol);
		
		Window.fillRect(g, posX, posY, sizeX, sizeY);
		
		g.setColor(Color.black);
		Window.drawText(g, posX, posY + 5, text);
		
		if (selected && enableHover)
		{
			Window.drawTooltip(g, this.hoverText);
		}
	}
	
	public void update(int x, int y)
	{
		this.posX = x;
		this.posY = y;

		double mx = Game.window.getMouseX();
		double my = Game.window.getMouseY();
		
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
	}
}

package tanks;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.font.FontRenderContext;

import javax.swing.*;

@SuppressWarnings("serial")
public class Screen extends JFrame 
{
	public static int sizeX = 1400;//1920;
	public static int sizeY = 900;//1100;

	public static double scale = 1;
	
	public Panel panel = new Panel();
	
	public static Screen screen;
	
	public static int offset = 22;

	static FontRenderContext frc = new FontRenderContext(null, true, true);
	
	public Screen()
	{
		this.addMouseListener(new MouseInputListener());
		this.addKeyListener(new KeyInputListener());
		this.setSize((int)(sizeX * scale), (int) ((sizeY + offset) * scale ));
		this.setVisible(true);
		Container visiblePart = this.getContentPane();
		visiblePart.add(panel);
		this.setResizable(true);
		this.setMinimumSize(new Dimension(350, 265 + offset));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screen = this;
	}
	
	@Override
	public void setSize(int x, int y)
	{
		super.setSize(x, (int) (y + 40 + offset * (1 - scale)));
	}
	
	@SuppressWarnings("static-access")
	public int getSizeX()
	{
		return (int) (this.WIDTH * scale);
	}
	
	@SuppressWarnings("static-access")
	public int getSizeY()
	{
		return (int) (this.HEIGHT * scale);
	}
	
	public static void fillOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x - sizeX / 2) + Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2);
		int drawY = (int) Math.round(scale * (y - sizeY / 2) + Math.max(0, Game.gamescreen.getSize().getHeight() - Screen.offset - 40 - Screen.sizeY * Screen.scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void drawOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x - sizeX / 2) + Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2);
		int drawY = (int) Math.round(scale * (y - sizeY / 2) + Math.max(0, Game.gamescreen.getSize().getHeight() - Screen.offset - 40 - Screen.sizeY * Screen.scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void fillRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x - sizeX / 2) + Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2);
		int drawY = (int) Math.round(scale * (y - sizeY / 2) + Math.max(0, Game.gamescreen.getSize().getHeight() - Screen.offset - 40 - Screen.sizeY * Screen.scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void drawRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x - sizeX / 2) + Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2);
		int drawY = (int) Math.round(scale * (y - sizeY / 2) + Math.max(0, Game.gamescreen.getSize().getHeight() - Screen.offset - 40 - Screen.sizeY * Screen.scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawRect(drawX , drawY, drawSizeX, drawSizeY);
	}
	
	public static void drawText(Graphics g, double x, double y, String text)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());
				
		double sizeY = g.getFont().getSize() / 3;
		//int size = text.length() * g.getFont().getSize() / 2;
		int drawX = (int) (scale * x - sizeX / 2 + Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2);
		int drawY = (int) (scale * (y + sizeY / 2) + Math.max(0, Game.gamescreen.getSize().getHeight() - Screen.offset - 40 - Screen.sizeY * Screen.scale) / 2);
		g.drawString(text, drawX, drawY);
	
		//g.setColor(Color.red);
		//g.drawRect((int)((x - sizeX / 2) * scale), (int)((y - sizeY * 3 / 2) * scale), (int)g.getFont().getStringBounds(text, frc).getWidth(), (int)g.getFont().getStringBounds(text, frc).getHeight());
	}
	
	public static double drawTooltip(Graphics g, String[] text)
	{
		double x = Game.gamescreen.getMouseX();
		double y = Game.gamescreen.getMouseY();

		int xPadding = (int) (16 * Screen.scale);
		int yPadding = (int) (8 * Screen.scale);

		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (Screen.scale * 14)));
		
		int sizeX = 0;
		for (int i = 0; i < text.length; i++)
		{
			sizeX = Math.max(sizeX, (int) Math.round(g.getFont().getStringBounds(text[i], frc).getWidth()));
		}
		
		int sizeY = g.getFont().getSize();

		//int size = text.length() * g.getFont().getSize() / 2;
		int drawX = (int) (scale * (x));
		int drawY = (int) (scale * (y));
		
		g.setColor(new Color(0, 0, 0, 127));
		g.fillRect(drawX, drawY, sizeX + xPadding * 2, sizeY + yPadding * 2 * text.length);
		
		g.setColor(Color.WHITE);
		for (int i = 0; i < text.length; i++)
		{
			g.drawString(text[i], drawX + xPadding, (int) (drawY + (i + 1) * (sizeY / 2 + yPadding * (1 + 1 / 2.0))));
		}
		
		return (y - (drawY / Screen.scale + sizeY + yPadding / Screen.scale * 2));
	}
	
	public double getMouseX()
	{
		return (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX() - Math.max(0, Game.gamescreen.getSize().getWidth() - Screen.sizeX * Screen.scale) / 2) / scale;
	}
	
	public double getMouseY()
	{
		return ((MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY() - Math.max(0, Game.gamescreen.getSize().getHeight() - (offset + 1) - Screen.sizeY * Screen.scale) / 2)) / scale - 2;
	}
	
	public void setScreenSize(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		this.setSize((int) (x * scale), (int) ((y + offset) * scale));
	}
	
	public void setScreenBounds(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		Game.currentSizeX = x / Game.tank_size;
		Game.currentSizeY = y / Game.tank_size;
	}
}

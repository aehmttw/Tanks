package tanks;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.MouseInfo;
import javax.swing.*;

@SuppressWarnings("serial")
public class Screen extends JFrame 
{
	public static int sizeX = 1400;//1920;
	public static int sizeY = 900;//1100;

	public static double scale = 1;
	
	public Panel panel = new Panel();
	
	public static Screen screen;
	
	public Screen()
	{
		this.addMouseListener(new MouseInputListener());
		this.addKeyListener(new KeyInputListener());
		this.setSize((int)(sizeX * scale), (int) (sizeY * scale + 22));
		this.setVisible(true);
		Container visiblePart = this.getContentPane();
		visiblePart.add(panel);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		screen = this;
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
		int drawX = (int) (scale * (x - sizeX / 2));
		int drawY = (int) (scale * (y - sizeY / 2));
		int drawSizeX = (int) (sizeX * scale);
		int drawSizeY = (int) (sizeY * scale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void drawOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) (scale * (x - sizeX / 2));
		int drawY = (int) (scale * (y - sizeY / 2));
		int drawSizeX = (int) (sizeX * scale);
		int drawSizeY = (int) (sizeY * scale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void fillRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) (scale * (x - sizeX / 2));
		int drawY = (int) (scale * (y - sizeY / 2));
		int drawSizeX = (int) (sizeX * scale);
		int drawSizeY = (int) (sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void drawRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) (scale * (x - sizeX / 2));
		int drawY = (int) (scale * (y - sizeY / 2));
		int drawSizeX = (int) (sizeX * scale);
		int drawSizeY = (int) (sizeY * scale);

		g.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public double getMouseX()
	{
		return (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX()) / scale;
	}
	
	public double getMouseY()
	{
		return (MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY()) / scale - 23;
	}
	
	public void setScreenSize(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		this.setSize((int) (x * scale), (int) (y * scale + 22));
	}
}

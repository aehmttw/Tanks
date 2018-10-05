package tanks;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;

import javax.swing.*;

@SuppressWarnings("serial")
public class Drawing extends JFrame 
{
	public static int sizeX = 1400;//1920;
	public static int sizeY = 900;//1100;

	public static double playerX = sizeX / 2;
	public static double playerY = sizeY / 2;

	public static double interfaceSizeX = 1400;
	public static double interfaceSizeY = 900;

	public static double scale = 1;
	public static double unzoomedScale = 1;

	public static double interfaceScale = 1;

	public static boolean enableMovingCamera = false;
	public static boolean enableMovingCameraX = false;
	public static boolean enableMovingCameraY = false;

	public static int statsHeight = 40;
	
	public static boolean movingCamera = false;

	public Panel panel = new Panel();

	public static Drawing window;

	public static int yOffset = 55;
	public static int xOffset = 22;
	public static int mouseXoffset = 0;
	public static int mouseYoffset = -26;

	static FontRenderContext frc = new FontRenderContext(null, true, true);
	public static ArrayList<String> pendingSounds = new ArrayList<String>();

	public Drawing()
	{
		this.addMouseListener(new InputMouse());
		this.addKeyListener(new InputKeyboard());
		this.addMouseWheelListener(new InputScroll());
		this.addMouseMotionListener(new InputMouse());

		this.setSize((int)(sizeX * scale), (int) ((sizeY + yOffset) * scale ));
		this.setVisible(true);
		Container visiblePart = this.getContentPane();
		visiblePart.add(panel);
		this.setResizable(true);
		this.setMinimumSize(new Dimension(350, 265 + yOffset));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window = this;

		if (System.getProperties().get("os.name").toString().toLowerCase().contains("mac"))
		{
			Drawing.yOffset = 22;
			Drawing.xOffset = 0;
			Drawing.mouseYoffset = -2;
		}
	}

	@Override
	public void setSize(int x, int y)
	{
		super.setSize(x + xOffset, (int) (y + statsHeight + yOffset * (1 - scale)));
	}

	public static void fillOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);

		if (drawX - 200 * Drawing.scale > Panel.windowWidth || drawX + 200 * Drawing.scale < 0 || drawY - 200 * Drawing.scale > Panel.windowHeight || drawY + 200 * Drawing.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void drawOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);

		if (drawX - 200 * Drawing.scale > Panel.windowWidth || drawX + 200 * Drawing.scale < 0 || drawY - 200 * Drawing.scale > Panel.windowHeight || drawY + 200 * Drawing.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void fillRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);

		if (drawX - 200 * Drawing.scale > Panel.windowWidth || drawX + 200 * Drawing.scale < 0 || drawY - 200 * Drawing.scale > Panel.windowHeight || drawY + 200 * Drawing.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public static void fill3DRect(Graphics g, double x, double y, double sizeX, double sizeY, boolean raised)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);

		if (drawX - 200 * Drawing.scale > Panel.windowWidth || drawX + 200 * Drawing.scale < 0 || drawY - 200 * Drawing.scale > Panel.windowHeight || drawY + 200 * Drawing.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fill3DRect(drawX, drawY, drawSizeX, drawSizeY, raised);
	}

	public static void fillBackgroundRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void drawRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);

		if (drawX - 200 * Drawing.scale > Panel.windowWidth || drawX + 200 * Drawing.scale < 0 || drawY - 200 * Drawing.scale > Panel.windowHeight || drawY + 200 * Drawing.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void fillInterfaceOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void drawInterfaceOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void fillInterfaceRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public static void drawInterfaceRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.drawRect(drawX , drawY, drawSizeX, drawSizeY);
	}

	public static void drawText(Graphics g, double x, double y, String text)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());

		double sizeY = g.getFont().getSize() / 3 / Drawing.scale;
		//int size = text.length() * g.getFont().getSize() / 2;
		int drawX = (int) (scale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2);
		int drawY = (int) (scale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2);
		g.drawString(text, drawX, drawY);

		//g.setColor(Color.red);
		//g.drawRect((int)((x - sizeX / 2) * scale), (int)((y - sizeY * 3 / 2) * scale), (int)g.getFont().getStringBounds(text, frc).getWidth(), (int)g.getFont().getStringBounds(text, frc).getHeight());
	}

	public static void drawInterfaceText(Graphics g, double x, double y, String text)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());

		double sizeY = g.getFont().getSize() / 3 / Drawing.interfaceScale;
		int drawX = (int) (interfaceScale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) (interfaceScale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		g.drawString(text, drawX, drawY);
	}

	public static void drawUncenteredInterfaceText(Graphics g, double x, double y, String text)
	{
		int drawX = (int) (interfaceScale * x + Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) (interfaceScale * y + Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		g.drawString(text, drawX, drawY);
	}

	public static void setFontSize(Graphics g, double size)
	{
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (size * Drawing.scale)));
	}

	public static void setInterfaceFontSize(Graphics g, double size)
	{
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (size * Drawing.interfaceScale)));
	}

	public static void drawTooltip(Graphics g, String[] text)
	{
		double x = Game.window.getInterfaceMouseX();
		double y = Game.window.getInterfaceMouseY();

		int xPadding = (int) (16);
		int yPadding = (int) (8);

		Drawing.setInterfaceFontSize(g, 14);

		int sizeX = 0;
		for (int i = 0; i < text.length; i++)
		{
			sizeX = Math.max(sizeX, (int) Math.round(g.getFont().getStringBounds(text[i], frc).getWidth() / interfaceScale) + xPadding);
		}

		int sizeY = 14;

		double drawX = x + sizeX / 2 + xPadding;
		double drawY = y + sizeY / 2 + yPadding * text.length;

		g.setColor(new Color(0, 0, 0, 127));
		Drawing.fillInterfaceRect(g, drawX, drawY, sizeX + xPadding * 2, sizeY + yPadding * 2 * text.length);

		g.setColor(Color.WHITE);
		for (int i = 0; i < text.length; i++)
		{
			Drawing.drawUncenteredInterfaceText(g, x + xPadding, y + yPadding * (2 * i + 1) + 14, text[i]);
		}

		//return (y - (drawY / Window.scale + sizeY + yPadding / Window.scale * 2));
	}

	public static void playSound(String sound)
	{
		Drawing.pendingSounds.add(sound);
	}

	public double getMouseX()
	{
		//return (MouseInfo.getPointerInfo().getLocation().getX() - getPlayerOffsetX() / Window.scale - this.getLocation().getX() - Math.max(0, Panel.windowWidth - Window.sizeX * Window.scale) / 2) / scale + mouseXoffset / Window.scale;
		//return (MouseInfo.getPointerInfo().getLocation().getX() - getPlayerOffsetX() / Window.scale - this.getLocation().getX()) / scale - Math.max(Panel.windowWidth - Window.interfaceSizeX * Window.interfaceScale, 0) / Window.scale / 2 - xOffset / Window.interfaceScale;
		////return (interfaceScale / scale * (getInterfaceMouseX()) - getPlayerOffsetX() /*+ Math.max(0, Panel.windowWidth  - Window.xOffset - Window.interfaceSizeX * Window.interfaceScale) / 2*/);
		//return x1 / scale * 2 - Math.max(0, Panel.windowWidth - Window.xOffset - Window.sizeX * Window.scale) - getPlayerOffsetX();
		if (Drawing.enableMovingCamera && Drawing.movingCamera && Drawing.enableMovingCameraX)		
			return (interfaceScale / scale * ((MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX()) / interfaceScale) - getPlayerOffsetX() + mouseXoffset - Drawing.xOffset / Drawing.scale/*+ Math.max(0, Panel.windowWidth  - Window.xOffset - Window.interfaceSizeX * Window.interfaceScale) / 2*/);
		else
			//return (interfaceScale / scale * (getInterfaceMouseX()) - getPlayerOffsetX() /*+ Math.max(0, Panel.windowWidth  - Window.xOffset - Window.interfaceSizeX * Window.interfaceScale) / 2*/);
			return ((MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX()) - Math.max(0, Panel.windowWidth - Drawing.xOffset - Drawing.sizeX * Drawing.scale) / 2) / scale - getPlayerOffsetX() - Drawing.xOffset / Drawing.scale;
	}

	public double getMouseY()
	{
		//return ((MouseInfo.getPointerInfo().getLocation().getY() - getPlayerOffsetY() / Window.scale - this.getLocation().getY() - Math.max(0, Panel.windowHeight - (yOffset + 1) - Window.sizeY * Window.scale) / 2)) / scale + mouseYoffset / Window.scale;
		//return (MouseInfo.getPointerInfo().getLocation().getY() - getPlayerOffsetY() / Window.scale - this.getLocation().getY()) / scale - Math.max(Panel.windowHeight - Window.interfaceSizeY * Window.interfaceScale, 0) / Window.scale / 2 - (yOffset + 1) / Window.interfaceScale;
		////return (interfaceScale / scale * (getInterfaceMouseY()) - getPlayerOffsetY() /*+ Math.max(0, Panel.windowHeight - statsHeight - Window.yOffset - Window.interfaceSizeY * Window.interfaceScale) / 2*/);
		//return y1 / scale * 2 - Math.max(0, Panel.windowHeight - statsHeight - Window.yOffset - Window.sizeY * Window.scale) - getPlayerOffsetY();
		if (Drawing.enableMovingCamera && Drawing.movingCamera && Drawing.enableMovingCameraY)
			return (interfaceScale / scale * (((MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY())) / interfaceScale) - getPlayerOffsetY() + mouseYoffset - Drawing.yOffset / Drawing.scale/*+ Math.max(0, Panel.windowHeight - statsHeight - Window.yOffset - Window.interfaceSizeY * Window.interfaceScale) / 2*/);
		else
			//return (interfaceScale / scale * (getInterfaceMouseY()) - getPlayerOffsetY() /*+ Math.max(0, Panel.windowHeight - statsHeight - Window.yOffset - Window.interfaceSizeY * Window.interfaceScale) / 2*/);
			return ((MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY()) - Math.max(0, Panel.windowHeight - Drawing.yOffset - statsHeight - Drawing.sizeY * Drawing.scale) / 2) / scale - getPlayerOffsetY() - Drawing.yOffset / Drawing.scale;

	}

	public double getInterfaceMouseX()
	{
		try
		{
			return (MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX() - Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2) / interfaceScale + mouseXoffset / Drawing.interfaceScale;
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	public double getInterfaceMouseY()
	{
		try
		{
			return ((MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY() - Math.max(0, Panel.windowHeight - (yOffset + 1) - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2)) / interfaceScale + mouseYoffset / Drawing.interfaceScale;
		}
		catch (Exception e)
		{
			return 0;
		}
	}

	public void setScreenSize(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		this.setSize((int) (x * scale), (int) ((y + yOffset) * scale));
	}

	public void setScreenBounds(int x, int y)
	{
		sizeX = x;
		sizeY = y;
		Game.currentSizeX = x / Game.tank_size;
		Game.currentSizeY = y / Game.tank_size;
	}

	public static double getPlayerOffsetX()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraX)
			return 0;

		double result = (playerX - (Panel.windowWidth) / Drawing.scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowWidth - Drawing.xOffset) / Drawing.scale > Drawing.sizeX)
			return 0 - (Drawing.sizeX - (Panel.windowWidth - Drawing.xOffset) / Drawing.scale);
		else
			return 0 - result;
	}

	public static double getPlayerOffsetY()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraY)
			return 0;

		double result = (playerY - Panel.windowHeight / Drawing.scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowHeight - statsHeight - Drawing.yOffset) / Drawing.scale > Drawing.sizeY)
			return 0 - (Drawing.sizeY - (Panel.windowHeight - statsHeight - Drawing.yOffset) / Drawing.scale);
		else
			return 0 - result;
	}
}

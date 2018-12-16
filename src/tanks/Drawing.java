package tanks;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
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

	public double scale = 1;
	public static double unzoomedScale = 1;

	public static double interfaceScale = 1;

	public static boolean enableMovingCamera = false;
	public static boolean enableMovingCameraX = false;
	public static boolean enableMovingCameraY = false;

	public static int statsHeight = 40;
	
	public static boolean movingCamera = false;

	public static Drawing window = new Drawing();

	public static int mouseXoffset = 0;
	public static int mouseYoffset = 0;

	public static FontRenderContext frc = new FontRenderContext(null, true, true);
	public static ArrayList<String> pendingSounds = new ArrayList<String>();

	private Drawing()
	{
		this.addMouseListener(new InputMouse());
		this.addKeyListener(new InputKeyboard());
		this.addMouseWheelListener(new InputScroll());
		this.addMouseMotionListener(new InputMouse());

		//this.setSize((int)(sizeX * scale), (int) ((sizeY + yOffset) * scale ));
		
		Container contentPane = this.getContentPane();
		Panel.panel.setPreferredSize(new Dimension(sizeX, sizeY + statsHeight));
		contentPane.add(Panel.panel);
		contentPane.setSize(sizeX, sizeY + statsHeight);

		this.pack();
		this.setVisible(true);
		
		this.setResizable(true);
		this.setMinimumSize(new Dimension(350, 265));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void initializeMouseOffsets()
	{
		mouseXoffset = 0 - (int) ((Drawing.window.getSize().getWidth() - Panel.panel.getSize().getWidth()) / 2);
		mouseYoffset = 1 - (int) ((Drawing.window.getSize().getHeight() - Panel.panel.getSize().getHeight()) / 2);
		
		if (System.getProperties().getProperty("os.name").toLowerCase().contains("mac"))
		{
			mouseYoffset += 6;
		}
	}
	
	public void setScale(double scale)
	{
		this.scale = scale;
	}
	
	public double getScale() 
	{
		return this.scale;
	}

	/*@Override
	public void setSize(int x, int y)
	{
		Panel.panel.setPreferredSize(new Dimension(x, (int) (y + statsHeight)));
		this.getContentPane().remove(Panel.panel);
		this.getContentPane().add(Panel.panel);
		this.pack();
	}*/

	public void fillOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.sizeX * this.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * this.scale) / 2);

		if (drawX - 200 * this.scale > Panel.windowWidth || drawX + 200 * this.scale < 0 || drawY - 200 * this.scale > Panel.windowHeight || drawY + 200 * this.scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void fillForcedOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.sizeX * this.scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * this.scale) / 2);

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.sizeX * scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.sizeX * scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void fill3DRect(Graphics g, double x, double y, double sizeX, double sizeY, boolean raised)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.sizeX * scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fill3DRect(drawX, drawY, drawSizeX, drawSizeY, raised);
	}

	public void fillBackgroundRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.sizeX * scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);
		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(scale * (x + getPlayerOffsetX() - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.sizeX * scale) / 2);
		int drawY = (int) Math.round(scale * (y + getPlayerOffsetY() - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);

		if (drawX - 200 * scale > Panel.windowWidth || drawX + 200 * scale < 0 || drawY - 200 * scale > Panel.windowHeight || drawY + 200 * scale < 0)
			return;

		int drawSizeX = (int) Math.round(sizeX * scale);
		int drawSizeY = (int) Math.round(sizeY * scale);

		g.drawRect(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.fillOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void drawInterfaceOval(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth  - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.drawOval(drawX, drawY, drawSizeX, drawSizeY);
	}

	public void fillInterfaceRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void fillInterfaceProgressRect(Graphics g, double x, double y, double sizeX, double sizeY, double progress)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale * progress);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.fillRect(drawX, drawY, drawSizeX, drawSizeY);
	}
	
	public void drawInterfaceImage(Graphics g, Image img, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.drawImage(img, drawX, drawY, drawSizeX, drawSizeY, null);
	}

	public void drawInterfaceRect(Graphics g, double x, double y, double sizeX, double sizeY)
	{
		int drawX = (int) Math.round(interfaceScale * (x - sizeX / 2) + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) Math.round(interfaceScale * (y - sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		int drawSizeX = (int) Math.round(sizeX * interfaceScale);
		int drawSizeY = (int) Math.round(sizeY * interfaceScale);

		g.drawRect(drawX , drawY, drawSizeX, drawSizeY);
	}

	public void drawText(Graphics g, double x, double y, String text)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());

		double sizeY = g.getFont().getSize() / 3 / scale;
		//int size = text.length() * g.getFont().getSize() / 2;
		int drawX = (int) (scale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - Drawing.sizeX * scale) / 2);
		int drawY = (int) (scale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.sizeY * scale) / 2);
		g.drawString(text, drawX, drawY);

		//g.setColor(Color.red);
		//g.drawRect((int)((x - sizeX / 2) * scale), (int)((y - sizeY * 3 / 2) * scale), (int)g.getFont().getStringBounds(text, frc).getWidth(), (int)g.getFont().getStringBounds(text, frc).getHeight());
	}

	public void drawInterfaceText(Graphics g, double x, double y, String text)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());

		double sizeY = g.getFont().getSize() / 3 / Drawing.interfaceScale;
		int drawX = (int) (interfaceScale * x - sizeX / 2 + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) (interfaceScale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		g.drawString(text, drawX, drawY);
	}

	public void drawInterfaceText(Graphics g, double x, double y, String text, boolean rightAligned)
	{
		double sizeX = Math.round(g.getFont().getStringBounds(text, frc).getWidth());

		double sizeY = g.getFont().getSize() / 3 / Drawing.interfaceScale;
		double offX = sizeX;
		
		if (!rightAligned)
			offX = 0;
		
		int drawX = (int) (interfaceScale * x - offX + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) (interfaceScale * (y + sizeY / 2) + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		g.drawString(text, drawX, drawY);
	}
	
	public void drawUncenteredInterfaceText(Graphics g, double x, double y, String text)
	{
		int drawX = (int) (interfaceScale * x + Math.max(0, Panel.windowWidth - Drawing.interfaceSizeX * Drawing.interfaceScale) / 2);
		int drawY = (int) (interfaceScale * y + Math.max(0, Panel.windowHeight  - statsHeight - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2);
		g.drawString(text, drawX, drawY);
	}

	public static void setFontSize(Graphics g, double size)
	{
		g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (size * Drawing.window.scale)));
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
		Drawing.window.fillInterfaceRect(g, drawX, drawY, sizeX + xPadding * 2, sizeY + yPadding * 2 * text.length);

		g.setColor(Color.WHITE);
		for (int i = 0; i < text.length; i++)
		{
			Drawing.window.drawUncenteredInterfaceText(g, x + xPadding, y + yPadding * (2 * i + 1) + 14, text[i]);
		}

		//return (y - (drawY / Window.scale + sizeY + yPadding / Window.scale * 2));
	}

	public static void playSound(String sound)
	{
		Drawing.pendingSounds.add(sound);
	}
	
	public double toGameCoordsX(double x)
	{
		double x1 = x;
		
		if (enableMovingCamera && movingCamera && enableMovingCameraX)
			x1 += (Panel.panel.getSize().getWidth() - Drawing.interfaceScale * Drawing.interfaceSizeX) / 2 / Drawing.interfaceScale;
		
		double rawX = interfaceScale * (x1);

		rawX -= (1400 - Drawing.sizeX * scale / interfaceScale) / 2 * interfaceScale;

		double gameX = (rawX) / scale - getPlayerMouseOffsetX();	
		
		return gameX;
	}
	
	public double toGameCoordsY(double y)
	{
		double y1 = y;

		if (enableMovingCamera && movingCamera && enableMovingCameraX)
			y1 += (Panel.panel.getSize().getHeight() - Drawing.interfaceScale * Drawing.interfaceSizeY - Drawing.statsHeight) / 2 / Drawing.interfaceScale;
		
		double rawY = interfaceScale * (y1);
		
		rawY -= (900 - Drawing.sizeY * scale / interfaceScale) / 2 * interfaceScale;

		double gameY = (rawY) / scale - getPlayerMouseOffsetY();
		
		return gameY;
	}

	public double getMouseX()
	{
		return toGameCoordsX(getInterfaceMouseX());
	}

	public double getMouseY()
	{
		return toGameCoordsY(getInterfaceMouseY());
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
			return ((MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY() - Math.max(0, Panel.windowHeight - (1) - Drawing.interfaceSizeY * Drawing.interfaceScale) / 2)) / interfaceScale + mouseYoffset / Drawing.interfaceScale;
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
		this.setSize((int) (x * scale), (int) ((y) * scale));
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

		double result = (playerX - (Panel.windowWidth) / Drawing.window.scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowWidth) / Drawing.window.scale > Drawing.sizeX)
			return 0 - (Drawing.sizeX - (Panel.windowWidth) / Drawing.window.scale);
		else
			return 0 - result;
	}

	public static double getPlayerOffsetY()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraY)
			return 0;

		double result = (playerY - Panel.windowHeight / Drawing.window.scale / 2);

		if (result < 0)
			return 0;
		else if (result + (Panel.windowHeight - statsHeight) / Drawing.window.scale > Drawing.sizeY)
			return 0 - (Drawing.sizeY - (Panel.windowHeight - statsHeight) / Drawing.window.scale);
		else
			return 0 - result;
	}
	
	public static double getPlayerMouseOffsetX()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraX)
			return 0;

		return getPlayerOffsetX() + Drawing.interfaceSizeX / 2;
	}

	public static double getPlayerMouseOffsetY()
	{
		if (!enableMovingCamera || !movingCamera || !enableMovingCameraY)
			return 0;

		return getPlayerOffsetY() + Drawing.interfaceSizeY / 2;
	}
}

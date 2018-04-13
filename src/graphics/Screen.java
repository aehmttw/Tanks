package graphics;
import java.awt.Container;
import java.awt.MouseInfo;
import javax.swing.*;

@SuppressWarnings("serial")
public class Screen extends JFrame 
{
	public static final int sizeX = 1400;//1920;
	public static final int sizeY = 900;//1100;

	public Panel panel = new Panel();
	
	public static Screen screen;
	
	public Screen()
	{
		this.addMouseListener(new MouseInputListener());
		this.addKeyListener(new KeyInputListener());
		this.setSize(sizeX, sizeY + 22);
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
		return this.WIDTH;
	}
	
	@SuppressWarnings("static-access")
	public int getSizeY()
	{
		return this.HEIGHT;
	}
	
	public double getMouseX()
	{
		return MouseInfo.getPointerInfo().getLocation().getX() - this.getLocation().getX();
	}
	
	public double getMouseY()
	{
		return MouseInfo.getPointerInfo().getLocation().getY() - this.getLocation().getY() - 23;
	}
}

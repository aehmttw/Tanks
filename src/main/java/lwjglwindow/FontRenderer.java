package lwjglwindow;

import basewindow.BaseFontRenderer;
import org.lwjgl.opengl.GL11;

public class FontRenderer extends BaseFontRenderer
{
	String chars;
	int[] charSizes;
	String image;
			
	public FontRenderer(LWJGLWindow h, String fontFile)
	{
		super(h);
		this.chars = " !\"#$%&'()*+,-./" +
				"0123456789:;<=>?" +
				"@ABCDEFGHIJKLMNO" +
				"PQRSTUVWXYZ[\\]^_" +
				"'abcdefghijklmno" +
				"pqrstuvwxyz{|}~`";
		this.charSizes = new int[]
				{
						3, 2, 4, 5, 5, 6, 5, 2, 3, 3, 4, 5, 1, 5, 1, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
						7, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
						2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
						5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2
				};
		this.image = fontFile;
	}
	
	protected int drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
	{
		int i = this.chars.indexOf(c);
		
		if (i == -1)
			i = 31;
		
		int col = i % 16;
		int row = i / 16;
		int width = charSizes[i];
		this.home.drawImage(x, y, z, sX, sY - 0.0001, col / 16f, (row * 2) / 16f, (col + width / 8f) / 16f, (row * 2 + 1) / 16f, image, true, depthtest);
		return width;
	}
	
	public void drawString(double x, double y, double z, double sX, double sY, String s)
	{
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		double curX = x;
		char[] c = s.toCharArray();
				
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
			{
				int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
				int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
				int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
				int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
				this.home.setColor(r, g, b, a);
				
				i += 12;
			}
			else
				curX += (drawChar(curX, y, z, sX, sY, c[i], true) + 1) * sX * 4;
		}

		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	public void drawString(double x, double y, double sX, double sY, String s)
	{
		double curX = x;
		char[] c = s.toCharArray();

		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
			{
				int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
				int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
				int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
				int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
				this.home.setColor(r, g, b, a);

				i += 12;
			}
			else
				curX += (drawChar(curX, y, 0, sX, sY, c[i], false) + 1) * sX * 4;
		}
	}
	
	public double getStringSizeX(double sX, String s)
	{
		double w = 0;
		char[] c = s.toCharArray();
				
		for (int i = 0; i < c.length; i++)
		{
			if (c[i] == '\u00C2')
				continue;
			else if (c[i] == '\u00A7')
				i += 12;
			else if (this.chars.indexOf(c[i]) == -1)
				c[i] = '?';
			else
				w += (charSizes[this.chars.indexOf(c[i])] + 1) * sX * 4;
		}
		
		return w;
	}
	
	public double getStringSizeY(double sY, String s)
	{
		return (sY * 32);
	}
}

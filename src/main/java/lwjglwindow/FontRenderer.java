package lwjglwindow;

public class FontRenderer 
{
	public LWJGLWindow home;
	String chars; 
	int[] charSizes;
	String image;
			
	public FontRenderer(LWJGLWindow h, String fontFile)
	{
		this.home = h;
		this.chars = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~`";
		this.charSizes = new int[]
				{
						3, 2, 4, 5, 5, 5, 5, 2, 4, 4, 4, 5, 1, 5, 1, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
						6, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
						5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
						2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
						5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2
				};
		this.image = fontFile;
	}
	
	public int drawChar(double x, double y, double sX, double sY, char c)
	{
		int i = this.chars.indexOf(c);
		
		if (i == -1)
			i = 31;
		
		int col = i % 16;
		int row = i / 16;
		int width = charSizes[i];
		this.home.drawImage(x, y, sX, sY - 0.0001, col / 16f, row / 16f, (col + width / 8f) / 16f, (row + 1) / 16f, image, true);
		return width;
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
				curX += (drawChar(curX, y, sX, sY, c[i]) + 1) * sX * 4;
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

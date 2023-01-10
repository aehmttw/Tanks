package lwjglwindow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ExperimentalFontRenderer
{
    protected boolean[] grid = new boolean[4];

    public ExperimentalFontRenderer(String image)
    {

        // loop through all positions in char
        // when find one with count > 0, start tracing
        //


    }

    public void checkCharacter(BufferedImage img, int startX, int startY, int endX, int endY)
    {
        boolean[][] explored = new boolean[endX - startX][endY - startY];
        for (int x = startX; x < endX; x++)
        {
            boolean before = false;
            for (int y = startY; y < endY; y++)
            {
                if (((img.getRGB(x, y) >>> 24) & 0xff) != 0)
                {
                    if (!before)
                    {
                        int loopX = x - 1;
                        int loopY = y - 1;

                        int prevLoopX = x - 1;
                        int prevLoopY = y - 2;

                        do
                        {
                            boolean[] corners = getCorner(img, loopX, loopY);

                            boolean minusY = corners[0] ^ corners[1];
                            boolean plusX = corners[1] ^ corners[2];
                            boolean plusY = corners[2] ^ corners[3];
                            boolean minusX = corners[3] ^ corners[0];

                            explored[loopX - startX][loopY - startY] = true;

                            if (minusX && prevLoopX != loopX - 1)
                            {
                                prevLoopX = loopX;
                                loopX--;
                            }
                            else if (minusY && prevLoopY != loopY - 1)
                            {
                                prevLoopY = loopY;
                                loopY--;
                            }
                            else if (plusX && prevLoopX != loopX + 1)
                            {
                                prevLoopX = loopX;
                                loopX++;
                            }
                            else if (plusY && prevLoopY != loopY + 1)
                            {
                                prevLoopY = loopY;
                                loopY++;
                            }
                        }
                        while (!(loopX == x && loopY == y));
                    }

                    before = true;
                }
                else
                    before = false;
            }
        }
    }

    public boolean[] getCorner(BufferedImage img, int x, int y)
    {
        grid[0] = (x >= 0 && y >= 0) && (((img.getRGB(x, y) >>> 24) & 0xff) != 0);
        grid[1] = (x + 1 >= 0 && y >= 0) && (((img.getRGB(x + 1, y) >>> 24) & 0xff) != 0);
        grid[2] = (x + 1 >= 0 && y + 1 >= 0) && (((img.getRGB(x + 1, y + 1) >>> 24) & 0xff) != 0);
        grid[3] = (x >= 0 && y + 1 >= 0) && (((img.getRGB(x, y + 1) >>> 24) & 0xff) != 0);

        return grid;
    }
}

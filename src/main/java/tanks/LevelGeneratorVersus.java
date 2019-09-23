package tanks;

import tanks.gui.screen.ScreenPartyHost;

import java.util.ArrayList;

public class LevelGeneratorVersus
{
	public static String generateLevelString() 
	{
		//int type = (int) (Math.random() * 13);
		//test ^
		//String name = Game.registryTank.getRandomTank().name;
		double size = Game.levelSize;

		int height = (int)(18 * size);
		int width = (int)(28 * size);
		double amountWalls = 12 * size * size;
		double amountTanks = 8 * size * size;

		double random = Math.random();
		int walls = (int) (random * amountWalls + 4);

		int vertical = 2;
		int horizontal = 2;

		int r = (int)(Math.random() * 50) + 185;
		int g = (int)(Math.random() * 50) + 185;
		int b = (int)(Math.random() * 50) + 185;

		boolean bouncy = Math.random() < 0.2;
		double bouncyWeight = Math.random() * 0.5 + 0.2;

		boolean shrubs = Math.random() < 0.2;
		int shrubCount = (int) (walls + Math.random() * 4 - 2);

		boolean teleporters = Math.random() < 0.2;
		int numTeleporters = walls / 5 + 2;

		StringBuilder s = new StringBuilder("{" + width + "," + height + "," + r + "," + g + "," + b + ",20,20,20|");

		boolean[][] cells = new boolean[width][height];
		double[][] cellWeights = new double[width][height];

		ArrayList<Integer[]> startPointsH = new ArrayList<Integer[]>();
		ArrayList<Integer[]> startPointsV = new ArrayList<Integer[]>();

		for (int i = 0; i < width; i++) 
		{
			for (int j = 0; j < height; j++) 
			{
				cellWeights[i][j] = 1;
			}
		}

		for (int i = 0; i < walls; i++) 
		{
			int x = 0;
			int y = 0;
			int xEnd = 0;
			int yEnd = 0;
			int l = 1 + (int) Math.max(1, (Math.random() * (Math.min(height, width) - 3)));

			String type = "";

			if (bouncy && Math.random() < bouncyWeight)
				type = "-bouncy";
			else if (Math.random() < 0.5)
				type = "-hard";
			else if (Math.random() < 0.25)
				type = "-hole";


			if (Math.random() * (vertical + horizontal) < horizontal) 
			{
				vertical++;
				int rand;
				Integer[] sp = null;

				if (Math.random() < 0.25 || startPointsH.isEmpty())
				{
					for (int in = 0; in < 50; in++)
					{
						boolean chosen = false;

						while (!chosen)
						{
							x = (int) (Math.random() * (width - l));
							y = (int) (Math.random() * (height));
							xEnd = x + l;
							yEnd = y;

							double weight = 0;
							for (int x1 = x; x1 <= xEnd; x1++)
							{
								weight += cellWeights[x1][y];
							}
							weight /= (xEnd - x + 1);

							if (Math.random() < weight)
								chosen = true;
						}

						boolean stop = false;

						for (int x1 = x - 2; x1 <= xEnd + 2; x1++)
						{
							for (int y1 = y - 2; y1 <= yEnd + 2; y1++)
							{
								if (cells[Math.max(0, Math.min(width-1, x1))][Math.max(0, Math.min(height-1, y1))])
								{
									stop = true;
									break;
								}
							}

							if (stop)
								break;
						}

						if (!stop)
							break;
					}
				}
				else
				{
					rand = (int) (Math.random() * startPointsH.size());
					x = startPointsH.get(rand)[0] + 1;
					y = startPointsH.get(rand)[1];
					xEnd = x + l + 1;
					yEnd = y;
					sp = startPointsH.remove(rand);

					if ((Math.random() < 0.5 && x > 1) || x >= width)
					{
						xEnd -= l + 2;
						x -= l + 2;
					}
				}

				x = Math.max(x, 0);
				xEnd = Math.min(xEnd, width - 1);

				if (sp == null || sp[0] != x || sp[1] != y)
					startPointsV.add(new Integer[]{x, y});

				if (sp == null || sp[0] != xEnd || sp[1] != yEnd)
					startPointsV.add(new Integer[]{xEnd, yEnd});

				boolean started = false;
				boolean stopped = false;

				for (int z = x; z <= xEnd; z++)
				{
					if (!cells[z][y])
					{
						if (!started)
						{
							if (stopped)
							{
								s.append("-").append(y);

								s.append(type);

								s.append(",");
								stopped = false;
							}

							s.append(z).append("...");
							started = true;
						}
					}
					else
					{
						if (started)
						{
							started = false;
							stopped = true;
							s.append(z - 1);
						}
					}
				}

				if (started)
				{
					s.append(xEnd);
				}

				if (started || stopped)
				{
					s.append("-").append(y);

					s.append(type);

				}

				for (int j = x; j <= xEnd; j++) 
				{
					cells[j][y] = true;
				}

				for (int j = Math.max(0, x - 5); j <= Math.min(xEnd + 5, width - 1); j++) 
				{
					for (int k = Math.max(0, y - 5); k <= Math.min(yEnd + 5, height - 1); k++) 
					{
						cellWeights[j][k] /= 2;
					}
				}
			}
			else 
			{
				horizontal++;
				int rand;
				Integer[] sp = null;

				if (Math.random() < 0.25 || startPointsV.isEmpty())
				{		
					for (int in = 0; in < 50; in++)
					{
						boolean chosen = false;

						while (!chosen)
						{
							x = (int) (Math.random() * (width));
							y = (int) (Math.random() * (height - l));
							xEnd = x;
							yEnd = y + l;

							double weight = 0;
							for (int y1 = y; y1 <= yEnd; y1++)
							{
								weight += cellWeights[x][y1];
							}
							weight /= (yEnd - y + 1);

							if (Math.random() < weight)
								chosen = true;
						}

						boolean stop = false;

						for (int x1 = x - 2; x1 <= xEnd + 2; x1++)
						{
							for (int y1 = y - 2; y1 <= yEnd + 2; y1++)
							{
								if (cells[Math.max(0, Math.min(width - 1, x1))][Math.max(0, Math.min(height - 1, y1))])
								{
									stop = true;
									break;
								}
							}

							if (stop)
								break;
						}

						if (!stop)
							break;
					}
				}
				else
				{
					rand = (int) (Math.random() * startPointsV.size());
					x = startPointsV.get(rand)[0];
					y = startPointsV.get(rand)[1] + 1;
					xEnd = x;
					yEnd = y + l + 1;
					sp = startPointsV.remove(rand);

					if ((Math.random() < 0.5 && y > 1) || y >= height)
					{
						yEnd -= l + 2;
						y -= l + 2;
					}
				}

				y = Math.max(y, 0);
				yEnd = Math.min(yEnd, height - 1);

				if (sp == null || sp[0] != x || sp[1] != y)
					startPointsH.add(new Integer[]{x, y});

				if (sp == null || sp[0] != xEnd || sp[1] != yEnd)
					startPointsH.add(new Integer[]{xEnd, yEnd});

				boolean started = false;
				boolean stopped = false;

				for (int z = y; z <= yEnd; z++)
				{
					if (!cells[x][z])
					{
						if (!started)
						{
							if (stopped)
							{
								s.append(type);

								s.append(",");
								stopped = false;
							}

							s.append(x).append("-").append(z).append("...");
							started = true;
						}
					}
					else
					{
						if (started)
						{
							s.append(z - 1);
							started = false;
							stopped = true;
						}
					}
				}

				if (started)
				{
					s.append(yEnd);
				}

				if (started || stopped)
				{
					s.append(type);
				}

				for (int j = y; j <= yEnd; j++) 
				{
					cells[x][j] = true;
				}

				for (int j = Math.max(0, x - 5); j <= Math.min(xEnd + 5, width - 1); j++) 
				{
					for (int k = Math.max(0, y - 5); k <= Math.min(yEnd + 5, height - 1); k++) 
					{
						cellWeights[j][k] /= 2;
					}
				}
			}

			if (i < walls - 1)
			{
				if (!s.toString().endsWith(","))
					s.append(",");
			}
		}

		if (shrubs)
		{
			for (int j = 0; j < shrubCount; j++)
			{
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);


				for (int i = 0; i < Math.random() * 20 + 4; i++)
				{						
					if (x < width && y < height && x > 0 && y > 0 && !cells[x][y])
					{
						cells[x][y] = true;
						
						if (!s.toString().endsWith(","))
							s.append(",");
						
						s.append(x).append("-").append(y).append("-shrub");
					}
					
					double rand = Math.random();
					
					if (rand < 0.25)
						x++;
					else if (rand < 0.5)
						x--;
					else if (rand < 0.75)
						y++;
					else
						y--;
				}
			}
		}
		
		if (teleporters)
		{
			int n = numTeleporters;
			while (n > 0)
			{
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);

				if (!cells[x][y])
				{
					for (int i = Math.max(x - 2, 0); i <= Math.min(x + 2, width - 1); i++)
						for (int j = Math.max(y - 2, 0); j <= Math.min(y + 2, height - 1); j++)
							cells[i][j] = true;
					
					if (!s.toString().endsWith(","))
						s.append(",");
					
					s.append(x).append("-").append(y).append("-teleporter");
					n--;
				}
			}
		}

		s.append("|");

		int numTanks = ScreenPartyHost.server.connections.size() + 1;

		int x = (int) (Math.random() * (width));
		int y = (int) (Math.random() * (height));
		while (cells[x][y]) 
		{
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));
		}
		for (int i = -2; i <= 2; i++)
			for (int j = -2; j <= 2; j++)
				cells[Math.max(0, Math.min(width - 1, x+i))][Math.max(0, Math.min(height - 1, y+j))] = true;

		for (int i = 0; i < numTanks; i++)
		{
			int angle = (int) (Math.random() * 4);
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));
			while (cells[x][y])
			{
				x = (int) (Math.random() * (width));
				y = (int) (Math.random() * (height));
			}
			for (int a = -1; a <= 1; a++)
				for (int j = -2; j <= 1; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append("player");
			s.append("-").append(angle);

			if (i == numTanks - 1) 
			{
				s.append("|ally-true}");
			} 
			else 
			{
				s.append(",");
			}
		}		
 
		return s.toString();
	}
}
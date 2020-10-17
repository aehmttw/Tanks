package tanks;

import tanks.gui.screen.ScreenPartyHost;

import java.util.ArrayList;

public class LevelGenerator 
{
	public static String generateLevelString() 
	{
		//int type = (int) (Math.random() * 13);
		//test ^
		//String name = Game.registryTank.getRandomTank().name;
		double size = Game.levelSize;

		if (Math.random() < 0.2)
			size *= 2;

		int height = (int)(18 * size);
		int width = (int)(28 * size);
		double amountWalls = 12 * size * size;
		double amountTanks = 8 * size * size;

		double random = Math.random();
		int walls = (int) (random * amountWalls + 4);

		int vertical = 2;
		int horizontal = 2;

		int shade = 185;

		if (Math.random() < 0.2)
			shade = (int) (Math.random() * 60);

		int r = (int)(Math.random() * 50) + shade;
		int g = (int)(Math.random() * 50) + shade;
		int b = (int)(Math.random() * 50) + shade;

		double heavyTerrain = 1;

		if (Math.random() < 0.2)
			heavyTerrain *= 2;

		if (Math.random() < 0.2)
			heavyTerrain *= 2;

		if (Math.random() < 0.2)
			heavyTerrain *= 4;

		boolean bouncy = Math.random() < 0.2;
		double bouncyWeight = Math.random() * 0.5 + 0.2;

		boolean shrubs = Math.random() < 0.2;
		int shrubCount = (int) (walls + Math.random() * 4 - 2);

		boolean mud = Math.random() < 0.2;
		int mudCount = (int) (walls + Math.random() * 4 - 2);

		boolean ice = Math.random() < 0.2;
		int iceCount = (int) (walls + Math.random() * 4 - 2);

		boolean snow = Math.random() < 0.2;
		int snowCount = (int) (walls + Math.random() * 4 - 2);

		boolean teleporters = Math.random() < 0.2;
		int numTeleporters = walls / 5 + 2;
		int teleporterGroups = (int) ((numTeleporters - 1) * 0.5 * Math.random()) + 1;

		StringBuilder s = new StringBuilder("{" + width + "," + height + "," + r + "," + g + "," + b + ",20,20,20|");

		int[][] teleporterArray = new int[width][height];

		for (int i = 0; i < teleporterArray.length; i++)
		{
			for (int j = 0; j < teleporterArray[0].length; j++)
			{
				teleporterArray[i][j] = -1;
			}
		}

		boolean[][] solid = new boolean[width][height];
		int[] tankX;
		int[] tankY;
		int[] playerTankX;
		int[] playerTankY;

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
			boolean passable = true;

			if (bouncy && Math.random() < bouncyWeight)
				type = "-bouncy";
			else if (Math.random() < 0.5)
			{
				type = "-hard";
				passable = false;
			}
			else if (Math.random() < 0.25)
			{
				type = "-hole";
				passable = false;
			}


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

						cells[z][y] = true;
						solid[z][y] = solid[z][y] || !passable;
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

						cells[x][z] = true;
						solid[x][z] = solid[x][z] || !passable;
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


				for (int i = 0; i < (Math.random() * 20 + 4) * heavyTerrain; i++)
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

		if (mud)
		{
			for (int j = 0; j < mudCount; j++)
			{
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);


				for (int i = 0; i < (Math.random() * 20 + 4) * heavyTerrain; i++)
				{
					if (x < width && y < height && x > 0 && y > 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-mud");
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

		if (ice)
		{
			for (int j = 0; j < iceCount; j++)
			{
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);

				for (int i = 0; i < (Math.random() * 40 + 8) * heavyTerrain; i++)
				{
					if (x < width && y < height && x > 0 && y > 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-ice");
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

		if (snow)
		{
			for (int j = 0; j < snowCount; j++)
			{
				int x = (int) (Math.random() * width);
				int y = (int) (Math.random() * height);

				for (int i = 0; i < (Math.random() * 40 + 8) * heavyTerrain; i++)
				{
					if (x < width && y < height && x > 0 && y > 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-snow");
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
			int groupProgress = 0;

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

					int id = groupProgress / 2;

					if (n == 1)
						id = (groupProgress - 1) / 2;

					groupProgress++;

					if (id >= teleporterGroups)
						id = (int) (Math.random() * teleporterGroups);
					
					s.append(x).append("-").append(y).append("-teleporter");
					teleporterArray[x][y] = id;

					if (id != 0)
						s.append("-").append(id);

					n--;
				}
			}
		}

		s.append("|");

		int numTanks = (int) (random * amountTanks + 1);
		tankX = new int[numTanks];
		tankY = new int[numTanks];

		int x = (int) (Math.random() * (width));
		int y = (int) (Math.random() * (height));

		while (cells[x][y])
		{
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));
		}

		/*for (int i = -2; i <= 2; i++)
			for (int j = -2; j <= 2; j++)
				cells[Math.max(0, Math.min(width - 1, x+i))][Math.max(0, Math.min(height - 1, y+j))] = true;

		s.append(x).append("-").append(y).append("-player-").append((int) (Math.random() * 4)).append(",");*/

		int numPlayers = 1;

		if (ScreenPartyHost.isServer)
			numPlayers += ScreenPartyHost.server.connections.size();

		playerTankX = new int[numPlayers];
		playerTankY = new int[numPlayers];

		for (int i = 0; i < numPlayers; i++)
		{
			int angle = (int) (Math.random() * 4);
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));

			while (cells[x][y])
			{
				x = (int) (Math.random() * (width));
				y = (int) (Math.random() * (height));
			}

			int bound;

			if (numPlayers < 20)
				bound = 2;
			else if (numPlayers < 56)
				bound = 1;
			else
				bound = 0;

			for (int a = -bound; a <= bound; a++)
				for (int j = -bound; j <= bound; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append("player");
			s.append("-").append(angle);

			playerTankX[i] = x;
			playerTankY[i] = y;

			s.append(",");
		}

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
				for (int j = -1; j <= 1; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append(Game.registryTank.getRandomTank().name);
			s.append("-").append(angle);

			tankX[i] = x;
			tankY[i] = y;

			if (i == numTanks - 1) 
			{
				s.append("}");
			} 
			else 
			{
				s.append(",");
			}
		}

		ArrayList<Integer> currentX = new ArrayList<>();
		ArrayList<Integer> currentY = new ArrayList<>();

		for (int i = 0; i < numPlayers; i++)
		{
			currentX.add(playerTankX[i]);
			currentY.add(playerTankY[i]);
		}

		while (!currentX.isEmpty())
		{
			int posX = currentX.remove(0);
			int posY = currentY.remove(0);

			solid[posX][posY] = true;

			if (teleporterArray[posX][posY] >= 0)
			{
				int id = teleporterArray[posX][posY];

				for (int i = 0; i < width; i++)
				{
					for (int j = 0; j < height; j++)
					{
						if (teleporterArray[i][j] == id && !(posX == i && posY == j) && !solid[i][j])
						{
							currentX.add(i);
							currentY.add(j);
						}
					}
				}
			}

			if (posX > 0 && !solid[posX - 1][posY])
			{
				currentX.add(posX - 1);
				currentY.add(posY);
				solid[posX - 1][posY] = true;
			}

			if (posX < width - 1 && !solid[posX + 1][posY])
			{
				currentX.add(posX + 1);
				currentY.add(posY);
				solid[posX + 1][posY] = true;
			}

			if (posY > 0 && !solid[posX][posY - 1])
			{
				currentX.add(posX);
				currentY.add(posY - 1);
				solid[posX][posY - 1] = true;
			}

			if (posY < height - 1 && !solid[posX][posY + 1])
			{
				currentX.add(posX);
				currentY.add(posY + 1);
				solid[posX][posY + 1] = true;
			}
		}

		for (int i = 0; i < numTanks; i++)
		{
			if (!solid[tankX[i]][tankY[i]])
			{
				return LevelGenerator.generateLevelString();
			}
		}

		return s.toString();
	}
}
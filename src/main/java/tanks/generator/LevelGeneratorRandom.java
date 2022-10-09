package tanks.generator;

import tanks.Game;
import tanks.Level;
import tanks.gui.screen.ScreenPartyHost;

import java.util.ArrayList;
import java.util.Random;

public class LevelGeneratorRandom extends LevelGenerator
{
	public static String generateLevelString()
	{
		return generateLevelString(-1);
	}

	public static String generateLevelString(int seed)
	{
		Random random;

		if (seed != -1)
			random = new Random(seed);
		else
			random = new Random();

		double size = Game.levelSize;

		double randomNum = random.nextDouble();

		if (random.nextDouble() < 0.3)
			size *= 2;

		int height = (int)(18 * size);
		int width = (int)(28 * size);
		double amountWalls = 12 * size * size;
		double amountTanks = 8 * size * size;

		int walls = (int) (randomNum * amountWalls + 4);

		int vertical = 2;
		int horizontal = 2;

		int shade = 185;

		if (random.nextDouble() < 0.2)
			shade = (int) (random.nextDouble() * 60);

		int r = (int)(random.nextDouble() * 50) + shade;
		int g = (int)(random.nextDouble() * 50) + shade;
		int b = (int)(random.nextDouble() * 50) + shade;

		double heavyTerrain = 1;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 2;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 2;

		if (random.nextDouble() < 0.2)
			heavyTerrain *= 4;

		boolean bouncy = random.nextDouble() < 0.2;
		double bouncyWeight = random.nextDouble() * 0.5 + 0.2;

		boolean nobounce = random.nextDouble() < 0.2;
		double noBounceWeight = random.nextDouble() * 0.5 + 0.2;

		boolean shrubs = random.nextDouble() < 0.2;
		int shrubCount = (int) (walls + random.nextDouble() * 4 - 2);

		boolean mud = random.nextDouble() < 0.2;
		int mudCount = (int) (walls + random.nextDouble() * 4 - 2);

		boolean ice = random.nextDouble() < 0.2;
		int iceCount = (int) (walls + random.nextDouble() * 4 - 2);

		boolean snow = random.nextDouble() < 0.2;
		int snowCount = (int) (walls + random.nextDouble() * 4 - 2);

		boolean teleporters = random.nextDouble() < 0.1;
		int numTeleporters = walls / 5 + 2;
		int teleporterGroups = (int) ((numTeleporters - 1) * 0.5 * random.nextDouble()) + 1;

		boolean boostPanels = random.nextDouble() < 0.2;
		int boostCount = (int) (walls + random.nextDouble() * 4 - 2);

		boolean explosives = random.nextDouble() < 0.2;
		int numExplosives = (int) (walls / 5 + random.nextDouble() * 4 + 1);

		int time = (int) (randomNum * amountTanks + 4) * 5;

		if (random.nextDouble() > 0.2)
			time = 0;
		else
			time += 45 * (size / Game.levelSize - 1);

		double light = 100;
		double shadeFactor = 0.5;

		if (random.nextDouble() < 0.2)
		{
			light *= random.nextDouble() * 1.25;
		}

		boolean dark = light < 50;
		int numLights = (int) (walls / 5 + random.nextDouble() * 6 + 1);

		if (random.nextDouble() < 0.2)
			shadeFactor = random.nextDouble() * 0.6 + 0.2;

		StringBuilder s = new StringBuilder("{" + width + "," + height + "," + r + "," + g + "," + b + ",20,20,20," + time + "," + (int) light + "," + (int) (light * shadeFactor) + "|");

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

		ArrayList<Integer[]> startPointsH = new ArrayList<>();
		ArrayList<Integer[]> startPointsV = new ArrayList<>();

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
			int l = 1 + (int) Math.max(1, (random.nextDouble() * (Math.min(height, width) - 3)));

			String type = "";
			boolean passable = true;

			if (bouncy && random.nextDouble() < bouncyWeight)
				type = "-bouncy";
			else if (nobounce && random.nextDouble() < noBounceWeight)
			{
				type = "-nobounce";
				passable = false;
			}
			else if (random.nextDouble() < 0.5)
			{
				type = "-hard";
				passable = false;
			}
			else if (random.nextDouble() < 0.25)
			{
				type = "-hole";
				passable = false;
			}
			else if (random.nextDouble() < 0.25)
			{
				type = "-breakable";
				passable = true;
			}

			if (random.nextDouble() * (vertical + horizontal) < horizontal)
			{
				vertical++;
				int rand;
				Integer[] sp = null;

				if (random.nextDouble() < 0.25 || startPointsH.isEmpty())
				{
					for (int in = 0; in < 50; in++)
					{
						boolean chosen = false;

						while (!chosen)
						{
							x = (int) (random.nextDouble() * (width - l));
							y = (int) (random.nextDouble() * (height));
							xEnd = x + l;
							yEnd = y;

							double weight = 0;
							for (int x1 = x; x1 <= xEnd; x1++)
							{
								weight += cellWeights[x1][y];
							}
							weight /= (xEnd - x + 1);

							if (random.nextDouble() < weight)
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
					rand = (int) (random.nextDouble() * startPointsH.size());
					x = startPointsH.get(rand)[0] + 1;
					y = startPointsH.get(rand)[1];
					xEnd = x + l + 1;
					yEnd = y;
					sp = startPointsH.remove(rand);

					if ((random.nextDouble() < 0.5 && x > 1) || x >= width)
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

				if (random.nextDouble() < 0.25 || startPointsV.isEmpty())
				{
					for (int in = 0; in < 50; in++)
					{
						boolean chosen = false;

						while (!chosen)
						{
							x = (int) (random.nextDouble() * (width));
							y = (int) (random.nextDouble() * (height - l));
							xEnd = x;
							yEnd = y + l;

							double weight = 0;
							for (int y1 = y; y1 <= yEnd; y1++)
							{
								weight += cellWeights[x][y1];
							}
							weight /= (yEnd - y + 1);

							if (random.nextDouble() < weight)
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
					rand = (int) (random.nextDouble() * startPointsV.size());
					x = startPointsV.get(rand)[0];
					y = startPointsV.get(rand)[1] + 1;
					xEnd = x;
					yEnd = y + l + 1;
					sp = startPointsV.remove(rand);

					if ((random.nextDouble() < 0.5 && y > 1) || y >= height)
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
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				for (int i = 0; i < (random.nextDouble() * 20 + 4) * heavyTerrain; i++)
				{
					if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-shrub");
					}

					double rand = random.nextDouble();

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

		if (boostPanels)
		{
			for (int j = 0; j < boostCount; j++)
			{
				int x1 = (int) (random.nextDouble() * width);
				int y1 = (int) (random.nextDouble() * height);

				int panelSize = (int)(random.nextDouble() * 3) + 1;

				for (int x = x1; x < x1 + panelSize; x++)
				{
					for (int y = y1; y < y1 + panelSize; y++)
					{
						if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
						{
							cells[x][y] = true;

							if (!s.toString().endsWith(","))
								s.append(",");

							s.append(x).append("-").append(y).append("-boostpanel");
						}
					}
				}
			}
		}

		if (explosives)
		{
			for (int j = 0; j < numExplosives; j++)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-explosive");
				}
			}
		}

		if (dark && Game.framework != Game.Framework.libgdx)
		{
			for (int j = 0; j < numLights; j++)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
				{
					cells[x][y] = true;

					if (!s.toString().endsWith(","))
						s.append(",");

					s.append(x).append("-").append(y).append("-light-").append((int)(random.nextDouble() * 5 + 1) / 2.0);
				}
			}
		}

		if (mud)
		{
			for (int j = 0; j < mudCount; j++)
			{
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);


				for (int i = 0; i < (random.nextDouble() * 20 + 4) * heavyTerrain; i++)
				{
					if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-mud");
					}

					double rand = random.nextDouble();

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
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				for (int i = 0; i < (random.nextDouble() * 40 + 8) * heavyTerrain; i++)
				{
					if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-ice");
					}

					double rand = random.nextDouble();

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
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

				for (int i = 0; i < (random.nextDouble() * 40 + 8) * heavyTerrain; i++)
				{
					if (x < width && y < height && x >= 0 && y >= 0 && !cells[x][y])
					{
						cells[x][y] = true;

						if (!s.toString().endsWith(","))
							s.append(",");

						s.append(x).append("-").append(y).append("-snow");
					}

					double rand = random.nextDouble();

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
				int x = (int) (random.nextDouble() * width);
				int y = (int) (random.nextDouble() * height);

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
						id = (int) (random.nextDouble() * teleporterGroups);

					s.append(x).append("-").append(y).append("-teleporter");
					teleporterArray[x][y] = id;

					if (id != 0)
						s.append("-").append(id);

					n--;
				}
			}
		}

		s.append("|");

		int numTanks = (int) (randomNum * amountTanks + 1);
		tankX = new int[numTanks];
		tankY = new int[numTanks];

		int x = (int) (random.nextDouble() * (width));
		int y = (int) (random.nextDouble() * (height));

		while (cells[x][y])
		{
			x = (int) (random.nextDouble() * (width));
			y = (int) (random.nextDouble() * (height));
		}

		/*for (int i = -2; i <= 2; i++)
			for (int j = -2; j <= 2; j++)
				cells[Math.max(0, Math.min(width - 1, x+i))][Math.max(0, Math.min(height - 1, y+j))] = true;

		s.append(x).append("-").append(y).append("-player-").append((int) (random.nextDouble(() * 4)).append(",");*/

		int numPlayers = 1;

		if (ScreenPartyHost.isServer)
			numPlayers += ScreenPartyHost.server.connections.size();

		playerTankX = new int[numPlayers];
		playerTankY = new int[numPlayers];

		for (int i = 0; i < numPlayers; i++)
		{
			int angle = (int) (random.nextDouble() * 4);
			x = (int) (random.nextDouble() * (width));
			y = (int) (random.nextDouble() * (height));

			while (cells[x][y])
			{
				x = (int) (random.nextDouble() * (width));
				y = (int) (random.nextDouble() * (height));
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
			int angle = (int) (random.nextDouble() * 4);
			x = (int) (random.nextDouble() * (width));
			y = (int) (random.nextDouble() * (height));

			while (cells[x][y])
			{
				x = (int) (random.nextDouble() * (width));
				y = (int) (random.nextDouble() * (height));
			}

			for (int a = -1; a <= 1; a++)
				for (int j = -1; j <= 1; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;

			s.append(x).append("-").append(y).append("-");
			s.append(Game.registryTank.getRandomTank(random).name);
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

		currentX.add(playerTankX[0]);
		currentY.add(playerTankY[0]);

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
				return LevelGeneratorRandom.generateLevelString(seed);
			}
		}

		for (int i = 1; i < numPlayers; i++)
		{
			if (!solid[playerTankX[i]][playerTankY[i]])
			{
				return LevelGeneratorRandom.generateLevelString(seed);
			}
		}

		return s.toString();
	}
}